package com.visiontek.Mantra.Activities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.DeviceInfoListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.DeviceInfoListModel;
import com.visiontek.Mantra.R;

import java.util.ArrayList;
import java.util.List;

import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class OfflineRationCardStatusActivity extends BaseActivity {

    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    ArrayList<DeviceInfoListModel> arraydata;
    Context context;
    DatabaseHelper databaseHelper;
    List<String> count;
    int offlineCount = 0, onlineCount = 0, uploadedCount = 0, pendingCount = 0;


    public void saveInfo(Context context) {
        try {
            ArrayList<String> value = new ArrayList<>();
            value.add("Total Offline Ration Cards");
            value.add("Total Offline Upload Records");
            value.add("Total Offline Pending Records");
            value.add("Total Offline Txn's Pending Ration Cards");
            ArrayList<String> info = new ArrayList<>();
            try {
                int saleRecordCount[] = databaseHelper.getSaleRecordAgrregateCounts();
                int totalRcCount = databaseHelper.getTotalRationCardCount();
                int pendingTxnRc = databaseHelper.getOfflineTxnPendingRationCard();
                uploadedCount = saleRecordCount[2];
                pendingCount = saleRecordCount[3];
                String upload = Integer.toString(uploadedCount);
                System.out.println("OFFLINE UPLOAD COUNT ===="+uploadedCount);
                String pending = Integer.toString(pendingCount);
                System.out.println("OFFLINE Pending COUNT ===="+pendingCount);

                String totalCountRc = Integer.toString(totalRcCount);
                String pendingTxnrc = Integer.toString(pendingTxnRc);
                info.add(totalCountRc);
                info.add(upload);
                info.add(pending);
                info.add(pendingTxnrc);
                recyclerView = findViewById(R.id.my_recycler_view);
                recyclerView.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                arraydata = new ArrayList<>();
                for (int i = 0; i < value.size(); i++) {
                    arraydata.add(new DeviceInfoListModel(value.get(i), info.get(i).trim()));
                }
                adapter = new DeviceInfoListAdapter(context, arraydata);
                recyclerView.setAdapter(adapter);
            } catch (Exception e) {
                System.out.println("@@Exception cought: " + e.toString());
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize() {
        try {
            context = OfflineRationCardStatusActivity.this;
            databaseHelper = new DatabaseHelper(context);
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_offline_rationcard_status, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            saveInfo(context);
            Button back = findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void initializeControls() {

    }
}
