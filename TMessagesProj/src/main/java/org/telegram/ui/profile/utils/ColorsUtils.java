package org.telegram.ui.profile.utils;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.OKLCH;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.drawable.ShowDrawable;
import org.telegram.ui.profile.view.ClippedListView;

public class ColorsUtils {

    private ClippedListView listView;
    private final ComponentsFactory components;
    private ViewComponentsHolder viewsHodler;
    private AttributesComponentsHolder attributesHolder;
    private ProfileActivity profileActivity;
    private RowsAndStatusComponentsHolder rowsHolder;
    public ColorsUtils(ComponentsFactory components) {
        this.components = components;
        this.listView = components.getListView();
        viewsHodler = components.getViewComponentsHolder();
        attributesHolder = components.getAttributesComponentsHolder();
        profileActivity = components.getProfileActivity();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
    }
    public static int dontApplyPeerColor(int color) {
        return dontApplyPeerColor(color, true, null);
    }

    public static int dontApplyPeerColor(int color, boolean actionBar) {
        return dontApplyPeerColor(color, actionBar, null);
    }

    public static int dontApplyPeerColor(int color, boolean actionBar, Boolean online) {
        return color;
    }


    public static int getThemedColor(int key, Theme.ResourcesProvider resourcesProvider) {
        return Theme.getColor(key, resourcesProvider);
    }

    public int getThemedColor(int key) {
        validateAttributesHolder();
        return Theme.getColor(key, attributesHolder.getResourcesProvider());
    }

    public void updateEmojiStatusDrawableColor() {
        validateRowsHolder();
        updateEmojiStatusDrawableColor(rowsHolder.getLastEmojiStatusProgress());
    }

