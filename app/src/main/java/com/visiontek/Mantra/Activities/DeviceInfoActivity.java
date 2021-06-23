package com.visiontek.Mantra.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.visiontek.Mantra.Adapters.DeviceInfoListAdapter;
import com.visiontek.Mantra.Models.DATAModels.DeviceInfoListModel;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.visiontek.Mantra.Activities.StartActivity.latitude;
import static com.visiontek.Mantra.Activities.StartActivity.longitude;
import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;


public class DeviceInfoActivity extends AppCompatActivity {
    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    ArrayList<DeviceInfoListModel> arraydata;
    Context context;

    public static String readKernelVersion() {
        try {
            Process p = Runtime.getRuntime().exec("uname -a");
            InputStream is = null;
            if (p.waitFor() == 0) {
                is = p.getInputStream();
            } else {
                is = p.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            br.close();
            return line;
        } catch (Exception ex) {
            System.out.println("Main : readKernelVersion : Exception : " + ex);
            return "ERROR: " + ex.getMessage();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device__info);

        context = DeviceInfoActivity.this;

        TextView toolbarRD = findViewById(R.id.toolbarRD);
        boolean rd_fps = RDservice(context);
        if (rd_fps) {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
            show_error_box(context.getResources().getString(R.string.RD_Service_Msg),
                    context.getResources().getString(R.string.RD_Service));
            return;
        }

        toolbarInitilisation();
        saveInfo(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void saveInfo(Context context) {
        ArrayList<String> value = new ArrayList<>();
        value.add("Brand");
        value.add("Product");
        value.add("Hardware");
        value.add("Device");
        value.add("Manufacture");
        value.add("Model");
        value.add("SecurityPatch");
        value.add("Release");
        value.add("SDK");
        value.add("Build No");
        value.add("Baseband");
        value.add("Kernel");
        value.add("Application");

        ArrayList<String> info = new ArrayList<>();
        info.add(Build.BRAND);
        info.add(Build.PRODUCT);
        info.add(Build.HARDWARE);
        info.add(Build.DEVICE);
        info.add(Build.MANUFACTURER);
        info.add(Build.MODEL);
        info.add(Build.VERSION.SECURITY_PATCH);
        info.add(Build.VERSION.RELEASE);
        info.add(Build.VERSION.SDK);
        info.add(Build.DISPLAY);
        info.add(Build.getRadioVersion());
        info.add(readKernelVersion());
        info.add("UniPOS_PDS_Demo");

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        arraydata = new ArrayList<>();

        for (int i = 3; i < value.size(); i++) {
            arraydata.add(new DeviceInfoListModel(value.get(i), info.get(i).trim()));
            System.out.println("1111=" + info.get(i));
        }
        adapter = new DeviceInfoListAdapter(context, arraydata);
        recyclerView.setAdapter(adapter);
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

        toolbarFpsid.setText("DEVICE ID");
        toolbarFpsidValue.setText(DEVICEID);
        toolbarActivity.setText("DEVICE INFO");

        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }
}