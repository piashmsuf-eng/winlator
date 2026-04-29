package com.winlator.core;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.winlator.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Installs a global UncaughtExceptionHandler that surfaces the stack trace
 * in a dialog with a Copy-to-Clipboard button instead of failing silently.
 *
 * Helps users file useful bug reports.
 */
public final class CrashHandler {
    private CrashHandler() {}

    public static void install(final Application app) {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                final String trace = formatTrace(app, t, e);
                showDialogOnMainThread(app, trace);
            } catch (Throwable ignored) {
                // never block the original handler
            }
            if (defaultHandler != null) defaultHandler.uncaughtException(t, e);
        });
    }

    private static void showDialogOnMainThread(final Context context, final String trace) {
        new android.os.Handler(Looper.getMainLooper()).post(() -> {
            try {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.crash_dialog_title)
                        .setMessage(trace)
                        .setPositiveButton(R.string.copy_to_clipboard, (d, w) -> {
                            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("winlator-crash", trace));
                            Toast.makeText(context, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .show();
            } catch (Throwable ignored) {
                // We may not have a foreground activity; bail.
            }
        });
    }

    private static String formatTrace(Context ctx, Thread t, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Winlator crash report");
        pw.println("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(new java.util.Date()));
        pw.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.DEVICE + ")");
        pw.println("Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")");
        pw.println("ABI: " + Build.SUPPORTED_ABIS[0]);
        try {
            pw.println("App: " + ctx.getPackageName() + " "
                    + ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName);
        } catch (Throwable ignored) {}
        pw.println("Thread: " + t.getName());
        pw.println();
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
