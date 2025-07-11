package org.telegram.ui.profile.view.button;

import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.core.util.Consumer;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ProfileGalleryView;

import java.util.ArrayList;
import java.util.List;

public class ButtonGroupView extends FrameLayout {
    private static final int SPACING_DP = 8;
    private static final float BLUR_RADIUS = 20f;

    private final List<ButtonViewEnum> visibleButtons = new ArrayList<>(4);
    private final List<ButtonViewEnum> extraButtons = new ArrayList<>();

    private float buttonWidth;
    private float darkProgress = -1f;
    private float openProgress;
    private boolean blurEnabled;

    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blurShadowPaint = new Paint();
    private final Path clipPath = new Path();

    private Consumer<ButtonViewEnum> clickListener;
    private final View avatarView;
    private final ProfileGalleryView galleryView;

    private final BlurRenderer blurRenderer;
    private final RecordDrawer contentDrawer = this::drawAvatarAndGallery;

    public ButtonGroupView(Context context, View avatar, ProfileGalleryView pager) {
        super(context);
        setWillNotDraw(false);
        this.avatarView = avatar;
        this.galleryView = pager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE) {

            float multiplier = SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH ? 1f : 2f;
            blurRenderer = new GPUBlurRenderer(this, multiplier);
        } else {
            blurRenderer = new DefaultBlurRenderer();
        }

        blurRenderer.setBlurRadius(BLUR_RADIUS);
        blurShadowPaint.setColor(0x42000000);
        setDarkProgress(0f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        blurRenderer.onSizeChanged(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        blurRenderer.release();
    }

    public void setOpenProgress(float progress) {
        if (this.openProgress == progress) return;
        this.openProgress = progress;
        blurShadowPaint.setColor(Color.argb((int) (0x42 * progress), 0, 0, 0));
        invalidate();
    }

    public void setDarkProgress(float progress) {
        if (this.darkProgress == progress) return;
        this.darkProgress = progress;
        int blendedColor = ColorUtils.blendARGB(Theme.getColor(Theme.key_listSelector), 0x21000000, progress);
        highlightPaint.setColor(blendedColor);
        invalidate();
    }

    public void setBlurEnabled(boolean enabled) {
        if (this.blurEnabled == enabled) return;
        this.blurEnabled = enabled;
        invalidate();
    }

    public void setButtonClickListener(Consumer<ButtonViewEnum> listener) {
        this.clickListener = listener;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        clipPath.rewind();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            float offsetTop = child.getHeight() * (1f - child.getScaleY());
            AndroidUtilities.rectTmp.set(
                    child.getLeft(), child.getTop() + offsetTop,
                    child.getRight(), child.getBottom()
            );
            clipPath.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(12), AndroidUtilities.dp(12), Path.Direction.CW);
        }

        if (blurEnabled && blurRenderer.canBlur()) {
            blurRenderer.record(contentDrawer);
            blurRenderer.draw(canvas, clipPath);
            invalidate();
        } else if (blurEnabled) {
            canvas.drawPath(clipPath, blurShadowPaint);
        }

