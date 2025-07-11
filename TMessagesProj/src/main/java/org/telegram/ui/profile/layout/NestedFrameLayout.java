package org.telegram.ui.profile.layout;

import static androidx.core.view.ViewCompat.TYPE_TOUCH;

import static org.telegram.ui.profile.utils.HideAndVisibility.hideFloatingButton;

import android.graphics.Canvas;
import android.view.View;

import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

import java.util.ArrayList;

public class NestedFrameLayout extends SizeNotifierFrameLayout implements NestedScrollingParent3 {

    private final NestedScrollingParentHelper nestedScrollingParentHelper;

    private final ProfileActivity profileActivity;
    private final RecyclerListView listView;
    private final ViewComponentsHolder viewsHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    private ComponentsFactory componentsFactory;
    public NestedFrameLayout(ComponentsFactory components) {
        super(components.getProfileActivity().getContext());
        componentsFactory = components;
        profileActivity = components.getProfileActivity();
        listView = components.getListView().getClippedList();
        viewsHolder = components.getViewComponentsHolder();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
        try {
            if (target == listView && rowsHolder.isSharedMediaLayoutAttached()) {
                RecyclerListView innerListView = viewsHolder.getSharedMediaLayout().getCurrentListView();
                int top = viewsHolder.getSharedMediaLayout().getTop();
                if (top == 0) {
                    consumed[1] = dyUnconsumed;
                    innerListView.scrollBy(0, dyUnconsumed);
                }
            }
            if (dyConsumed != 0 && type == TYPE_TOUCH) {
                hideFloatingButton(componentsFactory, profileActivity,!(viewsHolder.getSharedMediaLayout() == null || viewsHolder.getSharedMediaLayout().getClosestTab() == SharedMediaLayout.TAB_STORIES || viewsHolder.getSharedMediaLayout().getClosestTab() == SharedMediaLayout.TAB_ARCHIVED_STORIES) || dyConsumed > 0);
            }
        } catch (Throwable e) {
            FileLog.e(e);
            AndroidUtilities.runOnUIThread(() -> {
                try {
                    RecyclerListView innerListView = viewsHolder.getSharedMediaLayout().getCurrentListView();
                    if (innerListView != null && innerListView.getAdapter() != null) {
                        innerListView.getAdapter().notifyDataSetChanged();
                    }
                } catch (Throwable ignored) {

                }
            });
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
        if (target == listView && rowsHolder.getSharedMediaRow() != -1 && rowsHolder.isSharedMediaLayoutAttached()) {
            boolean searchVisible = profileActivity.getActionBar().isSearchFieldVisible();
            int t = viewsHolder.getSharedMediaLayout().getTop();
            if (dy < 0) {
                boolean scrolledInner = false;
                if (t <= 0) {
                    RecyclerListView innerListView = viewsHolder.getSharedMediaLayout().getCurrentListView();
                    if (innerListView != null) {
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) innerListView.getLayoutManager();
                        int pos = linearLayoutManager.findFirstVisibleItemPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            RecyclerView.ViewHolder holder = innerListView.findViewHolderForAdapterPosition(pos);
                            int top = holder != null ? holder.itemView.getTop() : -1;
                            int paddingTop = innerListView.getPaddingTop();
                            if (top != paddingTop || pos != 0) {
                                consumed[1] = pos != 0 ? dy : Math.max(dy, (top - paddingTop));
                                innerListView.scrollBy(0, dy);
                                scrolledInner = true;
                            }
                        }
                    }
                }
                if (searchVisible) {
                    if (!scrolledInner && t < 0) {
                        consumed[1] = dy - Math.max(t, dy);
                    } else {
                        consumed[1] = dy;
                    }
                }
            } else {
                if (searchVisible) {
                    RecyclerListView innerListView = viewsHolder.getSharedMediaLayout().getCurrentListView();
                    consumed[1] = dy;
                    if (t > 0) {
                        consumed[1] -= dy;
                    }
                    if (innerListView != null && consumed[1] > 0) {
                        innerListView.scrollBy(0, consumed[1]);
                    }
                }
            }
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int axes, int type) {
        return rowsHolder.getSharedMediaRow() != -1 && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View target, int type) {
        nestedScrollingParentHelper.onStopNestedScroll(target);
    }

    @Override
    public void onStopNestedScroll(View child) {

    }

    @Override
    protected void drawList(Canvas blurCanvas, boolean top, ArrayList<IViewWithInvalidateCallback> views) {
        super.drawList(blurCanvas, top, views);
        blurCanvas.save();
        blurCanvas.translate(0, listView.getY());
        viewsHolder.getSharedMediaLayout().drawListForBlur(blurCanvas, views);
        blurCanvas.restore();
    }
}

