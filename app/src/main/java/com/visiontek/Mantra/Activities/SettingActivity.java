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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import timber.log.Timber;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class SettingActivity extends AppCompatActivity {
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    Button device, info, update, pairing, back;
    Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mProgressDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = SettingActivity.this;

        try {

            TextView toolbarRD = findViewById(R.id.toolbarRD);
            boolean rd_fps = RDservice(context);
            if (rd_fps) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
                show_AlertDialog(context.getResources().getString(R.string.SETTINGS),
                        context.getResources().getString(R.string.RD_Service),
                        context.getResources().getString(R.string.RD_Service_Msg),0);
                return;
            }

            initilisation();

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
            pairing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preventTwoClick(v);
                    mProgressDlg = new ProgressDialog(context);
                    mProgressDlg.setMessage("Scanning...");
                    mProgressDlg.setCancelable(false);
                    mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", ((dialog, which) -> {
                        dialog.dismiss();
                        if (mBluetoothAdapter != null)
                            mBluetoothAdapter.cancelDiscovery();
                    }));

                    mBluetoothAdapter.startDiscovery();
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
                mProgressDlg.dismiss();

            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(EXTRA_DEVICE);
                System.out.println("@@Found bluetooth adding Name: "+device.getName() +" ,Address: "+device.getAddress());
                device.getAddress();
                mDeviceList.add(device);
                if(device!=null) {
                    if(device.getName()!=null)
                    {
                        if (device.getName().equalsIgnoreCase("VTWS100")) {
                            pairDevice(device,intent);

                        }}
                }
            }
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

/*try {
            device.setPin("94321".getBytes());
            device.setPairingConfirmation(true);
            device.createBond();

            IntentFilter filter = new IntentFilter(
                    "android.bluetooth.device.action.PAIRING_REQUEST");


            *//*
         * Registering a new BTBroadcast receiver from the Main Activity context
         * with pairing request event
         *//*
            registerReceiver(
                    new PairingRequest(), filter);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
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



    private void initilisation() {
        device = findViewById(R.id.device);
        info = findViewById(R.id.deviceinfo);
        update = findViewById(R.id.update);
        pairing = findViewById(R.id.pairdevice);
        toolbarInitilisation();
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

    private void toolbarInitilisation() {
        try {

            TextView toolbarVersion = findViewById(R.id.toolbarVersion);
            TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
            TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
            TextView toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
            TextView toolbarActivity = findViewById(R.id.toolbarActivity);
            TextView toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
            TextView toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);

            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);


            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("DEVICE ID");
            toolbarFpsidValue.setText(DEVICEID);
            toolbarActivity.setText(context.getResources().getString(R.string.SETTINGS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {
            Timber.tag("Settings-onCreate-").e(ex.getMessage(), "");
        }
    }

    private void show_Dialogbox(String msg, String header) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialogbox);
        Button back = (Button) dialog.findViewById(R.id.dialogcancel);
        Button confirm = (Button) dialog.findViewById(R.id.dialogok);
        TextView head = (TextView) dialog.findViewById(R.id.dialoghead);
        TextView status = (TextView) dialog.findViewById(R.id.dialogtext);
        head.setText(header);
        status.setText(msg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
}
