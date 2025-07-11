package org.telegram.ui.profile.view.button;

import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.S)
public class GPUBlurRenderer implements BlurRenderer {
    private final RenderNode renderNode = new RenderNode("blur");
    private final Paint colorShiftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float scaleFactor;
    private View group;

    GPUBlurRenderer(View group, float scaleMultiplier) {
        this.group = group;
        this.scaleFactor = 4f * scaleMultiplier;

        float amount = 0.225f;
        float scale = 1f - 2f * amount;
        colorShiftPaint.setColorFilter(new ColorMatrixColorFilter(new float[] {
                scale, 0f, 0f, 0f, amount * 255,
                0f, scale, 0f, 0f, amount * 255,
                0f, 0f, scale, 0f, amount * 255,
                0f, 0f, 0f, 1f, 0f
        }));
    }

    @Override
    public void onSizeChanged(int w, int h) {
        renderNode.setPosition(0, 0, (int) Math.ceil(w / scaleFactor), (int) Math.ceil(h / scaleFactor));
    }

    @Override
    public void setBlurRadius(float radius) {
        float adjusted = radius / (scaleFactor / 4f);
        renderNode.setRenderEffect(RenderEffect.createBlurEffect(adjusted, adjusted, Shader.TileMode.CLAMP));
    }

    @Override
    public void record(RecordDrawer drawer) {
        Canvas canvas = renderNode.beginRecording();
        canvas.scale(1f / scaleFactor, 1f / scaleFactor);
        drawer.draw(canvas);
        renderNode.endRecording();
    }

    @Override
    public void draw(Canvas canvas, Path path) {
        canvas.saveLayer(0, 0, group.getWidth(), group.getHeight(), colorShiftPaint);
        canvas.clipPath(path);
        canvas.scale(scaleFactor, scaleFactor);
        canvas.drawRenderNode(renderNode);
        canvas.restore();
    }
}
