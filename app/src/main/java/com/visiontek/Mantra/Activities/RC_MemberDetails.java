package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.ConnectivityReceiver;
import com.visiontek.Mantra.Utils.IMEIUtil;
import com.visiontek.Mantra.Utils.PIDUtil;
import com.visiontek.Mantra.Utils.TokenGenerator;
import com.visiontek.Mantra.Utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.MemberName;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.networkConnected;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class RC_MemberDetails extends BaseActivity {
    String certIdentifier, dataType, dc, dpId, hmac, mc, mId, rdId, rdVer, pId, skey;
    private int selectedIndex = -1;
    Boolean paramOK = false;
    public int k = 1;
    TableLayout tableLayout;
    TableRow tableRow;
    Button scanFP,btn_back;
    String token;
    //String impdsurlService = "http://eposservice.cg.gov.in/impdschgrTest/impdsApi/impdsMEService";
    //String impdsBenfAuthentication = "http://eposservice.cg.gov.in/impdschgrTest/impdsApi/impdsBenfAuthentication";
    String bioAuthType;
    int count = 0;



    private TextView txt1, txt2, home_state_name, text_fps_id, home_dist_name, text_scheme_id;

    private String sale_state_code, sale_dist_code, home_dist_code, rc_id,  district_name, state_name, state_code, sale_fps_id,
            sessionId, memberId, memberName, fusionStatus,android_id, id_type,alloc_month,alloc_year,
            delName, delUid, rc_uid, home_fps_id, scheme_id, scheme_name, fps_session_id;
    JSONObject jresponse = null;
    ArrayList<String> m_id_list, m_name_list,m_fusionStatus_list;
    Context context;
    ProgressDialog pd = null;
    String mem_Fusionstatus;
    int impdsFusionflag,impdsfusionflag;
    String fCount = "1";
    String fposh ="UNKNOWN";
    String aadharAuthType;
    int fusion_Count;
    @Override
    public void initialize() {
        try{
            context=RC_MemberDetails.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_r_c__member_details, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


        if (!networkConnected(context)) {
            show_AlertDialog(context.getResources().getString(R.string.Device),
                    context.getResources().getString(R.string.Internet_Connection),
                    context.getResources().getString(R.string.Internet_Connection_Msg),
                    0);
        }

        pd = new ProgressDialog(context);
        rc_id = ImpdsBean.getInstance().getRc_id();
        id_type = ImpdsBean.getInstance().getIdType();

        sale_state_code = ImpdsBean.getInstance().getSaleStateCode();
        sale_fps_id = ImpdsBean.getInstance().getSaleFpsId();
        sale_dist_code = ImpdsBean.getInstance().getSaleDistCode();
        fps_session_id = ImpdsBean.getInstance().getFps_session_id();

        m_id_list = new ArrayList<>();
        m_name_list = new ArrayList<>();
        m_fusionStatus_list = new ArrayList<>();

        android_id = DEVICEID;

        token = TokenGenerator.nextToken();
        setToken(token);

        tableLayout = findViewById(R.id.tablay1);
        tableLayout.setStretchAllColumns(true);

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //impdsFusionflag=0;
                //impdsfusionflag=0;
                Integer index = (Integer) v.getTag();
                System.out.println("INDEX1 ==="+index);
                mem_Fusionstatus  = m_fusionStatus_list.get(index-1);
                System.out.println("NAME ==="+mem_Fusionstatus);
                if (index != null) {
                    selectRow(index);
                    TableRow tablerow = (TableRow) v;
                    TextView sample = (TextView) tablerow.getChildAt(1);
                    String del_uid = sample.getText().toString();
                    System.out.println("del_uid ===="+del_uid);

                    TextView sample1 = (TextView) tablerow.getChildAt(0);
                    String dealer_name = sample1.getText().toString();
                    System.out.println("dealer_name ===="+dealer_name);

                   /* TextView sample2 =(TextView)tablerow.getChildAt(2);
                    String fusion_status =sample2.getText().toString();
                    System.out.println("fusion_status ===="+fusion_status);*/

                    /* int msg =m_fusionStatus_list.indexOf(fusionStatus);
                    String fusionflag = String.valueOf(msg);
                    System.out.println("MSG ====="+fusionflag);*/

                    rc_uid = del_uid;
                    memberName = dealer_name;
                    //mem_Fusionstatus=fusion_status;
                    System.out.println("MemberFusionStatus======"+mem_Fusionstatus);
                    ImpdsBean impdsBean = ImpdsBean.getInstance();
                    impdsBean.setDealername(dealer_name);
                    impdsBean.setDealeruid(rc_uid);
                    impdsBean.setSessionId(sessionId);
                    //impdsBean.setFusionStatus(fusion_status);

                }
            }
        };

        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

        String month = dateformat.format(new Date()).substring(9, 11);
        String year = dateformat.format(new Date()).substring(12, 16);
        try {
            jsonObject.put("distCode", sale_dist_code);
            jsonObject.put("fpsId", sale_fps_id);
            jsonObject.put("id", rc_id);
            jsonObject.put("idType", id_type);
            jsonObject.put("month", month);
            jsonObject.put("sessionId", fps_session_id);
            jsonObject.put("stateCode", sale_state_code);
            jsonObject.put("terminalId", android_id);
            jsonObject.put("token", "91f01a0a96c526d28e4d0c1189e80459");
            jsonObject.put("userName", "IMPDS");
            jsonObject.put("year", year);

        } catch (Exception e) {
            e.printStackTrace();
        }


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //showLog(jsonObject.toString(),"RQ");
        System.out.println("request_queue===" + jsonObject.toString());
            System.out.println("==========="+dealerConstants.fpsURLInfo.impdsURL+"impdsMEService");
            Show( context.getResources().getString(R.string.Please_wait),
                context.getResources().getString(R.string.Processing));

        System.out.println(dealerConstants.fpsURLInfo.impdsURL);
            //dealerConstants.fpsURLInfo.impdsURL+"impdsMEService",

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,dealerConstants.fpsURLInfo.impdsURL+"impdsMEService",
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Dismiss();
                            System.out.println("response_queue===" + response.toString());

                            String respMessage = response.getString("resp_message");
                            String respCode = response.getString("resp_code");
                            if (respCode != null && respCode.equals("001")) {
                                //showLog(response.toString(),"RSP");
                                jresponse = response;
                                rc_id = response.getString("rcId");
                                state_name = response.getString("homeStateName");
                                state_code = response.getString("homeStateCode");
                                home_dist_code = response.getString("districtCode");
                                district_name = response.getString("homeDistName");
                                sessionId = response.getString("saleSession");
                                home_fps_id = response.getString("fpsId");
                                scheme_id = response.getString("schemeId");
                                scheme_name = response.getString("schemeName");
                                alloc_month = response.getString("allocationmonth");
                                alloc_year =  response.getString("allocationyear");
                                fusion_Count = response.getInt("fusionCount");
                                System.out.println("FusionCOUNT===="+fusion_Count);
                                aadharAuthType = response.getString("aadharAuthType");
                                ImpdsBean impdsBean = ImpdsBean.getInstance();
                                impdsBean.setUsername("IMPDS");
                                // impdsBean.setSaleDistCode(sale_dist_code);
                                impdsBean.setHomeDistCode(home_dist_code);
                                impdsBean.setSessionId(sessionId);
                                impdsBean.setHomeFpsId(home_fps_id);
                                impdsBean.setReceiptId(sessionId);
                                impdsBean.setMemberId("");
                                impdsBean.setHomeStateCode(state_code);
                                impdsBean.setUidToken("");
                                impdsBean.setMemberName(memberName);
                                impdsBean.setRcId(rc_id);
                                impdsBean.setUid(rc_uid);
                                impdsBean.setSchemeId(scheme_id);
                                //impdsBean.setSaleStateCode(sale_state_code);
                                //impdsBean.setSaleStateName("Lakshadweep");
                                //impdsBean.setSaleDistName(impdsBean.getSaleDistName());
                                impdsBean.setHomeDistName(district_name);
                                // impdsBean.setSaleFpsId(sale_fps_id);
                                impdsBean.setSchemeName(scheme_name);
                                impdsBean.setAllocation_month(alloc_month);
                                impdsBean.setAllocation_year(alloc_year);
                                impdsBean.setHomeStateName(state_name);
                                impdsBean.setFusionCount(String.valueOf(fusion_Count));
                                impdsBean.setAadharAuthType(aadharAuthType);


                                JSONArray memberDetailsList = response.getJSONArray("memberDetailsList");
                                // JSONArray transactionList = response.getJSONArray("transactionList");

                                if (memberDetailsList != null && memberDetailsList.length() > 0) {
                                    for (int i = 0; i < memberDetailsList.length(); i++) {
                                        JSONObject object = memberDetailsList.getJSONObject(i);

                                        delName = object.getString("memberName");
                                        delUid = object.getString("uid");
                                        memberId = object.getString("memberId");
                                        System.out.println("memberId===="+memberId);
                                        memberName = object.getString("memberName");
                                        System.out.println("memberName===="+memberName);
                                        fusionStatus = object.getString("fusionStatus");
                                        System.out.println("Fusionstatus===="+fusionStatus);

                                        m_id_list.add(memberId);
                                        m_name_list.add(memberName);
                                        m_fusionStatus_list.add(fusionStatus);

                                        text_fps_id = findViewById(R.id.text_fps_id);
                                        text_scheme_id = findViewById(R.id.text_scheme_id);
                                        home_state_name = findViewById(R.id.home_state_name);
                                        home_dist_name = findViewById(R.id.home_dist_name);

                                        text_fps_id.setText(sale_fps_id);
                                        text_scheme_id.setText(scheme_name);
                                        home_state_name.setText(state_name);
                                        home_dist_name.setText(district_name);

                                        txt1 = new TextView(RC_MemberDetails.this);
                                        txt2 = new TextView(RC_MemberDetails.this);
                                        //TextView txt3 = new TextView(RC_MemberDetails.this);

                                        txt1.setText(delName);
                                        txt1.setPadding(8, 8, 8, 8);
                                        txt1.setBackgroundResource(R.drawable.ben_table_cell_shape);
                                        txt1.setGravity(Gravity.CENTER);
                                        txt1.setTextSize(18);
                                        txt1.setTextColor(Color.parseColor("#000000"));

                                        txt2.setText(delUid);
                                        txt2.setPadding(8, 8, 8, 8);
                                        txt2.setBackgroundResource(R.drawable.ben_table_cell_shape);
                                        txt2.setGravity(Gravity.CENTER);
                                        txt2.setTextSize(18);
                                        txt2.setTextColor(Color.parseColor("#000000"));

                                        //txt3.setText(fusionStatus);
                                        //txt3.setVisibility(View.GONE);

                                        tableRow = new TableRow(RC_MemberDetails.this);
                                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams
                                                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        tableRow.setLayoutParams(layoutParams);

                                        tableRow.setClickable(true);

                                        tableRow.setTag(k);
                                        tableRow.setOnClickListener(clickListener);

                                        tableRow.addView(txt1);
                                        tableRow.addView(txt2);
                                        //tableRow.addView(txt3);
                                        tableLayout.addView(tableRow, k);
                                        //tableLayout.setColumnCollapsed(2,false);
                                        k++;

                                    }


                                } else {
                                    show_AlertDialog(
                                            context.getResources().getString(R.string.IMPDS),
                                            context.getResources().getString(R.string.ResponseCode)+respCode,
                                            context.getResources().getString(R.string.ResponseMsg)+respMessage,
                                            1
                                    );

                                }

                            } else {
                                show_AlertDialog(
                                        context.getResources().getString(R.string.IMPDS),
                                        context.getResources().getString(R.string.ResponseCode)+respCode,
                                        context.getResources().getString(R.string.ResponseMsg)+respMessage,
                                        1
                                );
                            }
                        } catch (JSONException e) {
                            Dismiss();
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Dismiss();
                        show_AlertDialog(
                                context.getResources().getString(R.string.IMPDS),"Unable to Fetch Data"
                               ,error.getLocalizedMessage(),
                                0
                        );
                    }
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30 * 1000,
                0,
                0));
        requestQueue.add(jsonObjectRequest);


        scanFP = findViewById(R.id.scanFP);
        scanFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);

                 if (!(selectedIndex > 0)) {

                    show_AlertDialog(
                            context.getResources().getString(R.string.IMPDS),
                            context.getResources().getString(R.string.Please_Select_Member_Name),
                            "",
                            0
                    );
                }

                if (selectedIndex > 0) {
                    ImpdsBean impdsBean = ImpdsBean.getInstance();
                    impdsBean.setMemberName(m_name_list.get(selectedIndex - 1));
                    impdsBean.setMemberId(m_id_list.get(selectedIndex - 1));
                    impdsBean.setFusionStatus(m_fusionStatus_list.get(selectedIndex-1));

                    try {
                        String selectedPackage = "com.mantra.rdservice";
                        Intent intent1 = new Intent("in.gov.uidai.rdservice.fp.CAPTURE", null);
                        String pidOptXML = PIDUtil.getMantraPIDXml(fCount,aadharAuthType,fposh);
                        intent1.putExtra("PID_OPTIONS", pidOptXML);
                        intent1.setPackage(selectedPackage);
                        startActivityForResult(intent1, 13);
                    } catch (Exception e) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.IMPDS) ,
                                context.getResources().getString(R.string.Exception)+e.getMessage(),
                                "",
                                1
                        );
                    }

                }

            }
        });

        btn_back=findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                finish();
            }
        });
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Override
    public void initializeControls() {

        toolbarActivity.setText(context.getResources().getString(R.string.IMPDS));
        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
    }

    private void displayToast_success() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_MemberDetails.this);
        builder.setTitle(context.getResources().getString(R.string.BENEFICIARY));
        builder.setIcon(R.drawable.success);
        builder.setMessage(context.getResources().getString(R.string.Member_Authentication))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getApplicationContext(), RC_CommodityDetails.class);
                        i.putExtra("transactionList", jresponse.toString());
                        startActivity(i);
                        finish();
                    }
                });

        android.app.AlertDialog alert = builder.create();
        alert.show();

    }
    private void selectRow(int index) {

        if (index != selectedIndex) {
            if (selectedIndex >= 0) {
                deselectRow(selectedIndex);
            }
            TableRow tr = (TableRow) tableLayout.getChildAt(index);
            tr.setBackgroundColor(getResources().getColor(R.color.onClickColor));
            selectedIndex = index;

        }
    }

    private void deselectRow(int index) {
        if (index >= 0) {
            TableRow tr = (TableRow) tableLayout.getChildAt(index);
            tr.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Dismiss();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 13) {

                String pidDataXML = data.getStringExtra("PID_DATA");
                if (pidDataXML != null) {
                    if (pidDataXML.equals("") || pidDataXML.isEmpty()) {

                        paramOK = false;

                        return;
                    }
                    if (pidDataXML.startsWith("ERROR:-")) {

                        paramOK = false;

                        return;
                    } else {
                        DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
                        org.w3c.dom.Document inputDocument = null;
                        try {
                            inputDocument = db.newDocumentBuilder().parse(new InputSource(new StringReader(pidDataXML)));
                            NodeList nodes = inputDocument.getElementsByTagName("PidData");
                            if (nodes != null) {
                                //Element element = (Element) nodes.item(0);
                                NodeList respNode = inputDocument.getElementsByTagName("Resp");
                                if (respNode != null) {
                                    Element element2 = (Element) respNode.item(0);
                                    String errcode = element2.getAttribute("errCode");
                                    //Toast.makeText(getApplicationContext(),"errcode==="+errcode,Toast.LENGTH_SHORT).show();

                                    if (!errcode.equals("0")) {
                                        paramOK = false;

                                    } else {
                                        NodeList dataTypeNode = inputDocument.getElementsByTagName("Data");
                                        if (dataTypeNode != null) {
                                            Element element1 = (Element) dataTypeNode.item(0);
                                            pId = element1.getTextContent();
                                            dataType = element1.getAttribute("type");
                                        }

                                        NodeList hmacNode = inputDocument.getElementsByTagName("Hmac");
                                        if (hmacNode != null) {
                                            Element element1 = (Element) hmacNode.item(0);
                                            hmac = element1.getTextContent();
                                        }
                                        NodeList ciNode = inputDocument.getElementsByTagName("Skey");
                                        if (ciNode != null) {
                                            Element element1 = (Element) ciNode.item(0);
                                            skey = element1.getTextContent();
                                            certIdentifier = element1.getAttribute("ci");
                                        }
                                        NodeList devInfoNode = inputDocument.getElementsByTagName("DeviceInfo");
                                        if (devInfoNode != null) {
                                            Element element1 = (Element) devInfoNode.item(0);
                                            dpId = element1.getAttribute("dpId");
                                            rdId = element1.getAttribute("rdsId");
                                            rdVer = element1.getAttribute("rdsVer");
                                            dc = element1.getAttribute("dc");
                                            mc = element1.getAttribute("mc");
                                            mId = element1.getAttribute("mi");
                                        }
                                        paramOK = true;
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else if(requestCode ==2){
                fposh = data.getStringExtra("FUSION_DATA");
                System.out.println("FUSIONDATA====="+fposh);
                fCount ="2";
            }
            else if (requestCode==99){
                return;
            }
        }

        if (paramOK && requestCode == 13) {

            JSONObject jsonObject = new JSONObject();
             if(mem_Fusionstatus.equals("0")){
                 bioAuthType = "FMR";
             }else{
                 bioAuthType = "FUSION";
             }
             try {
                JSONObject object = new JSONObject();
                object.put("certificateIdentifier", certIdentifier);
                object.put("dataType", dataType);
                object.put("dc", dc);
                object.put("dpId", dpId);
                object.put("encHmac", hmac);
                object.put("mc", mc);
                object.put("mid", mId);
                object.put("rdId", rdId);
                object.put("rdVer", rdVer);
                object.put("secure_pid", pId);
                object.put("sessionKey", skey);

                jsonObject.put("authRD", object);
                jsonObject.put("bioAuthType",bioAuthType);
                //jsonObject.put("auth_type", "FMR");
                 jsonObject.put("auth_type", "B");
                 jsonObject.put("aadharAuthType",aadharAuthType);
                jsonObject.put("consent", "Y");
                jsonObject.put("token", "91f01a0a96c526d28e4d0c1189e80459");
                jsonObject.put("home_state_id", state_code);
                jsonObject.put("sale_state_id", sale_state_code);
                jsonObject.put("fps_id", sale_fps_id);
                jsonObject.put("rc_id", rc_id);
                jsonObject.put("rc_uid", rc_uid);
                jsonObject.put("terminal_id", android_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Show( context.getResources().getString(R.string.Please_wait),
                    context.getResources().getString(R.string.Authenticating));

            RequestQueue requestQueue = Volley.newRequestQueue(RC_MemberDetails.this);
                   //dealerConstants.fpsURLInfo.impdsURL+"impdsBenfAuthentication",
                   JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                           dealerConstants.fpsURLInfo.impdsURL+"impdsBenfAuthentication",
                    /*  "http://164.100.65.96/CTGMobileApplication/mobileimpdsApi/impdsBenfAuthentication",*/
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Dismiss();
                                System.out.println("bio---" + response.toString());
                                String respCode = response.getString("resp_code");
                                String respMessage = response.getString("resp_message");
                                if (respCode == null || respCode.isEmpty()) {
                                    System.out.println("Bio===null Response====");
                                    show_AlertDialog(
                                            context.getResources().getString(R.string.Dealer) + memberName,
                                            context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                            "",
                                            0);
                                    return;
                                }
                                if (respCode != null && respCode.equalsIgnoreCase("001"))
                                {
                                    String uid_refer_no = response.getString("txn_id");
                                    ImpdsBean impdsBean = ImpdsBean.getInstance();
                                    impdsBean.setUidRefNumber(uid_refer_no);
                                    impdsBean.setUid(rc_uid);
                                    displayToast_success();
                                    System.out.println("displayToast_success()==========");

                                }else if(mem_Fusionstatus.equals("0") && respCode.equals("300")){
                                    System.out.println("mem_Fusionstatus=========00000");
                                    //count = 2;
                                    count++;
                                    show_AlertDialog(
                                            context.getResources().getString(R.string.IMPDS),
                                            context.getResources().getString(R.string.Dealer_Fusion)+respCode,
                                            context.getResources().getString(R.string.ResponseMsg)+respMessage,
                                            2);

                                    return;

                                }else if(mem_Fusionstatus.equals("1"))
                                {
                                    count++;
                                    System.out.println("mem_Fusionstatus=========11111");
                                    Intent fingerslection = new Intent(context, FusionFingerSectionActivity.class);
                                    startActivityForResult(fingerslection,2);

                                }else{
                                    System.out.println("IMPDS==TEJ====11");
                                    show_AlertDialog(
                                            context.getResources().getString(R.string.IMPDS),
                                            context.getResources().getString(R.string.ResponseCode)+respCode,
                                            context.getResources().getString(R.string.ResponseMsg)+respMessage,
                                            0);
                                    System.out.println("IMPDS==TEJ====22");

                                }
                            }catch (JSONException e) {
                                Dismiss();
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Biometric data did not match(300)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("VolleyError---" + error.toString());

                            Dismiss();
                            show_AlertDialog(
                                    context.getResources().getString(R.string.IMPDS),
                                    context.getResources().getString(R.string.Authentication_Problem)+error.getMessage(),
                                    "",
                                    1
                            );
                        }
                    }
            );

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        } else {
            show_AlertDialog(
                    context.getResources().getString(R.string.IMPDS),
                    context.getResources().getString(R.string.Device_Not_Ready),
                    "",
                    0
            );
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
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
                if (i==1){
                    finish();
                }else if(i==2){
                    if(count==fusion_Count){
                        Intent cash = new Intent(RC_MemberDetails.this, IMPDSActivity.class);
                        startActivity(cash);
                    }else{
                        Intent fingerslection = new Intent(context, FusionFingerSectionActivity.class);
                        startActivityForResult(fingerslection, 2);
                    }
                }

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public void Dismiss(){
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }
    public void Show(String title,String msg){
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