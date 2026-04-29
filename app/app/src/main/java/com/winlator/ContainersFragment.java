package com.winlator;

import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.contentdialog.ContentDialog;
import com.winlator.contentdialog.StorageInfoDialog;
import com.winlator.core.AppUtils;
import com.winlator.core.ContainerExporter;
import com.winlator.core.PreloaderDialog;
import com.winlator.xenvironment.RootFS;

import java.util.ArrayList;
import java.util.List;

public class ContainersFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ContainerManager manager;
    private PreloaderDialog preloaderDialog;
    private String searchQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preloaderDialog = new PreloaderDialog(getActivity());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manager = new ContainerManager(getContext());
        loadContainersList();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.containers);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = (FrameLayout)inflater.inflate(R.layout.containers_fragment, container, false);
        recyclerView = frameLayout.findViewById(R.id.RecyclerView);
        Context context = recyclerView.getContext();
        emptyTextView = frameLayout.findViewById(R.id.TVEmptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_item_divider));
        recyclerView.addItemDecoration(itemDecoration);
        return frameLayout;
    }

    private void loadContainersList() {
        ArrayList<Container> all = manager.getContainers();
        ArrayList<Container> visible = new ArrayList<>();
        String q = searchQuery == null ? "" : searchQuery.trim().toLowerCase(java.util.Locale.US);
        for (Container c : all) {
            if (q.isEmpty() || c.getName().toLowerCase(java.util.Locale.US).contains(q)) visible.add(c);
        }
        recyclerView.setAdapter(new ContainersAdapter(visible));
        emptyTextView.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.containers_menu, menu);
        MenuItem search = menu.findItem(R.id.menu_item_search);
        if (search != null) {
            androidx.appcompat.widget.SearchView sv = (androidx.appcompat.widget.SearchView) search.getActionView();
            if (sv != null) {
                sv.setQueryHint(getString(R.string.search));
                sv.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override public boolean onQueryTextSubmit(String query) { searchQuery = query; loadContainersList(); return true; }
                    @Override public boolean onQueryTextChange(String newText) { searchQuery = newText; loadContainersList(); return true; }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_item_add) {
            if (!RootFS.find(getContext()).isValid()) return false;
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FLFragmentContainer, new ContainerDetailFragment())
                .commit();
            return true;
        }
        else if (menuItem.getItemId() == R.id.menu_item_import_container) {
            if (!RootFS.find(getContext()).isValid()) return false;
            importContainer();
            return true;
        }
        else return super.onOptionsItemSelected(menuItem);
    }

    private void importContainer() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.setOpenFileCallback((data) -> {
            preloaderDialog.show(R.string.importing_container);
            ContainerExporter.importAsync(getContext(), manager, data, success -> {
                preloaderDialog.close();
                AppUtils.showToast(getContext(), success ? R.string.settings_imported : R.string.import_failed);
                loadContainersList();
            });
        });
        activity.startActivityForResult(intent, MainActivity.OPEN_FILE_REQUEST_CODE);
    }

    private void exportContainer(Container container) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;
        String fileName = container.getName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".tzst";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zstd");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        activity.setCreateFileCallback((data) -> {
            preloaderDialog.show(R.string.exporting_container);
            ContainerExporter.exportAsync(getContext(), container, data, success -> {
                preloaderDialog.close();
                AppUtils.showToast(getContext(), success ? R.string.settings_exported : R.string.export_failed);
            });
        });
        activity.startActivityForResult(intent, MainActivity.CREATE_FILE_REQUEST_CODE);
    }

    private class ContainersAdapter extends RecyclerView.Adapter<ContainersAdapter.ViewHolder> {
        private final List<Container> data;

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView runButton;
            private final ImageView menuButton;
            private final ImageView imageView;
            private final TextView title;

            private ViewHolder(View view) {
                super(view);
                this.imageView = view.findViewById(R.id.ImageView);
                this.title = view.findViewById(R.id.TVTitle);
                this.runButton = view.findViewById(R.id.BTRun);
                this.menuButton = view.findViewById(R.id.BTMenu);
            }
        }

        public ContainersAdapter(List<Container> data) {
            this.data = data;
        }

        @Override
        public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Container item = data.get(position);
            holder.imageView.setImageResource(R.drawable.icon_container);
            holder.title.setText(item.getName());
            holder.runButton.setOnClickListener((view) -> runContainer(item));
            holder.menuButton.setOnClickListener((view) -> showListItemMenu(view, item));
        }

        @Override
        public final int getItemCount() {
            return data.size();
        }

        private void showListItemMenu(View anchorView, Container container) {
            MainActivity activity = (MainActivity)getActivity();
            PopupMenu listItemMenu = new PopupMenu(activity, anchorView);
            listItemMenu.inflate(R.menu.container_popup_menu);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listItemMenu.setForceShowIcon(true);

            listItemMenu.setOnMenuItemClickListener((menuItem) -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_file_manager:
                        activity.showFragment(new ContainerFileManagerFragment(container.id));
                        break;
                    case R.id.menu_item_edit:
                        activity.showFragment(new ContainerDetailFragment(container.id));
                        break;
                    case R.id.menu_item_duplicate:
                        ContentDialog.confirm(getContext(), R.string.do_you_want_to_duplicate_this_container, () -> {
                            preloaderDialog.show(R.string.duplicating_container);
                            manager.duplicateContainerAsync(container, () -> {
                                preloaderDialog.close();
                                loadContainersList();
                            });
                        });
                        break;
                    case R.id.menu_item_export:
                        exportContainer(container);
                        break;
                    case R.id.menu_item_remove:
                        ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_container, () -> {
                            preloaderDialog.show(R.string.removing_container);
                            manager.removeContainerAsync(container, () -> {
                                preloaderDialog.close();
                                loadContainersList();
                            });
                        });
                        break;
                    case R.id.menu_item_info:
                        (new StorageInfoDialog(activity, container)).show();
                        break;
                }
                return true;
            });
            listItemMenu.show();
        }

        private void runContainer(Container container) {
            Activity activity = getActivity();
            Intent intent = new Intent(activity, XServerDisplayActivity.class);
            intent.putExtra("container_id", container.id);
            activity.startActivity(intent);
        }
    }
}
