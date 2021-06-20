package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.MyFTPClientFunctions;
import com.visiontek.Mantra.Utils.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;


public class Device_Update extends AppCompatActivity {
    float appVersion = 0;
    Button usb, gprs;
    Context context;
    String something;
    MyFTPClientFunctions ftpclient = new MyFTPClientFunctions();
    ProgressDialog pd;
    String
            fHostName = "115.111.229.10",
            fUserName = "rnd",
            fPassword = "rnd123",
            Source = "/" + fUserName + "/apk/",
            FTP_file = "",
            Download = "",
            Device_Download_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device__update);
        context = Device_Update.this;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            System.out.println("@@Version: "+version);
            appVersion = Float.parseFloat(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

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
        isStoragePermissionGranted();

        Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/"+FTP_file;
        System.out.println("@@Device download path: "+Device_Download_path);

        usb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_error_box(context.getResources().getString(R.string.Connect_your_device_to_the_usb_Port), context.getResources().getString(R.string.USB_Connection));
            }
        });


        gprs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dealerConstants.fpsCommonInfo.versionUpdateRequired.equals("Y")) {
                    if (Util.networkConnected(context)) {
                        filesize();
                    } else {
                        show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection));
                    }
                }else {
                    show_error_box("No Updated Apk Found","Update Version");
                }
            }
        });

    }

    private void initilisation() {
        gprs = findViewById(R.id.gprs);
        usb = findViewById(R.id.usb);
        toolbarInitilisation();
    }

    private void filesize() {
        try {
            pd = ProgressDialog.show(context, "", context.getResources().getString(R.string.Processing), false, false);
            new Thread(new Runnable() {
                public void run() {
                    FTP_file = (ftpclient.Ffinding(fHostName, fUserName, fPassword, Source));
                    if (FTP_file.equalsIgnoreCase("NOFILE") || (FTP_file.equalsIgnoreCase("EXCEPTION"))) {
                        something="File Not Found";
                        handler.sendEmptyMessage(4);
                    } else {
                        float version = Float.parseFloat(FTP_file.substring(10,13));
                        System.out.println("@@Downloaded version");
                        if(version > appVersion)
                        {
                            System.out.println("@@Proceed to install");
                            Download = Source + FTP_file;
                            Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/"+FTP_file;
                            handler.sendEmptyMessage(2);
                        }else{
                            System.out.println("@@No need to install");
                            something = "No updates";
                            handler.sendEmptyMessage(5);
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            something = context.getResources().getString(R.string.ERROR_IN_GETTING_FILE);
            handler.sendEmptyMessage(4);
        }
    }

    private void fdownload() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean download = ftpclient.ftpDownload(Download, Device_Download_path);
                    if (download) {
                        handler.sendEmptyMessage(3);
                    } else {
                        something = context.getResources().getString(R.string.Download_Failed);
                        handler.sendEmptyMessage(4);
                    }
                }
            }).start();
        } catch (Exception e) {
            something = context.getResources().getString(R.string.ERROR_IN_DOWNLOAD) + e.toString();
            handler.sendEmptyMessage(4);
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.println("@@Permission granted");
                return true;
            } else {
                System.out.println("@@Permission revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            System.out.println("@@Permission granted");
            return true;
        }
    }


    private void install() {
        boolean isNonPlayAppAllowed = false;
        try {
            isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (!isNonPlayAppAllowed) {
            System.out.println("Unknown Resource file is not allowed to install");
            startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/"+FTP_file);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }

        startActivity(intent);
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (msg.what == 2) {
                if (Util.networkConnected(context)) {
                    fdownload();
                } else {
                    show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection));
                }
            } else if (msg.what == 3) {
                install();
            }  else if (msg.what == 4) {
                show_error_box(something,context.getResources().getString(R.string.Something_went_Wrong_Please_Try_Again));
            } else if (msg.what == 5) {
                show_error_box(something,"No Update Found");
            } else {
                something = context.getResources().getString(R.string.UNKNOWN_ERROR);
                System.out.println(something);
                Toast.makeText(context, something, Toast.LENGTH_SHORT).show();
            }

        }
    };
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

        toolbarFpsid.setText("DeviceID");
        toolbarFpsidValue.setText(DEVICEID);

        toolbarActivity.setText(context.getResources().getText(R.string.Start));

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}