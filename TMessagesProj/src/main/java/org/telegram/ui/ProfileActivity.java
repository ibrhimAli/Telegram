/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.ContactsController.PRIVACY_RULES_TYPE_ADDED_BY_PHONE;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.ui.profile.utils.ChatData.updateOnlineCount;
import static org.telegram.ui.profile.utils.HideAndVisibility.hideFloatingButton;
import static org.telegram.ui.profile.utils.HideAndVisibility.updateFloatingButtonOffset;
import static org.telegram.ui.profile.utils.Hints.updateCollectibleHint;
import static org.telegram.ui.profile.utils.Images.checkPhotoDescriptionAlpha;
import static org.telegram.ui.profile.utils.QR.updateQrItemVisibility;
import static org.telegram.ui.profile.utils.Update.updateAutoDeleteItem;
import static org.telegram.ui.profile.utils.Update.updateStar;
import static org.telegram.ui.profile.utils.Update.updateTimeItem;
import static org.telegram.ui.profile.utils.Views.updateStoriesViewBounds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSearchCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackButtonMenu;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.HintView;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.Stars.ProfileGiftsView;
import org.telegram.ui.Stories.ProfileStoriesView;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.profile.AvatarImageView;
import org.telegram.ui.profile.adapter.ListAdapter;
import org.telegram.ui.profile.adapter.SearchAdapter;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.layout.NestedFrameLayout;
import org.telegram.ui.profile.utils.Adapter;
import org.telegram.ui.profile.utils.Animation;
import org.telegram.ui.profile.utils.ClicksAndPress;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.utils.ColorsUtils;
import org.telegram.ui.profile.utils.FragmentView;
import org.telegram.ui.profile.utils.HideAndVisibility;
import org.telegram.ui.profile.utils.Layouts;
import org.telegram.ui.profile.utils.ListViewClick;
import org.telegram.ui.profile.utils.OpenAndWrite;
import org.telegram.ui.profile.utils.ProfileData;
import org.telegram.ui.profile.view.BlurImageView;
import org.telegram.ui.profile.view.OverlaysView;
import org.telegram.ui.profile.view.PagerIndicatorView;
import org.telegram.ui.profile.view.ClippedListView;
import org.telegram.ui.profile.view.TopView;
import org.telegram.ui.profile.view.button.ButtonViewEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.concurrent.CountDownLatch;

public class ProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate, SharedMediaLayout.SharedMediaPreloaderDelegate, ImageUpdater.ImageUpdaterDelegate, SharedMediaLayout.Delegate {
    private ListAdapter listAdapter;
    private SearchAdapter searchAdapter;
    private View blurredView;
    private HintView fwdRestrictedHint;
    private RadialProgressView avatarProgressView;
    private int avatarColor;

