package org.telegram.ui.profile.utils;

import android.view.View;
import android.widget.ImageView;

import org.telegram.messenger.ChatObject;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

public class Update {

    public static void updateAutoDeleteItem(ComponentsFactory components) {
        ViewComponentsHolder viewsHolder = components.getViewComponentsHolder();
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        if (viewsHolder.getAutoDeleteItem() == null || viewsHolder.getAutoDeletePopupWrapper() == null) {
            return;
        }
        int ttl = 0;
        if (attributesHolder.getUserInfo() != null || attributesHolder.getChatInfo() != null) {
            ttl = attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().ttl_period : attributesHolder.getChatInfo().ttl_period;
        }
        viewsHolder.getAutoDeleteItemDrawable().setTime(ttl);
        viewsHolder.getAutoDeletePopupWrapper().updateItems(ttl);
    }

    public static void updateTimeItem(ComponentsFactory components) {
        ViewComponentsHolder viewsHolder = components.getViewComponentsHolder();
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        if (viewsHolder.getTimerDrawable() == null) {
            return;
        }
        final boolean fromMonoforum = attributesHolder.getPreviousTransitionFragment() instanceof ChatActivity && ChatObject.isMonoForum(attributesHolder.getPreviousTransitionFragment().getCurrentChat());
        if (fromMonoforum) {
            viewsHolder.getTimeItem().setTag(null);
            viewsHolder.getTimeItem().setVisibility(View.GONE);
        } else if (attributesHolder.getCurrentEncryptedChat() != null) {
            viewsHolder.getTimerDrawable().setTime(attributesHolder.getCurrentEncryptedChat().ttl);
            viewsHolder.getTimeItem().setTag(1);
            viewsHolder.getTimeItem().setVisibility(View.VISIBLE);
        } else if (attributesHolder.getUserInfo() != null) {
            viewsHolder.getTimerDrawable().setTime(attributesHolder.getUserInfo().ttl_period);
            if (rowsHolder.isNeedTimerImage() && attributesHolder.getUserInfo().ttl_period != 0) {
                viewsHolder.getTimeItem().setTag(1);
                viewsHolder.getTimeItem().setVisibility(View.VISIBLE);
            } else {
                viewsHolder.getTimeItem().setTag(null);
                viewsHolder.getTimeItem().setVisibility(View.GONE);
            }
        } else if (attributesHolder.getChatInfo() != null) {
            viewsHolder.getTimerDrawable().setTime(attributesHolder.getChatInfo().ttl_period);
            if (rowsHolder.isNeedTimerImage() && attributesHolder.getChatInfo().ttl_period != 0) {
                viewsHolder.getTimeItem().setTag(1);
                viewsHolder.getTimeItem().setVisibility(View.VISIBLE);
            } else {
                viewsHolder.getTimeItem().setTag(null);
                viewsHolder.getTimeItem().setVisibility(View.GONE);
            }
        } else {
            viewsHolder.getTimeItem().setTag(null);
            viewsHolder.getTimeItem().setVisibility(View.GONE);
        }
    }

    public static void updateStar(ComponentsFactory componentsFactory, ImageView starBgItem, ImageView starFgItem) {
        if (starBgItem == null || starFgItem == null) return;
        AttributesComponentsHolder attributesHolder = componentsFactory.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        if (rowsHolder.isNeedStarImage() && attributesHolder.getCurrentChat() != null && (attributesHolder.getCurrentChat().flags2 & 2048) != 0) {
            starFgItem.setTag(1);
            starFgItem.setVisibility(View.VISIBLE);
            starBgItem.setTag(1);
            starBgItem.setVisibility(View.VISIBLE);
        } else {
            starFgItem.setTag(null);
            starFgItem.setVisibility(View.GONE);
            starBgItem.setTag(null);
            starBgItem.setVisibility(View.GONE);
        }
    }
}
