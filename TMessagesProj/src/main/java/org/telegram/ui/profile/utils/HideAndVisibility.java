package org.telegram.ui.profile.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

import java.util.ArrayList;

public class HideAndVisibility {
    private static final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    public static void hideFloatingButton(ComponentsFactory componentsFactory, ProfileActivity profileActivity, boolean hide) {
        ViewComponentsHolder viewsHolder = componentsFactory.getViewComponentsHolder();
        AttributesComponentsHolder attributes = componentsFactory.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        TLRPC.User bot = profileActivity.getMessagesController().getUser(attributes.getUserId());
        if (bot != null && bot.bot && bot.bot_can_edit && bot.bot_has_main_app) {
            StoriesController.BotPreviewsList list = (StoriesController.BotPreviewsList) profileActivity.getMessagesController().getStoriesController().getStoriesList(attributes.getUserId(), StoriesController.StoriesList.TYPE_BOTS);
            ArrayList<StoriesController.UploadingStory> uploadingStories = profileActivity.getMessagesController().getStoriesController().getUploadingStories(attributes.getUserId());
            if (list != null && list.getCount() + (uploadingStories == null ? 0 : uploadingStories.size()) >= profileActivity.getMessagesController().botPreviewMediasMax) {
                hide = true;
            }
        }
        if (rowsHolder.isFloatingHidden() == hide || viewsHolder.getFloatingButtonContainer() == null || rowsHolder.isWaitCanSendStoryRequest()) {
            return;
        }
        rowsHolder.setFloatingHidden(hide);
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(rowsHolder.getFloatingButtonHideProgress(), rowsHolder.isFloatingHidden() ? 1f : 0f);
        valueAnimator.addUpdateListener(animation -> {
            rowsHolder.setFloatingButtonHideProgress((float) animation.getAnimatedValue());
            updateFloatingButtonOffset(componentsFactory);
        });
        animatorSet.playTogether(valueAnimator);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(floatingInterpolator);
        viewsHolder.getFloatingButtonContainer().setClickable(!hide);
        animatorSet.start();
    }

    public static void updateFloatingButtonOffset(ComponentsFactory componentsFactory) {
        ViewComponentsHolder viewsHolder = componentsFactory.getViewComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        if (viewsHolder.getFloatingButtonContainer() != null) {
            viewsHolder.getFloatingButtonContainer().setTranslationY(AndroidUtilities.dp(100) * rowsHolder.getFloatingButtonHideProgress());
        }
    }

    private static AnimatorSet avatarAnimation;
    public static void showAvatarProgress(boolean show, boolean animated, RadialProgressView avatarProgressView) {
        if (avatarProgressView == null) {
            return;
        }
        if (avatarAnimation != null) {
            avatarAnimation.cancel();
            avatarAnimation = null;
        }
        if (animated) {
            avatarAnimation = new AnimatorSet();
            if (show) {
                avatarProgressView.setVisibility(View.VISIBLE);
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 1.0f));
            } else {
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 0.0f));
            }

            avatarAnimation.setDuration(180);
            avatarAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (avatarAnimation == null || avatarProgressView == null) {
                        return;
                    }
                    if (!show) {
                        avatarProgressView.setVisibility(View.INVISIBLE);
                    }
                    avatarAnimation = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    avatarAnimation = null;
                }
            });
            avatarAnimation.start();
        } else {
            if (show) {
                avatarProgressView.setAlpha(1.0f);
                avatarProgressView.setVisibility(View.VISIBLE);
            } else {
                avatarProgressView.setAlpha(0.0f);
                avatarProgressView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
