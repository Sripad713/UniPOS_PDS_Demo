package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
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
import com.visiontek.Mantra.Models.bean.ImpdsBean;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.ConnectivityReceiver;
import com.visiontek.Mantra.Utils.Util;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class IMPDSActivity extends BaseActivity {
    Button btn_submit,btn_back;
    EditText ed_rc_id;
    ConnectivityReceiver connectivityReceiver;
    private String st_ed_rc_id;
    String st_rd_type="R";
    RadioGroup rg_type;
    RadioButton rd_type_ration, rd_type_uid;
    TextView rc_label;
    Context context;


    @Override
    public void initialize() {
        try{
            context=IMPDSActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_i_m_p_d_s, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.toolbar));//33A1C9
        }


        connectivityReceiver = new ConnectivityReceiver();
        rc_label = findViewById(R.id.rc_label);
        rg_type=findViewById(R.id.rg_type);
        rd_type_uid=findViewById(R.id.rd_type_uid);
        rd_type_ration=findViewById(R.id.rd_type_ration);

        rg_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                if (arg1 == R.id.rd_type_ration) {
                    st_rd_type = "R";
                    rc_label.setText(context.getResources().getString(R.string.RC_No));
                    rc_label.setHint(context.getResources().getString(R.string.Please_Enter_RC_Number));
                }
                if (arg1 == R.id.rd_type_uid) {
                    st_rd_type = "U";
                    rc_label.setText(context.getResources().getString(R.string.Aadhaar_No));
                    rc_label.setHint(context.getResources().getString(R.string.Enter_UID));
                }
            }
        });

        ed_rc_id=findViewById(R.id.ed_rc_id);
        btn_submit=findViewById(R.id.btn_submit);
        btn_submit.setEnabled(true);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_submit.setEnabled(false);
                preventTwoClick(v);
                if (ed_rc_id.getText().toString().isEmpty()) {
                    if ( st_rd_type.equals("U")){
                        show_AlertDialog(
                                context.getResources().getString(R.string.Aadhaar),
                                context.getResources().getString(R.string.Invalid_UID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_UID)
                                ,0);

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.RC_No),
                                context.getResources().getString(R.string.Invalid_ID),
                                context.getResources().getString(R.string.Please_Enter_a_Valid_Number_RC),
                                0);

                    }

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

                }

            }
        });
        btn_submit.setEnabled(true);
        btn_back=findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                finish();
            }
        });
    } catch (Exception ex) {

        Timber.tag("Home-onCreate-").e(ex.getMessage(), "");
    }
    }

    @Override
    public void initializeControls() {
        toolbarActivity.setText(context.getResources().getString(R.string.IMPDS));
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
    public void onDestroy() {
        super.onDestroy();

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
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


}