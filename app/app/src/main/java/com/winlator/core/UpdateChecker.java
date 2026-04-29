package com.winlator.core;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.winlator.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Polls the latest GitHub release for the configured fork and offers to open
 * its download page when a newer tag is found.
 *
 * No-op unless the user has opted in via Settings -> "Check for updates".
 */
public final class UpdateChecker {
    private static final String TAG = "UpdateChecker";
    private static final String REPO = "piashmsuf-eng/winlator";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String RELEASES_PAGE = "https://github.com/" + REPO + "/releases";
    private static final long MIN_INTERVAL_MS = 24L * 60 * 60 * 1000; // 1 day

    private UpdateChecker() {}

    public static void maybeCheck(final Activity activity, final String currentVersionName) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (!prefs.getBoolean("check_for_updates", true)) return;

        long lastCheck = prefs.getLong("update_last_check", 0L);
        if (System.currentTimeMillis() - lastCheck < MIN_INTERVAL_MS) return;

        new Thread(() -> {
            String latestTag = fetchLatestTag();
            if (latestTag == null) return;
            prefs.edit().putLong("update_last_check", System.currentTimeMillis()).apply();
            if (isNewer(latestTag, currentVersionName)) {
                activity.runOnUiThread(() -> showUpdateDialog(activity, latestTag));
            }
        }, "winlator-update-check").start();
    }

    private static String fetchLatestTag() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "Winlator-UpdateChecker");
            if (conn.getResponseCode() != 200) return null;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            return json.optString("tag_name", null);
        } catch (Exception e) {
            Log.w(TAG, "Update check failed: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Naive semver comparison: strips a leading 'v' and compares numeric segments.
     * Returns true when {@code latest} > {@code current}.
     */
    private static boolean isNewer(String latest, String current) {
        if (latest == null || current == null) return false;
        int[] a = parse(latest);
        int[] b = parse(current);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        return false;
    }

    private static int[] parse(String v) {
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        String[] parts = v.split("[^0-9]+");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {
                out[i] = 0;
            }
        }
        return out;
    }

    private static void showUpdateDialog(Activity activity, String tag) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.update_available_title)
                .setMessage(activity.getString(R.string.update_available_message, tag))
                .setPositiveButton(R.string.open, (d, w) -> {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_PAGE));
                    activity.startActivity(i);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
