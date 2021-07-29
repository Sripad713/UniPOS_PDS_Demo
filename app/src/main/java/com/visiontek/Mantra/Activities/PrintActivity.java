package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.mantra.mTerminal100.printer.Prints;
import com.visiontek.Mantra.Adapters.PrintListAdapter;
import com.visiontek.Mantra.Models.DATAModels.PrintListModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Print;
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

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.MemberName;
import static com.visiontek.Mantra.Models.AppConstants.MemberUid;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Models.AppConstants.TOTALAMOUNT;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;


public class PrintActivity extends AppCompatActivity  implements PrinterCallBack {

    private PrintActivity mActivity;
    private  String ACTION_USB_PERMISSION;
    Print printReceipt;
    Context context;
    ProgressDialog pd = null;
    Button print;
    TextView total;
    int flagprint;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;

    private void hitURL(String ration) {
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

        XML_Parsing request = new XML_Parsing(PrintActivity.this, ration, 11);
        request.setOnResultListener(new XML_Parsing.OnResultListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompleted(String code, String msg, String ref, String flow, Object object) {
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
                    print.setEnabled(false);
                    printReceipt = (Print) object;
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

            Timber.tag("Print-onCreate-").e(ex.getMessage(),"");
        }
    }
    private void image(String content, String name,int align) {
        try {
            Util.image(content,name,align);
        }catch (Exception ex){

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
            es.submit(new TaskPrint(mTerminal100API,str,mActivity,context,i));
            Intent home = new Intent(context, CashPDSActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        }else {
            printbox(str,i);
        }
        }catch (Exception ex){

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
            Timber.tag("Print-Probe-").e(ex.getMessage(),"");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void calPrint() {
        try {

        String app;
        StringBuilder add = new StringBuilder();
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

        String date = printReceipt.printBeans.get(0).transaction_time.substring(0, 19);

        String str1,str2,str3,str4,str5;
        String[] str = new String[4];
        if (L.equals("hi")) {
            str1 = dealerConstants.stateBean.stateReceiptHeaderLl+"\n"+
                    context.getResources().getString(R.string.ISSUE)+"\n";

            image(str1,"header.bmp",1);
            str2 =  context.getResources().getString(R.string.FPS_Owner_Name) + " : " + Dealername + "\n"
                    + context.getResources().getString(R.string.FPS_No) + " : " + dealerConstants.stateBean.statefpsId + "\n"
                    + context.getResources().getString(R.string.Name_of_Consumer) + " : " + printReceipt.printBeans.get(0).member_name_ll+ "\n"
                    + context.getResources().getString(R.string.Card_No) + "/"+context.getResources().getString(R.string.sch)+ " : "+ printReceipt.rcId  + "/"+printReceipt.printBeans.get(0).scheme_desc_ll+"\n"
                    + context.getResources().getString(R.string.TransactionID) + " : " + printReceipt.receiptId + "\n"
                    + context.getResources().getString(R.string.Date) + " : " + date + "\n"
                    + context.getResources().getString(R.string.AllotmentMonth) +" : "+
                    menuConstants.fpsPofflineToken.allocationMonth+"\n"
                    +context.getResources().getString(R.string.AllotmentYear) +" : "+
                    menuConstants.fpsPofflineToken.allocationYear+"\n"
                    +
                    String.format("%-10s%-8s%-8s%-8s\n",
                            context.getResources().getString(R.string.commodity) ,
                            context.getResources().getString(R.string.lifted) ,
                            context.getResources().getString(R.string.rate) ,
                            context.getResources().getString(R.string.price));


            str3 = (add)+"";


            str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + " :" + printReceipt.printBeans.get(0).tot_amount ;

            image(str2+str3+str4,"body.bmp",0);

            str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                    + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n";
            image(str5,"tail.bmp",1);

            str[0] = "1";
            str[1] = "1";
            str[2] = "1";
            str[3] = "1";
            checkandprint(str, 1);
        }else {
            str1 = dealerConstants.stateBean.stateReceiptHeaderEn+"\n"+
                    context.getResources().getString(R.string.ISSUE)+"\n\n";
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

            Timber.tag("Print-Display-").e(ex.getMessage(),"");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        try {
            context=PrintActivity.this;

            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
            initilisation();

            Intent intent = getIntent();
            final String ration = intent.getStringExtra("key");

            Display();
            flagprint=0;
            print.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if (flagprint!=2) {
                        if (Util.networkConnected(context)) {

                            //Util.generateNoteOnSD(context, "RationReq.txt", ration);
                            hitURL(ration);
                        } else {
                            show_AlertDialog(context.getResources().getString(R.string.Print),
                                    context.getResources().getString(R.string.Internet_Connection),
                                    context.getResources().getString(R.string.Internet_Connection_Msg),
                                    0);
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
        }catch (Exception ex){

            Timber.tag("Print-onCreate-").e(ex.getMessage(),"");
        }
    }


    private void initilisation() {
        pd = new ProgressDialog(context);
        print = findViewById(R.id.print);
        total = findViewById(R.id.totalamount);
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


            TextView memname=findViewById(R.id.memname);
            TextView memnuid=findViewById(R.id.memuid);
            memname.setText(MemberName);
            memnuid.setText(MemberUid);
            TextView toolbarCard = findViewById(R.id.toolbarCard);
            toolbarCard.setText("RC : "+memberConstants.carddetails.rcId);
            TextView toolbarRD = findViewById(R.id.toolbarRD);
            if (rd_fps == 3) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else if (rd_fps == 2) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
            } else {
                if (RDservice(context)) {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                } else {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
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
            toolbarActivity.setText( context.getResources().getString(R.string.Print));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        }catch (Exception ex){

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
                dialog.dismiss();
                if (i==1) {
                    Intent home = new Intent(context, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                    finish();
                }else if (i==2){
                    flagprint=2;
                    calPrint();
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
