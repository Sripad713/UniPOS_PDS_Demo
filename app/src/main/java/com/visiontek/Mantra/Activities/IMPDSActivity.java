package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.ConnectivityReceiver;
import com.visiontek.Mantra.Utils.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;

public class IMPDSActivity extends AppCompatActivity {
    Button btn_submit;
    EditText ed_rc_id;
    Intent i;
    ConnectivityReceiver connectivityReceiver;
    private AlertDialog.Builder builder;
    private String vendor,st_ed_rc_id;
    String st_rd_type="R";
    ProgressDialog progressDialog;
    RadioGroup rg_type;
    RadioButton rd_type_ration, rd_type_uid;
    TextView rc_label;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_m_p_d_s);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.toolbar));//33A1C9
        }
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        String trxdate = dateFormat.format(date);

        toolbarInitilisation();
         connectivityReceiver = new ConnectivityReceiver();
        rc_label = findViewById(R.id.rc_label);
        rg_type=findViewById(R.id.rg_type);
        rd_type_uid=findViewById(R.id.rd_type_uid);
        rd_type_ration=findViewById(R.id.rd_type_ration);

        /*vendor= ImpdsBean.getInstance().getVendor();
        if(vendor == null || vendor.length() == 0){
            logout();
        }*/

        rg_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                if (arg1 == R.id.rd_type_ration) {
                    st_rd_type = "R";
                    rc_label.setText("Ration Card No");
                    rc_label.setHint("Enter Ration Card No");
                }
                if (arg1 == R.id.rd_type_uid) {
                    st_rd_type = "U";
                    rc_label.setText("Aadhar Card No");
                    rc_label.setHint("Enter Aadhar Card No");
                }
            }
        });

        ed_rc_id=findViewById(R.id.ed_rc_id);
        //ed_rc_id.setText("066000000000");// 782971625188

        btn_submit=findViewById(R.id.btn_submit);
        btn_submit.setEnabled(true);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_submit.setEnabled(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(IMPDSActivity.this);

                if (ed_rc_id.getText().toString().isEmpty()) {
                    builder.setTitle("Alert");

                    builder.setMessage(String.valueOf("Enter Valid Ration Card No."))
                            .setCancelable(false)
                            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    btn_submit.setEnabled(true);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;

                }else {

                    st_ed_rc_id=ed_rc_id.getText().toString().trim();
                    ImpdsBean impdsbean = ImpdsBean.getInstance();
                    impdsbean.setRc_id(st_ed_rc_id);
                    impdsbean.setIdType(st_rd_type);
                    impdsbean.setSaleStateCode(dealerConstants.stateBean.stateCode);
                    impdsbean.setSaleFpsId(dealerConstants.stateBean.statefpsId);
                    impdsbean.setSaleDistCode(dealerConstants.fpsCommonInfo.distCode);
                    impdsbean.setFps_session_id(dealerConstants.fpsCommonInfo.fpsSessionId);



                    Intent dashboard = new Intent(getBaseContext(), RC_MemberDetails.class);//RC_MemberDetailsTest
                    dashboard.addCategory(Intent.CATEGORY_HOME);
                    dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dashboard);
                    IMPDSActivity.this.finish();

                    // checkLogin();
                }

            }
        });
        btn_submit.setEnabled(true);
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
        toolbarActivity.setText("IMPDS");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(IMPDSActivity.this);
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
    protected void onStart() {
        super.onStart();

        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver,filter);
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
    public void onBackPressed() {

        Intent dashboard = new Intent(getBaseContext(),IssueActivity .class);
        dashboard.addCategory(Intent.CATEGORY_HOME);
        dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboard);
        IMPDSActivity.this.finish();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}