package org.telegram.ui.profile.view;

import static org.telegram.ui.profile.builder.ProfileParams.VIRTUAL_TOP_BAR_HEIGHT;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Stories.StoriesListPlaceProvider;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;


public class ClippedListView {


    private final ComponentsFactory componentsFactory;
    private ActionBar actionBar;
    private ClippedList clippedList;
    private Paint whitePaint;
    private ViewComponentsHolder viewsHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    public ClippedListView(ComponentsFactory componentsFactory) {
        this.componentsFactory = componentsFactory;
        viewsHolder = componentsFactory.getViewComponentsHolder();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
    }

    public RecyclerListView getClippedList(ActionBar actionBar, Paint whitePaint) {
        this.actionBar = actionBar;
        this.whitePaint = whitePaint;
        clippedList = createView();
        return clippedList;
    }

    public RecyclerListView getClippedList() {
        return clippedList;
    }
    private ClippedList createView() {
        checkForViewsHolder();
        checkForRowsHolder();

        return new ClippedList(componentsFactory.getProfileActivity().getContext(), actionBar) {

            private VelocityTracker velocityTracker;

            @Override
            protected boolean canHighlightChildAt(View child, float x, float y) {
                return !(child instanceof AboutLinkCell);
            }

            @Override
            protected boolean allowSelectChildAtPosition(View child) {
                return child != viewsHolder.getSharedMediaLayout();
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            protected void requestChildOnScreen(View child, View focused) {

            }

            @Override
            public void invalidate() {
                super.invalidate();
                if (componentsFactory.getProfileActivity().fragmentView != null) {
                    componentsFactory.getProfileActivity().fragmentView.invalidate();
                }
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent e) {
                if (viewsHolder.getSharedMediaLayout() != null) {
                    if (viewsHolder.getSharedMediaLayout().canEditStories() && viewsHolder.getSharedMediaLayout().isActionModeShown() && viewsHolder.getSharedMediaLayout().getClosestTab() == SharedMediaLayout.TAB_BOT_PREVIEWS) {
                        return false;
                    }
                    if (viewsHolder.getSharedMediaLayout().canEditStories() && viewsHolder.getSharedMediaLayout().isActionModeShown() && viewsHolder.getSharedMediaLayout().getClosestTab() == SharedMediaLayout.TAB_STORIES) {
                        return false;
                    }
                    if (viewsHolder.getSharedMediaLayout().giftsContainer != null && viewsHolder.getSharedMediaLayout().giftsContainer.isReordering()) {
                        return false;
                    }
                }
                return super.onInterceptTouchEvent(e);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                final int action = e.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain();
                    } else {
                        velocityTracker.clear();
                    }
                    velocityTracker.addMovement(e);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(e);
                        velocityTracker.computeCurrentVelocity(1000);
                        rowsHolder.setListViewVelocityY(velocityTracker.getYVelocity(e.getPointerId(e.getActionIndex())));
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (velocityTracker != null) {
                        velocityTracker.recycle();
                        velocityTracker = null;
                    }
                }
                final boolean result = super.onTouchEvent(e);
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (rowsHolder.isAllowPullingDown()) {
                        final View view = viewsHolder.getLayoutManager().findViewByPosition(0);
                        if (view != null) {
                            if (rowsHolder.isPulledDown()) {
                                final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                                this.smoothScrollBy(0, view.getTop() - this.getMeasuredWidth() + actionBarHeight, CubicBezierInterpolator.EASE_OUT_QUINT);
                            } else {
                                this.smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(VIRTUAL_TOP_BAR_HEIGHT) - 50, CubicBezierInterpolator.EASE_OUT_QUINT);
                            }
                        }
                    } else {
                        final View view = viewsHolder.getLayoutManager().findViewByPosition(0);
                        if (view != null) {

                            if (viewsHolder.getAvatarContainer().getY() < -(viewsHolder.getAnimatingItem() != null ? viewsHolder.getAnimatingItem().getMeasuredHeight() * viewsHolder.getAvatarContainer().getScaleY() / 5 : 0)) {
                                this.smoothScrollBy(0, view.getTop(), CubicBezierInterpolator.EASE_OUT_QUINT);
                            } else {
                                this.smoothScrollBy(0, view.getTop() - AndroidUtilities.dp(VIRTUAL_TOP_BAR_HEIGHT) - 50, CubicBezierInterpolator.EASE_OUT_QUINT);
                            }
                        }
                    }
                }
                return result;
            }

            @Override
            public boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (getItemAnimator().isRunning() && child.getBackground() == null && child.getTranslationY() != 0) {
                    boolean useAlpha = this.getChildAdapterPosition(child) == rowsHolder.getSharedMediaRow() && child.getAlpha() != 1f;
                    if (useAlpha) {
                        whitePaint.setAlpha((int) (255 * this.getAlpha() * child.getAlpha()));
                    }
                    canvas.drawRect(this.getX(), child.getY(), this.getX() + this.getMeasuredWidth(), child.getY() + child.getHeight(), whitePaint);
                    if (useAlpha) {
                        whitePaint.setAlpha((int) (255 * this.getAlpha()));
                    }
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                if (rowsHolder.getBizHoursRow() >= 0 && rowsHolder.getInfoStartRow() >= 0 && rowsHolder.getInfoEndRow() >= 0) {
                    drawSectionBackground(canvas, rowsHolder.getInfoStartRow(), rowsHolder.getInfoEndRow(), getThemedColor(Theme.key_windowBackgroundWhite));
                }
                super.dispatchDraw(canvas);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                componentsFactory.getLayouts().updateBottomButtonY();
            }
        };
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        }
    }

    private void checkForViewsHolder() {
        if (viewsHolder == null) {
            viewsHolder = componentsFactory.getViewComponentsHolder();
        }
    }

    public static class ClippedList extends RecyclerListView implements StoriesListPlaceProvider.ClippedView {
    private final org.telegram.ui.ActionBar.ActionBar actionBar;

    public ClippedList(Context context, org.telegram.ui.ActionBar.ActionBar actionBar) {
        super(context);
        this.actionBar = actionBar;
    }

    @Override
    public void updateClip(int[] clip) {
        clip[0] = actionBar.getMeasuredHeight();
        clip[1] = getMeasuredHeight() - getPaddingBottom();
    }
}
}
