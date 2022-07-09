package com.visiontek.Mantra.Activities;



import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.Util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Utils.Util.RDservice;

public abstract class BaseActivity extends AppCompatActivity {
    public LinearLayout llBody;
    public TextView toolbarVersion, toolbarDateValue, toolbarFpsid, toolbarFpsidValue,
            toolbarActivity, toolbarLatitudeValue, toolbarLongitudeValue, toolbarCard,
            toolbarRD;
    Context context;
    public static int rd_fps;
    public static String rd_vr;
    SharedPref sharedPref;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base2);
        context = BaseActivity.this;
        sharedPref = SharedPref.getInstance(context);
        toolbarInitilisation();
        initialize();
    }

    int val = 0;
    private void toolbarInitilisation() {
        llBody = findViewById(R.id.llbody);

        toolbarVersion = findViewById(R.id.toolbarVersion);
        toolbarDateValue = findViewById(R.id.toolbarDateValue);
        toolbarFpsid = findViewById(R.id.toolbarFpsid);
        toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        toolbarLatitudeValue = findViewById(R.id.toolbarLatitudeValue);
        toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);
        toolbarCard = findViewById(R.id.toolbarCard);

        toolbarRD = findViewById(R.id.toolbarRD);

        String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
        toolbarVersion.setText("V" + appversion);
        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        toolbarFpsid.setText("DEVICE ID");
        toolbarFpsidValue.setText(DEVICEID);

        if (Util.RDservice(context)) {
            if (Util.networkConnected(context)) {
                Intent intent = new Intent("in.gov.uidai.rdservice.fp.INFO");
                startActivityForResult(intent, 99);
            } else {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
            }
        } else {
            toolbarRD.setText("RD" );
            toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
        }


        new Thread(() -> {
            while (true) {
                if (isAppOnForeground(context)) {
                    if (Util.RDservice(context)) {
                        if (Util.networkConnected(context)) {
                            val = 0;
                        } else {
                            val = 1;
                        }
                    } else {
                        val = 2;
                    }

                    String latitude1 = sharedPref.getData("LATITUDE");
                    String longitude1 = sharedPref.getData("LONGITUDE");

                    if (latitude1.length() > 1 && longitude1.length() > 1) {
                        if (latitude1 != null && latitude1.length() > 10) {
                            latitude = latitude1.substring(0, 9);
                        } else {
                            longitude = longitude1;
                        }
                        if (longitude1 != null && longitude1.length() > 10) {
                            longitude = longitude1.substring(0, 9);
                        } else {
                            latitude = latitude1;
                        }

                    }


                    runOnUiThread(() -> {
                        if (rd_vr != null && rd_vr.length() > 1){
                            toolbarRD.setText("RD" + rd_vr);
                         }else {
                            toolbarRD.setText("RD" );
                        }
                        if (latitude != null && latitude.length() > 10) {
                            toolbarLatitudeValue.setText(latitude.substring(0, 9));
                        } else {
                            toolbarLatitudeValue.setText(latitude);
                        }
                        if (longitude != null && longitude.length() > 10) {
                            toolbarLongitudeValue.setText(longitude.substring(0, 9));
                        } else {
                            toolbarLongitudeValue.setText(longitude);
                        }
                        if (val == 2) {
                            toolbarRD.setText("RD" );
                            toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                        } else if (val == 1) {
                            toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                        } else {
                            if (rd_fps == 3) {
                                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
                            } else {
                                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                            }
                        }
                    });
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public abstract void initialize();

    public abstract void initializeControls();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if (data != null) {
                        String result = data.getStringExtra("DEVICE_INFO");
                        String rdService = data.getStringExtra("RD_SERVICE_INFO");
                        rdService = rdserviceStatus_parse_xml(rdService);
                        rd_fps = rdStatus(rdService);
                        rd_vr = rdserviceDeviceInfo_parse_xml(result);
                        if (rd_vr!=null && rd_vr.length()>1) {
                            sharedPref.saveData("RD", rd_vr);
                            rd_vr = sharedPref.getData("RD");
                            toolbarRD.setText("RD" + rd_vr);
                        }else {
                            toolbarRD.setText("RD");
                        }

                        if (rd_fps == 3) {
                            toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
                        } else if (rd_fps == 2) {
                            toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                        } else {
                            if (RDservice(context)) {
                                toolbarRD.setTextColor(context.getResources().getColor(R.color.yellow));
                            } else {
                                toolbarRD.setText("RD" );
                                toolbarRD.setTextColor(context.getResources().getColor(R.color.opaque_red));
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Error", "Error while deserialze device info", e);
                }
            }
        }
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public int rdStatus(String status) {
        if (status.equals("READY")) {
            return 3;
        } else if (status.equals("NOTREADY")) {
            return 2;
        } else {
            return 1;
        }

    }

    public String rdserviceStatus_parse_xml(String xmlString) {
        try {

            System.out.println("=========" + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if ((eventType == XmlPullParser.START_TAG) && (xpp.getName().equals("RDService"))) {
                    return xpp.getAttributeValue(null, "status");
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
        return "";
    }

    public String rdserviceDeviceInfo_parse_xml(String xmlString) {
        try {

            System.out.println("------" + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if ((eventType == XmlPullParser.START_TAG) && (xpp.getName().equals("DeviceInfo"))) {
                    return xpp.getAttributeValue(null, "rdsVer");
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;

    }

}