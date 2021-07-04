package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.DatabaseHelper;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class DeviceActivity extends AppCompatActivity {

    Context context;
    Button  back,log;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        context = DeviceActivity.this;
        try {

        db = new DatabaseHelper(context);

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg),
                    context.getResources().getString(R.string.RD_Service));
            return;
        }

        initilisation();

        function1();

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

            Timber.tag("Device-onCreate-").e(ex.getMessage(),"");
        }
    }

    private void initilisation() {
        back = findViewById(R.id.back);
        log = findViewById(R.id.log);
        TextView deviceid = findViewById(R.id.deviceid);
        deviceid.setText(DEVICEID);
        toolbarInitilisation();
    }

    private void function1() {
        try {

        String[] items = new String[]{context.getString(R.string.Language), context.getString(R.string.english), context.getString(R.string.hindi)};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        Spinner select = null;
        select = findViewById(R.id.select);
        select.setAdapter(adapter1);
        select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                function(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        }catch (Exception ex){

            Timber.tag("Device-function1-").e(ex.getMessage(),"");
        }
    }

    private void function(int position) {
        try {

        if (position == 2) {
            L = "hi";
            if (db.get_L("Language").isEmpty()) {
                db.insert_L("Language", L);
            } else {
                db.update_L("Language", L);
            }
            setLocal(L);
            finish();

        } else if (position == 1) {
            L = "en";
            if (db.get_L("Language").isEmpty()) {
                db.insert_L("Language", L);
            } else {
                db.update_L("Language", L);
            }
            setLocal(L);
            finish();

        }
        }catch (Exception ex){
            Timber.tag("Device-function-").e(ex.getMessage(),"");
        }
    }

    public void setLocal(String lang) {
        try {

        if (lang != null) {
            Locale locale = new Locale(lang);
            System.out.println("++++++++++++++++++++++++++++++++SET" + lang);
            Locale.setDefault(locale);
            Configuration con = new Configuration();
            con.locale = locale;
            getBaseContext().getResources().updateConfiguration(con, getBaseContext().getResources().getDisplayMetrics());
        }
        }catch (Exception ex){

            Timber.tag("Device-SetLocal-").e(ex.getMessage(),"");
        }
    }

    private void show_error_box(String msg, String title) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
        toolbarFpsid.setText("DEVICE ID");
        toolbarFpsidValue.setText(DEVICEID);
        //toolbarFpsid.setText("FPS ID");
//        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText( context.getResources().getString(R.string.DEVICE));

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {
            Timber.tag("Device-Toolbar-").e(ex.getMessage(), "");
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

}