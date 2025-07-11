package org.telegram.ui.profile.builder;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.style.CharacterStyle;
import android.util.SparseIntArray;

import androidx.annotation.Keep;

import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.ScamDrawable;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.SelectAnimatedEmojiDialog;
import org.telegram.ui.Stories.recorder.HintView2;
import org.telegram.ui.profile.AvatarImageView;

import java.util.ArrayList;
import java.util.HashSet;

public class AttributesComponentsHolder {

    private float photoDescriptionProgress = -1;
    private float customAvatarProgress;
    private boolean openCommonChats;
    private boolean openGifts;
    private boolean openedGifts;
    private boolean openSimilar;
    private Theme.ResourcesProvider resourcesProvider;
    private TLRPC.ChatFull chatInfo;
    private TLRPC.UserFull userInfo;
    private TLRPC.Chat currentChat;
    private int rowCount;
    private SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;
    private long userId;
    private long dialogId;
    private long topicId;
    private long chatId;
    private boolean myProfile;
    private CharSequence currentBio;
    private CharacterStyle loadingSpan;
    private HashSet<Integer> notificationsExceptionTopics = new HashSet<>();
    private final ArrayList<TLRPC.ChatParticipant> visibleChatParticipants = new ArrayList<>();
    private final ArrayList<Integer> visibleSortedUsers = new ArrayList<>();
    private MessagesController.PeerColor peerColor;
    private ChatActivityInterface previousTransitionFragment;
    private TLRPC.FileLocation avatar;
    private final boolean[] isOnline = new boolean[1];
    private String nameTextViewRightDrawable2ContentDescription = null;
    private String nameTextViewRightDrawableContentDescription = null;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] emojiStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[2];
    private final float[] expandAnimatorValues = new float[]{0f, 1f};
    private final CrossfadeDrawable[] verifiedCrossfadeDrawable = new CrossfadeDrawable[2];
    private final CrossfadeDrawable[] premiumCrossfadeDrawable = new CrossfadeDrawable[2];
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] botVerificationDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[2];
    private final SimpleTextView[] nameTextView = new SimpleTextView[2];
    private final SimpleTextView[] onlineTextView = new SimpleTextView[4];
    private final SparseIntArray adaptedColors = new SparseIntArray();
    private final Drawable[] verifiedDrawable = new Drawable[2];
    private final Drawable[] verifiedCheckDrawable = new Drawable[2];
    private final Drawable[] premiumStarDrawable = new Drawable[2];
    private RLottieDrawable cellCameraDrawable;
    private ArrayList<Integer> sortedUsers;
    private TLRPC.EncryptedChat currentEncryptedChat;
    private TLRPC.FileLocation avatarBig;
    private ProfileGalleryView avatarsViewPager;
    private RLottieDrawable cameraDrawable;
    private RLottieImageView writeButton;
    private Animator searchViewTransition;
    private AnimatorSet writeButtonAnimation;
    private AvatarDrawable avatarDrawable;
    private AvatarImageView avatarImage;
    private ImageLocation prevLoadedImageLocation;
    private ValueAnimator expandAnimator;
    private float pullUpProgress;
    private float groupButtonTransitionProgress = 1f;
    private AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;
    private ScamDrawable scamDrawable;
    private Drawable lockIconDrawable;
    private HintView2 collectibleHint;
    private DrawerProfileCell.AnimatedStatusView animatedStatusView;
    private AnimatorSet qrItemAnimation;
    private TLRPC.TL_emojiStatusCollectible collectibleStatus;
    private SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog;
    private AnimatorSet headerAnimatorSet;
    private AnimatorSet headerShadowAnimatorSet;
    private Bitmap blurImageBitmap;
    private boolean pulledUp;
    private AnimatedFloat groupViewAnimated = new AnimatedFloat(0, 150, CubicBezierInterpolator.DEFAULT);


    public AttributesComponentsHolder(ProfileActivity profileActivity) {

    }

    public float getPhotoDescriptionProgress() {
        return photoDescriptionProgress;
    }

    public void setPhotoDescriptionProgress(float photoDescriptionProgress) {
        this.photoDescriptionProgress = photoDescriptionProgress;
    }

    public float getCustomAvatarProgress() {
        return customAvatarProgress;
    }

    public void setCustomAvatarProgress(float customAvatarProgress) {
        this.customAvatarProgress = customAvatarProgress;
    }

    public boolean isOpenCommonChats() {
        return openCommonChats;
    }

    public void setOpenCommonChats(boolean openCommonChats) {
        this.openCommonChats = openCommonChats;
    }

    public boolean isOpenGifts() {
        return openGifts;
    }

    public void setOpenGifts(boolean openGifts) {
        this.openGifts = openGifts;
    }

    public boolean isOpenedGifts() {
        return openedGifts;
    }

    public void setOpenedGifts(boolean openedGifts) {
        this.openedGifts = openedGifts;
    }

    public boolean isOpenSimilar() {
        return openSimilar;
    }

    public void setOpenSimilar(boolean openSimilar) {
        this.openSimilar = openSimilar;
    }

    public Theme.ResourcesProvider getResourcesProvider() {
        return resourcesProvider;
    }

    public void setResourcesProvider(Theme.ResourcesProvider resourcesProvider) {
        this.resourcesProvider = resourcesProvider;
    }

    public TLRPC.ChatFull getChatInfo() {
        return chatInfo;
    }

    public void setChatInfo(TLRPC.ChatFull chatInfo) {
        this.chatInfo = chatInfo;
    }

    public TLRPC.UserFull getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(TLRPC.UserFull userInfo) {
        this.userInfo = userInfo;
    }

    public TLRPC.Chat getCurrentChat() {
        return currentChat;
    }

    public void setCurrentChat(TLRPC.Chat currentChat) {
        this.currentChat = currentChat;
    }

    public int getRowCount() {
        return rowCount;
    }
    public int getRowCountAndIncrement() {
        return rowCount++;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public SharedMediaLayout.SharedMediaPreloader getSharedMediaPreloader() {
        return sharedMediaPreloader;
    }

    public void setSharedMediaPreloader(SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader) {
        this.sharedMediaPreloader = sharedMediaPreloader;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getDialogId() {
        return dialogId;
    }

    public void setDialogId(long dialogId) {
        this.dialogId = dialogId;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public boolean isMyProfile() {
        return myProfile;
    }

    public void setMyProfile(boolean myProfile) {
        this.myProfile = myProfile;
    }

    public CharSequence getCurrentBio() {
        return currentBio;
    }

    public void setCurrentBio(CharSequence currentBio) {
        this.currentBio = currentBio;
    }

    public CharacterStyle getLoadingSpan() {
        return loadingSpan;
    }

    public void setLoadingSpan(CharacterStyle loadingSpan) {
        this.loadingSpan = loadingSpan;
    }

    public HashSet<Integer> getNotificationsExceptionTopics() {
        return notificationsExceptionTopics;
    }

    public void setNotificationsExceptionTopics(HashSet<Integer> notificationsExceptionTopics) {
        this.notificationsExceptionTopics = notificationsExceptionTopics;
    }

    public ArrayList<TLRPC.ChatParticipant> getVisibleChatParticipants() {
        return visibleChatParticipants;
    }

    public ArrayList<Integer> getVisibleSortedUsers() {
        return visibleSortedUsers;
    }

    public MessagesController.PeerColor getPeerColor() {
        return peerColor;
    }

    public void setPeerColor(MessagesController.PeerColor peerColor) {
        this.peerColor = peerColor;
    }

    public ChatActivityInterface getPreviousTransitionFragment() {
        return previousTransitionFragment;
    }

    public void setPreviousTransitionFragment(ChatActivityInterface previousTransitionFragment) {
        this.previousTransitionFragment = previousTransitionFragment;
    }

    public TLRPC.FileLocation getAvatar() {
        return avatar;
    }

    public void setAvatar(TLRPC.FileLocation avatar) {
        this.avatar = avatar;
    }

    public boolean[] getIsOnline() {
        return isOnline;
    }

    public String getNameTextViewRightDrawable2ContentDescription() {
        return nameTextViewRightDrawable2ContentDescription;
    }

    public void setNameTextViewRightDrawable2ContentDescription(String nameTextViewRightDrawable2ContentDescription) {
        this.nameTextViewRightDrawable2ContentDescription = nameTextViewRightDrawable2ContentDescription;
    }

    public String getNameTextViewRightDrawableContentDescription() {
        return nameTextViewRightDrawableContentDescription;
    }

    public void setNameTextViewRightDrawableContentDescription(String nameTextViewRightDrawableContentDescription) {
        this.nameTextViewRightDrawableContentDescription = nameTextViewRightDrawableContentDescription;
    }

    public AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] getEmojiStatusDrawable() {
        return emojiStatusDrawable;
    }

    public float[] getExpandAnimatorValues() {
        return expandAnimatorValues;
    }

    public CrossfadeDrawable[] getVerifiedCrossfadeDrawable() {
        return verifiedCrossfadeDrawable;
    }

    public CrossfadeDrawable[] getPremiumCrossfadeDrawable() {
        return premiumCrossfadeDrawable;
    }

    public AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable[] getBotVerificationDrawable() {
        return botVerificationDrawable;
    }

    public SimpleTextView[] getNameTextView() {
        return nameTextView;
    }

    public SimpleTextView[] getOnlineTextView() {
        return onlineTextView;
    }

    public SparseIntArray getAdaptedColors() {
        return adaptedColors;
    }

    public Drawable[] getVerifiedDrawable() {
        return verifiedDrawable;
    }

    public Drawable[] getVerifiedCheckDrawable() {
        return verifiedCheckDrawable;
    }

    public Drawable[] getPremiumStarDrawable() {
        return premiumStarDrawable;
    }

    public RLottieDrawable getCellCameraDrawable() {
        return cellCameraDrawable;
    }

    public void setCellCameraDrawable(RLottieDrawable cellCameraDrawable) {
        this.cellCameraDrawable = cellCameraDrawable;
    }

    public ArrayList<Integer> getSortedUsers() {
        return sortedUsers;
    }

    public void setSortedUsers(ArrayList<Integer> sortedUsers) {
        this.sortedUsers = sortedUsers;
    }

    public TLRPC.EncryptedChat getCurrentEncryptedChat() {
        return currentEncryptedChat;
    }

    public void setCurrentEncryptedChat(TLRPC.EncryptedChat currentEncryptedChat) {
        this.currentEncryptedChat = currentEncryptedChat;
    }

    public TLRPC.FileLocation getAvatarBig() {
        return avatarBig;
    }

    public void setAvatarBig(TLRPC.FileLocation avatarBig) {
        this.avatarBig = avatarBig;
    }

    public ProfileGalleryView getAvatarsViewPager() {
        return avatarsViewPager;
    }

    public void setAvatarsViewPager(ProfileGalleryView avatarsViewPager) {
        this.avatarsViewPager = avatarsViewPager;
    }

    public RLottieDrawable getCameraDrawable() {
        return cameraDrawable;
    }

    public void setCameraDrawable(RLottieDrawable cameraDrawable) {
        this.cameraDrawable = cameraDrawable;
    }

    public RLottieImageView getWriteButton() {
        return writeButton;
    }

    public void setWriteButton(RLottieImageView writeButton) {
        this.writeButton = writeButton;
    }

    public Animator getSearchViewTransition() {
        return searchViewTransition;
    }

    public void setSearchViewTransition(Animator searchViewTransition) {
        this.searchViewTransition = searchViewTransition;
    }

    public AnimatorSet getWriteButtonAnimation() {
        return writeButtonAnimation;
    }

    public void setWriteButtonAnimation(AnimatorSet writeButtonAnimation) {
        this.writeButtonAnimation = writeButtonAnimation;
    }

    public AvatarDrawable getAvatarDrawable() {
        return avatarDrawable;
    }

    public void setAvatarDrawable(AvatarDrawable avatarDrawable) {
        this.avatarDrawable = avatarDrawable;
    }

    public AvatarImageView getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(AvatarImageView avatarImage) {
        this.avatarImage = avatarImage;
    }

    public ImageLocation getPrevLoadedImageLocation() {
        return prevLoadedImageLocation;
    }

    public void setPrevLoadedImageLocation(ImageLocation prevLoadedImageLocation) {
        this.prevLoadedImageLocation = prevLoadedImageLocation;
    }

    public ValueAnimator getExpandAnimator() {
        return expandAnimator;
    }

    public void setExpandAnimator(ValueAnimator expandAnimator) {
        this.expandAnimator = expandAnimator;
    }

    public AudioPlayerAlert.ClippingTextViewSwitcher getMediaCounterTextView() {
        return mediaCounterTextView;
    }

    public void setMediaCounterTextView(AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView) {
        this.mediaCounterTextView = mediaCounterTextView;
    }

    public ScamDrawable getScamDrawable() {
        return scamDrawable;
    }

    public void setScamDrawable(ScamDrawable scamDrawable) {
        this.scamDrawable = scamDrawable;
    }

    public Drawable getLockIconDrawable() {
        return lockIconDrawable;
    }

    public void setLockIconDrawable(Drawable lockIconDrawable) {
        this.lockIconDrawable = lockIconDrawable;
    }

    public HintView2 getCollectibleHint() {
        return collectibleHint;
    }

    public void setCollectibleHint(HintView2 collectibleHint) {
        this.collectibleHint = collectibleHint;
    }

    public DrawerProfileCell.AnimatedStatusView getAnimatedStatusView() {
        return animatedStatusView;
    }

    public void setAnimatedStatusView(DrawerProfileCell.AnimatedStatusView animatedStatusView) {
        this.animatedStatusView = animatedStatusView;
    }

    public AnimatorSet getQrItemAnimation() {
        return qrItemAnimation;
    }

    public void setQrItemAnimation(AnimatorSet qrItemAnimation) {
        this.qrItemAnimation = qrItemAnimation;
    }

    public TLRPC.TL_emojiStatusCollectible getCollectibleStatus() {
        return collectibleStatus;
    }

    public void setCollectibleStatus(TLRPC.TL_emojiStatusCollectible collectibleStatus) {
        this.collectibleStatus = collectibleStatus;
    }

    public SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow getSelectAnimatedEmojiDialog() {
        return selectAnimatedEmojiDialog;
    }

    public void setSelectAnimatedEmojiDialog(SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog) {
        this.selectAnimatedEmojiDialog = selectAnimatedEmojiDialog;
    }

    public SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow setSelectAnimatedEmojiDialogAndReturn(SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog) {
        return this.selectAnimatedEmojiDialog = selectAnimatedEmojiDialog;
    }

    public AnimatorSet getHeaderAnimatorSet() {
        return headerAnimatorSet;
    }

    public void setHeaderAnimatorSet(AnimatorSet headerAnimatorSet) {
        this.headerAnimatorSet = headerAnimatorSet;
    }

    public AnimatorSet getHeaderShadowAnimatorSet() {
        return headerShadowAnimatorSet;
    }

    public void setHeaderShadowAnimatorSet(AnimatorSet headerShadowAnimatorSet) {
        this.headerShadowAnimatorSet = headerShadowAnimatorSet;
    }

    public Bitmap getBlurImageBitmap() {
        return blurImageBitmap;
    }

    public void setBlurImageBitmap(Bitmap blurImageBitmap) {
        this.blurImageBitmap = blurImageBitmap;
    }

    public AnimatedFloat getGroupViewAnimated() {
        return groupViewAnimated;
    }

    public float getPullUpProgress() {
        return pullUpProgress;
    }

    public void setPullUpProgress(float pullUpProgress, ComponentsFactory components) {
       ViewComponentsHolder viewHolder = components.getViewComponentsHolder();
        if (this.pullUpProgress == pullUpProgress) return;
        this.pullUpProgress = pullUpProgress;

        viewHolder.getAvatarContainer().invalidate();
        if (viewHolder.getButtonsGroup() != null) {
            viewHolder.getButtonsGroup().setExpandProgress((1f - pullUpProgress) * getGroupButtonTransitionProgress());
        }

        components.getLayouts().needLayout(true);
    }

    public float getGroupButtonTransitionProgress() {
        return groupButtonTransitionProgress;
    }

    @Keep
    public void setGroupButtonTransitionProgress(float groupButtonTransitionProgress) {
        this.groupButtonTransitionProgress = groupButtonTransitionProgress;
    }

    public boolean isPulledUp() {
        return pulledUp;
    }

    public void setPulledUp(boolean pulledUp) {
        this.pulledUp = pulledUp;
    }
}
