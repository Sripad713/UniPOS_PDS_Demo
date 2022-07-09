package com.visiontek.Mantra.Activities;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.DeviceInfoListAdapter;
import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DATAModels.DeviceInfoListModel;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails.DealerModel;
import com.visiontek.Mantra.Models.PartialOnlineData;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.SharedPref;

import java.util.ArrayList;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;

public class DeviceParametersActivity extends BaseActivity {

    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    ArrayList<DeviceInfoListModel> arraydata;
    Context context;
    String deviceType;
    DatabaseHelper databaseHelper;
    SharedPref sharedPref;


    public void saveInfo(Context context) {
        try {
            ArrayList<String> value = new ArrayList<>();
            value.add("Shop ID");
            value.add("Device Type");
            value.add("AllotYear");
            value.add("AllotMonth");
            value.add("KeyregisterDownloadStatus");
            value.add("fpsCbDownloadStatus");
            value.add("LastKeyregisterDownloadDate");
            ArrayList<String> info = new ArrayList<>();
            String fpsId = sharedPref.getData("fpsId");
            info.add(fpsId);
            String partialOnlineOfflineStatus = sharedPref.getData("partialOnlineOfflineStatus");
            System.out.println("DeviceType>>>>"+partialOnlineOfflineStatus);

            if(partialOnlineOfflineStatus.equals("Y")){

                deviceType="OFFLINE";
                info.add(deviceType);
                String allotYear =sharedPref.getData("allocationYear");
                System.out.println("allotYear>>>>"+allotYear);

                info.add(allotYear);
                String allotMonth = sharedPref.getData("allocationMonth");
                System.out.println("allotYear>>>>"+allotYear);
                info.add(allotMonth);

            }else{

                if(partialOnlineOfflineStatus.equals("N")){

                    deviceType = "ONLINE";
                    info.add(deviceType);
                    String allotYear =sharedPref.getData("allocationYear");
                    System.out.println("allotYear>>>>"+allotYear);
                    info.add(allotYear);
                    String allotMonth = sharedPref.getData("allocationMonth");
                    System.out.println("allotYear>>>>"+allotYear);
                    info.add(allotMonth);

                }
            }

           /* String allotYear =sharedPref.getData("allocationYear");
            info.add(allotYear);
            String allotMonth = sharedPref.getData("allocationMonth");
            info.add(allotMonth);*/
            String keyregisterDownloadStatus = sharedPref.getData("keyregisterDownloadStatus");
            info.add(keyregisterDownloadStatus);
            String fpsCbDownloadStatus = sharedPref.getData("fpsCbDownloadStatus");
            info.add(fpsCbDownloadStatus);
            String keyregisterDownloadDate =sharedPref.getData("KeyregisterDownloadDate");
            System.out.println("KeyRegister Date ======"+keyregisterDownloadDate);
            info.add(keyregisterDownloadDate);



           /* PartialOnlineData partialOnlineData = databaseHelper.getPartialOnlineData();
            info.add(partialOnlineData.getOffPassword());
            if(dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus.equals("Y")){

                deviceType="Offline";
                info.add(deviceType);
                info.add(partialOnlineData.getAllotYear());
                info.add(partialOnlineData.getAllotMonth());
                System.out.println("Offline");
            }else
                {
                if (dealerConstants.fpsCommonInfo.partialOnlineOfflineStatus.equals("N"))
                {
                    deviceType = "Online";
                    System.out.println("Online");
                }
                }

            System.out.println("TEJJJSAVEE1111");
            info.add(dealerConstants.fpsCommonInfo.keyregisterDownloadStatus);
            info.add(dealerConstants.fpsURLInfo.fpsCbDownloadStatus);*/

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

        }catch (Exception ex){

            ex.printStackTrace();
            Timber.tag("DevicePara-onCreate-").e(ex.getMessage(),"");


        }
    }
    @Override
    public void initialize() {

        try {
            context = DeviceParametersActivity.this;
            databaseHelper = new DatabaseHelper(context);
            sharedPref = new SharedPref(context);
            System.out.println("Device Parametes======");
            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_device_parameters, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();
             saveInfo(context);
            Button back=findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    finish();
                }
            });
        }catch (Exception ex){

            Timber.tag("DevicePara-onCreate-").e(ex.getMessage(),"");
        }
    }

    @Override
    public void initializeControls() {

        toolbarActivity.setText(context.getResources().getString(R.string.DEVICE_PARAMETERS));

    }
}
