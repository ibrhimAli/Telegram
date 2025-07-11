package org.telegram.ui.profile.view;

import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.appcompat.widget.AppCompatImageView;

public class BlurImageView  extends AppCompatImageView {


    public BlurImageView(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int width = view.getMeasuredHeight();
                    int height = view.getMeasuredHeight();
                    int radius = (Math.min(width, height) / 2);
                    outline.setOval((width - 2 * radius) / 2, (height - 2 * radius) / 2,
                            (width + 2 * radius) / 2, (height + 2 * radius) / 2);
                }
            });
            this.setClipToOutline(true);
        }
    }
}
