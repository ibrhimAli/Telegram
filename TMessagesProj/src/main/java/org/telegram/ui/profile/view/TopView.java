package org.telegram.ui.profile.view;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.StarGiftPatterns;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.utils.ColorsUtils;
import org.telegram.ui.profile.utils.ProfileDropRenderer;

public class TopView extends FrameLayout {

    private int currentColor;
    private final Paint paint = new Paint();
    private final ProfileActivity profileActivity;
    private final ViewComponentsHolder viewsHolder;
    private ColorsUtils colorUtils;
    private RowsAndStatusComponentsHolder rowsHolder;
    private AttributesComponentsHolder attributesHolder;
    private ComponentsFactory components;

    public TopView(ComponentsFactory componentsFactory, ProfileActivity context) {
        super(context.getContext());
        profileActivity = context;
        viewsHolder = componentsFactory.getViewComponentsHolder();
        colorUtils = componentsFactory.getColorsUtils();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        attributesHolder = componentsFactory.getAttributesComponentsHolder();
        components = componentsFactory;
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(widthMeasureSpec) + AndroidUtilities.dp(100)
        + AndroidUtilities.dp(ProfileParams.GROUP_BUTTON_DIMENSION + ProfileParams.GROUP_MARGIN * 2));
    }

    @Override
    public void setBackgroundColor(int color) {
        if (color != currentColor) {
            currentColor = color;
            paint.setColor(color);
            invalidate();
            if (!hasColorById) {
                rowsHolder.setActionBarBackgroundColor(currentColor);
            }
        }
    }

    public boolean hasColorById;
    private final AnimatedFloat hasColorAnimated = new AnimatedFloat(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    public int color1, color2;
    private final AnimatedColor color1Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedColor color2Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

    private int backgroundGradientColor1, backgroundGradientColor2, backgroundGradientHeight;
    private RadialGradient backgroundGradient;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setBackgroundColorId(MessagesController.PeerColor peerColor, boolean animated) {
        if (peerColor != null) {
            hasColorById = true;
            color1 = peerColor.getBgColor1(Theme.isCurrentThemeDark());
            color2 = peerColor.getBgColor2(Theme.isCurrentThemeDark());
            rowsHolder.setActionBarBackgroundColor(ColorUtils.blendARGB(color1, color2, 0.25f));
            if (peerColor.patternColor != 0) {
                emojiColor = peerColor.patternColor;
            } else {
                emojiColor = PeerColorActivity.adaptProfileEmojiColor(color1);
            }
        } else {
            rowsHolder.setActionBarBackgroundColor(currentColor);
            hasColorById = false;
            if (AndroidUtilities.computePerceivedBrightness(colorUtils.getThemedColor(Theme.key_actionBarDefault)) > .8f) {
                emojiColor = colorUtils.getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
            } else if (AndroidUtilities.computePerceivedBrightness(colorUtils.getThemedColor(Theme.key_actionBarDefault)) < .2f) {
                emojiColor = Theme.multAlpha(colorUtils.getThemedColor(Theme.key_actionBarDefaultTitle), .5f);
            } else {
                emojiColor = PeerColorActivity.adaptProfileEmojiColor(colorUtils.getThemedColor(Theme.key_actionBarDefault));
            }
        }
        if (!animated) {
            color1Animated.set(color1, true);
            color2Animated.set(color2, true);
        }
        invalidate();
    }

    private int emojiColor;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        emoji.attach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        emoji.detach();
    }

    public final AnimatedFloat emojiLoadedT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);
    public final AnimatedFloat emojiFullT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);

    private boolean hasEmoji;
    private boolean emojiIsCollectible;
    public void setBackgroundEmojiId(long emojiId, boolean isCollectible, boolean animated) {
        emoji.set(emojiId, animated);
        emoji.setColor(emojiColor);
        emojiIsCollectible = isCollectible;
        if (!animated) {
            emojiFullT.force(isCollectible);
        }
        hasEmoji = hasEmoji || emojiId != 0 && emojiId != -1;
        invalidate();
    }

    private boolean emojiLoaded;
    private boolean isEmojiLoaded() {
        if (emojiLoaded) {
            return true;
        }
        if (emoji != null && emoji.getDrawable() instanceof AnimatedEmojiDrawable) {
            AnimatedEmojiDrawable drawable = (AnimatedEmojiDrawable) emoji.getDrawable();
            if (drawable.getImageReceiver() != null && drawable.getImageReceiver().hasImageLoaded()) {
                return emojiLoaded = true;
            }
        }
        return false;
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        final int height = ActionBar.getCurrentActionBarHeight() + (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
//        final float v = rowsHolder.getExtraHeight() + height + rowsHolder.getSearchTransitionOffset();
//
//        int y1 = (int) (v * (1.0f - rowsHolder.getMediaHeaderAnimationProgress()));
//
//        if (y1 != 0) {
//            if (attributesHolder.getPreviousTransitionFragment() != null && attributesHolder.getPreviousTransitionFragment().getContentView() != null) {
//                blurBounds.set(0, 0, getMeasuredWidth(), y1);
//                if (attributesHolder.getPreviousTransitionFragment().getActionBar() != null && !attributesHolder.getPreviousTransitionFragment().getContentView().blurWasDrawn() && attributesHolder.getPreviousTransitionFragment().getActionBar().getBackground() == null) {
//                    paint.setColor(Theme.getColor(Theme.key_actionBarDefault, attributesHolder.getPreviousTransitionFragment().getResourceProvider()));
//                    canvas.drawRect(blurBounds, paint);
//                } else if (viewsHolder.getPreviousTransitionMainFragment() != null && viewsHolder.getPreviousTransitionMainFragment() instanceof DialogsActivity && viewsHolder.getPreviousTransitionMainFragment().getFragmentView() instanceof SizeNotifierFrameLayout) {
//                    viewsHolder.getPreviousTransitionMainFragment().getActionBar().blurScrimPaint.setColor(Theme.getColor(Theme.key_actionBarDefault, viewsHolder.getPreviousTransitionMainFragment().getResourceProvider()));
//                    ((SizeNotifierFrameLayout) viewsHolder.getPreviousTransitionMainFragment().getFragmentView()).drawBlurRect(canvas, getY(), blurBounds, viewsHolder.getPreviousTransitionMainFragment().getActionBar().blurScrimPaint, true);
//                } else {
//                    attributesHolder.getPreviousTransitionFragment().getContentView().drawBlurRect(canvas, getY(), blurBounds, attributesHolder.getPreviousTransitionFragment().getActionBar().blurScrimPaint, true);
//                }
//            }
//            paint.setColor(currentColor);
//            final int color1 = color1Animated.set(this.color1);
//            final int color2 = color2Animated.set(this.color2);
//            final int gradientHeight = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(144);
//            if (backgroundGradient == null || backgroundGradientColor1 != color1 || backgroundGradientColor2 != color2 || backgroundGradientHeight != gradientHeight) {
//                backgroundGradient = new LinearGradient(0, 0, 0, backgroundGradientHeight = gradientHeight, new int[] { backgroundGradientColor2 = color2, backgroundGradientColor1 = color1 }, new float[] { 0, 1 }, Shader.TileMode.CLAMP);
//                backgroundPaint.setShader(backgroundGradient);
//            }
//            final float progressToGradient = (rowsHolder.getPlayProfileAnimation() == 0 ? 1f : rowsHolder.getAvatarAnimationProgress()) * hasColorAnimated.set(hasColorById);
//            if (progressToGradient < 1) {
//                canvas.drawRect(0, 0, getMeasuredWidth(), y1, paint);
//            }
//            if (progressToGradient > 0) {
//                backgroundPaint.setAlpha((int) (0xFF * progressToGradient));
//                canvas.drawRect(0, 0, getMeasuredWidth(), y1, backgroundPaint);
//            }
//            if (hasEmoji) {
//                final float loadedScale = emojiLoadedT.set(isEmojiLoaded());
//                final float full = emojiFullT.set(emojiIsCollectible);
//                if (loadedScale > 0) {
//                    canvas.save();
//                    canvas.clipRect(0, 0, getMeasuredWidth(), y1);
//                    StarGiftPatterns.drawProfilePattern(canvas, emoji, getMeasuredWidth(), ((profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + dp(144)) - (1f - rowsHolder.getExtraHeight() / dp(88)) * dp(50), Math.min(1f, rowsHolder.getExtraHeight() / dp(88)), full);
//                    canvas.restore();
//                }
//            }
//            if (attributesHolder.getPreviousTransitionFragment() != null) {
//                ActionBar actionBar = attributesHolder.getPreviousTransitionFragment().getActionBar();
//                ActionBarMenu menu = actionBar.menu;
//                if (menu != null) {
//                    int restoreCount = canvas.save();
//                    canvas.translate(actionBar.getX() + menu.getX(), actionBar.getY() + menu.getY());
//                    canvas.saveLayerAlpha(0, 0, menu.getMeasuredWidth(), menu.getMeasuredHeight(), (int) (255 * (1f - rowsHolder.getAvatarAnimationProgress())), Canvas.ALL_SAVE_FLAG);
//                    menu.draw(canvas);
//                    canvas.restoreToCount(restoreCount);
//                }
//            }
//        }
//        if (y1 != v) {
//            int color = colorUtils.getThemedColor(Theme.key_windowBackgroundWhite);
//            paint.setColor(color);
//            blurBounds.set(0, y1, getMeasuredWidth(), (int) v);
//            viewsHolder.getContentView().drawBlurRect(canvas, getY(), blurBounds, paint, true);
//        }
//
//        if (profileActivity.getParentLayout() != null) {
//            profileActivity.getParentLayout().drawHeaderShadow(canvas, (int) (ProfileParams.headerShadowAlpha * 255), (int) v);
//        }
//    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        int actionBarHeight = ActionBar.getCurrentActionBarHeight();
//        int statusBarHeight = profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
//        int baseTopOffset = actionBarHeight + statusBarHeight;
//        float drawHeight = extraHeight + baseTopOffset + searchTransitionOffset;
//
//        int drawLimitY = (int) (drawHeight * (1f - mediaHeaderAnimationProgress));
//
//        if (drawLimitY == 0) {
//            return;
//        }
//
//        drawPreviousFragmentBackground(canvas, drawLimitY);
//        drawAnimatedGradient(canvas, baseTopOffset, drawLimitY);
//        drawEmojiBackground(canvas, drawLimitY);
//        drawProfileDrop(canvas, drawLimitY);
//        drawPreviousMenuFade(canvas);
//        drawBottomOverlay(canvas, drawLimitY, drawHeight);
//        drawHeaderShadow(canvas, drawHeight);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        int actionBarHeight = ActionBar.getCurrentActionBarHeight();
        int statusBarHeight = profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
        int baseTopOffset = actionBarHeight + statusBarHeight;
        float drawHeight = rowsHolder.getExtraHeight() + baseTopOffset + rowsHolder.getSearchTransitionOffset();

        int drawLimitY = (int) (drawHeight * (1f - rowsHolder.getMediaHeaderAnimationProgress()));

        if (drawLimitY == 0) {
            return;
        }

        drawPreviousFragmentBackground(canvas, drawLimitY);
        drawAnimatedGradient(canvas, baseTopOffset, drawLimitY);
        drawEmojiBackground(canvas, drawLimitY);
        drawProfileDrop(canvas, drawLimitY);
        drawPreviousMenuFade(canvas);
        drawBottomOverlay(canvas, drawLimitY, drawHeight);
        drawHeaderShadow(canvas, drawHeight);
    }

    private void drawPreviousFragmentBackground(Canvas canvas, int limitY) {
        if (attributesHolder.getPreviousTransitionFragment() == null || attributesHolder.getPreviousTransitionFragment().getContentView() == null) return;

        blurBounds.set(0, 0, getMeasuredWidth(), limitY);

        if (shouldDrawSimpleColorBackground()) {
            paint.setColor(Theme.getColor(Theme.key_actionBarDefault, attributesHolder.getPreviousTransitionFragment().getResourceProvider()));
            canvas.drawRect(blurBounds, paint);
        } else {
            drawBlurBackground(canvas);
        }
    }

    private boolean shouldDrawSimpleColorBackground() {
        return attributesHolder.getPreviousTransitionFragment().getActionBar() != null &&
                !attributesHolder.getPreviousTransitionFragment().getContentView().blurWasDrawn() &&
                attributesHolder.getPreviousTransitionFragment().getActionBar().getBackground() == null;
    }

    private void drawBlurBackground(Canvas canvas) {
        if (attributesHolder.getPreviousTransitionFragment() instanceof DialogsActivity &&
                viewsHolder.getPreviousTransitionMainFragment().getFragmentView() instanceof SizeNotifierFrameLayout) {

            Paint scrimPaint = attributesHolder.getPreviousTransitionFragment().getActionBar().blurScrimPaint;
            scrimPaint.setColor(Theme.getColor(Theme.key_actionBarDefault, attributesHolder.getPreviousTransitionFragment().getResourceProvider()));
            ((SizeNotifierFrameLayout) viewsHolder.getPreviousTransitionMainFragment().getFragmentView())
                    .drawBlurRect(canvas, getY(), blurBounds, scrimPaint, true);

        } else {
            Paint scrimPaint = attributesHolder.getPreviousTransitionFragment().getActionBar().blurScrimPaint;
            attributesHolder.getPreviousTransitionFragment().getContentView()
                    .drawBlurRect(canvas, getY(), blurBounds, scrimPaint, true);
        }
    }

    private void drawAnimatedGradient(Canvas canvas, int topOffset, int limitY) {
        paint.setColor(currentColor);

        int gradientColor1 = color1Animated.set(color1);
        int gradientColor2 = color2Animated.set(color2);
        int gradientHeight = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(144);
        float animationFactor = Math.min(1f, rowsHolder.getExtraHeight() / dp(188f));

        if (backgroundGradientNeedsUpdate(gradientColor1, gradientColor2, gradientHeight)) {
            updateBackgroundGradient(canvas.getWidth(), topOffset, animationFactor, gradientColor1, gradientColor2, gradientHeight);
        }

        float progress = (rowsHolder.getPlayProfileAnimation() == 0 ? 1f : rowsHolder.getAvatarAnimationProgress()) * hasColorAnimated.set(hasColorById);

        if (progress < 1f) {
            canvas.drawRect(0, 0, getMeasuredWidth(), limitY, paint);
        }

        if (progress > 0f) {
            backgroundPaint.setAlpha((int) (0xFF * progress));
            canvas.drawRect(0, 0, getMeasuredWidth(), limitY, backgroundPaint);
        }
    }

    private boolean backgroundGradientNeedsUpdate(int color1, int color2, int height) {
        return backgroundGradient == null ||
                backgroundGradientColor1 != color1 ||
                backgroundGradientColor2 != color2 ||
                backgroundGradientHeight != height;
    }

    private void updateBackgroundGradient(float width, int topOffset, float factor, int color1, int color2, int height) {
        backgroundGradientHeight = height;
        float x = width / 2f;
        float y = topOffset - (topOffset / (float) Math.pow(2f, factor));

        backgroundGradientColor1 = color1;
        backgroundGradientColor2 = color2;

        backgroundGradient = new RadialGradient(
                x, y, backgroundGradientHeight,
                new int[]{color2, color1},
                new float[]{0f, factor},
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(backgroundGradient);
    }

    private void drawEmojiBackground(Canvas canvas, int limitY) {
        if (!hasEmoji) return;

        float scale = emojiLoadedT.set(isEmojiLoaded());
        float full = emojiFullT.set(emojiIsCollectible);

        if (scale > 0f) {
            canvas.save();
            canvas.clipRect(0, 0, getMeasuredWidth(), limitY);

            float width = getMeasuredWidth();
            float height = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + dp(144)
                    - (1f - rowsHolder.getExtraHeight() / dp(188f)) * dp(50);
            float alpha = Math.min(1f, rowsHolder.getExtraHeight() / dp(188f));


            StarGiftPatterns.drawProfileCenteredPattern(canvas, emoji, width, height, alpha);
            canvas.restore();
        }
    }

    private void drawProfileDrop(Canvas canvas, int limitY) {
        float width = getMeasuredWidth();
        float height = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + dp(144)
                - (1f - rowsHolder.getExtraHeight() / dp(188f)) * dp(50);

        canvas.save();
        canvas.clipRect(0, 0, getMeasuredWidth(), limitY);
        ProfileDropRenderer.render(canvas, width, height, viewsHolder.getAvatarContainer());
        canvas.restore();
    }

    private void drawPreviousMenuFade(Canvas canvas) {
        if (attributesHolder.getPreviousTransitionFragment() == null) return;

        ActionBar actionBar = attributesHolder.getPreviousTransitionFragment().getActionBar();
        if (actionBar == null || actionBar.menu == null) return;

        ActionBarMenu menu = actionBar.menu;
        canvas.save();
        canvas.translate(actionBar.getX() + menu.getX(), actionBar.getY() + menu.getY());
        canvas.saveLayerAlpha(0, 0, menu.getMeasuredWidth(), menu.getMeasuredHeight(),
                (int) (255 * (1f - rowsHolder.getAvatarAnimationProgress())), Canvas.ALL_SAVE_FLAG);
        menu.draw(canvas);
        canvas.restore();
    }

    private void drawBottomOverlay(Canvas canvas, int startY, float endY) {
        if (startY == endY) return;

        int backgroundColor = colorUtils.getThemedColor(Theme.key_windowBackgroundWhite);
        paint.setColor(backgroundColor);

        blurBounds.set(0, startY, getMeasuredWidth(), (int) endY);
        viewsHolder.getContentView().drawBlurRect(canvas, getY(), blurBounds, paint, true);
    }

    private void drawHeaderShadow(Canvas canvas, float topOffset) {
        if (profileActivity.getParentLayout() != null) {
            profileActivity.getParentLayout().drawHeaderShadow(canvas, (int) (ProfileParams.headerShadowAlpha * 255), (int) topOffset);
        }
    }

    private final Rect blurBounds = new Rect();

}
