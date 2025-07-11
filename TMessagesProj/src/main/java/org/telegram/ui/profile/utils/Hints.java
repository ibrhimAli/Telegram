package org.telegram.ui.profile.utils;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;

public class Hints {

    public static void updateCollectibleHint(ComponentsFactory componentsFactory) {
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        AttributesComponentsHolder attributes = componentsFactory.getAttributesComponentsHolder();
        if (attributes.getCollectibleHint() == null) return;
        attributes.getCollectibleHint().setJointPx(0, -attributes.getCollectibleHint().getPaddingLeft() + attributes.getNameTextView()[1].getX() + (attributes.getNameTextView()[1].getRightDrawableX() - attributes.getNameTextView()[1].getRightDrawableWidth() * lerp(0.45f, 0.25f, rowsHolder.getCurrentExpandAnimatorValue())) * attributes.getNameTextView()[1].getScaleX());
        final float expanded = AndroidUtilities.lerp(attributes.getExpandAnimatorValues(), rowsHolder.getCurrentExpanAnimatorFracture());
        attributes.getCollectibleHint().setTranslationY(-attributes.getCollectibleHint().getPaddingBottom() + attributes.getNameTextView()[1].getY() - dp(24) + lerp(dp(6), -dp(12), expanded));
        attributes.getCollectibleHint().setBgColor(ColorUtils.blendARGB(rowsHolder.getCollectibleHintBackgroundColor(), 0x50000000, expanded));
        final boolean visible = rowsHolder.getExtraHeight() >= dp(82);
        if (rowsHolder.getCollectibleHintVisible() == null || rowsHolder.getCollectibleHintVisible() != visible) {
            attributes.getCollectibleHint().animate().alpha((rowsHolder.setCollectibleHintVisibleAndReturn(visible)) ? 1.0f : 0.0f).setInterpolator(CubicBezierInterpolator.EASE_OUT).setDuration(200).start();
        }
    }
}
