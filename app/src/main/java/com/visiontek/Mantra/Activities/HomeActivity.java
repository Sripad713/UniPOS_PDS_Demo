package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.Models.InspectionModel.InspectionDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;

import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Util.toast;

public class HomeActivity extends AppCompatActivity {
    String token = "9f943748d8c1ff6ded5145c59d0b2ae7";
    String mode = "PDS";
    Button issue, inspection, aadhar, receive, reports, others,logout;
    Intent i;
    Context context;
    TextView rd;
    ProgressDialog pd = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = HomeActivity.this;

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg),
                    context.getResources().getString(R.string.RD_Service),0);
            return;
        }

        initilisation();
        if (diableMenu(context, 10)) {
            inspection.setVisibility(View.INVISIBLE);
            inspection.setEnabled(false);
        }

        if (diableMenu(context, 3)) {
            aadhar.setVisibility(View.INVISIBLE);
            aadhar.setEnabled(false);
        }
        if (diableMenu(context, 11)) {
            receive.setVisibility(View.INVISIBLE);
            receive.setEnabled(false);
        }

        issue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(context, IssueActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivityForResult(i, 1);
            }
        });
        inspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (networkConnected(context)){
                   FrameXMLforInspection();
               }else {

                   show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection), 0);
               }

            }
        });
        aadhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(context, AadhaarServicesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(i, 1);
            }
        });
        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(networkConnected(context))
                {
                    FrameJsonforReceiveGoods();
                }
                else {

                    show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection), 0);
                }


            }
        });
        reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(context, ReportsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(i, 1);
            }
        });
        others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(context, context.getResources().getString(R.string.Not_Enabled));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              String logoutreq="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<SOAP-ENV:Envelope\n" +
                      "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                      "    xmlns:ser=\"http://service.fetch.rationcard/\">\n" +
                      "    <SOAP-ENV:Body>\n" +
                      "        <ser:logOut>\n" +
                      "            <fpsId>"+dealerConstants.stateBean.statefpsId+"</fpsId>\n" +
                      "            <fpsSessionId>"+dealerConstants.fpsCommonInfo.fpsSessionId+"</fpsSessionId>\n" +
                      "            <password>"+dealerConstants.fpsURLInfo.token +"</password>\n" +
                      "            <deviceId>"+DEVICEID+"</deviceId>\n" +
                      "            <stateCode>"+dealerConstants.stateBean.stateCode+"</stateCode>\n" +
                      "        </ser:logOut>\n" +
                      "    </SOAP-ENV:Body>\n" +
                      "</SOAP-ENV:Envelope>";
                logout(logoutreq);

            }
        });
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        logout = findViewById(R.id.logout);
        issue = findViewById(R.id.issue);
        inspection = findViewById(R.id.inspection);
        aadhar = findViewById(R.id.aadhaar_services);
        receive = findViewById(R.id.receive_goods);
        reports = findViewById(R.id.reports);
        others = findViewById(R.id.others);
        toolbarInitilisation();
    }

    private void logout(String logoutrequest) {
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Dealer), context.getResources().getString(R.string.Authenticating), true, false);
        Aadhaar_Parsing request = new Aadhaar_Parsing(context, logoutrequest, 10);
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
                    show_error_box(msg, context.getResources().getString(R.string.Inspection_Details)+ error, 0);
                } else {
                    show_error_box(msg,"Response Code :"+  error,1);
                }
            }

        });
        request.execute();
    }
    private void FrameXMLforInspection() {
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
                "            <password>" +dealerConstants. fpsURLInfo.token + "</password>\n" +
                "        </ser:getInspectorDetails>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        if(networkConnected(context))
        {
            Util.generateNoteOnSD(context, "InspectionDetailsReq.txt", inspection);
            hit_Inspection(inspection);
        }else {

            show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection), 0);
        }
    }

    private void FrameJsonforReceiveGoods() {
        if (mp!=null) {
            releaseMediaPlayer(context,mp);
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
        Util.generateNoteOnSD(context, "ReceiveGoodsReq.txt", receiveGoods);
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Receive_Goods), context.getResources().getString(R.string.Processing), true, false);
        Json_Parsing request = new Json_Parsing(context, receiveGoods, 1);
        request.setOnResultListener(new Json_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (code == null || code.isEmpty()){
                    show_error_box("Invalid Out put from Server","No Response",0);
                    return;
                }
                if (!code.equals("00")) {
                    show_error_box(msg, context.getResources().getString(R.string.Commodities_Error) + code, 0);
                } else {
                    ReceiveGoodsDetails receiveGoodsDetails= (ReceiveGoodsDetails) object;
                    i = new Intent(context, ReceiveGoodsActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("OBJ", (Serializable) receiveGoodsDetails);
                    startActivityForResult(i, 1);
                }
            }

        });

    }

    private void hit_Inspection(String inspection) {
        if (mp!=null) {
            releaseMediaPlayer(context,mp);
        }
        if (L.equals("hi")) {
        } else {
            mp = mp.create(context, R.raw.c100075);
            mp.start();
        }
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Members), context.getResources().getString(R.string.Fetching_Members), true, false);
        Aadhaar_Parsing request = new Aadhaar_Parsing(context, inspection,5 );
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
                    show_error_box(msg, context.getResources().getString(R.string.Inspection_Details)+ error, 0);
                } else {
                    InspectionDetails inspectionDetails= (InspectionDetails) object;
                    Intent in = new Intent(context, InspectionActivity.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.putExtra("OBJ", (Serializable) inspectionDetails);
                    startActivity(in);
                }
            }

        });
        request.execute();
    }

    private void show_error_box(String msg, String title, final int i) {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (i==1){
                            finish();
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
        toolbarVersion.setText("V" + appversion);


        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("HOME");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}
