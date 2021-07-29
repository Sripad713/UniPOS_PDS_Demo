package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.Models.IssueModel.LastReceipt;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.TaskPrint;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.encrypt;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Veroeff.validateVerhoeff;


public class CashPDSActivity extends AppCompatActivity implements PrinterCallBack {
    String Cash_ID;
    String ACTION_USB_PERMISSION;
    int select;
    RadioGroup radioGroup;
    Context context;
    EditText id;
    Button home, last, get_details;
    RadioButton radiorc, radioaadhaar;
    ProgressDialog pd = null;
    private CashPDSActivity mActivity;
    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private MTerminal100API mTerminal100API;
    TextView cardno;
    int flagprint;

    private void hitURL_LastRecipt(String lastRecipt) {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = MediaPlayer.create(context, R.raw.c100075);
                mp.start();
            }

            Show(context.getResources().getString(R.string.Processing),
                    context.getResources().getString(R.string.Fetching_Details));

            XML_Parsing request = new XML_Parsing(context, lastRecipt, 9);
            request.setOnResultListener((code, msg, ref, flow, object) -> {
               Dismiss();
                if (code == null || code.isEmpty()) {
                    id.setText("");
                    show_AlertDialog(
                            context.getResources().getString(R.string.Last_Recp)+Cash_ID,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }


                if (!code.equals("00")) {
                    id.setText("");
                    show_AlertDialog(
                            context.getResources().getString(R.string.Last_Recp)+Cash_ID,
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,
                            0);
                } else {
                    LastReceipt lastReceipt = (LastReceipt) object;

                    String app;
                    StringBuilder add = new StringBuilder();
                    int lastReceiptCommsize = lastReceipt.lastReceiptComm.size();
                    for (int i = 0; i < lastReceiptCommsize; i++) {

                        if(L.equals("hi")){
                            app = String.format("%-10s%-10s%-8s%-8s\n",
                                    lastReceipt.lastReceiptComm.get(i).comm_name_ll,
                                    lastReceipt.lastReceiptComm.get(i).carry_over,
                                    lastReceipt.lastReceiptComm.get(i).retail_price,
                                    lastReceipt.lastReceiptComm.get(i).commIndividualAmount);
                        }else {
                            app = String.format("%-10s%-10s%-8s%-8s\n",
                                    lastReceipt.lastReceiptComm.get(i).comm_name,
                                    lastReceipt.lastReceiptComm.get(i).carry_over,
                                    lastReceipt.lastReceiptComm.get(i).retail_price,
                                    lastReceipt.lastReceiptComm.get(i).commIndividualAmount);                            }

                        add.append(app);
                    }
                    String date = lastReceipt.lastReceiptComm.get(0).transaction_time.substring(0, 19);
                    String month = menuConstants.fpsPofflineToken.allocationMonth;
                    String year = menuConstants.fpsPofflineToken.allocationYear;

                    String str1, str2, str3, str4, str5;
                    String[] str = new String[4];
                    if (L.equals("hi")) {
                        str1 = dealerConstants.stateBean.stateReceiptHeaderLl + "\n" +
                                context.getResources().getString(R.string.LAST_RECEIPT) + "\n";
                        image(str1, "header.bmp", 1);
                        str2 =  context.getResources().getString(R.string.FPS_Owner_Name) + " : " + Dealername + "\n"
                                + context.getResources().getString(R.string.FPS_No) + " : " + dealerConstants.fpsCommonInfo.fpsId + "\n"
                                + context.getResources().getString(R.string.Availed_FPS_No) + " : " + lastReceipt.lastReceiptComm.get(0).availedFps + "\n"
                                + context.getResources().getString(R.string.Name_of_Consumer) + " : " + lastReceipt.lastReceiptComm.get(0).member_name_ll + "\n"
                                + context.getResources().getString(R.string.Card_No) + "/"+context.getResources().getString(R.string.sch)+" : " + lastReceipt.lastReceiptComm.get(0).rcId + "/" + lastReceipt.lastReceiptComm.get(0).scheme_desc_ll + "\n"
                                + context.getResources().getString(R.string.TransactionID) + " : " + lastReceipt.lastReceiptComm.get(0).reciept_id + "\n"
                                + context.getResources().getString(R.string.Date) + " : " + date + "\n"
                                + context.getResources().getString(R.string.AllotmentMonth) + " : " + month + "\n"
                                + context.getResources().getString(R.string.AllotmentYear) + " : " + year + "\n\n"
                                + String.format("%-10s%-10s%-10s%-10s\n",
                                context.getResources().getString(R.string.commodity),
                                context.getResources().getString(R.string.lifted),
                                context.getResources().getString(R.string.rate),
                                context.getResources().getString(R.string.price)) + "\n";
                        str3 = (add)
                                + "\n";
                        str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + "    : " + lastReceipt.lastReceiptComm.get(0).tot_amount;
                        image(str2 + str3 + str4, "body.bmp", 0);

                        str5 = context.getResources().getString(R.string.Public_Distribution_Dept) + "\n"
                                + context.getResources().getString(R.string.Note_Qualitys_in_KgsLtrs) + "\n\n";

                        image(str5, "tail.bmp", 1);
                        str[0] = "1";
                        str[1] = "1";
                        str[2] = "1";
                        str[3] = "1";
                        checkandprint(str, 1);
                    } else {
                        str1 = dealerConstants.stateBean.stateReceiptHeaderEn + "\n" +
                                context.getResources().getString(R.string.LAST_RECEIPT) + "\n\n";
                        str2 =
                                context.getResources().getString(R.string.FPS_Owner_Name) + "  :" + Dealername + "\n"
                                        + context.getResources().getString(R.string.FPS_No) + "          :" + dealerConstants.fpsCommonInfo.fpsId + "\n"
                                        + context.getResources().getString(R.string.Availed_FPS_No) + "  : " + lastReceipt.lastReceiptComm.get(0).availedFps + "\n"
                                        + context.getResources().getString(R.string.Name_of_Consumer) + ":" + lastReceipt.lastReceiptComm.get(0).member_name + "\n"
                                        + context.getResources().getString(R.string.Card_No) + "/scheme   :" + lastReceipt.lastReceiptComm.get(0).rcId + "/" + lastReceipt.lastReceiptComm.get(0).scheme_desc_en + "\n"
                                        + context.getResources().getString(R.string.TransactionID) + ":" + lastReceipt.lastReceiptComm.get(0).reciept_id + "\n"
                                        + context.getResources().getString(R.string.Date) + " : " + date + "\n"
                                        + context.getResources().getString(R.string.AllotmentMonth) + " : " + month + "\n"
                                        + context.getResources().getString(R.string.AllotmentYear) + "  : " + year + "\n"
                                        + String.format("%-10s%-8s%-8s%-8s\n",
                                        context.getResources().getString(R.string.commodity),
                                        context.getResources().getString(R.string.lifted),
                                        context.getResources().getString(R.string.rate),
                                        context.getResources().getString(R.string.price)) + "\n";

                        str3 = (add)
                                + "\n";

                        str4 = context.getResources().getString(R.string.TOTAL_AMOUNT) + "    : " + lastReceipt.lastReceiptComm.get(0).tot_amount + "\n"
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
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("CashPDS-LastRcpt-").e(ex.getMessage(), "");
        }
    }


    private void image(String content, String name, int align) {
        try {
            Util.image(content, name, align);
        } catch (Exception ex) {

            Timber.tag("CashPDS-Image-").e(ex.getMessage(), "");
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
                    id.setText("");
                } else {
                    printbox(str,i);
                }
            }
        } catch (Exception ex) {

            Timber.tag("CashPDS-Print-").e(ex.getMessage(), "");
        }
    }

    private void member_details() {
        try {

            String members ;

            if (select == 2) {
                String Cash_Aadhaar = null;
                if (validateVerhoeff(Cash_ID) && Cash_ID.length() == 12) {
                    try {
                        Cash_Aadhaar = encrypt(Cash_ID, menuConstants.skey);
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
                    members = "<SOAP-ENV:Envelope\n" +
                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                            "    <SOAP-ENV:Body>\n" +
                            "        <ns1:getePDSRationCardDetails>\n" +
                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                            "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                            "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                            "            <hts></hts>\n" +
                            "            <id>" + Cash_Aadhaar + "</id>\n" +
                            "            <idType>U</idType>\n" +
                            "            <mode>CA</mode>\n" +
                            "        </ns1:getePDSRationCardDetails>\n" +
                            "    </SOAP-ENV:Body>\n" +
                            "</SOAP-ENV:Envelope>";
                } else {
                    if (mp != null) {
                        releaseMediaPlayer(context, mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = MediaPlayer.create(context, R.raw.c100047);
                        mp.start();
                    }
                    show_AlertDialog(
                            context.getResources().getString(R.string.Member_Details)+Cash_ID,
                            context.getResources().getString(R.string.Invalid_UID),
                            context.getResources().getString(R.string.Please_Enter_Valid_Number),
                            0);
                    id.setText("");
                    return;
                }
            } else {
                members = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                        "<SOAP-ENV:Envelope\n" +
                        "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                        "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:getePDSRationCardDetails>\n" +
                        "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                        "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                        "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                        "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                        "            <hts></hts>\n" +
                        "            <id>" + Cash_ID + "</id>\n" +
                        "            <idType>R</idType>\n" +
                        "            <mode>CL</mode>\n" +
                        "        </ns1:getePDSRationCardDetails>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>";

            }
            if (networkConnected(context)) {
                if (Debug){
                    Util.generateNoteOnSD(context, "MemberDetails.txt", members);
                }
                hitURL(members);
            } else {
                show_AlertDialog(context.getResources().getString(R.string.Cash_PDS),
                        context.getResources().getString(R.string.Internet_Connection),
                        context.getResources().getString(R.string.Internet_Connection_Msg),
                        0);
            }
        } catch (Exception ex) {
            Timber.tag("CashPDS-MemFormat-").e(ex.getMessage(), "");
        }
    }

    private void hitURL(String members) {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
                mp = MediaPlayer.create(context, R.raw.c200183);
            } else {
                mp = MediaPlayer.create(context, R.raw.c100183);
            }
            mp.start();
            Show(context.getResources().getString(R.string.Members),
                    context.getResources().getString(R.string.Fetching_Members)  );

            XML_Parsing request = new XML_Parsing(context, members, 3);
            request.setOnResultListener((code, msg, ref, flow, object) -> {
                Dismiss();
                if (code == null || code.isEmpty()) {
                    id.setText("");
                    show_AlertDialog(
                            context.getResources().getString(R.string.Member_Details)+Cash_ID,
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    return;
                }

                if (!code.equals("00")) {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Member_Details)+Cash_ID,
                            context.getResources().getString(R.string.ResponseCode)+code,
                            context.getResources().getString(R.string.ResponseMsg)+msg,0);

                } else {
                    Intent members1 = new Intent(getApplicationContext(), MemberDetailsActivity.class);
                    members1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(members1);
                }
                id.setText("");
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("CashPDS-HitURL-").e(ex.getMessage(), "");
        }
    }


    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View v) {
        try {

            radiorc = findViewById(R.id.radio_rc_no);
            radioaadhaar = findViewById(R.id.radio_aadhaar);
            boolean checked = ((RadioButton) v).isChecked();
            if (checked) {
                switch (v.getId()) {
                    case R.id.radio_rc_no:
                        cardno.setText(context.getResources().getString(R.string.RC_No));
                        if (dealerConstants.fpsURLInfo.virtualKeyPadType.equals("A")) {
                            id.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        } else {
                            id.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                        InputFilter[] FilterArray = new InputFilter[1];
                        FilterArray[0] = new InputFilter.LengthFilter(Integer.parseInt(dealerConstants.fpsURLInfo.cardEntryLength));
                        id.setFilters(FilterArray);
                        select = 1;
                        radiorc.setTypeface(null, Typeface.BOLD_ITALIC);
                        radioaadhaar.setTypeface(null, Typeface.NORMAL);
                        id.setText("");
                        if (mp != null) {
                            releaseMediaPlayer(context, mp);
                        }
                        if (L.equals("hi")) {
                            mp = MediaPlayer.create(context, R.raw.c200043);
                        } else {
                            mp = MediaPlayer.create(context, R.raw.c100043);
                        }
                        mp.start();

                        break;

                    case R.id.radio_aadhaar:
                        cardno.setText(context.getResources().getString(R.string.Aadhaar_No));
                        id.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                        InputFilter[] FilterArray1 = new InputFilter[1];
                        FilterArray1[0] = new InputFilter.LengthFilter(12);
                        id.setFilters(FilterArray1);
                        select = 2;
                        radiorc.setTypeface(null, Typeface.NORMAL);
                        radioaadhaar.setTypeface(null, Typeface.BOLD_ITALIC);
                        id.setText("");
                        break;

                }
            }
        } catch (Exception ex) {

            Timber.tag("CashPDS-Select-").e(ex.getMessage(), "");
        }
    }


    private void lastReceipt_frame() {
        try {

            String lastRecipt;

            if (select == 2) {
                String Cash_Aadhaar = null;
                if (validateVerhoeff(Cash_ID)) {
                    try {
                        Cash_Aadhaar = encrypt(Cash_ID, menuConstants.skey);
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

                    lastRecipt = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                            "<SOAP-ENV:Envelope\n" +
                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                            "    <SOAP-ENV:Body>\n" +
                            "        <ns1:getReprintDetails>\n" +
                            "            <exiting_ration_card>" + Cash_Aadhaar + "</exiting_ration_card>\n" +
                            "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                            "            <idType>U</idType>\n" +
                            "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                            "        </ns1:getReprintDetails>\n" +
                            "    </SOAP-ENV:Body>\n" +
                            "</SOAP-ENV:Envelope>";
                } else {
                    if (mp != null) {
                        releaseMediaPlayer(context, mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = MediaPlayer.create(context, R.raw.c100047);
                        mp.start();
                    }
                    show_AlertDialog(
                            context.getResources().getString(R.string.Last_Recp)+Cash_ID,
                            context.getResources().getString(R.string.Invalid_UID),
                            context.getResources().getString(R.string.Please_Enter_Valid_Number),
                            0);

                    id.setText("");
                    return;
                }
            } else {
                lastRecipt = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                        "<SOAP-ENV:Envelope\n" +
                        "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                        "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:getReprintDetails>\n" +
                        "            <exiting_ration_card>" + Cash_ID + "</exiting_ration_card>\n" +
                        "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                        "            <idType>R</idType>\n" +
                        "            <token>" + dealerConstants.fpsURLInfo.token + "</token>\n" +
                        "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                        "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                        "        </ns1:getReprintDetails>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>";

            }
            if (networkConnected(context)) {
                if (Debug) {
                    Util.generateNoteOnSD(context, "LastReceiptReq.txt", lastRecipt);
                }
                hitURL_LastRecipt(lastRecipt);
            } else {
                show_AlertDialog(context.getResources().getString(R.string.Last_Reciept),
                        context.getResources().getString(R.string.Internet_Connection),
                        context.getResources().getString(R.string.Internet_Connection_Msg),
                        0);
            }
        } catch (Exception ex) {

            Timber.tag("CashPDS-ReceiptFrmt-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void OnOpen() {
        last.setEnabled(true);
    }

    @Override
    public void OnOpenFailed() {
        last.setEnabled(false);
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

        last.setEnabled(false);
        if (mUsbReceiver != null) {
            context.unregisterReceiver(mUsbReceiver);
        }
        probe();
    }

    @Override
    public void OnPrint(final int bPrintResult, final boolean bIsOpened) {
        mActivity.runOnUiThread(() -> mActivity.last.setEnabled(bIsOpened));
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {

                    probe();
                    last.setEnabled(true);
                    synchronized (this) {
                    }
                }
            } catch (Exception ex) {

                Timber.tag("CashPDS-Broadcast-").e(ex.getMessage(), "");
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
                        // TODO Auto-generated method stub


                        PendingIntent mPermissionIntent = PendingIntent
                                .getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        if (!mUsbManager.hasPermission(device)) {
                            mUsbManager.requestPermission(device, mPermissionIntent);
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(ACTION_USB_PERMISSION);
                            context.registerReceiver(mUsbReceiver, filter);
                        } else {
                            last.setEnabled(false);
                            es.submit(() -> mTerminal100API.printerOpenTask(mUsbManager, device, context));
                        }
                    }
                }
            }
        } catch (Exception ex) {

            Timber.tag("CashPDS-Probe-").e(ex.getMessage(), "");
        }
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
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                } else {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
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
            toolbarActivity.setText( context.getResources().getString(R.string.CASH_PDS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {

            Timber.tag("CashPDS-ToolBar-").e(ex.getMessage(), "");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash__p_d_s);

        try {
            context = CashPDSActivity.this;

            mActivity = this;
            ACTION_USB_PERMISSION = mActivity.getApplicationInfo().packageName;


            flagprint=0;
            initilisation();

            if (diableMenu("getePDSRationCardDetails")) {
                get_details.setEnabled(false);
            }

            get_details.setOnClickListener(view -> {
                preventTwoClick(view);
                Cash_ID = id.getText().toString().trim();
                if (Cash_ID.length() > 0) {
                    memberConstants=null;
                    member_details();
                } else {
                    if (select == 2) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Aadhaar)+Cash_ID,
                                context.getResources().getString(R.string.Invalid_UID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID)
                                ,0);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.RC_No)+Cash_ID,
                                context.getResources().getString(R.string.Invalid_ID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_RC),
                                0);

                    }
                }
            });
            home.setOnClickListener(view -> {
                preventTwoClick(view);
                Intent intent=new Intent(context,HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
            last.setOnClickListener(view -> {
                preventTwoClick(view);
                Cash_ID = id.getText().toString().trim();
                if (Cash_ID.length() == 12) {
                    lastReceipt_frame();
                } else {
                    if (select == 2) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Aadhaar)+Cash_ID,
                                context.getResources().getString(R.string.Invalid_UID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID)
                                ,0);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.RC_No)+Cash_ID,
                                context.getResources().getString(R.string.Invalid_ID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_RC),
                                0);

                    }
                }
            });

            mTerminal100API = new MTerminal100API();
            mTerminal100API.initPrinterAPI(this, this);
            last.setEnabled(false);
            probe();
        } catch (Exception ex) {
            Timber.tag("CashPDS-onCreate-").e(ex.getMessage(), "");
        }
    }


    private void initilisation() {


        pd = new ProgressDialog(context);
        id = findViewById(R.id.id);
        id.setText("");
        home = findViewById(R.id.cash_pds_home);
        last = findViewById(R.id.cash_pds_lastreciept);
        get_details = findViewById(R.id.cash_pds_getdetails);
        radioGroup = findViewById(R.id.groupradio);
        cardno = findViewById(R.id.cardno);
        if (dealerConstants.fpsURLInfo.virtualKeyPadType.equals("A")) {
            id.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            id.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(Integer.parseInt(dealerConstants.fpsURLInfo.cardEntryLength));
        id.setFilters(FilterArray);
        toolbarInitilisation();


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

