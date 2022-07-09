package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Adapters.DailySalesListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.DailySalesListModel;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails.SaleDetails;
import com.visiontek.Mantra.Models.ReportsModel.DailySalesDetails.drBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.txnType;
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;


public class DailySalesReportActivity extends AppCompatActivity implements PrinterCallBack {
    @SuppressLint("SimpleDateFormat")
    public SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    public String ACTION_USB_PERMISSION;
    public String selecteddate;
    public String date;
    public int flag_print;
    DatabaseHelper databaseHelper;
    String saleStateFpsId;
    Calendar myCalendar;
    Button back, home, print, view;
    Context context;
    String type;
    TextView edittext;
    ProgressDialog pd = null;
    private DailySalesReportActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    private RecyclerView recyclerView;
    private ArrayList<DailySalesListModel> data;
    SaleDetails saleDetails;
    private RecyclerView.Adapter adapter;
    private void checkandprint(String[] str, int i) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Util.batterylevel(context) || Util.adapter(context)) {
                    if (mp != null) {
                        releaseMediaPlayer(context, mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = MediaPlayer.create(context, R.raw.c100191);
                        mp.start();
                    }
                    es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i));
                    finish();
                } else {
                    printbox(str,i);
                }
            }
        } catch (Exception ex) {

            Timber.tag("DailySales-Print-").e(ex.getMessage(), "");
        }
    }

    private void image(String content, String name, int align) {
        try {
            Util.image(content, name, align);
        } catch (Exception ex) {
            Timber.tag("DailySales-Image-").e(ex.getMessage(), "");
        }
    }

    private void updateLabel() {
        try {

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edittext.setText(sdf.format(myCalendar.getTime()));
        } catch (Exception ex) {

            Timber.tag("DailySales-Date-").e(ex.getMessage(), "");
        }
    }

    private void hitURL(String sale) {
        try {
        Show(context.getResources().getString(R.string.Dealers), context.getResources().getString(R.string.Fetching_Dealers));
        //pd = ProgressDialog.show(context, context.getResources().getString(R.string.Dealers), context.getResources().getString(R.string.Fetching_Dealers), true, false);
        XML_Parsing request = new XML_Parsing(context, sale, 5);
        request.setOnResultListener((code, msg, ref, flow, object) -> {
            Dismiss();
            if (code == null || code.isEmpty()) {

                show_AlertDialog(
                        context.getResources().getString(R.string.Daily_Sales_Report),
                        context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                        "",
                        0);
                return;
            }

            if (!code.equals("00")) {
                show_AlertDialog(
                        context.getResources().getString(R.string.Daily_Sales_Report),
                        context.getResources().getString(R.string.ResponseCode)+code,
                        context.getResources().getString(R.string.ResponseMsg)+msg,
                        0);

            } else {

                saleDetails = (SaleDetails) object;
                data = new ArrayList<>();
                int drBeansize = saleDetails.drBean.size();
                for (int i = 0; i < drBeansize; i++) {
                    if(L.equals("hi")){
                        data.add(new DailySalesListModel(
                                saleDetails.drBean.get(i).commNamell,
                                saleDetails.drBean.get(i).schemeName,
                                saleDetails.drBean.get(i).sale));
                    }else {
                        data.add(new DailySalesListModel(
                                saleDetails.drBean.get(i).comm_name,
                                saleDetails.drBean.get(i).schemeName,
                                saleDetails.drBean.get(i).sale));
                    }

                }
                RecyclerView.Adapter adapter = new DailySalesListAdapter(context, data);
                recyclerView.setAdapter(adapter);
                flag_print = 2;
            }
        });
        request.execute();
        } catch (Exception ex) {
            Timber.e("DailySalesReportActivity-hitURL Exception ==>"+ ex.getLocalizedMessage());
            //Timber.tag("DailySales-Date-").e(ex.getMessage(), "");
        }
    }


    @Override
    public void OnOpen() {
        print.setEnabled(true);

    }

    @Override
    public void OnOpenFailed() {

        print.setEnabled(false);
        if (mp != null) {
            releaseMediaPlayer(context, mp);
        }
        if (L.equals("hi")) {
        } else {
            mp = MediaPlayer.create(context, R.raw.c100078);
            mp.start();
        }
    }

    @Override
    public void OnClose() {

        print.setEnabled(false);
        if (mUsbReceiver != null) {
            context.unregisterReceiver(mUsbReceiver);
        }
        probe();
    }

    @Override
    public void OnPrint(final int bPrintResult, final boolean bIsOpened) {
        mActivity.runOnUiThread(() -> mActivity.print.setEnabled(bIsOpened));

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                probe();
                print.setEnabled(true);
                synchronized (this) {
                }
            }
            } catch (Exception ex) {

                Timber.tag("DailySales-broadCast-").e(ex.getMessage(), "");
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

                final UsbDevice device = deviceIterator.next();
                if ((device.getProductId() == 22304) && (device.getVendorId() == 1155)) {

                    PendingIntent mPermissionIntent = PendingIntent
                            .getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    if (!mUsbManager.hasPermission(device)) {
                        mUsbManager.requestPermission(device, mPermissionIntent);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(
                                ACTION_USB_PERMISSION);
                        context.registerReceiver(mUsbReceiver, filter);

                    } else {

                        print.setEnabled(false);
                        es.submit(() -> mTerminal100API.printerOpenTask(mUsbManager, device, context));
                    }
                }
            }
        }
        } catch (Exception ex) {

            Timber.tag("DailySales-probe-").e(ex.getMessage(), "");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily__sales__report);
        try {
            context = DailySalesReportActivity.this;
            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
            initilisation();
            flag_print = 0;
            recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            view.setOnClickListener(v -> {
                preventTwoClick(view);
                flag_print = 0;
                date = edittext.getText().toString();
                selecteddate = edittext.getText().toString();
                if (!date.equals("dd/MM/yyyy") && date.length() > 0) {
                    PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
                    if ((!type.equalsIgnoreCase("offline")) && networkConnected(context)) {
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
                        if (networkConnected(context)) {
                            if (mp != null) {
                                releaseMediaPlayer(context, mp);
                            }
                            if (L.equals("hi")) {
                            } else {
                                mp = MediaPlayer.create(context, R.raw.c100075);
                                mp.start();
                            }
                            if (Debug) {
                                Util.generateNoteOnSD(context, "DailySaleReq.txt", sale);
                            }
                            hitURL(sale);
                            Timber.d("DailySaleReportActivity-DailySaleReq :"+sale);
                        }
                    }else if (partialOnlineData.getOfflineLogin().equals("Y")) {
                            System.out.println("@@Eligible for offline");
                            saleStateFpsId = partialOnlineData.getOffPassword();
                            getOfflineRecords(date);
                        } else {
                            show_AlertDialog(context.getResources().getString(R.string.Daily_Sales_Report),
                                    context.getResources().getString(R.string.Internet_Connection),
                                    context.getResources().getString(R.string.Internet_Connection_Msg),
                                    0);
                        }
                    } else {
                        show_AlertDialog(context.getResources().getString(R.string.Daily_Sales_Report),
                                context.getResources().getString(R.string.Enter_Date),
                                context.getResources().getString(R.string.Please_Enter_date_in_Edit_text_to_view_Stock),
                                0);
                    }
                });
            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);
            print.setEnabled(false);
            probe();
            print.setOnClickListener(v -> {
                preventTwoClick(v);
                if (flag_print == 2) {
                    print.setEnabled(false);
                    preventTwoClick(view);
                    String app;
                    StringBuilder add = new StringBuilder();
                    String time = sdf1.format(new Date()).substring(0, 5);
                    String date = sdf1.format(new Date()).substring(6, 16);
                    int drBeansize = saleDetails.drBean.size();
                    for (int i = 0; i < drBeansize; i++) {
                        if(L.equals("hi")){
                            app = String.format("%-10s%-12s%-12s\n",
                                    saleDetails.drBean.get(i).commNamell,
                                    saleDetails.drBean.get(i).schemeName,
                                    saleDetails.drBean.get(i).sale);
                        }else {
                            app = String.format("%-10s%-12s%-12s\n",
                                    saleDetails.drBean.get(i).comm_name,
                                    saleDetails.drBean.get(i).schemeName,
                                    saleDetails.drBean.get(i).sale);
                        }
                        add.append(app);

                    }
                    String str1, str2, str3, str4, str5;
                    String[] str = new String[4];
                    if (L.equals("hi")) {

                        str1 = dealerConstants.stateBean.stateReceiptHeaderLl + "\n" +
                                context.getResources().getString(R.string.DAY_REPORT) + "\n";
                        image(str1, "header.bmp", 1);

                        str2 = context.getResources().getString(R.string.Date) + " : " + date + "\n" + context.getResources().getString(R.string.Time) + " :" + time + "\n"
                                + context.getResources().getString(R.string.Day_Report_Date) + " : " + date + "\n"
                                + context.getResources().getString(R.string.FPS_ID) + " : " + dealerConstants.stateBean.statefpsId + "\n\n";

                        str3 = String.format("%-13s%-13s%-13s\n",
                                context.getResources().getString(R.string.commodity),
                                context.getResources().getString(R.string.scheme),
                                context.getResources().getString(R.string.sale))
                                + "\n";

                        str4 = String.valueOf(add);
                        image(str2 + str3 + str4, "body.bmp", 0);

                        str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n";

                        image(str5, "tail.bmp", 1);
                        str[0] = "1";
                        str[1] = "1";
                        str[2] = "1";
                        str[3] = "1";

                        checkandprint(str, 1);
                    } else {

                        str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n" +
                                context.getResources().getString(R.string.DAY_REPORT) + "\n\n";
                        str2 = context.getResources().getString(R.string.Date) + "           : " + date + "\n" +
                                context.getResources().getString(R.string.Time) + "           : " + time + "\n"
                                + context.getResources().getString(R.string.Day_Report_Date) + ": " + date + "\n"
                                + context.getResources().getString(R.string.FPS_ID) + "         : " + dealerConstants.stateBean.statefpsId + "\n\n"
                                + "\n";
                        str3 = String.format("%-10s%-12s%-12s\n",
                                context.getResources().getString(R.string.commodity),
                                context.getResources().getString(R.string.scheme),
                                context.getResources().getString(R.string.sale))
                                + "\n";
                        str4 = String.valueOf(add);

                        str5 = "\n" + context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";
                        str[0] = "1";
                        str[1] = str1;
                        str[2] = str2 + str3 + str4;
                        str[3] = str5;

                        System.out.println(str1+"\n"+
                                str2+"\n"+
                                str3+"\n"+
                                str4+"\n"+
                                str5+"");
                        checkandprint(str, 0);

                    }

                }else if (flag_print == 1) {
                    System.out.println("@@flag_print = 1");
                    print.setEnabled(false);
                    preventTwoClick(view);
                    String app;
                    StringBuilder add = new StringBuilder();
                    String time = sdf1.format(new Date()).substring(0, 5);
                    String date = sdf1.format(new Date()).substring(6, 16);
                    System.out.println("@@Getting size of saledETAILS...");
                    int drBeansize = saleDetails.drBean.size();
                    System.out.println("@@Sie:" +drBeansize);
                    for (int i = 0; i < drBeansize; i++) {
                        if(L.equals("hi")){
                            app = String.format("%-10s%-12s%-12s\n",
                                    saleDetails.drBean.get(i).commNamell,
                                    saleDetails.drBean.get(i).schemeName,
                                    saleDetails.drBean.get(i).sale);
                        }else {
                            app = String.format("%-10s%-12s%-12s\n",
                                    saleDetails.drBean.get(i).comm_name,
                                    saleDetails.drBean.get(i).schemeName,
                                    saleDetails.drBean.get(i).sale);
                        }
                        add.append(app);

                    }
                    String str1, str2, str3, str4, str5;
                    String[] str = new String[4];
                    if (L.equals("hi")) {

                        str1 = databaseHelper.getStateDetails().get(5) + "\n" +
                                context.getResources().getString(R.string.DAY_REPORT) + "\n";
                        //str1 = dealerConstants.stateBean.stateReceiptHeaderLl + "\n" +context.getResources().getString(R.string.DAY_REPORT) + "\n";
                        image(str1, "header.bmp", 1);

                        str2 = context.getResources().getString(R.string.Date) + " : " + date + "\n" + context.getResources().getString(R.string.Time) + " :" + time + "\n"
                                + context.getResources().getString(R.string.Day_Report_Date) + " : " + selecteddate + "\n"
                                + context.getResources().getString(R.string.FPS_ID) + " : " + databaseHelper.getStateDetails().get(6) + "\n\n";

                        str3 = String.format("%-13s%-13s%-13s\n",
                                context.getResources().getString(R.string.commodity),
                                context.getResources().getString(R.string.scheme),
                                context.getResources().getString(R.string.sale))
                                + "\n";

                        str4 = String.valueOf(add);
                        image(str2 + str3 + str4, "body.bmp", 0);

                        str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";

                        image(str5, "tail.bmp", 1);
                        str[0] = "1";
                        str[1] = "1";
                        str[2] = "1";
                        str[3] = "1";
                        System.out.println(str1+"\n"+
                                str2+"\n"+
                                str3+"\n"+
                                str4+"\n"+
                                str5+"");
                        checkandprint(str, 1);
                    } else {

                        str1 = databaseHelper.getStateDetails().get(4) + "\n" +
                                context.getResources().getString(R.string.DAY_REPORT) + "\n\n";
                        str2 =  context.getResources().getString(R.string.Date) + "           : " + date + "\n" +
                                context.getResources().getString(R.string.Time) + "           : " + time + "\n"
                                +context.getResources().getString(R.string.Day_Report_Date) + ": " + selecteddate + "\n"
                                +context.getResources().getString(R.string.FPS_ID) + "         : " + databaseHelper.getStateDetails().get(6) + "\n\n"
                                +"\n";
                        str3 = String.format("%-10s%-12s%-12s\n",
                                context.getResources().getString(R.string.commodity),
                                context.getResources().getString(R.string.scheme),
                                context.getResources().getString(R.string.sale))
                                +"\n";
                        str4 = String.valueOf(add);

                        str5 = "\n" + context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";
                        str[0] = "1";
                        str[1] = str1;
                        str[2] = str2 + str3 + str4;
                        str[3] = str5;

                        System.out.println(str1+"\n"+
                                str2+"\n"+
                                str3+"\n"+
                                str4+"\n"+
                                str5+"");
                        checkandprint(str, 0);

                    }

                } else {
                    show_AlertDialog(context.getResources().getString(R.string.Daily_Sales_Report),
                            context.getResources().getString(R.string.Date),
                            context.getResources().getString(R.string.Enter_Date_to_view_sales),
                            0);
                }
        });


        home.setOnClickListener(v -> {
                preventTwoClick(view);
                Intent home = new Intent(context, HomeActivity.class);
                startActivity(home);
                finish();
            });
            back.setOnClickListener(v -> finish());


            myCalendar = Calendar.getInstance();


            final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            };

            edittext.setOnClickListener(v -> new DatePickerDialog(DailySalesReportActivity.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show());
        } catch (Exception ex) {

            Timber.tag("DailySales-onCreate-").e(ex.getMessage(), "");
        }
    }

    public void getOfflineRecords(final String enteredDate)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    saleDetails = new SaleDetails();
                    SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = parser.parse(enteredDate);
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

                    saleDetails.drBean =
                            (ArrayList<drBean>) databaseHelper.getofflineSaleRecords(formattedDate);
                    Dealername = databaseHelper.getOfflineDealerName();
                    DailySalesReportActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            parseAdapterData(saleDetails);
                        }
                    });

                } catch (ParseException e) {
                    System.out.println("@@Exception: " +e.toString());
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void parseAdapterData(SaleDetails saleDetails)
    {
        data = new ArrayList<>();
        int drBeansize = saleDetails.drBean.size();
        for (int i = 0; i < drBeansize; i++) {
            data.add(new DailySalesListModel(saleDetails.drBean.get(i).comm_name,saleDetails.drBean.get(i).schemeName,saleDetails.drBean.get(i).sale));

        }
        adapter = new DailySalesListAdapter(context, data);
        recyclerView.setAdapter(adapter);
        flag_print = 1;
    }


    private void initilisation() {
        pd = new ProgressDialog(context);
        databaseHelper = new DatabaseHelper(context);
        back = findViewById(R.id.sale_back);
        home = findViewById(R.id.sale_home);
        print = findViewById(R.id.sale_print);
        view = findViewById(R.id.sale_view);
        edittext = findViewById(R.id.sale_date);
        String date = sdf1.format(new Date()).substring(6, 16);
        edittext.setText(date);
        type = getIntent().getStringExtra("type");
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
            toolbarActivity.setText( context.getResources().getString(R.string.Daily_Sales_Report));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {
            Timber.tag("DailySales-Toolbar-").e(ex.getMessage(), "");
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
        confirm.setOnClickListener(v -> dialog.dismiss());
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        confirm.setOnClickListener(v -> {
            dialog.dismiss();
            checkandprint(str,type);
        });
        back.setOnClickListener(v -> dialog.dismiss());

        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
}
