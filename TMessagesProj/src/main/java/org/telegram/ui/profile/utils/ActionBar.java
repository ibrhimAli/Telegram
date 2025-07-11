package org.telegram.ui.profile.utils;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.profile.utils.ColorsUtils.dontApplyPeerColor;
import static org.telegram.ui.profile.utils.HideAndVisibility.showAvatarProgress;
import static org.telegram.ui.profile.utils.Images.setForegroundImage;
import static org.telegram.ui.profile.utils.Update.updateAutoDeleteItem;
import static org.telegram.ui.profile.utils.Views.updateStoriesViewBounds;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.ui.AccountFrozenAlert;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.AutoDeleteMessagesActivity;
import org.telegram.ui.BasePermissionsActivity;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatEditActivity;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AutoDeletePopupWrapper;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.MediaActivity;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LogoutActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.QrActivity;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.TopicCreateFragment;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.BotWebViewAttachedSheet;
import org.telegram.ui.profile.adapter.ListAdapter;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;
import org.telegram.ui.profile.view.button.ButtonViewEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionBar {


    private final OpenAndWrite openAndWrite;
    private final ProfileActivity profileActivity;
    private final Adapter adapter;
    private ClicksAndPress clicksAndPress;
    private final ProfileData profileData;
    private ClippedListView listView;
    private final ComponentsFactory components;
    private ViewComponentsHolder viewsHolder;
    private AttributesComponentsHolder attributesHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    private ColorsUtils colorsUtils;

    public ActionBar(ComponentsFactory components) {
        profileActivity = components.getProfileActivity();
        this.components = components;
        this.openAndWrite = components.getOpenAndWrite();
        adapter = components.getAdapter();
        clicksAndPress = components.getClicksAndPress();
        profileData = components.getProfileData();
        listView = components.getListView();
        viewsHolder = components.getViewComponentsHolder();
        attributesHolder = components.getAttributesComponentsHolder();
        colorsUtils = components.getColorsUtils();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
    }
    public void handleActionbarClick(int id, ListAdapter listAdapter,
                                            float photoDescriptionProgress, float customAvatarProgress,
                                            int avatarUploadingRequest, ImageLocation uploadingImageLocation,
                                            TL_bots.BotInfo botInfo, RadialProgressView avatarProgressView) {
        if (profileActivity.getParentActivity() == null) {
            return;
        }
        if (listView == null) {
            listView = components.getListView();
        }
        if (id == -1) {
            profileActivity.finishFragment();
        } else if (id == ProfileParams.block_contact) {
            clicksAndPress.onProfileButtonClicked(ButtonViewEnum.BLOCK);
        } else if (id == ProfileParams.add_contact) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            Bundle args = new Bundle();
            args.putLong("user_id", user.id);
            args.putBoolean("addContact", true);
            openAndWrite.openAddToContact(listAdapter, args, user, photoDescriptionProgress, customAvatarProgress, 1f);
        } else if (id == ProfileParams.share_contact) {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
            args.putString("selectAlertString", LocaleController.getString(R.string.SendContactToText));
            args.putString("selectAlertStringGroup", LocaleController.getString(R.string.SendContactToGroupText));
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate(profileActivity);
            profileActivity.presentFragment(fragment);
        } else if (id == ProfileParams.edit_contact) {
            Bundle args = new Bundle();
            args.putLong("user_id", attributesHolder.getUserId());
            profileActivity.presentFragment(new ContactAddActivity(args, attributesHolder.getResourcesProvider()));
        } else if (id == ProfileParams.delete_contact) {
            final TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (user == null || profileActivity.getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
            builder.setTitle(LocaleController.getString(R.string.DeleteContact));
            builder.setMessage(LocaleController.getString(R.string.AreYouSureDeleteContact));
            builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialogInterface, i) -> {
                ArrayList<TLRPC.User> arrayList = new ArrayList<>();
                arrayList.add(user);
                profileActivity.getAccountInstance().getContactsController().deleteContact(arrayList, true);
                if (user != null) {
                    user.contact = false;
                    adapter.updateListAnimated(false);
                }
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            AlertDialog dialog = builder.create();
            profileActivity.showDialog(dialog);
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(colorsUtils.getThemedColor(Theme.key_text_RedBold));
            }
        } else if (id == ProfileParams.leave_group) {
            clicksAndPress.leaveChatPressed();
        } else if (id == ProfileParams.delete_topic) {
            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getContext());
            builder.setTitle(LocaleController.getPluralString("DeleteTopics", 1));
            TLRPC.TL_forumTopic topic = MessagesController.getInstance(UserConfig.selectedAccount).getTopicsController().findTopic(attributesHolder.getChatId(), attributesHolder.getTopicId());
            builder.setMessage(formatString("DeleteSelectedTopic", R.string.DeleteSelectedTopic, topic == null ? "topic" : topic.title));
            builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
                ArrayList<Integer> topicIds = new ArrayList<>();
                topicIds.add((int) attributesHolder.getTopicId());
                profileActivity.getMessagesController().getTopicsController().deleteTopics(attributesHolder.getChatId(), topicIds);
                components.getRowsAndStatusComponentsHolder().setPlayProfileAnimation(0);
                if (profileActivity.getParentLayout() != null && profileActivity.getParentLayout().getFragmentStack() != null) {
                    for (int i = 0; i < profileActivity.getParentLayout().getFragmentStack().size(); ++i) {
                        BaseFragment fragment = profileActivity.getParentLayout().getFragmentStack().get(i);
                        if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getTopicId() == attributesHolder.getTopicId()) {
                            fragment.removeSelfFromStack();
                        }
                    }
                }
                profileActivity.finishFragment();

                Context context = profileActivity.getContext();
                if (context != null) {
                    BulletinFactory.of(Bulletin.BulletinWindow.make(context), attributesHolder.getResourcesProvider()).createSimpleBulletin(R.raw.ic_delete, LocaleController.getPluralString("TopicsDeleted", 1)).show();
                }
                dialog.dismiss();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        } else if (id == ProfileParams.report) {
            //ReportBottomSheet.openChat(profileActivity, profileActivity.getDialogId());
            clicksAndPress.onProfileButtonClicked(ButtonViewEnum.REPORT);
        } else if (id == ProfileParams.edit_channel) {
            if (rowsHolder.isTopic()) {
                Bundle args = new Bundle();
                args.putLong("chat_id", attributesHolder.getChatId());
                TopicCreateFragment fragment = TopicCreateFragment.create(attributesHolder.getChatId(), attributesHolder.getTopicId());
                profileActivity.presentFragment(fragment);
            } else {
                Bundle args = new Bundle();
                if (attributesHolder.getChatId() != 0) {
                    args.putLong("chat_id", attributesHolder.getChatId());
                } else if (rowsHolder.isBot()) {
                    args.putLong("user_id", attributesHolder.getUserId());
                }
                ChatEditActivity fragment = new ChatEditActivity(args);
                if (attributesHolder.getChatInfo() != null) {
                    fragment.setInfo(attributesHolder.getChatInfo());
                } else {
                    fragment.setInfo(attributesHolder.getUserInfo());
                }
                profileActivity.presentFragment(fragment);
            }
        } else if (id == ProfileParams.edit_profile) {
            profileActivity.presentFragment(new UserInfoActivity());
        } else if (id == ProfileParams.invite_to_group) {
            final TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (user == null) {
                return;
            }
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_ADD_USERS_TO);
            args.putBoolean("resetDelegate", false);
            args.putBoolean("closeFragment", false);
