package org.telegram.ui.profile.utils;

import static org.telegram.messenger.Utilities.stackBlurBitmapWithScaleFactor;
import static org.telegram.ui.profile.utils.Hints.updateCollectibleHint;
import static org.telegram.ui.profile.utils.Images.setForegroundImage;
import static org.telegram.ui.profile.utils.QR.updateQrItemVisibility;
import static org.telegram.ui.profile.utils.Views.updateEmojiStatusEffectPosition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;

public class Layouts {

    private ClippedListView listView;
    private final ComponentsFactory components;
    private final ProfileActivity profileActivity;
    private ViewComponentsHolder viewsHolder;
    private AttributesComponentsHolder attributesHolder;
    private ColorsUtils colorsUtils;
    private RowsAndStatusComponentsHolder rowsHolder;
    public Layouts(ComponentsFactory components) {
        this.components = components;
        listView = components.getListView();
        viewsHolder = components.getViewComponentsHolder();
        this.profileActivity = components.getProfileActivity();
        colorsUtils = components.getColorsUtils();
        attributesHolder = components.getAttributesComponentsHolder();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
    }
    public boolean needInsetForStories(ProfileActivity profileActivity) {
        return profileActivity.getMessagesController().getStoriesController().hasStories(profileActivity.getDialogId()) && !rowsHolder.isTopic();
    }

    public int getSmallAvatarRoundRadius(ProfileActivity profileActivity) {
        checkForAttributesHolder();
        if (attributesHolder.getChatId() != 0) {
            TLRPC.Chat chatLocal = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
            if (ChatObject.isForum(chatLocal)) {
                return AndroidUtilities.dp(needInsetForStories(profileActivity) ? 11 : 16);
            }
        }
        return AndroidUtilities.dp(ProfileParams.AVATAR_DIMENSION / 2);
    }

    public void refreshNameAndOnlineXY() {
        float transitionProgress = attributesHolder.getGroupButtonTransitionProgress();
        float v = Math.min(transitionProgress, 0.8f) / 0.8f;
        float actionBarOffset = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);

        View nameView = attributesHolder.getNameTextView()[1];
        View onlineView = attributesHolder.getOnlineTextView()[1];

        float avatarY = rowsHolder.getAvatarY();
        float avatarScale = rowsHolder.getAvatarScale();
        float avatarHeight = ProfileParams.AVATAR_DIMENSION;

        float nameTextWidth = nameView.getScaleX() * nameView.getMeasuredWidth();
        float avatarCenterX = viewsHolder.getAvatarContainer().getX() +
                (viewsHolder.getAvatarContainer().getMeasuredWidth() * avatarScale) / 2f;
        float nameX = avatarCenterX - (nameTextWidth / 2f);
        float newNameY = (float) Math.floor(avatarY)
                + AndroidUtilities.dp(16f)
                + viewsHolder.getAvatarContainer().getMeasuredHeight() * avatarScale;
        float minNameY = AndroidUtilities.dp(12)
                + (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        float nameY = Math.max(newNameY, minNameY);

        rowsHolder.setNameX(nameX);
        rowsHolder.setNameY(nameY);

        if (onlineView != null) {
            float onlineTextWidth = onlineView.getScaleX() * onlineView.getMeasuredWidth();
            float onlineOpenX = avatarCenterX - onlineTextWidth / 2f - onlineView.getLeft();
            float onlineX = avatarCenterX - (onlineTextWidth / 2f);

            float onlineY = rowsHolder.getNameY() + onlineView.getMeasuredHeight() * onlineView.getScaleX();

            rowsHolder.setOnlineX(onlineX);
            rowsHolder.setOnlineY(onlineY);
        }
    }





    public static Bitmap loadBitmapFromView(View v, float scale) {
        Bitmap bmp = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return stackBlurBitmapWithScaleFactor(bmp, scale);
    }

    public void needLayout(boolean animated) {
        checkForViewsHolder();
        checkForAttributesHolder();
        checkForRowsHolder();
        if (listView == null) {
            listView = components.getListView();
        }
        final int newTop = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();

        FrameLayout.LayoutParams layoutParams;
        if (listView != null && !rowsHolder.isOpenAnimationInProgress()) {
            layoutParams = (FrameLayout.LayoutParams) listView.getClippedList().getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.getClippedList().setLayoutParams(layoutParams);
            }
        }

