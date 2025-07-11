package org.telegram.ui.profile.utils;

import static org.telegram.ui.profile.utils.ColorsUtils.dontApplyPeerColor;
import static org.telegram.ui.profile.utils.Hints.updateCollectibleHint;
import static org.telegram.ui.profile.utils.Images.checkPhotoDescriptionAlpha;
import static org.telegram.ui.profile.utils.QR.isQrNeedVisible;
import static org.telegram.ui.profile.utils.QR.updateQrItemVisibility;
import static org.telegram.ui.profile.utils.Views.updateStoriesViewBounds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Property;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;

import java.util.ArrayList;

public class Animation {


    private final ProfileActivity profileActivity;
    private final Adapter profileAdapter;
    private ClippedListView listView;
    private Layouts layouts;
    private final ComponentsFactory componentsFactory;
    private ViewComponentsHolder viewsHolder;
    private ColorsUtils colorsUtils;
    private RowsAndStatusComponentsHolder rowsHolder;
    AttributesComponentsHolder attributesHolder;

    public Animation(ComponentsFactory componentsFactory) {
        this.componentsFactory = componentsFactory;
        profileActivity = componentsFactory.getProfileActivity();
        profileAdapter = componentsFactory.getAdapter();
        listView = componentsFactory.getListView();
        layouts = componentsFactory.getLayouts();
        viewsHolder = componentsFactory.getViewComponentsHolder();
        colorsUtils = componentsFactory.getColorsUtils();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        attributesHolder = componentsFactory.getAttributesComponentsHolder();
    }
    public final Property<org.telegram.ui.ActionBar.ActionBar, Float> ACTIONBAR_HEADER_PROGRESS = new AnimationProperties.FloatProperty<org.telegram.ui.ActionBar.ActionBar>("avatarAnimationProgress") {
        @Override
        public void setValue(org.telegram.ui.ActionBar.ActionBar object, float value) {
            rowsHolder.setMediaHeaderAnimationProgress(value);
            if (viewsHolder.getStoryView() != null) {
                viewsHolder.getStoryView().setActionBarActionMode(value);
            }
            if (componentsFactory.getViewComponentsHolder().getGiftsView() != null) {
                componentsFactory.getViewComponentsHolder().getGiftsView().setActionBarActionMode(value);
            }
            viewsHolder.getTopView().invalidate();

            if (colorsUtils == null) {
                colorsUtils = componentsFactory.getColorsUtils();
            }
            int color1 = colorsUtils.getThemedColor(Theme.key_profile_title);
            int color2 = colorsUtils.getThemedColor(Theme.key_player_actionBarTitle);
            int c = AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f);
            attributesHolder.getNameTextView()[1].setTextColor(c);
            if (attributesHolder.getLockIconDrawable() != null) {
                attributesHolder.getLockIconDrawable().setColorFilter(c, PorterDuff.Mode.MULTIPLY);
            }
            if (attributesHolder.getScamDrawable() != null) {
                color1 = colorsUtils.getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
                attributesHolder.getScamDrawable().setColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f));
            }

            color1 = attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon);
            color2 = colorsUtils.getThemedColor(Theme.key_actionBarActionModeDefaultIcon);
            profileActivity.getActionBar().setItemsColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            color1 = attributesHolder.getPeerColor() != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : colorsUtils.getThemedColor(Theme.key_avatar_actionBarSelectorBlue);
            color2 = colorsUtils.getThemedColor(Theme.key_actionBarActionModeDefaultSelector);
            profileActivity.getActionBar().setItemsBackgroundColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            viewsHolder.getTopView().invalidate();
            viewsHolder.getOtherItem().setIconColor(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon));
            viewsHolder.getCallItem().setIconColor(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon));
            viewsHolder.getVideoCallItem().setIconColor(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon));
            viewsHolder.getEditItem().setIconColor(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon));

            if (attributesHolder.getVerifiedDrawable()[0] != null) {
                color1 = colorsUtils.getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = colorsUtils.getThemedColor(Theme.key_player_actionBarTitle);
                attributesHolder.getVerifiedDrawable()[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (attributesHolder.getVerifiedDrawable()[1] != null) {
                color1 = attributesHolder.getPeerColor() != null ? Theme.adaptHSV(ColorUtils.blendARGB(attributesHolder.getPeerColor().getColor2(), attributesHolder.getPeerColor().hasColor6(Theme.isCurrentThemeDark()) ? attributesHolder.getPeerColor().getColor5() : attributesHolder.getPeerColor().getColor3(), .4f), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f) : colorsUtils.getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = colorsUtils.getThemedColor(Theme.key_player_actionBarTitle);
                attributesHolder.getVerifiedDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            if (attributesHolder.getVerifiedCheckDrawable()[0] != null) {
                color1 = colorsUtils.getThemedColor(Theme.key_profile_verifiedCheck);
                color2 = colorsUtils.getThemedColor(Theme.key_windowBackgroundWhite);
                attributesHolder.getVerifiedCheckDrawable()[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (attributesHolder.getVerifiedCheckDrawable()[1] != null) {
                color1 = attributesHolder.getPeerColor() != null ? Color.WHITE : dontApplyPeerColor(colorsUtils.getThemedColor(Theme.key_profile_verifiedCheck));
                color2 = colorsUtils.getThemedColor(Theme.key_windowBackgroundWhite);
                attributesHolder.getVerifiedCheckDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }


            if (attributesHolder.getPremiumStarDrawable()[0] != null) {
                color1 = colorsUtils.getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = colorsUtils.getThemedColor(Theme.key_player_actionBarTitle);
                attributesHolder.getPremiumStarDrawable()[0].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            if (attributesHolder.getPremiumStarDrawable()[1] != null) {
                color1 = dontApplyPeerColor(colorsUtils.getThemedColor(Theme.key_profile_verifiedBackground));
                color2 = dontApplyPeerColor(colorsUtils.getThemedColor(Theme.key_player_actionBarTitle));
                attributesHolder.getPremiumStarDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            colorsUtils.updateEmojiStatusDrawableColor();

            if (viewsHolder.getAvatarsViewPagerIndicatorView().getSecondaryMenuItem() != null && (rowsHolder.isVideoCallItemVisible() || rowsHolder.isEditItemVisible() || rowsHolder.isCallItemVisible())) {
                layouts.needLayoutText(Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)));
            }
        }

        @Override
        public Float get(ActionBar object) {
            return rowsHolder.getMediaHeaderAnimationProgress();
        }
    };


    public void setMediaHeaderVisible(boolean visible) {
        if (rowsHolder.isMediaHeaderVisible() == visible) {
            return;
        }
        rowsHolder.setMediaHeaderVisible(visible);
        if (attributesHolder.getHeaderAnimatorSet() != null) {
            attributesHolder.getHeaderAnimatorSet().cancel();
        }
        if (attributesHolder.getHeaderShadowAnimatorSet() != null) {
            attributesHolder.getHeaderShadowAnimatorSet().cancel();
        }
        ActionBarMenuItem mediaSearchItem = viewsHolder.getSharedMediaLayout().getSearchItem();
        ImageView mediaOptionsItem = viewsHolder.getSharedMediaLayout().getSearchOptionsItem();
        TextView saveItem = viewsHolder.getSharedMediaLayout().getSaveItem();
        if (!rowsHolder.isMediaHeaderVisible()) {
            if (rowsHolder.isCallItemVisible()) {
                viewsHolder.getCallItem().setVisibility(View.VISIBLE);
            }
            if (rowsHolder.isVideoCallItemVisible()) {
                viewsHolder.getVideoCallItem().setVisibility(View.VISIBLE);
            }
            if (rowsHolder.isEditItemVisible()) {
                viewsHolder.getEditItem().setVisibility(View.VISIBLE);
            }
            viewsHolder.getOtherItem().setVisibility(View.VISIBLE);
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.GONE);
            }
            if (saveItem != null) {
                saveItem.setVisibility(View.GONE);
            }
        } else {
            if (viewsHolder.getSharedMediaLayout().isSearchItemVisible()) {
                mediaSearchItem.setVisibility(View.VISIBLE);
            }
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.VISIBLE);
            }
            if (viewsHolder.getSharedMediaLayout().isOptionsItemVisible()) {
                viewsHolder.getSharedMediaLayout().photoVideoOptionsItem.setVisibility(View.VISIBLE);
                viewsHolder.getSharedMediaLayout().animateSearchToOptions(true, false);
            } else {
                viewsHolder.getSharedMediaLayout().photoVideoOptionsItem.setVisibility(View.INVISIBLE);
                viewsHolder.getSharedMediaLayout().animateSearchToOptions(false, false);
            }
        }
        updateStoriesViewBounds(viewsHolder, profileActivity, componentsFactory.getViewComponentsHolder(), false);

        if (profileActivity.getActionBar() != null) {
            profileActivity.getActionBar().createMenu().requestLayout();
        }

        ArrayList<Animator> animators = new ArrayList<>();

        animators.add(ObjectAnimator.ofFloat(viewsHolder.getCallItem(), View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getVideoCallItem(), View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getOtherItem(), View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getEditItem(), View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getCallItem(), View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getVideoCallItem(), View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getOtherItem(), View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getEditItem(), View.TRANSLATION_Y, visible ? -AndroidUtilities.dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.TRANSLATION_Y, visible ? 0.0f : AndroidUtilities.dp(10)));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getSharedMediaLayout().photoVideoOptionsItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(viewsHolder.getSharedMediaLayout().photoVideoOptionsItem, View.TRANSLATION_Y, visible ? 0.0f : AndroidUtilities.dp(10)));
        animators.add(ObjectAnimator.ofFloat(profileActivity.getActionBar(), ACTIONBAR_HEADER_PROGRESS, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(attributesHolder.getOnlineTextView()[1], View.ALPHA, visible ? 0.0f : 1.0f));
        if (componentsFactory.getAttributesComponentsHolder().isMyProfile()) animators.add(ObjectAnimator.ofFloat(attributesHolder.getOnlineTextView()[3], View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(attributesHolder.getMediaCounterTextView(), View.ALPHA, visible ? 1.0f : 0.0f));
        if (visible) {
            animators.add(ObjectAnimator.ofFloat(profileActivity, HEADER_SHADOW, 0.0f));
        }
        if (viewsHolder.getStoryView() != null || componentsFactory.getViewComponentsHolder().getGiftsView() != null) {
            ValueAnimator va = ValueAnimator.ofFloat(0, 1);
            va.addUpdateListener(a -> updateStoriesViewBounds(viewsHolder, profileActivity, componentsFactory.getViewComponentsHolder(), true));
            animators.add(va);
        }

        attributesHolder.setHeaderAnimatorSet(new AnimatorSet());
        attributesHolder.getHeaderAnimatorSet().playTogether(animators);
        attributesHolder.getHeaderAnimatorSet().setInterpolator(CubicBezierInterpolator.DEFAULT);
        attributesHolder.getHeaderAnimatorSet().addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (attributesHolder.getHeaderAnimatorSet() != null) {
                    if (rowsHolder.isMediaHeaderVisible()) {
                        if (rowsHolder.isCallItemVisible()) {
                            viewsHolder.getCallItem().setVisibility(View.GONE);
                        }
                        if (rowsHolder.isVideoCallItemVisible()) {
                            viewsHolder.getVideoCallItem().setVisibility(View.GONE);
                        }
                        if (rowsHolder.isEditItemVisible()) {
                            viewsHolder.getEditItem().setVisibility(View.GONE);
                        }
                        viewsHolder.getOtherItem().setVisibility(View.GONE);
                    } else {
                        if (viewsHolder.getSharedMediaLayout().isSearchItemVisible()) {
                            mediaSearchItem.setVisibility(View.VISIBLE);
                        }

                        viewsHolder.getSharedMediaLayout().photoVideoOptionsItem.setVisibility(View.INVISIBLE);

                        attributesHolder.setHeaderShadowAnimatorSet(new AnimatorSet());
                        attributesHolder.getHeaderShadowAnimatorSet().playTogether(ObjectAnimator.ofFloat(profileActivity, HEADER_SHADOW, 1.0f));
                        attributesHolder.getHeaderShadowAnimatorSet().setDuration(100);
                        attributesHolder.getHeaderShadowAnimatorSet().addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                attributesHolder.setHeaderShadowAnimatorSet(null);
                            }
                        });
                        attributesHolder.getHeaderShadowAnimatorSet().start();
                    }
                }
                updateStoriesViewBounds(viewsHolder, profileActivity, componentsFactory.getViewComponentsHolder(), false);
                attributesHolder.setHeaderAnimatorSet(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                attributesHolder.setHeaderAnimatorSet(null);
            }
        });
        attributesHolder.getHeaderAnimatorSet().setDuration(150);
        attributesHolder.getHeaderAnimatorSet().start();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
    }

    private void updateSearchViewState(boolean enter) {
        if (listView == null) {
            listView = componentsFactory.getListView();
        }
        int hide = enter ? View.GONE : View.VISIBLE;
        listView.getClippedList().setVisibility(hide);
        viewsHolder.getSearchListView().setVisibility(enter ? View.VISIBLE : View.GONE);
        viewsHolder.getSearchItem().getSearchContainer().setVisibility(enter ? View.VISIBLE : View.GONE);

        profileActivity.getActionBar().onSearchFieldVisibilityChanged(enter);

        viewsHolder.getAvatarContainer().setVisibility(hide);
        viewsHolder.getBlurImageMask().setVisibility(hide);
        if (viewsHolder.getStoryView() != null) {
            viewsHolder.getStoryView().setVisibility(hide);
        }
        attributesHolder.getNameTextView()[1].setVisibility(hide);
        attributesHolder.getOnlineTextView()[1].setVisibility(hide);
        attributesHolder.getOnlineTextView()[3].setVisibility(hide);

        if (viewsHolder.getOtherItem() != null) {
            viewsHolder.getOtherItem().setAlpha(1f);
            viewsHolder.getOtherItem().setVisibility(hide);
        }
        if (viewsHolder.getQrItem() != null) {
            viewsHolder.getQrItem().setAlpha(1f);
            viewsHolder.getQrItem().setVisibility(enter || !isQrNeedVisible() ? View.GONE : View.VISIBLE);
        }
        viewsHolder.getSearchItem().setVisibility(hide);

        viewsHolder.getAvatarContainer().setAlpha(1f);
        viewsHolder.getBlurImageMask().setAlpha(1f);
        if (viewsHolder.getStoryView() != null) {
            viewsHolder.getStoryView().setAlpha(1f- attributesHolder.getPullUpProgress());
        }
        if (componentsFactory.getViewComponentsHolder().getGiftsView() != null) {
            componentsFactory.getViewComponentsHolder().getGiftsView().setAlpha(1f);
        }
        attributesHolder.getNameTextView()[1].setAlpha(1f);
        attributesHolder.getOnlineTextView()[1].setAlpha(1f);
        viewsHolder.getSearchItem().setAlpha(1f);
        listView.getClippedList().setAlpha(1f);
        viewsHolder.getSearchListView().setAlpha(1f);
        viewsHolder.getEmptyView().setAlpha(1f);
        if (enter) {
            viewsHolder.getSearchListView().setEmptyView(viewsHolder.getEmptyView());
        } else {
            viewsHolder.getEmptyView().setVisibility(View.GONE);
        }
    }

    public Animator searchExpandTransition(ProfileActivity profileActivity, boolean enter) {
        if (enter) {
            AndroidUtilities.requestAdjustResize(profileActivity.getParentActivity(), profileActivity.getClassGuid());
            AndroidUtilities.setAdjustResizeToNothing(profileActivity.getParentActivity(), profileActivity.getClassGuid());
        }
        if (attributesHolder.getSearchViewTransition() != null) {
            attributesHolder.getSearchViewTransition().removeAllListeners();
            attributesHolder.getSearchViewTransition().cancel();
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(rowsHolder.getSearchTransitionProgress(), enter ? 0f : 1f);
        float offset = rowsHolder.getExtraHeight();
        viewsHolder.getSearchListView().setTranslationY(offset);
        viewsHolder.getSearchListView().setVisibility(View.VISIBLE);
        viewsHolder.getSearchItem().setVisibility(View.VISIBLE);

        listView.getClippedList().setVisibility(View.VISIBLE);

        layouts.needLayout(true);

        viewsHolder.getAvatarContainer().setVisibility(View.VISIBLE);
        viewsHolder.getBlurImageMask().setVisibility(View.VISIBLE);
        attributesHolder.getNameTextView()[1].setVisibility(View.VISIBLE);
        attributesHolder.getOnlineTextView()[1].setVisibility(View.VISIBLE);
        attributesHolder.getOnlineTextView()[3].setVisibility(View.VISIBLE);

        profileActivity.getActionBar().onSearchFieldVisibilityChanged(rowsHolder.getSearchTransitionProgress() > 0.5f);
        int itemVisibility = rowsHolder.getSearchTransitionProgress() > 0.5f ? View.VISIBLE : View.GONE;
        if (viewsHolder.getOtherItem() != null) {
            viewsHolder.getOtherItem().setVisibility(itemVisibility);
        }
        if (viewsHolder.getQrItem() != null) {
            updateQrItemVisibility(componentsFactory,false);
        }
        viewsHolder.getSearchItem().setVisibility(itemVisibility);

        viewsHolder.getSearchItem().getSearchContainer().setVisibility(rowsHolder.getSearchTransitionProgress() > 0.5f ? View.GONE : View.VISIBLE);
        viewsHolder.getSearchListView().setEmptyView(viewsHolder.getEmptyView());
        viewsHolder.getAvatarContainer().setClickable(false);
        viewsHolder.getBlurImageMask().setClickable(false);

        valueAnimator.addUpdateListener(animation -> {
            rowsHolder.setSearchTransitionProgress((float) valueAnimator.getAnimatedValue());
            float progressHalf = (rowsHolder.getSearchTransitionProgress() - 0.5f) / 0.5f;
            float progressHalfEnd = (0.5f - rowsHolder.getSearchTransitionProgress()) / 0.5f;
            if (progressHalf < 0) {
                progressHalf = 0f;
            }
            if (progressHalfEnd < 0) {
                progressHalfEnd = 0f;
            }

            rowsHolder.setSearchTransitionOffset((int) (-offset * (1f - rowsHolder.getSearchTransitionProgress())));
            viewsHolder.getSearchListView().setTranslationY(offset * rowsHolder.getSearchTransitionProgress());
            viewsHolder.getEmptyView().setTranslationY(offset * rowsHolder.getSearchTransitionProgress());
            listView.getClippedList().setTranslationY(-offset * (1f - rowsHolder.getSearchTransitionProgress()));

            listView.getClippedList().setScaleX(1f - 0.01f * (1f - rowsHolder.getSearchTransitionProgress()));
            listView.getClippedList().setScaleY(1f - 0.01f * (1f - rowsHolder.getSearchTransitionProgress()));
            listView.getClippedList().setAlpha(rowsHolder.getSearchTransitionProgress());
            layouts.needLayout(true);

            listView.getClippedList().setAlpha(progressHalf);

            viewsHolder.getSearchListView().setAlpha(1f - rowsHolder.getSearchTransitionProgress());
            viewsHolder.getSearchListView().setScaleX(1f + 0.05f * rowsHolder.getSearchTransitionProgress());
            viewsHolder.getSearchListView().setScaleY(1f + 0.05f * rowsHolder.getSearchTransitionProgress());
            viewsHolder.getEmptyView().setAlpha(1f - progressHalf);

            viewsHolder.getAvatarContainer().setAlpha(progressHalf);
            if (viewsHolder.getStoryView() != null) {
                viewsHolder.getStoryView().setAlpha(progressHalf);
            }
            attributesHolder.getNameTextView()[1].setAlpha(progressHalf);
            attributesHolder.getOnlineTextView()[1].setAlpha(progressHalf);
            attributesHolder.getOnlineTextView()[3].setAlpha(progressHalf);

            viewsHolder.getSearchItem().getSearchField().setAlpha(progressHalfEnd);
            if (enter && rowsHolder.getSearchTransitionProgress() < 0.7f) {
                viewsHolder.getSearchItem().requestFocusOnSearchView();
            }

            viewsHolder.getSearchItem().getSearchContainer().setVisibility(rowsHolder.getSearchTransitionProgress() < 0.5f ? View.VISIBLE : View.GONE);
            int visibility = rowsHolder.getSearchTransitionProgress() > 0.5f ? View.VISIBLE : View.GONE;
            if (viewsHolder.getOtherItem() != null) {
                viewsHolder.getOtherItem().setVisibility(visibility);
                viewsHolder.getOtherItem().setAlpha(progressHalf);
            }
            if (viewsHolder.getQrItem() != null) {
                viewsHolder.getQrItem().setAlpha(progressHalf);
                updateQrItemVisibility(componentsFactory,false);
            }
            viewsHolder.getSearchItem().setVisibility(visibility);

            profileActivity.getActionBar().onSearchFieldVisibilityChanged(rowsHolder.getSearchTransitionProgress() < 0.5f);

            if (viewsHolder.getOtherItem() != null) {
                viewsHolder.getOtherItem().setAlpha(progressHalf);
            }
            if (viewsHolder.getQrItem() != null) {
                viewsHolder.getQrItem().setAlpha(progressHalf);
            }
            viewsHolder.getSearchItem().setAlpha(progressHalf);
            viewsHolder.getTopView().invalidate();
            profileActivity.fragmentView.invalidate();
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                updateSearchViewState(enter);
                viewsHolder.getAvatarContainer().setClickable(true);
                if (enter) {
                    viewsHolder.getSearchItem().requestFocusOnSearchView();
                }
                layouts.needLayout(true);
                attributesHolder.setSearchViewTransition(null);
                profileActivity.fragmentView.invalidate();

                if (enter) {
                    rowsHolder.setInvalidateScroll(true);
                    profileAdapter.saveScrollPosition();
                    AndroidUtilities.requestAdjustResize(profileActivity.getParentActivity(), profileActivity.getClassGuid());
                    viewsHolder.getEmptyView().setPreventMoving(false);
                }
            }
        });

        if (!enter) {
            rowsHolder.setInvalidateScroll(true);
            profileAdapter.saveScrollPosition();
            AndroidUtilities.requestAdjustNothing(profileActivity.getParentActivity(), profileActivity.getClassGuid());
            viewsHolder.getEmptyView().setPreventMoving(true);
        }

        valueAnimator.setDuration(220);
        valueAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        attributesHolder.setSearchViewTransition(valueAnimator);
        return valueAnimator;
    }

    public void setAvatarExpandProgress(final BaseFragment baseFragment, float photoDescriptionProgress, float customAvatarProgress, float animatedFracture) {
        checkForViewsHolder();
        checkForColorUtils();
        checkForRowsHolder();
        checkForAttributesHolder();
        final int newTop = ActionBar.getCurrentActionBarHeight() + (baseFragment.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        final float value = rowsHolder.setCurrentExpandAnimatorValueAndReturn(AndroidUtilities.lerp(attributesHolder.getExpandAnimatorValues(), rowsHolder.setCurrentExpanAnimatorFractureAndReturn(animatedFracture)));
        checkPhotoDescriptionAlpha(componentsFactory, photoDescriptionProgress, customAvatarProgress);
        viewsHolder.getAvatarContainer().setScaleX(rowsHolder.getAvatarScale());
        viewsHolder.getAvatarContainer().setScaleY(rowsHolder.getAvatarScale());

        float buttonsAlpha = AndroidUtilities.lerp(1f, 0f, value);

        float buttonContainerY = rowsHolder.getNameY() + AndroidUtilities.dp(52);
        rowsHolder.setAvatarX((viewsHolder.getAvatarContainer2().getMeasuredWidth() - (viewsHolder.getAvatarContainer().getMeasuredWidth() * rowsHolder.getAvatarScale())) / 2f);
        viewsHolder.getAvatarContainer().setTranslationX(AndroidUtilities.lerp(rowsHolder.getAvatarX(), 0f, value));
        viewsHolder.getAvatarContainer().setTranslationY(AndroidUtilities.lerp((float) Math.ceil(rowsHolder.getAvatarY()), 0f, value));

        viewsHolder.getBlurImageMask().setScaleX(rowsHolder.getAvatarScale());
        viewsHolder.getBlurImageMask().setScaleY(rowsHolder.getAvatarScale());
        viewsHolder.getBlurImageMask().setTranslationX(AndroidUtilities.lerp(rowsHolder.getAvatarX(), 0f, value));
        viewsHolder.getBlurImageMask().setTranslationY(AndroidUtilities.lerp((float) Math.ceil(rowsHolder.getAvatarY()), 0f, value));

        viewsHolder.getAvatarContainer().setTranslationX(AndroidUtilities.lerp(rowsHolder.getAvatarX(), 0f, value));
        viewsHolder.getAvatarContainer().setTranslationY(AndroidUtilities.lerp((float) Math.ceil(rowsHolder.getAvatarY()), 0f, value));
        attributesHolder.getAvatarImage().setRoundRadius((int) AndroidUtilities.lerp(componentsFactory.getLayouts().getSmallAvatarRoundRadius(profileActivity), 0f, value));
        if (viewsHolder.getStoryView() != null) {
            viewsHolder.getStoryView().setExpandProgress(value);
        }

        if (viewsHolder.getButtonsGroup() != null) {
            viewsHolder.getButtonsGroup().setOpenProgress(value);
        }
        if (componentsFactory.getViewComponentsHolder().getGiftsView() != null) {
            componentsFactory.getViewComponentsHolder().getGiftsView().setExpandProgress(value);
        }
        if (viewsHolder.getSearchItem() != null) {
            viewsHolder.getSearchItem().setAlpha(1.0f - value);
            viewsHolder.getSearchItem().setScaleY(1.0f - value);
            viewsHolder.getSearchItem().setVisibility(View.VISIBLE);
            viewsHolder.getSearchItem().setClickable(viewsHolder.getSearchItem().getAlpha() > .5f);
            if (viewsHolder.getQrItem() != null) {
                float translation = AndroidUtilities.dp(48) * value;
//                    if (viewsHolder.getSearchItem().getVisibility() == View.VISIBLE)
//                        translation += AndroidUtilities.dp(48);
                viewsHolder.getQrItem().setTranslationX(translation);
                viewsHolder.getAvatarsViewPagerIndicatorView().setTranslationX(translation - AndroidUtilities.dp(48));
            }
        }

        if (rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) && rowsHolder.getExpandProgress() < 0.33f) {
            layouts.refreshNameAndOnlineXY();
            layouts.refreshAvatar();
        }

        if (attributesHolder.getScamDrawable() != null) {
            attributesHolder.getScamDrawable().setColor(ColorUtils.blendARGB(colorsUtils.getThemedColor(Theme.key_avatar_subtitleInProfileBlue), Color.argb(179, 255, 255, 255), value));
        }

        if (attributesHolder.getLockIconDrawable() != null) {
            attributesHolder.getLockIconDrawable().setColorFilter(ColorUtils.blendARGB(colorsUtils.getThemedColor(Theme.key_chat_lockIcon), Color.WHITE, value), PorterDuff.Mode.MULTIPLY);
        }

        if (attributesHolder.getVerifiedCrossfadeDrawable()[0] != null) {
            attributesHolder.getVerifiedCrossfadeDrawable()[0].setProgress(value);
        }
        if (attributesHolder.getVerifiedCrossfadeDrawable()[1] != null) {
            attributesHolder.getVerifiedCrossfadeDrawable()[1].setProgress(value);
        }

        if (attributesHolder.getPremiumCrossfadeDrawable()[0] != null) {
            attributesHolder.getPremiumCrossfadeDrawable()[0].setProgress(value);
        }
        if (attributesHolder.getPremiumCrossfadeDrawable()[1] != null) {
            attributesHolder.getPremiumCrossfadeDrawable()[1].setProgress(value);
        }

        colorsUtils.updateEmojiStatusDrawableColor(value);

        final float k = AndroidUtilities.dpf2(8f);

        final float nameTextViewXEnd = AndroidUtilities.dpf2(18f) - attributesHolder.getNameTextView()[1].getLeft();
        final float nameTextViewYEnd = newTop + rowsHolder.getExtraHeight() - AndroidUtilities.dpf2(38f) - attributesHolder.getNameTextView()[1].getBottom();
        final float nameTextViewCx = k + rowsHolder.getNameX() + (nameTextViewXEnd - rowsHolder.getNameX()) / 2f;
        final float nameTextViewCy = k + rowsHolder.getNameY() + (nameTextViewYEnd - rowsHolder.getNameY()) / 2f;
        final float nameTextViewX = (1 - value) * (1 - value) * rowsHolder.getNameX() + 2 * (1 - value) * value * nameTextViewCx + value * value * nameTextViewXEnd;
        final float nameTextViewY = (1 - value) * (1 - value) * rowsHolder.getNameY() + 2 * (1 - value) * value * nameTextViewCy + value * value * nameTextViewYEnd;

        final float onlineTextViewXEnd = AndroidUtilities.dpf2(16f) - attributesHolder.getOnlineTextView()[1].getLeft();
        final float onlineTextViewYEnd = newTop + rowsHolder.getExtraHeight() - AndroidUtilities.dpf2(18f) - attributesHolder.getOnlineTextView()[1].getBottom();
        final float onlineTextViewCx = k + rowsHolder.getOnlineX() + (onlineTextViewXEnd - rowsHolder.getOnlineX()) / 2f;
        final float onlineTextViewCy = k + rowsHolder.getOnlineY() + (onlineTextViewYEnd - rowsHolder.getOnlineY()) / 2f;
        final float onlineTextViewX = (1 - value) * (1 - value) * rowsHolder.getOnlineX() + 2 * (1 - value) * value * onlineTextViewCx + value * value * onlineTextViewXEnd;
        final float onlineTextViewY = (1 - value) * (1 - value) * rowsHolder.getOnlineY() + 2 * (1 - value) * value * onlineTextViewCy + value * value * onlineTextViewYEnd;

        float currentNameX = getCurrentNameX();
        float newNameX = AndroidUtilities.lerp(currentNameX, AndroidUtilities.dp(18), value);

        float onlineScale = attributesHolder.getOnlineTextView()[1].getScaleY();
        float currentOnlineX = ((viewsHolder.getAvatarContainer2().getMeasuredWidth() / 2f - (attributesHolder.getOnlineTextView()[1].getTextWidth() * onlineScale) / 2)) - (attributesHolder.getOnlineTextView()[1].getPaddingLeft() * onlineScale);
        float newOnlineX = AndroidUtilities.lerp(currentOnlineX, AndroidUtilities.dp(16), value);


        attributesHolder.getNameTextView()[1].setTranslationX(newNameX);
        attributesHolder.getNameTextView()[1].setTranslationY(nameTextViewY);
        attributesHolder.getOnlineTextView()[1].setTranslationX(newOnlineX);
        attributesHolder.getOnlineTextView()[1].setTranslationY(onlineTextViewY);
        attributesHolder.getMediaCounterTextView().setTranslationX(newOnlineX);
        attributesHolder.getMediaCounterTextView().setTranslationY(onlineTextViewY);
        final Object onlineTextViewTag = attributesHolder.getOnlineTextView()[1].getTag();
        int statusColor;
        boolean online = false;
        if (onlineTextViewTag instanceof Integer) {
            statusColor = colorsUtils.getThemedColor((Integer) onlineTextViewTag);
            online = (Integer) onlineTextViewTag == Theme.key_profile_status;
        } else {
            statusColor = colorsUtils.getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
        }
        attributesHolder.getOnlineTextView()[1].setTextColor(ColorUtils.blendARGB(colorsUtils.applyPeerColor(statusColor, true, online), 0xB3FFFFFF, value));
        if (rowsHolder.getExtraHeight() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) {
            attributesHolder.getNameTextView()[1].setPivotY(AndroidUtilities.lerp(0, attributesHolder.getNameTextView()[1].getMeasuredHeight(), value));
            attributesHolder.getNameTextView()[1].setScaleX(AndroidUtilities.lerp(1.12f, 1.67f, value));
            attributesHolder.getNameTextView()[1].setScaleY(AndroidUtilities.lerp(1.12f, 1.67f, value));
        }
        if (viewsHolder.getShowStatusButton() != null) {
            viewsHolder.getShowStatusButton().setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(rowsHolder.getActionBarBackgroundColor(), +0.18f, -0.1f), 0.5f), 0x23ffffff, rowsHolder.getCurrentExpandAnimatorValue()));
        }

        componentsFactory.getLayouts().needLayoutText(Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)));

        attributesHolder.getNameTextView()[1].setTextColor(ColorUtils.blendARGB(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_profile_title), Color.WHITE, rowsHolder.getCurrentExpandAnimatorValue()));
        baseFragment.getActionBar().setItemsColor(ColorUtils.blendARGB(attributesHolder.getPeerColor() != null ? Color.WHITE : colorsUtils.getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE, value), false);
        baseFragment.getActionBar().setMenuOffsetSuppressed(true);

        attributesHolder.getAvatarImage().setForegroundAlpha(value);

        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewsHolder.getAvatarContainer().getLayoutParams();
        params.width = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(ProfileParams.AVATAR_DIMENSION), listView.getClippedList().getMeasuredWidth() / rowsHolder.getAvatarScale(), value);
        params.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(ProfileParams.AVATAR_DIMENSION), (rowsHolder.getExtraHeight() + newTop) / rowsHolder.getAvatarScale(), value);
        params.leftMargin = (int) AndroidUtilities.lerp(0, 0f, value);
        viewsHolder.getAvatarContainer().requestLayout();

        updateCollectibleHint(componentsFactory);
    }

    private float getCurrentNameX() {
        float nameScale = attributesHolder.getNameTextView()[1].getScaleY();
        float currentNameX = ((viewsHolder.getAvatarContainer2().getMeasuredWidth() / 2f - (attributesHolder.getNameTextView()[1].getTextWidth() * nameScale) / 2)) - (attributesHolder.getNameTextView()[1].getPaddingLeft() * nameScale);
        if (attributesHolder.getNameTextView()[1].getRightDrawable() != null) {
            currentNameX = currentNameX - ((attributesHolder.getNameTextView()[1].getRightDrawable().getMinimumWidth() * nameScale) / 2);
        }
        return currentNameX;
    }

    private void checkForAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = componentsFactory.getAttributesComponentsHolder();
        }
    }

    private void checkForColorUtils() {
        colorsUtils = componentsFactory.getColorsUtils();
    }

    public final Property<ProfileActivity, Float> HEADER_SHADOW = new AnimationProperties.FloatProperty<ProfileActivity>("headerShadow") {
        @Override
        public void setValue(ProfileActivity object, float value) {
            ProfileParams.headerShadowAlpha = value;
            viewsHolder.getTopView().invalidate();
        }

        @Override
        public Float get(ProfileActivity object) {
            return ProfileParams.headerShadowAlpha;
        }
    };
    public void checkListViewScroll() {
        checkForViewsHolder();
        checkForRowsHolder();
        checkForAttributesHolder();
        if (listView == null) {
            listView = componentsFactory.getListView();
        }
        if (listView.getClippedList().getVisibility() != View.VISIBLE) {
            return;
        }
        if (rowsHolder.isSharedMediaLayoutAttached()) {
            viewsHolder.getSharedMediaLayout().setVisibleHeight(listView.getClippedList().getMeasuredHeight() - viewsHolder.getSharedMediaLayout().getTop());
        }

        if (listView.getClippedList().getChildCount() <= 0 || rowsHolder.isOpenAnimationInProgress()) {
            return;
        }

        int newOffset = 0;
        View child = null;
        for (int i = 0; i < listView.getClippedList().getChildCount(); i++) {
            if (listView.getClippedList().getChildAdapterPosition(listView.getClippedList().getChildAt(i)) == 0) {
                child = listView.getClippedList().getChildAt(i);
                break;
            }
        }
        if (child != null) {
            attributesHolder.setPullUpProgress(AndroidUtilities.lerp(1f, MathUtils.clamp(1f - (float) child.getTop() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT), 0, 1), rowsHolder.getSearchTransitionProgress()), componentsFactory);
        } else {
            attributesHolder.setPullUpProgress(1f, componentsFactory);
        }
        RecyclerListView.Holder holder = child == null ? null : (RecyclerListView.Holder) listView.getClippedList().findContainingViewHolder(child);
        int top = child == null ? 0 : child.getTop();
        int adapterPosition = holder != null ? holder.getAdapterPosition() : RecyclerView.NO_POSITION;
        if (top >= 0 && adapterPosition == 0) {
            newOffset = top;
        }
        boolean mediaHeaderVisible;
        boolean searchVisible = viewsHolder.getImageUpdater() == null && profileActivity.getActionBar().isSearchFieldVisible();
        if (rowsHolder.getSharedMediaRow() != -1 && !searchVisible) {
            holder = (RecyclerListView.Holder) listView.getClippedList().findViewHolderForAdapterPosition(rowsHolder.getSharedMediaRow());
            mediaHeaderVisible = holder != null && holder.itemView.getTop() <= 0;
        } else {
            mediaHeaderVisible = searchVisible;
        }
        setMediaHeaderVisible(mediaHeaderVisible);

        if (rowsHolder.getExtraHeight() != newOffset && !rowsHolder.isTransitionAnimationInProress()) {
            rowsHolder.setExtraHeight(newOffset);
            viewsHolder.getTopView().invalidate();
            if (rowsHolder.getPlayProfileAnimation() != 0) {
                rowsHolder.setAllowProfileAnimation(rowsHolder.getExtraHeight() != 0);
            }
            if (layouts == null) {
                layouts = componentsFactory.getLayouts();
            }
            layouts.needLayout(true);
        }
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        }
    }

    public void setGroupButtonTransitionProgress(float progress) {
        checkForAttributesHolder();
        checkForViewsHolder();
        attributesHolder.setGroupButtonTransitionProgress(progress);
        if (viewsHolder.getButtonsGroup() != null) {
            viewsHolder.getButtonsGroup().setExpandProgress((1f - attributesHolder.getPullUpProgress() * attributesHolder.getGroupButtonTransitionProgress()));
        }
        componentsFactory.getLayouts().needLayout(false);
    }
    private void checkForViewsHolder() {
        if (viewsHolder == null) {
            viewsHolder = componentsFactory.getViewComponentsHolder();
        }
    }
}
