package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.networkConnected;

public class RC_MemberDetails extends AppCompatActivity {
    String certIdentifier, dataType, dc, dpId, hmac, mc, mId, rdId, rdVer, pId, skey;
    private int selectedIndex = -1;
    Boolean paramOK = false;
    public int k = 1;
    TableLayout tableLayout;
    TableRow tableRow;
    Button scanFP;
    ConnectivityReceiver connectivityReceiver;
   // private SimpleLocation location;
    String token;
    private AlertDialog.Builder builder;
    private TextView txt1, txt2, home_state_name, text_fps_id, home_dist_name, text_scheme_id;
    // ImpdsBean impdsBean;
    ProgressDialog progressDialog;
    private String sale_state_code, sale_dist_code, home_dist_code, rc_id,  district_name, state_name, state_code, sale_fps_id,
            sessionId, memberId, memberName, android_id, id_type,alloc_month,alloc_year,
            delName, delUid, rc_uid, home_fps_id, scheme_id, scheme_name, fps_session_id;
    JSONObject jresponse = null;
    ArrayList<String> m_id_list, m_name_list;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_c__member_details);

        context=RC_MemberDetails.this;
        if (!networkConnected(context)) {
            builder.setTitle("Internet Connection");
            builder.setMessage("Please Check Your Internet Connection").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create().show();
        }

        toolbarInitilisation();
        rc_id = ImpdsBean.getInstance().getRc_id();
        id_type = ImpdsBean.getInstance().getIdType();

        sale_state_code = ImpdsBean.getInstance().getSaleStateCode();
        sale_fps_id = ImpdsBean.getInstance().getSaleFpsId();
        sale_dist_code = ImpdsBean.getInstance().getSaleDistCode();
        fps_session_id = ImpdsBean.getInstance().getFps_session_id();

        m_id_list = new ArrayList<>();
        m_name_list = new ArrayList<>();

        connectivityReceiver = new ConnectivityReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.toolbar));//33A1C9
        }
        // location = new SimpleLocation(this);
        android_id = DEVICEID;

        token = TokenGenerator.nextToken();
        setToken(token);

        tableLayout = findViewById(R.id.tablay1);
        tableLayout.setStretchAllColumns(true);

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                if (index != null) {
                    selectRow(index);

                    TableRow tablerow = (TableRow) v;
                    TextView sample = (TextView) tablerow.getChildAt(1);
                    String del_uid = sample.getText().toString();

                    TextView sample1 = (TextView) tablerow.getChildAt(0);
                    String dealer_name = sample1.getText().toString();
                    rc_uid = del_uid;
                    memberName = dealer_name;
                    ImpdsBean impdsBean = ImpdsBean.getInstance();
                    impdsBean.setDealername(dealer_name);
                    impdsBean.setDealeruid(rc_uid);
                    impdsBean.setSessionId(sessionId);
                }
            }
        };

        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        System.out.println("+++++++===="+dateformat.format(new Date()));
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

        showBar();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//showLog(jsonObject.toString(),"RQ");
        System.out.println("request_queue===" + jsonObject.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "http://164.100.65.96/CTGMobileApplication/mobileimpdsApi/impdsMEService",
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            progressDialog.dismiss();
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

                                JSONArray memberDetailsList = response.getJSONArray("memberDetailsList");
                                // JSONArray transactionList = response.getJSONArray("transactionList");

                                if (memberDetailsList != null && memberDetailsList.length() > 0) {
                                    for (int i = 0; i < memberDetailsList.length(); i++) {
                                        JSONObject object = memberDetailsList.getJSONObject(i);

                                        delName = object.getString("memberName");
                                        delUid = object.getString("uid");
                                        memberId = object.getString("memberId");
                                        memberName = object.getString("memberName");

                                        m_id_list.add(memberId);
                                        m_name_list.add(memberName);

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


                                        tableRow = new TableRow(RC_MemberDetails.this);
                                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams
                                                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        tableRow.setLayoutParams(layoutParams);

                                        tableRow.setClickable(true);

                                        tableRow.setTag(k);
                                        tableRow.setOnClickListener(clickListener);

                                        tableRow.addView(txt1);
                                        tableRow.addView(txt2);

                                        tableLayout.addView(tableRow, k);
                                        k++;

                                    }
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RC_MemberDetails.this);
                                    builder.setTitle("Alert");

                                    builder.setMessage(respMessage)
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent i = new Intent(getApplicationContext(), IMPDSActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            });

                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RC_MemberDetails.this);
                                builder.setTitle("Alert");

                                builder.setMessage(respMessage + "(" + respCode + ")")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                Intent i = new Intent(getApplicationContext(), IMPDSActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();

                            }
                        } catch (JSONException e) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showMessageDialogue("Network connection timed out.Please Try later(E)", "Alert ");
                        // onBackPressed();
                    }
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                0,
                0));
        requestQueue.add(jsonObjectRequest);


        scanFP = findViewById(R.id.scanFP);
        scanFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!(selectedIndex > 0)) {
                    Toast.makeText(RC_MemberDetails.this, "Select user", Toast.LENGTH_SHORT).show();
                }

                if (selectedIndex > 0) {
                    System.out.println(selectedIndex + "--id--" + m_id_list.get(selectedIndex - 1) + "--name--" + m_name_list.get(selectedIndex - 1));
                    ImpdsBean impdsBean = ImpdsBean.getInstance();
                    impdsBean.setMemberName(m_name_list.get(selectedIndex - 1));
                    impdsBean.setMemberId(m_id_list.get(selectedIndex - 1));
                   /* if (vendor != null && vendor.contains("Startek")) {
                        try {
                            String selectedPackage = "com.acpl.registersdk";
                            Intent intent1 = new Intent("in.gov.uidai.rdservice.fp.CAPTURE", null);
                            String pidOptXML = PIDUtil.getOthersXml();

                            intent1.putExtra("PID_OPTIONS", pidOptXML);
                            intent1.setPackage(selectedPackage);
                            startActivityForResult(intent1, 13);
                        } catch (Exception e) {
                            showMessageDialogue("EXCEPTION- " + e.getMessage(), "EXCEPTION");
                        }
                    } else if (vendor != null && vendor.contains("NEXT")) {
                        try {
                            String selectedPackage = "com.nextbiometrics.rdservice";
                            Intent intent1 = new Intent("in.gov.uidai.rdservice.fp.CAPTURE", null);
                            String pidOptXML = PIDUtil.getOthersXml();

                            intent1.putExtra("PID_OPTIONS", pidOptXML);
                            intent1.setPackage(selectedPackage);
                            startActivityForResult(intent1, 13);
                        } catch (Exception e) {
                            showMessageDialogue("EXCEPTION- " + e.getMessage(), "EXCEPTION");
                        }
                    } else if (vendor != null && vendor.equalsIgnoreCase("Mantra")) {*/
                        try {
                            String selectedPackage = "com.mantra.rdservice";
                            Intent intent1 = new Intent("in.gov.uidai.rdservice.fp.CAPTURE", null);
                            String pidOptXML = PIDUtil.getMantraPIDXml();

                            intent1.putExtra("PID_OPTIONS", pidOptXML);
                            intent1.setPackage(selectedPackage);
                            startActivityForResult(intent1, 13);
                        } catch (Exception e) {
                            showMessageDialogue("EXCEPTION- " + e.getMessage(), "EXCEPTION");
                        }
                   /* } else {
                        //Toast.makeText(RC_MemberDetails.this, vendor + "  Please unplug and plugin the device", Toast.LENGTH_SHORT).show();
                        showMessageDialogue("Session timed out" , "Alert");
                    }*/
                }

            }
        });

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

        toolbarFpsid.setText("DEVICE ID");
        toolbarFpsidValue.setText(DEVICEID);
        toolbarActivity.setText("IMPDS MEMBER");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }

      private void displayToast_success() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_MemberDetails.this);
        builder.setTitle("Beneficiary");
        builder.setIcon(R.drawable.success);
        builder.setMessage("Authentication Successful")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getApplicationContext(), RC_CommodityDetails.class);
                        i.putExtra("transactionList", jresponse.toString());
                        startActivity(i);
                        startActivity(i);
                        finish();
                    }
                });

        android.app.AlertDialog alert = builder.create();
        alert.show();

    }

    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_MemberDetails.this);
        builder.setTitle("Alert");

        builder.setMessage("Session expired.Kindly login again")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }
    private void showLog(String messageTxt, String argTitle) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_MemberDetails.this);
        builder.setTitle("Alert");

        builder.setMessage(messageTxt)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void showMessageDialogue(String messageTxt, String argTitle) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_MemberDetails.this);
        builder.setTitle("Alert");

        builder.setMessage(messageTxt)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onBackPressed();
                        //dialog.cancel();
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


    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        location.beginUpdates();

    }

    @Override
    protected void onPause() {
        // location.endUpdates();
        super.onPause();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void displayToast() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RC_MemberDetails.this);
        builder.setTitle("Confirmation");

        builder.setMessage("Are you sure to quit from this APP?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


    @Override
    public void onBackPressed() {
        Intent dashboard = new Intent(getBaseContext(), IMPDSActivity.class);
        dashboard.addCategory(Intent.CATEGORY_HOME);
        dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboard);
        RC_MemberDetails.this.finish();
        super.onBackPressed();
    }

    public void showBar() {
        builder = new AlertDialog.Builder(RC_MemberDetails.this);
        progressDialog = new ProgressDialog(RC_MemberDetails.this);
        progressDialog.setMessage("Processing Data...");
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait");
        progressDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}