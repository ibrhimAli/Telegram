package org.telegram.ui.profile.utils;

import static org.telegram.ui.profile.utils.Drawables.getPremiumCrossfadeDrawable;
import static org.telegram.ui.profile.utils.Views.updateEmojiStatusEffectPosition;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;

import androidx.core.math.MathUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.SelectAnimatedEmojiDialog;
import org.telegram.ui.Stars.StarGiftSheet;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

public class Status {

    public static void showStatusSelect(ComponentsFactory componentsFactory, ProfileActivity profileActivity) {
        ViewComponentsHolder viewsHolder = componentsFactory.getViewComponentsHolder();
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        if (attributesHolder.getSelectAnimatedEmojiDialog() != null) {
            return;
        }
        final SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[] popup = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[1];
        int xoff, yoff;
        getEmojiStatusLocation(componentsFactory, AndroidUtilities.rectTmp2);
        int topMarginDp = attributesHolder.getNameTextView()[1].getScaleX() < 1.5f ? 16 : 32;
        yoff = -(viewsHolder.getAvatarContainer2().getHeight() - AndroidUtilities.rectTmp2.centerY()) - AndroidUtilities.dp(topMarginDp);
        int popupWidth = (int) Math.min(AndroidUtilities.dp(340 - 16), AndroidUtilities.displaySize.x * .95f);
        int ecenter = AndroidUtilities.rectTmp2.centerX();
        xoff = MathUtils.clamp(ecenter - popupWidth / 2, 0, AndroidUtilities.displaySize.x - popupWidth);
        ecenter -= xoff;
        SelectAnimatedEmojiDialog popupLayout = new SelectAnimatedEmojiDialog(profileActivity, profileActivity.getContext(), true, Math.max(0, ecenter), attributesHolder.getCurrentChat() == null ? SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS : SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL, true, attributesHolder.getResourcesProvider(), topMarginDp) {
            @Override
            protected boolean willApplyEmoji(View view, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(UserConfig.selectedAccount).findUserStarGift(gift.id);
                    return savedStarGift == null || MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) >= 2;
                }
                return true;
            }

            @Override
            public long getDialogId() {
                return profileActivity.getDialogId();
            }

            @Override
            protected void onEmojiSelected(View emojiView, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                final TLRPC.EmojiStatus emojiStatus;
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(UserConfig.selectedAccount).findUserStarGift(gift.id);
                    if (savedStarGift != null && MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) < 2) {
                        MessagesController.getGlobalMainSettings().edit().putInt("statusgiftpage", MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) + 1).apply();
                        new StarGiftSheet(getContext(), UserConfig.selectedAccount, UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId(), attributesHolder.getResourcesProvider())
                                .set(savedStarGift, null)
                                .setupWearPage()
                                .show();
                        if (popup[0] != null) {
                            attributesHolder.setSelectAnimatedEmojiDialog(null);
                            popup[0].dismiss();
                        }
                        return;
                    }
                    final TLRPC.TL_inputEmojiStatusCollectible status = new TLRPC.TL_inputEmojiStatusCollectible();
                    status.collectible_id = gift.id;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                } else if (documentId == null) {
                    emojiStatus = new TLRPC.TL_emojiStatusEmpty();
                } else {
                    final TLRPC.TL_emojiStatus status = new TLRPC.TL_emojiStatus();
                    status.document_id = documentId;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                }
                rowsHolder.setEmojiStatusGiftId(gift != null ? gift.id : null);
                profileActivity.getMessagesController().updateEmojiStatus(attributesHolder.getCurrentChat() == null ? 0 : -attributesHolder.getCurrentChat().id, emojiStatus, gift);
                for (int a = 0; a < 2; ++a) {
                    if (attributesHolder.getEmojiStatusDrawable()[a] != null) {
                        if (documentId == null && attributesHolder.getCurrentChat() == null) {
                            attributesHolder.getEmojiStatusDrawable()[a].set(getPremiumCrossfadeDrawable(componentsFactory, profileActivity, a), true);
                        } else if (documentId != null) {
                            attributesHolder.getEmojiStatusDrawable()[a].set(documentId, true);
                        } else {
                            attributesHolder.getEmojiStatusDrawable()[a].set((Drawable) null, true);
                        }
                        attributesHolder.getEmojiStatusDrawable()[a].setParticles(gift != null, true);
                    }
                }
                if (documentId != null) {
                    attributesHolder.getAnimatedStatusView().animateChange(ReactionsLayoutInBubble.VisibleReaction.fromCustomEmoji(documentId));
                }
                componentsFactory.getColorsUtils().updateEmojiStatusDrawableColor();
                updateEmojiStatusEffectPosition(componentsFactory);
                if (popup[0] != null) {
                    attributesHolder.setSelectAnimatedEmojiDialog(null);
                    popup[0].dismiss();
                }
            }
        };
        TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
        if (user != null) {
            popupLayout.setExpireDateHint(DialogObject.getEmojiStatusUntil(user.emoji_status));
        }
        if (rowsHolder.getEmojiStatusGiftId() != null) {
            popupLayout.setSelected(rowsHolder.getEmojiStatusGiftId());
        } else {
            popupLayout.setSelected(attributesHolder.getEmojiStatusDrawable()[1] != null && attributesHolder.getEmojiStatusDrawable()[1].getDrawable() instanceof AnimatedEmojiDrawable ? ((AnimatedEmojiDrawable) attributesHolder.getEmojiStatusDrawable()[1].getDrawable()).getDocumentId() : null);
        }
        popupLayout.setSaveState(3);
        popupLayout.setScrimDrawable(attributesHolder.getEmojiStatusDrawable()[1], attributesHolder.getNameTextView()[1]);
        popup[0] = attributesHolder.setSelectAnimatedEmojiDialogAndReturn(new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
            @Override
            public void dismiss() {
                super.dismiss();
                attributesHolder.setSelectAnimatedEmojiDialog(null);
            }
        });
        int[] loc = new int[2];
        if (attributesHolder.getNameTextView()[1] != null) {
            attributesHolder.getNameTextView()[1].getLocationOnScreen(loc);
        }
        popup[0].showAsDropDown(profileActivity.fragmentView, xoff, yoff, Gravity.TOP | Gravity.LEFT);
        popup[0].dimBehind();
    }

    public static void getEmojiStatusLocation(ComponentsFactory componentsFactory, Rect rect) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getNameTextView()[1] == null) {
            return;
        }
        if (attributesHolder.getNameTextView()[1].getRightDrawable() == null) {
            rect.set(attributesHolder.getNameTextView()[1].getWidth() - 1, attributesHolder.getNameTextView()[1].getHeight() / 2 - 1, attributesHolder.getNameTextView()[1].getWidth() + 1, attributesHolder.getNameTextView()[1].getHeight() / 2 + 1);
            return;
        }
        rect.set(attributesHolder.getNameTextView()[1].getRightDrawable().getBounds());
        rect.offset((int) (rect.centerX() * (attributesHolder.getNameTextView()[1].getScaleX() - 1f)), 0);
        rect.offset((int) attributesHolder.getNameTextView()[1].getX(), (int) attributesHolder.getNameTextView()[1].getY());
    }

}
