package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails.DealerModel;
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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.BaseActivity.rd_vr;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Utils.Util.ConsentForm;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.encrypt;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Veroeff.validateVerhoeff;

public class BeneficiaryDetailsActivity extends AppCompatActivity  implements PrinterCallBack {

    String ACTION_USB_PERMISSION;
    Button back, Ekyc;
    Context context;
    ProgressDialog pd = null;
    String details;
    BeneficiaryAuth beneficiaryAuth;
    BeneficiaryModel beneficiaryModel = new BeneficiaryModel();
    BeneficiaryDetails beneficiaryDetails;
    private BeneficiaryDetailsActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    int flagprint;
    DealerModel dealerModel = new DealerModel();

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
            RecyclerView.Adapter adapter = new BeneficiaryVerificationListAdapter(context, data, p -> {
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
                dealerModel.DaadhaarAuthType = dealerConstants.fpsCommonInfo.fpsDetails.get(p).aadhaarAuthType;
                System.out.println("beneficiaryModel==DaadhaarAuthType==="+dealerModel.DaadhaarAuthType);
            });
            recyclerView.setAdapter(adapter);
        } catch (Exception ex) {
            Timber.tag("Beneficiary-display-").e(ex.getMessage(), "");
        }
    }

    public interface OnClickBen {
        void onClick(int p);
    }

    private void ConsentformURL(String consentrequest) {
        try {

            Show(context.getResources().getString(R.string.Beneficiary_Details), context.getResources().getString(R.string.Consent_Form));

            Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
            request.setOnResultListener((code, msg, object) -> {
                Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Consent_Form)+beneficiaryModel.memberName,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }
                show_AlertDialog(
                        context.getResources().getString(R.string.Consent_Form)+beneficiaryModel.memberName,
                        context.getResources().getString(R.string.ResponseCode)+code,
                        context.getResources().getString(R.string.ResponseMsg)+msg,
                        0);
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
            confirm.setOnClickListener(v -> {
                preventTwoClick(v);
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
                        mp = MediaPlayer.create(context, R.raw.c100047);
                        mp.start();
                        show_AlertDialog(
                                context.getResources().getString(R.string.Beneficiary_Details)+ beneficiaryModel.Enter_UID,
                                context.getResources().getString(R.string.Invalid_UID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID),
                                0);

                    }
                }
            });
            back.setOnClickListener(v -> dialog.dismiss());

            dialog.setCanceledOnTouchOutside(false);
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
            confirm.setOnClickListener(v -> {
                preventTwoClick(v);
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

            });
            back.setOnClickListener(v -> dialog.dismiss());

            dialog.setCanceledOnTouchOutside(false);
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.show();
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Consent-").e(ex.getMessage(), "");
        }
    }


    private void hitURL1(String BenAuth) {
        try {

            Show( context.getResources().getString(R.string.Beneficiary_Verification), context.getResources().getString(R.string.Processing));

            Aadhaar_Parsing request = new Aadhaar_Parsing(context, BenAuth, 4);
            request.setOnResultListener((code, msg, ref, flow, object) -> {
                Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Beneficiary_Verification)+beneficiaryModel.memberName,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }

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
                    mp = MediaPlayer.create(context, R.raw.c200032);
                    mp.start();
                } else {
                    mp = MediaPlayer.create(context, R.raw.c100032);
                    mp.start();
                }

                  beneficiaryModel.fCount = "2";
                  beneficiaryModel.fType = dealerModel.DaadhaarAuthType;
                 System.out.println("beneficiaryModel_fType========"+beneficiaryModel.fType);
                 String xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<PidOptions ver =\"1.0\">\n" +
                        "    <Opts env=\"P\" fCount=\"" + beneficiaryModel.fCount + "\" iCount=\"" + beneficiaryModel.iCount + "\" iType=\"" + beneficiaryModel.iType + "\" fType=\"" + beneficiaryModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"" + wadhvalue + "\" posh=\"UNKNOWN\"/>\n" +
                        "</PidOptions>";
                System.out.println("beneficiaryModel_fType======"+xmplpid);
                Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
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
                        prep_Mlogin();
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.RD_Service),
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
    //beneficiaryModel.rdModel.icount ="0";
    private void prep_Mlogin() {
        try {
            String BenAuth = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns1:getEKYCAuthenticateRD>\n" +
                    "          <aadhaarAuthType>"+dealerModel.DaadhaarAuthType+"</aadhaarAuthType>\n"+
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
                    "                <iCount>0</iCount>\n" +
                    "                <iType>0</iType>\n" +
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
                    mp = MediaPlayer.create(context, R.raw.c100187);
                    mp.start();
                }
                if (Debug) {
                    Util.generateNoteOnSD(context, "BenVerificationAuthReq.txt", BenAuth);
                }
                System.out.println("BenVerificationAuthReq========="+BenAuth);
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
                    synchronized (this) {

                    }
                }
            } catch (Exception ex) {
                Timber.tag("Beneficiary-BroadCast-").e(ex.getMessage(), "");
            }
        }
    };

    private void callPrint() {
        try {

            String str1, str2;
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
                        + "\n";
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
                    if (Debug) {
                        Util.generateNoteOnSD(context, "ConsentFormReq.txt", consentrequest);
                    }
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

    private void checkandprint(String[] str, int i) {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                            es.submit(() -> mTerminal100API.printerOpenTask(mUsbManager, device, context));
                        }

                    }
                }
            }
        } catch (Exception ex) {

            Timber.tag("Beneficiary-Probe-").e(ex.getMessage(), "");
        }
    }


    @Override
    public void OnOpen() {

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

    private void printbox(final String[] str, final int type) {

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
            preventTwoClick(v);
            dialog.dismiss();
            checkandprint(str,type);
        });
        back.setOnClickListener(v -> dialog.dismiss());
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary__details);
        try {
            context = BeneficiaryDetailsActivity.this;

            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
            beneficiaryDetails = (BeneficiaryDetails) getIntent().getSerializableExtra("OBJ");
            initilisation();



            flagprint=0;
            beneficiaryModel.click = false;

            Ekyc.setOnClickListener(view -> {
                preventTwoClick(view);
                if (flagprint!=2) {
                    if (beneficiaryModel.click) {
                        if (beneficiaryModel.verification.equals("N")) {
                            AadhaarDialog();
                        } else {
                            show_AlertDialog(
                                    beneficiaryModel.memberName,
                                    beneficiaryModel.verifyStatus_en,
                                    ""
                                    , 0);

                        }
                    } else {
                        show_AlertDialog(context.getResources().getString(R.string.Beneficiary_Details),
                                context.getResources().getString(R.string.Please_Select_a_Member),
                                ""
                                , 0);

                    }
                }
            });

            Display();
            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);
            probe();
            back.setOnClickListener(view -> {
                preventTwoClick(view);
                finish();
            });
        } catch (Exception ex) {
            Timber.tag("Beneficiary-onCreate-").e(ex.getMessage(), "");
        }
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        back = findViewById(R.id.Ben_details_back);
        Ekyc = findViewById(R.id.Ben_details_Ekyc);

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
            TextView toolbarCard = findViewById(R.id.toolbarCard);
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
        confirm.setOnClickListener(v -> {
            preventTwoClick(v);
            dialog.dismiss();

            if (i == 1) {
                flagprint=2;
                callPrint();
            } else if (i == 2) {
                prep_consent();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
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