    private View scrimView = null;
    private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        @Override
        public void setAlpha(int a) {
            super.setAlpha(a);
            fragmentView.invalidate();
        }
    };
    private final Paint actionBarBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    private ImageView ttlIconView;
    public boolean saved;
    private long mergeDialogId;


    private boolean loadingUsers;
    private LongSparseArray<TLRPC.ChatParticipant> participantsMap = new LongSparseArray<>();
    private boolean usersEndReached;

    private long banFromGroup;
    private boolean recreateMenuAfterAnimation;

    private final HashMap<Integer, Integer> positionToOffset = new HashMap<>();

    public final Paint whitePaint = new Paint();

    public boolean createdBirthdayFetcher;
    public ProfileBirthdayEffect.BirthdayEffectFetcher birthdayFetcher;

    private long selectedUser;

    public TL_bots.BotInfo botInfo;
    private TLRPC.ChannelParticipant currentChannelParticipant;
    private TL_account.TL_password currentPassword;

    private ImageLocation uploadingImageLocation;

    private final Rect rect = new Rect();

    private int transitionIndex;
    PinchToZoomHelper pinchToZoomHelper;
    private int actionBarAnimationColorFrom = 0;
    private int navigationBarAnimationColorFrom = 0;

    private String vcardFirstName;
    private String vcardLastName;
    private boolean loadingBoostsStats;

    public Runnable applyBulletin;
    private OpenAndWrite openAndWrite;
    private org.telegram.ui.profile.utils.ActionBar profileActionBar;
    private Adapter profileAdapter;
    private Animation profileAnimation;
    private ClicksAndPress clicksAndPress;
    private ListViewClick profileListViewClick;
    private ProfileData profileData;
    private RecyclerListView listView;
    private Layouts profileLayouts;
    private ColorsUtils profileColors;
    public ViewComponentsHolder viewComponents;
    private AttributesComponentsHolder attributesComponents;
    private RowsAndStatusComponentsHolder rowsAndStatusComponents;

    public static ProfileActivity of(long dialogId) {
        Bundle bundle = new Bundle();
        if (dialogId >= 0) {
            bundle.putLong("user_id", dialogId);
        } else {
            bundle.putLong("chat_id", -dialogId);
        }
        return new ProfileActivity(bundle);
    }

    public long getTopicId() {
        return attributesComponents.getTopicId();
    }

    public ProfileActivity(Bundle args) {
        this(args, null);
    }

    public ProfileActivity(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        if (attributesComponents == null) {
            initProfileComponents();
        }
        attributesComponents.setSharedMediaPreloader(preloader);
    }

    ComponentsFactory componentsFactory;
    private void initProfileComponents() {
        //        ///ComponentsFactory[] factory = new ComponentsFactory[1];
        viewComponents = new ViewComponentsHolder(this);
        attributesComponents = new AttributesComponentsHolder(this);
        rowsAndStatusComponents = new RowsAndStatusComponentsHolder(this);

        componentsFactory = new ComponentsFactory.ComponentsFactoryBuilder(this)
                .setOpenAndWriteFactory(factory -> new OpenAndWrite(factory))
                .setActionBarFactory(factory -> new org.telegram.ui.profile.utils.ActionBar(factory))
                .setAdapterFactory(factory -> new Adapter(factory))
                .setAnimationFactory(factory -> new Animation(factory))
                .setClicksAndPressFactory(factory -> new ClicksAndPress(factory))
                .setListViewClickFactory(factory -> new ListViewClick(factory))
                .setProfileDataFactory(factory -> new ProfileData(factory))
                .setListViewFactory(factory -> new ClippedListView(factory))
                //.setChatDataFactory(factory -> new ChatData(factory))
                .setColorsUtilsFactory(factory -> new ColorsUtils(factory))
                .setLayoutsFactory(factory -> new Layouts(factory))
                .setListAdapterFactory(factory -> new ListAdapter(factory))
                .setViewComponentsHolderFactory(factory -> viewComponents)
                .setAttributesComponentsHolderFactory(factory -> attributesComponents)
                .setRowsAndStatusComponentsHolderFactory(factory -> rowsAndStatusComponents)
                .build();

        viewComponents.setComponents(componentsFactory);
        rowsAndStatusComponents.setComponents(componentsFactory);
        openAndWrite = componentsFactory.getOpenAndWrite();
        profileAdapter = componentsFactory.getAdapter();
        profileActionBar = componentsFactory.getActionBar();
        profileAnimation = componentsFactory.getAnimation();
        clicksAndPress = componentsFactory.getClicksAndPress();
        profileListViewClick = componentsFactory.getListViewClick();
        profileData = componentsFactory.getProfileData();
        profileColors = componentsFactory.getColorsUtils();
//        listView = componentsFactory.getListView().getClippedList(actionBar, whitePaint);
        profileLayouts = componentsFactory.getLayouts();
        listAdapter = componentsFactory.getListAdapter();
        //profileChatData = componentsFactory.getChatData();
    }
    @Override
    public boolean onFragmentCreate() {
        if (attributesComponents == null && componentsFactory == null && viewComponents == null) {
            initProfileComponents();
        }
        attributesComponents.setUserId(arguments.getLong("user_id", 0));
        attributesComponents.setChatId(arguments.getLong("chat_id", 0));
        attributesComponents.setTopicId(arguments.getLong("topic_id", 0));
        saved = arguments.getBoolean("saved", false);
        attributesComponents.setOpenSimilar(arguments.getBoolean("similar", false));
        rowsAndStatusComponents.setTopic(attributesComponents.getTopicId() != 0);
        banFromGroup = arguments.getLong("ban_chat_id", 0);
        rowsAndStatusComponents.setReportReactionMessageId(arguments.getInt("report_reaction_message_id", 0));
        rowsAndStatusComponents.setReportReactionFromDialogId(arguments.getLong("report_reaction_from_dialog_id", 0));
        rowsAndStatusComponents.setShowAddToContacts(arguments.getBoolean("show_add_to_contacts", true));
        rowsAndStatusComponents.setVcardPhone(PhoneFormat.stripExceptNumbers(arguments.getString("vcard_phone")));
        vcardFirstName = arguments.getString("vcard_first_name");
        vcardLastName = arguments.getString("vcard_last_name");
        rowsAndStatusComponents.setReportSpam(arguments.getBoolean("reportSpam", false));
        attributesComponents.setMyProfile(arguments.getBoolean("my_profile", false));
        attributesComponents.setOpenGifts(arguments.getBoolean("open_gifts", false));
        attributesComponents.setOpenCommonChats(arguments.getBoolean("open_common", false));
        //ProfileParams.baseFragment = this;
        if (!rowsAndStatusComponents.isExpandPhoto()) {
            rowsAndStatusComponents.setExpandPhoto(arguments.getBoolean("expandPhoto", false));
            if (rowsAndStatusComponents.isExpandPhoto()) {
                rowsAndStatusComponents.setCurrentExpandAnimatorValue(1f);
                rowsAndStatusComponents.setNeedSendMessage(true) ;
            }
        }

        if (attributesComponents.getUserId() != 0) {
            attributesComponents.setDialogId(arguments.getLong("dialog_id", 0));
            if (attributesComponents.getDialogId() != 0) {
                attributesComponents.setCurrentEncryptedChat(getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(attributesComponents.getDialogId())));
            }
            if (viewComponents.getFlagSecureReason() != null) {
                viewComponents.getFlagSecureReason().invalidate();
            }
            TLRPC.User user = getMessagesController().getUser(attributesComponents.getUserId());
            if (user == null) {
                return false;
            }

            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.reloadInterface);

            rowsAndStatusComponents.setUserBlocked(getMessagesController().blockePeers.indexOfKey(attributesComponents.getUserId()) >= 0);
            if (user.bot) {
                rowsAndStatusComponents.setBot(true);
                getMediaDataController().loadBotInfo(user.id, user.id, true, classGuid);
            }
            attributesComponents.setUserInfo(getMessagesController().getUserFull(attributesComponents.getUserId()));
            getMessagesController().loadFullUser(getMessagesController().getUser(attributesComponents.getUserId()), classGuid, true);
            participantsMap = null;

            if (UserObject.isUserSelf(user)) {
                viewComponents.setImageUpdater(new ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, true));
                viewComponents.getImageUpdater().setOpenWithFrontfaceCamera(true);
                viewComponents.getImageUpdater().parentFragment = this;
                viewComponents.getImageUpdater().setDelegate(this);
                getMediaDataController().checkFeaturedStickers();
                getMessagesController().loadSuggestedFilters();
                getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, classGuid);
            }
            actionBarAnimationColorFrom = arguments.getInt("actionBarColor", 0);
        } else if (attributesComponents.getChatId() != 0) {
            attributesComponents.setCurrentChat(getMessagesController().getChat(attributesComponents.getChatId()));
            if (attributesComponents.getCurrentChat() == null) {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                getMessagesStorage().getStorageQueue().postRunnable(() -> {
                    attributesComponents.setCurrentChat(getMessagesStorage().getChat(attributesComponents.getChatId()));
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (attributesComponents.getCurrentChat() != null) {
                    getMessagesController().putChat(attributesComponents.getCurrentChat(), true);
                } else {
                    return false;
                }
            }
            if (viewComponents.getFlagSecureReason() != null) {
                viewComponents.getFlagSecureReason().invalidate();
            }

            if (attributesComponents.getCurrentChat().megagroup) {
                getChannelParticipants(true);
            } else {
                participantsMap = null;
            }
            getNotificationCenter().addObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.channelRightsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.chatWasBoostedByUser);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.uploadStoryEnd);
            attributesComponents.setSortedUsers(new ArrayList<>());
            updateOnlineCount(componentsFactory, this,true);
            if (attributesComponents.getChatInfo() == null) {
                attributesComponents.setChatInfo(getMessagesController().getChatFull(attributesComponents.getChatId()));
            }
            if (ChatObject.isChannel(attributesComponents.getCurrentChat())) {
                getMessagesController().loadFullChat(attributesComponents.getChatId(), classGuid, true);
            } else if (attributesComponents.getChatInfo() == null) {
                attributesComponents.setChatInfo(getMessagesStorage().loadChatInfo(attributesComponents.getChatId(), false, null, false, false));
            }

            profileListViewClick.updateExceptions(this, listAdapter);
        } else {
            return false;
        }
        if (attributesComponents.getSharedMediaPreloader() == null) {
            attributesComponents.setSharedMediaPreloader(new SharedMediaLayout.SharedMediaPreloader(this));
        }
        attributesComponents.getSharedMediaPreloader().addDelegate(this);

        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().addObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().addObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().addObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().addObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().addObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        if (componentsFactory == null) {
            initProfileComponents();
        }
        profileAdapter.updateRowsIds();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        if (arguments.containsKey("preload_messages")) {
            getMessagesController().ensureMessagesLoaded(attributesComponents.getUserId(), 0, null);
        }

        if (attributesComponents.getUserId() != 0) {
            TLRPC.User user = getMessagesController().getUser(attributesComponents.getUserId());

            if (UserObject.isUserSelf(user)) {
                TL_account.getPassword req = new TL_account.getPassword();
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (response instanceof TL_account.TL_password) {
                        currentPassword = (TL_account.TL_password) response;
                    }
                });
            }
        }

        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public int getTopOffset(int tag) {
                return AndroidUtilities.statusBarHeight;
            }

            @Override
            public int getBottomOffset(int tag) {
                if (viewComponents.getBottomButtonsContainer() == null) {
                    return 0;
                }
                final float gifts = Utilities.clamp01(1f - Math.abs(viewComponents.getSharedMediaLayout().getTabProgress() - SharedMediaLayout.TAB_GIFTS));
                final float stories = Utilities.clamp01(1f - Math.abs(viewComponents.getSharedMediaLayout().getTabProgress() - SharedMediaLayout.TAB_STORIES));
                final float archivedStories = Utilities.clamp01(1f - Math.abs(viewComponents.getSharedMediaLayout().getTabProgress() - SharedMediaLayout.TAB_ARCHIVED_STORIES));
                return lerp((int) (dp(72) - viewComponents.getBottomButtonsContainer().getTranslationY() - archivedStories * viewComponents.getBottomButtonContainer()[1].getTranslationY() - stories * viewComponents.getBottomButtonContainer()[0].getTranslationY()), 0, gifts);
            }

            @Override
            public boolean bottomOffsetAnimated() {
                return viewComponents.getBottomButtonsContainer() == null;
            }
        });

        if (attributesComponents.getUserId() != 0 && UserObject.isUserSelf(getMessagesController().getUser(attributesComponents.getUserId())) && !attributesComponents.isMyProfile()) {
            getMessagesController().getContentSettings(null);
        }

        return true;
    }

    @Override
    public boolean isActionBarCrossfadeEnabled() {
        return !rowsAndStatusComponents.isPulledDown();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().onDestroy();
        }
        if (attributesComponents.getSharedMediaPreloader() != null) {
            attributesComponents.getSharedMediaPreloader().onDestroy(this);
        }
        if (attributesComponents.getSharedMediaPreloader() != null) {
            attributesComponents.getSharedMediaPreloader().removeDelegate(this);
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().removeObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().removeObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().removeObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().removeObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().removeObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().removeObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().removeObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
        if (attributesComponents.getAvatarsViewPager() != null) {
            attributesComponents.getAvatarsViewPager().onDestroy();
        }
        if (attributesComponents.getUserId() != 0) {
            getNotificationCenter().removeObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
            getMessagesController().cancelLoadFullUser(attributesComponents.getUserId());
        } else if (attributesComponents.getChatId() != 0) {
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.uploadStoryEnd);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatWasBoostedByUser);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.channelRightsUpdated);
        }
        if (attributesComponents.getAvatarImage() != null) {
            attributesComponents.getAvatarImage().setImageDrawable(null);
        }
        if (viewComponents.getImageUpdater() != null) {
            viewComponents.getImageUpdater().clear();
        }
        if (pinchToZoomHelper != null) {
            pinchToZoomHelper.clear();
        }
        if (birthdayFetcher != null && createdBirthdayFetcher) {
            birthdayFetcher.detach(true);
            birthdayFetcher = null;
        }

        if (applyBulletin != null) {
            Runnable runnable = applyBulletin;
            applyBulletin = null;
            AndroidUtilities.runOnUIThread(runnable);
        }
    }

    @Override
    public ActionBar createActionBar(Context context) {
        BaseFragment lastFragment = parentLayout.getLastFragment();
        if (lastFragment instanceof ChatActivity && ((ChatActivity) lastFragment).themeDelegate != null && ((ChatActivity) lastFragment).themeDelegate.getCurrentTheme() != null) {
            attributesComponents.setResourcesProvider(lastFragment.getResourceProvider());
        }
        ActionBar actionBar = new ActionBar(context, attributesComponents.getResourcesProvider()) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                viewComponents.getAvatarContainer().getHitRect(rect);
                if (rect.contains((int) event.getX(), (int) event.getY())) {
                    return false;
                }
                return super.onTouchEvent(event);
            }

            @Override
            public void setItemsColor(int color, boolean isActionMode) {
                super.setItemsColor(color, isActionMode);
                if (!isActionMode && ttlIconView != null) {
                    ttlIconView.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateStoriesViewBounds(viewComponents,ProfileActivity.this, viewComponents, false);
            }
        };
        actionBar.setForceSkipTouches(true);
        actionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBar.setItemsBackgroundColor(attributesComponents.getPeerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), true);
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setClipContent(true);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet() && !inBubbleMode);
        ImageView backButton = actionBar.getBackButton();
        backButton.setOnLongClickListener(e -> {
            ActionBarPopupWindow menu = BackButtonMenu.show(this, backButton, getDialogId(), getTopicId(), attributesComponents.getResourcesProvider());
            if (menu != null) {
                menu.setOnDismissListener(() -> dimBehindView(false));
                dimBehindView(backButton, 0.3f);
                if (viewComponents.getUndoView() != null) {
                    viewComponents.getUndoView().hide(true, 1);
                }
                return true;
            } else {
                return false;
            }
        });
        return actionBar;
    }

    @Override
    public void setParentLayout(INavigationLayout layout) {
        super.setParentLayout(layout);

        if (viewComponents.getFlagSecureReason() != null) {
            viewComponents.getFlagSecureReason().detach();
            viewComponents.cleanFlagSecureReason();
        }
        if (layout != null && layout.getParentActivity() != null) {
            viewComponents.initFlagSecureReason(layout);
        }
    }

    @Override
    public View createView(Context context) {
        Theme.createProfileResources(context);
        Theme.createChatResources(context, false);
        BaseFragment lastFragment = parentLayout.getLastFragment();
        if (lastFragment instanceof ChatActivity && ((ChatActivity) lastFragment).themeDelegate != null && ((ChatActivity) lastFragment).themeDelegate.getCurrentTheme() != null) {
            attributesComponents.setResourcesProvider(lastFragment.getResourceProvider());
        }
        rowsAndStatusComponents.setSearchTransitionOffset(0);
        rowsAndStatusComponents.setSearchTransitionProgress(1f);
        rowsAndStatusComponents.setSearchMode(false);
        hasOwnBackground = true;
        rowsAndStatusComponents.setExtraHeight(AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                profileActionBar.handleActionbarClick(id, listAdapter,
                        attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress(), avatarUploadingRequest,
                        uploadingImageLocation, botInfo, avatarProgressView);
            }
        });

        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().onDestroy();
        }
        final long did;
        if (attributesComponents.getDialogId() != 0) {
            did = attributesComponents.getDialogId();
        } else if (attributesComponents.getUserId() != 0) {
            did = attributesComponents.getUserId();
        } else {
            did = -attributesComponents.getChatId();
        }

        FrameLayout frameLayout = (FrameLayout) fragmentView;
        ViewGroup decorView;
        if (Build.VERSION.SDK_INT >= 21) {
            decorView = (ViewGroup) getParentActivity().getWindow().getDecorView();
        } else {
            decorView = frameLayout;
        }

        pinchToZoomHelper = new PinchToZoomHelper(decorView, frameLayout) {

            Paint statusBarPaint;

            @Override
            protected void invalidateViews() {
                super.invalidateViews();
                fragmentView.invalidate();
                for (int i = 0; i < attributesComponents.getAvatarsViewPager().getChildCount(); i++) {
                    attributesComponents.getAvatarsViewPager().getChildAt(i).invalidate();
                }
                if (attributesComponents.getWriteButton() != null) {
                    attributesComponents.getWriteButton().invalidate();
                }
            }

            @Override
            protected void drawOverlays(Canvas canvas, float alpha, float parentOffsetX, float parentOffsetY, float clipTop, float clipBottom) {
                if (alpha > 0) {
                    AndroidUtilities.rectTmp.set(0, 0, attributesComponents.getAvatarsViewPager().getMeasuredWidth(), attributesComponents.getAvatarsViewPager().getMeasuredHeight() + AndroidUtilities.dp(30));
                    canvas.saveLayerAlpha(AndroidUtilities.rectTmp, (int) (255 * alpha), Canvas.ALL_SAVE_FLAG);

                    viewComponents.getAvatarContainer2().draw(canvas);

                    if (actionBar.getOccupyStatusBar() && !SharedConfig.noStatusBar) {
                        if (statusBarPaint == null) {
                            statusBarPaint = new Paint();
                            statusBarPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.2f)));
                        }
                        canvas.drawRect(actionBar.getX(), actionBar.getY(), actionBar.getX() + actionBar.getMeasuredWidth(), actionBar.getY() + AndroidUtilities.statusBarHeight, statusBarPaint);
                    }
                    canvas.save();
                    canvas.translate(actionBar.getX(), actionBar.getY());
                    actionBar.draw(canvas);
                    canvas.restore();

                    if (attributesComponents.getWriteButton() != null && attributesComponents.getWriteButton().getVisibility() == View.VISIBLE && attributesComponents.getWriteButton().getAlpha() > 0) {
                        canvas.save();
                        float s = 0.5f + 0.5f * alpha;
                        canvas.scale(s, s, attributesComponents.getWriteButton().getX() + attributesComponents.getWriteButton().getMeasuredWidth() / 2f, attributesComponents.getWriteButton().getY() + attributesComponents.getWriteButton().getMeasuredHeight() / 2f);
                        canvas.translate(attributesComponents.getWriteButton().getX(), attributesComponents.getWriteButton().getY());
                        attributesComponents.getWriteButton().draw(canvas);
                        canvas.restore();
                    }
                    canvas.restore();
                }
            }

            @Override
            protected boolean zoomEnabled(View child, ImageReceiver receiver) {
                if (!super.zoomEnabled(child, receiver)) {
                    return false;
                }
                return listView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING;
            }
        };



        if (attributesComponents.isMyProfile()) {
            viewComponents.initBottomContainerButton(context, attributesComponents);
        }

        viewComponents.initSharedMediaLayout(attributesComponents);
        viewComponents.getSharedMediaLayout().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        ActionBarMenu menu = actionBar.createMenu();

        if (attributesComponents.getUserId() == getUserConfig().clientUserId && !attributesComponents.isMyProfile()) {
            viewComponents.initQrItem(menu);
            viewComponents.getQrItem().setContentDescription(LocaleController.getString(R.string.GetQRCode));
            updateQrItemVisibility(componentsFactory, false);
            if (ContactsController.getInstance(currentAccount).getPrivacyRules(PRIVACY_RULES_TYPE_ADDED_BY_PHONE) == null) {
                ContactsController.getInstance(currentAccount).loadPrivacySettings();
            }
        }
        if (viewComponents.getImageUpdater() != null && !attributesComponents.isMyProfile()) {
            viewComponents.initSearchItem(menu, searchAdapter);
            viewComponents.getSearchItem().setContentDescription(LocaleController.getString(R.string.SearchInSettings));
            viewComponents.getSearchItem().setSearchFieldHint(LocaleController.getString(R.string.SearchInSettings));
            viewComponents.getSharedMediaLayout().getSearchItem().setVisibility(View.GONE);
            if (viewComponents.getSharedMediaLayout().getSearchOptionsItem() != null) {
                viewComponents.getSharedMediaLayout().getSearchOptionsItem().setVisibility(View.GONE);
            }
            if (viewComponents.getSharedMediaLayout().getSaveItem() != null) {
                viewComponents.getSharedMediaLayout().getSaveItem().setVisibility(View.GONE);
            }
            if (rowsAndStatusComponents.isExpandPhoto()) {
                viewComponents.getSearchItem().setVisibility(View.GONE);
            }
        }

        viewComponents.initVideoCallItem(menu);
        viewComponents.getVideoCallItem().setContentDescription(LocaleController.getString(R.string.VideoCall));
        viewComponents.initCallItem(menu);
        viewComponents.initEditItem(menu);
        viewComponents.initOtherItem(menu, attributesComponents);
        ttlIconView = new ImageView(context);
        ttlIconView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.MULTIPLY));
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, false, 0.8f, false);
        ttlIconView.setImageResource(R.drawable.msg_mini_autodelete_timer);
        viewComponents.getOtherItem().addView(ttlIconView, LayoutHelper.createFrame(12, 12, Gravity.CENTER_VERTICAL | Gravity.LEFT, 8, 2, 0, 0));
        viewComponents.getOtherItem().setContentDescription(LocaleController.getString(R.string.AccDescrMoreOptions));

        int scrollTo;
        int scrollToPosition = 0;
        Object writeButtonTag = null;
        if (listView != null && viewComponents.getImageUpdater() != null) {
            scrollTo = viewComponents.getLayoutManager().findFirstVisibleItemPosition();
            View topView = viewComponents.getLayoutManager().findViewByPosition(scrollTo);
            if (topView != null) {
                scrollToPosition = topView.getTop() - listView.getPaddingTop();
            } else {
                scrollTo = -1;
            }
            writeButtonTag = attributesComponents.getWriteButton().getTag();
        } else {
            scrollTo = -1;
        }

        if (componentsFactory == null) {
            initProfileComponents();
        }


        listView = componentsFactory.getListView().getClippedList(actionBar, whitePaint);

        fragmentView = new FragmentView(componentsFactory).getFragmentView(listAdapter, pinchToZoomHelper,
                positionToOffset, whitePaint, scrimPaint, blurredView, scrimView, actionBarBackgroundPaint,
                profileTransitionInProgress, attributesComponents.isOpenSimilar(), banFromGroup);

        profileActionBar.createActionBarMenu( false);

        searchAdapter = new SearchAdapter(this, componentsFactory);
        attributesComponents.setAvatarDrawable(new AvatarDrawable());
        attributesComponents.getAvatarDrawable().setProfile(true);

        fragmentView.setWillNotDraw(false);
        viewComponents.setContentView(((NestedFrameLayout) fragmentView));
        viewComponents.getContentView().needBlur = true;
        frameLayout = (FrameLayout) fragmentView;


        listView.setVerticalScrollBarEnabled(false);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator() {

            int animationIndex = -1;

            @Override
            protected void onAllAnimationsDone() {
                super.onAllAnimationsDone();
                AndroidUtilities.runOnUIThread(() -> {
                    getNotificationCenter().onAnimationFinish(animationIndex);
                });
            }

            @Override
            public void runPendingAnimations() {
                boolean removalsPending = !mPendingRemovals.isEmpty();
                boolean movesPending = !mPendingMoves.isEmpty();
                boolean changesPending = !mPendingChanges.isEmpty();
                boolean additionsPending = !mPendingAdditions.isEmpty();
                if (removalsPending || movesPending || additionsPending || changesPending) {
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
                    valueAnimator.addUpdateListener(valueAnimator1 -> listView.invalidate());
                    valueAnimator.setDuration(getMoveDuration());
                    valueAnimator.start();
                    animationIndex = getNotificationCenter().setAnimationInProgress(animationIndex, null);
                }
                super.runPendingAnimations();
            }

            @Override
            protected long getAddAnimationDelay(long removeDuration, long moveDuration, long changeDuration) {
                return 0;
            }

            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
                profileLayouts.updateBottomButtonY();
            }
        };
        listView.setItemAnimator(defaultItemAnimator);
        defaultItemAnimator.setMoveDelay(0);
        defaultItemAnimator.setMoveDuration(320);
        defaultItemAnimator.setRemoveDuration(320);
        defaultItemAnimator.setAddDuration(320);
        defaultItemAnimator.setSupportsChangeAnimations(false);
        defaultItemAnimator.setDelayAnimations(false);
        defaultItemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        listView.setClipToPadding(false);
        listView.setHideIfEmpty(false);

        viewComponents.initLayoutManager();
        viewComponents.getLayoutManager().setOrientation(LinearLayoutManager.VERTICAL);
        viewComponents.getLayoutManager().mIgnoreTopPadding = false;
        listView.setLayoutManager(viewComponents.getLayoutManager());
        listView.setGlowColor(0);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            profileListViewClick.handleOnItemClick(view, position, x, y, listAdapter, did,
                    vcardFirstName, vcardLastName, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress(),
                    1f, currentPassword, birthdayEffect);
        });

        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            private int pressCount = 0;
            @Override
            public boolean onItemClick(View view, int position) {
                return profileListViewClick.handleOnItemLongClick(view, position, listAdapter, pressCount);
            }
        });

        if (attributesComponents.isOpenSimilar()) {
            profileAdapter.updateRowsIds();
            scrollToSharedMedia();
            rowsAndStatusComponents.setSavedScrollToSharedMedia(true);
            rowsAndStatusComponents.setSavedScrollPosition(rowsAndStatusComponents.getSharedMediaRow());
            rowsAndStatusComponents.setSavedScrollOffset(0);
        }

        if (viewComponents.getSearchItem() != null) {
            viewComponents.initSearchListView(context, searchAdapter);
            frameLayout.addView(viewComponents.getSearchListView(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
            viewComponents.getSearchListView().setOnItemClickListener((view, position) -> {
                if (position < 0) {
                    return;
                }
                Object object = rowsAndStatusComponents.getNumberRow();
                boolean add = true;
                if (searchAdapter.searchWas) {
                    if (position < searchAdapter.searchResults.size()) {
                        object = searchAdapter.searchResults.get(position);
                    } else {
                        position -= searchAdapter.searchResults.size() + 1;
                        if (position >= 0 && position < searchAdapter.faqSearchResults.size()) {
                            object = searchAdapter.faqSearchResults.get(position);
                        }
                    }
                } else {
                    if (!searchAdapter.recentSearches.isEmpty()) {
                        position--;
                    }
                    if (position >= 0 && position < searchAdapter.recentSearches.size()) {
                        object = searchAdapter.recentSearches.get(position);
                    } else {
                        position -= searchAdapter.recentSearches.size() + 1;
                        if (position >= 0 && position < searchAdapter.faqSearchArray.size()) {
                            object = searchAdapter.faqSearchArray.get(position);
                            add = false;
                        }
                    }
                }
                if (object instanceof SearchAdapter.SearchResult) {
                    SearchAdapter.SearchResult result = (SearchAdapter.SearchResult) object;
                    result.open();
                } else if (object instanceof MessagesController.FaqSearchResult) {
                    MessagesController.FaqSearchResult result = (MessagesController.FaqSearchResult) object;
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.openArticle, searchAdapter.faqWebPage, result.url);
                }
                if (add && object != null) {
                    searchAdapter.addRecent(object);
                }
            });
            viewComponents.getSearchListView().setOnItemLongClickListener((view, position) -> {
                if (searchAdapter.isSearchWas() || searchAdapter.recentSearches.isEmpty()) {
                    return false;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), attributesComponents.getResourcesProvider());
                builder.setTitle(LocaleController.getString(R.string.ClearSearchAlertTitle));
                builder.setMessage(LocaleController.getString(R.string.ClearSearchAlert));
                builder.setPositiveButton(LocaleController.getString(R.string.ClearButton), (dialogInterface, i) -> searchAdapter.clearRecent());
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                }
                return true;
            });
            viewComponents.getSearchListView().setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }
            });
            viewComponents.getSearchListView().setAnimateEmptyView(true, RecyclerListView.EMPTY_VIEW_ANIMATION_TYPE_ALPHA_SCALE);

            viewComponents.initEmptyView(context);
            frameLayout.addView(viewComponents.getEmptyView());

            searchAdapter.loadFaqWebPage();
        }

        if (banFromGroup != 0) {
            TLRPC.Chat chat = getMessagesController().getChat(banFromGroup);
            if (currentChannelParticipant == null) {
                TLRPC.TL_channels_getParticipant req = new TLRPC.TL_channels_getParticipant();
                req.channel = MessagesController.getInputChannel(chat);
                req.participant = getMessagesController().getInputPeer(attributesComponents.getUserId());
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (response != null) {
                        AndroidUtilities.runOnUIThread(() -> currentChannelParticipant = ((TLRPC.TL_channels_channelParticipant) response).participant);
                    }
                });
            }
            FrameLayout frameLayout1 = new FrameLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    int bottom = Theme.chat_composeShadowDrawable.getIntrinsicHeight();
                    Theme.chat_composeShadowDrawable.setBounds(0, 0, getMeasuredWidth(), bottom);
                    Theme.chat_composeShadowDrawable.draw(canvas);
                    canvas.drawRect(0, bottom, getMeasuredWidth(), getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
                }
            };
            frameLayout1.setWillNotDraw(false);

            frameLayout.addView(frameLayout1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 51, Gravity.LEFT | Gravity.BOTTOM));
            frameLayout1.setOnClickListener(v -> {
                ChatRightsEditActivity fragment = new ChatRightsEditActivity(attributesComponents.getUserId(), banFromGroup, null, chat.default_banned_rights, currentChannelParticipant != null ? currentChannelParticipant.banned_rights : null, "", ChatRightsEditActivity.TYPE_BANNED, true, false, null);
                fragment.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
                    @Override
                    public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                        removeSelfFromStack();
                        TLRPC.User user = getMessagesController().getUser(attributesComponents.getUserId());
                        if (user != null && chat != null && attributesComponents.getUserId() != 0 && fragment != null && fragment.banning && fragment.getParentLayout() != null) {
                            for (BaseFragment fragment : fragment.getParentLayout().getFragmentStack()) {
                                if (fragment instanceof ChannelAdminLogActivity) {
                                    ((ChannelAdminLogActivity) fragment).reloadLastMessages();
                                    AndroidUtilities.runOnUIThread(() -> {
                                        BulletinFactory.createRemoveFromChatBulletin(fragment, user, chat.title).show();
                                    });
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void didChangeOwner(TLRPC.User user) {
                        viewComponents.getUndoView().showWithAction(-attributesComponents.getChatId(), attributesComponents.getCurrentChat().megagroup ? UndoView.ACTION_OWNER_TRANSFERED_GROUP : UndoView.ACTION_OWNER_TRANSFERED_CHANNEL, user);
                    }
                });
                presentFragment(fragment);
            });

            TextView textView = new TextView(context);
            textView.setTextColor(getThemedColor(Theme.key_text_RedRegular));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(AndroidUtilities.bold());
            textView.setText(LocaleController.getString(R.string.BanFromTheGroup));
            frameLayout1.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 1, 0, 0));

            listView.setPadding(0, AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT), 0, AndroidUtilities.dp(48));
            listView.setBottomGlowOffset(AndroidUtilities.dp(48));
        } else {
            listView.setPadding(0, AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT), 0, 0);
        }

        viewComponents.setTopView(new TopView(componentsFactory, ProfileActivity.this));
        viewComponents.getTopView().setBackgroundColorId(attributesComponents.getPeerColor(), false);
        viewComponents.getTopView().setBackgroundColor(getThemedColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(viewComponents.getTopView());
        viewComponents.getContentView().blurBehindViews.add(viewComponents.getTopView());

        attributesComponents.setAnimatedStatusView(new DrawerProfileCell.AnimatedStatusView(context, 20, 60));
        attributesComponents.getAnimatedStatusView().setPivotX(AndroidUtilities.dp(30));
        attributesComponents.getAnimatedStatusView().setPivotY(AndroidUtilities.dp(30));

        viewComponents.initAvatarContainer(context);
        viewComponents.setBlurImageMask(new BlurImageView(context));
        viewComponents.initAvatarContainer2(context);
        viewComponents.initFallbackImage();
        AndroidUtilities.updateViewVisibilityAnimated(viewComponents.getAvatarContainer2(), true, 1f, false);
        frameLayout.addView(viewComponents.getAvatarContainer2(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.START, 0, 0, 0, 0));
        viewComponents.getAvatarContainer().setPivotX(0);
        viewComponents.getAvatarContainer().setPivotY(0);
        viewComponents.getBlurImageMask().setPivotY(0);
        viewComponents.getAvatarContainer2().addView(viewComponents.getAvatarContainer(), LayoutHelper.createFrame(ProfileParams.AVATAR_DIMENSION, ProfileParams.AVATAR_DIMENSION, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));
        attributesComponents.setAvatarImage(new AvatarImageView(context) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                if (getImageReceiver().hasNotThumb()) {
                    info.setText(LocaleController.getString(R.string.AccDescrProfilePicture));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, LocaleController.getString(R.string.Open)));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, LocaleController.getString(R.string.AccDescrOpenInPhotoViewer)));
                    }
                } else {
                    info.setVisibleToUser(false);
                }
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (animatedEmojiDrawable != null && animatedEmojiDrawable.getImageReceiver() != null) {
                    animatedEmojiDrawable.getImageReceiver().startAnimation();
                }
            }
        });
        attributesComponents.getAvatarImage().getImageReceiver().setAllowDecodeSingleFrame(true);
        attributesComponents.getAvatarImage().setRoundRadius(componentsFactory.getLayouts().getSmallAvatarRoundRadius(ProfileActivity.this));
        attributesComponents.getAvatarImage().setPivotX(0);
        attributesComponents.getAvatarImage().setPivotY(0);
        viewComponents.getAvatarContainer().addView(attributesComponents.getAvatarImage(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.CENTER));
        attributesComponents.getAvatarImage().setOnClickListener(v -> {
            if (attributesComponents.getAvatarBig() != null) {
                return;
            }
            if (rowsAndStatusComponents.isTopic() && !getMessagesController().premiumFeaturesBlocked()) {
                ArrayList<TLRPC.TL_forumTopic> topics = getMessagesController().getTopicsController().getTopics(attributesComponents.getChatId());
                if (topics != null) {
                    TLRPC.TL_forumTopic currentTopic = null;
                    for (int i = 0; currentTopic == null && i < topics.size(); ++i) {
                        TLRPC.TL_forumTopic topic = topics.get(i);
                        if (topic != null && topic.id == attributesComponents.getTopicId()) {
                            currentTopic = topic;
                        }
                    }
                    if (currentTopic != null && currentTopic.icon_emoji_id != 0) {
                        long documentId = currentTopic.icon_emoji_id;
                        TLRPC.Document document = AnimatedEmojiDrawable.findDocument(currentAccount, documentId);
                        if (document == null) {
                            return;
                        }
                        Bulletin bulletin = BulletinFactory.of(ProfileActivity.this).createContainsEmojiBulletin(document, BulletinFactory.CONTAINS_EMOJI_IN_TOPIC, set -> {
                            ArrayList<TLRPC.InputStickerSet> inputSets = new ArrayList<>(1);
                            inputSets.add(set);
                            EmojiPacksAlert alert = new EmojiPacksAlert(ProfileActivity.this, getParentActivity(), attributesComponents.getResourcesProvider(), inputSets);
                            showDialog(alert);
                        });
                        if (bulletin != null) {
                            bulletin.show();
                        }
                    }
                }
                return;
            }
            if (expandAvatar()) {
                return;
            }
            openAndWrite.openAvatar();
        });
        attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
        attributesComponents.getAvatarImage().setOnLongClickListener(v -> {
            if (attributesComponents.getAvatarBig() != null || rowsAndStatusComponents.isTopic()) {
                return false;
            }
            openAndWrite.openAvatar();
            return false;
        });

        avatarProgressView = new RadialProgressView(context) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paint.setColor(0x55000000);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                if (attributesComponents.getAvatarImage() != null && attributesComponents.getAvatarImage().getImageReceiver().hasNotThumb()) {
                    paint.setAlpha((int) (0x55 * attributesComponents.getAvatarImage().getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, getMeasuredWidth() / 2.0f, paint);
                }
                super.onDraw(canvas);
            }
        };
        avatarProgressView.setSize(AndroidUtilities.dp(26));
        avatarProgressView.setProgressColor(0xffffffff);
        avatarProgressView.setNoProgress(false);
        viewComponents.getAvatarContainer().addView(avatarProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        viewComponents.initTimeItem(context);
        frameLayout.addView(viewComponents.getTimeItem(), LayoutHelper.createFrame(34, 34, Gravity.TOP | Gravity.LEFT));

        viewComponents.initStarBgItem();
        frameLayout.addView(viewComponents.getStarBgItem(), LayoutHelper.createFrame(20, 20, Gravity.TOP | Gravity.LEFT));
        viewComponents.initStarFgItem();
        frameLayout.addView(viewComponents.getStarFgItem(), LayoutHelper.createFrame(20, 20, Gravity.TOP | Gravity.LEFT));

        showAvatarProgress(false, false);

        if (attributesComponents.getAvatarsViewPager() != null) {
            attributesComponents.getAvatarsViewPager().onDestroy();
        }
        viewComponents.setOverlaysView(new OverlaysView(componentsFactory));
        attributesComponents.setAvatarsViewPager(new ProfileGalleryView(context, attributesComponents.getUserId() != 0 ? attributesComponents.getUserId() : -attributesComponents.getChatId(), actionBar, listView, attributesComponents.getAvatarImage(), getClassGuid(), viewComponents.getOverlaysView()) {
            @Override
            protected void setCustomAvatarProgress(float progress) {
                attributesComponents.setCustomAvatarProgress(progress);
                checkPhotoDescriptionAlpha(componentsFactory, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress());
            }
        });
        if (attributesComponents.getUserId() != getUserConfig().clientUserId && attributesComponents.getUserInfo() != null) {
            attributesComponents.setCustomAvatarProgress(attributesComponents.getUserInfo().profile_photo == null ? 0 : 1);
        }
        if (!rowsAndStatusComponents.isTopic()) {
            attributesComponents.getAvatarsViewPager().setChatInfo(attributesComponents.getChatInfo());
        }
        viewComponents.getAvatarContainer2().addView(attributesComponents.getAvatarsViewPager(), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER));
        viewComponents.getAvatarContainer2().addView(viewComponents.getOverlaysView());
        attributesComponents.getAvatarImage().setAvatarsViewPager(attributesComponents.getAvatarsViewPager());

        viewComponents.setAvatarsViewPagerIndicatorView(new PagerIndicatorView(componentsFactory));
        viewComponents.getAvatarContainer2().addView(viewComponents.getAvatarsViewPagerIndicatorView(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        frameLayout.addView(actionBar);

        float rightMargin = (54 + ((rowsAndStatusComponents.isCallItemVisible() && attributesComponents.getUserId() != 0) ? 54 : 0));
        boolean hasTitleExpanded = false;
        int initialTitleWidth = LayoutHelper.WRAP_CONTENT;
        if (parentLayout != null && parentLayout.getLastFragment() instanceof ChatActivity) {
            ChatAvatarContainer avatarContainer = ((ChatActivity) parentLayout.getLastFragment()).getAvatarContainer();
            if (avatarContainer != null) {
                hasTitleExpanded = avatarContainer.getTitleTextView().getPaddingRight() != 0;
                if (avatarContainer.getLayoutParams() != null && avatarContainer.getTitleTextView() != null) {
                    rightMargin =
                            (((ViewGroup.MarginLayoutParams) avatarContainer.getLayoutParams()).rightMargin +
                                    (avatarContainer.getWidth() - avatarContainer.getTitleTextView().getRight())) / AndroidUtilities.density;
//                initialTitleWidth = (int) (avatarContainer.getTitleTextView().getWidth() / AndroidUtilities.density);
                }
            }
        }

        for (int a = 0; a < attributesComponents.getNameTextView().length; a++) {
            if (rowsAndStatusComponents.getPlayProfileAnimation() == 0 && a == 0) {
                continue;
            }
            attributesComponents.getNameTextView()[a] = new SimpleTextView(context) {
                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    if (isFocusable() && (attributesComponents.getNameTextViewRightDrawableContentDescription() != null || attributesComponents.getNameTextViewRightDrawable2ContentDescription() != null)) {
                        StringBuilder s = new StringBuilder(getText());
                        if (attributesComponents.getNameTextViewRightDrawable2ContentDescription() != null) {
                            if (s.length() > 0) s.append(", ");
                            s.append(attributesComponents.getNameTextViewRightDrawable2ContentDescription());
                        }
                        if (attributesComponents.getNameTextViewRightDrawableContentDescription() != null) {
                            if (s.length() > 0) s.append(", ");
                            s.append(attributesComponents.getNameTextViewRightDrawableContentDescription());
                        }
                        info.setText(s);
                    }
                }

                @Override
                protected void onDraw(Canvas canvas) {
                    final int wasRightDrawableX = getRightDrawableX();
                    super.onDraw(canvas);
                    if (wasRightDrawableX != getRightDrawableX()) {
                        updateCollectibleHint(componentsFactory);
                    }
                }
            };
            if (a == 1) {
                attributesComponents.getNameTextView()[a].setTextColor(getThemedColor(Theme.key_profile_title));
            } else {
                attributesComponents.getNameTextView()[a].setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
            }
            attributesComponents.getNameTextView()[a].setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(a == 0 ? 12 : 4));
            attributesComponents.getNameTextView()[a].setTextSize(18);
            attributesComponents.getNameTextView()[a].setGravity(Gravity.LEFT);
            attributesComponents.getNameTextView()[a].setTypeface(AndroidUtilities.bold());
            attributesComponents.getNameTextView()[a].setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
            attributesComponents.getNameTextView()[a].setPivotX(0);
            attributesComponents.getNameTextView()[a].setPivotY(0);
            attributesComponents.getNameTextView()[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1) {
                attributesComponents.getNameTextView()[a].setScrollNonFitText(true);
                attributesComponents.getNameTextView()[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            attributesComponents.getNameTextView()[a].setFocusable(a == 0);
            attributesComponents.getNameTextView()[a].setEllipsizeByGradient(true);
            attributesComponents.getNameTextView()[a].setRightDrawableOutside(a == 0);
            viewComponents.getAvatarContainer2().addView(attributesComponents.getNameTextView()[a], LayoutHelper.createFrame(a == 0 ? initialTitleWidth : LayoutHelper.WRAP_CONTENT,
                    LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, -6, 0, 0));
        }
        for (int a = 0; a < attributesComponents.getOnlineTextView().length; a++) {
            if (a == 1) {
                attributesComponents.getOnlineTextView()[a] = new LinkSpanDrawable.ClickableSmallTextView(context) {

                    @Override
                    public void setAlpha(float alpha) {
                        super.setAlpha(alpha);
                        checkPhotoDescriptionAlpha(componentsFactory, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress());
                    }

                    @Override
                    public void setTranslationY(float translationY) {
                        super.setTranslationY(translationY);
                        attributesComponents.getOnlineTextView()[2].setTranslationY(translationY);
                        attributesComponents.getOnlineTextView()[3].setTranslationY(translationY);
                    }

                    @Override
                    public boolean setText(CharSequence value) {
                        boolean tex = super.setText(value);
                        profileLayouts.needLayout(false);
                        return tex;
                    }

                    private Path path = new Path();

                    @Override
                    public void draw(@NonNull Canvas canvas) {
                        if (!rowsAndStatusComponents.isTopic()) {
                            super.draw(canvas);
                            return;
                        }
                        canvas.save();
                        float rad = AndroidUtilities.dp(lerp(16, 4, attributesComponents.getPullUpProgress()));
                        float left = dp(12) * attributesComponents.getPullUpProgress();
                        float top = dp(6) * attributesComponents.getPullUpProgress();
                        path.rewind();
                        path.addRoundRect(left, top, getTextWidth() + getPaddingLeft() + getPaddingRight() - left, getHeight() - top, rad, rad, Path.Direction.CW);
                        canvas.clipPath(path);
                        super.draw(canvas);
                        canvas.restore();
                    }

                    @Override
                    public void setTranslationX(float translationX) {
                        super.setTranslationX(translationX);
                        attributesComponents.getOnlineTextView()[2].setTranslationX(translationX);
                        attributesComponents.getOnlineTextView()[3].setTranslationX(translationX);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        if (attributesComponents.getOnlineTextView()[2] != null) {
                            attributesComponents.getOnlineTextView()[2].setTextColor(color);
                            attributesComponents.getOnlineTextView()[3].setTextColor(color);
                        }
                        if (viewComponents.getShowStatusButton() != null) {
                            viewComponents.getShowStatusButton().setTextColor(Theme.multAlpha(Theme.adaptHSV(color, -.02f, +.15f), 1.4f));
                        }
                    }
                };
            } else {
                attributesComponents.getOnlineTextView()[a] = new LinkSpanDrawable.ClickableSmallTextView(context);
            }

            attributesComponents.getOnlineTextView()[a].setEllipsizeByGradient(true);
            attributesComponents.getOnlineTextView()[a].setTextColor(profileColors.applyPeerColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, null));
            attributesComponents.getOnlineTextView()[a].setTextSize(14);
            attributesComponents.getOnlineTextView()[a].setGravity(Gravity.LEFT);
            attributesComponents.getOnlineTextView()[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1 || a == 2 || a == 3) {
                attributesComponents.getOnlineTextView()[a].setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(2), AndroidUtilities.dp(4), AndroidUtilities.dp(2));
            }
            if (a > 0) {
                attributesComponents.getOnlineTextView()[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            attributesComponents.getOnlineTextView()[a].setFocusable(a == 0);
            viewComponents.getAvatarContainer2().addView(attributesComponents.getOnlineTextView()[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, -(a == 1 || a == 2 || a == 3 ? 4 : 0), (a == 1 || a == 2 || a == 3 ? -2 : 0), (a == 0 ? rightMargin - (hasTitleExpanded ? 10 : 0) : 8) - (a == 1 || a == 2 || a == 3 ? 4 : 0), 0));
        }
        checkPhotoDescriptionAlpha(componentsFactory, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress());
        viewComponents.getAvatarContainer2().addView(attributesComponents.getAnimatedStatusView());

        attributesComponents.setMediaCounterTextView(new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AndroidUtilities.dp(14));
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setGravity(Gravity.LEFT);
                return textView;
            }
        });
        attributesComponents.getMediaCounterTextView().setAlpha(0.0f);
        viewComponents.getAvatarContainer2().addView(attributesComponents.getMediaCounterTextView(), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, -2, 8, 0));
        viewComponents.initButtonsGroup();
        clicksAndPress.updateProfileButtons(false);
        viewComponents.getAvatarContainer2().addView(viewComponents.getButtonsGroup(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, ProfileParams.GROUP_BUTTON_DIMENSION));
        viewComponents.initStoryView(context, attributesComponents);
        updateStoriesViewBounds(viewComponents, ProfileActivity.this, viewComponents, false);
        if (attributesComponents.getUserInfo() != null) {
            viewComponents.getStoryView().setStories(attributesComponents.getUserInfo().stories);
        } else if (attributesComponents.getChatInfo() != null) {
            viewComponents.getStoryView().setStories(attributesComponents.getChatInfo().stories);
        }
        if (attributesComponents.getAvatarImage() != null) {
            attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
        }
        viewComponents.getAvatarContainer2().addView(viewComponents.getBlurImageMask(), LayoutHelper.createFrame(ProfileParams.AVATAR_DIMENSION, ProfileParams.AVATAR_DIMENSION, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));
        viewComponents.getAvatarContainer2().addView(viewComponents.getStoryView(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        viewComponents.initGiftsView(attributesComponents);
        viewComponents.getAvatarContainer2().addView(viewComponents.getGiftsView(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        profileData.updateProfileData(true);

        attributesComponents.setWriteButton(new RLottieImageView(context));
        profileColors.writeButtonSetBackground(ProfileActivity.this);
        if (attributesComponents.getUserId() != 0) {
            if (viewComponents.getImageUpdater() != null) {
                attributesComponents.setCameraDrawable(new RLottieDrawable(R.raw.camera_outline, String.valueOf(R.raw.camera_outline), AndroidUtilities.dp(56), AndroidUtilities.dp(56), false, null));
                attributesComponents.setCellCameraDrawable(new RLottieDrawable(R.raw.camera_outline, R.raw.camera_outline + "_cell", AndroidUtilities.dp(42), AndroidUtilities.dp(42), false, null));

                attributesComponents.getWriteButton().setAnimation(attributesComponents.getCameraDrawable());
                attributesComponents.getWriteButton().setContentDescription(LocaleController.getString(R.string.AccDescrChangeProfilePicture));
                attributesComponents.getWriteButton().setPadding(AndroidUtilities.dp(2), 0, 0, AndroidUtilities.dp(2));
            } else {
                attributesComponents.getWriteButton().setImageResource(R.drawable.profile_newmsg);
                attributesComponents.getWriteButton().setContentDescription(LocaleController.getString(R.string.AccDescrOpenChat));
            }
        } else {
            attributesComponents.getWriteButton().setImageResource(R.drawable.profile_discuss);
            attributesComponents.getWriteButton().setContentDescription(LocaleController.getString(R.string.ViewDiscussion));
        }
        attributesComponents.getWriteButton().setScaleType(ImageView.ScaleType.CENTER);

        frameLayout.addView(attributesComponents.getWriteButton(), LayoutHelper.createFrame(60, 60, Gravity.RIGHT | Gravity.TOP, 0, 0, 16, 0));
        attributesComponents.getWriteButton().setOnClickListener(v -> {
            if (attributesComponents.getWriteButton().getTag() != null) {
                return;
            }
            openAndWrite.onWriteButtonClick();
        });
        profileLayouts.needLayout(false);

//        if (scrollTo != -1) {
//            if (writeButtonTag != null) {
//                attributesComponents.getWriteButton().setTag(0);
//                attributesComponents.getWriteButton().setScaleX(0.2f);
//                attributesComponents.getWriteButton().setScaleY(0.2f);
//                attributesComponents.getWriteButton().setAlpha(0.0f);
//            }
//        }

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
                if (rowsAndStatusComponents.isOpeningAvatar() && newState != RecyclerView.SCROLL_STATE_SETTLING) {
                    rowsAndStatusComponents.setOpeningAvatar(false);
                }
                if (viewComponents.getSearchItem() != null) {
                    rowsAndStatusComponents.setScrolling(newState != RecyclerView.SCROLL_STATE_IDLE);
                    viewComponents.getSearchItem().setEnabled(!rowsAndStatusComponents.isScrolling() && !rowsAndStatusComponents.isPulledDown());
                }
                viewComponents.getSharedMediaLayout().scrollingByUser = listView.scrollingByUser;
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !rowsAndStatusComponents.isAllowPullingDown()) {
                    View view = viewComponents.getLayoutManager().findViewByPosition(0);
                    if (view != null) {
                        tryStickPulledUp(view.getTop());
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (fwdRestrictedHint != null) {
                    fwdRestrictedHint.hide();
                }
                profileAnimation.checkListViewScroll();
                if (participantsMap != null && !usersEndReached && viewComponents.getLayoutManager().findLastVisibleItemPosition() > rowsAndStatusComponents.getMembersEndRow() - 8) {
                    getChannelParticipants(false);
                }
                viewComponents.getSharedMediaLayout().setPinnedToTop(viewComponents.getSharedMediaLayout().getY() <= 0);
                profileLayouts.updateBottomButtonY();
            }
        });

        viewComponents.initUndoView(context, attributesComponents);
        frameLayout.addView(viewComponents.getUndoView(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        attributesComponents.setExpandAnimator(ValueAnimator.ofFloat(0f, 1f));
        attributesComponents.getExpandAnimator().addUpdateListener(anim -> {
            setAvatarExpandProgress(anim.getAnimatedFraction());
        });
        attributesComponents.getExpandAnimator().setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        attributesComponents.getExpandAnimator().addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                viewComponents.getButtonsGroup().setBlurEnabled(true);
//                viewComponents.getBlurImageMask().startAnimation();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                actionBar.setItemsBackgroundColor(rowsAndStatusComponents.isPulledDown() ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : attributesComponents.getPeerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                attributesComponents.getAvatarImage().clearForeground();
                rowsAndStatusComponents.setDoNotSetForeground(false);
                viewComponents.getButtonsGroup().setBlurEnabled(rowsAndStatusComponents.isPulledDown());
                updateStoriesViewBounds(viewComponents, ProfileActivity.this, viewComponents, false);
            }
        });
        profileAdapter.updateRowsIds();

        updateSelectedMediaTabText();

        fwdRestrictedHint = new HintView(getParentActivity(), 9);
        fwdRestrictedHint.setAlpha(0);
        frameLayout.addView(fwdRestrictedHint, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 12, 0, 12, 0));
        viewComponents.getSharedMediaLayout().setForwardRestrictedHint(fwdRestrictedHint);



        pinchToZoomHelper.setCallback(new PinchToZoomHelper.Callback() {
            @Override
            public void onZoomStarted(MessageObject messageObject) {
                listView.cancelClickRunnables(true);
                if (viewComponents.getSharedMediaLayout() != null && viewComponents.getSharedMediaLayout().getCurrentListView() != null) {
                    viewComponents.getSharedMediaLayout().getCurrentListView().cancelClickRunnables(true);
                }
                viewComponents.getTopView().setBackgroundColor(ColorUtils.blendARGB(getAverageColor(pinchToZoomHelper.getPhotoImage()), getThemedColor(Theme.key_windowBackgroundWhite), 0.1f));
            }
        });
        attributesComponents.getAvatarsViewPager().setPinchToZoomHelper(pinchToZoomHelper);
        scrimPaint.setAlpha(0);
        actionBarBackgroundPaint.setColor(getThemedColor(Theme.key_listSelector));
        viewComponents.getContentView().blurBehindViews.add(viewComponents.getSharedMediaLayout());
        updateTtlIcon();

        blurredView = new View(context) {
            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            blurredView.setForeground(new ColorDrawable(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhite), 100)));
        }
        blurredView.setFocusable(false);
        blurredView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        blurredView.setOnClickListener(e -> {
            finishPreviewFragment();
        });
        blurredView.setVisibility(View.GONE);
        blurredView.setFitsSystemWindows(true);
        viewComponents.getContentView().addView(blurredView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createBirthdayEffect();
        createFloatingActionButton(getContext());

        if (attributesComponents.isMyProfile()) {
            viewComponents.getContentView().addView(viewComponents.getBottomButtonsContainer(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 72 + (1 / AndroidUtilities.density), Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        }

        if (attributesComponents.isOpenGifts() || attributesComponents.isOpenCommonChats()) {
            AndroidUtilities.runOnUIThread(this::scrollToSharedMedia);
        }

        return fragmentView;
    }

    private void tryStickPulledUp(int top) {
        if (top < 0) return;

        if (!attributesComponents.isPulledUp() && attributesComponents.getPullUpProgress() >= 0.25f) {
            attributesComponents.setPulledUp(true);
            listView.smoothScrollBy(0, top, 250, CubicBezierInterpolator.DEFAULT);
        } else if (attributesComponents.isPulledUp() && attributesComponents.getPullUpProgress() <= 0.75f) {
            attributesComponents.setPulledUp(false);
            listView.smoothScrollBy(0, top - AndroidUtilities.dp(ProfileParams.GROUP_BUTTON_DIMENSION), 400, CubicBezierInterpolator.EASE_OUT_QUINT);
        } else if (attributesComponents.getPullUpProgress() >= 0.5f) {
            attributesComponents.setPulledUp(true);
            listView.smoothScrollBy(0, top, 250, CubicBezierInterpolator.DEFAULT);
        } else {
            attributesComponents.setPulledUp(false);
            listView.smoothScrollBy(0, top - AndroidUtilities.dp(ProfileParams.GROUP_BUTTON_DIMENSION), 400, CubicBezierInterpolator.EASE_OUT_QUINT);
        }
    }
    public void updateBottomButtonY() {
        profileLayouts.updateBottomButtonY();
    }

    RLottieImageView floatingButton;
    boolean showBoostsAlert;

    public void checkCanSendStoryForPosting() {
        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(attributesComponents.getChatId());
        if (!ChatObject.isBoostSupported(chat)) {
            return;
        }
        StoriesController storiesController = getMessagesController().getStoriesController();
        rowsAndStatusComponents.setWaitCanSendStoryRequest(true);
        storiesController.canSendStoryFor(getDialogId(), canSend -> {
            rowsAndStatusComponents.setWaitCanSendStoryRequest(false);
            showBoostsAlert = !canSend;
            hideFloatingButton(componentsFactory,ProfileActivity.this, !(viewComponents.getSharedMediaLayout() == null || viewComponents.getSharedMediaLayout().getClosestTab() == SharedMediaLayout.TAB_STORIES || viewComponents.getSharedMediaLayout().getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES));
        }, false, attributesComponents.getResourcesProvider());
    }

    public void updateAvatarRoundRadius() {
        attributesComponents.getAvatarImage().setRoundRadius((int) AndroidUtilities.lerp(profileLayouts.getSmallAvatarRoundRadius(ProfileActivity.this), 0f, rowsAndStatusComponents.getCurrentExpandAnimatorValue()));
    }

    private void createFloatingActionButton(Context context) {
        if (!getMessagesController().storiesEnabled()) {
            return;
        }
        if (getDialogId() > 0L) {
            return;
        }
        TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat(attributesComponents.getChatId());
        if (!ChatObject.isBoostSupported(currentChat)) {
            return;
        }
        StoriesController storiesController = getMessagesController().getStoriesController();
        if (!storiesController.canPostStories(getDialogId()) || !(viewComponents.getButtonsGroup() != null && viewComponents.getButtonsGroup().getExtraButtons().contains(ButtonViewEnum.ADD_STORY))) {
            return;
        } else {
            checkCanSendStoryForPosting();
        }
        long dialogId = getDialogId();
        viewComponents.initFloatingButtonContainer(context);
        viewComponents.getContentView().addView(viewComponents.getFloatingButtonContainer(), LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        viewComponents.getFloatingButtonContainer().setOnClickListener(v -> {
            if (showBoostsAlert) {
                if (loadingBoostsStats) {
                    return;
                }
                MessagesController messagesController = MessagesController.getInstance(currentAccount);
                loadingBoostsStats = true;
                messagesController.getBoostsController().getBoostsStats(dialogId, boostsStatus -> {
                    loadingBoostsStats = false;
                    if (boostsStatus == null) {
                        return;
                    }
                    messagesController.getBoostsController().userCanBoostChannel(dialogId, boostsStatus, canApplyBoost -> {
                        if (canApplyBoost == null) {
                            return;
                        }
                        BaseFragment lastFragment = LaunchActivity.getLastFragment();
                        LimitReachedBottomSheet.openBoostsForPostingStories(lastFragment, dialogId, canApplyBoost, boostsStatus, () -> {
                            TLRPC.Chat chat = getMessagesController().getChat(attributesComponents.getChatId());
                            presentFragment(StatisticActivity.create(chat));
                        });
                    });
                });
                return;
            }
            StoryRecorder recorder = StoryRecorder.getInstance(getParentActivity(), currentAccount)
                    .selectedPeerId(getDialogId())
                    .canChangePeer(false)
                    .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                        @Override
                        public void preLayout(long dialogId, Runnable runnable) {
                            attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
                            if (dialogId == getDialogId()) {
                                collapseAvatarInstant();
                            }
                            AndroidUtilities.runOnUIThread(runnable, 30);
                        }

                        @Override
                        public StoryRecorder.SourceView getView(long dialogId) {
                            if (dialogId != getDialogId()) {
                                return null;
                            }
                            updateAvatarRoundRadius();
                            return StoryRecorder.SourceView.fromAvatarImage(attributesComponents.getAvatarImage(), ChatObject.isForum(currentChat));
                        }
                    });
            recorder.open(StoryRecorder.SourceView.fromFloatingButton(viewComponents.getFloatingButtonContainer()), true);
        });

        floatingButton = new RLottieImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(viewComponents.getFloatingButtonContainer(), View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(viewComponents.getFloatingButtonContainer(), View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            viewComponents.getFloatingButtonContainer().setStateListAnimator(animator);
            viewComponents.getFloatingButtonContainer().setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        viewComponents.getFloatingButtonContainer().addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        floatingButton.setAnimation(R.raw.write_contacts_fab_icon_camera, 56, 56);
        viewComponents.getFloatingButtonContainer().setContentDescription(LocaleController.getString(R.string.AccDescrCaptureStory));
        profileColors.updateFloatingButtonColor(ProfileActivity.this);

        rowsAndStatusComponents.setFloatingHidden(true);
        rowsAndStatusComponents.setFloatingButtonHideProgress(1.0f);
        updateFloatingButtonOffset(componentsFactory);
    }

    public void collapseAvatarInstant() {
        if (rowsAndStatusComponents.isAllowPullingDown() && rowsAndStatusComponents.getCurrentExpandAnimatorValue() > 0) {
            viewComponents.getLayoutManager().scrollToPositionWithOffset(0, AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) - listView.getPaddingTop());
            listView.post(() -> {
                profileLayouts.needLayout(true);
                if (attributesComponents.getExpandAnimator().isRunning()) {
                    attributesComponents.getExpandAnimator().cancel();
                }
                setAvatarExpandProgress(1f);
            });
        }
    }

    public boolean expandAvatar() {
        if (!AndroidUtilities.isTablet() && !rowsAndStatusComponents.isInLandscapeMode() && attributesComponents.getAvatarImage().getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled()) {
            rowsAndStatusComponents.setOpeningAvatar(true);
            rowsAndStatusComponents.setAllowPullingDown(true);
            View child = null;
            for (int i = 0; i < listView.getChildCount(); i++) {
                if (listView.getChildAdapterPosition(listView.getChildAt(i)) == 0) {
                    child = listView.getChildAt(i);
                    break;
                }
            }
            if (child != null) {
                RecyclerView.ViewHolder holder = listView.findContainingViewHolder(child);
                if (holder != null) {
                    Integer offset = positionToOffset.get(holder.getAdapterPosition());
                    if (offset != null) {
                        listView.smoothScrollBy(0, -(offset + (listView.getPaddingTop() - child.getTop() - actionBar.getMeasuredHeight())), CubicBezierInterpolator.EASE_OUT_QUINT);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setAvatarExpandProgress(float animatedFracture) {
        profileAnimation.setAvatarExpandProgress(this,  attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress(), animatedFracture);
    }

    private void updateTtlIcon() {
        if (ttlIconView == null) {
            return;
        }
        boolean visible = false;
        if (attributesComponents.getCurrentEncryptedChat() == null) {
            if (attributesComponents.getUserInfo() != null && attributesComponents.getUserInfo().ttl_period > 0) {
                visible = true;
            } else if (attributesComponents.getChatInfo() != null && ChatObject.canUserDoAdminAction(attributesComponents.getCurrentChat(), ChatObject.ACTION_DELETE_MESSAGES) && attributesComponents.getChatInfo().ttl_period > 0) {
                visible = true;
            }
        }
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, visible, 0.8f, rowsAndStatusComponents.isFragmentOpened());
    }


    public TLRPC.Chat getCurrentChat() {
        return attributesComponents.getCurrentChat();
    }

    @Override
    public boolean isFragmentOpened() {
        return isFragmentOpened;
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
        return onMemberClick(participant, isLong, false, view);
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly) {
        return onMemberClick(participant, isLong, resultOnly, null);
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly, View view) {
        if (getParentActivity() == null) {
            return false;
        }
        if (isLong) {
            TLRPC.User user = getMessagesController().getUser(participant.user_id);
            if (user == null || participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            selectedUser = participant.user_id;
            boolean allowKick;
            boolean canEditAdmin;
            boolean canRestrict;
            boolean editingAdmin;
            final TLRPC.ChannelParticipant channelParticipant;

            if (ChatObject.isChannel(attributesComponents.getCurrentChat())) {
                channelParticipant = ((TLRPC.TL_chatChannelParticipant) participant).channelParticipant;
                TLRPC.User u = getMessagesController().getUser(participant.user_id);
                canEditAdmin = ChatObject.canAddAdmins(attributesComponents.getCurrentChat());
                if (canEditAdmin && (channelParticipant instanceof TLRPC.TL_channelParticipantCreator || channelParticipant instanceof TLRPC.TL_channelParticipantAdmin && !channelParticipant.can_edit)) {
                    canEditAdmin = false;
                }
                allowKick = canRestrict = ChatObject.canBlockUsers(attributesComponents.getCurrentChat()) && (!(channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || channelParticipant instanceof TLRPC.TL_channelParticipantCreator) || channelParticipant.can_edit);
                if (attributesComponents.getCurrentChat().gigagroup) {
                    canRestrict = false;
                }
                editingAdmin = channelParticipant instanceof TLRPC.TL_channelParticipantAdmin;
            } else {
                channelParticipant = null;
                allowKick = attributesComponents.getCurrentChat().creator || participant instanceof TLRPC.TL_chatParticipant && (ChatObject.canBlockUsers(attributesComponents.getCurrentChat()) || participant.inviter_id == getUserConfig().getClientUserId());
                canEditAdmin = attributesComponents.getCurrentChat().creator;
                canRestrict = attributesComponents.getCurrentChat().creator;
                editingAdmin = participant instanceof TLRPC.TL_chatParticipantAdmin;
            }

            boolean result = (canEditAdmin || canRestrict || allowKick);
            if (resultOnly || !result) {
                return result;
            }

            Utilities.Callback<Integer> openRightsEdit = action -> {
                if (channelParticipant != null) {
                    openRightsEdit(action, user, participant, channelParticipant.admin_rights, channelParticipant.banned_rights, channelParticipant.rank, editingAdmin);
                } else {
                    openRightsEdit(action, user, participant, null, null, "", editingAdmin);
                }
            };

            ItemOptions.makeOptions(this, view)
                    .setScrimViewBackground(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundWhite)))
                    .addIf(canEditAdmin, R.drawable.msg_admins, editingAdmin ? LocaleController.getString(R.string.EditAdminRights) : LocaleController.getString(R.string.SetAsAdmin), () -> openRightsEdit.run(0))
                    .addIf(canRestrict, R.drawable.msg_permissions, LocaleController.getString(R.string.ChangePermissions), () -> {
                        if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || participant instanceof TLRPC.TL_chatParticipantAdmin) {
                            showDialog(
                                    new AlertDialog.Builder(getParentActivity(), attributesComponents.getResourcesProvider())
                                            .setTitle(LocaleController.getString(R.string.AppName))
                                            .setMessage(formatString("AdminWillBeRemoved", R.string.AdminWillBeRemoved, ContactsController.formatName(user.first_name, user.last_name)))
                                            .setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> openRightsEdit.run(1))
                                            .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                                            .create()
                            );
                        } else {
                            openRightsEdit.run(1);
                        }
                    })
                    .addIf(allowKick, R.drawable.msg_remove, LocaleController.getString(R.string.KickFromGroup), true, () -> {
                        kickUser(selectedUser, participant);
                    })
                    .setMinWidth(190)
                    .show();
        } else {
            if (participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            Bundle args = new Bundle();
            args.putLong("user_id", participant.user_id);
            args.putBoolean("preload_messages", true);
            presentFragment(new ProfileActivity(args));
        }
        return true;
    }

    private void openRightsEdit(int action, TLRPC.User user, TLRPC.ChatParticipant participant, TLRPC.TL_chatAdminRights adminRights, TLRPC.TL_chatBannedRights bannedRights, String rank, boolean editingAdmin) {
        boolean[] needShowBulletin = new boolean[1];
        ChatRightsEditActivity fragment = new ChatRightsEditActivity(user.id, attributesComponents.getChatId(), adminRights, attributesComponents.getCurrentChat().default_banned_rights, bannedRights, rank, action, true, false, null) {
            @Override
            public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
                if (!isOpen && backward && needShowBulletin[0] && BulletinFactory.canShowBulletin(ProfileActivity.this)) {
                    BulletinFactory.createPromoteToAdminBulletin(ProfileActivity.this, user.first_name).show();
                }
            }
        };
        fragment.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
            @Override
            public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                if (action == 0) {
                    if (participant instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.TL_chatChannelParticipant channelParticipant1 = ((TLRPC.TL_chatChannelParticipant) participant);
                        if (rights == 1) {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipantAdmin();
                            channelParticipant1.channelParticipant.flags |= 4;
                        } else {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                        }
                        channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                        channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                        channelParticipant1.channelParticipant.peer.user_id = participant.user_id;
                        channelParticipant1.channelParticipant.date = participant.date;
                        channelParticipant1.channelParticipant.banned_rights = rightsBanned;
                        channelParticipant1.channelParticipant.admin_rights = rightsAdmin;
                        channelParticipant1.channelParticipant.rank = rank;
                    } else if (participant != null) {
                        TLRPC.ChatParticipant newParticipant;
                        if (rights == 1) {
                            newParticipant = new TLRPC.TL_chatParticipantAdmin();
                        } else {
                            newParticipant = new TLRPC.TL_chatParticipant();
                        }
                        newParticipant.user_id = participant.user_id;
                        newParticipant.date = participant.date;
                        newParticipant.inviter_id = participant.inviter_id;
                        int index = attributesComponents.getChatInfo().participants.participants.indexOf(participant);
                        if (index >= 0) {
                            attributesComponents.getChatInfo().participants.participants.set(index, newParticipant);
                        }
                    }
                    if (rights == 1 && !editingAdmin) {
                        needShowBulletin[0] = true;
                    }
                } else if (action == 1) {
                    if (rights == 0) {
                        if (attributesComponents.getCurrentChat().megagroup && attributesComponents.getChatInfo() != null && attributesComponents.getChatInfo().participants != null) {
                            boolean changed = false;
                            for (int a = 0; a < attributesComponents.getChatInfo().participants.participants.size(); a++) {
                                TLRPC.ChannelParticipant p = ((TLRPC.TL_chatChannelParticipant) attributesComponents.getChatInfo().participants.participants.get(a)).channelParticipant;
                                if (MessageObject.getPeerId(p.peer) == participant.user_id) {
                                    attributesComponents.getChatInfo().participants_count--;
                                    attributesComponents.getChatInfo().participants.participants.remove(a);
                                    changed = true;
                                    break;
                                }
                            }
                            if (attributesComponents.getChatInfo() != null && attributesComponents.getChatInfo().participants != null) {
                                for (int a = 0; a < attributesComponents.getChatInfo().participants.participants.size(); a++) {
                                    TLRPC.ChatParticipant p = attributesComponents.getChatInfo().participants.participants.get(a);
                                    if (p.user_id == participant.user_id) {
                                        attributesComponents.getChatInfo().participants.participants.remove(a);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                updateOnlineCount(componentsFactory, ProfileActivity.this,true);
                                profileAdapter.updateRowsIds();
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void didChangeOwner(TLRPC.User user) {
                viewComponents.getUndoView().showWithAction(-attributesComponents.getChatId(), attributesComponents.getCurrentChat().megagroup ? UndoView.ACTION_OWNER_TRANSFERED_GROUP : UndoView.ACTION_OWNER_TRANSFERED_CHANNEL, user);
            }
        });
        presentFragment(fragment);
    }

    private void getChannelParticipants(boolean reload) {
        if (loadingUsers || participantsMap == null || attributesComponents.getChatInfo() == null) {
            return;
        }
        loadingUsers = true;
        final int delay = participantsMap.size() != 0 && reload ? 300 : 0;

        final TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
        req.channel = getMessagesController().getInputChannel(attributesComponents.getChatId());
        req.filter = new TLRPC.TL_channelParticipantsRecent();
        req.offset = reload ? 0 : participantsMap.size();
        req.limit = 200;
        int reqId = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> getNotificationCenter().doOnIdle(() -> {
            if (error == null) {
                TLRPC.TL_channels_channelParticipants res = (TLRPC.TL_channels_channelParticipants) response;
                getMessagesController().putUsers(res.users, false);
                getMessagesController().putChats(res.chats, false);
                if (res.users.size() < 200) {
                    usersEndReached = true;
                }
                if (req.offset == 0) {
                    participantsMap.clear();
                    attributesComponents.getChatInfo().participants = new TLRPC.TL_chatParticipants();
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, true, true);
                    getMessagesStorage().updateChannelUsers(attributesComponents.getChatId(), res.participants);
                }
                for (int a = 0; a < res.participants.size(); a++) {
                    TLRPC.TL_chatChannelParticipant participant = new TLRPC.TL_chatChannelParticipant();
                    participant.channelParticipant = res.participants.get(a);
                    participant.inviter_id = participant.channelParticipant.inviter_id;
                    participant.user_id = MessageObject.getPeerId(participant.channelParticipant.peer);
                    participant.date = participant.channelParticipant.date;
                    if (participantsMap.indexOfKey(participant.user_id) < 0) {
                        if (attributesComponents.getChatInfo().participants == null) {
                            attributesComponents.getChatInfo().participants = new TLRPC.TL_chatParticipants();
                        }
                        attributesComponents.getChatInfo().participants.participants.add(participant);
                        participantsMap.put(participant.user_id, participant);
                    }
                }
            }
            loadingUsers = false;
            profileAdapter.saveScrollPosition();
            profileAdapter.updateListAnimated(true);
        }), delay));
        getConnectionsManager().bindRequestToGuid(reqId, classGuid);
    }

    public void updateSelectedMediaTabText() {
        if (viewComponents.getSharedMediaLayout() == null || attributesComponents.getMediaCounterTextView() == null) {
            return;
        }
        int id = viewComponents.getSharedMediaLayout().getClosestTab();
        int[] mediaCount = attributesComponents.getSharedMediaPreloader().getLastMediaCount();
        if (id == SharedMediaLayout.TAB_PHOTOVIDEO) {
            if (mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0 && mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                if (mediaCount[MediaDataController.MEDIA_PHOTOVIDEO] <= 0) {
                    attributesComponents.getMediaCounterTextView().setText(LocaleController.getString(R.string.SharedMedia));
                } else {
                    attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("Media", mediaCount[MediaDataController.MEDIA_PHOTOVIDEO]));
                }
            } else if (viewComponents.getSharedMediaLayout().getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_PHOTOS_ONLY || mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]));
            } else if (viewComponents.getSharedMediaLayout().getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_VIDEOS_ONLY || mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
            } else {
                String str = String.format("%s, %s", LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]), LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
                attributesComponents.getMediaCounterTextView().setText(str);
            }
        } else if (id == SharedMediaLayout.TAB_FILES) {
            if (mediaCount[MediaDataController.MEDIA_FILE] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.getString(R.string.Files));
            } else {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("Files", mediaCount[MediaDataController.MEDIA_FILE]));
            }
        } else if (id == SharedMediaLayout.TAB_VOICE) {
            if (mediaCount[MediaDataController.MEDIA_AUDIO] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.getString(R.string.Voice));
            } else {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("Voice", mediaCount[MediaDataController.MEDIA_AUDIO]));
            }
        } else if (id == SharedMediaLayout.TAB_LINKS) {
            if (mediaCount[MediaDataController.MEDIA_URL] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.getString(R.string.SharedLinks));
            } else {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("Links", mediaCount[MediaDataController.MEDIA_URL]));
            }
        } else if (id == SharedMediaLayout.TAB_AUDIO) {
            if (mediaCount[MediaDataController.MEDIA_MUSIC] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.getString(R.string.Music));
            } else {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("MusicFiles", mediaCount[MediaDataController.MEDIA_MUSIC]));
            }
        } else if (id == SharedMediaLayout.TAB_GIF) {
            if (mediaCount[MediaDataController.MEDIA_GIF] <= 0) {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.getString(R.string.AccDescrGIFs));
            } else {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("GIFs", mediaCount[MediaDataController.MEDIA_GIF]));
            }
        } else if (id == SharedMediaLayout.TAB_COMMON_GROUPS) {
            attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("CommonGroups", attributesComponents.getUserInfo().common_chats_count));
        } else if (id == SharedMediaLayout.TAB_GROUPUSERS) {
            attributesComponents.getMediaCounterTextView().setText(attributesComponents.getOnlineTextView()[1].getText());
        } else if (id == SharedMediaLayout.TAB_STORIES) {
            if (rowsAndStatusComponents.isBot()) {
                attributesComponents.getMediaCounterTextView().setText(viewComponents.getSharedMediaLayout().getBotPreviewsSubtitle(false));
            } else {
                attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("ProfileStoriesCount", viewComponents.getSharedMediaLayout().getStoriesCount(id)));
            }
        } else if (id == SharedMediaLayout.TAB_BOT_PREVIEWS) {
            attributesComponents.getMediaCounterTextView().setText(viewComponents.getSharedMediaLayout().getBotPreviewsSubtitle(true));
        } else if (id == SharedMediaLayout.TAB_ARCHIVED_STORIES) {
            attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("ProfileStoriesArchiveCount", viewComponents.getSharedMediaLayout().getStoriesCount(id)));
        } else if (id == SharedMediaLayout.TAB_RECOMMENDED_CHANNELS) {
            final MessagesController.ChannelRecommendations rec = MessagesController.getInstance(currentAccount).getChannelRecommendations(getDialogId());
            attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString(rowsAndStatusComponents.isBot() ? "Bots" : "Channels", rec == null ? 0 : rec.chats.size() + rec.more));
        } else if (id == SharedMediaLayout.TAB_SAVED_MESSAGES) {
            int messagesCount = getMessagesController().getSavedMessagesController().getMessagesCount(getDialogId());
            attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralString("SavedMessagesCount", Math.max(1, messagesCount)));
        } else if (id == SharedMediaLayout.TAB_GIFTS) {
            attributesComponents.getMediaCounterTextView().setText(LocaleController.formatPluralStringComma("ProfileGiftsCount", viewComponents.getSharedMediaLayout().giftsContainer == null ? 0 : viewComponents.getSharedMediaLayout().giftsContainer.getGiftsCount()));
        }
    }

    public RecyclerListView getListView() {
        return listView;
    }
    public ComponentsFactory getComponentsFactory() {
        return componentsFactory;
    }
    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    profileAnimation.checkListViewScroll();
                    profileLayouts.needLayout(true);
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().onConfigurationChanged(newConfig);
        }
        invalidateIsInLandscapeMode();
        if (rowsAndStatusComponents.isInLandscapeMode() && rowsAndStatusComponents.isPulledDown()) {
            final View view = viewComponents.getLayoutManager().findViewByPosition(0);
            if (view != null) {
                listView.scrollBy(0, view.getTop() - AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
            }
        }
        fixLayout();
    }

    private void invalidateIsInLandscapeMode() {
        final Point size = new Point();
        final Display display = getParentActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        rowsAndStatusComponents.setInLandscapeMode(size.x > size.y);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.uploadStoryEnd || id == NotificationCenter.chatWasBoostedByUser) {
            checkCanSendStoryForPosting();
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            boolean infoChanged = (mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0;
            if (attributesComponents.getUserId() != 0) {
                if (infoChanged) {
                    profileData.updateProfileData(true);
                }
                if ((mask & MessagesController.UPDATE_MASK_PHONE) != 0) {
                    if (listView != null) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForPosition(rowsAndStatusComponents.getPhoneRow());
                        if (holder != null) {
                            listAdapter.onBindViewHolder(holder, rowsAndStatusComponents.getPhoneRow());
                        }
                    }
                }
            } else if (attributesComponents.getChatId() != 0) {
                if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_MEMBERS) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0) {
                    if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0) {
                        profileAdapter.updateListAnimated( true);
                    } else {
                        updateOnlineCount(componentsFactory, ProfileActivity.this,true);
                    }
                    profileData.updateProfileData(true);
                }
                if (infoChanged) {
                    if (listView != null) {
                        int count = listView.getChildCount();
                        for (int a = 0; a < count; a++) {
                            View child = listView.getChildAt(a);
                            if (child instanceof UserCell) {
                                ((UserCell) child).update(mask);
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.chatOnlineCountDidLoad) {
            Long chatId = (Long) args[0];
            if (attributesComponents.getChatInfo() == null || attributesComponents.getCurrentChat() == null || attributesComponents.getCurrentChat().id != chatId) {
                return;
            }
            attributesComponents.getChatInfo().online_count = (Integer) args[1];
            updateOnlineCount(componentsFactory, ProfileActivity.this,true);
            profileData.updateProfileData(false);
        } else if (id == NotificationCenter.contactsDidLoad || id == NotificationCenter.channelRightsUpdated) {
            profileActionBar.createActionBarMenu(true);
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (rowsAndStatusComponents.isCreatingChat()) {
                AndroidUtilities.runOnUIThread(() -> {
                    getNotificationCenter().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                    getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
                    Bundle args2 = new Bundle();
                    args2.putInt("enc_id", encryptedChat.id);
                    presentFragment(new ChatActivity(args2), true);
                });
            }
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) args[0];
            if (attributesComponents.getCurrentEncryptedChat() != null && chat.id == attributesComponents.getCurrentEncryptedChat().id) {
                attributesComponents.setCurrentEncryptedChat(chat);
                profileAdapter.updateListAnimated(false);
                if (viewComponents.getFlagSecureReason() != null) {
                    viewComponents.getFlagSecureReason().invalidate();
                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoad) {
            boolean oldValue = rowsAndStatusComponents.isUserBlocked();
            rowsAndStatusComponents.setUserBlocked(getMessagesController().blockePeers.indexOfKey(attributesComponents.getUserId()) >= 0);
            if (oldValue != rowsAndStatusComponents.isUserBlocked()) {
                profileActionBar.createActionBarMenu(true);
                profileAdapter.updateListAnimated( false);
            }
        } else if (id == NotificationCenter.groupCallUpdated) {
            Long chatId = (Long) args[0];
            if (attributesComponents.getCurrentChat() != null && chatId == attributesComponents.getCurrentChat().id && ChatObject.canManageCalls(attributesComponents.getCurrentChat())) {
                TLRPC.ChatFull chatFull = MessagesController.getInstance(currentAccount).getChatFull(chatId);
                if (chatFull != null) {
                    if (attributesComponents.getChatInfo() != null) {
                        chatFull.participants = attributesComponents.getChatInfo().participants;
                    }
                    attributesComponents.setChatInfo(chatFull);
                }
                if (viewComponents.getSharedMediaLayout() != null) {
                    viewComponents.getSharedMediaLayout().setChatInfo(attributesComponents.getChatInfo());
                }
                if (attributesComponents.getChatInfo() != null && (attributesComponents.getChatInfo().call == null && !rowsAndStatusComponents.isHasVoiceChatItem() || attributesComponents.getChatInfo().call != null && rowsAndStatusComponents.isHasVoiceChatItem())) {
                    profileActionBar.createActionBarMenu(false);
                }
                if (viewComponents.getStoryView() != null && attributesComponents.getChatInfo() != null) {
                    viewComponents.getStoryView().setStories(attributesComponents.getChatInfo().stories);
                }
                if (viewComponents.getGiftsView() != null) {
                    viewComponents.getGiftsView().update();
                }
                if (attributesComponents.getAvatarImage() != null) {
                    attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
                }
                if (chatId != 0) {
                    viewComponents.getOtherItem().setSubItemShown(ProfileParams.gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && attributesComponents.getChatInfo() != null && attributesComponents.getChatInfo().stargifts_available);
                }
            }
        } else if (id == NotificationCenter.chatInfoDidLoad) {
            final TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == attributesComponents.getChatId()) {
                final boolean byChannelUsers = (Boolean) args[2];
                if (attributesComponents.getChatInfo() instanceof TLRPC.TL_channelFull) {
                    if (chatFull.participants == null) {
                        chatFull.participants = attributesComponents.getChatInfo().participants;
                    }
                }
                final boolean loadChannelParticipants = attributesComponents.getChatInfo() == null && chatFull instanceof TLRPC.TL_channelFull;
                attributesComponents.setChatInfo(chatFull);
                if (mergeDialogId == 0 && attributesComponents.getChatInfo().migrated_from_chat_id != 0) {
                    mergeDialogId = -attributesComponents.getChatInfo().migrated_from_chat_id;
                    getMediaDataController().getMediaCount(mergeDialogId, attributesComponents.getTopicId(), MediaDataController.MEDIA_PHOTOVIDEO, classGuid, true);
                }
                fetchUsersFromChannelInfo();
                if (attributesComponents.getAvatarsViewPager() != null && !rowsAndStatusComponents.isTopic()) {
                    attributesComponents.getAvatarsViewPager().setChatInfo(attributesComponents.getChatInfo());
                }
                profileAdapter.updateListAnimated(true);
                TLRPC.Chat newChat = getMessagesController().getChat(attributesComponents.getChatId());
                if (newChat != null) {
                    attributesComponents.setCurrentChat(newChat);
                    profileActionBar.createActionBarMenu( true);
                }
                if (viewComponents.getFlagSecureReason() != null) {
                    viewComponents.getFlagSecureReason().invalidate();
                }
                if (attributesComponents.getCurrentChat().megagroup && (loadChannelParticipants || !byChannelUsers)) {
                    getChannelParticipants(true);
                }

                updateAutoDeleteItem(componentsFactory);
                updateTtlIcon();
                if (viewComponents.getStoryView() != null && attributesComponents.getChatInfo() != null) {
                    viewComponents.getStoryView().setStories(attributesComponents.getChatInfo().stories);
                }
                if (viewComponents.getGiftsView() != null) {
                    viewComponents.getGiftsView().update();
                }
                if (attributesComponents.getAvatarImage() != null) {
                    attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
                }
                if (viewComponents.getSharedMediaLayout() != null) {
                    viewComponents.getSharedMediaLayout().setChatInfo(attributesComponents.getChatInfo());
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack(true);
        } else if (id == NotificationCenter.botInfoDidLoad) {
            final TL_bots.BotInfo info = (TL_bots.BotInfo) args[0];
            if (info.user_id == attributesComponents.getUserId()) {
                botInfo = info;
                profileAdapter.updateListAnimated(false);
            }
        } else if (id == NotificationCenter.userInfoDidLoad) {
            final long uid = (Long) args[0];
            if (uid == attributesComponents.getUserId()) {
                attributesComponents.setUserInfo((TLRPC.UserFull) args[1]);
                if (viewComponents.getStoryView() != null) {
                    viewComponents.getStoryView().setStories(attributesComponents.getUserInfo().stories);
                }
                if (viewComponents.getGiftsView() != null) {
                    viewComponents.getGiftsView().update();
                }
                if (attributesComponents.getAvatarImage() != null) {
                    attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
                }
                if (viewComponents.getSharedMediaLayout() != null) {
                    viewComponents.getSharedMediaLayout().setUserInfo(attributesComponents.getUserInfo());
                }
                if (viewComponents.getImageUpdater() != null) {
                    if (listAdapter != null && !TextUtils.equals(attributesComponents.getUserInfo().about, attributesComponents.getCurrentBio())) {
                        listAdapter.notifyItemChanged(rowsAndStatusComponents.getBioRow());
                    }
                } else {
                    if (!rowsAndStatusComponents.isOpenAnimationInProgress() && !rowsAndStatusComponents.isCallItemVisible()) {
                        profileActionBar.createActionBarMenu(true);
                    } else {
                        recreateMenuAfterAnimation = true;
                    }
                    profileAdapter.updateListAnimated(false);
                    if (viewComponents.getSharedMediaLayout() != null) {
                        viewComponents.getSharedMediaLayout().setCommonGroupsCount(attributesComponents.getUserInfo().common_chats_count);
                        updateSelectedMediaTabText();
                        if (attributesComponents.getSharedMediaPreloader() == null || attributesComponents.getSharedMediaPreloader().isMediaWasLoaded()) {
                            resumeDelayedFragmentAnimation();
                            profileLayouts.needLayout(true);
                        }
                    }
                }
                updateAutoDeleteItem(componentsFactory);
                updateTtlIcon();
                if (viewComponents.getProfileChannelMessageFetcher() == null && !isSettings()) {
                    viewComponents.initProfileChannelMessageFetcher(new ProfileChannelCell.ChannelMessageFetcher(getCurrentAccount()));
                    viewComponents.getProfileChannelMessageFetcher().subscribe(() -> profileAdapter.updateListAnimated( false));
                    viewComponents.getProfileChannelMessageFetcher().fetch(attributesComponents.getUserInfo());
                }
                if (!isSettings()) {
                    ProfileBirthdayEffect.BirthdayEffectFetcher oldFetcher = birthdayFetcher;
                    birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, attributesComponents.getUserInfo(), birthdayFetcher);
                    createdBirthdayFetcher = birthdayFetcher != oldFetcher;
                    if (birthdayFetcher != null) {
                        birthdayFetcher.subscribe(this::createBirthdayEffect);
                    }
                }
                if (viewComponents.getOtherItem() != null) {
                    if (profileData.hasPrivacyCommand()) {
                        viewComponents.getOtherItem().showSubItem(ProfileParams.bot_privacy);
                    } else {
                        viewComponents.getOtherItem().hideSubItem(ProfileParams.bot_privacy);
                    }
                }
            }
        } else if (id == NotificationCenter.privacyRulesUpdated) {
            if (viewComponents.getQrItem() != null) {
                updateQrItemVisibility(componentsFactory,true);
            }
        } else if (id == NotificationCenter.didReceiveNewMessages) {
            final boolean scheduled = (Boolean) args[2];
            if (scheduled) {
                return;
            }
            final long did = getDialogId();
            if (did == (Long) args[0]) {
                boolean enc = DialogObject.isEncryptedDialog(did);
                ArrayList<MessageObject> arr = (ArrayList<MessageObject>) args[1];
                for (int a = 0; a < arr.size(); a++) {
                    MessageObject obj = arr.get(a);
                    if (attributesComponents.getCurrentEncryptedChat() != null && obj.messageOwner.action instanceof TLRPC.TL_messageEncryptedAction && obj.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL) {
                        TLRPC.TL_decryptedMessageActionSetMessageTTL action = (TLRPC.TL_decryptedMessageActionSetMessageTTL) obj.messageOwner.action.encryptedAction;
                        if (listAdapter != null) {
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        } else if (id == NotificationCenter.reloadInterface) {
            profileAdapter.updateListAnimated(false);
        } else if (id == NotificationCenter.newSuggestionsAvailable) {
            final int prevRow1 = rowsAndStatusComponents.getPasswordSuggestionRow();
            final int prevRow2 = rowsAndStatusComponents.getPhoneSuggestionRow();
            final int prevRow3 = rowsAndStatusComponents.getGraceSuggestionRow();
            profileAdapter.updateRowsIds();
            if (prevRow1 != rowsAndStatusComponents.getPasswordSuggestionRow() || prevRow2 != rowsAndStatusComponents.getPhoneSuggestionRow() || prevRow3 != rowsAndStatusComponents.getGraceSuggestionRow()) {
                listAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.topicsDidLoaded) {
            if (rowsAndStatusComponents.isTopic()) {
                profileData.updateProfileData(false);
            }
        } else if (id == NotificationCenter.updateSearchSettings) {
            if (searchAdapter != null) {
                searchAdapter.searchArray = searchAdapter.onCreateSearchArray();
                searchAdapter.recentSearches.clear();
                searchAdapter.updateSearchArray();
                searchAdapter.search(searchAdapter.lastSearchString);
            }
        } else if (id == NotificationCenter.reloadDialogPhotos) {
            profileData.updateProfileData(false);
        } else if (id == NotificationCenter.storiesUpdated || id == NotificationCenter.storiesReadUpdated) {
            if (attributesComponents.getAvatarImage() != null) {
                attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
                updateAvatarRoundRadius();
            }
            if (viewComponents.getStoryView() != null) {
                if (attributesComponents.getUserInfo() != null) {
                    viewComponents.getStoryView().setStories(attributesComponents.getUserInfo().stories);
                } else if (attributesComponents.getChatInfo() != null) {
                    viewComponents.getStoryView().setStories(attributesComponents.getChatInfo().stories);
                }
            }
        } else if (id == NotificationCenter.userIsPremiumBlockedUpadted) {
            if (viewComponents.getOtherItem() != null) {
                viewComponents.getOtherItem().setSubItemShown(ProfileParams.start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(attributesComponents.getUserId())));
            }
            profileColors.updateEditColorIcon(ProfileActivity.this);
        } else if (id == NotificationCenter.currentUserPremiumStatusChanged) {
            profileColors.updateEditColorIcon(ProfileActivity.this);
        } else if (id == NotificationCenter.starBalanceUpdated) {
            profileAdapter.updateListAnimated( false);
        } else if (id == NotificationCenter.botStarsUpdated) {
            profileAdapter.updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsTransactionsLoaded) {
            profileAdapter.updateListAnimated(false);
        } else if (id == NotificationCenter.dialogDeleted) {
            final long dialogId = (long) args[0];
            if (getDialogId() == dialogId) {
                if (parentLayout != null && parentLayout.getLastFragment() == this) {
                    finishFragment();
                } else {
                    removeSelfFromStack();
                }
            }
        } else if (id == NotificationCenter.channelRecommendationsLoaded) {
            final long dialogId = (long) args[0];
            if (rowsAndStatusComponents.getSharedMediaRow() < 0 && dialogId == getDialogId()) {
                profileAdapter.updateRowsIds();
                updateSelectedMediaTabText();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.starUserGiftsLoaded) {
            final long dialogId = (long) args[0];
            if (dialogId == getDialogId() && !isSettings()) {
                if (rowsAndStatusComponents.getSharedMediaRow() < 0) {
                    profileAdapter.updateRowsIds();
                    updateSelectedMediaTabText();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if (viewComponents.getSharedMediaLayout() != null) {
                            viewComponents.getSharedMediaLayout().updateTabs(true);
                            viewComponents.getSharedMediaLayout().updateAdapters();
                        }
                    });
                } else if (viewComponents.getSharedMediaLayout() != null) {
                    viewComponents.getSharedMediaLayout().updateTabs(true);
                }
            }
        }
    }

    @Override
    public boolean needDelayOpenAnimation() {
        return rowsAndStatusComponents.getPlayProfileAnimation() == 0;
    }

    @Override
    public void mediaCountUpdated() {
        if (viewComponents.getSharedMediaLayout() != null && attributesComponents.getSharedMediaPreloader() != null) {
            viewComponents.getSharedMediaLayout().setNewMediaCounts(attributesComponents.getSharedMediaPreloader().getLastMediaCount());
        }
        updateSharedMediaRows();
        updateSelectedMediaTabText();

        if (attributesComponents.getUserInfo() != null) {
            resumeDelayedFragmentAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().onResume();
        }
        invalidateIsInLandscapeMode();
        if (listAdapter != null) {
            // saveScrollPosition();
            rowsAndStatusComponents.setFirstLayout(true);
            listAdapter.notifyDataSetChanged();
        }
        if (!parentLayout.isInPreviewMode() && blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            blurredView.setVisibility(View.GONE);
            blurredView.setBackground(null);
        }

        if (viewComponents.getImageUpdater() != null) {
            viewComponents.getImageUpdater().onResume();
            setParentActivityTitle(LocaleController.getString(R.string.Settings));
        }

        profileData.updateProfileData(true);
        fixLayout();
        if (attributesComponents.getNameTextView()[1] != null) {
            setParentActivityTitle(attributesComponents.getNameTextView()[1].getText());
        }
        if (attributesComponents.getUserId() != 0) {
            final TLRPC.User user = getMessagesController().getUser(attributesComponents.getUserId());
            if (user != null && user.photo == null) {
                if (rowsAndStatusComponents.getExtraHeight() >= AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) {
                    attributesComponents.getExpandAnimator().cancel();
                    attributesComponents.getExpandAnimatorValues()[0] = 1f;
                    attributesComponents.getExpandAnimatorValues()[1] = 0f;
                    setAvatarExpandProgress(1f);
                    attributesComponents.getAvatarsViewPager().setVisibility(View.GONE);
                    rowsAndStatusComponents.setExtraHeight(AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
                    rowsAndStatusComponents.setAllowPullingDown(false);
                    viewComponents.getLayoutManager().scrollToPositionWithOffset(0, AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) - listView.getPaddingTop());
                }
            }
        }
        if (viewComponents.getFlagSecureReason() != null) {
            viewComponents.getFlagSecureReason().attach();
        }
        profileData.updateItemsUsername(ProfileActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewComponents.getUndoView() != null) {
            viewComponents.getUndoView().hide(true, 0);
        }
        if (viewComponents.getImageUpdater() != null) {
            viewComponents.getImageUpdater().onPause();
        }
        if (viewComponents.getFlagSecureReason() != null) {
            viewComponents.getFlagSecureReason().detach();
        }
        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().onPause();
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        if (attributesComponents.getAvatarsViewPager() != null && attributesComponents.getAvatarsViewPager().getVisibility() == View.VISIBLE && attributesComponents.getAvatarsViewPager().getRealCount() > 1) {
            attributesComponents.getAvatarsViewPager().getHitRect(rect);
            if (event != null && rect.contains((int) event.getX(), (int) event.getY() - actionBar.getMeasuredHeight())) {
                return false;
            }
        }
        if (rowsAndStatusComponents.getSharedMediaRow() == -1 || viewComponents.getSharedMediaLayout() == null) {
            return true;
        }
        if (!viewComponents.getSharedMediaLayout().isSwipeBackEnabled()) {
            return false;
        }
        viewComponents.getSharedMediaLayout().getHitRect(rect);
        if (event != null && !rect.contains((int) event.getX(), (int) event.getY() - actionBar.getMeasuredHeight())) {
            return true;
        }
        return viewComponents.getSharedMediaLayout().isCurrentTabFirst();
    }

    @Override
    public boolean canBeginSlide() {
        if (!viewComponents.getSharedMediaLayout().isSwipeBackEnabled()) {
            return false;
        }
        return super.canBeginSlide();
    }

    public UndoView getUndoView() {
        return viewComponents.getUndoView();
    }

    public boolean onBackPressed() {
        if (closeSheet()) {
            return false;
        }
        return actionBar.isEnabled() && (rowsAndStatusComponents.getSharedMediaRow() == -1 || viewComponents.getSharedMediaLayout() == null || !viewComponents.getSharedMediaLayout().closeActionMode());
    }

    @Override
    public void onBecomeFullyHidden() {
        if (viewComponents.getUndoView() != null) {
            viewComponents.getUndoView().hide(true, 0);
        }
        super.onBecomeFullyHidden();
        fullyVisible = false;
    }

    public void setPlayProfileAnimation(int type) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        if (!AndroidUtilities.isTablet()) {
            rowsAndStatusComponents.setNeedTimerImage(type != 0);
            rowsAndStatusComponents.setNeedStarImage(type != 0);
            if (preferences.getBoolean("view_animations", true)) {
                rowsAndStatusComponents.setPlayProfileAnimation(type);
            } else if (type == 2) {
                rowsAndStatusComponents.setExpandPhoto(true);
            }
        }
    }

    private void updateSharedMediaRows() {
        if (listAdapter == null) {
            return;
        }
        profileAdapter.updateListAnimated(false);
    }

    public boolean isFragmentOpened;

    @Override
    public void onTransitionAnimationStart(boolean isOpen, boolean backward) {
        super.onTransitionAnimationStart(isOpen, backward);
        isFragmentOpened = isOpen;
        if ((!isOpen && backward || isOpen && !backward) && rowsAndStatusComponents.getPlayProfileAnimation() != 0 && rowsAndStatusComponents.isAllowProfileAnimation() && !rowsAndStatusComponents.isPulledDown()) {
            rowsAndStatusComponents.setOpenAnimationInProgress(true);
        }
        if (isOpen) {
            if (viewComponents.getImageUpdater() != null) {
                transitionIndex = getNotificationCenter().setAnimationInProgress(transitionIndex, new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoad, NotificationCenter.mediaCountsDidLoad, NotificationCenter.userInfoDidLoad, NotificationCenter.needCheckSystemBarColors});
            } else {
                transitionIndex = getNotificationCenter().setAnimationInProgress(transitionIndex, new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoad, NotificationCenter.mediaCountsDidLoad, NotificationCenter.needCheckSystemBarColors});
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !backward && getParentActivity() != null) {
                navigationBarAnimationColorFrom = getParentActivity().getWindow().getNavigationBarColor();
            }
        }
        rowsAndStatusComponents.setTransitionAnimationInProress(true);
        checkPhotoDescriptionAlpha(componentsFactory, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress());
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            if (!backward) {
                if (rowsAndStatusComponents.getPlayProfileAnimation() != 0 && rowsAndStatusComponents.isAllowProfileAnimation()) {
                    if (rowsAndStatusComponents.getPlayProfileAnimation() == 1) {
                        rowsAndStatusComponents.setCurrentExpandAnimatorValue(0f);
                    }
                    rowsAndStatusComponents.setOpenAnimationInProgress(false);
                    profileAnimation.checkListViewScroll();
                    if (recreateMenuAfterAnimation) {
                        profileActionBar.createActionBarMenu(true);
                    }
                }
                if (!rowsAndStatusComponents.isFragmentOpened()) {
                    rowsAndStatusComponents.setFragmentOpened(true);
                    rowsAndStatusComponents.setInvalidateScroll(true);
                    fragmentView.requestLayout();
                }
            }
            getNotificationCenter().onAnimationFinish(transitionIndex);

            if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
                blurredView.setVisibility(View.GONE);
                blurredView.setBackground(null);
            }
        }
        rowsAndStatusComponents.setTransitionAnimationInProress(false);
        checkPhotoDescriptionAlpha(componentsFactory, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress());
    }

    @Keep
    public float getAvatarAnimationProgress() {
        return rowsAndStatusComponents.getAvatarAnimationProgress();
    }


    @Keep
    public void setAvatarAnimationProgress(float progress) {
        rowsAndStatusComponents.setAvatarAnimationProgress(rowsAndStatusComponents.setCurrentExpanAnimatorFractureAndReturn(progress));
        checkPhotoDescriptionAlpha(componentsFactory, attributesComponents.getPhotoDescriptionProgress(), attributesComponents.getCustomAvatarProgress());
        if (rowsAndStatusComponents.getPlayProfileAnimation() == 2) {
            attributesComponents.getAvatarImage().setProgressToExpand(progress);
        }

        listView.setAlpha(progress);

        listView.setTranslationX(AndroidUtilities.dp(48) - AndroidUtilities.dp(48) * progress);

        int color;
        if (rowsAndStatusComponents.getPlayProfileAnimation() == 2 && avatarColor != 0) {
            color = avatarColor;
        } else {
            color = AvatarDrawable.getProfileBackColorForId(attributesComponents.getUserId() != 0 || ChatObject.isChannel(attributesComponents.getChatId(), currentAccount) && !attributesComponents.getCurrentChat().megagroup ? 5 : attributesComponents.getChatId(), attributesComponents.getResourcesProvider());
        }

        int actionBarColor = actionBarAnimationColorFrom != 0 ? actionBarAnimationColorFrom : getThemedColor(Theme.key_actionBarDefault);
        int actionBarColor2 = actionBarColor;
        if (SharedConfig.chatBlurEnabled()) {
            actionBarColor = ColorUtils.setAlphaComponent(actionBarColor, 0);
        }
        viewComponents.getTopView().setBackgroundColor(ColorUtils.blendARGB(actionBarColor, color, progress));
        viewComponents.getTimerDrawable().setBackgroundColor(ColorUtils.blendARGB(actionBarColor2, color, progress));

        color = AvatarDrawable.getIconColorForId(attributesComponents.getUserId() != 0 || ChatObject.isChannel(attributesComponents.getChatId(), currentAccount) && !attributesComponents.getCurrentChat().megagroup ? 5 : attributesComponents.getChatId(), attributesComponents.getResourcesProvider());
        int iconColor = attributesComponents.getPeerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon);
        actionBar.setItemsColor(ColorUtils.blendARGB(iconColor, color, rowsAndStatusComponents.getAvatarAnimationProgress()), false);

        color = getThemedColor(Theme.key_profile_title);
        int titleColor = getThemedColor(Theme.key_actionBarDefaultTitle);
        for (int i = 0; i < 2; i++) {
            if (attributesComponents.getNameTextView()[i] == null || i == 1 && rowsAndStatusComponents.getPlayProfileAnimation() == 2) {
                continue;
            }
            attributesComponents.getNameTextView()[i].setTextColor(ColorUtils.blendARGB(titleColor, color, progress));
        }

        color = attributesComponents.getIsOnline()[0] ? getThemedColor(Theme.key_profile_status) : AvatarDrawable.getProfileTextColorForId(attributesComponents.getUserId() != 0 || ChatObject.isChannel(attributesComponents.getChatId(), currentAccount) && !attributesComponents.getCurrentChat().megagroup ? 5 : attributesComponents.getChatId(), attributesComponents.getResourcesProvider());
        int subtitleColor = getThemedColor(attributesComponents.getIsOnline()[0] ? Theme.key_chat_status : Theme.key_actionBarDefaultSubtitle);
        for (int i = 0; i < 3; i++) {
            if (attributesComponents.getOnlineTextView()[i] == null || i == 1 || i == 2 && rowsAndStatusComponents.getPlayProfileAnimation() == 2) {
                continue;
            }
            attributesComponents.getOnlineTextView()[i].setTextColor(ColorUtils.blendARGB(i == 0 ? subtitleColor : profileColors.applyPeerColor(subtitleColor, true, attributesComponents.getIsOnline()[0]), i == 0 ? color : profileColors.applyPeerColor(color, true, attributesComponents.getIsOnline()[0]), progress));
        }
        rowsAndStatusComponents.setExtraHeight(rowsAndStatusComponents.getInitialAnimationExtraHeight() * progress);
        color = AvatarDrawable.getProfileColorForId(attributesComponents.getUserId() != 0 ? attributesComponents.getUserId() : attributesComponents.getChatId(), attributesComponents.getResourcesProvider());
        int color2 = AvatarDrawable.getColorForId(attributesComponents.getUserId() != 0 ? attributesComponents.getUserId() : attributesComponents.getChatId());
        if (color != color2) {
            attributesComponents.getAvatarDrawable().setColor(ColorUtils.blendARGB(color2, color, progress));
            attributesComponents.getAvatarImage().invalidate();
        }

        if (navigationBarAnimationColorFrom != 0) {
            color = ColorUtils.blendARGB(navigationBarAnimationColorFrom, getNavigationBarColor(), progress);
            setNavigationBarColor(color);
        }

        viewComponents.getTopView().invalidate();

        profileLayouts.needLayout( true);
        if (fragmentView != null) {
            fragmentView.invalidate();
        }

        if (viewComponents.getAboutLinkCell() != null) {
            viewComponents.getAboutLinkCell().invalidate();
        }

        if (getDialogId() > 0) {
            if (attributesComponents.getAvatarImage() != null) {
                attributesComponents.getAvatarImage().setProgressToStoriesInsets(rowsAndStatusComponents.getAvatarAnimationProgress());
            }
            if (viewComponents.getStoryView() != null) {
                viewComponents.getStoryView().setProgressToStoriesInsets(rowsAndStatusComponents.getAvatarAnimationProgress());
            }
            if (viewComponents.getGiftsView() != null) {
                viewComponents.getGiftsView().setProgressToStoriesInsets(rowsAndStatusComponents.getAvatarAnimationProgress());
            }
        }
    }

    boolean profileTransitionInProgress;

    @Override
    public AnimatorSet onCustomTransitionAnimation(final boolean isOpen, final Runnable callback) {
        if (rowsAndStatusComponents.getPlayProfileAnimation() != 0 && rowsAndStatusComponents.isAllowProfileAnimation() && !rowsAndStatusComponents.isPulledDown() && !rowsAndStatusComponents.isReportSpam()) {

            if (rowsAndStatusComponents.getPlayProfileAnimation() == 2) {
                viewComponents.getButtonsGroup().setBlurEnabled(true);
            }

            if (viewComponents.getTimeItem() != null) {
                viewComponents.getTimeItem().setAlpha(1.0f);
            }
            if (viewComponents.getStarFgItem() != null) {
                viewComponents.getStarFgItem().setAlpha(1.0f);
                viewComponents.getStarFgItem().setScaleX(1.0f);
                viewComponents.getStarFgItem().setScaleY(1.0f);
            }
            if (viewComponents.getStarBgItem() != null) {
                viewComponents.getStarBgItem().setAlpha(1.0f);
                viewComponents.getStarBgItem().setScaleX(1.0f);
                viewComponents.getStarBgItem().setScaleY(1.0f);
            }
            viewComponents.setPreviousTransitionMainFragment(null);
            if (parentLayout != null && parentLayout.getFragmentStack().size() >= 2) {
                BaseFragment fragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
                if (fragment instanceof ChatActivityInterface) {
                    attributesComponents.setPreviousTransitionFragment((ChatActivityInterface) fragment);
                }
                if (fragment instanceof DialogsActivity) {
                    DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                    if (dialogsActivity.rightSlidingDialogContainer != null && dialogsActivity.rightSlidingDialogContainer.currentFragment instanceof ChatActivityInterface) {
                        viewComponents.setPreviousTransitionMainFragment(dialogsActivity);
                        attributesComponents.setPreviousTransitionFragment((ChatActivityInterface) dialogsActivity.rightSlidingDialogContainer.currentFragment);
                    }
                }
            }
            final boolean fromChat = attributesComponents.getPreviousTransitionFragment() instanceof ChatActivity && attributesComponents.getPreviousTransitionFragment().getCurrentChat() != null;
            if (attributesComponents.getPreviousTransitionFragment() != null) {
                updateTimeItem(componentsFactory);
                updateStar(componentsFactory, viewComponents.getStarBgItem(), viewComponents.getStarFgItem());
            }
            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(rowsAndStatusComponents.getPlayProfileAnimation() == 2 ? 250 : 180);
            listView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            ActionBarMenu menu = actionBar.createMenu();
            if (menu.getItem(10) == null) {
                if (viewComponents.getAnimatingItem() == null) {
                    viewComponents.setAnimatingItem(menu.addItem(10, R.drawable.ic_ab_other));
                }
            }
            if (isOpen) {
                for (int i = 0; i < 2; i++) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) attributesComponents.getOnlineTextView()[i + 1].getLayoutParams();
                    layoutParams.rightMargin = (int) (-21 * AndroidUtilities.density + AndroidUtilities.dp(8));
                    attributesComponents.getOnlineTextView()[i + 1].setLayoutParams(layoutParams);
                }


                if (rowsAndStatusComponents.getPlayProfileAnimation() != 2) {
                    int width = (int) Math.ceil(AndroidUtilities.displaySize.x - AndroidUtilities.dp(118 + 8) + 21 * AndroidUtilities.density);
                    float width2 = attributesComponents.getNameTextView()[1].getPaint().measureText(attributesComponents.getNameTextView()[1].getText().toString()) * 1.12f + attributesComponents.getNameTextView()[1].getSideDrawablesSize();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) attributesComponents.getNameTextView()[1].getLayoutParams();
                    if (width < width2) {
                        layoutParams.width = (int) Math.ceil(width / 1.12f);
                    } else {
                        layoutParams.width = LayoutHelper.WRAP_CONTENT;
                    }
                    attributesComponents.getNameTextView()[1].setLayoutParams(layoutParams);

                    rowsAndStatusComponents.setInitialAnimationExtraHeight(AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
                } else {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) attributesComponents.getNameTextView()[1].getLayoutParams();
                    layoutParams.width = (int) ((AndroidUtilities.displaySize.x - AndroidUtilities.dp(32)) / 1.67f);
                    attributesComponents.getNameTextView()[1].setLayoutParams(layoutParams);
                    //attributesComponents.getNameTextView()[1].setTranslationX(AndroidUtilities.dp(18));
                    //attributesComponents.getOnlineTextView()[1].setTranslationX(AndroidUtilities.dp(16));
                }
                fragmentView.setBackgroundColor(0);
                setAvatarAnimationProgress(0);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(ObjectAnimator.ofFloat(this, "avatarAnimationProgress", 0.0f, 1.0f));
                profileAnimation.setGroupButtonTransitionProgress(0);
                animators.add(ObjectAnimator.ofFloat(attributesComponents, "groupButtonTransitionProgress", 0f, 1f));
                if (attributesComponents.getWriteButton() != null && attributesComponents.getWriteButton().getTag() == null) {
                    attributesComponents.getWriteButton().setScaleX(0.2f);
                    attributesComponents.getWriteButton().setScaleY(0.2f);
                    attributesComponents.getWriteButton().setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getWriteButton(), View.SCALE_X, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getWriteButton(), View.SCALE_Y, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getWriteButton(), View.ALPHA, 1.0f));
                }
                if (rowsAndStatusComponents.getPlayProfileAnimation() == 2) {
                    avatarColor = getAverageColor(attributesComponents.getAvatarImage().getImageReceiver());
                    attributesComponents.getNameTextView()[1].setTextColor(Color.WHITE);
                    attributesComponents.getOnlineTextView()[1].setTextColor(0xB3FFFFFF);
                    actionBar.setItemsBackgroundColor(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, false);
                    if (viewComponents.getShowStatusButton() != null) {
                        viewComponents.getShowStatusButton().setBackgroundColor(0x23ffffff);
                    }
                    viewComponents.getOverlaysView().setOverlaysVisible();
                }
                for (int a = 0; a < 2; a++) {
                    attributesComponents.getNameTextView()[a].setAlpha(a == 0 ? 1.0f : 0.0f);
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getNameTextView()[a], View.ALPHA, a == 0 ? 0.0f : 1.0f));
                }
                if (viewComponents.getStoryView() != null) {
                    if (getDialogId() > 0) {
                        viewComponents.getStoryView().setAlpha(0f);
                        animators.add(ObjectAnimator.ofFloat(viewComponents.getStoryView(), View.ALPHA, 1.0f));
                    } else {
                        viewComponents.getStoryView().setAlpha(1f);
                        viewComponents.getStoryView().setFragmentTransitionProgress(0);
                        animators.add(ObjectAnimator.ofFloat(viewComponents.getStoryView(), ProfileStoriesView.FRAGMENT_TRANSITION_PROPERTY, 1.0f));
                    }
                }
                if (viewComponents.getGiftsView() != null) {
                    viewComponents.getGiftsView().setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getGiftsView(), View.ALPHA, 1.0f));
                }
                if (viewComponents.getTimeItem().getTag() != null) {
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getTimeItem(), View.ALPHA, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getTimeItem(), View.SCALE_X, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getTimeItem(), View.SCALE_Y, 1.0f, 0.0f));
                }
                if (viewComponents.getStarFgItem().getTag() != null) {
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarFgItem(), View.ALPHA, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarFgItem(), View.SCALE_X, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarFgItem(), View.SCALE_Y, 1.0f, 0.0f));
                }
                if (viewComponents.getStarBgItem().getTag() != null) {
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarBgItem(), View.ALPHA, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarBgItem(), View.SCALE_X, 1.0f, 0.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarBgItem(), View.SCALE_Y, 1.0f, 0.0f));
                }
                if (viewComponents.getAnimatingItem() != null) {
                    viewComponents.getAnimatingItem().setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getAnimatingItem(), View.ALPHA, 0.0f));
                }
                if (rowsAndStatusComponents.isCallItemVisible() && (attributesComponents.getChatId() != 0 || fromChat)) {
                    viewComponents.getCallItem().setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getCallItem(), View.ALPHA, 1.0f));
                }
                if (rowsAndStatusComponents.isVideoCallItemVisible()) {
                    viewComponents.getVideoCallItem().setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getVideoCallItem(), View.ALPHA, 1.0f));
                }
                if (rowsAndStatusComponents.isEditItemVisible()) {
                    viewComponents.getEditItem().setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getEditItem(), View.ALPHA, 1.0f));
                }
                if (ttlIconView.getTag() != null) {
                    ttlIconView.setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(ttlIconView, View.ALPHA, 1.0f));
                }
                if (viewComponents.getFloatingButtonContainer() != null) {
                    viewComponents.getFloatingButtonContainer().setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getFloatingButtonContainer(), View.ALPHA, 1.0f));
                }
                if (attributesComponents.getAvatarImage() != null) {
                    attributesComponents.getAvatarImage().setCrossfadeProgress(1.0f);
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getAvatarImage(), AvatarImageView.CROSSFADE_PROGRESS, 0.0f));
                }

                boolean onlineTextCrosafade = false;

                if (attributesComponents.getPreviousTransitionFragment() != null) {
                    ChatAvatarContainer avatarContainer = attributesComponents.getPreviousTransitionFragment().getAvatarContainer();
                    if (avatarContainer != null && avatarContainer.getSubtitleTextView() instanceof SimpleTextView && ((SimpleTextView) avatarContainer.getSubtitleTextView()).getLeftDrawable() != null || avatarContainer.statusMadeShorter[0]) {
                        viewComponents.setTransitionOnlineText(avatarContainer.getSubtitleTextView());
                        viewComponents.getAvatarContainer2().invalidate();
                        onlineTextCrosafade = true;
                        attributesComponents.getOnlineTextView()[0].setAlpha(0f);
                        attributesComponents.getOnlineTextView()[1].setAlpha(0f);
                        animators.add(ObjectAnimator.ofFloat(attributesComponents.getOnlineTextView()[1], View.ALPHA, 1.0f));
                    }
                }

                if (!onlineTextCrosafade) {
                    for (int a = 0; a < 2; a++) {
                        attributesComponents.getOnlineTextView()[a].setAlpha(a == 0 ? 1.0f : 0.0f);
                        animators.add(ObjectAnimator.ofFloat(attributesComponents.getOnlineTextView()[a], View.ALPHA, a == 0 ? 0.0f : 1.0f));
                    }
                }
                animatorSet.playTogether(animators);
            } else {
                rowsAndStatusComponents.setInitialAnimationExtraHeight(rowsAndStatusComponents.getExtraHeight());
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(ObjectAnimator.ofFloat(this, "avatarAnimationProgress", 1.0f, 0.0f));
                profileAnimation.setGroupButtonTransitionProgress(1f);
                animators.add(ObjectAnimator.ofFloat(attributesComponents, "groupButtonTransitionProgress", 1.0f, 0.0f));
                if (attributesComponents.getWriteButton() != null) {
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getWriteButton(), View.SCALE_X, 0.2f));
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getWriteButton(), View.SCALE_Y, 0.2f));
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getWriteButton(), View.ALPHA, 0.0f));
                }
                for (int a = 0; a < 2; a++) {
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getNameTextView()[a], View.ALPHA, a == 0 ? 1.0f : 0.0f));
                }
                if (viewComponents.getStoryView() != null) {
                    if (attributesComponents.getDialogId() > 0) {
                        animators.add(ObjectAnimator.ofFloat(viewComponents.getStoryView(), View.ALPHA, 0.0f));
                    } else {
                        animators.add(ObjectAnimator.ofFloat(viewComponents.getStoryView(), ProfileStoriesView.FRAGMENT_TRANSITION_PROPERTY, 0.0f));
                    }
                }
                if (viewComponents.getGiftsView() != null) {
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getGiftsView(), View.ALPHA, 0.0f));
                }
                if (viewComponents.getTimeItem().getTag() != null) {
                    viewComponents.getTimeItem().setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getTimeItem(), View.ALPHA, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getTimeItem(), View.SCALE_X, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getTimeItem(), View.SCALE_Y, 0.0f, 1.0f));
                }
                if (viewComponents.getStarFgItem().getTag() != null) {
                    viewComponents.getStarFgItem().setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarFgItem(), View.ALPHA, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarFgItem(), View.SCALE_X, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarFgItem(), View.SCALE_Y, 0.0f, 1.0f));
                }
                if (viewComponents.getStarBgItem().getTag() != null) {
                    viewComponents.getStarBgItem().setAlpha(0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarBgItem(), View.ALPHA, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarBgItem(), View.SCALE_X, 0.0f, 1.0f));
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getStarBgItem(), View.SCALE_Y, 0.0f, 1.0f));
                }
                if (viewComponents.getAnimatingItem() != null) {
                    viewComponents.getAnimatingItem().setAlpha(0.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getAnimatingItem(), View.ALPHA, 1.0f));
                }
                if (rowsAndStatusComponents.isCallItemVisible() && (attributesComponents.getChatId() != 0 || fromChat)) {
                    viewComponents.getCallItem().setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getCallItem(), View.ALPHA, 0.0f));
                }
                if (rowsAndStatusComponents.isVideoCallItemVisible()) {
                    viewComponents.getVideoCallItem().setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getVideoCallItem(), View.ALPHA, 0.0f));
                }
                if (rowsAndStatusComponents.isEditItemVisible()) {
                    viewComponents.getEditItem().setAlpha(1.0f);
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getEditItem(), View.ALPHA, 0.0f));
                }
                if (ttlIconView != null) {
                    animators.add(ObjectAnimator.ofFloat(ttlIconView, View.ALPHA, ttlIconView.getAlpha(), 0.0f));
                }
                if (viewComponents.getFloatingButtonContainer() != null) {
                    animators.add(ObjectAnimator.ofFloat(viewComponents.getFloatingButtonContainer(), View.ALPHA, 0.0f));
                }
                if (attributesComponents.getAvatarImage() != null) {
                    animators.add(ObjectAnimator.ofFloat(attributesComponents.getAvatarImage(), AvatarImageView.CROSSFADE_PROGRESS, 1.0f));
                }

                boolean crossfadeOnlineText = false;
                BaseFragment previousFragment = parentLayout.getFragmentStack().size() > 1 ? parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2) : null;
                if (previousFragment instanceof ChatActivity) {
                    ChatAvatarContainer avatarContainer = ((ChatActivity) previousFragment).getAvatarContainer();
                    View subtitleTextView = avatarContainer.getSubtitleTextView();
                    if (subtitleTextView instanceof SimpleTextView && ((SimpleTextView) subtitleTextView).getLeftDrawable() != null || avatarContainer.statusMadeShorter[0]) {
                        viewComponents.setTransitionOnlineText(avatarContainer.getSubtitleTextView());
                        viewComponents.getAvatarContainer2().invalidate();
                        crossfadeOnlineText = true;
                        animators.add(ObjectAnimator.ofFloat(attributesComponents.getOnlineTextView()[0], View.ALPHA, 0.0f));
                        animators.add(ObjectAnimator.ofFloat(attributesComponents.getOnlineTextView()[1], View.ALPHA, 0.0f));
                    }
                }
                if (!crossfadeOnlineText) {
                    for (int a = 0; a < 2; a++) {
                        animators.add(ObjectAnimator.ofFloat(attributesComponents.getOnlineTextView()[a], View.ALPHA, a == 0 ? 1.0f : 0.0f));
                    }
                }
                animatorSet.playTogether(animators);
                if (birthdayEffect != null) {
                    birthdayEffect.hide();
                }
            }
            profileTransitionInProgress = true;
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
                updateStoriesViewBounds(viewComponents,ProfileActivity.this, viewComponents,true);
            });
            animatorSet.playTogether(valueAnimator);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (fragmentView == null) {
                        callback.run();
                        return;
                    }
                    attributesComponents.getAvatarImage().setProgressToExpand(0);
                    listView.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (viewComponents.getAnimatingItem() != null) {
                        ActionBarMenu menu = actionBar.createMenu();
                        menu.clearItems();
                        viewComponents.setAnimatingItem(null);
                    }
                    callback.run();
                    if (rowsAndStatusComponents.getPlayProfileAnimation() == 2) {
                        rowsAndStatusComponents.setPlayProfileAnimation(1);
                        attributesComponents.getAvatarImage().setForegroundAlpha(1.0f);
                        viewComponents.getAvatarContainer().setVisibility(View.GONE);
                        viewComponents.getBlurImageMask().setVisibility(View.GONE);
                        attributesComponents.getAvatarsViewPager().resetCurrentItem();
                        attributesComponents.getAvatarsViewPager().setVisibility(View.VISIBLE);
                    }
                    viewComponents.setTransitionOnlineText(null);
                    viewComponents.getAvatarContainer2().invalidate();
                    profileTransitionInProgress = false;
                    attributesComponents.setPreviousTransitionFragment(null);
                    viewComponents.setPreviousTransitionMainFragment(null);
                    fragmentView.invalidate();
                }
            });
            animatorSet.setInterpolator(rowsAndStatusComponents.getPlayProfileAnimation() == 2 ? CubicBezierInterpolator.DEFAULT : new DecelerateInterpolator());

            AndroidUtilities.runOnUIThread(animatorSet::start, 50);
            return animatorSet;
        } else {
            attributesComponents.getNameTextView()[1].setTranslationX(AndroidUtilities.dp(18));
            attributesComponents.getOnlineTextView()[1].setTranslationX(AndroidUtilities.dp(16));
        }
        return null;
    }

    private int getAverageColor(ImageReceiver imageReceiver) {
        if (imageReceiver.getDrawable() instanceof VectorAvatarThumbDrawable) {
            return ((VectorAvatarThumbDrawable)imageReceiver.getDrawable()).gradientTools.getAverageColor();
        }
        return AndroidUtilities.calcBitmapColor(attributesComponents.getAvatarImage().getImageReceiver().getBitmap());
    }

    public void setChatInfo(TLRPC.ChatFull value) {
        if (viewComponents == null) {
            initProfileComponents();
        }
        attributesComponents.setChatInfo(value);
        if (attributesComponents.getChatInfo() != null && attributesComponents.getChatInfo().migrated_from_chat_id != 0 && mergeDialogId == 0) {
            mergeDialogId = -attributesComponents.getChatInfo().migrated_from_chat_id;
            getMediaDataController().getMediaCounts(mergeDialogId, attributesComponents.getTopicId(), classGuid);
        }
        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().setChatInfo(attributesComponents.getChatInfo());
        }
        if (attributesComponents.getAvatarsViewPager() != null && !rowsAndStatusComponents.isTopic()) {
            attributesComponents.getAvatarsViewPager().setChatInfo(attributesComponents.getChatInfo());
        }
        if (viewComponents.getStoryView() != null && attributesComponents.getChatInfo() != null) {
            viewComponents.getStoryView().setStories(attributesComponents.getChatInfo().stories);
        }
        if (viewComponents.getGiftsView() != null) {
            viewComponents.getGiftsView().update();
        }
        if (attributesComponents.getAvatarImage() != null) {
            attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
        }
        fetchUsersFromChannelInfo();
        if (attributesComponents.getChatId() != 0) {
            viewComponents.getOtherItem().setSubItemShown(ProfileParams.gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && attributesComponents.getChatInfo() != null && attributesComponents.getChatInfo().stargifts_available);
        }
    }


    public void setUserInfo(
            TLRPC.UserFull value,
            ProfileChannelCell.ChannelMessageFetcher channelMessageFetcher,
            ProfileBirthdayEffect.BirthdayEffectFetcher birthdayAssetsFetcher
    ) {

        if (viewComponents == null) {
            initProfileComponents();
        }
        attributesComponents.setUserInfo(value);
        if (viewComponents.getStoryView() != null) {
            viewComponents.getStoryView().setStories(attributesComponents.getUserInfo().stories);
        }
        if (viewComponents.getGiftsView() != null) {
            viewComponents.getGiftsView().update();
        }
        if (attributesComponents.getAvatarImage() != null) {
            attributesComponents.getAvatarImage().setHasStories(profileLayouts.needInsetForStories(ProfileActivity.this));
        }
        if (viewComponents.getSharedMediaLayout() != null) {
            viewComponents.getSharedMediaLayout().setUserInfo(attributesComponents.getUserInfo());
        }
        if (viewComponents.getProfileChannelMessageFetcher() == null) {
            viewComponents.initProfileChannelMessageFetcher(channelMessageFetcher);
        }
        if (viewComponents.getProfileChannelMessageFetcher() == null) {
            viewComponents.initProfileChannelMessageFetcher(new ProfileChannelCell.ChannelMessageFetcher(currentAccount));
        }
        viewComponents.getProfileChannelMessageFetcher().subscribe(() -> profileAdapter.updateListAnimated(false));
        viewComponents.getProfileChannelMessageFetcher().fetch(attributesComponents.getUserInfo());
        if (birthdayFetcher == null) {
            birthdayFetcher = birthdayAssetsFetcher;
        }
        if (birthdayFetcher == null) {
            birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, attributesComponents.getUserInfo(), birthdayFetcher);
            createdBirthdayFetcher = birthdayFetcher != null;
        }
        if (birthdayFetcher != null) {
            birthdayFetcher.subscribe(this::createBirthdayEffect);
        }
        if (viewComponents.getOtherItem() != null) {
            viewComponents.getOtherItem().setSubItemShown(ProfileParams.start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(attributesComponents.getUserId())));
            if (profileData.hasPrivacyCommand()) {
                viewComponents.getOtherItem().showSubItem(ProfileParams.bot_privacy);
            } else {
                viewComponents.getOtherItem().hideSubItem(ProfileParams.bot_privacy);
            }
        }
    }

    public boolean canSearchMembers() {
        return rowsAndStatusComponents.isCanSearchMembers();
    }

    private void fetchUsersFromChannelInfo() {
        if (attributesComponents.getCurrentChat() == null || !attributesComponents.getCurrentChat().megagroup) {
            return;
        }
        if (attributesComponents.getChatInfo() instanceof TLRPC.TL_channelFull && attributesComponents.getChatInfo().participants != null) {
            for (int a = 0; a < attributesComponents.getChatInfo().participants.participants.size(); a++) {
                TLRPC.ChatParticipant chatParticipant = attributesComponents.getChatInfo().participants.participants.get(a);
                participantsMap.put(chatParticipant.user_id, chatParticipant);
            }
        }
    }

    private void kickUser(long uid, TLRPC.ChatParticipant participant) {
        if (uid != 0) {
            TLRPC.User user = getMessagesController().getUser(uid);
            getMessagesController().deleteParticipantFromChat(attributesComponents.getChatId(), user);
            if (attributesComponents.getCurrentChat() != null && user != null && BulletinFactory.canShowBulletin(this)) {
                BulletinFactory.createRemoveFromChatBulletin(this, user, attributesComponents.getCurrentChat().title).show();
            }
            if (attributesComponents.getChatInfo().participants.participants.remove(participant)) {
                profileAdapter.updateListAnimated(true);
            }
        } else {
            getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
            if (AndroidUtilities.isTablet()) {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats, -attributesComponents.getChatId());
            } else {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            }
            getMessagesController().deleteParticipantFromChat(attributesComponents.getChatId(), getMessagesController().getUser(getUserConfig().getClientUserId()));
            rowsAndStatusComponents.setPlayProfileAnimation(0);
            finishFragment();
        }
    }

    public boolean isChat() {
        return attributesComponents.getChatId() != 0;
    }
    private int dontApplyPeerColor(int color) {
        return dontApplyPeerColor(color, true, null);
    }

    private int dontApplyPeerColor(int color, boolean actionBar) {
        return dontApplyPeerColor(color, actionBar, null);
    }

    private int dontApplyPeerColor(int color, boolean actionBar, Boolean online) {
        return color;
    }

    @Override
    public Theme.ResourcesProvider getResourceProvider() {
        return attributesComponents.getResourcesProvider();
    }

    public int getThemedColor(int key) {
        return Theme.getColor(key, attributesComponents.getResourcesProvider());
    }

    @Override
    public Drawable getThemedDrawable(String drawableKey) {
        Drawable drawable = attributesComponents.getResourcesProvider() != null ? attributesComponents.getResourcesProvider().getDrawable(drawableKey) : null;
        return drawable != null ? drawable : super.getThemedDrawable(drawableKey);
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        if (listView != null) {
            listView.invalidateViews();
        }
    }

    @Override
    public boolean didSelectDialogs(DialogsActivity fragment, ArrayList<MessagesStorage.TopicKey> dids, CharSequence message, boolean param, boolean notify, int scheduleDate, TopicsFragment topicsFragment) {
        long did = dids.get(0).dialogId;
        Bundle args = new Bundle();
        args.putBoolean("scrollToTopOnResume", true);
        if (DialogObject.isEncryptedDialog(did)) {
            args.putInt("enc_id", DialogObject.getEncryptedChatId(did));
        } else if (DialogObject.isUserDialog(did)) {
            args.putLong("user_id", did);
        } else if (DialogObject.isChatDialog(did)) {
            args.putLong("chat_id", -did);
        }
        if (!getMessagesController().checkCanOpenChat(args, fragment)) {
            return false;
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
        presentFragment(new ChatActivity(args), true);
        removeSelfFromStack();
        TLRPC.User user = getMessagesController().getUser(attributesComponents.getUserId());
        getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of(user, did, null, null, null, null, notify, scheduleDate));
        if (!TextUtils.isEmpty(message)) {
            AccountInstance accountInstance = AccountInstance.getInstance(currentAccount);
            SendMessagesHelper.prepareSendingText(accountInstance, message.toString(), did, notify, scheduleDate, 0);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (viewComponents.getImageUpdater() != null) {
            viewComponents.getImageUpdater().onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
        if (requestCode == 101 || requestCode == 102) {
            final TLRPC.User user = getMessagesController().getUser(attributesComponents.getUserId());
            if (user == null) {
                return;
            }
            boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
                if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (grantResults.length > 0 && allGranted) {
                VoIPHelper.startCall(user, requestCode == 102, attributesComponents.getUserInfo() != null && attributesComponents.getUserInfo().video_calls_available, getParentActivity(), attributesComponents.getUserInfo(), getAccountInstance());
            } else {
                VoIPHelper.permissionDenied(getParentActivity(), null, requestCode);
            }
        } else if (requestCode == 103) {
            if (attributesComponents.getCurrentChat() == null) {
                return;
            }
            boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
                if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (grantResults.length > 0 && allGranted) {
                ChatObject.Call call = getMessagesController().getGroupCall(attributesComponents.getChatId(), false);
                VoIPHelper.startCall(attributesComponents.getCurrentChat(), null, null, call == null, getParentActivity(), ProfileActivity.this, getAccountInstance());
            } else {
                VoIPHelper.permissionDenied(getParentActivity(), null, requestCode);
            }
        }
    }

    @Override
    public void dismissCurrentDialog() {
        if (viewComponents.getImageUpdater() != null && viewComponents.getImageUpdater().dismissCurrentDialog(visibleDialog)) {
            return;
        }
        super.dismissCurrentDialog();
    }

    @Override
    public boolean dismissDialogOnPause(Dialog dialog) {
        return (viewComponents.getImageUpdater() == null || viewComponents.getImageUpdater().dismissDialogOnPause(dialog)) && super.dismissDialogOnPause(dialog);
    }

    @Override
    public void onUploadProgressChanged(float progress) {
        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(progress);
        attributesComponents.getAvatarsViewPager().setUploadProgress(uploadingImageLocation, progress);
    }

    @Override
    public void didStartUpload(boolean fromAvatarConstructor, boolean isVideo) {
        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(0.0f);
    }

    int avatarUploadingRequest;
    @Override
    public void didUploadPhoto(final TLRPC.InputFile photo, final TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, final TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {
        AndroidUtilities.runOnUIThread(() -> {
            if (photo != null || video != null || emojiMarkup != null) {
                if (attributesComponents.getAvatar() == null) {
                    return;
                }
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                if (photo != null) {
                    req.file = photo;
                    req.flags |= 1;
                }
                if (video != null) {
                    req.video = video;
                    req.flags |= 2;
                    req.video_start_ts = videoStartTimestamp;
                    req.flags |= 4;
                }
                if (emojiMarkup != null) {
                    req.video_emoji_markup = emojiMarkup;
                    req.flags |= 16;
                }
                avatarUploadingRequest = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
                        if (user == null) {
                            user = getUserConfig().getCurrentUser();
                            if (user == null) {
                                return;
                            }
                            getMessagesController().putUser(user, false);
                        } else {
                            getUserConfig().setCurrentUser(user);
                        }

                        TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                        ArrayList<TLRPC.PhotoSize> sizes = photos_photo.photo.sizes;
                        TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                        TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                        TLRPC.VideoSize videoSize = photos_photo.photo.video_sizes.isEmpty() ? null : FileLoader.getClosestVideoSizeWithSize(photos_photo.photo.video_sizes, 1000);
                        user.photo = new TLRPC.TL_userProfilePhoto();
                        user.photo.photo_id = photos_photo.photo.id;
                        if (small != null) {
                            user.photo.photo_small = small.location;
                        }
                        if (big != null) {
                            user.photo.photo_big = big.location;
                        }

                        if (small != null && attributesComponents.getAvatar() != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(small, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(attributesComponents.getAvatar(), true);
                            src.renameTo(destFile);
                            String oldKey = attributesComponents.getAvatar().volume_id + "_" + attributesComponents.getAvatar().local_id + "@50_50";
                            String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                            ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), false);
                        }

                        if (videoSize != null && videoPath != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(videoSize, "mp4", true);
                            File src = new File(videoPath);
                            src.renameTo(destFile);
                        } else if (big != null && attributesComponents.getAvatarBig() != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(big, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(attributesComponents.getAvatarBig(), true);
                            src.renameTo(destFile);
                        }
                        getMessagesController().getDialogPhotos(user.id).addPhotoAtStart(((TLRPC.TL_photos_photo) response).photo);
                        ArrayList<TLRPC.User> users = new ArrayList<>();
                        users.add(user);
                        getMessagesStorage().putUsersAndChats(users, null, false, true);
                        TLRPC.UserFull userFull = getMessagesController().getUserFull(attributesComponents.getUserId());
                        if (userFull != null) {
                            userFull.profile_photo = photos_photo.photo;
                            getMessagesStorage().updateUserInfo(userFull, false);
                        }
                    }

                    rowsAndStatusComponents.setAllowPullingDown(!AndroidUtilities.isTablet() && !rowsAndStatusComponents.isInLandscapeMode() && attributesComponents.getAvatarImage().getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled());
                    attributesComponents.setAvatar(null);
                    attributesComponents.setAvatarBig(null);
                    attributesComponents.getAvatarsViewPager().scrolledByUser = true;
                    attributesComponents.getAvatarsViewPager().removeUploadingImage(uploadingImageLocation);
                    attributesComponents.getAvatarsViewPager().setCreateThumbFromParent(false);
                    profileData.updateProfileData( true);
                    showAvatarProgress(false, true);
                    getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    getUserConfig().saveConfig(true);

                }));
            } else {
                attributesComponents.setAvatar(smallSize.location);
                attributesComponents.setAvatarBig(bigSize.location);
                attributesComponents.getAvatarImage().setImage(ImageLocation.getForLocal(attributesComponents.getAvatar()), "50_50", attributesComponents.getAvatarDrawable(), null);
                if (rowsAndStatusComponents.getSetAvatarRow() != -1) {
                    profileAdapter.updateRowsIds();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    profileLayouts.needLayout(true);
                }
                attributesComponents.getAvatarsViewPager().addUploadingImage(uploadingImageLocation = ImageLocation.getForLocal(attributesComponents.getAvatarBig()), ImageLocation.getForLocal(attributesComponents.getAvatar()));
                showAvatarProgress(true, false);
            }
            actionBar.createMenu().requestLayout();
        });
    }

    private void showAvatarProgress(boolean show, boolean animated) {
        HideAndVisibility.showAvatarProgress(show, animated, avatarProgressView);
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (viewComponents.getImageUpdater() != null) {
            viewComponents.getImageUpdater().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (viewComponents.getImageUpdater() != null && viewComponents.getImageUpdater().currentPicturePath != null) {
            args.putString("path", viewComponents.getImageUpdater().currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (viewComponents.getImageUpdater() != null) {
            viewComponents.getImageUpdater().currentPicturePath = args.getString("path");
        }
    }

    private void dimBehindView(View view, float value) {
        scrimView = view;
        dimBehindView(value);
    }

    private void dimBehindView(boolean enable) {
        dimBehindView(enable ? 0.2f : 0);
    }

    private AnimatorSet scrimAnimatorSet = null;

    private void dimBehindView(float value) {
        boolean enable = value > 0;
        fragmentView.invalidate();
        if (scrimAnimatorSet != null) {
            scrimAnimatorSet.cancel();
        }
        scrimAnimatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        ValueAnimator scrimPaintAlphaAnimator;
        if (enable) {
            animators.add(scrimPaintAlphaAnimator = ValueAnimator.ofFloat(0, value));
        } else {
            animators.add(scrimPaintAlphaAnimator = ValueAnimator.ofFloat(scrimPaint.getAlpha() / 255f, 0));
        }
        scrimPaintAlphaAnimator.addUpdateListener(a -> {
            scrimPaint.setAlpha((int) (255 * (float) a.getAnimatedValue()));
        });
        scrimAnimatorSet.playTogether(animators);
        scrimAnimatorSet.setDuration(enable ? 150 : 220);
        if (!enable) {
            scrimAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrimView = null;
                    fragmentView.invalidate();
                }
            });
        }
        scrimAnimatorSet.start();
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        if (attributesComponents.getResourcesProvider() != null) {
            return null;
        }
        ThemeDescription.ThemeDescriptionDelegate themeDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof UserCell) {
                        ((UserCell) child).update(0);
                    }
                }
            }
            if (!rowsAndStatusComponents.isPulledDown()) {
                if (attributesComponents.getOnlineTextView()[1] != null) {
                    final Object onlineTextViewTag = attributesComponents.getOnlineTextView()[1].getTag();
                    for (int i = 0; i < 2; i++) {
                        if (onlineTextViewTag instanceof Integer) {
                            attributesComponents.getOnlineTextView()[i + 1].setTextColor(profileColors.applyPeerColor(getThemedColor((Integer) onlineTextViewTag), true, attributesComponents.getIsOnline()[0]));
                        } else {
                            attributesComponents.getOnlineTextView()[i + 1].setTextColor(profileColors.applyPeerColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, true));
                        }
                    }
                }
                if (attributesComponents.getLockIconDrawable() != null) {
                    attributesComponents.getLockIconDrawable().setColorFilter(getThemedColor(Theme.key_chat_lockIcon), PorterDuff.Mode.MULTIPLY);
                }
                if (attributesComponents.getScamDrawable() != null) {
                    attributesComponents.getScamDrawable().setColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
                }
                if (attributesComponents.getNameTextView()[1] != null) {
                    attributesComponents.getNameTextView()[1].setTextColor(getThemedColor(Theme.key_profile_title));
                }
                if (actionBar != null) {
                    actionBar.setItemsColor(attributesComponents.getPeerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), false);
                    actionBar.setItemsBackgroundColor(attributesComponents.getPeerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                }
            }
            profileColors.updateEmojiStatusDrawableColor();
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        if (viewComponents.getSharedMediaLayout() != null) {
            arrayList.addAll(viewComponents.getSharedMediaLayout().getThemeDescriptions());
        }

        arrayList.add(new ThemeDescription(listView, 0, null, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), 0, null, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(listView, 0, null, null, null, null, Theme.key_windowBackgroundGray));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_actionBarDefaultIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_actionBarSelectorBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_chat_lockIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_subtitleInProfileBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundActionBarBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_profile_title));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_profile_status));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_subtitleInProfileBlue));

        if (attributesComponents.getMediaCounterTextView() != null) {
            arrayList.add(new ThemeDescription(attributesComponents.getMediaCounterTextView().getTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, themeDelegate, Theme.key_player_actionBarSubtitle));
            arrayList.add(new ThemeDescription(attributesComponents.getMediaCounterTextView().getNextTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, themeDelegate, Theme.key_player_actionBarSubtitle));
        }

        arrayList.add(new ThemeDescription(viewComponents.getTopView(), ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        arrayList.add(new ThemeDescription(attributesComponents.getAvatarImage(), 0, null, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(attributesComponents.getAvatarImage(), 0, null, null, new Drawable[]{attributesComponents.getAvatarDrawable()}, null, Theme.key_avatar_backgroundInProfileBlue));

        arrayList.add(new ThemeDescription(attributesComponents.getWriteButton(), ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_profile_actionIcon));
        arrayList.add(new ThemeDescription(attributesComponents.getWriteButton(), ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_profile_actionBackground));
        arrayList.add(new ThemeDescription(attributesComponents.getWriteButton(), ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_profile_actionPressedBackground));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGreenText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_text_RedRegular));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueButton));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueIcon));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"detailTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{SettingsSuggestionCell.class}, new String[]{"detailTextView"}, null, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_buttonText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_addButton));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_buttonText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_addButton));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{UserCell.class}, new String[]{"adminTextView"}, null, null, null, Theme.key_profile_creatorIcon));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, themeDelegate, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, themeDelegate, Theme.key_windowBackgroundWhiteBlueText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundRed));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundOrange));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundViolet));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundGreen));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundCyan));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundPink));

        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_undo_background));
        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, null, null, null, Theme.key_undo_cancelColor));
        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, null, null, null, Theme.key_undo_cancelColor));
        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(viewComponents.getUndoView(), ThemeDescription.FLAG_IMAGECOLOR, new Class[]{UndoView.class}, new String[]{"leftImageView"}, null, null, null, Theme.key_undo_infoColor));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{AboutLinkCell.class}, Theme.linkSelectionPaint, null, null, Theme.key_windowBackgroundWhiteLinkSelection));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText));
        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection));

        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), 0, new Class[]{SettingsSearchCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), 0, new Class[]{SettingsSearchCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(viewComponents.getSearchListView(), 0, new Class[]{SettingsSearchCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));

        if (rowsAndStatusComponents.isMediaHeaderVisible()) {
            arrayList.add(new ThemeDescription(attributesComponents.getNameTextView()[1], 0, null, null, attributesComponents.getVerifiedCheckDrawable(), null, Theme.key_player_actionBarTitle));
            arrayList.add(new ThemeDescription(attributesComponents.getNameTextView()[1], 0, null, null, attributesComponents.getVerifiedDrawable(), null, Theme.key_windowBackgroundWhite));
        } else {
            arrayList.add(new ThemeDescription(attributesComponents.getNameTextView()[1], 0, null, null, attributesComponents.getVerifiedCheckDrawable(), null, Theme.key_profile_verifiedCheck));
            arrayList.add(new ThemeDescription(attributesComponents.getNameTextView()[1], 0, null, null, attributesComponents.getVerifiedDrawable(), null, Theme.key_profile_verifiedBackground));
        }

        return arrayList;
    }

    public void scrollToSharedMedia() {
        scrollToSharedMedia(false);
    }
    public void scrollToSharedMedia(boolean animated) {
        if (rowsAndStatusComponents.getSharedMediaRow() >= 0) {
            if (animated) {
                LinearSmoothScrollerCustom linearSmoothScroller = new LinearSmoothScrollerCustom(getContext(), LinearSmoothScrollerCustom.POSITION_TOP, .6f);
                linearSmoothScroller.setTargetPosition(rowsAndStatusComponents.getSharedMediaRow());
                linearSmoothScroller.setOffset(-listView.getPaddingTop());
                viewComponents.getLayoutManager().startSmoothScroll(linearSmoothScroller);
            } else {
                viewComponents.getLayoutManager().scrollToPositionWithOffset(rowsAndStatusComponents.getSharedMediaRow(), -listView.getPaddingTop());
            }
        }
    }

    public void onTextDetailCellImageClicked(View view) {
        View parent = (View) view.getParent();
        if (parent.getTag() != null && ((int) parent.getTag()) == rowsAndStatusComponents.getUsernameRow()) {
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesComponents.getChatId());
            args.putLong("user_id", attributesComponents.getUserId());
            presentFragment(new QrActivity(args));
        } else if (parent.getTag() != null && ((int) parent.getTag()) == rowsAndStatusComponents.getBirthdayRow()) {
            if (attributesComponents.getUserId() == getUserConfig().getClientUserId()) {
                presentFragment(new PremiumPreviewFragment("my_profile_gift"));
                return;
            }
            if (UserObject.areGiftsDisabled(attributesComponents.getUserInfo())) {
                BulletinFactory.of(this).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(attributesComponents.getUserId())))).show();
                return;
            }
            showDialog(new GiftSheet(getContext(), currentAccount, attributesComponents.getUserId(), null, null));
        }
    }

    private boolean fullyVisible;

    @Override
    public void onBecomeFullyVisible() {
        super.onBecomeFullyVisible();
        profileColors.writeButtonSetBackground(ProfileActivity.this);
        fullyVisible = true;
        createBirthdayEffect();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openAddToContact(TLRPC.User user, Bundle args) {
        openAndWrite.openAddToContact(listAdapter, args, user, attributesComponents.getPhotoDescriptionProgress(),
                attributesComponents.getCustomAvatarProgress(),  1f);
    }

    @Override
    public boolean isLightStatusBar() {
        int color;
        if (rowsAndStatusComponents.isPulledDown()) {
            return false;
        }
        if (actionBar.isActionModeShowed()) {
            color = getThemedColor(Theme.key_actionBarActionModeDefault);
        } else if (rowsAndStatusComponents.isMediaHeaderVisible()) {
            color = getThemedColor(Theme.key_windowBackgroundWhite);
        } else if (attributesComponents.getPeerColor() != null) {
            color = attributesComponents.getPeerColor().getBgColor2(Theme.isCurrentThemeDark());
        } else {
            color = getThemedColor(Theme.key_actionBarDefault);
        }
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    public String getLink(String username, int topicId) {
        String link = getMessagesController().linkPrefix + "/" + username;
        if (topicId != 0) {
            link += "/" + topicId;
        }
        return link;
    }

    @Override
    public void onTransitionAnimationProgress(boolean isOpen, float progress) {
        super.onTransitionAnimationProgress(isOpen, progress);
        if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            if (isOpen) {
                blurredView.setAlpha(1.0f - progress);
            } else {
                blurredView.setAlpha(progress);
            }
        }
    }

    public void prepareBlurBitmap() {
        if (blurredView == null) {
            return;
        }
        int w = (int) (fragmentView.getMeasuredWidth() / 6.0f);
        int h = (int) (fragmentView.getMeasuredHeight() / 6.0f);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1.0f / 6.0f, 1.0f / 6.0f);
        fragmentView.draw(canvas);
        Utilities.stackBlurBitmap(bitmap, Math.max(7, Math.max(w, h) / 180));
        blurredView.setBackground(new BitmapDrawable(bitmap));
        blurredView.setAlpha(0.0f);
        blurredView.setVisibility(View.VISIBLE);
    }

    private ProfileBirthdayEffect birthdayEffect;

    public TLRPC.UserFull getUserInfo() {
        return attributesComponents.getUserInfo();
    }

    public long getDialogId() {
        if (attributesComponents.getDialogId() != 0) {
            return attributesComponents.getDialogId();
        } else if (attributesComponents.getUserId() != 0) {
            return attributesComponents.getUserId();
        } else {
            return -attributesComponents.getChatId();
        }
    }
    private void createBirthdayEffect() {
        if (fragmentView == null || !fullyVisible || birthdayFetcher == null || getContext() == null)
            return;

        if (birthdayEffect != null) {
            birthdayEffect.updateFetcher(birthdayFetcher);
            birthdayEffect.invalidate();
            return;
        }

        birthdayEffect = new ProfileBirthdayEffect(this, birthdayFetcher);
        ((FrameLayout) fragmentView).addView(birthdayEffect, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL_HORIZONTAL | Gravity.TOP));
    }

    public boolean isMyProfile() {
        return attributesComponents.isMyProfile();
    }

    public boolean isSettings() {
        return viewComponents.getImageUpdater() != null && !isMyProfile();
    }

    public SharedMediaLayout getSharedMediaLayout() {
        if (viewComponents == null) initProfileComponents();

        return viewComponents.getSharedMediaLayout();
    }

    public ProfileGiftsView getGiftsView() {
        return viewComponents.getGiftsView();
    }
}