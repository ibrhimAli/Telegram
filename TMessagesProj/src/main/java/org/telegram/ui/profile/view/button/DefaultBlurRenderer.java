package org.telegram.ui.profile.view.button;

import android.graphics.Canvas;
import android.graphics.Path;

public class DefaultBlurRenderer implements BlurRenderer {
    @Override public void onSizeChanged(int w, int h) {}
    @Override public void setBlurRadius(float radius) {}
    @Override public void record(RecordDrawer drawer) {}
    @Override public void draw(Canvas canvas, Path path) {}
    @Override public boolean canBlur() { return false; }

}
