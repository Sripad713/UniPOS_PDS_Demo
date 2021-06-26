package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.visiontek.Mantra.Adapters.RationListAdapter;
import com.visiontek.Mantra.Models.DATAModels.RationListModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.MemberModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Json_Parsing;
import com.visiontek.Mantra.Utils.UsbService;
import com.visiontek.Mantra.Utils.Util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.checkdotvalue;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class RationDetailsActivity extends AppCompatActivity {
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Spinner options;
    public static int MESSAGE_FROM_SERIAL_PORT = 0;
    public static double TOTALAMOUNT;
    public int choice;
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter mBluetoothAdapter;
    private UsbService usbService;
    private MyHandler mHandler;
    String bt = null, usb = "";

    StringBuilder storeBTdata = new StringBuilder();


    UsbSerialDevice serialport = null;
    UsbDeviceConnection connection = null;
    UsbDevice device = null;

    //--------------------------------------------------------------------------------
    Button confirm, back;
    //    get;
    Context context;
    ProgressDialog pd = null;

    MemberModel memberModel;
    // RationDetailsModel rationDetailsModel = new RationDetailsModel();
    public String
            reasonName,
            reasonid,
            Ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ration_details);
        context = RationDetailsActivity.this;


        Ref = getIntent().getStringExtra("REF");
        memberModel = (MemberModel) getIntent().getSerializableExtra("OBJ");
        initilisation();
        Display(0);
        pd = new ProgressDialog(context);
        confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                conformRation();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventTwoClick(view);
                dialog();
            }
        });

        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancle", ((dialog, which) -> {
            dialog.dismiss();
            if (mBluetoothAdapter != null)
                mBluetoothAdapter.cancelDiscovery();
        }));

        String[] items = new String[]{"Communication", "Bluetooth", "USB"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        options.setAdapter(adapter1);
        options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                preventTwoClick(view);
                choice = position;
                if (choice == 2) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                if (usbService != null) {
                                    device = usbService.findSerialPortDevice(context);
                                    break;
                                }
                            }
                        }
                    }).start();
                } else if (choice == 1) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
                        ArrayList<String> deviceAdress = new ArrayList<String>();
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().equals("APPDS_VNTK@2015") || device.getName().equals("VTWS100")) {
                                address = (device.getAddress());
                            } else {
                                String noDevices = "No Devices have been paired";
                                System.out.println(noDevices);
                            }
                        }
                    } else {
                        String noDevices = "No Devices have been paired";
                        System.out.println(noDevices);
                        // mPairedDevicesArrayAdapter.add(noDevices);
                    }

                }
                System.out.println("SELETED=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        //pd = ProgressDialog.show(context, "Connt", "Please_Wait", true, false);

    }


    private void initilisation() {
        pd = new ProgressDialog(context);
        options = findViewById(R.id.options);
        confirm = findViewById(R.id.confirm);
        back = findViewById(R.id.ration_back);
        //get=findViewById(R.id.getweight);

        mHandler = new MyHandler(this);
        toolbarInitilisation();
    }

    private void dialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(RationDetailsActivity.this);
        builderSingle.setTitle("Please Selcet below Reason");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(RationDetailsActivity.this, android.R.layout.select_dialog_singlechoice);
        int reasonBeanListssize = dealerConstants.reasonBeanLists.size();
        for (int i = 0; i < reasonBeanListssize; i++) {
            arrayAdapter.add(dealerConstants.reasonBeanLists.get(i).reasonValue);
        }
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reasonName = arrayAdapter.getItem(which);
                reasonid = dealerConstants.reasonBeanLists.get(which).reasonId;
                AlertDialog.Builder builderInner = new AlertDialog.Builder(RationDetailsActivity.this);
                builderInner.setMessage(reasonName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelRequest();
                    }
                });
                builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    private void cancelRequest() {
        String mos = "P";
        String mt = "R";
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        //currentDateTimeString = "23032021163452";

        String reasons = "{\n" +
                "   \"fpsId\" :" + "\"" + dealerConstants.stateBean.statefpsId + "\"" + ",\n" +
                "   \"modeOfService\" : " + "\"" + mos + "\"" + ",\n" +
                "   \"moduleType\" :" + "\"" + mt + "\"" + ",\n" +
                "   \"rcId\" : " + "\"" + memberConstants.carddetails.rcId + "\"" + ",\n" +
                "   \"requestId\" :" + "\"" + reasonid + "\"" + ",\n" +
                "   \"requestValue\" :" + "\"" + reasonName + "\"" + ",\n" +
                "   \"sessionId\" : " + "\"" + dealerConstants.fpsCommonInfo.fpsSessionId + "\"" + ",\n" +
                "   \"stateCode\" : " + "\"" + dealerConstants.stateBean.stateCode + "\"" + ",\n" +
                "   \"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                "   \"timeStamp\" : " + "\"" + currentDateTimeString + "\"" + ",\n" +
                /* "   \"token\" : " + "\"" + dealerConstants.fpsURLInfo.token + "\"" + "\n" +*/
                "   \"token\" : " + "\"9f943748d8c1ff6ded5145c59d0b2ae7\"" + "\n" +
                "}";
        Util.generateNoteOnSD(context, "CancelRequestReq.txt", reasons);
        pd = ProgressDialog.show(context, "Please Wait ", context.getResources().getString(R.string.Processing), true, false);
        Json_Parsing request = new Json_Parsing(context, reasons, 2);
        request.setOnResultListener(new Json_Parsing.OnResultListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCompleted(String code, String msg, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (code == null || code.isEmpty()) {
                    show_error_box("Invalid Response from Server", "No Response", 0);
                    return;
                }
                if (!code.equals("00")) {
                    show_error_box(msg, code, 1);
                } else {
                    show_error_box(msg, code, 1);
                }
            }

        });
    }

    private void Display(int value) {
        RecyclerView.Adapter adapter;
        RecyclerView recyclerView;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ArrayList<RationListModel> modeldata = new ArrayList<>();
        int commDetailssize = memberConstants.commDetails.size();
        float commprice, commamount, commqty, commclosebal, commbal;
        for (int i = 0; i < commDetailssize; i++) {
            commqty = Float.parseFloat(memberConstants.commDetails.get(i).requiredQty);
            commprice = Float.parseFloat(memberConstants.commDetails.get(i).price);
            commclosebal = Float.parseFloat(memberConstants.commDetails.get(i).closingBal);
            commbal = Float.parseFloat(memberConstants.commDetails.get(i).balQty);
            if (value == 0) {
                if (memberConstants.commDetails.get(i).weighing.equals("N")) {
                    memberConstants.commDetails.get(i).requiredQty = "0.0";
                } else {
                    if (commbal <= commclosebal) {
                        memberConstants.commDetails.get(i).requiredQty = memberConstants.commDetails.get(i).balQty;
                    } else {
                        memberConstants.commDetails.get(i).requiredQty = memberConstants.commDetails.get(i).closingBal;
                    }
                }
            }

            commamount = commprice * commqty;
            memberConstants.commDetails.get(i).amount = String.valueOf(commamount);


            modeldata.add(new RationListModel(memberConstants.commDetails.get(i).commName +
                    "\n(" + memberConstants.commDetails.get(i).totQty + ")",
                    memberConstants.commDetails.get(i).price,
                    memberConstants.commDetails.get(i).balQty,
                    memberConstants.commDetails.get(i).closingBal,
                    memberConstants.commDetails.get(i).requiredQty,
                    memberConstants.commDetails.get(i).amount));
        }
        adapter = new RationListAdapter(context, modeldata, new OnClickRation() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(int p) {

                float commclbal = Float.parseFloat(memberConstants.commDetails.get(p).closingBal);
                float commbal = Float.parseFloat(memberConstants.commDetails.get(p).balQty);
                float commmin = Float.parseFloat(memberConstants.commDetails.get(p).minQty);
                if (!(commclbal < commmin)) {
                    if (!(commbal < commmin)) {

                        if (memberConstants.commDetails.get(p).weighing.equals("N")) {
                            if (choice != 0) {
                                MESSAGE_FROM_SERIAL_PORT = 1;
                                WeighingDialog(p);
                            } else {
                                show_error_box("Please select Communication mode", "Communication", 0);
                            }
                        } else {
                            ManualDialog(p);
                        }
                    } else {
                        show_error_box("Balance Quantity is Not Available ", (memberConstants.commDetails.get(p).commName), 0);
                    }
                } else {
                    show_error_box("Closing Balance Quantity is Not Available ", (memberConstants.commDetails.get(p).commName), 0);

                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void WeighingDialog(final int position) {
        try {
            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.activity_weighing);
            getweight = dialog.findViewById(R.id.weigh);

            weightstatus = dialog.findViewById(R.id.weight_status);
            Button confirm = (Button) dialog.findViewById(R.id.confirm);
            Button back = (Button) dialog.findViewById(R.id.back);

            TextView commName = (TextView) dialog.findViewById(R.id.a);
            TextView price = (TextView) dialog.findViewById(R.id.b);
            TextView balQty = (TextView) dialog.findViewById(R.id.c);
            TextView closingBal = (TextView) dialog.findViewById(R.id.d);

            commName.setText(memberConstants.commDetails.get(position).commName);
            price.setText(memberConstants.commDetails.get(position).price);
            balQty.setText(memberConstants.commDetails.get(position).balQty);
            closingBal.setText(memberConstants.commDetails.get(position).closingBal);
            final Button get = dialog.findViewById(R.id.weighing_get);
            get.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    getflag = true;
                    if (choice == 2) {
                        MESSAGE_FROM_SERIAL_PORT = 0;
                        getweightUSB();
                    } else if (choice == 1) {
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter.isEnabled()) {
                            if (address != null) {
                                MESSAGE_FROM_SERIAL_PORT = 0;
                                if (btSocket != null) {
                                    if (btSocket.isConnected()) {
                                        getbtvalue(btSocket);
                                    } else {
                                        pd = ProgressDialog.show(context, "Connt", "Please_Wait", true, false);
                                        checkBTState(mBluetoothAdapter);
                                    }
                                } else {
                                    pd = ProgressDialog.show(context, "Connt", "Please_Wait", true, false);
                                    checkBTState(mBluetoothAdapter);
                                }
                            } else {
                                show_error_box("Device Not Found", "Bluetooth Device", 0);
                            }
                        } else {
                            show_error_box(context.getResources().getString(R.string.Enable_Bluetooth_and_pair_your_Device_Manually), context.getResources().getString(R.string.Bluetooth), 0);
                        }
                    }

                }
            });

            confirm.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    MESSAGE_FROM_SERIAL_PORT = 1;
                    if (getflag) {
                        String weighingweight = getweight.getText().toString();
                        System.out.println(weighingweight.length());
                        if (weighingweight.length() > 10) {
                            CheckWeight(position, weighingweight, 1);

                        } else {
                            show_error_box("NOt a Proper Weight ", "Weighing Weight Error", 0);
                        }

                   /* memberConstants.commDetails.get(position).requiredQty = String.valueOf(value);
                    Display(1);*/
                    } else {
                        Toast.makeText(context, "Please get Weight ", Toast.LENGTH_SHORT).show();
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
        } catch (Exception e) {
            String ex = e.getMessage();
            show_error_box(ex, "WeighingDialog Exception", 0);

        }
    }

    private void getweightUSB() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (device == null) {
                        device = usbService.findSerialPortDevice(context);
                    }
                    if (device != null) {
                        if (connection == null) {
                            connection = usbService.connection(device);
                        }
                        if (connection != null) {
                            if (serialport == null) {
                                serialport = usbService.getserialport(device, connection);
                            }
                            if (serialport != null) {
                                serialport.syncOpen();
                                serialport.setBaudRate(9600);
                                serialport.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                serialport.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                serialport.setParity(UsbSerialInterface.PARITY_NONE);
                                serialport.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                StringBuilder st = new StringBuilder();

                                byte[] buffer = new byte[100];
                                for (int i = 0; i < 11; i++) {
                                    int n = serialport.syncRead(buffer, 0);
                                    if (n > 0) {
                                        byte[] received = new byte[n];
                                        System.arraycopy(buffer, 0, received, 0, n);
                                        String receivedStr = new String(received);
                                        if (receivedStr.equals("+")) {
                                            st.setLength(0);
                                            buffer = new byte[100];
                                            System.out.println("++++++++++" + st);
                                            i = 0;
                                        }
                                        st.append(receivedStr);
                                        if (st.toString().contains("g")) {
                                            if (mHandler != null) {
                                                mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, st).sendToTarget();
                                            }
                                        }
                                    }
                                }
                            } else {
                                System.out.println("SerialPort connection failed");
                                if (mHandler != null) {
                                    mHandler.obtainMessage(1, "SerialPort Connection Failed").sendToTarget();
                                }

                                // getweight.setText("Failed Serialport");
                            }
                        } else {
                            System.out.println("Failed to connect");
                            if (mHandler != null) {
                                mHandler.obtainMessage(2, "Failed to Connect").sendToTarget();
                            }

                            //getweight.setText("Failed to connect");
                        }
                    } else {

                        System.out.println("Device Not Found");
                        if (mHandler != null) {
                            mHandler.obtainMessage(3, "Device NOt Found").sendToTarget();
                        }

                        // getweight.setText("Device Not found");
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    EditText weight;
    TextView getweight, weightstatus;
    boolean getflag = false;

    private void ManualDialog(final int position) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.activity_weight);
        weight = dialog.findViewById(R.id.enter);

        weightstatus = dialog.findViewById(R.id.weight_status);
        Button confirm = (Button) dialog.findViewById(R.id.confirm);
        Button back = (Button) dialog.findViewById(R.id.back);

        TextView commName = (TextView) dialog.findViewById(R.id.a);
        TextView price = (TextView) dialog.findViewById(R.id.b);
        TextView balQty = (TextView) dialog.findViewById(R.id.c);
        TextView closingBal = (TextView) dialog.findViewById(R.id.d);

        commName.setText(memberConstants.commDetails.get(position).commName);
        price.setText(memberConstants.commDetails.get(position).price);
        balQty.setText(memberConstants.commDetails.get(position).balQty);
        closingBal.setText(memberConstants.commDetails.get(position).closingBal);


        confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String enteredweight = weight.getText().toString();
                if (enteredweight != null && !enteredweight.isEmpty()
                        && enteredweight.length() > 0) {
                    if (checkdotvalue(enteredweight)) {
                        float value = Float.parseFloat(enteredweight);
                        if (value == 0.0) {
                            memberConstants.commDetails.get(position).requiredQty = String.valueOf(value);
                            Display(1);
                        } else if (value > 0.0) {
                            CheckWeight(position, enteredweight, 0);
                        }
                    } else {
                        show_error_box("Please Enter Valid Values", "Invalid Input", 0);

                    }
                } else {
                    show_error_box("Please Enter Valid values", "Invalid Input", 0);

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CheckWeight(int position, String enteredweight, int i) {
        if (enteredweight != null && !enteredweight.isEmpty() && !enteredweight.equals("null")) {
            float requiredQty = verify_Weight(position, enteredweight, i);
            if (requiredQty >= 0) {
                int calculated = cal(requiredQty, position);
                if (calculated == 0) {
                    memberConstants.commDetails.get(position).requiredQty = String.valueOf(requiredQty);
                    Display(1);
                } else if (calculated == 2) {
                    show_error_box(context.getResources().getString(R.string.Please_Issue_Commodity_upto_Bal_Qty_only),
                            memberConstants.commDetails.get(position).commName, 0);
                } else if (calculated == 3) {
                    show_error_box("Please Enter the  Quantity greater than or Equal to " + memberConstants.commDetails.get(position).minQty,
                            memberConstants.commDetails.get(position).commName, 0);
                } else {
                    show_error_box("Please Enter the Quantity less than or Equal to " + memberConstants.commDetails.get(position).closingBal,
                            memberConstants.commDetails.get(position).commName, 0);
                }
            } else {
                show_error_box("Issue Qty should be Multiple by Minimum Qty -"
                                + memberConstants.commDetails.get(position).minQty,
                        memberConstants.commDetails.get(position).commName, 0);
            }
        } else {
            show_error_box(context.getResources().getString(R.string.Please_Enter_the_Weight), context.getResources().getString(R.string.Enter_valid_weight), 0);
        }
    }

    private int cal(float requiredQty, int position) {
        float price, balQty, closingBal, minQty;
        price = Float.parseFloat(memberConstants.commDetails.get(position).price);
        memberConstants.commDetails.get(position).amount = String.valueOf((requiredQty * price));
        balQty = Float.parseFloat(memberConstants.commDetails.get(position).balQty);
        closingBal = Float.parseFloat(memberConstants.commDetails.get(position).closingBal);
        minQty = Float.parseFloat(memberConstants.commDetails.get(position).minQty);
        if (requiredQty > balQty) {
            return 2;
        } else if (requiredQty < minQty) {
            return 3;
        } else if (requiredQty > closingBal) {
            return 4;
        } else {
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private float verify_Weight(int position, String com, int check) {
        try {
            String weight;
            float minQty, verifiedWeight, modules, plus_mins;
            String m = "0.0" + dealerConstants.fpsCommonInfo.weighAccuracyValueInGms;
            plus_mins = Float.parseFloat(m);
            minQty = Float.parseFloat((memberConstants.commDetails.get(position).minQty));
            if (check == 1) {
                weight = com.substring(1, 8);
                verifiedWeight = Float.parseFloat(weight);
                modules = verifiedWeight % minQty;
                if (modules == (float) 0) {
                    return verifiedWeight;
                }
                float ky = (modules - plus_mins);
                float kx = (modules + plus_mins);
                if (kx >= minQty) {
                    verifiedWeight = verifiedWeight - modules;
                    verifiedWeight = verifiedWeight + minQty;
                    return verifiedWeight;
                }
                if (ky <= (float) 0) {
                    verifiedWeight = verifiedWeight - modules;
                    return verifiedWeight;
                }
            } else {
                verifiedWeight = Float.parseFloat((com));
                modules = verifiedWeight % minQty;
                if (modules == (float) 0) {
                    return verifiedWeight;
                }
            }
        } catch (Exception ex) {
            show_error_box(ex.getMessage(), "Verify_Weight", 0);
        }
        return -1;

    }

    private String add_comm() {
        TOTALAMOUNT = 0.0;
        StringBuilder add = new StringBuilder();
        String str;
        int userCommModelssize = memberConstants.commDetails.size();
        float commqty, commprice, commamount;
        if (userCommModelssize > 0) {
            for (int i = 0; i < userCommModelssize; i++) {
                commqty = Float.parseFloat((memberConstants.commDetails.get(i).requiredQty));
                if (commqty > 0.0) {
                    commqty = Float.parseFloat(memberConstants.commDetails.get(i).requiredQty);
                    commprice = Float.parseFloat(memberConstants.commDetails.get(i).price);
                    commamount = commprice * commqty;
                    TOTALAMOUNT = TOTALAMOUNT + commamount;
                    str = "<commodityDetail>\n" +
                            "<allocationType>" + memberConstants.commDetails.get(i).allocationType + "</allocationType>\n" +
                            "<allotedMonth>" + memberConstants.commDetails.get(i).allotedMonth + "</allotedMonth>\n" +
                            "<allotedYear>" + memberConstants.commDetails.get(i).allotedYear + "</allotedYear>\n" +
                            "<commCode>" + memberConstants.commDetails.get(i).commcode + "</commCode>\n" +
                            "<commName>" + memberConstants.commDetails.get(i).commName + "</commName>\n" +
                            "<requiredQuantity>" + memberConstants.commDetails.get(i).requiredQty + "</requiredQuantity>\n" +
                            "<commodityAmount>" + memberConstants.commDetails.get(i).price + "</commodityAmount>\n" +
                            "<price>" + memberConstants.commDetails.get(i).amount + "</price>\n" +
                            "</commodityDetail>\n";
                    add.append(str);
                }
            }
            if (add.length() > 0) {
                return String.valueOf(add);
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void conformRation() {
        String com = add_comm();
        if (com != null && com.length() > 0) {
            String ration = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns1:getCommodityTransaction>\n" +
                    "            <fpsSessionId>" + dealerConstants.fpsCommonInfo.fpsSessionId + "</fpsSessionId>\n" +
                    "            <stateCode>" + dealerConstants.stateBean.stateCode + "</stateCode>\n" +
                    "            <exRCNumber>" + memberConstants.carddetails.rcId + "</exRCNumber>\n" +
                    "            <shop_no>" + dealerConstants.stateBean.statefpsId + "</shop_no>\n" +
                    "            <deviceId>" + DEVICEID + "</deviceId>\n" +
                    "            <rationCardId>" + memberConstants.carddetails.rcId + "</rationCardId>\n" +
                    "            <schemeId>" + memberConstants.carddetails.schemeId + "</schemeId>\n" +
                    com + "\n" +
                    "            <recieptId>3863389061819</recieptId>\n" +
                    "            <totAmount>" + TOTALAMOUNT + "</totAmount>\n" +
                    "            <uid_no>" + memberModel.uid + "</uid_no>\n" +
                    "            <uid_ref_no>" + Ref + "</uid_ref_no>\n" +
                    "            <card_type></card_type>\n" +
                    "            <password>" + dealerConstants.fpsURLInfo.token + "</password>\n" +
                    "            <memberId>" + memberModel.zmemberId + "</memberId>\n" +
                    "            <surveyEntryQuantity>0.0</surveyEntryQuantity>\n" +
                    "            <surveyStatus>N</surveyStatus>\n" +
                    "            <trans_type>F</trans_type>\n" +
                    "            <availedBenfName>" + memberModel.memberName + "</availedBenfName>\n" +
                    "        </ns1:getCommodityTransaction>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>";
            if (Util.networkConnected(context)) {

                Util.generateNoteOnSD(context, "RationReq.txt", ration);
                hitURL(ration);
            } else {
                show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection), 0);
            }
        } else {
            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {

            } else {
                mp = mp.create(context, R.raw.c100189);
                mp.start();
                show_error_box("", "Please Select any Commodity for Issuance", 0);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void hitURL(String xmlformat) {
        Intent p = new Intent(getApplicationContext(), PrintActivity.class);
        p.putExtra("key", xmlformat);
        p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(p);
    }

    public interface OnClickRation {
        void onClick(int p);
    }


    private void checkBTState(BluetoothAdapter mBluetoothAdapter) {

        new Thread(() -> {
            if (btSocket == null && mBluetoothAdapter != null && address != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    System.out.println("Socket_creation_failed");
                }
            }

            if (btSocket != null && !btSocket.isConnected()) {
                try {
                    btSocket.connect();
                } catch (IOException e) {
                    address = null;
                    e.printStackTrace();
                    try {
                        btSocket.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }

            runOnUiThread(() -> {
                if (btSocket.isConnected()) {
                    Toast.makeText(context, "Bluetooth Weighing Machine is Ready"
                            , Toast.LENGTH_SHORT).show();
                }
                if (pd.isShowing())
                    pd.dismiss();
            });

            if (btSocket.isConnected()) {
                getbtvalue(btSocket);
            }

           /* ConnectedThread mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();*/
        }).start();


    }

    private void getbtvalue(BluetoothSocket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream tmpIn = socket.getInputStream();
                    byte[] buffer = new byte[100];
                    int bytes;
                    StringBuilder storeBT = new StringBuilder();
                    for (int i = 0; i < 11; i++) {
                        bytes = tmpIn.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                        if (readMessage.equals("+")) {
                            storeBT.setLength(0);
                            buffer = new byte[100];
                            System.out.println("++++++++++" + storeBT);
                            i = 0;
                        }
                        storeBT.append(readMessage);
                        if (storeBT.toString().contains("g")) {
                            if (mHandler != null) {
                                mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, storeBT).sendToTarget();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

   /* private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException ignored) {
            }
            mmInStream = tmpIn;
        }

        public void run() {

            byte[] buffer = new byte[2048];
            int bytes;
            StringBuilder storeBT = new StringBuilder();
            String wt;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    if (MESSAGE_FROM_SERIAL_PORT == 0) {
                        storeBT.append(readMessage);
                        if (storeBT.toString().contains("g")) {
                            wt = storeBT.toString().trim();
                            storeBT.setLength(0);
                            final String finalWt = wt;
                            RationDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (finalWt.length() < 12) {
                                        getweight.setText(finalWt);
                                    }
                                }
                            });
                        }
                    } else {
                        usb = "";
                        storeBTdata.setLength(0);
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btSocket != null && btSocket.isConnected()) {
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("++++++++++++++++");
            }
        }

        unregisterReceiver(mUsbReceiver);
        unregisterReceiver(mReceiver);
    }


    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
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
                System.out.println("Device Not Found");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device1 = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device1);
                if (device1 != null) {
                    if (device1.getName() != null) {
                        if (device1.getName().equalsIgnoreCase("APPDS_VNTK@2015") ||
                                device1.getName().equalsIgnoreCase("VTWS100")) {
                            device1.createBond();
                            mBluetoothAdapter.cancelDiscovery();
                            mProgressDlg.dismiss();
                        }
                    }
                }
            }
        }
    };

    private void setFiltersbluetooth() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver, filter);
    }
    public  String address;
    @Override
    protected void onResume() {
        super.onResume();

        setFilters();
        startService(UsbService.class, usbConnection, null);
        setFiltersbluetooth();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("APPDS_VNTK@2015") || device.getName().equals("VTWS100")) {
                        address = (device.getAddress());
                        return;
                    }
                }
            }
            mBluetoothAdapter.startDiscovery();
        }
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private class MyHandler extends Handler {
        private final WeakReference<RationDetailsActivity> mActivity;

        MyHandler(RationDetailsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_FROM_SERIAL_PORT) {
                final StringBuilder data = (StringBuilder) msg.obj;
                if (data != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getweight.setText(data);
                        }
                    });
                }
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "NOt granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No_USB_connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    device = null;
                    Toast.makeText(context, "USB_disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    device = null;
                    Toast.makeText(context, "USB_device_not_supported", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DETACHED: // USB NOT SUPPORTED
                    device = null;
                    Toast.makeText(context, "USB_device_not_supported", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_ATTACHED: // USB NOT SUPPORTED
                    usbService.findSerialPortDevice(context);
                    Toast.makeText(context, "USB_device_not_supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (connection == null) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);

            Intent bindingIntent = new Intent(this, service);
            bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        filter.addAction(UsbService.ACTION_USB_ATTACHED);
        filter.addAction(UsbService.ACTION_USB_DETACHED);
        filter.addAction(UsbService.ACTION_USB_READY);
        filter.addAction(UsbService.ACTION_USB_READY);
        registerReceiver(mUsbReceiver, filter);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
   /* @SuppressLint("HandlerLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data == null) {
                return;
            }
            checkBTState(mBluetoothAdapter);
        }
    }*/

    private void show_error_box(String msg, String title, final int i) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                        if (i == 1) {
                            finish();
                        }
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
        TextView toolbarCard = findViewById(R.id.toolbarCard);
        toolbarCard.setText("RC : " + memberConstants.carddetails.rcId);
        String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
        System.out.println(appversion);
        toolbarVersion.setText("V" + appversion);

        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("ISSUE GOODS");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}

   /* private void checkBTState(BluetoothAdapter mBluetoothAdapter, int n) {


        if (btSocket==null || !btSocket.isConnected()) {
            //handler();

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), context.getResources().getString(R.string.Socket_creation_failed), Toast.LENGTH_LONG).show();
            }
            try {
                btSocket.connect();

            } catch (IOException e) {
                String msg;
                msg = String.valueOf(e);
                try {
                    btSocket.close();
                } catch (IOException ignored) {
                    msg = String.valueOf(ignored);
                }
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                pd = ProgressDialog.show(context, context.getResources().getString(R.string.Connecting), context.getResources().getString(R.string.Please_Wait), true, false);
                if (n < 3)
                    checkBTState(mBluetoothAdapter, n + 1);
                return;
            }
        }

        if (pd.isShowing()) {
            pd.dismiss();
        }
        ConnectedThread mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    private void handler() {
        bluetoothIn = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_FROM_SERIAL_PORT) {
                    String data = (String) msg.obj;
                    storeBTdata.append(data);
                    if (storeBTdata.toString().contains("g")) {
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                        usb = String.valueOf(storeBTdata).trim();
                        storeBTdata.setLength(0);
                        System.out.println("BLUETOOTH====" + usb);
                        //getweight.setText(bt);
                    }
                }
            }
        };
    }*/
/* private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException ignored) {
            }
            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[2048];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    if (MESSAGE_FROM_SERIAL_PORT == 0) {
                        bluetoothIn.obtainMessage(MESSAGE_FROM_SERIAL_PORT, bytes, -1, readMessage).sendToTarget();
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
    }*/