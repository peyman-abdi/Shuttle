package com.simplecity.amp_library.ui.modelviews;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.download.DownloadHelper;
import com.simplecityapps.recycler_adapter.model.BaseViewModel;
import com.simplecityapps.recycler_adapter.recyclerview.BaseViewHolder;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.FetchListener;

import java.lang.ref.WeakReference;

import be.appfoundry.progressbutton.ProgressButton;
import io.reactivex.disposables.CompositeDisposable;

import static com.afollestad.aesthetic.Rx.distinctToMainThread;
import static com.simplecity.amp_library.R.layout.list_item_download;
import static com.simplecity.amp_library.ui.adapters.ViewType.DOWNLOAD_INFO;

/**
 * Created by peyman on 4/11/18.
 */
public class DownloadView extends BaseViewModel<DownloadView.ViewHolder> {

    public static final String TAG = "DownloadView";

    public interface OnClickListener {
        void onPauseClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download);
        void onResumeClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download);
        void onCompleteClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download);
        void onErrorClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download);
        void onRestartClick(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download);
        void onCancelClicked(DownloadView buttonView, DownloadView.ViewHolder viewHolder, Download download);
    }

    public Download download;

    public DownloadView(Download d) {
        this.download = d;
    }

    @Nullable
    private DownloadView.OnClickListener listener;

    public void setListener(@Nullable DownloadView.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getViewType() {
        return DOWNLOAD_INFO;
    }

    @Override
    public DownloadView.ViewHolder createViewHolder(ViewGroup parent) {
        return new DownloadView.ViewHolder(createView(parent));
    }

    @Override
    public int getLayoutResId() {
        return list_item_download;
    }

    @Override
    public void bindView(DownloadView.ViewHolder holder) {
        super.bindView(holder);

        DownloadHelper.DownloadTag tagData = DownloadHelper.getInstance().getDownloadTag(download);

        if (tagData != null) {
            if (tagData.title != null) {
                holder.lineOne.setText(tagData.title);
            } else {
                holder.lineOne.setText("");
            }
            if (tagData.artist != null) {
                holder.lineTwo.setText(tagData.artist);
            } else {
                holder.lineTwo.setText("");
            }
        } else {
            holder.lineOne.setText("");
            holder.lineTwo.setText("");
            holder.lineTree.setText("");
        }

        holder.progressButton.setAnimationDelay(1);

        Aesthetic.get(holder.progressButton.getContext())
                .colorPrimary()
                .take(1)
                .subscribe(primaryColor -> {
                    holder.progressButton.setColor(primaryColor);
                    float[] hsv = new float[3];
                    Color.colorToHSV(primaryColor, hsv);
                    float luminance = hsv[2];
                    hsv[2] = luminance * 0.7f;
                    holder.progressButton.setStrokeColor(Color.HSVToColor(hsv));
                    hsv[2] = 1.0f - 0.5f * (1.0f - luminance);
                    holder.progressButton.setProgressColor(Color.HSVToColor(hsv));
                });

        holder.disposables.add(Aesthetic.get(holder.progressButton.getContext())
                .colorPrimary()
                .compose(distinctToMainThread())
                .subscribe(primaryColor -> {
                    holder.progressButton.setColor(primaryColor);
                    float[] hsv = new float[3];
                    Color.colorToHSV(primaryColor, hsv);
                    float luminance = hsv[2];
                    hsv[2] = luminance * 0.7f;
                    holder.progressButton.setStrokeColor(Color.HSVToColor(hsv));
                    hsv[2] = 1.0f - 0.5f * (1.0f - luminance);
                    holder.progressButton.setProgressColor(Color.HSVToColor(hsv));
                }));

        updateForDownload(download, holder.lineOne, holder.lineTwo, holder.lineTree, holder.progressButton);
        holder.setTargetDownload(download);
    }

    private static void updateForDownload(Download download, TextView lineOne, TextView lineTwo, TextView lineTree, ProgressButton progressButton) {
        switch (download.getStatus()) {
            case NONE:
            case CANCELLED:
            case DELETED:
            case REMOVED:
                lineTree.setText(R.string.download_status_stopped);
                progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_av_play_dark));
                progressButton.stopAnimating();
                break;
            case FAILED:
                if (download.getGroup() == DownloadHelper.AUTO_RETRY_GROUP) {
                    lineTree.setText(R.string.download_status_queued);
                    progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_av_stop_dark));
                    progressButton.setIndeterminate(true);
                    progressButton.startAnimating();
                } else {
                    lineTree.setText(R.string.download_status_error);
                    progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_warning_24dp));
                    progressButton.stopAnimating();
                }
                break;
            case COMPLETED:
                lineTree.setText(R.string.download_status_completed);
                progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_close_24dp));
                progressButton.stopAnimating();
                break;
            case QUEUED:
                lineTree.setText(R.string.download_status_queued);
                progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_av_stop_dark));
                progressButton.setIndeterminate(true);
                progressButton.startAnimating();
                break;
            case PAUSED:
                lineTree.setText(R.string.download_status_paused);
                progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_av_play_dark));
                progressButton.stopAnimating();
                break;
            case DOWNLOADING:
                lineTree.setText(R.string.download_status_downloading);
                progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_av_pause_dark));
                progressButton.setIndeterminate(false);
                progressButton.setProgress(0);
                progressButton.startAnimating();
                break;
        }
    }
    private static void updateProgress(Download download, TextView lineTree, ProgressButton progressButton, long estimatedTime, long averageSpeed) {
        if (averageSpeed > 0) {
            int seconds = (int)(estimatedTime % 60);
            int minutes = (int)(estimatedTime / 60);
            String seconds_string = seconds >= 10 ? String.valueOf(seconds):"0"+String.valueOf(seconds);
            String minutes_string = minutes >= 10 ? String.valueOf(minutes):"0"+String.valueOf(minutes);
            float speed = averageSpeed / 1000;
            String speedUnit = "KB/s";
            if (speed > 1000) {
                speed = speed / 1000;
                speedUnit = "MB/s";
                if (speed > 1000) {
                    speed = speed / 1000;
                    speedUnit = "GB/s";
                }
            }
            lineTree.setText(minutes_string+":"+seconds_string+"  "+String.format(" %.2f", speed)+speedUnit);
        } else {
            lineTree.setText("");
        }
        if (progressButton.isIndeterminate()) {
            progressButton.setIcon(progressButton.getContext().getDrawable(R.drawable.ic_av_pause_dark));
            progressButton.setIndeterminate(false);
            progressButton.setProgress(0);
            progressButton.startAnimating();
        }

        float progress = (float)download.getProgress();
        if (progress > 100) {
            progress = 100;
        } else if (progress < 0) {
            progress = 0;
        }
        progressButton.setProgress(progress);
    }

    void onStatusButtonClicked(DownloadView.ViewHolder viewHolder) {
        if (listener == null) {
            return;
        }

        switch (download.getStatus()) {
            case NONE:
            case CANCELLED:
            case DELETED:
            case REMOVED:
                listener.onRestartClick(this, viewHolder, download);
                break;
            case FAILED:
                if (download.getGroup() == DownloadHelper.AUTO_RETRY_GROUP) {
                    listener.onCancelClicked(this, viewHolder, download);
                } else {
                    listener.onErrorClick(this, viewHolder, download);
                }
                break;
            case COMPLETED:
                listener.onCompleteClick(this, viewHolder, download);
                break;
            case QUEUED:
            case DOWNLOADING:
                listener.onPauseClick(this, viewHolder, download);
                break;
            case PAUSED:
                listener.onResumeClick(this, viewHolder, download);
                break;
        }
    }

    public static class ViewHolder extends BaseViewHolder<DownloadView> implements FetchListener {

        ProgressButton progressButton;
        TextView lineOne, lineTwo, lineTree;
        protected CompositeDisposable disposables = new CompositeDisposable();

        public ViewHolder(View itemView) {
            super(itemView);

            disposables.clear();

            progressButton = itemView.findViewById(R.id.progress_button);
            lineOne = itemView.findViewById(R.id.line_one);
            lineTwo = itemView.findViewById(R.id.line_two);
            lineTree = itemView.findViewById(R.id.line_three);

            progressButton.setOnClickListener(v -> viewModel.onStatusButtonClicked(this));
        }

        private Download targetDownload;

        public void setTargetDownload(Download target) {
            this.targetDownload = target;
            if (targetDownload != null) {
                DownloadHelper.getInstance().addListener(targetDownload, new WeakReference<>(this));
            }
        }

        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (targetDownload != null) {
                DownloadHelper.getInstance().addListener(targetDownload, new WeakReference<>(this));
            }
        }

        @Override
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (targetDownload != null) {
                DownloadHelper.getInstance().removeListener(targetDownload);
            }
        }

        @Override
        public String toString() {
            return "DownloadView.ViewHolder";
        }


        @Override
        public void onQueued(Download download) {
            DownloadView.updateForDownload(download, this.lineOne, this.lineTwo, this.lineTree, this.progressButton);
        }

        @Override
        public void onCompleted(Download download) {
            DownloadView.updateForDownload(download, this.lineOne, this.lineTwo, this.lineTree, this.progressButton);
        }

        @Override
        public void onError(Download download) {
            DownloadView.updateForDownload(download, this.lineOne, this.lineTwo, this.lineTree, this.progressButton);
        }

        @Override
        public void onProgress(Download download, long l, long l1) {
            DownloadView.updateProgress(download, this.lineTree, this.progressButton, l, l1);
        }

        @Override
        public void onPaused(Download download) {
            DownloadView.updateForDownload(download, this.lineOne, this.lineTwo, this.lineTree, this.progressButton);
        }

        @Override
        public void onResumed(Download download) {
            DownloadView.updateForDownload(download, this.lineOne, this.lineTwo, this.lineTree, this.progressButton);
        }

        @Override
        public void onCancelled(Download download) {
            DownloadView.updateForDownload(download, this.lineOne, this.lineTwo, this.lineTree, this.progressButton);
        }

        @Override
        public void onRemoved(Download download) {
        }

        @Override
        public void onDeleted(Download download) {
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean areContentsEqual(Object other) {
        return equals(other);
    }
}
