package org.telegram.ui.profile.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.utils.ProfileData;

import java.util.Arrays;

public class OverlaysView extends View implements ProfileGalleryView.Callback {

    private int statusBarHeight =  AndroidUtilities.statusBarHeight;

    private final Rect topOverlayRect = new Rect();
    private final Rect bottomOverlayRect = new Rect();
    private final RectF rect = new RectF();

    private final GradientDrawable topOverlayGradient;
    private final GradientDrawable bottomOverlayGradient;
    public final ValueAnimator animator;
    private final float[] animatorValues = new float[]{0f, 1f};
    private final Paint backgroundPaint;
    private final Paint barPaint;
    private final Paint selectedBarPaint;

    private final GradientDrawable[] pressedOverlayGradient = new GradientDrawable[2];
    private final boolean[] pressedOverlayVisible = new boolean[2];
    private final float[] pressedOverlayAlpha = new float[2];

    private boolean isOverlaysVisible;
    private float currentAnimationValue;
    private float alpha = 0f;
    private float[] alphas = null;
    private long lastTime;
    private float previousSelectedProgress;
    private int previousSelectedPotision = -1;
    private float currentProgress;
    private int selectedPosition;

    private float currentLoadingAnimationProgress;
    private int currentLoadingAnimationDirection = 1;
    private ProfileActivity profileActivity;
    private final ProfileData profileData;
    private RowsAndStatusComponentsHolder rowsHolder;
    private AttributesComponentsHolder attributesHolder;

