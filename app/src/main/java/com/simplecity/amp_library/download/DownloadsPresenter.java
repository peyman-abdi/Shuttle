package com.simplecity.amp_library.download;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ShuttleApplication;
import com.simplecity.amp_library.ui.modelviews.DownloadView;
import com.simplecity.amp_library.ui.presenters.Presenter;
import com.simplecity.amp_library.utils.SettingsManager;
import com.simplecityapps.recycler_adapter.model.ViewModel;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Status;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.FlowableOperator;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by peyman on 4/11/18.
 */
public class DownloadsPresenter extends Presenter<DownloadsView> {
    public static final String TAG = "DownloadsPresenter";

    @Override
    public void bindView(@NonNull DownloadsView view) {
        super.bindView(view);

        view.setStatusVisibility(Status.COMPLETED, SettingsManager.getInstance().getDownloadsStatusVisibility(Status.COMPLETED));
        view.setStatusVisibility(Status.FAILED, SettingsManager.getInstance().getDownloadsStatusVisibility(Status.FAILED));
        view.setStatusVisibility(Status.PAUSED, SettingsManager.getInstance().getDownloadsStatusVisibility(Status.PAUSED));
        view.setStatusVisibility(Status.QUEUED, SettingsManager.getInstance().getDownloadsStatusVisibility(Status.QUEUED));
        view.setStatusVisibility(Status.DOWNLOADING, SettingsManager.getInstance().getDownloadsStatusVisibility(Status.DOWNLOADING));
    }

    public void loadData() {
        DownloadsView downloadsView = getView();
        if (downloadsView != null) {
            downloadsView.setLoading(true);

            boolean showDownloading = SettingsManager.getInstance().getDownloadsStatusVisibility(Status.DOWNLOADING);
            boolean showCompleted = SettingsManager.getInstance().getDownloadsStatusVisibility(Status.COMPLETED);
            boolean showStopped = SettingsManager.getInstance().getDownloadsStatusVisibility(Status.CANCELLED);
            boolean showQueued = SettingsManager.getInstance().getDownloadsStatusVisibility(Status.QUEUED);
            boolean showPaused = SettingsManager.getInstance().getDownloadsStatusVisibility(Status.PAUSED);

            if (showDownloading && showCompleted && showStopped && showQueued && showPaused) {
                addDisposable(DownloadHelper.getInstance()
                        .getDownloads()
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .lift(new DownloadsFilterOperator(this))
                        .subscribe(items -> {
                            if (items.size() > 0) {
                                downloadsView.setItems(items);
                            } else {
                                downloadsView.setEmpty(true);
                            }
                        }, err -> {
                            Log.e(TAG, err.getMessage());
                        })
                );
            } else if (!showDownloading && !showCompleted && !showStopped && !showQueued && !showPaused) {
                downloadsView.setEmpty(true);
            } else {
                ArrayList<Status> filters = new ArrayList<>();
                if (showDownloading) filters.add(Status.DOWNLOADING);
                if (showCompleted) filters.add(Status.COMPLETED);
                if (showQueued) filters.add(Status.QUEUED);
                if (showPaused) filters.add(Status.PAUSED);
                if (showStopped) {
                    filters.add(Status.REMOVED);
                    filters.add(Status.CANCELLED);
                    filters.add(Status.FAILED);
                    filters.add(Status.DELETED);
                    filters.add(Status.NONE);
                }
                addDisposable(DownloadHelper.getInstance()
                        .getDownloadsWithStatus(filters)
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .lift(new DownloadsFilterOperator(this))
                        .subscribe(items -> {
                            if (items.size() > 0) {
                                downloadsView.setItems(items);
                            } else {
                                downloadsView.setEmpty(true);
                            }
                        }, err -> {
                            Log.e(TAG, err.getMessage());
                        })
                );
            }

        }
    }

    public void setStatusVisible(Status status, boolean visible) {
        SettingsManager.getInstance().setDownloadsStatusVisibility(status, visible);
        loadData();
    }

    public static class DownloadsFilterOperator implements FlowableOperator<List<ViewModel>, List<Download>> {

        DownloadViewClickListener clickListener;
        DownloadsPresenter presenter;
        public DownloadsFilterOperator(DownloadsPresenter presenter) {
            this.presenter = presenter;
        }

        @Override
        public Subscriber<? super List<Download>> apply(Subscriber<? super List<ViewModel>> observer) throws Exception {
            return new FlowableSubscriber<List<Download>>() {
                @Override
                public void onSubscribe(Subscription s) {
                    observer.onSubscribe(s);
                }

                @Override
                public void onNext(List<Download> downloads) {
                    clickListener = new DownloadViewClickListener(downloads, presenter);

                    List<ViewModel> viewModels = Stream.of(downloads).map(item -> {
                        DownloadView view = new DownloadView(item);
                        view.setListener(clickListener);
                        return view;
                    }).collect(Collectors.toList());
                    observer.onNext(viewModels);
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            };
        }
    }

    public static class DownloadViewClickListener implements DownloadView.OnClickListener {

        List<Download> downloads;
        DownloadsPresenter  presenter;

        public DownloadViewClickListener(List<Download> downloads, DownloadsPresenter presenter) {
            this.downloads = downloads;
            this.presenter = presenter;
        }

        @Override
        public void onPauseClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download) {
            DownloadHelper.getInstance().pause(download);
            presenter.loadData();
        }

        @Override
        public void onResumeClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download) {
            DownloadHelper.getInstance().resume(download);
            presenter.loadData();
        }

        @Override
        public void onErrorClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download) {
            getRestartOrDeleteDialog(viewHolder.itemView.getContext(), download, presenter).show();
        }

        @Override
        public void onRestartClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download) {
            DownloadHelper.getInstance().restart(download);
            presenter.loadData();
        }

        @Override
        public void onCancelClicked(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download) {
            DownloadHelper.getInstance().cancel(download);
            presenter.loadData();
        }

        @Override
        public void onCompleteClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download) {
            getRestartOrDeleteDialog(viewHolder.itemView.getContext(), download, presenter).show();
        }

        private static MaterialDialog getRestartOrDeleteDialog(Context context, Download download, @Nullable DownloadsPresenter presenter) {
            Resources r = ShuttleApplication.getInstance().getBaseContext().getResources();
            ArrayList<String> items = new ArrayList<>();
            items.add(r.getString(R.string.download_dialog_remove));
            items.add(r.getString(R.string.download_dialog_restart_download));

            return new MaterialDialog.Builder(context)
                    .title(r.getString(R.string.download_dialog_already_downloaded))
                    .items(items)
                    .itemsCallback((dialog, view, which, string) -> {
                        if (which == 0) {
                            DownloadHelper.getInstance().delete(download);
                        } else if (which == 1) {
                            DownloadHelper.getInstance().restart(download);
                        }
                        if (presenter != null) {
                            presenter.loadData();
                        }
                    }).build();
        }
    }
}
