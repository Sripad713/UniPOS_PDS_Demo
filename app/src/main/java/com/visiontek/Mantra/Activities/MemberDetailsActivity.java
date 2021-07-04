package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.MemberListAdapter;
import com.visiontek.Mantra.Models.DATAModels.MemberListModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.Ekyc;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.MemberModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import static com.visiontek.Mantra.Models.AppConstants.Mdealer;
import static com.visiontek.Mantra.Models.AppConstants.MemberName;
import static com.visiontek.Mantra.Models.AppConstants.MemberUid;
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

public class MemberDetailsActivity extends AppCompatActivity {

    MemberModel memberModel=new MemberModel();
    Button scanfp, back;
    ProgressDialog pd = null;
    Context context;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member__details);
        try {

        context = MemberDetailsActivity.this;

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_AlertDialog(context.getResources().getString(R.string.Member),
                    context.getResources().getString(R.string.RD_Service),
                    context.getResources().getString(R.string.RD_Service_Msg),0);
            return;
        }

        initilisation();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MemberDetailsActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        scanfp.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                if (memberModel.click) {
                    if (memberModel.uid.equals("NA")){
                        show_AlertDialog(
                                context.getResources().getString(R.string.Mem)+MemberName,
                                context.getResources().getString(R.string.NA),
                                context.getResources().getString(R.string.NA_MSG),
                                0);

                        return;
                    }
                    if (networkConnected(context)) {
                        ConsentDialog(ConsentForm(context, 1));

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Member),
                                context.getResources().getString(R.string.Internet_Connection),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                0);
                    }
                } else {
                    if (mp!=null) {
                        releaseMediaPlayer(context,mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = mp.create(context, R.raw.c100065);
                        mp.start();
                    }
                    show_AlertDialog(
                            context.getResources().getString(R.string.Member),
                            context.getResources().getString(R.string.Please_Select_Member_Name),
                            "",
                            0);
                }
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
               finish();
            }
        });

        ArrayList<MemberListModel> data = new ArrayList<>();
        int memberdetailssize=memberConstants.memberdetails.size();
        for (int i = 0; i <memberdetailssize ; i++) {
            if(L.equals("hi")){
                data.add(new MemberListModel(
                        memberConstants.memberdetails.get(i).memberNamell,
                        memberConstants.memberdetails.get(i).uid));
            }else {
                data.add(new MemberListModel(
                        memberConstants.memberdetails.get(i).memberName,
                        memberConstants.memberdetails.get(i).uid));
            }
        }
        RecyclerView.Adapter adapter = new MemberListAdapter(context, data, new OnClickMember() {
            @Override
            public void onClick(int p) {
                memberModel.click = true;
                memberModel.Fusionflag = 0;
                memberModel.wadhflag = 0;
                memberModel.FIRflag = 0;
                memberModel.fusionflag = 0;

                memberModel.memberNamell = memberConstants.memberdetails.get(p).memberNamell;
                memberModel.memberName = memberConstants.memberdetails.get(p).memberName;
                memberModel.uid = memberConstants.memberdetails.get(p).uid;
                memberModel.zmemberId = memberConstants.memberdetails.get(p).zmemberId;
                memberModel.xfinger = memberConstants.memberdetails.get(p).xfinger;
                memberModel.zwgenWadhAuth = memberConstants.memberdetails.get(p).zwgenWadhAuth;
                memberModel.zmanual = memberConstants.memberdetails.get(p).zmanual;
                memberModel.member_fusion = memberConstants.memberdetails.get(p).member_fusion;
                memberModel.w_uid_status = memberConstants.memberdetails.get(p).w_uid_status;
                if(L.equals("hi")){
                    MemberName=memberModel.memberNamell;
                }else {
                    MemberName=memberModel.memberName;
                }

                MemberUid=memberConstants.memberdetails.get(p).uid;
                if (memberModel.xfinger.equals("Y")) {
                    memberModel.mBIO = true;
                    memberModel.mMan = false;
                    memberModel.mDeal = false;
                } else if (memberModel.zmanual.equals("M")) {
                    memberModel.mMan = true;
                    memberModel.mBIO = false;
                    memberModel.mDeal = false;
                } else if (memberModel.zmanual.equals("D")) {
                    memberModel.mDeal = true;
                    memberModel.mBIO = false;
                    memberModel.mMan = false;
                }
            }
        });
        recyclerView.setAdapter(adapter);
        }catch (Exception ex){

            Timber.tag("Member-onCreate-").e(ex.getMessage(),"");
        }
    }
    /*private void Sessiontimeout(String msg, String title) {

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
    }*/
    private void initilisation() {
        pd = new ProgressDialog(context);
        scanfp = findViewById(R.id.member_scanFP);
        back = findViewById(R.id.member_back);

        toolbarInitilisation();
    }


    private void prep_consent() {
        try {

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
         }catch (Exception ex){

            Timber.tag("Member-CnsntFmt-").e(ex.getMessage(),"");
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
        TextView tv=(TextView)dialog.findViewById(R.id.consent);
        tv.setText(concent);
        final CheckBox checkBox =(CheckBox) dialog.findViewById(R.id.check);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    dialog.dismiss();
                    memberModel.Fusionflag = 0;
                    memberModel.wadhflag = 0;
                    memberModel.FIRflag = 0;
                    memberModel.fusionflag = 0;
                    memberModel.fCount="1";
                    callScanFP();
                } else {
                    show_AlertDialog(
                            MemberName,
                            context.getResources().getString(R.string.Consent_Form),
                            context.getResources().getString(R.string.Please_check_Consent_Form),
                            4);

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
         }catch (Exception ex){

            Timber.tag("Member-consent-").e(ex.getMessage(),"");
        }
    }
    private void ConsentformURL(String consentrequest) {
        try {

        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Member), context.getResources().getString(R.string.Consent_Form), true, false);
        Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
        request.setOnResultListener(new Json_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, Object object) {
                Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Mem)+MemberName,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }


                if (!code.equals("00")) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Consent_Form),
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,
                            0);

                } else {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Consent_Form),
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,
                            0);
                }
            }
        });
 }catch (Exception ex){

            Timber.tag("Member-ConsentReq-").e(ex.getMessage(),"");
        }

    }

   /* private  void show_AfterEkyc(String msg, String title, final String flow) {
        try {

         final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (flow.equals("D")) {
                            DealerAuth();
                        } else if (flow.equals("M")) {
                            ManualAuth();
                        }else if (flow.equals("F")){
                            memberModel.trans_type="E";
                            Intent ration = new Intent(context, RationDetailsActivity.class);
                            ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            ration.putExtra("OBJ", memberModel);
                            ration.putExtra("REF", Ekyc.zdistrTxnId);
                            startActivity(ration);
                            finish();
                        }

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
         }catch (Exception ex){

            Timber.tag("Member-ekycflow-").e(ex.getMessage(),"");
        }
    }
*/

 /*   private void show_error_box(String msg, String title, final int type) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (type == 1) {
                            callScanFP();
                        }else if (type==2){
                            AadhaarDialog();
                        }else if (type==3){
                            memberModel.trans_type="F";
                            Intent ration = new Intent(context, RationDetailsActivity.class);
                            ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            ration.putExtra("OBJ", memberModel);
                            ration.putExtra("REF", Ekyc.zdistrTxnId);
                            startActivity(ration);
                            finish();
                        }else if (type==4){
                            prep_consent();
                        }

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/

    private void callScanFP() {
        try {

        if (memberModel.mBIO) {
            memberModel.MEMBER_AUTH_TYPE = "Bio";
            if (memberModel.zwgenWadhAuth.equals("Y")) {

                connectRDserviceEKYC(memberConstants.carddetails.zwadh);
            } else {
                if (memberModel.member_fusion.equals("0")) {
                    memberModel.fCount = "2";
                }

                connectRDservice();
            }
        } else if (memberModel.mMan) {

            ManualAuth();
        } else if (memberModel.mDeal) {

            DealerAuth();
        } else {
            show_AlertDialog(
                    context.getResources().getString(R.string.Member),
                    context.getResources().getString(R.string.Authentication_Type),
                    context.getResources().getString(R.string.Authentication_Type_Not_Specified),
                    0);

        }
         }catch (Exception ex){

            Timber.tag("Member-scanfp-").e(ex.getMessage(),"");
        }
    }

    private void DealerAuth() {
        try {

        Mdealer = 1;
        Intent dealer = new Intent(getApplicationContext(), DealerDetailsActivity.class);
        dealer.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        dealer.putExtra("OBJ",  memberModel);
        startActivityForResult(dealer, 2);
        }catch (Exception ex){

            Timber.tag("Member-DealerAuth-").e(ex.getMessage(),"");
        }
    }

    private void ManualAuth() {
        try {

        String manual = "<soapenv:Envelope\n" +
                "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <ser:ackRequest>\n" +
                "            <fpsCard>" + memberConstants.carddetails.rcId + "</fpsCard >\n" +
                "            <terminalId>" + DEVICEID + "</terminalId>\n" +
                "            <user_password>" + dealerConstants.fpsCommonInfo.dealer_password + "</user_password>\n" +
                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <uidNumber>" + memberModel.uid + "</uidNumber>\n" +
                "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                "            <auth_type>M</auth_type>\n" +
                "            <user_type>B</user_type>\n" +
                "            <memberId>" + memberModel.zmemberId + "</memberId>\n" +
                "            <fpsId>" + dealerConstants.fpsCommonInfo.fpsId + "</fpsId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "        </ser:ackRequest>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        Util.generateNoteOnSD(context, "ManualRes.txt", manual);
        if (networkConnected(context)) {
            hitManual(manual);
        } else {
            show_AlertDialog(context.getResources().getString(R.string.Manual),
                    context.getResources().getString(R.string.Internet_Connection),
                    context.getResources().getString(R.string.Internet_Connection_Msg),
                    0);        }
         }catch (Exception ex){

            Timber.tag("Member-Manual-").e(ex.getMessage(),"");
        }
    }

    private void hitManual(String manual) {
        try {

            Show(context.getResources().getString(R.string.Member),
                    context.getResources().getString(R.string.Authenticating) );
/*
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Dealer), context.getResources().getString(R.string.Authenticating), true, false);
*/
        XML_Parsing request = new XML_Parsing(MemberDetailsActivity.this, manual, 10);
        request.setOnResultListener(new XML_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, String ref, String flow, Object object) {
               Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Manual)+MemberName,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }


                if (!code.equals("00")) {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Man)+MemberName,
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,
                            0);
                } else {
                    memberModel.trans_type="F";
                    Intent ration = new Intent(context, RationDetailsActivity.class);
                    ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ration.putExtra("OBJ",  memberModel);
                    startActivity(ration);
                    finish();
                }
            }
        });
        request.execute();
         }catch (Exception ex){

            Timber.tag("Member-ManualRes-").e(ex.getMessage(),"");
        }
    }

    private void hitURL1(String memberlogin) {
        try {

            Show(context.getResources().getString(R.string.Member_Authentication),
                    context.getResources().getString(R.string.Processing));
/*
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Member_Authentication),
         context.getResources().getString(R.string.Processing), true, false);
*/
        XML_Parsing request = new XML_Parsing(MemberDetailsActivity.this, memberlogin, 4);
        request.setOnResultListener(new XML_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Manual)+MemberName,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }


                if (!code.equals("00")) {
                    if (code.equals("300") && flow.equals("F")) {
                        if (memberModel.zwgenWadhAuth.equals("Y")) {
                            if (memberModel.wadhflag != 1) {
                                memberModel.wadhflag = 1;
                                show_AlertDialog(context.getResources().getString(R.string.Member_Wadh)+MemberName,
                                        context.getResources().getString(R.string.ResponseCode) + code,
                                        context.getResources().getString(R.string.ResponseMsg)+ msg,
                                        1);
                            } else {
                                memberModel.EKYC = 1;
                                memberModel.fCount = "1";
                                show_AlertDialog(context.getResources().getString(R.string.ekyc)+MemberName,
                                        context.getResources().getString(R.string.ResponseCode) + code,
                                        context.getResources().getString(R.string.ResponseMsg)+ msg,
                                        2);

                            }
                        } /*else if (fpsCommonInfo.firauthFlag.equals("Y")) {
                            if (memberModel.FIRflag != 1) {
                                memberModel.FIRflag = 1;
                                show_error_box(msg, context.getResources().getString(R.string.Dealer_FIR_Authentication) + isError, 1);
                            } else {
                                memberModel.EKYC = 1;
                                memberModel.fCount = "1";
                                show_error_box(msg, context.getResources().getString(R.string.Member_Authentication) + isError, 2);

                            }
                        }*/ else if (memberModel.member_fusion.equals("1")) {
                            if (memberModel.Fusionflag != 1) {
                                memberModel.fCount = "2";
                                memberModel.Fusionflag = 1;
                                show_AlertDialog(context.getResources().getString(R.string.Member_Fusion)+MemberName,
                                        context.getResources().getString(R.string.ResponseCode) + code,
                                        context.getResources().getString(R.string.ResponseMsg)+ msg,
                                        1);

                            } else {
                                memberModel.EKYC = 1;
                                memberModel.fCount = "1";
                                show_AlertDialog(context.getResources().getString(R.string.ekyc)+MemberName,
                                        context.getResources().getString(R.string.ResponseCode) + code,
                                        context.getResources().getString(R.string.ResponseMsg)+ msg,
                                        2);
                            }
                        } else {
                            if (memberModel.fusionflag != 1) {
                                memberModel.fCount = "2";
                                memberModel.fusionflag = 1;
                                show_AlertDialog(context.getResources().getString(R.string.Member_Fusion)+MemberName,
                                        context.getResources().getString(R.string.ResponseCode) + code,
                                        context.getResources().getString(R.string.ResponseMsg)+ msg,
                                        1);
                            } else {
                                memberModel.EKYC = 1;
                                memberModel.fCount = "1";
                                show_AlertDialog(context.getResources().getString(R.string.ekyc)+MemberName,
                                        context.getResources().getString(R.string.ResponseCode) + code,
                                        context.getResources().getString(R.string.ResponseMsg)+ msg,
                                        2);
                            }
                        }
                        return;
                    }
                    memberModel.fCount = "1";
                    show_AlertDialog(context.getResources().getString(R.string.Mem)+MemberName,
                            context.getResources().getString(R.string.ResponseCode) + code,
                            context.getResources().getString(R.string.ResponseMsg)+ msg,
                            0);
                } else {
                    if (memberModel.fusionflag == 1) {
                        memberModel.fusionflag = 0;
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
                                "            <user_type>MEM</user_type>\n" +
                                "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                                "            <uidNumber>" + memberModel.uid + "</uidNumber>\n" +
                                "            <member_fusion>1</member_fusion>\n" +
                                "            <member_id>MEM</member_id>\n" +
                                "        </ns1:getFusionRecord>\n" +
                                "    </SOAP-ENV:Body>\n" +
                                "</SOAP-ENV:Envelope>";
                        Util.generateNoteOnSD(context, "MemberFusionReq.txt", fusion);
                        if (networkConnected(context)) {
                            hitURLfusion(fusion);

                        } else {
                            show_AlertDialog(context.getResources().getString(R.string.Member),
                                    context.getResources().getString(R.string.Internet_Connection),
                                    context.getResources().getString(R.string.Internet_Connection_Msg),
                                    0);
                        }
                    }
                    memberModel.trans_type="F";
                    Intent ration = new Intent(getApplicationContext(), RationDetailsActivity.class);
                    ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ration.putExtra("REF",ref);
                    ration.putExtra("OBJ", (Serializable) memberModel);
                    startActivity(ration);
                    finish();
                }
            }
        });
        request.execute();
        }catch (Exception ex){

            Timber.tag("Member-MemAuth-").e(ex.getMessage(),"");
        }
    }

    private void hitURLfusion(String fusion) {
        try {

      //  pd = ProgressDialog.show(context, context.getResources().getString(R.string.Member), context.getResources().getString(R.string.Authenticating), true, false);
        XML_Parsing request = new XML_Parsing(MemberDetailsActivity.this, fusion, 4);
        request.setOnResultListener(new XML_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String isError, String msg, String ref, String flow, Object object) {
              /*  if (pd.isShowing()) {
                    pd.dismiss();
                }*/
               /* if (isError == null || isError.isEmpty()) {
                    show_error_box("Invalid Response from Server", "No Response", 0);
                    return;
                }
                if (isError.equals("057") || isError.equals("008") || isError.equals("09D")) {
                    Sessiontimeout(msg, isError);
                    return;
                }*/

            }
        });
        request.execute();
        }catch (Exception ex){

            Timber.tag("Member-fusion-").e(ex.getMessage(),"");
        }
    }

    private void EKYCAuth() {
        try {

        String memeKyc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:getEKYCAuthenticateRD>\n" +
                "            <fpsSessionId>" +dealerConstants. fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                "            <terminal_id>" + DEVICEID + "</terminal_id>\n" +
                "            <existingRCNumber>" + memberConstants.carddetails.rcId + "</existingRCNumber>\n" +
                "            <rcMemberName>" + memberModel.memberName + "</rcMemberName>\n" +
                "            <rcUid>" + memberModel.uid + "</rcUid>\n" +
                "            <memberId>" + memberModel.zmemberId + "</memberId>\n" +
                "            <ekycresAuth>\n" +
                "                <dc>" + memberModel.rdModel.dc + "</dc>\n" +
                "                <dpId>" + memberModel.rdModel.dpId + "</dpId>\n" +
                "                <mc>" + memberModel.rdModel.mc + "</mc>\n" +
                "                <mid>" +memberModel.rdModel. mi + "</mid>\n" +
                "                <rdId>" +memberModel.rdModel. rdsId + "</rdId>\n" +
                "                <rdVer>" + memberModel.rdModel.rdsVer + "</rdVer>\n" +
                "                <res_Consent_POIandPOA>Y</res_Consent_POIandPOA>\n" +
                "                <res_Consent_mobileOREmail>Y</res_Consent_mobileOREmail>\n" +
                "                <res_certificateIdentifier>" + memberModel.rdModel.ci + "</res_certificateIdentifier>\n" +
                "                <res_encHmac>" + memberModel.rdModel.hmac + "</res_encHmac>\n" +
                "                <res_secure_pid>" + memberModel.rdModel.pid + "</res_secure_pid>\n" +
                "                <res_sessionKey>" + memberModel.rdModel.skey + "</res_sessionKey>\n" +
                "                <res_uid>" + memberModel.Aadhaar + "</res_uid>\n" +
                "            </ekycresAuth>\n" +
                "            <password>" +dealerConstants. fpsURLInfo.token + "</password>\n" +
                "            <eKYCType>eKYCN</eKYCType>\n" +
                "            <Resp>\n" +
                "                <errCode>0</errCode>\n" +
                "                <errInfo>y</errInfo>\n" +
                "                <nmPoints>" + memberModel.rdModel.nmpoint + "</nmPoints>\n" +
                "                <fCount>" + memberModel.rdModel.fcount + "</fCount>\n" +
                "                <fType>" +memberModel.rdModel. ftype + "</fType>\n" +
                "                <iCount>" + memberModel.rdModel.icount + "</iCount>\n" +
                "                <iType>" + memberModel.rdModel.itype + "</iType>\n" +
                "                <pCount>0</pCount>\n" +
                "                <pType>0</pType>\n" +
                "                <qScore>0</qScore>\n" +
                "            </Resp>\n" +
                "        </ns1:getEKYCAuthenticateRD>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";

        Util.generateNoteOnSD(context, "MembereKycReq.txt", memeKyc);
        if (networkConnected(context)) {
            hiteKyc(memeKyc);
        } else {
            show_AlertDialog(context.getResources().getString(R.string.Member),
                    context.getResources().getString(R.string.Internet_Connection),
                    context.getResources().getString(R.string.Internet_Connection_Msg),
                    0);
        }
  }catch (Exception ex){

            Timber.tag("Member-EkycAuth-").e(ex.getMessage(),"");
        }
    }
    Ekyc Ekyc;
    private void hiteKyc(String memeKyc) {
        try {

            Show(context.getResources().getString(R.string.Member),
                    context.getResources().getString(R.string.Member));
/*
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Member)
         context.getResources().getString(R.string.Authenticating_EKYC), true, false);
*/
        XML_Parsing request = new XML_Parsing(MemberDetailsActivity.this, memeKyc, 8);
        request.setOnResultListener(new XML_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.ekyc)+MemberName,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }

                Ekyc= (Ekyc) object;

                if (!code.equals("E00")) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.ekyc)+MemberName,
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,
                            0);
                } else {
                    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    String details = "\n"+
                            context.getResources().getString(R.string.MemberName) + Ekyc.eKYCMemberName + "\n" +
                            context.getResources().getString(R.string.DOB) + " : "+Ekyc.eKYCDOB + "\n" +
                            context.getResources().getString(R.string.PindCode) +" : "+ Ekyc.eKYCPindCode+ "\n" +
                            context.getResources().getString(R.string.Gender) +" : "+ Ekyc.eKYCGeneder+ "\n" +
                            context.getResources().getString(R.string.Date) +" : "+  currentDateTimeString + "\n";

                    show_EKYCAlertDialog(
                            context.getResources().getString(R.string.ekyc) +code,
                            msg + details,
                            flow);
                }
            }
        });
        request.execute();
        }catch (Exception ex){

            Timber.tag("Member-Ekycres-").e(ex.getMessage(),"");
        }
    }

    private void prep_Mlogin() {
        try {

        String memberlogin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"http://www.uidai.gov.in/authentication/uid-auth-request/2.0\"\n" +
                "    xmlns:ns2=\"http://service.fetch.rationcard/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns2:getAuthenticateNICAuaAuthRD2>\n" +
                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                "            <uidNumber>" + memberModel.uid + "</uidNumber>\n" +
                "            <udc>" + DEVICEID + "</udc>\n" +
                "            <authMode>B</authMode>\n" +
                "            <User_Id>" + memberConstants.carddetails.rcId + "</User_Id>\n" +
                "            <auth_packet>\n" +
                "                <ns1:certificateIdentifier>" +memberModel.rdModel. ci + "</ns1:certificateIdentifier>\n" +
                "                <ns1:dataType>" + memberModel.rdModel.type + "</ns1:dataType>\n" +
                "                <ns1:dc>" + memberModel.rdModel.dc + "</ns1:dc>\n" +
                "                <ns1:dpId>" + memberModel.rdModel.dpId + "</ns1:dpId>\n" +
                "                <ns1:encHmac>" + memberModel.rdModel.hmac + "</ns1:encHmac>\n" +
                "                <ns1:mc>" + memberModel.rdModel.mc + "</ns1:mc>\n" +
                "                <ns1:mid>" + memberModel.rdModel.mi + "</ns1:mid>\n" +
                "                <ns1:rdId>" + memberModel.rdModel.rdsId + "</ns1:rdId>\n" +
                "                <ns1:rdVer>" + memberModel.rdModel.rdsVer + "</ns1:rdVer>\n" +
                "                <ns1:secure_pid>" + memberModel.rdModel.pid + "</ns1:secure_pid>\n" +
                "                <ns1:sessionKey>" + memberModel.rdModel.skey + "</ns1:sessionKey>\n" +
                "            </auth_packet>\n" +
                "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                "            <scannerId></scannerId>\n" +
                "            <authType>" +memberModel. MEMBER_AUTH_TYPE + "</authType>\n" +
                "            <memberId>" + memberModel.zmemberId + "</memberId>\n" +
                "            <wadhStatus>" + memberModel.zwgenWadhAuth + "</wadhStatus>\n" +
                "            <Resp>\n" +
                "                <errCode>0</errCode>\n" +
                "                <errInfo>y</errInfo>\n" +
                "                <nmPoints>" +memberModel.rdModel. nmpoint + "</nmPoints>\n" +
                "                <fCount>" + memberModel.rdModel.fcount + "</fCount>\n" +
                "                <fType>" + memberModel.rdModel.ftype + "</fType>\n" +
                "                <iCount>" + memberModel.rdModel.icount + "</iCount>\n" +
                "                <iType>" + memberModel.rdModel.itype + "</iType>\n" +
                "                <pCount>0</pCount>\n" +
                "                <pType>0</pType>\n" +
                "                <qScore>0</qScore>\n" +
                "            </Resp>\n" +
                "        </ns2:getAuthenticateNICAuaAuthRD2>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
        Util.generateNoteOnSD(context, "MemberAuthReq.txt", memberlogin);
        hitURL1(memberlogin);
        }catch (Exception ex){

            Timber.tag("Member-AuthFmt-").e(ex.getMessage(),"");
        }
    }

    private void connectRDservice() {
        try {


            if (mp!=null) {
                releaseMediaPlayer(context,mp);
            }
            if(L.equals("hi") ) {
                mp=mp.create(context,R.raw.c200032);
                        mp.start();
            }
            else {
                mp=mp.create(context,R.raw.c100032);
                        mp.start();
            }
            String xmplpid;
           /* if (fpsCommonInfo.getfirauthFlag().equals("Y")) {
                memberModel.fCount = fpsCommonInfo.getfirauthCount();
                memberModel.fType = "1";
                xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<PidOptions ver =\"1.0\">\n" +
                        "    <Opts env=\"P\" fCount=\"" + memberModel.fCount + "\" iCount=\"" + memberModel.iCount + "\" iType=\"" + memberModel.iType + "\" fType=\"" + memberModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"\" posh=\"UNKNOWN\"/>\n" +
                        "</PidOptions>";
                System.out.println("FingerPrint Request");
            } else {*/
                memberModel.fType = "0";
                xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<PidOptions ver =\"1.0\">\n" +
                        "    <Opts env=\"P\" fCount=\"" + memberModel.fCount + "\" iCount=\"" + memberModel.iCount + "\" iType=\"" + memberModel.iType + "\" fType=\"" + memberModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"\" posh=\"UNKNOWN\"/>\n" +
                        "</PidOptions>";
         //   }

            Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            final boolean isIntentSafe = activities.size() > 0;

            act.putExtra("PID_OPTIONS", xmplpid);
            startActivityForResult(act, memberModel.RD_SERVICE);

        }catch (Exception ex){

            Timber.tag("Member-PIDReq-").e(ex.getMessage(),"");
        }
    }

    private void connectRDserviceEKYC(String wadhvalue) {


        try {
            if (mp!=null) {
                releaseMediaPlayer(context,mp);
            }
            if(L.equals("hi")) {
                mp=mp.create(context, R.raw.c200032);
                        mp.start();
            }

            else {
                mp=mp.create(context, R.raw.c100032);
                        mp.start();
            }

            memberModel.fCount = "1";
            memberModel.fType = "0";
            String xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<PidOptions ver =\"1.0\">\n" +
                    "    <Opts env=\"P\" fCount=\"" + memberModel.fCount + "\" iCount=\"" + memberModel.iCount + "\" iType=\"" +memberModel. iType + "\" fType=\"" + memberModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"" + wadhvalue + "\" posh=\"UNKNOWN\"/>\n" +
                    "</PidOptions>";
            Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            final boolean isIntentSafe = activities.size() > 0;

            act.putExtra("PID_OPTIONS", xmplpid);
            startActivityForResult(act, memberModel.RD_SERVICE);

        }catch (Exception ex){
            Timber.tag("Member-EkycPIDReq-").e(ex.getMessage(),"");
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
        dialogbox.setText(context.getResources().getString(R.string.EKYC));
        final EditText enter = (EditText) dialog.findViewById(R.id.enter);
        tv.setText(context.getResources().getString(R.string.Please_Enter_Member_UID_Number));
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                memberModel.Enter_UID = enter.getText().toString();
                if (memberModel.Enter_UID.length() == 12 && validateVerhoeff(memberModel.Enter_UID)) {
                    try {
                        memberModel.Aadhaar = encrypt(memberModel.Enter_UID, menuConstants.skey);
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
                    connectRDserviceEKYC(memberConstants.carddetails.zwadh);

                } else {
                    memberModel.EKYC = 0;
                    show_AlertDialog(
                            context.getResources().getString(R.string.Mem)+memberModel.Enter_UID,
                            context.getResources().getString(R.string.Invalid_UID),
                            context.getResources().getString(R.string.Please_enter_a_valid_Value),
                            0);

                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                memberModel.EKYC = 0;
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
        }catch (Exception ex){

            Timber.tag("Member-Aadhardilg-").e(ex.getMessage(),"");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("OnActivityResult");
        if (requestCode == memberModel.RD_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println(data.getStringExtra("PID_DATA"));
                String piddata = data.getStringExtra("PID_DATA");
                int code = createAuthXMLRegistered(piddata);
                if (piddata != null && piddata.contains("errCode=\"0\"") && code == 0) {
                    System.out.println("PID DATA = " + piddata);
                    if (memberModel.EKYC == 1) {
                        memberModel.EKYC = 0;
                        EKYCAuth();
                    } else {
                        prep_Mlogin();
                    }
                } else {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Dealer),
                            memberModel.err_code,
                            memberModel.rdModel.errinfo,
                            0);
                }
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.RD_Service),
                        memberModel.err_code,
                        memberModel.rdModel.errinfo,
                        0);

            }
        } else {
            Intent ration = new Intent(getApplicationContext(), RationDetailsActivity.class);
            ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            ration.putExtra("OBJ", (Serializable) memberModel);
            startActivity(ration);
            finish();
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

            memberModel.err_code = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errCode").getTextContent();
            if (!memberModel.err_code.equals("0")) {
                memberModel.rdModel.errinfo = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errInfo").getTextContent();
                return 1;
            } else {
                memberModel.rdModel.icount = "0";
                memberModel.rdModel.itype = "0";
                memberModel.rdModel.fcount = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fCount").getTextContent();
                memberModel.rdModel.ftype = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fType").getTextContent();
                memberModel.rdModel.nmpoint = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("nmPoints").getTextContent();
                memberModel.rdModel.pid = doc.getElementsByTagName("Data").item(0).getTextContent();
                memberModel.rdModel.skey = doc.getElementsByTagName("Skey").item(0).getTextContent();
                memberModel.rdModel.ci = doc.getElementsByTagName("Skey").item(0).getAttributes().getNamedItem("ci").getTextContent();
                memberModel.rdModel.hmac = doc.getElementsByTagName("Hmac").item(0).getTextContent();
                memberModel.rdModel.type = doc.getElementsByTagName("Data").item(0).getAttributes().getNamedItem("type").getTextContent();
                memberModel.rdModel.dpId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dpId").getTextContent();
                memberModel.rdModel.rdsId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsId").getTextContent();
                memberModel.rdModel.rdsVer = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsVer").getTextContent();
                memberModel.rdModel. dc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dc").getTextContent();
                memberModel.rdModel.mi = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mi").getTextContent();
                memberModel.rdModel.mc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mc").getTextContent();
                memberModel.rdModel.skey = memberModel.rdModel.skey.replaceAll(" ", "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.tag("Member-PIDData-").e(e.getMessage(),"");
            memberModel.rdModel.errinfo = String.valueOf(e);
            return 2;
        }
        return 0;
    }

    private void connectIRISRDservice(String xmplpid) {
        Intent intent = new Intent("in.gov.uidai.rdservice.iris.CAPTURE");
        intent.setClassName("com.iritech.rdservice", "com.iritech.rdservice.irishield.IriShieldRDActivity");
        intent.putExtra("PID_OPTIONS", xmplpid);
        startActivityForResult(intent, memberModel.RD_SERVICE);
    }

    public interface OnClickMember {
        void onClick(int p);
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
        toolbarCard.setText("RC:"+memberConstants.carddetails.rcId);

        String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
        System.out.println(appversion);
        toolbarVersion.setText("V" + appversion);


        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText( context.getResources().getString(R.string.MEMEBER_DETAILS));

        toolbarCard.setText(memberConstants.carddetails.rcId);
        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
        }catch (Exception ex){

            Timber.tag("Member-Toolbar-").e(ex.getMessage(),"");
        }
    }
    private void show_Dialogbox(String msg,String header) {

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
    private void show_EKYCAlertDialog(String headermsg,String bodymsg,String flow) {

            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.alertdialog);
            Button confirm = (Button) dialog.findViewById(R.id.alertdialogok);
            TextView head = (TextView) dialog.findViewById(R.id.alertdialoghead);
            TextView body = (TextView) dialog.findViewById(R.id.alertdialogbody);


        head.setText(headermsg);
        body.setText(bodymsg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (flow.equals("D")) {
                    DealerAuth();
                } else if (flow.equals("M")) {
                    ManualAuth();
                }else if (flow.equals("F")){
                    memberModel.trans_type="E";
                    Intent ration = new Intent(context, RationDetailsActivity.class);
                    ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ration.putExtra("OBJ", memberModel);
                    ration.putExtra("REF", Ekyc.zdistrTxnId);
                    startActivity(ration);
                    finish();
                }

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
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (i == 1) {
                    callScanFP();
                }else if (i==2){
                    AadhaarDialog();
                }else if (i==3){
                    memberModel.trans_type="F";
                    Intent ration = new Intent(context, RationDetailsActivity.class);
                    ration.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ration.putExtra("OBJ", memberModel);
                    ration.putExtra("REF", Ekyc.zdistrTxnId);
                    startActivity(ration);
                    finish();
                }else if (i==4){
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

}