        if (viewsHolder.getAvatarContainer() != null) {
            final float diff = Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));

            listView.getClippedList().setTopGlowOffset((int) rowsHolder.getExtraHeight());
            listView.getClippedList().setOverScrollMode(rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) && rowsHolder.getExtraHeight() < listView.getClippedList().getMeasuredWidth() - newTop ? View.OVER_SCROLL_NEVER : View.OVER_SCROLL_ALWAYS);

            if (animated && viewsHolder.getBlurImageMask() == null && diff == 1 && viewsHolder.getAvatarContainer().getMeasuredWidth() > 0 && viewsHolder.getAvatarContainer().getVisibility() == View.VISIBLE && getSmallAvatarRoundRadius(this.profileActivity) == attributesHolder.getAvatarImage().getRoundRadius()[0]) {
                attributesHolder.setBlurImageBitmap(loadBitmapFromView(viewsHolder.getAvatarContainer(), 1f));
                viewsHolder.getBlurImageMask().setImageBitmap(attributesHolder.getBlurImageBitmap());
            }
            if (attributesHolder.getWriteButton() != null) {
                attributesHolder.getWriteButton().setTranslationY((profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + rowsHolder.getExtraHeight() + rowsHolder.getSearchTransitionOffset() - AndroidUtilities.dp(29.5f));

                boolean writeButtonVisible = diff > 0.2f && !rowsHolder.isSearchMode() && (viewsHolder.getImageUpdater() == null || rowsHolder.getSetAvatarRow() == -1) && (rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT + 50));
                if (writeButtonVisible && attributesHolder.getChatId() != 0) {
                    writeButtonVisible = ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup && attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().linked_chat_id != 0 && rowsHolder.getInfoHeaderRow() != -1 && (rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT + 50));
                }
                if (!rowsHolder.isOpenAnimationInProgress()) {
                    boolean currentVisible = attributesHolder.getWriteButton().getTag() == null;
                    if (writeButtonVisible != currentVisible) {
                        if (writeButtonVisible) {
                            attributesHolder.getWriteButton().setTag(null);
                        } else {
                            attributesHolder.getWriteButton().setTag(0);
                        }
                        if (attributesHolder.getWriteButtonAnimation() != null) {
                            AnimatorSet old = attributesHolder.getWriteButtonAnimation();
                            attributesHolder.setWriteButtonAnimation(null);
                            old.cancel();
                        }
                        if (animated) {
                            attributesHolder.setWriteButtonAnimation(new AnimatorSet());
                            if (writeButtonVisible) {
                                attributesHolder.getWriteButtonAnimation().setInterpolator(new DecelerateInterpolator());
                                attributesHolder.getWriteButtonAnimation().playTogether(
                                        ObjectAnimator.ofFloat(attributesHolder.getWriteButton(), View.SCALE_X, 1.0f),
                                        ObjectAnimator.ofFloat(attributesHolder.getWriteButton(), View.SCALE_Y, 1.0f),
                                        ObjectAnimator.ofFloat(attributesHolder.getWriteButton(), View.ALPHA, 1.0f)
                                );
                            } else {
                                attributesHolder.getWriteButtonAnimation().setInterpolator(new AccelerateInterpolator());
                                attributesHolder.getWriteButtonAnimation().playTogether(
                                        ObjectAnimator.ofFloat(attributesHolder.getWriteButton(), View.SCALE_X, 0.2f),
                                        ObjectAnimator.ofFloat(attributesHolder.getWriteButton(), View.SCALE_Y, 0.2f),
                                        ObjectAnimator.ofFloat(attributesHolder.getWriteButton(), View.ALPHA, 0.0f)
                                );
                            }
                            attributesHolder.getWriteButtonAnimation().setDuration(150);
                            attributesHolder.getWriteButtonAnimation().addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (attributesHolder.getWriteButtonAnimation() != null && attributesHolder.getWriteButtonAnimation().equals(animation)) {
                                        attributesHolder.setWriteButtonAnimation(null);
                                    }
                                }
                            });
                            attributesHolder.getWriteButtonAnimation().start();
                        } else {
                            attributesHolder.getWriteButton().setScaleX(writeButtonVisible ? 1.0f : 0.2f);
                            attributesHolder.getWriteButton().setScaleY(writeButtonVisible ? 1.0f : 0.2f);
                            attributesHolder.getWriteButton().setAlpha(writeButtonVisible ? 1.0f : 0.0f);
                        }
                    }

                    if (viewsHolder.getQrItem() != null) {
                        updateQrItemVisibility(components, animated);
                        if (!animated) {
                            float translation = AndroidUtilities.dp(48) * viewsHolder.getQrItem().getAlpha();
                            viewsHolder.getQrItem().setTranslationX(translation);
                            if (viewsHolder.getAvatarsViewPagerIndicatorView() != null) {
                                viewsHolder.getAvatarsViewPagerIndicatorView().setTranslationX(translation - AndroidUtilities.dp(48));
                            }
                        }
                    }
                }

                if (viewsHolder.getStoryView() != null) {
                    viewsHolder.getStoryView().setExpandCoords(viewsHolder.getAvatarContainer2().getMeasuredWidth() - AndroidUtilities.dp(40), writeButtonVisible, (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + rowsHolder.getExtraHeight() + rowsHolder.getSearchTransitionOffset());
                }
                if (components.getViewComponentsHolder().getGiftsView() != null) {
                    components.getViewComponentsHolder().getGiftsView().setExpandCoords(viewsHolder.getAvatarContainer2().getMeasuredWidth() - AndroidUtilities.dp(40), writeButtonVisible, (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + rowsHolder.getExtraHeight() + rowsHolder.getSearchTransitionOffset());
                }
            }

            rowsHolder.setAvatarX((viewsHolder.getAvatarContainer2().getMeasuredWidth() / 2f) - ((AndroidUtilities.dp(ProfileParams.AVATAR_DIMENSION) * rowsHolder.getAvatarScale()) / 2f));
            //rowsHolder.setAvatarY((profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff + profileActivity.getActionBar().getTranslationY());
            float factor = Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
            final int height = ActionBar.getCurrentActionBarHeight() + (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
            float y = height - (height / ((float) Math.pow(2f, factor)));
            rowsHolder.setAvatarY(y);
            float h = rowsHolder.isOpenAnimationInProgress() ? rowsHolder.getInitialAnimationExtraHeight() : rowsHolder.getExtraHeight();
            if (h > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) || rowsHolder.isPulledDown()) {
                rowsHolder.setExpandProgress(Math.max(0f, Math.min(1f, (h - AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) / (listView.getClippedList().getMeasuredWidth() - newTop - AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)))));
                rowsHolder.setAvatarScale(AndroidUtilities.lerp((ProfileParams.AVATAR_DIMENSION + 18f) / ProfileParams.AVATAR_DIMENSION, (ProfileParams.AVATAR_DIMENSION + ProfileParams.AVATAR_DIMENSION + 18f) / ProfileParams.AVATAR_DIMENSION, Math.min(1f, rowsHolder.getExpandProgress() * 3f)));
                if (viewsHolder.getStoryView() != null) {
                    viewsHolder.getStoryView().invalidate();
                }
                if (components.getViewComponentsHolder().getGiftsView() != null) {
                    components.getViewComponentsHolder().getGiftsView().invalidate();
                }

                final float durationFactor = Math.min(AndroidUtilities.dpf2(2000f), Math.max(AndroidUtilities.dpf2(1100f), Math.abs(rowsHolder.getListViewVelocityY()))) / AndroidUtilities.dpf2(1100f);

                if (rowsHolder.isAllowPullingDown() && (rowsHolder.isOpeningAvatar() || rowsHolder.getExpandProgress() >= 0.33f)) {
                    if (!rowsHolder.isPulledDown()) {
                        if (viewsHolder.getOtherItem() != null) {
                            if (!profileActivity.getMessagesController().isChatNoForwards(attributesHolder.getCurrentChat())) {
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.gallery_menu_save);
                            } else {
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.gallery_menu_save);
                            }
                            if (viewsHolder.getImageUpdater() != null) {
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.add_photo);
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.edit_avatar);
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.delete_avatar);
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.set_as_main);
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.logout);
                            }
                        }
                        if (viewsHolder.getSearchItem() != null) {
                            viewsHolder.getSearchItem().setEnabled(false);
                        }
                        rowsHolder.setPulledDown(true);
                        viewsHolder.getButtonsGroup().invalidate();
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                        viewsHolder.getOverlaysView().setOverlaysVisible(true, durationFactor);
                        viewsHolder.getAvatarsViewPagerIndicatorView().refreshVisibility(durationFactor);
                        attributesHolder.getAvatarsViewPager().setCreateThumbFromParent(true);
                        attributesHolder.getAvatarsViewPager().getAdapter().notifyDataSetChanged();
                        attributesHolder.getExpandAnimator().cancel();
                        float value = AndroidUtilities.lerp(attributesHolder.getExpandAnimatorValues(), rowsHolder.getCurrentExpanAnimatorFracture());
                        attributesHolder.getExpandAnimatorValues()[0] = value;
                        attributesHolder.getExpandAnimatorValues()[1] = 1f;
                        if (viewsHolder.getStoryView() != null && !viewsHolder.getStoryView().isEmpty()) {
                            attributesHolder.getExpandAnimator().setInterpolator(new FastOutSlowInInterpolator());
                            attributesHolder.getExpandAnimator().setDuration((long) ((1f - value) * 1.3f * 250f / durationFactor));
                        } else {
                            attributesHolder.getExpandAnimator().setInterpolator(CubicBezierInterpolator.EASE_BOTH);
                            attributesHolder.getExpandAnimator().setDuration((long) ((1f - value) * 250f / durationFactor));
                        }
                        attributesHolder.getExpandAnimator().addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setForegroundImage(components,false);
                                attributesHolder.getAvatarsViewPager().setAnimatedFileMaybe(attributesHolder.getAvatarImage().getImageReceiver().getAnimation());
                                attributesHolder.getAvatarsViewPager().resetCurrentItem();
                                viewsHolder.getButtonsGroup().setBlurEnabled(true);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                attributesHolder.getExpandAnimator().removeListener(this);
                                viewsHolder.getButtonsGroup().setBlurEnabled(rowsHolder.isPulledDown());
                                viewsHolder.getTopView().setBackgroundColor(Color.BLACK);
                                viewsHolder.getAvatarContainer().setVisibility(View.GONE);
                                viewsHolder.getBlurImageMask().setVisibility(View.GONE);
                                attributesHolder.getAvatarsViewPager().setVisibility(View.VISIBLE);
                            }
                        });
                        attributesHolder.getExpandAnimator().start();
                    }
                    ViewGroup.LayoutParams params = attributesHolder.getAvatarsViewPager().getLayoutParams();
                    params.width = listView.getClippedList().getMeasuredWidth();
                    params.height = (int) (h + newTop);
                    attributesHolder.getAvatarsViewPager().requestLayout();
                    if (!attributesHolder.getExpandAnimator().isRunning()) {
                        float additionalTranslationY = 0;
                        if (rowsHolder.isOpenAnimationInProgress() && rowsHolder.getPlayProfileAnimation() == 2) {
                            additionalTranslationY = -(1.0f - rowsHolder.getAvatarAnimationProgress()) * AndroidUtilities.dp(50);
                        }
                        rowsHolder.setOnlineX(AndroidUtilities.dpf2(16f) - attributesHolder.getOnlineTextView()[1].getLeft());
                        attributesHolder.getNameTextView()[1].setTranslationX(AndroidUtilities.dpf2(18f) - attributesHolder.getNameTextView()[1].getLeft());
                        attributesHolder.getNameTextView()[1].setTranslationY(newTop + h - AndroidUtilities.dpf2(38f) - attributesHolder.getNameTextView()[1].getBottom() - AndroidUtilities.dp(ProfileParams.GROUP_BUTTON_DIMENSION) + additionalTranslationY);
                        attributesHolder.getOnlineTextView()[1].setTranslationX(rowsHolder.getOnlineX() - rowsHolder.getCustomPhotoOffset());
                        attributesHolder.getOnlineTextView()[1].setTranslationY(newTop + h - AndroidUtilities.dpf2(18f) - attributesHolder.getOnlineTextView()[1].getBottom() - AndroidUtilities.dp(ProfileParams.GROUP_BUTTON_DIMENSION) + additionalTranslationY);
                        attributesHolder.getMediaCounterTextView().setTranslationX(attributesHolder.getOnlineTextView()[1].getTranslationX());
                        attributesHolder.getMediaCounterTextView().setTranslationY(attributesHolder.getOnlineTextView()[1].getTranslationY());
                        refreshAvatar();
                        updateCollectibleHint(components);
                    }
                } else {
                    if (rowsHolder.isPulledDown()) {
                        rowsHolder.setPulledDown(false);
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                        if (viewsHolder.getOtherItem() != null) {
                            viewsHolder.getOtherItem().hideSubItem(ProfileParams.gallery_menu_save);
                            if (viewsHolder.getImageUpdater() != null) {
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.set_as_main);
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.edit_avatar);
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.delete_avatar);
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.add_photo);
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.logout);
//                                viewsHolder.getOtherItem().showSubItem(edit_name);
                            }
                        }
                        if (viewsHolder.getSearchItem() != null) {
                            viewsHolder.getSearchItem().setEnabled(!rowsHolder.isScrolling());
                        }
                        viewsHolder.getOverlaysView().setOverlaysVisible(false, durationFactor);
                        viewsHolder.getAvatarsViewPagerIndicatorView().refreshVisibility(durationFactor);
                        attributesHolder.getExpandAnimator().cancel();
                        attributesHolder.getAvatarImage().getImageReceiver().setAllowStartAnimation(true);
                        attributesHolder.getAvatarImage().getImageReceiver().startAnimation();

                        float value = AndroidUtilities.lerp(attributesHolder.getExpandAnimatorValues(), rowsHolder.getCurrentExpanAnimatorFracture());
                        attributesHolder.getExpandAnimatorValues()[0] = value;
                        attributesHolder.getExpandAnimatorValues()[1] = 0f;
                        attributesHolder.getExpandAnimator().setInterpolator(CubicBezierInterpolator.EASE_BOTH);
                        if (!rowsHolder.isInLandscapeMode()) {
                            attributesHolder.getExpandAnimator().setDuration((long) (value * 250f / durationFactor));
                        } else {
                            attributesHolder.getExpandAnimator().setDuration(0);
                        }
                        viewsHolder.getTopView().setBackgroundColor(colorsUtils.getThemedColor(Theme.key_avatar_backgroundActionBarBlue));

                        if (!rowsHolder.isDoNotSetForeground()) {
                            BackupImageView imageView = attributesHolder.getAvatarsViewPager().getCurrentItemView();
                            if (imageView != null) {
                                if (imageView.getImageReceiver().getDrawable() instanceof VectorAvatarThumbDrawable) {
                                    attributesHolder.getAvatarImage().drawForeground(false);
                                } else {
                                    attributesHolder.getAvatarImage().drawForeground(true);
                                    attributesHolder.getAvatarImage().setForegroundImageDrawable(imageView.getImageReceiver().getDrawableSafe());
                                }
                            }
                        }
                        attributesHolder.getAvatarImage().setForegroundAlpha(1f);
                        viewsHolder.getAvatarContainer().setVisibility(View.VISIBLE);
                        viewsHolder.getBlurImageMask().setVisibility(View.VISIBLE);
                        attributesHolder.getAvatarsViewPager().setVisibility(View.GONE);
                        attributesHolder.getExpandAnimator().start();
                    }

                    viewsHolder.getAvatarContainer().setScaleX(rowsHolder.getAvatarScale());
                    viewsHolder.getAvatarContainer().setScaleY(rowsHolder.getAvatarScale());
                    viewsHolder.getBlurImageMask().setScaleX(rowsHolder.getAvatarScale());
                    viewsHolder.getBlurImageMask().setScaleY(rowsHolder.getAvatarScale());

                    if (attributesHolder.getExpandAnimator() == null || !attributesHolder.getExpandAnimator().isRunning()) {
                        viewsHolder.getAvatarContainer().setTranslationX((listView.getClippedList().getWidth() - viewsHolder.getAvatarContainer().getWidth() * rowsHolder.getAvatarScale()) / 2);
                        refreshNameAndOnlineXY();
                        attributesHolder.getNameTextView()[1].setTranslationX(rowsHolder.getNameX());
                        attributesHolder.getNameTextView()[1].setTranslationY(rowsHolder.getNameY());
                        attributesHolder.getOnlineTextView()[1].setTranslationX(rowsHolder.getOnlineX() + rowsHolder.getCustomPhotoOffset());
                        attributesHolder.getOnlineTextView()[1].setTranslationY(rowsHolder.getOnlineY());
                        attributesHolder.getMediaCounterTextView().setTranslationX(rowsHolder.getOnlineX());
                        attributesHolder.getMediaCounterTextView().setTranslationY(rowsHolder.getOnlineY());
                        refreshAvatar();
                        updateCollectibleHint(components);
                    }
                }
            } else {
                handleAvatarBlurredImage();
            }

            if (rowsHolder.isOpenAnimationInProgress() && rowsHolder.getPlayProfileAnimation() == 2) {
                float avX = 0;
                float avY = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f - 21 * AndroidUtilities.density + profileActivity.getActionBar().getTranslationY();

                attributesHolder.getNameTextView()[0].setTranslationX(0);
                attributesHolder.getNameTextView()[0].setTranslationY((float) Math.floor(avY) + AndroidUtilities.dp(1.3f));
                attributesHolder.getOnlineTextView()[0].setTranslationX(0);
                attributesHolder.getOnlineTextView()[0].setTranslationY((float) Math.floor(avY) + AndroidUtilities.dp(24));
                attributesHolder.getNameTextView()[0].setScaleX(1.0f);
                attributesHolder.getNameTextView()[0].setScaleY(1.0f);

                attributesHolder.getNameTextView()[1].setPivotY(attributesHolder.getNameTextView()[1].getMeasuredHeight());
                attributesHolder.getNameTextView()[1].setScaleX(1.67f);
                attributesHolder.getNameTextView()[1].setScaleY(1.67f);

                rowsHolder.setAvatarScale(AndroidUtilities.lerp(1.0f, (ProfileParams.AVATAR_DIMENSION + ProfileParams.AVATAR_DIMENSION + 18f) / ProfileParams.AVATAR_DIMENSION, rowsHolder.getAvatarAnimationProgress()));
                if (viewsHolder.getStoryView() != null) {
                    viewsHolder.getStoryView().setExpandProgress(1f);
                }
                if (components.getViewComponentsHolder().getGiftsView() != null) {
                    components.getViewComponentsHolder().getGiftsView().setExpandProgress(1f);
                }

                attributesHolder.getAvatarImage().setRoundRadius((int) AndroidUtilities.lerp(getSmallAvatarRoundRadius(profileActivity), 0f, attributesHolder.getGroupButtonTransitionProgress()));
                viewsHolder.getAvatarContainer().setTranslationX(rowsHolder.getAvatarX());
                viewsHolder.getAvatarContainer().setTranslationY(AndroidUtilities.lerp((float) Math.ceil(avY), 0f, rowsHolder.getAvatarAnimationProgress()));
                viewsHolder.getBlurImageMask().setTranslationX(rowsHolder.getAvatarX());
                viewsHolder.getBlurImageMask().setTranslationY((float) AndroidUtilities.lerp(Math.ceil(avY), 0f, rowsHolder.getAvatarAnimationProgress()));
                float extra = (viewsHolder.getAvatarContainer().getMeasuredWidth() - AndroidUtilities.dp(ProfileParams.AVATAR_DIMENSION)) * rowsHolder.getAvatarScale();
                viewsHolder.getTimeItem().setTranslationX(viewsHolder.getAvatarContainer().getX() + AndroidUtilities.dp(16) + extra);
                viewsHolder.getTimeItem().setTranslationY(viewsHolder.getAvatarContainer().getY() + AndroidUtilities.dp(15) + extra);
                components.getViewComponentsHolder().getStarBgItem().setTranslationX(viewsHolder.getAvatarContainer().getX() + AndroidUtilities.dp(28) + extra);
                components.getViewComponentsHolder().getStarBgItem().setTranslationY(viewsHolder.getAvatarContainer().getY() + AndroidUtilities.dp(24) + extra);
                components.getViewComponentsHolder().getStarBgItem().setTranslationX(viewsHolder.getAvatarContainer().getX() + AndroidUtilities.dp(28) + extra);
                components.getViewComponentsHolder().getStarBgItem().setTranslationY(viewsHolder.getAvatarContainer().getY() + AndroidUtilities.dp(24) + extra);
                viewsHolder.getAvatarContainer().setScaleX(rowsHolder.getAvatarScale());
                viewsHolder.getAvatarContainer().setScaleY(rowsHolder.getAvatarScale());
                viewsHolder.getBlurImageMask().setScaleX(rowsHolder.getAvatarScale());
                viewsHolder.getBlurImageMask().setScaleY(rowsHolder.getAvatarScale());

                viewsHolder.getOverlaysView().setAlphaValue(rowsHolder.getAvatarAnimationProgress(), false);
                profileActivity.getActionBar().setItemsColor(ColorUtils.blendARGB(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE, rowsHolder.getAvatarAnimationProgress()), false);

                if (attributesHolder.getScamDrawable() != null) {
                    attributesHolder.getScamDrawable().setColor(ColorUtils.blendARGB(colorsUtils.getThemedColor(Theme.key_avatar_subtitleInProfileBlue), Color.argb(179, 255, 255, 255), rowsHolder.getAvatarAnimationProgress()));
                }
                if (attributesHolder.getLockIconDrawable() != null) {
                    attributesHolder.getLockIconDrawable().setColorFilter(ColorUtils.blendARGB(colorsUtils.getThemedColor(Theme.key_chat_lockIcon), Color.WHITE, rowsHolder.getAvatarAnimationProgress()), PorterDuff.Mode.MULTIPLY);
                }
                if (attributesHolder.getVerifiedCrossfadeDrawable()[1] != null) {
                    attributesHolder.getVerifiedCrossfadeDrawable()[1].setProgress(rowsHolder.getAvatarAnimationProgress());
                    attributesHolder.getNameTextView()[1].invalidate();
                }
                if (attributesHolder.getPremiumCrossfadeDrawable()[1] != null) {
                    attributesHolder.getPremiumCrossfadeDrawable()[1].setProgress(rowsHolder.getAvatarAnimationProgress());
                    attributesHolder.getNameTextView()[1].invalidate();
                }
                colorsUtils.updateEmojiStatusDrawableColor(rowsHolder.getAvatarAnimationProgress());

                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewsHolder.getAvatarContainer().getLayoutParams();
                params.width = params.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(ProfileParams.AVATAR_DIMENSION), (rowsHolder.getExtraHeight() + newTop) / rowsHolder.getAvatarScale(), rowsHolder.getAvatarAnimationProgress());
                params.leftMargin = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(0), 0f, rowsHolder.getAvatarAnimationProgress());
                viewsHolder.getAvatarContainer().requestLayout();

                updateCollectibleHint(components);
            } else if (rowsHolder.getExtraHeight() <= AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) {
                rowsHolder.setAvatarScale((ProfileParams.AVATAR_DIMENSION + 18) * ((float) Math.pow(diff, 1.2f)) / ProfileParams.AVATAR_DIMENSION);
                if (viewsHolder.getStoryView() != null) {
                    viewsHolder.getStoryView().invalidate();
                }
                if (components.getViewComponentsHolder().getGiftsView() != null) {
                    components.getViewComponentsHolder().getGiftsView().invalidate();
                }
                float nameScale = 1.0f + 0.12f * diff;
                if (attributesHolder.getExpandAnimator() == null || !attributesHolder.getExpandAnimator().isRunning()) {
                    viewsHolder.getAvatarContainer().setScaleX(rowsHolder.getAvatarScale());
                    viewsHolder.getAvatarContainer().setScaleY(rowsHolder.getAvatarScale());
                    viewsHolder.getBlurImageMask().setScaleX(rowsHolder.getAvatarScale());
                    viewsHolder.getBlurImageMask().setScaleY(rowsHolder.getAvatarScale());
                    viewsHolder.getAvatarContainer().setTranslationX(rowsHolder.getAvatarX());
                    viewsHolder.getAvatarContainer().setTranslationY((float) Math.ceil(rowsHolder.getAvatarY()));
//                    viewsHolder.getAvatarContainer().setTranslationX(AndroidUtilities.lerp(AndroidUtilities.dp(64), (listView.getClippedList().getWidth() - viewsHolder.getAvatarContainer().getWidth() * rowsHolder.getAvatarScale()) / 2f, attributesHolder.getGroupButtonTransitionProgress()));
//                    viewsHolder.getAvatarContainer().setTranslationY((float) Math.ceil(rowsHolder.getAvatarY()) + ProfileParams.AVATAR_DIMENSION);
                    viewsHolder.getBlurImageMask().setTranslationX(rowsHolder.getAvatarX());
                    viewsHolder.getBlurImageMask().setTranslationY((float) Math.ceil(rowsHolder.getAvatarY()));
                    float extra = AndroidUtilities.dp(ProfileParams.AVATAR_DIMENSION) * rowsHolder.getAvatarScale() - AndroidUtilities.dp(42);
                    viewsHolder.getTimeItem().setTranslationX(viewsHolder.getAvatarContainer().getX() + AndroidUtilities.dp(16) + extra);
                    viewsHolder.getTimeItem().setTranslationY(viewsHolder.getAvatarContainer().getY() + AndroidUtilities.dp(15) + extra);
                    components.getViewComponentsHolder().getStarBgItem().setTranslationX(viewsHolder.getAvatarContainer().getX() + AndroidUtilities.dp(28) + extra);
                    components.getViewComponentsHolder().getStarBgItem().setTranslationY(viewsHolder.getAvatarContainer().getY() + AndroidUtilities.dp(24) + extra);
                    components.getViewComponentsHolder().getStarBgItem().setTranslationX(viewsHolder.getAvatarContainer().getX() + AndroidUtilities.dp(28) + extra);
                    components.getViewComponentsHolder().getStarBgItem().setTranslationY(viewsHolder.getAvatarContainer().getY() + AndroidUtilities.dp(24) + extra);
                }
                refreshNameAndOnlineXY();
                float baselineY = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2f - 21 * AndroidUtilities.density + profileActivity.getActionBar().getTranslationY() - AndroidUtilities.dp(7.5f);
                rowsHolder.setNameX(AndroidUtilities.lerp(AndroidUtilities.dpf2(52.33f), rowsHolder.getNameX(), attributesHolder.getGroupButtonTransitionProgress()));
                rowsHolder.setNameY(AndroidUtilities.lerp((float) Math.floor(baselineY) + AndroidUtilities.dp(1.3f) + AndroidUtilities.dp(7), rowsHolder.getNameY(), attributesHolder.getGroupButtonTransitionProgress()));
                rowsHolder.setOnlineX(AndroidUtilities.lerp(AndroidUtilities.dpf2(52.33f), rowsHolder.getOnlineX(), attributesHolder.getGroupButtonTransitionProgress()));
                rowsHolder.setOnlineY(AndroidUtilities.lerp((float) Math.floor(baselineY) + AndroidUtilities.dp(21) + (float) Math.floor(11 * AndroidUtilities.density), rowsHolder.getOnlineY(), attributesHolder.getGroupButtonTransitionProgress()));
                if (viewsHolder.getShowStatusButton() != null) {
                    viewsHolder.getShowStatusButton().setAlpha((int) (0xFF * diff));
                }
                for (int a = 0; a < attributesHolder.getNameTextView().length; a++) {
                    if (attributesHolder.getNameTextView()[a] == null) {
                        continue;
                    }
                    if (attributesHolder.getExpandAnimator() == null || !attributesHolder.getExpandAnimator().isRunning()) {
                        float d = Math.max(0, Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) * Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)));
                        float nameCurrentX = getNameCurrentX(a, nameScale);
                        float nameNewX = AndroidUtilities.lerp(nameCurrentX, AndroidUtilities.dp(62), 1 - d);

                        float onlineScale = attributesHolder.getOnlineTextView()[a].getScaleX();
                        float onlineCurrentX = ((viewsHolder.getAvatarContainer().getMeasuredWidth() / 2f - (attributesHolder.getOnlineTextView()[a].getTextWidth() * onlineScale) / 2)) - (attributesHolder.getOnlineTextView()[a].getPaddingLeft() * onlineScale);
                        float onlineNewX = AndroidUtilities.lerp(onlineCurrentX, AndroidUtilities.dp(62), 1 - d);

                        attributesHolder.getNameTextView()[a].setTranslationX(nameNewX);
                        attributesHolder.getOnlineTextView()[a].setTranslationX(nameNewX + ((float) attributesHolder.getNameTextView()[a].getWidth() / 2) - 10);

                        attributesHolder.getNameTextView()[a].setTranslationY(rowsHolder.getNameY());
                        attributesHolder.getOnlineTextView()[a].setTranslationY(rowsHolder.getOnlineY() + 10);

