package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.AvailedCommodity;
import com.visiontek.Mantra.Utils.CommodityTransactionData;
import com.visiontek.Mantra.Utils.ConnectivityReceiver;
import com.visiontek.Mantra.Utils.DataIssuedCommodity;
import com.visiontek.Mantra.Utils.IMEIUtil;
import com.visiontek.Mantra.Utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.setListViewHeightBasedOnChildren;

public class RC_CommodityDetails extends BaseActivity {
    JSONObject response = null;
    ProgressDialog pd = null;
    TableLayout tableLayout2;
    Button buttonProceed;
    ArrayList<DataIssuedCommodity> dataIssuedCommodities;
    ArrayList<AvailedCommodity> dataAfterUpdatingAviled;
    ArrayList<CommodityTransactionData> commodityTransactionData;
    ConnectivityReceiver connectivityReceiver;
    private String sale_state_code, sale_dist_code, rc_id, session_id, sale_fps_id, receiptId, transactionId,
            android_id, fps_session_id, uid_refer_no, member_id, member_name, weighStatus, home_dist_code,
            rc_uid, home_fps_id, home_state_id, scheme_id, scheme_name;
    private String com_code, com_name, units, tot_qty, price, clo_bal, availed_qty;
    private double tot_cb = 0.0, tot_tot_qty = 0.0, tot_amount = 0.0, tot_availed_qty = 0.0, totalqty = 0.0;

    ArrayList<String> list_status, list_text_issued, list_comm, list_iss_qty, new_Edit_list;
    ArrayList<String> list_comm_name, list_units, list_issue_qty, list_price, list_close_bal, list_weig_status, list_comm_code;
    RC_CommodityDetails.Myadapter myadapter;
    ListView lv_issuecomm;
    String st_new_issued_qty;
    int st_flag_one;
    ArrayList<Integer> list_flag_one, list_flag_two;
    Button back;
    Context context;



    @Override
    public void initialize() {
        try {

            context=RC_CommodityDetails.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.ben_activity_issued_commodity, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();
            pd = new ProgressDialog(context);
            connectivityReceiver = new ConnectivityReceiver();

            buttonProceed = findViewById(R.id.proceed);
            buttonProceed.setEnabled(true);
            dataIssuedCommodities = new ArrayList<>();
            dataAfterUpdatingAviled = new ArrayList<>();
            commodityTransactionData = new ArrayList<>();

            tableLayout2 = findViewById(R.id.tablay2);
            tableLayout2.setStretchAllColumns(true);

            android_id = IMEIUtil.getDeviceIMEI(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.toolbar));//33A1C9
            }

            Intent intent = getIntent();
            String jsonObject1 = intent.getStringExtra("transactionList");


