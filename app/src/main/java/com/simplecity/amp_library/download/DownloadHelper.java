package com.simplecity.amp_library.download;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.annotations.SerializedName;
import com.simplecity.amp_library.BuildConfig;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ShuttleApplication;
import com.simplecity.amp_library.http.HttpClient;
import com.simplecity.amp_library.http.ahangify.AhangifyFile;
import com.simplecity.amp_library.http.ahangify.AhangifyWaitForSeconds;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.utils.CustomMediaScanner;
import com.simplecity.amp_library.utils.FileHelper;
import com.simplecity.amp_library.utils.SettingsManager;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.RequestInfo;
import com.tonyodev.fetch2.Status;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableOperator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.simplecity.amp_library.http.HttpClient.AHANGIFY_WAIT_FOR_FILE_DEFAULT;

/**
 * Created by peyman on 4/11/18.
 */
public class DownloadHelper implements FetchListener {

    public static final String TAG = "DownloadHelper";
    public static final int AUTO_RETRY_GROUP = 101;

    private static DownloadHelper sInstance;
    public static synchronized DownloadHelper getInstance() {
        if (sInstance == null) {
            sInstance = new DownloadHelper();
            HttpClient.getInstance().downloaderService.addListener(sInstance);
        }
        return sInstance;
    }

    private Map<Integer, WeakReference<FetchListener>> downloadListeners = new HashMap<>();
    private ArrayList<String> waitingRequests = new ArrayList<>();

    private DownloadHelper() {
    }

    public void startDownload(Song song) {
        this.startDownload(song, null);
    }

    public void startDownload(Song song, AhangifyFile track) {
        String filename = getSongPath(song);
        Request request = new Request(HttpClient.getAhangifyFileURL(song, track), filename);
        request.setUid(getSongUID(song));
        request.setNetworkType(getNetworkType());
        request.setTag(getSongTag(song));
        request.setGroupId(0);
        startDownload(request);
    }

    public void startDownload(Request request) {
        HttpClient.getInstance().downloaderService.enqueue(request, download -> {
            Log.d(TAG, "Download enqueue: " + download.getFile());
        }, error -> {
            Log.e(TAG, error.toString());
        });
    }

    public void autoStartWaitingDownloads() {
        HttpClient.getInstance().downloaderService.getDownloadsInGroup(DownloadHelper.AUTO_RETRY_GROUP, downloads -> {
            for (Download dl: downloads) {
                restart(dl);
            }
        });
    }

