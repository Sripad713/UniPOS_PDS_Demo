package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import timber.log.Timber;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class AadhaarServicesActivity extends BaseActivity {
    Button back,
           // uid,
            beneficiary;
    Context context;

    @Override
    public void initialize() {
        try {

            context = AadhaarServicesActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_aadhar__services, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


        /*if (diableMenu(context, 4)) {
            uid.setVisibility(View.INVISIBLE);
            uid.setEnabled(false);
        }*/

            if (diableMenu("beneficiaryVerificationDetails")) {
                beneficiary.setEnabled(false);
            }

            beneficiary.setOnClickListener(view -> {
                preventTwoClick(view);
                Intent ben = new Intent(context, BeneficiaryVerificationActivity.class);
                startActivity(ben);
            });
            back.setOnClickListener(view -> {
                preventTwoClick(view);
                finish();
            });
        }catch (Exception ex){
            Timber.tag("AadharService-OnCreate-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initializeControls() {
        /*uid = findViewById(R.id.btn_uid_seeding);*/
        beneficiary = findViewById(R.id.btn_beneficiaryverification);
        back = findViewById(R.id.btn_back);
        toolbarActivity.setText(context.getResources().getString(R.string.AADHAAR_SERVICES));
    }
}
