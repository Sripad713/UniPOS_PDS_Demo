package com.visiontek.Mantra.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Adapters.CumulativeStockListAdapter;
import com.visiontek.Mantra.Adapters.StockListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.CumulativeStockListModel;
import com.visiontek.Mantra.Models.DATAModels.StockListModel;
import com.visiontek.Mantra.Models.ReportsModel.Stockdetails.StockDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class CumulativeStockReportActivity extends AppCompatActivity implements PrinterCallBack {

    public SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    Button back, print;
    Context context;
    public String ACTION_USB_PERMISSION;
    public int flag_print;
    ProgressDialog pd = null;
    private CumulativeStockReportActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<CumulativeStockListModel> data;
    StockDetails stockDetails;
    boolean isOffline;
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
                printbox(str,i);
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
            Show(context.getResources().getString(R.string.stock),
                    context.getResources().getString(R.string.Details));

            XML_Parsing request = new XML_Parsing(context, stock, 16);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {

                        show_AlertDialog(
                                context.getResources().getString(R.string.Stock_Report),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Stock_Report),
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                0);

                    } else {
                        stockDetails= (StockDetails) object;
                        setData();
                    }
                }
            });
            request.execute();
        }catch (Exception ex){
            ex.printStackTrace();
            //Timber.tag("Stock-StockRes-").e(ex.getMessage(),"");
            Timber.e("StockReportActivity-hitURLStockDetails Exception ==>"+ ex.getLocalizedMessage());

        }
    }

    public void setData(){
        flag_print = 2;
        //stockDetails = new StockDetails();
        int astockBeansize= stockDetails.astockBean.size();
        data = new ArrayList<>();
        for (int i = 0; i < astockBeansize; i++) {
            if(L.equals("hi")){
                System.out.println("HINDI>>>>");
                data.add(new CumulativeStockListModel(
                        stockDetails.astockBean.get(i).comm_name,
                        //stockDetails.astockBean.get(i).scheme_desc_ll,
                        stockDetails.astockBean.get(i).opening_balance,
                        stockDetails.astockBean.get(i).issued_qty,
                        stockDetails.astockBean.get(i).closing_balance));
                System.out.println("TEJJJJ");

            }else {
                System.out.println("ENGLISH>>>");
                data.add(new CumulativeStockListModel(
                        stockDetails.astockBean.get(i).comm_name,
                        //stockDetails.astockBean.get(i).scheme_desc_en,
                        stockDetails.astockBean.get(i).opening_balance,
                        stockDetails.astockBean.get(i).issued_qty,
                        stockDetails.astockBean.get(i).closing_balance));
                System.out.println("TEJJJ222");

            }
        }
        adapter = new CumulativeStockListAdapter(context, data);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void OnOpen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                print.setEnabled(true);
            }
        });

        // btnConnect.setEnabled(false);

    }

    @Override
    public void OnOpenFailed() {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                print.setEnabled(false);

                if (mp!=null) {
                    releaseMediaPlayer(context,mp);
                }
                //btnConnect.setEnabled(true);
                if (L.equals("hi")) {
                } else {
                    mp= mp.create(context,R.raw.c100078);
                    mp.start();
                }
            }
        });


    }

    @Override
    public void OnClose() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                print.setEnabled(false);
                // btnConnect.setEnabled(true);
                if (mUsbReceiver != null) {
                    context.unregisterReceiver(mUsbReceiver);
                }

                // If Close is caused because the printer is turned off. Then you need to re-enumerate it here.
                probe();
            }
        });

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cumulative_stock_report);
        try {
            context = CumulativeStockReportActivity.this;
            flag_print = 0;
            initilisation();
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
                    try {
                        if (Util.networkConnected(context)) {
                            if (flag_print == 2) {
                                print.setEnabled(false);
                                String app;
                                String time = sdf1.format(new Date()).substring(0, 5);
                                String date = sdf1.format(new Date()).substring(6, 16);
                                StringBuilder add = new StringBuilder();
                                int astockBeansize = stockDetails.astockBean.size();
                                for (int i = 0; i < astockBeansize; i++) {

                                    if (L.equals("hi")) {
                                        app = String.format("%-6s%-10s%-8s%-8s\n",
                                                stockDetails.astockBean.get(i).comm_name,
                                                //stockDetails.astockBean.get(i).scheme_desc_ll,
                                                stockDetails.astockBean.get(i).opening_balance,
                                                stockDetails.astockBean.get(i).issued_qty,
                                                stockDetails.astockBean.get(i).closing_balance);
                                    } else {
                                        app = String.format("%-6s%-10s%-8s%-8s\n",
                                                stockDetails.astockBean.get(i).comm_name,
                                                //stockDetails.astockBean.get(i).scheme_desc_en,
                                                stockDetails.astockBean.get(i).opening_balance,
                                                stockDetails.astockBean.get(i).issued_qty,
                                                stockDetails.astockBean.get(i).closing_balance);
                                    }
                                    add.append(app);
                                }

                                String str1, str2, str3, str4, str5, str6;
                                String[] str = new String[4];
                                if (L.equals("hi")) {
                                    str1 = dealerConstants.stateBean.stateReceiptHeaderLl + "\n" +
                                            context.getResources().getString(R.string.Cumulative_Current_Stock) + "\n" + context.getResources().getString(R.string.report) + "\n";
                                    image(str1, "header.bmp", 1);
                                    str2 = context.getResources().getString(R.string.Date) + " : " + date + "\n" +
                                            context.getResources().getString(R.string.Time) + " : " + time + "\n";
                                    str3 = context.getResources().getString(R.string.Report_Type) + " : PDS\n"
                                            + context.getResources().getString(R.string.FPS_ID) + " : " + dealerConstants.stateBean.statefpsId + "\n\n";

                                    str4 = String.format("%-6s%-6s%-12s%-6s\n",
                                            context.getResources().getString(R.string.name),
                                            context.getResources().getString(R.string.OB),
                                            context.getResources().getString(R.string.issued),
                                            context.getResources().getString(R.string.cb))
                                            + "\n";
                                    System.out.println("STR44  HINDIIII>>>>>" + str4);


                                    str5 = String.valueOf(add);
                                    image(str2 + str3 + str4 + str5, "body.bmp", 0);

                                    str6 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                            + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n";

                                    image(str6, "tail.bmp", 1);
                                    str[0] = "1";
                                    str[1] = "1";
                                    str[2] = "1";
                                    str[3] = "1";
                                    checkandprint(str, 1);
                                } else {

                                    str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n" +
                                            context.getResources().getString(R.string.current_stock) + "\n" + context.getResources().getString(R.string.report) + "\n\n";
                                    str2 = context.getResources().getString(R.string.Date) + "        : " + date + "\n" +
                                            context.getResources().getString(R.string.Time) + "        : " + time + "\n";
                                    str3 = context.getResources().getString(R.string.Report_Type) + " : PDS\n" +
                                            context.getResources().getString(R.string.FPS_ID) + "      : " + dealerConstants.stateBean.statefpsId + "\n\n";
                                    str4 = String.format("%-8s%-6s%-12s%-6s\n",
                                            context.getResources().getString(R.string.name),
                                            context.getResources().getString(R.string.OB),
                                            context.getResources().getString(R.string.issued),
                                            context.getResources().getString(R.string.cb))
                                            + "\n";
                                    System.out.println("STR44 English>>>>>" + str4);
                                    str5 = String.valueOf(add);

                                    str6 = "\n" + context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                            + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";

                                    str[0] = "1";
                                    str[1] = str1;
                                    str[2] = str2 + str3 + str4 + str5;
                                    str[3] = str6;
                                    checkandprint(str, 0);

                                }
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                /*else {
                    if(!Util.networkConnected(context)){
                        System.out.println("OFFLINE PRINT");
                        String app;
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
                                System.out.println("<<<<<OFFLINE HINDI PRINT>>>");
                                str1 = databaseHelper.getStateDetails().get(5) + "\n";
                                str1 = str1 + context.getResources().getString(R.string.current_stock) + "\n" + context.getResources().getString(R.string.report);
                                image(str1, "header.bmp", 1);
                                str2 = context.getResources().getString(R.string.Date) + " : " + date + "\n" +
                                        context.getResources().getString(R.string.Time) + " : " + time + "\n";
                                str3 = context.getResources().getString(R.string.Report_Type) + " : PDS\n"
                                        + context.getResources().getString(R.string.FPS_ID) + " : " + databaseHelper.getStateDetails().get(6)+ "\n";

                                str4 = context.getResources().getString(R.string.name) + "  " + context.getResources().getString(R.string.sch) + "  " + context.getResources().getString(R.string.stock) + "  " + context.getResources().getString(R.string.issued) + "  " + context.getResources().getString(R.string.cb);
                                System.out.println("@@str4: "+str4);
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
                            }
                            else{
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
                        } catch (Exception e) {
                            System.out.println("@@Exception: "+e.toString());
                        }

                    }


                }*/


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
                    "        <ser:getStockDetails>\n" +
                    "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                    "            <report_type>S</report_type>\n" +
                    "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                    "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                    "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                    "        </ser:getStockDetails>\n" +
                    "    </soapenv:Body>\n" +
                    "</soapenv:Envelope>";

            if (networkConnected(context)) {
                //Util.generateNoteOnSD(context, "StockReporReq.txt", stock);
                hitURL(stock);
                Timber.d("CumulativeStockReportActivity-stock : " + stock);
            } else {
                show_AlertDialog(context.getResources().getString(R.string.Stock_Report),
                        context.getResources().getString(R.string.Internet_Connection),
                        context.getResources().getString(R.string.Internet_Connection_Msg),
                        0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void initilisation() {
        print = findViewById(R.id.stock_print);
        back = findViewById(R.id.stock_back);
        pd = new ProgressDialog(context);
        //databaseHelper = new DatabaseHelper(context);
        //partialOnlineData = databaseHelper.getPartialOnlineData();
        recyclerView = findViewById(R.id.my_recycler_view);
        toolbarInitilisation();
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


            TextView toolbarRD = findViewById(R.id.toolbarRD);
            if (rd_fps == 3) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else if (rd_fps == 2) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
            } else {
                if (RDservice(context)) {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                } else {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                }
            }
            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);


            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            toolbarActivity.setText( context.getResources().getString(R.string.Cumulative_Current_Stock));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        }catch (Exception ex){
            Timber.tag("Stock-Toolbar-").e(ex.getMessage(),"");
        }
    }


    private void show_AlertDialog(String headermsg,String bodymsg,String talemsg,int i) {

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

    public void Dismiss(){
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }
    public void Show(String msg,String title){
        SpannableString ss1=  new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2=  new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);


        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
    }



    private void printbox( final String[] str, final int type) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialogbox);
        Button back = (Button) dialog.findViewById(R.id.dialogcancel);
        Button confirm = (Button) dialog.findViewById(R.id.dialogok);
        TextView head = (TextView) dialog.findViewById(R.id.dialoghead);
        TextView status = (TextView) dialog.findViewById(R.id.dialogtext);
        head.setText(context.getResources().getString(R.string.Battery));
        status.setText( context.getResources().getString(R.string.Battery_Msg));
        confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
                checkandprint(str,type);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
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
}