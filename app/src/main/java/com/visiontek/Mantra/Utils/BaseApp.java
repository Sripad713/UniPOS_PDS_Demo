package com.visiontek.Mantra.Utils;

import android.app.Application;
import android.content.Intent;

import com.visiontek.Mantra.Services.Rhmservice;

import timber.log.Timber;

public class BaseApp extends Application {
    protected static BaseApp instance;
    public BaseApp() {
        super();
        instance = this;
        Timber.plant(new FileLoggingTree());
    }

    public static BaseApp get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            startService(new Intent(this, Rhmservice.class));
        } catch (Exception e) {
            Timber.e("BASE" + " Exception :: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            stopService(new Intent(this, Rhmservice.class));
        } catch (Exception e) {
            Timber.e("BASE" + " Exception :: " + e.getLocalizedMessage());
        }
    }
}