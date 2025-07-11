package org.telegram.ui.profile.utils;

import android.util.SparseIntArray;

import androidx.recyclerview.widget.DiffUtil;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;

import java.util.ArrayList;

public class DiffCallback extends DiffUtil.Callback {

    int oldRowCount;

    private ComponentsFactory componentsFactory;
    private RowsAndStatusComponentsHolder rowsHolder;
    private AttributesComponentsHolder attributesHolder;
    public DiffCallback(ComponentsFactory componentsFactory) {
        this.componentsFactory = componentsFactory;
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
        attributesHolder = componentsFactory.getAttributesComponentsHolder();
    }
    SparseIntArray oldPositionToItem = new SparseIntArray();
    SparseIntArray newPositionToItem = new SparseIntArray();
    ArrayList<TLRPC.ChatParticipant> oldChatParticipant = new ArrayList<>();
    ArrayList<Integer> oldChatParticipantSorted = new ArrayList<>();
    int oldMembersStartRow;
    int oldMembersEndRow;

    @Override
    public int getOldListSize() {
        return oldRowCount;
    }

    @Override
    public int getNewListSize() {
        return componentsFactory.getAttributesComponentsHolder().getRowCount();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (newItemPosition >= rowsHolder.getMembersStartRow() && newItemPosition < rowsHolder.getMembersEndRow()) {
            if (oldItemPosition >= oldMembersStartRow && oldItemPosition < oldMembersEndRow) {
                TLRPC.ChatParticipant oldItem;
                TLRPC.ChatParticipant newItem;
                if (!oldChatParticipantSorted.isEmpty()) {
                    oldItem = oldChatParticipant.get(oldChatParticipantSorted.get(oldItemPosition - oldMembersStartRow));
                } else {
                    oldItem = oldChatParticipant.get(oldItemPosition - oldMembersStartRow);
                }

                if (!attributesHolder.getSortedUsers().isEmpty()) {
                    newItem = attributesHolder.getVisibleChatParticipants().get(attributesHolder.getVisibleSortedUsers().get(newItemPosition - rowsHolder.getMembersStartRow()));
                } else {
                    newItem = attributesHolder.getVisibleChatParticipants().get(newItemPosition - rowsHolder.getMembersStartRow());
                }
                return oldItem.user_id == newItem.user_id;
            }
        }
        int oldIndex = oldPositionToItem.get(oldItemPosition, -1);
        int newIndex = newPositionToItem.get(newItemPosition, -1);
        return oldIndex == newIndex && oldIndex >= 0;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(oldItemPosition, newItemPosition);
    }

    public void fillPositions(SparseIntArray sparseIntArray) {
        sparseIntArray.clear();
        int pointer = 0;
        put(++pointer, rowsHolder.getSetAvatarRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSetAvatarSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getNumberSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getNumberRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSetUsernameRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBioRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPhoneSuggestionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPhoneSuggestionSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPasswordSuggestionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPasswordSuggestionSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getGraceSuggestionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getGraceSuggestionSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSettingsSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSettingsSectionRow2(), sparseIntArray);
        put(++pointer, rowsHolder.getNotificationRow(), sparseIntArray);
        put(++pointer, rowsHolder.getLanguageRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPremiumRow(), sparseIntArray);
        put(++pointer, rowsHolder.getStarsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBusinessRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPremiumSectionsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPremiumGiftingRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPrivacyRow(), sparseIntArray);
        put(++pointer, rowsHolder.getDataRow(), sparseIntArray);
        put(++pointer, rowsHolder.getLiteModeRow(), sparseIntArray);
        put(++pointer, rowsHolder.getChatRow(), sparseIntArray);
        put(++pointer, rowsHolder.getFiltersRow(), sparseIntArray);
        put(++pointer, rowsHolder.getStickersRow(), sparseIntArray);
        put(++pointer, rowsHolder.getDevicesRow(), sparseIntArray);
        put(++pointer, rowsHolder.getDevicesSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getHelpHeaderRow(), sparseIntArray);
        put(++pointer, rowsHolder.getQuestionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getFaqRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPolicyRow(), sparseIntArray);
        put(++pointer, rowsHolder.getHelpSectionCell(), sparseIntArray);
        put(++pointer, rowsHolder.getDebugHeaderRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSendLogsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSendLastLogsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getClearLogsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSwitchBackendRow(), sparseIntArray);
        put(++pointer, rowsHolder.getVersionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getEmptyRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBottomPaddingRow(), sparseIntArray);
        put(++pointer, rowsHolder.getInfoHeaderRow(), sparseIntArray);
        put(++pointer, rowsHolder.getPhoneRow(), sparseIntArray);
        put(++pointer, rowsHolder.getLocationRow(), sparseIntArray);
        put(++pointer, rowsHolder.getUserInfoRow(), sparseIntArray);
        put(++pointer, rowsHolder.getChannelInfoRow(), sparseIntArray);
        put(++pointer, rowsHolder.getUsernameRow(), sparseIntArray);
        put(++pointer, rowsHolder.getNotificationsDividerRow(), sparseIntArray);
        put(++pointer, rowsHolder.getReportDividerRow(), sparseIntArray);
        put(++pointer, rowsHolder.getNotificationsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getInfoSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getAffiliateRow(), sparseIntArray);
        put(++pointer, rowsHolder.getInfoAffiliateRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSendMessageRow(), sparseIntArray);
        put(++pointer, rowsHolder.getReportRow(), sparseIntArray);
        put(++pointer, rowsHolder.getReportReactionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getAddToContactsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSettingsTimerRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSettingsKeyRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSecretSettingsSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getMembersHeaderRow(), sparseIntArray);
        put(++pointer, rowsHolder.getAddMemberRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSubscribersRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSubscribersRequestsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getAdministratorsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSettingsRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBlockedUsersRow(), sparseIntArray);
        put(++pointer, rowsHolder.getMembersSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getChannelBalanceSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getSharedMediaRow(), sparseIntArray);
        put(++pointer, rowsHolder.getUnblockRow(), sparseIntArray);
        put(++pointer, rowsHolder.getAddToGroupButtonRow(), sparseIntArray);
        put(++pointer, rowsHolder.getAddToGroupInfoRow(), sparseIntArray);
        put(++pointer, rowsHolder.getJoinRow(), sparseIntArray);
        put(++pointer, rowsHolder.getLastSectionRow(), sparseIntArray);
        put(++pointer, rowsHolder.getNotificationsSimpleRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBizHoursRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBizLocationRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBirthdayRow(), sparseIntArray);
        put(++pointer, rowsHolder.getChannelRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBotStarsBalanceRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBotTonBalanceRow(), sparseIntArray);
        put(++pointer, rowsHolder.getChannelBalanceRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBalanceDividerRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBotAppRow(), sparseIntArray);
        put(++pointer, rowsHolder.getBotPermissionsHeader(), sparseIntArray);
        put(++pointer, rowsHolder.getBotPermissionLocation(), sparseIntArray);
        put(++pointer, rowsHolder.getBotPermissionEmojiStatus(), sparseIntArray);
        put(++pointer, rowsHolder.getBotPermissionBiometry(), sparseIntArray);
        put(++pointer, rowsHolder.getBotPermissionsDivider(), sparseIntArray);
        put(++pointer, rowsHolder.getChannelDividerRow(), sparseIntArray);
    }

    private void put(int id, int position, SparseIntArray sparseIntArray) {
        if (position >= 0) {
            sparseIntArray.put(position, id);
        }
    }
}