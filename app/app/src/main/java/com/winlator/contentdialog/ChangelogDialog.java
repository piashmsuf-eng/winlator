package com.winlator.contentdialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winlator.R;
import com.winlator.core.AppUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class ChangelogDialog extends ContentDialog {
    private static final String RELEASES_URL = "https://api.github.com/repos/piashmsuf-eng/winlator/releases?per_page=10";

    public ChangelogDialog(Context context) {
        super(context, R.layout.changelog_dialog);
        setTitle(context.getString(R.string.changelog));
        setIcon(R.drawable.icon_settings);

        LinearLayout content = findViewById(R.id.LLContent);
        content.getLayoutParams().width = AppUtils.getPreferredDialogWidth(context);

        TextView body = findViewById(R.id.TVBody);
        body.setText(context.getString(R.string.loading));
        body.setMovementMethod(LinkMovementMethod.getInstance());

        Executors.newSingleThreadExecutor().execute(() -> {
            String result = fetch();
            new Handler(Looper.getMainLooper()).post(() -> body.setText(result));
        });
    }

    private static String fetch() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RELEASES_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            int code = conn.getResponseCode();
            if (code != 200) return "GitHub API error: " + code;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append('\n');
            }
            JSONArray arr = new JSONArray(sb.toString());
            StringBuilder out = new StringBuilder();
            int n = Math.min(arr.length(), 10);
            for (int i = 0; i < n; i++) {
                JSONObject o = arr.getJSONObject(i);
                String tag = o.optString("tag_name", "?");
                String name = o.optString("name", tag);
                String published = o.optString("published_at", "").replace('T', ' ').replace('Z', ' ').trim();
                if (published.length() >= 16) published = published.substring(0, 16);
                String body = o.optString("body", "").trim();
                out.append("● ").append(name).append("  (").append(tag).append(")\n");
                if (!published.isEmpty()) out.append("   ").append(published).append('\n');
                if (!body.isEmpty()) out.append('\n').append(body).append('\n');
                out.append("\n----------\n\n");
            }
            return out.toString().trim();
        } catch (Throwable e) {
            return "Could not fetch changelog: " + e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
