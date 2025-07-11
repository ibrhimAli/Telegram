package org.telegram.ui.profile.utils;

import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

public class Views {

    public static void updateEmojiStatusEffectPosition(ComponentsFactory componentsFactory) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        attributesHolder.getAnimatedStatusView().setScaleX(attributesHolder.getNameTextView()[1].getScaleX());
        attributesHolder.getAnimatedStatusView().setScaleY(attributesHolder.getNameTextView()[1].getScaleY());
        attributesHolder.getAnimatedStatusView().translate(
                attributesHolder.getNameTextView()[1].getX() + attributesHolder.getNameTextView()[1].getRightDrawableX() * attributesHolder.getNameTextView()[1].getScaleX(),
                attributesHolder.getNameTextView()[1].getY() + (attributesHolder.getNameTextView()[1].getHeight() - (attributesHolder.getNameTextView()[1].getHeight() - attributesHolder.getNameTextView()[1].getRightDrawableY()) * attributesHolder.getNameTextView()[1].getScaleY())
        );
    }

    public static void updateStoriesViewBounds(ViewComponentsHolder viewsHolder, ProfileActivity profileActivity, ViewComponentsHolder viewComponents, boolean animated) {
        if (viewsHolder.getStoryView() == null && viewComponents.getGiftsView() == null || profileActivity.getActionBar() == null) {
            return;
        }
        float atop = profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
        float aleft = 0;
        float aright = profileActivity.getActionBar().getWidth();

        if (profileActivity.getActionBar().getBackButton() != null) {
            aleft = Math.max(aleft, profileActivity.getActionBar().getBackButton().getRight());
        }
        if (profileActivity.getActionBar().menu != null) {
            for (int i = 0; i < profileActivity.getActionBar().menu.getChildCount(); ++i) {
                View child = profileActivity.getActionBar().menu.getChildAt(i);
                if (child.getAlpha() <= 0 || child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int left = profileActivity.getActionBar().menu.getLeft() + (int) child.getX();
                if (left < aright) {
                    aright = AndroidUtilities.lerp(aright, left, child.getAlpha());
                }
            }
        }
        if (viewComponents.getStoryView() != null) {
            viewComponents.getStoryView().setBounds(aleft, aright, atop + (profileActivity.getActionBar().getHeight() - atop) / 2f, !animated);
        }
        if (viewComponents.getGiftsView() != null) {
            viewComponents.getGiftsView().setBounds(aleft, aright, atop + (profileActivity.getActionBar().getHeight() - atop) / 2f, !animated);
        }
    }
}
