package com.visiontek.Mantra.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.address;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.toast;


public class DeviceListActivity extends Activity {
Context context;
    private static final String TAG = "DeviceListActivity";
    public String EXTRA_DEVICE_ADDRESS = "device_address";

    TextView textView1;
    ArrayList<String> deviceAdress;
    HashMap<String, String> shorlisteddeviceAdr = new HashMap<String, String>();
    private BluetoothAdapter mBtAdapter;

    // Set up on-click listener for the list (nicked this - unsure)
    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            textView1.setText(context.getResources().getString(R.string.Connecting));
            // Get the device MAC address, which is the last 17 chars in the View
            // String info = ((TextView) v).getText().toString();
            //  String address = info.substring(info.length() - 17);
            address = deviceAdress.get(arg2);
            System.out.println("addresssssssss" + address);
            // Make an intent to start next activity while taking an extra which is the MAC address.
            Intent i = new Intent();
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    };

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.ACCESS_COARSE_LOCATION"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_list);
        context = DeviceListActivity.this;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();

        if (shouldAskPermissions()) {
            askPermissions();
        }
        //shorlisteddeviceAdr.add("34:81:F4:09:15:EF","");
        shorlisteddeviceAdr.put("34:81:F4:09:15:EF", "TD002040");
        shorlisteddeviceAdr.put("34:81:F4:09:16:17", "DT000351");
        shorlisteddeviceAdr.put("34:81:F4:09:16:07", "DT000243");
        shorlisteddeviceAdr.put("34:81:F4:09:16:1A", "DT000244");
        shorlisteddeviceAdr.put("34:81:F4:09:16:19", "DT000352");
        shorlisteddeviceAdr.put("34:81:F4:09:16:09", "TD002160");
        shorlisteddeviceAdr.put("34:81:F4:09:16:1E", "DT000245");

    }

    @Override
    public void onResume() {
        super.onResume();
        checkBTState();
        textView1 = findViewById(R.id.connecting);
        textView1.setTextSize(40);
        textView1.setText(" ");

        // Initialize array adapter for paired devices
        ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
            deviceAdress = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                //  for (String shortlisted:shorlisteddeviceAdr.keySet()){
                //      if (shortlisted.equals(device.getAddress())){
                //   mPairedDevicesArrayAdapter.add(shorlisteddeviceAdr.get(shortlisted));//+ "\n" + device.getAddress());
                mPairedDevicesArrayAdapter.add(device.getName());//+ "\n" + device.getAddress());
                deviceAdress.add(device.getAddress());
            }
        } else {
            String noDevices = "No Devices have been paired";
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), context.getResources().getString(R.string.Device_does_not_support_Bluetooth), Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }

    @Override
    public void onBackPressed() {


        finish();

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

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("DEVICE LIST");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}