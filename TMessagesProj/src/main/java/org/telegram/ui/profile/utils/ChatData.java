package org.telegram.ui.profile.utils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatData {

    public static void updateOnlineCount(ComponentsFactory components, ProfileActivity profileActivity, boolean notify) {
        ViewComponentsHolder viewsHolder = components.getViewComponentsHolder();
        AttributesComponentsHolder attributesHolder = components.getAttributesComponentsHolder();
        RowsAndStatusComponentsHolder rowsHolder = components.getRowsAndStatusComponentsHolder();
        rowsHolder.setOnlineCount(0);
        int currentTime = profileActivity.getConnectionsManager().getCurrentTime();
        attributesHolder.getSortedUsers().clear();
        if (attributesHolder.getChatInfo() instanceof TLRPC.TL_chatFull || attributesHolder.getChatInfo() instanceof TLRPC.TL_channelFull && attributesHolder.getChatInfo().participants_count <= 200 && attributesHolder.getChatInfo().participants != null) {
            final ArrayList<Integer> sortNum = new ArrayList<>();
            for (int a = 0; a < attributesHolder.getChatInfo().participants.participants.size(); a++) {
                TLRPC.ChatParticipant participant = attributesHolder.getChatInfo().participants.participants.get(a);
                TLRPC.User user = profileActivity.getMessagesController().getUser(participant.user_id);
                if (user != null && user.status != null && (user.status.expires > currentTime || user.id == profileActivity.getUserConfig().getClientUserId()) && user.status.expires > 10000) {
                    rowsHolder.setOnlineCount(rowsHolder.getOnlineCount() + 1);
                }
                attributesHolder.getSortedUsers().add(a);
                int sort = Integer.MIN_VALUE;
                if (user != null) {
                    if (user.bot) {
                        sort = -110;
                    } else if (user.self) {
                        sort = currentTime + 50000;
                    } else if (user.status != null) {
                        sort = user.status.expires;
                    }
                }
                sortNum.add(sort);
            }

            try {
                Collections.sort(attributesHolder.getSortedUsers(), Comparator.comparingInt(hs -> sortNum.get((int) hs)).reversed());
            } catch (Exception e) {
                FileLog.e(e);
            }

            if (notify && components.getListAdapter() != null && rowsHolder.getMembersStartRow() > 0) {
                AndroidUtilities.updateVisibleRows(components.getListView().getClippedList());
            }
            if (viewsHolder.getSharedMediaLayout() != null && rowsHolder.getSharedMediaRow() != -1 && (attributesHolder.getSortedUsers().size() > 5 || rowsHolder.getUsersForceShowingIn() == 2) && rowsHolder.getUsersForceShowingIn() != 1) {
                viewsHolder.getSharedMediaLayout().setChatUsers(attributesHolder.getSortedUsers(), attributesHolder.getChatInfo());
            }
        } else if (attributesHolder.getChatInfo() instanceof TLRPC.TL_channelFull && attributesHolder.getChatInfo().participants_count > 200) {
            rowsHolder.setOnlineCount(attributesHolder.getChatInfo().online_count);
        }
    }

}
