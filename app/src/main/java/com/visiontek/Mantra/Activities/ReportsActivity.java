package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Adapters.StockListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.StockListModel;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.Dealer;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.ReportsModel.Stockdetails.StockDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;


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
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.txnType;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.image;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class ReportsActivity extends BaseActivity implements PrinterCallBack {
    public int flag_print;
    private RecyclerView.Adapter adapter;
    private ArrayList<StockListModel> data;
    PartialOnlineData partialOnlineData;
    Button daily_report, stock_report, back;
    Button daily_report_offline,stock_report_offline;
    Button cumulativestockreport,offline_rationcardstatus;
    Context context;
    StockDetails stockDetails;
    DatabaseHelper databaseHelper;
    public String ACTION_USB_PERMISSION;
    MTerminal100API mTerminal100API;

    public SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                probe();
                // btnConnect.performClick();
                Toast.makeText(context, context.getResources().getString(R.string.ConnectUSB), Toast.LENGTH_LONG).show();
                //print.setEnabled(true);
                daily_report_offline.setEnabled(true);
                stock_report_offline.setEnabled(true);
                synchronized (this) {

                }
            }

        }
    };

    private void probe()
    {
        System.out.println("@@In probe");
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
                        Toast.makeText(getApplicationContext(),
                                context.getResources().getString(R.string.Permission_denied), Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.Connecting), Toast.LENGTH_SHORT).show();

                        //print.setEnabled(false);
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

    @Override
    public void initialize() {
        try {

            context = ReportsActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_reports, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

            System.out.println("@@Initialising printer");
            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                probe();
            } else {
                finish();
            }

            System.out.println("@@Printer initialised");

            System.out.println("@@Checking for network connection");
            if(Util.networkConnected(context)) {

                if (diableMenu("getDailyReport")) {

                    daily_report.setEnabled(false);
                }

                if (diableMenu("getStockReportDetails")) {
                    stock_report.setEnabled(false);
                }
                System.out.println("@@Chck1");
            }

            daily_report_offline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("@@CLICKED on daily_report_offline...");
                    preventTwoClick(v);
                    Intent daily = new Intent(context, DailySalesReportActivity.class);
                    daily.putExtra("type","offline");
                    startActivity(daily);
                }
            });

        daily_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                if(txnType == 1)
                {
                    if(Util.networkConnected(context))
                    {
                        Intent daily = new Intent(context, DailySalesReportActivity.class);
                        daily.putExtra("type","online");
                        startActivity(daily);
                    }else{
                        show_AlertDialog(context.getResources().getString(R.string.Daily_Sales_Report),
                                context.getResources().getString(R.string.Internet_Connection),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                0);
                    }
                }else{
                    show_AlertDialog(context.getResources().getString(R.string.Daily_Sales_Report),
                            context.getResources().getString(R.string.Internet_Connection),
                            context.getResources().getString(R.string.Password_login),
                            0);
                }
            }
        });

            stock_report_offline.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);

                    Intent stock = new Intent(context, StockReportActivity.class);
                    stock.putExtra("isOffline",true);
                    startActivity(stock);

                    /* String app;
                    String time = sdf1.format(new Date()).substring(0, 5);
                    String date = sdf1.format(new Date()).substring(6, 16);
                    StringBuilder add = new StringBuilder();
                    stockDetails.astockBean = databaseHelper.getOfflineCurrentStock();
                    int astockBeansize = stockDetails.astockBean.size();
                    System.out.println("@@Beansize: "+astockBeansize);
                    for (int i = 0; i < astockBeansize; i++) {
                        app = stockDetails.astockBean.get(i).comm_name + "  " +
                                stockDetails.astockBean.get(i).issued_qty + "   " +
                                "\n";
                        add.append(app);
                    }
                    try {
                        String str1, str2, str3, str4, str5, str6;
                        String[] str = new String[4];
                        if (L!=null && L.equals("hi")) {
                            System.out.println("@@in if hindi");
                            str1 = databaseHelper.getStateDetails().get(5) + "\n";
                            str1 = str1 + context.getResources().getString(R.string.current_stock) + "\n" + context.getResources().getString(R.string.report);
                            image(str1, "header.bmp", 1);
                            str2 = context.getResources().getString(R.string.Date) + " : " + date + "\n" +
                                    context.getResources().getString(R.string.Time) + " : " + time + "\n";
                            str3 = context.getResources().getString(R.string.Report_Type) + " : PDS\n"
                                    + context.getResources().getString(R.string.FPS_ID) + " : " + databaseHelper.getStateDetails().get(6)+ "\n";

                            str4 = context.getResources().getString(R.string.name) + "  " + context.getResources().getString(R.string.sch) + "  " + context.getResources().getString(R.string.stock) + "  " + context.getResources().getString(R.string.issued) + "  " + context.getResources().getString(R.string.cb);

                            str5 = String.valueOf(add);
                            image(str2 + str3 + str4 + str5, "body.bmp", 0);

                            str6 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                    + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n";

                            image(str6, "tail.bmp", 1);
                            str[0] = "1";
                            str[1] = "1";
                            str[2] = "1";
                            str[3] = "1";
                            checkandprint(str, 1);
                        }else{
                            System.out.println("@@In else english lang");
                            if(partialOnlineData.getOfflineLogin().equals("Y"))
                            {
                                System.out.println("@@gg to get offline stock");
                                getofflineCurrentStock(partialOnlineData);
                            }
                            //dealerConstants.stateBean.statefpsId = partialOnlineData.getOffPassword();
                            System.out.println("@@In else of print");
                            str1 = databaseHelper.getStateDetails().get(4) + "\n" ;
                            str1 = str1 + context.getResources().getString(R.string.current_stock)+"\n"+context.getResources().getString(R.string.report)+ "\n\n";
                            System.out.println("@@str1: "+str1);
                            str2 = context.getResources().getString(R.string.Date)+"        : " + date +"\n"+
                                    context.getResources().getString(R.string.Time) +"        : "+ time + "\n";
                            System.out.println("@@str2: "+str2);
                            str3 = context.getResources().getString(R.string.Report_Type) + " : PDS\n"
                                    + context.getResources().getString(R.string.FPS_ID)+"      : " + databaseHelper.getStateDetails().get(6) + "\n"
                                    + "-------------------------------\n";
                            System.out.println("@@str3: "+str3);
                            str4 = context.getResources().getString(R.string.name)+"  "+context.getResources().getString(R.string.issued)+ "\n"
                                    + "-------------------------------\n";
                            System.out.println("@@str4: "+str4);
                            str5 = String.valueOf(add);
                            System.out.println("@@str5: "+str5);
                            str6 = "\n"+context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                                    + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n\n\n";
                            System.out.println("@@str6: "+str6);
                            str[0]="1";
                            str[1]=str1;
                            str[2]=str2+str3+str4+str5;
                            str[3]=str6;
                            checkandprint(str,0);

                        }
                    } catch (IOException e) {
                        System.out.println("@@Exception: "+e.toString());
                    }*/
              }
            });


            stock_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                if(Util.networkConnected(context)) {
                    Intent stock = new Intent(context, StockReportActivity.class);
                    startActivity(stock);
                }else{
                    show_AlertDialog(context.getResources().getString(R.string.Stock_Report),
                            context.getResources().getString(R.string.Internet_Connection),
                            context.getResources().getString(R.string.Internet_Connection_Msg),
                            0);
                    return;
                }
            }
        });
            cumulativestockreport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent info = new Intent(context, CumulativeStockReportActivity.class);
                    startActivity(info);


                }
            });


            offline_rationcardstatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent info = new Intent(context, OfflineRationCardStatusActivity.class);
                    startActivity(info);
                }
            });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                finish();
            }
        });
        }catch (Exception exception) {
            System.out.println("@@Exception12: "+exception.toString() );
            exception.printStackTrace();
        }
    }

    private void getofflineCurrentStock(PartialOnlineData partialOnlineData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stockDetails = new StockDetails();

                //dealerConstants.stateBean.statefpsId = partialOnlineData.getOffPassword();
                stockDetails.astockBean = databaseHelper.getOfflineCurrentStock();
                ReportsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        setAdapterData();
                    }
                });
            }
        }).start();
    }

    public void setAdapterData()
    {
        flag_print = 1;
        int astockBeansize= stockDetails.astockBean.size();
        data = new ArrayList<>();
        for (int i = 0; i < astockBeansize; i++) {
            data.add(new StockListModel(stockDetails.astockBean.get(i).comm_name,
                    stockDetails.astockBean.get(i).scheme_desc_en,
                    stockDetails.astockBean.get(i).total_quantity,
                    stockDetails.astockBean.get(i).issued_qty,
                    stockDetails.astockBean.get(i).closing_balance));
        }
        adapter = new StockListAdapter(context, data);
        //recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkandprint(String[] str, int i) {
        System.out.println("@@In check and print");

        if (Util.batterylevel(context)|| Util.adapter(context)) {
            System.out.println("@@In if....");
            if (mp!=null) {
                System.out.println("@@Releasing mp");
                releaseMediaPlayer(context,mp);
            }else{
                System.out.println("@@ mp is null");
            }
            if (L==null && L.equals("hi")) {
                System.out.println("@@lang is null");
            } else {
                System.out.println("@@Lang is not null creating mp...");
                mp = mp.create(context, R.raw.c100191);
                mp.start();
            }
            System.out.println("@@Control here123...");

            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                probe();
            } else {
                finish();
            }

            if(mTerminal100API!=null)
            {
                System.out.println("@@Data in mTerminal100API: "+mTerminal100API);
            }else{
                System.out.println("@@mTerminalAPI100 is null");
            }

            es.submit(new TaskPrint(mTerminal100API,str,this,context,i));
            //es.submit(new TaskPrint(mTerminal100API,str,mActivity,context,i));

        }else {
            System.out.println("@@Battery problem");
            show_AlertDialog(context.getResources().getString(R.string.Battery_Msg),context.getResources().getString(R.string.Battery),"",3);
        }
    }


    @Override
    public void initializeControls() {
        System.out.println("@@In initialise controls");
        databaseHelper = new DatabaseHelper(context);
        stockDetails = new StockDetails();
        partialOnlineData = databaseHelper.getPartialOnlineData();
        daily_report = findViewById(R.id.btn_dailysales_report);
        stock_report = findViewById(R.id.btn_stock_report);

        daily_report_offline = findViewById(R.id.btn_dailysales_report_offline);
        stock_report_offline = findViewById(R.id.btn_stock_report_offline);
        offline_rationcardstatus = findViewById(R.id.btn_offline_ration_card_status);
        cumulativestockreport = findViewById(R.id.btn_cumulative_current_stock);
        daily_report_offline.setEnabled(false);
        stock_report_offline.setEnabled(false);
        back = findViewById(R.id.btn_back);
        toolbarActivity.setText(context.getResources().getString(R.string.REPORTS));
        toolbarFpsid.setText("FPS ID");
        //cumulativestockreport.setVisibility(View.INVISIBLE);
        if(dealerConstants == null || dealerConstants.stateBean == null || dealerConstants.stateBean.statefpsId==null)
        {
            System.out.println("@@NULL");
            ArrayList<String> statefpsiD = databaseHelper.getStateDetails();
            toolbarFpsidValue.setText(statefpsiD.get(6));
        }else {
            System.out.println("@@Setting val");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        }

        if(txnType == 1) {
            System.out.println("@@Online transaction...");
            if (diableMenu("getDailyReport")) {
                daily_report.setEnabled(false);
            }

            if (diableMenu("getStockReportDetails")) {
                stock_report.setEnabled(false);
            }

            SharedPref sharedPref = new SharedPref(context);
            String status = sharedPref.getData("partialOnlineOfflineStatus");

            daily_report.setVisibility(View.VISIBLE);
            stock_report.setVisibility(View.VISIBLE);
            daily_report_offline.setVisibility(View.VISIBLE);
            stock_report_offline.setVisibility(View.VISIBLE);

            if(status.equals("Y"))
            {
                System.out.println("YYYYYYYYY");
                daily_report_offline.setEnabled(true);
                stock_report_offline.setEnabled(true);
            }else{
                daily_report_offline.setEnabled(false);
                stock_report_offline.setEnabled(false);
            }
            daily_report.setEnabled(true);
            stock_report.setEnabled(true);
            offline_rationcardstatus.setVisibility(View.INVISIBLE);

        }else{
            System.out.println("@@Offline transaction");
            daily_report.setVisibility(View.INVISIBLE);
            stock_report.setVisibility(View.INVISIBLE);
            daily_report_offline.setVisibility(View.VISIBLE);
            stock_report_offline.setVisibility(View.VISIBLE);
            offline_rationcardstatus.setVisibility(View.VISIBLE);
            cumulativestockreport.setVisibility(View.INVISIBLE);

            daily_report.setEnabled(false);
            stock_report.setEnabled(false);
            daily_report_offline.setEnabled(true);
            stock_report_offline.setEnabled(true);
            offline_rationcardstatus.setEnabled(true);

        }

    }

    private void show_AlertDialog(String headermsg, String bodymsg, String talemsg, int i) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.alertdialog);
        Button confirm = (Button) dialog.findViewById(R.id.alertdialogok);
        TextView head = (TextView) dialog.findViewById(R.id.alertdialoghead);
        TextView body = (TextView) dialog.findViewById(R.id.alertdialogbody);
        TextView tale = (TextView) dialog.findViewById(R.id.alertdialogtale);
        head.setText(headermsg);
        body.setText(bodymsg);
        tale.setText(talemsg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    public void OnOpen() {
        daily_report_offline.setEnabled(true);
        stock_report_offline.setEnabled(true);
    }

    @Override
    public void OnOpenFailed() {

    }

    @Override
    public void OnClose() {

    }

    @Override
    public void OnPrint(int i, boolean b) {

    }
}
