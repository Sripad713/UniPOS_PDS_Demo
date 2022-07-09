package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;

import com.visiontek.Mantra.R;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class Device_Update extends BaseActivity  {

    Button TMS,USB,FTP;
    public boolean isStoragePermissionGranted() {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    return true;
                } else {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return false;
                }
            }
        } catch (Exception ex) {
            Timber.tag("DeviceUpdate-Prmsn-").e(ex.getMessage(), "");
        }
        return true;
    }
    Intent i;

    @Override
    public void initialize() {
        try {
            context = Device_Update.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_device__update, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


            isStoragePermissionGranted();


            USB.setOnClickListener(view -> {
                preventTwoClick(view);
                i=new Intent(context,USBActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });


            FTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    i=new Intent(context, FTPActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);


                    // if (dealerConstants.fpsCommonInfo.versionUpdateRequired.equals("Y")) {
               /* }else {
                    show_error_box("No Updated Apk Found","Update Version");
                }*/
                }
            });



            TMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    i=new Intent(context,RHMSActivity.class);
                    i.putExtra("FLAG","F");
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                }
            });
            Button back = findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        } catch (Exception ex) {

            Timber.tag("DeviceUpdate-onCreate-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void initializeControls() {
        FTP = findViewById(R.id.gprs);

        USB = findViewById(R.id.usb);

        TMS = findViewById(R.id.tms);
        toolbarActivity.setText(context.getResources().getString(R.string.UPDATE));

    }
}