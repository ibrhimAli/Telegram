package org.telegram.ui.profile.utils;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.profile.utils.Hints.updateCollectibleHint;

import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stories.recorder.HintView2;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

public class Gifts {

    public static void setCollectibleGiftStatus(ComponentsFactory componentsFactory, ProfileActivity profileActivity, TLRPC.TL_emojiStatusCollectible status) {
        ViewComponentsHolder viewComponentsHolder = componentsFactory.getViewComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        AttributesComponentsHolder attributes = componentsFactory.getAttributesComponentsHolder();

        if (viewComponentsHolder.getAvatarContainer2() == null) return;
        if (attributes.getCollectibleStatus() == status) return;
        if (attributes.getCollectibleStatus() != null && status != null && attributes.getCollectibleStatus().collectible_id == status.collectible_id) return;
        attributes.setCollectibleStatus(status);
        if (attributes.getCollectibleHint() != null) {
            attributes.getCollectibleHint().hide();
        }
        if (status != null && !TextUtils.isEmpty(status.slug)) {
            rowsHolder.setCollectibleHintVisible(null);
            attributes.setCollectibleHint(new HintView2(profileActivity.getContext(), HintView2.DIRECTION_BOTTOM));
            rowsHolder.setCollectibleHintBackgroundColor(Theme.blendOver(status.center_color | 0xFF000000, Theme.multAlpha(status.pattern_color | 0xFF000000, .5f)));
            attributes.getCollectibleHint().setPadding(dp(4), 0, dp(4), dp(2));
            attributes.getCollectibleHint().setFlicker(.66f, Theme.multAlpha(status.text_color | 0xFF000000, 0.5f));
            viewComponentsHolder.getAvatarContainer2().addView(attributes.getCollectibleHint(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 24));
            attributes.getCollectibleHint().setTextSize(9.33f);
            attributes.getCollectibleHint().setTextTypeface(AndroidUtilities.bold());
            attributes.getCollectibleHint().setText(status.title);
            attributes.getCollectibleHint().setDuration(-1);
            attributes.getCollectibleHint().setInnerPadding(4.66f + 1, 2.66f, 4.66f + 1, 2.66f);
            attributes.getCollectibleHint().setArrowSize(4, 2.66f);
            attributes.getCollectibleHint().setRoundingWithCornerEffect(false);
            attributes.getCollectibleHint().setRounding(16);
            attributes.getCollectibleHint().show();
            final String slug = status.slug;
            attributes.getCollectibleHint().setOnClickListener(v -> {
                Browser.openUrl(profileActivity.getContext(), "https://" + profileActivity.getMessagesController().linkPrefix + "/nft/" + slug);
            });
            if (rowsHolder.getExtraHeight() < dp(82)) {
                rowsHolder.setCollectibleHintVisible(false);
                attributes.getCollectibleHint().setAlpha(0.0f);
            }
            updateCollectibleHint(componentsFactory);
            AndroidUtilities.runOnUIThread(attributes.getCollectibleHint()::hide, 6 * 1000);
        }
    }

}
