package org.telegram.ui.Stars;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class StarGiftPatterns {

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_ACTION = 1;
    public static final int TYPE_GIFT = 2;
    public static final int TYPE_LINK_PREVIEW = 3;

    private static final float[][] patternLocations = new float[][] {
            {
                    83.33f, 24, 27.33f, .22f,
                    68.66f, 75.33f, 25.33f, .21f,
                    0, 86, 25.33f, .12f,
                    -68.66f, 75.33f, 25.33f, .21f,
                    -82.66f, 13.66f, 27.33f, .22f,
                    -80, -33.33f, 20, .24f,
                    -46.5f, -63.16f, 27, .21f,
                    1, -82.66f, 20, .15f,
                    46.5f, -63.16f, 27, .21f,
                    80, -33.33f, 19.33f, .24f,

                    115.66f, -63, 20, .15f,
                    134, -10.66f, 20, .18f,
                    118.66f, 55.66f, 20, .15f,
                    124.33f, 98.33f, 20, .11f,

                    -128, 98.33f, 20, .11f,
                    -108, 55.66f, 20, .15f,
                    -123.33f, -10.66f, 20, .18f,
                    -116, -63.33f, 20, .15f
            },
            {
                    27.33f, -57.66f, 20, .12f,
                    59, -32, 19.33f, .22f,
                    77, 4.33f, 22.66f, .2f,
                    100, 40.33f, 18, .12f,
                    58.66f, 59, 20, .18f,
                    73.33f, 100.33f, 22.66f, .15f,
                    75, 155, 22, .11f,

                    -27.33f, -57.33f, 20, .12f,
                    -59, -32.33f, 19.33f, .2f,
                    -77, 4.66f, 23.33f, .2f,
                    -98.66f, 41, 18.66f, .12f,
                    -58, 59.33f, 19.33f, .18f,
                    -73.33f, 100, 22, .15f,
                    -75.66f, 155, 22, .11f
            },
            {
                    -0.83f, -52.16f, 12.33f, .2f,
                    26.66f, -40.33f, 16, .2f,
                    44.16f, -20.5f, 12.33f, .2f,
                    53, 7.33f, 16, .2f,
                    31, 23.66f, 14.66f, .2f,
                    0, 32, 13.33f, .2f,
                    -29, 23.66f, 14, .2f,
                    -53, 7.33f, 16, .2f,
                    -44.5f, -20.16f, 12.33f, .2f,
                    -27.33f, -40.33f, 16, .2f,
                    43.66f, 50, 14.66f, .2f,
                    -41.66f, 48, 14.66f, .2f
            },
            {
                    -0.16f, -103.5f, 20.33f, .15f,
                    39.66f, -77.33f, 26.66f, .15f,
                    70.66f, -46.33f, 21.33f, .15f,
                    84.5f, -3.83f, 29.66f, .15f,
                    65.33f, 56.33f, 24.66f, .15f,
                    0, 67.66f, 24.66f, .15f,
                    -65.66f, 56.66f, 24.66f, .15f,
                    -85, -4, 29.33f, .15f,
                    -70.66f, -46.33f, 21.33f, .15f,
                    -40.33f, -77.66f, 26.66f, .15f,

                    62.66f, -109.66f, 21.33f, .11f,
                    103.166f, -67.5f, 20.33f, .11f,
                    110.33f, 37.66f, 20.66f, .11f,
                    94.166f, 91.16f, 20.33f, .11f,
                    38.83f, 91.16f, 20.33f, .11f,
                    0, 112.5f, 20.33f, .11f,
                    -38.83f, 91.16f, 20.33f, .11f,
                    -94.166f, 91.16f, 20.33f, .11f,
                    -110.33f, 37.66f, 20.66f, .11f,
                    -103.166f, -67.5f, 20.33f, .11f,
                    -62.66f, -109.66f, 21.33f, .11f
            }
    };

    public static void drawPattern(Canvas canvas, Drawable pattern, float w, float h, float alpha, float scale) {
        drawPattern(canvas, TYPE_DEFAULT, pattern, w, h, alpha, scale);
    }

    public static void drawPattern(Canvas canvas, int type, Drawable pattern, float w, float h, float alpha, float scale) {
        if (alpha <= 0.0f) return;
        for (int i = 0; i < patternLocations[type].length; i += 4) {
            final float x = patternLocations[type][i];
            final float y = patternLocations[type][i + 1];
            final float size = patternLocations[type][i + 2];
            final float thisAlpha = patternLocations[type][i + 3];

            float cx = x, cy = y, sz = size;
            if (w < h && type == TYPE_DEFAULT) {
                cx = y;
                cy = x;
            }
            cx *= scale;
            cy *= scale;
            sz *= scale;
            pattern.setBounds((int) (dp(cx) - dp(sz) / 2.0f), (int) (dp(cy) - dp(sz) / 2.0f), (int) (dp(cx) + dp(sz) / 2.0f), (int) (dp(cy) + dp(sz) / 2.0f));

            pattern.setAlpha((int) (0xFF * alpha * thisAlpha));
            pattern.draw(canvas);
        }
    }

    // Left, right, and center profiles (unchanged coordinates, but cleaned usage follows)
    private static final float[] PROFILE_LEFT = new float[] {
            0, -107.33f, 16, 0.1505f,
            14.33f, -84, 18, 0.1988f,
            0, -50.66f, 18.66f, 0.3225f,
            13, -15, 18.66f, 0.37f,
            43.33f, 1, 18.66f, 0.3186f
    };

    private static final float[] PROFILE_RIGHT = new float[] {
            -35.66f, -5, 24, 0.2388f,
            -14.33f, -29.33f, 20.66f, 0.32f,
            -15, -73.66f, 19.33f, 0.32f,
            -2, -99.66f, 18, 0.1476f,
            -64.33f, -24.66f, 23.33f, 0.3235f,
            -40.66f, -53.33f, 24, 0.3654f,
            -50.33f, -85.66f, 20, 0.172f,
            -96, -1.33f, 19.33f, 0.3343f,
            -136.66f, -13, 18.66f, 0.2569f,
            -104.66f, -33.66f, 20.66f, 0.2216f,
            -82, -62.33f, 22.66f, 0.2562f,
            -131.66f, -60, 18, 0.1316f,
            -105.66f, -88.33f, 18, 0.1487f
    };

    // Tolerance levels used to stagger star movement
    private static final float T1 = 0.15f, T2 = 0.2f, T3 = 0.25f, T4 = 0.28f, T5 = 0.3f;

    private static final float[] PROFILE_CENTER = new float[] {
            -45f, -80f, 16f, 0.18f, T4,
            45f, -80f, 16f, 0.18f, T5,
            -100f, -55f, 16f, 0.18f, T2,
            0f, -66f, 20f, 0.3f, T1,
            100f, -55f, 16f, 0.18f, T3,
            -60f, -38f, 20f, 0.3f, T1,
            60f, -38f, 20f, 0.3f, T2,
            -135f, 0, 16f, 0.18f, T4,
            -80f, 0, 20f, 0.3f, T3,
            80f, 0, 20f, 0.3f, T3,
            135f, 0, 16f, 0.18f, T4,
            -60f, 38f, 20f, 0.3f, T2,
            60f, 38f, 20f, 0.3f, T1,
            -100f, 55f, 16f, 0.18f, T2,
            0f, 66f, 20f, 0.3f, T1,
            100f, 55f, 16f, 0.18f, T3,
            -45f, 80f, 16f, 0.18f, T4,
            45f, 80f, 16f, 0.18f, T5
    };

    public static void drawProfilePattern(Canvas canvas, Drawable drawable, float width, float height, float alpha, float full) {
        if (alpha <= 0f) return;

        float baseY = height;
        float left = 0;
        float right = width;

        // Draw left-side stars
        if (full > 0) {
            for (int i = 0; i < PROFILE_LEFT.length; i += 4) {
                float x = dpf2(PROFILE_LEFT[i]);
                float y = dpf2(PROFILE_LEFT[i + 1]);
                float size = dpf2(PROFILE_LEFT[i + 2]) / 2f;
                float localAlpha = PROFILE_LEFT[i + 3];

                drawable.setBounds(
                        (int) (left + x - size), (int) (baseY + y - size),
                        (int) (left + x + size), (int) (baseY + y + size)
                );
                drawable.setAlpha((int) (255 * alpha * localAlpha * full));
                drawable.draw(canvas);
            }

            // Dynamic stripe across width
            float stripeLeft = 77.5f, stripeRight = 173.33f;
            float availableSpace = width / AndroidUtilities.density - stripeLeft - stripeRight;
            int count = Math.max(0, Math.round(availableSpace / 27.25f));
            if (count % 2 == 0) count++;

            for (int i = 0; i < count; i++) {
                float x = dpf2(stripeLeft + availableSpace * i / (count - 1));
                float y = dpf2(i % 2 == 0 ? 0 : -12.5f);
                float size = dpf2(17f) / 2f;

                drawable.setBounds(
                        (int) (left + x - size), (int) (baseY + y - size),
                        (int) (left + x + size), (int) (baseY + y + size)
                );
                drawable.setAlpha((int) (255 * alpha * 0.21f * full));
                drawable.draw(canvas);
            }
        }

        // Draw right-side stars
        for (int i = 0; i < PROFILE_RIGHT.length; i += 4) {
            float x = dpf2(PROFILE_RIGHT[i]);
            float y = dpf2(PROFILE_RIGHT[i + 1]);
            float size = dpf2(PROFILE_RIGHT[i + 2]) / 2f;
            float localAlpha = PROFILE_RIGHT[i + 3];

            drawable.setBounds(
                    (int) (right + x - size), (int) (baseY + y - size),
                    (int) (right + x + size), (int) (baseY + y + size)
            );
            drawable.setAlpha((int) (255 * alpha * localAlpha));
            drawable.draw(canvas);
        }
    }

    public static void drawProfileCenteredPattern(Canvas canvas, Drawable drawable, float width, float height, float alpha) {
        if (alpha <= 0f) return;

        float centerX = width / 2f;
        float centerY = AndroidUtilities.lerp(height / (2f / alpha), -dp(16), 1f - alpha);

        for (int i = 0; i < PROFILE_CENTER.length; i += 5) {
            float x = PROFILE_CENTER[i];
            float y = PROFILE_CENTER[i + 1];
            float size = PROFILE_CENTER[i + 2];
            float thisAlpha = PROFILE_CENTER[i + 3];
            float tolerance = PROFILE_CENTER[i + 4];

            float tInv = 1f - tolerance;
            if (tInv == 0f) tInv = 1f;

            float progress = Math.max(0f, (1f - alpha - tolerance)) / tInv;
            float translatedX = AndroidUtilities.lerp(x, 0f, Math.min(progress / tolerance, 1f));
            float translatedY = AndroidUtilities.lerp(y, 0f, Math.min(progress / tolerance, 1f));

            if (alpha == 0f) {
                translatedX = 0f;
                translatedY = 0f;
            }

            float scaledSize = dpf2(size * alpha) / 2f;
            float dx = dpf2(translatedX);
            float dy = dpf2(translatedY);

            drawable.setBounds(
                    (int) (centerX + dx - scaledSize), (int) (centerY + dy - scaledSize),
                    (int) (centerX + dx + scaledSize), (int) (centerY + dy + scaledSize)
            );
            drawable.setAlpha((int) (255 * alpha * thisAlpha));
            drawable.draw(canvas);
        }
    }

}
