package com.winlator.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

/**
 * Lightweight in-game overlay that shows elapsed time, battery level,
 * and CPU/GPU thermal-zone temperature. Designed to live in a corner of
 * XServerDisplayActivity.
 */
public class PerfOverlayView extends TextView {
    private static final int UPDATE_INTERVAL_MS = 1500;
    private static final String[] TEMP_PATHS = {
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp"
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTimeMs = 0;
    private int batteryLevel = -1;
    private float batteryTempC = -1f;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            if (level > 0 && scale > 0) batteryLevel = (level * 100) / scale;
            int rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            if (rawTemp > 0) batteryTempC = rawTemp / 10f;
        }
    };

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateText();
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    public PerfOverlayView(Context context) { this(context, null); }

    public PerfOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextColor(Color.argb(220, 0, 255, 0));
        setBackgroundColor(Color.argb(140, 0, 0, 0));
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        setTypeface(android.graphics.Typeface.MONOSPACE);
        int p = (int) (4 * getResources().getDisplayMetrics().density);
        setPadding(p * 2, p, p * 2, p);
        setGravity(Gravity.START);
        setIncludeFontPadding(false);
    }

    public void start() {
        startTimeMs = System.currentTimeMillis();
        try {
            getContext().registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Throwable ignored) {}
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    public void stop() {
        handler.removeCallbacks(tick);
        try { getContext().unregisterReceiver(batteryReceiver); } catch (Throwable ignored) {}
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    private void updateText() {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        long mins = elapsed / 60000L;
        long secs = (elapsed / 1000L) % 60L;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%02d:%02d", mins, secs));
        if (batteryLevel >= 0) sb.append("  bat ").append(batteryLevel).append('%');
        if (batteryTempC > 0)  sb.append("  ").append(Math.round(batteryTempC)).append("\u00B0C");
        float cpuTemp = readThermal();
        if (cpuTemp > 0) sb.append("  cpu ").append(Math.round(cpuTemp)).append("\u00B0C");
        setText(sb.toString());
    }

    private static float readThermal() {
        for (String path : TEMP_PATHS) {
            File f = new File(path);
            if (!f.canRead()) continue;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line = br.readLine();
                if (line == null) continue;
                float v = Float.parseFloat(line.trim());
                if (v > 1000) v /= 1000f;
                if (v > 10 && v < 150) return v;
            } catch (Throwable ignored) {}
        }
        return -1;
    }
}