    public void updateFloatingButtonColor(ProfileActivity profileActivity) {
        validateViewsHolder();
        if (profileActivity.getParentActivity() == null) {
            return;
        }
        Drawable drawable;
        if (viewsHodler.getFloatingButtonContainer() != null) {
            drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), dontApplyPeerColor(Theme.getColor(Theme.key_chats_actionBackground), false), dontApplyPeerColor(Theme.getColor(Theme.key_chats_actionPressedBackground), false));
            if (Build.VERSION.SDK_INT < 21) {
                Drawable shadowDrawable = ContextCompat.getDrawable(profileActivity.getParentActivity(), R.drawable.floating_shadow).mutate();
                shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
                CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
                combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                drawable = combinedDrawable;
            }
            viewsHodler.getFloatingButtonContainer().setBackground(drawable);
        }
    }

    private void validateViewsHolder() {
        if (viewsHodler == null) {
            viewsHodler = components.getViewComponentsHolder();
        }
    }

    public void updateEmojiStatusDrawableColor(float progress) {
        validateRowsHolder();
        for (int a = 0; a < 2; ++a) {
            final int fromColor;
            if (attributesHolder.getPeerColor() != null && a == 1) {
                fromColor = ColorUtils.blendARGB(attributesHolder.getPeerColor().getStoryColor1(Theme.isCurrentThemeDark()), 0xFFFFFFFF, 0.25f);
            } else {
                fromColor = AndroidUtilities.getOffsetColor(getThemedColor(Theme.key_profile_verifiedBackground), getThemedColor(Theme.key_player_actionBarTitle), rowsHolder.getMediaHeaderAnimationProgress(), 1.0f);
            }
            final int color = ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0xffffffff, progress), getThemedColor(Theme.key_player_actionBarTitle), rowsHolder.getMediaHeaderAnimationProgress());
            if (attributesHolder.getEmojiStatusDrawable()[a] != null) {
                attributesHolder.getEmojiStatusDrawable()[a].setColor(color);
            }
            if (attributesHolder.getBotVerificationDrawable()[a] != null) {
                attributesHolder.getBotVerificationDrawable()[a].setColor(ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0x99ffffff, progress), getThemedColor(Theme.key_player_actionBarTitle), rowsHolder.getMediaHeaderAnimationProgress()));
            }
            if (a == 1) {
                attributesHolder.getAnimatedStatusView().setColor(color);
            }
        }
        rowsHolder.setLastEmojiStatusProgress(progress);
    }

    private void validateRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }

    private int applyPeerColor2(int color) {
        if (attributesHolder.getPeerColor() != null) {
            int accentColor = attributesHolder.getPeerColor().getBgColor2(Theme.isCurrentThemeDark());
            return Theme.changeColorAccent(getThemedColor(Theme.key_windowBackgroundWhiteBlueIcon), accentColor, color, Theme.isCurrentThemeDark(), accentColor);
        }
        return color;
    }

    public ShowDrawable getShowStatusButton() {
        if (viewsHodler.getShowStatusButton() == null) {
            viewsHodler.setShowStatusButton(new ShowDrawable(LocaleController.getString(R.string.StatusHiddenShow)));
            viewsHodler.getShowStatusButton().setAlpha((int) (0xFF * Math.min(1f, rowsHolder.getExtraHeight() / AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT))));
            viewsHodler.getShowStatusButton().setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(rowsHolder.getActionBarBackgroundColor(), +0.18f, -0.1f), 0.5f), 0x23ffffff, rowsHolder.getCurrentExpandAnimatorValue()));
        }
        return viewsHodler.getShowStatusButton();
    }

    public void writeButtonSetBackground(ProfileActivity profileActivity) {
        if (attributesHolder.getWriteButton() == null) return;
        try {
            Drawable shadowDrawable = profileActivity.fragmentView.getContext().getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
            int color1 = getThemedColor(Theme.key_profile_actionBackground);
            int color2 = getThemedColor(Theme.key_profile_actionPressedBackground);
            int iconColor = getThemedColor(Theme.key_profile_actionIcon);
            if (attributesHolder.getPeerColor() != null && Theme.hasHue(color1)) {
                color1 = Theme.adaptHSV(attributesHolder.getPeerColor().getBgColor1(false), +.05f, -.04f);
                color2 = applyPeerColor2(color2);
                iconColor = Color.WHITE;
            }
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable,
                    Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), color1, color2),
                    0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            attributesHolder.getWriteButton().setBackground(combinedDrawable);
            attributesHolder.getWriteButton().setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
        } catch (Exception e) {}
    }


    public int applyPeerColor(int color, boolean actionBar, Boolean online) {
        if (!actionBar && profileActivity.isSettings()) return color;
        if (attributesHolder.getPeerColor() != null) {
            if (!actionBar) {
                int index = attributesHolder.getAdaptedColors().indexOfKey(color);
                if (index < 0) {
                    final int baseColor = Theme.adaptHSV(attributesHolder.getPeerColor().getBgColor1(Theme.isCurrentThemeDark()), Theme.isCurrentThemeDark() ? 0 : +.05f, Theme.isCurrentThemeDark() ? -.1f : -.04f);
                    int adapted = OKLCH.adapt(color, baseColor);
                    attributesHolder.getAdaptedColors().put(color, adapted);
                    return adapted;
                } else {
                    return attributesHolder.getAdaptedColors().valueAt(index);
                }
            }
            final int baseColor = getThemedColor(actionBar ? Theme.key_actionBarDefault : Theme.key_windowBackgroundWhiteBlueIcon);
            final int storyColor = ColorUtils.blendARGB(attributesHolder.getPeerColor().getStoryColor1(Theme.isCurrentThemeDark()), attributesHolder.getPeerColor().getStoryColor2(Theme.isCurrentThemeDark()), .5f);
            int accentColor = actionBar ? storyColor : attributesHolder.getPeerColor().getBgColor1(Theme.isCurrentThemeDark());
            if (!Theme.hasHue(baseColor)) {
                return online != null && !online ? Theme.adaptHSV(Theme.multAlpha(storyColor, .7f), -.2f, +.2f) : storyColor;
            }
            return Theme.changeColorAccent(baseColor, accentColor, color, Theme.isCurrentThemeDark(), online != null && !online ? Theme.multAlpha(storyColor, .7f) : storyColor);
        }
        return color;
    }

    public void updatedPeerColor(ProfileActivity profileActivity) {
        attributesHolder.getAdaptedColors().clear();
        if (listView == null) {
            listView = components.getListView();
        }
        if (viewsHodler.getTopView() != null) {
            viewsHodler.getTopView().setBackgroundColorId(attributesHolder.getPeerColor(), true);
        }
        if (attributesHolder.getOnlineTextView()[1] != null) {
            int statusColor;
            if (attributesHolder.getOnlineTextView()[1].getTag() instanceof Integer) {
                statusColor = getThemedColor((Integer) attributesHolder.getOnlineTextView()[1].getTag());
            } else {
                statusColor = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
            }
            attributesHolder.getOnlineTextView()[1].setTextColor(ColorUtils.blendARGB(applyPeerColor(statusColor, true, attributesHolder.getIsOnline()[0]), 0xB3FFFFFF, rowsHolder.getCurrentExpandAnimatorValue()));
        }
        if (viewsHodler.getShowStatusButton() != null) {
            viewsHodler.getShowStatusButton().setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(rowsHolder.getActionBarBackgroundColor(), +0.18f, -0.1f), 0.5f), 0x23ffffff, rowsHolder.getCurrentExpandAnimatorValue()));
        }
        if (profileActivity.getActionBar() != null) {
            profileActivity.getActionBar().setItemsColor(ColorUtils.blendARGB(attributesHolder.getPeerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), getThemedColor(Theme.key_actionBarActionModeDefaultIcon), rowsHolder.getMediaHeaderAnimationProgress()), false);
            profileActivity.getActionBar().setItemsBackgroundColor(ColorUtils.blendARGB(attributesHolder.getPeerColor() != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : attributesHolder.getPeerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), getThemedColor(Theme.key_actionBarActionModeDefaultSelector), rowsHolder.getMediaHeaderAnimationProgress()), false);
        }
        if (attributesHolder.getVerifiedDrawable()[1] != null) {
            final int color1 = attributesHolder.getPeerColor() != null ? Theme.adaptHSV(ColorUtils.blendARGB(attributesHolder.getPeerColor().getColor2(), attributesHolder.getPeerColor().hasColor6(Theme.isCurrentThemeDark()) ? attributesHolder.getPeerColor().getColor5() : attributesHolder.getPeerColor().getColor3(), .4f), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f) : getThemedColor(Theme.key_profile_verifiedBackground);
            final int color2 = getThemedColor(Theme.key_player_actionBarTitle);
            attributesHolder.getVerifiedDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, rowsHolder.getMediaHeaderAnimationProgress(), 1.0f), PorterDuff.Mode.MULTIPLY);
        }
        if (attributesHolder.getVerifiedCheckDrawable()[1] != null) {
            final int color1 = attributesHolder.getPeerColor() != null ? Color.WHITE : dontApplyPeerColor(getThemedColor(Theme.key_profile_verifiedCheck));
            final int color2 = getThemedColor(Theme.key_windowBackgroundWhite);
            attributesHolder.getVerifiedCheckDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, rowsHolder.getMediaHeaderAnimationProgress(), 1.0f), PorterDuff.Mode.MULTIPLY);
        }
        if (attributesHolder.getNameTextView()[1] != null) {
            attributesHolder.getNameTextView()[1].setTextColor(ColorUtils.blendARGB(ColorUtils.blendARGB(attributesHolder.getPeerColor() != null ? Color.WHITE : getThemedColor(Theme.key_profile_title), getThemedColor(Theme.key_player_actionBarTitle), rowsHolder.getMediaHeaderAnimationProgress()), Color.WHITE, rowsHolder.getCurrentExpandAnimatorValue()));
        }
        if (viewsHodler.getAutoDeletePopupWrapper() != null && viewsHodler.getAutoDeletePopupWrapper().textView != null) {
            viewsHodler.getAutoDeletePopupWrapper().textView.invalidate();
        }
        AndroidUtilities.forEachViews(listView.getClippedList(), view -> {
            if (view instanceof HeaderCell) {
                ((HeaderCell) view).setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), false));
            } else if (view instanceof TextDetailCell) {
                ((TextDetailCell) view).updateColors();
            } else if (view instanceof TextCell) {
                ((TextCell) view).updateColors();
            } else if (view instanceof AboutLinkCell) {
                ((AboutLinkCell) view).updateColors();
            } else if (view instanceof NotificationsCheckCell) {
                ((NotificationsCheckCell) view).getCheckBox().invalidate();
            } else if (view instanceof ProfileHoursCell) {
                ((ProfileHoursCell) view).updateColors();
            } else if (view instanceof ProfileChannelCell) {
                ((ProfileChannelCell) view).updateColors();
            }
        });
        if (viewsHodler.getSharedMediaLayout() != null && viewsHodler.getSharedMediaLayout().scrollSlidingTextTabStrip != null) {
            viewsHodler.getSharedMediaLayout().scrollSlidingTextTabStrip.updateColors();
        }
        if (viewsHodler.getSharedMediaLayout() != null && viewsHodler.getSharedMediaLayout().giftsContainer != null) {
            viewsHodler.getSharedMediaLayout().giftsContainer.updateColors();
        }
        writeButtonSetBackground(profileActivity);
        updateEmojiStatusDrawableColor();
        if (viewsHodler.getStoryView() != null) {
            viewsHodler.getStoryView().update();
        }
        if (components.getViewComponentsHolder().getGiftsView() != null) {
            components.getViewComponentsHolder().getGiftsView().update();
        }
    }

    public void updateEditColorIcon(ProfileActivity profileActivity) {
        validateViewsHolder();
        validateAttributesHolder();
        if (profileActivity.getContext() == null || viewsHodler.getEditColorItem() == null) return;
        if (profileActivity.getUserConfig().isPremium()) {
            viewsHodler.getEditColorItem().setIcon(R.drawable.menu_profile_colors);
        } else {
            Drawable icon = ContextCompat.getDrawable(profileActivity.getContext(), R.drawable.menu_profile_colors_locked);
            icon.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultSubmenuItemIcon), PorterDuff.Mode.SRC_IN));
            Drawable lockIcon = ContextCompat.getDrawable(profileActivity.getContext(), R.drawable.msg_gallery_locked2);
            lockIcon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.5f), PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(icon, lockIcon, dp(1), -dp(1)) {
                @Override
                public void setColorFilter(ColorFilter colorFilter) {}
            };
            viewsHodler.getEditColorItem().setIcon(combinedDrawable);
        }
    }

    private void validateAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = components.getAttributesComponentsHolder();
        }
    }
}
