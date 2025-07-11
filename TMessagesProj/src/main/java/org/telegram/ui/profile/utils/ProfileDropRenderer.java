package org.telegram.ui.profile.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

public class ProfileDropRenderer {

    private static final Paint dropPaint = createDropPaint();

    private static Paint createDropPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    public static void render(Canvas canvas, float canvasWidth, float canvasHeight, View avatarView) {
        float scaleX = avatarView.getScaleX();
        float scaleY = avatarView.getScaleY();
        float avatarWidth = avatarView.getMeasuredWidth();
        float avatarHeight = avatarView.getMeasuredHeight();
        float avatarY = avatarView.getY();

        float maxRadius = AndroidUtilities.dp(62);
        float radius = Math.min(maxRadius, (avatarWidth * scaleX) / 2f);
        float centerX = canvasWidth / 2f;
        float centerY = avatarY + radius;

        // Draw static circle background when not expanded too much
        if (scaleY < 1.2f) {
            canvas.drawCircle(centerX, centerY, radius, dropPaint);
        }

        // Draw dynamic ripple when contracted further
        if (scaleY < 1.1f) {
            float interpolationFactor = (1.1f - scaleY) / 1.1f;
            Path ripplePath = new Path();

            if (avatarY > 0) {
                float rippleHeight = 40 - avatarY;
                ripplePath.moveTo(centerX - radius, 0);
                ripplePath.cubicTo(centerX - radius / 2f, 0, centerX - radius / 2f, rippleHeight, centerX, rippleHeight);
                ripplePath.cubicTo(centerX + radius / 2f, rippleHeight, centerX + radius / 2f, 0, centerX + radius, 0);
            } else {
                float previousY = Math.max(0f, 40 - avatarY);
                float newY = avatarY + avatarHeight * scaleY;

                float smoothFactor = Math.min((interpolationFactor - 0.2f) / 0.3f, 1f);

                float rippleY = AndroidUtilities.lerp(previousY, newY, smoothFactor);
                float rippleRadius = AndroidUtilities.lerp(radius, radius * 2f, smoothFactor);
                float leftControlX = AndroidUtilities.lerp(centerX - radius / 2f, centerX - rippleRadius / 2f, smoothFactor);
                float rightControlX = AndroidUtilities.lerp(centerX + radius / 2f, centerX + rippleRadius / 2f, smoothFactor);

                ripplePath.moveTo(centerX - rippleRadius, 0);
                ripplePath.cubicTo(leftControlX, 0, leftControlX, rippleY, centerX, rippleY);
                ripplePath.cubicTo(rightControlX, rippleY, rightControlX, 0, centerX + rippleRadius, 0);
            }

            canvas.drawPath(ripplePath, dropPaint);
        }
    }
}
