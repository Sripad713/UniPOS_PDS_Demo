package com.visiontek.Mantra.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
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

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;


public class DeviceActivity extends AppCompatActivity {
    int opt;
    Context context;
    Button setlang,back;
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        context = DeviceActivity.this;
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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initilisation() {
        back=findViewById(R.id.back);
        toolbarInitilisation();
    }

    private void function1() {
        String[] items = new String[]{context.getString(R.string.Set_Language),context.getString(R.string.english), context.getString(R.string.hindi)};
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
    }

    private void function(int position) {

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
    }
    public void setLocal(String lang) {
        if (lang != null) {
            Locale locale = new Locale(lang);
            System.out.println("++++++++++++++++++++++++++++++++SET" + lang);
            Locale.setDefault(locale);
            Configuration con = new Configuration();
            con.locale = locale;
            getBaseContext().getResources().updateConfiguration(con, getBaseContext().getResources().getDisplayMetrics());
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
        TextView toolbarVersion = findViewById(R.id.toolbarVersion);
        TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
        TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
        TextView toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
        TextView toolbarActivity = findViewById(R.id.toolbarActivity);
        TextView toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
        TextView toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);

        String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
        System.out.println(appversion);
        toolbarVersion.setText("Version : " + appversion);


        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("DEVICE");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}