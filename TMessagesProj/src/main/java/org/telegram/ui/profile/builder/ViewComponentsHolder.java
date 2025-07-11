package org.telegram.ui.profile.builder;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.profile.builder.ProfileParams.VIRTUAL_TOP_BAR_HEIGHT;
import static org.telegram.ui.profile.utils.ColorsUtils.dontApplyPeerColor;
import static org.telegram.ui.profile.utils.Hints.updateCollectibleHint;
import static org.telegram.ui.profile.utils.Views.updateStoriesViewBounds;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FlagSecureReason;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_stories;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.AutoDeletePopupWrapper;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CanvasButton;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.StickerEmptyView;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.ProfileGiftsView;
import org.telegram.ui.Stories.ProfileStoriesView;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.StoryViewer;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.bots.BotBiometry;
import org.telegram.ui.bots.BotLocation;
import org.telegram.ui.profile.adapter.SearchAdapter;
import org.telegram.ui.profile.drawable.ShowDrawable;
import org.telegram.ui.profile.layout.NestedFrameLayout;
import org.telegram.ui.profile.view.BlurImageView;
import org.telegram.ui.profile.view.OverlaysView;
import org.telegram.ui.profile.view.PagerIndicatorView;
import org.telegram.ui.profile.view.TopView;
import org.telegram.ui.profile.view.button.ButtonGroupView;

import java.util.ArrayList;


public class ViewComponentsHolder {
    private final ProfileActivity profileActivity;
    private ImageView starBgItem, starFgItem;
    private ProfileGiftsView giftsView;
    private FlagSecureReason flagSecureReason;
    private SharedMediaLayout sharedMediaLayout;
    private ComponentsFactory components;
    private ActionBarMenuItem qrItem;
    private ActionBarMenuItem searchItem;
    private ActionBarMenuItem videoCallItem;
    private ActionBarMenuItem callItem;
    private ActionBarMenuItem editItem;
    private ActionBarMenuItem otherItem;
    private ActionBarMenuItem animatingItem;
    private ActionBarMenuSubItem autoDeleteItem;
    private ActionBarMenuSubItem editColorItem;
    private ActionBarMenuSubItem setUsernameItem;
    private ActionBarMenuSubItem linkItem;
    private FrameLayout avatarContainer;
    private FrameLayout avatarContainer2;
    private View transitionOnlineText;
    private ImageReceiver fallbackImage;
    private RecyclerListView searchListView;
    private FrameLayout floatingButtonContainer;

    //
    private FrameLayout bottomButtonsContainer;
    private FrameLayout[] bottomButtonContainer;
    private SpannableStringBuilder bottomButtonPostText;
    private ButtonWithCounterView[] bottomButton;
    private ImageView timeItem;
    private UndoView undoView;
    private StickerEmptyView emptyView;
    private ProfileStoriesView storyView;
    private ProfileChannelCell.ChannelMessageFetcher profileChannelMessageFetcher;
    private ShowDrawable showStatusButton;
    private PagerIndicatorView avatarsViewPagerIndicatorView;
    private TimerDrawable timerDrawable;
    private AboutLinkCell aboutLinkCell;
    private BaseFragment previousTransitionMainFragment;
    private AutoDeletePopupWrapper autoDeletePopupWrapper;
    private TimerDrawable autoDeleteItemDrawable;
    private BotLocation botLocation;
    private BotBiometry botBiometry;
    private TextCell setAvatarCell;
    private LinearLayoutManager layoutManager;
    private NestedFrameLayout contentView;
    private OverlaysView overlaysView;
    private TopView topView;
    private ImageUpdater imageUpdater;
    private BlurImageView blurImageMask;
    private ButtonGroupView buttonsGroup;
    //private SearchAdapter searchAdapter;

    public ViewComponentsHolder(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }


    public ProfileActivity getProfileActivity() {
        return profileActivity;
    }

    public ImageView getStarBgItem() {
        return starBgItem;
    }

    public ImageView getStarFgItem() {
        return starFgItem;
    }

    public void initStarBgItem() {
        starBgItem = new ImageView(this.getProfileActivity().getContext());
        starBgItem.setImageResource(R.drawable.star_small_outline);
        starBgItem.setColorFilter(new PorterDuffColorFilter(this.getProfileActivity().getThemedColor(Theme.key_actionBarDefault), PorterDuff.Mode.SRC_IN));
        starBgItem.setAlpha(0.0f);
        starBgItem.setScaleY(0.0f);
        starBgItem.setScaleX(0.0f);
    }

    public void initStarFgItem() {
        starFgItem = new ImageView(this.getProfileActivity().getContext());
        starFgItem.setImageResource(R.drawable.star_small_inner);
        starFgItem.setAlpha(0.0f);
        starFgItem.setScaleY(0.0f);
        starFgItem.setScaleX(0.0f);
    }

    public ProfileGiftsView getGiftsView() {
        return giftsView;
    }

