package com.winlator;

import android.app.Application;

import com.winlator.core.CrashHandler;

/**
 * Application subclass used to install process-wide hooks (crash handler).
 */
public class WinlatorApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.install(this);
    }
}
