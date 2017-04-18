package com.mattaniahbeezy.icecreamlibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wearprefs.WearPrefs;

/**
 * Created by Mattaniah on 8/9/2015.
 */
public class SettingsHelper {
    Context context;
    SharedPreferences sharedPreferences;

    final String timePatternKey = "key:timepattern";

    final static public String candelLightKey = "key:candelLightiing";

    public final static String longitudeKey = "key:longitude";
    public final static String latitudeKey = "key:latitude";
    public final static String elevationKey = "key:elevation";

    public SettingsHelper(Context context) {
        this.context = context;
        WearPrefs.init(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void setInt(String key, int newValue) {
        sharedPreferences.edit().putInt(key, newValue).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void setString(String key, String newValue) {
        sharedPreferences.edit().putString(key, newValue).apply();
    }

    public String getTimePatern() {
        return isTwentyFourHours() ? "H:mm" : "h:mm";
    }

    public boolean isTwentyFourHours() {
        return sharedPreferences.getBoolean(timePatternKey, false);
    }

    public void setTimePattern(boolean isTwentyFourHour) {
        sharedPreferences.edit().putBoolean(timePatternKey, isTwentyFourHour).apply();
    }

}
