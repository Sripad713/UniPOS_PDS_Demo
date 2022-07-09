package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.txnType;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class IssueActivity extends BaseActivity {
    Button cash, impds, back;
    Context context;
    DatabaseHelper databaseHelper;
    ProgressDialog pd = null;

    @Override
    public void initialize() {
        try {
            System.out.println("@@In initialize");
            context = IssueActivity.this;
            databaseHelper = new DatabaseHelper(context);
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_issue, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

        if (diableMenu("getCommodityTransaction")) {
            cash.setEnabled(false);
        }
        if (diableMenu("impdsMEService")) {
            impds.setEnabled(false);
        }

            cash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent cash = new Intent(IssueActivity.this, CashPDSActivity.class);
                    cash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(cash);


                }
            });

            impds.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent cash = new Intent(IssueActivity.this, IMPDSActivity.class);
                    startActivity(cash);

                    //  Util.toast(IssueActivity.this, "IMPDS function is Not Available");
                }
            });
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        }catch (Exception ex){
            System.out.println("@@Exception(initializze): " +ex.toString());
            Timber.tag("Issue-onCreate-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initializeControls() {
        System.out.println("@@In initializeControls of IssueActivity");
        pd = new ProgressDialog(context);
        cash = findViewById(R.id.btn_cash_pds);
        impds = findViewById(R.id.btn_impds);
        back = findViewById(R.id.btn_back);
        toolbarActivity.setText(context.getResources().getString(R.string.ISSUE));
        toolbarFpsid.setText("FPS ID");
        //toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        if(dealerConstants == null || dealerConstants.stateBean == null || dealerConstants.stateBean.statefpsId==null)
        {
            System.out.println("@@NULL");
            ArrayList<String> statefpsiD = databaseHelper.getStateDetails();
            toolbarFpsidValue.setText(statefpsiD.get(6));
        }else {
            System.out.println("@@Setting val");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        }

        if(txnType == -1){
            System.out.println("@@Offline transaction making IMPDS disable");
            impds.setVisibility(View.INVISIBLE);
        }else{
            System.out.println("@@Online transaction");
            impds.setVisibility(View.VISIBLE);
        }
    }


    public void Dismiss(){
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }
    public void Show(String msg,String title){
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
