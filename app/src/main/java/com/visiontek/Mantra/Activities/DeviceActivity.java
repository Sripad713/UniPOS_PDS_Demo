package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class DeviceActivity extends BaseActivity {

    Context context;
    Button  back,log;

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

            SharedPref SharedPref = com.visiontek.Mantra.Utils.SharedPref.getInstance(context);
            if (position == 2) {
                L = "hi";

                SharedPref.saveData("LANG", "hi");

                setLocal(L);
                finish();

            } else if (position == 1) {
                L = "en";

                SharedPref.saveData("LANG", "en");

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

            Locale.setDefault(locale);
            Configuration con = new Configuration();
            con.locale = locale;
            getBaseContext().getResources().updateConfiguration(con, getBaseContext().getResources().getDisplayMetrics());
        }
        }catch (Exception ex){

            Timber.tag("Device-SetLocal-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initialize() {
        try {
            context = DeviceActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_device, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


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

    @Override
    public void initializeControls() {
        back = findViewById(R.id.back);
        log = findViewById(R.id.log);
        TextView deviceid = findViewById(R.id.deviceid);
        deviceid.setText(DEVICEID);
        toolbarActivity.setText(context.getResources().getString(R.string.DEVICE));
    }
}