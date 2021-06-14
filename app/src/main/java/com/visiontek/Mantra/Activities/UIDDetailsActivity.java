package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.CustomAdapter4;
import com.visiontek.Mantra.Models.AadhaarServicesModel.UIDSeeding.GetURLDetails.UIDAuth;
import com.visiontek.Mantra.Models.AadhaarServicesModel.UIDSeeding.GetURLDetails.UIDDetails;
import com.visiontek.Mantra.Models.AadhaarServicesModel.UIDSeeding.GetUserDetails.UIDModel;
import com.visiontek.Mantra.Models.DATAModels.DataModel5;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Aadhaar_Parsing;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.menuConstants;
import static com.visiontek.Mantra.Utils.Util.ConsentForm;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.encrypt;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Util.toast;
import static com.visiontek.Mantra.Utils.Veroeff.validateVerhoeff;

public class UIDDetailsActivity extends AppCompatActivity {

    Button back, Ekyc;
    Context context;
    RecyclerView.Adapter adapter;
    ProgressDialog pd = null;
    TextView UID_details_cardnum;
    CheckBox checkBox;
    UIDDetails uidDetails;
    UIDModel uidModel=new UIDModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_u_i_d__details);

        context = UIDDetailsActivity.this;

        uidDetails = (UIDDetails) getIntent().getSerializableExtra("OBJ");
        TextView rd = findViewById(R.id.rd);
        boolean  rd_fps;
        rd_fps = RDservice(context);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();
        if (rd_fps) {
            rd.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg),context.getResources().getString(R.string.RD_Service));
            rd.setTextColor(context.getResources().getColor(R.color.black));
        }
        pd = new ProgressDialog(context);



        UID_details_cardnum = findViewById(R.id.UID_details_cardnum);
        UID_details_cardnum.setText(uidDetails.rationCardId);
        Ekyc = findViewById(R.id.UID_details_Ekyc);
        back = findViewById(R.id.UID_details_back);

        checkBox=findViewById(R.id.check);

        Ekyc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uidModel.click) {
                    if (Util.networkConnected(context)) {
                        if (checkBox.isChecked()) {
                            if (uidModel.bfd_1.equals("Y")) {
                                ConsentDialog(ConsentForm(context));
                            } else {
                                show_error_box(context.getResources().getString(R.string.Member_is_already_Verified), context.getResources().getString(R.string.Already_Verified));
                            }
                        } else {
                            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                            currentDateTimeString = "23032021163452";
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
                                   /* "   \"token\" : " + "\"" + fpsURLInfo.token() + "\"" + ",\n" +*/
                                    "   \"token\" : "+"\"9f943748d8c1ff6ded5145c59d0b2ae7\""+"\n" +
                                    "}";
                            Util.generateNoteOnSD(context, "ConsentFormReq.txt", consentrequest);
                            ConsentformURL(consentrequest);
                        }
                    }else {
                        show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection));
                    }


                } else {
                    if (mp != null) {
                        releaseMediaPlayer(context, mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = mp.create(context, R.raw.c100065);
                        mp.start();
                        toast(context, context.getResources().getString(R.string.Please_Select_a_Member));
                    }
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ArrayList<DataModel5> data = new ArrayList<>();
        int rcMemberDetssize=uidDetails.rcMemberDet.size();
        for (int i = 0; i < rcMemberDetssize; i++) {
            data.add(new DataModel5(uidDetails.rcMemberDet.get(i).memberName,
                    uidDetails.rcMemberDet.get(i).uid,
                    uidDetails.rcMemberDet.get(i).w_uid_status));
        }

        adapter = new CustomAdapter4(context, data, new DealerDetailsActivity.OnClickListener() {
            @Override
            public void onClick_d(int p) {
                uidModel.click = true;
                uidModel.memberId = uidDetails.rcMemberDet.get(p).memberId;
                uidModel.bfd_1 = uidDetails.rcMemberDet.get(p).bfd_1;
                uidModel.memberName = uidDetails.rcMemberDet.get(p).memberName;
                uidModel.w_uid_status = uidDetails.rcMemberDet.get(p).w_uid_status;
                uidModel.member_fusion = uidDetails.rcMemberDet.get(p).member_fusion;
                uidModel.uid = uidDetails.rcMemberDet.get(p).uid;

            }
        }, 1);
        recyclerView.setAdapter(adapter);
    }

    private void ConsentDialog(String concent) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.consent);
        Button confirm = (Button) dialog.findViewById(R.id.agree);
        Button back = (Button) dialog.findViewById(R.id.cancel);
        TextView tv=(TextView)dialog.findViewById(R.id.consent);
        tv.setText(concent);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                AadhaarDialog();

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
        pd = ProgressDialog.show(context, context.getResources().getString(R.string.UID_DETAILS), context.getResources().getString(R.string.Consent_Form), true, false);
        Json_Parsing request = new Json_Parsing(context, consentrequest, 3);
        request.setOnResultListener(new Json_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (!code.equals("00")) {
                    show_error_box(msg,  code);
                } else {
                    show_error_box(msg,  code);
                }
            }

        });

    }

    private void AadhaarDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        alert.setMessage(context.getResources().getString(R.string.Please_Enter_Member_UID_Number));
        alert.setTitle(context.getResources().getString(R.string.UID_Number));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(12);
        edittext.setFilters(FilterArray);
        alert.setView(edittext);
        alert.setPositiveButton(context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                uidModel.Enter_UID = edittext.getText().toString();
                if (uidModel.Enter_UID.length() == 12 && validateVerhoeff(uidModel.Enter_UID)) {
                    try {
                        uidModel.UID_Details_Aadhaar = encrypt(uidModel.Enter_UID,menuConstants.skey);
                        connectRDserviceEKYC(uidDetails.zwadh);
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
                    if (mp!=null) {
                        releaseMediaPlayer(context,mp);
                    }
                    if (L.equals("hi")) {
                    } else {
                        mp = mp.create(context, R.raw.c100047);
                        mp.start();
                        toast(context, context.getResources().getString(R.string.Invalid_Aadhaar));
                    }
                }
            }
        });
        alert.setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();

    }

    private void hitURL1(String uidauth) {

        pd = ProgressDialog.show(context, context.getResources().getString(R.string.Members), context.getResources().getString(R.string.Fetching_Members), true, false);
        Aadhaar_Parsing request = new Aadhaar_Parsing(context, uidauth, 2);
        request.setOnResultListener(new Aadhaar_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String error, String msg, String ref, String flow, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (!error.equals("E00")) {
                    System.out.println("ERRORRRRRRRRRRRRRRRRRRRR");
                    show_error_box(msg, context.getResources().getString(R.string.Member_Details) + error);
                } else {
                    UIDAuth uidAuth= (UIDAuth) object;
                    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    String details = "\n"+context.getResources().getString(R.string.MemberName) +uidAuth.eKYCMemberName + "\n" +
                            context.getResources().getString(R.string.DOB) + uidAuth.eKYCDOB + "\n" +
                            context.getResources().getString(R.string.PindCode) +uidAuth.eKYCPindCode + "\n" +
                            context.getResources().getString(R.string.Gender) + uidAuth.eKYCGeneder + "\n" +
                            context.getResources().getString(R.string.Date) + currentDateTimeString + "\n";
                    show_error_box(msg + details, context.getResources().getString(R.string.UID_Seeding) + error);

                }
            }



        });
        request.execute();
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

            uidModel.fCount = "1";
            String pidXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<PidOptions ver =\"1.0\">\n" +
                    "    <Opts env=\"P\" fCount=\"" + uidModel.fCount + "\" iCount=\"" + uidModel.iCount + "\" iType=\"" + uidModel.iType + "\" fType=\"" + uidModel.fType + "\" pCount=\"0\" pType=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" otp=\"\" wadh=\"" + wadhvalue + "\" posh=\"UNKNOWN\"/>\n" +
                    "</PidOptions>";

            Intent act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            final boolean isIntentSafe = activities.size() > 0;
            System.out.println("Boolean check for activities = " + isIntentSafe);
            if (!isIntentSafe) {
                Toast.makeText(getApplicationContext(), context.getResources().getString(R.string.No_RD_Service_Available), Toast.LENGTH_SHORT).show();
            }
            System.out.println("No of activities = " + activities.size());
            act.putExtra("PID_OPTIONS", pidXML);
            startActivityForResult(act, uidModel.RD_SERVICE);
        } catch (Exception e) {
            System.out.println("Error while connecting to RDService");
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("OnActivityResult");
        if (requestCode == uidModel.RD_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println(data.getStringExtra("PID_DATA"));
                String piddata = data.getStringExtra("PID_DATA");
                int code = createAuthXMLRegistered(piddata);
                if (piddata != null && piddata.contains("errCode=\"0\"") && code == 0) {
                    System.out.println("PID DATA = " + piddata);
                    prep_Mlogin();
                } else {
                    show_error_box(uidModel.rdModel.errinfo, context.getResources().getString(R.string.PID_Exception));
                    System.out.println("ERROR PID DATA = " + piddata);
                }
            }
        }
    }

    private void prep_Mlogin() {

        String UIDAuth = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns1:getEKYCAuthenticateRD>\n" +
                "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                "            <Shop_no>" + dealerConstants.stateBean.statefpsId + "</Shop_no>\n" +
                "            <terminal_id>" + DEVICEID + "</terminal_id>\n" +
                "            <existingRCNumber>" + uidDetails.rationCardId + "</existingRCNumber>\n" +
                "            <rcMemberName>" + uidModel.memberName + "</rcMemberName>\n" +
                "            <rcUid>" + uidModel.uid + "</rcUid>\n" +
                "            <memberId>" + uidModel.memberId + "</memberId>\n" +
                "            <ekycresAuth>\n" +
                "                <dc>" + uidModel.rdModel.dc + "</dc>\n" +
                "                <dpId>" + uidModel.rdModel.dpId + "</dpId>\n" +
                "                <mc>" + uidModel.rdModel.mc + "</mc>\n" +
                "                <mid>" + uidModel.rdModel.mi + "</mid>\n" +
                "                <rdId>" + uidModel.rdModel.rdsId + "</rdId>\n" +
                "                <rdVer>" +uidModel.rdModel. rdsVer + "</rdVer>\n" +
                "                <res_Consent_POIandPOA>Y</res_Consent_POIandPOA>\n" +
                "                <res_Consent_mobileOREmail>Y</res_Consent_mobileOREmail>\n" +
                "                <res_certificateIdentifier>" +uidModel.rdModel. ci + "</res_certificateIdentifier>\n" +
                "                <res_encHmac>" + uidModel.rdModel.hmac + "</res_encHmac>\n" +
                "                <res_secure_pid>" + uidModel.rdModel.pid + "</res_secure_pid>\n" +
                "                <res_sessionKey>" + uidModel.rdModel.skey + "</res_sessionKey>\n" +
                "                <res_uid>" + uidModel.UID_Details_Aadhaar + "</res_uid>\n" +
                "            </ekycresAuth>\n" +
                "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                "            <eKYCType>eKYCS</eKYCType>\n" +
                "            <Resp>\n" +
                "                <errCode>0</errCode>\n" +
                "                <errInfo>y</errInfo>\n" +
                "                <nmPoints>" +uidModel.rdModel. nmpoint + "</nmPoints>\n" +
                "                <fCount>" + uidModel.rdModel.fcount + "</fCount>\n" +
                "                <fType>" + uidModel.rdModel.ftype + "</fType>\n" +
                "                <iCount>" + uidModel.rdModel.icount + "</iCount>\n" +
                "                <iType>" +uidModel.rdModel. itype + "</iType>\n" +
                "                <pCount>0</pCount>\n" +
                "                <pType>0</pType>\n" +
                "                <qScore>0</qScore>\n" +
                "            </Resp>\n" +
                "        </ns1:getEKYCAuthenticateRD>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";

        Util.generateNoteOnSD(context, "UIDAuthReq.txt", UIDAuth);
        if (networkConnected(context)) {
            if (mp!=null) {
                releaseMediaPlayer(context,mp);
            }
            if (L.equals("hi")) {
            } else {
                mp = mp.create(context, R.raw.c100187);
                mp.start();
            }
            hitURL1(UIDAuth);
        } else {
            show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection));
        }
    }

    private void show_error_box(String msg, String title) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    public int createAuthXMLRegistered(String piddataxml) {

        try {
            InputStream is = new ByteArrayInputStream(piddataxml.getBytes());
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setIgnoringComments(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(is);

            uidModel.err_code = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errCode").getTextContent();
            if (uidModel.err_code.equals("1")) {
                uidModel.rdModel.errinfo = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errInfo").getTextContent();
                return 1;
            } else {


                uidModel.rdModel. icount = "0";
                uidModel.rdModel. itype = "0";
                uidModel.rdModel.fcount = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fCount").getTextContent();
                uidModel.rdModel.ftype = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("fType").getTextContent();
                uidModel.rdModel.nmpoint = doc.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("nmPoints").getTextContent();
                uidModel.rdModel.pid = doc.getElementsByTagName("Data").item(0).getTextContent();
                uidModel.rdModel.skey = doc.getElementsByTagName("Skey").item(0).getTextContent();
                uidModel.rdModel.ci = doc.getElementsByTagName("Skey").item(0).getAttributes().getNamedItem("ci").getTextContent();
                uidModel.rdModel.hmac = doc.getElementsByTagName("Hmac").item(0).getTextContent();
                uidModel.rdModel.type = doc.getElementsByTagName("Data").item(0).getAttributes().getNamedItem("type").getTextContent();
                uidModel.rdModel.dpId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dpId").getTextContent();
                uidModel.rdModel.rdsId = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsId").getTextContent();
                uidModel.rdModel.rdsVer = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("rdsVer").getTextContent();
                uidModel.rdModel.dc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("dc").getTextContent();
                uidModel.rdModel.mi = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mi").getTextContent();
                uidModel.rdModel.mc = doc.getElementsByTagName("DeviceInfo").item(0).getAttributes().getNamedItem("mc").getTextContent();
                uidModel.rdModel.skey = uidModel.rdModel.skey.replaceAll(" ", "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("createAuthXMLRegistered error= " + e);
            uidModel.rdModel.errinfo = String.valueOf(e);
            return 2;
        }
        return 0;
    }


}
