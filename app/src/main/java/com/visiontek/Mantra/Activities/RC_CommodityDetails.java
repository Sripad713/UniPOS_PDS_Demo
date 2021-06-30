package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.Utils.AvailedCommodity;
import com.visiontek.Mantra.Utils.CommodityTransactionData;
import com.visiontek.Mantra.Utils.DataIssuedCommodity;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.ConnectivityReceiver;
import com.visiontek.Mantra.Utils.IMEIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.visiontek.Mantra.Utils.Util.setListViewHeightBasedOnChildren;

public class RC_CommodityDetails extends AppCompatActivity {
    private static final String TAG = "IssuedCommodity";
    final private int REQUEST_CODE_ASK_PERMISSION = 111;

    JSONObject response = null;
    ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
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
    JSONObject jsonObject = null;
    ArrayList<String> list_status, list_text_issued, list_comm, list_iss_qty, new_Edit_list;
    ArrayList<String> list_comm_name, list_units, list_issue_qty, list_price, list_close_bal, list_weig_status, list_comm_code;
    RC_CommodityDetails.Myadapter myadapter;
    ListView lv_issuecomm;
    String st_new_issued_qty;
    int st_flag_one;
    ArrayList<Integer> list_flag_one, list_flag_two;
    TextView rcid;
    //ImpdsBean impdsBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ben_activity_issued_commodity);

        connectivityReceiver = new ConnectivityReceiver();

        buttonProceed = findViewById(R.id.proceed);
        buttonProceed.setEnabled(true);
        rcid = findViewById(R.id.rcid);
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
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        String trxdate = dateFormat.format(date);
        Intent intent = getIntent();
        String jsonObject1 = intent.getStringExtra("transactionList");

