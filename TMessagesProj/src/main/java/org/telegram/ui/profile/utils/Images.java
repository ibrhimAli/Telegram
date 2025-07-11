package org.telegram.ui.profile.utils;

import android.graphics.drawable.Drawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

public class Images {

    public static void setForegroundImage(ComponentsFactory componentsFactory, boolean secondParent) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        Drawable drawable = attributesHolder.getAvatarImage().getImageReceiver().getDrawable();
        if (drawable instanceof VectorAvatarThumbDrawable) {
            attributesHolder.getAvatarImage().setForegroundImage(null, null, drawable);
        } else if (drawable instanceof AnimatedFileDrawable) {
            AnimatedFileDrawable fileDrawable = (AnimatedFileDrawable) drawable;
            attributesHolder.getAvatarImage().setForegroundImage(null, null, fileDrawable);
            if (secondParent) {
                fileDrawable.addSecondParentView(attributesHolder.getAvatarImage());
            }
        } else {
            ImageLocation location = attributesHolder.getAvatarsViewPager().getImageLocation(0);
            String filter;
            if (location != null && location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = "avatar";
            } else {
                filter = null;
            }
            attributesHolder.getAvatarImage().setForegroundImage(location, filter, drawable);
        }
    }

    public static void checkPhotoDescriptionAlpha(ComponentsFactory componentsFactory, float photoDescriptionProgress, float customAvatarProgress) {
        ViewComponentsHolder viewsHolder = componentsFactory.getViewComponentsHolder();
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        float p = photoDescriptionProgress;
        if (rowsHolder.getPlayProfileAnimation() == 1 && (!rowsHolder.isFragmentOpened() || rowsHolder.isOpenAnimationInProgress())) {
            photoDescriptionProgress = 0;
        } else if (rowsHolder.getPlayProfileAnimation() == 2 && (!rowsHolder.isFragmentOpened() || rowsHolder.isOpenAnimationInProgress())) {
            photoDescriptionProgress = attributesHolder.getOnlineTextView()[1] == null ? 0 : attributesHolder.getOnlineTextView()[1].getAlpha();
        } else {
            if (attributesHolder.getUserId() == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
                photoDescriptionProgress = rowsHolder.getCurrentExpandAnimatorValue() * (1f - customAvatarProgress);
            } else {
                photoDescriptionProgress = rowsHolder.getCurrentExpandAnimatorValue() * customAvatarProgress;
            }
        }
//        if (p == photoDescriptionProgress) {
//            return;
//        }
        if (attributesHolder.getUserId() == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
            if (rowsHolder.isHasFallbackPhoto()) {
                rowsHolder.setCustomPhotoOffset(AndroidUtilities.dp(28) * photoDescriptionProgress);
                if (attributesHolder.getOnlineTextView()[2] != null) {
                    attributesHolder.getOnlineTextView()[2].setAlpha(rowsHolder.getCurrentExpandAnimatorValue());
                    attributesHolder.getOnlineTextView()[3].setAlpha(1f - rowsHolder.getCurrentExpandAnimatorValue());
                    //  attributesHolder.getOnlineTextView()[1].setAlpha(1f - ProfileParams.expandProgress);
                    attributesHolder.getOnlineTextView()[1].setTranslationX(rowsHolder.getOnlineX() + rowsHolder.getCustomPhotoOffset());
                    viewsHolder.getAvatarContainer2().invalidate();
                    if (viewsHolder.getShowStatusButton() != null) {
                        viewsHolder.getShowStatusButton().setAlpha2(1f - rowsHolder.getCurrentExpandAnimatorValue());
                    }
                }
            } else {
                if (attributesHolder.getOnlineTextView()[2] != null) {
                    attributesHolder.getOnlineTextView()[2].setAlpha(0);
                    attributesHolder.getOnlineTextView()[3].setAlpha(0);
                }
                if (viewsHolder.getShowStatusButton() != null) {
                    viewsHolder.getShowStatusButton().setAlpha2(1f);
                }
            }

        } else {
            if (rowsHolder.isHasCustomPhoto()) {
                if (attributesHolder.getOnlineTextView()[2] != null) {
                    attributesHolder.getOnlineTextView()[2].setAlpha(photoDescriptionProgress);
                }
                if (viewsHolder.getShowStatusButton() != null) {
                    viewsHolder.getShowStatusButton().setAlpha2(1f - photoDescriptionProgress);
                }
            } else {
                if (attributesHolder.getOnlineTextView()[2] != null) {
                    attributesHolder.getOnlineTextView()[2].setAlpha(0);
                }
                if (viewsHolder.getShowStatusButton() != null) {
                    viewsHolder.getShowStatusButton().setAlpha2(1f);
                }
            }
        }
    }
}