    public void initGiftsView(AttributesComponentsHolder attributesHolder) {
        giftsView = new ProfileGiftsView(this.profileActivity.getContext(), UserConfig.selectedAccount, profileActivity.getDialogId(), avatarContainer, attributesHolder.getAvatarImage(), attributesHolder.getResourcesProvider());
    }

    public FlagSecureReason getFlagSecureReason() {
        return flagSecureReason;
    }

    public void initFlagSecureReason(INavigationLayout layout) {
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        flagSecureReason = new FlagSecureReason(layout.getParentActivity().getWindow(), () -> attributesHolder.getCurrentEncryptedChat() != null || this.getProfileActivity().getMessagesController().isChatNoForwards(components.getAttributesComponentsHolder().getCurrentChat()));
    }

    public void cleanFlagSecureReason() {
        flagSecureReason = null;
    }

    public SharedMediaLayout getSharedMediaLayout() {
        return sharedMediaLayout;
    }

    public void initSharedMediaLayout(AttributesComponentsHolder attributesHolder) {
        AttributesComponentsHolder attributes = components.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        final ArrayList<Integer> users = attributes.getChatInfo() != null && attributes.getChatInfo().participants != null && attributes.getChatInfo().participants.participants.size() > 5 ? attributes.getSortedUsers() : null;
        int initialTab = -1;
        if (attributes.isOpenCommonChats()) {
            initialTab = SharedMediaLayout.TAB_COMMON_GROUPS;
        } else if (attributes.isOpenGifts() && (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().stargifts_count > 0 || attributes.getChatInfo() != null && attributes.getChatInfo().stargifts_count > 0)) {
            initialTab = SharedMediaLayout.TAB_GIFTS;
            attributes.setOpenedGifts(true);
        } else if (attributes.isOpenSimilar()) {
            initialTab = SharedMediaLayout.TAB_RECOMMENDED_CHANNELS;
        } else if (users != null) {
            initialTab = SharedMediaLayout.TAB_GROUPUSERS;
        }

        sharedMediaLayout = new SharedMediaLayout(this.getProfileActivity().getContext(), profileActivity.getDialogId(), attributesHolder.getSharedMediaPreloader(), attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().common_chats_count : 0, attributes.getSortedUsers(), attributes.getChatInfo(), attributesHolder.getUserInfo(), initialTab, profileActivity, profileActivity, SharedMediaLayout.VIEW_TYPE_PROFILE_ACTIVITY, attributesHolder.getResourcesProvider()) {
            @Override
            protected int processColor(int color) {
                return dontApplyPeerColor(color, false);
            }

            @Override
            protected void onSelectedTabChanged() {
                profileActivity.updateSelectedMediaTabText();
            }

            @Override
            protected boolean includeSavedDialogs() {
                return attributes.getDialogId() == getProfileActivity().getUserConfig().getClientUserId() && !profileActivity.saved;
            }

            @Override
            protected boolean isSelf() {
                return attributesHolder.isMyProfile();
            }

            @Override
            protected boolean isStoriesView() {
                return attributesHolder.isMyProfile();
            }

            @Override
            protected void onSearchStateChanged(boolean expanded) {
                AndroidUtilities.removeAdjustResize(getProfileActivity().getParentActivity(), getProfileActivity().getClassGuid());

                components.getListView().getClippedList().stopScroll();
                avatarContainer2.setPivotY(avatarContainer.getPivotY() + avatarContainer.getMeasuredHeight() / 2f);
                avatarContainer2.setPivotX(avatarContainer2.getMeasuredWidth() / 2f);
                AndroidUtilities.updateViewVisibilityAnimated(avatarContainer2, !expanded, 0.95f, true);

                callItem.setVisibility(expanded || !rowsHolder.isCallItemVisible() ? GONE : INVISIBLE);
                videoCallItem.setVisibility(expanded || !rowsHolder.isVideoCallItemVisible() ? GONE : INVISIBLE);
                editItem.setVisibility(expanded || !rowsHolder.isEditItemVisible() ? GONE : INVISIBLE);
                otherItem.setVisibility(expanded ? GONE : INVISIBLE);
                if (qrItem != null) {
                    qrItem.setVisibility(expanded ? GONE : INVISIBLE);
                }
                updateStoriesViewBounds(ViewComponentsHolder.this, getProfileActivity(), ViewComponentsHolder.this,false);
            }

            @Override
            protected boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
                return getProfileActivity().onMemberClick(participant, isLong, view);
            }

            @Override
            protected void drawBackgroundWithBlur(Canvas canvas, float y, Rect rectTmp2, Paint backgroundPaint) {
                contentView.drawBlurRect(canvas, components.getListView().getClippedList().getY() + getY() + y, rectTmp2, backgroundPaint, true);
            }

            @Override
            protected void invalidateBlur() {
                if (contentView != null) {
                    contentView.invalidateBlur();
                }
            }

            @Override
            protected int getInitialTab() {
                return TAB_STORIES;
            }

            @Override
            protected void showActionMode(boolean show) {
                super.showActionMode(show);
                if (attributesHolder.isMyProfile()) {
                    disableScroll(show);

                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
                    if (a < 0 || a > 1) return;
                    bottomButtonContainer[a]
                            .animate()
                            .translationY(show || a == 0 && MessagesController.getInstance(getProfileActivity().getCurrentAccount()).storiesEnabled() ? 0 : dp(72))
                            .setDuration(320)
                            .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                            .setUpdateListener(anm -> components.getLayouts().updateBottomButtonY())
                            .start();
                }
            }

            @Override
            protected void onTabProgress(float progress) {
                super.onTabProgress(progress);
                if (attributesHolder.isMyProfile()) {
                    int width = sharedMediaLayout == null ? AndroidUtilities.displaySize.x : sharedMediaLayout.getMeasuredWidth();
                    if (bottomButtonContainer[0] != null)
                        bottomButtonContainer[0].setTranslationX((SharedMediaLayout.TAB_STORIES - progress) * width);
                    if (bottomButtonContainer[1] != null)
                        bottomButtonContainer[1].setTranslationX((SharedMediaLayout.TAB_ARCHIVED_STORIES - progress) * width);
                    components.getLayouts().updateBottomButtonY();
                }
            }

            @Override
            protected void onActionModeSelectedUpdate(SparseArray<MessageObject> messageObjects) {
                super.onActionModeSelectedUpdate(messageObjects);
                if (attributesHolder.isMyProfile()) {
                    final int count = messageObjects.size();
                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
                    if (a < 0 || a > 1) return;
                    if (a == 0) {
                        bottomButton[a].setText(count > 0 || !MessagesController.getInstance(getProfileActivity().getCurrentAccount()).storiesEnabled() ? formatPluralString("ArchiveStories", count) : bottomButtonPostText, true);
                    }
                    bottomButton[a].setCount(count, true);
                }
            }

            @Override
            public void openStoryRecorder() {
                StoryRecorder.getInstance(getProfileActivity().getParentActivity(), getProfileActivity().getCurrentAccount())
                        .selectedPeerId(profileActivity.getDialogId())
                        .canChangePeer(false)
                        .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                            @Override
                            public void preLayout(long dialogId, Runnable runnable) {
                                attributesHolder.getAvatarImage().setHasStories(components.getLayouts().needInsetForStories(getProfileActivity()));
                                if (dialogId == profileActivity.getDialogId()) {
                                    profileActivity.collapseAvatarInstant();
                                }
                                AndroidUtilities.runOnUIThread(runnable, 30);
                            }

                            @Override
                            public StoryRecorder.SourceView getView(long dialogId) {
                                if (dialogId != profileActivity.getDialogId()) {
                                    return null;
                                }
                                profileActivity.updateAvatarRoundRadius();
                                return StoryRecorder.SourceView.fromAvatarImage(attributesHolder.getAvatarImage(), ChatObject.isForum(attributesHolder.getCurrentChat()));
                            }
                        })
                        .open(StoryRecorder.SourceView.fromFloatingButton(floatingButtonContainer), true);
            }

            @Override
            public void updateTabs(boolean animated) {
                super.updateTabs(animated);
                if (attributes.isOpenGifts() && !attributes.isOpenedGifts() && scrollSlidingTextTabStrip.hasTab(TAB_GIFTS)) {
                    attributes.setOpenedGifts(true);
                    scrollToPage(TAB_GIFTS);
                }
            }
        };

    }

    public ComponentsFactory getComponents() {
        return components;
    }

    public void setComponents(ComponentsFactory components) {
        this.components = components;
    }

    public ActionBarMenuItem getQrItem() {
        return qrItem;
    }

    public void initQrItem(ActionBarMenu menu) {
        this.qrItem = menu.addItem(ProfileParams.qr_button, R.drawable.msg_qr_mini, getProfileActivity().getResourceProvider());
    }

    public ActionBarMenuItem getSearchItem() {
        return searchItem;
    }

    public void initSearchItem(ActionBarMenu menu, SearchAdapter searchAdapter) {
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        searchItem =  menu.addItem(ProfileParams.search_button, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

            @Override
            public Animator getCustomToggleTransition() {
                rowsHolder.setSearchMode(!rowsHolder.isSearchMode());
                if (!rowsHolder.isSearchMode()) {
                    searchItem.clearFocusOnSearchView();
                }
                if (rowsHolder.isSearchMode()) {
                    searchItem.getSearchField().setText("");
                }
                return components.getAnimation().searchExpandTransition(getProfileActivity(), rowsHolder.isSearchMode());
            }

            @Override
            public void onTextChanged(EditText editText) {
                searchAdapter.search(editText.getText().toString().toLowerCase());
            }
        });
    }

    public ActionBarMenuItem getVideoCallItem() {
        return videoCallItem;
    }

    public void initVideoCallItem(ActionBarMenu menu) {
        this.videoCallItem = menu.addItem(ProfileParams.video_call_item, R.drawable.profile_video);
    }

    public ActionBarMenuItem getCallItem() {
        return callItem;
    }

    public void initCallItem(ActionBarMenu menu) {
        AttributesComponentsHolder attributes = components.getAttributesComponentsHolder();
        if (attributes.getChatId() != 0) {
            callItem = menu.addItem(ProfileParams.call_item, R.drawable.msg_voicechat2);
            if (ChatObject.isChannelOrGiga(components.getAttributesComponentsHolder().getCurrentChat())) {
                callItem.setContentDescription(LocaleController.getString(R.string.VoipChannelVoiceChat));
            } else {
                callItem.setContentDescription(LocaleController.getString(R.string.VoipGroupVoiceChat));
            }
        } else {
            callItem = menu.addItem(ProfileParams.call_item, R.drawable.ic_call);
            callItem.setContentDescription(LocaleController.getString(R.string.Call));
        }
    }

    public ActionBarMenuItem getEditItem() {
        return editItem;
    }

    public void initEditItem(ActionBarMenu menu) {
        if (components.getAttributesComponentsHolder().isMyProfile()) {
            editItem = menu.addItem(ProfileParams.edit_profile, R.drawable.group_edit_profile);
            editItem.setContentDescription(LocaleController.getString(R.string.Edit));
        } else {
            editItem = menu.addItem(ProfileParams.edit_channel, R.drawable.group_edit_profile);
            editItem.setContentDescription(LocaleController.getString(R.string.Edit));
        }
    }

    public ActionBarMenuItem getOtherItem() {
        return otherItem;
    }

    public void initOtherItem(ActionBarMenu menu, AttributesComponentsHolder attributesHolder) {
        otherItem = menu.addItem(10, R.drawable.ic_ab_other, attributesHolder.getResourcesProvider());
    }

    public FrameLayout getAvatarContainer() {
        return avatarContainer;
    }

    public void initAvatarContainer(Context context) {
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        ViewComponentsHolder viewHolder = components.getViewComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        avatarContainer = new FrameLayout(context) {
            private Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);{
                setWillNotDraw(false);
            }

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (attributesHolder.getPullUpProgress() == 1f) {
                    return;
                }
                if (rowsHolder.isTopic() || attributesHolder.getPullUpProgress() == 0f) {
                    super.draw(canvas);
                    return;
                }
                canvas.save();
                float v = Math.min(attributesHolder.getPullUpProgress(), 0.75f) / 0.75f;
                float sc = AndroidUtilities.lerp(1f, rowsHolder.getAvatarScale(), v);
                canvas.scale(sc, sc, getWidth() / 2f, getHeight() / 2f);
                super.draw(canvas);
                blackPaint.setAlpha((int) (Math.min(attributesHolder.getPullUpProgress(), 0.8f) / 0.8f * 0xFF));
                float inset = attributesHolder.getAvatarImage().hasStories() ? dp(3.5f) * (1f - attributesHolder.getPullUpProgress()) : 0f;
                canvas.drawRoundRect(inset, inset, getWidth() - inset, getHeight() - inset, attributesHolder.getAvatarImage().getRadius(), attributesHolder.getAvatarImage().getRadius(), blackPaint);
                canvas.restore();
            }

            @Override
            public void setScaleY(float scaleY) {
                super.setScaleY(scaleY);
                attributesHolder.getAvatarImage().invalidate();
            }
        };
    }
    public FrameLayout getAvatarContainer2() {
        return avatarContainer2;
    }

    public void initAvatarContainer2(Context context) {
        AttributesComponentsHolder attributes = components.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        avatarContainer2 = new FrameLayout(context) {
            CanvasButton canvasButton;

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (transitionOnlineText != null) {
                    canvas.save();
                    canvas.translate(attributes.getOnlineTextView()[0].getX(), attributes.getOnlineTextView()[0].getY());
                    canvas.saveLayerAlpha(0, 0, transitionOnlineText.getMeasuredWidth(), transitionOnlineText.getMeasuredHeight(), (int) (255 * (1f - components.getRowsAndStatusComponentsHolder().getAvatarAnimationProgress())), Canvas.ALL_SAVE_FLAG);
                    transitionOnlineText.draw(canvas);
                    canvas.restore();
                    canvas.restore();
                    invalidate();
                }
                if (rowsHolder.isHasFallbackPhoto() && attributes.getPhotoDescriptionProgress() != 0 && attributes.getCustomAvatarProgress() != 1f) {
                    float cy = attributes.getOnlineTextView()[1].getY() + attributes.getOnlineTextView()[1].getMeasuredHeight() / 2f;
                    float size = AndroidUtilities.dp(22);
                    float x = AndroidUtilities.dp(28) - components.getRowsAndStatusComponentsHolder().getCustomPhotoOffset() + attributes.getOnlineTextView()[1].getX() - size;

                    fallbackImage.setImageCoords(x, cy - size / 2f, size, size);
                    fallbackImage.setAlpha(attributes.getPhotoDescriptionProgress());
                    canvas.save();
                    float s = attributes.getPhotoDescriptionProgress();
                    canvas.scale(s, s, fallbackImage.getCenterX(), fallbackImage.getCenterY());
                    fallbackImage.draw(canvas);
                    canvas.restore();

                    if (attributes.getCustomAvatarProgress() == 0) {
                        if (canvasButton == null) {
                            canvasButton = new CanvasButton(this);
                            canvasButton.setDelegate(() -> {
                                if (attributes.getCustomAvatarProgress() != 1f) {
                                    attributes.getAvatarsViewPager().scrollToLastItem();
                                }
                            });
                        }
                        AndroidUtilities.rectTmp.set(x - AndroidUtilities.dp(4), cy - AndroidUtilities.dp(14), x + attributes.getOnlineTextView()[2].getTextWidth() + AndroidUtilities.dp(28) * (1f - attributes.getCustomAvatarProgress()) + AndroidUtilities.dp(4), cy + AndroidUtilities.dp(14));
                        canvasButton.setRect(AndroidUtilities.rectTmp);
                        canvasButton.setRounded(true);
                        canvasButton.setColor(Color.TRANSPARENT, ColorUtils.setAlphaComponent(Color.WHITE, 50));
                        canvasButton.draw(canvas);
                    } else {
                        if (canvasButton != null) {
                            canvasButton.cancelRipple();
                        }
                    }
                }
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return (canvasButton != null && canvasButton.checkTouchEvent(ev)) || super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return (canvasButton != null && canvasButton.checkTouchEvent(event)) || super.onTouchEvent(event);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                fallbackImage.onAttachedToWindow();
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                fallbackImage.onDetachedFromWindow();
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateCollectibleHint(components);
            }
        };
    }

    public View getTransitionOnlineText() {
        return transitionOnlineText;
    }

    public void setTransitionOnlineText(View transitionOnlineText) {
        this.transitionOnlineText = transitionOnlineText;
    }

    public ImageReceiver getFallbackImage() {
        return fallbackImage;
    }

    public void initFallbackImage() {
        fallbackImage = new ImageReceiver(getAvatarContainer2());
        fallbackImage.setRoundRadius(AndroidUtilities.dp(11));
    }

    public RecyclerListView getSearchListView() {
        return searchListView;
    }

    public void initSearchListView(Context context, SearchAdapter searchAdapter) {
        searchListView = new RecyclerListView(context);
        searchListView.setVerticalScrollBarEnabled(false);
        searchListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        searchListView.setGlowColor(profileActivity.getThemedColor(Theme.key_avatar_backgroundActionBarBlue));
        searchListView.setAdapter(searchAdapter);
        searchListView.setItemAnimator(null);
        searchListView.setVisibility(View.GONE);
        searchListView.setLayoutAnimation(null);
        searchListView.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
    }

    public void initBottomContainerButton(Context context, AttributesComponentsHolder attributesHolder) {
        bottomButtonsContainer = new FrameLayout(context);

        bottomButtonContainer = new FrameLayout[2];
        bottomButton = new ButtonWithCounterView[2];
        for (int a = 0; a < 2; ++a) {
            bottomButtonContainer[a] = new FrameLayout(context);
            bottomButtonContainer[a].setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));

            View shadow = new View(context);
            shadow.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundGray));
            bottomButtonContainer[a].addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1 / AndroidUtilities.density, Gravity.TOP | Gravity.FILL_HORIZONTAL));

            bottomButton[a] = new ButtonWithCounterView(context, attributesHolder.getResourcesProvider());
            if (a == 0) {
                bottomButtonPostText = new SpannableStringBuilder("c");
                bottomButtonPostText.setSpan(new ColoredImageSpan(R.drawable.filled_premium_camera), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                bottomButtonPostText.append("  ").append(getString(R.string.StoriesAddPost));
                bottomButton[a].setText(bottomButtonPostText, false);
            } else {
                bottomButton[a].setText(getString(R.string.StorySave), false);
            }
            final int finalA = a;
            bottomButton[a].setOnClickListener(v -> {
                if (finalA == 0 && !sharedMediaLayout.isActionModeShown()) {
                    if (!profileActivity.getMessagesController().storiesEnabled()) {
                        profileActivity.showDialog(new PremiumFeatureBottomSheet(profileActivity, PremiumPreviewFragment.PREMIUM_FEATURE_STORIES, true));
                        return;
                    }
                    profileActivity.getMessagesController().getMainSettings().edit().putBoolean("story_keep", true).apply();
                    StoryRecorder.getInstance(profileActivity.getParentActivity(), profileActivity.getCurrentAccount())
                            .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                                @Override
                                public void preLayout(long dialogId, Runnable runnable) {
                                    attributesHolder.getAvatarImage().setHasStories(components.getLayouts().needInsetForStories(profileActivity));
                                    if (dialogId == profileActivity.getDialogId()) {
                                        profileActivity.collapseAvatarInstant();
                                    }
                                    AndroidUtilities.runOnUIThread(runnable, 30);
                                }

                                @Override
                                public StoryRecorder.SourceView getView(long dialogId) {
                                    if (dialogId != profileActivity.getDialogId()) {
                                        return null;
                                    }
                                    profileActivity.updateAvatarRoundRadius();
                                    return StoryRecorder.SourceView.fromAvatarImage(attributesHolder.getAvatarImage(), ChatObject.isForum(attributesHolder.getCurrentChat()));
                                }
                            })
                            .open(null);
                } else {
                    final long dialogId = profileActivity.getUserConfig().getClientUserId();
                    if (profileActivity.applyBulletin != null) {
                        profileActivity.applyBulletin.run();
                        profileActivity.applyBulletin = null;
                    }
                    Bulletin.hideVisible();
                    boolean pin = sharedMediaLayout.getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES;
                    int count = 0;
                    ArrayList<TL_stories.StoryItem> storyItems = new ArrayList<>();
                    SparseArray<MessageObject> actionModeMessageObjects = sharedMediaLayout.getActionModeSelected();
                    if (actionModeMessageObjects != null) {
                        for (int i = 0; i < actionModeMessageObjects.size(); ++i) {
                            MessageObject messageObject = actionModeMessageObjects.valueAt(i);
                            if (messageObject.storyItem != null) {
                                storyItems.add(messageObject.storyItem);
                                count++;
                            }
                        }
                    }
                    sharedMediaLayout.closeActionMode(false);
                    if (pin) {
                        sharedMediaLayout.scrollToPage(SharedMediaLayout.TAB_STORIES);
                    }
                    if (storyItems.isEmpty()) {
                        return;
                    }
                    boolean[] pastValues = new boolean[storyItems.size()];
                    for (int i = 0; i < storyItems.size(); ++i) {
                        TL_stories.StoryItem storyItem = storyItems.get(i);
                        pastValues[i] = storyItem.pinned;
                        storyItem.pinned = pin;
                    }
                    profileActivity.getMessagesController().getStoriesController().updateStoriesInLists(dialogId, storyItems);
                    final boolean[] undone = new boolean[]{false};
                    profileActivity.applyBulletin = () -> {
                        profileActivity.getMessagesController().getStoriesController().updateStoriesPinned(dialogId, storyItems, pin, null);
                    };
                    final Runnable undo = () -> {
                        undone[0] = true;
                        AndroidUtilities.cancelRunOnUIThread(profileActivity.applyBulletin);
                        for (int i = 0; i < storyItems.size(); ++i) {
                            TL_stories.StoryItem storyItem = storyItems.get(i);
                            storyItem.pinned = pastValues[i];
                        }
                        profileActivity.getMessagesController().getStoriesController().updateStoriesInLists(dialogId, storyItems);
                    };
                    Bulletin bulletin;
                    if (pin) {
                        bulletin = BulletinFactory.of(profileActivity).createSimpleBulletin(R.raw.contact_check, LocaleController.formatPluralString("StorySavedTitle", count), LocaleController.getString(R.string.StorySavedSubtitle), LocaleController.getString(R.string.Undo), undo).show();
                    } else {
                        bulletin = BulletinFactory.of(profileActivity).createSimpleBulletin(R.raw.chats_archived, LocaleController.formatPluralString("StoryArchived", count), LocaleController.getString(R.string.Undo), Bulletin.DURATION_PROLONG, undo).show();
                    }
                    bulletin.setOnHideListener(() -> {
                        if (!undone[0] && profileActivity.applyBulletin != null) {
                            profileActivity.applyBulletin.run();
                        }
                        profileActivity.applyBulletin = null;
                    });
                }
            });
            bottomButtonContainer[a].addView(bottomButton[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 12, 12, 12, 12));

            bottomButtonsContainer.addView(bottomButtonContainer[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
            if (a == 1 || !profileActivity.getMessagesController().storiesEnabled()) {
                bottomButtonContainer[a].setTranslationY(dp(72));
            }
        }
    }


    public FrameLayout getBottomButtonsContainer() {
        return bottomButtonsContainer;
    }
    public FrameLayout[] getBottomButtonContainer() {
        return bottomButtonContainer;
    }
    public SpannableStringBuilder getBottomButtonPostText() {
        return bottomButtonPostText;
    }
    public ButtonWithCounterView[] getBottomButton() {
        return bottomButton;
    }

    public FrameLayout getFloatingButtonContainer() {
        return floatingButtonContainer;
    }

    public void initFloatingButtonContainer(Context context) {
        floatingButtonContainer = new FrameLayout(context);
        floatingButtonContainer.setVisibility(View.VISIBLE);
    }

    public ImageView getTimeItem() {
        return timeItem;
    }

    public void initTimeItem(Context context) {
        timeItem = new ImageView(context);
        timeItem.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(5), AndroidUtilities.dp(5));
        timeItem.setScaleType(ImageView.ScaleType.CENTER);
        timeItem.setAlpha(0.0f);
        timeItem.setImageDrawable(timerDrawable = new TimerDrawable(context, null));
        timeItem.setTranslationY(-1);
    }

    public UndoView getUndoView() {
        return undoView;
    }

    public void initUndoView(Context context, AttributesComponentsHolder attributesHolder) {
        undoView = new UndoView(context, null, false, attributesHolder.getResourcesProvider());
    }

    public StickerEmptyView getEmptyView() {
        return emptyView;
    }

    public void initEmptyView(Context context) {
        emptyView = new StickerEmptyView(context, null, 1);
        emptyView.setAnimateLayoutChange(true);
        emptyView.subtitle.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    public ProfileStoriesView getStoryView() {
        return storyView;
    }

    public void initStoryView(Context context, AttributesComponentsHolder attributesHolder) {
        RowsAndStatusComponentsHolder rowsHodler = components.getRowsAndStatusComponentsHolder();
        storyView = new ProfileStoriesView(context, profileActivity.getCurrentAccount(), profileActivity.getDialogId(), rowsHodler.isTopic(), avatarContainer, attributesHolder.getAvatarImage(), attributesHolder.getResourcesProvider()) {
            @Override
            protected void onTap(StoryViewer.PlaceProvider provider) {
                long did = profileActivity.getDialogId();
                StoriesController storiesController = profileActivity.getMessagesController().getStoriesController();
                if (storiesController.hasStories(did) || storiesController.hasUploadingStories(did) || storiesController.isLastUploadingFailed(did)) {
                    profileActivity.getOrCreateStoryViewer().open(context, did, provider);
                } else if (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().stories != null && !attributesHolder.getUserInfo().stories.stories.isEmpty() && attributesHolder.getUserId() != profileActivity.getUserConfig().clientUserId) {
                    profileActivity.getOrCreateStoryViewer().open(context, attributesHolder.getUserInfo().stories, provider);
                } else if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().stories != null && !attributesHolder.getChatInfo().stories.stories.isEmpty()) {
                    profileActivity.getOrCreateStoryViewer().open(context, attributesHolder.getChatInfo().stories, provider);
                } else {
                    profileActivity.expandAvatar();
                }
            }

            @Override
            protected void onLongPress() {
                components.getOpenAndWrite().openAvatar();
            }
        };
    }

    public ActionBarMenuItem getAnimatingItem() {
        return animatingItem;
    }

    public void setAnimatingItem(ActionBarMenuItem animatingItem) {
        this.animatingItem = animatingItem;
    }

    public ProfileChannelCell.ChannelMessageFetcher getProfileChannelMessageFetcher() {
        return profileChannelMessageFetcher;
    }

    public void initProfileChannelMessageFetcher(ProfileChannelCell.ChannelMessageFetcher profileChannelMessageFetcher) {
        this.profileChannelMessageFetcher = profileChannelMessageFetcher;
    }

    public ShowDrawable getShowStatusButton() {
        return showStatusButton;
    }

    public void setShowStatusButton(ShowDrawable showStatusButton) {
        this.showStatusButton = showStatusButton;
    }

    public PagerIndicatorView getAvatarsViewPagerIndicatorView() {
        return avatarsViewPagerIndicatorView;
    }

    public void setAvatarsViewPagerIndicatorView(PagerIndicatorView avatarsViewPagerIndicatorView) {
        this.avatarsViewPagerIndicatorView = avatarsViewPagerIndicatorView;
    }

    public TimerDrawable getTimerDrawable() {
        return timerDrawable;
    }

    public void setTimerDrawable(TimerDrawable timerDrawable) {
        this.timerDrawable = timerDrawable;
    }

    public AboutLinkCell getAboutLinkCell() {
        return aboutLinkCell;
    }

    public void setAboutLinkCell(AboutLinkCell aboutLinkCell) {
        this.aboutLinkCell = aboutLinkCell;
    }

    public BaseFragment getPreviousTransitionMainFragment() {
        return previousTransitionMainFragment;
    }

    public void setPreviousTransitionMainFragment(BaseFragment previousTransitionMainFragment) {
        this.previousTransitionMainFragment = previousTransitionMainFragment;
    }

    public AutoDeletePopupWrapper getAutoDeletePopupWrapper() {
        return autoDeletePopupWrapper;
    }

    public void setAutoDeletePopupWrapper(AutoDeletePopupWrapper autoDeletePopupWrapper) {
        this.autoDeletePopupWrapper = autoDeletePopupWrapper;
    }

    public ActionBarMenuSubItem getAutoDeleteItem() {
        return autoDeleteItem;
    }

    public void setAutoDeleteItem(ActionBarMenuSubItem autoDeleteItem) {
        this.autoDeleteItem = autoDeleteItem;
    }

    public TimerDrawable getAutoDeleteItemDrawable() {
        return autoDeleteItemDrawable;
    }

    public void setAutoDeleteItemDrawable(TimerDrawable autoDeleteItemDrawable) {
        this.autoDeleteItemDrawable = autoDeleteItemDrawable;
    }

    public ActionBarMenuSubItem getEditColorItem() {
        return editColorItem;
    }

    public void setEditColorItem(ActionBarMenuSubItem editColorItem) {
        this.editColorItem = editColorItem;
    }

    public ActionBarMenuSubItem getSetUsernameItem() {
        return setUsernameItem;
    }

    public void setSetUsernameItem(ActionBarMenuSubItem setUsernameItem) {
        this.setUsernameItem = setUsernameItem;
    }

    public ActionBarMenuSubItem getLinkItem() {
        return linkItem;
    }

    public void setLinkItem(ActionBarMenuSubItem linkItem) {
        this.linkItem = linkItem;
    }

    public BotLocation getBotLocation() {
        return botLocation;
    }

    public void setBotLocation(BotLocation botLocation) {
        this.botLocation = botLocation;
    }

    public BotBiometry getBotBiometry() {
        return botBiometry;
    }

    public void setBotBiometry(BotBiometry botBiometry) {
        this.botBiometry = botBiometry;
    }

    public TextCell getSetAvatarCell() {
        return setAvatarCell;
    }

    public void setSetAvatarCell(TextCell setAvatarCell) {
        this.setAvatarCell = setAvatarCell;
    }

    public LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void initLayoutManager() {
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        layoutManager = new LinearLayoutManager(profileActivity.getContext()) {

            @Override
            public boolean supportsPredictiveItemAnimations() {
                return imageUpdater != null;
            }

            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                final View view = layoutManager.findViewByPosition(0);
                if (view != null && !rowsHolder.isOpeningAvatar()) {
                    final int canScroll = view.getTop() - AndroidUtilities.dp(VIRTUAL_TOP_BAR_HEIGHT);
                    if (!rowsHolder.isAllowPullingDown() && canScroll > dy) {
                        dy = canScroll;
                        if (attributesHolder.getAvatarsViewPager().hasImages() && attributesHolder.getAvatarImage().getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled() && !rowsHolder.isInLandscapeMode() && !AndroidUtilities.isTablet()) {
                            rowsHolder.setAllowPullingDown(attributesHolder.getAvatarBig() == null);
                        }
                    } else if (rowsHolder.isAllowPullingDown()) {
                        if (dy >= canScroll) {
                            dy = canScroll;
                            rowsHolder.setAllowPullingDown(false);
                        } else if (components.getListView().getClippedList().getScrollState() == RecyclerListView.SCROLL_STATE_DRAGGING) {
                            if (!rowsHolder.isPulledDown()) {
                                dy /= 2;
                            }
                        }
                    }
                }
                return super.scrollVerticallyBy(dy, recycler, state);
            }
        };
    }

    public NestedFrameLayout getContentView() {
        return contentView;
    }

    public void setContentView(NestedFrameLayout contentView) {
        this.contentView = contentView;
    }

    public OverlaysView getOverlaysView() {
        return overlaysView;
    }

    public void setOverlaysView(OverlaysView overlaysView) {
        this.overlaysView = overlaysView;
    }

    public TopView getTopView() {
        return topView;
    }

    public void setTopView(TopView topView) {
        this.topView = topView;
    }

    public ImageUpdater getImageUpdater() {
        return imageUpdater;
    }

    public void setImageUpdater(ImageUpdater imageUpdater) {
        this.imageUpdater = imageUpdater;
    }

    public BlurImageView getBlurImageMask() {
        return blurImageMask;
    }

    public void setBlurImageMask(BlurImageView blurImageMask) {
        this.blurImageMask = blurImageMask;
    }

    public ButtonGroupView getButtonsGroup() {
        return buttonsGroup;
    }

    public void initButtonsGroup() {
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        buttonsGroup = new ButtonGroupView(profileActivity.getContext(), avatarContainer, attributesHolder.getAvatarsViewPager()) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                float f = attributesHolder.getGroupViewAnimated().set(topView != null && topView.hasColorById || rowsHolder.isPulledDown());
                setDarkProgress(f);
                super.onDraw(canvas);
                if (attributesHolder.getGroupViewAnimated().isInProgress()) invalidate();
            }
        };
        buttonsGroup.setPadding(dp(12), 0, dp(12), 0);
        buttonsGroup.setButtonClickListener(components.getClicksAndPress()::onProfileButtonClicked);
    }

//    public void setSearchAdapter(SearchAdapter searchAdapter) {
//        this.searchAdapter = searchAdapter;
//    }
}