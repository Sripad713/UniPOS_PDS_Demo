package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.mantra.mTerminal100.printer.Prints;
import com.visiontek.Mantra.Adapters.PrintListAdapter;
import com.visiontek.Mantra.Models.AppConstants;
import com.visiontek.Mantra.Models.DATAModels.PrintListModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Print;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.UploadingModels.CommWiseData;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Utils.PrintListener;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.BaseActivity.rd_vr;
import static com.visiontek.Mantra.Activities.RationDetailsActivity.TOTALAMOUNT;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.mp;


import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.MemberName;
import static com.visiontek.Mantra.Models.AppConstants.MemberUid;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Models.AppConstants.offlineEligible;
import static com.visiontek.Mantra.Models.AppConstants.txnType;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class PrintActivity extends AppCompatActivity implements PrinterCallBack {

    private PrintActivity mActivity;
    private String ACTION_USB_PERMISSION;
    Print printReceipt;
    Context context;
    ProgressDialog pd = null;
    Button print;
    TextView total;
    TextView textmemuid, memname, memnuid;
    DatabaseHelper databaseHelper;
    String rationCardNo;
    String session;
    String membername,memberId;

    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    String saleStateFpsId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        context = PrintActivity.this;
       /* TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String im = telephonyManager.getImei(1);*/
        try {
            textmemuid = findViewById(R.id.textmemuid);
            System.out.println("@@In PrintActivity.java");
            databaseHelper = new DatabaseHelper(
                    context);
            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
            rationCardNo = getIntent().getStringExtra("rationCardNo");
            System.out.println("@@Ratin card number received: " +rationCardNo);
            if(rationCardNo == null)
            {
                rationCardNo = memberConstants.carddetails.rcId;
            }
            session = getIntent().getStringExtra("session");
            System.out.println("SESSION ENTER IN PRINTACTIVITY>>>>  "+session);
            membername = getIntent().getStringExtra("membername");
            System.out.println("MEMBERNAME ENTER IN PRINTACTIVITY>>>>  "+membername);
            memberId = getIntent().getStringExtra("memberId");
            System.out.println("MEMBER_ID ENTER IN PRINTACTIVITY>>>>  "+memberId);
            if(txnType == -1)
            {
                System.out.println("@@Offline transaction...");
                saleStateFpsId = databaseHelper.getStateDetails().get(6);
                //saleStateFpsId = dealerConstants.stateBean.statefpsId;
                Dealername = databaseHelper.getOfflineDealerName();
            }
            else
            {
                System.out.println("@@Online transaction");
                PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();

                saleStateFpsId = partialOnlineData.getOffPassword();
            }


            System.out.println("@@Going to initialisation...");
            initilisation();
            System.out.println("@@Initialisation done...");
            Intent intent = getIntent();
            final String ration = intent.getStringExtra("key");
            System.out.println("@@Data in ration: " +ration);

            Display();
            System.out.println("@@Display finished");

            print.setOnClickListener(new View.OnClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    System.out.println("@@Clicked on print button");
                    preventTwoClick(view);
                    try {

                        if (txnType == 1 && Util.networkConnected(context)) {
                            System.out.println("@@Network connected");
                            Util.generateNoteOnSD(context, "RationReq.txt", ration);
                            Timber.d("PrintActivity-RationReq : "+ration);
                            hitURL(ration);
                        } else if (txnType == -1 || (dealerConstants != null && dealerConstants.fpsCommonInfo != null && dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus.equalsIgnoreCase("Y")) || session.equalsIgnoreCase("Offline"))
                        {
                            System.out.println("@@Offline");
                            PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
                            Dealername = databaseHelper.getOfflineDealerName();
                            saleStateFpsId = partialOnlineData.getOffPassword();

                            System.out.println("@@Eligible for offline");
                            String message = databaseHelper.txnAllotedBetweenTime();
                            //message = "";
                            if (message.isEmpty()) {
                                System.out.println("@@Message is empty...");
                                if (txnType == 1) {
                                    System.out.println("<<<<<<<ONLINE>>>>>");
                                    session = "Partial";
                                    System.out.println("<<<<<<<CHANGE PARTIAL>>>>>");
                                    System.out.println("@@Online");
                                    proceedForOfflineTransaction(false, new Print());
                                    /*final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setMessage("No Network go to Offline Txns");
                                    alertDialogBuilder.setTitle("Network Unavailable");
                                    alertDialogBuilder.setCancelable(false);
                                    alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    proceedForOfflineTransaction(false, new Print());
                                                }
                                            });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();*/
                                } else {
                                    if(session.equalsIgnoreCase("Online")) {
                                        System.out.println("<<<<<<ONLINE>>>>>>ELSE>>>>>");
                                         session= "Partial";
                                    }

                                    System.out.println("@@Going to proceedForOfflineTransaction");
                                    proceedForOfflineTransaction(false, new Print());
                                }
                            } else
                                show_error_box("Offline Distribution error", message, 0);
                        } /*else {

                        }
                    }*/ else {
                            System.out.println("@@Data in offline eligible: " + offlineEligible);
                            System.out.println("@@Online transaction but network not available");
                            show_AlertDialog(context.getResources().getString(R.string.Print),
                                    context.getResources().getString(R.string.Internet_Connection),
                                    context.getResources().getString(R.string.Internet_Connection_Msg),
                                    0);
                        }
                    } catch (Exception e) {
                        System.out.println("@@Exception in PrintActivity: " + e.toString());
                    }
                }

                //}
            });

            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);
            print.setEnabled(false);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                probe();
            } else {
                finish();
            }
        }catch (Exception ex){
            System.out.println("@@Exception(PrintActivity.java)#1#: " +ex.toString());
            Timber.tag("Print-onCreate-").e(ex.getMessage(),"");
        }
    }
    StringBuilder add = new StringBuilder();
    private void initilisation() {
        pd = new ProgressDialog(context);
        print = findViewById(R.id.print);
        total = findViewById(R.id.totalamount);
        toolbarInitilisation();
    }

    private void hitURL(String ration) {
        System.out.println("@@In hitURL");
        try {

            if (mp!=null) {
                releaseMediaPlayer(context,mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100076);
                mp.start();
            }

            Show( context.getResources().getString(R.string.Confirm),
                    context.getResources().getString(R.string.Please_Wait) );
/*
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Confirm),
         context.getResources().getString(R.string.Please_Wait), true, false);
*/
            System.out.println("@@Data in ratiion: " +ration);
            XML_Parsing request = new XML_Parsing(PrintActivity.this, ration, 11);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    System.out.println("@@Parsing completed");
                    Dismiss();
                    if (code == null || code.isEmpty()) {

                        show_AlertDialog(
                                context.getResources().getString(R.string.Print),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Print),
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                1);
                    } else {
                        System.out.println("@@Code 00");
                        printReceipt = (Print) object;
                        String app;

                        int printReceiptsize= printReceipt.printBeans.size();
                        for (int i = 0; i <printReceiptsize ; i++) {
                            if(L.equals("hi")){
                                app =  String.format("%-10s%-8s%-8s%-8s\n",
                                        printReceipt.printBeans.get(i).comm_name_ll,
                                        printReceipt.printBeans.get(i).carry_over ,
                                        printReceipt.printBeans.get(i).retail_price ,
                                        printReceipt.printBeans.get(i).commIndividualAmount);

                            }else {
                                app =  String.format("%-10s%-8s%-8s%-8s\n",
                                        printReceipt.printBeans.get(i).comm_name,
                                        printReceipt.printBeans.get(i).carry_over ,
                                        printReceipt.printBeans.get(i).retail_price ,
                                        printReceipt.printBeans.get(i).commIndividualAmount);
                            }

                            add.append(app);
                        }
                        Print offlinePrintData = databaseHelper.getPrintDataFromLocal(rationCardNo);
                        String deviceTxnId,orderDateTime;
                        Date now = new Date();

                            DateFormat dateFormat = new SimpleDateFormat("hhmmss");
                            DateFormat orderdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            Calendar calendar = Calendar.getInstance();
                            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
                            deviceTxnId = String.format("%s%03d%s", AppConstants.DEVICEID,dayOfYear,dateFormat.format(now));
                            orderDateTime = orderdateFormat.format(now);

                        String temp =session;
                        System.out.println("HIT URL SESSION>>>>>>>"+temp);
//                        if(session.equalsIgnoreCase("PARTIAL"))
//                        {
//                            temp = "Offline";
//                        }else{
//                            temp = "Online";
//                        }

                        boolean isSuccessful = databaseHelper.updateOfflineData(getApplicationContext(), offlinePrintData,rationCardNo,temp,deviceTxnId,orderDateTime,membername,memberId);

                        /*if(databaseHelper.checkForOfflineDistribution()==0)
                        {
                            System.out.println("@@Going to proceedForOfflineTransaction");
                            proceedForOfflineTransaction(true,printReceipt);
                            // calPrint();
                        }else{
                            setUpNProceedToPrint(printReceipt,true);
                            //   calPrint();
                        }*/

                    show_AlertDialog(
                            context.getResources().getString(R.string.Transaction_Successfull_Printing_Please_Wait),
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,
                            2);
                    }
                }
            });
            request.execute();
        }catch (Exception ex){
            System.out.println("@@Exception...............1" +ex.toString());
            //Timber.tag("Print-onCreate-").e(ex.getMessage(),"");
            Timber.e("PrintActivity-OnCreate Exception :"+ex.getLocalizedMessage());

        }
    }
    private void image(String content, String name,int align) {
        try {
            Util.image(content,name,align);
        }catch (Exception ex){
            System.out.println("@@Exception...............2" +ex.toString());
            Timber.tag("Print-Image-").e(ex.getMessage(),"");
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
                System.out.println("<<<<<<<TASKPRINT>>>>1111");
                es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i));
                Intent cash = new Intent(PrintActivity.this, CashPDSActivity.class);
                cash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cash);
                finish();

            }else {
                printbox(str,i);
            }
        }catch (Exception ex){
            System.out.println("@@Exception...............3" +ex.toString());
            Timber.tag("Print-Battery-").e(ex.getMessage(),"");
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
                                public void run() { mTerminal100API.printerOpenTask(mUsbManager, device, context);
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
            System.out.println("@@Exception...............4" +ex.toString());
            Timber.tag("Print-Probe-").e(ex.getMessage(),"");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void calPrint() {
        try {
            String date = printReceipt.printBeans.get(0).transaction_time.substring(0, 19);

            String str1,str2,str3,str4,str5;
            String[] str = new String[4];
            if (L.equals("hi")) {
                str1 = dealerConstants.stateBean.stateReceiptHeaderLl+"\n";

                image(str1,"header.bmp",1);
                str2 =  context.getResources().getString(R.string.FPS_Owner_Name) + " : " + Dealername + "\n"
                        + context.getResources().getString(R.string.FPS_No) + "    :  " + dealerConstants.stateBean.statefpsId +"\n"
                        + context.getResources().getString(R.string.Name_of_Consumer) + "       :   " + printReceipt.printBeans.get(0).member_name_ll+ "\n"
                        + context.getResources().getString(R.string.Card_No) + "/"+context.getResources().getString(R.string.sch)+ " : "+ printReceipt.rcId  + "/"+printReceipt.printBeans.get(0).scheme_desc_ll+"\n"
                        + context.getResources().getString(R.string.TransactionID)+":" + printReceipt.receiptId + "\n"
                        + context.getResources().getString(R.string.Date) + " : " + date + "\n"
                        + context.getResources().getString(R.string.AllotmentMonth) +"       :   "+
                        menuConstants.fpsPofflineToken.allocationMonth+"\n"
                        +context.getResources().getString(R.string.AllotmentYear) +"        :   "+
                        menuConstants.fpsPofflineToken.allocationYear+"\n"
                        +
                        String.format("%-10s%-8s%-8s%-8s\n\n",
                                context.getResources().getString(R.string.commodity) ,
                                context.getResources().getString(R.string.lifted) ,
                                context.getResources().getString(R.string.rate) ,
                                context.getResources().getString(R.string.price));


                str3 = (add)
                        +"\n";
                str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + "      : " + printReceipt.printBeans.get(0).tot_amount ;

                image(str2+str3+str4,"body.bmp",0);

                str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                        + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";
                image(str5,"tail.bmp",1);

                str[0] = "1";
                str[1] = "1";
                str[2] = "1";
                str[3] = "1";
                checkandprint(str, 1);
            }else {
                str1 = dealerConstants.stateBean.stateReceiptHeaderEn+"\n\n";

                str2 =
                        context.getResources().getString(R.string.FPS_Owner_Name) + "  :" + Dealername + "\n"
                                + context.getResources().getString(R.string.FPS_No) + "          :" + dealerConstants.stateBean.statefpsId + "\n"
                                + context.getResources().getString(R.string.Name_of_Consumer) + ":" + printReceipt.printBeans.get(0).member_name+ "\n"
                                + context.getResources().getString(R.string.Card_No) + "/scheme   :" + printReceipt.rcId  + "/"+printReceipt.printBeans.get(0).scheme_desc_en+"\n"
                                + context.getResources().getString(R.string.TransactionID) + ":" + printReceipt.receiptId + "\n"
                                + context.getResources().getString(R.string.Date) + " :" + date + "\n"
                                + context.getResources().getString(R.string.AllotmentMonth) +"   :"+
                                menuConstants.fpsPofflineToken.allocationMonth+"\n"
                                +context.getResources().getString(R.string.AllotmentYear) +"    :"+
                                menuConstants.fpsPofflineToken.allocationYear+"\n"
                                +
                                String.format("%-10s%-8s%-8s%-8s\n",
                                        context.getResources().getString(R.string.commodity) ,
                                        context.getResources().getString(R.string.lifted) ,
                                        context.getResources().getString(R.string.rate) ,
                                        context.getResources().getString(R.string.price));

                str3 = (add)
                        + "\n";

                str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + "      : " + printReceipt.printBeans.get(0).tot_amount+ "\n"
                        + "\n";


                str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                        + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";

                str[0] = "1";
                str[1] = str1;
                str[2] = str2 + str3 + str4;
                str[3] = str5;
                checkandprint(str, 0);
            }
        }catch (Exception ex){
            System.out.println("@@Exception...............5" +ex.toString());
            Timber.tag("Print-Printcall-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void OnOpen() {
        // TODO Auto-generated method stub
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                print.setEnabled(true);
                // btnConnect.setEnabled(false);

            }
        });
    }

    @Override
    public void OnOpenFailed() {
        // TODO Auto-generated method stub
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                print.setEnabled(false);
                //btnConnect.setEnabled(true);


            }
        });
    }

    @Override
    public void OnClose() {
        // TODO Auto-generated method stub
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                print.setEnabled(false);
                if (mUsbReceiver != null) {
                    context.unregisterReceiver(mUsbReceiver);
                }probe();
            }
        });
    }
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
                System.out.println("@@Exception...............6" +ex.toString());
                Timber.tag("Print-Broadcast-").e(ex.getMessage(),"");
            }
        }
    };


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

    private void Display() {
        System.out.println("@@In display...");
        try {

            RecyclerView recyclerView;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            ArrayList<PrintListModel> data= new ArrayList<>();
            int commDetailssize = memberConstants.commDetails.size();
            System.out.println("@@Com details size: " +commDetailssize);

            float commbal,commqty;
            String required;

            for (int i = 0; i < commDetailssize; i++) {
                commbal= Float.parseFloat((memberConstants.commDetails.get(i).balQty));
                commqty= Float.parseFloat((memberConstants.commDetails.get(i).requiredQty));
                required= String.valueOf(commbal-commqty);
                if (commqty>0.0) {

                    if(L.equals("hi")){
                        data.add(new PrintListModel(
                                memberConstants.commDetails.get(i).commNamell +
                                        "\n(" + memberConstants.commDetails.get(i).totQty + ")",
                                memberConstants.commDetails.get(i).price,
                                required,
                                memberConstants.commDetails.get(i).requiredQty,
                                memberConstants.commDetails.get(i).amount));
                    }else {
                        data.add(new PrintListModel(
                                memberConstants.commDetails.get(i).commName +
                                        "\n(" + memberConstants.commDetails.get(i).totQty + ")",
                                memberConstants.commDetails.get(i).price,
                                required,
                                memberConstants.commDetails.get(i).requiredQty,
                                memberConstants.commDetails.get(i).amount));
                    }
                }
            }
            RecyclerView.Adapter adapter = new PrintListAdapter(this, data);
            recyclerView.setAdapter(adapter);

            String t= String.valueOf(TOTALAMOUNT);
            total.setText(t);
        }catch (Exception ex){
            System.out.println("@@Exception cought while displaying: " +ex.toString());
            Timber.tag("Print-Display-").e(ex.getMessage(),"");
        }
    }
    private void toolbarInitilisation() {
        System.out.println("@@In toolbarInitilisation");
        try {
            TextView toolbarVersion = findViewById(R.id.toolbarVersion);
            TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
            TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
            TextView toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
            TextView toolbarActivity = findViewById(R.id.toolbarActivity);
            TextView toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
            TextView toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);
            memname=findViewById(R.id.memname);
            memnuid=findViewById(R.id.memuid);

            if(session.equalsIgnoreCase("partial"))
            {
                memnuid.setVisibility(View.INVISIBLE);
                textmemuid.setVisibility(View.INVISIBLE);
            }else{
                memnuid.setVisibility(View.VISIBLE);
                textmemuid.setVisibility(View.VISIBLE);
                memnuid.setText(MemberUid);
                memname.setText(MemberName);
            }
            TextView toolbarCard = findViewById(R.id.toolbarCard);

            if(memberConstants == null)
            {
                toolbarCard.setText("RC : "+rationCardNo);
            }else if(memberConstants.carddetails == null)
            {
                toolbarCard.setText("RC : "+rationCardNo);
            }else if(memberConstants.carddetails.rcId == null)
            {
                toolbarCard.setText("RC : "+rationCardNo);
            }else {
                toolbarCard.setText("RC : " + memberConstants.carddetails.rcId);
            }

            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);

            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            ArrayList<String> stateData = databaseHelper.getStateDetails();
            if(dealerConstants == null || dealerConstants.stateBean == null || dealerConstants.stateBean.statefpsId == null)
            {
                toolbarFpsidValue.setText(stateData.get(6));
            }else{
                toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            }


            toolbarActivity.setText( context.getResources().getString(R.string.Print));
            TextView toolbarRD = findViewById(R.id.toolbarRD);
            if (rd_vr != null && rd_vr.length() > 1){
                toolbarRD.setText("RD" + rd_vr);
            }else {
                toolbarRD.setText("RD" );
            }
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


            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude );
        }catch (Exception ex){
            System.out.println("@@Exception(PrintActivity) in toolbarInitilisation: "+ex.toString());
            Timber.tag("Print-Toolbar-").e(ex.getMessage(),"");
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
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
                if (i==1) {
                    Intent home = new Intent(context, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                    finish();
                }else if (i==2){
                    calPrint();
                }else if(i==3){

                    Intent login = new Intent(PrintActivity.this, StartActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(login);
                    finish();
                    System.out.println("TASKPRINT>>>>>2222ENDDD>>>>>>>StartActivity");

                }
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

    /***************************** OFFLINE APIS **************************/
    public void proceedForOfflineTransaction(boolean isOnlineTransaction,Print printData ) throws SQLException {
        System.out.println("@@In proceedForOfflineTransaction");
        Date now = new Date();
        String deviceTxnId,orderDateTime;
        if(!isOnlineTransaction)
        {
            System.out.println("@@Not online transaction");
            DateFormat dateFormat = new SimpleDateFormat("hhmmss");
            DateFormat orderdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Calendar calendar = Calendar.getInstance();
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            deviceTxnId = String.format("%s%03d%s", AppConstants.DEVICEID,dayOfYear,dateFormat.format(now));
            orderDateTime = orderdateFormat.format(now);
        }
        else
        {
            System.out.println("@@online transaction");
            DateFormat fromDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            DateFormat toDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date orderDate = null;
            try {
                orderDate = fromDateFormat.parse(printData.printBeans.get(0).transaction_time);
                System.out.println("@@order date: " +orderDate);
            } catch (ParseException e) {
                System.out.println("@@Exception...............6" +e.toString());
                e.printStackTrace();
            }
            orderDateTime = toDateFormat.format(orderDate);
            deviceTxnId = printData.receiptId;
        }


        Print offlinePrintData = databaseHelper.getPrintDataFromLocal(rationCardNo);

        System.out.println("@@Goig to updateOfflineData");

        System.out.println("@@Ration card no: " +rationCardNo);

        String temp = session;
        System.out.println("TEMP SESSION>>>>>"+session);
//        if(session.equalsIgnoreCase("PARTIAL"))
//        {
//            temp = "Offline";
//        }else{
//            temp = "Online";
//        }
/*
        String temp = "";
        if (txnType == 1)
    {
        temp = "Online";
    }else{
            temp = "Offline";
    }
*/
        boolean isSuccessful = databaseHelper.updateOfflineData(getApplicationContext(), offlinePrintData,rationCardNo,temp,deviceTxnId,orderDateTime,membername,memberId);
        if(isSuccessful)
        {
            System.out.println("@@updateOfflineData success");
            if(!isOnlineTransaction) {
                System.out.println("@@Going to setUpNProceedToPrint1");
                setUpNProceedToPrint(offlinePrintData,isOnlineTransaction);
            }
            else
            {
                System.out.println("@@Going to setUpNProceedToPrint2");
                setUpNProceedToPrint(printData,isOnlineTransaction);
            }
        }
        else
        {
            show_error_box(context.getResources().getString(R.string.Distribution_Response),context.getResources().getString(R.string.Distribution_Failed_Please_try_again),0);
        }
    }

    public void setUpNProceedToPrint(Print printReceipt,boolean isOnlineTransaction) throws SQLException {
        System.out.println("@@In setUpNProceedTOPrint");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            String app;
            StringBuilder add = new StringBuilder();
            int printReceiptsize= printReceipt.printBeans.size();
            Double totalAmount = 0.0;
            if(isOnlineTransaction)
                totalAmount = Double.parseDouble(printReceipt.printBeans.get(0).tot_amount);

            for (int i = 0; i <printReceiptsize ; i++) {


                app =  String.format("%-10s%-8s%-8s%-8s\n",
                        printReceipt.printBeans.get(i).comm_name,
                        printReceipt.printBeans.get(i).carry_over ,
                        printReceipt.printBeans.get(i).retail_price ,
                        printReceipt.printBeans.get(i).commIndividualAmount);

//                app = "  " + printReceipt.printBeans.get(i).comm_name
//                        + "\n" + printReceipt.printBeans.get(i).total_quantity+ "  " + "  "
//                        + printReceipt.printBeans.get(i).carry_over + "  " + "  "
//                        + printReceipt.printBeans.get(i).retail_price + "  " + "  "
//                        + printReceipt.printBeans.get(i).commIndividualAmount + "  " + "\n";
                if(!isOnlineTransaction)
                    totalAmount += Double.parseDouble(printReceipt.printBeans.get(i).tot_amount);
                add.append(app);
            }

            String date = printReceipt.printBeans.get(0).transaction_time.substring(0, 19);

            String str1,str2,str3,str4,str5;
            String[] str = new String[4];
            ArrayList <String> stateHeader = databaseHelper.getStateDetails();
            String monthYear[] = databaseHelper.getMonthYear(context);
            String member = "";
            String recptID = "";
            if (L.equals("hi"))
            {
                str1 = "";

                if(!isOnlineTransaction){
                    str1 = context.getResources().getString(R.string.Offline_Receipt);
                    DatabaseHelper.ExcessData rcData = databaseHelper.getExcessData(rationCardNo);
                    member = rcData.getMemberNameLocal();

                    List<CommWiseData> sample =  databaseHelper.getPendingOfflineData(rationCardNo);
                    CommWiseData temp = sample.get(sample.size()-1);
                    recptID = temp.getReceiptId();
                }else{
                    member = printReceipt.printBeans.get(0).member_name_ll;
                    recptID = printReceipt.receiptId;
                }

                str1 = str1 + "\n" +stateHeader.get(4) + "\n";
                image(str1,"header.bmp",1);
                str2 = context.getResources().getString(R.string.FPS_Owner_Name) + " :" + Dealername + "\n"
                        + context.getResources().getString(R.string.FPS_No) + " :" + saleStateFpsId + "\n"
                        + context.getResources().getString(R.string.Name_of_Consumer) + " :" +member+ "\n"
                        + context.getResources().getString(R.string.Card_No) + " :" + printReceipt.rcId + "\n"
                        + context.getResources().getString(R.string.TransactionID) + " :" + recptID + "\n"
                        + context.getResources().getString(R.string.Date) + " :" + date + "\n"
                        + context.getResources().getString(R.string.AllotmentMonth) +" : "+
                        monthYear[0]+"\n"
                        +context.getResources().getString(R.string.AllotmentYear) +"   : "+
                        monthYear[1]+"\n" +

                        String.format("%-12s%-12s%-8s%-6s\n",
                                context.getResources().getString(R.string.commodity) ,
                                context.getResources().getString(R.string.lifted) ,
                                context.getResources().getString(R.string.rate) ,
                                context.getResources().getString(R.string.price));

                //+ context.getResources().getString(R.string.commodity) + " " + context.getResources().getString(R.string.lifted) + "   " + context.getResources().getString(R.string.rate) + "    " + context.getResources().getString(R.string.price) ;
                //str3 = (add)+"";

                //str4 = context.getResources().getString(R.string.Total_Amount) + " :" + totalAmount ;
                str3 = (add)+"\n";
                        //+ "________________________________\n";

                str4 = context.getResources().getString(R.string.Total_Amount) + "    :" + totalAmount+ "\n";
                        //+ "________________________________\n";

                image(str2+str3+str4,"body.bmp",0);

                str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                        + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n";
                image(str5,"tail.bmp",1);

                str[0] = "1";
                str[1] = "1";
                str[2] = "1";
                str[3] = "1";
                //dialog(str,1);
                show_AlertDialogbox(str,context.getResources().getString(R.string.Transaction),
                        context.getResources().getString(R.string.Printing),"",1);
            }else {
                System.out.println("@@Eng lang");
                str1 = "";
                if(!isOnlineTransaction){
                    str1 = context.getResources().getString(R.string.Offline_Receipt);
                    DatabaseHelper.ExcessData rcData = databaseHelper.getExcessData(rationCardNo);
                    member = rcData.getMemberName();
                    List<CommWiseData> sample =  databaseHelper.getPendingOfflineData(rationCardNo);
                    CommWiseData temp = sample.get(sample.size()-1);
                    recptID = temp.getReceiptId();
                }else{
                    member = printReceipt.printBeans.get(0).member_name;
                }

                str1 = str1 + "\n" +stateHeader.get(5) + "\n";
                str2 = "\n________________________________\n"
                        + context.getResources().getString(R.string.FPS_Owner_Name) + "  :" + Dealername + "\n"
                        + context.getResources().getString(R.string.FPS_No) + "          :" + saleStateFpsId + "\n"
                        + context.getResources().getString(R.string.Name_of_Consumer) + ":" + member + "\n"
                        + context.getResources().getString(R.string.Card_No) + "          :" + printReceipt.rcId  + "\n"
                        + context.getResources().getString(R.string.TransactionID) + " :" + recptID + "\n"
                        + context.getResources().getString(R.string.Date) + " : " + date + "\n"
                        + context.getResources().getString(R.string.AllotmentMonth) +" : "+
                        monthYear[0]+"\n"
                        +context.getResources().getString(R.string.AllotmentYear)  +" : "+
                        monthYear[1]+"\n"+
                String.format("%-10s%-8s%-8s%-8s\n",
                        context.getResources().getString(R.string.commodity) ,
                        context.getResources().getString(R.string.lifted) ,
                        context.getResources().getString(R.string.rate) ,
                        context.getResources().getString(R.string.price));

                       //+context.getResources().getString(R.string.commodity) + " " + context.getResources().getString(R.string.lifted) + "   " + context.getResources().getString(R.string.rate) + "    " + context.getResources().getString(R.string.price) + "\n"
                        //+ "________________________________\n";
                str3 = (add)
                        + "________________________________\n";

                str4 = context.getResources().getString(R.string.Total_Amount) + "    :" + totalAmount+ "\n"
                        + "________________________________\n";


                str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                        + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";

                str[0] = "1";
                str[1] = str1;
                str[2] = str2 + str3 + str4;
                str[3] = str5;
                //dialog(str,0);
                show_AlertDialogbox(str,context.getResources().getString(R.string.Transaction),
                        context.getResources().getString(R.string.Printing),"",0);

            }

        }

    }

    private void show_error_box(String msg, String title, final int i) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (i==1) {
                            Intent home = new Intent(context, HomeActivity.class);
                            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(home);
                            finish();
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void dialog(final String[] str, final int i) {
        System.out.println("@@In dialog");
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.Printing));
        alertDialogBuilder.setTitle(context.getResources().getString(R.string.Transaction));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        if (Util.batterylevel(context)|| Util.adapter(context)) {
                            if (mp!=null) {
                                releaseMediaPlayer(context,mp);
                            }
                            if (L.equals("hi")) {
                            } else {
                                mp = mp.create(context, R.raw.c100191);
                                mp.start();
                            }
                            es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i));
                            Intent cash = new Intent(PrintActivity.this, CashPDSActivity.class);
                            cash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(cash);
                            finish();

                        }else {
                            show_error_box(context.getResources().getString(R.string.Battery_Msg),context.getResources().getString(R.string.Battery),1);
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void show_AlertDialogbox(String str[], String headermsg, String bodymsg, String talemsg, int i) {
        System.out.println("@@ dialog>>>>");
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
        confirm.setOnClickListener(v -> {
            preventTwoClick(v);
            dialog.dismiss();
            if (Util.batterylevel(context)|| Util.adapter(context)) {
                if (mp!=null) {
                    releaseMediaPlayer(context,mp);
                }
                if (L.equals("hi")) {
                } else {
                    mp = mp.create(context, R.raw.c100191);
                    mp.start();
                }
                PrintListener printListener = new PrintListener() {
                    @Override
                    public void onPrintFinished(int result) {
                        if(Util.networkConnected(context)) {
                            //onNetworkChanged();
                            show_AlertDialog(context.getResources().getString(R.string.Network_Available),context.getResources().getString(R.string.Please_login_in_Online_mode),"",3);
                        }else{
                                Intent cash = new Intent(PrintActivity.this, CashPDSActivity.class);
                                cash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(cash);
                                finish();
                                System.out.println("TASKPRINT>>>>>2222ENDDD>>>>>>>CashPDS");
                        }
                    }
                };
                es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i,printListener));

            }else {
                show_error_box(context.getResources().getString(R.string.Battery_Msg),context.getResources().getString(R.string.Battery),1);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

}
