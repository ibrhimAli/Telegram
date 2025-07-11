package org.telegram.ui.profile.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.button.ButtonViewEnum;

public class QR {

    public static boolean isQrNeedVisible() {
        return true;
    }

    public static void updateQrItemVisibility(ComponentsFactory componentsFactory, boolean animated) {
        ViewComponentsHolder viewsHolder = componentsFactory.getViewComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (viewsHolder.getQrItem() == null) {
            return;
        }
        boolean setQrVisible = isQrNeedVisible() && Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) > .5f && (viewsHolder.getButtonsGroup() == null || viewsHolder.getButtonsGroup().getExtraButtons().contains(ButtonViewEnum.QR_CODE));
        if (animated) {
            if (setQrVisible != rowsHolder.isQrItemVisible()) {
                rowsHolder.setQrItemVisible(setQrVisible);
                if (attributesHolder.getQrItemAnimation() != null) {
                    attributesHolder.getQrItemAnimation().cancel();
                    attributesHolder.setQrItemAnimation(null);
                }
                viewsHolder.getQrItem().setClickable(rowsHolder.isQrItemVisible());
                attributesHolder.setQrItemAnimation(new AnimatorSet());
                if (!(viewsHolder.getQrItem().getVisibility() == View.GONE && !setQrVisible)) {
                    viewsHolder.getQrItem().setVisibility(View.VISIBLE);
                }
                if (setQrVisible) {
                    attributesHolder.getQrItemAnimation().setInterpolator(new DecelerateInterpolator());
                    attributesHolder.getQrItemAnimation().playTogether(
                            ObjectAnimator.ofFloat(viewsHolder.getQrItem(), View.ALPHA, 1.0f),
                            ObjectAnimator.ofFloat(viewsHolder.getQrItem(), View.SCALE_Y, 1f),
                            ObjectAnimator.ofFloat(viewsHolder.getAvatarsViewPagerIndicatorView(), View.TRANSLATION_X, -AndroidUtilities.dp(48))
                    );
                } else {
                    attributesHolder.getQrItemAnimation().setInterpolator(new AccelerateInterpolator());
                    attributesHolder.getQrItemAnimation().playTogether(
                            ObjectAnimator.ofFloat(viewsHolder.getQrItem(), View.ALPHA, 0.0f),
                            ObjectAnimator.ofFloat(viewsHolder.getQrItem(), View.SCALE_Y, 0f),
                            ObjectAnimator.ofFloat(viewsHolder.getAvatarsViewPagerIndicatorView(), View.TRANSLATION_X, 0)
                    );
                }
                attributesHolder.getQrItemAnimation().setDuration(150);
                attributesHolder.getQrItemAnimation().addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        attributesHolder.setQrItemAnimation(null);
                    }
                });
                attributesHolder.getQrItemAnimation().start();
            }
        } else {
            if (attributesHolder.getQrItemAnimation() != null) {
                attributesHolder.getQrItemAnimation().cancel();
                attributesHolder.setQrItemAnimation(null);
            }
            rowsHolder.setQrItemVisible(setQrVisible);
            viewsHolder.getQrItem().setClickable(rowsHolder.isQrItemVisible());
            viewsHolder.getQrItem().setAlpha(setQrVisible ? 1.0f : 0.0f);
            viewsHolder.getQrItem().setVisibility(setQrVisible ? View.VISIBLE : View.GONE);
        }
    }

}
