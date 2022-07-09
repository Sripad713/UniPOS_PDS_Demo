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
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Adapters.DealerListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.DealerListModel;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails.DealerModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import org.json.JSONException;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.ConsentForm;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class DealerAuthenticationActivity extends AppCompatActivity implements PrinterCallBack {
    public String ACTION_USB_PERMISSION;
    private DealerAuthenticationActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    DealerModel dealerModel = new DealerModel();
    Button scanfp, back;
    ProgressDialog pd = null;
    Context context;
    String MEMBER_AUTH_TYPE;
    String refno;
    int flagprint;
    ReceiveGoodsModel receiveGoodsModel;
    DatabaseHelper databaseHelper;

    public interface OnClickDealerAUTH {
        void onClick(int p);
    }

    private void ConsentformURL(String consentrequest) {
        try {
            Show(context.getResources().getString(R.string.Dealer),
                    context.getResources().getString(R.string.Consent_Form));

            Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
            request.setOnResultListener((code, msg, object) -> {
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
            });
        } catch (Exception ex) {
            Timber.tag("RC_DealerAuth-CnsntRsp-").e(ex.getMessage(), "");
        }

    }

    private void hitURL1(String dealerlogin) {
        try {
            Show(context.getResources().getString(R.string.Dealer), context.getResources().getString(R.string.Authenticating));
            XML_Parsing request = new XML_Parsing(DealerAuthenticationActivity.this, dealerlogin, 15);
            request.setOnResultListener((code, msg, ref, flow, object) -> {
                System.out.println("FMR+FIR===");
                System.out.println("MASGG ===="+msg);
                System.out.println("CODEE ===="+code);

                Dismiss();

                if (code == null || code.isEmpty()) {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Dealer),
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }

                if (!code.equals("00")) {
                    dealerModel.fusionflag = 0;
                    if (code.equals("300")) {
                        if (flow.equals("F")) {
                            if (dealerModel.fCount.equals("1")) {
                                dealerModel.fCount = "2";
                                dealerModel.fusionflag = 1;
                                show_AlertDialog(
                                        context.getResources().getString(R.string.Dealer),
                                        context.getResources().getString(R.string.Dealer_FP_Authentication) + code,
                                        context.getResources().getString(R.string.ResponseMsg) + msg,
                                        1);

                                return;
                            }
                        }
                    }
                    dealerModel.fCount = "1";
                    show_AlertDialog(
                            context.getResources().getString(R.string.Dealer),
                            context.getResources().getString(R.string.Dealer_FP_Authentication) + code,
                            context.getResources().getString(R.string.ResponseMsg) + msg,
                            0);



                } else {
                    refno = ref;
                    Upload();

                }
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-DAuthRsp-").e(ex.getMessage(), "");
        }
    }

    private void Upload() {
        try {

            String com = addComm();
            if (!com.equals("1")) {
                String stockupdate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope\n" +
                        "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:stockInfoUpdate>\n" +
                        "            <Stock_Entry>\n" +
                        "                <deviceId>" + DEVICEID + "</deviceId>\n" +
                        "                <dispatchId>" + receiveGoodsModel.cid + "</dispatchId>\n" +
                        "                <do_ro_no>" + receiveGoodsModel.orderno + "</do_ro_no>\n" +
                        "                <noOfComm>" + receiveGoodsModel.tcCommDetails.size() + "</noOfComm>\n" +
                        "                <route_off_auth>" + refno + "</route_off_auth>\n" +
                        "                <route_uid>" + dealerModel.DUid + "</route_uid>\n" +
                        "                <shopNo>" + dealerConstants.stateBean.statefpsId + "</shopNo>\n" +
                        com +
                        "                <truckChitNo>" + receiveGoodsModel.chit + "</truckChitNo>\n" +
                        "                <truckNo>" + receiveGoodsModel.truckno + "</truckNo>\n" +
                        "            </Stock_Entry>\n" +
                        "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                        "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                        "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                        "        </ns1:stockInfoUpdate>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>";
                System.out.println(stockupdate);
                System.out.println("STOCK_UPDATE ++++++++++++++++"+stockupdate);
                Util.generateNoteOnSD(context, "StockUploadDetailsReq.txt", stockupdate);
                hitUploading(stockupdate);

            }
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-Upload-").e(ex.getMessage(), "");
        }
    }

    private void hitUploading(String stockupdate) {
        try {

            Show(context.getResources().getString(R.string.Uploading_Stock),
                    context.getResources().getString(R.string.Processing));

            Aadhaar_Parsing request = new Aadhaar_Parsing(DealerAuthenticationActivity.this, stockupdate, 9);
            request.setOnResultListener((code, msg, ref, flow, object) -> {
                System.out.println("STOCK $$$$$$$$$$$$$$"+code);
                System.out.println("STOCK $$$$$$$$$$$$$$"+msg);
                Dismiss();
                if (code == null || code.isEmpty()) {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Uploading_Stock),
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }

                if (!code.equals("00")) {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Uploading_Stock),
                            context.getResources().getString(R.string.ResponseCode) + code,
                            context.getResources().getString(R.string.ResponseCode) + msg,
                            2);

                } else {

                    for (int i = 0; i < receiveGoodsModel.tcCommDetails.size(); i++) {
                        try {
                            parse_OfflineStockReceive(receiveGoodsModel.tcCommDetails.get(i).releasedQuantity, receiveGoodsModel.tcCommDetails.get(i).commCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            show_AlertDialog(
                                    context.getResources().getString(R.string.Receive_Goods),
                                    "DataBase Update Failed","",0
                                    );
                        }
                    }
                    show_AlertDialog(
                            context.getResources().getString(R.string.Uploading_Stock),
                            context.getResources().getString(R.string.ResponseCode) + code,
                            context.getResources().getString(R.string.ResponseCode) + msg,
                            3);

                    //Add received data into database

                }
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-UplodRsp-").e(ex.getMessage(), "");
        }
    }

    //  Offline Recv goods
    public void parse_OfflineStockReceive(String RecvdQty,String CommCode) throws JSONException {
        System.out.println(">>>>>>>>>>In parseOfflineStockReceive");

        try {
            System.out.println("<<<<<<<OFFLINE RECEIVE GOODS>>>>>>");
            databaseHelper.updatePosOB(context,RecvdQty,CommCode);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private String addComm() {
        try {

            StringBuilder add = new StringBuilder();
            String str;
            int size = receiveGoodsModel.tcCommDetails.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    str = "                <stockNewBean>\n" +
                            "                    <commCode>" + receiveGoodsModel.tcCommDetails.get(i).commCode + "</commCode>\n" +
                            "                    <commName>" + receiveGoodsModel.tcCommDetails.get(i).commName + "</commName>\n" +
                            "                    <KRA>" + receiveGoodsModel.tcCommDetails.get(i).allotment + "</KRA>\n" +
                            "                    <receiveQty>" + receiveGoodsModel.tcCommDetails.get(i).enteredvalue + "</receiveQty>\n" +
                            "                    <releasedQty>" + receiveGoodsModel.tcCommDetails.get(i).releasedQuantity + "</releasedQty>\n" +
                            "                    <shemeId>" + receiveGoodsModel.tcCommDetails.get(i).schemeId + "</shemeId>\n" +
                            "                    <allotedMonth>" + receiveGoodsModel.month + "</allotedMonth>\n" +
                            "                    <allotedYear>" + receiveGoodsModel.year + "</allotedYear>\n" +
                            "                </stockNewBean>\n";
                    add.append(str);

                }
                return String.valueOf(add);
            }
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-AddCom-").e(ex.getMessage(), "");
        }
        return "0";


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
                    callScanFP();
                } else {
                    show_AlertDialog(
                            Dealername,
                            context.getResources().getString(R.string.Consent_Form),
                            context.getResources().getString(R.string.Please_check_Consent_Form),
                            4);
                }

            });
            back.setOnClickListener(v -> dialog.dismiss());

            dialog.setCanceledOnTouchOutside(false);
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.show();
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-consent-").e(ex.getMessage(), "");
        }
    }

    private void prep_consent() {
        try {

            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

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
                    /*"   \"token\" : "+"\""+fpsURLInfo.token()+"\""+",\n" +*/
                    "   \"token\" : " + "\"9f943748d8c1ff6ded5145c59d0b2ae7\"" + "\n" +
                    "}";
            if (Debug) {
                Util.generateNoteOnSD(context, "ConsentFormReq.txt", consentrequest);
            }
            ConsentformURL(consentrequest);
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-CnsntFmt-").e(ex.getMessage(), "");
        }
    }

    private void print() {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String time = sdf1.format(new Date()).substring(0, 5);
            String date = sdf1.format(new Date()).substring(6, 16);
            StringBuilder add = new StringBuilder();
            String app;
            int size = receiveGoodsModel.tcCommDetails.size();
            for (int i = 0; i < size; i++) {

                app = String.format("%-10s%-10s%-10s%-10s\n",
                        receiveGoodsModel.tcCommDetails.get(i).commName,
                        receiveGoodsModel.tcCommDetails.get(i).schemeName,
                        receiveGoodsModel.tcCommDetails.get(i).releasedQuantity,
                        receiveGoodsModel.tcCommDetails.get(i).enteredvalue);
                add.append(app);

            }
            String str1, str2, str3, str4, str5;
            String[] str = new String[4];
            if (L.equals("hi")) {

                str1 = dealerConstants.stateBean.stateReceiptHeaderLl + "\n" +
                        context.getResources().getString(R.string.Receive_Goods) + "\n";
                image(str1, "header.bmp", 1);
                str2 = context.getResources().getString(R.string.Date) + " : " + date + "\n" +
                        context.getResources().getString(R.string.Time) + " : " + time + "\n" +
                        context.getResources().getString(R.string.Truck_Chit_No)+ " : " + receiveGoodsModel.chit + "\n" +
                        context.getResources().getString(R.string.RO)+" : " + time + "\n" +
                        context.getResources().getString(R.string.Truck_No)+" : " + receiveGoodsModel.truckno + "\n" +
                        "\n";
                str3 = String.format("%-10s%-8s%-8s%-8s\n",
                        "ItemName",
                        "Sch",
                        "Dispt",
                        "Recv")
                        + "\n";
                str4 = String.valueOf(add);
                image(str2 + str3 + str4, "body.bmp", 0);
                str5 = "\n" + context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                        + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n";
                image(str5, "tail.bmp", 1);
                str[0] = "1";
                str[1] = "1";
                str[2] = "1";
                str[3] = "1";
                checkandprint(str, 1);

            } else {

                str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n" +
                        context.getResources().getString(R.string.Receive_Goods) + "\n\n";
                str2 =  context.getResources().getString(R.string.Date) + "          : " + date + "\n" +
                        context.getResources().getString(R.string.Time) + "          :" + time + "\n" +
                        context.getResources().getString(R.string.Truck_Chit_No) + " : " + receiveGoodsModel.chit + "\n" +
                        context.getResources().getString(R.string.RO) + "            : " + time + "\n" +
                        context.getResources().getString(R.string.Truck_No) + "      : " + receiveGoodsModel.truckno + "\n" +
                        "\n";
                str3 = String.format("%-10s%-8s%-8s%-8s\n",
                        "ItemName",
                        "Sch",
                        "Dispt",
                        "Recv")
                        + "\n";
                str4 = String.valueOf(add);

                str5 = "\n" + context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                        + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n\n\n";
                str[0] = "1";
                str[1] = str1;
                str[2] = str2 + str3 + str4;
                str[3] = str5;
                checkandprint(str, 0);

            }
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-Print-").e(ex.getMessage(), "");
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
                        mp = MediaPlayer.create(context, R.raw.c100191);
                        mp.start();
                    }
                    es.submit(new TaskPrint(mTerminal100API, str, mActivity, context, i));
                    Intent home = new Intent(context, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                    finish();

                } else {

                    printbox(str,i);
                }
            }
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-battry-").e(ex.getMessage(), "");
        }
    }

    private void image(String content, String name, int align) {
        try {
            Util.image(content, name, align);
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-Image-").e(ex.getMessage(), "");
        }

    }

    private void callScanFP() {
        try {

            //if ("F".equals(dealerModel.DAtype)) {
                MEMBER_AUTH_TYPE = "Bio";
                // wadhverify = false;
                connectRDservice();
            //}
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-ScanFP-").e(ex.getMessage(), "");
        }
    }

    private void prep_Dlogin() {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = MediaPlayer.create(context, R.raw.c100187);
                mp.start();
            }
            dealerModel.fType = dealerModel.DaadhaarAuthType;

            String dealerlogin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:ns1=\"http://www.uidai.gov.in/authentication/uid-auth-request/2.0\"\n" +
                    "    xmlns:ns2=\"http://service.fetch.rationcard/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns2:getAuthenticateNICAuaAuthRD2>\n" +
                    "             <aadhaarAuthType>"+dealerModel.DaadhaarAuthType+"</aadhaarAuthType>\n"+
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
                    "            <scannerId>123456</scannerId>\n" +
                    /*"            <authType>FIR</authType>\n"+*/
                    "            <authType>" + MEMBER_AUTH_TYPE + "</authType>\n" +/*" + DAtype + "*/
                    "            <memberId>" + dealerModel.Dtype + "</memberId>\n" +
                    "            <wadhStatus>" + dealerModel.Dwadh + "</wadhStatus>\n" +
                    /*"            <wadhStatus>Y</wadhStatus>\n" +*/
                    "            <Resp>\n" +
                    "                <errCode>0</errCode>\n" +
                    "                <errInfo>y</errInfo>\n" +
                    "                <nmPoints>" + dealerModel.rdModel.nmpoint + "</nmPoints>\n" +
                    "                <fCount>" + dealerModel.rdModel.fcount + "</fCount>\n" +
                    "                <fType>" + dealerModel.rdModel.ftype+ "</fType>\n" +
                    "                <iCount>0</iCount>\n" +
                    "                <iType>0</iType>\n" +
                    "                <pCount>0</pCount>\n" +
                    "                <pType>0</pType>\n" +
                    "                <qScore>0</qScore>\n" +
                    "            </Resp>\n" +
                    "        </ns2:getAuthenticateNICAuaAuthRD2>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>";

            //Util.generateNoteOnSD(context, "RGDealerAuthReq.txt", dealerlogin);
            hitURL1(dealerlogin);
            Timber.d("RC_DealerAuthenticationActivity-Dealerlogin :"+dealerlogin);
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-AuthFmt-").e(ex.getMessage(), "");
        }
    }

    private void connectRDservice() {
        try {


            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
                mp = MediaPlayer.create(context, R.raw.c200032);
            } else {
                mp = MediaPlayer.create(context, R.raw.c100032);
            }
            mp.start();

            //dealerModel.rdModel.ftype = "0"; //******CHANGEfType*****/
            dealerModel.fType = dealerModel.DaadhaarAuthType; //******CHANGEfType*****/

            String xmplpid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<PidOptions ver =\"1.0\">\n" +
                    "    <Opts env=\"P\" fCount=\"" + dealerModel.fCount + "\" iCount=\"" + dealerModel.iCount + "\" iType=\"" + dealerModel.iType + "\" fType=\"" + dealerModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"\" posh=\"UNKNOWN\"/>\n" +
                    "</PidOptions>";
            System.out.println("DealerAuthenticationXMLPD====="+xmplpid);


            Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
            act.putExtra("PID_OPTIONS", xmplpid);
            startActivityForResult(act, dealerModel.RD_SERVICE);
        } catch (Exception ex) {
            Timber.tag("RC_DealerAuth-RD-").e(ex.getMessage(), "");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            System.out.println("OnActivityResult");
            if (requestCode == dealerModel.RD_SERVICE) {
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println(data.getStringExtra("PID_DATA"));
                    String piddata = data.getStringExtra("PID_DATA");
                    int code = createAuthXMLRegistered(piddata);
                    if (piddata != null && piddata.contains("errCode=\"0\"") && code == 0) {
                        System.out.println("PID DATA = " + piddata);
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
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-ResPID-").e(ex.getMessage(), "");
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
            ex.printStackTrace();
            dealerModel.rdModel.errinfo = String.valueOf(ex.getMessage());
            Timber.tag("RC_DealerAuth-onCreate-").e(ex.getMessage(), "");
            return 2;
        }
        return 0;
    }


    @Override
    public void OnOpen() {

    }

    @Override
    public void OnOpenFailed() {

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
        if (mUsbReceiver != null) {
            context.unregisterReceiver(mUsbReceiver);
        }
        probe();
    }

    @Override
    public void OnPrint(final int bPrintResult, final boolean bIsOpened) {

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    probe();
                }
            } catch (Exception ex) {
                Timber.tag("RC_DealerAuth-Bradcst-").e(ex.getMessage(), "");
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
                            es.submit(() -> mTerminal100API.printerOpenTask(mUsbManager, device, context));
                        }
                    }
                }
            }
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-probe-").e(ex.getMessage(), "");
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
                callScanFP();
            } else if (i == 2) {
                Intent home = new Intent(context, HomeActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
                finish();
            } else if (i == 3) {
                flagprint=2;
                print();
            } else if (i == 4) {
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

    public void Show(String msg, String title) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
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

    private void initilisation() {
        pd = new ProgressDialog(context);
        scanfp = findViewById(R.id.dealer_scanFP);
        back = findViewById(R.id.dealer_back);
        databaseHelper = new DatabaseHelper(context);
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
            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);


            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            toolbarActivity.setText(context.getResources().getString(R.string.DEALER_DETAILS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {

            Timber.tag("RC_DealerAuth-Toolbar-").e(ex.getMessage(), "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dealer_authentication);
        try {
            context = DealerAuthenticationActivity.this;
            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;
            receiveGoodsModel = (ReceiveGoodsModel) getIntent().getSerializableExtra("OBJ");
            initilisation();
            flagprint=0;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            scanfp.setOnClickListener(view -> {
                preventTwoClick(view);
                if (flagprint!=2) {
                    if (dealerModel.click) {
                        if (Util.networkConnected(context)) {
                            if (L.equals("hi")){
                                ConsentDialog(ConsentForm(context,1));
                            }else {
                                ConsentDialog(ConsentForm(context,0));
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
                            mp = MediaPlayer.create(context, R.raw.c100176);
                            mp.start();

                        }
                        show_AlertDialog(context.getResources().getString(R.string.Dealer),
                                context.getResources().getString(R.string.Please_Select_Dealer_Name),
                                ""
                                , 0);
                    }
                }
            });
            back.setOnClickListener(view -> {
                preventTwoClick(view);
                finish();
            });
            ArrayList<DealerListModel> data = new ArrayList<>();
            int dealerlistsize = dealerConstants.fpsCommonInfo.fpsDetails.size();
            for (int i = 0; i < dealerlistsize; i++) {
                if (L.equals("hi")) {
                    data.add(new DealerListModel(
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delNamell,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).dealer_type,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delUid));
                } else {
                    data.add(new DealerListModel(
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delName,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).dealer_type,
                            dealerConstants.fpsCommonInfo.fpsDetails.get(i).delUid));
                }
            }
            RecyclerView.Adapter adapter = new DealerListAdapter(context, data, (OnClickDealerAUTH) p -> {
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
                dealerModel.DaadhaarAuthType= dealerConstants.fpsCommonInfo.fpsDetails.get(p).aadhaarAuthType;

               // if ("F".equals(dealerModel.DAtype)) {
                    dealerModel.click = true;
                    if (dealerModel.Dfusion.equals("1")) {
                        dealerModel.fCount = "2";
                    } else {
                        dealerModel.fCount = "1";
                    }
               /* } else {
                    dealerModel.click = false;
                }*/
            }, 1);
            recyclerView.setAdapter(adapter);

            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);

            probe();
        } catch (Exception ex) {
            Timber.tag("RC_DealerAuth-onCreate-").e(ex.getMessage(), "");
        }
    }

}