package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
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

import com.irisking.irisalgo.util.EnumDeviceType;
import com.visiontek.Mantra.Adapters.DealerListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.DealerListModel;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsCommonInfo;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails.DealerModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.MemberModel;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.Models.ResponseData;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.OFFLINE_TOKEN;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.Mdealer;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.ConsentForm;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class DealerDetailsActivity extends BaseActivity {
    DatabaseHelper databaseHelper;
    DealerModel dealerModel = new DealerModel();
    RecyclerView.Adapter adapter;
    Button scanfp, back;
    ProgressDialog pd = null;
    Handler mHandler;
    Context context;
    MemberModel memberModel;
    String fusiondata;
    String fposh = "UNKNOWN";
    private void ConsentformURL(String consentrequest) {
        try {
            Show(context.getResources().getString(R.string.Dealer),
                    context.getResources().getString(R.string.Consent_Form));

            Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Dealer) + Dealername,
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }
                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Consent_Form) + Dealername,
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseCode) + msg,
                                0);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Consent_Form) + Dealername,
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);
                    }
                }

            });
        } catch (Exception ex) {

            Timber.tag("DealerAuth-ConsntRes-").e(ex.getMessage(), "");
        }
    }

    private void password_Dialog() {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100074);
                mp.start();
            }

            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.uid);
            Button back = (Button) dialog.findViewById(R.id.back);
            Button confirm = (Button) dialog.findViewById(R.id.confirm);
            TextView tv = (TextView) dialog.findViewById(R.id.dialog);
            TextView status = (TextView) dialog.findViewById(R.id.status);
            final EditText enter = (EditText) dialog.findViewById(R.id.enter);
            tv.setText(context.getResources().getString(R.string.Password));
            status.setText(context.getResources().getString(R.string.Please_Enter_Dealer_Authentication_Password));
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    dialog.dismiss();
                    dealerModel.EnterPassword = enter.getText().toString();
                    if (!dealerModel.EnterPassword.isEmpty() && dealerConstants.fpsCommonInfo.dealer_password.equals(dealerModel.EnterPassword)) {
                        if (mp != null) {
                            releaseMediaPlayer(context, mp);
                        }
                        if (L.equals("hi")) {

                        } else {
                            mp = mp.create(context, R.raw.c100178);
                            mp.start();
                        }
                        String pdealerlogin = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                "<SOAP-ENV:Envelope\n" +
                                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                "    xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
                                "    xmlns:tns=\"http://service.fetch.rationcard/\"\n" +
                                "    xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
                                "    xmlns:ns1=\"http://schemas.xmlsoap.org/soap/http\"\n" +
                                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
                                "    <SOAP-ENV:Body>\n" +
                                "        <mns1:ackRequest\n" +
                                "            xmlns:mns1=\"http://service.fetch.rationcard/\">\n" +
                                "            <fpsCard>" + dealerConstants.stateBean.statefpsId + "</fpsCard>\n" +
                                "            <terminalId>" + DEVICEID + "</terminalId>\n" +
                                "            <user_password>" + dealerModel.EnterPassword + "</user_password>\n" +
                                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                "            <uidNumber>" + dealerModel.DUid + "</uidNumber>\n" +
                                "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                                "            <auth_type>" + dealerModel.DEALER_AUTH_TYPE + "</auth_type>\n" +
                                "            <user_type>" + dealerModel.Dtype + "</user_type>\n" +
                                "            <memberId>0</memberId>\n" +
                                "            <fpsId>" + dealerConstants.fpsCommonInfo.fpsId + "</fpsId>\n" +
                                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                "        </mns1:ackRequest>\n" +
                                "    </SOAP-ENV:Body>\n" +
                                "</SOAP-ENV:Envelope> ";
                        if (Debug) {
                            Util.generateNoteOnSD(context, "DealerPasswordReq.txt", pdealerlogin);
                        }
                        hitURLDealerAuthentication(pdealerlogin);
                        Timber.d("DealerDetailsActivity-DealerPasswordReq :"+pdealerlogin);
                    } else {
                        show_AlertDialog(Dealername,
                                context.getResources().getString(R.string.Invalid_Password),
                                context.getResources().getString(R.string.Please_Enter_a_valid_Password),
                                0);
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

            Timber.tag("DealerAuth-Password-").e(ex.getMessage(), "");
        }
    }


    private void hitURLDealerAuthentication(String dealerlogin) {
        try {
            Show(context.getResources().getString(R.string.Please_wait),
                    context.getResources().getString(R.string.Authenticating));
            XML_Parsing request = new XML_Parsing(DealerDetailsActivity.this, dealerlogin, 2);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {
                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    System.out.println("hitURLDealerAuthentication CODE ======"+code);
                    System.out.println("hitURLDealerAuthentication MSG ======"+msg);
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Dealer),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

                    if (!code.equals("00")) {
                        if (code.equals("300") && flow.equals("F")) {
                            if (dealerModel.Dwadh.equals("Y") && dealerModel.wadhflag != 1) {
                                dealerModel.wadhflag = 1;
                                show_AlertDialog(Dealername,
                                        context.getResources().getString(R.string.Dealer_Wadh) + code,
                                        context.getResources().getString(R.string.ResponseMsg) + msg,
                                        1);
                                return;
                            } /*else if (dealerConstants.fpsCommonInfo.firauthFlag.equals("Y") && dealerModel.FIRflag != 1) {
                            dealerModel.FIRflag = 1;
                            show_error_box(msg, context.getResources().getString(R.string.Dealer_FIR_Authentication) + isError, 1);
                            return;
                        }*/ else if (dealerModel.Dfusion.equals("1") && dealerModel.Fusionflag != 1) {
                                dealerModel.Fusionflag = 1;
                                dealerModel.fCount = "2";
                                show_AlertDialog(
                                        Dealername,
                                        context.getResources().getString(R.string.Dealer_Fusion) + code,
                                        context.getResources().getString(R.string.ResponseMsg) + msg,
                                        1);
                                return;
                            } else {
                                if (dealerModel.fCount.equals("1") && dealerModel.fusionflag != 1) {
                                    dealerModel.fusionflag = 1;
                                    dealerModel.fCount = "2";
                                    show_AlertDialog(
                                            Dealername,
                                            context.getResources().getString(R.string.Dealer_FP_Authentication) + code,
                                            context.getResources().getString(R.string.ResponseMsg) + msg,
                                            1);
                                    return;
                                }
                            }
                        }
                        dealerModel.fCount = "1";
                        fposh = "UNKNOWN";
                        show_AlertDialog(
                                Dealername,
                                context.getResources().getString(R.string.Dealer_Authentication) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);
                        System.out.println("FMRRRR====");

                    } else {
                        /*if(object == null ){
                            show_Dialogbox("Dealer Authentication","Invalid Data from server");
                            return;
                        }*/
                        if (Mdealer == 1) {
                            Mdealer = 0;
                            finish();
                        } else {
                            if (dealerModel.fusionflag == 1) {
                                dealerModel.fusionflag = 0;
                                if (dealerModel.Dtype.equals("N1")) {
                                    dealerModel.dealertype = "REP1";
                                } else if (dealerModel.Dtype.equals("N2")){
                                    dealerModel.dealertype = "REP2";
                                } else {
                                    dealerModel.dealertype = "DEL";
                                }
                                String fusion = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                                        "<SOAP-ENV:Envelope\n" +
                                        "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                        "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                                        "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                        "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                                        "    <SOAP-ENV:Body>\n" +
                                        "        <ns1:getFusionRecord>\n" +
                                        "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                        "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                        "            <user_type>" + dealerModel.dealertype + "</user_type>\n" +
                                        "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                                        "            <uidNumber>" + dealerModel.DUid + "</uidNumber>\n" +
                                        "            <member_fusion>1</member_fusion>\n" +
                                        "            <member_id>" + dealerModel.dealertype + "</member_id>\n" +
                                        "        </ns1:getFusionRecord>\n" +
                                        "    </SOAP-ENV:Body>\n" +
                                        "</SOAP-ENV:Envelope>";
                                if(Debug) {
                                    Util.generateNoteOnSD(context, "DealerFusionReq.txt", fusion);
                                }
                                hitURLfusion(fusion);
                                Timber.d("DealerDetailsActivity-DealerFusionReq :"+fusion);
                            }
                            if (dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus.equals("Y"))
                            {
                                System.out.println("@@Eligible for partial offline");
                                if (dealerConstants.fpsCommonInfo.keyregisterDownloadStatus.equals("Y") || dealerConstants.fpsURLInfo.fpsCbDownloadStatus.equals("Y")) {
                                    System.out.println("@@Uploading pending transactions");
                                    new UploadPendingRecords(dealerConstants.fpsCommonInfo.fpsSessionId, dealerConstants.stateBean.statefpsId, "", dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus).execute();
                                    if (dealerConstants.fpsCommonInfo.keyregisterDownloadStatus.equals("Y")) {
                                        System.out.println("@@key register download status Y");
                                        String keyregister = "{\n" +
                                                "\"fpsId\" : " + "\"" + dealerConstants.fpsCommonInfo.fpsId + "\"" + ",\n" +
                                                "\"sessionId\" : " + "\"" + dealerConstants.fpsCommonInfo.fpsSessionId + "\"" + ",\n" +
                                                "\"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                                                "\"token\" : " + "\"" + OFFLINE_TOKEN + "\"" + ",\n" +
                                                "\"stateCode\" : " + "\"" + "22" + "\"" + "\n" +
                                                "}";
                                        if(Debug) {
                                            Util.generateNoteOnSD(context, "keyregisterdownloadReq.txt", keyregister);
                                        }
                                        hitURLforPOS_OB(keyregister);
                                    }
                                    /*if (dealerConstants.fpsURLInfo.fpsCbDownloadStatus.equals("Y")) {
                                        System.out.println("@@fpsCbDownloadStatus Y");
                                    }*/

                                    if(dealerConstants.fpsURLInfo.fpsCbDownloadStatus==null || dealerConstants.fpsURLInfo.fpsCbDownloadStatus.isEmpty())
                                    {
                                        show_Dialogbox(context.getResources().getString(R.string.Dealer_Authentication),"No Data from server\nfpsCbDownloadStatusflag");
                                         return;
                                    }else{

                                        if (dealerConstants.fpsURLInfo.fpsCbDownloadStatus.equals("Y")) {
                                            System.out.println("@@fpsCbDownloadStatus Y");
                                        }
                                    }
                                    String menu = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                                            "<SOAP-ENV:Envelope\n" +
                                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                                            "    <SOAP-ENV:Body>\n" +
                                            "        <ns1:menuDisplayService>\n" +
                                            "            <shop_number>" + dealerConstants.stateBean.statefpsId + "</shop_number>\n" +
                                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                            "        </ns1:menuDisplayService>\n" +
                                            "    </SOAP-ENV:Body>\n" +
                                            "</SOAP-ENV:Envelope>";
                                    if(Debug) {
                                        Util.generateNoteOnSD(context, "MenuReq.txt", menu);
                                    }
                                    hitURLMENU(menu, false);
                                } else {
                                    String menu = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                                            "<SOAP-ENV:Envelope\n" +
                                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                                            "    <SOAP-ENV:Body>\n" +
                                            "        <ns1:menuDisplayService>\n" +
                                            "            <shop_number>" + dealerConstants.stateBean.statefpsId + "</shop_number>\n" +
                                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                            "        </ns1:menuDisplayService>\n" +
                                            "    </SOAP-ENV:Body>\n" +
                                            "</SOAP-ENV:Envelope>";
                                    if(Debug) {
                                        Util.generateNoteOnSD(context, "MenuReq.txt", menu);
                                    }
                                    hitURLMENU(menu, false);
                                }
                            } else {
                                if (dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus.equals("N"))

                                     System.out.println("@@OFFLINE PENDING UPLOADINS >>>>");
                                     new Thread(new Runnable() {
                                    @Override
                                     public void run() {
                                        String errorMessage = "";
                                        OfflineUploadNDownload offlineUploadNDownload = new OfflineUploadNDownload(context);
                                        int pendingTxns = databaseHelper.getPendingTxnCount();
                                        if (pendingTxns > 0){
                                            int ret = offlineUploadNDownload.ManualServerUploadPartialTxns(dealerConstants.fpsCommonInfo.fpsId, dealerConstants.fpsCommonInfo.fpsSessionId);
                                            System.out.println("RET>>>>>>>   "+ret);
                                            Timber.d("ManualServerUploadPartialTxns RET : "+ret);
                                            if (ret == -2) {
                                                errorMessage = "Internet not available";
                                            }
                                            if(ret == 0){
                                                //Delete Database
                                                System.out.println("DELETEEEE");
                                                databaseHelper.forceDelteKeyRegNPosOb();
                                            }
                                        }
                                        String finalErrorMessage = errorMessage;
                                        DealerDetailsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(finalErrorMessage.isEmpty()){
                                                   /* System.out.println("@@Data not available in download");
                                                    System.out.println("@@Data not available in download please go online");
                                                    Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(i);*/

                                                    System.out.println("@@hitting URL menu");
                                                    String menu = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                                                            "<SOAP-ENV:Envelope\n" +
                                                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                                                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                                                            "    <SOAP-ENV:Body>\n" +
                                                            "        <ns1:menuDisplayService>\n" +
                                                            "            <shop_number>" + dealerConstants.stateBean.statefpsId + "</shop_number>\n" +
                                                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                                            "        </ns1:menuDisplayService>\n" +
                                                            "    </SOAP-ENV:Body>\n" +
                                                            "</SOAP-ENV:Envelope>";
                                                    if (Debug) {
                                                        Util.generateNoteOnSD(context, "MenuReq.txt", menu);
                                                    }
                                                    hitURLMENU(menu, false);


                                                }
                                            }
                                        });
                                    }
                                }).start();
                            }
                                  /*System.out.println("@@hitting URL menu");
                                    String menu = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                                            "<SOAP-ENV:Envelope\n" +
                                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                                            "    <SOAP-ENV:Body>\n" +
                                            "        <ns1:menuDisplayService>\n" +
                                            "            <shop_number>" + dealerConstants.stateBean.statefpsId + "</shop_number>\n" +
                                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                                            "        </ns1:menuDisplayService>\n" +
                                            "    </SOAP-ENV:Body>\n" +
                                            "</SOAP-ENV:Envelope>";
                                    if (Debug) {
                                        Util.generateNoteOnSD(context, "MenuReq.txt", menu);
                                    }
                                    hitURLMENU(menu, false);*/

                        }
                        }

                }
            });
            request.execute();
        } catch (Exception ex) {
            Timber.e("DealerDetailsActivity-DealerAuth-hitURLDealerAuthentication Exception ==>"+ex.getLocalizedMessage());
            //Timber.tag("DealerAuth-AuthReq-").e(ex.getMessage(), "");
        }
    }

    //Offline
    private void hitURLforPOS_OB(String keyregister) {
        try {
            Show(context.getResources().getString(R.string.Please_wait),
                    context.getResources().getString(R.string.Dealer_Details));
            Json_Parsing request = new Json_Parsing(context, keyregister, 5);
            request.setOnResultListener(new Json_Parsing.OnResultListener() {
                @Override
                public void onCompleted(String code, String msg, Object object) throws SQLException {
                    Dismiss();

                }
            });
        } catch (Exception e) {

            System.out.println("@@Exception: " + e.toString());
            Timber.d("DealerDetailsActivity-hitURLforPOS_OB "+e.getLocalizedMessage());
        }
    }

    private void hitURLfusion(String fusion) {
        try {
            System.out.println("TEJJ FUSION");

            XML_Parsing request = new XML_Parsing(DealerDetailsActivity.this, fusion, 2);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String isError, String msg, String ref, String flow, Object object) {
                    Log.e("","sdkjaskdjsajkdsjd");

                }
            });
            request.execute();
        } catch (Exception ex) {

            Timber.e("DealerDetailsActivity-hitURLfusion-DealerAuthfusion Exception ==> :"+ex.getLocalizedMessage());
            //Timber.tag("DealerAuth-fusion-").e(ex.getMessage(), "");
        }
    }

    private void hitURLMENU(String menu, boolean isUpdateAckReq) {
        SharedPref SharedPref = new SharedPref(context);
        try {

            Show(context.getResources().getString(R.string.Please_wait),
                    context.getResources().getString(R.string.Downloading_Menus));
            XML_Parsing request = new XML_Parsing(DealerDetailsActivity.this, menu, 7);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Dealer),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

              /*  if (code.equals("057") || code.equals("008") || code.equals("09D")) {
                    SessionAlert(
                            context.getResources().getString(R.string.Dealer),
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg);

                    return;
                }*/
                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Menus),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);

                    } else {
                        if (isUpdateAckReq) {
                            System.out.println("@@Update acknowledgement");
                            System.out.println("==========UPDTAE ACKNOWLEDGEMENT========");
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedDate = df.format(c.getTime());
                            System.out.println("KeyregisterDownloadDATEEEE ======"+formattedDate);
                            SharedPref.saveData("KeyregisterDownloadDate",formattedDate);
                            updateDownloadStatus();
                        } else {
                            System.out.println("@@show txn details");
                            System.out.println("==========SHOW TXN DETAILS========");
                            showTxnsdetails();
                        }
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            //Timber.tag("DealerAuth-Menus-").e(ex.getMessage(), "");
            Timber.e("DealerDetailsActivity-DealerAuth-hitURLMENU Exception :"+ex.getLocalizedMessage());
        }
    }


    public void updateDownloadStatus() {

        System.out.println("===========UPLOAD DOWNLOAD STATUS==========");
        try {
            Show(context.getResources().getString(R.string.updateDownloadSts), context.getResources().getString(R.string.Consent_Form));
            //pd = ProgressDialog.show(context, context.getResources().getString(R.string.updateDownloadSts), context.getResources().getString(R.string.Consent_Form), true, false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String message = "Invalid Response from Server\\nPlease try again";
                    OfflineUploadNDownload offlineUploadNDownload = new OfflineUploadNDownload(context);
                    ResponseData responseData = offlineUploadNDownload.postDataDownloadAck(dealerConstants.stateBean.statefpsId, dealerConstants.fpsCommonInfo.fpsSessionId, dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus, dealerConstants.fpsCommonInfo.keyregisterDataDeleteStatus);

                    if (responseData != null) {

                        DealerDetailsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            /*if (pd.isShowing())
                                pd.dismiss();*/
                                Dismiss();
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                alertDialogBuilder.setMessage(responseData.getRespMessage());
                                alertDialogBuilder.setTitle("Download ACK Response");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (responseData.getRespCode() == 0) {

                                            showTxnsdetails();
                                        }
                                    }
                                });

                                //small change
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                //alertDialog.show();
                                DealerDetailsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (!((Activity) context).isFinishing()) {
                                            try {
                                                alertDialog.show();
                                                //System.out.println("$$$$$$<<<<TEJJJ>>>>$$$$$");
                                            } catch (WindowManager.BadTokenException e) {
                                                Log.e("WindowManagerBad ", e.toString());
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        show_AlertDialog("DATA", "Uploading data not available", "", 3);
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //Offline
    public void showTxnsdetails() {
        System.out.println("@@In showTxnsdetails");
        int offlineCount = 0, onlineCount = 0, uploadedCount = 0, pendingCount = 0;
        try {
            int saleRecordCount[] = databaseHelper.getSaleRecordAgrregateCounts();
            onlineCount = saleRecordCount[0];
            offlineCount = saleRecordCount[1];
            uploadedCount = saleRecordCount[2];

            pendingCount = saleRecordCount[3];
        } catch (Exception e) {
            System.out.println("@@Exception cought: " + e.toString());
            e.printStackTrace();
        }
            //"Date : 15-08-1947 00:00:00 \n"
            PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
            System.out.println("PartialOnlineData111>>>>>>>>>>" + partialOnlineData);
            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            AlertDialog.Builder alert = new AlertDialog.Builder(DealerDetailsActivity.this,
                    AlertDialog.THEME_HOLO_LIGHT);
            alert.setTitle(context.getResources().getString(R.string.Transaction_Details));
            String details = "PDS-"+" : V"+appversion+" \n" +
                    context.getResources().getString(R.string.Total_Txn_Records) + " :   " + (onlineCount + offlineCount) + "\n " +
                    context.getResources().getString(R.string.Online_Txn_Records) + " :  " + onlineCount + " \n " +
                    context.getResources().getString(R.string.Offline_Txn_Records) + ":  " + offlineCount + " \n" +
                    context.getResources().getString(R.string.Uploaded_Offline_Records) + ":  " + uploadedCount + "\n" +
                    context.getResources().getString(R.string.Pending_Offline_Records) + ":   " + pendingCount + "\n" +
                    context.getResources().getString(R.string.Alloted_Month_Year) + ":  " + partialOnlineData.getAllotMonth() + "-" + partialOnlineData.getAllotYear() + "\n" +
                    context.getResources().getString(R.string.Date) + ": " + formattedDate + "\n" +
                    context.getResources().getString(R.string.Fps_Id) + " : " + dealerConstants.fpsCommonInfo.fpsId;
            alert.setMessage(details);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("@@Going to home activity");
                    dialog.dismiss();
                    Intent home = new Intent(context, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                    finish();
                }
            });
        alert.show();
        //small change
         /*DealerDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!((Activity) context).isFinishing()) {
                    try {
                        alert.show();
                    } catch (WindowManager.BadTokenException e) {
                        Log.e("WindowManagerBad ", e.toString());
                    }
                }
            }
        });*/

    }


    private void prep_consent() {
        try {

            System.out.println("@@ In dealer details else case");
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
                    /* "   \"token\" : "+"\""+ dealerConstants.fpsURLInfo.token+"\""+"\n" +*/
                    "   \"token\" : " + "\"9f943748d8c1ff6ded5145c59d0b2ae7\"" + "\n" +
                    "}";
            if(Debug) {
                Util.generateNoteOnSD(context, "ConsentFormReq.txt", consentrequest);
            }
            ConsentformURL(consentrequest);
        } catch (Exception ex) {

            Timber.tag("DealerAuth-CnsntFmt-").e(ex.getMessage(), "");
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
            final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.check);
            tv.setText(concent);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    if (checkBox.isChecked()) {
                        dealerModel.Fusionflag = 0;
                        dealerModel.wadhflag = 0;
                        dealerModel.FIRflag = 0;
                        dealerModel.fusionflag = 0;
                        dealerModel.fCount = "1";
                        callScanFP();
                        System.out.println(">>>>>>>>>>>>>>");
                        dialog.dismiss();
                    } else {
                        show_AlertDialog(
                                Dealername,
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

            Timber.tag("DealerAuth-Consent-").e(ex.getMessage(), "");
        }
    }

    private void callScanFP() {
        try {
            if (dealerModel.Dwadh.equals("Y")) {

                connectRDservice(dealerConstants.fpsCommonInfo.wadhValue, 1);

            } else {
                System.out.println("TEJJ===FUSION====1");
                if (dealerModel.Dfusion.equals("1")) {
                    System.out.println("TEJJ===FUSION Condition");
                    dealerModel.fCount = "2";
                }
                System.out.println("TEJJ===FUSION===2");
                System.out.println("fingerPrint Request====1");
                connectRDservice("", 0);
                System.out.println("fingerPrint Request====2");
            }

        } catch (Exception ex) {

            Timber.tag("DealerAuth-ScanFp-").e(ex.getMessage(), "");
        }
    }

    private void prep_Dlogin() {
        System.out.println("=====PrepareDialogue=====");
        try {
            String dealerlogin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:ns1=\"http://www.uidai.gov.in/authentication/uid-auth-request/2.0\"\n" +
                    "    xmlns:ns2=\"http://service.fetch.rationcard/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns2:getAuthenticateNICAuaAuthRD2>\n" +
                    "            <aadhaarAuthType>"+dealerModel.DaadhaarAuthType+"</aadhaarAuthType>\n"+
                    "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                    "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                    "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                    "            <uidNumber>" + dealerModel.DUid + "</uidNumber>\n" +
                    "            <udc>" + DEVICEID + "</udc>\n" +
                    "            <authMode>" + dealerModel.Dtype + "</authMode>\n" +
                    "            <User_Id>" + dealerConstants.fpsCommonInfo.fpsId + "</User_Id>\n" +
                    "            <auth_packet>\n" +
                    "                <ns1:certificateIdentifier>" + dealerModel.rdModel.ci + "</ns1:certificateIdentifier>\n" +
                    "                <ns1:dataType>" + dealerModel.rdModel.type + "</ns1:dataType>\n" +
                    "                <ns1:dc>" + dealerModel.rdModel.dc + "</ns1:dc>\n" +
                    "                <ns1:dpId>" + dealerModel.rdModel.dpId + "</ns1:dpId>\n" +
                    "                <ns1:encHmac>" + dealerModel.rdModel.hmac + "</ns1:encHmac>\n" +
                    "                <ns1:mc>" + dealerModel.rdModel.mc + "</ns1:mc>\n" +
                    "                <ns1:mid>" + dealerModel.rdModel.mi + "</ns1:mid>\n" +
                    "                <ns1:rdId>" + dealerModel.rdModel.rdsId + "</ns1:rdId>\n" +
                    "                <ns1:rdVer>" + dealerModel.rdModel.rdsVer + "</ns1:rdVer>\n" +
                    "                <ns1:secure_pid>" + dealerModel.rdModel.pid + "</ns1:secure_pid>\n" +
                    "                <ns1:sessionKey>" + dealerModel.rdModel.skey + "</ns1:sessionKey>\n" +
                    "            </auth_packet>\n" +
                    "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                    "            <scannerId></scannerId>\n" +
                    "            <authType>" + dealerModel.DEALER_AUTH_TYPE + "</authType>\n" +
                    "            <memberId>" + dealerModel.Dtype + "</memberId>\n" +
                    "            <wadhStatus>" + dealerModel.Dwadh + "</wadhStatus>\n" +
                    "            <Resp>\n" +
                    "                <errCode>0</errCode>\n" +
                    "                <errInfo>y</errInfo>\n" +
                    "                <nmPoints>" + dealerModel.rdModel.nmpoint + "</nmPoints>\n" +
                    "                <fCount>" + dealerModel.rdModel.fcount + "</fCount>\n" +
                    "                <fType>" + dealerModel.rdModel.ftype + "</fType>\n" +
                    "                <iCount>" + dealerModel.rdModel.icount + "</iCount>\n" +
                    "                <iType>" + dealerModel.rdModel.itype + "</iType>\n" +
                    "                <pCount>0</pCount>\n" +
                    "                <pType>0</pType>\n" +
                    "                <qScore>0</qScore>\n" +
                    "            </Resp>\n" +
                    "        </ns2:getAuthenticateNICAuaAuthRD2>\n" +
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
                if(Debug) {
                    Util.generateNoteOnSD(context, "DealerAuthReq.txt", dealerlogin);
                }
                hitURLDealerAuthentication(dealerlogin);
                Timber.d("DealerDetailsActivity-DealerAuthReq :"+dealerlogin);
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.Dealer),
                        context.getResources().getString(R.string.Internet_Connection),
                        context.getResources().getString(R.string.Internet_Connection_Msg),
                        0);

            }
        } catch (Exception ex) {

            Timber.tag("DealerAuth-AuthFrmt-").e(ex.getMessage(), "");
        }
    }

    private void connectRDservice(String wadhvalue, int ekyc) {
        try {
            String xmplpid = PIDFormat(wadhvalue, ekyc);
            Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            final boolean isIntentSafe = activities.size() > 0;
            act.putExtra("PID_OPTIONS", xmplpid);
            startActivityForResult(act, dealerModel.RD_SERVICE);

        } catch (Exception ex) {

            Timber.tag("DealerAuth-RDservice-").e(ex.getMessage(), "");
        }
    }
    private String PIDFormat(String wadhvalue, int ekyc) {
        String xmplpid;
        if (ekyc == 1) {
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

            dealerModel.fCount = "1";
            //dealerModel.fType = "0";//******CHANGEfType******/
            dealerModel.fType = dealerModel.DaadhaarAuthType;
            xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<PidOptions ver =\"1.0\">\n" +
                    "    <Opts env=\"P\" fCount=\"" + dealerModel.fCount + "\" iCount=\"" + dealerModel.iCount + "\" iType=\"" + dealerModel.iType + "\" fType=\"" + dealerModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"" + wadhvalue + "\" posh=\"UNKNOWN\"/>\n" +
                    "</PidOptions>";
            System.out.println("XMPLPID1 ====");
        } else {
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

            /*if (dealerConstants.fpsCommonInfo.firauthFlag.equals("Y")) {
                dealerModel.fCount = dealerConstants.fpsCommonInfo.firauthCount;
                dealerModel.fType = "1";
                xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<PidOptions ver =\"1.0\">\n" +
                        "    <Opts env=\"P\" fCount=\"" + dealerModel.fCount + "\" iCount=\"" +dealerModel. iCount + "\" iType=\"" +dealerModel. iType + "\" fType=\"" + dealerModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"\" posh=\"UNKNOWN\"/>\n" +
                        "</PidOptions>";
                System.out.println("FIR Request");
            } else {*/
              //dealerModel.fType = "0";//******CHANGEfType******/
              //dealerModel.fType = "2";
              dealerModel.fType = dealerModel.DaadhaarAuthType;
              xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                          "<PidOptions ver =\"1.0\">\n" +
                          "    <Opts env=\"P\" fCount=\"" + dealerModel.fCount + "\" iCount=\"" + dealerModel.iCount + "\" iType=\"" + dealerModel.iType + "\" fType=\"" + dealerModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"\" posh=\"" + fposh + "\"/>\n" +
                          "</PidOptions>";
                  System.out.println("XMPLPID2 ====" + xmplpid);

            // }
        }
        return xmplpid;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            System.out.println("OnActivityResult");
            if (requestCode == dealerModel.RD_SERVICE) {
                if (resultCode == Activity.RESULT_OK) {
                    String piddata = data.getStringExtra("PID_DATA");
                    int code = createAuthXMLRegistered(piddata);
                    if (piddata != null && piddata.contains("errCode=\"0\"")) {
                        if (code == 0) {
                            prep_Dlogin();
                        } else {
                            show_AlertDialog(
                                    context.getResources().getString(R.string.RD_Service),
                                    dealerModel.err_code,
                                    dealerModel.rdModel.errinfo,
                                    0);
                        }
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.RD_Service),
                                dealerModel.err_code,
                                dealerModel.rdModel.errinfo,
                                0);

                    }
                }
            }else if(requestCode == 2){
                  fposh = data.getStringExtra("FUSION_DATA");
                  System.out.println("FUSIONDATA====="+fposh);
                  callScanFP();
            }
        } catch (Exception ex) {

            Timber.tag("DealerAuth-PIDRes-").e(ex.getMessage(), "");
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

            dealerModel.err_code = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errCode").getTextContent();
            if (!dealerModel.err_code.equals("0")) {
                dealerModel.rdModel.errinfo = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errInfo").getTextContent();
                return 1;
            } else {
                dealerModel.rdModel.icount = "0";
                dealerModel.rdModel.itype = "0";
                dealerModel.rdModel.fcount = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fCount").getTextContent();

                dealerModel.rdModel.ftype = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fType").getTextContent();

                dealerModel.rdModel.nmpoint = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("nmPoints").getTextContent();

                dealerModel.rdModel.pid = doc.getElementsByTagName("Data").item(0).getTextContent();

                dealerModel.rdModel.skey = doc.getElementsByTagName("Skey").item(0).getTextContent();

                dealerModel.rdModel.ci = doc.getElementsByTagName("Skey").item(0).getAttributes().getNamedItem("ci").getTextContent();

                dealerModel.rdModel.hmac = doc.getElementsByTagName("Hmac").item(0).getTextContent();

                dealerModel.rdModel.type = doc.getElementsByTagName("Data").item(0).getAttributes().getNamedItem("type").getTextContent();

                dealerModel.rdModel.dpId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dpId").getTextContent();

                dealerModel.rdModel.rdsId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsId").getTextContent();

                dealerModel.rdModel.rdsVer = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsVer").getTextContent();

                dealerModel.rdModel.dc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dc").getTextContent();

                dealerModel.rdModel.mi = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mi").getTextContent();

                dealerModel.rdModel.mc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mc").getTextContent();

                dealerModel.rdModel.skey = dealerModel.rdModel.skey.replaceAll(" ", "\n");

            }

        } catch (Exception ex) {

            Timber.tag("DealerAuth-onCreate-").e(ex.getMessage(), "");
            ex.printStackTrace();
            dealerModel.rdModel.errinfo = String.valueOf(ex);
            return 2;
        }
        return 0;
    }

    public interface OnClickDealer {
        void onClick(int p);
    }

    @Override
    public void initialize() {
        try {
            context = DealerDetailsActivity.this;
            databaseHelper = new DatabaseHelper(context);
            mHandler = new Handler(context.getMainLooper());
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_dealer__details, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);

            memberModel = (MemberModel) getIntent().getSerializableExtra("OBJ");
            initializeControls();

            RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            scanfp.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if (dealerModel.click) {
                        if (Util.networkConnected(context)) {
                            if (dealerModel.DAtype.equals("P")) {
                                password_Dialog();
                            } else {
                                if (L.equals("hi")){
                                    ConsentDialog(ConsentForm(context,1));
                                }else {
                                    ConsentDialog(ConsentForm(context,0));
                                }
                            }
                        } else {
                            show_AlertDialog(context.getResources().getString(R.string.Dealer),
                                    context.getResources().getString(R.string.Internet_Connection),
                                    context.getResources().getString(R.string.Internet_Connection_Msg),
                                    0);
                        }
                    } else {
                        if (mp != null) {
                            releaseMediaPlayer(context, mp);
                        }
                        if (L.equals("hi")) {
                        } else {
                            mp = mp.create(context, R.raw.c100176);
                            mp.start();

                        }
                        show_AlertDialog(context.getResources().getString(R.string.Dealer),
                                context.getResources().getString(R.string.Please_Select_Dealer_Name),
                                ""
                                , 0);
                    }
                }
            });
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    show_Dialogbox(context.getResources().getString(R.string.Dealer),
                            context.getResources().getString(R.string.Do_you_want_to_Quit));
                }
            });

            ArrayList<DealerListModel> data = new ArrayList<>();
            int dealerlistsize = dealerConstants.fpsCommonInfo.fpsDetails.size();
            for (int i = 0; i < dealerlistsize; i++) {
                if (L.equals("hi")) {
                    String hindidealertype = null;
                   /* if (dealerConstants.fpsCommonInfo.fpsDetails.get(i).dealer_type.equals("D")){
                        hindidealertype="डीलर";
                    }*/
                    data.add(new DealerListModel(
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delNamell,
                            //hindidealertype,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).dealer_type,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delUid));
                } else {
                    data.add(new DealerListModel(
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delName,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).dealer_type,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delUid));
                }
            }
            adapter = new DealerListAdapter(context, data, new OnClickDealer() {
                @Override
                public void onClick(int p) {
                    dealerModel.click = true;
                    dealerModel.Fusionflag = 0;
                    dealerModel.wadhflag = 0;
                    dealerModel.FIRflag = 0;
                    dealerModel.fusionflag = 0;
                    dealerModel.DName = dealerConstants.fpsCommonInfo.fpsDetails.get(p).delName;
                    dealerModel.DUid = dealerConstants.fpsCommonInfo.fpsDetails.get(p).delUid;
                    dealerModel.Dtype = dealerConstants.fpsCommonInfo.fpsDetails.get(p).dealer_type;
                    dealerModel.DAtype = dealerConstants.fpsCommonInfo.fpsDetails.get(p).authType;
                    dealerModel.Dfusion = dealerConstants.fpsCommonInfo.fpsDetails.get(p).dealerFusion;
                    dealerModel.Dnamell = dealerConstants.fpsCommonInfo.fpsDetails.get(p).delNamell;
                    dealerModel.Dwadh = dealerConstants.fpsCommonInfo.fpsDetails.get(p).wadhStatus;
                    dealerModel.DaadhaarAuthType = dealerConstants.fpsCommonInfo.fpsDetails.get(p).aadhaarAuthType;
                    System.out.println("aadhaarAuthType====DealerDetailsActivity"+dealerModel.DaadhaarAuthType);

                    if (L.equals("hi")) {
                        Dealername = dealerModel.Dnamell;
                    } else {
                        Dealername = dealerModel.DName;
                    }

                    switch (dealerModel.DAtype) {
                        case "F":
                            dealerModel.DEALER_AUTH_TYPE = "Bio";
                            break;
                        case "P":
                            dealerModel.DEALER_AUTH_TYPE = "P";
                            break;
                    }
                }
            }, 0);
            recyclerView.setAdapter(adapter);
        } catch (Exception ex) {

            Timber.tag("DealerAuth-onCreate-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initializeControls() {
        pd = new ProgressDialog(context);
        scanfp = findViewById(R.id.dealer_scanFP);
        back = findViewById(R.id.dealer_back);
        toolbarActivity.setText(context.getResources().getString(R.string.DEALER_DETAILS));
        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
    }

    private void show_Dialogbox(String header, String msg) {

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
                preventTwoClick(v);
                dialog.dismiss();
                finish();
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
                preventTwoClick(v);
                dialog.dismiss();
                if (i == 1) {
                    //show_AlertDialog("DealerAuthentication","Please place two fingers on the \n scanner one after the other","",3);
                    //callScanFP();
                    Intent fingerslection = new Intent(context, FusionFingerSectionActivity.class);
                    //fingerslection.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(fingerslection,2);

                } else if (i == 2) {
                    prep_consent();
                }/*else if(i == 3){
                    callScanFP();
                }*/

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void SessionAlert(String headermsg, String bodymsg, String talemsg) {
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
                preventTwoClick(v);
                dialog.dismiss();
                Intent i = new Intent(context, StartActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

            }
        });
        return;
    }
