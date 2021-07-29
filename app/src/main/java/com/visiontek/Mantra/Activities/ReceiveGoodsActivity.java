package com.visiontek.Mantra.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.ReceiveGoodsListAdapter;
import com.visiontek.Mantra.Models.DATAModels.ReceiveGoodsListModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsDetails;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.ReceiveGoodsModel;
import com.visiontek.Mantra.Models.ReceiveGoodsModel.tcCommDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.DecimalDigitsInputFilter;
import com.visiontek.Mantra.Utils.Util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import timber.log.Timber;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.checkdotvalue;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;


public class ReceiveGoodsActivity extends BaseActivity {

    ProgressDialog pd;
    Button back, scapfp;
    Context context;
    Spinner options;
    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;

    TextView trucknumber;
    ReceiveGoodsDetails receiveGoodsDetails;
    ReceiveGoodsModel receiveGoodsModel=new ReceiveGoodsModel();

    private void addCommDetails() {

        try {

        int size=receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.size();
        tcCommDetails tcCommDetails;

        if (receiveGoodsModel.tcCommDetails.size()>0){
            receiveGoodsModel.tcCommDetails.clear();
        }
        for (int i=0;i<size;i++){

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

        }
        }catch (Exception ex){

            Timber.tag("ReceiveGoods-addcomm-").e(ex.getMessage(),"");
        }
    }


    private boolean check() {
        try {

        float val = 0;
        int size=receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.size();
        for (int i=0;i<size;i++){
            val= Float.parseFloat(receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(i).enteredvalue);
            if (val>0.0){
                return true;
            }
        }
        }catch (Exception ex){

            Timber.tag("ReceiveGoods-check-").e(ex.getMessage(),"");
        }
        return false;

    }


