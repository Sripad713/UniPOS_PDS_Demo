package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.DeviceInfoListAdapter;
import com.visiontek.Mantra.Models.DATAModels.DeviceInfoListModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import timber.log.Timber;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class DeviceInfoActivity extends BaseActivity {
    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    ArrayList<DeviceInfoListModel> arraydata;
    Context context;

    public String readKernelVersion() {

        try {
            Process p = Runtime.getRuntime().exec("uname -a");
            InputStream is = null;
            if (p.waitFor() == 0) {
                is = p.getInputStream();
            } else {
                is = p.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            br.close();
            return line;
        } catch (Exception ex) {
            System.out.println("Main : readKernelVersion : Exception : " + ex);
            return "ERROR: " + ex.getMessage();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void saveInfo(Context context) {
        try {

        ArrayList<String> value = new ArrayList<>();
        value.add("Device");
        value.add("Manufacture");
        value.add("Model");
        value.add("SecurityPatch");
        value.add("Release");
        value.add("SDK");
        value.add("Build No");
        value.add("Baseband");
        value.add("Kernel");
        value.add("Application");

        ArrayList<String> info = new ArrayList<>();
        info.add(Build.DEVICE);
        info.add(Build.MANUFACTURER);
        info.add(Build.MODEL);
        info.add(Build.VERSION.SECURITY_PATCH);
        info.add(Build.VERSION.RELEASE);
        info.add(Build.VERSION.SDK);
        info.add(Build.DISPLAY);
        info.add(Build.getRadioVersion());
        info.add(readKernelVersion());
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        System.out.println("@@Version: " + version);
        float appVersion = Float.parseFloat(version);
        info.add("MantraPDS_"+appVersion);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        arraydata = new ArrayList<>();

        for (int i = 3; i < value.size(); i++) {
            arraydata.add(new DeviceInfoListModel(value.get(i), info.get(i).trim()));
        }
        adapter = new DeviceInfoListAdapter(context, arraydata);
        recyclerView.setAdapter(adapter);
         }catch (Exception ex){

            Timber.tag("DeviceInfo-onCreate-").e(ex.getMessage(),"");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initialize() {
        try {
            context = DeviceInfoActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_device__info, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();



            saveInfo(context);
            Button back=findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        }catch (Exception ex){

            Timber.tag("DeviceInfo-onCreate-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initializeControls() {
        toolbarActivity.setText(context.getResources().getString(R.string.DEVICE_INFO));
    }

}