        try {
            response = new JSONObject(jsonObject1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //impdsBean = ImpdsBean.getInstance();
        rc_id = ImpdsBean.getInstance().getRcId();

        rc_uid =ImpdsBean.getInstance().getUid();
        fps_session_id = ImpdsBean.getInstance().getFps_session_id();
        uid_refer_no = ImpdsBean.getInstance().getUidRefNumber();
        home_state_id = ImpdsBean.getInstance().getHomeStateCode();
        sale_state_code = ImpdsBean.getInstance().getSaleStateCode();
        home_fps_id = ImpdsBean.getInstance().getHomeFpsId();
        sale_fps_id = ImpdsBean.getInstance().getSaleFpsId();
        scheme_id =ImpdsBean.getInstance().getSchemeId();
        scheme_name =ImpdsBean.getInstance().getSchemeName();
        member_id = ImpdsBean.getInstance().getMemberId();
        member_name = ImpdsBean.getInstance().getMemberName();
        sale_dist_code = ImpdsBean.getInstance().getSaleDistCode();
        home_dist_code = ImpdsBean.getInstance().getHomeDistCode();
        session_id = ImpdsBean.getInstance().getSessionId();

        System.out.println("******" + rc_uid + "**" + uid_refer_no);

        if(rc_id == null || sale_fps_id == null || rc_uid == null){
            logout();
        }
        ImpdsBean impdsBean = ImpdsBean.getInstance();
        // impdsBean.setSaleFpsId(home_fps_id);
        impdsBean.setSchemeName(scheme_name);
        rcid.setText("Ration Card ID - " + rc_id);

        lv_issuecomm = findViewById(R.id.lv_issuecomm);
        lv_issuecomm.setOnItemClickListener(listener_issue);


        try {
            progressDialog.dismiss();
            System.out.println("response=====" + response.toString());
            // showLog(response.toString(),"RES");

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

            list_comm_name.add("");
            list_units.add("");
            list_issue_qty.add("");
            list_price.add("");
            list_close_bal.add("");
            list_weig_status.add("");
            list_comm_code.add("");


            String respCode = response.getString("resp_code");
            String respMessage = response.getString("resp_message");

            if (respCode != null && respCode.equalsIgnoreCase("001")) {

                JSONArray jsonArray_entitle = response.getJSONArray("transactionList");
                for (int j = 0; j < jsonArray_entitle.length(); j++) {
                    JSONObject jsonObject_entitle = jsonArray_entitle.getJSONObject(j);

                    com_code = jsonObject_entitle.getString("commCode");
                    com_name = jsonObject_entitle.getString("commName");
                    units = "Kgs";//jsonObject_entitle.getString("measureUnit");
                    tot_qty =  jsonObject_entitle.getString("allowedAllotment");
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
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_CommodityDetails.this);
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

                android.app.AlertDialog alert = builder.create();
                alert.show();
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
                for (int i = 1; i < list_comm_name.size(); i++) {

                    String status = list_weig_status.get(i);
                    String edit_iss = new_Edit_list.get(i);
                    String issued_val = list_issue_qty.get(i);
                    String comm = list_comm_name.get(i);
                    String cb = list_close_bal.get(i);
                    String unit = list_units.get(i);
                    String comm_code = list_comm_code.get(i);

                    if (status.equals("T")) {
                        totalqty += Double.parseDouble(edit_iss);
                        System.out.println(comm_code + "--*===" + Double.parseDouble(edit_iss) + "**" + Double.parseDouble(issued_val)
                                + "***" + Double.parseDouble(cb) + "****" + totalqty);
                        if (Double.parseDouble(edit_iss) >= 0.0 && unit.contains("Pk") && Double.parseDouble(edit_iss) % 1 != 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                            builder.setTitle("Alert");

                            builder.setMessage("Decimal value for " + comm + "-" + unit + " not allowed")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                            decimal_err++;
                        }
                        if (Double.parseDouble(edit_iss) >= 0.0 && unit.equalsIgnoreCase("Kgs") && Double.parseDouble(edit_iss) % 0.5 != 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                            builder.setTitle("Alert");

                            builder.setMessage("Only multiples of 500 gms allowed for " + comm + "(" + unit+")")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                            decimal_err++;
                        }
                        if (Double.parseDouble(edit_iss) >= 0.0 && Double.parseDouble(edit_iss) <= Double.parseDouble(issued_val)) {
                            if (Double.parseDouble(edit_iss) > Double.parseDouble(cb)) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                                builder.setTitle("Alert");

                                builder.setMessage("Issued Quantity value for " + comm + " should be less than or equal to closing balance")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                return;
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();
                                decimal_err++;
                            } else {
                                st_flag_one = 1;
                                list_flag_one.add(st_flag_one);
                            }

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                            builder.setTitle("Alert");

                            builder.setMessage("Issued Quantity value for " + comm + " must be between 0 and alloted qty")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                            decimal_err++;

                        }
                    } else if (status.equals("F")) {
                        totalqty += Double.parseDouble(edit_iss);
                        System.out.println(comm_code + "--*===" + Double.parseDouble(edit_iss) + "**" + Double.parseDouble(issued_val)
                                + "***" + Double.parseDouble(cb) + "****" + totalqty);
                        if (Double.parseDouble(edit_iss) == 0 || Double.parseDouble(edit_iss) == Double.parseDouble(issued_val)) {
//                            Toast.makeText(IssuedCommodity.this, "F Success", Toast.LENGTH_SHORT).show();
                            if (Double.parseDouble(edit_iss) > Double.parseDouble(cb)) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                                builder.setTitle("Alert");

                                builder.setMessage("Issued Quantity value for " + comm + " should be less than or equal to closing balance")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                return;
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();
                                decimal_err++;
                            } else {
                                st_flag_one = 2;
                                list_flag_two.add(st_flag_one);
                            }

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                            builder.setTitle("Alert");

                            builder.setMessage("Issued Quantity value for " + comm + " must be equals to 0 or balance qty")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                            decimal_err++;
                        }
                    }
                }
                if (totalqty <= 0) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                    builder.setTitle("Alert");

                    builder.setMessage("Please issue atleast one commodity")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                    decimal_err++;
                }

                int count_weig_T = Collections.frequency(list_weig_status, "T");
                int count_weig_F = Collections.frequency(list_weig_status, "F");
                System.out.println(totalqty+"===^^22^^" + decimal_err);
                System.out.println(count_weig_T + "**clik--*" + list_flag_one.size());
                System.out.println(count_weig_F + "**clik--*" + list_flag_two.size());
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
            int c = position + 1;
            Toast.makeText(getApplicationContext(), "" + c, 300).show();

        }


    };


    public void sendrequest() {

        jsonObject = new JSONObject();
        try {
            jsonObject.put("username", "IMPDS");
            jsonObject.put("token", "91f01a0a96c526d28e4d0c1189e80459");
            jsonObject.put("sessionId", session_id);
            jsonObject.put("fps_session_id", fps_session_id);
            jsonObject.put("rcId", rc_id);
            jsonObject.put("uid", rc_uid);
            jsonObject.put("uidToken", "");
            jsonObject.put("uidRefNumber", uid_refer_no);
            jsonObject.put("homeStateCode", home_state_id);
            jsonObject.put("saleStateCode", sale_state_code);
            jsonObject.put("homeFpsId", home_fps_id);

            JSONArray jsonArray = new JSONArray();
            double price = 0;
            for (int t = 1; t < dataIssuedCommodities.size(); t++) {

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
            jsonObject.put("receiptId", session_id);//receiptId
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
        showBar();

        RequestQueue requestQueue_issue = Volley.newRequestQueue(RC_CommodityDetails.this);

        //  showMessageDialogue("request==="+jsonObject.toString(),"");
        JsonObjectRequest jsonObjectRequest_issue = new JsonObjectRequest(
                Request.Method.POST,
                "",
                jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        if (tot_cb == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                            builder.setTitle("Alert");

                            builder.setMessage("Closing Balance Should be greater than 0")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent i = new Intent(getApplicationContext(), RC_CommodityDetails.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                        } else if (tot_tot_qty == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                            builder.setTitle("Alert");
                            builder.setMessage("Please issue at least one commodity")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent i = new Intent(getApplicationContext(), RC_CommodityDetails.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {

                            try {
                                String respMessage = response.getString("respMessage");
                                String respCode = response.getString("respCode");
                                if (respCode != null && respCode.equalsIgnoreCase("001")) {
                                    displayToast_success();

                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
                                    builder.setTitle("Alert");

                                    builder.setMessage(respMessage + "(" + respCode + ")")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //dialog.cancel();
                                                    onBackPressed();
                                                }
                                            });

                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }

                            } catch (JSONException e) {
                                showMessageDialogue("Network connection timed out.Please try later", "Alert ");
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showMessageDialogue("Network connection timed out.Please try later", "Alert ");
                    }
                }
        );
        jsonObjectRequest_issue.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                0,
                0));
        requestQueue_issue.add(jsonObjectRequest_issue);

    }

    private void showLog(String messageTxt, String argTitle) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_CommodityDetails.this);
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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_CommodityDetails.this);
        builder.setTitle("Alert");
        builder.setMessage(messageTxt)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onBackPressed();
                    }
                });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void displayToast_success() {
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




    public void showBar() {
        builder = new AlertDialog.Builder(RC_CommodityDetails.this);
        progressDialog = new ProgressDialog(RC_CommodityDetails.this);
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

    private void displayToast() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RC_CommodityDetails.this);
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

    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RC_CommodityDetails.this);
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
    @Override
    public void onBackPressed() {

        Intent dashboard = new Intent(getBaseContext(), IMPDSActivity.class);
        dashboard.addCategory(Intent.CATEGORY_HOME);
        dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboard);
        RC_CommodityDetails.this.finish();
    }
}