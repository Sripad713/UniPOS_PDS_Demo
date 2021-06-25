package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.visiontek.Mantra.Adapters.InspectionListAdapter;
import com.visiontek.Mantra.Models.DATAModels.InspectionListModel;
import com.visiontek.Mantra.Models.InspectionModel.InspectionAuth;
import com.visiontek.Mantra.Models.InspectionModel.InspectionDetails;
import com.visiontek.Mantra.Models.RDModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Utils.Util.ConsentForm;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.checkdotvalue;
import static com.visiontek.Mantra.Utils.Util.encrypt;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Util.toast;
import static com.visiontek.Mantra.Utils.Veroeff.validateVerhoeff;

public class InspectionActivity extends AppCompatActivity implements PrinterCallBack {
    String ACTION_USB_PERMISSION;

    int RD_SERVICE = 0;
    String errcode = "1";

    String fCount;
    Context context;
    ProgressDialog pd = null;
    RadioGroup radioGroup;
    RadioButton ok, seized;
    int select;
    ArrayList<InspectionListModel> data;

    String DATA;
    Float textdata;
    float cb;
    float var;
    String AFTERDATA;
    Button next, back;
    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    String Ivendor, Iname, Itrans;
    String com;
    String approval;
    String Iref;
    String Aadhaar;


    String Enter_UID;

