package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.mantra.mTerminal100.printer.Prints;
import com.visiontek.Mantra.Adapters.DailySalesListAdapter;
import com.visiontek.Mantra.Models.DATAModels.DailySalesListModel;
import com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails.SaleDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Util.toast;

public class DailySalesReportActivity extends AppCompatActivity implements PrinterCallBack {
    public SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    public String ACTION_USB_PERMISSION;
    public String date;
    public int flag_print;
    Calendar myCalendar;
    Button back, home, print, view;
    Context context;
    TextView edittext;
    ProgressDialog pd = null;
    private DailySalesReportActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<DailySalesListModel> data;
    SaleDetails saleDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily__sales__report);
        context = DailySalesReportActivity.this;
        mActivity = this;
        ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg),
                    context.getResources().getString(R.string.RD_Service));
            return;
        }

        initilisation();

        flag_print = 0;
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               flag_print = 0;
                date=edittext.getText().toString();

                if (!date.equals("dd/MM/yyyy") && date.length()>0) {
                    String sale = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                            "<SOAP-ENV:Envelope\n" +
                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                            "    <SOAP-ENV:Body>\n" +
                            "        <ns1:getDailyReport>\n" +
                            "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                            "            <from_date>" + date + "</from_date>\n" +
                            "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                            "        </ns1:getDailyReport>\n" +
                            "    </SOAP-ENV:Body>\n" +
                            "</SOAP-ENV:Envelope>";
                    if(networkConnected(context)) {
                        if (mp!=null) {
                            releaseMediaPlayer(context,mp);
                        }
                        if(L.equals("hi")){}else {
                            mp = mp.create(context, R.raw.c100075);
                            mp.start();
                        }
                        Util.generateNoteOnSD(context, "DailySaleReq.txt", sale);
                        hitURL(sale);
                    }else
                        show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection));
                } else {
                    show_error_box(context.getResources().getString(R.string.Please_Enter_date_in_Edit_text_to_view_Stock), context.getResources().getString(R.string.Enter_Date));
                }

            }
        });
        mTerminal100API = new MTerminal100API();
        mTerminal100API.initPrinterAPI(this, this);
        print.setEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            probe();
        } else {
            finish();
        }
        print.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (flag_print == 1) {
                    String app;
                    StringBuilder add = new StringBuilder();
                    String time = sdf1.format(new Date()).substring(0, 5);
                    String date = sdf1.format(new Date()).substring(6, 16);
                    int drBeansize =saleDetails.drBean.size();
                    for (int i = 0; i < drBeansize; i++) {
                        app = String.format("%-13s%-13s%-13s\n",
                                saleDetails.drBean.get(i).comm_name,
                                saleDetails.drBean.get(i).schemeName,
                                saleDetails.drBean.get(i).sale);
                        add.append(app);

                    }
                    String str1,str2,str3,str4,str5;
                    String[] str = new String[4];
                    if (L.equals("hi")){

                    str1 = context.getResources().getString(R.string.DAY_REPORT);
                        image(str1,"header.bmp",1);
                     str2=context.getResources().getString(R.string.Date)+" : " + date +"\n"+ context.getResources().getString(R.string.Time)+" :" + time + "\n"
                            +context.getResources().getString(R.string.Day_Report_Date)+" : " + date + "\n"
                            +context.getResources().getString(R.string.FPS_ID)+" : "+ dealerConstants.stateBean.statefpsId + "\n";

                     str3 =context.getResources().getString(R.string.commodity)+"      "+context.getResources().getString(R.string.scheme)+"       "+context.getResources().getString(R.string.sale);

                     str4 = String.valueOf(add);
                        image(str2+str3+str4,"body.bmp",0);

                     str5 = context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                            + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n";

                        image(str5,"tail.bmp",1);
                        str[0]="1";
                        str[1]="1";
                        str[2]="1";
                        str[3]="1";
                        checkandprint(str,1);
                    }else {

                         str1 = context.getResources().getString(R.string.DAY_REPORT)+"\n\n";
                         str2=   context.getResources().getString(R.string.Date)+"           : " + date +"\n"+
                                 context.getResources().getString(R.string.Time)+"           :" + time + "\n"
                                +context.getResources().getString(R.string.Day_Report_Date)+": " + date + "\n"
                                +context.getResources().getString(R.string.FPS_ID)+"         : "+ dealerConstants.stateBean.statefpsId + "\n"
                                + "-------------------------------\n";
                         str3 = String.format("%-13s%-13s%-13s\n",
                                 context.getResources().getString(R.string.commodity),
                                 context.getResources().getString(R.string.scheme),
                                 context.getResources().getString(R.string.sale))
                                + "-------------------------------\n";
                         str4 = String.valueOf(add);

                         str5 = "\n"+context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n\n\n";
                        str[0]="1";
                        str[1]=str1;
                        str[2]=str2+str3+str4;
                        str[3]=str5;
                        checkandprint(str,0);

                    }

                } else {
                    toast(context, context.getResources().getString(R.string.No_Data_to_Print));
                }
            }
        });


        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(context, HomeActivity.class);
                startActivity(home);
                finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage(context.getResources().getString(R.string.Do_you_want_to_cancel_Session));
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        });
                alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.No),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        myCalendar = Calendar.getInstance();


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        edittext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(DailySalesReportActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        back = findViewById(R.id.sale_back);
        home = findViewById(R.id.sale_home);
        print = findViewById(R.id.sale_print);
        view = findViewById(R.id.sale_view);
        edittext = findViewById(R.id.sale_date);
        String date = sdf1.format(new Date()).substring(6, 16);
        edittext.setText(date);
        toolbarInitilisation();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkandprint(String[] str, int i) {
        if (Util.batterylevel(context)|| Util.adapter(context)) {
            if (mp!=null) {
                releaseMediaPlayer(context,mp);
            }
            if(L.equals("hi")){}else {
                mp = mp.create(context, R.raw.c100191);
                mp.start();
            }
            es.submit(new TaskPrint(mTerminal100API,str,mActivity,context,i));
            finish();
        }else {
            show_error_box(context.getResources().getString(R.string.Battery_Msg),context.getResources().getString(R.string.Battery));
        }
    }
    private void image(String content, String name,int align) {
        try {
            Util.image(content,name,align);
        } catch (IOException e) {
            e.printStackTrace();
            show_error_box(e.toString(),"Image formation Error");
        }
    }
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edittext.setText(sdf.format(myCalendar.getTime()));
        System.out.println("+++++++++++++++++++++++"+edittext);

        //d = "11/03/2021";
    }

    private void hitURL(String sale) {
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Dealers), context.getResources().getString(R.string.Fetching_Dealers), true, false);
        XML_Parsing request = new XML_Parsing(context, sale, 5);
        request.setOnResultListener(new XML_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String isError, String msg, String ref, String flow, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (isError == null || isError.isEmpty()) {
                    show_error_box("Invalid Response from Server", "No Response");
                    return;
                }

                if (!isError.equals("00")) {
                    System.out.println("ERRORRRRRRRRRRRRRRRRRRRR");
                    show_error_box(msg, context.getResources().getString(R.string.Dealer_Details) + isError);

                } else {

                    saleDetails= (SaleDetails) object;
                    data = new ArrayList<>();
                    int drBeansize =saleDetails.drBean.size();
                    for (int i = 0; i < drBeansize; i++) {
                        data.add(new DailySalesListModel(saleDetails.drBean.get(i).comm_name,
                                saleDetails.drBean.get(i).schemeName,
                                saleDetails.drBean.get(i).sale));
                        /*
                        String sch = saleDetails.drBean.get(i).schemeName.trim();switch (sch) {
                            case "AAY":
                                data.add(new DataModel3(saleDetails.drBean.get(i).comm_name,
                                        saleDetails.drBean.get(i).total_cards, "0.0", "0.0", saleDetails.drBean.get(i).sale));
                                break;
                            case "SFY":
                                data.add(new DataModel3(saleDetails.drBean.get(i).comm_name, "0.0",
                                        saleDetails.drBean.get(i).total_cards, "0.0", saleDetails.drBean.get(i).sale));
                                break;
                            case "PHH-CGFSA":
                                data.add(new DataModel3(saleDetails.drBean.get(i).comm_name, "0.0", "0.0",
                                        saleDetails.drBean.get(i).total_cards, saleDetails.drBean.get(i).sale));
                                break;
                        }*/

                    }
                    adapter = new DailySalesListAdapter(context, data);
                    recyclerView.setAdapter(adapter);
                    flag_print = 1;
                }
            }
        });
        request.execute();
    }

    private void show_error_box(String msg, String title) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



    @Override
    public void OnOpen() {
        print.setEnabled(true);
        // btnConnect.setEnabled(false);
        Toast.makeText(context, context.getResources().getString(R.string.CONNECTED), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnOpenFailed() {
        print.setEnabled(false);
        if (mp != null) {
            releaseMediaPlayer(context, mp);
        }
        //btnConnect.setEnabled(true);
        if (L.equals("hi")) {
        } else {
            mp = mp.create(context, R.raw.c100078);
            mp.start();
            Toast.makeText(context, context.getResources().getString(R.string.Connection_Failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnClose() {
        print.setEnabled(false);
        // btnConnect.setEnabled(true);
        if (mUsbReceiver != null) {
            context.unregisterReceiver(mUsbReceiver);
        }

        // If Close is caused because the printer is turned off. Then you need to re-enumerate it here.
        probe();
    }

    @Override
    public void OnPrint(final int bPrintResult, final boolean bIsOpened) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Toast.makeText(context.getApplicationContext(), (bPrintResult == 0) ? getResources().getString(R.string.printsuccess) : getResources().getString(R.string.printfailed) + " " + Prints.ResultCodeToString(bPrintResult), Toast.LENGTH_SHORT).show();
                mActivity.print.setEnabled(bIsOpened);
            }
        });

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                probe();
                // btnConnect.performClick();
                Toast.makeText(context, context.getResources().getString(R.string.ConnectUSB), Toast.LENGTH_LONG).show();
                print.setEnabled(true);
                synchronized (this) {
                }
            }
        }
    };


    private void probe() {
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceList.size() > 0) {

            while (deviceIterator.hasNext()) {
// Here is if not while, indicating that I only want to support a device
                final UsbDevice device = deviceIterator.next();
                if ((device.getProductId() == 22304) && (device.getVendorId() == 1155)) {
                    // TODO Auto-generated method stub
                    PendingIntent mPermissionIntent = PendingIntent
                            .getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    if (!mUsbManager.hasPermission(device)) {
                        mUsbManager.requestPermission(device, mPermissionIntent);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(
                                ACTION_USB_PERMISSION);
                        context.registerReceiver(mUsbReceiver, filter);
                        Toast.makeText(getApplicationContext(),
                                context.getResources().getString(R.string.Permission_denied), Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.Connecting), Toast.LENGTH_SHORT).show();

                        print.setEnabled(false);
                        es.submit(new Runnable() {
                            @Override
                            public void run() {
                                mTerminal100API.printerOpenTask(mUsbManager, device, context);
                            }
                        });
                    }
                    //  });
                } else {
                    //  Toast.makeText(ConnectUSBActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void toolbarInitilisation() {
        TextView toolbarVersion = findViewById(R.id.toolbarVersion);
        TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
        TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
        TextView toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
        TextView toolbarActivity = findViewById(R.id.toolbarActivity);
        TextView toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
        TextView toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);

        String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
        System.out.println(appversion);
        toolbarVersion.setText("V" + appversion);

        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("DAILY SALES");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}
