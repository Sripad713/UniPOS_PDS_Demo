package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.lvrenyang.io.base.IO;
import com.visiontek.Mantra.BuildConfig;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.MyFTPClientFunctions;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.UnzipUtil;
import com.visiontek.Mantra.Utils.Util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import timber.log.Timber;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class SettingActivity extends BaseActivity {
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    Button device, info,deviceparameters,dbdownload,log,update, pairing, back;
    Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mProgressDlg;
    String strDate, strTime;
    String filename;
    String mSerial;
    String SERIAL;
    String deviceId;
    MyFTPClientFunctions ftpclient;
    ProgressDialog progressDialog;
    public String dbPassword;
    //String dbPassword;
    String data = "";
    public FTPClient mFTPClient = new FTPClient();
    String
            fHostName = "115.111.229.10",
            fUserName = "rnd",
            fPassword = "rnd123";

    String FTP_file = "",
            Download = "",
            Device_Download_path;
    String BasicPath = "/" + fUserName + "/CGPDS/";



    @Override
    protected void onResume() {

        super.onResume();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mProgressDlg.isShowing()) {
                    mProgressDlg.dismiss();
                }

            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(EXTRA_DEVICE);
                device.getAddress();
                mDeviceList.add(device);
                if(device!=null) {
                    if(device.getName()!=null)
                    {
                        /* if (device.getName().equalsIgnoreCase("APPDS_VNTK@2015")) {*/
                        //"VTWS100"//change_2
                        if (device.getName().equalsIgnoreCase("VTWS100")) {
                            if (mBluetoothAdapter != null)
                                mBluetoothAdapter.cancelDiscovery();
                            String Mac= device.getAddress();
                            SharedPref SharedPref = com.visiontek.Mantra.Utils.SharedPref.getInstance(context);
                            SharedPref.saveData("MAC",Mac);
                            pairDevice(device,intent);
                            // String value = SharedPref.getData("MAC");
                            //if (value!=null){
                               // if (value.length()>0){
                                    //if (value.equals(Mac)){
                                       /* runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mProgressDlg.isShowing()) {
                                                    mProgressDlg.dismiss();
                                                }

                                                show_AlertDialog(
                                                        context.getResources().getString(R.string.Pairing),
                                                        "Bluetooth Already Paired",
                                                        "",
                                                        0
                                                );
                                            }
                                        });*/
                                   /* }else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mProgressDlg.isShowing()) {
                                                    mProgressDlg.dismiss();
                                                }
                                                show_Dialogbox(
                                                        device,
                                                        intent
                                                );
                                            }
                                        });

                                    }*/
                               // }else {
                                    //SharedPref.saveData("MAC",Mac);
                                   // pairDevice(device,intent);
                               // }
                          //  }
                        }
                    }
                }
            }


           /* String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(EXTRA_DEVICE);
                device.getAddress();
                mDeviceList.add(device);
                if(device!=null) {
                    if(device.getName()!=null)
                    {
                        if (device.getName().equalsIgnoreCase("VTWS100")) {
                            pairDevice(device,intent);

                        }}
                }
            }*/
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void pairDevice(BluetoothDevice device,Intent intent) {
        System.out.println("@@Pairing device: " +device.getName());
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");

            BluetoothDevice device1 = intent.getParcelableExtra(EXTRA_DEVICE);
            int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 0);

