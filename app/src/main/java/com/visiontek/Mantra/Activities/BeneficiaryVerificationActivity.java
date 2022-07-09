package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.visiontek.Mantra.Models.AadhaarServicesModel.BeneficiaryVerification.GetURLDetails.BeneficiaryDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Util;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import timber.log.Timber;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.encrypt;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Veroeff.validateVerhoeff;

public class BeneficiaryVerificationActivity extends BaseActivity {
    String BEN_ID;
    Button back, details;
    Context context;
    RadioButton radiorc, radioaadhaar;
    EditText id;
    int select;
    TextView cardno;
    ProgressDialog pd = null;
    String DisplayUID;


    private void Benverify() {
        try {

            String ben ;
            if (select == 2) {
                if (validateVerhoeff(BEN_ID) && BEN_ID.length() == 12) {
                    try {
                        BEN_ID = encrypt(BEN_ID, menuConstants.skey);
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
                    ben = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SOAP-ENV:Envelope\n" +
                            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                            "    <SOAP-ENV:Body>\n" +
                            "        <ns1:beneficiaryVerificationDetails>\n" +
                            "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                            "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                            "            <id>" + BEN_ID + "</id>\n" +
                            "            <idType>U</idType>\n" +
                            "            <fpsID>" + dealerConstants.stateBean.statefpsId + "</fpsID>\n" +
                            "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                            "            <hts></hts>\n" +
                            "        </ns1:beneficiaryVerificationDetails>\n" +
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
                            context.getResources().getString(R.string.Beneficiary_Verification) + BEN_ID,
                            context.getResources().getString(R.string.Invalid_UID),
                            context.getResources().getString(R.string.Please_Enter_Valid_Number),
                            0);
                    id.setText("");
                    return;
                }
            } else {

                ben = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope\n" +
                        "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:beneficiaryVerificationDetails>\n" +
                        "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                        "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                        "            <id>" + BEN_ID + "</id>\n" +
                        "            <idType>R</idType>\n" +
                        "            <fpsID>" + dealerConstants.stateBean.statefpsId + "</fpsID>\n" +
                        "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                        "            <hts></hts>\n" +
                        "        </ns1:beneficiaryVerificationDetails>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>";
            }
            if (networkConnected(context)) {
                if (Debug) {
                    Util.generateNoteOnSD(context, "BenVerificationReq.txt", ben);
                }
                benVerification(ben);
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.Beneficiary_Verification),
                        context.getResources().getString(R.string.Internet_Connection),
                        context.getResources().getString(R.string.Internet_Connection_Msg), 0);
            }
        } catch (Exception ex) {

            Timber.tag("BeneficiaryV-onCreate-").e(ex.getMessage(), "");
        }
    }

    private void benVerification(String ben) {
        try {

            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = MediaPlayer.create(context, R.raw.c100075);
                mp.start();
            }
            Show(context.getResources().getString(R.string.Beneficiary_Details), context.getResources().getString(R.string.Fetching_Members));
            Aadhaar_Parsing request = new Aadhaar_Parsing(context, ben, 3);
            request.setOnResultListener((Aadhaar_Parsing.OnResultListener) (code, msg, ref, flow, object) -> {
                Dismiss();
                if (code == null || code.isEmpty()) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.Beneficiary_Verification),
                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                            "",
                            0);
                    id.setText("");
                    return;
                }

                if (!code.equals("00")) {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Beneficiary_Verification) + DisplayUID,
                            context.getResources().getString(R.string.ResponseCode) + code,
                            context.getResources().getString(R.string.ResponseMsg) + msg,
                            0);
                } else {
                    BeneficiaryDetails beneficiaryDetails = (BeneficiaryDetails) object;
                    Intent ben1 = new Intent(context, BeneficiaryDetailsActivity.class);
                    ben1.putExtra("OBJ", (Serializable) beneficiaryDetails);
                    startActivity(ben1);
                }
                id.setText("");
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("BeneficiaryV-AuthResp-").e(ex.getMessage(), "");
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
                        if (mp != null) {
                            releaseMediaPlayer(context, mp);
                        }
                        if (L.equals("hi")) {
                            mp = MediaPlayer.create(context, R.raw.c200043);
                        } else {
                            mp = MediaPlayer.create(context, R.raw.c100043);
                        }
                        mp.start();
                        select = 1;
                        radiorc.setTypeface(null, Typeface.BOLD_ITALIC);
                        radioaadhaar.setTypeface(null, Typeface.NORMAL);
                        id.setText("");
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

            Timber.tag("StartActivity-select-").e(ex.getMessage(), "");
        }
    }


    @Override
    public void initialize() {
        try {
            context = BeneficiaryVerificationActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_beneficiary__verification, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


            details.setOnClickListener(view -> {
                preventTwoClick(view);
                BEN_ID = id.getText().toString().trim();
                if (BEN_ID.length() > 0) {
                    DisplayUID = BEN_ID;
                    Benverify();
                } else {
                    if (select == 2) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Beneficiary_Verification),
                                context.getResources().getString(R.string.Invalid_UID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID)
                                , 0);

                    } else {
                        show_AlertDialog(context.getResources().getString(R.string.Beneficiary_Verification),
                                context.getResources().getString(R.string.Invalid_ID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_RC),
                                0);

                    }
                }
            });
            back.setOnClickListener(view -> {
                preventTwoClick(view);
                finish();
            });
        } catch (Exception ex) {
            Timber.tag("BeneficiaryV-onCreate-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initializeControls() {
        pd = new ProgressDialog(context);
        details = findViewById(R.id.button_ok);
        back = findViewById(R.id.button_back);
        id = findViewById(R.id.id);
        cardno = findViewById(R.id.cardno);
        if (dealerConstants.fpsURLInfo.virtualKeyPadType.equals("A")) {
            id.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            id.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(Integer.parseInt(dealerConstants.fpsURLInfo.cardEntryLength));
        id.setFilters(FilterArray);
        toolbarActivity.setText(context.getResources().getString(R.string.BENEFICIARY));
        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
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
        confirm.setOnClickListener(v -> dialog.dismiss());
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
}
