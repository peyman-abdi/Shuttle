package com.simplecity.amp_library.download;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ui.drawer.DrawerLockManager;
import com.simplecity.amp_library.ui.fragments.BaseFragment;
import com.simplecity.amp_library.ui.modelviews.EmptyView;
import com.simplecity.amp_library.ui.modelviews.LoadingView;
import com.simplecity.amp_library.utils.ResourceUtils;
import com.simplecityapps.recycler_adapter.adapter.ViewModelAdapter;
import com.simplecityapps.recycler_adapter.model.ViewModel;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tonyodev.fetch2.Status;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by peyman on 4/10/18.
 */
public class DownloadsFragment extends BaseFragment implements
        DrawerLockManager.DrawerLock,
        DownloadsView
{
    public static final String TAG = "DownloadsFragment";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recyclerView)
    FastScrollRecyclerView recyclerView;

    private ViewModelAdapter adapter;
    private CompositeDisposable disposables = new CompositeDisposable();
    private DownloadsPresenter downloadsPresenter;
    private Unbinder unbinder;
    private EmptyView emptyView;
    private View rootView;
    private LoadingView loadingView;

    public static DownloadsFragment newInstance() {
        DownloadsFragment fragment = new DownloadsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadsPresenter = new DownloadsPresenter();

        emptyView = new EmptyView(R.string.now_downloads_found);
        emptyView.setHeight(ResourceUtils.toPixels(96));

        loadingView = new LoadingView();

        adapter = new ViewModelAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_downloads, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        toolbar.setTitle(R.string.downloads_history);
        toolbar.setNavigationOnClickListener(v -> getNavigationController().popViewController());
        toolbar.inflateMenu(R.menu.menu_downloads);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.show_completed:
                    item.setChecked(!item.isChecked());
                    downloadsPresenter.setStatusVisible(Status.COMPLETED, item.isChecked());
                    break;
                case R.id.show_downloading:
                    item.setChecked(!item.isChecked());
                    downloadsPresenter.setStatusVisible(Status.DOWNLOADING, item.isChecked());
                    break;
                case R.id.show_paused:
                    item.setChecked(!item.isChecked());
                    downloadsPresenter.setStatusVisible(Status.PAUSED, item.isChecked());
                    break;
                case R.id.show_queued:
                    item.setChecked(!item.isChecked());
                    downloadsPresenter.setStatusVisible(Status.QUEUED, item.isChecked());
                    break;
                case R.id.show_stopped:
                    item.setChecked(!item.isChecked());
                    downloadsPresenter.setStatusVisible(Status.CANCELLED, item.isChecked());
                    break;
            }
            return false;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();

        downloadsPresenter.bindView(this);
        downloadsPresenter.loadData();
    }

    @Override
    public void onPause() {
        disposables.clear();
        downloadsPresenter.unbindView(this);

        super.onPause();
    }

    @Override
    protected String screenName() {
        return TAG;
    }


    @Override
    public void setLoading(boolean loading) {
        adapter.setItems(Collections.singletonList(loadingView));
    }

    @Override
    public void setEmpty(boolean empty) {
        adapter.setItems(Collections.singletonList(emptyView));
    }

    @Override
    public Disposable setItems(@NonNull List<ViewModel> items) {
        Disposable disposable = adapter.setItems(items);
        recyclerView.scrollToPosition(0);
        return disposable;
    }

    @Override
    public void setStatusVisibility(Status status, boolean visible) {
        switch (status) {
            case DOWNLOADING:
                toolbar.getMenu().findItem(R.id.show_downloading).setChecked(visible);
                break;
            case COMPLETED:
                toolbar.getMenu().findItem(R.id.show_completed).setChecked(visible);
                break;
            case QUEUED:
                toolbar.getMenu().findItem(R.id.show_queued).setChecked(visible);
                break;
            case PAUSED:
                toolbar.getMenu().findItem(R.id.show_paused).setChecked(visible);
                break;
            default:
                toolbar.getMenu().findItem(R.id.show_stopped).setChecked(visible);
                break;
        }
    }
}
