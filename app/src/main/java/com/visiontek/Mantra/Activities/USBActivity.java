package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.itextpdf.xmp.impl.Utils;
import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.CopyTaskParam;
import com.visiontek.Mantra.Utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Utils.Util.RDservice;

public class USBActivity extends AppCompatActivity{
    private static final String ACTION_USB_PERMISSION = "com.androidinspain.otgviewer.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    Context context;
    String extention;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u_s_b);
        try {
            context = USBActivity.this;

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

            status=findViewById(R.id.status);
            Button back=findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbarInitilisation();
            status.setText("Please Insert USB device and give Permission");
        }catch (Exception ex){
            ex.printStackTrace();
        }
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
            TextView toolbarRD = findViewById(R.id.toolbarRD);
            if (rd_fps == 3) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else if (rd_fps == 2) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
            } else {
                if (RDservice(context)) {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                } else {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                }
            }

            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);

            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            toolbarActivity.setText( context.getResources().getString(R.string.USB));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {

            Timber.tag("CashPDS-ToolBar-").e(ex.getMessage(), "");
        }
    }

    boolean flagvalue=false;
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                checkUSBStatus();
            }else if (ACTION_USB_PERMISSION.equals(action)) {

                status.setText("Please Wait");
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (!flagvalue) {
                            flagvalue=true;

                            UsbMassStorageDevice mSelectedDevice = null;
                            UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(context);

                            if (devices.length > 0)
                                mSelectedDevice = devices[0];

                            try {
                                if (mSelectedDevice != null) {
                                    mSelectedDevice.init();
                                    FileSystem fs = mSelectedDevice.getPartitions().get(0).getFileSystem();
                                    UsbFile root = fs.getRootDirectory();
                                    UsbFile[] rooti = root.listFiles();
                                    UsbFile[] sub = root.listFiles();
                                    UsbFile rootj;
                                    String Name;
                                    File path=new File(String.valueOf(Util.otgViewerPath));
                                    for (UsbFile usbFile : rooti) {
                                        Name = usbFile.getName();
                                        System.out.println("==========2=" + usbFile.getName());
                                        if (Name.contains("CGPDS")) {
                                            if (usbFile.isDirectory()){
                                                sub=usbFile.listFiles();
                                                for (UsbFile usbFile1 : sub) {
                                                    Name = usbFile1.getName();
                                                    if (Name.contains("MantraPDS_")) {
                                                        System.out.println(Name + "=====" + Name.substring(10, 13));
                                                        float usbapk = Float.parseFloat(Name.substring(10, 13));
                                                        float appversion = Float.parseFloat(Util.getAppVersionFromPkgName(getApplicationContext()));
                                                        if (usbapk > appversion && !usbFile1.isDirectory()) {

                                                            if (path.exists()) {
                                                                deleteRecursive(Util.otgViewerPath);
                                                                createDir(Util.otgViewerPath);
                                                            }

                                                            rootj = usbFile1;
                                                            downloadFile(rootj);
                                                            return;
                                                        } else {
                                                            status.setText("Update Not found");
                                                            return;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    status.setText("APK Not found");

                                } else {
                                    status.setText("Device Not Found");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        } else {
                        flagallow=false;

                            status.setText("Please Grant Permission");
                        }

                }
            }

        }
    };

    boolean flagallow=false;
    private void checkUSBStatus() {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (mUsbManager != null) {
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            if (!deviceList.isEmpty()) {
                for (UsbDevice device : deviceList.values()) {
                    if (Util.isMassStorageDevice(device)) {
                        status.setText("USB Device Detected");
                        if (!flagallow) {
                            flagallow=true;
                            mUsbManager.requestPermission(device, mPermissionIntent);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filter);

        checkUSBStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        //Utils.deleteCache(getCacheDir());
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }
    private void downloadFile(UsbFile entry) {

        CopyTaskParam param = new CopyTaskParam();
        param.from = entry;
        Util.otgViewerPath.mkdirs();
        int index = entry.getName().lastIndexOf(".");
        String prefix;
        String ext = "";
        if (index < 0) {
            prefix = entry.getName();
        } else {
            prefix = entry.getName().substring(0, index);
            ext = entry.getName().substring(index);
            extention = ext;
        }
        if (prefix.length() < 3) {
            prefix += "pad";
        }
        String fileName = prefix + ext;
        param.to = new File(Util.otgViewerPath, fileName);
        //ImageViewer.getInstance().setCurrentFile(entry);
        status.setText("Copying APK");
        new CopyTask(context,entry.getName()).execute(param);
    }

    private void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }
    private void launchIntent(File f) {

        if (extention.equals(".apk")) {
            status.setText("Grant Permissions to Update APK");
            File file = new File(f.getAbsolutePath());
            Uri fileUri = Uri.fromFile(file);
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            }
            System.out.println(fileUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
            finish();
        }
    }

    private class CopyTask extends AsyncTask<CopyTaskParam, Integer, Void> {

        private ProgressDialog dialog;
        private CopyTaskParam param;
        private final CopyTask cp;

        public CopyTask(Context context, String name) {
            cp = this;
            showDialog(name);
        }

        private void showDialog(String name) {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Copying "+name);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        }

        @Override
        protected void onCancelled(Void result) {

            if (param != null)
                param.to.delete();
        }

        @Override
        protected void onPreExecute() {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cp.cancel(true);
                }
            });

            dialog.show();
        }

        @Override
        protected Void doInBackground(CopyTaskParam... params) {
            long time = System.currentTimeMillis();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            param = params[0];
            long length = params[0].from.getLength();
            try {
                FileOutputStream out = new FileOutputStream(param.to);
                for (long i = 0; i < length; i += buffer.limit()) {
                    if (!isCancelled()) {
                        buffer.limit((int) Math.min(buffer.capacity(), length - i));
                        params[0].from.read(i, buffer);
                        out.write(buffer.array(), 0, buffer.limit());
                        publishProgress((int) i);
                        buffer.clear();
                    }
                }
                out.close();
            } catch (IOException e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            launchIntent(param.to);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setMax((int) param.from.getLength());
            dialog.setProgress(values[0]);
        }

    }

}