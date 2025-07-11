package org.telegram.ui.profile.view.button;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ButtonView extends LinearLayout {
    ImageView icon;
    TextView label;
    ButtonViewEnum button;

    ButtonView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        icon = new ImageView(context);
        icon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        addView(icon, LayoutHelper.createLinear(28, 28));

        label = new TextView(context);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        label.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        label.setTextColor(Color.WHITE);
        addView(label, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 3, 0, 0));
    }

    ButtonView bind(ButtonViewEnum button, Runnable onClick) {
        this.button = button;
        icon.setImageResource(button.icon);
        label.setText(button.title);
        setBackground(Theme.AdaptiveRipple.createRect(0, Theme.getColor(Theme.key_listSelector), 16));
        setOnClickListener(v -> onClick.run());
        return this;
    }
}
