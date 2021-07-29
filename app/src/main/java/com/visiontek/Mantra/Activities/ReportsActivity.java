package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;


import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.diableMenu;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class ReportsActivity extends BaseActivity {

    Button daily_report, stock_report, back;
    Context context;

    @Override
    public void initialize() {
        try {

            context = ReportsActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_reports, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

        if (diableMenu("getDailyReport")) {
            daily_report.setEnabled(false);
        }

        if (diableMenu("getStockReportDetails")) {
            stock_report.setEnabled(false);
        }

        daily_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                Intent daily = new Intent(context, DailySalesReportActivity.class);
                startActivity(daily);
            }
        });

        stock_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                Intent stock = new Intent(context, StockReportActivity.class);
                startActivity(stock);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                finish();
            }
        });
        }catch (Exception exception) {

            exception.printStackTrace();
        }
    }

    @Override
    public void initializeControls() {
        daily_report = findViewById(R.id.btn_dailysales_report);
        stock_report = findViewById(R.id.btn_stock_report);
        back = findViewById(R.id.btn_back);
        toolbarActivity.setText(context.getResources().getString(R.string.REPORTS));
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
