package org.telegram.ui.profile.view;

import static org.telegram.ui.profile.utils.Views.updateStoriesViewBounds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

public class PagerIndicatorView extends View {

    private final RectF indicatorRect = new RectF();

    private final TextPaint textPaint;
    private final Paint backgroundPaint;

    private final ValueAnimator animator;
    private final float[] animatorValues = new float[]{0f, 1f};

    private PagerAdapter adapter;

    private boolean isIndicatorVisible;

    private final ProfileActivity profileActivity;
    private final ViewComponentsHolder viewsHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    private AttributesComponentsHolder attributesHolder;
    public PagerIndicatorView(ComponentsFactory componentsFactory) {
        super(componentsFactory.getProfileActivity().getContext());
        setVisibility(GONE);
        profileActivity = componentsFactory.getProfileActivity();
        this.viewsHolder = componentsFactory.getViewComponentsHolder();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        attributesHolder = componentsFactory.getAttributesComponentsHolder();
        adapter = attributesHolder.getAvatarsViewPager().getAdapter();
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(AndroidUtilities.dpf2(15f));
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0x26000000);
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
        animator.addUpdateListener(a -> {
            final float value = AndroidUtilities.lerp(animatorValues, a.getAnimatedFraction());
            if (viewsHolder.getSearchItem() != null && !rowsHolder.isPulledDown()) {
                viewsHolder.getSearchItem().setScaleX(1f - value);
                viewsHolder.getSearchItem().setScaleY(1f - value);
                viewsHolder.getSearchItem().setAlpha(1f - value);
            }
            if (rowsHolder.isEditItemVisible()) {
                viewsHolder.getEditItem().setScaleX(1f - value);
                viewsHolder.getEditItem().setScaleY(1f - value);
                viewsHolder.getEditItem().setAlpha(1f - value);
            }
            if (rowsHolder.isCallItemVisible()) {
                viewsHolder.getCallItem().setScaleX(1f - value);
                viewsHolder.getCallItem().setScaleY(1f - value);
                viewsHolder.getCallItem().setAlpha(1f - value);
            }
            if (rowsHolder.isVideoCallItemVisible()) {
                viewsHolder.getVideoCallItem().setScaleX(1f - value);
                viewsHolder.getVideoCallItem().setScaleY(1f - value);
                viewsHolder.getVideoCallItem().setAlpha(1f - value);
            }
            setScaleX(value);
            setScaleY(value);
            setAlpha(value);
        });
        boolean expanded = rowsHolder.isExpandPhoto();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isIndicatorVisible) {
                    if (viewsHolder.getSearchItem() != null) {
                        viewsHolder.getSearchItem().setClickable(false);
                    }
                    if (rowsHolder.isEditItemVisible()) {
                        viewsHolder.getEditItem().setVisibility(GONE);
                    }
                    if (rowsHolder.isCallItemVisible()) {
                        viewsHolder.getCallItem().setVisibility(GONE);
                    }
                    if (rowsHolder.isVideoCallItemVisible()) {
                        viewsHolder.getVideoCallItem().setVisibility(GONE);
                    }
                } else {
                    setVisibility(GONE);
                }
                updateStoriesViewBounds(viewsHolder, profileActivity, viewsHolder,false);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (viewsHolder.getSearchItem() != null && !expanded) {
                    viewsHolder.getSearchItem().setClickable(true);
                }
                if (rowsHolder.isEditItemVisible()) {
                    viewsHolder.getEditItem().setVisibility(VISIBLE);
                }
                if (rowsHolder.isCallItemVisible()) {
                    viewsHolder.getCallItem().setVisibility(VISIBLE);
                }
                if (rowsHolder.isVideoCallItemVisible()) {
                    viewsHolder.getVideoCallItem().setVisibility(VISIBLE);
                }
                setVisibility(VISIBLE);
                updateStoriesViewBounds(viewsHolder, profileActivity, viewsHolder, false);
            }
        });
        attributesHolder.getAvatarsViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int prevPage;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int realPosition = attributesHolder.getAvatarsViewPager().getRealPosition(position);
                invalidateIndicatorRect(prevPage != realPosition);
                prevPage = realPosition;
                updateAvatarItems();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                int count = attributesHolder.getAvatarsViewPager().getRealCount();
                if (rowsHolder.getOverlayCountVisible() == 0 && count > 1 && count <= 20 && viewsHolder.getOverlaysView().isOverlaysVisible()) {
                    rowsHolder.setOverlayCountVisible(1);
                }
                invalidateIndicatorRect(false);
                refreshVisibility(1f);
                updateAvatarItems();
            }
        });
    }

    private void updateAvatarItemsInternal() {
        if (viewsHolder.getOtherItem() == null || attributesHolder.getAvatarsViewPager() == null) {
            return;
        }
        if (rowsHolder.isPulledDown()) {
            int position = attributesHolder.getAvatarsViewPager().getRealPosition();
            if (position == 0) {
                viewsHolder.getOtherItem().hideSubItem(ProfileParams.set_as_main);
                viewsHolder.getOtherItem().showSubItem(ProfileParams.add_photo);
            } else {
                viewsHolder.getOtherItem().showSubItem(ProfileParams.set_as_main);
                viewsHolder.getOtherItem().hideSubItem(ProfileParams.add_photo);
            }
        }
    }

    private void updateAvatarItems() {
        if (viewsHolder.getImageUpdater() == null) {
            return;
        }
        if (viewsHolder.getOtherItem().isSubMenuShowing()) {
            AndroidUtilities.runOnUIThread(this::updateAvatarItemsInternal, 500);
        } else {
            updateAvatarItemsInternal();
        }
    }

    public boolean isIndicatorVisible() {
        return isIndicatorVisible;
    }

    public boolean isIndicatorFullyVisible() {
        return isIndicatorVisible && !animator.isRunning();
    }

    public void setIndicatorVisible(boolean indicatorVisible, float durationFactor) {
        if (indicatorVisible != isIndicatorVisible) {
            isIndicatorVisible = indicatorVisible;
            animator.cancel();
            final float value = AndroidUtilities.lerp(animatorValues, animator.getAnimatedFraction());
            if (durationFactor <= 0f) {
                animator.setDuration(0);
            } else if (indicatorVisible) {
                animator.setDuration((long) ((1f - value) * 250f / durationFactor));
            } else {
                animator.setDuration((long) (value * 250f / durationFactor));
            }
            animatorValues[0] = value;
            animatorValues[1] = indicatorVisible ? 1f : 0f;
            animator.start();
        }
    }

    public void refreshVisibility(float durationFactor) {
        setIndicatorVisible(rowsHolder.isPulledDown() && attributesHolder.getAvatarsViewPager().getRealCount() > 20, durationFactor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        invalidateIndicatorRect(false);
    }

    private void invalidateIndicatorRect(boolean pageChanged) {
        if (pageChanged) {
            viewsHolder.getOverlaysView().saveCurrentPageProgress();
        }
        viewsHolder.getOverlaysView().invalidate();
        final float textWidth = textPaint.measureText(getCurrentTitle());
        indicatorRect.right = getMeasuredWidth() - AndroidUtilities.dp(54f) - (viewsHolder.getQrItem() != null ? AndroidUtilities.dp(48) : 0);
        indicatorRect.left = indicatorRect.right - (textWidth + AndroidUtilities.dpf2(16f));
        indicatorRect.top = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(15f);
        indicatorRect.bottom = indicatorRect.top + AndroidUtilities.dp(26);
        setPivotX(indicatorRect.centerX());
        setPivotY(indicatorRect.centerY());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float radius = AndroidUtilities.dpf2(12);
        canvas.drawRoundRect(indicatorRect, radius, radius, backgroundPaint);
        canvas.drawText(getCurrentTitle(), indicatorRect.centerX(), indicatorRect.top + AndroidUtilities.dpf2(18.5f), textPaint);
    }

    private String getCurrentTitle() {
        return adapter.getPageTitle(attributesHolder.getAvatarsViewPager().getCurrentItem()).toString();
    }

    public ActionBarMenuItem getSecondaryMenuItem() {
        if (rowsHolder.isCallItemVisible()) {
            return viewsHolder.getCallItem();
        } else if (rowsHolder.isEditItemVisible()) {
            return viewsHolder.getEditItem();
        } else if (viewsHolder.getSearchItem() != null) {
            return viewsHolder.getSearchItem();
        } else {
            return null;
        }
    }
}
