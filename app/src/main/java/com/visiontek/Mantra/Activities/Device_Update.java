package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.core.content.FileProvider;

import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.MyFTPClientFunctions;
import com.visiontek.Mantra.Utils.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Utils.Util.RDservice;


public class Device_Update extends AppCompatActivity {

    Button usb, gprs;
    Context context;
    String something;
    MyFTPClientFunctions ftpclient = new MyFTPClientFunctions();
    ProgressDialog pd;
    String
            fHostName = "115.111.229.10",
            fUserName = "NEPDS",
            fPassword = "mktg",
            Source = "/" + fUserName + "/",
            FTP_file = "MantraPDS.apk",
            Download = Source + FTP_file,
            Destination,
            Device_Download_path;
    AlertDialog falert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device__update);
        context = Device_Update.this;


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
        Device_Download_path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/");
        Destination = Device_Download_path + FTP_file;
        usb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_error_box(context.getResources().getString(R.string.Connect_your_device_to_the_usb_Port), context.getResources().getString(R.string.USB_Connection));
            }
        });
        gprs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.networkConnected(context)) {
                    filesize();
                } else {
                    show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection));
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
                    boolean find = (ftpclient.Ffinding(fHostName, fUserName, fPassword, Source, FTP_file));
                    if (find) {
                        handler.sendEmptyMessage(2);
                    } else {
                        something="File Not Found";
                        handler.sendEmptyMessage(4);
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
                    boolean download = ftpclient.ftpDownload(Download, Destination);
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

        File file = new File("/sdcard/Download/", FTP_file); // assume refers to "sdcard/myapp_folder/myapp.apk"
        Uri fileUri = Uri.fromFile(file); //for Build.VERSION.SDK_INT <= 2
        if (Build.VERSION.SDK_INT >= 24) {

            fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        }
        System.out.println(fileUri);
        Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //dont forget add this line
        context.startActivity(intent);

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
                System.out.println(something);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getResources().getString(R.string.Something_went_Wrong_Please_Try_Again))
                        .setCancelable(false)
                        .setNegativeButton(context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                falert = builder.create();
                falert.setTitle(something);
                falert.show();
                falert.setCancelable(false);
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
