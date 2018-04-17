package com.simplecity.amp_library.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.LightDarkColorState;
import com.simplecity.amp_library.R;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class OverflowButton extends NonScrollImageButton {

    private Disposable aestheticDisposable;

    public Drawable drawable;
    public Drawable offlineDrawable;
    public Drawable onlineDrawable;
    public Drawable onlineDownloadedDrawable;

    private boolean dark = false;

    public void setDrawableMode(boolean online, boolean downloaded) {
        if (online) {
            if (downloaded) {
                drawable = onlineDownloadedDrawable;
            } else {
                drawable = onlineDrawable;
            }
        } else {
            drawable = offlineDrawable;
        }
    }

    public OverflowButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OverflowButton, 0, 0);
        if (typedArray.hasValue(R.styleable.OverflowButton_isDark)) {
            dark = typedArray.getBoolean(R.styleable.OverflowButton_isDark, false);
        }

        offlineDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_overflow_20dp)).mutate();
        onlineDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_download_24dp)).mutate();
        onlineDownloadedDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_close_24dp)).mutate();

        drawable = offlineDrawable;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode()) {
            setImageDrawable(drawable);
        } else {
            aestheticDisposable = Observable.combineLatest(
                    Aesthetic.get(getContext()).textColorSecondary(),
                    Observable.just(Color.WHITE),
                    Observable.just(dark),
                    LightDarkColorState.creator()
            ).subscribe(lightDarkColorState -> {
                DrawableCompat.setTint(drawable, lightDarkColorState.color());
                setImageDrawable(drawable);
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        aestheticDisposable.dispose();

        super.onDetachedFromWindow();
    }
}