package org.telegram.ui.profile.utils;

import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.profile.utils.ChatData.updateOnlineCount;

import android.text.TextUtils;

import android.view.Gravity;
import android.view.View;

import androidx.recyclerview.widget.DiffUtil;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Business.OpeningHoursActivity;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.PrivacyControlActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.SetupEmojiStatusSheet;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;
import org.telegram.ui.profile.view.button.ButtonViewEnum;

import java.util.Set;

public class Adapter {

    private final ProfileActivity profileActivity;
    private final OpenAndWrite openAndWrite;
    private ClippedListView listView;
    private final ComponentsFactory components;
    private ViewComponentsHolder viewsHolder;
    private AttributesComponentsHolder attributesHolder;
    private RowsAndStatusComponentsHolder rowsHolder;

    public Adapter(ComponentsFactory componentsFactory) {
        this.components = componentsFactory;
        profileActivity = componentsFactory.getProfileActivity();
        this.openAndWrite = componentsFactory.getOpenAndWrite();
        listView = componentsFactory.getListView();
        viewsHolder = componentsFactory.getViewComponentsHolder();
        attributesHolder = componentsFactory.getAttributesComponentsHolder();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
    }
    public void saveScrollPosition() {
        if (listView == null) {
            listView = components.getListView();
        }
        if (listView != null && viewsHolder.getLayoutManager() != null && listView.getClippedList().getChildCount() > 0 && !rowsHolder.isSavedScrollToSharedMedia()) {
            View view = null;
            int position = -1;
            int top = Integer.MAX_VALUE;
            for (int i = 0; i < listView.getClippedList().getChildCount(); i++) {
                int childPosition = listView.getClippedList().getChildAdapterPosition(listView.getClippedList().getChildAt(i));
                View child = listView.getClippedList().getChildAt(i);
                if (childPosition != RecyclerListView.NO_POSITION && child.getTop() < top) {
                    view = child;
                    position = childPosition;
                    top = child.getTop();
                }
            }
            if (view != null) {
                rowsHolder.setSavedScrollPosition(position);
                rowsHolder.setSavedScrollOffset(view.getTop());
                if (rowsHolder.getSavedScrollPosition() == 0 && !rowsHolder.isAllowPullingDown() && rowsHolder.getSavedScrollOffset() > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) {
                    rowsHolder.setSavedScrollOffset(AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT));
                }

                viewsHolder.getLayoutManager().scrollToPositionWithOffset(position, view.getTop() - listView.getClippedList().getPaddingTop());
            }
        }
    }


    public void updateListAnimated(boolean updateOnlineCount) {
        updateListAnimated(updateOnlineCount, false);
    }

    private void updateListAnimated(boolean updateOnlineCount, boolean triedInLayout) {
        if (listView == null) {
            listView = components.getListView();
        }

        if (components.getListAdapter() == null || listView == null || listView.getClippedList() == null) {
            if (updateOnlineCount) {
                updateOnlineCount(components, profileActivity, false);
            }
            updateRowsIds();
            return;
        }

        if (!triedInLayout && listView.getClippedList().isInLayout()) {
            if (!listView.getClippedList().isAttachedToWindow()) return;
            listView.getClippedList().post(() -> updateListAnimated(updateOnlineCount, true));
            return;
        }

        DiffCallback diffCallback = new DiffCallback(components);
        diffCallback.oldRowCount = attributesHolder.getRowCount();
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        diffCallback.oldChatParticipant.clear();
        diffCallback.oldChatParticipantSorted.clear();
        diffCallback.oldChatParticipant.addAll(attributesHolder.getVisibleChatParticipants());
        diffCallback.oldChatParticipantSorted.addAll(attributesHolder.getVisibleSortedUsers());
        diffCallback.oldMembersStartRow = rowsHolder.getMembersStartRow();
        diffCallback.oldMembersEndRow = rowsHolder.getMembersEndRow();
        if (updateOnlineCount) {
            updateOnlineCount(components, profileActivity, false);
        }
        saveScrollPosition();
        updateRowsIds();
        diffCallback.fillPositions(diffCallback.newPositionToItem);
        try {
            DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(components.getListAdapter());
        } catch (Exception e) {
            FileLog.e(e);
            components.getListAdapter().notifyDataSetChanged();
        }
        if (rowsHolder.getSavedScrollPosition() >= 0) {
            viewsHolder.getLayoutManager().scrollToPositionWithOffset(rowsHolder.getSavedScrollPosition(), rowsHolder.getSavedScrollOffset() - listView.getClippedList().getPaddingTop());
        }
        AndroidUtilities.updateVisibleRows(listView.getClippedList());
    }

    public boolean editRow(View view, int position) {
        if (listView == null) {
            listView = components.getListView();
        }
        if (!attributesHolder.isMyProfile()) return false;

        if (view instanceof ProfileChannelCell) {
            view = ((ProfileChannelCell) view).dialogCell;
        }

        TLRPC.User user = profileActivity.getUserConfig().getCurrentUser();
        if (user == null) return false;
        TLRPC.UserFull userFull = attributesHolder.getUserInfo() == null ? profileActivity.getMessagesController().getUserFull(user.id) : attributesHolder.getUserInfo();
        if (userFull == null) return false;

        String copyButton = getString(R.string.Copy);
        String textToCopy = null;
        if (position == rowsHolder.getChannelInfoRow() || position == rowsHolder.getUserInfoRow() || position == rowsHolder.getBioRow()) {
            textToCopy = userFull.about;
        } else if (position == rowsHolder.getBizHoursRow()) {
            textToCopy = OpeningHoursActivity.toString(UserConfig.selectedAccount, user, userFull.business_work_hours);
            copyButton = getString(R.string.ProfileHoursCopy);
        } else if (position == rowsHolder.getBizLocationRow()) {
            textToCopy = userFull.business_location.address;
            copyButton = getString(R.string.ProfileLocationCopy);
        } else if (position == rowsHolder.getUsernameRow()) {
            textToCopy = UserObject.getPublicUsername(user);
            if (textToCopy != null) textToCopy = "@" + textToCopy;
            copyButton = getString(R.string.ProfileCopyUsername);
        } else if (position == rowsHolder.getPhoneRow()) {
            textToCopy = user.phone;
        } else if (position == rowsHolder.getBirthdayRow()) {
            textToCopy = UserInfoActivity.birthdayString(attributesHolder.getUserInfo().birthday);
        }

        ItemOptions itemOptions = ItemOptions.makeOptions(viewsHolder.getContentView(), components.getAttributesComponentsHolder().getResourcesProvider(), view);
        itemOptions.setGravity(Gravity.LEFT);

        if (position == rowsHolder.getBizLocationRow() && userFull.business_location != null) {
            if (userFull.business_location.geo_point != null) {
                itemOptions.add(R.drawable.msg_view_file, getString(R.string.ProfileLocationView), () -> {
                    openAndWrite.openLocation(false);
                });
            }
            itemOptions.add(R.drawable.msg_map, getString(R.string.ProfileLocationMaps), () -> {
                openAndWrite.openLocation(true);
            });
        }

        if (textToCopy != null) {
            final String text = textToCopy;
            itemOptions.add(R.drawable.msg_copy, copyButton, () -> {
                AndroidUtilities.addToClipboard(text);
            });
        }

        if (position == rowsHolder.getBizHoursRow()) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileHoursEdit), () -> {
                profileActivity.presentFragment(new OpeningHoursActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.ProfileHoursRemove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BusinessHoursClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BusinessHoursClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBusinessWorkHours req = new TL_account.updateBusinessWorkHours();
                    if (userFull != null) {
                        userFull.business_work_hours = null;
                        userFull.flags2 &=~ 1;
                    }
                    profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(profileActivity).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateRowsIds();
                    components.getListAdapter().notifyItemRemoved(position);
                    profileActivity.getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                profileActivity.showDialog(builder.create());
            });
        } else if (position == rowsHolder.getBizLocationRow()) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileLocationEdit), () -> {
                profileActivity.presentFragment(new org.telegram.ui.Business.LocationActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.ProfileLocationRemove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BusinessLocationClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BusinessLocationClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBusinessLocation req = new TL_account.updateBusinessLocation();
                    if (userFull != null) {
                        userFull.business_location = null;
                        userFull.flags2 &=~ 2;
                    }
                    profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(profileActivity).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateRowsIds();
                    components.getListAdapter().notifyItemRemoved(position);
                    profileActivity.getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                profileActivity.showDialog(builder.create());
            });
        } else if (position == rowsHolder.getUsernameRow()) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileUsernameEdit), () -> {
                profileActivity.presentFragment(new ChangeUsernameActivity());
            });
        } else if (position == rowsHolder.getChannelInfoRow() || position == rowsHolder.getUserInfoRow() || position == rowsHolder.getBioRow()) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileEditBio), () -> {
                profileActivity.presentFragment(new UserInfoActivity());
            });
        } else if (position == rowsHolder.getPhoneRow()) {
            itemOptions.add(R.drawable.menu_storage_path, getString(R.string.ProfilePhoneEdit), () -> {
                profileActivity.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
            });
        } else if (position == rowsHolder.getBirthdayRow()) {
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileBirthdayChange), () -> {
                profileActivity.showDialog(AlertsCreator.createBirthdayPickerDialog(profileActivity.getContext(), getString(R.string.EditProfileBirthdayTitle), getString(R.string.EditProfileBirthdayButton), userFull.birthday, birthday -> {
                    TL_account.updateBirthday req = new TL_account.updateBirthday();
                    req.flags |= 1;
                    req.birthday = birthday;
                    TL_account.TL_birthday oldBirthday = userFull != null ? userFull.birthday : null;
                    if (userFull != null) {
                        userFull.flags2 |= 32;
                        userFull.birthday = birthday;
                    }
                    profileActivity.getMessagesController().invalidateContentSettings();
                    profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TLRPC.TL_boolTrue) {
                            BulletinFactory.of(profileActivity)
                                    .createSimpleBulletin(R.raw.contact_check, LocaleController.getString(R.string.PrivacyBirthdaySetDone))
                                    .setDuration(Bulletin.DURATION_PROLONG).show();
                        } else {
                            if (userFull != null) {
                                if (oldBirthday == null) {
                                    userFull.flags2 &=~ 32;
                                } else {
                                    userFull.flags2 |= 32;
                                }
                                userFull.birthday = oldBirthday;
                                profileActivity.getMessagesStorage().updateUserInfo(userFull, false);
                            }
                            if (err != null && err.text != null && err.text.startsWith("FLOOD_WAIT_")) {
                                if (profileActivity.getContext() != null) {
                                    profileActivity.showDialog(
                                            new AlertDialog.Builder(profileActivity.getContext(), profileActivity.getResourceProvider())
                                                    .setTitle(getString(R.string.PrivacyBirthdayTooOftenTitle))
                                                    .setMessage(getString(R.string.PrivacyBirthdayTooOftenMessage))
                                                    .setPositiveButton(getString(R.string.OK), null)
                                                    .create()
                                    );
                                }
                            } else {
                                BulletinFactory.of(profileActivity)
                                        .createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.UnknownError))
                                        .show();
                            }
                        }
                    }), ConnectionsManager.RequestFlagDoNotWaitFloodWait);
                }, () -> {
                    BaseFragment.BottomSheetParams params = new BaseFragment.BottomSheetParams();
                    params.transitionFromLeft = true;
                    params.allowNestedScroll = false;
                    profileActivity.showAsSheet(new PrivacyControlActivity(PrivacyControlActivity.PRIVACY_RULES_TYPE_BIRTHDAY), params);
                }, profileActivity.getResourceProvider()).create());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.Remove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.BirthdayClearTitle));
                builder.setMessage(LocaleController.getString(R.string.BirthdayClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updateBirthday req = new TL_account.updateBirthday();
                    if (userFull != null) {
                        userFull.birthday = null;
                        userFull.flags2 &=~ 32;
                    }
                    profileActivity.getMessagesController().invalidateContentSettings();
                    profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(profileActivity).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateListAnimated(false);
                    profileActivity.getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                profileActivity.showDialog(builder.create());
            });
        } else if (position == rowsHolder.getChannelRow()) {
            TLRPC.Chat channel = profileActivity.getMessagesController().getChat(userFull.personal_channel_id);
            if (channel != null && ChatObject.getPublicUsername(channel) != null) {
                itemOptions.add(R.drawable.msg_copy, getString(R.string.ProfileChannelCopy), () -> {
                    AndroidUtilities.addToClipboard("https://" + profileActivity.getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(channel));
                });
            }
            itemOptions.add(R.drawable.msg_edit, getString(R.string.ProfileChannelChange), () -> {
                profileActivity.presentFragment(new UserInfoActivity());
            });
            itemOptions.add(R.drawable.msg_delete, getString(R.string.Remove), true, () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.ProfileChannelClearTitle));
                builder.setMessage(LocaleController.getString(R.string.ProfileChannelClearMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Remove), (di, w) -> {
                    TL_account.updatePersonalChannel req = new TL_account.updatePersonalChannel();
                    req.channel = new TLRPC.TL_inputChannelEmpty();
                    if (userFull != null) {
                        userFull.personal_channel_id = 0;
                        userFull.personal_channel_message = 0;
                        userFull.flags2 &=~ 64;
                    }
                    profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (err != null) {
                            BulletinFactory.showError(err);
                        } else if (res instanceof TLRPC.TL_boolFalse) {
                            BulletinFactory.of(profileActivity).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                        }
                    }));
                    updateListAnimated(false);
                    profileActivity.getMessagesStorage().updateUserInfo(userFull, false);
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                profileActivity.showDialog(builder.create());
            });
        }

        if (itemOptions.getItemsCount() <= 0) {
            return false;
        }

        itemOptions.show();

        return true;
    }
    public void updateRowsIds() {
        validateViewHolder();
        validateAttributesHolder();
        validateRowsHolder();
        int prevRowsCount = attributesHolder.getRowCount();
        attributesHolder.setRowCount(0);

        rowsHolder.setSetAvatarRow(-1);
        rowsHolder.setSetAvatarSectionRow(-1);
        rowsHolder.setNumberSectionRow(-1);
        rowsHolder.setNumberRow(-1);
        rowsHolder.setBirthdayRow(-1);
        rowsHolder.setSetUsernameRow(-1);
        rowsHolder.setBioRow(-1);
        rowsHolder.setChannelRow(-1);
        rowsHolder.setChannelDividerRow(-1);
        rowsHolder.setPhoneSuggestionSectionRow(-1);
        rowsHolder.setPhoneSuggestionRow(-1);
        rowsHolder.setPasswordSuggestionSectionRow(-1);
        rowsHolder.setGraceSuggestionRow(-1);
        rowsHolder.setGraceSuggestionSectionRow(-1);
        rowsHolder.setPasswordSuggestionRow(-1);
        rowsHolder.setSettingsSectionRow(-1);
        rowsHolder.setSettingsSectionRow2(-1);
        rowsHolder.setNotificationRow(-1);
        rowsHolder.setLanguageRow(-1);
        rowsHolder.setPremiumRow(-1);
        rowsHolder.setStarsRow(-1);
        rowsHolder.setBusinessRow(-1);
        rowsHolder.setPremiumGiftingRow(-1);
        rowsHolder.setPremiumSectionsRow(-1);
        rowsHolder.setPrivacyRow(-1);
        rowsHolder.setDataRow(-1);
        rowsHolder.setChatRow(-1);
        rowsHolder.setFiltersRow(-1);
        rowsHolder.setLiteModeRow(-1);
        rowsHolder.setStickersRow(-1);
        rowsHolder.setDevicesRow(-1);
        rowsHolder.setDevicesSectionRow(-1);
        rowsHolder.setHelpHeaderRow(-1);
        rowsHolder.setQuestionRow(-1);
        rowsHolder.setFaqRow(-1);
        rowsHolder.setPolicyRow(-1);
        rowsHolder.setHelpSectionCell(-1);
        rowsHolder.setDebugHeaderRow(-1);
        rowsHolder.setSendLogsRow(-1);
        rowsHolder.setSendLastLogsRow(-1);
        rowsHolder.setClearLogsRow(-1);
        rowsHolder.setSwitchBackendRow(-1);
        rowsHolder.setVersionRow(-1);
        rowsHolder.setBotAppRow(-1);
        rowsHolder.setBotPermissionsHeader(-1);
        rowsHolder.setBotPermissionBiometry(-1);
        rowsHolder.setBotPermissionEmojiStatus(-1);
        rowsHolder.setBotPermissionLocation(-1);
        rowsHolder.setBotPermissionsDivider(-1);

        rowsHolder.setSendMessageRow(-1);
        rowsHolder.setReportRow(-1);
        rowsHolder.setReportReactionRow(-1);
        rowsHolder.setAddToContactsRow(-1);
        rowsHolder.setEmptyRow(-1);
        rowsHolder.setInfoHeaderRow(-1);
        rowsHolder.setPhoneRow(-1);
        rowsHolder.setUserInfoRow(-1);
        rowsHolder.setLocationRow(-1);
        rowsHolder.setChannelInfoRow(-1);
        rowsHolder.setUsernameRow(-1);
        rowsHolder.setSettingsTimerRow(-1);
        rowsHolder.setSettingsKeyRow(-1);
        rowsHolder.setNotificationsDividerRow(-1);
        rowsHolder.setReportDividerRow(-1);
        rowsHolder.setNotificationsRow(-1);
        rowsHolder.setBizLocationRow(-1);
        rowsHolder.setBizHoursRow(-1);
        rowsHolder.setInfoSectionRow(-1);
        rowsHolder.setAffiliateRow(-1);
        rowsHolder.setInfoAffiliateRow(-1);
        rowsHolder.setSecretSettingsSectionRow(-1);
        rowsHolder.setBottomPaddingRow(-1);
        rowsHolder.setAddToGroupButtonRow(-1);
        rowsHolder.setAddToGroupInfoRow(-1);
        rowsHolder.setInfoStartRow(-1);
        rowsHolder.setInfoEndRow(-1);

        rowsHolder.setMembersHeaderRow(-1);
        rowsHolder.setMembersStartRow(-1);
        rowsHolder.setMembersEndRow(-1);
        rowsHolder.setAddMemberRow(-1);
        rowsHolder.setSubscribersRow(-1);
        rowsHolder.setSubscribersRequestsRow(-1);
        rowsHolder.setAdministratorsRow(-1);
        rowsHolder.setBlockedUsersRow(-1);
        rowsHolder.setMembersSectionRow(-1);
        rowsHolder.setChannelBalanceSectionRow(-1);
        rowsHolder.setSharedMediaRow(-1);
        rowsHolder.setNotificationsSimpleRow(-1);
        rowsHolder.setSettingsRow(-1);
        rowsHolder.setBotStarsBalanceRow(-1);
        rowsHolder.setBotTonBalanceRow(-1);
        rowsHolder.setChannelBalanceRow(-1);
        rowsHolder.setBalanceDividerRow(-1);

        rowsHolder.setUnblockRow(-1);
        rowsHolder.setJoinRow(-1);
        rowsHolder.setLastSectionRow(-1);
        attributesHolder.getVisibleChatParticipants().clear();
        attributesHolder.getVisibleSortedUsers().clear();
        
        boolean hasMedia = false;
        if (attributesHolder.getSharedMediaPreloader() != null) {
            int[] lastMediaCount = attributesHolder.getSharedMediaPreloader().getLastMediaCount();
            for (int a = 0; a < lastMediaCount.length; a++) {
                if (lastMediaCount[a] > 0) {
                    hasMedia = true;
                    break;
                }
            }
            if (!hasMedia) {
                hasMedia = attributesHolder.getSharedMediaPreloader().hasSavedMessages;
            }
            if (!hasMedia) {
                hasMedia = attributesHolder.getSharedMediaPreloader().hasPreviews;
            }
        }
        if (!hasMedia && attributesHolder.getUserInfo() != null) {
            hasMedia = attributesHolder.getUserInfo().stories_pinned_available;
        }
        if (!hasMedia && attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().bot_info != null) {
            hasMedia = attributesHolder.getUserInfo().bot_info.has_preview_medias;
        }
        if (!hasMedia && (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().stargifts_count > 0 || attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().stargifts_count > 0)) {
            hasMedia = true;
        }
        if (!hasMedia && attributesHolder.getChatInfo() != null) {
            hasMedia = attributesHolder.getChatInfo().stories_pinned_available;
        }
        if (!hasMedia) {
            if (attributesHolder.getChatId() != 0 && MessagesController.ChannelRecommendations.hasRecommendations(UserConfig.selectedAccount, -attributesHolder.getChatId())) {
                hasMedia = true;
            } else if (rowsHolder.isBot() && attributesHolder.getUserId() != 0 && MessagesController.ChannelRecommendations.hasRecommendations(UserConfig.selectedAccount, attributesHolder.getUserId())) {
                hasMedia = true;
            }
        }

        if (attributesHolder.getUserId() != 0) {
            if (LocaleController.isRTL) {
                rowsHolder.setEmptyRow(attributesHolder.getRowCountAndIncrement());
            }
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());

            if (UserObject.isUserSelf(user) && !attributesHolder.isMyProfile()) {
                if (attributesHolder.getAvatarBig() == null && (user.photo == null || !(user.photo.photo_big instanceof TLRPC.TL_fileLocation_layer97) && !(user.photo.photo_big instanceof TLRPC.TL_fileLocationToBeDeprecated)) && (attributesHolder.getAvatarsViewPager() == null || attributesHolder.getAvatarsViewPager().getRealCount() == 0)) {
                    rowsHolder.setSetAvatarRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setSetAvatarSectionRow(attributesHolder.getRowCountAndIncrement());
                } 
                rowsHolder.setNumberSectionRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setNumberRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setSetUsernameRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setBioRow(attributesHolder.getRowCountAndIncrement());

                rowsHolder.setSettingsSectionRow(attributesHolder.getRowCountAndIncrement());

                Set<String> suggestions = profileActivity.getMessagesController().pendingSuggestions;
                if (suggestions.contains("PREMIUM_GRACE")) {
                    rowsHolder.setGraceSuggestionRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setGraceSuggestionSectionRow(attributesHolder.getRowCountAndIncrement());
                } else if (suggestions.contains("VALIDATE_PHONE_NUMBER")) {
                    rowsHolder.setPhoneSuggestionRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setPhoneSuggestionSectionRow(attributesHolder.getRowCountAndIncrement());
                } else if (suggestions.contains("VALIDATE_PASSWORD")) {
                    rowsHolder.setPasswordSuggestionRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setPasswordSuggestionSectionRow(attributesHolder.getRowCountAndIncrement());
                }

                rowsHolder.setSettingsSectionRow2(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setChatRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setPrivacyRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setNotificationRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setDataRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setLiteModeRow(attributesHolder.getRowCountAndIncrement());
//                rowsHolder.setStickersRow(attributesHolder.getRowCountAndIncrement());
                if (profileActivity.getMessagesController().filtersEnabled || !profileActivity.getMessagesController().dialogFilters.isEmpty()) {
                    rowsHolder.setFiltersRow(attributesHolder.getRowCountAndIncrement());
                }
                rowsHolder.setDevicesRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setLanguageRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setDevicesSectionRow(attributesHolder.getRowCountAndIncrement());
                if (!profileActivity.getMessagesController().premiumFeaturesBlocked()) {
                    rowsHolder.setPremiumRow(attributesHolder.getRowCountAndIncrement());
                }
                if (profileActivity.getMessagesController().starsPurchaseAvailable()) {
                    rowsHolder.setStarsRow(attributesHolder.getRowCountAndIncrement());
                }
                if (!profileActivity.getMessagesController().premiumFeaturesBlocked()) {
                    rowsHolder.setBusinessRow(attributesHolder.getRowCountAndIncrement());
                }
                if (!profileActivity.getMessagesController().premiumPurchaseBlocked()) {
                    rowsHolder.setPremiumGiftingRow(attributesHolder.getRowCountAndIncrement());
                }
                if (rowsHolder.getPremiumRow() >= 0 || rowsHolder.getStarsRow() >= 0 || rowsHolder.getBusinessRow() >= 0 || rowsHolder.getPremiumGiftingRow() >= 0) {
                    rowsHolder.setPremiumSectionsRow(attributesHolder.getRowCountAndIncrement());
                }
                rowsHolder.setHelpHeaderRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setQuestionRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setFaqRow(attributesHolder.getRowCountAndIncrement());
                rowsHolder.setPolicyRow(attributesHolder.getRowCountAndIncrement());
                if (BuildVars.LOGS_ENABLED || BuildVars.DEBUG_PRIVATE_VERSION) {
                    rowsHolder.setHelpSectionCell(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setDebugHeaderRow(attributesHolder.getRowCountAndIncrement());
                }
                if (BuildVars.LOGS_ENABLED) {
                    rowsHolder.setSendLogsRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setSendLastLogsRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setClearLogsRow(attributesHolder.getRowCountAndIncrement());
                }
                if (BuildVars.DEBUG_VERSION) {
                    rowsHolder.setSwitchBackendRow(attributesHolder.getRowCountAndIncrement());
                }
                rowsHolder.setVersionRow(attributesHolder.getRowCountAndIncrement());
            } else {
                String username = UserObject.getPublicUsername(user);
                boolean hasInfo = attributesHolder.getUserInfo() != null && !TextUtils.isEmpty(attributesHolder.getUserInfo().about) || user != null && !TextUtils.isEmpty(username);
                boolean hasPhone = user != null && (!TextUtils.isEmpty(user.phone) || !TextUtils.isEmpty(rowsHolder.getVcardPhone()));

                if (attributesHolder.getUserInfo() != null && (attributesHolder.getUserInfo().flags2 & 64) != 0 && (viewsHolder.getProfileChannelMessageFetcher() == null || !viewsHolder.getProfileChannelMessageFetcher().loaded || viewsHolder.getProfileChannelMessageFetcher().messageObject != null)) {
                    final TLRPC.Chat channel = profileActivity.getMessagesController().getChat(attributesHolder.getUserInfo().personal_channel_id);
                    if (channel != null && (ChatObject.isPublic(channel) || !ChatObject.isNotInChat(channel))) {
                        rowsHolder.setChannelRow(attributesHolder.getRowCountAndIncrement());
                        rowsHolder.setChannelDividerRow(attributesHolder.getRowCountAndIncrement());
                    }
                }
                rowsHolder.setInfoStartRow(attributesHolder.getRowCount());
                rowsHolder.setInfoHeaderRow(attributesHolder.getRowCountAndIncrement());
                if (!rowsHolder.isBot() && (hasPhone || !hasInfo)) {
                    rowsHolder.setPhoneRow(attributesHolder.getRowCountAndIncrement());
                }
                if (attributesHolder.getUserInfo() != null && !TextUtils.isEmpty(attributesHolder.getUserInfo().about)) {
                    rowsHolder.setUserInfoRow(attributesHolder.getRowCountAndIncrement());
                }
                if (user != null && username != null) {
                    rowsHolder.setUsernameRow(attributesHolder.getRowCountAndIncrement());
                }
                if (attributesHolder.getUserInfo() != null) {
                    if (attributesHolder.getUserInfo().birthday != null) {
                        rowsHolder.setBirthdayRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (attributesHolder.getUserInfo().business_work_hours != null) {
                        rowsHolder.setBizHoursRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (attributesHolder.getUserInfo().business_location != null) {
                        rowsHolder.setBizLocationRow(attributesHolder.getRowCountAndIncrement());
                    }
                }
//                if (rowsHolder.getPhoneRow() != -1 || rowsHolder.getUserInfoRow() != -1 || rowsHolder.getUsernameRow() != -1 || rowsHolder.getBizHoursRow() != -1 || rowsHolder.getBizLocationRow() != -1) {
//                    ProfileParams.notificationsDividerRow = attributesHolder.getRowCountAndIncrement();
//                }
                if (attributesHolder.getUserId() != profileActivity.getUserConfig().getClientUserId()) {
                    rowsHolder.setNotificationsRow(attributesHolder.getRowCountAndIncrement());
                }
                if (rowsHolder.isBot() && user != null && user.bot_has_main_app) {
                    rowsHolder.setBotAppRow(attributesHolder.getRowCountAndIncrement());
                }
                rowsHolder.setInfoEndRow(attributesHolder.getRowCount() - 1);
                rowsHolder.setInfoSectionRow(attributesHolder.getRowCountAndIncrement());

                if (rowsHolder.isBot() && attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().starref_program != null && (attributesHolder.getUserInfo().starref_program.flags & 2) == 0 && profileActivity.getMessagesController().starrefConnectAllowed) {
                    rowsHolder.setAffiliateRow(attributesHolder.getRowCountAndIncrement());;
                    rowsHolder.setInfoAffiliateRow(attributesHolder.getRowCountAndIncrement());
                }

                if (rowsHolder.isBot()) {
                    if (viewsHolder.getBotLocation() == null && profileActivity.getContext() != null) viewsHolder.setBotLocation(viewsHolder.getBotLocation().get(profileActivity.getContext(), UserConfig.selectedAccount, attributesHolder.getUserId()));
                    if (viewsHolder.getBotBiometry() == null && profileActivity.getContext() != null) viewsHolder.setBotBiometry(viewsHolder.getBotBiometry().get(profileActivity.getContext(), UserConfig.selectedAccount, attributesHolder.getUserId()));
                    final boolean containsPermissionLocation = viewsHolder.getBotLocation() != null && viewsHolder.getBotLocation().asked();
                    final boolean containsPermissionBiometry = viewsHolder.getBotBiometry() != null && viewsHolder.getBotBiometry().asked();
                    final boolean containsPermissionEmojiStatus = attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().bot_can_manage_emoji_status || SetupEmojiStatusSheet.getAccessRequested(profileActivity.getContext(), UserConfig.selectedAccount, attributesHolder.getUserId());

                    if (containsPermissionEmojiStatus || containsPermissionLocation || containsPermissionBiometry) {
                        rowsHolder.setBotPermissionsHeader(attributesHolder.getRowCountAndIncrement());
                        if (containsPermissionEmojiStatus) {
                            rowsHolder.setBotPermissionEmojiStatus(attributesHolder.getRowCountAndIncrement());
                        }
                        if (containsPermissionLocation) {
                            rowsHolder.setBotPermissionLocation(attributesHolder.getRowCountAndIncrement());
                        }
                        if (containsPermissionBiometry) {
                            rowsHolder.setBotPermissionBiometry(attributesHolder.getRowCountAndIncrement());
                        }
                        rowsHolder.setBotPermissionsDivider(attributesHolder.getRowCountAndIncrement());
                    }
                }

                if (attributesHolder.getCurrentEncryptedChat() instanceof TLRPC.TL_encryptedChat) {
                    rowsHolder.setSettingsTimerRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setSettingsKeyRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setSecretSettingsSectionRow(attributesHolder.getRowCountAndIncrement());
                }

                if (user != null && !rowsHolder.isBot() && attributesHolder.getCurrentEncryptedChat() == null && user.id != profileActivity.getUserConfig().getClientUserId()) {
                    if (rowsHolder.isUserBlocked()) {
                        rowsHolder.setUnblockRow(attributesHolder.getRowCountAndIncrement());
                        rowsHolder.setLastSectionRow(attributesHolder.getRowCountAndIncrement());
                    }
                }


                boolean divider = false;
                if (user != null && user.bot) {
                    if (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().can_view_revenue && BotStarsController.getInstance(UserConfig.selectedAccount).getTONBalance(attributesHolder.getUserId()) > 0) {
                        rowsHolder.setBotTonBalanceRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (BotStarsController.getInstance(UserConfig.selectedAccount).getBotStarsBalance(attributesHolder.getUserId()).amount > 0 || BotStarsController.getInstance(UserConfig.selectedAccount).hasTransactions(attributesHolder.getUserId())) {
                        rowsHolder.setBotStarsBalanceRow(attributesHolder.getRowCountAndIncrement());
                    }
                }

                if (user != null && rowsHolder.isBot() && !user.bot_nochats) {
                    rowsHolder.setAddToGroupButtonRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setAddToGroupInfoRow(attributesHolder.getRowCountAndIncrement());
                } else if (rowsHolder.getBotStarsBalanceRow() >= 0) {
                    divider = true;
                }

                if (!attributesHolder.isMyProfile() && rowsHolder.isShowAddToContacts() && user != null && !user.contact && !user.bot && !UserObject.isService(user.id)) {
                    rowsHolder.setAddToContactsRow(attributesHolder.getRowCountAndIncrement());
                    divider = true;
                }
                if (!attributesHolder.isMyProfile() && rowsHolder.getReportReactionMessageId() != 0 && !ContactsController.getInstance(UserConfig.selectedAccount).isContact(attributesHolder.getUserId())) {
                    rowsHolder.setReportReactionRow(attributesHolder.getRowCountAndIncrement());
                    divider = true;
                }
                if (divider) {
                    rowsHolder.setReportDividerRow(attributesHolder.getRowCountAndIncrement());
                }

                if (hasMedia || (user != null && user.bot && user.bot_can_edit) || attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().common_chats_count != 0 || attributesHolder.isMyProfile()) {
                    rowsHolder.setSharedMediaRow(attributesHolder.getRowCountAndIncrement());
                } else if (rowsHolder.getLastSectionRow() == -1 && rowsHolder.isNeedSendMessage()) {
                    rowsHolder.setSendMessageRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setReportRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setLastSectionRow(attributesHolder.getRowCountAndIncrement());
                }
            }
        } else if (rowsHolder.isTopic()) {
            rowsHolder.setInfoHeaderRow(attributesHolder.getRowCountAndIncrement());
            rowsHolder.setUsernameRow(attributesHolder.getRowCountAndIncrement());
            rowsHolder.setNotificationsSimpleRow(attributesHolder.getRowCountAndIncrement());
            rowsHolder.setInfoSectionRow(attributesHolder.getRowCountAndIncrement());
            if (hasMedia) {
                rowsHolder.setSharedMediaRow(attributesHolder.getRowCountAndIncrement());
            }
        } else if (attributesHolder.getChatId() != 0) {
            if (attributesHolder.getChatInfo() != null && (!TextUtils.isEmpty(attributesHolder.getChatInfo().about) || attributesHolder.getChatInfo().location instanceof TLRPC.TL_channelLocation) || ChatObject.isPublic(attributesHolder.getCurrentChat())) {
                if (LocaleController.isRTL && ChatObject.isChannel(attributesHolder.getCurrentChat()) && attributesHolder.getChatInfo() != null && !attributesHolder.getCurrentChat().megagroup && attributesHolder.getChatInfo().linked_chat_id != 0) {
                    rowsHolder.setEmptyRow(attributesHolder.getRowCountAndIncrement());
                }
                rowsHolder.setInfoHeaderRow(attributesHolder.getRowCountAndIncrement());
                if (attributesHolder.getChatInfo() != null) {
                    if (!TextUtils.isEmpty(attributesHolder.getChatInfo().about)) {
                        rowsHolder.setChannelInfoRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (attributesHolder.getChatInfo().location instanceof TLRPC.TL_channelLocation) {
                        rowsHolder.setLocationRow(attributesHolder.getRowCountAndIncrement());
                    }
                }
                if (ChatObject.isPublic(attributesHolder.getCurrentChat())) {
                    rowsHolder.setUsernameRow(attributesHolder.getRowCountAndIncrement());
                }
            }
//            if (ProfileParams.infoHeaderRow != -1) {
//                ProfileParams.notificationsDividerRow = attributesHolder.getRowCountAndIncrement();
//            }
            rowsHolder.setNotificationsRow(attributesHolder.getRowCountAndIncrement());
            rowsHolder.setInfoSectionRow(attributesHolder.getRowCountAndIncrement());

            if (ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup) {
                if (attributesHolder.getChatInfo() != null && (attributesHolder.getCurrentChat().creator || attributesHolder.getChatInfo().can_view_participants)) {
                    rowsHolder.setMembersHeaderRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setSubscribersRow(attributesHolder.getRowCountAndIncrement());
                    if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().requests_pending > 0) {
                        rowsHolder.setSubscribersRequestsRow(attributesHolder.getRowCountAndIncrement());
                    }
                    rowsHolder.setAdministratorsRow(attributesHolder.getRowCountAndIncrement());
                    if (attributesHolder.getChatInfo() != null && (attributesHolder.getChatInfo().banned_count != 0 || attributesHolder.getChatInfo().kicked_count != 0)) {
                        rowsHolder.setBlockedUsersRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (
                            attributesHolder.getChatInfo() != null &&
                                    attributesHolder.getChatInfo().can_view_stars_revenue && (
                                    BotStarsController.getInstance(UserConfig.selectedAccount).getBotStarsBalance(-attributesHolder.getChatId()).amount > 0 ||
                                            BotStarsController.getInstance(UserConfig.selectedAccount).hasTransactions(-attributesHolder.getChatId())
                            ) ||
                                    attributesHolder.getChatInfo() != null &&
                                            attributesHolder.getChatInfo().can_view_revenue &&
                                            BotStarsController.getInstance(UserConfig.selectedAccount).getTONBalance(-attributesHolder.getChatId()) > 0
                    ) {
                        rowsHolder.setChannelBalanceRow(attributesHolder.getRowCountAndIncrement());
                    }
                    rowsHolder.setSettingsRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setChannelBalanceSectionRow(attributesHolder.getRowCountAndIncrement());
                }
            } else {
                if (
                        attributesHolder.getChatInfo() != null &&
                                attributesHolder.getChatInfo().can_view_stars_revenue && (
                                BotStarsController.getInstance(UserConfig.selectedAccount).getBotStarsBalance(-attributesHolder.getChatId()).amount > 0 ||
                                        BotStarsController.getInstance(UserConfig.selectedAccount).hasTransactions(-attributesHolder.getChatId())
                        ) ||
                                attributesHolder.getChatInfo() != null &&
                                        attributesHolder.getChatInfo().can_view_revenue &&
                                        BotStarsController.getInstance(UserConfig.selectedAccount).getTONBalance(-attributesHolder.getChatId()) > 0
                ) {
                    rowsHolder.setChannelBalanceRow(attributesHolder.getRowCountAndIncrement());
                    rowsHolder.setChannelBalanceSectionRow(attributesHolder.getRowCountAndIncrement());
                }
            }

            if (ChatObject.isChannel(attributesHolder.getCurrentChat())) {
                if (!rowsHolder.isTopic() && attributesHolder.getChatInfo() != null && attributesHolder.getCurrentChat().megagroup && attributesHolder.getChatInfo().participants != null && attributesHolder.getChatInfo().participants.participants != null && !attributesHolder.getChatInfo().participants.participants.isEmpty()) {
                    if (!ChatObject.isNotInChat(attributesHolder.getCurrentChat()) && ChatObject.canAddUsers(attributesHolder.getCurrentChat()) && attributesHolder.getChatInfo().participants_count < profileActivity.getMessagesController().maxMegagroupCount) {
                        rowsHolder.setAddMemberRow(attributesHolder.getRowCountAndIncrement());
                    }
                    int count = attributesHolder.getChatInfo().participants.participants.size();
                    if ((count <= 5 || !hasMedia || rowsHolder.getUsersForceShowingIn() == 1) && rowsHolder.getUsersForceShowingIn() != 2) {
                        if (rowsHolder.getAddMemberRow() == -1) {
                            rowsHolder.setMembersHeaderRow(attributesHolder.getRowCountAndIncrement());
                        }
                        rowsHolder.setMembersStartRow(attributesHolder.getRowCount());
                        attributesHolder.setRowCount(attributesHolder.getRowCount() + count);
                        rowsHolder.setMembersEndRow(attributesHolder.getRowCount());
                        rowsHolder.setMembersSectionRow(attributesHolder.getRowCountAndIncrement());
                        attributesHolder.getVisibleChatParticipants().addAll(attributesHolder.getChatInfo().participants.participants);
                        if (attributesHolder.getSortedUsers() != null) {
                            attributesHolder.getVisibleSortedUsers().addAll(attributesHolder.getSortedUsers());
                        }
                        rowsHolder.setUsersForceShowingIn(1);
                        if (viewsHolder.getSharedMediaLayout() != null) {
                            viewsHolder.getSharedMediaLayout().setChatUsers(null, null);
                        }
                    } else {
                        if (rowsHolder.getAddMemberRow() != -1) {
                            rowsHolder.setMembersSectionRow(attributesHolder.getRowCountAndIncrement());
                        }
                        if (viewsHolder.getSharedMediaLayout() != null) {
                            if (!attributesHolder.getSortedUsers().isEmpty()) {
                                rowsHolder.setUsersForceShowingIn(2);
                            }
                            viewsHolder.getSharedMediaLayout().setChatUsers(attributesHolder.getSortedUsers(), attributesHolder.getChatInfo());
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(attributesHolder.getCurrentChat()) && ChatObject.canAddUsers(attributesHolder.getCurrentChat()) && attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().participants_hidden) {
                        rowsHolder.setAddMemberRow(attributesHolder.getRowCountAndIncrement());
                        rowsHolder.setMembersSectionRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (viewsHolder.getSharedMediaLayout() != null) {
                        viewsHolder.getSharedMediaLayout().updateAdapters();
                    }
                }

                if (rowsHolder.getLastSectionRow() == -1 && attributesHolder.getCurrentChat().left && !attributesHolder.getCurrentChat().kicked && (viewsHolder.getButtonsGroup() == null || viewsHolder.getButtonsGroup().getExtraButtons().contains(ButtonViewEnum.JOIN))) {
                    long requestedTime = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getLong("dialog_join_requested_time_" + attributesHolder.getDialogId(), -1);
                    if (!(requestedTime > 0 && System.currentTimeMillis() - requestedTime < 1000 * 60 * 2)) {
                        rowsHolder.setJoinRow(attributesHolder.getRowCountAndIncrement());
                        rowsHolder.setLastSectionRow(attributesHolder.getRowCountAndIncrement());
                    }
                }
            } else if (attributesHolder.getChatInfo() != null) {
                if (!rowsHolder.isTopic() && attributesHolder.getChatInfo().participants != null && attributesHolder.getChatInfo().participants.participants != null && !(attributesHolder.getChatInfo().participants instanceof TLRPC.TL_chatParticipantsForbidden)) {
                    if (ChatObject.canAddUsers(attributesHolder.getCurrentChat()) || attributesHolder.getCurrentChat().default_banned_rights == null || !attributesHolder.getCurrentChat().default_banned_rights.invite_users) {
                        rowsHolder.setAddMemberRow(attributesHolder.getRowCountAndIncrement());
                    }
                    int count = attributesHolder.getChatInfo().participants.participants.size();
                    if (count <= 5 || !hasMedia) {
                        if (rowsHolder.getAddMemberRow() == -1) {
                            rowsHolder.setMembersHeaderRow(attributesHolder.getRowCountAndIncrement());
                        }
                        rowsHolder.setMembersStartRow(attributesHolder.getRowCount());
                        attributesHolder.setRowCount(attributesHolder.getRowCount() + attributesHolder.getChatInfo().participants.participants.size());
                        rowsHolder.setMembersEndRow(attributesHolder.getRowCount());
                        rowsHolder.setMembersSectionRow(attributesHolder.getRowCountAndIncrement());
                        attributesHolder.getVisibleChatParticipants().addAll(attributesHolder.getChatInfo().participants.participants);
                        if (attributesHolder.getSortedUsers() != null) {
                            attributesHolder.getVisibleSortedUsers().addAll(attributesHolder.getSortedUsers());
                        }
                        if (viewsHolder.getSharedMediaLayout() != null) {
                            viewsHolder.getSharedMediaLayout().setChatUsers(null, null);
                        }
                    } else {
                        if (rowsHolder.getAddMemberRow() != -1) {
                            rowsHolder.setMembersSectionRow(attributesHolder.getRowCountAndIncrement());
                        }
                        if (viewsHolder.getSharedMediaLayout() != null) {
                            viewsHolder.getSharedMediaLayout().setChatUsers(attributesHolder.getSortedUsers(), attributesHolder.getChatInfo());
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(attributesHolder.getCurrentChat()) && ChatObject.canAddUsers(attributesHolder.getCurrentChat()) && attributesHolder.getChatInfo().participants_hidden) {
                        rowsHolder.setAddMemberRow(attributesHolder.getRowCountAndIncrement());
                        rowsHolder.setMembersSectionRow(attributesHolder.getRowCountAndIncrement());
                    }
                    if (viewsHolder.getSharedMediaLayout() != null) {
                        viewsHolder.getSharedMediaLayout().updateAdapters();
                    }
                }
            }

            if (hasMedia) {
                rowsHolder.setSharedMediaRow(attributesHolder.getRowCountAndIncrement());
            }
        }
        if (rowsHolder.getSharedMediaRow() == -1) {
            rowsHolder.setBottomPaddingRow(attributesHolder.getRowCountAndIncrement());
        }
        final int actionBarHeight = profileActivity.getActionBar() != null ? ActionBar.getCurrentActionBarHeight() + (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) : 0;
        RecyclerListView clippedList = listView != null ? listView.getClippedList() : null;
        if (listView == null || prevRowsCount > attributesHolder.getRowCount() || rowsHolder.getListContentHeight() != 0 && (clippedList == null || rowsHolder.getListContentHeight() + actionBarHeight + AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT) < clippedList.getMeasuredHeight())) {
            rowsHolder.setLastMeasuredContentWidth(0);
        }

        if (clippedList != null) {
            clippedList.setTranslateSelectorPosition(rowsHolder.getBizHoursRow());
        }
    }

    private void validateRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }

    private void validateAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = components.getAttributesComponentsHolder();
        }
    }

    private void validateViewHolder() {
        if (viewsHolder == null) {
            viewsHolder = components.getViewComponentsHolder();
        }
    }
}