    public OverlaysView(ComponentsFactory componentsFactory) {
        super(componentsFactory.getProfileActivity().getContext());
        setVisibility(GONE);


        profileActivity = componentsFactory.getProfileActivity();
        profileData = new ProfileData(componentsFactory);
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(0x55ffffff);
        selectedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedBarPaint.setColor(0xffffffff);
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        attributesHolder = componentsFactory.getAttributesComponentsHolder();

        topOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x42000000, 0});
        topOverlayGradient.setShape(GradientDrawable.RECTANGLE);

        bottomOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x42000000, 0});
        bottomOverlayGradient.setShape(GradientDrawable.RECTANGLE);

        statusBarHeight = profileActivity.getActionBar().getOccupyStatusBar() && !profileActivity.isInBubbleMode() ? AndroidUtilities.statusBarHeight : 0;

        for (int i = 0; i < 2; i++) {
            final GradientDrawable.Orientation orientation = i == 0 ? GradientDrawable.Orientation.LEFT_RIGHT : GradientDrawable.Orientation.RIGHT_LEFT;
            pressedOverlayGradient[i] = new GradientDrawable(orientation, new int[]{0x32000000, 0});
            pressedOverlayGradient[i].setShape(GradientDrawable.RECTANGLE);
        }

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(66);
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(250);
        animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
        animator.addUpdateListener(anim -> {
            float value = AndroidUtilities.lerp(animatorValues, currentAnimationValue = anim.getAnimatedFraction());
            setAlphaValue(value, true);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isOverlaysVisible) {
                    setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(VISIBLE);
            }
        });

    }

    public void saveCurrentPageProgress() {
        previousSelectedProgress = currentProgress;
        previousSelectedPotision = selectedPosition;
        currentLoadingAnimationProgress = 0.0f;
        currentLoadingAnimationDirection = 1;
    }

    public void setAlphaValue(float value, boolean self) {
        if (Build.VERSION.SDK_INT > 18) {
            int alpha = (int) (255 * value);
            topOverlayGradient.setAlpha(alpha);
            bottomOverlayGradient.setAlpha(alpha);
            backgroundPaint.setAlpha((int) (66 * value));
            barPaint.setAlpha((int) (0x55 * value));
            selectedBarPaint.setAlpha(alpha);
            this.alpha = value;
        } else {
            setAlpha(value);
        }
        if (!self) {
            currentAnimationValue = value;
        }
        invalidate();
    }

    public boolean isOverlaysVisible() {
        return isOverlaysVisible;
    }

    public void setOverlaysVisible() {
        isOverlaysVisible = true;
        setVisibility(VISIBLE);
    }

    public void setOverlaysVisible(boolean overlaysVisible, float durationFactor) {
        if (overlaysVisible != isOverlaysVisible) {
            isOverlaysVisible = overlaysVisible;
            animator.cancel();
            final float value = AndroidUtilities.lerp(animatorValues, currentAnimationValue);
            if (overlaysVisible) {
                animator.setDuration((long) ((1f - value) * 250f / durationFactor));
            } else {
                animator.setDuration((long) (value * 250f / durationFactor));
            }
            animatorValues[0] = value;
            animatorValues[1] = overlaysVisible ? 1f : 0f;
            animator.start();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int actionBarHeight = statusBarHeight + ActionBar.getCurrentActionBarHeight();
        final float k = 0.5f;
        topOverlayRect.set(0, 0, w, (int) (actionBarHeight * k));
        bottomOverlayRect.set(0, (int) (h - AndroidUtilities.dp(72f) * k), w, h);
        topOverlayGradient.setBounds(0, topOverlayRect.bottom, w, actionBarHeight + AndroidUtilities.dp(16f));
        bottomOverlayGradient.setBounds(0, h - AndroidUtilities.dp(72f) - AndroidUtilities.dp(24f), w, bottomOverlayRect.top);
        pressedOverlayGradient[0].setBounds(0, 0, w / 5, h);
        pressedOverlayGradient[1].setBounds(w - (w / 5), 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 2; i++) {
            if (pressedOverlayAlpha[i] > 0f) {
                pressedOverlayGradient[i].setAlpha((int) (pressedOverlayAlpha[i] * 255));
                pressedOverlayGradient[i].draw(canvas);
            }
        }

        topOverlayGradient.draw(canvas);
        bottomOverlayGradient.draw(canvas);
        canvas.drawRect(topOverlayRect, backgroundPaint);
        canvas.drawRect(bottomOverlayRect, backgroundPaint);

        int count = attributesHolder.getAvatarsViewPager().getRealCount();
        selectedPosition = attributesHolder.getAvatarsViewPager().getRealPosition();

        if (alphas == null || alphas.length != count) {
            alphas = new float[count];
            Arrays.fill(alphas, 0.0f);
        }

        boolean invalidate = false;

        long newTime = SystemClock.elapsedRealtime();
        long dt = (newTime - lastTime);
        if (dt < 0 || dt > 20) {
            dt = 17;
        }
        lastTime = newTime;

        if (count > 1 && count <= 20) {
            if (rowsHolder.getOverlayCountVisible() == 0) {
                alpha = 0.0f;
                rowsHolder.setOverlayCountVisible(3);
            } else if (rowsHolder.getOverlayCountVisible() == 1) {
                alpha = 0.0f;
                rowsHolder.setOverlayCountVisible(2);
            }
            if (rowsHolder.getOverlayCountVisible() == 2) {
                barPaint.setAlpha((int) (0x55 * alpha));
                selectedBarPaint.setAlpha((int) (0xff * alpha));
            }
            int width = (getMeasuredWidth() - AndroidUtilities.dp(5 * 2) - AndroidUtilities.dp(2 * (count - 1))) / count;
            int y = AndroidUtilities.dp(4) + (Build.VERSION.SDK_INT >= 21 && !profileActivity.isInBubbleMode() ? AndroidUtilities.statusBarHeight : 0);
            for (int a = 0; a < count; a++) {
                int x = AndroidUtilities.dp(5 + a * 2) + width * a;
                float progress;
                int baseAlpha = 0x55;
                if (a == previousSelectedPotision && Math.abs(previousSelectedProgress - 1.0f) > 0.0001f) {
                    progress = previousSelectedProgress;
                    canvas.save();
                    canvas.clipRect(x + width * progress, y, x + width, y + AndroidUtilities.dp(2));
                    rect.set(x, y, x + width, y + AndroidUtilities.dp(2));
                    barPaint.setAlpha((int) (0x55 * alpha));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), barPaint);
                    baseAlpha = 0x50;
                    canvas.restore();
                    invalidate = true;
                } else if (a == selectedPosition) {
                    if (attributesHolder.getAvatarsViewPager().isCurrentItemVideo()) {
                        progress = currentProgress = attributesHolder.getAvatarsViewPager().getCurrentItemProgress();
                        if (progress <= 0 && attributesHolder.getAvatarsViewPager().isLoadingCurrentVideo() || currentLoadingAnimationProgress > 0.0f) {
                            currentLoadingAnimationProgress += currentLoadingAnimationDirection * dt / 500.0f;
                            if (currentLoadingAnimationProgress > 1.0f) {
                                currentLoadingAnimationProgress = 1.0f;
                                currentLoadingAnimationDirection *= -1;
                            } else if (currentLoadingAnimationProgress <= 0) {
                                currentLoadingAnimationProgress = 0.0f;
                                currentLoadingAnimationDirection *= -1;
                            }
                        }
                        rect.set(x, y, x + width, y + AndroidUtilities.dp(2));
                        barPaint.setAlpha((int) ((0x55 + 0x30 * currentLoadingAnimationProgress) * alpha));
                        canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), barPaint);
                        invalidate = true;
                        baseAlpha = 0x50;
                    } else {
                        progress = currentProgress = 1.0f;
                    }
                } else {
                    progress = 1.0f;
                }
                rect.set(x, y, x + width * progress, y + AndroidUtilities.dp(2));

                if (a != selectedPosition) {
                    if (rowsHolder.getOverlayCountVisible() == 3) {
                        barPaint.setAlpha((int) (AndroidUtilities.lerp(baseAlpha, 0xff, CubicBezierInterpolator.EASE_BOTH.getInterpolation(alphas[a])) * alpha));
                    }
                } else {
                    alphas[a] = 0.75f;
                }

                canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), a == selectedPosition ? selectedBarPaint : barPaint);
            }

            if (rowsHolder.getOverlayCountVisible() == 2) {
                if (alpha < 1.0f) {
                    alpha += dt / 180.0f;
                    if (alpha > 1.0f) {
                        alpha = 1.0f;
                    }
                    invalidate = true;
                } else {
                    rowsHolder.setOverlayCountVisible(3);
                }
            } else if (rowsHolder.getOverlayCountVisible() == 3) {
                for (int i = 0; i < alphas.length; i++) {
                    if (i != selectedPosition && alphas[i] > 0.0f) {
                        alphas[i] -= dt / 500.0f;
                        if (alphas[i] <= 0.0f) {
                            alphas[i] = 0.0f;
                            if (i == previousSelectedPotision) {
                                previousSelectedPotision = -1;
                            }
                        }
                        invalidate = true;
                    } else if (i == previousSelectedPotision) {
                        previousSelectedPotision = -1;
                    }
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            if (pressedOverlayVisible[i]) {
                if (pressedOverlayAlpha[i] < 1f) {
                    pressedOverlayAlpha[i] += dt / 180.0f;
                    if (pressedOverlayAlpha[i] > 1f) {
                        pressedOverlayAlpha[i] = 1f;
                    }
                    invalidate = true;
                }
            } else {
                if (pressedOverlayAlpha[i] > 0f) {
                    pressedOverlayAlpha[i] -= dt / 180.0f;
                    if (pressedOverlayAlpha[i] < 0f) {
                        pressedOverlayAlpha[i] = 0f;
                    }
                    invalidate = true;
                }
            }
        }

        if (invalidate) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public void onDown(boolean left) {
        pressedOverlayVisible[left ? 0 : 1] = true;
        postInvalidateOnAnimation();
    }

    @Override
    public void onRelease() {
        Arrays.fill(pressedOverlayVisible, false);
        postInvalidateOnAnimation();
    }

    @Override
    public void onPhotosLoaded() {
        profileData.updateProfileData(false);
    }

    @Override
    public void onVideoSet() {
        invalidate();
    }
}
