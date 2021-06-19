package com.visiontek.Mantra.Activities;

import android.annotation.SuppressLint;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.CustomAdapter1;
import com.visiontek.Mantra.Models.DATAModels.DataModel1;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.MemberModel;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.RationDetailsModel;
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

import static com.visiontek.Mantra.Activities.DeviceListActivity.address;
import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
import static com.visiontek.Mantra.Utils.Util.toast;

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
    StringBuilder storeUSBdata = new StringBuilder();
    StringBuilder storeBTdata = new StringBuilder();
    Handler bluetoothIn;
    //--------------------------------------------------------------------------------
    Button confirm, back;
        //    get;
    Context context;
    ProgressDialog pd = null;
    TextView rd;
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

        MESSAGE_FROM_SERIAL_PORT = 0;
        Ref = getIntent().getStringExtra("REF");
        memberModel = (MemberModel) getIntent().getSerializableExtra("OBJ");
        initilisation();
        Display(0);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conformRation();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });


        String[] items = new String[]{"Bluetooth", "USB"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        options.setAdapter(adapter1);
        options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                choice = position;
                System.out.println("SELETED=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        options=findViewById(R.id.options);
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
        currentDateTimeString = "23032021163452";

        String reasons = "{\n" +
                "   \"fpsId\" :" + "\"" + dealerConstants.stateBean.statefpsId + "\"" + ",\n" +
                "   \"modeOfService\" : " + "\"" + mos + "\"" + ",\n" +
                "   \"moduleType\" :" + "\"" + mt + "\"" + ",\n" +
                "   \"rcId\" : " + "\"" + memberConstants.carddetails.rcId + "\"" + ",\n" +
                "   \"requestId\" :" + "\"" +reasonid + "\"" + ",\n" +
                "   \"requestValue\" :" + "\"" + reasonName + "\"" + ",\n" +
                "   \"sessionId\" : " + "\"" + dealerConstants.fpsCommonInfo.fpsSessionId + "\"" + ",\n" +
                "   \"stateCode\" : " + "\"" + dealerConstants.stateBean.stateCode + "\"" + ",\n" +
                "   \"terminalId\" : " + "\"" + DEVICEID + "\"" + ",\n" +
                "   \"timeStamp\" : " + "\"" + currentDateTimeString + "\"" + ",\n" +
                "   \"token\" : " + "\"" + dealerConstants.fpsURLInfo.token + "\"" + "\n" +
                "}";
        Util.generateNoteOnSD(context, "CancelRequestReq.txt", reasons);
        pd = ProgressDialog.show(context, "Please Wait ", context.getResources().getString(R.string.Processing), true, false);
        Json_Parsing request = new Json_Parsing(context, reasons, 2);
        request.setOnResultListener(new Json_Parsing.OnResultListener() {

            @Override
            public void onCompleted(String code, String msg, Object object) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (code == null || code.isEmpty()) {
                    show_error_box("Invalid Response from Server", "No Response");
                    return;
                }
                if (!code.equals("00")) {
                    // show_error_box(msg, context.getResources().getString(R.string.Commodities_Error) + code);
                } else {
                    finish();
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
        ArrayList<DataModel1> modeldata = new ArrayList<>();
        int commDetailssize = memberConstants.commDetails.size();
        for (int i = 0; i < commDetailssize; i++) {
            if (value==0) {
                if (memberConstants.commDetails.get(i).weighing.equals("Y")) {
                    memberConstants.commDetails.get(i).requiredQty="0.0";
                }else {
                    memberConstants.commDetails.get(i).requiredQty = memberConstants.commDetails.get(i).balQty;
                }
            }
            modeldata.add(new DataModel1(memberConstants.commDetails.get(i).commName +
                    "\n(" + memberConstants.commDetails.get(i).totQty + ")",
                    memberConstants.commDetails.get(i).balQty,
                    memberConstants.commDetails.get(i).price,
                    memberConstants.commDetails.get(i).closingBal,
                    memberConstants.commDetails.get(i).requiredQty));
        }
        adapter = new CustomAdapter1(context, modeldata, new OnClickListener() {
            @Override
            public void onClick_d(int p) {
                ManualDialog(p);

            }
        }, 1);
        recyclerView.setAdapter(adapter);
    }

    EditText weight;
    TextView  weightstatus;
    boolean getflag=false;
    private void ManualDialog(final int position) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);

        if (memberConstants.commDetails.get(position).weighing.equals("Y")) {
            dialog.setContentView(R.layout.activity_weighing);
          //  getweight = dialog.findViewById(R.id.weigh);

            final Button get =  dialog.findViewById(R.id.weighing_get);
            get.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( usb.length()==0) {
                        get.setEnabled(false);
                        getflag = true;
                        MESSAGE_FROM_SERIAL_PORT = 0;
                        if (choice == 1) {
                            setFilters();
                            startService(UsbService.class, usbConnection, null);
                        } else {
                            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (mBluetoothAdapter.isEnabled()) {
                                if (address == null) {
                                    BTList();
                                } else {
                                    checkBTState();
                                }
                            } else {
                                show_error_box(context.getResources().getString(R.string.Enable_Bluetooth_and_pair_your_Device_Manually), context.getResources().getString(R.string.Bluetooth));
                            }
                        }
                    }
                }
            });

        } else {
            dialog.setContentView(R.layout.activity_weight);
            weight = dialog.findViewById(R.id.weights);

        }
        weightstatus = dialog.findViewById(R.id.weight_status);
        Button confirm = (Button) dialog.findViewById(R.id.weight_confirm);
        Button back = (Button) dialog.findViewById(R.id.weight_back);

        TextView name=(TextView) dialog.findViewById(R.id.name);
        TextView bal=(TextView) dialog.findViewById(R.id.bal);
        TextView rate=(TextView) dialog.findViewById(R.id.rate);
        TextView close=(TextView) dialog.findViewById(R.id.close);

        name.setText(memberConstants.commDetails.get(position).commName);
        bal.setText(memberConstants.commDetails.get(position).balQty);
        rate.setText(memberConstants.commDetails.get(position).price);
        close.setText(memberConstants.commDetails.get(position).closingBal);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (memberConstants.commDetails.get(position).weighing.equals("Y")) {
                    if (getflag) {
                       // String weighingweight = getweight.getText().toString();
                        //float value= Float.parseFloat(weighingweight);
                        if ( usb.length()>0) {
                            CheckWeight(position, usb, 1);
                            usb="";
                        }
                        /*if (value==0.0) {
                            memberConstants.commDetails.get(position).requiredQty= String.valueOf(value);
                            Display(1);
                        }else if (value>0.0){

                        }*/
                    }else {
                        weightstatus.setTextColor(context.getResources().getColor(R.color.cancel));
                        weightstatus.setText(context.getResources().getString(R.string.Please_get_Weight_from_Weighing_Machine));
                    }

                } else {
                    String enteredweight = weight.getText().toString();
                    float value= Float.parseFloat(enteredweight);
                    if (value==0.0) {
                        memberConstants.commDetails.get(position).requiredQty= String.valueOf(value);
                        Display(1);
                    }else if (value>0.0){
                        CheckWeight(position, enteredweight, 0);
                    }
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

    private void CheckWeight(int position, String enteredweight, int i) {
        if (enteredweight != null && !enteredweight.isEmpty() && !enteredweight.equals("null")) {
            float requiredQty = verify_Weight(position,enteredweight, i);
            if (requiredQty >= 0) {
                int calculated = cal(requiredQty, position);
                if (calculated == 0) {
                    memberConstants.commDetails.get(position).requiredQty= String.valueOf(requiredQty);
                    Display(1);
                } else if (calculated == 2) {
                    show_error_box(context.getResources().getString(R.string.Please_Issue_Commodity_upto_Bal_Qty_only),
                            context.getResources().getString(R.string.Not_a_Valid_Weight));
                } else if (calculated == 3) {
                    show_error_box("Please Enter the "+memberConstants.commDetails.get(position).commName+
                            " Qty greater than or Equal to "+memberConstants.commDetails.get(position).minQty, context.getResources().getString(R.string.Not_a_Valid_Weight));
                } else {
                    show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value),
                            context.getResources().getString(R.string.Not_a_Valid_Weight));
                }
            } else {
                show_error_box("Issue Qty should be Multiple by Minimum Qty -"
                                +memberConstants.commDetails.get(position).minQty, context.getResources().getString(R.string.Enter_valid_weight));
            }
        } else {
            show_error_box(context.getResources().getString(R.string.Please_Enter_the_Weight), context.getResources().getString(R.string.Enter_valid_weight));
        }
    }

    private int cal(float requiredQty, int position) {
        float price,balQty,closingBal,minQty;
        price= Float.parseFloat(memberConstants.commDetails.get(position).price);
        memberConstants.commDetails.get(position).totalPrice= String.valueOf((requiredQty * price));
        balQty= Float.parseFloat(memberConstants.commDetails.get(position).balQty);
        closingBal= Float.parseFloat(memberConstants.commDetails.get(position).closingBal);
        minQty= Float.parseFloat(memberConstants.commDetails.get(position).minQty);
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

    private float verify_Weight(int position, String com, int check) {
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
        return -1;
    }

    private String add_comm() {
        TOTALAMOUNT = 0.0;
        StringBuilder add = new StringBuilder();
        String str;
        int userCommModelssize = memberConstants.commDetails.size();
        float commprice,commqty,commamount;
        if (userCommModelssize > 0) {
            for (int i = 0; i < userCommModelssize; i++) {
                commqty = Float.parseFloat((memberConstants.commDetails.get(i).requiredQty));
                if (commqty>0.0) {
                    commprice = Float.parseFloat((memberConstants.commDetails.get(i).price));
                    commamount = commprice * commqty;
                    TOTALAMOUNT = TOTALAMOUNT + commamount;
                    memberConstants.commDetails.get(i).totalPrice= String.valueOf(commamount);
                    str = "<commodityDetail>\n" +
                            "<allocationType>" + memberConstants.commDetails.get(i).allocationType + "</allocationType>\n" +
                            "<allotedMonth>" + memberConstants.commDetails.get(i).allotedMonth + "</allotedMonth>\n" +
                            "<allotedYear>" + memberConstants.commDetails.get(i).allotedYear + "</allotedYear>\n" +
                            "<commCode>" + memberConstants.commDetails.get(i).commcode + "</commCode>\n" +
                            "<commName>" + memberConstants.commDetails.get(i).commName + "</commName>\n" +
                            "<requiredQuantity>" + memberConstants.commDetails.get(i).requiredQty + "</requiredQuantity>\n" +
                            "<commodityAmount>" + memberConstants.commDetails.get(i).price + "</commodityAmount>\n" +
                            "<price>" + memberConstants.commDetails.get(i).totalPrice + "</price>\n" +
                            "</commodityDetail>\n";
                    add.append(str);
                }
            }
            if (add.length()>0) {
                return String.valueOf(add);
            }
        }
        return null;
    }

    private void conformRation() {

        String com = add_comm();
        if (!com.equals(null) && com.length()>0) {
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
                show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection));
            }
        } else {
            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {

            } else {
                mp = mp.create(context, R.raw.c100189);
                mp.start();
                show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_Inputs));
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

    public interface OnClickListener {
        void onClick_d(int p);
    }
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, context.getResources().getString(R.string.USB_Ready), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, context.getResources().getString(R.string.USB_Permission_not_granted), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, context.getResources().getString(R.string.No_USB_connected), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, context.getResources().getString(R.string.USB_disconnected), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, context.getResources().getString(R.string.USB_device_not_supported), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void BTList() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            show_error_box(context.getResources().getString(R.string.No_Paired_Devices), context.getResources().getString(R.string.Bluetooth));
        } else {
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>(pairedDevices);
            Intent intent = new Intent(this, DeviceListActivity.class);
            intent.putParcelableArrayListExtra("device.list", list);
            startActivityForResult(intent, 1);
        }
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle
            extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("HandlerLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data == null) {
                return;
            }

            checkBTState();
        }
    }

    private void checkBTState() {
        handler();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        /*String address1 = (String) Objects.requireNonNull(data.getExtras()).get("device_name");*/
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), context.getResources().getString(R.string.Socket_creation_failed), Toast.LENGTH_LONG).show();
            finish();
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
            //getweight.setText(context.getResources().getString(R.string.Please_Wait));
            pd = ProgressDialog.show(context, context.getResources().getString(R.string.Connecting), context.getResources().getString(R.string.Please_Wait), true, false);
            checkBTState();
            return;
            //show_error_box(msg, "Socket Exception Please try again");
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
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private final WeakReference<RationDetailsActivity> mActivity;

        MyHandler(RationDetailsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_FROM_SERIAL_PORT) {
                String data = (String) msg.obj;
                storeUSBdata.append(data);
                if (storeUSBdata.toString().contains("g")) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    usb = String.valueOf(storeUSBdata).trim();
                    storeUSBdata.setLength(0);
                    System.out.println("USB====" + usb);
                   // mActivity.get().getweight.setText(usb);
                }
            }
        }
    }

    private class ConnectedThread extends Thread {
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
        toolbarVersion.setText("V." + appversion);

        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);

        toolbarFpsid.setText("FPS ID");
        toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
        toolbarActivity.setText("COMMODITIES");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}