//                        attributesHolder.getNameTextView()[a].setTranslationX(rowsHolder.getNameX());
//                        attributesHolder.getNameTextView()[a].setTranslationY(rowsHolder.getNameY());
//
//                        attributesHolder.getOnlineTextView()[a].setTranslationX(rowsHolder.getOnlineX() + rowsHolder.getCustomPhotoOffset());
                        if (a == 1) {
                            if (Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) < 0.5) {
                                attributesHolder.getMediaCounterTextView().setTranslationX(onlineNewX);
                            } else {
                                attributesHolder.getMediaCounterTextView().setTranslationX(onlineCurrentX);
                            }
                            attributesHolder.getMediaCounterTextView().setTranslationY(rowsHolder.getOnlineY());
                        }
                    }
                    attributesHolder.getNameTextView()[a].setScaleX(nameScale);
                    attributesHolder.getNameTextView()[a].setScaleY(nameScale);
                }
                updateCollectibleHint(components);
            }

            if (!rowsHolder.isOpenAnimationInProgress() && (attributesHolder.getExpandAnimator() == null || !attributesHolder.getExpandAnimator().isRunning())) {
                needLayoutText(diff);
            }

            viewsHolder.getButtonsGroup().setTranslationY(rowsHolder.getExtraHeight() * rowsHolder.getSearchTransitionProgress()
                + newTop - AndroidUtilities.dp(ProfileParams.GROUP_BUTTON_DIMENSION + ProfileParams.GROUP_MARGIN));
        }

        if (rowsHolder.isPulledDown() || (viewsHolder.getOverlaysView() != null && viewsHolder.getOverlaysView().animator != null && viewsHolder.getOverlaysView().animator.isRunning())) {
            final ViewGroup.LayoutParams overlaysLp = viewsHolder.getOverlaysView().getLayoutParams();
            overlaysLp.width = listView.getClippedList().getMeasuredWidth();
            overlaysLp.height = (int) (rowsHolder.getExtraHeight() + newTop);
            viewsHolder.getOverlaysView().requestLayout();
        }

        updateEmojiStatusEffectPosition(components);
    }

    private float getNameCurrentX(int a, float nameScale) {
        float nameCurrentX = ((viewsHolder.getAvatarContainer2().getMeasuredWidth() / 2f - (attributesHolder.getNameTextView()[a].getTextWidth() * nameScale) / 2)) - ((attributesHolder.getNameTextView()[a].getPaddingRight() * nameScale) / 2);
        if (attributesHolder.getNameTextView()[a].getRightDrawable() != null) {
            nameCurrentX = nameCurrentX - ((attributesHolder.getNameTextView()[a].getRightDrawable().getMinimumWidth() * nameScale) / 2);
        }
        return nameCurrentX;
    }

    private void handleAvatarBlurredImage() {
        float step = rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT);
        rowsHolder.setAvatarY(AndroidUtilities.lerp(-((viewsHolder.getAvatarContainer().getMeasuredHeight() * rowsHolder.getAvatarScale()) + AndroidUtilities.dp(18)), rowsHolder.getAvatarY(), step));

        float s1 = 0.9f;
        float s2 = 0.7f;
        float s3 = 0.45f;

        if (step <= s1 && step > s2) {
            float value = (s1 - step) / (s1 - s2);
            attributesHolder.getAvatarImage().setAlpha(AndroidUtilities.lerp(1f, 0f, value));
            viewsHolder.getBlurImageMask().setAlpha(AndroidUtilities.lerp(0f, 0.8f, value));
        } else if (step <= s2 && step > s3) {
            float value = (s2 - step) / (s2 - s3);
            attributesHolder.getAvatarImage().setAlpha(AndroidUtilities.lerp(0f, 0f, value));
            viewsHolder.getBlurImageMask().setAlpha(AndroidUtilities.lerp(0.8f, 0f, value));
        } else if (step <= s3) {
            viewsHolder.getBlurImageMask().setAlpha(0f);
            attributesHolder.getAvatarImage().setAlpha(0f);
        } else {
            viewsHolder.getBlurImageMask().setAlpha(0f);
            attributesHolder.getAvatarImage().setAlpha(1f);
        }
    }

    public void refreshAvatar() {
        checkForRowsHolder();
        checkForViewsHolder();
        rowsHolder.setAvatarX((viewsHolder.getAvatarContainer2().getMeasuredWidth() / 2f) - ((AndroidUtilities.dp(ProfileParams.AVATAR_DIMENSION) * rowsHolder.getAvatarScale()) / 2));
        viewsHolder.getAvatarContainer().setTranslationX(rowsHolder.getAvatarX());
        viewsHolder.getBlurImageMask().setTranslationX(rowsHolder.getAvatarX());
    }
    public void updateBottomButtonY() {
        checkForViewsHolder();
        if (viewsHolder.getBottomButtonsContainer() == null) {
            return;
        }
        viewsHolder.getBottomButtonsContainer().setTranslationY(
                viewsHolder.getSharedMediaLayout() != null && viewsHolder.getSharedMediaLayout().isAttachedToWindow() ?
                        Math.max(0, AndroidUtilities.dp(72 + 64 + 48) - (listView.getClippedList().getMeasuredHeight() - viewsHolder.getSharedMediaLayout().getY()))
                        : AndroidUtilities.dp(72));
        final Bulletin bulletin = Bulletin.getVisibleBulletin();
        if (bulletin != null) {
            bulletin.updatePosition();
        }
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }

    private void checkForAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = components.getAttributesComponentsHolder();
        }
    }

    private void checkForViewsHolder() {
        if (viewsHolder == null) {
            viewsHolder = components.getViewComponentsHolder();
        }
    }

    public void needLayoutText(float diff) {
        FrameLayout.LayoutParams layoutParams;
        float scale = attributesHolder.getNameTextView()[1].getScaleX();
        float maxScale = rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) ? 1.67f : 1.12f;

        if (rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) && scale != maxScale) {
            return;
        }

        int viewWidth = AndroidUtilities.isTablet() ? AndroidUtilities.dp(490) : AndroidUtilities.displaySize.x;
        ActionBarMenuItem item = viewsHolder.getAvatarsViewPagerIndicatorView().getSecondaryMenuItem();
        int extra = 0;
        if (rowsHolder.isEditItemVisible()) {
            extra += 48;
        }
        if (rowsHolder.isCallItemVisible()) {
            extra += 48;
        }
        if (rowsHolder.isVideoCallItemVisible()) {
            extra += 48;
        }
        if (viewsHolder.getSearchItem() != null) {
            extra += 48;
        }
        int buttonsWidth = AndroidUtilities.dp(118 + 8 + (40 + extra * (1.0f - rowsHolder.getMediaHeaderAnimationProgress())));
        int minWidth = viewWidth - buttonsWidth;

        int width = (int) (viewWidth - buttonsWidth * Math.max(0.0f, 1.0f - (diff != 1.0f ? diff * 0.15f / (1.0f - diff) : 1.0f)) - attributesHolder.getNameTextView()[1].getTranslationX());
        float width2 = attributesHolder.getNameTextView()[1].getPaint().measureText(attributesHolder.getNameTextView()[1].getText().toString()) * scale + attributesHolder.getNameTextView()[1].getSideDrawablesSize();
        layoutParams = (FrameLayout.LayoutParams) attributesHolder.getNameTextView()[1].getLayoutParams();
        int prevWidth = layoutParams.width;
        if (width < width2) {
            layoutParams.width = Math.max(minWidth, (int) Math.ceil((width - AndroidUtilities.dp(24)) / (scale + ((maxScale - scale) * 7.0f))));
        } else {
            layoutParams.width = (int) Math.ceil(width2);
        }
        layoutParams.width = (int) Math.min((viewWidth - attributesHolder.getNameTextView()[1].getX()) / scale - AndroidUtilities.dp(8), layoutParams.width);
        if (layoutParams.width != prevWidth) {
            attributesHolder.getNameTextView()[1].requestLayout();
        }

        width2 = attributesHolder.getOnlineTextView()[1].getPaint().measureText(attributesHolder.getOnlineTextView()[1].getText().toString()) + attributesHolder.getOnlineTextView()[1].getRightDrawableWidth();
        layoutParams = (FrameLayout.LayoutParams) attributesHolder.getOnlineTextView()[1].getLayoutParams();
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) attributesHolder.getMediaCounterTextView().getLayoutParams();
        prevWidth = layoutParams.width;
        layoutParams2.rightMargin = layoutParams.rightMargin = (int) Math.ceil(attributesHolder.getOnlineTextView()[1].getTranslationX() + AndroidUtilities.dp(8) + AndroidUtilities.dp(40) * (1.0f - diff));
        if (width < width2) {
            layoutParams2.width = layoutParams.width = (int) Math.ceil(width);
        } else {
            layoutParams2.width = layoutParams.width = LayoutHelper.WRAP_CONTENT;
        }
        if (prevWidth != layoutParams.width) {
            attributesHolder.getOnlineTextView()[2].getLayoutParams().width = layoutParams.width;
            attributesHolder.getOnlineTextView()[2].requestLayout();
            attributesHolder.getOnlineTextView()[3].getLayoutParams().width = layoutParams.width;
            attributesHolder.getOnlineTextView()[3].requestLayout();
            attributesHolder.getOnlineTextView()[1].requestLayout();
            attributesHolder.getMediaCounterTextView().requestLayout();
        }
    }
}
