package org.telegram.ui.profile.utils;

import android.text.style.CharacterStyle;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;

public class Loaders {

    public static void setLoadingSpan(ComponentsFactory componentsFactory, RecyclerListView listView, CharacterStyle span) {
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        if (attributesHolder.getLoadingSpan() == span) return;
        attributesHolder.setLoadingSpan(span);
        AndroidUtilities.forEachViews(listView, view -> {
            if (view instanceof TextDetailCell) {
                ((TextDetailCell) view).textView.setLoading(attributesHolder.getLoadingSpan());
                ((TextDetailCell) view).valueTextView.setLoading(attributesHolder.getLoadingSpan());
            }
        });
    }
}
