package com.winlator.core;

import android.content.Context;
import android.net.Uri;

import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.xenvironment.RootFS;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Executors;

/**
 * Helpers for exporting and importing a single container as a .tzst (zstd-compressed tar) archive.
 *
 * The archive structure mirrors the container directory tree exactly:
 *   /.container
 *   /.wine/...
 *   /.local/...
 *   ...
 *
 * On import we extract on top of a fresh container directory inside ContainerManager.getHomeDir().
 */
public final class ContainerExporter {

    private ContainerExporter() {}

    public interface Callback {
        void onComplete(boolean success);
    }

    public static void exportAsync(Context ctx, Container container, Uri destUri, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = export(ctx, container, destUri);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onComplete(ok));
        });
    }

    public static boolean export(Context ctx, Container container, Uri destUri) {
        if (container == null || container.getRootDir() == null) return false;
        try (OutputStream raw = ctx.getContentResolver().openOutputStream(destUri);
             BufferedOutputStream buf = new BufferedOutputStream(raw, StreamUtils.BUFFER_SIZE);
             ZstdCompressorOutputStream zstd = new ZstdCompressorOutputStream(buf, 3);
             TarArchiveOutputStream tar = new TarArchiveOutputStream(zstd)) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            File root = container.getRootDir();
            addDir(tar, root, "");
            tar.finish();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static void addDir(TarArchiveOutputStream tar, File dir, String basePath) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String entryName = basePath + f.getName();
            if (FileUtils.isSymlink(f)) {
                org.apache.commons.compress.archivers.tar.TarArchiveEntry entry =
                    new org.apache.commons.compress.archivers.tar.TarArchiveEntry(
                        entryName, org.apache.commons.compress.archivers.tar.TarConstants.LF_SYMLINK);
                entry.setLinkName(FileUtils.readSymlink(f));
                tar.putArchiveEntry(entry);
                tar.closeArchiveEntry();
            } else if (f.isDirectory()) {
                String dirEntry = entryName + "/";
                tar.putArchiveEntry(tar.createArchiveEntry(f, dirEntry));
                tar.closeArchiveEntry();
                addDir(tar, f, dirEntry);
            } else {
                tar.putArchiveEntry(tar.createArchiveEntry(f, entryName));
                try (java.io.BufferedInputStream in =
                         new java.io.BufferedInputStream(new java.io.FileInputStream(f), StreamUtils.BUFFER_SIZE)) {
                    StreamUtils.copy(in, tar);
                }
                tar.closeArchiveEntry();
            }
        }
    }

    public static void importAsync(Context ctx, ContainerManager manager, Uri sourceUri, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = importTzst(ctx, manager, sourceUri);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onComplete(ok));
        });
    }

    private static boolean importTzst(Context ctx, ContainerManager manager, Uri sourceUri) {
        try {
            int id = manager.getNextContainerId();
            File homeDir = manager.getHomeDir();
            if (homeDir == null) return false;
            File targetDir = new File(homeDir, RootFS.USER + "-" + id);
            if (!targetDir.mkdirs()) return false;

            boolean ok = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, ctx, sourceUri, targetDir);
            if (!ok) {
                FileUtils.delete(targetDir);
                return false;
            }

            File configFile = new File(targetDir, ".container");
            if (configFile.isFile()) {
                String json = FileUtils.readString(configFile);
                try {
                    org.json.JSONObject data = new org.json.JSONObject(json);
                    data.put("id", id);
                    if (data.has("name")) {
                        data.put("name", data.getString("name") + " (imported)");
                    }
                    FileUtils.writeString(configFile, data.toString());
                } catch (org.json.JSONException ignored) {}
            }
            manager.reload();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