//the pin in case you need to accept for an specific pin
            Log.d("PIN", " " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",0));
//maybe you look for a name or address
            Log.d("Bonded", device.getName());
            byte[] pinBytes;
            pinBytes = ("94321").getBytes("UTF-8");
            device.setPin(pinBytes);
//setPairing confirmation if neeeded
            device.setPairingConfirmation(true);

        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }

    }

    public static class PairingRequest extends BroadcastReceiver {
        public PairingRequest() {
            super();
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                try {
                    System.out.println("@@In onReceive of bluetooth");
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                    int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 0);

//the pin in case you need to accept for an specific pin
                    Log.d("PIN", " " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",0));
//maybe you look for a name or address
                    Log.d("Bonded", device.getName());
                    byte[] pinBytes;
                    pinBytes = ("94321").getBytes("UTF-8");
                    device.setPin(pinBytes);
//setPairing confirmation if neeeded
                    device.setPairingConfirmation(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            finish();
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




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void initialize() {
        try {
            context = SettingActivity.this;
            ftpclient = new MyFTPClientFunctions();
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_settings, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();


            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent info = new Intent(context, DeviceActivity.class);
                    startActivityForResult(info, 1);
                }
            });
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent info = new Intent(context, DeviceInfoActivity.class);
                    startActivity(info);
                }
            });
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent update = new Intent(context, Device_Update.class);
                    startActivity(update);
                }
            });

            deviceparameters.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent deviceparameters = new Intent(context, DeviceParametersActivity.class);
                    startActivity(deviceparameters);
                }
            });



            dbdownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if(Util.networkConnected(context)){
                         filesize(BasicPath);
                        //ftpPassword_Dialog();
                        /*Calendar c = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                        strDate = sdf.format(c.getTime());
                        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
                        strTime = sdf1.format(c.getTime());
                        filename = mSerial + "_" + strDate + ".txt";*/
                        //generateNoteOnSD(filename, longitude, latitude, percantage);
                       /* Show(context.getResources().getString(R.string.Processing),
                        context.getResources().getString(R.string.Please_wait));
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FileUploadDemo();
                            }
                        });
                        thread.start();*/
                    }else{
                        show_AlertDialog(context.getResources().getString(R.string.Internet_Connection),context.getResources().getString(R.string.Internet_Connection_Msg),"",0);
                    }
                }
            });
            log.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if(Util.networkConnected(context)){

                        Show(context.getResources().getString(R.string.Processing),
                                context.getResources().getString(R.string.Please_wait));
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                logUploadDemo();
                            }
                        });
                        thread.start();


                    }else{
                        show_AlertDialog(context.getResources().getString(R.string.Internet_Connection),context.getResources().getString(R.string.Internet_Connection_Msg),"",0);
                    }

                }
            });

            pairing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);

                    if(checkForPairedDevices()){

                            show_AlertDialog("Device is Already paired","","",0);
                            System.out.println("=======Already Paired=========");

                    }else{
                           //System.out.println("Please Pairrrr>>>>>>");
                           show_AlertDialog("Bluetooth Pairing Password: 94321","","",1);
                           /*mProgressDlg = new ProgressDialog(context);
                            mProgressDlg.setMessage("Scanning...");
                            mProgressDlg.setCancelable(false);
                            mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", ((dialog, which) -> {
                            dialog.dismiss();
                          if (mBluetoothAdapter != null)
                              mBluetoothAdapter.cancelDiscovery();
                                System.out.println("cancelll");

                         }));
                            mBluetoothAdapter.startDiscovery();

*/

                        }


                    /* if(deviceName.equals(device)){

                        AlertDialogbox("Device is Already paired");


                    }else{

                        AlertDialogbox("Please pair the device");

                    }*/
                    /*mProgressDlg = new ProgressDialog(context);
                    mProgressDlg.setMessage("Scanning...");
                    mProgressDlg.setCancelable(false);
                    mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", ((dialog, which) -> {
                        dialog.dismiss();
                        if (mBluetoothAdapter != null)
                            mBluetoothAdapter.cancelDiscovery();
                    }));

                    mBluetoothAdapter.startDiscovery();*/
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            registerReceiver(mReceiver, filter);
            back = findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        } catch (Exception ex) {

            Timber.tag("Settings-onCreate-").e(ex.getMessage(), "");
        }
    }

    public void FileUploadDemo() {

        String server = "115.111.229.10";
        int port = 21;
        String user = "rnd";
        String pass = "rnd123";

        FTPClient ftpClient = new FTPClient();

        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.makeDirectory("CGPDS");
            ftpClient.changeWorkingDirectory("CGPDS");
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            //strDate = sdf.format(c.getTime());
            //SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd-HHmmss");
            strDate = sdf1.format(c.getTime());
            SERIAL = mSerial + "_" + strDate+".zip";
            filename = context.getDatabasePath( "PDS.db").getPath();
            String  zipFilePath = getFilesDir().getAbsolutePath()+File.separator+"PDS.db.zip";
            UnzipUtil.zip(new String[]{filename},zipFilePath);
            File firstLocalFile = new File(zipFilePath);
            System.out.println("DB FILE>>>>>>>>>" + firstLocalFile);
            InputStream inputStream = new FileInputStream(firstLocalFile);
            boolean done = ftpClient.storeFile(SERIAL,inputStream);
            System.out.println("DONE =====" + done);
            inputStream.close();
            if (done) {
                System.out.println("The file is uploaded successfully.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressDialog.dismiss();
                        show_AlertDialog((context.getResources().getString(R.string.Database_Upload)),(context.getResources().getString(R.string.file_is_uploaded_successfully)),"",2);

                    }
                });

            } else {
                System.out.println("The first file is upload Failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        show_AlertDialog((context.getResources().getString(R.string.Database_Upload)),(context.getResources().getString(R.string.file_is_uploaded_failed)),"",0);


                    }
                });

            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


    }
    public void logUploadDemo() {

        String server = "115.111.229.10";
        int port = 21;
        String user = "rnd";
        String pass = "rnd123";

        final int logFileCount = 10;

        FTPClient ftpClient = new FTPClient();
        try {
            String[] logFilesPaths = new String[logFileCount];
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.makeDirectory("CGPDS/Log");
            ftpClient.changeWorkingDirectory("CGPDS/Log");
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd-HHmmss");
            strDate = sdf1.format(c.getTime());
            SERIAL = mSerial + "_" + strDate+"_Log.zip";

            String logFileZip = getFilesDir()+File.separator+"log.zip";
            {
                File destFle = new File(logFileZip);
//                if(destFle.exists())
//                    destFle.delete();
            }

            Calendar cal = Calendar.getInstance();
            // get starting date
            // loop adding one day in each iteration
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    BuildConfig.APPLICATION_ID + File.separator + "Log");
            for(int i = 0; i< logFileCount; i++){
                cal.add(Calendar.DAY_OF_YEAR, -i);

                String fileNameTimeStamp = sdf.format(cal.getTime());
                logFilesPaths[i] = root+ File.separator+fileNameTimeStamp+".txt";
            }
            UnzipUtil.zip(logFilesPaths,logFileZip);
            File firstLocalFile = new File(logFileZip);
            System.out.println(" LOG FILE>>>>>>>>>" + firstLocalFile);
            InputStream inputStream = new FileInputStream(firstLocalFile);
            boolean done = ftpClient.storeFile(SERIAL, inputStream);
            System.out.println("DONE =====" + done);
            inputStream.close();
            if (done) {
                System.out.println("The file is uploaded successfully.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        show_AlertDialog(context.getResources().getString(R.string.Log_File_Upload),context.getResources().getString(R.string.file_is_uploaded_successfully),"",0);
                    }
                });

            } else {
                System.out.println("The first file is upload Failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        show_AlertDialog(context.getResources().getString(R.string.Log_File_Upload),context.getResources().getString(R.string.file_is_uploaded_failed),"",0);
                    }
                });

            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


    }
    private void filesize(final String PATH) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    FTP_file = (ftpclient.Ffinding1(fHostName, fUserName, fPassword, PATH));
                    System.out.println("FTP_FILE>>>>>" + FTP_file);
                    if (FTP_file.equalsIgnoreCase("NOFILE") || (FTP_file.equalsIgnoreCase("EXCEPTION"))) {
                        //something = context.getResources().getString(R.string.No_File_Available);
                        //handler.sendEmptyMessage(4);
                         System.out.println("FILE ===="+FTP_file);
                         Timber.d("SettingActivity-filesize ==="+FTP_file);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                StaticFtpPassword_Dialog();

                            }
                        });

                    }else{
                        System.out.println(FTP_file);
                        Download = PATH + FTP_file;
                        System.out.println("Source ===" + Download);
                        Device_Download_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + FTP_file;
                        System.out.println("Des ===" + Device_Download_path);
                        boolean download = ftpclient.ftpDownload(Download, Device_Download_path);
                        System.out.println("DOWNLOAD ==Boolean ==="+download);
                        if(download){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ftpPassword_Dialog();
                                }
                            });
                            System.out.println("DOWNLAOD ===Sucess");
                            Timber.d("SettingActivity-filesize-DOWNLOAD SUCCESS");
                        }else{
                            System.out.println("Download ====Failed");
                            Timber.d("SettingActivity-filesize-DOWNLOAD FAILED");
                        }
                        //handler.sendEmptyMessage(2);
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("SettingActivity-DB DWONLOAD FTP-filesize Exception :"+e.getLocalizedMessage());
            //something = context.getResources().getString(R.string.ERROR_IN_GETTING_FILE);
            //handler.sendEmptyMessage(4);
            //Timber.tag("DeviceUpdate-FileName-").e(e.getMessage(), "");
        }

    }
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }
    public void Show(String msg, String title) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);
        progressDialog.setTitle(ss1);
        progressDialog.setMessage(ss2);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    public void StaticFtpPassword_Dialog() {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.ftppassword);
        Button confirm = (Button) dialog.findViewById(R.id.confirm);
        Button back = (Button) dialog.findViewById(R.id.back);
        TextView tv = (TextView) dialog.findViewById(R.id.dialog);
        TextView status = (TextView) dialog.findViewById(R.id.status);
        final EditText enter = (EditText) dialog.findViewById(R.id.enter);
        tv.setText(context.getResources().getString(R.string.Ftp_Passowrd));
        status.setText(context.getResources().getString(R.string.Enter_Ftp_Password));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
               /* String fileName_r = "/sdcard/Download/" + "Password.txt";
                System.out.println("FILE..." + fileName_r);
                System.out.println(">>>>>>>>>>>>>");
                File file = new File(fileName_r);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    br = new BufferedReader(isr);
                    System.out.println(">>>>>>FILEINPUT>>>>>>>>>"+fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while((data = br.readLine()) != null){

                        System.out.println("LINE>>>>>" + data);
                    }
                    text.append(data);
                    br.close();
                }catch (IOException e){

                    e.printStackTrace();
                }*/
                if (Util.networkConnected(context)) {
                    dbPassword = enter.getText().toString();
                    System.out.println("DBPASS ===="+dbPassword);
                    if(!dbPassword.isEmpty()) {
                        if (dbPassword.equals("123456")) {
                            Show(context.getResources().getString(R.string.Processing),
                                    context.getResources().getString(R.string.Please_wait));
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    FileUploadDemo();
                                }
                            });
                            thread.start();
                        }else{
                            System.out.println("INVALID 2222222");
                            show_AlertDialog("",
                                    context.getResources().getString(R.string.Invalid_Password),
                                    context.getResources().getString(R.string.Please_Enter_a_valid_Password),
                                    0);
                        }
                    }else {
                        show_AlertDialog("",
                                context.getResources().getString(R.string.Invalid_Password),
                                context.getResources().getString(R.string.Please_Enter_a_valid_Password),
                                0);
                    }
                }else {
                    show_AlertDialog(context.getResources().getString(R.string.Internet_Connection), context.getResources().getString(R.string.Internet_Connection_Msg), "", 0);
                }

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

    public void ftpPassword_Dialog() {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.ftppassword);
        Button confirm = (Button) dialog.findViewById(R.id.confirm);
        Button back = (Button) dialog.findViewById(R.id.back);
        TextView tv = (TextView) dialog.findViewById(R.id.dialog);
        TextView status = (TextView) dialog.findViewById(R.id.status);
        final EditText enter = (EditText) dialog.findViewById(R.id.enter);
        tv.setText(context.getResources().getString(R.string.Ftp_Passowrd));
        status.setText(context.getResources().getString(R.string.Enter_Ftp_Password));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
               /* String fileName_r = "/sdcard/Download/" + "Password.txt";
                System.out.println("FILE..." + fileName_r);
                System.out.println(">>>>>>>>>>>>>");
                File file = new File(fileName_r);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    br = new BufferedReader(isr);
                    System.out.println(">>>>>>FILEINPUT>>>>>>>>>"+fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while((data = br.readLine()) != null){

                        System.out.println("LINE>>>>>" + data);
                    }
                    text.append(data);
                    br.close();
                }catch (IOException e){

                    e.printStackTrace();
                }*/
                if (Util.networkConnected(context)) {
                    dbPassword = enter.getText().toString();
                    String pass =readPassword();
                    System.out.println("DBPASS ===="+dbPassword);
                    System.out.println("PASSs====="+pass);
                    if(!dbPassword.isEmpty()) {
                        if (dbPassword.equals(pass)) {
                            Show(context.getResources().getString(R.string.Processing),
                                    context.getResources().getString(R.string.Please_wait));
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    FileUploadDemo();
                                }
                            });
                            thread.start();
                        }else{
                            System.out.println("INVALID 2222222");
                            show_AlertDialog("",
                                    context.getResources().getString(R.string.Invalid_Password),
                                    context.getResources().getString(R.string.Please_Enter_a_valid_Password),
                                    0);
                        }
                    }else{
                        show_AlertDialog("",
                                context.getResources().getString(R.string.Invalid_Password),
                                context.getResources().getString(R.string.Please_Enter_a_valid_Password),
                                0);
                        System.out.println("INVLID.....");

                          }


                 }else {

                    show_AlertDialog(context.getResources().getString(R.string.Internet_Connection), context.getResources().getString(R.string.Internet_Connection_Msg), "", 0);
                }

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
    public String readPassword(){
        String fileName_r = "/sdcard/Download/" +"Password.txt";
        System.out.println("FILE..." + fileName_r);
        System.out.println(">>>>>>>>>>>>>");
        File file = new File(fileName_r);
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            System.out.println(">>>>>>FILEINPUT>>>>>>>>>"+fis);

            String data;
            while((data = br.readLine()) != null){
                sb.append(data);
                System.out.println("LINE>>>>>" + data);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void showToast(final String toast) {

        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(SettingActivity.this);
        alert.setTitle("Response");

        alert.setMessage(toast);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
    private boolean checkForPairedDevices()
    {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();

        String deviceName = null;
        // Get paired devices.
        Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                deviceName = device.getName();
                //"VTWS100"//change_3
                if(deviceName.equals("VTWS100")){

                    return true;
                }
            }
                System.out.println("deviceNAME>>>>"+deviceName);


                //String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void initializeControls() {

        mSerial = Build.getSerial();
        device = findViewById(R.id.device);
        progressDialog = new ProgressDialog(this);
        info = findViewById(R.id.deviceinfo);
        deviceparameters= findViewById(R.id.deviceparameters);
        update = findViewById(R.id.update);
        pairing = findViewById(R.id.pairdevice);
        dbdownload = findViewById(R.id.dbdownloadtoftp);
        log = findViewById(R.id.logfile);
        toolbarActivity.setText(context.getResources().getString(R.string.SETTINGS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver!=null) {
            unregisterReceiver(mReceiver);
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
                if(i==1){
                    mProgressDlg = new ProgressDialog(context);
                    mProgressDlg.setMessage("Scanning...");
                    mProgressDlg.setCancelable(false);
                    mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", ((dialog, which) -> {
                        dialog.dismiss();
                        if (mBluetoothAdapter != null)
                            mBluetoothAdapter.cancelDiscovery();
                        System.out.println("cancelll");

                    }));
                    System.out.println("====startDiscovery1111 Main====");

                    mBluetoothAdapter.startDiscovery();
                }


            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    private void show_Dialogbox(BluetoothDevice device, Intent intent) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialogbox);
        Button back = (Button) dialog.findViewById(R.id.dialogcancel);
        Button confirm = (Button) dialog.findViewById(R.id.dialogok);
        TextView head = (TextView) dialog.findViewById(R.id.dialoghead);
        TextView status = (TextView) dialog.findViewById(R.id.dialogtext);
        status.setText("Do you Want to pair another device");
        confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                dialog.dismiss();
                pairDevice(device,intent);
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

    public void AlertDialogbox(final String toast) {

        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(SettingActivity.this);
        alert.setTitle("Message");

        alert.setMessage(toast);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alert.show();

    }



}
