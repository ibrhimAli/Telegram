package org.telegram.ui.profile.utils;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.profile.utils.Drawables.getBotVerificationDrawable;
import static org.telegram.ui.profile.utils.Drawables.getEmojiStatusDrawable;
import static org.telegram.ui.profile.utils.Drawables.getLockIconDrawable;
import static org.telegram.ui.profile.utils.Drawables.getScamDrawable;
import static org.telegram.ui.profile.utils.Drawables.getVerifiedCrossfadeDrawable;
import static org.telegram.ui.profile.utils.Gifts.setCollectibleGiftStatus;
import static org.telegram.ui.profile.utils.QR.updateQrItemVisibility;
import static org.telegram.ui.profile.utils.Status.showStatusSelect;
import static org.telegram.ui.profile.utils.Views.updateEmojiStatusEffectPosition;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.DotDividerSpan;
import org.telegram.ui.Components.EmptyStubSpan;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.MessagePrivateSeenView;
import org.telegram.ui.Components.Premium.PremiumPreviewBottomSheet;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;

public class ProfileData {


    private final ProfileActivity profileActivity;
    private final Adapter adapter;
    private final OpenAndWrite openAndWrite;
    private final ClippedListView listView;
    private Layouts layouts;
    private ColorsUtils colorsUtils;
    private final ComponentsFactory components;
    private ViewComponentsHolder viewsHolder;
    private AttributesComponentsHolder attributesHolder;
    private RowsAndStatusComponentsHolder rowsHolder;

    public ProfileData(ComponentsFactory components) {
        profileActivity = components.getProfileActivity();
        this.components = components;
        this.adapter = components.getAdapter();
        this.openAndWrite = components.getOpenAndWrite();
        listView = components.getListView();
        layouts = components.getLayouts();
        viewsHolder = components.getViewComponentsHolder();
        colorsUtils = components.getColorsUtils();
        attributesHolder = components.getAttributesComponentsHolder();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
    }
    public boolean hasPrivacyCommand() {
        if (!rowsHolder.isBot()) return false;
        if (attributesHolder.getUserInfo() == null || attributesHolder.getUserInfo().bot_info == null) return false;
        if (attributesHolder.getUserInfo().bot_info.privacy_policy_url != null) return true;
        for (TLRPC.TL_botCommand command : attributesHolder.getUserInfo().bot_info.commands) {
            if ("privacy".equals(command.command)) {
                return true;
            }
        }
        return true;
    }

    public void setAutoDeleteHistory(ProfileActivity profileActivity, int time, int action) {
        long did = profileActivity.getDialogId();
        profileActivity.getMessagesController().setDialogHistoryTTL(did, time);
        if (attributesHolder.getUserInfo() != null || attributesHolder.getChatInfo() != null) {
            viewsHolder.getUndoView().showWithAction(did, action, profileActivity.getMessagesController().getUser(did), attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().ttl_period : attributesHolder.getChatInfo().ttl_period, null, null);
        }
    }

