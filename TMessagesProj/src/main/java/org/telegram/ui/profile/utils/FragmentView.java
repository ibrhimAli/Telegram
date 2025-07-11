package org.telegram.ui.profile.utils;

import static org.telegram.ui.profile.utils.Hints.updateCollectibleHint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.PinchToZoomHelper;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.adapter.ListAdapter;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.layout.NestedFrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class FragmentView {

    private int lastMeasuredContentHeight;
    private final ProfileActivity profileActivity;
    private final Animation animation;
    private final RecyclerListView listView;
    private final ComponentsFactory components;
    private final ViewComponentsHolder viewsHolder;
    private final RowsAndStatusComponentsHolder rowsHolder;
    private AttributesComponentsHolder attributesHolder;

    public FragmentView(ComponentsFactory components) {
        profileActivity = components.getProfileActivity();
        animation = components.getAnimation();
        listView = components.getListView().getClippedList();
        viewsHolder = components.getViewComponentsHolder();
        this.components = components;
        rowsHolder = components.getRowsAndStatusComponentsHolder();
        attributesHolder = components.getAttributesComponentsHolder();
    }
    public View getFragmentView(ListAdapter listAdapter,
                                             PinchToZoomHelper pinchToZoomHelper, HashMap<Integer, Integer> positionToOffset,
                                             Paint whitePaint, Paint scrimPaint,
                                             View blurredView, View scrimView, Paint actionBarBackgroundPaint, boolean profileTransitionInProgress,
                                             boolean openSimilar, long banFromGroup) {
        // ProfileParams.qrItem.setTranslationX(translation);

        return new NestedFrameLayout(components) {

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (pinchToZoomHelper.isInOverlayMode()) {
                    return pinchToZoomHelper.onTouchEvent(ev);
                }
                if (viewsHolder.getSharedMediaLayout() != null && viewsHolder.getSharedMediaLayout().isInFastScroll() && viewsHolder.getSharedMediaLayout().isPinnedToTop()) {
                    return viewsHolder.getSharedMediaLayout().dispatchFastScrollEvent(ev);
                }
                if (viewsHolder.getSharedMediaLayout() != null && viewsHolder.getSharedMediaLayout().checkPinchToZoom(ev)) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            private boolean ignoreLayout;
            private final Paint grayPaint = new Paint();

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            private boolean wasPortrait;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                if (listView != null) {
                    LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
                    if (layoutParams.topMargin != actionBarHeight) {
                        layoutParams.topMargin = actionBarHeight;
                    }
                }
                if (viewsHolder.getSearchListView() != null) {
                    LayoutParams layoutParams = (LayoutParams) viewsHolder.getSearchListView().getLayoutParams();
                    if (layoutParams.topMargin != actionBarHeight) {
                        layoutParams.topMargin = actionBarHeight;
                    }
                }

                int height = MeasureSpec.getSize(heightMeasureSpec);
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

                boolean changed = false;
                if (rowsHolder.getLastMeasuredContentWidth() != getMeasuredWidth() || lastMeasuredContentHeight != getMeasuredHeight()) {
                    changed = rowsHolder.getLastMeasuredContentWidth() != 0 && rowsHolder.getLastMeasuredContentWidth() != getMeasuredWidth();
                    rowsHolder.setListContentHeight(0);
                    int count = listAdapter.getItemCount();
                    rowsHolder.setLastMeasuredContentWidth(getMeasuredWidth());
                    lastMeasuredContentHeight = getMeasuredHeight();
                    int ws = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
                    int hs = MeasureSpec.makeMeasureSpec(listView.getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
                    positionToOffset.clear();
                    for (int i = 0; i < count; i++) {
                        int type = listAdapter.getItemViewType(i);
                        positionToOffset.put(i, rowsHolder.getListContentHeight());
                        if (type == ListAdapter.VIEW_TYPE_SHARED_MEDIA) {
                            rowsHolder.setListContentHeight(rowsHolder.getListContentHeight() + listView.getMeasuredHeight());
                        } else {
                            RecyclerView.ViewHolder holder = listAdapter.createViewHolder(null, type);
                            listAdapter.onBindViewHolder(holder, i);
                            holder.itemView.measure(ws, hs);
                            rowsHolder.setListContentHeight(rowsHolder.getListContentHeight() + holder.itemView.getMeasuredHeight());
                        }
                    }

                    if (viewsHolder.getEmptyView() != null) {
                        ((LayoutParams) viewsHolder.getEmptyView().getLayoutParams()).topMargin = AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) + AndroidUtilities.statusBarHeight;
                    }
                }

                if (attributesHolder.getPreviousTransitionFragment() != null) {
                    attributesHolder.getNameTextView()[0].setRightPadding(attributesHolder.getNameTextView()[0].getMeasuredWidth() - attributesHolder.getPreviousTransitionFragment().getAvatarContainer().getTitleTextView().getMeasuredWidth());
                }

                if (!rowsHolder.isFragmentOpened() && (rowsHolder.isExpandPhoto() || rowsHolder.isOpenAnimationInProgress() && rowsHolder.getPlayProfileAnimation() == 2)) {
                    ignoreLayout = true;

                    if (rowsHolder.isExpandPhoto()) {
                        if (viewsHolder.getSearchItem() != null) {
                            viewsHolder.getSearchItem().setAlpha(0.0f);
                            viewsHolder.getSearchItem().setEnabled(false);
                            viewsHolder.getSearchItem().setVisibility(GONE);
                        }
                        attributesHolder.getNameTextView()[1].setTextColor(Color.WHITE);
                        attributesHolder.getNameTextView()[1].setPivotY(attributesHolder.getNameTextView()[1].getMeasuredHeight());
                        attributesHolder.getNameTextView()[1].setScaleX(1.67f);
                        attributesHolder.getNameTextView()[1].setScaleY(1.67f);
                        if (attributesHolder.getScamDrawable() != null) {
                            attributesHolder.getScamDrawable().setColor(Color.argb(179, 255, 255, 255));
                        }
                        if (attributesHolder.getLockIconDrawable() != null) {
                            attributesHolder.getLockIconDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                        }
                        if (attributesHolder.getVerifiedCrossfadeDrawable()[0] != null) {
                            attributesHolder.getVerifiedCrossfadeDrawable()[0].setProgress(1f);
                        }
                        if (attributesHolder.getVerifiedCrossfadeDrawable()[1] != null) {
                            attributesHolder.getVerifiedCrossfadeDrawable()[1].setProgress(1f);
                        }
                        if (attributesHolder.getPremiumCrossfadeDrawable()[0] != null) {
                            attributesHolder.getPremiumCrossfadeDrawable()[0].setProgress(1f);
                        }
                        if (attributesHolder.getPremiumCrossfadeDrawable()[1] != null) {
                            attributesHolder.getPremiumCrossfadeDrawable()[1].setProgress(1f);
                        }
                        components.getColorsUtils().updateEmojiStatusDrawableColor(1f);
                        attributesHolder.getOnlineTextView()[1].setTextColor(0xB3FFFFFF);
                        profileActivity.getActionBar().setItemsBackgroundColor(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, false);
                        profileActivity.getActionBar().setItemsColor(Color.WHITE, false);
                        viewsHolder.getOverlaysView().setOverlaysVisible();
                        viewsHolder.getOverlaysView().setAlphaValue(1.0f, false);
                        attributesHolder.getAvatarImage().setForegroundAlpha(1.0f);
                        viewsHolder.getAvatarContainer().setVisibility(View.GONE);
                        attributesHolder.getAvatarsViewPager().resetCurrentItem();
                        attributesHolder.getAvatarsViewPager().setVisibility(View.VISIBLE);
                        if (viewsHolder.getShowStatusButton() != null) {
                            viewsHolder.getShowStatusButton().setBackgroundColor(0x23ffffff);
                        }
                        if (viewsHolder.getStoryView() != null) {
                            viewsHolder.getStoryView().setExpandProgress(1f);
                        }
                        if (components.getViewComponentsHolder().getGiftsView() != null) {
                            components.getViewComponentsHolder().getGiftsView().setExpandProgress(1f);
                        }
                        rowsHolder.setExpandPhoto(false);
                        updateCollectibleHint(components);
                    }

                    rowsHolder.setAllowPullingDown(true);
                    rowsHolder.setPulledDown(true);
                    viewsHolder.getButtonsGroup().invalidate();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                    if (viewsHolder.getOtherItem() != null) {
                        if (!profileActivity.getMessagesController().isChatNoForwards(components.getAttributesComponentsHolder().getCurrentChat())) {
                            viewsHolder.getOtherItem().showSubItem(ProfileParams.gallery_menu_save);
                        } else {
                            viewsHolder.getOtherItem().hideSubItem(ProfileParams.gallery_menu_save);
                        }
                        if (viewsHolder.getImageUpdater() != null) {
                            viewsHolder.getOtherItem().showSubItem(ProfileParams.edit_avatar);
                            viewsHolder.getOtherItem().showSubItem(ProfileParams.delete_avatar);
                            viewsHolder.getOtherItem().hideSubItem(ProfileParams.logout);
                        }
                    }
                    rowsHolder.setCurrentExpanAnimatorFracture(1.0f);

                    int paddingTop;
                    int paddingBottom;
                    if (rowsHolder.isInLandscapeMode()) {
                        paddingTop = AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT);
                        paddingBottom = 0;
                    } else {
                        paddingTop = listView.getMeasuredWidth() + AndroidUtilities.dp(ProfileParams.GROUP_MARGIN + ProfileParams.GROUP_MARGIN * 2);
                        paddingBottom = Math.max(0, getMeasuredHeight() - (rowsHolder.getListContentHeight() + AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) + actionBarHeight));
                    }
                    if (banFromGroup != 0) {
                        paddingBottom += AndroidUtilities.dp(48);
                        listView.setBottomGlowOffset(AndroidUtilities.dp(48));
                    } else {
                        listView.setBottomGlowOffset(0);
                    }
                    rowsHolder.setInitialAnimationExtraHeight(paddingTop - actionBarHeight);
                    if (rowsHolder.getPlayProfileAnimation() == 0) {
                        rowsHolder.setExtraHeight(rowsHolder.getInitialAnimationExtraHeight());
                    }
                    viewsHolder.getLayoutManager().scrollToPositionWithOffset(0, -actionBarHeight);
                    listView.setPadding(0, paddingTop, 0, paddingBottom);
                    measureChildWithMargins(listView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    listView.layout(0, actionBarHeight, listView.getMeasuredWidth(), actionBarHeight + listView.getMeasuredHeight());
                    ignoreLayout = false;
                } else if (rowsHolder.isFragmentOpened() && !rowsHolder.isOpenAnimationInProgress() && !rowsHolder.isFirstLayout()) {
                    ignoreLayout = true;

                    int paddingTop;
                    int paddingBottom;
                    if (rowsHolder.isInLandscapeMode() || AndroidUtilities.isTablet()) {
                        paddingTop = AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT);
                        paddingBottom = 0;
                    } else {
                        paddingTop = listView.getMeasuredWidth();
                        paddingBottom = Math.max(0, getMeasuredHeight() - (rowsHolder.getListContentHeight() + AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) + actionBarHeight));
                    }
                    if (banFromGroup != 0) {
                        paddingBottom += AndroidUtilities.dp(48);
                        listView.setBottomGlowOffset(AndroidUtilities.dp(48));
                    } else {
                        listView.setBottomGlowOffset(0);
                    }
                    int currentPaddingTop = listView.getPaddingTop();
                    View view = null;
                    int pos = RecyclerView.NO_POSITION;
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        int p = listView.getChildAdapterPosition(listView.getChildAt(i));
                        if (p != RecyclerView.NO_POSITION) {
                            view = listView.getChildAt(i);
                            pos = p;
                            break;
                        }
                    }
                    if (view == null) {
                        view = listView.getChildAt(0);
                        if (view != null) {
                            RecyclerView.ViewHolder holder = listView.findContainingViewHolder(view);
                            pos = Objects.requireNonNull(holder).getAdapterPosition();
                            if (pos == RecyclerView.NO_POSITION) {
                                pos = holder.getPosition();
                            }
                        }
                    }

                    int top = paddingTop;
                    if (view != null) {
                        top = view.getTop();
                    }
                    boolean layout = false;
                    if ((profileActivity.getActionBar().isSearchFieldVisible() || openSimilar) && rowsHolder.getSharedMediaRow() >= 0) {
                        viewsHolder.getLayoutManager().scrollToPositionWithOffset(rowsHolder.getSharedMediaRow(), -paddingTop);
                        layout = true;
                    } else if (rowsHolder.isInvalidateScroll() || currentPaddingTop != paddingTop) {
                        if (rowsHolder.getSavedScrollPosition() >= 0) {
                            viewsHolder.getLayoutManager().scrollToPositionWithOffset(rowsHolder.getSavedScrollPosition(), rowsHolder.getSavedScrollOffset() - paddingTop);
                        } else if ((!changed || !rowsHolder.isAllowPullingDown()) && view != null) {
                            if (pos == 0 && !rowsHolder.isAllowPullingDown() && top > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) {
                                top = AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT);
                            }
                            viewsHolder.getLayoutManager().scrollToPositionWithOffset(pos, top - paddingTop);
                            layout = true;
                        } else {
                            viewsHolder.getLayoutManager().scrollToPositionWithOffset(0, AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) - paddingTop);
                        }
                    }
                    if (currentPaddingTop != paddingTop || listView.getPaddingBottom() != paddingBottom) {
                        listView.setPadding(0, paddingTop, 0, paddingBottom);
                        layout = true;
                    }
                    if (layout) {
                        measureChildWithMargins(listView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        try {
                            listView.layout(0, actionBarHeight, listView.getMeasuredWidth(), actionBarHeight + listView.getMeasuredHeight());
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    ignoreLayout = false;
                }

                boolean portrait = height > MeasureSpec.getSize(widthMeasureSpec);
                if (portrait != wasPortrait) {
                    post(() -> {
                        if (attributesHolder.getSelectAnimatedEmojiDialog() != null) {
                            attributesHolder.getSelectAnimatedEmojiDialog().dismiss();
                            attributesHolder.setSelectAnimatedEmojiDialog(null);
                        }
                    });
                    wasPortrait = portrait;
                }

                if (viewsHolder.getSearchItem() != null && viewsHolder.getQrItem() != null) {
                    float translation = AndroidUtilities.dp(48) * rowsHolder.getCurrentExpandAnimatorValue();
                    // ProfileParams.qrItem.setTranslationX(translation);
                }
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                rowsHolder.setSavedScrollPosition(-1);
                rowsHolder.setFirstLayout(false);
                rowsHolder.setInvalidateScroll(false);
                animation.checkListViewScroll();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            private final ArrayList<View> sortedChildren = new ArrayList<>();
            private final Comparator<View> viewComparator = (view, view2) -> (int) (view.getY() - view2.getY());


            @Override
            protected void dispatchDraw(Canvas canvas) {
                whitePaint.setColor(components.getColorsUtils().getThemedColor(Theme.key_windowBackgroundWhite));
                if (listView.getVisibility() == VISIBLE) {
                    grayPaint.setColor(components.getColorsUtils().getThemedColor(Theme.key_windowBackgroundGray));
                    if (rowsHolder.isTransitionAnimationInProress()) {
                        whitePaint.setAlpha((int) (255 * listView.getAlpha()));
                    }
                    if (rowsHolder.isTransitionAnimationInProress()) {
                        grayPaint.setAlpha((int) (255 * listView.getAlpha()));
                    }

                    int count = listView.getChildCount();
                    sortedChildren.clear();
                    boolean hasRemovingItems = false;
                    for (int i = 0; i < count; i++) {
                        View child = listView.getChildAt(i);
                        if (listView.getChildAdapterPosition(child) != RecyclerView.NO_POSITION) {
                            sortedChildren.add(listView.getChildAt(i));
                        } else {
                            hasRemovingItems = true;
                        }
                    }
                    Collections.sort(sortedChildren, viewComparator);
                    boolean hasBackground = false;
                    float lastY = listView.getY();
                    count = sortedChildren.size();
                    if (!rowsHolder.isOpenAnimationInProgress() && count > 0 && !hasRemovingItems) {
                        lastY += sortedChildren.get(0).getY();
                    }
                    float alpha = 1f;
                    for (int i = 0; i < count; i++) {
                        View child = sortedChildren.get(i);
                        boolean currentHasBackground = child.getBackground() != null;
                        int currentY = (int) (listView.getY() + child.getY());
                        if (hasBackground == currentHasBackground) {
                            if (child.getAlpha() == 1f) {
                                alpha = 1f;
                            }
                            continue;
                        }
                        if (hasBackground) {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, grayPaint);
                        } else {
                            if (alpha != 1f) {
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, grayPaint);
                                whitePaint.setAlpha((int) (255 * alpha));
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, whitePaint);
                                whitePaint.setAlpha(255);
                            } else {
                                canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), currentY, whitePaint);
                            }
                        }
                        hasBackground = currentHasBackground;
                        lastY = currentY;
                        alpha = child.getAlpha();
                    }

                    if (hasBackground) {
                        canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), grayPaint);
                    } else {
                        if (alpha != 1f) {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), grayPaint);
                            whitePaint.setAlpha((int) (255 * alpha));
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), whitePaint);
                            whitePaint.setAlpha(255);
                        } else {
                            canvas.drawRect(listView.getX(), lastY, listView.getX() + listView.getMeasuredWidth(), listView.getBottom(), whitePaint);
                        }
                    }
                } else {
                    int top = viewsHolder.getSearchListView().getTop();
                    canvas.drawRect(0, top + rowsHolder.getExtraHeight() + rowsHolder.getSearchTransitionOffset(), getMeasuredWidth(), top + getMeasuredHeight(), whitePaint);
                }
                super.dispatchDraw(canvas);
                if (profileTransitionInProgress && profileActivity.getParentLayout().getFragmentStack().size() > 1) {
                    BaseFragment fragment = profileActivity.getParentLayout().getFragmentStack().get(profileActivity.getParentLayout().getFragmentStack().size() - 2);
                    if (fragment instanceof ChatActivity) {
                        ChatActivity chatActivity = (ChatActivity) fragment;
                        FragmentContextView fragmentContextView = chatActivity.getFragmentContextView();

                        if (fragmentContextView != null && fragmentContextView.isCallStyle()) {
                            float progress = rowsHolder.getExtraHeight() / AndroidUtilities.dpf2(fragmentContextView.getStyleHeight());
                            if (progress > 1f) {
                                progress = 1f;
                            }
                            canvas.save();
                            canvas.translate(fragmentContextView.getX(), fragmentContextView.getY());
                            fragmentContextView.setDrawOverlay(true);
                            fragmentContextView.setCollapseTransition(true, rowsHolder.getExtraHeight(), progress);
                            fragmentContextView.draw(canvas);
                            fragmentContextView.setCollapseTransition(false, rowsHolder.getExtraHeight(), progress);
                            fragmentContextView.setDrawOverlay(false);
                            canvas.restore();
                        }
                    }
                }

                if (scrimPaint.getAlpha() > 0) {
                    canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);
                }
                if (scrimView != null) {
                    int c = canvas.save();
                    canvas.translate(scrimView.getLeft(), scrimView.getTop());
                    if (scrimView == profileActivity.getActionBar().getBackButton()) {
                        int r = Math.max(scrimView.getMeasuredWidth(), scrimView.getMeasuredHeight()) / 2;
                        int wasAlpha = actionBarBackgroundPaint.getAlpha();
                        actionBarBackgroundPaint.setAlpha((int) (wasAlpha * (scrimPaint.getAlpha() / 255f) / 0.3f));
                        canvas.drawCircle(r, r, r * 0.7f, actionBarBackgroundPaint);
                        actionBarBackgroundPaint.setAlpha(wasAlpha);
                    }
                    scrimView.draw(canvas);
                    canvas.restoreToCount(c);
                }
                if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
                    if (blurredView.getAlpha() != 1f) {
                        if (blurredView.getAlpha() != 0) {
                            canvas.saveLayerAlpha(blurredView.getLeft(), blurredView.getTop(), blurredView.getRight(), blurredView.getBottom(), (int) (255 * blurredView.getAlpha()), Canvas.ALL_SAVE_FLAG);
                            canvas.translate(blurredView.getLeft(), blurredView.getTop());
                            blurredView.draw(canvas);
                            canvas.restore();
                        }
                    } else {
                        blurredView.draw(canvas);
                    }
                }
            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (pinchToZoomHelper.isInOverlayMode() && (child == viewsHolder.getAvatarContainer2() || child == profileActivity.getActionBar() || child == attributesHolder.getWriteButton())) {
                    return true;
                }
                if (child == blurredView) {
                    return true;
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                rowsHolder.setFragmentViewAttached(true);
                for (AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable swapAnimatedEmojiDrawable : attributesHolder.getEmojiStatusDrawable()) {
                    if (swapAnimatedEmojiDrawable != null) {
                        swapAnimatedEmojiDrawable.attach();
                    }
                }
                for (AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable swapAnimatedEmojiDrawable : attributesHolder.getBotVerificationDrawable()) {
                    if (swapAnimatedEmojiDrawable != null) {
                        swapAnimatedEmojiDrawable.attach();
                    }
                }
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                rowsHolder.setFragmentViewAttached(false);
                for (AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable swapAnimatedEmojiDrawable : attributesHolder.getEmojiStatusDrawable()) {
                    if (swapAnimatedEmojiDrawable != null) {
                        swapAnimatedEmojiDrawable.detach();
                    }
                }
                for (AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable swapAnimatedEmojiDrawable : attributesHolder.getBotVerificationDrawable()) {
                    if (swapAnimatedEmojiDrawable != null) {
                        swapAnimatedEmojiDrawable.detach();
                    }
                }
            }
        };
    }
}
