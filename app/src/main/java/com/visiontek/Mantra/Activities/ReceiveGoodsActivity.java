package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.CustomAdapter1;
import com.visiontek.Mantra.Models.DATAModels.DataModel1;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetUserDetails.MemberModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.tcCommDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.DatabaseHelper;
import com.visiontek.Mantra.Utils.Util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.visiontek.Mantra.Activities.StartActivity.L;
import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Activities.StartActivity.mp;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;


public class ReceiveGoodsActivity extends AppCompatActivity {

    ProgressDialog pd;
    Button back, scapfp;
    Context context;
    Spinner options;
    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    ArrayList<DataModel1> modeldata;
    TextView trucknumber;


    ReceiveGoodsDetails receiveGoodsDetails;
    ReceiveGoodsModel receiveGoodsModel=new ReceiveGoodsModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive__goods);

        context = ReceiveGoodsActivity.this;

        receiveGoodsDetails = (ReceiveGoodsDetails) getIntent().getSerializableExtra("OBJ");

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg),
                    context.getResources().getString(R.string.RD_Service),0);
            return;
        }

        initilisation();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        modeldata = new ArrayList<>();


        ArrayList<String> trucklist=new ArrayList<>();
        int size=receiveGoodsDetails.infoTCDetails.size();
        for (int i=0;i<size;i++) {
            trucklist.add( receiveGoodsDetails.infoTCDetails.get(i).truckNo);
        }

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, trucklist);
        options.setAdapter(adapter1);
        options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                receiveGoodsModel. select = position;
                System.out.println("SELETED=" + position);
                receiveGoodsModel.length = receiveGoodsDetails.infoTCDetails.get(position).CommLength;
                receiveGoodsModel.fps = receiveGoodsDetails.infoTCDetails.get(position).fpsId;
                receiveGoodsModel.month =receiveGoodsDetails.infoTCDetails.get(position).allotedMonth;
                receiveGoodsModel.year = receiveGoodsDetails.infoTCDetails.get(position).allotedYear;
                receiveGoodsModel.chit = receiveGoodsDetails.infoTCDetails.get(position).truckChitNo;
                receiveGoodsModel.cid = receiveGoodsDetails.infoTCDetails.get(position).challanId;
                receiveGoodsModel. orderno = receiveGoodsDetails.infoTCDetails.get(position).allocationOrderNo;
                receiveGoodsModel. truckno = receiveGoodsDetails.infoTCDetails.get(position).truckNo;
                trucknumber.setText(context.getResources().getString(R.string.Truck_No) + receiveGoodsModel.truckno);
                DisplayTruck(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        scapfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check()) {
                    if (Util.networkConnected(context)) {
                        Intent i = new Intent(ReceiveGoodsActivity.this, DealerAuthenticationActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("OBJ",  receiveGoodsModel);
                        startActivity(i);
                    } else {
                        show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg),context.getResources().getString(R.string.Internet_Connection),0);
                    }
                } else {
                    if (mp!=null) {
                        releaseMediaPlayer(context,mp);
                    }  if (L.equals("hi")) {
                    } else {
                   mp=mp.create(context, R.raw.c100051);
                           mp.start();
                    show_error_box( context.getResources().getString(R.string.Please_Select_Dealer_Name),  context.getResources().getString(R.string.Dealer), 0);
                }}
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage(context.getResources().getString(R.string.Do_you_want_to_cancel_Session));
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        });
                alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.No),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        trucknumber = findViewById(R.id.tv_truckno);
        scapfp = findViewById(R.id.next);
        back = findViewById(R.id.back);
        options = findViewById(R.id.truckchit);
        toolbarInitilisation();
    }

    private boolean check() {
        tcCommDetails tcCommDetails;
        int size=receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.size();
      // float val = 0;
        for (int i=0;i<size;i++){
            /*val= Float.parseFloat(receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).enteredvalue);
            //if (val>0.0){*/
                tcCommDetails =new tcCommDetails();
                tcCommDetails.enteredvalue=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).enteredvalue;
                tcCommDetails.allotment=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).allotment;
                tcCommDetails.commCode=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).commCode;
                tcCommDetails.commName=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).commName;
                tcCommDetails.releasedQuantity=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).releasedQuantity;
                tcCommDetails.schemeId=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).schemeId;
                tcCommDetails.schemeName=
                        receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).schemeName;
                receiveGoodsModel.tcCommDetails.add(tcCommDetails);

          //  }
        }
      /*  for (int i=0;i<size;i++){
            System.out.println("==========="+val);
            val= Float.parseFloat(receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).enteredvalue);
            if (val>0.0){
                System.out.println("="+val);
                return true;
            }
        }*/
        return true;
    }


    private void DisplayTruck(int position) {
        int tcCommDetailssize=receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.size();
        for (int k = 0; k <tcCommDetailssize ; k++) {
            modeldata.add(new DataModel1(receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).commName,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).schemeName,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).allotment,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).releasedQuantity,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).enteredvalue));
        }
        Display();
    }

    private void Display() {
        adapter = new CustomAdapter1(context, modeldata, new OnClickListener() {
            @Override
            public void onClick_d(int p) {
                EnterComm(p);
            }
        }, 1);
        recyclerView.setAdapter(adapter);
    }

    private void EnterComm(final int p) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        alert.setMessage(context.getResources().getString(R.string.Please_Enter_the_required_quantity));
        alert.setTitle(context.getResources().getString(R.string.Enter_Quantity));

        edittext.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(12);
        edittext.setFilters(FilterArray);
        alert.setView(edittext);
        alert.setPositiveButton(context.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                receiveGoodsModel.YouEditTextValue = edittext.getText().toString();
                if (!receiveGoodsModel.YouEditTextValue.isEmpty()) {
                    receiveGoodsModel.textdata = Double.parseDouble((receiveGoodsModel.YouEditTextValue));
                    receiveGoodsModel.AFTERDATA = String.valueOf(receiveGoodsModel.textdata);
                    receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(p).enteredvalue=receiveGoodsModel.AFTERDATA;
                    modeldata.clear();
                    DisplayTruck(receiveGoodsModel.select);
                } else {
                    show_error_box(context.getResources().getString(R.string.Please_enter_a_valid_Value), context.getResources().getString(R.string.Invalid_Quantity), 0);
                }
            }
        });
        alert.setNegativeButton(context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void show_error_box(String msg, String title, final int i) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (i == 1) {
                             // print();
                        }
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public interface OnClickListener {
        void onClick_d(int p);
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
        toolbarActivity.setText("RECEIVE GOODS");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}
