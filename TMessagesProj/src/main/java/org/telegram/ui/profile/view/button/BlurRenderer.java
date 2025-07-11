package org.telegram.ui.profile.view.button;

import android.graphics.Canvas;
import android.graphics.Path;

public interface BlurRenderer {
    void onSizeChanged(int w, int h);
    void setBlurRadius(float radius);
    void record(RecordDrawer drawer);
    void draw(Canvas canvas, Path path);
    default boolean canBlur() { return true; }
    default void release() {}
}