        canvas.drawPath(clipPath, highlightPaint);
    }
    @Override


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        if (!visibleButtons.isEmpty()) {
            buttonWidth = (float) (MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight() - AndroidUtilities.dp(SPACING_DP) * (visibleButtons.size() - 1)) / visibleButtons.size();
        }

        int w = (int) (MeasureSpec.getSize(widthMeasureSpec) * buttonWidth);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
    }

    @Override


    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            ButtonViewEnum btn = ((ButtonView) v).button;
            int j = visibleButtons.indexOf(btn);
            if (j != -1) {
                float l = getPaddingLeft() + (buttonWidth + AndroidUtilities.dp(SPACING_DP)) * j;
                v.layout((int) l, 0, (int) (l + buttonWidth), getMeasuredHeight());
            } else {
                removeViewAt(i);
                i--;
            }
        }
    }

    private void drawAvatarAndGallery(Canvas canvas) {
        if (avatarView.getVisibility() == View.VISIBLE) {
            canvas.save();
            canvas.translate(avatarView.getX() - getX(), avatarView.getY() - getY());
            canvas.scale(avatarView.getScaleX(), avatarView.getScaleY(), avatarView.getPivotX(), avatarView.getPivotY());
            canvas.clipRect(0, 0, avatarView.getWidth() * avatarView.getScaleX(), avatarView.getHeight() * avatarView.getScaleY());
            avatarView.draw(canvas);
            canvas.restore();
        }

        if (galleryView.getVisibility() == View.VISIBLE) {
            canvas.save();
            canvas.translate(galleryView.getX() - getX(), galleryView.getY() - getY());
            canvas.scale(galleryView.getScaleX(), galleryView.getScaleY(), galleryView.getPivotX(), galleryView.getPivotY());
            canvas.clipRect(0, 0, galleryView.getWidth(), galleryView.getHeight());

            int current = galleryView.getCurrentItem();
            for (int i = -1; i <= 1; i++) {
                View itemView = galleryView.getItemView(current + i);
                if (itemView != null) {
                    float transformPos = (float) (itemView.getLeft() - galleryView.getScrollX()) / galleryView.getWidth();
                    if (transformPos <= 1f) {
                        canvas.save();
                        canvas.translate(galleryView.getWidth() * transformPos, 0);
                        itemView.draw(canvas);
                        canvas.restore();
                    }
                }
            }
            canvas.restore();
        }
    }

    public void setExpandProgress(float v) {
        setAlpha((MathUtils.clamp(v, 0.15f, 0.35f) - 0.15f) / 0.2f);
        float sc = (MathUtils.clamp(v, 0.1f, 0.5f) - 0.1f) / 0.4f;

        for (int i = 0; i < getChildCount(); i++) {
            ButtonView btn = (ButtonView) getChildAt(i);
            btn.setScaleX(AndroidUtilities.lerp(0.2f, 1f, sc));
            btn.setScaleY(AndroidUtilities.lerp(0.2f, 1f, sc));
            btn.setPivotX(btn.getWidth() / 2f);
            btn.setPivotY(btn.getHeight());
        }
        invalidate();
    }



    public View getButtonView(ButtonViewEnum btn) {
        for (int i = 0; i < getChildCount(); i++) {
            ButtonView buttonView = (ButtonView) getChildAt(i);
            if (buttonView.button == btn) {
                return buttonView;
            }
        }

        return this;
    }



    public void setButtons(List<ButtonViewEnum> buttons, boolean animate) {
        visibleButtons.clear();
        extraButtons.clear();

        int limit = buttons.contains(ButtonViewEnum.REPORT) ? 3 : 4;
        for (ButtonViewEnum btn : buttons) {
            if (visibleButtons.size() >= limit) {
                extraButtons.add(btn);
            } else {
                visibleButtons.add(btn);
            }
        }

        if (buttons.contains(ButtonViewEnum.REPORT)) {
            visibleButtons.add(ButtonViewEnum.REPORT);
        }

        if (!extraButtons.isEmpty()) {
            if (extraButtons.contains(ButtonViewEnum.GIFT) && buttons.contains(ButtonViewEnum.DISCUSS)) {
                extraButtons.remove(ButtonViewEnum.GIFT);
                buttons.remove(ButtonViewEnum.DISCUSS);
                extraButtons.add(ButtonViewEnum.DISCUSS);
                buttons.add(ButtonViewEnum.GIFT);
            }
        }

        for (ButtonViewEnum btn : visibleButtons) {
            boolean contains = false;
            for (int i = 0; i < getChildCount(); i++) {
                if (btn == ((ButtonView) getChildAt(i)).button) {
                    contains = true;
                    break;
                }
            }

            if (contains) continue;

            addView(new ButtonView(getContext()).bind(btn, () -> {
                if (clickListener != null) {
                    clickListener.accept(btn);
                }
            }));
        }
        requestLayout();
    }


    public List<ButtonViewEnum> getExtraButtons() {
        return extraButtons;
    }
}
