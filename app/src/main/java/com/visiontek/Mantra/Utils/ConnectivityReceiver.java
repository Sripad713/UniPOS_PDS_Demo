package com.visiontek.Mantra.Utils;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.visiontek.Mantra.Activities.StartActivity;


public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            //    Toast.makeText(context, "Conn Changed", Toast.LENGTH_SHORT).show();
            isConnectedToNetwork(context);
//            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//            boolean wifiConnected=false;
//            boolean mobile=false;
//
//            //     textView.setText(str);
//
//            if (networkInfo != null && networkInfo.isConnected()) {
//                wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
//                mobile = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
//
//                if (wifiConnected) {
//                    Toast.makeText(context, "Connected via WiFi", Toast.LENGTH_SHORT).show();
//                } else if (mobile) {
//                    Toast.makeText(context, "Connected via Mobile data", Toast.LENGTH_SHORT).show();
//                }
//
//            }else {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("No Internet Connection");
//                builder.setMessage("You need to connect to a network or Wifi.");
//                builder.setCancelable(true);
//                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            }
        }
    }

    private boolean isConnectedToNetwork(final Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean wifiConnected=false;
        boolean mobile=false;

        if (networkInfo != null && networkInfo.isConnected()) {
            wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobile = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

            if (wifiConnected) {
                //       Toast.makeText(context, "Connected via WiFi", Toast.LENGTH_SHORT).show();
            } else if (mobile) {
                //       Toast.makeText(context, "Connected via Mobile data", Toast.LENGTH_SHORT).show();
            }

        }else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No Internet Connection");
            builder.setMessage("You need to connect to a network or Wifi.");
            builder.setCancelable(true);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    context.startActivity(new Intent(context, StartActivity.class));
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(context, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }


        if (mobile || wifiConnected)
            return true;
        else return false;
    }
}
