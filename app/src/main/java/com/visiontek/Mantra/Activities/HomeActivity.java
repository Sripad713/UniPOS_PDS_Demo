package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.visiontek.Mantra.Models.InspectionModel.InspectionDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.Util;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class HomeActivity extends BaseActivity {
    String token = "9f943748d8c1ff6ded5145c59d0b2ae7";
    String mode = "PDS";
    Button issue, inspection, aadhar, receive, reports, others, logout;
    Intent i;
    Context context;
    ProgressDialog pd = null;

    private void logout(String logoutrequest) {
        try {
            Show(context.getResources().getString(R.string.Logout),
                    "");

            Aadhaar_Parsing request = new Aadhaar_Parsing(context, logoutrequest, 10);
            request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }


                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                1);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                1);


                    }
                }

            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("Home-logout-").e(ex.getMessage(), "");
        }
    }

    private void Sessiontimeout(String msg, String title) {
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
    }

    private void FrameXMLforInspection() {
        try {

            String inspection = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                    "<soapenv:Envelope\n" +
                    "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                    "    <soapenv:Header/>\n" +
                    "    <soapenv:Body>\n" +
                    "        <ser:getInspectorDetails>\n" +
                    "            <fpsId>" + dealerConstants.stateBean.statefpsId + "</fpsId>\n" +
                    "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                    "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                    "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                    "        </ser:getInspectorDetails>\n" +
                    "    </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            if (networkConnected(context)) {
                //Util.generateNoteOnSD(context, "InspectionDetailsReq.txt", inspection);
                hit_Inspection(inspection);
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.Home),
                        context.getResources().getString(R.string.Internet_Connection_Msg),
                        context.getResources().getString(R.string.Internet_Connection),
                        0);
            }
        } catch (Exception ex) {

            Timber.tag("Home-InspFrmt-").e(ex.getMessage(), "");
        }
    }

    private void FrameJsonforReceiveGoods() {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100075);
                mp.start();
            }
            String receiveGoods = " {\n" +
                    "  \"fps_id\" : " + "\"" + dealerConstants.stateBean.statefpsId + "\"" + ",\n" +
                    "  \"mode\" :" + "\"" + mode + "\"" + ",\n" +
                    "  \"stateCode\"  :" + "\"" + dealerConstants.stateBean.stateCode + "\"" + ",\n" +
                    "  \"token\" :  " + "\"" + token + "\"" + "\n" +
                    "} ";
            //Util.generateNoteOnSD(context, "ReceiveGoodsReq.txt", receiveGoods);
            Show(context.getResources().getString(R.string.Receive_Goods),
                    context.getResources().getString(R.string.Processing)
            );


            Json_Parsing request = new Json_Parsing(context, receiveGoods, 1);

            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }


                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Receive_Goods),
                                context.getResources().getString(R.string.ResponseCode) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);
                    } else {
                        ReceiveGoodsDetails receiveGoodsDetails = (ReceiveGoodsDetails) object;
                        i = new Intent(context, ReceiveGoodsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("OBJ", (Serializable) receiveGoodsDetails);
                        startActivityForResult(i, 1);
                    }
                }

            });
        } catch (Exception ex) {

            Timber.tag("Home-RGReq-").e(ex.getMessage(), "");
        }
    }

    private void hit_Inspection(String inspection) {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100075);
                mp.start();
            }
            Show(context.getResources().getString(R.string.Inspection),
                    context.getResources().getString(R.string.Fetching_Details));

            Aadhaar_Parsing request = new Aadhaar_Parsing(context, inspection, 5);
            request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String code, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Logout),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }


                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Receive_Goods),
                                context.getResources().getString(R.string.Inspection_Details) + code,
                                context.getResources().getString(R.string.ResponseMsg) + msg,
                                0);
                    } else {
                        InspectionDetails inspectionDetails = (InspectionDetails) object;
                        Intent in = new Intent(context, InspectionActivity.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.putExtra("OBJ", (Serializable) inspectionDetails);
                        startActivity(in);
                    }
                }

            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("Home-InspReq-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initialize() {
        try {
            context = HomeActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_home, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


            TextView toolbarRD = findViewById(R.id.toolbarRD);
            boolean rd_fps = RDservice(context);
            if (rd_fps) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
                show_AlertDialog(
                        context.getResources().getString(R.string.Home),
                        context.getResources().getString(R.string.RD_Service),
                        context.getResources().getString(R.string.RD_Service_Msg), 0);
                return;
            }



            if (diableMenu("getFpsStockDetails")) {
                receive.setEnabled(false);
            }

            if (diableMenu("getInspectorDetails")) {
                inspection.setEnabled(false);
            }

            others.setEnabled(false);
            issue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    i = new Intent(context, IssueActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivityForResult(i, 1);
                }
            });
            inspection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    if (networkConnected(context)) {
                        FrameXMLforInspection();
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Home),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                context.getResources().getString(R.string.Internet_Connection),
                                0);
                    }

                }
            });
            aadhar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    i = new Intent(context, AadhaarServicesActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, 1);
                }
            });
            receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    if (networkConnected(context)) {
                        FrameJsonforReceiveGoods();
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Home),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                context.getResources().getString(R.string.Internet_Connection),
                                0);
                    }


                }
            });
            reports.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    i = new Intent(context, ReportsActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, 1);
                }
            });
            others.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                }
            });

            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    memberConstants = null;
                    String logoutreq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SOAP-ENV:Envelope\n" +
                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                            "    <SOAP-ENV:Body>\n" +
                            "        <ser:logOut>\n" +
                            "            <fpsId>" + dealerConstants.stateBean.statefpsId + "</fpsId>\n" +
                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                            "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                            "            <deviceId>" + DEVICEID + "</deviceId>\n" +
                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                            "        </ser:logOut>\n" +
                            "    </SOAP-ENV:Body>\n" +
                            "</SOAP-ENV:Envelope>";
                    logout(logoutreq);

                }
            });
        } catch (Exception ex) {

            Timber.tag("Home-onCreate-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initializeControls() {
        pd = new ProgressDialog(context);
        logout = findViewById(R.id.logout);
        issue = findViewById(R.id.issue);
        inspection = findViewById(R.id.inspection);
        aadhar = findViewById(R.id.aadhaar_services);
        receive = findViewById(R.id.receive_goods);
        reports = findViewById(R.id.reports);
        others = findViewById(R.id.others);
        toolbarActivity.setText(context.getResources().getString(R.string.HOME));
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
                if (i == 1) {
                    finish();
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    public void Dismiss() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    public void Show(String title, String msg) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);


        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
    }


}
