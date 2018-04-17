package com.simplecity.amp_library.download;

import android.support.annotation.NonNull;

import com.simplecityapps.recycler_adapter.model.ViewModel;
import com.tonyodev.fetch2.Status;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by peyman on 4/11/18.
 */
public interface DownloadsView {
    void setLoading(boolean loading);

    void setEmpty(boolean empty);

    Disposable setItems(@NonNull List<ViewModel> items);

    void setStatusVisibility(Status status, boolean visible);
}
