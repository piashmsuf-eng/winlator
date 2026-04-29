package com.winlator;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.winlator.container.Shortcut;
import com.winlator.contentdialog.ContentDialog;
import com.winlator.contentdialog.CreateFolderDialog;
import com.winlator.contentdialog.ShortcutSettingsDialog;
import com.winlator.core.AppUtils;
import com.winlator.core.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShortcutsFragment extends BaseFileManagerFragment<Shortcut> {
    private String searchQuery = "";
    private FloatingActionButton lastPlayedFab;
    private Shortcut lastPlayedShortcut;

    public enum SortMode { NAME, LAST_PLAYED, PLAYTIME, LAUNCH_COUNT }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewStyle = ViewStyle.valueOf(preferences.getString("shortcuts_view_style", "GRID"));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (root != null) {
            lastPlayedFab = root.findViewById(R.id.BTLastPlayed);
            if (lastPlayedFab != null) {
                lastPlayedFab.setOnClickListener((v) -> {
                    if (lastPlayedShortcut != null) launchShortcut(lastPlayedShortcut);
                });
            }
        }
        return root;
    }

    @Override
    public void refreshContent() {
        super.refreshContent();

        Shortcut selectedFolder = !folderStack.isEmpty() ? folderStack.peek() : null;
        ArrayList<Shortcut> shortcuts = manager.loadShortcuts(selectedFolder);
        ArrayList<Shortcut> visible = new ArrayList<>();
        String q = searchQuery == null ? "" : searchQuery.trim().toLowerCase(java.util.Locale.US);
        for (Shortcut s : shortcuts) {
            if (q.isEmpty() || s.name.toLowerCase(java.util.Locale.US).contains(q)) visible.add(s);
        }

        SortMode sortMode = SortMode.NAME;
        try { sortMode = SortMode.valueOf(preferences.getString("shortcuts_sort_mode", "NAME")); }
        catch (IllegalArgumentException ignored) {}
        applySort(visible, sortMode);

        lastPlayedShortcut = findLastPlayed(shortcuts);
        if (lastPlayedFab != null) {
            boolean show = lastPlayedShortcut != null && folderStack.isEmpty();
            lastPlayedFab.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        recyclerView.setAdapter(new ShortcutsAdapter(visible));
        emptyTextView.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private static void applySort(ArrayList<Shortcut> list, SortMode mode) {
        list.sort((a, b) -> {
            int dirCmp = Boolean.compare(b.file.isDirectory(), a.file.isDirectory());
            if (dirCmp != 0) return dirCmp;
            int favCmp = Boolean.compare(isFavorite(b), isFavorite(a));
            if (favCmp != 0) return favCmp;
            switch (mode) {
                case LAST_PLAYED:   return Long.compare(b.getLastPlayed(), a.getLastPlayed());
                case PLAYTIME:      return Long.compare(b.getTotalPlaytimeMs(), a.getTotalPlaytimeMs());
                case LAUNCH_COUNT:  return Long.compare(b.getLaunchCount(), a.getLaunchCount());
                case NAME: default: return a.name.compareToIgnoreCase(b.name);
            }
        });
    }

    private static boolean isFavorite(Shortcut s) {
        if (s == null || s.file == null || s.file.isDirectory()) return false;
        return "1".equals(s.getExtra("favorite", "0"));
    }

    private static Shortcut findLastPlayed(List<Shortcut> shortcuts) {
        Shortcut best = null;
        long bestTs = 0;
        for (Shortcut s : shortcuts) {
            if (s.file.isDirectory()) continue;
            long ts = s.getLastPlayed();
            if (ts > bestTs) { bestTs = ts; best = s; }
        }
        return best;
    }

    public void launchShortcut(Shortcut shortcut) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null || shortcut == null || shortcut.file.isDirectory()) return;
        Intent intent = new Intent(activity, XServerDisplayActivity.class);
        intent.putExtra("container_id", shortcut.container.id);
        intent.putExtra("shortcut_path", shortcut.file.getPath());
        activity.startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shortcuts_menu, menu);
        refreshViewStyleMenuItem(menu.findItem(R.id.menu_item_view_style));
        MenuItem search = menu.findItem(R.id.menu_item_search);
        if (search != null) {
            androidx.appcompat.widget.SearchView sv = (androidx.appcompat.widget.SearchView) search.getActionView();
            if (sv != null) {
                sv.setQueryHint(getString(R.string.search));
                sv.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override public boolean onQueryTextSubmit(String query) { searchQuery = query; refreshContent(); return true; }
                    @Override public boolean onQueryTextChange(String newText) { searchQuery = newText; refreshContent(); return true; }
                });
            }
        }
    }

    private void createFolder() {
        clearClipboard();
        if (manager.getContainers().isEmpty()) return;
        CreateFolderDialog createFolderDialog = new CreateFolderDialog(manager);
        createFolderDialog.setOnCreateFolderListener((container, name) -> {
            File desktopDir = new File(container.getUserDir(), "Desktop");
            File parent = !folderStack.isEmpty() ? folderStack.peek().file : desktopDir;
            File file = new File(parent, name);
            if (file.isDirectory()) {
                AppUtils.showToast(getContext(), R.string.there_already_file_with_that_name);
            }
            else {
                file.mkdir();
                refreshContent();
            }
        });
        createFolderDialog.show();
    }

    @Override
    protected void pasteFiles() {
        if (folderStack.isEmpty()) {
            clearClipboard();
            AppUtils.showToast(getContext(), R.string.you_cannot_paste_files_here);
            return;
        }

        clipboard.targetDir = folderStack.peek().file;
        super.pasteFiles();
    }

    private void instantiateClipboard(Shortcut shortcut, boolean cutMode) {
        clearClipboard();
        File linkFile = shortcut.getLinkFile();
        File[] files = {new File(shortcut.file.getParentFile(), shortcut.file.getName())};
        if (shortcut.file.isFile()) files = ArrayUtils.concat(files, new File[]{new File(linkFile.getParentFile(), linkFile.getName())});

        clipboard = new Clipboard(files, cutMode);
        pasteButton.setVisibility(View.VISIBLE);
    }

    private void setSortMode(SortMode mode) {
        preferences.edit().putString("shortcuts_sort_mode", mode.name()).apply();
        refreshContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.menu_item_sort_name)         { setSortMode(SortMode.NAME); return true; }
        if (itemId == R.id.menu_item_sort_last_played)  { setSortMode(SortMode.LAST_PLAYED); return true; }
        if (itemId == R.id.menu_item_sort_playtime)     { setSortMode(SortMode.PLAYTIME); return true; }
        if (itemId == R.id.menu_item_sort_launch_count) { setSortMode(SortMode.LAUNCH_COUNT); return true; }
        if (itemId == R.id.menu_item_view_style) {
            setViewStyle(viewStyle == ViewStyle.GRID ? ViewStyle.LIST : ViewStyle.GRID);
            preferences.edit().putString("shortcuts_view_style", viewStyle.name()).apply();
            refreshViewStyleMenuItem(menuItem);
            return true;
        }
        else if (itemId == R.id.menu_item_new_folder) {
            createFolder();
            return true;
        }
        else return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected String getHomeTitle() {
        return getString(R.string.shortcuts);
    }

    private static String formatPlaytime(long ms) {
        if (ms <= 0) return "0m";
        long minutes = ms / 60000L;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60L;
        long remMin = minutes % 60L;
        if (hours < 100) return hours + "h " + remMin + "m";
        return hours + "h";
    }

    private class ShortcutsAdapter extends RecyclerView.Adapter<ShortcutsAdapter.ViewHolder> {
        private final List<Shortcut> data;

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView runButton;
            private final ImageView menuButton;
            private final ImageView imageView;
            private final TextView title;
            private final TextView subtitle;

            private ViewHolder(View view) {
                super(view);
                this.imageView = view.findViewById(R.id.ImageView);
                this.title = view.findViewById(R.id.TVTitle);
                this.subtitle = view.findViewById(R.id.TVSubtitle);
                this.runButton = view.findViewById(R.id.BTRun);
                this.menuButton = view.findViewById(R.id.BTMenu);
            }
        }

        public ShortcutsAdapter(List<Shortcut> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int resource = viewStyle == ViewStyle.LIST ? R.layout.file_list_item : R.layout.file_grid_item;
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Shortcut item = data.get(position);

            if (item.cover != null) {
                holder.imageView.setImageBitmap(item.cover);
            }
            else if (item.icon == null) {
                int iconResId = item.file.isDirectory() ? R.drawable.container_folder : R.drawable.container_file_link;
                holder.imageView.setImageResource(iconResId);
            }
            else holder.imageView.setImageBitmap(item.icon);

            boolean fav = isFavorite(item);
            holder.title.setText(fav ? "\u2605 " + item.name : item.name);

            String subtitle = item.container.getName();
            if (!item.file.isDirectory()) {
                long playMs = item.getTotalPlaytimeMs();
                long count = item.getLaunchCount();
                if (count > 0) {
                    subtitle += " · " + formatPlaytime(playMs) + " · " + count + "×";
                }
            }
            holder.subtitle.setText(subtitle);

            if (item.file.isDirectory()) {
                holder.runButton.setImageResource(R.drawable.icon_open);
            }
            else holder.runButton.setImageResource(R.drawable.icon_run);

            holder.imageView.setOnClickListener((v) -> runFromShortcut(item));
            holder.runButton.setOnClickListener((v) -> runFromShortcut(item));
            holder.menuButton.setOnClickListener((v) -> showListItemMenu(v, item));
        }

        @Override
        public final int getItemCount() {
            return data.size();
        }

        private void showListItemMenu(View anchorView, final Shortcut shortcut) {
            final Context context = getContext();
            PopupMenu listItemMenu = new PopupMenu(context, anchorView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listItemMenu.setForceShowIcon(true);

            listItemMenu.inflate(R.menu.file_manager_popup_menu);

            Menu menu = listItemMenu.getMenu();
            menu.findItem(R.id.menu_item_rename).setVisible(false);
            menu.findItem(R.id.menu_item_info).setVisible(false);
            MenuItem favItem = menu.findItem(R.id.menu_item_add_favorite);
            if (favItem != null) {
                if (shortcut.file.isDirectory()) {
                    favItem.setVisible(false);
                } else {
                    favItem.setVisible(true);
                    favItem.setTitle(isFavorite(shortcut) ? R.string.unfavorite : R.string.favorite);
                }
            }

            listItemMenu.setOnMenuItemClickListener((menuItem) -> {
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.menu_item_settings:
                        clearClipboard();
                        (new ShortcutSettingsDialog(ShortcutsFragment.this, shortcut)).show();
                        break;
                    case R.id.menu_item_copy:
                    case R.id.menu_item_cut:
                        instantiateClipboard(shortcut, itemId == R.id.menu_item_cut);
                        break;
                    case R.id.menu_item_add_favorite:
                        shortcut.putExtra("favorite", isFavorite(shortcut) ? "0" : "1");
                        shortcut.saveData();
                        refreshContent();
                        break;
                    case R.id.menu_item_remove:
                        clearClipboard();
                        ContentDialog.confirm(context, R.string.do_you_want_to_remove_this_file, () -> {
                            shortcut.remove();
                            refreshContent();
                        });
                        break;
                }
                return true;
            });
            listItemMenu.show();
        }

        private void runFromShortcut(Shortcut shortcut) {
            AppCompatActivity activity = (AppCompatActivity)getActivity();

            if (shortcut.file.isDirectory()) {
                folderStack.push(shortcut);
                refreshContent();

                ActionBar actionBar = activity.getSupportActionBar();
                actionBar.setHomeAsUpIndicator(R.drawable.icon_action_bar_back);
                actionBar.setTitle(shortcut.name);
            }
            else {
                Intent intent = new Intent(activity, XServerDisplayActivity.class);
                intent.putExtra("container_id", shortcut.container.id);
                intent.putExtra("shortcut_path", shortcut.file.getPath());
                activity.startActivity(intent);
            }
        }
    }
}
