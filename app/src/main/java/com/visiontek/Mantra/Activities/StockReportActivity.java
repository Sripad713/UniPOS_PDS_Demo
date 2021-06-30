package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
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
import android.widget.TextView;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.mantra.mTerminal100.printer.Prints;
import com.visiontek.Mantra.Adapters.StockListAdapter;
import com.visiontek.Mantra.Models.DATAModels.StockListModel;
import com.visiontek.Mantra.Models.ReportsModel.Stockdetails.StockDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class StockReportActivity extends AppCompatActivity implements PrinterCallBack {
    public SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    Button back, print;
    Context context;
    public String ACTION_USB_PERMISSION;
    public int flag_print;
    ProgressDialog pd = null;
    private StockReportActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<StockListModel> data;

    StockDetails stockDetails;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                probe();
                // btnConnect.performClick();
                print.setEnabled(true);
                synchronized (this) {
                }
            }
            }catch (Exception ex){
                Timber.tag("Stock-Broadcast-").e(ex.getMessage(),"");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock__report);
        context = StockReportActivity.this;
        try {

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg), context.getResources().getString(R.string.RD_Service));
            return;
        }

        initilisation();


        flag_print = 0;

        mActivity = this;
        ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        print.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
               if (flag_print==1) {

                   String app;
                   String time = sdf1.format(new Date()).substring(0, 5);
                   String date = sdf1.format(new Date()).substring(6, 16);
                   StringBuilder add = new StringBuilder();
                   int astockBeansize= stockDetails.astockBean.size();
                   for (int i = 0; i < astockBeansize; i++) {

                       app = String.format("%-6s%-6s%-6s%-6s%-8s\n",
                               stockDetails.astockBean.get(i).comm_name,
                               stockDetails.astockBean.get(i).scheme_desc_en ,
                               stockDetails.astockBean.get(i).opening_balance,
                               stockDetails.astockBean.get(i).issued_qty,
                               stockDetails.astockBean.get(i).closing_balance);
                       add.append(app);
                   }

                   String str1,str2,str3,str4,str5,str6;
                   String[] str = new String[4];
                   if (L.equals("hi")){
                    str1 = context.getResources().getString(R.string.current_stock)+"\n"+context.getResources().getString(R.string.report);
                       image(str1,"header.bmp",1);
                    str2 = context.getResources().getString(R.string.Date)+" : " + date +"\n"+
                           context.getResources().getString(R.string.Time) +" : "+ time + "\n";
                    str3 = context.getResources().getString(R.string.Report_Type) + " : PDS\n"
                           + context.getResources().getString(R.string.FPS_ID)+" : " + dealerConstants.stateBean.statefpsId + "\n";

                    str4 = context.getResources().getString(R.string.name)+"  "+context.getResources().getString(R.string.sch)+"  " +context.getResources().getString(R.string.stock)+"  "+context.getResources().getString(R.string.issued)+"  "+context.getResources().getString(R.string.cb);

                    str5 = String.valueOf(add);
                       image(str2+str3+str4+str5,"body.bmp",0);

                    str6 = context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                           + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n";

                       image(str6,"tail.bmp",1);
                       str[0]="1";
                       str[1]="1";
                       str[2]="1";
                       str[3]="1";
                       checkandprint(str,1);
                   }else {

                        str1 = //dealerConstants.stateBean.stateReceiptHeaderEn+"\n"+
                                context.getResources().getString(R.string.current_stock)+"\n"+context.getResources().getString(R.string.report)+ "\n\n";
                        str2 = context.getResources().getString(R.string.Date) +"        : " + date +"\n"+
                               context.getResources().getString(R.string.Time) +"        : "+ time + "\n";
                        str3 = context.getResources().getString(R.string.Report_Type) +" : PDS\n"+
                                context.getResources().getString(R.string.FPS_ID) +"      : " + dealerConstants.stateBean.statefpsId + "\n"
                               + "-------------------------------\n";
                        str4 = String.format("%-6s%-8s%-4s%-8s%-8s\n",
                                context.getResources().getString(R.string.name),
                                context.getResources().getString(R.string.sch),
                                "OB",
                                context.getResources().getString(R.string.issued),
                                "Clbal")
                               + "-------------------------------\n";
                        str5 = String.valueOf(add);

                        str6 = "\n"+context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                               + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n\n\n";

                       str[0]="1";
                       str[1]=str1;
                       str[2]=str2+str3+str4+str5;
                       str[3]=str6;
                       checkandprint(str,0);

                   }

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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                finish();
            }
        });

        String stock = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                "<soapenv:Envelope\n" +
                "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <ser:getStockReportDetails>\n" +
                "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                "            <report_type>S</report_type>\n" +
                "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                "            <fpsSessionId>" +dealerConstants. fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "        </ser:getStockReportDetails>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Util.generateNoteOnSD(context, "StockReporReq.txt", stock);
        hitURL(stock);
 }catch (Exception ex){

            Timber.tag("Stock-onCreate-").e(ex.getMessage(),"");
        }
    }
    private void Sessiontimeout(String msg, String title) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(title);
        alertDialogBuilder.setTitle(msg);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent i = new Intent(context, StartActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void initilisation() {
        print = findViewById(R.id.stock_print);
        back = findViewById(R.id.stock_back);
        pd = new ProgressDialog(context);
        recyclerView = findViewById(R.id.my_recycler_view);
        toolbarInitilisation();
    }

    private void image(String content, String name,int align) {
        try {
            Util.image(content,name,align);
        }catch (Exception ex){

            Timber.tag("Stock-onCreate-").e(ex.getMessage(),"");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkandprint(String[] str, int i) {
        try {

        if (Util.batterylevel(context)|| Util.adapter(context)) {
            if (mp!=null) {
                releaseMediaPlayer(context,mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100191);
                mp.start();
            }
            es.submit(new TaskPrint(mTerminal100API,str,mActivity,context,i));
            finish();
        }else {
            show_error_box(context.getResources().getString(R.string.Battery_Msg),context.getResources().getString(R.string.Battery));
        }
        }catch (Exception ex){

            Timber.tag("Stock-Battery-").e(ex.getMessage(),"");
        }
    }
    private void probe() {
        try {

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
                        filter.addAction(ACTION_USB_PERMISSION);
                        context.registerReceiver(mUsbReceiver, filter);

                    } else {

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
        }catch (Exception ex){

            Timber.tag("Stock-Probe-").e(ex.getMessage(),"");
        }
    }

    private void hitURL(String stock) {
        try {

        pd = ProgressDialog.show(context, context.getResources().getString(R.string.stock), context.getResources().getString(R.string.Details), true, false);
        XML_Parsing request = new XML_Parsing(context, stock, 6);
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
                if (isError.equals("057") || isError.equals("008") || isError.equals("09D")) {
                    Sessiontimeout(msg, isError);
                    return;
                }
                if (!isError.equals("00")) {
                    System.out.println("ERRORRRRRRRRRRRRRRRRRRRR");
                    show_error_box(msg, context.getResources().getString(R.string.Dealer_Details) + isError);

                } else {
                    stockDetails= (StockDetails) object;
                    flag_print = 1;
                    int astockBeansize= stockDetails.astockBean.size();
                    data = new ArrayList<>();
                    for (int i = 0; i < astockBeansize; i++) {
                        data.add(new StockListModel(
                                stockDetails.astockBean.get(i).comm_name,
                                stockDetails.astockBean.get(i).scheme_desc_en,
                                stockDetails.astockBean.get(i).opening_balance,
                                stockDetails.astockBean.get(i).issued_qty,
                                stockDetails.astockBean.get(i).closing_balance));
                    }
                    adapter = new StockListAdapter(context, data);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
        request.execute();
        }catch (Exception ex){

            Timber.tag("Stock-StockRes-").e(ex.getMessage(),"");
        }
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

    }

    @Override
    public void OnOpenFailed() {
        print.setEnabled(false);

        if (mp!=null) {
            releaseMediaPlayer(context,mp);
        }
        //btnConnect.setEnabled(true);
        if (L.equals("hi")) {
        } else {
      mp= mp.create(context,R.raw.c100078);
              mp.start();


    }}

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
                mActivity.print.setEnabled(bIsOpened);
            }
        });

    }

    private void toolbarInitilisation() {
        try {

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
        toolbarActivity.setText("STOCK REPORT");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
        }catch (Exception ex){

            Timber.tag("Stock-Toolbar-").e(ex.getMessage(),"");
        }
    }

}
