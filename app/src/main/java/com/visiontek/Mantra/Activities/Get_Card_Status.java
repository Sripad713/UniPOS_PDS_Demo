package com.visiontek.Mantra.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.visiontek.Mantra.Adapters.CustomAdapterGetCardStatus;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.DataModel1;
import com.visiontek.Mantra.Models.IssueModel.MemberDetailsModel.GetURLDetails.commDetails;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static com.visiontek.Mantra.Activities.BaseActivity.rd_fps;
import static com.visiontek.Mantra.Activities.BaseActivity.rd_vr;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.memberConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;

public class Get_Card_Status extends AppCompatActivity {


    Context context;
    String rationCardNo;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__card__status);
        context = Get_Card_Status.this;
        rationCardNo = getIntent().getStringExtra("rationcard");
        databaseHelper = new DatabaseHelper(this);


        RecyclerView.Adapter adapter;
        RecyclerView recyclerView;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ArrayList<DataModel1> modeldata = new ArrayList<>();

        final List<commDetails> commDetails = databaseHelper.getCommodityDetails(rationCardNo);
        int commDetailssize = commDetails.size();



        for (int i = 0; i < commDetailssize; i++) {
            /*if (value==0) {
                memberConstants.commDetails.get(i).requiredQty = memberConstants.commDetails.get(i).balQty;
            }*/
            modeldata.add(new DataModel1(commDetails.get(i).commName +
                    "\n(" + commDetails.get(i).totQty + ")",
                    commDetails.get(i).balQty,
                    commDetails.get(i).price,
                    commDetails.get(i).availedQty,
                    commDetails.get(i).closingBal));
        }
        adapter = new CustomAdapterGetCardStatus(context, modeldata);
        recyclerView.setAdapter(adapter);
        Button back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbarInitilisation();

    }

    private void toolbarInitilisation() {
        try {
            System.out.println("@@In toolbarInitialisation");
            TextView toolbarVersion = findViewById(R.id.toolbarVersion);
            TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
            TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
            TextView toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
            TextView toolbarActivity = findViewById(R.id.toolbarActivity);
            TextView toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
            TextView toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);
            TextView toolbarRD = findViewById(R.id.toolbarRD);
            if (rd_vr != null && rd_vr.length() > 1){
                toolbarRD.setText("RD" + rd_vr);
            }else {
                toolbarRD.setText("RD" );
            }
            if (rd_fps == 3) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else if (rd_fps == 2) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
            } else {
                if (RDservice(context)) {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                } else {
                    toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                }
            }
            toolbarActivity.setText( context.getResources().getString(R.string.CASH_PDS));
            String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
            System.out.println(appversion);
            toolbarVersion.setText("V" + appversion);

            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String date = dateformat.format(new Date()).substring(6, 16);
            toolbarDateValue.setText(date);
            System.out.println(date);

            toolbarFpsid.setText("FPS ID");
            if(dealerConstants == null || dealerConstants.stateBean == null || dealerConstants.stateBean.statefpsId==null)
            {
                System.out.println("@@NULL");
                ArrayList<String> statefpsiD = databaseHelper.getStateDetails();
                toolbarFpsidValue.setText(statefpsiD.get(6));
            }else {
                System.out.println("@@Setting val");
                toolbarFpsidValue.setText(dealerConstants.stateBean.statefpsId);
            }

            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        } catch (Exception ex) {
            System.out.println("@@Exception: " +ex.toString());
            Timber.tag("CashPDS-ToolBar-").e(ex.getMessage(), "");
        }
    }
}