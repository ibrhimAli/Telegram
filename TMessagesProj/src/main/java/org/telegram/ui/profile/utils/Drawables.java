package org.telegram.ui.profile.utils;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.profile.utils.ColorsUtils.dontApplyPeerColor;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.ScamDrawable;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;

public class Drawables {

    public static Drawable getLockIconDrawable(ComponentsFactory componentsFactory) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getLockIconDrawable() == null) {
            attributesHolder.setLockIconDrawable(Theme.chat_lockIconDrawable.getConstantState().newDrawable().mutate());
        }
        return attributesHolder.getLockIconDrawable();
    }

    public static Drawable getScamDrawable(ComponentsFactory componentsFactory, int type) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getScamDrawable() == null) {
            attributesHolder.setScamDrawable(new ScamDrawable(11, type));
            attributesHolder.getScamDrawable().setColor(componentsFactory.getColorsUtils().getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
        }
        return attributesHolder.getScamDrawable();
    }

    public static Drawable getVerifiedCrossfadeDrawable(ComponentsFactory componentsFactory, ProfileActivity profileActivity, int a) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getVerifiedCrossfadeDrawable()[a] == null) {
            attributesHolder.getVerifiedDrawable()[a] = Theme.profile_verifiedDrawable.getConstantState().newDrawable().mutate();
            attributesHolder.getVerifiedCheckDrawable()[a] = Theme.profile_verifiedCheckDrawable.getConstantState().newDrawable().mutate();
            if (a == 1 && attributesHolder.getPeerColor() != null) {
                int color = Theme.adaptHSV(attributesHolder.getPeerColor().hasColor6(Theme.isCurrentThemeDark()) ? attributesHolder.getPeerColor().getColor5() : attributesHolder.getPeerColor().getColor3(), +.1f, Theme.isCurrentThemeDark() ? -.1f : -.08f);
                attributesHolder.getVerifiedDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color, componentsFactory.getColorsUtils().getThemedColor(Theme.key_player_actionBarTitle), componentsFactory.getRowsAndStatusComponentsHolder().getMediaHeaderAnimationProgress(), 1.0f), PorterDuff.Mode.MULTIPLY);
                color = Color.WHITE;
                attributesHolder.getVerifiedCheckDrawable()[1].setColorFilter(AndroidUtilities.getOffsetColor(color, componentsFactory.getColorsUtils().getThemedColor(Theme.key_windowBackgroundWhite), componentsFactory.getRowsAndStatusComponentsHolder().getMediaHeaderAnimationProgress(), 1.0f), PorterDuff.Mode.MULTIPLY);
            }
            attributesHolder.getVerifiedCrossfadeDrawable()[a] = new CrossfadeDrawable(
                    new CombinedDrawable(attributesHolder.getVerifiedDrawable()[a], attributesHolder.getVerifiedCheckDrawable()[a]),
                    ContextCompat.getDrawable(profileActivity.getParentActivity(), R.drawable.verified_profile)
            );
        }
        return attributesHolder.getVerifiedCrossfadeDrawable()[a];
    }

    public static Drawable getEmojiStatusDrawable(ComponentsFactory componentsFactory, ProfileActivity profileActivity, TLRPC.EmojiStatus emojiStatus, boolean switchable, boolean animated, int a) {
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getEmojiStatusDrawable()[a] == null) {
            attributesHolder.getEmojiStatusDrawable()[a] = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(attributesHolder.getNameTextView()[a], AndroidUtilities.dp(24), a == 0 ? AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS : AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD);
            if (rowsHolder.isFragmentViewAttached()) {
                attributesHolder.getEmojiStatusDrawable()[a].attach();
            }
        }
        if (a == 1) {
            componentsFactory.getRowsAndStatusComponentsHolder().setEmojiStatusGiftId(null);
        }
        if (emojiStatus instanceof TLRPC.TL_emojiStatus) {
            final TLRPC.TL_emojiStatus status = (TLRPC.TL_emojiStatus) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                attributesHolder.getEmojiStatusDrawable()[a].set(status.document_id, animated);
                attributesHolder.getEmojiStatusDrawable()[a].setParticles(false, animated);
            } else {
                attributesHolder.getEmojiStatusDrawable()[a].set(getPremiumCrossfadeDrawable(componentsFactory, profileActivity, a), animated);
                attributesHolder.getEmojiStatusDrawable()[a].setParticles(false, animated);
            }
        } else if (emojiStatus instanceof TLRPC.TL_emojiStatusCollectible) {
            final TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                if (a == 1) {
                    componentsFactory.getRowsAndStatusComponentsHolder().setEmojiStatusGiftId(status.collectible_id);
                }
                attributesHolder.getEmojiStatusDrawable()[a].set(status.document_id, animated);
                attributesHolder.getEmojiStatusDrawable()[a].setParticles(true, animated);
            } else {
                attributesHolder.getEmojiStatusDrawable()[a].set(getPremiumCrossfadeDrawable(componentsFactory, profileActivity, a), animated);
                attributesHolder.getEmojiStatusDrawable()[a].setParticles(false, animated);
            }
        } else {
            attributesHolder.getEmojiStatusDrawable()[a].set(getPremiumCrossfadeDrawable(componentsFactory, profileActivity, a), animated);
            attributesHolder.getEmojiStatusDrawable()[a].setParticles(false, animated);
        }
        componentsFactory.getColorsUtils().updateEmojiStatusDrawableColor();
        return attributesHolder.getEmojiStatusDrawable()[a];
    }

    public static Drawable getPremiumCrossfadeDrawable(ComponentsFactory componentsFactory, ProfileActivity profileActivity, int a) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getPremiumCrossfadeDrawable()[a] == null) {
            attributesHolder.getPremiumStarDrawable()[a] = ContextCompat.getDrawable(profileActivity.getParentActivity(), R.drawable.msg_premium_liststar).mutate();
            int color = componentsFactory.getColorsUtils().getThemedColor(Theme.key_profile_verifiedBackground);
            if (a == 1) {
                color = dontApplyPeerColor(color);
            }
            attributesHolder.getPremiumStarDrawable()[a].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            attributesHolder.getPremiumCrossfadeDrawable()[a] = new CrossfadeDrawable(attributesHolder.getPremiumStarDrawable()[a], ContextCompat.getDrawable(profileActivity.getParentActivity(), R.drawable.msg_premium_prolfilestar).mutate());
        }
        return attributesHolder.getPremiumCrossfadeDrawable()[a];
    }

    public static Drawable getBotVerificationDrawable(ComponentsFactory componentsFactory, long icon, boolean animated, int a) {
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getBotVerificationDrawable()[a] == null) {
            attributesHolder.getBotVerificationDrawable()[a] = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(attributesHolder.getNameTextView()[a], AndroidUtilities.dp(17), a == 0 ? AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS : AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD);
            attributesHolder.getBotVerificationDrawable()[a].offset(0, dp(1));
            if (rowsHolder.isFragmentViewAttached()) {
                attributesHolder.getBotVerificationDrawable()[a].attach();
            }
        }
        if (icon != 0) {
            attributesHolder.getBotVerificationDrawable()[a].set(icon, animated);
        } else {
            attributesHolder.getBotVerificationDrawable()[a].set((Drawable) null, animated);
        }
        componentsFactory.getColorsUtils().updateEmojiStatusDrawableColor();
        return attributesHolder.getBotVerificationDrawable()[a];
    }

}
