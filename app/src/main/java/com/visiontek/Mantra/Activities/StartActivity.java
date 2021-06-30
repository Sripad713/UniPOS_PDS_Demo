package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.DatabaseHelper;
import com.visiontek.Mantra.Utils.FileLoggingTree;
import com.visiontek.Mantra.Utils.TelephonyInfo;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class StartActivity extends AppCompatActivity {
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int REQUEST_STORAGE_WRITE_SDCARD = 3;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 4;
    static String L;
    static MediaPlayer mp;
    Context context;
    Button start, quit, settings;
    ProgressDialog pd = null;
    LocationManager locationManager;
    int PS;
    TelephonyInfo telephonyInfo;
    String STATE, IMEI;
    TelephonyManager telephonyManager;
    SubscriptionManager subscriptionManager;
    List<SubscriptionInfo> subscriptionInfoList;
    static String longitude;
    static String latitude;
    DatabaseHelper db;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = StartActivity.this;
        checkLanguage();
        setContentView(R.layout.activity_start);
        Timber.plant(new FileLoggingTree());
        try {

            mp = mp.create(context, R.raw.c100041);
            mp.start();
            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

            PS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            TextView toolbarRD = findViewById(R.id.toolbarRD);
            boolean rd_fps = RDservice(context);
            if (rd_fps) {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                toolbarRD.setTextColor(context.getResources().getColor(R.color.black));
                show_error_box(context.getResources().getString(R.string.RD_Service_Msg), context.getResources().getString(R.string.RD_Service), 0);
                return;
            }

            initilisation();
            dealerConstants = null;
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.enable();

            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    if (DEVICEID.length() > 0&& DEVICEID!=null) {
                        if (Util.networkConnected(context)) {
                            if (mp != null) {
                                releaseMediaPlayer(context, mp);
                            }
                            if (L.equals("hi")) {
                                mp = mp.create(context, R.raw.c200175);
                                mp.start();
                            } else {
                                mp = mp.create(context, R.raw.c100175);
                                mp.start();
                            }
                            FramexmlforDealerDetails();
                        } else {
                            show_error_box(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection), 0);
                        }
                    } else {
                        show_error_box("Please Grant All Permission", "Permissions", 0);
                    }
                }
            });

            quit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    show_box();
                }
            });


            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preventTwoClick(view);
                    Intent settings = new Intent(context, SettingActivity.class);
                    startActivityForResult(settings, 1);
                }
            });

        } catch (Exception ex) {

            Timber.tag("StartActivity-onCreate-").e(ex.getMessage(), "");
        }
    }

    TextView toolbarFpsidValue,toolbarLatitudeValue,toolbarLongitudeValue;
    private void toolbarInitilisation() {

        TextView toolbarVersion = findViewById(R.id.toolbarVersion);
        TextView toolbarDateValue = findViewById(R.id.toolbarDateValue);
        TextView toolbarFpsid = findViewById(R.id.toolbarFpsid);
        toolbarFpsidValue = findViewById(R.id.toolbarFpsidValue);
        TextView toolbarActivity = findViewById(R.id.toolbarActivity);
        toolbarLatitudeValue  = findViewById(R.id.toolbarLatitudeValue);
         toolbarLongitudeValue = findViewById(R.id.toolbarLongitudeValue);

        String appversion = Util.getAppVersionFromPkgName(getApplicationContext());
        System.out.println(appversion);
        toolbarVersion.setText("V" + appversion);
        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String date = dateformat.format(new Date()).substring(6, 16);
        toolbarDateValue.setText(date);
        System.out.println(date);
        toolbarFpsid.setText("DEVICE ID");
        toolbarFpsidValue.setText(DEVICEID);
        toolbarActivity.setText("LOGIN");
        DisplayGPS();
        toolbarLatitudeValue.setText(latitude);
        toolbarLongitudeValue.setText(longitude);
    }

    private void initilisation() {
        pd = new ProgressDialog(context);
        start = findViewById(R.id.button_start);
        quit = findViewById(R.id.button_quit);
        settings = findViewById(R.id.button_settings);
        toolbarInitilisation();
        Startbutton();
        File file = new File("", "YourApp.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void checkLanguage() {
        try {

            db = new DatabaseHelper(context);
            L = db.get_L("Language");
            if (L != null && !L.isEmpty()) {
                setLocal(L);
            }
        } catch (Exception ex) {

            Timber.tag("StartActivity-checklng-").e(ex.getMessage(), "");
        }
    }

    private void FramexmlforDealerDetails() {
        try {

            String dealers = "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n" +
                    "<SOAP-ENV:Envelope\n" +
                    "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    xmlns:ns1=\"http://service.fetch.rationcard/\">\n" +
                    "    <SOAP-ENV:Body>\n" +
                    "        <ns1:getPDSFpsNoDetails>\n" +
                    "            <VersionNo>2.3</VersionNo>\n" +//static
                    "            <deviceID>" + DEVICEID + "</deviceID>\n" +//dynamic
                    "            <token>7797602c3da57f23e57a259b60358622</token>\n" +//static
                    "            <key>111</key>\n" +//static
                    "            <simID>" + IMEI + "</simID>\n" +//dynamic
                    "            <checkSum></checkSum>\n" +//dynamic
                    "            <longtude>" + longitude + "</longtude>\n" +//dynamic
                    "            <latitude>" + latitude + "</latitude>\n" +//dynamic
                    "            <vendorId></vendorId>\n" +//static
                    "            <simStatus>" + STATE + "</simStatus>\n" +//dynamic
                    "        </ns1:getPDSFpsNoDetails>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>\n";
            Util.generateNoteOnSD(context, "DealerDetailsReq.txt", dealers);
            hitURLforDealer(dealers);
        } catch (Exception ex) {

            Timber.tag("StartActivity-Format-").e(ex.getMessage(), "");
        }
    }

    private void hitURLforDealer(String dealers) {
        try {
            pd = ProgressDialog.show(context, context.getResources().getString(R.string.Dealer_Details), context.getResources().getString(R.string.Please_wait), true, false);
            XML_Parsing request = new XML_Parsing(StartActivity.this, dealers, 1);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String isError, String msg, String ref, String flow, Object object) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }

                    if (isError == null || isError.isEmpty()) {
                        show_error_box("Invalid Response from Server" + isError, "No Response", 0);
                        return;
                    }

                    if (!isError.equals("00")) {
                        show_error_box(msg, isError, 0);
                    } else {
                        Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        //show_error_box(msg,  isError,1);

                    }
                }
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("StartActivity-Request-").e(ex.getMessage(), "");
        }
    }


    private void show_error_box(String msg, String title, final int i) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(title);
        alertDialogBuilder.setTitle(msg);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (i == 1) {
                            Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void show_box() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.Do_you_want_to_cancel_Session));
        alertDialogBuilder.setTitle(context.getResources().getString(R.string.CHHATISGARHPDS));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            recreate();
        }
    }

    public void setLocal(String lang) {
        try {
            if (lang != null) {
                Locale locale = new Locale(lang);
                Locale.setDefault(locale);
                Configuration con = new Configuration();
                con.locale = locale;
                getBaseContext().getResources().updateConfiguration(con, getBaseContext().getResources().getDisplayMetrics());
            }
        } catch (Exception ex) {

            Timber.tag("StartActivity-SetLng-").e(ex.getMessage(), "");
        }
    }

    private boolean getLocation() {
        try {
            int locationMode = 0;
            String locationProviders;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;
            } else {
                locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                return !TextUtils.isEmpty(locationProviders);
            }
        } catch (Exception ex) {

            Timber.tag("StartActivity-getloc-").e(ex.getMessage(), "");
            return false;
        }
    }

    private void DisplayGPS() {
        try {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean st = getLocation();
            System.out.println("LOCATION = " + st);
            if (!st) {
                statusCheck();
            }
            LocationListener locationListener = new MyLocationListener();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        } catch (Exception ex) {

            Timber.tag("StartAct-displyGps-").e(ex.getMessage(), "");
        }
    }

    public void statusCheck() {
        try {

            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }
        } catch (Exception ex) {

            Timber.tag("StartActivity-onCreate-").e(ex.getMessage(), "");
        }
    }

    private void buildAlertMessageNoGps() {
        try {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(context.getResources().getString(R.string.Your_GPS_seems_to_be_disabled_do_you_want_to_enable_it))
                    .setCancelable(false)
                    .setPositiveButton(context.getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(context.getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception ex) {

            Timber.tag("StartActivity-msg-").e(ex.getMessage(), "");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void get_method() {
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;

            }
            //MultiSimTelephonyManager multiSimTelephonyManager = new MultiSimTelephonyManager(this);
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            telephonyInfo = TelephonyInfo.getInstance(this);
            STATE = null;
            STATE = get_state();

            if (STATE.equals("No Sim")) {
                System.out.println("No Sims");
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                IMEI = get_Imei();
            }
        } catch (Exception ex) {

            Timber.tag("StartActivity-Sim-").e(ex.getMessage(), "");
        }
    }

    @SuppressLint("HardwareIds")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private String get_Imei() {
        try {
        } catch (Exception ex) {
            show_error_box(ex.getMessage(), "Start", 0);
            Timber.tag("StartActivity-onCreate-").e(ex.getMessage(), "");
        }
        String imei = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return null;
        }
        String sim1_imei = telephonyManager.getDeviceId(0);
        String sim2_imei = telephonyManager.getDeviceId(1);
        String SIM;
        if (sim2_imei.length() <= 0 || sim2_imei == null || sim2_imei.isEmpty()) {
            SIM = sim1_imei;
        } else {
            SIM = sim2_imei;
        }
        return SIM;
    }

    private String get_state() {


        boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
        boolean isSIM2Ready = telephonyInfo.isSIM2Ready();

        if (isSIM1Ready && isSIM2Ready) {

            return "Y";
        } else if (isSIM1Ready) {
            return "Y";
        } else if (isSIM2Ready) {
            return "Y";
        } else {
            return "N";
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
                    }
                }
                break;
            case REQUEST_ACCESS_COARSE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_WRITE_SDCARD);
                    }
                }
                break;
            case REQUEST_STORAGE_WRITE_SDCARD:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                    }
                }
                break;
            case REQUEST_ACCESS_FINE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Startbutton();
                    }
                }

                break;
            default:
                break;
        }
    }

    private void Startbutton() {
        try {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            get_method();
            DisplayGPS();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           /* DEVICEID = Build.getSerial();
            toolbarFpsidValue.setText(DEVICEID);*/
            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        }
        } catch (Exception ex) {

            Timber.tag("StartActivity-onCreate-").e(ex.getMessage(), "");
        }
    }

    private void statecode(String st) {
        switch (st) {
            case "22":
                break;
            case "13":
                break;
            case "14":
                break;
            case "15":
                break;
            case "25":
                break;
        }

    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {

            longitude = String.valueOf(loc.getLongitude());
            System.out.println("-------------------" + longitude);
            latitude = String.valueOf(loc.getLatitude());
            System.out.println("-------------------" + latitude);
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("*********Disabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("*********Enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("*********Status Changed");
        }
    }
}