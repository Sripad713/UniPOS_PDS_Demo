package com.visiontek.Mantra.Utils;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;
import static com.visiontek.Mantra.Models.AppConstants.longitude;
import static com.visiontek.Mantra.Models.AppConstants.latitude;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class RhmsUtils {
    Context context;
    static String sSerNo;
    String rhmsClientVerion;
    List<SubscriptionInfo> subscriptionInfoList = new ArrayList<>();
    TelephonyInfo telephonyInfo;

    public static boolean boot;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public RhmsUtils(Context context) {
        this.context = context;

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            rhmsClientVerion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 0, locationListener);

        SubscriptionManager subscriptionManager;
        subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        telephonyInfo = TelephonyInfo.getInstance(context);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sSerNo = android.os.Build.getSerial();
        } else {
            sSerNo = Build.SERIAL;
        }
        startMainTask();

    }

    public void startMainTask() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                details();
            }
        }, 20000);
    }



    private static class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            latitude = String.valueOf(loc.getLatitude());
            longitude = String.valueOf(loc.getLongitude());
            System.out.println(latitude + "___________"+longitude);

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void details() {
        try {
            String request_healthstatus = "<HealthStatus>\n" +
                    "<SerialNo>" + sSerNo + "</SerialNo>\n" +
                    "<Date_Time>" + updateTimeDate() + "</Date_Time>\n" +
                    "<GPS>\n" +
                    "<Latitude>" + latitude + "</Latitude>\n" +
                    "<Longitude>" + longitude + "</Longitude>\n" +
                    "<Captured_Time>" + updateTimeDate() + "</Captured_Time>\n" +
                    "</GPS>\n" +
                    "<Adapter>" + adapterandbattery(1) + "</Adapter>\n" +
                    "<Battery>" + adapterandbattery(2) + "</Battery>\n" +
                    "<RTC>Success</RTC>\n" +
                    "<BatteryVoltage>" + batteryvol() + "</BatteryVoltage>\n" +
                    "<Comm>" + checkConnection() + "</Comm>\n" +
                    "<System_memory>\n" +
                    "<Total>" + getTotalInternalMemorySize() + "</Total>\n" +
                    "<Use>" + getUsedInternalMemmory() + "</Use>\n" +
                    "<Free>" + getAvailableInternalMemorySize() + "</Free>\n" +
                    "</System_memory>\n" +
                    "<RAM>\n" +
                    "<Total>" + getTotRAM() + "</Total>\n" +
                    "<Use>" + getUsedRAM() + "</Use>\n" +
                    "<Free>" + getAvalRAM() + "</Free>\n" +
                    "</RAM>\n" +
                    "</HealthStatus>";
            System.out.println("REQUEST_HEALTH\n" + request_healthstatus);
            new SetSoap1().execute("https://rhms2.visiontek.co.in/api/HealthStatus", request_healthstatus);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }


    public class SetSoap1 extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String post_url = params[0];
            String request = params[1];

            try {
                URL url = new URL(post_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);
                connection.setUseCaches(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/xml");
                connection.setRequestProperty("Content-Type", "application/xml");
                OutputStream outputStream = connection.getOutputStream();
                byte[] b = request.getBytes("UTF-8");
                outputStream.write(b);
                outputStream.flush();
                outputStream.close();
                InputStream inputStream = connection.getInputStream();
                byte[] res = new byte[2048];
                int i = 0;
                StringBuilder response = new StringBuilder();
                while ((i = inputStream.read(res)) != -1) {
                    response.append(new String(res, 0, i));
                }
                System.out.println(response);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(Boolean result) {


            try {
                if (!boot) {
                    String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    String request_hardware = "<DeviceDetails>\n" +
                            "<SerialNo>" + sSerNo + "</SerialNo>\n" +
                            "<Date_Time>" + updateTimeDate() + "</Date_Time>\n" +
                            "<UbootImageName>" + Build.BOOTLOADER + "</UbootImageName>\n" +
                            "<KernelImageName>" + Build.HOST + "</KernelImageName>\n" +
                            "<RootfsImageName>" + Build.HARDWARE + "</RootfsImageName>\n" +
                            "<IMEInumberExists>" + imeiexists() + "</IMEInumberExists>\n" +
                            "<IMEInumber>" + imei() + "</IMEInumber>\n" +
                            "<UIDExists>Yes</UIDExists>\n" +
                            "<UID>" + Build.ID + "</UID>\n" +
                            "<WiFiMACIDExists>" + wifimacidexists() + "</WiFiMACIDExists>\n" +
                            "<WiFiMACID>" + wifimacid() + "</WiFiMACID>\n" +
                            "<HardwareID>" + android_id + "</HardwareID>\n" +
                            "</DeviceDetails>\n";

                    System.out.println("REQUEST_HARDWARE\n" + request_hardware);
                    new SetSoap2().execute("https://rhms2.visiontek.co.in/api/HardwareStatus", request_hardware);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    private class SetSoap2 extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String post_url = params[0];
            String request = params[1];
            try {
                URL url = new URL(post_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);
                connection.setUseCaches(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/xml");
                connection.setRequestProperty("Content-Type", "application/xml");
                OutputStream outputStream = connection.getOutputStream();
                byte[] b = request.getBytes("UTF-8");
                outputStream.write(b);
                outputStream.flush();
                outputStream.close();
                InputStream inputStream = connection.getInputStream();
                byte[] res = new byte[2048];
                int i = 0;
                StringBuilder response = new StringBuilder();
                while ((i = inputStream.read(res)) != -1) {
                    response.append(new String(res, 0, i));
                }
                System.out.println(response);
                inputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        @Override
        protected void onPostExecute(Boolean result) {
            try {
                String requestboot = "<BootStatus>\n" +
                        "<SerialNo>" + sSerNo + "</SerialNo>\n" +
                        "<Date_Time>" + updateTimeDate() + "</Date_Time>\n" +
                        "<Ethernet>" + checkEthernet(context) + "</Ethernet>\n" +
                        "<Bluetooth>" + btstatus() + "</Bluetooth>\n" +
                        "<WIFI>" + wifistatus(context) + "</WIFI>\n" +
                        "<Camera>" + checkCameraHardware(context) + "</Camera>\n" +
                        "<Audio>" + audiostatus(context) + "</Audio>\n" +
                        "<ExternalMemExists>" + externalmem() + "</ExternalMemExists>\n" +
                        "<UsbdeviceExists>" + usb(context) + "</UsbdeviceExists>\n" +
                        "<SIM1CCIDnumberExists>" + get_state(1, telephonyInfo) + "</SIM1CCIDnumberExists>\n" +
                        "<SIM1Operator>" + get_Operator(1, subscriptionInfoList) + "</SIM1Operator>\n" +
                        "<SIM2CCIDnumberExists>" + get_state(2, telephonyInfo) + "</SIM2CCIDnumberExists>\n" +
                        "<SIM2Operator>" + get_Operator(2, subscriptionInfoList) + "</SIM2Operator>\n" +
                        "<RHMSClientVersion>" + rhmsClientVerion + "</RHMSClientVersion>\n" +
                        "</BootStatus>\n";
                System.out.println("REQUEST_BOOT\n" + requestboot);
                new SetSoap3().execute("https://rhms2.visiontek.co.in/api/BootTimeStatus", requestboot);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    public class SetSoap3 extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... params) {
            String post_url = params[0];
            String request = params[1];
            try {
                URL url = new URL(post_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);
                connection.setUseCaches(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/xml");
                connection.setRequestProperty("Content-Type", "application/xml");
                OutputStream outputStream = connection.getOutputStream();
                byte[] b = request.getBytes("UTF-8");
                outputStream.write(b);
                outputStream.flush();
                outputStream.close();
                InputStream inputStream = connection.getInputStream();
                byte[] res = new byte[2048];
                int i = 0;
                StringBuilder response = new StringBuilder();
                while ((i = inputStream.read(res)) != -1) {
                    response.append(new String(res, 0, i));
                }
                System.out.println(response);
                inputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //?origin=eProcurement
            boot=true;
        }
    }

    public String getAppVersionFromPkgName(Context context, String pack) {
        String version;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(pack, 0);
            version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String adapterandbattery(int type) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        assert batteryStatus != null;
        if (type == 1) {
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            if (acCharge) {
                return "Connected";
            } else {
                return "NotConnected";
            }
        } else {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float) scale;
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            if (batteryPct * 100 == 50) {
                return "NotConnected";
            } else if (!usbCharge && !acCharge) {
                return "Discharging";
            } else {
                if (batteryPct * 100 == 100) {
                    return "Charged";
                } else {
                    return "Charging";
                }
            }
        }
    }

    public String updateTimeDate() {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentDate, currentTime;
        currentDate = date.format(new Date());
        currentTime = time.format(new Date());
        return currentDate + "T" + currentTime;
    }

    public String imei() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "NotFound";
        }
        String imei = telephonyManager.getDeviceId();
        if (imei != null && !imei.isEmpty()) {
            return imei;
        }
        return "NotFound";
    }

    public String imeiexists() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "Error";
        }
        String imei = telephonyManager.getDeviceId();
        if (imei != null && !imei.isEmpty()) {
            return "Yes";
        }
        return "Error";
    }

    public String wifimacid() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        if (macAddress != null) {
            return macAddress;
        } else {
            return "Error";
        }
    }

    public String wifimacidexists() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        if (!wInfo.equals(null)) return "Yes";
        else return "Error";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String checkConnection() {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "GSM";
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return "Ethernet";
            }
        }
        return String.valueOf(activeNetworkInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return formatSize(totalBlocks * blockSize);
    }

    public static String formatSize(long size) {
        String suffix = null;
        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
            if (size >= 1024) {
                suffix = "GB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return formatSize(blockSize * availableBlocks);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getUsedInternalMemmory() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long totalBlocks = stat.getBlockCountLong();
        long UsedBlocks = totalBlocks * blockSize - blockSize * availableBlocks;
        return formatSize(UsedBlocks);

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getTotRAM() {
        String lastValueaval = "";
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long totalMemory = mi.totalMem / 1048576L;
        if (totalMemory > 1) {
            lastValueaval = twoDecimalForm.format(totalMemory).concat("MB");
        } else {
            lastValueaval = twoDecimalForm.format(totalMemory).concat("KB");
        }
        System.out.println("LAST" + lastValueaval);
        return lastValueaval;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getUsedRAM() {
        String lastValueaval = "";
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long totalMemory = mi.totalMem / 1048576L;
        long freeMemory = mi.availMem / 1048576L;
        long usedMemory = totalMemory - freeMemory;
        if (usedMemory > 1) {
            lastValueaval = twoDecimalForm.format(usedMemory).concat("MB");
        } else {
            lastValueaval = twoDecimalForm.format(usedMemory).concat("KB");
        }
        System.out.println("LAST" + lastValueaval);
        return lastValueaval;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getAvalRAM() {
        String lastValueaval = "";
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long freeMemory = mi.availMem / 1048576L;
        if (freeMemory > 1) {
            lastValueaval = twoDecimalForm.format(freeMemory).concat("MB");
        } else {
            lastValueaval = twoDecimalForm.format(freeMemory).concat("KB");
        }
        System.out.println("LAST" + lastValueaval);
        return lastValueaval;
    }

    public String batteryvol() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batStatus = context.registerReceiver(null, filter);
        int batteryVol = batStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        float fullVolt = (float) (batteryVol * 0.001);
        DecimalFormat decFormt = new DecimalFormat("#.#");
        Double decVolt = Double.valueOf(decFormt.format(fullVolt));
        return ((decVolt + "V"));
    }

    public String checkEthernet(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return "Connected";
            }

        }
        return "NotConnected";
    }

    public String usb(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
            return "Yes";
        } else {
            return "No";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public String get_Operator(int type, List<SubscriptionInfo> subscriptionInfoList) {
        if (subscriptionInfoList != null && subscriptionInfoList.size() > 0) {
            StringBuilder Carrier = new StringBuilder();
            for (int i = 0; i < subscriptionInfoList.size(); i++) {
                SubscriptionInfo lsuSubscriptionInfo = subscriptionInfoList.get(i);
                Carrier.append("").append(lsuSubscriptionInfo.getDisplayName()).append("");
                if (type == 1) {
                    return String.valueOf(Carrier);
                }
                if (type == 2 && i == 1) {
                    return String.valueOf(Carrier);
                }
            }
        }
        return "NotFound";
    }

    public String btstatus() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return "No";
        } else if (!mBluetoothAdapter.isEnabled()) {
            return "No";
        } else {
            return "Yes";
        }
    }

    public String externalmem() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();
        if (isSDSupportedDevice && isSDPresent) return "Yes";

        else return "No";
    }

    public String wifistatus(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() != 0) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public String checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public String audiostatus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            return "Success";
        } else return "Failure";
    }

    public String get_state(int count, TelephonyInfo telephonyInfo) {
        if (count == 1) {
            if (telephonyInfo.isSIM1Ready()) {
                return "Yes";
            } else {
                return "NO_SIM";
            }
        } else {
            if (telephonyInfo.isSIM2Ready()) {
                return "Yes";
            } else {
                return "NO_SIM";
            }
        }
    }

}
