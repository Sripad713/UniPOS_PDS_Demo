package com.visiontek.Mantra.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.ScrollTextView;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.MemberName;
import static com.visiontek.Mantra.Models.AppConstants.MemberUid;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.image;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class SuccessActivity extends AppCompatActivity implements PrinterCallBack {
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    private SuccessActivity mActivity;
    private  String ACTION_USB_PERMISSION;
    public int flag_print;


    private int selectedIndex = -1;
    public int k = 1;
    ProgressDialog pd = null;
    TableLayout tableLayout;
    TableRow tableRow;
    private AlertDialog.Builder builder;
    private TextView txt1, txt2, txt3, txt4, txt5;
    //ImpdsBean impdsBean;

    private String rcid, district_name, home_state_name, sale_fps_id, alloc_month, alloc_year,
            uid_refer_no, totalamount, memberName, sale_state_name, home_dist_name, sale_dist_name,
            receiptId, scheme_name;

    JSONObject response = null;
    String[] CMonths = {"January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"};
    NumberFormat rsformat = new DecimalFormat("#0.00");
    Context context;
    Button Print;
    StringBuilder add = new StringBuilder();


    private void checkandprint(String[] str, int i) {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    Intent home = new Intent(context, IMPDSActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                    finish();
                }else {
                    printbox(str,i);
                }
            }
        }catch (Exception ex){

            Timber.tag("Print-Battery-").e(ex.getMessage(),"");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
//        location.beginUpdates();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    private void image(String content, String name,int align) {
        try {
            Util.image(content,name,align);
        }catch (Exception ex){

            Timber.tag("Print-Image-").e(ex.getMessage(),"");
        }

    }
    private void showMessageDialogue() {
        new AlertDialog.Builder(SuccessActivity.this)
                .setCancelable(false)
                .setTitle("Alert")
                .setMessage("Unable to fetch receipt details!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  dialog.cancel();
                        onBackPressed();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent dashboard = new Intent(getBaseContext(), IMPDSActivity.class);
        dashboard.addCategory(Intent.CATEGORY_HOME);
        dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboard);
        SuccessActivity.this.finish();
        super.onBackPressed();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        context=SuccessActivity.this;

        pd = new ProgressDialog(context);
        flag_print = 0;

        mActivity = this;
        ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
        toolbarInitilisation();
        Print=findViewById(R.id.print);

        if (!networkConnected(context)) {

            builder.setTitle("Internet Connection");
            builder.setMessage("Please Check Your Internet Connection").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.toolbar));//33A1C9
        }

        tableLayout = findViewById(R.id.commodity_table);
        tableLayout.setStretchAllColumns(true);


        builder = new AlertDialog.Builder(SuccessActivity.this);
        try {
            // impdsBean = ImpdsBean.getInstance();
            Intent intent = getIntent();
            String jsonObject1 = intent.getStringExtra("saleresponse");
            response = new JSONObject(jsonObject1);
            String vendor = ImpdsBean.getInstance().getVendor();
            System.out.println(vendor+"---response_queue===" + response.toString());

            home_state_name = ImpdsBean.getInstance().getHomeStateName();
            sale_state_name = ImpdsBean.getInstance().getSaleStateName();
            home_dist_name = ImpdsBean.getInstance().getHomeDistName();
            sale_dist_name = ImpdsBean.getInstance().getSaleDistName();
            scheme_name = ImpdsBean.getInstance().getSchemeName();
            totalamount = ImpdsBean.getInstance().getTotal_amount();
            alloc_month = ImpdsBean.getInstance().getAllocation_month();
            alloc_year = ImpdsBean.getInstance().getAllocation_year();

            rcid = response.getString("rcId");
            receiptId = response.getString("receiptId");
            uid_refer_no = response.getString("uidRefNumber");
            sale_fps_id = response.getString("saleFpsId");
            memberName = response.getString("memberName");


            TextView home_state_text = findViewById(R.id.home_state_text);
            TextView text_dist_name = findViewById(R.id.home_dist_name);
            TextView sale_state_text = findViewById(R.id.sale_state_name);
            TextView sale_dist_text = findViewById(R.id.sale_dist_name);
            TextView sale_fps_text = findViewById(R.id.text_fps_id);
            TextView member_name = findViewById(R.id.text_member_name);
            TextView rc_id = findViewById(R.id.text_card_id);
            TextView ref_id = findViewById(R.id.text_ref_id);
            TextView trx_date = findViewById(R.id.text_trx_date);
            TextView text_alloc_month = findViewById(R.id.text_alloc_month);
            TextView text_alloc_year = findViewById(R.id.text_alloc_year);
            TextView rct_id = findViewById(R.id.text_rec_id);

            TextView total_amount = findViewById(R.id.text_total_amount);

            home_state_text.setText(home_state_name);
            sale_state_text.setText(sale_state_name);
            text_dist_name.setText(home_dist_name);
            sale_dist_text.setText(sale_dist_name);
            sale_fps_text.setText(sale_fps_id);
            member_name.setText(memberName);
            rc_id.setText(rcid + "(" + scheme_name + ")");
            ref_id.setText(uid_refer_no);

            rct_id.setText(receiptId);
            text_alloc_month.setText(CMonths[Integer.parseInt(alloc_month) - 1]);
            text_alloc_year.setText(alloc_year);
            total_amount.setText(rsformat.format(Double.parseDouble(totalamount)));

            JSONArray transactionList = response.getJSONArray("transactionList");
            System.out.println("");;
            // JSONArray transactionList = response.getJSONArray("transactionList");
            String app;

            if (transactionList != null && transactionList.length() > 0) {
                for (int i = 0; i < transactionList.length(); i++) {
                    JSONObject object = transactionList.getJSONObject(i);

                    String commodityName = object.getString("commodityName");
                    String availedQuantity = object.getString("availedQuantity");
                    String amount = object.getString("amount");
                    String totalQuantity = object.getString("totalQuantity");
                    String price = object.getString("pricePerKg");

                    app =  String.format("%-10s%-8s%-8s%-8s\n",
                            commodityName,
                            availedQuantity,
                            price,
                            amount
                    );



                    add.append(app);

                    txt1 = new TextView(SuccessActivity.this);
                    txt2 = new TextView(SuccessActivity.this);
                    txt3 = new TextView(SuccessActivity.this);
                    txt4 = new TextView(SuccessActivity.this);
                    txt5 = new TextView(SuccessActivity.this);

                    txt1.setText(commodityName);
                    txt1.setPadding(8, 8, 8, 8);
                    txt1.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt1.setGravity(Gravity.CENTER);
                    txt1.setTextSize(18);
                    txt1.setTextColor(Color.parseColor("#000000"));

                    txt2.setText(totalQuantity);
                    txt2.setPadding(8, 8, 8, 8);
                    txt2.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt2.setGravity(Gravity.CENTER);
                    txt2.setTextSize(18);
                    txt2.setTextColor(Color.parseColor("#000000"));

                    txt3.setText(availedQuantity);
                    txt3.setPadding(8, 8, 8, 8);
                    txt3.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt3.setGravity(Gravity.CENTER);
                    txt3.setTextSize(18);
                    txt3.setTextColor(Color.parseColor("#000000"));

                    txt4.setText(rsformat.format(Double.parseDouble(price)));
                    txt4.setPadding(8, 8, 8, 8);
                    txt4.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt4.setGravity(Gravity.CENTER);
                    txt4.setTextSize(18);
                    txt4.setTextColor(Color.parseColor("#000000"));

                    txt5.setText(rsformat.format(Double.parseDouble(amount)));
                    txt5.setPadding(8, 8, 8, 8);
                    txt5.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt5.setGravity(Gravity.CENTER);
                    txt5.setTextSize(18);
                    txt5.setTextColor(Color.parseColor("#000000"));


                    tableRow = new TableRow(SuccessActivity.this);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams
                            (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tableRow.setLayoutParams(layoutParams);

                    tableRow.addView(txt1);
                    tableRow.addView(txt2);
                    tableRow.addView(txt3);
                    tableRow.addView(txt4);
                    tableRow.addView(txt5);

                    tableLayout.addView(tableRow, k);
                    k++;

                }
            }

        } catch (Exception e) {
            Dismiss();
            e.printStackTrace();
            showMessageDialogue();
        }

        mTerminal100API = new MTerminal100API();
        mTerminal100API.initPrinterAPI(this, this);
        Print.setEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            probe();
        } else {
            finish();
        }
        Print.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                if (flag_print != 2) {
                    flag_print=2;
                    String str1, str2, str3, str4, str5;
                    String[] str = new String[4];
                    if (L.equals("hi")) {
                        str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n"
                                + context.getResources().getString(R.string.IMPDS) + "\n";
                        image(str1, "header.bmp", 1);
                        str2 = context.getResources().getString(R.string.Home_State) + " : " + home_state_name + "\n"
                                /*+ context.getResources().getString(R.string.Sale_State) + " : " + sale_state_name + "\n"*/
                                + context.getResources().getString(R.string.Sale_State_FPSID) + " : " + sale_fps_id + "\n"
                                + context.getResources().getString(R.string.Receipt_No) + " : " + receiptId + "\n"
                                + context.getResources().getString(R.string.Shop_ID) + " : " + dealerConstants.stateBean.statefpsId + "\n"
                                + context.getResources().getString(R.string.Consumer_Name) + " : " + memberName + "\n"
                                + context.getResources().getString(R.string.Card_No) + " : " + rcid + "\n"
                                /*+ context.getResources().getString(R.string.Card_Type) + "    :" + +"\n"*/
                                + context.getResources().getString(R.string.UID_Ref_ID) + " : " + uid_refer_no + "\n"
                                + context.getResources().getString(R.string.AllotmentMonth) + " : " + CMonths[Integer.parseInt(alloc_month) - 1] + "\n"
                                + context.getResources().getString(R.string.AllotmentYear) + " : " + alloc_year + "\n"

                                + String.format("%-10s%-8s%-8s%-8s\n",
                                context.getResources().getString(R.string.Comm),
                                context.getResources().getString(R.string.AvailQty),
                                context.getResources().getString(R.string.price),
                                context.getResources().getString(R.string.Total));

                        str3 = (add) + "";
                        str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + " : " + rsformat.format(Double.parseDouble(totalamount)) + "\n"
                                + "\n";
                        image(str2 + str3 + str4, "body.bmp", 0);
                        str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n";
                        image(str5, "tail.bmp", 1);
                        str[0] = "1";
                        str[1] = str1;
                        str[2] = str2 + str3 + str4;
                        str[3] = str5;
                        checkandprint(str, 1);
                    } else {
                        str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n\n"
                                + context.getResources().getString(R.string.IMPDS) + "\n\n";

                        str2 = context.getResources().getString(R.string.Home_State) + "  :" + home_state_name + "\n"
                                /*+ context.getResources().getString(R.string.Sale_State) + "    :" + sale_state_name + "\n"*/
                                + context.getResources().getString(R.string.Sale_State_FPSID) + ":" + sale_fps_id + "\n"
                                + context.getResources().getString(R.string.Receipt_No) + "   :" + receiptId + "\n"
                                + context.getResources().getString(R.string.Shop_ID) + ":" + dealerConstants.stateBean.statefpsId + "\n"
                                + context.getResources().getString(R.string.Consumer_Name) + " :" + memberName + "\n"
                                + context.getResources().getString(R.string.Card_No) + "   :" + rcid + "\n"
                                /*+ context.getResources().getString(R.string.Card_Type) + "    :" + +"\n"*/
                                + context.getResources().getString(R.string.UID_Ref_ID) + " :" + uid_refer_no + "\n"
                                + context.getResources().getString(R.string.AllotmentMonth) + "   :" + CMonths[Integer.parseInt(alloc_month) - 1] + "\n"
                                + context.getResources().getString(R.string.AllotmentYear) + "    :" + alloc_year + "\n"

                                + String.format("%-10s%-8s%-8s%-8s\n",
                                context.getResources().getString(R.string.Comm),
                                context.getResources().getString(R.string.AvailQty),
                                context.getResources().getString(R.string.price),
                                context.getResources().getString(R.string.Total));

                        str3 = (add)
                                + "\n";
                        str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + "      : " + rsformat.format(Double.parseDouble(totalamount)) + "\n"
                                + "\n";

                        str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";

                        str[0] = "1";
                        str[1] = str1;
                        str[2] = str2 + str3 + str4;
                        str[3] = str5;
                        checkandprint(str, 0);
                    }
                }
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
            toolbarActivity.setText( context.getResources().getString(R.string.IMPDS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        }catch (Exception ex){

            Timber.tag("Print-Toolbar-").e(ex.getMessage(),"");
        }
    }

    public void Dismiss(){
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }
    public void Show(String title,String msg){
        SpannableString ss1=  new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2=  new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);


        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    public void OnOpen() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Print.setEnabled(true);
                // btnConnect.setEnabled(false);

            }
        });
    }

    @Override
    public void OnOpenFailed() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Print.setEnabled(false);
                //btnConnect.setEnabled(true);


            }
        });
    }

    @Override
    public void OnClose() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Print.setEnabled(false);
                if (mUsbReceiver != null) {
                    context.unregisterReceiver(mUsbReceiver);
                }probe();
            }
        });
    }

    @Override
    public void OnPrint(int i, boolean b) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mActivity.Print.setEnabled(b);

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
                    Print.setEnabled(true);
                    synchronized (this) {
                    }
                }
            }catch (Exception ex){

                Timber.tag("Print-Broadcast-").e(ex.getMessage(),"");
            }
        }
    };

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

                            Print.setEnabled(false);
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
                preventTwoClick(v);
                dialog.dismiss();
                flag_print=0;
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
}