    private void DisplayTruck(int position) {
        try {

        ArrayList<ReceiveGoodsListModel> modeldata = new ArrayList<>();
        int tcCommDetailssize=receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.size();
        /*if (position==-1){
            tcCommDetailssize=0;
        }*/
        for (int k = 0; k <tcCommDetailssize ; k++) {

            modeldata.add(new ReceiveGoodsListModel(
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).commName,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).schemeName,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).allotment,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).releasedQuantity,
                    receiveGoodsDetails.infoTCDetails.get(position).tcCommDetails.get(k).enteredvalue));
        }
        adapter = new ReceiveGoodsListAdapter(context, modeldata, new OnClickReceived() {
            @Override
            public void onClick(int p) {
                EnterComm(p);
            }
        });
        recyclerView.setAdapter(adapter);
        }catch (Exception ex){

            Timber.tag("ReceiveGoods-Display-").e(ex.getMessage(),"");
        }
    }

    public interface OnClickReceived {
        void onClick(int p);
    }
    private void EnterComm(final int position) {
        try {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.receivegoods);
        final EditText received = dialog.findViewById(R.id.enter);
        received.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(3)});
        Button confirm = (Button) dialog.findViewById(R.id.confirm);
        Button back = (Button) dialog.findViewById(R.id.back);

        TextView name=(TextView) dialog.findViewById(R.id.a);
        TextView scheme=(TextView) dialog.findViewById(R.id.b);
        TextView allot=(TextView) dialog.findViewById(R.id.c);
        TextView dispatch=(TextView) dialog.findViewById(R.id.d);
        TextView status = (TextView) dialog.findViewById(R.id.status);

        status.setText(context.getResources().getString(R.string.Enter_Received));
        name.setText( receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select)
                .tcCommDetails.get(position).commName);
        scheme.setText( receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select)
                .tcCommDetails.get(position).schemeName);
        allot.setText( receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select)
                .tcCommDetails.get(position).allotment);
        dispatch.setText(receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select)
                .tcCommDetails.get(position).releasedQuantity );
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String res;
                 res= received.getText().toString();
                if (!res.isEmpty()) {
                    if (checkdotvalue(res)) {
                        receiveGoodsModel.received = res;
                        float textdata = Float.parseFloat((receiveGoodsModel.received));
                        float dispatch = Float.parseFloat((receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(position).releasedQuantity));
                        if (textdata > dispatch) {
                            show_AlertDialog(context.getResources().getString(R.string.Receive_Goods),
                                    context.getResources().getString(R.string.Invalid_Quantity),
                                    context.getResources().getString(R.string.Please_Receive_the_Qty_upto_Dispatched_Qty_only),
                                    0);

                        } else {
                            receiveGoodsDetails.infoTCDetails.get(receiveGoodsModel.select).tcCommDetails.get(position).enteredvalue = receiveGoodsModel.received;
                            DisplayTruck(receiveGoodsModel.select);
                        }
                    }else {
                        show_AlertDialog(context.getResources().getString(R.string.Receive_Goods),
                                context.getResources().getString(R.string.Invalid_Quantity),
                                context.getResources().getString(R.string.Please_enter_a_valid_Value),
                                0);
                    }
                } else {
                    show_AlertDialog(context.getResources().getString(R.string.Receive_Goods),
                            context.getResources().getString(R.string.Invalid_Quantity),
                            context.getResources().getString(R.string.Please_enter_a_valid_Value),
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
        }catch (Exception ex){

            Timber.tag("ReceiveGoods-Entered-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initialize() {
        try {
            context = ReceiveGoodsActivity.this;
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_ration_details, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

            receiveGoodsDetails = (ReceiveGoodsDetails) getIntent().getSerializableExtra("OBJ");

            TextView toolbarRD = findViewById(R.id.toolbarRD);
            boolean rd_fps = RDservice(context);
            if (rd_fps) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
                show_AlertDialog(context.getResources().getString(R.string.RECEIVE_GOODS),
                        context.getResources().getString(R.string.RD_Service),
                        context.getResources().getString(R.string.RD_Service_Msg),0);
                return;
            }
            receiveGoodsModel=new ReceiveGoodsModel();


            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            recyclerView = findViewById(R.id.my_recycler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            ArrayList<String> trucklist=new ArrayList<>();

            int size=receiveGoodsDetails.infoTCDetails.size() ;
            trucklist.add(context.getResources().getString(R.string.Select_Truck_Chit));
            for (int i=0;i<size;i++) {
                trucklist.add( receiveGoodsDetails.infoTCDetails.get(i).truckChitNo);
            }

            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, trucklist);
            options.setAdapter(adapter1);
            System.out.println(Collections.unmodifiableList(trucklist));
            options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                    preventTwoClick(view);
                    if (position!=0) {
                        receiveGoodsModel.select = position-1;
                        receiveGoodsModel.length = receiveGoodsDetails.infoTCDetails.get(position-1).CommLength;
                        receiveGoodsModel.fps = receiveGoodsDetails.infoTCDetails.get(position-1).fpsId;
                        receiveGoodsModel.month = receiveGoodsDetails.infoTCDetails.get(position-1).allotedMonth;
                        receiveGoodsModel.year = receiveGoodsDetails.infoTCDetails.get(position-1).allotedYear;
                        receiveGoodsModel.chit = receiveGoodsDetails.infoTCDetails.get(position-1).truckChitNo;
                        receiveGoodsModel.cid = receiveGoodsDetails.infoTCDetails.get(position-1).challanId;
                        receiveGoodsModel.orderno = receiveGoodsDetails.infoTCDetails.get(position-1).allocationOrderNo;
                        receiveGoodsModel.truckno = receiveGoodsDetails.infoTCDetails.get(position-1).truckNo;
                        trucknumber.setText(context.getResources().getString(R.string.Truck_No) + receiveGoodsModel.truckno);
                        DisplayTruck(position-1);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            scapfp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if (receiveGoodsModel.select!=-1) {
                        if (check()) {
                            if (Util.networkConnected(context)) {
                                addCommDetails();
                                Intent i = new Intent(ReceiveGoodsActivity.this, DealerAuthenticationActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.putExtra("OBJ", receiveGoodsModel);
                                startActivity(i);
                            } else {
                                show_AlertDialog(context.getResources().getString(R.string.Receive_Goods),
                                        context.getResources().getString(R.string.Internet_Connection_Msg),
                                        context.getResources().getString(R.string.Internet_Connection), 0);
                            }
                        } else {
                            show_AlertDialog(context.getResources().getString(R.string.Receive_Goods),
                                    context.getResources().getString(R.string.Enter_Received),
                                    "",
                                    0);

                        }
                    }else {
                        show_AlertDialog(context.getResources().getString(R.string.Receive_Goods),
                                context.getResources().getString(R.string.Please_Select_Truck_Chit_No),
                                "",
                                0);

                    }
                }
            });

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        }catch (Exception ex){

            Timber.tag("ReceiveGoods-onCreate-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initializeControls() {
        pd = new ProgressDialog(context);
        trucknumber = findViewById(R.id.tv_truckno);
        scapfp = findViewById(R.id.next);
        back = findViewById(R.id.back);
        options = findViewById(R.id.truckchit);
        toolbarActivity.setText(context.getResources().getString(R.string.RECEIVE_GOODS));
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
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
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