//small modify
    public void Dismiss() {
                 try {
                   if (pd != null) {
                       if (pd.isShowing()) {
                         pd.dismiss();
                     }
                 }
             }catch ( IllegalArgumentException e){
                 e.printStackTrace();
             }catch (Exception e){
                 e.printStackTrace();
             }
          }
   //some modify
    public void Show(String title, String msg) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);

        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        //pd.show();
       DealerDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!((Activity) context).isFinishing()) {
                    try {
                        pd.show();
                    } catch (WindowManager.BadTokenException e) {
                        Log.e("WindowManagerBad ", e.toString());
                    }
                }
            }
        });


        /*try {

        }
        catch (WindowManager.BadTokenException e) {
            //use a log message
            //e.printStackTrace();
        }*/

        //pd.show();
    }

    public class UploadPendingRecords extends AsyncTask<Void, Void, Integer> {
        String fpsSessionId, fpsId, terminalId, partialDataDownloadFlag, errorMessage;
        OfflineUploadNDownload offlineUploadNDownload;

        public UploadPendingRecords(String fpsSessionId, String fpsId, String terminalId, String partialDataDownloadFlag) {
            this.fpsSessionId = fpsSessionId;
            this.fpsId = fpsId;
            this.terminalId = terminalId;
            this.partialDataDownloadFlag = partialDataDownloadFlag;
            offlineUploadNDownload = new OfflineUploadNDownload(context);
            errorMessage = "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessage(context.getResources().getString(R.string.Please_Wait_Uploading_Pending_Records));
        }
       //change pd
        public void showMessage(String message) {
            pd.setMessage(message);
            mHandler.post(new Runnable() {
                public void run() {
                    //if(pd!= null)
                     pd.show();
                }
            });

        }
        @Override
        protected void onPostExecute(Integer ret) {
            System.out.println("@@ onPostExecute uploading pending records");
            super.onPostExecute(ret);
            if (pd.isShowing())
                pd.dismiss();
            if (ret == 0) {
                System.out.println("@@Return value zero");
                if (errorMessage.isEmpty()) {
                    String keyregister = "{\n" +
                            "\"fpsId\" : " + "\"" + fpsId + "\"" + ",\n" +
                            "\"sessionId\" : " + "\"" + fpsSessionId + "\"" + ",\n" +
                            "\"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                            "\"token\" : " + "\"" + OFFLINE_TOKEN + "\"" + ",\n" +
                            "\"stateCode\" : " + "\"" + "22" + "\"" + "\n" +
                            "}";
                    keyregisterurl(keyregister);
                    Timber.d("DealerDetailsActivity-keyregister-1 :"+keyregister);

                } else {
                    String keyregister = "{\n" +
                            "\"fpsId\" : " + "\"" + fpsId + "\"" + ",\n" +
                            "\"sessionId\" : " + "\"" + fpsSessionId + "\"" + ",\n" +
                            "\"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                            "\"token\" : " + "\"" + OFFLINE_TOKEN + "\"" + ",\n" +
                            "\"stateCode\" : " + "\"" + "22" + "\"" + "\n" +
                            "}";
                    keyregisterurl(keyregister);
                    Timber.d("DealerDetailsActivity-keyregister-2:"+keyregister);
                }

            } else {
                System.out.println("@@Return vsl: " + ret);
                show_AlertDialog("Uploading error", errorMessage, "", 3);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... data) {
            int pendingTxns = databaseHelper.getPendingTxnCount();
            if (pendingTxns > 0) {
                int ret = offlineUploadNDownload.ManualServerUploadPartialTxns(fpsId, fpsSessionId);
                if (ret == -2) {
                    errorMessage = "Internet not available";
                    return ret;
                }
                ret = offlineUploadNDownload.updateTransStatus(fpsId, fpsSessionId, partialDataDownloadFlag);
                if (ret == 0) {
                    errorMessage = context.getResources().getString(R.string.All_Offline_txn_records_were_Uploaded_to_server);
                } else
                    errorMessage = context.getResources().getString(R.string.Pending_txn_records_are_exists_Please_try_again);
                return ret;
            } else {
                return 0;
            }
        }
    }

    private void keyregisterurl(String keyregister) {
        System.out.println("@@Executing keyregisterurl: " + keyregister);
        try {

            Show(context.getResources().getString(R.string.Beneficiary_Details), context.getResources().getString(R.string.Consent_Form));
            //pd = ProgressDialog.show(context, context.getResources().getString(R.string.Beneficiary_Details), context.getResources().getString(R.string.Consent_Form), true, false);
            Json_Parsing request = new Json_Parsing(context, keyregister, 5);
            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, Object object) throws SQLException {
                    System.out.println("@@Request complete");
                /*if (pd.isShowing()) {
                    pd.dismiss();
                }*/
                    Dismiss();
                    if (!code.equals("00")) {
                        System.out.println("@@Code not 0 in keyregisterurl");
                        show_AlertDialog(msg, code, "", 3);
                    } else {
                        if (dealerConstants.fpsURLInfo.fpsCbDownloadStatus.equals("Y")) {
                            fpsCommonInfo fpsCommonInfoData = dealerConstants.fpsCommonInfo;
                            String[] monthyear = databaseHelper.getMonthYear(context);
                            System.out.println("@@Month and Year: " + monthyear[0]);
                            String CB = "{\n" +
                                    "\"fpsId\" : " + "\"" + fpsCommonInfoData.fpsId + "\"" + ",\n" +
                                    "\"sessionId\" : " + "\"" + fpsCommonInfoData.fpsSessionId + "\"" + ",\n" +
                                    "\"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                                    "\"token\" : " + "\"" + OFFLINE_TOKEN + "\"" + ",\n" +
                                    "\"stateCode\" : " + "\"" + "22" + "\"" + ",\n" +
                                    "\"allocationMonth\" : " + "\"" + monthyear[0] + "\"" + ",\n" +
                                    "\"allocationYear\"  : " + "\"" + monthyear[1] + "\"" + "\n" +
                                    "}";

                            System.out.println("@@ Going to CBDownload");
                            CBDownload(CB);
                            Timber.d("DealerDeatilsActivity-CB :" + CB);
                        } else {
                            System.out.println("@@Going to getMenuData");
                            getMenuData(true);
                        }

                    }
                }

            });
        }catch (Exception e){

            e.printStackTrace();
            Timber.e("DealerDetailsActivity-keyregisterurl Exception ==>"+e.getLocalizedMessage());
        }
    }

    public void getMenuData(boolean isUpdateAckReq) {
        System.out.println("@@In getMenuData");
        String menu = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:menuDisplayService>\n" +
                "            <shop_number>" + dealerConstants.stateBean.statefpsId + "</shop_number>\n" +
                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "        </ns1:menuDisplayService>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
        if(Debug) {
            Util.generateNoteOnSD(context, "MenuReq.txt", menu);
        }
        hitURLMENU(menu, isUpdateAckReq);
        Timber.d("DealerDetailsActivity-MenuReq :"+menu);
    }

    private void CBDownload(String keyregister) {
        System.out.println("@@ In CBDownload");
        try {
            Show(context.getResources().getString(R.string.Beneficiary_Details), context.getResources().getString(R.string.Consent_Form));
            //pd = ProgressDialog.show(context, context.getResources().getString(R.string.Beneficiary_Details), context.getResources().getString(R.string.Consent_Form), true, false);
            Json_Parsing request = new Json_Parsing(context, keyregister, 6);
            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, Object object) throws SQLException {
                /*if (pd.isShowing()) {
                    pd.dismiss();
                }*/
                    Dismiss();
                    if (!code.equals("00")) {
                        show_AlertDialog(msg, code, "", 3);
                    } else {
                        getMenuData(true);

                    }
                }

            });
        }catch(Exception e)
        {
            e.printStackTrace();
            Timber.e("DealerDetailsActivity-CBDownload Exception ==>:"+e.getLocalizedMessage());

        }



    }

}