    public Flowable<List<Download>> getDownloads() {
        return Flowable.create(emitter -> {
            HttpClient.getInstance().downloaderService.getDownloads(downloads -> {
                ArrayList<Download> dls = new ArrayList<>(downloads);
                emitter.onNext(dls);
            });
            emitter.setCancellable(() -> {
                emitter.onNext(null);
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<List<Download>> getDownloadsWithStatus(Status status) {
        return Flowable.create(emitter -> {
            HttpClient.getInstance().downloaderService.getDownloadsWithStatus(status, downloads -> {
                ArrayList<Download> dls = new ArrayList<>(downloads);
                emitter.onNext(dls);
            });

            emitter.setCancellable(() -> {
                emitter.onNext(null);
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<List<Download>> getDownloadsWithStatus(List<Status> statuses) {
        return Flowable.create(emitter -> {
            HttpClient.getInstance().downloaderService.getDownloadsWithStatuses(statuses, downloads -> {
                ArrayList<Download> dls = new ArrayList<>(downloads);
                emitter.onNext(dls);
            });
            emitter.setCancellable(() -> {
                emitter.onNext(null);
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<List<Download>> getDownloadsInGroup(int group) {
        return Flowable.create(emitter -> {
            HttpClient.getInstance().downloaderService.getDownloadsInGroup(group, downloads -> {
                ArrayList<Download> dls = new ArrayList<>(downloads);
                emitter.onNext(dls);
            });

            emitter.setCancellable(() -> {
                emitter.onNext(null);
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<List<Download>> getDownloadsWithUIDs(List<String> uids) {
        return Flowable.create(emitter -> {
            HttpClient.getInstance().downloaderService.getDownloadsWithUIDs(uids, downloads -> {
                ArrayList<Download> dls = new ArrayList<>(downloads);
                emitter.onNext(dls);
            });

            emitter.setCancellable(() -> {
                emitter.onNext(null);
            });
        }, BackpressureStrategy.BUFFER);
    }


    public void pause(Download download) {
        HttpClient.getInstance().downloaderService.pause(download.getId());
    }
    public void resume(Download download) {
        HttpClient.getInstance().downloaderService.resume(download.getId());
    }
    public void stop(Download download) {
        HttpClient.getInstance().downloaderService.cancel(download.getId());
    }
    public void delete(Download download) {
        if (download.getStatus() == Status.COMPLETED) {

        }
        HttpClient.getInstance().downloaderService.delete(download.getId());
    }
    public void restart(Download download) {
        delete(download);
        Request request = download.getRequest();

        request.setTag(download.getTag());
        request.setGroupId(0);
        request.setNetworkType(download.getNetworkType());
        request.setPriority(download.getPriority());
        request.setUid(download.getUid());

        startDownload(request);
    }
    public void cancel(Download download) {
        HttpClient.getInstance().downloaderService.cancel(download.getId());
    }
    public void remove(Download download) {
        HttpClient.getInstance().downloaderService.remove(download.getId());
    }

    public void addListener(Download download, WeakReference<FetchListener> listener) {
        downloadListeners.put(download.getId(), listener);
    }
    public void removeListener(Download download) {
        downloadListeners.remove(download.getId());
    }

    public MaterialDialog getSelectQualityDialogAndStart(Context context, Song song) {
        ArrayList<String> items = new ArrayList<>();
        if (song.onlineTrack != null && song.onlineTrack.files != null) {
            for (AhangifyFile file: song.onlineTrack.files) {
                items.add(file.quality + "  (" + FileHelper.getHumanReadableSize(file.size) + ")");
            }
        }
        return new MaterialDialog.Builder(context)
                .title(R.string.download_quality_dialog_title)
                .items(items)
                .itemsCallback((dialog, view, which, text) -> {
                    if (song.onlineTrack != null && song.onlineTrack.files != null) {
                        if (song.onlineTrack.files.length > which) {
                            startDownload(song, song.onlineTrack.files[which]);
                        }
                    }
                }).build();
    }

    public String getStoragePath() {
        int downloadLocation = SettingsManager.getInstance().getDownloadLocation();
        if (downloadLocation == 0) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        } else if (downloadLocation == 1) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        } else if (downloadLocation == 2) {

        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
    }
    public String getSongPath(Song song) {
        return getStoragePath() + "/" + song.name.toLowerCase().replaceAll("\\s", "_") + ".mp3";
    }

    public NetworkType getNetworkType() {
        if (SettingsManager.getInstance().getDownloadWifiOnly()) {
            return NetworkType.WIFI_ONLY;
        }
        return NetworkType.ALL;
    }

    public String getSongUID(Song song) {
        return String.valueOf(song.id);
    }

    public String getSongTag(Song song) {
        return HttpClient.getInstance().gson.toJson(new DownloadTag(song));
    }

    public DownloadTag getDownloadTag(Download download) {
        return HttpClient.getInstance().gson.fromJson(download.getTag(), DownloadTag.class);
    }

    public void addWaitingRequest(AhangifyWaitForSeconds waiting, okhttp3.Request request) {
        waitingRequests.add(request.url().url().toString());
        long wait = waiting.wait > 0 ? waiting.wait: AHANGIFY_WAIT_FOR_FILE_DEFAULT;
        Completable
                .timer(wait, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.single())
                .subscribe(() -> {
                    Log.d(TAG, "Retry download now");
                    getDownloads()
                            .subscribeOn(Schedulers.single())
                            .observeOn(AndroidSchedulers.mainThread())
                            .lift(new DownloadsFilterUrlOperator(request.url().url().toString()))
                            .subscribe(download -> {
                                if (download != null) {
                                    Log.d(TAG, "Restarting download");
                                    restart(download);
                                } else {
                                    Log.d(TAG, "Download not found!");
                                }
                            });
                }, error -> {
                    //@todo: report error
                    Log.e(TAG, "Error retrying download");
                });
    }
    private boolean isInWaitingList(Request request) {
        return waitingRequests.contains(request.getUrl());
    }

    @Override
    public void onQueued(Download download) {
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_queued_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onQueued(download);
            }
        }
    }

    @Override
    public void onCompleted(Download download) {
        Context context = ShuttleApplication.getInstance().getApplicationContext();;

        if (BuildConfig.DEBUG) {
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_completed_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onCompleted(download);
            }
        }
        CustomMediaScanner.scanFile(download.getFile(), message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onError(Download download) {
        Request request = download.getRequest();
        if (isInWaitingList(request)) {
            waitingRequests.remove(request.getUrl());
            RequestInfo info = new RequestInfo();
            info.setGroupId(AUTO_RETRY_GROUP);
            info.setNetworkType(request.getNetworkType());
            info.setPriority(request.getPriority());
            info.setTag(request.getTag());
            for(Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                info.addHeader(key, value);
            }
            HttpClient.getInstance().downloaderService.updateRequest(download.getId(), info, dl -> {
                if (downloadListeners.containsKey(download.getId())) {
                    WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
                    if (listener.get() != null) {
                        listener.get().onError(dl);
                    }
                }
            }, null);
        } else {
            if (downloadListeners.containsKey(download.getId())) {
                WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
                if (listener.get() != null) {
                    listener.get().onError(download);
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }

            Toast.makeText(context, context.getResources().getString(R.string.download_failed_message, title, download.getError().getThrowable().getLocalizedMessage().toString()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgress(Download download, long l, long l1) {
        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onProgress(download, l , l1);
            }
        }
    }

    @Override
    public void onPaused(Download download) {
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_paused_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onPaused(download);
            }
        }
    }

    @Override
    public void onResumed(Download download) {
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_resumed_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onResumed(download);
            }
        }
    }

    @Override
    public void onCancelled(Download download) {
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_canceled_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onCancelled(download);
            }
        }
    }

    @Override
    public void onRemoved(Download download) {
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_removed_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onRemoved(download);
            }
        }
    }

    @Override
    public void onDeleted(Download download) {
        if (BuildConfig.DEBUG) {
            Context context = ShuttleApplication.getInstance().getApplicationContext();;
            String title = context.getResources().getString(R.string.download_common_title, String.valueOf(download.getId()));
            DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);
            if (tagData != null && tagData.title != null) {
                title = tagData.title;
            }
            Toast.makeText(context, context.getResources().getString(R.string.download_deleted_message, title), Toast.LENGTH_SHORT).show();
        }

        if (downloadListeners.containsKey(download.getId())) {
            WeakReference<FetchListener> listener = downloadListeners.get(download.getId());
            if (listener.get() != null) {
                listener.get().onDeleted(download);
            }
        }
    }

    public static class DownloadTag {
        @SerializedName("id")
        public long id;
        @SerializedName("title")
        public String title;
        @SerializedName("artist")
        public String artist;
        @SerializedName("album")
        public String album;

        public DownloadTag(Song song) {
            this.id = song.id;
            this.title = song.name;
            this.artist = song.artistName;
            this.album = song.albumName;
        }
    }

    public static class DownloadsFilterUrlOperator implements FlowableOperator<Download, List<Download>> {

        private String urlFilter;
        public DownloadsFilterUrlOperator(String urlFilter) {
            this.urlFilter = urlFilter;
        }

        @Override
        public Subscriber<? super List<Download>> apply(Subscriber<? super Download> observer) throws Exception {
            return new Subscriber<List<Download>>() {
                @Override
                public void onSubscribe(Subscription s) {
                    observer.onSubscribe(s);
                }

                @Override
                public void onNext(List<Download> downloads) {
                    ArrayList<Download> targets = new ArrayList<>();
                    for (Download dl: downloads) {
                        if (dl.getRequest().getUrl().equals(urlFilter)) {
                            observer.onNext(dl);
                            return;
                        }
                    }
                    observer.onNext(null);
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
}
