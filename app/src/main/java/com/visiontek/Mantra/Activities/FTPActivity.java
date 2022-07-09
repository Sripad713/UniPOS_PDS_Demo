package com.visiontek.Mantra.Activities;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.MyFTPClientFunctions;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.Util;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class FTPActivity extends BaseActivity {

    float appVersion = 0;
    Button PDS,RD,Misc;

    Context context;
    String something;
    MyFTPClientFunctions ftpclient = new MyFTPClientFunctions();
    ProgressDialog pd;

    String
            fHostName = "115.111.229.10",
            fUserName = "mktg",
            fPassword = "mktg",

    FTP_file = "",
            Download = "",
            Device_Download_path;
    String BasicPath="/" + fUserName + "/Android/CGPDS/";
    private void filesize(int type,String PATH) {
        try {
            Show(context.getResources().getString(R.string.Processing),
                    context.getResources().getString(R.string.Please_wait));

            new Thread(new Runnable() {
                public void run() {
                    FTP_file = (ftpclient.Ffinding(fHostName, fUserName, fPassword, PATH,type));
                    if (FTP_file.equalsIgnoreCase("NOFILE") || (FTP_file.equalsIgnoreCase("EXCEPTION"))) {
                        something = context.getResources().getString(R.string.No_Update_Found);
                        //System.out.println("NOOOOOOOO");
                        handler.sendEmptyMessage(4);
                    } else {
                        System.out.println(FTP_file);
                        if (type==1) {
                            float version = Float.parseFloat(FTP_file.substring(10, 13));
                            System.out.println("VERSION>>>"+version);
                            if (version > appVersion) {
                                Download = PATH + FTP_file;
                                Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                handler.sendEmptyMessage(2);
                            } else {
                                something = context.getResources().getString(R.string.No_Update_Found);
                                handler.sendEmptyMessage(5);
                            }
                        }else if (type==2){

                            if (!Util.RDservice(context)){
                                Download = PATH + FTP_file;
                                Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                handler.sendEmptyMessage(2);
                            }else {
                                SharedPref sharedPref=SharedPref.getInstance(context);
                                String rd_vr=sharedPref.getData("RD");
                                if (rd_vr.length()>1){

                                    String[] arry1 = rd_vr.split("\\.");
                                    int j1= Integer.parseInt(arry1[0]);
                                    int k1= Integer.parseInt(arry1[1]);
                                    int l1= Integer.parseInt(arry1[2]);
                                    String vr_rd_String = FTP_file.substring(10, 15);
                                    String[] arry2 = vr_rd_String.split("\\.");
                                    int j2= Integer.parseInt(arry2[0]);
                                    int k2= Integer.parseInt(arry2[1]);
                                    int l2= Integer.parseInt(arry2[2]);

                                    if (j1==j2){
                                        if (k1==k2){
                                            if (l1==l2){
                                                something = context.getResources().getString(R.string.No_Update_Found);
                                                handler.sendEmptyMessage(5);
                                            }else if(l1<l2){
                                                Download = PATH + FTP_file;
                                                Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                                handler.sendEmptyMessage(2);
                                            }else {
                                                something = context.getResources().getString(R.string.No_Update_Found);
                                                handler.sendEmptyMessage(5);
                                            }
                                        }else if (k1<k2){
                                            Download = PATH + FTP_file;
                                            Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                            handler.sendEmptyMessage(2);
                                        }else {
                                            something = context.getResources().getString(R.string.No_Update_Found);
                                            handler.sendEmptyMessage(5);
                                        }

                                    }else if (j1<j2){
                                        Download = PATH + FTP_file;
                                        Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                        handler.sendEmptyMessage(2);
                                    }else {
                                        something = context.getResources().getString(R.string.No_Update_Found);
                                        handler.sendEmptyMessage(5);
                                    }
                                }else {
                                    Download = PATH + FTP_file;
                                    Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                    handler.sendEmptyMessage(2);
                                }
                            }
                               /* if (deviceRDvr<serverRDver) {
                                    Download = PATH + FTP_file;
                                    Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                                    handler.sendEmptyMessage(2);
                                }else {
                                    something = context.getResources().getString(R.string.No_Update_Found);
                                    handler.sendEmptyMessage(5);
                                }*/
                        }else if (type==3){
                            Download = PATH + FTP_file;
                            Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                            handler.sendEmptyMessage(2);
                        }

                    }
                }
            }).start();
        } catch (Exception e) {
            something = context.getResources().getString(R.string.ERROR_IN_GETTING_FILE);
            handler.sendEmptyMessage(4);
            Timber.tag("DeviceUpdate-FileName-").e(e.getMessage(), "");
        }

    }

    private void fdownload() {

        try {
            Show(context.getResources().getString(R.string.Downloading_APK),
                    context.getResources().getString(R.string.Please_wait));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean download = ftpclient.ftpDownload(Download, Device_Download_path);
                    if (download) {
                        something = context.getResources().getString(R.string.Download_Completed);
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
            Timber.tag("DeviceUpdate-Fdownld-").e(e.getMessage(), "");
        }

    }

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


    private void install() {

        boolean isNonPlayAppAllowed = false;
        try {
            isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;

            if (!isNonPlayAppAllowed) {

                startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file);
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
            finish();
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            Timber.tag("DeviceUpdate-onCreate-").e(e.getMessage(), "");
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            Dismiss();
            if (msg.what == 2) {
                if (Util.networkConnected(context)) {
                    fdownload();
                } else {
                    show_AlertDialog(context.getResources().getString(R.string.FTP),
                            context.getResources().getString(R.string.Internet_Connection),
                            context.getResources().getString(R.string.Internet_Connection_Msg),
                            0);
                }
            } else if (msg.what == 3) {
                show_AlertDialog(context.getResources().getString(R.string.FTP),
                        something,
                        context.getResources().getString(R.string.UPDATE),
                        1);

            } else if (msg.what == 4) {
                show_AlertDialog(context.getResources().getString(R.string.FTP),
                        something,
                        context.getResources().getString(R.string.Please_Try_Again),
                        0);
            } else if (msg.what == 5) {
                show_AlertDialog(context.getResources().getString(R.string.FTP),
                        something,
                        "",
                        0);

            } else {
                something = context.getResources().getString(R.string.UNKNOWN_ERROR);
                show_AlertDialog(context.getResources().getString(R.string.Device),
                        something,
                        "",
                        0);
            }

        }
    };


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
                preventTwoClick(v);
                dialog.dismiss();
                if (i == 1) {
                    install();
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    public void Dismiss() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    public void Show(String msg, String title) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);
        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9 && resultCode == RESULT_OK) {
            something = "Application Update Success";
            finish();
        } else if (requestCode == 99){

        }else {
            something = "Application Update Failed";
        }
    }

    public String getAppVersionFromPkgName(Context context, String Packagename) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }




    @Override
    public void initialize() {
        try {
            context = FTPActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_ftpactivity, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            appVersion = Float.parseFloat(version);


            isStoragePermissionGranted();

            Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;


            PDS.setOnClickListener(view -> {
                preventTwoClick(view);
                filesize(1,BasicPath+"MantraPDS/");

            });
            RD.setOnClickListener(view -> {
                preventTwoClick(view);
                filesize(2,BasicPath+"RDService/" );

            });
            Misc.setOnClickListener(view -> {
                preventTwoClick(view);
                filesize(3,BasicPath+"MISC/");

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
        pd = new ProgressDialog(context);
        PDS = findViewById(R.id.pds);
        RD = findViewById(R.id.rdservice);
        Misc = findViewById(R.id.misc);
        toolbarActivity.setText(context.getResources().getString(R.string.FTP));
    }


}