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
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.visiontek.Mantra.Models.DATAModels.MemberListModel;
import com.visiontek.Mantra.Models.DATAModels.RationListModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.MemberModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.DecimalDigitsInputFilter;
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

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Models.AppConstants.TOTALAMOUNT;
import static com.visiontek.Mantra.Utils.Util.checkdotvalue;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;


public class RationDetailsActivity extends AppCompatActivity {
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Spinner options;
    public int MESSAGE_FROM_SERIAL_PORT;

    public int choice;
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter mBluetoothAdapter;
    private UsbService usbService;
    private MyHandler mHandler;

    UsbSerialDevice serialport = null;
    UsbDeviceConnection connection = null;
    UsbDevice device = null;
    String weight_Data;
    //--------------------------------------------------------------------------------
    Button confirm, back;
    Context context;
    ProgressDialog pd = null;
    MemberModel memberModel;
    public String
            reasonName,
            reasonid,
            Ref;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ration_details);
        context = RationDetailsActivity.this;
        try {


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
            mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", ((dialog, which) -> {
                dialog.dismiss();
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.cancelDiscovery();
            }));
            // mProgressDlg.show();

            String[] items = new String[]{"Comm Mode", "Bluetooth", "USB"};
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
                            for (BluetoothDevice device : pairedDevices) {
                                if ((device.getName().equals("VTWS100"))) {
                                    address = (device.getAddress());
                                    break;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        } catch (Exception ex) {

            Timber.tag("Ration-onCreate-").e(ex.getMessage(), "");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initilisation() {
        pd = new ProgressDialog(context);
        options = findViewById(R.id.options);
        confirm = findViewById(R.id.confirm);
        back = findViewById(R.id.ration_back);
        mHandler = new MyHandler(this);
        toolbarInitilisation();
    }

    private void dialog() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(RationDetailsActivity.this);
        builderSingle.setTitle(context.getResources().getString(R.string.Please_Select_Any_Option));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(RationDetailsActivity.this, android.R.layout.select_dialog_singlechoice);
        int reasonBeanListssize = dealerConstants.reasonBeanLists.size();
        for (int i = 0; i < reasonBeanListssize; i++) {
            arrayAdapter.add(dealerConstants.reasonBeanLists.get(i).reasonValue);
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
                reasonName = arrayAdapter.getItem(which);
                reasonid = dealerConstants.reasonBeanLists.get(which).reasonId;
                cancelRequest();
            }
        });
        builderSingle.show();
    }

    private void cancelRequest() {
        try {

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
            //Util.generateNoteOnSD(context, "CancelRequestReq.txt", reasons);
            Show(context.getResources().getString(R.string.Please_wait),context.getResources().getString(R.string.Processing));

            Json_Parsing request = new Json_Parsing(context, reasons, 2);
            request.setOnResultListener(new Json_Parsing.OnResultListener() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onCompleted(String code, String msg, Object object) {
                    Dismiss();
                    if (code == null || code.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Reasonreq),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "",
                                0);
                        return;
                    }

                    if (!code.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Reasonreq),
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                0);
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Reasonreq),
                                context.getResources().getString(R.string.ResponseCode)+code,
                                context.getResources().getString(R.string.ResponseMsg)+msg,
                                1);
                    }
                }

            });
        } catch (Exception ex) {

            Timber.tag("Ration-CnclReq-").e(ex.getMessage(), "");
        }
    }

    private void Display(int value) {
        try {

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
                    if (memberConstants.commDetails.get(i).weighing.equals("Y")) {
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


                if(L.equals("hi")){
                    modeldata.add(new RationListModel(
                            memberConstants.commDetails.get(i).commNamell +
                                    "\n(" + memberConstants.commDetails.get(i).totQty + ")",
                            memberConstants.commDetails.get(i).price,
                            memberConstants.commDetails.get(i).balQty,
                            memberConstants.commDetails.get(i).closingBal,
                            memberConstants.commDetails.get(i).requiredQty,
                            memberConstants.commDetails.get(i).amount));
                }else {
                    modeldata.add(new RationListModel(
                            memberConstants.commDetails.get(i).commName +
                                    "\n(" + memberConstants.commDetails.get(i).totQty + ")",
                            memberConstants.commDetails.get(i).price,
                            memberConstants.commDetails.get(i).balQty,
                            memberConstants.commDetails.get(i).closingBal,
                            memberConstants.commDetails.get(i).requiredQty,
                            memberConstants.commDetails.get(i).amount));
                }
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

                            if (memberConstants.commDetails.get(p).weighing.equals("Y")) {
                                if (choice != 0) {
                                    MESSAGE_FROM_SERIAL_PORT = 0;
                                    WeighingDialog(p);
                                } else {
                                    show_AlertDialog(
                                            context.getResources().getString(R.string.Weighing_machine),
                                            context.getResources().getString(R.string.Please_Select_Other_Mode_of_Communication),
                                            "",
                                            0);
                                }
                            } else {
                                ManualDialog(p);
                            }
                        } else {
                            show_AlertDialog(
                                    (memberConstants.commDetails.get(p).commName),
                                    context.getResources().getString(R.string.Commodity_Balance_Qty_not_available_for_this_Entered_Card),
                                    "",
                                    0);
                        }
                    } else {
                        show_AlertDialog(
                                (memberConstants.commDetails.get(p).commName),
                                context.getResources().getString(R.string.Commodity_Closing_balance_not_available_for_this_shop),
                                "",
                                0);

                    }
                }
            });
            recyclerView.setAdapter(adapter);
        } catch (Exception ex) {

            Timber.tag("Ration-Display-").e(ex.getMessage(), "");
        }
    }

    Button get;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void WeighingDialog(final int position) {

        getflag=false;
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
            if(L.equals("hi")){
                commName.setText(memberConstants.commDetails.get(position).commNamell);
            }else {
                commName.setText(memberConstants.commDetails.get(position).commName);
            }
            commName.setText(memberConstants.commDetails.get(position).commName);
            price.setText(memberConstants.commDetails.get(position).price);
            balQty.setText(memberConstants.commDetails.get(position).balQty);
            closingBal.setText(memberConstants.commDetails.get(position).closingBal);
            get = dialog.findViewById(R.id.weighing_get);
            get.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    get.setEnabled(false);
                    getflag = true;
                    if (choice == 2) {
                        MESSAGE_FROM_SERIAL_PORT = 0;
                        getweightUSB();
                    } else if (choice == 1) {

                        if (mBluetoothAdapter.isEnabled()) {
                            if (address != null) {
                                if (btSocket != null) {
                                    MESSAGE_FROM_SERIAL_PORT = 0;
                                    if (btSocket.isConnected()) {
                                        getbtvalue(btSocket);
                                        getbtvalue(btSocket);
                                    } else {
                                        pd = ProgressDialog.show(context, "Connecting", "Please_Wait", true, false);
                                        checkBTState(mBluetoothAdapter);
                                    }
                                } else {
                                    pd = ProgressDialog.show(context, "Connecting", "Please_Wait", true, false);
                                    checkBTState(mBluetoothAdapter);
                                }
                            } else {
                                get.setEnabled(true);
                                show_AlertDialog(
                                        context.getResources().getString(R.string.Bluetooth),
                                        context.getResources().getString(R.string.Device_Not_Found),
                                        "", 0);
                            }
                        } else {
                            get.setEnabled(true);
                            show_AlertDialog(context.getResources().getString(R.string.Please_check_your_bluetooth_connection),
                                    context.getResources().getString(R.string.Bluetooth),
                                    "",0);
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
                            show_AlertDialog(
                                    context.getResources().getString(R.string.Invalid_Weight),
                                    context.getResources().getString(R.string.Please_try_again),
                                    "",
                                    0);
                        }

                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Please_get_Weight_from_Weighing_Machine),
                                "" ,
                                "",
                                0);
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

            Timber.tag("Ration-Weighing-").e(e.getMessage(), "");
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
                        connection = usbService.connection(device);
                        if (connection == null) {
                            connection = usbService.connection(device);
                        }
                        if (connection != null) {
                            serialport = usbService.getserialport(device, connection);
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

                                for (int i = 0; i < 50; i++) {
                                    int n = serialport.syncRead(buffer, 0);
                                    if (n > 0) {
                                        byte[] received = new byte[n];
                                        System.arraycopy(buffer, 0, received, 0, n);
                                        String receivedStr = new String(received);
                                        st.append(receivedStr);
                                    }
                                }
                                if (mHandler != null) {
                                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, st).sendToTarget();
                                    MESSAGE_FROM_SERIAL_PORT = 1;
                                }
                            } else {

                                if (mHandler != null) {
                                    mHandler.obtainMessage(2,  context.getResources().getString(R.string.Connection_Failed)).sendToTarget();
                                }
                            }
                        } else {

                            if (mHandler != null) {
                                mHandler.obtainMessage(2, context.getResources().getString(R.string.Connection_Failed)).sendToTarget();
                            }
                        }
                    } else {
                        if (mHandler != null) {
                            mHandler.obtainMessage(2,  context.getResources().getString(R.string.Device_Not_Found)).sendToTarget();
                        }
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
            Timber.tag("Ration-WeightUSB-").e(e.getMessage(), "");
        }

    }

    EditText weight;
    TextView getweight, weightstatus;
    boolean getflag = false;

    private void ManualDialog(final int position) {
        try {

            final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.activity_weight);
            weight = dialog.findViewById(R.id.enter);
            weight.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(3)});
            weightstatus = dialog.findViewById(R.id.weight_status);
            Button confirm = (Button) dialog.findViewById(R.id.confirm);
            Button back = (Button) dialog.findViewById(R.id.back);

            TextView commName = (TextView) dialog.findViewById(R.id.a);
            TextView price = (TextView) dialog.findViewById(R.id.b);
            TextView balQty = (TextView) dialog.findViewById(R.id.c);
            TextView closingBal = (TextView) dialog.findViewById(R.id.d);

            if(L.equals("hi")){
                commName.setText(memberConstants.commDetails.get(position).commNamell);
            }else {
                commName.setText(memberConstants.commDetails.get(position).commName);
            }
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
                            show_AlertDialog(
                                    context.getResources().getString(R.string.Invalid_Inputs),
                                    context.getResources().getString(R.string.Please_enter_a_valid_Value),
                                    "",
                                    0);


                        }
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Invalid_Inputs),
                                context.getResources().getString(R.string.Please_enter_a_valid_Value),
                                "",
                                0);

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
        } catch (Exception ex) {

            Timber.tag("Ration-ManualDilg-").e(ex.getMessage(), "");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CheckWeight(int position, String enteredweight, int i) {
        try {

            if (enteredweight != null && !enteredweight.isEmpty() && !enteredweight.equals("null")) {
                float requiredQty = verify_Weight(position, enteredweight, i);
                if (requiredQty >= 0) {
                    int calculated = cal(requiredQty, position);
                    if (calculated == 0) {
                        memberConstants.commDetails.get(position).requiredQty = String.valueOf(requiredQty);
                        Display(1);
                    } else if (calculated == 2) {
                        show_AlertDialog(
                                memberConstants.commDetails.get(position).commName,
                                context.getResources().getString(R.string.Please_Issue_Commodity_upto_Bal_Qty_only)+memberConstants.commDetails.get(position).balQty,
                                "",
                                0);

                    } else if (calculated == 3) {
                        show_AlertDialog(
                                memberConstants.commDetails.get(position).commName,
                                context.getResources().getString(R.string.Issue_quantity_should_more_than_or_equal_to_Minimum_quantity)+memberConstants.commDetails.get(position).minQty,
                                "",
                                0);
                    } else if (calculated == 4){
                        show_AlertDialog(
                                memberConstants.commDetails.get(position).commName,
                                context.getResources().getString(R.string.Please_Issue_Commodity_upto_Closing_Balance_only_Closing_Balance)+memberConstants.commDetails.get(position).closingBal,
                                "",
                                0);

                    }else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Enter_valid_weight),
                                context.getResources().getString(R.string.Please_Enter_the_Weight),
                                "",
                                0);
                    }
                } else {
                    show_AlertDialog(
                            memberConstants.commDetails.get(position).commName,
                            context.getResources().getString(R.string.Issue_Qty_Should_Be_Multiple_By_Minimum_Qty)+memberConstants.commDetails.get(position).minQty,
                            "",
                            0);

                }
            } else {
                show_AlertDialog(
                        context.getResources().getString(R.string.Enter_valid_weight),
                        context.getResources().getString(R.string.Please_Enter_the_Weight),
                        "",
                        0);

            }
        } catch (Exception ex) {

            Timber.tag("Ration-check-").e(ex.getMessage(), "");
        }
    }

    private int cal(float requiredQty, int position) {
        try {

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
        } catch (Exception ex) {

            Timber.tag("Ration-W8conditions-").e(ex.getMessage(), "");
            return 5;
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

            Timber.tag("Ration-Verify-").e(ex.getMessage(), "");
        }
        return -1;

    }

    private String add_comm() {
        try {
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
                                "<commodityAmount>" +memberConstants.commDetails.get(i).amount  + "</commodityAmount>\n" +
                                "<price>" +  memberConstants.commDetails.get(i).price + "</price>\n" +
                                "</commodityDetail>\n";
                        add.append(str);
                    }
                }
                if (add.length() > 0) {
                    return String.valueOf(add);
                }
            }
        } catch (Exception ex) {

            Timber.tag("Ration-add_comm-").e(ex.getMessage(), "");
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void conformRation() {
        try {

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
                        "            <trans_type>"+memberModel.trans_type+"</trans_type>\n" +
                        "            <availedBenfName>" + memberModel.memberName + "</availedBenfName>\n" +
                        "        </ns1:getCommodityTransaction>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>";
                if (Util.networkConnected(context)) {

                    //Util.generateNoteOnSD(context, "RationReq.txt", ration);
                    hitURL(ration);
                } else {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Internet_Connection),
                            context.getResources().getString(R.string.Internet_Connection_Msg),
                            "",
                            0);
                }
            } else {
                if (mp != null) {
                    releaseMediaPlayer(context, mp);
                }
                if (L.equals("hi")) {

                } else {
                    mp = mp.create(context, R.raw.c100189);
                    mp.start();


                }
                show_AlertDialog(
                        context.getResources().getString(R.string.Commodity),
                        context.getResources().getString(R.string.Please_Select_Any_Commodity_For_Issuance),
                        "",
                        0);
            }
        } catch (Exception ex) {

            Timber.tag("Ration-conform-").e(ex.getMessage(), "");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void hitURL(String xmlformat) {
        try {

            Intent p = new Intent(getApplicationContext(), PrintActivity.class);
            p.putExtra("key", xmlformat);
            p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(p);
        } catch (Exception ex) {

            Timber.tag("Ration-onCreate-").e(ex.getMessage(), "");
        }
    }

    public interface OnClickRation {
        void onClick(int p);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {

            if (btSocket != null && btSocket.isConnected()) {
                try {
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }

            unregisterReceiver(mUsbReceiver);
            //unregisterReceiver(mReceiver);
        } catch (Exception ex) {

            Timber.tag("Ration-OnDestroy-").e(ex.getMessage(), "");
        }
    }


    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onReceive(Context context, Intent intent) {
            try {

                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    mDeviceList = new ArrayList<BluetoothDevice>();

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    mProgressDlg.dismiss();
                    System.out.println("Device Not Found");
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device1 = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDeviceList.add(device1);
                    if (device1 != null) {
                        if (device1.getName() != null) {
                            if (
                                    device1.getName().equalsIgnoreCase("VTWS100")) {
                                device1.createBond();
                                mBluetoothAdapter.cancelDiscovery();
                                mProgressDlg.dismiss();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Timber.tag("Ration-BroadCast-").e(ex.getMessage(), "");
            }
        }
    };



    public String address;

    @Override
    protected void onResume() {
        super.onResume();
        try {

            setFilters();
            startService(UsbService.class, usbConnection, null);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals("VTWS100")){
                            address = (device.getAddress());

                            break;
                        }
                    }
                }
            }else {
                mBluetoothAdapter.enable();
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals(device.getName().equals("VTWS100"))){
                            address = (device.getAddress());

                            break;
                        }
                    }
                }
            }

        } catch (Exception ex) {

            Timber.tag("Ration-onResume-").e(ex.getMessage(), "");
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

    private void checkBTState(BluetoothAdapter mBluetoothAdapter) {
        try {

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
                        e.printStackTrace();
                        try {
                            btSocket.close();
                        } catch (IOException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }

                runOnUiThread(() -> {
                    get.setEnabled(true);
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

            }).start();
        } catch (Exception ex) {
            Timber.tag("Ration-CheckBTState-").e(ex.getMessage(), "");
        }

    }

    private void getbtvalue(BluetoothSocket socket) {
        try {
            System.out.println("@@In getbtvalue");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        InputStream tmpIn = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int bytes;
                        StringBuilder data = new StringBuilder();
                        for (int i = 0; i < 50; i++) {
                            int n = tmpIn.read(buffer);
                            System.out.println("@@n value: " + n);
                            if (n > 100) {
                                i = 20;
                            } else {

                                if (n > 0) {
                                    System.out.println("@@Value of n: " + n);
                                    byte[] received = new byte[n];
                                    System.arraycopy(buffer, 0, received, 0, n);

                                    String receivedStr = new String(received);
                                    data.append(receivedStr);

                                    System.out.println("@@Data in storeBT" + data);
                                }

                            }
                        }

                        if (data != null) {
                            String temp = "";
                            for (int i = 0; i < data.length()-11; i++) {
                                temp = "";
                                if (data.charAt(i) == '+') {
                                    temp = data.substring(i, i + 4);
                                    i = i + 4;
                                    if (data.charAt(i) == '.') {
                                        temp = temp + data.substring(i, i + 4);
                                        i = i + 4;
                                        if (data.charAt(i) == ' ' ) {
                                            temp = temp + data.substring(i, i + 3);
                                            i = i + 3;
                                            weight_Data = temp;
                                            getweight.setText(weight_Data);
                                            i =data.length();
                                        }
                                    }
                                }
                            }



                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        getweight.setText(weight_Data);
                                        get.setEnabled(true);
                                    } catch (Exception ex) {
                                        Timber.tag("Ration-myhandler1-").e(ex.getMessage(), "");
                                    }

                                }
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception ex) {

            Timber.tag("Ration-getbtvalue-").e(ex.getMessage(), "");
        }

    }
    private class MyHandler extends Handler {

        private final WeakReference<RationDetailsActivity> mActivity;

        MyHandler(RationDetailsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0) {

                    MESSAGE_FROM_SERIAL_PORT = 1;
                    final StringBuilder data = (StringBuilder) msg.obj;
                    if (data != null) {
                        StringBuilder value = new StringBuilder();

                        int index = data.lastIndexOf("g");
                        int index1 = data.lastIndexOf("+");

                        if(index1>index)
                        {
                            String data_1 = data.substring(0,index+1);
                            index1 = data_1.lastIndexOf("+");
                            index = data_1.lastIndexOf("g");
                            weight_Data  = data_1.substring(index1,index+1);
                        }
                        else{
                            weight_Data  = data.substring(index1,index+1);

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    getweight.setText(weight_Data);
                                    get.setEnabled(true);
                                } catch (Exception ex) {
                                    Timber.tag("Ration-myhandler1-").e(ex.getMessage(), "");
                                }

                            }
                        });

                    }
                } else if (msg.what == 2) {
                    final String data = (String) msg.obj;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                get.setEnabled(true);
                                show_AlertDialog(
                                        "USB",
                                        data,
                                        "",
                                        0);
                            } catch (Exception ex) {
                                Timber.tag("Ration-Myhandler2-").e(ex.getMessage(), "");
                            }
                        }
                    });
                }
            }catch (Exception ex){
                Timber.tag("Ration-Myhandler-").e(ex.getMessage(), "");
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                switch (Objects.requireNonNull(intent.getAction())) {
                    case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                        break;
                    case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                        break;
                    case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                        break;
                    case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                        device = null;
                        break;
                    case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                        device = null;
                        break;
                    case UsbService.ACTION_USB_DETACHED: // USB NOT SUPPORTED
                        device = null;
                        break;
                    case UsbService.ACTION_USB_ATTACHED: // USB NOT SUPPORTED
                        usbService.findSerialPortDevice(context);
                        break;
                }
            } catch (Exception ex) {

                Timber.tag("Ration-onCreate-").e(ex.getMessage(), "");
            }
        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        try {

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
        } catch (Exception ex) {

            Timber.tag("Ration-startservice-").e(ex.getMessage(), "");
        }
    }

    private void setFilters() {
        try {

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
        } catch (Exception ex) {

            Timber.tag("Ration-usbfilter-").e(ex.getMessage(), "");
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void toolbarInitilisation() {
        try {

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
            toolbarActivity.setText( context.getResources().getString(R.string.ISSUE_GOODS));

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {
            Timber.tag("Ration-Toolbar-").e(ex.getMessage(), "");
        }
    }
    private void show_Dialogbox(String msg,String header) {

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


    private void show_AlertDialog(String headermsg,String bodymsg,String talemsg,int i) {

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

                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (i == 1) {
                    finish();
                }
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
    private void SessionAlert(String headermsg, String bodymsg,String talemsg) {
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
                Intent i = new Intent(context, StartActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }
        });

    }
    public void Dismiss(){
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }
    public void Show(String msg,String title){
        SpannableString ss1=  new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2=  new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);


        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
    }

}