    private InspectionActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    private final String iCount = "0";
    private final String fType = "0";
    private final String iType = "0";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                probe();
                // btnConnect.performClick();
                Toast.makeText(context, context.getResources().getString(R.string.ConnectUSB), Toast.LENGTH_LONG).show();
                //last.setEnabled(true);
                synchronized (this) {

                }
            }
        }
    };


    RDModel rdModel = new RDModel();
    InspectionDetails inspectionDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        context = InspectionActivity.this;
        mActivity = this;
        ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg), context.getResources().getString(R.string.RD_Service), 0);
            return;
        }
        initilisation();
        radioGroup = findViewById(R.id.groupradio);
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        inspectionDetails = (InspectionDetails) getIntent().getSerializableExtra("OBJ");

        mTerminal100API = new MTerminal100API();
        mTerminal100API.initPrinterAPI(this, this);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            probe();
        } else {
            finish();
        }
        Display(1);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check()) {
                    fCount = "1";
                    Enter_UID();
                }else {
                    show_error_box("Please Select atleast one Observation", context.getResources().getString(R.string.Internet_Connection), 0);

                }
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

    }

    private boolean check() {
        int size=inspectionDetails.commDetails.size();
        float val;
        for (int i=0;i<size;i++){
            val= Float.parseFloat(inspectionDetails.commDetails.get(i).entered);
            System.out.println("==========="+val);
            if (val>0.0){
                System.out.println("="+val);
                return true;
            }
        }
        return false;
    }

    private void initilisation() {
        pd = new ProgressDialog(context);

        next = findViewById(R.id.inspection_next);
        back = findViewById(R.id.inspection_back);
        toolbarInitilisation();
    }


    private void hit_inspectioAuth(String inspectionAuth) {
        if (select == 2) {
            app("Seized");
        } else {
            app("OK");
        }
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.COMMODITIES), context.getResources().getString(R.string.Commodity_details_are_updating), true, false);
        Aadhaar_Parsing request = new Aadhaar_Parsing(context, inspectionAuth, 6);
        request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String error, String msg, String ref, String flow, Object object) {

                if (pd.isShowing()) {
                    pd.dismiss();
                }

                if (error == null || error.isEmpty()) {
                    show_error_box("Invalid Response from Server", "No Response", 0);
                    return;
                }
                if (!error.equals("00")) {
                    System.out.println("ERRORRRRRRRRRRRRRRRRRRRR");
                    show_error_box(msg, "Member Details: " + error, 0);
                } else {
                    InspectionAuth inspectionAuth = (InspectionAuth) object;

                    Ivendor = inspectionAuth.inspectorDesignation;
                    Iname = inspectionAuth.inspectorName;
                    Itrans = inspectionAuth.auth_transaction_code;
                    com = addComm();
                    if (!com.equals("1")) {
                        String Inspectionpush = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<SOAP-ENV:Envelope\n" +
                                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                                "    <SOAP-ENV:Body>\n" +
                                "        <ns1:inspPushCBData>\n" +
                                "            <fpsId>" + dealerConstants.fpsCommonInfo.fpsId + "</fpsId>\n" +
                                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                                "            <approvalStatus>" + approval + "</approvalStatus>\n" +
                                com +
                                "            <inspUid>" + Enter_UID + "</inspUid>\n" +
                                "        </ns1:inspPushCBData>\n" +
                                "    </SOAP-ENV:Body>\n" +
                                "</SOAP-ENV:Envelope>";
                        if (Util.networkConnected(context)) {
                            hitpush(Inspectionpush);
                            Util.generateNoteOnSD(context, "InspectionPushReq.txt", Inspectionpush);
                        } else {
                            show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection), 0);
                        }
                    } else {
                        toast(context, context.getResources().getString(R.string.Invalid_Inputs));
                    }
                }
            }
        });
        request.execute();

    }

    private void ConsentDialog(String concent) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.consent);
        Button confirm = (Button) dialog.findViewById(R.id.agree);
        Button back = (Button) dialog.findViewById(R.id.cancel);
        TextView tv = (TextView) dialog.findViewById(R.id.consent);
        tv.setText(concent);
        final CheckBox checkBox =(CheckBox) dialog.findViewById(R.id.check);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    dialog.dismiss();
                    connectRDservice();
                }  else {
                    show_error_box("Please Check the Consent Message",context.getResources().getString(R.string.Consent_Form), 2);
                }

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
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void ConsentformURL(String consentrequest) {
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Inspection), context.getResources().getString(R.string.Consent_Form), true, false);
        Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
        request.setOnResultListener(new Json_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (!code.equals("00")) {
                    show_error_box(msg, code, 0);
                } else {
                    show_error_box(msg, code, 0);
                }
            }

        });

    }

    private void app(String ok) {
        int size = inspectionDetails.approvals.size();
        for (int i = 0; i < size; i++) {
            if (inspectionDetails.approvals.get(i).approveValue.equals(ok)) {
                approval = inspectionDetails.approvals.get(i).approveKey;
            }
        }
    }

    public void onRadioButtonClicked(View v) {
        ok = findViewById(R.id.ok);
        seized = findViewById(R.id.seized);
        boolean checked = ((RadioButton) v).isChecked();
        if (checked) {
            switch (v.getId()) {
                case R.id.ok:
                    select = 1;
                    ok.setTypeface(null, Typeface.BOLD_ITALIC);
                    seized.setTypeface(null, Typeface.NORMAL);

                    break;

                case R.id.seized:
                    select = 2;
                    ok.setTypeface(null, Typeface.NORMAL);
                    seized.setTypeface(null, Typeface.BOLD_ITALIC);

                    break;

            }
        }
    }

    private void hitpush(String inspectionpush) {
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.COMMODITIES), context.getResources().getString(R.string.Commodity_details_are_updating), true, false);
        Aadhaar_Parsing request = new Aadhaar_Parsing(context, inspectionpush, 7);
        request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompleted(String error, String msg, String ref, String flow, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (error == null || error.isEmpty()) {
                    show_error_box("Invalid Response from Server", "No Response", 0);
                    return;
                }
                if (!error.equals("00")) {
                    System.out.println("ERRORRRRRRRRRRRRRRRRRRRR");
                    show_error_box(msg, context.getResources().getString(R.string.Commodities_Error) + error, 0);
                } else {
                    Iref = ref;
                    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    StringBuilder add = new StringBuilder();
                    String app;
                    int size = inspectionDetails.commDetails.size();
                    for (int i = 0; i < size; i++) {

                        app =  String.format("%-8s%-8s%-8s%-8s\n",
                                inspectionDetails.commDetails.get(i).commNameEn,
                                inspectionDetails.commDetails.get(i).closingBalance,
                                inspectionDetails.commDetails.get(i).entered,
                                inspectionDetails.commDetails.get(i).variation);
                        add.append(app);

                    }
                    String str1, str2, str3, str4, str5;
                    String[] str = new String[4];
                    if (L.equals("hi")) {
                        str1 = context.getResources().getString(R.string.Inspection) + "\n" +
                                context.getResources().getString(R.string.Receipt) + "\n";
                        image(str1, "header.bmp", 1);
                        str2 = context.getResources().getString(R.string.FPS_ID) + dealerConstants.fpsCommonInfo.fpsId + "\n"
                                + context.getResources().getString(R.string.TransactionID) + Iref + "\n\n"
                                + context.getResources().getString(R.string.Inspected_By) + Iname + "\n"
                                + context.getResources().getString(R.string.Designation) + Ivendor + "\n";
                        str3 = context.getResources().getString(R.string.Date) + currentDateTimeString + "\n\n"
                                + context.getResources().getString(R.string.Status) + approval + " \n"
                                + context.getResources().getString(R.string.Commidity_CB_Obsevn_Varitn) + "\n";

                        str4 = add + "";
                        image(str2 + str3 + str4, "body.bmp", 0);
                        str5 = context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n";

                        image(str5,"tail.bmp",1);
                        str[0] = "1";
                        str[1] = "1";
                        str[2] = "1";
                        str[3] = "1";
                        checkandprint(str, 1);
                    } else {

                        str1 = context.getResources().getString(R.string.Inspection) + "\n" +
                                context.getResources().getString(R.string.Receipt) + "\n\n";
                        str2 =    context.getResources().getString(R.string.FPS_ID) +"       :"+ dealerConstants.fpsCommonInfo.fpsId + "\n"
                                + context.getResources().getString(R.string.TransactionID) +":"+ Iref + "\n"
                                + context.getResources().getString(R.string.Inspected_By) +" :"+ Iname + "\n"
                                + context.getResources().getString(R.string.Designation) +"  :"+ Ivendor + "\n"
                                + context.getResources().getString(R.string.Date) +" : "+currentDateTimeString + "\n"
                                + context.getResources().getString(R.string.Status) +"       :"+approval + " \n";
                        str3 = String.format("%-8s%-8s%-8s%-8s\n",
                                "CommName",
                                context.getResources().getString(R.string.ClBal),
                                "Obs",
                                "Var");
                        str4 = String.valueOf(add);
                        str5 = "\n"+context.getResources().getString(R.string.Public_Distribution_Dept)+"\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs)+"\n\n\n\n";
                        str[0] = "1";
                        str[1] = str1;
                        str[2] = str2 + str3 + str4;
                        str[3] = str5;
                        checkandprint(str, 0);

                    }

                }
            }
        });
        request.execute();
    }

    private String addComm() {
        StringBuilder add = new StringBuilder();
        String str;
        int commsize = inspectionDetails.commDetails.size();
        if (commsize > 0) {
            for (int i = 0; i < commsize; i++) {
                str = "<inspCBUpdate>\n" +
                        "                <closingBalance>" + inspectionDetails.commDetails.get(i).closingBalance + "</closingBalance>\n" +
                        "                <commCode>" + inspectionDetails.commDetails.get(i).commCode + "</commCode>\n" +
                        "                <observedClosingBalance>" + inspectionDetails.commDetails.get(i).entered + "</observedClosingBalance>\n" +
                        "                <variation>" + inspectionDetails.commDetails.get(i).variation + "</variation>\n" +
                        "</inspCBUpdate>\n";
                add.append(str);
            }
            return String.valueOf(add);
        } else {
            return "0";
        }
    }

    private void Enter_UID() {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.uid);
        Button back = (Button) dialog.findViewById(R.id.back);
        Button confirm = (Button) dialog.findViewById(R.id.confirm);
        TextView tv = (TextView) dialog.findViewById(R.id.status);
        final EditText enter = (EditText) dialog.findViewById(R.id.enter);
        tv.setText("Please Enter Inspector UID");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Enter_UID = enter.getText().toString();
                if (Enter_UID.length() == 12 && validateVerhoeff(Enter_UID)) {
                    try {
                        Aadhaar = encrypt(Enter_UID, menuConstants.skey);

                        if (Util.networkConnected(context)) {
                            ConsentDialog(ConsentForm(context));
                        } else {
                            show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection), 0);
                        }

                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (mp != null) {
                        releaseMediaPlayer(context, mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = mp.create(context, R.raw.c100047);
                        mp.start();
                    }
                    show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_UID), 0);
                }

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
       /* AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        alert.setMessage(context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID));
        alert.setTitle(context.getResources().getString(R.string.Enter_UID));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(12);
        edittext.setFilters(FilterArray);
        alert.setView(edittext);
        alert.setPositiveButton(context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();*/

    }


    private void prep_consent() {
        System.out.println("@@ In dealer details else case");
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        //currentDateTimeString="26032021114610";
        String consentrequest="{\n" +
                "   \"fpsId\" : "+"\""+dealerConstants.stateBean.statefpsId+"\""+",\n" +
                "   \"modeOfService\" : \"D\",\n" +
                "   \"moduleType\" : \"C\",\n" +
                "   \"rcId\" : "+"\""+dealerConstants.stateBean.statefpsId+"\""+",\n" +
                "   \"requestId\" : \"0\",\n" +
                "   \"requestValue\" : \"N\",\n" +
                "   \"sessionId\" : "+"\""+dealerConstants.fpsCommonInfo.fpsSessionId+"\""+",\n" +
                "   \"stateCode\" : "+"\""+dealerConstants.stateBean.stateCode+"\""+",\n" +
                "   \"terminalId\" : "+"\""+DEVICEID+"\""+",\n" +
                "   \"timeStamp\" : "+"\""+currentDateTimeString+"\""+",\n" +
                /*"   \"token\" : "+"\""+fpsURLInfo.token()+"\""+"\n" +*/
                "   \"token\" : "+"\"9f943748d8c1ff6ded5145c59d0b2ae7\""+"\n" +
                "}";
        Util.generateNoteOnSD(context, "ConsentFormReq.txt", consentrequest);
        ConsentformURL(consentrequest);
    }

    private void connectRDservice() {
        try {
            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
                mp = mp.create(context, R.raw.c200032);
                mp.start();

            } else {
                mp = mp.create(context, R.raw.c100032);
                mp.start();

            }
            String xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<PidOptions ver =\"1.0\">\n" +
                    "    <Opts env=\"P\" fCount=\"" + fCount + "\" iCount=\"" + iCount + "\" iType=\"" + iType + "\" fType=\"" + fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"\" posh=\"UNKNOWN\"/>\n" +
                    "</PidOptions>";
            Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            try {
                System.out.println(activities);
                for (int i = 0; i < activities.size(); i++) {
                    System.out.println(">  >>>>>>> i=" + i + "," + activities.get(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
                String msg = String.valueOf(e);
                show_error_box(msg, context.getResources().getString(R.string.RD_SERVICE_ERROR), 0);
            }
            final boolean isIntentSafe = activities.size() > 0;
            System.out.println("Boolean check for activities = " + isIntentSafe);
            if (!isIntentSafe) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.No_RD_Service_Available), Toast.LENGTH_SHORT).show();
            }
            System.out.println("No of activities = " + activities.size());
            act.putExtra("PID_OPTIONS", xmplpid);
            startActivityForResult(act, RD_SERVICE);
        } catch (Exception e) {
            System.out.println("Error while connecting to RDService");
            e.printStackTrace();
        }
    }

    private void xml_Frame() {
        String InspectionAuth = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"http://www.uidai.gov.in/authentication/uid-auth-request/2.0\"\n" +
                "    xmlns:ns2=\"http://service.fetch.rationcard/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns2:getAuthenticateNICAuaInspectionRD2>\n" +
                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                "            <User_Id>" + dealerConstants.fpsCommonInfo.fpsId + "</User_Id>\n" +
                "            <uidNumber>" + Aadhaar + "</uidNumber>\n" +
                "            <udc>" + DEVICEID + "</udc>\n" +
                "            <authMode>V</authMode>\n" +
                "            <auth_packet>\n" +
                "                <ns1:certificateIdentifier>" + rdModel.ci + "</ns1:certificateIdentifier>\n" +
                "                <ns1:dataType>X</ns1:dataType>\n" +
                "                <ns1:dc>" + rdModel.dc + "</ns1:dc>\n" +
                "                <ns1:dpId>" + rdModel.dpId + "</ns1:dpId>\n" +
                "                <ns1:encHmac>" + rdModel.hmac + "</ns1:encHmac>\n" +
                "                <ns1:mc>" + rdModel.mc + "</ns1:mc>\n" +
                "                <ns1:mid>" + rdModel.mi + "</ns1:mid>\n" +
                "                <ns1:rdId>" + rdModel.rdsId + "</ns1:rdId>\n" +
                "                <ns1:rdVer>" + rdModel.rdsVer + "</ns1:rdVer>\n" +
                "                <ns1:secure_pid>" + rdModel.pid + "</ns1:secure_pid>\n" +
                "                <ns1:sessionKey>" + rdModel.skey + "</ns1:sessionKey>\n" +
                "            </auth_packet>\n" +
                "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                "            <Resp>\n" +
                "                <errCode>0</errCode>\n" +
                "                <errInfo>y</errInfo>\n" +
                "                <nmPoints>" + rdModel.nmpoint + "</nmPoints>\n" +
                "                <fCount>" + rdModel.fcount + "</fCount>\n" +
                "                <fType>" + rdModel.ftype + "</fType>\n" +
                "                <iCount>" + rdModel.icount + "</iCount>\n" +
                "                <iType>" + rdModel.itype + "</iType>\n" +
                "                <pCount>0</pCount>\n" +
                "                <pType>0</pType>\n" +
                "                <qScore>0</qScore>\n" +
                "            </Resp>\n" +
                "        </ns2:getAuthenticateNICAuaInspectionRD2>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";


        Util.generateNoteOnSD(context, "InspectionAuthReq.txt", InspectionAuth);
        if (networkConnected(context)) {

            hit_inspectioAuth(InspectionAuth);
        } else {
            show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection), 0);
        }
    }

   /* private boolean replcae(int p) {

        System.out.println("p value = " + p);
        for (int i = 0; i < place.size(); i++) {
            System.out.println("I value = " + i);
            System.out.println("PLACE = " + place.get(i));
            System.out.println("PLACE SIZE = " + place.size());
            if (p == place.get(i)) {
                System.out.println("ENTERED SIZE = " + entered.size());
                System.out.println("REPLACING VALUES at " + i);
                entered.set(i, DATA);
                variation.set(i, AFTERDATA);
                updatedcb.set(i, closingBalance.get(p));
                updatedname.set(i, commNameEn.get(p));
                updatedcode.set(i, commNameEn.get(p));
                return true;
            }
        }
        return false;
    }*/

    private void EnterComm(final int position) {
        try {
            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.inspection);
            final EditText observation = dialog.findViewById(R.id.enter);
            Button confirm = (Button) dialog.findViewById(R.id.confirm);
            Button back = (Button) dialog.findViewById(R.id.back);

            TextView name = (TextView) dialog.findViewById(R.id.a);
            TextView bal = (TextView) dialog.findViewById(R.id.b);
            TextView obs = (TextView) dialog.findViewById(R.id.c);
            TextView vari = (TextView) dialog.findViewById(R.id.d);

            TextView status = (TextView) dialog.findViewById(R.id.status);
            status.setText("Please Enter Observation ");

            name.setText(inspectionDetails.commDetails.get(position).commNameEn);
            bal.setText(inspectionDetails.commDetails.get(position).closingBalance);
            obs.setText(inspectionDetails.commDetails.get(position).entered);
            vari.setText(inspectionDetails.commDetails.get(position).variation);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    String Check = observation.getText().toString();
                    if (!Check.isEmpty()&& Check!=null&& Check.length()>0 ) {
                        if (checkdotvalue(Check)) {
                            textdata = Float.parseFloat(Check);
                            cb = Float.parseFloat(inspectionDetails.commDetails.get(position).closingBalance);
                            if (textdata <= cb && textdata >= 0) {
                                var = (Float) (cb - textdata);
                                if (var >= 0 && var <= cb) {
                                    System.out.println("----------------0");
                                    AFTERDATA = String.valueOf(var);
                                    DATA = String.valueOf(textdata);
                                    inspectionDetails.commDetails.get(position).entered = DATA;
                                    inspectionDetails.commDetails.get(position).variation = AFTERDATA;
                                    data.clear();
                                    Display(0);
                                }
                            } else {
                                show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_Quantity), 0);
                            }
                        }else {
                            show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_Quantity), 0);
                        }
                        } else {
                            show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_Quantity), 0);
                        }

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
            /*AlertDialog.Builder alert = new AlertDialog.Builder(context);
            final EditText edittext = new EditText(context);
            alert.setMessage(context.getResources().getString(R.string.Please_Enter_the_required_quantity));
            alert.setTitle(context.getResources().getString(R.string.Enter_Quantity));
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(12);
            edittext.setFilters(FilterArray);
            alert.setView(edittext);
            alert.setPositiveButton(context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Check = edittext.getText().toString();
                    textdata = Float.parseFloat(Check);
                    cb = Float.parseFloat(inspectionDetails.commDetails.get(p).closingBalance);
                    if (!Check.isEmpty() && textdata < cb && textdata > 0) {
                        var = (Float) (cb - textdata);
                        if (var > 0 && var < cb) {
                            System.out.println("----------------0");
                            AFTERDATA = String.valueOf(var);
                            DATA = String.valueOf(textdata);

                            inspectionDetails.commDetails.get(p).entered = DATA;
                            inspectionDetails.commDetails.get(p).variation = AFTERDATA;

                            data.clear();
                            int size = inspectionDetails.commDetails.size();
                            for (int i = 0; i < size; i++) {
                                data.add(new DataModel2(inspectionDetails.commDetails.get(i).commNameEn,
                                        inspectionDetails.commDetails.get(i).closingBalance,
                                        inspectionDetails.commDetails.get(i).entered,
                                        inspectionDetails.commDetails.get(i).variation));

                            }
                            Display();
                        }
                    } else {
                        show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_Quantity), 0);
                    }
                }
            });
            alert.setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();*/
        } catch (Exception e) {
            show_error_box("Please enter a valid value", context.getResources().getString(R.string.Invalid_Quantity), 0);

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("OnActivityResult");
        if (requestCode == RD_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println(data.getStringExtra("PID_DATA"));
                String piddata = data.getStringExtra("PID_DATA");
                int code = createAuthXMLRegistered(piddata);
                if (piddata != null && piddata.contains("errCode=\"0\"") && code == 0) {
                    System.out.println("PID DATA = " + piddata);
                    xml_Frame();
                } else {
                    show_error_box(rdModel.errinfo, context.getResources().getString(R.string.PID_Exception), 0);
                    System.out.println("ERROR PID DATA = " + piddata);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public int createAuthXMLRegistered(String piddataxml) {

        try {
            InputStream is = new ByteArrayInputStream(piddataxml.getBytes());
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setIgnoringComments(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(is);

            errcode = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errCode").getTextContent();
            if (errcode.equals("1")) {
                rdModel.errinfo = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errInfo").getTextContent();
                return 1;
            } else {
                rdModel.icount = "0";
                rdModel.itype = "0";
                rdModel.fcount = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fCount").getTextContent();
                rdModel.ftype = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fType").getTextContent();
                rdModel.nmpoint = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("nmPoints").getTextContent();
                rdModel.pid = doc.getElementsByTagName("Data").item(0).getTextContent();
                rdModel.skey = doc.getElementsByTagName("Skey").item(0).getTextContent();
                rdModel.ci = doc.getElementsByTagName("Skey").item(0).getAttributes().getNamedItem("ci").getTextContent();
                rdModel.hmac = doc.getElementsByTagName("Hmac").item(0).getTextContent();
                rdModel.type = doc.getElementsByTagName("Data").item(0).getAttributes().getNamedItem("type").getTextContent();
                rdModel.dpId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dpId").getTextContent();
                rdModel.rdsId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsId").getTextContent();
                rdModel.rdsVer = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsVer").getTextContent();
                rdModel.dc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dc").getTextContent();
                rdModel.mi = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mi").getTextContent();
                rdModel.mc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mc").getTextContent();
                rdModel.skey = rdModel.skey.replaceAll(" ", "\n");

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("createAuthXMLRegistered error= " + e);
            rdModel.errinfo = String.valueOf(e);
            return 2;
        }
        return 0;
    }

    private void show_error_box(String msg, String title, final int i) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (i==2){
                            prep_consent();
                        }

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void image(String content, String name, int align) {
        try {
            Util.image(content, name, align);
        } catch (IOException e) {
            e.printStackTrace();
            show_error_box(e.toString(), "Image formation Error", 0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkandprint(String[] str, int i) {
        if (Util.batterylevel(context) || Util.adapter(context)) {
            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100191);
                mp.start();
            }
            es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i));
            finish();
        } else {
            printbox(context.getResources().getString(R.string.Battery_Msg), context.getResources().getString(R.string.Battery), str, i);
        }
    }

    private void printbox(String msg, String title, final String[] str, final int type) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        checkandprint(str, type);

                    }
                });
        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.Cancel),
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {


                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private void Display(int val) {
        data = new ArrayList<>();
        int commDetailssize = inspectionDetails.commDetails.size();
        for (int i = 0; i < commDetailssize; i++) {
            if (val==1) {
                inspectionDetails.commDetails.get(i).entered = "0.0";
                inspectionDetails.commDetails.get(i).variation = "0.0";
            }

            data.add(new InspectionListModel(inspectionDetails.commDetails.get(i).commNameEn,
                    inspectionDetails.commDetails.get(i).closingBalance,
                    inspectionDetails.commDetails.get(i).entered,
                    inspectionDetails.commDetails.get(i).variation));
        }
        adapter = new InspectionListAdapter(this, data, new OnClickInspector() {
            @Override
            public void onClick(int p) {
                EnterComm(p);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public interface OnClickInspector {
        void onClick(int p);
    }
    @Override
    public void OnOpen() {
        //last.setEnabled(true);
        // btnConnect.setEnabled(false);
        Toast.makeText(context, context.getResources().getString(R.string.CONNECTED), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnOpenFailed() {
        //last.setEnabled(false);
        //btnConnect.setEnabled(true);

        Toast.makeText(context, context.getResources().getString(R.string.Connection_Failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnClose() {
        //  last.setEnabled(false);
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
                //mActivity.last.setEnabled(bIsOpened);
            }
        });

    }


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
                        filter.addAction(ACTION_USB_PERMISSION);
                        context.registerReceiver(mUsbReceiver, filter);
                        Toast.makeText(getApplicationContext(),
                                context.getResources().getString(R.string.Permission_denied), Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.Connecting), Toast.LENGTH_SHORT).show();

                        //last.setEnabled(false);
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

    public interface OnClickListener {
        void onClick_d(int p);
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
        toolbarVersion.setText("Version : " + appversion);


        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("INSPECTION");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}
