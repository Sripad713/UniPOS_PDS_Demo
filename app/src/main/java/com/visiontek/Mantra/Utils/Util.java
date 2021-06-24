package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.visiontek.Mantra.R;

import org.apache.commons.net.util.Base64;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.visiontek.Mantra.Models.AppConstants.dealerConstants;


public class Util {

    public static void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void releaseMediaPlayer(Context context, MediaPlayer mp) {
        mp.stop();
        mp.release();
        mp = null;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean batterylevel(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) >= 30;
    }

    public static String ConsentForm(Context context) {
        String concent = context.getResources().getString(R.string.part1) + dealerConstants.stateBean.stateReceiptHeaderEn +
                context.getResources().getString(R.string.part2) + dealerConstants.stateBean.stateReceiptHeaderEn +
                context.getResources().getString(R.string.part3);
        return concent;
    }


    public static boolean diableMenu(Context context, int val) {
        int len;
        ArrayList<String> serialno;
        ArrayList<String> status;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        serialno = databaseHelper.get_MENU(2);
        status = databaseHelper.get_MENU(3);
        for (int i = 0; i < serialno.size(); i++) {
            len = Integer.parseInt(serialno.get(i));
            if (val == len && status.get(i).equals("N")) {
                return true;
            }
        }
        return false;
    }

    public static boolean networkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean RDservice(Context context) {
        Intent act;
        try {
            act = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");

            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            try {
                for (int i = 0; i < activities.size(); i++) {
                    System.out.println(">  >>>>>>> i=" + i + "," + activities.get(i));
                }
            } catch (Exception e) {
                System.out.println(">>>>>>>>>>>>" + e);
                e.printStackTrace();
                return false;
            }
            final boolean isIntentSafe = activities.size() > 0;

            if (!isIntentSafe) {
                //Toast.makeText(context, "No RD Service Available", Toast.LENGTH_SHORT).show();
                return isIntentSafe;
            } else {
                //Toast.makeText(context, "RD Service Available", Toast.LENGTH_SHORT).show();
                return isIntentSafe;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean IRISRDservice(Context context) {
        Intent act;
        try {
            act = new Intent("in.gov.uidai.rdservice.iris.CAPTURE");
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(act, PackageManager.MATCH_DEFAULT_ONLY);
            try {
                for (int i = 0; i < activities.size(); i++) {
                    System.out.println(">  >>>>>>> i=" + i + "," + activities.get(i));
                }
            } catch (Exception e) {
                System.out.println(">>>>>>>>>>>>" + e);
                e.printStackTrace();
                return false;
            }
            final boolean isIntentSafe = activities.size() > 0;

            if (!isIntentSafe) {
                //Toast.makeText(context, "No RD Service Available", Toast.LENGTH_SHORT).show();
                return isIntentSafe;
            } else {
                //Toast.makeText(context, "RD Service Available", Toast.LENGTH_SHORT).show();
                return isIntentSafe;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static PublicKey getPublicKey(String base64PublicKey) {
        PublicKey publicKey = null;
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(base64PrivateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static String encrypt(String data, String publicKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
        byte[] encrypted1 = Base64.encodeBase64(cipher.doFinal(data.getBytes()));
        String encrypted = new String(encrypted1);
        return encrypted;
    }

    public static String decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(data));
    }


    public static boolean adapter(Context context) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        assert batteryStatus != null;
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;

        return acCharge;
    }


    public static byte[] image(String print, String name, int align) throws IOException {

        byte[] logo = new byte[0];

        final TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(20f);
        StaticLayout staticLayout;
        if (align == 1) {
            staticLayout = new StaticLayout(print, 0, print.length(),
                    textPaint, 300, Layout.Alignment.ALIGN_CENTER, 1.0f,
                    1.0f, false);
        } else {
            staticLayout = new StaticLayout(print, 0, print.length(),
                    textPaint, 300, Layout.Alignment.ALIGN_NORMAL, 1.0f,
                    1.0f, false);
        }

        float baseline = (int) (-textPaint.ascent() + 3f);
        int linecount = staticLayout.getLineCount();

        int height = (int) (baseline + textPaint.descent() + 3) * linecount + 10;

        final Bitmap bmp = Bitmap.createBitmap(384, height, Bitmap.Config.ARGB_8888); //use ARGB_8888 for better quality

        final Canvas canvas = new Canvas(bmp);
        canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF);

        staticLayout.draw(canvas);

        FileOutputStream stream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + name); //create your FileOutputStream here
        BufferedOutputStream bos = new BufferedOutputStream(stream);

        Bitmap image = addBorder(bmp, 2, Color.WHITE);

        image.compress(Bitmap.CompressFormat.PNG, 100, bos);

        bos.flush();
        bos.close();

        try {
            logo = LogoUtil.toBytes(Environment.getExternalStorageDirectory() + "/" + name);
            System.out.println(".............." + Arrays.toString(logo));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logo;
    }

    public static Bitmap addBorder(Bitmap bmp, int borderSize, int borderColor) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize
                * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(borderColor);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap getImageFromAssetsFile(Context ctx, String s) {
        Bitmap image = null;
        File sdcard = Environment.getExternalStorageDirectory();
        String bmp_file = sdcard + "/" + s;
        image = BitmapFactory.decodeFile(bmp_file);

        return image;
    }


    public static String getAppVersionFromPkgName(Context context) {
        String version;
        try {

            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo("com.visiontek.Mantra", 0);
            version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static boolean checkdotvalue(String Qty) {
        int i = 0, count = 0;
        int len = 0, dotPos = 0;

        for (i = 0; i < (Qty.length()); i++) {
            if (Qty.charAt(0) == '.') {
                return false;
            } else if (Qty.charAt((Qty.length()) - 1) == '.') {
                return false;
            } else if (Qty.charAt(i) == '.') {
                dotPos = i;
                count++;
            }
        }

        if (count > 1) {
            return false;
        } else {
            if (dotPos == 0) {
                return true;
            } else {
                len = (Qty.length()) - (dotPos + 1);
                if (len > 3) {
                    return false;
                }
            }
        }

        return true;
    }
    //***************************************************************************


}
