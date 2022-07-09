package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Adapters.RationListAdapter;
import com.visiontek.Mantra.Adapters.ReceiveGoodsListAdapter;
import com.visiontek.Mantra.Adapters.ReceiveGoodsListOfflineAdapter;
import com.visiontek.Mantra.Models.DATAModels.RationListModel;
import com.visiontek.Mantra.Models.DATAModels.ReceiveGoodsListModel;
import com.visiontek.Mantra.Models.DATAModels.ReceiveGoodsOfflineListModel;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsCommonInfo;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails.DealerModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Print;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.RCOffline;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsOfflineModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.tcCommDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Utils.DecimalDigitsInputFilter;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;

import org.w3c.dom.Text;

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
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class offlineRecvNew extends AppCompatActivity implements PrinterCallBack {

    private offlineRecvNew mActivity;
    ArrayList<ReceiveGoodsOfflineListModel> modeldata;
    Button Add,Back,Submit;
    Context context;
    DatabaseHelper databaseHelper;
    ArrayList<String> comNames;
    ArrayList<String> schemeNames;


    MTerminal100API mTerminal100API;
    public String ACTION_USB_PERMISSION;

    ArrayList<String> Coms;
    ArrayList<String> Schemes;
    ArrayList<String> Qts;

    RecyclerView.Adapter adapter;

    ReceiveGoodsOfflineModel receiveGoodsOfflineModel;
    RCOffline rcoffline =new RCOffline();
    ReceiveGoodsDetails receiveGoodsDetails;
    RecyclerView recyclerView;

    public int count = 0;
    String truckNo = "",vehicle="";
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
                synchronized (this) {

                }
            }

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkandprint(String[] str, int i) {
        System.out.println("@@In check and print data: " +str[0]);

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
            mTerminal100API.initPrinterAPI(this, context);
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

            es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i));

            Intent intent = new Intent(offlineRecvNew.this,OfflineUploadNDownload.class);
            startActivity(intent);

            //es.submit(new TaskPrint(mTerminal100API,str,mActivity,context,i));

        }else {
            System.out.println("@@Battery problem");
            show_error_box(context.getResources().getString(R.string.Battery_Msg),context.getResources().getString(R.string.Battery));
        }
    }

    private void show_error_box(String msg, String title) {
        final androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
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

    private void probe() {
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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = offlineRecvNew.this;
        databaseHelper = new DatabaseHelper(context);


        setContentView(R.layout.activity_offline_receive_goods_n);

        mActivity = this;
        ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;

        truckNo = getIntent().getStringExtra("truck");
        vehicle = getIntent().getStringExtra("vehicle");
        System.out.println("@@Truck no received: " +truckNo);
        modeldata = new ArrayList<>();
        Add = findViewById(R.id.receive_add);
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("@@Clicked on Add");
                if(count == 0) {
                    rcoffline.comm = "select";
                    rcoffline.sch = "select";
                    rcoffline.enter = "Qty";
                    rcoffline.text = "KG";
                    receiveGoodsOfflineModel.rcoffline.add(0,rcoffline);
                }
                add();
            }
        });

        Back = findViewById(R.id.receive_back);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(offlineRecvNew.this,offlineRecvGoods.class);
                startActivity(intent);
                finish();
            }
        });



        Submit = findViewById(R.id.receive_submit);
        Submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                System.out.println("@@Going to updatePosOb");
                updatePosOb();
            }
        });

        receiveGoodsDetails = (ReceiveGoodsDetails) getIntent().getSerializableExtra("OBJ");


        toolbarInitilisation();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recyclerView = findViewById(R.id.llspinners);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        receiveGoodsOfflineModel = new ReceiveGoodsOfflineModel();

        rcoffline.comm="select";
        rcoffline.enter="select";
        rcoffline.sch="select";
        rcoffline.text="select";
        receiveGoodsOfflineModel.rcoffline.add(0,rcoffline);

        add();

        String truckID = getIntent().getStringExtra("truck");
        String vehicleID = getIntent().getStringExtra("vehicle");
        String challanID = getIntent().getStringExtra("challan");

        System.out.println("@@Received truck ID: " +truckID);
        System.out.println("@@Received vehicle ID: " +vehicleID);
        System.out.println("@@Received challan ID: " +challanID);


        Coms  = new ArrayList();
        Schemes  = new ArrayList();
        Qts  = new ArrayList();

        comNames = new ArrayList();

        comNames = databaseHelper.getCommodities();

        System.out.println("@@No of commodities available: " +comNames.size());
        for(int i=0;i<comNames.size();i++)
        {
            System.out.println("@@" +comNames.get(i));
        }
        System.out.println("******************************");

        schemeNames = new ArrayList();
        schemeNames = databaseHelper.getSchemeName();

        System.out.println("@@No of schemes available: " +schemeNames.size());
        for(int i=0;i<schemeNames.size();i++)
        {
            System.out.println("@@" +schemeNames.get(i));
        }
        System.out.println("******************************");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePosOb() {
        System.out.println("@@In updatePosOb");
        SharedPref sharedPref = new SharedPref(context);
        //fpsCommonInfo fpsCommonInfoData = dealerConstants.fpsCommonInfo;
        //DealerModel dealerModel = new DealerModel();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dateformat1 = new SimpleDateFormat("hh:mm:ss");
        String date = dateformat.format(new Date());
        String time = dateformat1.format(new Date());
        System.out.println(date);
        String printData[] = new String[4];
        printData[0] = "";
        printData[1] = "Offline Received Goods Receipt\n";
        printData[2] = "FPS Owner Name : "+sharedPref.getData("dealerName") +"\n";
        printData[2] = printData[2] + "FPS ID           : "+databaseHelper.getStateDetails().get(6) +"\n";
        printData[2] = printData[2] + "TRUCK CHIT NO    :" +truckNo +"\n";
        printData[2] = printData[2] + "VEHICLE NO       :" +vehicle +"\n";
        printData[2] = printData[2] + "Date :"  +date +    "   Time: " +time+"\n";
        printData[2] = printData[2] + "Allocation Month/Year :" +date.substring(3,date.length()) +"\n";
        printData[2] = printData[2] + "Commodity(Unit)  Scheme    R.Qty\n";
        printData[2] = printData[2] + "----------------------------------------------\n";

        String body = "";
        for (int i = 0; i < modeldata.size(); i++) {
            //printData[i] = "Commodity: " +modeldata.get(i).comm +"\n Quantity: " +modeldata.get(i).received +"\n";
            body = body + modeldata.get(i).comm+("(KG)");
            for(int j=modeldata.get(i).comm.length()+4;j<16;j++){
                body = body+" ";
            }
            body = body + modeldata.get(i).scheme;
            for(int j=modeldata.get(i).scheme.length();j<12;j++){
                body = body+" ";
            }
            body = body + modeldata.get(i).received +"\n";
        }

        printData[2] = printData[2] + body;
        printData[2] = printData[2] + "----------------------------------------------\n";
        printData[3] = "Thank You";

        show_AlertDialog("Confirmation",body,"",0);

        System.out.println("@@Data in model data: "+modeldata.get(0).comm);

        for (int i = 0; i < modeldata.size(); i++) {
            System.out.println("@@Adding to arrayList: "+i);
            databaseHelper.updatePosOB(modeldata.get(i).comm,modeldata.get(i).received);
        }

        try {

            checkandprint(printData,0);

            Intent intent = new Intent(offlineRecvNew.this,OfflineUploadNDownload.class);
            startActivity(intent);
            finish();
        }catch (Exception e){
            System.out.println("@@Exception: " +e.toString());
        }
        //show_AlertDialog("Message","Success","DB update success",0);
    }

    private void add() {
        System.out.println("@@In add method");
        if(count == 0) {
            ArrayList<ReceiveGoodsOfflineListModel> arrayList = new ArrayList<>();
            int i=0;
            //for (int i = 0; i < receiveGoodsOfflineModel.rcoffline.size(); i++) {
            System.out.println("@@Adding to arrayList: "+i);
            arrayList.add(new ReceiveGoodsOfflineListModel(
                    receiveGoodsOfflineModel.rcoffline.get(i).comm,
                    receiveGoodsOfflineModel.rcoffline.get(i).sch,
                    receiveGoodsOfflineModel.rcoffline.get(i).enter,
                    receiveGoodsOfflineModel.rcoffline.get(i).text));
            //}

            adapter = new ReceiveGoodsListOfflineAdapter(context, arrayList, new OnClickReceived() {
                @Override
                public void onClick(int listPosition) {
                    /*EnterComm(listPosition);*/
                    System.out.println("@@onclick received....");
                    input_Alert(listPosition);
                }
            });

            recyclerView.setAdapter(adapter);
            count++;
        }else{
            System.out.println("@@In else setting adapter");
            addDetails(receiveGoodsOfflineModel.rcoffline.get(0).comm,receiveGoodsOfflineModel.rcoffline.get(1).sch,receiveGoodsOfflineModel.rcoffline.get(0).enter,receiveGoodsOfflineModel.rcoffline.get(0).text);
        }
    }

    private void addDetails(String com,String scheme,String qty,String unit) {
        System.out.println("@@In addDetails: " +com +scheme +qty +unit);

        //ArrayList<ReceiveGoodsOfflineListModel> arrayList = new ArrayList<>();
        //for (int i = 0; i < receiveGoodsOfflineModel.rcoffline.size(); i++) {
        System.out.println("@@Adding: " +qty);
        System.out.println("@@Adding: nn"+receiveGoodsOfflineModel.rcoffline.get(0).enter);

        //arrayList.add(new ReceiveGoodsOfflineListModel(com,scheme,qty,unit));
        modeldata.add(new ReceiveGoodsOfflineListModel("select","select","Qty","KG"));
        //}

        adapter = new ReceiveGoodsListOfflineAdapter(context, modeldata, new OnClickReceived() {
            @Override
            public void onClick(int listPosition) {
                System.out.println("@@onclick received....");
                input_Alert(listPosition);
            }
        });
        recyclerView.setAdapter(adapter);
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
            System.out.println("@@App version: "+appversion);
            toolbarVersion.setText("V" + appversion);


            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            toolbarActivity.setText( context.getResources().getString(R.string.CASH_PDS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {

            Timber.tag("CashPDS-ToolBar-").e(ex.getMessage(), "");
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
                //if (i == 1) {
                finish();
                //}
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public void input_Alert(int pos)
    {
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog builder = new Dialog(this);
        builder.setContentView(R.layout.dialog_offline_receivegoods);

        Button Confirm = (Button) builder.findViewById(R.id.btn_confirm);
        Button Cancel = (Button) builder.findViewById(R.id.btn_Cancel);

        Spinner spinner = (Spinner) builder.findViewById(R.id.spinnerCom);
        spinner.setMinimumWidth(250);
        spinner.setAdapter(new ArrayAdapter<String>(offlineRecvNew.this, android.R.layout.simple_spinner_dropdown_item,comNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                System.out.println("@@Item selected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        final Spinner spinner1 = (Spinner) builder.findViewById(R.id.spinnerSch);
        spinner1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,schemeNames));
        spinner1.setMinimumWidth(250);

        final EditText qty = (EditText) builder.findViewById(R.id.etQty);
        qty.setMinimumWidth(250);

        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("@@Clicked on confirm");
                String com = spinner.getSelectedItem().toString();
                String scheme = spinner1.getSelectedItem().toString();
                String quantity = qty.getText().toString();

                System.out.println("@@Commodity: " +com);
                System.out.println("@@Scheme: " +scheme);
                System.out.println("@@Quantity: " +quantity );


                //int tcCommDetailssize=receiveGoodsOfflineModel.rcoffline.size();
        /*if (position==-1){
            tcCommDetailssize=0;
        }*/
                //for (int k = 0; k <tcCommDetailssize ; k++) {
                System.out.println("@@Adding model data in position: " +pos);
                rcoffline.comm=com;
                rcoffline.sch=scheme;
                rcoffline.enter=quantity;
                rcoffline.text="KG";
                //modeldata.add(new ReceiveGoodsOfflineListModel(com,scheme,quantity,"KG"));
                if(count == 1) {
                    modeldata.add(pos, new ReceiveGoodsOfflineListModel(com, scheme, quantity, "KG"));

                    count++;
                }else {
                    modeldata.set(pos, new ReceiveGoodsOfflineListModel(com, scheme, quantity, "KG"));

                }
                System.out.println("@@Adding at posoition: " +pos +rcoffline.comm +rcoffline.enter);
                receiveGoodsOfflineModel.rcoffline.add(pos,rcoffline);

                //}

                adapter = new ReceiveGoodsListOfflineAdapter(context,modeldata,new offlineRecvNew.OnClickReceived(){
                    @Override
                    public void onClick(int p)
                    {
                        input_Alert(p);
                    }
                });

                recyclerView.setAdapter(adapter);
                builder.dismiss();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("@@Cancel clicked");
                builder.dismiss();
            }
        });
        builder.show();

    }

    @Override
    public void OnOpen() {

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

    public interface OnClickReceived {

        void onClick(int listPosition);
    }

}