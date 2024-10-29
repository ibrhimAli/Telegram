package org.telegram.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.R;

public class BotStartHintDrawable extends Drawable implements Drawable.Callback {

    private final Drawable icon;
    private int height;
    private int width;

    private ValueAnimator valueAnimator;
    private float animatedTranslationY;

    public BotStartHintDrawable(@NonNull Context context) {
        icon = ContextCompat.getDrawable(context, R.drawable.msg_go_down);
        assert icon != null;
        width = icon.getIntrinsicWidth();
        height = icon.getIntrinsicHeight();
        icon.setCallback(this);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        invalidateSelf(); // Redraw with new size
    }

    public void start() {
        stop();

        valueAnimator = ValueAnimator.ofFloat(-.08f, .08f);
        valueAnimator.addUpdateListener(a -> {
            animatedTranslationY = (float) a.getAnimatedValue();
            invalidateSelf();
        });
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setDuration(400L);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
    }

    public void stop() {
        if (valueAnimator != null) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.cancel();
            valueAnimator = null;
        }
        animatedTranslationY = 0f;
        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return width != 0 ? width : icon.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return height != 0 ? height : icon.getIntrinsicHeight();
    }

    @Override
    public int getMinimumHeight() {
        return height != 0 ? height : icon.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return width != 0 ? width : icon.getIntrinsicWidth();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        icon.setBounds(left, top, right, bottom);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.translate(0, getIntrinsicHeight() * animatedTranslationY);
        canvas.translate(0, -getIntrinsicHeight() / 6f);
        icon.draw(canvas);
        canvas.translate(0, 2 * getIntrinsicHeight() / 6f);
        icon.draw(canvas);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        icon.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        icon.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return icon.getOpacity();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }
}
