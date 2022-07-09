package com.visiontek.Mantra.Activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mantra.mTerminal100.MTerminal100API;
import com.mantra.mTerminal100.printer.PrinterCallBack;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;

public class offlineRecvGoods extends AppCompatActivity implements PrinterCallBack {
    public Button back,submit;
    public EditText truckNo,challanNo,vehicleNo;
    String truckNumber,challanNumber,vehicleNumber;
    DatabaseHelper databaseHelper;
    MTerminal100API mTerminal100API;
    public String ACTION_USB_PERMISSION;
    Context context;

    private final ExecutorService es = Executors.newScheduledThreadPool(30);
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                probe();
                // btnConnect.performClick();
                Toast.makeText(context, context.getResources().getString(R.string.ConnectUSB), Toast.LENGTH_LONG).show();
                //print.setEnabled(true);
                synchronized (this) {

                }
            }

        }
    };

    private void probe() {
        System.out.println("@@In probe");
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceList.size() > 0) {

            while (deviceIterator.hasNext()) {
// Here is if not while, indicating that I only want to support a device
                final UsbDevice device = deviceIterator.next();
                if ((device.getProductId() == 22304) && (device.getVendorId() == 1155)) {
                    // TODO Auto-generated method stub
                    PendingIntent mPermissionIntent = PendingIntent
                            .getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    if (!mUsbManager.hasPermission(device)) {
                        mUsbManager.requestPermission(device, mPermissionIntent);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(ACTION_USB_PERMISSION);
                        context.registerReceiver(mUsbReceiver, filter);
                        Toast.makeText(getApplicationContext(),
                                context.getResources().getString(R.string.Permission_denied), Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.Connecting), Toast.LENGTH_SHORT).show();

                        //print.setEnabled(false);
                        es.submit(new Runnable() {
                            @Override
                            public void run() {
                                mTerminal100API.printerOpenTask(mUsbManager, device, context);
                            }
                        });
                    }
                    //  });
                } else {
                    //  Toast.makeText(ConnectUSBActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_receivegoods);
        context = getApplicationContext();
        databaseHelper = new DatabaseHelper(this);
        back = findViewById(R.id.back);
        submit = findViewById(R.id.submit);
        truckNo = findViewById(R.id.truckNo);
        challanNo = findViewById(R.id.challanNo);
        vehicleNo = findViewById(R.id.vehicleNo);

        mTerminal100API = new MTerminal100API();
        mTerminal100API.initPrinterAPI(this, context);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            probe();
        } else {
            finish();
        }

        toolbarInitilisation();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("@@Clicked on submit of offline receive goods");
                truckNumber = truckNo.getText().toString();
                challanNumber = challanNo.getText().toString();
                vehicleNumber = vehicleNo.getText().toString();
                if(truckNumber==null || truckNumber.length()<1)
                {
                    truckNo.requestFocus();
                    truckNo.setError("please enter truck number");
                    return;
                }
                if(challanNumber == null || challanNumber.length()<1)
                {
                    challanNo.requestFocus();
                    challanNo.setError("please enter challan number");
                    return;
                }
                if(vehicleNumber == null || vehicleNumber.length()<1)
                {
                    vehicleNo.requestFocus();
                    vehicleNo.setError("please entire vehicle number");
                    return;
                }


                if(challanNumber.charAt(0)=='R' || challanNumber.charAt(0)=='E') {
                    boolean ret = databaseHelper.isChallanExists(challanNumber);
                    if (ret) {
                        show_AlertDialog("INVALID CHALLAN", "Challan number already exists", "Enter valid challan number");
                        return;
                    }
                }else{
                    show_AlertDialog("INVALID CHALLAN", "Entered challan number is not valid...", "Enter valid challan number");
                    return;
                }


                Intent intent = new Intent(offlineRecvGoods.this,offlineRecvNew.class);
                intent.putExtra("truck",truckNumber);
                intent.putExtra("vehicle",vehicleNumber);
                intent.putExtra("challan",challanNumber);
                startActivity(intent);

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("@@Clicked on back of offline receive goods");
                Intent intent = new Intent(offlineRecvGoods.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void show_AlertDialog(String headermsg,String bodymsg,String talemsg) {

        final Dialog dialog = new Dialog(offlineRecvGoods.this, android.R.style.Theme_Dialog);
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
            System.out.println("@@App version: "+appversion);
            toolbarVersion.setText("V" + appversion);


            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            toolbarActivity.setText( context.getResources().getString(R.string.CASH_PDS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {

            Timber.tag("CashPDS-ToolBar-").e(ex.getMessage(), "");
        }
    }

    @Override
    public void OnOpen() {

    }

    @Override
    public void OnOpenFailed() {

    }

    @Override
    public void OnClose() {

    }

    @Override
    public void OnPrint(int i, boolean b) {

    }
}