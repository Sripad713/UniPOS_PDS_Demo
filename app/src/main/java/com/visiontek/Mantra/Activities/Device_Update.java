package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.Models.RHMS;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.MyFTPClientFunctions;
import com.visiontek.Mantra.Utils.UnzipUtil;
import com.visiontek.Mantra.Utils.Util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import timber.log.Timber;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class Device_Update extends BaseActivity  {
    float appVersion = 0;
    Button usb, gprs, TMS;
    Context context;
    String something;
    MyFTPClientFunctions ftpclient = new MyFTPClientFunctions();
    ProgressDialog pd;
    String
            fHostName = "115.111.229.10",
            fUserName = "mktg",
            fPassword = "mktg",
            Source = "/" + fUserName + "/Android/Chhattisgarh/",
            FTP_file = "",
            Download = "",
            Device_Download_path;

    //-------------------------------------------------------------------
    String serialno;

    ArrayList<RHMS> Application;
    String rhmspath = "/sdcard/";
    String appliaction_name, application_type, project_name, version_;
    String finalResponse, msg;

    private void filesize() {
        try {
            Show(context.getResources().getString(R.string.Processing),
                    context.getResources().getString(R.string.Please_wait));

            new Thread(new Runnable() {
                public void run() {
                    FTP_file = (ftpclient.Ffinding(fHostName, fUserName, fPassword, Source));
                    if (FTP_file.equalsIgnoreCase("NOFILE") || (FTP_file.equalsIgnoreCase("EXCEPTION"))) {
                        something = context.getResources().getString(R.string.No_Update_Found);
                        handler.sendEmptyMessage(4);
                    } else {
                        float version = Float.parseFloat(FTP_file.substring(10, 13));
                        if (version > appVersion) {
                            Download = Source + FTP_file;
                            Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                            handler.sendEmptyMessage(2);
                        } else {

                            something = context.getResources().getString(R.string.No_Update_Found);
                            handler.sendEmptyMessage(5);
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
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            Timber.tag("DeviceUpdate-onCreate-").e(e.getMessage(), "");
        }
    }

    private void InstallfromUSB() {
        try {

            boolean isNonPlayAppAllowed = false;

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
        } catch (Exception ex) {

            Timber.tag("DeviceUpdate-onCreate-").e(ex.getMessage(), "");
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

    private void hitit() {
        try {

            String url = "https://rhms2.visiontek.co.in/api/ApplicationStatus?serialNo=" + serialno;

            new makeservicecall().execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private class makeservicecall extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Show(context.getResources().getString(R.string.Downloading),
                    context.getResources().getString(R.string.Please_wait));
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(String... params) {
            String reqURL = params[0];
            String response = null;
            try {
                URL url = new URL(reqURL);
                URLConnection urlConnection = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
                httpConn.setRequestMethod("GET");
                try {
                    InputStream in = new BufferedInputStream(httpConn.getInputStream());
                    response = convertStreamToString(in);
                    finalResponse = response;

                } catch (Exception e) {

                    msg ="No Response for this Device";
                    e.printStackTrace();
                    System.out.println("========="+e.getLocalizedMessage());
                    return false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("==1"+e.getMessage());
                msg = e.getMessage();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Dismiss();
            if (result) {
                parseXml_dealer(finalResponse);
            } else {
                show_AlertDialog(context.getResources().getString(R.string.TMS),
                        msg,
                        "",
                        0);

            }
        }
    }

    public String convertStreamToString(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            msg = e.getMessage();
        }
        return sb.toString();
    }


    public void parseXml_dealer(String xmlString) {

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();

            RHMS rhms = new RHMS();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("ProjectName")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            rhms.ProjectName = (xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ApplicationType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.ApplicationType = (xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ApplicationName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.ApplicationName = (xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("Version")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.Version = (xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ApplicationURL")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.ApplicationURL = (xpp.getText());
                                Application.add(rhms);
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            msg = e.getMessage();

        }
        check();
    }

    private void check() {
        try {


        if (Application.size() > 0) {
            boolean app;
            String appver;
            float version, appversion;
            ArrayList<RHMS> apps = new ArrayList<>();
            RHMS rhms1 = new RHMS();
            for (int size = 0; size < Application.size(); size++) {
                File del = new File(rhmspath, Application.get(size).ApplicationName);
                if (del.exists()) {
                    deleteRecursive(new File(rhmspath + Application.get(size).ApplicationName));
                }
                Application.get(size).ApplicationType = Application.get(size).ApplicationType.replaceAll("-", ".");
                app = getApp(context, Application.get(size).ApplicationType);
                if (app) {
                    version = Float.parseFloat(Application.get(size).Version);
                    appver = getAppVersionFromPkgName(context, Application.get(size).ApplicationType);
                    appversion = Float.parseFloat(appver);
                    if (appversion < version) {
                        rhms1.ApplicationName = (Application.get(size).ApplicationName);
                        rhms1.ApplicationType = (Application.get(size).ApplicationType);
                        rhms1.Version = (Application.get(size).Version);
                        rhms1.ApplicationURL = (Application.get(size).ApplicationURL);
                        apps.add(rhms1);
                    }
                } else {
                    rhms1.ApplicationName = (Application.get(size).ApplicationName);
                    rhms1.ApplicationType = (Application.get(size).ApplicationType);
                    rhms1.Version = (Application.get(size).Version);
                    rhms1.ApplicationURL = (Application.get(size).ApplicationURL);
                    apps.add(rhms1);
                }
            }


            if (apps.size() == 1) {

                Download(apps.get(0).ApplicationURL, apps.get(0).ApplicationName, apps.get(0).Version);
            } else if (apps.size() > 1) {
                dialog(apps);
            } else {
                show_AlertDialog(context.getResources().getString(R.string.TMS),
                        context.getResources().getString(R.string.No_Update_Found),
                        "",
                        0);
            }

        } else {
            show_AlertDialog(context.getResources().getString(R.string.TMS), context.getResources().getString(R.string.No_Update_Found), "", 0);
        }
         }catch (Exception e) {
            e.printStackTrace();
            msg = e.getMessage();

        }
    }

    private void dialog(ArrayList<RHMS> apps) {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Device_Update.this);
        builderSingle.setTitle(context.getResources().getString(R.string.Please_Select_Any_Option));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Device_Update.this, android.R.layout.select_dialog_singlechoice);
        int size = apps.size();
        for (int i = 0; i < size; i++) {
            arrayAdapter.add(apps.get(i).ApplicationName);
        }
        builderSingle.setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String applicationurl = apps.get(which).ApplicationURL;
                String applicationname = apps.get(which).ApplicationName;
                String version = apps.get(which).Version;
                Download(applicationurl, applicationname, version);
            }
        });
        builderSingle.show();
    }


    private void Download(String applicationurl, String applicationname, String version) {
        File appfile = new File(rhmspath + applicationname);
        if (!appfile.isDirectory()) {
            appfile.mkdir();
        }
        appliaction_name = applicationname;
        version_ = version;
        new downloadapk().execute(applicationurl, applicationname, version, String.valueOf(appfile));
    }

    private class downloadapk extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Show(context.getResources().getString(R.string.Downloading),
                    context.getResources().getString(R.string.Please_wait));
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(String... params) {
            String applicationurl = params[0];
            String applicationname = params[1];
            String version = params[2];
            String appfile = params[3];
            applicationname = applicationname + "_" + version + ".zip";
            System.out.println(applicationurl);
            System.out.println(applicationname);
            System.out.println(version);
            System.out.println(appfile);
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(applicationurl).openConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(10000);
                conn.connect();
                InputStream input = conn.getInputStream();

                FileOutputStream fOut = new FileOutputStream(new File(appfile, applicationname));
                int byteCount = 0;
                byte[] buffer = new byte[4096];
                int bytesRead = -1;

                while ((bytesRead = input.read(buffer)) != -1) {
                    fOut.write(buffer, 0, bytesRead);
                    byteCount += bytesRead;
                }
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
                msg = e.getMessage();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            try {


            Dismiss();
            if (result) {
                msg = "Zip file Downloaded Successfully";

                String list = rhmspath + appliaction_name + "/";
                String source = rhmspath + appliaction_name + "/" + appliaction_name + "_" + version_ + ".zip";
                String destination = rhmspath + appliaction_name + "/" + appliaction_name + "_" + version_ + "/";

                String zip;
                File forzipfile = new File(list);
                for (File f : Objects.requireNonNull(forzipfile.listFiles())) {
                    zip = (f.getName());
                    System.out.println(zip);
                    if (zip.contains(appliaction_name + "_" + version_ + ".zip")) {
                        File appzipfile = new File(destination);
                        if (!appzipfile.exists()) {
                            appzipfile.mkdir();
                        }
                        new UnZipTask().execute(source, destination);
                        return;
                    }
                }
                msg = "Zip file Not Found";

            } else {
                deleteRecursive(new File(rhmspath + appliaction_name));
            }
            show_AlertDialog(context.getResources().getString(R.string.TMS),
                    msg,
                    "",
                    0);
            }catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();

            }
        }
    }


    private class UnZipTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Show(context.getResources().getString(R.string.Processing),
                    context.getResources().getString(R.string.Please_wait));
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(String... params) {
            String filePath = params[0];
            String destinationPath = params[1];

            File archive = new File(filePath);
            try {
                ZipFile zipfile = new ZipFile(archive);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    unzipEntry(zipfile, entry, destinationPath);
                }

                UnzipUtil d = new UnzipUtil(filePath, destinationPath);
                d.unzip();
            } catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Dismiss();
            if (result) {
                msg = "Extracted Successfully";

                boolean isNonPlayAppAllowed = false;
                try {
                    isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                if (!isNonPlayAppAllowed) {

                    startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                }
                File yourDir = new File(rhmspath + appliaction_name + "/" + appliaction_name + "_" + version_);
                String file;
                for (File f : Objects.requireNonNull(yourDir.listFiles())) {
                    file = (f.getName());
                    System.out.println(file);
                    if (file.contains(appliaction_name + "_" + version_ + ".apk")) {

                        install(yourDir.toString(), f.getName());
                        return;
                    }
                }
                //deleteRecursive(new File(rhms));

                msg = "APK Not Found";
            } else {

                deleteRecursive(new File(rhmspath + appliaction_name));
            }
            show_AlertDialog(context.getResources().getString(R.string.TMS),
                    msg,
                    "",
                    0);
        }
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {

        } finally {
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    private void install(String path, String downloadfilename) {
        msg = "Please click on Install Button";

        System.out.println("Path : " + path + " file : " + downloadfilename);
        File file = new File(path, downloadfilename); // assume refers to "sdcard/myapp_folder/myapp.apk"
        Uri fileUri = Uri.fromFile(file); //for Build.VERSION.SDK_INT <= 2
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        }
        System.out.println(fileUri);
        Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //dont forget add this line
        startActivityForResult(intent, 9);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9 && resultCode == RESULT_OK) {
            msg = "Application Update Success";
        } else if (requestCode == 99){

        }else {
            msg = "Application Update Failed";
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

    private boolean getApp(Context applicationContext, String applicationType) {

        PackageManager pm = applicationContext.getPackageManager();
        try {
            pm.getPackageInfo(applicationContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    @Override
    public void initialize() {
        try {
            context = Device_Update.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_device__update, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;

            appVersion = Float.parseFloat(version);


            isStoragePermissionGranted();

            Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;


            usb.setOnClickListener(view -> {
                preventTwoClick(view);
               Intent i=new Intent(context,USBActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
               startActivity(i);
            });


            gprs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    // if (dealerConstants.fpsCommonInfo.versionUpdateRequired.equals("Y")) {
                    if (Util.networkConnected(context)) {
                        filesize();
                    } else {
                        show_AlertDialog(context.getResources().getString(R.string.TMS),
                                context.getResources().getString(R.string.Internet_Connection),
                                context.getResources().getString(R.string.Internet_Connection_Msg),
                                0);
                    }
               /* }else {
                    show_error_box("No Updated Apk Found","Update Version");
                }*/
                }
            });


            TMS = findViewById(R.id.tms);
            TMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        serialno = android.os.Build.getSerial();

                        if (Util.networkConnected(context)) {
                            Application = new ArrayList<>();
                            hitit();
                        } else {
                            show_AlertDialog(context.getResources().getString(R.string.TMS),
                                    context.getResources().getString(R.string.Internet_Connection),
                                    context.getResources().getString(R.string.Internet_Connection_Msg),
                                    0);
                        }
                    } else {
                        serialno = Build.SERIAL;

                    }
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
        pd = new ProgressDialog(context);
        gprs = findViewById(R.id.gprs);
        usb = findViewById(R.id.usb);
        toolbarActivity.setText(context.getResources().getString(R.string.UPDATE));
    }
}