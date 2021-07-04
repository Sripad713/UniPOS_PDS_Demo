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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Adapters.BeneficiaryVerificationListAdapter;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.BeneficiaryAuth;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.BeneficiaryDetails;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetUserDetails.BeneficiaryModel;
import com.visiontek.Mantra.Models.DATAModels.BeneficiaryVerificationListModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
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

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Utils.Util.ConsentForm;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.encrypt;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Veroeff.validateVerhoeff;

public class BeneficiaryDetailsActivity extends AppCompatActivity implements PrinterCallBack {

    String ACTION_USB_PERMISSION;
    Button back, Ekyc;
    Context context;
    RecyclerView.Adapter adapter;
    ProgressDialog pd = null;
    String details;
    BeneficiaryAuth beneficiaryAuth;
    BeneficiaryModel beneficiaryModel = new BeneficiaryModel();
    BeneficiaryDetails beneficiaryDetails;
    private BeneficiaryDetailsActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary__details);

        try {

            context = BeneficiaryDetailsActivity.this;
            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;

            beneficiaryDetails = (BeneficiaryDetails) getIntent().getSerializableExtra("OBJ");

            TextView toolbarRD = findViewById(R.id.toolbarRD);
            boolean rd_fps = RDservice(context);
            if (rd_fps) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.blackblack));
                show_AlertDialog(context.getResources().getString(R.string.Beneficiary_Details),
                        context.getResources().getString(R.string.RD_Service),
                        context.getResources().getString(R.string.RD_Service_Msg),0);
                return;
            }

            initilisation();


            beneficiaryModel.click = false;

            Ekyc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if (beneficiaryModel.click) {
                        if (beneficiaryModel.verification.equals("N")) {
                            AadhaarDialog();
                        } else {
                            show_AlertDialog(
                                    beneficiaryModel.memberName,
                                    beneficiaryModel.verifyStatus_en,
                                    ""
                                    ,0);

                        }
                    } else {
                        show_AlertDialog(context.getResources().getString(R.string.Beneficiary_Details),
                                context.getResources().getString(R.string.Please_Select_a_Member),
                                ""
                                ,0);

                    }
                }
            });


            Display();
            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);
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
        } catch (Exception ex) {

            Timber.tag("Beneficiary-onCreate-").e(ex.getMessage(), "");
        }
    }

    private void Sessiontimeout(String msg, String title) {
        try {

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
        } catch (Exception ex) {

            Timber.tag("Beneficiary-timeout-").e(ex.getMessage(), "");
        }
    }

    private void Display() {
        try {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            ArrayList<BeneficiaryVerificationListModel> data = new ArrayList<>();
            int rcMemberDetVerifysize = beneficiaryDetails.rcMemberDetVerify.size();
            for (int i = 0; i < rcMemberDetVerifysize; i++) {
                if (L.equals("hi")) {
                    data.add(new BeneficiaryVerificationListModel(
                            beneficiaryDetails.rcMemberDetVerify.get(i).memberNamell,
                            beneficiaryDetails.rcMemberDetVerify.get(i).uid,
                            beneficiaryDetails.rcMemberDetVerify.get(i).verifyStatus_ll));
                } else {
                    data.add(new BeneficiaryVerificationListModel(
                            beneficiaryDetails.rcMemberDetVerify.get(i).memberName,
                            beneficiaryDetails.rcMemberDetVerify.get(i).uid,
                            beneficiaryDetails.rcMemberDetVerify.get(i).verifyStatus_en));
                }
            }
            adapter = new BeneficiaryVerificationListAdapter(context, data, new OnClickBen() {
                @Override
                public void onClick(int p) {
                    beneficiaryModel.click = true;
                    beneficiaryModel.memberId = beneficiaryDetails.rcMemberDetVerify.get(p).memberId;
                    beneficiaryModel.memberName = beneficiaryDetails.rcMemberDetVerify.get(p).memberName;
                    beneficiaryModel.memberNamell = beneficiaryDetails.rcMemberDetVerify.get(p).memberNamell;
                    beneficiaryModel.member_fusion = beneficiaryDetails.rcMemberDetVerify.get(p).member_fusion;
                    beneficiaryModel.uid = beneficiaryDetails.rcMemberDetVerify.get(p).uid;
                    beneficiaryModel.verification = beneficiaryDetails.rcMemberDetVerify.get(p).verification;
                    beneficiaryModel.verifyStatus_en = beneficiaryDetails.rcMemberDetVerify.get(p).verifyStatus_en;
                    beneficiaryModel.verifyStatus_ll = beneficiaryDetails.rcMemberDetVerify.get(p).verifyStatus_ll;
                    beneficiaryModel.w_uid_status = beneficiaryDetails.rcMemberDetVerify.get(p).w_uid_status;
                }
            });
            recyclerView.setAdapter(adapter);
        } catch (Exception ex) {

            Timber.tag("Beneficiary-display-").e(ex.getMessage(), "");
        }
    }

    public interface OnClickBen {
        void onClick(int p);
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        back = findViewById(R.id.Ben_details_back);
        Ekyc = findViewById(R.id.Ben_details_Ekyc);
        toolbarInitilisation();
    }

    private void ConsentformURL(String consentrequest) {
        try {

            Show(context.getResources().getString(R.string.Beneficiary_Details),
                    context.getResources().getString(R.string.Consent_Form));
/*
            pd = ProgressDialog.show(context, context.getResources().getString(R.string.Beneficiary_Details),
             context.getResources().getString(R.string.Consent_Form), true, false);
*/
            Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {

                        show_AlertDialog(
                                context.getResources().getString(R.string.Consent_Form)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

                    /*if (code.equals("057") || code.equals("008") || code.equals("09D")) {
                        SessionAlert(
                                context.getResources().getString(R.string.Consent_Form)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg);
                        return;
                    }*/
                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Consent_Form)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                0);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Consent_Form)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                0);
                    }
                }

            });
        } catch (Exception ex) {

            Timber.tag("Beneficiary-ConsentReq-").e(ex.getMessage(), "");
        }
    }

    private void AadhaarDialog() {
        try {

            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.uid);
            Button back = (Button) dialog.findViewById(R.id.back);
            Button confirm = (Button) dialog.findViewById(R.id.confirm);
            TextView tv = (TextView) dialog.findViewById(R.id.status);
            TextView dialogbox = (TextView) dialog.findViewById(R.id.dialog);
            final EditText enter = (EditText) dialog.findViewById(R.id.enter);
            dialogbox.setText(context.getResources().getString(R.string.EKYC));
            tv.setText(context.getResources().getString(R.string.Please_Enter_Customer_Aadhaar_No));
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    beneficiaryModel.Enter_UID = enter.getText().toString();

                    if (validateVerhoeff(beneficiaryModel.Enter_UID) && beneficiaryModel.Enter_UID.length() == 12) {
                        try {
                            beneficiaryModel.Enter_UID = encrypt(beneficiaryModel.Enter_UID, menuConstants.skey);

                            if (Util.networkConnected(context)) {
                                if (L.equals("hi")){
                                    ConsentDialog(ConsentForm(context,1));
                                }else {
                                    ConsentDialog(ConsentForm(context,0));
                                }

                            } else {
                                show_AlertDialog(context.getResources().getString(R.string.Beneficiary_Details),
                                        context.getResources().getString(R.string.Internet_Connection),
                                        context.getResources().getString(R.string.Internet_Connection_Msg),
                                        0);
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
                            show_AlertDialog(
                                    context.getResources().getString(R.string.Beneficiary_Details)+ beneficiaryModel.Enter_UID,
                                    context.getResources().getString(R.string.Invalid_UID),
                                    context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID),
                                    0);

                        }
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
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Aadhaar-").e(ex.getMessage(), "");
        }
    }

    private void ConsentDialog(String concent) {
        try {

            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.consent);
            Button confirm = (Button) dialog.findViewById(R.id.agree);
            Button back = (Button) dialog.findViewById(R.id.cancel);
            TextView tv = (TextView) dialog.findViewById(R.id.consent);
            tv.setText(concent);
            final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.check);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        dialog.dismiss();
                        connectRDserviceEKYC(beneficiaryDetails.wadh);
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Beneficiary_Details)+beneficiaryModel.memberName ,
                                context.getResources().getString(R.string.Consent_Form),
                                context.getResources().getString(R.string.Please_check_Consent_Form),
                                2);
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
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Consent-").e(ex.getMessage(), "");
        }
    }


    private void hitURL1(String BenAuth) {
        try {

            Show( context.getResources().getString(R.string.Beneficiary_Verification), context.getResources().getString(R.string.Processing));
/*
            pd = ProgressDialog.show(context,
            context.getResources().getString(R.string.Beneficiary_Verification), context.getResources().getString(R.string.Processing), true, false);
*/
            Aadhaar_Parsing request = new Aadhaar_Parsing(context, BenAuth, 4);
            request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {

                        show_AlertDialog(
                                context.getResources().getString(R.string.Beneficiary_Verification)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

                   /* if (code.equals("057") || code.equals("008") || code.equals("09D")) {
                        SessionAlert(
                                context.getResources().getString(R.string.Beneficiary_Verification)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg);
                        return;
                    }*/
                    if (!code.equals("E00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Beneficiary_Verification)+beneficiaryModel.memberName,
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                0);

                    } else {
                        beneficiaryAuth = (BeneficiaryAuth) object;
                        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                        details = "\n" + context.getResources().getString(R.string.MemberName) + beneficiaryAuth.eKYCMemberName + "\n" +
                                context.getResources().getString(R.string.DOB) + " : " + beneficiaryAuth.eKYCDOB + "\n" +
                                context.getResources().getString(R.string.PindCode) + " : " + beneficiaryAuth.eKYCPindCode + "\n" +
                                context.getResources().getString(R.string.Gender) + " : " + beneficiaryAuth.eKYCGeneder + "\n" +
                                context.getResources().getString(R.string.Date) + " : " + currentDateTimeString + "\n";

                        show_AlertDialog(
                                context.getResources().getString(R.string.ResponseCode)+code,
                                details,
                                "",
                                        1);

                    }
                }
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Verify-").e(ex.getMessage(), "");
        }
    }

    private void connectRDserviceEKYC(String wadhvalue) {
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

                beneficiaryModel.fCount = "2";
                String xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<PidOptions ver =\"1.0\">\n" +
                        "    <Opts env=\"P\" fCount=\"" + beneficiaryModel.fCount + "\" iCount=\"" + beneficiaryModel.iCount + "\" iType=\"" + beneficiaryModel.iType + "\" fType=\"" + beneficiaryModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"" + wadhvalue + "\" posh=\"UNKNOWN\"/>\n" +
                        "</PidOptions>";

                Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
                final boolean isIntentSafe = activities.size() > 0;

                act.putExtra("PID_OPTIONS", xmplpid);
                startActivityForResult(act, beneficiaryModel.RD_SERVICE);

        } catch (Exception ex) {
            Timber.tag("Beneficiary-RD-").e(ex.getMessage(), "");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {


            if (requestCode == beneficiaryModel.RD_SERVICE) {
                if (resultCode == Activity.RESULT_OK) {

                    String piddata = data.getStringExtra("PID_DATA");
                    int code = createAuthXMLRegistered(piddata);
                    if (piddata != null && piddata.contains("errCode=\"0\"") && code == 0) {
                        System.out.println("PID DATA = " + piddata);

                        prep_Mlogin();

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Dealer),
                                beneficiaryModel.err_code,
                                beneficiaryModel.rdModel.errinfo,
                                0);
                    }
                } else {
                    show_AlertDialog(
                            context.getResources().getString(R.string.RD_Service),
                            beneficiaryModel.err_code,
                            beneficiaryModel.rdModel.errinfo,
                            0);

                }
            }
        } catch (Exception ex) {
            Timber.tag("Beneficiary-PID-").e(ex.getMessage(), "");
        }
    }

    private void prep_Mlogin() {
        try {

            String BenAuth = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns1:getEKYCAuthenticateRD>\n" +
                    "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                    "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                    "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                    "            <terminal_id>" + DEVICEID + "</terminal_id>\n" +
                    "            <existingRCNumber>" + beneficiaryDetails.rationCardId + "</existingRCNumber>\n" +
                    "            <rcMemberName>" + beneficiaryModel.memberName + "</rcMemberName>\n" +
                    "            <rcUid>" + beneficiaryModel.uid + "</rcUid>\n" +
                    "            <memberId>" + beneficiaryModel.memberId + "</memberId>\n" +
                    "            <ekycresAuth>\n" +
                    "                <dc>" + beneficiaryModel.rdModel.dc + "</dc>\n" +
                    "                <dpId>" + beneficiaryModel.rdModel.dpId + "</dpId>\n" +
                    "                <mc>" + beneficiaryModel.rdModel.mc + "</mc>\n" +
                    "                <mid>" + beneficiaryModel.rdModel.mi + "</mid>\n" +
                    "                <rdId>" + beneficiaryModel.rdModel.rdsId + "</rdId>\n" +
                    "                <rdVer>" + beneficiaryModel.rdModel.rdsVer + "</rdVer>\n" +
                    "                <res_Consent_POIandPOA>Y</res_Consent_POIandPOA>\n" +
                    "                <res_Consent_mobileOREmail>Y</res_Consent_mobileOREmail>\n" +
                    "                <res_certificateIdentifier>" + beneficiaryModel.rdModel.ci + "</res_certificateIdentifier>\n" +
                    "                <res_encHmac>" + beneficiaryModel.rdModel.hmac + "</res_encHmac>\n" +
                    "                <res_secure_pid>" + beneficiaryModel.rdModel.pid + "</res_secure_pid>\n" +
                    "                <res_sessionKey>" + beneficiaryModel.rdModel.skey + "</res_sessionKey>\n" +
                    "                <res_uid>" + beneficiaryModel.Enter_UID + "</res_uid>\n" +
                    "            </ekycresAuth>\n" +
                    "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                    "            <eKYCType>eKYCV</eKYCType>\n" +
                    "            <Resp>\n" +
                    "                <errCode>0</errCode>\n" +
                    "                <errInfo>y</errInfo>\n" +
                    "                <nmPoints>" + beneficiaryModel.rdModel.nmpoint + "</nmPoints>\n" +
                    "                <fCount>" + beneficiaryModel.rdModel.fcount + "</fCount>\n" +
                    "                <fType>" + beneficiaryModel.rdModel.ftype + "</fType>\n" +
                    "                <iCount>" + beneficiaryModel.rdModel.icount + "</iCount>\n" +
                    "                <iType>" + beneficiaryModel.rdModel.itype + "</iType>\n" +
                    "                <pCount>0</pCount>\n" +
                    "                <pType>0</pType>\n" +
                    "                <qScore>0</qScore>\n" +
                    "            </Resp>\n" +
                    "        </ns1:getEKYCAuthenticateRD>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>";
            if (networkConnected(context)) {
                if (mp != null) {
                    releaseMediaPlayer(context, mp);
                }
                if (L.equals("hi")) {
                } else {
                    mp = mp.create(context, R.raw.c100187);
                    mp.start();
                }
                Util.generateNoteOnSD(context, "BenVerificationAuthReq.txt", BenAuth);
                hitURL1(BenAuth);
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.Login),
                        context.getResources().getString(R.string.Internet_Connection),
                        context.getResources().getString(R.string.Internet_Connection_Msg),0);
            }
        } catch (Exception ex) {
            Timber.tag("Beneficiary-AuthFormat-").e(ex.getMessage(), "");
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    probe();
                    // btnConnect.performClick();

                    //last.setEnabled(true);
                    synchronized (this) {

                    }
                }
            } catch (Exception ex) {
                Timber.tag("Beneficiary-BroadCast-").e(ex.getMessage(), "");
            }
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void callPrint() {
        try {

            String str1, str2, str3;
            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            String[] str = new String[4];
            if (L.equals("hi")) {
                str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n" +
                        context.getResources().getString(R.string.VERIFICATION_RECEIPT) + "\n";
                image(str1, "header.bmp", 1);
                str2 = context.getResources().getString(R.string.Date) + " : " + currentDateTimeString +
                       /* context.getResources().getString(R.string.Time) + " : " + currentDateTimeString + " \n" +*/
                        context.getResources().getString(R.string.FPS_ID) + " : " + dealerConstants.fpsCommonInfo.fpsId + "\n"
                        + context.getResources().getString(R.string.NAME) + " : " + beneficiaryAuth.eKYCMemberName + "\n"
                        + context.getResources().getString(R.string.Gender) + " : " + beneficiaryAuth.eKYCGeneder + "\n"
                        + context.getResources().getString(R.string.DOB) +" : " + beneficiaryAuth.eKYCDOB + "\n"
                        + context.getResources().getString(R.string.Ration_Card_Number) + " : " + "\n"
                        + context.getResources().getString(R.string.Status) + " : " + "Success" + "\n";

                image(str2, "body.bmp", 0);

                str[0] = "1";
                str[1] = "1";
                str[2] = "1";
                str[3] = "0";
                checkandprint(str, 1);
            } else {

                str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n" +
                        context.getResources().getString(R.string.VERIFICATION_RECEIPT) + "\n"
                        + "-----------------------------\n";
                str2 = context.getResources().getString(R.string.Date) + "           :" + currentDateTimeString + "\n"
                        /*+ context.getResources().getString(R.string.Time) + "           : " + currentDateTimeString + " \n"*/
                        + context.getResources().getString(R.string.FPS_ID) + "         : " + dealerConstants.fpsCommonInfo.fpsId + "\n"
                        + context.getResources().getString(R.string.NAME) + "           : " + beneficiaryAuth.eKYCMemberName + "\n"
                        + context.getResources().getString(R.string.Gender) +"         : " + beneficiaryAuth.eKYCGeneder + "\n"
                        + context.getResources().getString(R.string.DOB) +"            :" + beneficiaryAuth.eKYCDOB + "\n"
                        + context.getResources().getString(R.string.Ration_Card_Number) + " : " + beneficiaryDetails.rationCardId + "\n"
                        + context.getResources().getString(R.string.Status) + "         : " + "Success" + "\n";

                str[0] = "1";
                str[1] = str1;
                str[2] = str2;
                str[3] = "0";
                checkandprint(str, 0);
            }
        } catch (Exception ex) {

            Timber.tag("StartActivity-print-").e(ex.getMessage(), "");
        }
    }

    private void prep_consent() {
        try {


            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            //currentDateTimeString="26032021114610";
            String consentrequest = "{\n" +
                    "   \"fpsId\" : " + "\"" + dealerConstants.stateBean.statefpsId + "\"" + ",\n" +
                    "   \"modeOfService\" : \"D\",\n" +
                    "   \"moduleType\" : \"C\",\n" +
                    "   \"rcId\" : " + "\"" + dealerConstants.stateBean.statefpsId + "\"" + ",\n" +
                    "   \"requestId\" : \"0\",\n" +
                    "   \"requestValue\" : \"N\",\n" +
                    "   \"sessionId\" : " + "\"" + dealerConstants.fpsCommonInfo.fpsSessionId + "\"" + ",\n" +
                    "   \"stateCode\" : " + "\"" + dealerConstants.stateBean.stateCode + "\"" + ",\n" +
                    "   \"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                    "   \"timeStamp\" : " + "\"" + currentDateTimeString + "\"" + ",\n" +
                    /*"   \"token\" : "+"\""+fpsURLInfo.token()+"\""+"\n" +*/
                    "   \"token\" : " + "\"9f943748d8c1ff6ded5145c59d0b2ae7\"" + "\n" +
                    "}";
            Util.generateNoteOnSD(context, "ConsentFormReq.txt", consentrequest);
            ConsentformURL(consentrequest);
        } catch (Exception ex) {

            Timber.tag("Beneficiary-CnsntFrmt-").e(ex.getMessage(), "");
        }
    }

    private void image(String content, String name, int align) {
        try {
            Util.image(content, name, align);
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Image-").e(ex.getMessage(), "");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkandprint(String[] str, int i) {
        try {

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
                Intent intent = new Intent(context, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                printbox(str, i);
            }
        } catch (Exception ex) {
            Timber.tag("Beneficiary-Batter-").e(ex.getMessage(), "");
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

                beneficiaryModel.err_code = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errCode").getTextContent();
                if (!beneficiaryModel.err_code.equals("0")) {
                    beneficiaryModel.rdModel.errinfo = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errInfo").getTextContent();
                    return 1;
                } else {
                    beneficiaryModel.rdModel.fcount = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fCount").getTextContent();
                    beneficiaryModel.rdModel.ftype = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fType").getTextContent();
                    beneficiaryModel.rdModel.nmpoint = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("nmPoints").getTextContent();
                    beneficiaryModel.rdModel.pid = doc.getElementsByTagName("Data").item(0).getTextContent();
                    beneficiaryModel.rdModel.skey = doc.getElementsByTagName("Skey").item(0).getTextContent();
                    beneficiaryModel.rdModel.ci = doc.getElementsByTagName("Skey").item(0).getAttributes().getNamedItem("ci").getTextContent();
                    beneficiaryModel.rdModel.hmac = doc.getElementsByTagName("Hmac").item(0).getTextContent();
                    beneficiaryModel.rdModel.type = doc.getElementsByTagName("Data").item(0).getAttributes().getNamedItem("type").getTextContent();
                    beneficiaryModel.rdModel.dpId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dpId").getTextContent();
                    beneficiaryModel.rdModel.rdsId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsId").getTextContent();
                    beneficiaryModel.rdModel.rdsVer = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsVer").getTextContent();
                    beneficiaryModel.rdModel.dc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dc").getTextContent();
                    beneficiaryModel.rdModel.mi = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mi").getTextContent();
                    beneficiaryModel.rdModel.mc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mc").getTextContent();
                    beneficiaryModel.rdModel.skey = beneficiaryModel.rdModel.skey.replaceAll(" ", "\n");

                }
            } catch (Exception e) {
                e.printStackTrace();
                Timber.tag("Beneficiary-PID-").e(e.getMessage(), "");
                beneficiaryModel.rdModel.errinfo = String.valueOf(e);
                return 2;
            }

        return 0;
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

                    }
                }
            }
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Probe-").e(ex.getMessage(), "");
        }
    }


    @Override
    public void OnOpen() {
        //last.setEnabled(true);
        // btnConnect.setEnabled(false);

    }

    @Override
    public void OnOpenFailed() {

    }

    @Override
    public void OnClose() {
        if (mUsbReceiver != null) {
            context.unregisterReceiver(mUsbReceiver);
        }
        probe();
    }

    @Override
    public void OnPrint(final int bPrintResult, final boolean bIsOpened) {

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
    private void toolbarInitilisation() {
        try {

            TextView toolbarVersion = findViewById(R.id.toolbarVersion);
            TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
            TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
            TextView toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
            TextView toolbarActivity = findViewById(R.id.toolbarActivity);
            TextView toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
            TextView toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);
            TextView toolbarCard = findViewById(R.id.toolbarCard);
            toolbarCard.setText("RC : " + beneficiaryDetails.rationCardId);
            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);


            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            toolbarActivity.setText( context.getResources().getString(R.string.BENEFICIARY_DETAILS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {

            Timber.tag("Beneficiary-ToolBar-").e(ex.getMessage(), "");
        }
    }

    private void show_Dialogbox(String msg, String header) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialogbox);
        Button back = (Button) dialog.findViewById(R.id.dialogcancel);
        Button confirm = (Button) dialog.findViewById(R.id.dialogok);
        TextView head = (TextView) dialog.findViewById(R.id.dialoghead);
        TextView status = (TextView) dialog.findViewById(R.id.dialogtext);
        head.setText(header);
        status.setText(msg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

                if (i == 1) {
                    callPrint();
                } else if (i == 2) {
                    prep_consent();
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void SessionAlert(String headermsg, String bodymsg,String talemsg) {
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
                Intent i = new Intent(context, StartActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }
        });

    }

    public void Dismiss() {
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
}
