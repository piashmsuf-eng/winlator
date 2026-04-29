package com.winlator;

import android.app.Application;

import androidx.preference.PreferenceManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.winlator.core.CrashHandler;

/**
 * Application subclass used to install process-wide hooks (crash handler,
 * Material You / dynamic-color theme).
 */
public class WinlatorApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.install(this);

        // Enable Material You wallpaper-derived colors on Android 12+.
        // Disabled by default to keep the existing dark theme as the baseline;
        // user can opt-in via Settings -> "Dynamic Colors (Material You)".
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("dynamic_colors", false);
        if (enabled) {
            DynamicColors.applyToActivitiesIfAvailable(this,
                    new DynamicColorsOptions.Builder().build());
        }
    }
}
