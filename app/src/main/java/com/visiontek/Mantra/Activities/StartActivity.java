package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.FileLoggingTree;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.TelephonyInfo;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.VERSION_NO;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Utils.Util.RDservice;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;

public class StartActivity extends BaseActivity {
    int PSp, RS, NS, AC, WS,WC, AF;

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
    TelephonyInfo telephonyInfo;
    String STATE, IMEI;
    TelephonyManager telephonyManager;
    SubscriptionManager subscriptionManager;
    List<SubscriptionInfo> subscriptionInfoList;

    com.visiontek.Mantra.Utils.SharedPref SharedPref;


    @Override
    public void initialize() {

        try {
            context=StartActivity.this;
            checkLanguage();
            Timber.plant(new FileLoggingTree());
            mp = MediaPlayer.create(context, R.raw.c100041);
            mp.start();

            LinearLayout llAboutUs = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_start, null);
            llBody.addView(llAboutUs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llBody.setVisibility(View.VISIBLE);
            initializeControls();

            dealerConstants = null;
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.enable();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            grantPermission();
            start.setOnClickListener(view -> {
                preventTwoClick(view);

                if (DEVICEID!=null && Startbutton() && DEVICEID.length() > 0   ) {
                    if (Util.networkConnected(context)) {
                        if (mp != null) {
                            releaseMediaPlayer(context, mp);
                        }
                        if (L.equals("hi")) {
                            mp = MediaPlayer.create(context, R.raw.c200175);
                        } else {
                            mp = MediaPlayer.create(context, R.raw.c100175);
                        }
                        mp.start();
                        FramexmlforDealerDetails();
                    } else {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Login),
                                context.getResources().getString(R.string.Internet_Connection),
                                context.getResources().getString(R.string.Internet_Connection_Msg),0);
                    }
                } else {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Login),
                            "Permissions","Please Grant All Permission", 0);
                }
            });

            quit.setOnClickListener(view -> {
                preventTwoClick(view);
                show_Dialogbox(context.getResources().getString(R.string.CHHATISGARHPDS),
                        context.getResources().getString(R.string.Do_you_want_to_Cancel)
                );
            });

            grantPermission();

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

    @Override
    public void initializeControls() {
        pd = new ProgressDialog(context);
        start = findViewById(R.id.button_start);
        quit = findViewById(R.id.button_quit);
        settings = findViewById(R.id.button_settings);
        Startbutton();
        toolbarActivity.setText(context.getResources().getString(R.string.Login));
    }



    private void checkLanguage() {
        try {
            SharedPref = com.visiontek.Mantra.Utils.SharedPref.getInstance(context);
            String value = SharedPref.getData("LANG");
            if (value != null) {
                if (value.length() < 1) {
                    SharedPref.saveData("LANG", "hi");
                }
            } else {
                SharedPref = com.visiontek.Mantra.Utils.SharedPref.getInstance(context);
            }
            L = SharedPref.getData("LANG");
            setLocal(L);
        } catch (Exception ex) {
            Timber.tag("StartActivity-checklng-").e(ex.getMessage(), "");
        }
    }


    private void grantPermission() {
        PSp = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        RS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        NS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        AC = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        WS = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        WC = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
        AF = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (PSp != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        } else if (RS != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        } else if (NS != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        } else if (AC != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        } else if (WS != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        } else if (WC != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        }else if (AF != PackageManager.PERMISSION_GRANTED) {
            askpermission();
        }
    }

    protected void askpermission() {
        String[] permissions = {"android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_SMS",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_ADMIN",
                "android.permission.INTERNET",
                "android.permission.READ_CALENDAR",
                "android.permission.READ_SYNC_SETTINGS",
                "android.permission.CAMERA",
                "android.permission.WRITE_CONTACTS",
                "android.permission.READ_INTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
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
                    "            <VersionNo>"+VERSION_NO+"</VersionNo>\n" +//static
                    "            <deviceID>" + DEVICEID + "</deviceID>\n" +//dynamic
                    "            <token>7797602c3da57f23e57a259b60358622</token>\n" +//static
                    "            <key>111</key>\n" +//static
                    "            <simID></simID>\n" +//dynamic
                    "            <checkSum></checkSum>\n" +//dynamic
                    "            <longtude>" + longitude + "</longtude>\n" +//dynamic
                    "            <latitude>" + latitude + "</latitude>\n" +//dynamic
                    "            <vendorId></vendorId>\n" +//static
                    "            <simStatus></simStatus>\n" +//dynamic
                    "        </ns1:getPDSFpsNoDetails>\n" +
                    "    </SOAP-ENV:Body>\n" +
                    "</SOAP-ENV:Envelope>\n";
            if(Debug) {
                Util.generateNoteOnSD(context, "DealerDetailsReq.txt", dealers);
            }
            hitURLforDealer(dealers);
        } catch (Exception ex) {

            Timber.tag("StartActivity-Format-").e(ex.getMessage(), "");
        }
    }

    private void hitURLforDealer(String dealers) {
        try {
            Show(context.getResources().getString(R.string.Please_wait),
                    context.getResources().getString(R.string.Dealer_Details));

            XML_Parsing request = new XML_Parsing(StartActivity.this, dealers, 1);
            request.setOnResultListener(new XML_Parsing.OnResultListener() {

                @Override
                public void onCompleted(String isError, String msg, String ref, String flow, Object object) {
                    Dismiss();
                    if (isError == null || isError.isEmpty()) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Login),
                                context.getResources().getString(R.string.Invalid_Response_from_Server_Please_try_again),
                                "", 0);
                        return;
                    }

                    if (!isError.equals("00")) {
                        show_AlertDialog(
                                context.getResources().getString(R.string.Login),
                                context.getResources().getString(R.string.ResponseCode)+isError,
                                context.getResources().getString(R.string.ResponseCode)+msg,0);
                    } else {
                        Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);

                    }
                }
            });
            request.execute();
        } catch (Exception ex) {

            Timber.tag("StartActivity-Request-").e(ex.getMessage(), "");
        }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Startbutton();
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
                        grantPermission();

                    }
                }

                break;

            default:
                break;
        }
    }

    private boolean Startbutton() {
        try {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //DEVICEID = Build.getSerial();
            //DEVICEID = "MTR4361880";
            DEVICEID = "0110000106";
            toolbarFpsidValue.setText(DEVICEID);
            toolbarLatitudeValue.setText(latitude);
            toolbarLongitudeValue.setText(longitude);
        }
        } catch (Exception ex) {
            Timber.tag("StartActivity-onCreate-").e(ex.getMessage(), "");
            return false;
        }
        return true;
    }



    private void show_Dialogbox(String header,String msg ) {

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
                finish();
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
                if (i == 1) {
                    Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
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
    public void Show(String title,String msg){
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


/*
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

                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        } catch (Exception ex) {

            Timber.tag("StartAct-displyGps-").e(ex.getMessage(), "");
        }
    }



    @SuppressLint("HardwareIds")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private String get_Imei() {

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

            latitude = String.valueOf(loc.getLatitude());

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

 */