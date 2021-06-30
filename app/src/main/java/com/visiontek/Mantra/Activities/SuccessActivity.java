package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.ScrollTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.visiontek.Mantra.Utils.Util.networkConnected;

public class SuccessActivity extends AppCompatActivity {
    private int selectedIndex = -1;
    public int k = 1;
    TableLayout tableLayout;
    TableRow tableRow;
    private AlertDialog.Builder builder;
    private TextView txt1, txt2, txt3, txt4, txt5;
    //ImpdsBean impdsBean;
    ProgressDialog progressDialog;
    private String rcid, district_name, home_state_name, sale_fps_id, alloc_month, alloc_year,
            uid_refer_no, totalamount, memberName, sale_state_name, home_dist_name, sale_dist_name,
            receiptId, scheme_name;

    JSONObject response = null;
    String[] CMonths = {"January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"};
    NumberFormat rsformat = new DecimalFormat("#0.00");
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        context=SuccessActivity.this;



        if (!networkConnected(context)) {

            builder.setTitle("Internet Connection");
            builder.setMessage("Please Check Your Internet Connection").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.toolbar));//33A1C9
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String trxdate = dateFormat.format(date);

        tableLayout = findViewById(R.id.commodity_table);
        tableLayout.setStretchAllColumns(true);


        showBar();

        try {
            // impdsBean = ImpdsBean.getInstance();
            Intent intent = getIntent();
            String jsonObject1 = intent.getStringExtra("saleresponse");
            response = new JSONObject(jsonObject1);
            String vendor = ImpdsBean.getInstance().getVendor();
            System.out.println(vendor+"---response_queue===" + response.toString());

            progressDialog.dismiss();
            if(response == null || vendor == null){
                logout();
            }

            home_state_name = ImpdsBean.getInstance().getHomeStateName();
            sale_state_name = ImpdsBean.getInstance().getSaleStateName();
            home_dist_name = ImpdsBean.getInstance().getHomeDistName();
            sale_dist_name = ImpdsBean.getInstance().getSaleDistName();
            scheme_name = ImpdsBean.getInstance().getSchemeName();
            totalamount = ImpdsBean.getInstance().getTotal_amount();
            alloc_month = ImpdsBean.getInstance().getAllocation_month();
            alloc_year = ImpdsBean.getInstance().getAllocation_year();

            rcid = response.getString("rcId");
            receiptId = response.getString("receiptId");
            uid_refer_no = response.getString("uidRefNumber");
            sale_fps_id = response.getString("saleFpsId");
            memberName = response.getString("memberName");


            TextView home_state_text = findViewById(R.id.home_state_text);
            TextView text_dist_name = findViewById(R.id.home_dist_name);
            TextView sale_state_text = findViewById(R.id.sale_state_name);
            TextView sale_dist_text = findViewById(R.id.sale_dist_name);
            TextView sale_fps_text = findViewById(R.id.text_fps_id);
            TextView member_name = findViewById(R.id.text_member_name);
            TextView rc_id = findViewById(R.id.text_card_id);
            TextView ref_id = findViewById(R.id.text_ref_id);
            TextView trx_date = findViewById(R.id.text_trx_date);
            TextView text_alloc_month = findViewById(R.id.text_alloc_month);
            TextView text_alloc_year = findViewById(R.id.text_alloc_year);
            TextView rct_id = findViewById(R.id.text_rec_id);

            TextView total_amount = findViewById(R.id.text_total_amount);

            home_state_text.setText(home_state_name);
            sale_state_text.setText(sale_state_name);
            text_dist_name.setText(home_dist_name);
            sale_dist_text.setText(sale_dist_name);
            sale_fps_text.setText(sale_fps_id);
            member_name.setText(memberName);
            rc_id.setText(rcid + "(" + scheme_name + ")");
            ref_id.setText(uid_refer_no);

            rct_id.setText(receiptId);
            text_alloc_month.setText(CMonths[Integer.parseInt(alloc_month) - 1]);
            text_alloc_year.setText(alloc_year);
            total_amount.setText(rsformat.format(Double.parseDouble(totalamount)));

            JSONArray transactionList = response.getJSONArray("transactionList");
            // JSONArray transactionList = response.getJSONArray("transactionList");

            if (transactionList != null && transactionList.length() > 0) {
                for (int i = 0; i < transactionList.length(); i++) {
                    JSONObject object = transactionList.getJSONObject(i);

                    String commodityName = object.getString("commodityName");
                    String availedQuantity = object.getString("availedQuantity");
                    String amount = object.getString("amount");
                    String totalQuantity = object.getString("totalQuantity");
                    String price = object.getString("pricePerKg");

                    txt1 = new TextView(SuccessActivity.this);
                    txt2 = new TextView(SuccessActivity.this);
                    txt3 = new TextView(SuccessActivity.this);
                    txt4 = new TextView(SuccessActivity.this);
                    txt5 = new TextView(SuccessActivity.this);

                    txt1.setText(commodityName);
                    txt1.setPadding(8, 8, 8, 8);
                    txt1.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt1.setGravity(Gravity.CENTER);
                    txt1.setTextSize(18);
                    txt1.setTextColor(Color.parseColor("#000000"));

                    txt2.setText(totalQuantity);
                    txt2.setPadding(8, 8, 8, 8);
                    txt2.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt2.setGravity(Gravity.CENTER);
                    txt2.setTextSize(18);
                    txt2.setTextColor(Color.parseColor("#000000"));

                    txt3.setText(availedQuantity);
                    txt3.setPadding(8, 8, 8, 8);
                    txt3.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt3.setGravity(Gravity.CENTER);
                    txt3.setTextSize(18);
                    txt3.setTextColor(Color.parseColor("#000000"));

                    txt4.setText(rsformat.format(Double.parseDouble(price)));
                    txt4.setPadding(8, 8, 8, 8);
                    txt4.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt4.setGravity(Gravity.CENTER);
                    txt4.setTextSize(18);
                    txt4.setTextColor(Color.parseColor("#000000"));

                    txt5.setText(rsformat.format(Double.parseDouble(amount)));
                    txt5.setPadding(8, 8, 8, 8);
                    txt5.setBackgroundResource(R.drawable.ben_table_cell_shape);
                    txt5.setGravity(Gravity.CENTER);
                    txt5.setTextSize(18);
                    txt5.setTextColor(Color.parseColor("#000000"));


                    tableRow = new TableRow(SuccessActivity.this);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams
                            (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tableRow.setLayoutParams(layoutParams);

                    tableRow.addView(txt1);
                    tableRow.addView(txt2);
                    tableRow.addView(txt3);
                    tableRow.addView(txt4);
                    tableRow.addView(txt5);

                    tableLayout.addView(tableRow, k);
                    k++;

                }
            }

        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            e.printStackTrace();
            showMessageDialogue();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
//        location.beginUpdates();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }




    private void showMessageDialogue() {
        new AlertDialog.Builder(SuccessActivity.this)
                .setCancelable(false)
                .setTitle("Alert")
                .setMessage("Unable to fetch receipt details!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  dialog.cancel();
                        onBackPressed();
                    }
                })
                .show();
    }

    private void displayToast() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SuccessActivity.this);
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
        SuccessActivity.this.finish();
        super.onBackPressed();
    }

    public void showBar() {
        builder = new AlertDialog.Builder(SuccessActivity.this);
        progressDialog = new ProgressDialog(SuccessActivity.this);
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

    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SuccessActivity.this);
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
}