            try {
                response = new JSONObject(jsonObject1);

                rc_id = ImpdsBean.getInstance().getRcId();

                rc_uid = ImpdsBean.getInstance().getUid();
                fps_session_id = ImpdsBean.getInstance().getFps_session_id();
                uid_refer_no = ImpdsBean.getInstance().getUidRefNumber();
                home_state_id = ImpdsBean.getInstance().getHomeStateCode();
                sale_state_code = ImpdsBean.getInstance().getSaleStateCode();
                home_fps_id = ImpdsBean.getInstance().getHomeFpsId();
                sale_fps_id = ImpdsBean.getInstance().getSaleFpsId();
                scheme_id = ImpdsBean.getInstance().getSchemeId();
                scheme_name = ImpdsBean.getInstance().getSchemeName();
                member_id = ImpdsBean.getInstance().getMemberId();
                member_name = ImpdsBean.getInstance().getMemberName();
                sale_dist_code = ImpdsBean.getInstance().getSaleDistCode();
                home_dist_code = ImpdsBean.getInstance().getHomeDistCode();
                session_id = ImpdsBean.getInstance().getSessionId();

                ImpdsBean impdsBean = ImpdsBean.getInstance();

                impdsBean.setSchemeName(scheme_name);

                lv_issuecomm = findViewById(R.id.lv_issuecomm);
                lv_issuecomm.setOnItemClickListener(listener_issue);


                list_status = new ArrayList<>();
                list_text_issued = new ArrayList<>();
                list_comm = new ArrayList<>();
                list_iss_qty = new ArrayList<>();

                list_status.add("");
                list_text_issued.add("");
                list_comm.add("");
                list_iss_qty.add("");

                list_comm_name = new ArrayList<>();
                list_units = new ArrayList<>();
                list_issue_qty = new ArrayList<>();
                list_price = new ArrayList<>();
                list_close_bal = new ArrayList<>();
                list_weig_status = new ArrayList<>();
                list_comm_code = new ArrayList<>();
/*
            list_comm_name.add("");
            list_units.add("");
            list_issue_qty.add("");
            list_price.add("");
            list_close_bal.add("");
            list_weig_status.add("");
            list_comm_code.add("");*/


                String respCode = response.getString("resp_code");
                String respMessage = response.getString("resp_message");

                if (respCode != null && respCode.equalsIgnoreCase("001")) {

                    JSONArray jsonArray_entitle = response.getJSONArray("transactionList");
                    for (int j = 0; j < jsonArray_entitle.length(); j++) {
                        JSONObject jsonObject_entitle = jsonArray_entitle.getJSONObject(j);

                        com_code = jsonObject_entitle.getString("commCode");
                        com_name = jsonObject_entitle.getString("commName");
                        units = "Kgs";//jsonObject_entitle.getString("measureUnit");
                        tot_qty = jsonObject_entitle.getString("allowedAllotment");
                        price = jsonObject_entitle.getString("pricePerKg");
                        clo_bal = jsonObject_entitle.getString("cb");
                        availed_qty = jsonObject_entitle.getString("allowedAllotment");
                        weighStatus = "T";//jsonObject_entitle.getString("weighStatus");

                        list_comm_code.add(com_code);
                        list_comm_name.add(com_name);
                        list_units.add(units);
                        list_issue_qty.add(tot_qty);
                        list_price.add(price);
                        list_close_bal.add(clo_bal);
                        list_text_issued.add(tot_qty);
                        list_weig_status.add(weighStatus);
                    }
                } else {
                    show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                            respMessage,
                            "",
                            1);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                myadapter = new Myadapter(RC_CommodityDetails.this);
                lv_issuecomm.setAdapter(myadapter);
                setListViewHeightBasedOnChildren(lv_issuecomm);
                myadapter.notifyDataSetChanged();
            } catch (NullPointerException e) {

                Toast.makeText(getApplicationContext(), "No records found...",
                        Toast.LENGTH_LONG).show();
            }


            buttonProceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    list_flag_one = new ArrayList<>();
                    list_flag_two = new ArrayList<>();
                    new_Edit_list = new ArrayList<>();
                    for (int j = 0; j < list_comm_name.size(); j++) {
                        if (list_text_issued.get(j).equals("")) {
                            new_Edit_list.add(list_text_issued.get(j).replace(list_text_issued.get(j), String.valueOf(0)));
                        } else {
                            new_Edit_list.add(list_text_issued.get(j));
                        }

                    }
                    int decimal_err = 0;
                    for (int i = 0; i < list_comm_name.size(); i++) {

                        String status = list_weig_status.get(i);
                        String edit_iss = new_Edit_list.get(i);
                        String issued_val = list_issue_qty.get(i);
                        String comm = list_comm_name.get(i);
                        String cb = list_close_bal.get(i);
                        String unit = list_units.get(i);
                        String comm_code = list_comm_code.get(i);

                        if (status.equals("T")) {
                            totalqty += Double.parseDouble(edit_iss);
                            if (Double.parseDouble(edit_iss) >= 0.0 && unit.contains("Pk") && Double.parseDouble(edit_iss) % 1 != 0) {
                                show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                        "Decimal value for " + comm + "-" + unit + " not allowed",
                                        "",
                                        0);
                                decimal_err++;
                            }
                            if (Double.parseDouble(edit_iss) >= 0.0 && unit.equalsIgnoreCase("Kgs") && Double.parseDouble(edit_iss) % 0.5 != 0) {
                                show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                        "Only multiples of 500 gms allowed for " + comm + "(" + unit + ")",
                                        "",
                                        0);
                                decimal_err++;
                            }
                            if (Double.parseDouble(edit_iss) >= 0.0 && Double.parseDouble(edit_iss) <= Double.parseDouble(issued_val)) {
                                if (Double.parseDouble(edit_iss) > Double.parseDouble(cb)) {
                                    show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                            "Issued Quantity value for " + comm + " should be less than or equal to closing balance",
                                            "",
                                            0);

                                    decimal_err++;
                                } else {
                                    st_flag_one = 1;
                                    list_flag_one.add(st_flag_one);
                                }

                            } else {
                                show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                        "Issued Quantity value for " + comm + " must be between 0 and alloted qty",
                                        "",
                                        0);

                                decimal_err++;

                            }
                        } else if (status.equals("F")) {
                            totalqty += Double.parseDouble(edit_iss);
                            if (Double.parseDouble(edit_iss) == 0 || Double.parseDouble(edit_iss) == Double.parseDouble(issued_val)) {

                                if (Double.parseDouble(edit_iss) > Double.parseDouble(cb)) {
                                    show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                            "Issued Quantity value for " + comm + " should be less than or equal to closing balance",
                                            "",
                                            0);

                                    decimal_err++;
                                } else {
                                    st_flag_one = 2;
                                    list_flag_two.add(st_flag_one);
                                }

                            } else {
                                show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                        "Issued Quantity value for " + comm + " must be equals to 0 or balance qty",
                                        "",
                                        0);

                                decimal_err++;
                            }
                        }
                    }
                    if (totalqty <= 0) {
                        show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                "Please issue atleast one commodity",
                                "",
                                0);

                        decimal_err++;
                    }

                    int count_weig_T = Collections.frequency(list_weig_status, "T");
                    int count_weig_F = Collections.frequency(list_weig_status, "F");
                    if (count_weig_T == list_flag_one.size() && count_weig_F == list_flag_two.size() && decimal_err == 0) {

                        buttonProceed.setEnabled(false);
                        buttonProceed.setClickable(false);
                        // buttonProceed.setVisibility(View.GONE);

                        for (int i = 0; i < list_comm_name.size(); i++) {

                            String status = list_weig_status.get(i);
                            String edit_iss = new_Edit_list.get(i);
                            String issued_val = list_issue_qty.get(i);
                            String comm = list_comm_name.get(i);
                            String cb = list_close_bal.get(i);
                            String price_new = list_price.get(i);
                            String comm_code = list_comm_code.get(i);

                            DataIssuedCommodity issuedCommodity = new DataIssuedCommodity(comm, edit_iss, price_new,
                                    comm_code, cb, availed_qty, weighStatus);
                            dataIssuedCommodities.add(issuedCommodity);
                        }
                        sendrequest();
                    } else {
                        //   Toast.makeText(getApplicationContext(),"Please enter correct values!",Toast.LENGTH_LONG).show();
                    }
                }
            });

            back = findViewById(R.id.btn_back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    finish();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initializeControls() {
        toolbarActivity.setText(context.getResources().getString(R.string.IMPDS));
        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
    }

    public class Myadapter extends BaseAdapter {

        private Context context;
        LayoutInflater inflater;

        public Myadapter(Context c) {
            context = c;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list_comm_name.size();

        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public class ViewHolder {
            TextView text_comm_name;
            TextView text_units;
            TextView text_availed_qty;
            EditText text_issue_qty;
            TextView text_price;
            TextView text_clos_bal;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup parent) {
            final RC_CommodityDetails.Myadapter.ViewHolder holder;

            if (convertView == null) {
                holder = new RC_CommodityDetails.Myadapter.ViewHolder();

                convertView = inflater.inflate(R.layout.issue_commodity, null);

                //      convertView.setBackgroundColor(Color.parseColor("#e8fbfc"));

                holder.text_comm_name = (TextView) convertView.findViewById(R.id.text_comm_name);
                holder.text_units = (TextView) convertView.findViewById(R.id.text_units);
                holder.text_availed_qty = (TextView) convertView.findViewById(R.id.text_availed_qty);
                holder.text_issue_qty = (EditText) convertView.findViewById(R.id.text_issue_qty);
                holder.text_price = (TextView) convertView.findViewById(R.id.text_price);
                holder.text_clos_bal = (TextView) convertView.findViewById(R.id.text_clos_bal);

                convertView.setTag(holder);

            } else {
                holder = (RC_CommodityDetails.Myadapter.ViewHolder) convertView.getTag();
            }

            holder.text_comm_name.setText(list_comm_name.get(i));
            holder.text_units.setText(list_units.get(i));
            holder.text_availed_qty.setText(list_issue_qty.get(i));
            holder.text_issue_qty.setText(list_issue_qty.get(i));
            holder.text_price.setText(list_price.get(i));
            holder.text_clos_bal.setText(list_close_bal.get(i));
            holder.text_issue_qty.setBackgroundColor(Color.YELLOW);
            holder.text_issue_qty.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub


                    st_new_issued_qty = holder.text_issue_qty.getText().toString().trim();

                    list_text_issued.set(i, st_new_issued_qty);

                }
            });


            return convertView;
        }

        public void notifyDataSetChanged() {
            // TODO Auto-generated method stub
            super.notifyDataSetChanged();
        }
    }


    private AdapterView.OnItemClickListener listener_issue = new AdapterView.OnItemClickListener() {
        @SuppressLint("WrongConstant")
        public void onItemClick(AdapterView parent, View v, int position,
                                long id) {
            // Create custom dialog object
            int c = position;
            Toast.makeText(getApplicationContext(), "" + c, 300).show();

        }


    };


    public void sendrequest() {
        String deviceTxnId;
        DateFormat dateFormat = new SimpleDateFormat("hhmmss");
        DateFormat orderdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        deviceTxnId = String.format("%s%03d%s", DEVICEID, dayOfYear, dateFormat.format(now));

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", "IMPDS");
            jsonObject.put("token", "91f01a0a96c526d28e4d0c1189e80459");
            jsonObject.put("sessionId", session_id);
            /*jsonObject.put("fps_session_id", fps_session_id);*/
            jsonObject.put("rcId", rc_id);
            jsonObject.put("uid", rc_uid);
            jsonObject.put("uidToken", "XXXXX");
            jsonObject.put("uidRefNumber", uid_refer_no);
            jsonObject.put("homeStateCode", home_state_id);
            jsonObject.put("saleStateCode", sale_state_code);
            jsonObject.put("homeFpsId", home_fps_id);

            JSONArray jsonArray = new JSONArray();
            double price = 0;
            for (int t = 0; t < dataIssuedCommodities.size(); t++) {

                if (Double.parseDouble(dataIssuedCommodities.get(t).getTotalQty()) > 0) {
                    tot_amount += Double.parseDouble(dataIssuedCommodities.get(t).getPrice()) *
                            Double.parseDouble(dataIssuedCommodities.get(t).getTotalQty());
                    tot_cb = tot_cb + Double.parseDouble(dataIssuedCommodities.get(t).getCb());
                    tot_tot_qty = tot_tot_qty + Double.parseDouble(dataIssuedCommodities.get(t).getTotalQty());
                    price = Double.parseDouble(dataIssuedCommodities.get(t).getPrice()) *
                            Double.parseDouble(dataIssuedCommodities.get(t).getTotalQty());

                    JSONObject object = new JSONObject();
                    object.put("commodityCode", dataIssuedCommodities.get(t).getCommCode());
                    object.put("commodityName", dataIssuedCommodities.get(t).getCommodity());
                    object.put("availedQuantity", dataIssuedCommodities.get(t).getTotalQty());
                    object.put("amount", price);
                    object.put("pricePerKg", dataIssuedCommodities.get(t).getPrice());
                    object.put("totalQuantity", dataIssuedCommodities.get(t).getTotalQty());

                    jsonArray.put(object);
                }
            }
            ImpdsBean impdsBean = ImpdsBean.getInstance();
            impdsBean.setTotal_amount(String.valueOf(tot_amount));
            jsonObject.put("saleFpsId", sale_fps_id);
            jsonObject.put("transactionList", jsonArray);
            jsonObject.put("transStatus", "S");
            jsonObject.put("schemeId", scheme_id);
            jsonObject.put("schemeName", scheme_name);
            jsonObject.put("terminalId", android_id);
            jsonObject.put("receiptId", deviceTxnId);//receiptId
            jsonObject.put("transactionId", session_id);
            jsonObject.put("memberId", member_id);
            jsonObject.put("memberName", member_name);
            jsonObject.put("saleDistCode", sale_dist_code);
            jsonObject.put("homeDistCode", home_dist_code);
            jsonObject.put("typeOfTrans", "Cash");
            jsonObject.put("authMode", "Finger");
            jsonObject.put("status", "0");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //  showLog("request==="+jsonObject.toString(),"");
        System.out.println(jsonObject.toString());

        Show(context.getResources().getString(R.string.Please_wait),
                context.getResources().getString(R.string.Processing));

        RequestQueue requestQueue_issue = Volley.newRequestQueue(RC_CommodityDetails.this);
        System.out.println(dealerConstants.fpsURLInfo.impdsURL);
        JsonObjectRequest jsonObjectRequest_issue = new JsonObjectRequest(
                Request.Method.POST,
                dealerConstants.fpsURLInfo.impdsURL + "saleTransaction",
                /*"http://164.100.65.96/CTGMobileApplication/mobileimpdsApi/saleTransaction",*/
                jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Dismiss();
                        if (tot_cb == 0) {
                            show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                    "Closing Balance Should be greater than 0",
                                    "",
                                    0);

                        } else if (tot_tot_qty == 0) {
                            show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                    "Please issue at least one commodity",
                                    "",
                                    0);

                        } else {

                            try {
                                String respMessage = response.getString("respMessage");
                                String respCode = response.getString("respCode");
                                if (respCode != null && respCode.equalsIgnoreCase("001")) {
                                    displayToast_success(jsonObject);

                                } else {
                                    show_AlertDialog(context.getResources().getString(R.string.IMPDS),
                                            respMessage + "(" + respCode + ")",
                                            "",
                                            1);

                                }

                            } catch (JSONException e) {
                                show_AlertDialog(context.getResources().getString(R.string.Device),
                                        context.getResources().getString(R.string.Internet_Connection),
                                        context.getResources().getString(R.string.Internet_Connection_Msg),
                                        0);
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Dismiss();
                        show_AlertDialog(context.getResources().getString(R.string.Device),
                                context.getResources().getString(R.string.Internet_Connection),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                0);
                    }
                }
        );
        jsonObjectRequest_issue.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                0,
                0));
        requestQueue_issue.add(jsonObjectRequest_issue);

    }


    private void displayToast_success(JSONObject jsonObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
        builder.setTitle("Transaction Status");
        builder.setMessage("Stock Issued Successfully")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getApplicationContext(), SuccessActivity.class);
                        i.putExtra("saleresponse", jsonObject.toString());
                        startActivity(i);
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Dismiss();
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
                    Intent i = new Intent(getApplicationContext(), IMPDSActivity.class);
                    startActivity(i);
                    finish();
                }

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

}