//                    args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupAlertText", R.string.AddToTheGroupAlertText, UserObject.getUserName(user), "%1$s"));
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, topicsFragment) -> {
                long did = dids.get(0).dialogId;

                TLRPC.Chat chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-did);
                if (chat != null && (chat.creator || chat.admin_rights != null && chat.admin_rights.add_admins)) {
                    profileActivity.getMessagesController().checkIsInChat(false, chat, user, (isInChatAlready, rightsAdmin, currentRank) -> AndroidUtilities.runOnUIThread(() -> {
                        ChatRightsEditActivity editRightsActivity = new ChatRightsEditActivity(attributesHolder.getUserId(), -did, rightsAdmin, null, null, currentRank, ChatRightsEditActivity.TYPE_ADD_BOT, true, !isInChatAlready, null);
                        editRightsActivity.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
                            @Override
                            public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                                rowsHolder.setDisableProfileAnimation(true);
                                fragment.removeSelfFromStack();
                                profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.closeChats);
                                profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                            }

                            @Override
                            public void didChangeOwner(TLRPC.User user) {
                            }
                        });
                        profileActivity.presentFragment(editRightsActivity);
                    }));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
                    builder.setTitle(LocaleController.getString(R.string.AddBot));
                    String chatName = chat == null ? "" : chat.title;
                    builder.setMessage(AndroidUtilities.replaceTags(formatString("AddMembersAlertNamesText", R.string.AddMembersAlertNamesText, UserObject.getUserName(user), chatName)));
                    builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString(R.string.AddBot), (di, i) -> {
                        rowsHolder.setDisableProfileAnimation(true);

                        Bundle args1 = new Bundle();
                        args1.putBoolean("scrollToTopOnResume", true);
                        args1.putLong("chat_id", -did);
                        if (!profileActivity.getMessagesController().checkCanOpenChat(args1, fragment1)) {
                            return;
                        }
                        ChatActivity chatActivity = new ChatActivity(args1);
                        profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.closeChats);
                        profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                        profileActivity.getMessagesController().addUserToChat(-did, user, 0, null, chatActivity, true, null, null);
                        profileActivity.presentFragment(chatActivity, true);
                    });
                    profileActivity.showDialog(builder.create());
                }
                return true;
            });
            profileActivity.presentFragment(fragment);
        } else if (id == ProfileParams.share) {
            clicksAndPress.onProfileButtonClicked(ButtonViewEnum.SHARE);
        } else if (id == ProfileParams.add_shortcut) {
            try {
                long did;
                if (attributesHolder.getCurrentEncryptedChat() != null) {
                    did = DialogObject.makeEncryptedDialogId(attributesHolder.getCurrentEncryptedChat().id);
                } else if (attributesHolder.getUserId() != 0) {
                    did = attributesHolder.getUserId();
                } else if (attributesHolder.getChatId() != 0) {
                    did = -attributesHolder.getChatId();
                } else {
                    return;
                }
                profileActivity.getMediaDataController().installShortcut(did, MediaDataController.SHORTCUT_TYPE_USER_OR_CHAT);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (id == ProfileParams.call_item || id == ProfileParams.video_call_item) {
            if (attributesHolder.getUserId() != 0) {
                TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                if (user != null) {
                    clicksAndPress.onProfileButtonClicked(id == ProfileParams.video_call_item ? ButtonViewEnum.VIDEO : ButtonViewEnum.CALL);
                }
            } else if (attributesHolder.getChatId() != 0) {
                clicksAndPress.onProfileButtonClicked(ButtonViewEnum.LIVE_STREAM);
            }
        } else if (id == ProfileParams.search_members) {
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesHolder.getChatId());
            args.putInt("type", ChatUsersActivity.TYPE_USERS);
            args.putBoolean("open_search", true);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(attributesHolder.getChatInfo());
            profileActivity.presentFragment(fragment);
        } else if (id == ProfileParams.add_member) {
            openAndWrite.openAddMember();
        } else if (id == ProfileParams.statistics) {
            TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
            profileActivity.presentFragment(StatisticActivity.create(chat, false));
        } else if (id == ProfileParams.view_discussion) {
            openAndWrite.openDiscussion();
        } else if (id == ProfileParams.gift_premium) {
            clicksAndPress.onProfileButtonClicked(ButtonViewEnum.GIFT);
        } else if (id == ProfileParams.channel_stories) {
            Bundle args = new Bundle();
            args.putInt("type", MediaActivity.TYPE_ARCHIVED_CHANNEL_STORIES);
            args.putLong("dialog_id", -attributesHolder.getChatId());
            MediaActivity fragment = new MediaActivity(args, null);
            fragment.setChatInfo(attributesHolder.getChatInfo());
            profileActivity.presentFragment(fragment);
        } else if (id == ProfileParams.start_secret_chat) {
            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
            builder.setTitle(LocaleController.getString(R.string.AreYouSureSecretChatTitle));
            builder.setMessage(LocaleController.getString(R.string.AreYouSureSecretChat));
            builder.setPositiveButton(LocaleController.getString(R.string.Start), (dialogInterface, i) -> {
                if (MessagesController.getInstance(UserConfig.selectedAccount).isFrozen()) {
                    AccountFrozenAlert.show(UserConfig.selectedAccount);
                    return;
                }
                rowsHolder.setCreatingChat(true);
                profileActivity.getAccountInstance().getSecretChatHelper().startSecretChat(profileActivity.getParentActivity(), profileActivity.getMessagesController().getUser(attributesHolder.getUserId()));
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            profileActivity.showDialog(builder.create());
        } else if (id == ProfileParams.bot_privacy) {
            BotWebViewAttachedSheet.openPrivacy(UserConfig.selectedAccount, attributesHolder.getUserId());
        } else if (id == ProfileParams.gallery_menu_save) {
            if (profileActivity.getParentActivity() == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && profileActivity.getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                profileActivity.getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                return;
            }
            ImageLocation location = attributesHolder.getAvatarsViewPager().getImageLocation(attributesHolder.getAvatarsViewPager().getRealPosition());
            if (location == null) {
                return;
            }
            final boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
            File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(location.location, isVideo ? "mp4" : null, true);
            if (isVideo && !f.exists()) {
                f = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_IMAGE), FileLoader.getAttachFileName(location.location, "mp4"));
            }
            if (f.exists()) {
                MediaController.saveFile(f.toString(), profileActivity.getParentActivity(), 0, null, null, uri -> {
                    if (profileActivity.getParentActivity() == null) {
                        return;
                    }
                    BulletinFactory.createSaveToGalleryBulletin(profileActivity, isVideo, null).show();
                });
            }
        } else if (id == ProfileParams.edit_info) {
            profileActivity.presentFragment(new UserInfoActivity());
        } else if (id == ProfileParams.edit_color) {
            if (!profileActivity.getUserConfig().isPremium()) {
                profileActivity.showDialog(new PremiumFeatureBottomSheet(profileActivity, PremiumPreviewFragment.PREMIUM_FEATURE_NAME_COLOR, true));
                return;
            }
            profileActivity.presentFragment(new PeerColorActivity(0).startOnProfile().setOnApplied(profileActivity));
        } else if (id == ProfileParams.copy_link_profile) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            AndroidUtilities.addToClipboard(profileActivity.getMessagesController().linkPrefix + "/" + UserObject.getPublicUsername(user));
        } else if (id == ProfileParams.set_username) {
            profileActivity.presentFragment(new ChangeUsernameActivity());
        } else if (id == ProfileParams.logout) {
            profileActivity.presentFragment(new LogoutActivity());
        } else if (id == ProfileParams.set_as_main) {
            int position = attributesHolder.getAvatarsViewPager().getRealPosition();
            TLRPC.Photo photo = attributesHolder.getAvatarsViewPager().getPhoto(position);
            if (photo == null) {
                return;
            }
            attributesHolder.getAvatarsViewPager().startMovePhotoToBegin(position);

            TLRPC.TL_photos_updateProfilePhoto req = new TLRPC.TL_photos_updateProfilePhoto();
            req.id = new TLRPC.TL_inputPhoto();
            req.id.id = photo.id;
            req.id.access_hash = photo.access_hash;
            req.id.file_reference = photo.file_reference;
            UserConfig userConfig = profileActivity.getUserConfig();
            profileActivity.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                attributesHolder.getAvatarsViewPager().finishSettingMainPhoto();
                if (response instanceof TLRPC.TL_photos_photo) {
                    TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                    profileActivity.getMessagesController().putUsers(photos_photo.users, false);
                    TLRPC.User user = profileActivity.getMessagesController().getUser(userConfig.clientUserId);
                    if (photos_photo.photo instanceof TLRPC.TL_photo) {
                        attributesHolder.getAvatarsViewPager().replaceFirstPhoto(photo, photos_photo.photo);
                        if (user != null) {
                            user.photo.photo_id = photos_photo.photo.id;
                            userConfig.setCurrentUser(user);
                            userConfig.saveConfig(true);
                        }
                    }
                }
            }));
            viewsHolder.getUndoView().showWithAction(attributesHolder.getUserId(), UndoView.ACTION_PROFILE_PHOTO_CHANGED, photo.video_sizes.isEmpty() ? null : 1);
            TLRPC.User user = profileActivity.getMessagesController().getUser(userConfig.clientUserId);

            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 800);
            if (user != null) {
                TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 90);
                user.photo.photo_id = photo.id;
                user.photo.photo_small = smallSize.location;
                user.photo.photo_big = bigSize.location;
                userConfig.setCurrentUser(user);
                userConfig.saveConfig(true);
                NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                profileData.updateProfileData(true);
            }
            attributesHolder.getAvatarsViewPager().commitMoveToBegin();
        } else if (id == ProfileParams.edit_avatar) {
            if (MessagesController.getInstance(UserConfig.selectedAccount).isFrozen()) {
                AccountFrozenAlert.show(UserConfig.selectedAccount);
                return;
            }
            int position = attributesHolder.getAvatarsViewPager().getRealPosition();
            ImageLocation location = attributesHolder.getAvatarsViewPager().getImageLocation(position);
            if (location == null) {
                return;
            }

            File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(PhotoViewer.getFileLocation(location), PhotoViewer.getFileLocationExt(location), true);
            boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
            String thumb;
            if (isVideo) {
                ImageLocation imageLocation = attributesHolder.getAvatarsViewPager().getRealImageLocation(position);
                thumb = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(PhotoViewer.getFileLocation(imageLocation), PhotoViewer.getFileLocationExt(imageLocation), true).getAbsolutePath();
            } else {
                thumb = null;
            }
            viewsHolder.getImageUpdater().openPhotoForEdit(f.getAbsolutePath(), thumb, 0, isVideo);
        } else if (id == ProfileParams.delete_avatar) {
            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
            ImageLocation location = attributesHolder.getAvatarsViewPager().getImageLocation(attributesHolder.getAvatarsViewPager().getRealPosition());
            if (location == null) {
                return;
            }
            if (location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                builder.setTitle(LocaleController.getString(R.string.AreYouSureDeleteVideoTitle));
                builder.setMessage(getString(R.string.AreYouSureDeleteVideo));
            } else {
                builder.setTitle(LocaleController.getString(R.string.AreYouSureDeletePhotoTitle));
                builder.setMessage(getString(R.string.AreYouSureDeletePhoto));
            }
            builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialogInterface, i) -> {
                int position = attributesHolder.getAvatarsViewPager().getRealPosition();
                TLRPC.Photo photo = attributesHolder.getAvatarsViewPager().getPhoto(position);
                TLRPC.UserFull userFull = attributesHolder.getUserInfo();
                if (attributesHolder.getAvatar() != null && position == 0) {
                    viewsHolder.getImageUpdater().cancel();
                    if (avatarUploadingRequest != 0) {
                        profileActivity.getConnectionsManager().cancelRequest(avatarUploadingRequest, true);
                    }
                    rowsHolder.setAllowPullingDown(!AndroidUtilities.isTablet() && !rowsHolder.isInLandscapeMode() && attributesHolder.getAvatarImage().getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled());
                    attributesHolder.setAvatar(null);
                    attributesHolder.setAvatarBig(null);
                    attributesHolder.getAvatarsViewPager().scrolledByUser = true;
                    attributesHolder.getAvatarsViewPager().removeUploadingImage(uploadingImageLocation);
                    attributesHolder.getAvatarsViewPager().setCreateThumbFromParent(false);
                    profileData.updateProfileData(true);
                    showAvatarProgress(false, true, avatarProgressView);
                    profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    profileActivity.getUserConfig().saveConfig(true);
                    return;
                }
                if (rowsHolder.isHasFallbackPhoto() && photo != null && userFull != null && userFull.fallback_photo != null && userFull.fallback_photo.id == photo.id) {
                    userFull.fallback_photo = null;
                    userFull.flags &= ~4194304;
                    profileActivity.getMessagesStorage().updateUserInfo(userFull, true);
                    profileData.updateProfileData(false);
                }
                if (attributesHolder.getAvatarsViewPager().getRealCount() == 1) {
                    setForegroundImage(components, true);
                }
                if (photo == null || attributesHolder.getAvatarsViewPager().getRealPosition() == 0) {
                    TLRPC.Photo nextPhoto = attributesHolder.getAvatarsViewPager().getPhoto(1);
                    if (nextPhoto != null) {
                        profileActivity.getUserConfig().getCurrentUser().photo = new TLRPC.TL_userProfilePhoto();
                        TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 90);
                        TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 1000);
                        if (smallSize != null && bigSize != null) {
                            profileActivity.getUserConfig().getCurrentUser().photo.photo_small = smallSize.location;
                            profileActivity.getUserConfig().getCurrentUser().photo.photo_big = bigSize.location;
                        }
                    } else {
                        profileActivity.getUserConfig().getCurrentUser().photo = new TLRPC.TL_userProfilePhotoEmpty();
                    }
                    profileActivity.getMessagesController().deleteUserPhoto(null);
                } else {
                    TLRPC.TL_inputPhoto inputPhoto = new TLRPC.TL_inputPhoto();
                    inputPhoto.id = photo.id;
                    inputPhoto.access_hash = photo.access_hash;
                    inputPhoto.file_reference = photo.file_reference;
                    if (inputPhoto.file_reference == null) {
                        inputPhoto.file_reference = new byte[0];
                    }
                    profileActivity.getMessagesController().deleteUserPhoto(inputPhoto);
                    profileActivity.getMessagesStorage().clearUserPhoto(attributesHolder.getUserId(), photo.id);
                }
                if (attributesHolder.getAvatarsViewPager().removePhotoAtIndex(position) || attributesHolder.getAvatarsViewPager().getRealCount() <= 0) {
                    attributesHolder.getAvatarsViewPager().setVisibility(View.GONE);
                    attributesHolder.getAvatarImage().setForegroundAlpha(1f);
                    viewsHolder.getAvatarContainer().setVisibility(View.VISIBLE);
                    rowsHolder.setDoNotSetForeground(true);
                    final View view = viewsHolder.getLayoutManager().findViewByPosition(0);
                    if (view != null) {
                        listView.getClippedList().smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT), CubicBezierInterpolator.EASE_OUT_QUINT);
                    }
                }
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            profileActivity.showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(colorsUtils.getThemedColor(Theme.key_text_RedBold));
            }
        } else if (id == ProfileParams.add_photo) {
            openAndWrite.onWriteButtonClick();
        } else if (id == ProfileParams.qr_button) {
            if (viewsHolder.getQrItem() != null && viewsHolder.getQrItem().getAlpha() > 0) {
                Bundle args = new Bundle();
                args.putLong("chat_id", attributesHolder.getChatId());
                args.putLong("user_id", attributesHolder.getUserId());
                profileActivity.presentFragment(new QrActivity(args));
            }
        }
    }
    public void createAutoDeleteItem(Context context) {
        checkForAttributesHolder();
        checkForColorUtils();
        AutoDeletePopupWrapper autoDeletePopupWrapper = new AutoDeletePopupWrapper(context, viewsHolder.getOtherItem().getPopupLayout().getSwipeBack(), new AutoDeletePopupWrapper.Callback() {

            @Override
            public void dismiss() {
                viewsHolder.getOtherItem().toggleSubMenu();
            }

            @Override
            public void setAutoDeleteHistory(int time, int action) {
                profileData.setAutoDeleteHistory(profileActivity, time, action);
            }

            @Override
            public void showGlobalAutoDeleteScreen() {
                profileActivity.presentFragment(new AutoDeleteMessagesActivity());
                dismiss();
            }
        }, false, 0, attributesHolder.getResourcesProvider());
        viewsHolder.setAutoDeletePopupWrapper(autoDeletePopupWrapper);
        if (attributesHolder.getDialogId() > 0 || attributesHolder.getUserId() > 0) {
            int linkColor = dontApplyPeerColor(colorsUtils.getThemedColor(Theme.key_windowBackgroundWhiteBlueText), false);
            viewsHolder.getAutoDeletePopupWrapper().allowExtendedHint(linkColor);
        }
        int ttl = 0;
        if (attributesHolder.getUserInfo() != null || attributesHolder.getChatInfo() != null) {
            ttl = attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().ttl_period : attributesHolder.getChatInfo().ttl_period;
        }
        viewsHolder.setAutoDeleteItemDrawable(TimerDrawable.getTtlIcon(ttl));
        viewsHolder.setAutoDeleteItem(viewsHolder.getOtherItem().addSwipeBackItem(0, viewsHolder.getAutoDeleteItemDrawable(), LocaleController.getString(R.string.AutoDeletePopupTitle), viewsHolder.getAutoDeletePopupWrapper().windowLayout));
        viewsHolder.getOtherItem().addColoredGap();
        updateAutoDeleteItem(components);
    }

    private void checkForColorUtils() {
        if (colorsUtils == null) {
            colorsUtils = components.getColorsUtils();
        }
    }

    private void checkForAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = components.getAttributesComponentsHolder();
        }
    }

    public void createActionBarMenu(boolean animated) {
        checkForViewsHolder();
        checkForAttributesHolder();
        checkForRowsHolder();
        checkForClickAndPress();
        if (profileActivity.getActionBar() == null || viewsHolder.getOtherItem() == null) {
            return;
        }
        clicksAndPress.updateProfileButtons(animated);
        List<ButtonViewEnum> extras = viewsHolder.getButtonsGroup() != null ? viewsHolder.getButtonsGroup().getExtraButtons() : Collections.emptyList();
        Context context = profileActivity.getActionBar().getContext();
        viewsHolder.getOtherItem().removeAllSubItems();
        viewsHolder.setAnimatingItem(null);

        rowsHolder.setEditItemVisible(false);
        rowsHolder.setCallItemVisible(false);
        rowsHolder.setVideoCallItemVisible(false);
        rowsHolder.setCanSearchMembers(false);
        boolean selfUser = false;

        if (attributesHolder.getUserId() != 0) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (user == null) {
                return;
            }
            if (UserObject.isUserSelf(user)) {
                rowsHolder.setEditItemVisible(attributesHolder.isMyProfile());
                viewsHolder.getOtherItem().addSubItem(ProfileParams.edit_info, R.drawable.msg_edit, LocaleController.getString(R.string.EditInfo));
                if (viewsHolder.getImageUpdater() != null) {
                    viewsHolder.getOtherItem().addSubItem(ProfileParams.add_photo, R.drawable.msg_addphoto, LocaleController.getString(R.string.AddPhoto));
                }
                viewsHolder.setEditColorItem(viewsHolder.getOtherItem().addSubItem(ProfileParams.edit_color, R.drawable.menu_profile_colors, LocaleController.getString(R.string.ProfileColorEdit)));
                components.getColorsUtils().updateEditColorIcon(profileActivity);
                if (attributesHolder.isMyProfile()) {
                    viewsHolder.setSetUsernameItem(viewsHolder.getOtherItem().addSubItem(ProfileParams.set_username, R.drawable.menu_username_change, getString(R.string.ProfileUsernameEdit)));
                    viewsHolder.setLinkItem(viewsHolder.getOtherItem().addSubItem(ProfileParams.copy_link_profile, R.drawable.msg_link2, getString(R.string.ProfileCopyLink)));
                    components.getProfileData().updateItemsUsername(profileActivity);
                }
                selfUser = true;
            } else {
                if (user.bot && user.bot_can_edit) {
                    rowsHolder.setEditItemVisible(true);
                }

                if (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().phone_calls_available) {
                    rowsHolder.setCallItemVisible(extras.contains(ButtonViewEnum.CALL));
                    rowsHolder.setVideoCallItemVisible(attributesHolder.getUserInfo().video_calls_available && extras.contains(ButtonViewEnum.VIDEO));
                }
                if (rowsHolder.isBot() || profileActivity.getAccountInstance().getContactsController().contactsDict.get(attributesHolder.getUserId()) == null) {
                    if (MessagesController.isSupportUser(user)) {
                        if (rowsHolder.isUserBlocked()) {
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.block_contact, R.drawable.msg_block, LocaleController.getString(R.string.Unblock));
                        }
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                    } else if (profileActivity.getDialogId() != UserObject.VERIFY) {
                        if (attributesHolder.getCurrentEncryptedChat() == null) {
                            createAutoDeleteItem(context);
                        }
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                        if (rowsHolder.isBot()) {
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.share, R.drawable.msg_share, LocaleController.getString(R.string.BotShare));
                        } else {
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.add_contact, R.drawable.msg_addcontact, LocaleController.getString(R.string.AddContact));
                        }
                        if (!TextUtils.isEmpty(user.phone)) {
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.share_contact, R.drawable.msg_share, LocaleController.getString(R.string.ShareContact));
                        }
                        if (rowsHolder.isBot()) {
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.bot_privacy, R.drawable.menu_privacy_policy, getString(R.string.BotPrivacyPolicy));
                            if (profileData.hasPrivacyCommand()) {
                                viewsHolder.getOtherItem().showSubItem(ProfileParams.bot_privacy);
                            } else {
                                viewsHolder.getOtherItem().hideSubItem(ProfileParams.bot_privacy);
                            }
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.report, R.drawable.msg_report, LocaleController.getString(R.string.ReportBot)).setColors(colorsUtils.getThemedColor(Theme.key_text_RedRegular), colorsUtils.getThemedColor(Theme.key_text_RedRegular));
                            viewsHolder.getOtherItem().setSubItemShown(ProfileParams.report, extras.contains(ButtonViewEnum.REPORT));
                            if (!rowsHolder.isUserBlocked()) {
                                viewsHolder.getOtherItem().addSubItem(ProfileParams.block_contact, R.drawable.msg_block2, LocaleController.getString(R.string.DeleteAndBlock)).setColors(colorsUtils.getThemedColor(Theme.key_text_RedRegular), colorsUtils.getThemedColor(Theme.key_text_RedRegular));
                                viewsHolder.getOtherItem().setSubItemShown(ProfileParams.block_contact, extras.contains(ButtonViewEnum.BLOCK));
                            } else {
                                viewsHolder.getOtherItem().addSubItem(ProfileParams.block_contact, R.drawable.msg_retry, LocaleController.getString(R.string.BotRestart));
                            }
                        } else {
                            viewsHolder.getOtherItem().addSubItem(ProfileParams.block_contact, R.drawable.msg_block, !rowsHolder.isUserBlocked() ? LocaleController.getString(R.string.BlockContact) : LocaleController.getString(R.string.Unblock));
                        }
                    }
                } else {
                    if (attributesHolder.getCurrentEncryptedChat() == null) {
                        createAutoDeleteItem(context);
                    }
                    if (!TextUtils.isEmpty(user.phone)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.share_contact, R.drawable.msg_share, LocaleController.getString(R.string.ShareContact));
                    }
                    viewsHolder.getOtherItem().addSubItem(ProfileParams.block_contact, R.drawable.msg_block, !rowsHolder.isUserBlocked() ? LocaleController.getString(R.string.BlockContact) : LocaleController.getString(R.string.Unblock));
                    viewsHolder.getOtherItem().addSubItem(ProfileParams.edit_contact, R.drawable.msg_edit, LocaleController.getString(R.string.EditContact));
                    viewsHolder.getOtherItem().addSubItem(ProfileParams.delete_contact, R.drawable.msg_delete, LocaleController.getString(R.string.DeleteContact));
                }
                if (!UserObject.isDeleted(user) && !rowsHolder.isBot() && attributesHolder.getCurrentEncryptedChat() == null && !rowsHolder.isUserBlocked() && attributesHolder.getUserId() != 333000 && attributesHolder.getUserId() != 777000 && attributesHolder.getUserId() != 42777) {
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !user.self && !user.bot && !MessagesController.isSupportUser(user) && !profileActivity.getMessagesController().premiumPurchaseBlocked() && extras.contains(ButtonViewEnum.GIFT)) {
                        StarsController.getInstance(UserConfig.selectedAccount).loadStarGifts();
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.gift_premium, R.drawable.msg_gift_premium, LocaleController.getString(R.string.ProfileSendAGift));
                    }
                    viewsHolder.getOtherItem().addSubItem(ProfileParams.start_secret_chat, R.drawable.msg_secret, LocaleController.getString(R.string.StartEncryptedChat));
                    viewsHolder.getOtherItem().setSubItemShown(ProfileParams.start_secret_chat, DialogObject.isEmpty(profileActivity.getMessagesController().isUserContactBlocked(attributesHolder.getUserId())));
                }
                if (!rowsHolder.isBot() && profileActivity.getAccountInstance().getContactsController().contactsDict.get(attributesHolder.getUserId()) != null) {
                    viewsHolder.getOtherItem().addSubItem(ProfileParams.add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
                }
            }
        } else if (attributesHolder.getChatId() != 0) {
            TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
            rowsHolder.setHasVoiceChatItem(false);

            if (attributesHolder.getTopicId() == 0 && ChatObject.canChangeChatInfo(chat)) {
                createAutoDeleteItem(context);
            }
            if (ChatObject.isChannel(chat)) {
                if (rowsHolder.isTopic()) {
                    if (ChatObject.canManageTopic(UserConfig.selectedAccount, chat, attributesHolder.getTopicId())) {
                        rowsHolder.setEditItemVisible(true);
                    }
                } else {
                    if (ChatObject.hasAdminRights(chat) || chat.megagroup && ChatObject.canChangeChatInfo(chat)) {
                        rowsHolder.setEditItemVisible(true);
                    }
                }
                if (attributesHolder.getChatInfo() != null) {
                    if (ChatObject.canManageCalls(chat) && attributesHolder.getChatInfo().call == null && extras.contains(ButtonViewEnum.LIVE_STREAM)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.call_item, R.drawable.msg_voicechat, chat.megagroup && !chat.gigagroup ? LocaleController.getString(R.string.StartVoipChat) : LocaleController.getString(R.string.StartVoipChannel));
                        rowsHolder.setHasVoiceChatItem(true);
                    }
                    if ((attributesHolder.getChatInfo().can_view_stats || attributesHolder.getChatInfo().can_view_revenue || attributesHolder.getChatInfo().can_view_stars_revenue || profileActivity.getMessagesController().getStoriesController().canPostStories(profileActivity.getDialogId())) && attributesHolder.getTopicId() == 0) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.statistics, R.drawable.msg_stats, LocaleController.getString(R.string.Statistics));
                    }
                    ChatObject.Call call = profileActivity.getMessagesController().getGroupCall(attributesHolder.getChatId(), false);
                    rowsHolder.setCallItemVisible(call != null && extras.contains(ButtonViewEnum.LIVE_STREAM));
                }
                if (chat.megagroup) {
                    if (attributesHolder.getChatInfo() == null || !attributesHolder.getChatInfo().participants_hidden || ChatObject.hasAdminRights(chat)) {
                        rowsHolder.setCanSearchMembers(true);
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.search_members, R.drawable.msg_search, LocaleController.getString(R.string.SearchMembers));
                    }
                    if (!chat.creator && !chat.left && !chat.kicked && !rowsHolder.isTopic() && extras.contains(ButtonViewEnum.LEAVE)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.LeaveMegaMenu));
                    }
                    if (rowsHolder.isTopic() && ChatObject.canDeleteTopic(UserConfig.selectedAccount, chat, attributesHolder.getTopicId())) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.delete_topic, R.drawable.msg_delete, LocaleController.getPluralString("DeleteTopics", 1));
                    }
                } else {
                    if (chat.creator || chat.admin_rights != null && chat.admin_rights.edit_stories) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.channel_stories, R.drawable.msg_archive, LocaleController.getString(R.string.OpenChannelArchiveStories));
                    }
                    if (ChatObject.isPublic(chat) && extras.contains(ButtonViewEnum.SHARE)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.share, R.drawable.msg_share, LocaleController.getString(R.string.BotShare));
                    }
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !profileActivity.getMessagesController().premiumPurchaseBlocked() && extras.contains(ButtonViewEnum.GIFT)) {
                        StarsController.getInstance(UserConfig.selectedAccount).loadStarGifts();
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.gift_premium, R.drawable.msg_gift_premium, LocaleController.getString(R.string.ProfileSendAGiftToChannel));
                        viewsHolder.getOtherItem().setSubItemShown(ProfileParams.gift_premium, attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().stargifts_available);
                    }
                    if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().linked_chat_id != 0 && extras.contains(ButtonViewEnum.DISCUSS)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.view_discussion, R.drawable.msg_discussion, LocaleController.getString(R.string.ViewDiscussion));
                    }
                    if (!attributesHolder.getCurrentChat().creator && !attributesHolder.getCurrentChat().left && !attributesHolder.getCurrentChat().kicked && extras.contains(ButtonViewEnum.LEAVE)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.LeaveChannelMenu));
                    }
                }
            } else {
                if (attributesHolder.getChatInfo() != null) {
                    if (ChatObject.canManageCalls(chat) && attributesHolder.getChatInfo().call == null && extras.contains(ButtonViewEnum.LIVE_STREAM)) {
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.call_item, R.drawable.msg_voicechat, LocaleController.getString(R.string.StartVoipChat));
                        rowsHolder.setHasVoiceChatItem(true);
                    }
                    ChatObject.Call call = profileActivity.getMessagesController().getGroupCall(attributesHolder.getChatId(), false);
                    rowsHolder.setCallItemVisible(call != null && extras.contains(ButtonViewEnum.LIVE_STREAM));
                }
                if (ChatObject.canChangeChatInfo(chat)) {
                    rowsHolder.setEditItemVisible(true);
                }
                if (!ChatObject.isKickedFromChat(chat) && !ChatObject.isLeftFromChat(chat)) {
                    if (attributesHolder.getChatInfo() == null || !attributesHolder.getChatInfo().participants_hidden || ChatObject.hasAdminRights(chat)) {
                        rowsHolder.setCanSearchMembers(true);
                        viewsHolder.getOtherItem().addSubItem(ProfileParams.search_members, R.drawable.msg_search, LocaleController.getString(R.string.SearchMembers));
                    }
                }
                viewsHolder.getOtherItem().addSubItem(ProfileParams.leave_group, R.drawable.msg_leave, LocaleController.getString(R.string.DeleteAndExit));
            }
            if (attributesHolder.getTopicId() == 0) {
                viewsHolder.getOtherItem().addSubItem(ProfileParams.add_shortcut, R.drawable.msg_home, LocaleController.getString(R.string.AddShortcut));
            }
        }

        if (viewsHolder.getImageUpdater() != null) {
            viewsHolder.getOtherItem().addSubItem(ProfileParams.set_as_main, R.drawable.msg_openprofile, LocaleController.getString(R.string.SetAsMain));
            viewsHolder.getOtherItem().addSubItem(ProfileParams.gallery_menu_save, R.drawable.msg_gallery, LocaleController.getString(R.string.SaveToGallery));
            //viewsHolder.getOtherItem().addSubItem(ProfileParams.edit_avatar, R.drawable.photo_paint, LocaleController.getString(R.string.EditPhoto));
            viewsHolder.getOtherItem().addSubItem(ProfileParams.delete_avatar, R.drawable.msg_delete, LocaleController.getString(R.string.Delete));
        } else {
            viewsHolder.getOtherItem().addSubItem(ProfileParams.gallery_menu_save, R.drawable.msg_gallery, LocaleController.getString(R.string.SaveToGallery));
        }
        if (profileActivity.getMessagesController().isChatNoForwards(attributesHolder.getCurrentChat())) {
            viewsHolder.getOtherItem().hideSubItem(ProfileParams.gallery_menu_save);
        }

        if (selfUser && !attributesHolder.isMyProfile()) {
            viewsHolder.getOtherItem().addSubItem(ProfileParams.logout, R.drawable.msg_leave, LocaleController.getString(R.string.LogOut));
        }
        if (!rowsHolder.isPulledDown()) {
            viewsHolder.getOtherItem().hideSubItem(ProfileParams.gallery_menu_save);
            viewsHolder.getOtherItem().hideSubItem(ProfileParams.set_as_main);
            viewsHolder.getOtherItem().showSubItem(ProfileParams.add_photo);
            viewsHolder.getOtherItem().hideSubItem(ProfileParams.edit_avatar);
            viewsHolder.getOtherItem().hideSubItem(ProfileParams.delete_avatar);
        }
        if (!rowsHolder.isMediaHeaderVisible()) {
            if (rowsHolder.isCallItemVisible()) {
                if (viewsHolder.getCallItem().getVisibility() != View.VISIBLE) {
                    viewsHolder.getCallItem().setVisibility(View.VISIBLE);
                    if (animated) {
                        viewsHolder.getCallItem().setAlpha(0);
                        viewsHolder.getCallItem().animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (viewsHolder.getCallItem().getVisibility() != View.GONE) {
                    viewsHolder.getCallItem().setVisibility(View.GONE);
                }
            }
            if (rowsHolder.isVideoCallItemVisible()) {
                if (viewsHolder.getVideoCallItem().getVisibility() != View.VISIBLE) {
                    viewsHolder.getVideoCallItem().setVisibility(View.VISIBLE);
                    if (animated) {
                        viewsHolder.getVideoCallItem().setAlpha(0);
                        viewsHolder.getVideoCallItem().animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (viewsHolder.getVideoCallItem().getVisibility() != View.GONE) {
                    viewsHolder.getVideoCallItem().setVisibility(View.GONE);
                }
            }
            if (rowsHolder.isEditItemVisible()) {
                if (viewsHolder.getEditItem().getVisibility() != View.VISIBLE) {
                    viewsHolder.getEditItem().setVisibility(View.VISIBLE);
                    if (animated) {
                        viewsHolder.getEditItem().setAlpha(0);
                        viewsHolder.getEditItem().animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (viewsHolder.getEditItem().getVisibility() != View.GONE) {
                    viewsHolder.getEditItem().setVisibility(View.GONE);
                }
            }
        }
        if (viewsHolder.getAvatarsViewPagerIndicatorView() != null) {
            if (viewsHolder.getAvatarsViewPagerIndicatorView().isIndicatorFullyVisible()) {
                if (rowsHolder.isEditItemVisible()) {
                    viewsHolder.getEditItem().setVisibility(View.GONE);
                    viewsHolder.getEditItem().animate().cancel();
                    viewsHolder.getEditItem().setAlpha(1f);
                }
                if (rowsHolder.isCallItemVisible()) {
                    viewsHolder.getCallItem().setVisibility(View.GONE);
                    viewsHolder.getCallItem().animate().cancel();
                    viewsHolder.getCallItem().setAlpha(1f);
                }
                if (rowsHolder.isVideoCallItemVisible()) {
                    viewsHolder.getVideoCallItem().setVisibility(View.GONE);
                    viewsHolder.getVideoCallItem().animate().cancel();
                    viewsHolder.getVideoCallItem().setAlpha(1f);
                }
            }
        }
        if (viewsHolder.getSharedMediaLayout() != null) {
            viewsHolder.getSharedMediaLayout().getSearchItem().requestLayout();
        }
        updateStoriesViewBounds(viewsHolder, profileActivity, components.getViewComponentsHolder(), false);
    }

    private void checkForClickAndPress() {
        if (clicksAndPress == null) {
            clicksAndPress = components.getClicksAndPress();
        }
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }

    private void checkForViewsHolder() {
        if (viewsHolder == null) {
            viewsHolder = components.getViewComponentsHolder();
        }
    }
}
