package com.visiontek.Mantra.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {


        private static com.visiontek.Mantra.Utils.SharedPref SharedPref;
        private final SharedPreferences sharedPreferences;

        public static com.visiontek.Mantra.Utils.SharedPref getInstance(Context context) {
            if (SharedPref == null) {
                SharedPref = new SharedPref(context);
            }
            return SharedPref;
        }

        public SharedPref(Context context) {
            sharedPreferences = context.getSharedPreferences("PDS", Context.MODE_PRIVATE);
        }

        public void saveData(String key, String value) {
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            prefsEditor .putString(key, value);
            prefsEditor.apply();
        }

    public void saveData(String key, int value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putInt(key, value);
        prefsEditor.apply();
    }

        public String getData(String key) {
            if (sharedPreferences!= null) {
                return sharedPreferences.getString(key, "");
            }
            return null;
        }

    public int getIntegerData(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getInt(key, -1);
        }
        return -1;
    }

}