    public void updateProfileData(boolean reload) {
        checkForViewsHolder();
        checkForAttributesHolder();
        checkForRowsHolder();
        checkForColorUtils();
        if (viewsHolder.getAvatarContainer() == null || attributesHolder.getNameTextView() == null || profileActivity.getParentActivity() == null) {
            return;
        }
        if (layouts == null) {
            layouts = components.getLayouts();
        }
        String onlineTextOverride;
        int currentConnectionState = profileActivity.getConnectionsManager().getConnectionState();
        if (currentConnectionState == ConnectionsManager.ConnectionStateWaitingForNetwork) {
            onlineTextOverride = LocaleController.getString(R.string.WaitingForNetwork);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnecting) {
            onlineTextOverride = LocaleController.getString(R.string.Connecting);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
            onlineTextOverride = LocaleController.getString(R.string.Updating);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnectingToProxy) {
            onlineTextOverride = LocaleController.getString(R.string.ConnectingToProxy);
        } else {
            onlineTextOverride = null;
        }

        BaseFragment prevFragment = null;
        if (profileActivity.getParentLayout() != null && profileActivity.getParentLayout().getFragmentStack().size() >= 2) {
            BaseFragment fragment = profileActivity.getParentLayout().getFragmentStack().get(profileActivity.getParentLayout().getFragmentStack().size() - 2);
            if (fragment instanceof ChatActivityInterface) {
                prevFragment = fragment;
            }
            if (fragment instanceof DialogsActivity) {
                DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                if (dialogsActivity.rightSlidingDialogContainer != null && dialogsActivity.rightSlidingDialogContainer.getCurrentFragment() instanceof ChatActivityInterface) {
                    viewsHolder.setPreviousTransitionMainFragment(dialogsActivity);
                    prevFragment = dialogsActivity.rightSlidingDialogContainer.getCurrentFragment();
                }
            }
        }
        final boolean copyFromChatActivity = prevFragment instanceof ChatActivity && ((ChatActivity) prevFragment).avatarContainer != null && ((ChatActivity) prevFragment).getChatMode() == ChatActivity.MODE_SUGGESTIONS;

        TLRPC.TL_forumTopic topic = null;
        boolean shortStatus;

        rowsHolder.setHasFallbackPhoto(false);
        rowsHolder.setHasCustomPhoto(false);
        if (attributesHolder.getUserId() != 0) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (user == null) {
                return;
            }
            shortStatus = user.photo != null && user.photo.personal;
            TLRPC.FileLocation photoBig = null;
            if (user.photo != null) {
                photoBig = user.photo.photo_big;
            }
            attributesHolder.getAvatarDrawable().setInfo(UserConfig.selectedAccount, user);

            final MessagesController.PeerColor wasPeerColor = attributesHolder.getPeerColor();
            attributesHolder.setPeerColor(MessagesController.PeerColor.fromCollectible(user.emoji_status));
            if (attributesHolder.getPeerColor() == null) {
                final int colorId = UserObject.getProfileColorId(user);
                final MessagesController.PeerColors peerColors = MessagesController.getInstance(UserConfig.selectedAccount).profilePeerColors;
                attributesHolder.setPeerColor(peerColors == null ? null : peerColors.getColor(colorId));
            }
            if (wasPeerColor != attributesHolder.getPeerColor()) {
                colorsUtils.updatedPeerColor(profileActivity);
            }
            if (viewsHolder.getTopView() != null) {
                viewsHolder.getTopView().setBackgroundEmojiId(UserObject.getProfileEmojiId(user), user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible, true);
            }
            setCollectibleGiftStatus(components, profileActivity, user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible ? (TLRPC.TL_emojiStatusCollectible) user.emoji_status : null);

            final ImageLocation imageLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_BIG);
            final ImageLocation thumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL);
            final ImageLocation videoThumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_VIDEO_BIG);
            VectorAvatarThumbDrawable vectorAvatarThumbDrawable = null;
            TLRPC.VideoSize vectorAvatar = null;
            if (attributesHolder.getUserInfo() != null) {
                vectorAvatar = FileLoader.getVectorMarkupVideoSize(user.photo != null && user.photo.personal ? attributesHolder.getUserInfo().personal_photo : attributesHolder.getUserInfo().profile_photo);
                if (vectorAvatar != null) {
                    vectorAvatarThumbDrawable = new VectorAvatarThumbDrawable(vectorAvatar, user.premium, VectorAvatarThumbDrawable.TYPE_PROFILE);
                }
            }
            final ImageLocation videoLocation = attributesHolder.getAvatarsViewPager().getCurrentVideoLocation(thumbLocation, imageLocation);
            if (attributesHolder.getAvatar() == null) {
                attributesHolder.getAvatarsViewPager().initIfEmpty(vectorAvatarThumbDrawable, imageLocation, thumbLocation, reload);
            }
            if (attributesHolder.getAvatarBig() == null) {
                if (vectorAvatar != null) {
                    attributesHolder.getAvatarImage().setImageDrawable(vectorAvatarThumbDrawable);
                } else if (videoThumbLocation != null && !user.photo.personal) {
                    attributesHolder.getAvatarImage().getImageReceiver().setVideoThumbIsSame(true);
                    attributesHolder.getAvatarImage().setImage(videoThumbLocation, "avatar", thumbLocation, "50_50", attributesHolder.getAvatarDrawable(), user);
                } else {
                    attributesHolder.getAvatarImage().setImage(videoLocation, ImageLoader.AUTOPLAY_FILTER, thumbLocation, "50_50", attributesHolder.getAvatarDrawable(), user);
                }
            }

            if (thumbLocation != null && rowsHolder.getSetAvatarRow() != -1 || thumbLocation == null && rowsHolder.getSetAvatarRow() == -1) {
                adapter.updateListAnimated(false);
                layouts.needLayout(true);
            }
            if (imageLocation != null && (attributesHolder.getPrevLoadedImageLocation() == null || imageLocation.photoId != attributesHolder.getPrevLoadedImageLocation().photoId)) {
                attributesHolder.setPrevLoadedImageLocation(imageLocation);
                profileActivity.getFileLoader().loadFile(imageLocation, user, null, FileLoader.PRIORITY_LOW, 1);
            }

            CharSequence newString = UserObject.getUserName(user);
            String newString2;
            boolean hiddenStatusButton = false;
            if (user.id == profileActivity.getUserConfig().getClientUserId()) {
                if (UserObject.hasFallbackPhoto(attributesHolder.getUserInfo())) {
                    newString2 = "";
                    rowsHolder.setHasFallbackPhoto(true);
                    TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(attributesHolder.getUserInfo().fallback_photo.sizes, 1000);
                    if (smallSize != null) {
                        viewsHolder.getFallbackImage().setImage(ImageLocation.getForPhoto(smallSize, attributesHolder.getUserInfo().fallback_photo), "50_50", null, 0, null, UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser(), 0);
                    }
                } else {
                    newString2 = LocaleController.getString(R.string.Online);
                }
            } else if (user.id == UserObject.VERIFY) {
                newString2 = LocaleController.getString(R.string.VerifyCodesNotifications);
            } else if (user.id == 333000 || user.id == 777000 || user.id == 42777) {
                newString2 = LocaleController.getString(R.string.ServiceNotifications);
            } else if (MessagesController.isSupportUser(user)) {
                newString2 = LocaleController.getString(R.string.SupportStatus);
            } else if (rowsHolder.isBot()) {
                if (user.bot_active_users != 0) {
                    newString2 = LocaleController.formatPluralStringComma("BotUsers", user.bot_active_users, ',');
                } else {
                    newString2 = LocaleController.getString(R.string.Bot);
                }
            } else {
                attributesHolder.getIsOnline()[0] = false;
                newString2 = LocaleController.formatUserStatus(UserConfig.selectedAccount, user, attributesHolder.getIsOnline(), shortStatus ? new boolean[1] : null);
                hiddenStatusButton = user != null && !attributesHolder.getIsOnline()[0] && !profileActivity.getUserConfig().isPremium() && user.status != null && (user.status instanceof TLRPC.TL_userStatusRecently || user.status instanceof TLRPC.TL_userStatusLastMonth || user.status instanceof TLRPC.TL_userStatusLastWeek) && user.status.by_me;
                if (attributesHolder.getOnlineTextView()[1] != null && !rowsHolder.isMediaHeaderVisible()) {
                    int key = attributesHolder.getIsOnline()[0] && attributesHolder.getPeerColor() == null ? Theme.key_profile_status : Theme.key_avatar_subtitleInProfileBlue;
                    attributesHolder.getOnlineTextView()[1].setTag(key);
                    if (!rowsHolder.isPulledDown()) {
                        attributesHolder.getOnlineTextView()[1].setTextColor(colorsUtils.applyPeerColor(colorsUtils.getThemedColor(key), true, attributesHolder.getIsOnline()[0]));
                    }
                }
            }
            rowsHolder.setHasCustomPhoto(user.photo != null && user.photo.personal);
            try {
                newString = Emoji.replaceEmoji(newString, attributesHolder.getNameTextView()[1].getPaint().getFontMetricsInt(), false);
            } catch (Exception ignore) {
            }
            if (copyFromChatActivity) {
                ChatActivity chatActivity = (ChatActivity) prevFragment;
                BackupImageView fromAvatarImage = chatActivity.avatarContainer.getAvatarImageView();
                attributesHolder.getAvatarImage().setAnimateFromImageReceiver(fromAvatarImage.getImageReceiver());
            }
            for (int a = 0; a < 2; a++) {
                if (attributesHolder.getNameTextView()[a] == null) {
                    continue;
                }
                if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    SimpleTextView titleTextView = chatActivity.avatarContainer.getTitleTextView();
                    attributesHolder.getNameTextView()[a].setText(titleTextView.getText());
                    attributesHolder.getNameTextView()[a].setRightDrawable(titleTextView.getRightDrawable());
                    attributesHolder.getNameTextView()[a].setRightDrawable2(titleTextView.getRightDrawable2());
                } else if (a == 0 && user.id != profileActivity.getUserConfig().getClientUserId() && !MessagesController.isSupportUser(user) && user.phone != null && user.phone.length() != 0 && profileActivity.getAccountInstance().getContactsController().contactsDict.get(user.id) == null &&
                        (!profileActivity.getAccountInstance().getContactsController().contactsDict.isEmpty() || !profileActivity.getAccountInstance().getContactsController().isLoadingContacts())) {
                    attributesHolder.getNameTextView()[a].setText(PhoneFormat.getInstance().format("+" + user.phone));
                } else {
                    attributesHolder.getNameTextView()[a].setText(newString);
                }
                if (a == 0 && onlineTextOverride != null) {
                    attributesHolder.getOnlineTextView()[a].setText(onlineTextOverride);
                } else if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    if (chatActivity.avatarContainer.getSubtitleTextView() instanceof SimpleTextView) {
                        SimpleTextView textView = (SimpleTextView) chatActivity.avatarContainer.getSubtitleTextView();
                        attributesHolder.getOnlineTextView()[a].setText(textView.getText());
                    } else if (chatActivity.avatarContainer.getSubtitleTextView() instanceof AnimatedTextView) {
                        AnimatedTextView textView = (AnimatedTextView) chatActivity.avatarContainer.getSubtitleTextView();
                        attributesHolder.getOnlineTextView()[a].setText(textView.getText());
                    }
                } else {
                    attributesHolder.getOnlineTextView()[a].setText(newString2);
                }
                attributesHolder.getOnlineTextView()[a].setDrawablePadding(dp(9));
                attributesHolder.getOnlineTextView()[a].setRightDrawableInside(true);
                attributesHolder.getOnlineTextView()[a].setRightDrawable(a == 1 && hiddenStatusButton ? colorsUtils.getShowStatusButton() : null);
                attributesHolder.getOnlineTextView()[a].setRightDrawableOnClick(a == 1 && hiddenStatusButton ? v -> {
                    MessagePrivateSeenView.showSheet(profileActivity.getContext(), UserConfig.selectedAccount, profileActivity.getDialogId(), true, null, () -> {
                        profileActivity.getMessagesController().reloadUser(profileActivity.getDialogId());
                    }, attributesHolder.getResourcesProvider());
                } : null);
                Drawable leftIcon = attributesHolder.getCurrentEncryptedChat() != null ? getLockIconDrawable(components) : null;
                boolean rightIconIsPremium = false, rightIconIsStatus = false;
                attributesHolder.getNameTextView()[a].setRightDrawableOutside(a == 0);
                if (a == 0 && !copyFromChatActivity) {
                    if (user.scam || user.fake) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getScamDrawable(components, user.scam ? 0 : 1));
                        attributesHolder.setNameTextViewRightDrawable2ContentDescription(LocaleController.getString(R.string.ScamMessage));
                    } else if (user.verified) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getVerifiedCrossfadeDrawable(components, profileActivity, a));
                        attributesHolder.setNameTextViewRightDrawable2ContentDescription(LocaleController.getString(R.string.AccDescrVerified));
                    } else if (profileActivity.getMessagesController().isDialogMuted(attributesHolder.getDialogId() != 0 ? attributesHolder.getDialogId() : attributesHolder.getUserId(), attributesHolder.getTopicId())) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(profileActivity.getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                        attributesHolder.setNameTextViewRightDrawable2ContentDescription(LocaleController.getString(R.string.NotificationsMuted));
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(null);
                        attributesHolder.setNameTextViewRightDrawable2ContentDescription(null);
                    }
                    if (user != null && !profileActivity.getMessagesController().premiumFeaturesBlocked() && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                        rightIconIsStatus = true;
                        rightIconIsPremium = false;
                        attributesHolder.getNameTextView()[a].setRightDrawable(getEmojiStatusDrawable(components, profileActivity, user.emoji_status, false, false, a));
                        attributesHolder.setNameTextViewRightDrawableContentDescription(LocaleController.getString(R.string.AccDescrPremium));
                    } else if (profileActivity.getMessagesController().isPremiumUser(user)) {
                        rightIconIsStatus = false;
                        rightIconIsPremium = true;
                        attributesHolder.getNameTextView()[a].setRightDrawable(getEmojiStatusDrawable(components, profileActivity, null, false, false, a));
                        attributesHolder.setNameTextViewRightDrawableContentDescription(LocaleController.getString( R.string.AccDescrPremium));
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable(null);
                        attributesHolder.setNameTextViewRightDrawableContentDescription(null);
                    }
                } else if (a == 1) {
                    if (user.scam || user.fake) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getScamDrawable(components, user.scam ? 0 : 1));
                    } else if (user.verified) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getVerifiedCrossfadeDrawable(components, profileActivity, a));
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(null);
                    }
                    if (!profileActivity.getMessagesController().premiumFeaturesBlocked() && user != null && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                        rightIconIsStatus = true;
                        rightIconIsPremium = false;
                        attributesHolder.getNameTextView()[a].setRightDrawable(getEmojiStatusDrawable(components, profileActivity, user.emoji_status, true, true, a));
                    } else if (profileActivity.getMessagesController().isPremiumUser(user)) {
                        rightIconIsStatus = false;
                        rightIconIsPremium = true;
                        attributesHolder.getNameTextView()[a].setRightDrawable(getEmojiStatusDrawable(components, profileActivity, null, true, true, a));
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable(null);
                    }
                }
                if (leftIcon == null && attributesHolder.getCurrentEncryptedChat() == null && user.bot_verification_icon != 0) {
                    attributesHolder.getNameTextView()[a].setLeftDrawableOutside(true);
                    leftIcon = getBotVerificationDrawable(components, user.bot_verification_icon, false, a);
                } else {
                    attributesHolder.getNameTextView()[a].setLeftDrawableOutside(false);
                }
                attributesHolder.getNameTextView()[a].setLeftDrawable(leftIcon);
                if (a == 1 && (rightIconIsStatus || rightIconIsPremium)) {
                    attributesHolder.getNameTextView()[a].setRightDrawableOutside(true);
                }
                if (user.self && profileActivity.getMessagesController().isPremiumUser(user)) {
                    attributesHolder.getNameTextView()[a].setRightDrawableOnClick(v -> {
                        showStatusSelect(components, profileActivity);
                    });
                }
                if (!user.self && profileActivity.getMessagesController().isPremiumUser(user)) {
                    final SimpleTextView textView = attributesHolder.getNameTextView()[a];
                    attributesHolder.getNameTextView()[a].setRightDrawableOnClick(v -> {
                        if (user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                            TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) user.emoji_status;
                            if (status != null) {
                                Browser.openUrl(profileActivity.getContext(), "https://" + profileActivity.getMessagesController().linkPrefix + "/nft/" + status.slug);
                            }
                            return;
                        }
                        PremiumPreviewBottomSheet premiumPreviewBottomSheet = new PremiumPreviewBottomSheet(profileActivity, UserConfig.selectedAccount, user, attributesHolder.getResourcesProvider());
                        int[] coords = new int[2];
                        textView.getLocationOnScreen(coords);
                        premiumPreviewBottomSheet.startEnterFromX = textView.rightDrawableX;
                        premiumPreviewBottomSheet.startEnterFromY = textView.rightDrawableY;
                        premiumPreviewBottomSheet.startEnterFromScale = textView.getScaleX();
                        premiumPreviewBottomSheet.startEnterFromX1 = textView.getLeft();
                        premiumPreviewBottomSheet.startEnterFromY1 = textView.getTop();
                        premiumPreviewBottomSheet.startEnterFromView = textView;
                        if (textView.getRightDrawable() == attributesHolder.getEmojiStatusDrawable()[1] && attributesHolder.getEmojiStatusDrawable()[1] != null && attributesHolder.getEmojiStatusDrawable()[1].getDrawable() instanceof AnimatedEmojiDrawable) {
                            premiumPreviewBottomSheet.startEnterFromScale *= 0.98f;
                            TLRPC.Document document = ((AnimatedEmojiDrawable) attributesHolder.getEmojiStatusDrawable()[1].getDrawable()).getDocument();
                            if (document != null) {
                                BackupImageView icon = new BackupImageView(profileActivity.getContext());
                                String filter = "160_160";
                                ImageLocation mediaLocation;
                                String mediaFilter;
                                SvgHelper.SvgDrawable thumbDrawable = DocumentObject.getSvgThumb(document.thumbs, Theme.key_windowBackgroundWhiteGrayIcon, 0.2f);
                                TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 90);
                                if ("video/webm".equals(document.mime_type)) {
                                    mediaLocation = ImageLocation.getForDocument(document);
                                    mediaFilter = filter + "_" + ImageLoader.AUTOPLAY_FILTER;
                                    if (thumbDrawable != null) {
                                        thumbDrawable.overrideWidthAndHeight(512, 512);
                                    }
                                } else {
                                    if (thumbDrawable != null && MessageObject.isAnimatedStickerDocument(document, false)) {
                                        thumbDrawable.overrideWidthAndHeight(512, 512);
                                    }
                                    mediaLocation = ImageLocation.getForDocument(document);
                                    mediaFilter = filter;
                                }
                                icon.setLayerNum(7);
                                icon.setRoundRadius(AndroidUtilities.dp(4));
                                icon.setImage(mediaLocation, mediaFilter, ImageLocation.getForDocument(thumb, document), "140_140", thumbDrawable, document);
                                if (((AnimatedEmojiDrawable) attributesHolder.getEmojiStatusDrawable()[1].getDrawable()).canOverrideColor()) {
                                    icon.setColorFilter(new PorterDuffColorFilter(colorsUtils.getThemedColor(Theme.key_windowBackgroundWhiteBlueIcon), PorterDuff.Mode.SRC_IN));
                                    premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                                } else {
                                    premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                                }
                                premiumPreviewBottomSheet.overrideTitleIcon = icon;
                                premiumPreviewBottomSheet.isEmojiStatus = true;
                            }
                        }
                        profileActivity.showDialog(premiumPreviewBottomSheet);
                    });
                }
            }

            if (attributesHolder.getUserId() == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
                attributesHolder.getOnlineTextView()[2].setText(LocaleController.getString(R.string.FallbackTooltip));
                attributesHolder.getOnlineTextView()[3].setText(LocaleController.getString(R.string.Online));
            } else {
                if (user.photo != null && user.photo.personal && user.photo.has_video) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(newString2);
                    spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, newString2.length(), 0);
                    spannableStringBuilder.append(" d ");
                    spannableStringBuilder.append(LocaleController.getString(R.string.CustomAvatarTooltipVideo));
                    spannableStringBuilder.setSpan(new DotDividerSpan(), newString2.length() + 1, newString2.length() + 2, 0);
                    attributesHolder.getOnlineTextView()[2].setText(spannableStringBuilder);
                } else {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(newString2);
                    spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, newString2.length(), 0);
                    spannableStringBuilder.append(" d ");
                    spannableStringBuilder.append(LocaleController.getString(R.string.CustomAvatarTooltip));
                    spannableStringBuilder.setSpan(new DotDividerSpan(), newString2.length() + 1, newString2.length() + 2, 0);
                    attributesHolder.getOnlineTextView()[2].setText(spannableStringBuilder);
                }
            }

            attributesHolder.getOnlineTextView()[2].setVisibility(View.VISIBLE);
            if (!rowsHolder.isSearchMode()) {
                attributesHolder.getOnlineTextView()[3].setVisibility(View.VISIBLE);
            }

            if (attributesHolder.getPreviousTransitionFragment() != null) {
                attributesHolder.getPreviousTransitionFragment().checkAndUpdateAvatar();
            }
            attributesHolder.getAvatarImage().getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (profileActivity.getLastStoryViewer() == null || profileActivity.getLastStoryViewer().transitionViewHolder.view != attributesHolder.getAvatarImage()), viewsHolder.getStoryView() != null);
        } else if (attributesHolder.getChatId() != 0) {
            TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
            if (chat != null) {
                attributesHolder.setCurrentChat(chat);
            } else {
                chat = attributesHolder.getCurrentChat();
            }
            if (viewsHolder.getFlagSecureReason() != null) {
                viewsHolder.getFlagSecureReason().invalidate();
            }

            final MessagesController.PeerColor wasPeerColor = attributesHolder.getPeerColor();
            attributesHolder.setPeerColor(MessagesController.PeerColor.fromCollectible(chat.emoji_status));
            if (attributesHolder.getPeerColor() == null) {
                final int colorId = ChatObject.getProfileColorId(chat);
                MessagesController.PeerColors peerColors = MessagesController.getInstance(UserConfig.selectedAccount).profilePeerColors;
                attributesHolder.setPeerColor(peerColors == null ? null : peerColors.getColor(colorId));
            }
            if (wasPeerColor != attributesHolder.getPeerColor()) {
                colorsUtils.updatedPeerColor(profileActivity);
            }
            if (viewsHolder.getTopView() != null) {
                viewsHolder.getTopView().setBackgroundEmojiId(ChatObject.getProfileEmojiId(chat), chat != null && chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible, true);
            }
            setCollectibleGiftStatus(components, profileActivity, chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible ? (TLRPC.TL_emojiStatusCollectible) chat.emoji_status : null);

            if (rowsHolder.isTopic()) {
                topic = profileActivity.getMessagesController().getTopicsController().findTopic(attributesHolder.getChatId(), attributesHolder.getTopicId());
            }

            CharSequence statusString;
            CharSequence profileStatusString;
            boolean profileStatusIsButton = false;
            if (ChatObject.isChannel(chat)) {
                if (!rowsHolder.isTopic() && (attributesHolder.getChatInfo() == null || !attributesHolder.getCurrentChat().megagroup && (attributesHolder.getChatInfo().participants_count == 0 || ChatObject.hasAdminRights(attributesHolder.getCurrentChat()) || attributesHolder.getChatInfo().can_view_participants))) {
                    if (attributesHolder.getCurrentChat().megagroup) {
                        statusString = profileStatusString = LocaleController.getString(R.string.Loading).toLowerCase();
                    } else {
                        if (ChatObject.isPublic(chat)) {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPublic).toLowerCase();
                        } else {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPrivate).toLowerCase();
                        }
                    }
                } else {
                    if (rowsHolder.isTopic()) {
                        int count = 0;
                        if (topic != null) {
                            count = topic.totalMessagesCount - 1;
                        }
                        if (count > 0) {
                            statusString = LocaleController.formatPluralString("messages", count, count);
                        } else {
                            statusString = formatString("TopicProfileStatus", R.string.TopicProfileStatus, chat.title);
                        }
                        SpannableString arrowString = new SpannableString(">");
                        arrowString.setSpan(new ColoredImageSpan(R.drawable.arrow_newchat), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileStatusString = new SpannableStringBuilder(chat.title).append(' ').append(arrowString);
                        profileStatusIsButton = true;
                    } else if (attributesHolder.getCurrentChat().megagroup) {
                        if (rowsHolder.getOnlineCount() > 1 && attributesHolder.getChatInfo().participants_count != 0) {
                            statusString = String.format("%s, %s", LocaleController.formatPluralString("Members", attributesHolder.getChatInfo().participants_count), LocaleController.formatPluralString("OnlineCount", Math.min(rowsHolder.getOnlineCount(), attributesHolder.getChatInfo().participants_count)));
                            profileStatusString = String.format("%s, %s", LocaleController.formatPluralStringComma("Members", attributesHolder.getChatInfo().participants_count), LocaleController.formatPluralStringComma("OnlineCount", Math.min(rowsHolder.getOnlineCount(), attributesHolder.getChatInfo().participants_count)));
                        } else {
                            if (attributesHolder.getChatInfo().participants_count == 0) {
                                if (chat.has_geo) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaLocation).toLowerCase();
                                } else if (ChatObject.isPublic(chat)) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPublic).toLowerCase();
                                } else {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPrivate).toLowerCase();
                                }
                            } else {
                                statusString = LocaleController.formatPluralString("Members", attributesHolder.getChatInfo().participants_count);
                                profileStatusString = LocaleController.formatPluralStringComma("Members", attributesHolder.getChatInfo().participants_count);
                            }
                        }
                    } else {
                        int[] result = new int[1];
                        String shortNumber = LocaleController.formatShortNumber(attributesHolder.getChatInfo().participants_count, result);
                        if (attributesHolder.getCurrentChat().megagroup) {
                            statusString = LocaleController.formatPluralString("Members", attributesHolder.getChatInfo().participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Members", attributesHolder.getChatInfo().participants_count);
                        } else {
                            statusString = LocaleController.formatPluralString("Subscribers", attributesHolder.getChatInfo().participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Subscribers", attributesHolder.getChatInfo().participants_count);
                        }
                    }
                }
            } else {
                if (ChatObject.isKickedFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouWereKicked);
                } else if (ChatObject.isLeftFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouLeft);
                } else {
                    int count = chat.participants_count;
                    if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().participants != null) {
                        count = attributesHolder.getChatInfo().participants.participants.size();
                    }
                    if (count != 0 && rowsHolder.getOnlineCount() > 1) {
                        statusString = profileStatusString = String.format("%s, %s", LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("OnlineCount", rowsHolder.getOnlineCount()));
                    } else {
                        statusString = profileStatusString = LocaleController.formatPluralString("Members", count);
                    }
                }
            }
            if (copyFromChatActivity) {
                ChatActivity chatActivity = (ChatActivity) prevFragment;
                if (chatActivity.avatarContainer.getSubtitleTextView() instanceof SimpleTextView) {
                    statusString = ((SimpleTextView) chatActivity.avatarContainer.getSubtitleTextView()).getText();
                } else if (chatActivity.avatarContainer.getSubtitleTextView() instanceof AnimatedTextView) {
                    statusString = ((AnimatedTextView) chatActivity.avatarContainer.getSubtitleTextView()).getText();
                }
                BackupImageView fromAvatarImage = chatActivity.avatarContainer.getAvatarImageView();
                attributesHolder.getAvatarImage().setAnimateFromImageReceiver(fromAvatarImage.getImageReceiver());
            }

            boolean changed = false;
            for (int a = 0; a < 2; a++) {
                if (attributesHolder.getNameTextView()[a] == null) {
                    continue;
                }
                if (a == 0 && copyFromChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) prevFragment;
                    SimpleTextView titleTextView = chatActivity.avatarContainer.getTitleTextView();
                    if (attributesHolder.getNameTextView()[a].setText(titleTextView.getText())) {
                        changed = true;
                    }
                    if (attributesHolder.getNameTextView()[a].setRightDrawable(titleTextView.getRightDrawable())) {
                        changed = true;
                    }
                    if (attributesHolder.getNameTextView()[a].setRightDrawable2(titleTextView.getRightDrawable2())) {
                        changed = true;
                    }
                } else if (rowsHolder.isTopic()) {
                    CharSequence title = topic == null ? "" : topic.title;
                    try {
                        title = Emoji.replaceEmoji(title, attributesHolder.getNameTextView()[a].getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {
                    }
                    if (attributesHolder.getNameTextView()[a].setText(title)) {
                        changed = true;
                    }
                } else if (ChatObject.isMonoForum(chat)) {
                    CharSequence title = getString(R.string.ChatMessageSuggestions);
                    if (attributesHolder.getNameTextView()[a].setText(title)) {
                        changed = true;
                    }
                } else if (chat.title != null) {
                    CharSequence title = chat.title;
                    try {
                        title = Emoji.replaceEmoji(title, attributesHolder.getNameTextView()[a].getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {
                    }
                    if (attributesHolder.getNameTextView()[a].setText(title)) {
                        changed = true;
                    }
                }
                attributesHolder.getNameTextView()[a].setLeftDrawableOutside(false);
                attributesHolder.getNameTextView()[a].setLeftDrawable(null);
                attributesHolder.getNameTextView()[a].setRightDrawableOutside(a == 0);
                attributesHolder.getNameTextView()[a].setRightDrawableOnClick(null);
                if (a != 0) {
                    if (chat.scam || chat.fake) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getScamDrawable(components, chat.scam ? 0 : 1));
                        attributesHolder.setNameTextViewRightDrawableContentDescription(LocaleController.getString(R.string.ScamMessage));
                    } else if (chat.verified) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getVerifiedCrossfadeDrawable(components, profileActivity, a));
                        attributesHolder.setNameTextViewRightDrawableContentDescription(LocaleController.getString(R.string.AccDescrVerified));
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(null);
                        attributesHolder.setNameTextViewRightDrawableContentDescription(null);
                    }
                    if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                        attributesHolder.getNameTextView()[a].setRightDrawable(getEmojiStatusDrawable(components, profileActivity, chat.emoji_status, true, false, a));
                        attributesHolder.getNameTextView()[a].setRightDrawableOutside(true);
                        attributesHolder.setNameTextViewRightDrawableContentDescription(null);
                        if (ChatObject.canChangeChatInfo(chat)) {
                            attributesHolder.getNameTextView()[a].setRightDrawableOnClick(v -> {
                                showStatusSelect(components, profileActivity);
                            });
                            if (rowsHolder.isPreloadedChannelEmojiStatuses()) {
                                rowsHolder.setPreloadedChannelEmojiStatuses(true);
                                profileActivity.getMediaDataController().loadRestrictedStatusEmojis();
                            }
                        } else if (chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                            final String slug = ((TLRPC.TL_emojiStatusCollectible) chat.emoji_status).slug;
                            attributesHolder.getNameTextView()[a].setRightDrawableOnClick(v -> {
                                Browser.openUrl(profileActivity.getContext(), "https://" + profileActivity.getMessagesController().linkPrefix + "/nft/" + slug);
                            });
                        }
                    }
                } else if (!copyFromChatActivity) {
                    if (chat.scam || chat.fake) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getScamDrawable(components, chat.scam ? 0 : 1));
                    } else if (chat.verified) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(getVerifiedCrossfadeDrawable(components, profileActivity, a)) ;
                    } else if (profileActivity.getMessagesController().isDialogMuted(-attributesHolder.getChatId(), attributesHolder.getTopicId())) {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(profileActivity.getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable2(null);
                    }
                    if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                        attributesHolder.getNameTextView()[a].setRightDrawable(getEmojiStatusDrawable(components, profileActivity, chat.emoji_status, false, false, a));
                        attributesHolder.getNameTextView()[a].setRightDrawableOutside(true);
                    } else {
                        attributesHolder.getNameTextView()[a].setRightDrawable(null);
                    }
                }
                if (chat.bot_verification_icon != 0) {
                    attributesHolder.getNameTextView()[a].setLeftDrawableOutside(true);
                    attributesHolder.getNameTextView()[a].setLeftDrawable(getBotVerificationDrawable(components, chat.bot_verification_icon, false, a));
                } else {
                    attributesHolder.getNameTextView()[a].setLeftDrawable(null);
                }
                if (a == 0 && onlineTextOverride != null) {
                    attributesHolder.getOnlineTextView()[a].setText(onlineTextOverride);
                } else {
                    if (copyFromChatActivity || (attributesHolder.getCurrentChat().megagroup && attributesHolder.getChatInfo() != null && rowsHolder.getOnlineCount() > 0) || rowsHolder.isTopic()) {
                        attributesHolder.getOnlineTextView()[a].setText(a == 0 ? statusString : profileStatusString);
                    } else if (a == 0 && ChatObject.isChannel(attributesHolder.getCurrentChat()) && attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().participants_count != 0 && (attributesHolder.getCurrentChat().megagroup || attributesHolder.getCurrentChat().broadcast)) {
                        int[] result = new int[1];
                        boolean ignoreShort = AndroidUtilities.isAccessibilityScreenReaderEnabled();
                        String shortNumber = ignoreShort ? String.valueOf(result[0] = attributesHolder.getChatInfo().participants_count) : LocaleController.formatShortNumber(attributesHolder.getChatInfo().participants_count, result);
                        if (attributesHolder.getCurrentChat().megagroup) {
                            if (attributesHolder.getChatInfo().participants_count == 0) {
                                if (chat.has_geo) {
                                    attributesHolder.getOnlineTextView()[a].setText(LocaleController.getString(R.string.MegaLocation).toLowerCase());
                                } else if (ChatObject.isPublic(chat)) {
                                    attributesHolder.getOnlineTextView()[a].setText(LocaleController.getString(R.string.MegaPublic).toLowerCase());
                                } else {
                                    attributesHolder.getOnlineTextView()[a].setText(LocaleController.getString(R.string.MegaPrivate).toLowerCase());
                                }
                            } else {
                                attributesHolder.getOnlineTextView()[a].setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", result[0]), shortNumber));
                            }
                        } else {
                            attributesHolder.getOnlineTextView()[a].setText(LocaleController.formatPluralString("Subscribers", result[0]).replace(String.format("%d", result[0]), shortNumber));
                        }
                    } else {
                        attributesHolder.getOnlineTextView()[a].setText(a == 0 ? statusString : profileStatusString);
                    }
                }
                if (a == 1 && rowsHolder.isTopic()) {
                    if (profileStatusIsButton) {
                        attributesHolder.getOnlineTextView()[a].setOnClickListener(e -> openAndWrite.goToForum());
                    } else {
                        attributesHolder.getOnlineTextView()[a].setOnClickListener(null);
                        attributesHolder.getOnlineTextView()[a].setClickable(false);
                    }
                }
            }
            if (changed) {
                layouts.needLayout(true);
            }

            TLRPC.FileLocation photoBig = null;
            if (chat.photo != null && !rowsHolder.isTopic()) {
                photoBig = chat.photo.photo_big;
            }

            final ImageLocation imageLocation;
            final ImageLocation thumbLocation;
            final ImageLocation videoLocation;
            if (rowsHolder.isTopic()) {
                imageLocation = null;
                thumbLocation = null;
                videoLocation = null;
                ForumUtilities.setTopicIcon(attributesHolder.getAvatarImage(), topic, true, true, attributesHolder.getResourcesProvider());
            } else if (ChatObject.isMonoForum(attributesHolder.getCurrentChat())) {
                TLRPC.Chat channel = profileActivity.getMessagesController().getMonoForumLinkedChat(attributesHolder.getCurrentChat().id);
                attributesHolder.getAvatarDrawable().setInfo(UserConfig.selectedAccount, channel);
                imageLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_SMALL);
                videoLocation = attributesHolder.getAvatarsViewPager().getCurrentVideoLocation(thumbLocation, imageLocation);
            } else {
                attributesHolder.getAvatarDrawable().setInfo(UserConfig.selectedAccount, chat);
                imageLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_SMALL);
                videoLocation = attributesHolder.getAvatarsViewPager().getCurrentVideoLocation(thumbLocation, imageLocation);
            }

            boolean initied = attributesHolder.getAvatarsViewPager().initIfEmpty(null, imageLocation, thumbLocation, reload);
            if ((imageLocation == null || initied) && rowsHolder.isPulledDown()) {
                final View view = viewsHolder.getLayoutManager().findViewByPosition(0);
                if (view != null) {
                    listView.getClippedList().smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT), CubicBezierInterpolator.EASE_OUT_QUINT);
                }
            }
            String filter;
            if (videoLocation != null && videoLocation.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = ImageLoader.AUTOPLAY_FILTER;
            } else {
                filter = null;
            }
            if (attributesHolder.getAvatarBig() == null && !rowsHolder.isTopic()) {
                attributesHolder.getAvatarImage().setImage(videoLocation, filter, thumbLocation, "50_50", attributesHolder.getAvatarDrawable(), chat);
            }
            if (imageLocation != null && (attributesHolder.getPrevLoadedImageLocation() == null || imageLocation.photoId != attributesHolder.getPrevLoadedImageLocation().photoId)) {
                attributesHolder.setPrevLoadedImageLocation(imageLocation);
                profileActivity.getFileLoader().loadFile(imageLocation, chat, null, FileLoader.PRIORITY_LOW, 1);
            }
            attributesHolder.getAvatarImage().getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (profileActivity.getLastStoryViewer() == null || profileActivity.getLastStoryViewer().transitionViewHolder.view != attributesHolder.getAvatarImage()), viewsHolder.getStoryView() != null);
        }

        if (viewsHolder.getQrItem() != null) {
            updateQrItemVisibility(components, true);
        }
        AndroidUtilities.runOnUIThread(() -> updateEmojiStatusEffectPosition(components));
    }

    private void checkForColorUtils() {
        if (colorsUtils == null) {
            colorsUtils = components.getColorsUtils();
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

    public void updateItemsUsername(ProfileActivity profileActivity) {
        checkForViewsHolder();
        checkForAttributesHolder();
        if (!attributesHolder.isMyProfile() || viewsHolder.getSetUsernameItem() == null || viewsHolder.getLinkItem() == null) return;
        TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
        if (user == null) {
            return;
        }
        final boolean hasUsername = UserObject.getPublicUsername(user) != null;
        viewsHolder.getSetUsernameItem().setIcon(hasUsername ? R.drawable.menu_username_change : R.drawable.menu_username_set);
        viewsHolder.getSetUsernameItem().setText(hasUsername ? getString(R.string.ProfileUsernameEdit) : getString(R.string.ProfileUsernameSet));
        viewsHolder.getLinkItem().setVisibility(UserObject.getPublicUsername(user) != null ? View.VISIBLE : View.GONE);
    }

}
