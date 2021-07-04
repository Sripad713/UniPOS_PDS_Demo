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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class IssueActivity extends AppCompatActivity {
    Button cash, cashless, impds, back;
    Context context;
    TextView rd;
    ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        try {

        context = IssueActivity.this;

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_AlertDialog(
                    context.getResources().getString(R.string.Issue),
                    context.getResources().getString(R.string.RD_Service),
                    context.getResources().getString(R.string.RD_Service_Msg),0);
            return;
        }

        initilisation();

        if (diableMenu("getCommodityTransaction")) {
            cash.setEnabled(false);
            }
    /*    if (diableMenu("impdsMEService")) {
            impds.setEnabled(false);
        }*/

        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                Intent cash = new Intent(IssueActivity.this, CashPDSActivity.class);
                cash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cash);


            }
        });
        cashless.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                Util.toast(IssueActivity.this, "Cashless PDS function is Not Available");
            }
        });
        impds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                Intent cash = new Intent(IssueActivity.this, IMPDSActivity.class);
                cash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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

            Timber.tag("Issue-onCreate-").e(ex.getMessage(),"");
        }
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        cash = findViewById(R.id.btn_cash_pds);
        cashless = findViewById(R.id.btn_cashless_pds);
        impds = findViewById(R.id.btn_impds);
        back = findViewById(R.id.btn_back);
        toolbarInitilisation();
    }

    private void toolbarInitilisation() {
        try {

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

        toolbarFpsid.setText("Fps ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText( context.getResources().getString(R.string.ISSUE_GOODS));

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
        }catch (Exception ex){
            Timber.tag("Issue-Toolbar-").e(ex.getMessage(),"");
        }
    }

    private void show_Dialogbox(String msg,String header) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialogbox);
        Button back = (Button) dialog.findViewById(R.id.dialogcancel);
        Button confirm = (Button) dialog.findViewById(R.id.dialogok);
        TextView head = (TextView) dialog.findViewById(R.id.dialoghead);
        TextView status = (TextView) dialog.findViewById(R.id.dialogtext);
        head.setText(header);
        status.setText(msg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    private void show_AlertDialog(String headermsg,String bodymsg,String talemsg,int i) {

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
    private void SessionAlert(String headermsg, String bodymsg,String talemsg) {
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
                Intent i = new Intent(context, StartActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }
        });

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
