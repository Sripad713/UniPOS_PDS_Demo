package com.visiontek.Mantra.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.visiontek.Mantra.Utils.RhmsUtils;

import java.security.Provider;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class Rhmservice extends Service {

    public long RHMS_INTERVAL =60*60*1000;
    //public long RHMS_INTERVAL =3*60*1000;
    private Handler mHandler2 = new Handler();
    private Timer time = null;
    private RHMSTASK rhmstask = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        rhmstask.cancel();

        time.cancel();
        time.purge();
        this.stopSelf();
    }

    @Override
    public void onCreate() {
        if (time != null) {
            time.cancel();

        } else {
            time = new Timer();
        }
        rhmstask = new RHMSTASK();
        time.scheduleAtFixedRate(rhmstask, 0, RHMS_INTERVAL);
    }


    @Override
    public void onDestroy() {
        rhmstask.cancel();
        time.cancel();
        time.purge();
        super.onDestroy();
    }

    class RHMSTASK extends TimerTask {
        @Override
        public void run() {
            mHandler2.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    RhmsUtils info = new RhmsUtils(getApplicationContext());
                }
            });
        }
    }

}
