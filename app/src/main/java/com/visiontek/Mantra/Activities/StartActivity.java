package com.visiontek.Mantra.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.visiontek.Mantra.Database.DatabaseHelper;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetURLDetails.fpsCommonInfoModel.fpsCommonInfo;
import com.visiontek.Mantra.Models.DealerDetailsModel.GetUserDetails.DealerModel;
import com.visiontek.Mantra.Models.RHMS;
import com.visiontek.Mantra.R;
import com.visiontek.Mantra.Utils.FileLoggingTree;
import com.visiontek.Mantra.Utils.RhmsUtils;
import com.visiontek.Mantra.Utils.SharedPref;
import com.visiontek.Mantra.Utils.Util;
import com.visiontek.Mantra.Utils.XML_Parsing;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

import static com.visiontek.Mantra.Models.AppConstants.DEVICEID;
import static com.visiontek.Mantra.Models.AppConstants.Dealername;
import static com.visiontek.Mantra.Models.AppConstants.Debug;
import static com.visiontek.Mantra.Models.AppConstants.VERSION_NO;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.offlineEligible;
import static com.visiontek.Mantra.Models.AppConstants.txnType;
import static com.visiontek.Mantra.Utils.Util.preventTwoClick;
import static com.visiontek.Mantra.Utils.Util.releaseMediaPlayer;
public class StartActivity extends BaseActivity {
    int PSp, RS, NS, AC, WS, WC, AF;
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int REQUEST_STORAGE_WRITE_SDCARD = 3;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 4;
    static String L;
    static MediaPlayer mp;
    Context context;
    Button start, quit, settings;
    ProgressDialog pd = null;
    SharedPref SharedPref;

    /************* OFFLINE DECLARATIONS ***************/
    DatabaseHelper db;
    DealerModel dealerModel = new DealerModel();

    /*************************************************/
    @Override
    public void initialize() {

        try {
            context = StartActivity.this;
            SharedPref = com.visiontek.Mantra.Utils.SharedPref.getInstance(context);
            db = new DatabaseHelper(context);
            checkLanguage();
            Timber.plant(new FileLoggingTree());
            Timber.d("Kiran is a good boy");
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
                SharedPref.saveData("MODE",-1);
                if (DEVICEID != null && Startbutton() && DEVICEID.length() > 0) {
                    if (Util.networkConnected(context)) {
                        hitit();
                    } else {
                        txnType = -1;
                        System.out.println();
                        int offLineCheckFlag = db.checkForOfflineDistribution();
                        if (offLineCheckFlag == 0)  {
                            offlineEligible = 1;
                            System.out.println("@@offline eligible");
                            show_AlertDialog(context.getResources().getString(R.string.Internet_Not_Available), context.getResources().getString(R.string.proceed_offline), "", 2);
                        } else if (offLineCheckFlag < 0)
                            show_AlertDialog(context.getResources().getString(R.string.Offline_Data_Not_available),  context.getResources().getString(R.string.Please_login_in_online_mode), "", 3);
                        else
                            show_AlertDialog(context.getResources().getString(R.string.Login),context.getResources().getString(R.string.Internet_Connection), context.getResources().getString(R.string.Internet_Connection_Msg), 3);
                    }
                } else {
                    show_AlertDialog(
                            context.getResources().getString(R.string.Login),
                            "Permissions", "Please Grant All Permission", 0);
                }
            });

