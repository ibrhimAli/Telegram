package org.telegram.ui.profile.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.TopicsFragment;
import org.telegram.ui.profile.adapter.ListAdapter;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class OpenAndWrite {

    final private ProfileActivity profileActivity;
    private final Adapter adapter;
    private final Animation animation;
    private ClippedListView listView;
    private final ComponentsFactory components;
    private ViewComponentsHolder viewsHolder;
    private AttributesComponentsHolder attributesHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    public OpenAndWrite(ComponentsFactory components){
        profileActivity = components.getProfileActivity();
        this.components = components;
        this.adapter = components.getAdapter();
        this.animation = components.getAnimation();
        listView = components.getListView();
        viewsHolder = components.getViewComponentsHolder();
        attributesHolder = components.getAttributesComponentsHolder();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
    }

    private final PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview, boolean closing) {
            if (fileLocation == null) {
                return null;
            }

            TLRPC.FileLocation photoBig = null;
            if (attributesHolder.getUserId() != 0) {
                TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    photoBig = user.photo.photo_big;
                }
            } else if (attributesHolder.getChatId() != 0) {
                TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                if (chat != null && chat.photo != null && chat.photo.photo_big != null) {
                    photoBig = chat.photo.photo_big;
                }
            }

            if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int[] coords = new int[2];
                attributesHolder.getAvatarImage().getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                object.parentView = attributesHolder.getAvatarImage();
                object.imageReceiver = attributesHolder.getAvatarImage().getImageReceiver();
                if (attributesHolder.getUserId() != 0) {
                    object.dialogId = attributesHolder.getUserId();
                } else if (attributesHolder.getChatId() != 0) {
                    object.dialogId = -attributesHolder.getChatId();
                }
                object.thumb = object.imageReceiver.getBitmapSafe();
                object.size = -1;
                object.radius = attributesHolder.getAvatarImage().getImageReceiver().getRoundRadius(true);
                object.scale = viewsHolder.getAvatarContainer().getScaleX();
                object.canEdit = attributesHolder.getUserId() == profileActivity.getUserConfig().clientUserId;
                return object;
            }
            return null;
        }

        @Override
        public void willHidePhotoViewer() {
            attributesHolder.getAvatarImage().getImageReceiver().setVisible(true, true);
        }

        @Override
        public void openPhotoForEdit(String file, String thumb, boolean isVideo) {
            viewsHolder.getImageUpdater().openPhotoForEdit(file, thumb, 0, isVideo);
        }
    };

    public void openAddMember() {
        Bundle args = new Bundle();
        args.putBoolean("addToGroup", true);
        args.putLong("chatId", attributesHolder.getCurrentChat().id);
        GroupCreateActivity fragment = new GroupCreateActivity(args);
        fragment.setInfo(attributesHolder.getChatInfo());
        if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().participants != null) {
            LongSparseArray<TLObject> users = new LongSparseArray<>();
            for (int a = 0; a < attributesHolder.getChatInfo().participants.participants.size(); a++) {
                users.put(attributesHolder.getChatInfo().participants.participants.get(a).user_id, null);
            }
            fragment.setIgnoreUsers(users);
        }
        fragment.setDelegate2((users, fwdCount) -> {
            HashSet<Long> currentParticipants = new HashSet<>();
            ArrayList<TLRPC.User> addedUsers = new ArrayList<>();
            if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().participants != null && attributesHolder.getChatInfo().participants.participants != null) {
                for (int i = 0; i < attributesHolder.getChatInfo().participants.participants.size(); i++) {
                    currentParticipants.add(attributesHolder.getChatInfo().participants.participants.get(i).user_id);
                }
            }
            profileActivity.getMessagesController().addUsersToChat(attributesHolder.getCurrentChat(), profileActivity, users, fwdCount, user -> {
                addedUsers.add(user);
            }, restrictedUser -> {
                for (int i = 0; i < attributesHolder.getChatInfo().participants.participants.size(); i++) {
                    if (attributesHolder.getChatInfo().participants.participants.get(i).user_id == restrictedUser.id) {
                        attributesHolder.getChatInfo().participants.participants.remove(i);
                        adapter.updateListAnimated(true);
                        break;
                    }
                }
            }, () -> {
                int N = addedUsers.size();
                int[] finished = new int[1];
                for (int a = 0; a < N; a++) {
                    TLRPC.User user = addedUsers.get(a);
                    if (!currentParticipants.contains(user.id)) {
                        if (attributesHolder.getChatInfo().participants == null) {
                            attributesHolder.getChatInfo().participants = new TLRPC.TL_chatParticipants();
                        }
                        if (ChatObject.isChannel(attributesHolder.getCurrentChat())) {
                            TLRPC.TL_chatChannelParticipant channelParticipant1 = new TLRPC.TL_chatChannelParticipant();
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                            channelParticipant1.channelParticipant.inviter_id = profileActivity.getUserConfig().getClientUserId();
                            channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                            channelParticipant1.channelParticipant.peer.user_id = user.id;
                            channelParticipant1.channelParticipant.date = profileActivity.getConnectionsManager().getCurrentTime();
                            channelParticipant1.user_id = user.id;
                            attributesHolder.getChatInfo().participants.participants.add(channelParticipant1);
                        } else {
                            TLRPC.ChatParticipant participant = new TLRPC.TL_chatParticipant();
                            participant.user_id = user.id;
                            participant.inviter_id = profileActivity.getAccountInstance().getUserConfig().clientUserId;
                            attributesHolder.getChatInfo().participants.participants.add(participant);
                        }
                        attributesHolder.getChatInfo().participants_count++;
                        profileActivity.getMessagesController().putUser(user, false);
                    }
                }
                adapter.updateListAnimated(true);
            });

        });
        profileActivity.presentFragment(fragment);
    }

    public void onWriteButtonClick() {
        checkForViewsHolder();
        checkForAttributesHolder();
        checkForRowsHolder();
        if (attributesHolder.getUserId() != 0) {
            if (viewsHolder.getImageUpdater() != null) {
                TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId());
                if (user == null) {
                    user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                viewsHolder.getImageUpdater().openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> {
                    MessagesController.getInstance(UserConfig.selectedAccount).deleteUserPhoto(null);
                    attributesHolder.getCameraDrawable().setCurrentFrame(0);
                    attributesHolder.getCellCameraDrawable().setCurrentFrame(0);
                }, dialog -> {
                    if (!viewsHolder.getImageUpdater().isUploadingImage()) {
                        attributesHolder.getCameraDrawable().setCustomEndFrame(86);
                        attributesHolder.getCellCameraDrawable().setCustomEndFrame(86);
                        attributesHolder.getWriteButton().playAnimation();
                        if (viewsHolder.getSetAvatarCell() != null) {
                            viewsHolder.getSetAvatarCell().getImageView().playAnimation();
                        }
                    } else {
                        attributesHolder.getCameraDrawable().setCurrentFrame(0, false);
                        attributesHolder.getCellCameraDrawable().setCurrentFrame(0, false);
                    }
                }, 0);
                attributesHolder.getCameraDrawable().setCurrentFrame(0);
                attributesHolder.getCameraDrawable().setCustomEndFrame(43);
                attributesHolder.getCellCameraDrawable().setCurrentFrame(0);
                attributesHolder.getCellCameraDrawable().setCustomEndFrame(43);
                attributesHolder.getWriteButton().playAnimation();
                if (viewsHolder.getSetAvatarCell() != null) {
                    viewsHolder.getSetAvatarCell().getImageView().playAnimation();
                }
            } else {
                if (rowsHolder.getPlayProfileAnimation() != 0 && profileActivity.getParentLayout() != null && profileActivity.getParentLayout().getFragmentStack() != null && profileActivity.getParentLayout().getFragmentStack().size() >= 2 && profileActivity.getParentLayout().getFragmentStack().get(profileActivity.getParentLayout().getFragmentStack().size() - 2) instanceof ChatActivity) {
                    profileActivity.finishFragment();
                } else {
                    TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                    if (user == null || user instanceof TLRPC.TL_userEmpty) {
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putLong("user_id", attributesHolder.getUserId());
                    if (!profileActivity.getMessagesController().checkCanOpenChat(args, profileActivity)) {
                        return;
                    }
                    boolean removeFragment = profileActivity.getArguments().getBoolean("removeFragmentOnChatOpen", true);
                    if (!AndroidUtilities.isTablet() && removeFragment) {
                        profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.closeChats);
                        profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    }
                    int distance = profileActivity.getArguments().getInt("nearby_distance", -1);
                    if (distance >= 0) {
                        args.putInt("nearby_distance", distance);
                    }
                    ChatActivity chatActivity = new ChatActivity(args);
                    chatActivity.setPreloadedSticker(profileActivity.getMediaDataController().getGreetingsSticker(), false);
                    profileActivity.presentFragment(chatActivity, removeFragment);
                    if (AndroidUtilities.isTablet()) {
                        profileActivity.finishFragment();
                    }
                }
            }
        } else {
            openDiscussion();
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

    public void openDiscussion() {
        if (attributesHolder.getChatInfo() == null || attributesHolder.getChatInfo().linked_chat_id == 0) {
            return;
        }
        Bundle args = new Bundle();
        args.putLong("chat_id", attributesHolder.getChatInfo().linked_chat_id);
        if (!profileActivity.getMessagesController().checkCanOpenChat(args, profileActivity)) {
            return;
        }
        profileActivity.presentFragment(new ChatActivity(args));
    }

    public void openLocation(boolean inMapsApp) {
        if (attributesHolder.getUserInfo() == null || attributesHolder.getUserInfo().business_location == null) return;
        if (attributesHolder.getUserInfo().business_location.geo_point != null && !inMapsApp) {
            LocationActivity fragment = new LocationActivity(3) {
                @Override
                protected boolean disablePermissionCheck() {
                    return true;
                }
            };
            fragment.setResourceProvider(attributesHolder.getResourcesProvider());
            TLRPC.TL_message message = new TLRPC.TL_message();
            message.local_id = -1;
            message.peer_id = profileActivity.getMessagesController().getPeer(profileActivity.getDialogId());
            TLRPC.TL_messageMediaGeo media = new TLRPC.TL_messageMediaGeo();
            media.geo = attributesHolder.getUserInfo().business_location.geo_point;
            media.address = attributesHolder.getUserInfo().business_location.address;
            message.media = media;
            fragment.setSharingAllowed(false);
            fragment.setMessageObject(new MessageObject(UserConfig.selectedAccount, message, false, false));
            profileActivity.presentFragment(fragment);
        } else {
            String domain;
            if (BuildVars.isHuaweiStoreApp()) {
                domain = "mapapp://navigation";
            } else {
                domain = "http://maps.google.com/maps";
            }
//                    if (myLocation != null) {
//                        try {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, domain + "?saddr=%f,%f&daddr=%f,%f", myLocation.getLatitude(), myLocation.getLongitude(), daddrLat, daddrLong)));
//                            getParentActivity().startActivity(intent);
//                        } catch (Exception e) {
//                            FileLog.e(e);
//                        }
//                    } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, domain + "?q=" + attributesHolder.getUserInfo().business_location.address )));
                profileActivity.getParentActivity().startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
//                    }
        }

    }

    public void goToForum() {
        if (profileActivity.getParentLayout() != null && profileActivity.getParentLayout().getFragmentStack() != null) {
            for (int i = 0; i < profileActivity.getParentLayout().getFragmentStack().size(); ++i) {
                BaseFragment fragment = profileActivity.getParentLayout().getFragmentStack().get(i);
                if (fragment instanceof DialogsActivity) {
                    if (((DialogsActivity) fragment).rightSlidingDialogContainer != null) {
                        BaseFragment previewFragment = ((DialogsActivity) fragment).rightSlidingDialogContainer.getFragment();
                        if (previewFragment instanceof TopicsFragment && ((TopicsFragment) previewFragment).getDialogId() == profileActivity.getDialogId()) {
                            ((DialogsActivity) fragment).rightSlidingDialogContainer.finishPreview();
                        }
                    }
                } else if (fragment instanceof ChatActivity) {
                    if (((ChatActivity) fragment).getDialogId() == profileActivity.getDialogId()) {
                        profileActivity.getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof TopicsFragment) {
                    if (((TopicsFragment) fragment).getDialogId() == profileActivity.getDialogId()) {
                        profileActivity.getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof ProfileActivity) {
                    if (fragment != profileActivity /*&& ((ProfileActivity) fragment).getDialogId() == getDialogId() */&& rowsHolder.isTopic()) {
                        profileActivity.getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                }
            }
        }

        rowsHolder.setPlayProfileAnimation(0);

        Bundle args = new Bundle();
        args.putLong("chat_id", attributesHolder.getChatId());
        profileActivity.presentFragment(TopicsFragment.getTopicsOrChat(profileActivity, args));
    }

    public void openAvatar() {
        checkForViewsHolder();
        checkForAttributesHolder();
        checkForRowsHolder();
        if (listView == null) {
            listView = components.getListView();
        }
        if (listView.getClippedList().getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }
        if (attributesHolder.getUserId() != 0) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity( profileActivity);
                if (user.photo.dc_id != 0) {
                    user.photo.photo_big.dc_id = user.photo.dc_id;
                }
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
            }
        } else if (attributesHolder.getChatId() != 0) {
            TLRPC.Chat chat =  profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
            if (chat.photo != null && chat.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity( profileActivity);
                if (chat.photo.dc_id != 0) {
                    chat.photo.photo_big.dc_id = chat.photo.dc_id;
                }
                ImageLocation videoLocation;
                if (attributesHolder.getChatInfo() != null && (attributesHolder.getChatInfo().chat_photo instanceof TLRPC.TL_photo) && !attributesHolder.getChatInfo().chat_photo.video_sizes.isEmpty()) {
                    videoLocation = ImageLocation.getForPhoto(attributesHolder.getChatInfo().chat_photo.video_sizes.get(0), attributesHolder.getChatInfo().chat_photo);
                } else {
                    videoLocation = null;
                }
                PhotoViewer.getInstance().openPhotoWithVideo(chat.photo.photo_big, videoLocation, provider);
            }
        }
    }

    public void openAddToContact(ListAdapter listAdapter, Bundle args,
                                        TLRPC.User user, float photoDescriptionProgress, float customAvatarProgress,
                                        float animatedFracture) {
        ContactAddActivity contactAddActivity = new ContactAddActivity(args, attributesHolder.getResourcesProvider());
        contactAddActivity.setDelegate(() -> {
            int currentAddToContactsRow = rowsHolder.getAddToContactsRow();
            if (currentAddToContactsRow >= 0) {
                if (rowsHolder.getSharedMediaRow() == -1) {
                    adapter.updateRowsIds();
                    listAdapter.notifyDataSetChanged();
                } else {
                    adapter.updateListAnimated(false);
                }
            }

            if (rowsHolder.getSharedMediaRow() == -1) {
                if (rowsHolder.isInLandscapeMode() || AndroidUtilities.isTablet()) {
                    listView.getClippedList().setPadding(0, AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT), 0, 0);
                    attributesHolder.getExpandAnimator().cancel();
                    attributesHolder.getExpandAnimatorValues()[0] = 1f;
                    attributesHolder.getExpandAnimatorValues()[1] = 0f;
                    animation.setAvatarExpandProgress(profileActivity,photoDescriptionProgress, customAvatarProgress, animatedFracture);
                    rowsHolder.setExtraHeight(AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
                } else {
                    final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                    int ws = View.MeasureSpec.makeMeasureSpec(listView.getClippedList().getMeasuredWidth(), View.MeasureSpec.EXACTLY);
                    int hs = View.MeasureSpec.makeMeasureSpec(listView.getClippedList().getMeasuredHeight(), View.MeasureSpec.UNSPECIFIED);
                    int contentHeight = 0;
                    for (int i = 0; i < listAdapter.getItemCount(); i++) {
                        RecyclerView.ViewHolder holder = listAdapter.createViewHolder(null, listAdapter.getItemViewType(i));
                        listAdapter.onBindViewHolder(holder, i);
                        holder.itemView.measure(ws, hs);
                        contentHeight += holder.itemView.getMeasuredHeight();
                    }
                    int paddingBottom = Math.max(0, profileActivity.fragmentView.getMeasuredHeight() - (contentHeight + AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) + actionBarHeight));
                    listView.getClippedList().setPadding(0, listView.getClippedList().getPaddingTop(), 0, paddingBottom);
                }
            }
            viewsHolder.getUndoView().showWithAction(attributesHolder.getDialogId(), UndoView.ACTION_CONTACT_ADDED, user);
        });
        profileActivity.presentFragment(contactAddActivity);
    }


}