            quit.setOnClickListener(view -> {
                preventTwoClick(view);
                show_Dialogbox(context.getResources().getString(R.string.CHHATISGARHPDS),
                        context.getResources().getString(R.string.Do_you_want_to_Cancel)
                );
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
            System.out.println("@@Exception: " + ex.toString());
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
            System.out.println("@@Exception111...." + ex.toString());
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
        } else if (AF != PackageManager.PERMISSION_GRANTED) {
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
                    "            <VersionNo>" + VERSION_NO + "</VersionNo>\n" +//static
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
            if (Debug) {
                Util.generateNoteOnSD(context, "DealerDetailsReq.txt", dealers);
            }
            hitURLforDealer(dealers);
            Timber.d("StartActivity-FramexmlforDealerDetails Req : "+dealers);
        } catch (Exception ex) {
            System.out.println("@@Exceptionnnnnn: " + ex.toString());
            //Timber.tag("StartActivity-Format-").e(ex.getMessage(), "");
            Timber.e("StartActivity-Format "+ex.getLocalizedMessage());
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
                                context.getResources().getString(R.string.ResponseCode) + isError,
                                context.getResources().getString(R.string.ResponseCode) + msg, 0);
                    } else {
                        /********************** OFFLINE CODE *******************/
                        System.out.println("@@Inserting into DB:");
                        int ret = db.insertStateDetails(dealerConstants.stateBean.stateCode, dealerConstants.stateBean.stateNameEn, dealerConstants.stateBean.stateNameLl, dealerConstants.stateBean.stateProfile, dealerConstants.stateBean.stateReceiptHeaderEn, dealerConstants.stateBean.stateReceiptHeaderLl, dealerConstants.stateBean.statefpsId, dealerConstants.stateBean.consentHeader, dealerConstants.stateBean.consentHeader);
                        System.out.println("@@Value of ret: " + ret);
                        System.out.println("@@Checking for partial online eligibility");
                        fpsCommonInfo fpsCommonInfoData = dealerConstants.fpsCommonInfo;
                        if (fpsCommonInfoData.partialOnlineOfflineStatus.equals("Y")) {
                            System.out.println("@@Partial online offline status enabled... Downling offline data");
                            if (Util.networkConnected(context)) {

                                System.out.println("@@Going to DealerDetailsActivity");
                                Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            } else {
                                show_AlertDialog(context.getResources().getString(R.string.Internet_Connection_Msg), context.getResources().getString(R.string.Internet_Connection), "", 3);
                            }
                        } else {

                            if(fpsCommonInfoData.partialOnlineOfflineStatus.equals("N")){

                                System.out.println("@@Data not available in download");
                                System.out.println("@@Data not available in download please go online");
                                Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);

                            }
                            /*if (fpsCommonInfoData.partialOnlineOfflineStatus.equals("Y"))
                            {
                                System.out.println("@@OFFLINE PENDING UPLOADINS >>>>");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String errorMessage = "";
                                        OfflineUploadNDownload offlineUploadNDownload = new OfflineUploadNDownload(context);
                                        int pendingTxns = db.getPendingTxnCount();
                                        if (pendingTxns > 0){
                                            int ret = offlineUploadNDownload.ManualServerUploadPartialTxns(fpsCommonInfoData.fpsId, fpsCommonInfoData.fpsSessionId);
                                            System.out.println("RET>>>>>>>   "+ret);
                                            if (ret == -2) {
                                                errorMessage = "Internet not available";
                                            }
                                            if(ret == 0){
                                                //Delete Database
                                                System.out.println("DELETEEEE");
                                                db.forceDelteKeyRegNPosOb();
                                            }
                                        }

                                        String finalErrorMessage = errorMessage;
                                        StartActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(finalErrorMessage.isEmpty()){
                                                    System.out.println("@@Data not available in download");
                                                    System.out.println("@@Data not available in download please go online");
                                                    Intent i = new Intent(StartActivity.this, DealerDetailsActivity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(i);
                                                }
                                            }
                                        });
                                    }
                                }).start();
                            }*/
                        }
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            System.out.println("@@Exceptiooooo: " + ex.toString());
            Timber.e("StartActivity-hitURLforDealer Exception ==> :"+ex.getLocalizedMessage());
            //Timber.tag("StartActivity-Request-").e(ex.getMessage(), "");
        }
    }

    //Offline
    public void Dismiss() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    //Offline
    public void Show(String title, String msg) {
        SpannableString ss1 = new SpannableString(title);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);
        SpannableString ss2 = new SpannableString(msg);
        ss2.setSpan(new RelativeSizeSpan(3f), 0, ss2.length(), 0);


        pd.setTitle(ss1);
        pd.setMessage(ss2);
        pd.setCancelable(false);
        pd.show();
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
            System.out.println("@@Exceptttttt: " + ex.toString());
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
    private boolean Startbutton(){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                   DEVICEID = Build.getSerial();//field
                  //DEVICEID = "MTR4361844";
                  //DEVICEID = "0110000106";//our device
                   DEVICEID = "MTR4361828";//present
                 toolbarFpsidValue.setText(DEVICEID);
                 toolbarLatitudeValue.setText(latitude);
                 toolbarLongitudeValue.setText(longitude);
            }
        } catch (Exception ex) {
            System.out.println("@@Exceptiooonnnnn: " + ex.toString());
            Timber.tag("StartActivity-onCreate-").e(ex.getMessage(), "");
            return false;
        }
        return true;
    }

    private void show_Dialogbox(String header, String msg) {

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
                //finish();
                finishAffinity();
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

    private void show_AlertDialog(String headermsg,String bodymsg, String talemsg, int i) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.alertdialog);
        Button confirm = (Button) dialog.findViewById(R.id.alertdialogok);
        TextView head = (TextView) dialog.findViewById(R.id.alertdialoghead);
        TextView body = (TextView) dialog.findViewById(R.id.alertdialogbody);
        TextView tale = (TextView) dialog.findViewById(R.id.alertdialogtale);
        //TextView title = (TextView) dialog.findViewById(R.id.alertdialogTitle);
        head.setText(headermsg);
        body.setText(bodymsg);
        tale.setText(talemsg);
        //title.setText(titlemsg);
        confirm.setOnClickListener(v -> {
            preventTwoClick(v);
            dialog.dismiss();
            if (i == 1) {
                Intent i1 = new Intent(StartActivity.this, DealerDetailsActivity.class);
                i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i1);
            } else if (i == 2) {
                System.out.println("@@Displaying password_dialog");
                password_Dialog();
            } else if (i == 10) {
                startActivity(new Intent(context, Device_Update.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }





    //Offline
    private void password_Dialog() {
        if (mp != null) {
            releaseMediaPlayer(context, mp);
        }
        if (L.equals("hi")) {
        } else {
            mp = mp.create(context, R.raw.c100074);
            mp.start();
        }

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.uid);
        Button confirm = (Button) dialog.findViewById(R.id.confirm);
        Button back = (Button) dialog.findViewById(R.id.back);
        TextView tv = (TextView) dialog.findViewById(R.id.dialog);
        TextView status = (TextView) dialog.findViewById(R.id.status);
        final EditText enter = (EditText) dialog.findViewById(R.id.enter);
        tv.setText(context.getResources().getString(R.string.Password));
        status.setText(context.getResources().getString(R.string.Please_Enter_Password));
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preventTwoClick(v);
                System.out.println("@@Clicked on confirm");
                dialog.dismiss();
                dealerModel.EnterPassword = enter.getText().toString();
                System.out.println("@@Password entered: " + dealerModel.EnterPassword);
                if (!dealerModel.EnterPassword.isEmpty()) {
                    System.out.println("@@Password matched");
                    if (mp != null) {
                        System.out.println("@@Medi player not null");
                        releaseMediaPlayer(context, mp);
                    }
                    if (L.equals("hi")) {
                        System.out.println("@@Hindi language");
                    } else {

                        System.out.println("@@Eng lang");
                        mp = mp.create(context, R.raw.c100178);
                        mp.start();
                    }
                    System.out.println("@@Going to proceedinOffline API");
                    proceedinOffline(dealerModel.EnterPassword);
                } else {
                    System.out.println("@@Invalid password");
                    show_AlertDialog(Dealername,
                            context.getResources().getString(R.string.Invalid_Password),
                            context.getResources().getString(R.string.Please_Enter_a_valid_Password),
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
    }

    //offline
    public void proceedinOffline(String password) {
        System.out.println("@@In proceedinOffline mode received txnType: " + txnType);
        String errorMessage1 = db.loginByPassword(this, password);
        System.out.println("@@Data in error message: " + errorMessage1);
        if (errorMessage1.equalsIgnoreCase("Invalid password")) {
            System.out.println("@@Received invalid password");
            show_AlertDialog(context.getResources().getString(R.string.Invalid_login), context.getResources().getString(R.string.Invalid_password),
                    context.getResources().getString(R.string.Enter_valid_password), 3);
        } else if (errorMessage1.isEmpty()) {
            System.out.println("@@Response empty going to HomeActivity.java class");
            dealerConstants = null;
            Intent home = new Intent(context, HomeActivity.class);
            startActivity(home);
        } else {
            System.out.println("@@In else");
            show_AlertDialog(context.getResources().getString(R.string.Login), errorMessage1, "", 3);
        }
    }


    //================Checkfor update

    public void hitit() {
        try {

            String url = "https://rhms2.visiontek.co.in/api/ApplicationStatus?serialNo=" + Build.SERIAL;

            new makeservicecall().execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String finalResponse, msg;

    public class makeservicecall extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Show(context.getResources().getString(R.string.Downloading),
                    context.getResources().getString(R.string.Please_wait));
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(String... params) {
            String reqURL = params[0];
            String response = null;
            try {
                URL url = new URL(reqURL);
                URLConnection urlConnection = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
                httpConn.setRequestMethod("GET");
                try {
                    InputStream in = new BufferedInputStream(httpConn.getInputStream());
                    response = convertStreamToString(in);
                    finalResponse = response;


                } catch (Exception e) {
                    msg = "No Response for this Device";
                    e.printStackTrace();
                    return false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("==1" + e.getMessage());
                msg = e.getMessage();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Dismiss();
            if (result) {
                System.out.println("FINALREPONSE >>>>>>"+finalResponse);
                parseXml_dealer(finalResponse);
            } else {
                if (mp != null) {
                    releaseMediaPlayer(context, mp);
                }
                if (L.equals("hi")) {
                    mp = MediaPlayer.create(context, R.raw.c200175);
                } else {
                    mp = MediaPlayer.create(context, R.raw.c100175);
                }
                mp.start();
                txnType = 1;
                FramexmlforDealerDetails();

            }
        }
    }

    public String convertStreamToString(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            msg = e.getMessage();
        }
        return sb.toString();
    }

    public void parseXml_dealer(String xmlString) {
        ArrayList<RHMS> Application = new ArrayList<>();
        try {

            System.out.println("=============" + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlString));
            int eventType = xpp.getEventType();
            RHMS rhms = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("ProjectName")) {
                        eventType = xpp.next();
                        if (eventType == XmlPullParser.TEXT) {
                            rhms = new RHMS();
                            rhms.ProjectName = (xpp.getText());
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ApplicationType")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.ApplicationType = (xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ApplicationName")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.ApplicationName = (xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("Version")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.Version = (xpp.getText());
                            }
                        }
                    }
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("ApplicationURL")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                rhms.ApplicationURL = (xpp.getText());
                                Application.add(rhms);
                            }
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            msg = e.getMessage();

        }
        if (!check(Application)) {
            if (mp != null) {
                releaseMediaPlayer(context, mp);
            }
            if (L.equals("hi")) {
                mp = MediaPlayer.create(context, R.raw.c200175);
            } else {
                mp = MediaPlayer.create(context, R.raw.c100175);
            }
            mp.start();
            txnType = 1;
            FramexmlforDealerDetails();
        } else {
            show_AlertDialog(context.getResources().getString(R.string.TMS),
                    "",
                    context.getResources().getString(R.string.Update_Found),
                    10);
        }
    }

    private boolean check(ArrayList<RHMS> Application) {
        try {
            if (Application.size() > 0) {
                String appver;
                float version, appversion;
                for (int val = 0; val < Application.size(); val++) {
                    if (Application.get(val).ApplicationName.equals("MantraPDS")) {
                        version = Float.parseFloat(Application.get(val).Version);
                        appver = getAppVersionFromPkgName(context, Application.get(val).ApplicationType);
                        appversion = Float.parseFloat(appver);
                        //change
                        //Download(Application.get(val).ApplicationURL, Application.get(val).ApplicationName, Application.get(val).Version);
                        return appversion < version;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = e.getMessage();
        }
        return false;
    }

    public String getAppVersionFromPkgName(Context context, String Packagename) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
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