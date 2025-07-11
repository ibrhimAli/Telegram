package org.telegram.ui.profile.utils;

import static org.telegram.messenger.LocaleController.getString;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.collection.LongSparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.AuthTokensHelper;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ChatThemeController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatNotificationsPopupWrapper;
import org.telegram.ui.Components.FloatingDebug.FloatingDebugController;
import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Paint.PersistColorPalette;
import org.telegram.ui.Components.Premium.boosts.UserSelectorBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.DataSettingsActivity;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.IdenticonActivity;
import org.telegram.ui.LanguageSelectActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LiteModeSettingsActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.MemberRequestsActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.PrivacySettingsActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProfileBirthdayEffect;
import org.telegram.ui.ProfileNotificationsActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.SessionsActivity;
import org.telegram.ui.Stars.BotStarsActivity;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.StickersActivity;
import org.telegram.ui.Stories.recorder.DualCameraView;
import org.telegram.ui.ThemeActivity;
import org.telegram.ui.TopicsNotifySettingsFragments;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;
import org.telegram.ui.bots.BotBiometry;
import org.telegram.ui.bots.BotDownloads;
import org.telegram.ui.bots.BotLocation;
import org.telegram.ui.bots.ChannelAffiliateProgramsFragment;
import org.telegram.ui.bots.SetupEmojiStatusSheet;
import org.telegram.ui.profile.adapter.ListAdapter;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.ClippedListView;
import org.telegram.ui.profile.view.button.ButtonViewEnum;

import java.util.ArrayList;
import java.util.Set;

public class ListViewClick {


    private static int botPermissionEmojiStatusReqId;
    private final ProfileActivity profileActivity;
    private final OpenAndWrite openAndWrite;
    private final Adapter adapter;
    private final ClicksAndPress clicksAndPress;
    private ClippedListView listView;
    private final ComponentsFactory components;
    private ViewComponentsHolder viewsHolder;
    private AttributesComponentsHolder attributesHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    public ListViewClick(ComponentsFactory components) {
        profileActivity = components.getProfileActivity();
        this.components = components;
        openAndWrite = components.getOpenAndWrite();
        adapter = components.getAdapter();
        clicksAndPress = components.getClicksAndPress();
        viewsHolder = components.getViewComponentsHolder();
        listView = components.getListView();
        attributesHolder = components.getAttributesComponentsHolder();
        rowsHolder = components.getRowsAndStatusComponentsHolder();
    }
    public void handleOnItemClick(View view, int position, float x, float y,
                                  ListAdapter listAdapter, long did, String vcardFirstName, String vcardLastName,
                                  float photoDescriptionProgress, float customAvatarProgress, float animatedFracture,
                                  TL_account.Password currentPassword, ProfileBirthdayEffect birthdayEffect) {
        if (profileActivity.getParentActivity() == null) {
            return;
        }
        checkForAttributesHolder();
        checkForViewsHolder();
        checkForRowsHolder();
        if (listView == null) {
            listView = components.getListView();
        }
        listView.getClippedList().stopScroll();
        if (position == rowsHolder.getAffiliateRow()) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().starref_program != null) {
                final long selfId = profileActivity.getUserConfig().getClientUserId();
                BotStarsController.getInstance(UserConfig.selectedAccount).getConnectedBot(profileActivity.getContext(), selfId, attributesHolder.getUserId(), connectedBot -> {
                    if (connectedBot == null) {
                        ChannelAffiliateProgramsFragment.showConnectAffiliateAlert(profileActivity.getContext(), UserConfig.selectedAccount, attributesHolder.getUserInfo().starref_program, profileActivity.getUserConfig().getClientUserId(), attributesHolder.getResourcesProvider(), false);
                    } else {
                        ChannelAffiliateProgramsFragment.showShareAffiliateAlert(profileActivity.getContext(), UserConfig.selectedAccount, connectedBot, selfId, attributesHolder.getResourcesProvider());
                    }
                });
            } else if (user != null && user.bot_can_edit) {
                profileActivity.presentFragment(new AffiliateProgramFragment(attributesHolder.getUserId()));
            }
        } else if (position == rowsHolder.getNotificationsSimpleRow()) {
            boolean muted = profileActivity.getMessagesController().isDialogMuted(did, attributesHolder.getTopicId());
            profileActivity.getAccountInstance().getNotificationsController().muteDialog(did, attributesHolder.getTopicId(), !muted);
            BulletinFactory.createMuteBulletin(profileActivity, !muted, null).show();
            updateExceptions(profileActivity, listAdapter);
            if (rowsHolder.getNotificationsSimpleRow() >= 0 && listAdapter != null) {
                listAdapter.notifyItemChanged(rowsHolder.getNotificationsSimpleRow());
            }
        } else if (position == rowsHolder.getAddToContactsRow()) {
            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            Bundle args = new Bundle();
            args.putLong("user_id", user.id);
            args.putBoolean("addContact", true);
            args.putString("phone", rowsHolder.getVcardPhone());
            args.putString("first_name_card", vcardFirstName);
            args.putString("last_name_card", vcardLastName);
            openAndWrite.openAddToContact(listAdapter, args, user, photoDescriptionProgress, customAvatarProgress, animatedFracture);
        } else if (position == rowsHolder.getReportReactionRow()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
            builder.setTitle(LocaleController.getString(R.string.ReportReaction));
            builder.setMessage(LocaleController.getString(R.string.ReportAlertReaction));

            TLRPC.Chat chat = profileActivity.getMessagesController().getChat(-rowsHolder.getReportReactionFromDialogId());
            CheckBoxCell[] cells = new CheckBoxCell[1];
            if (chat != null && ChatObject.canBlockUsers(chat)) {
                LinearLayout linearLayout = new LinearLayout(profileActivity.getParentActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                cells[0] = new CheckBoxCell(profileActivity.getParentActivity(), 1, attributesHolder.getResourcesProvider());
                cells[0].setBackgroundDrawable(Theme.getSelectorDrawable(false));
                cells[0].setText(LocaleController.getString(R.string.BanUser), "", true, false);
                cells[0].setPadding(LocaleController.isRTL ? AndroidUtilities.dp(16) : AndroidUtilities.dp(8), 0, LocaleController.isRTL ? AndroidUtilities.dp(8) : AndroidUtilities.dp(16), 0);
                linearLayout.addView(cells[0], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                cells[0].setOnClickListener(v -> {
                    cells[0].setChecked(!cells[0].isChecked(), true);
                });
                builder.setView(linearLayout);
            }

            builder.setPositiveButton(LocaleController.getString(R.string.ReportChat), (dialog, which) -> {
                TLRPC.TL_messages_reportReaction req = new TLRPC.TL_messages_reportReaction();
                req.user_id = profileActivity.getMessagesController().getInputUser(attributesHolder.getUserId());
                req.peer = profileActivity.getMessagesController().getInputPeer(rowsHolder.getReportReactionFromDialogId());
                req.id = rowsHolder.getReportReactionMessageId();
                ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {

                });

                if (cells[0] != null && cells[0].isChecked()) {
                    TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                    profileActivity.getMessagesController().deleteParticipantFromChat(-rowsHolder.getReportReactionFromDialogId(), user);
                }

                rowsHolder.setReportReactionMessageId(0);
                adapter.updateListAnimated(false);
                BulletinFactory.of(profileActivity).createReportSent(attributesHolder.getResourcesProvider()).show();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.show();
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        } else if (position == rowsHolder.getSettingsKeyRow()) {
            Bundle args = new Bundle();
            args.putInt("chat_id", DialogObject.getEncryptedChatId(attributesHolder.getDialogId()));
            profileActivity.presentFragment(new IdenticonActivity(args));
        } else if (position == rowsHolder.getSettingsTimerRow()) {
            profileActivity.showDialog(AlertsCreator.createTTLAlert(profileActivity.getParentActivity(), attributesHolder.getCurrentEncryptedChat(), attributesHolder.getResourcesProvider()).create());
        } else if (position == rowsHolder.getNotificationsRow()) {
            if (LocaleController.isRTL && x <= AndroidUtilities.dp(76) || !LocaleController.isRTL && x >= view.getMeasuredWidth() - AndroidUtilities.dp(76)) {
                NotificationsCheckCell checkCell = (NotificationsCheckCell) view;
                boolean checked = !checkCell.isChecked();

                boolean defaultEnabled = profileActivity.getAccountInstance().getNotificationsController().isGlobalNotificationsEnabled(did, false, false);

                String key = NotificationsController.getSharedPrefKey(did, attributesHolder.getTopicId());
                if (checked) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (defaultEnabled) {
                        editor.remove("notify2_" + key);
                    } else {
                        editor.putInt("notify2_" + key, 0);
                    }
                    if (attributesHolder.getTopicId() == 0) {
                        profileActivity.getMessagesStorage().setDialogFlags(did, 0);
                        TLRPC.Dialog dialog = profileActivity.getMessagesController().dialogs_dict.get(did);
                        if (dialog != null) {
                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                        }
                    }
                    editor.apply();
                } else {
                    int untilTime = Integer.MAX_VALUE;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
                    SharedPreferences.Editor editor = preferences.edit();
                    long flags;
                    if (!defaultEnabled) {
                        editor.remove("notify2_" + key);
                        flags = 0;
                    } else {
                        editor.putInt("notify2_" + key, 2);
                        flags = 1;
                    }
                    profileActivity.getAccountInstance().getNotificationsController().removeNotificationsForDialog(did);
                    if (attributesHolder.getTopicId() == 0) {
                        profileActivity.getMessagesStorage().setDialogFlags(did, flags);
                        TLRPC.Dialog dialog = profileActivity.getMessagesController().dialogs_dict.get(did);
                        if (dialog != null) {
                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                            if (defaultEnabled) {
                                dialog.notify_settings.mute_until = untilTime;
                            }
                        }
                    }
                    editor.apply();
                }
                updateExceptions(profileActivity, listAdapter);
                profileActivity.getAccountInstance().getNotificationsController().updateServerNotificationsSettings(did, attributesHolder.getTopicId());
                checkCell.setChecked(checked);
                RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.getClippedList().findViewHolderForPosition(rowsHolder.getNotificationsRow());
                if (holder != null) {
                    listAdapter.onBindViewHolder(holder, rowsHolder.getNotificationsRow());
                }
                return;
            }
            ChatNotificationsPopupWrapper chatNotificationsPopupWrapper = new ChatNotificationsPopupWrapper(profileActivity.getContext(), UserConfig.selectedAccount, null, true, true, new ChatNotificationsPopupWrapper.Callback() {
                @Override
                public void toggleSound() {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
                    boolean enabled = !preferences.getBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(did, attributesHolder.getTopicId()), true);
                    preferences.edit().putBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(did, attributesHolder.getTopicId()), enabled).apply();
                    if (BulletinFactory.canShowBulletin(profileActivity)) {
                        BulletinFactory.createSoundEnabledBulletin(profileActivity, enabled ? NotificationsController.SETTING_SOUND_ON : NotificationsController.SETTING_SOUND_OFF, profileActivity.getResourceProvider()).show();
                    }
                }

                @Override
                public void muteFor(int timeInSeconds) {
                    if (timeInSeconds == 0) {
                        if (profileActivity.getMessagesController().isDialogMuted(did, attributesHolder.getTopicId())) {
                            toggleMute();
                        }
                        if (BulletinFactory.canShowBulletin(profileActivity)) {
                            BulletinFactory.createMuteBulletin(profileActivity, NotificationsController.SETTING_MUTE_UNMUTE, timeInSeconds, profileActivity.getResourceProvider()).show();
                        }
                    } else {
                        profileActivity.getAccountInstance().getNotificationsController().muteUntil(did, attributesHolder.getTopicId(), timeInSeconds);
                        if (BulletinFactory.canShowBulletin(profileActivity)) {
                            BulletinFactory.createMuteBulletin(profileActivity, NotificationsController.SETTING_MUTE_CUSTOM, timeInSeconds, profileActivity.getResourceProvider()).show();
                        }
                        updateExceptions(profileActivity, listAdapter);
                        if (rowsHolder.getNotificationsRow() >= 0 && listAdapter != null) {
                            listAdapter.notifyItemChanged(rowsHolder.getNotificationsRow());
                        }
                    }
                }

                @Override
                public void showCustomize() {
                    if (did != 0) {
                        Bundle args = new Bundle();
                        args.putLong("dialog_id", did);
                        args.putLong("topic_id", attributesHolder.getTopicId());
                        profileActivity.presentFragment(new ProfileNotificationsActivity(args, attributesHolder.getResourcesProvider()));
                    }
                }

                @Override
                public void toggleMute() {
                    boolean muted = profileActivity.getMessagesController().isDialogMuted(did, attributesHolder.getTopicId());
                    profileActivity.getAccountInstance().getNotificationsController().muteDialog(did, attributesHolder.getTopicId(), !muted);
                    if (profileActivity.fragmentView != null) {
                        BulletinFactory.createMuteBulletin(profileActivity, !muted, null).show();
                    }
                    updateExceptions(profileActivity, listAdapter);
                    if (rowsHolder.getNotificationsRow() >= 0 && listAdapter != null) {
                        listAdapter.notifyItemChanged(rowsHolder.getNotificationsRow());
                    }
                }

                @Override
                public void openExceptions() {
                    Bundle bundle = new Bundle();
                    bundle.putLong("dialog_id", did);
                    TopicsNotifySettingsFragments notifySettings = new TopicsNotifySettingsFragments(bundle);
                    notifySettings.setExceptions(attributesHolder.getNotificationsExceptionTopics());
                    profileActivity.presentFragment(notifySettings);
                }
            }, profileActivity.getResourceProvider());
            chatNotificationsPopupWrapper.update(did, attributesHolder.getTopicId(), attributesHolder.getNotificationsExceptionTopics());
            if (AndroidUtilities.isTablet()) {
                View v = profileActivity.getParentLayout().getView();
                x += v.getX() + v.getPaddingLeft();
                y += v.getY() + v.getPaddingTop();
            }
            chatNotificationsPopupWrapper.showAsOptions(profileActivity, view, x, y);
        } else if (position == rowsHolder.getUnblockRow()) {
            profileActivity.getMessagesController().unblockPeer(attributesHolder.getUserId());
            if (BulletinFactory.canShowBulletin(profileActivity)) {
                BulletinFactory.createBanBulletin(profileActivity, false).show();
            }
        } else if (position == rowsHolder.getAddToGroupButtonRow()) {
            try {
                profileActivity.getActionBar().getActionBarMenuOnItemClick().onItemClick(ProfileParams.invite_to_group);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (position == rowsHolder.getSendMessageRow()) {
            openAndWrite.onWriteButtonClick();
        } else if (position == rowsHolder.getReportRow()) {
            ReportBottomSheet.openChat(profileActivity, profileActivity.getDialogId());
        } else if (position >= rowsHolder.getMembersStartRow() && position < rowsHolder.getMembersEndRow()) {
            TLRPC.ChatParticipant participant;
            if (!attributesHolder.getSortedUsers().isEmpty()) {
                participant = attributesHolder.getChatInfo().participants.participants.get(attributesHolder.getSortedUsers().get(position - rowsHolder.getMembersStartRow()));
            } else {
                participant = attributesHolder.getChatInfo().participants.participants.get(position - rowsHolder.getMembersStartRow());
            }
            profileActivity.onMemberClick(participant, false, view);
        } else if (position == rowsHolder.getAddMemberRow()) {
            openAndWrite.openAddMember();
        } else if (position == rowsHolder.getUsernameRow()) {
            clicksAndPress.processOnClickOrPress(position, view, x, y);
        } else if (position == rowsHolder.getLocationRow()) {
            if (attributesHolder.getChatInfo().location instanceof TLRPC.TL_channelLocation) {
                LocationActivity fragment = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP_VIEW);
                fragment.setChatLocation(attributesHolder.getChatId(), (TLRPC.TL_channelLocation) attributesHolder.getChatInfo().location);
                profileActivity.presentFragment(fragment);
            }
        } else if (position == rowsHolder.getJoinRow()) {
            clicksAndPress.onProfileButtonClicked(ButtonViewEnum.JOIN);
        } else if (position == rowsHolder.getSubscribersRow()) {
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesHolder.getChatId());
            args.putInt("type", ChatUsersActivity.TYPE_USERS);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(attributesHolder.getChatInfo());
            profileActivity.presentFragment(fragment);
        } else if (position == rowsHolder.getSubscribersRequestsRow()) {
            MemberRequestsActivity activity = new MemberRequestsActivity(attributesHolder.getChatId());
            profileActivity.presentFragment(activity);
        } else if (position == rowsHolder.getAdministratorsRow()) {
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesHolder.getChatId());
            args.putInt("type", ChatUsersActivity.TYPE_ADMIN);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(attributesHolder.getChatInfo());
            profileActivity.presentFragment(fragment);
        } else if (position == rowsHolder.getSettingsRow()) {
            viewsHolder.getEditItem().performClick();
        } else if (position == rowsHolder.getBotStarsBalanceRow()) {
            profileActivity.presentFragment(new BotStarsActivity(BotStarsActivity.TYPE_STARS, attributesHolder.getUserId()));
        } else if (position == rowsHolder.getBotTonBalanceRow()) {
            profileActivity.presentFragment(new BotStarsActivity(BotStarsActivity.TYPE_TON, attributesHolder.getUserId()));
        } else if (position == rowsHolder.getChannelBalanceRow()) {
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesHolder.getChatId());
            args.putBoolean("start_from_monetization", true);
            profileActivity.presentFragment(new StatisticActivity(args));
        } else if (position == rowsHolder.getBlockedUsersRow()) {
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesHolder.getChatId());
            args.putInt("type", ChatUsersActivity.TYPE_BANNED);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(attributesHolder.getChatInfo());
            profileActivity.presentFragment(fragment);
        } else if (position == rowsHolder.getNotificationRow()) {
            profileActivity.presentFragment(new NotificationsSettingsActivity());
        } else if (position == rowsHolder.getPrivacyRow()) {
            profileActivity.presentFragment(new PrivacySettingsActivity().setCurrentPassword(currentPassword));
        } else if (position == rowsHolder.getDataRow()) {
            profileActivity.presentFragment(new DataSettingsActivity());
        } else if (position == rowsHolder.getChatRow()) {
            profileActivity.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
        } else if (position == rowsHolder.getFiltersRow()) {
            profileActivity.presentFragment(new FiltersSetupActivity());
        } else if (position == rowsHolder.getStickersRow()) {
            profileActivity.presentFragment(new StickersActivity(MediaDataController.TYPE_IMAGE, null));
        } else if (position == rowsHolder.getLiteModeRow()) {
            profileActivity.presentFragment(new LiteModeSettingsActivity());
        } else if (position == rowsHolder.getDevicesRow()) {
            profileActivity.presentFragment(new SessionsActivity(0));
        } else if (position == rowsHolder.getQuestionRow()) {
            profileActivity.showDialog(AlertsCreator.createSupportAlert(profileActivity, attributesHolder.getResourcesProvider()));
        } else if (position == rowsHolder.getFaqRow()) {
            Browser.openUrl(profileActivity.getParentActivity(), LocaleController.getString(R.string.TelegramFaqUrl));
        } else if (position == rowsHolder.getPolicyRow()) {
            Browser.openUrl(profileActivity.getParentActivity(), LocaleController.getString(R.string.PrivacyPolicyUrl));
        } else if (position == rowsHolder.getSendLogsRow()) {
            Logging.sendLogs(profileActivity.getParentActivity(), false);
        } else if (position == rowsHolder.getSendLastLogsRow()) {
            Logging.sendLogs(profileActivity.getParentActivity(), true);
        } else if (position == rowsHolder.getClearLogsRow()) {
            FileLog.cleanupLogs();
        } else if (position == rowsHolder.getSwitchBackendRow()) {
            if (profileActivity.getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder1 = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
            builder1.setMessage(LocaleController.getString(R.string.AreYouSure));
            builder1.setTitle(LocaleController.getString(R.string.AppName));
            builder1.setPositiveButton(LocaleController.getString(R.string.OK), (dialogInterface, i) -> {
                SharedConfig.pushAuthKey = null;
                SharedConfig.pushAuthKeyId = null;
                SharedConfig.saveConfig();
                profileActivity.getConnectionsManager().switchBackend(true);
            });
            builder1.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            profileActivity.showDialog(builder1.create());
        } else if (position == rowsHolder.getLanguageRow()) {
            profileActivity.presentFragment(new LanguageSelectActivity());
        } else if (position == rowsHolder.getSetUsernameRow()) {
            profileActivity.presentFragment(new ChangeUsernameActivity());
        } else if (position == rowsHolder.getBioRow()) {
            profileActivity.presentFragment(new UserInfoActivity());
        } else if (position == rowsHolder.getNumberRow()) {
            profileActivity.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
        } else if (position == rowsHolder.getSetAvatarRow()) {
            openAndWrite.onWriteButtonClick();
        } else if (position == rowsHolder.getPremiumRow()) {
            profileActivity.presentFragment(new PremiumPreviewFragment("settings"));
        } else if (position == rowsHolder.getStarsRow()) {
            profileActivity.presentFragment(new StarsIntroActivity());
        } else if (position == rowsHolder.getBusinessRow()) {
            profileActivity.presentFragment(new PremiumPreviewFragment(PremiumPreviewFragment.FEATURES_BUSINESS, "settings"));
        } else if (position == rowsHolder.getPremiumGiftingRow()) {
            UserSelectorBottomSheet.open(0, BirthdayController.getInstance(UserConfig.selectedAccount).getState());
        } else if (position == rowsHolder.getBotPermissionLocation()) {
            if (viewsHolder.getBotLocation() != null) {
                viewsHolder.getBotLocation().setGranted(!viewsHolder.getBotLocation().granted(), () -> {
                    ((TextCell) view).setChecked(viewsHolder.getBotLocation().granted());
                });
            }
        } else if (position == rowsHolder.getBotPermissionBiometry()) {
            if (viewsHolder.getBotBiometry() != null) {
                viewsHolder.getBotBiometry().setGranted(!viewsHolder.getBotBiometry().granted());
                ((TextCell) view).setChecked(viewsHolder.getBotBiometry().granted());
            }
        } else if (position == rowsHolder.getBotPermissionEmojiStatus()) {
            ((TextCell) view).setChecked(!((TextCell) view).isChecked());
            if (botPermissionEmojiStatusReqId > 0) {
                profileActivity.getConnectionsManager().cancelRequest(botPermissionEmojiStatusReqId, true);
            }
            TL_bots.toggleUserEmojiStatusPermission req = new TL_bots.toggleUserEmojiStatusPermission();
            req.bot = profileActivity.getMessagesController().getInputUser(attributesHolder.getUserId());
            req.enabled = ((TextCell) view).isChecked();
            if (attributesHolder.getUserInfo() != null) {
                attributesHolder.getUserInfo().bot_can_manage_emoji_status = req.enabled;
            }
            final int[] reqId = new int[1];
            reqId[0] = botPermissionEmojiStatusReqId = profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                if (!(res instanceof TLRPC.TL_boolTrue)) {
                    BulletinFactory.of(profileActivity).showForError(err);
                }
                if (botPermissionEmojiStatusReqId == reqId[0]) {
                    botPermissionEmojiStatusReqId = 0;
                }
            }));
        } else if (position == rowsHolder.getBizHoursRow()) {
            rowsHolder.setHoursExpanded(!rowsHolder.isHoursExpanded());
            adapter.saveScrollPosition();
            view.requestLayout();
            listAdapter.notifyItemChanged(rowsHolder.getBizHoursRow());
            if (rowsHolder.getSavedScrollPosition() >= 0) {
                viewsHolder.getLayoutManager().scrollToPositionWithOffset(rowsHolder.getSavedScrollPosition(), rowsHolder.getSavedScrollOffset() - listView.getClippedList().getPaddingTop());
            }
        } else if (position == rowsHolder.getBizLocationRow()) {
            openAndWrite.openLocation(false);
        } else if (position == rowsHolder.getChannelRow()) {
            if (attributesHolder.getUserInfo() == null) return;
            Bundle args = new Bundle();
            args.putLong("chat_id", attributesHolder.getUserInfo().personal_channel_id);
            profileActivity.presentFragment(new ChatActivity(args));
        } else if (position == rowsHolder.getBirthdayRow()) {
            if (birthdayEffect != null && birthdayEffect.start()) {
                return;
            }
            if (adapter.editRow( view, position)) {
                return;
            }
            TextDetailCell cell = (TextDetailCell) view;
            if (cell.hasImage()) {
                profileActivity.onTextDetailCellImageClicked(cell.getImageView());
            }
        } else {
            clicksAndPress.processOnClickOrPress(position, view, x, y);
        }
    }

    private void checkForViewsHolder() {
        if (viewsHolder == null) {
            viewsHolder = components.getViewComponentsHolder();
        }
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }

    private void checkForAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = components.getAttributesComponentsHolder();
        }
    }


    public boolean handleOnItemLongClick(View view, int position, ListAdapter listAdapter, int pressCount) {
            if (position == rowsHolder.getVersionRow()) {
                pressCount++;
                if (pressCount >= 2 || BuildVars.DEBUG_PRIVATE_VERSION) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
                    builder.setTitle(getString(R.string.DebugMenu));
                    CharSequence[] items;
                    items = new CharSequence[]{
                            getString(R.string.DebugMenuImportContacts),
                            getString(R.string.DebugMenuReloadContacts),
                            getString(R.string.DebugMenuResetContacts),
                            getString(R.string.DebugMenuResetDialogs),
                            BuildVars.DEBUG_VERSION ? null : (BuildVars.LOGS_ENABLED ? getString("DebugMenuDisableLogs", R.string.DebugMenuDisableLogs) : getString("DebugMenuEnableLogs", R.string.DebugMenuEnableLogs)),
                            SharedConfig.inappCamera ? getString("DebugMenuDisableCamera", R.string.DebugMenuDisableCamera) : getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera),
                            getString("DebugMenuClearMediaCache", R.string.DebugMenuClearMediaCache),
                            getString(R.string.DebugMenuCallSettings),
                            null,
                            BuildVars.DEBUG_PRIVATE_VERSION || ApplicationLoader.isStandaloneBuild() || ApplicationLoader.isBetaBuild() ? getString("DebugMenuCheckAppUpdate", R.string.DebugMenuCheckAppUpdate) : null,
                            getString("DebugMenuReadAllDialogs", R.string.DebugMenuReadAllDialogs),
                            BuildVars.DEBUG_PRIVATE_VERSION ? (SharedConfig.disableVoiceAudioEffects ? "Enable voip audio effects" : "Disable voip audio effects") : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Clean app update" : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Reset suggestions" : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? getString(R.string.DebugMenuClearWebViewCache) : null,
                            getString(R.string.DebugMenuClearWebViewCookies),
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? getString(SharedConfig.debugWebView ? R.string.DebugMenuDisableWebViewDebug : R.string.DebugMenuEnableWebViewDebug) : null,
                            (AndroidUtilities.isTabletInternal() && BuildVars.DEBUG_PRIVATE_VERSION) ? (SharedConfig.forceDisableTabletMode ? "Enable tablet mode" : "Disable tablet mode") : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? getString(SharedConfig.isFloatingDebugActive ? R.string.FloatingDebugDisable : R.string.FloatingDebugEnable) : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Force remove premium suggestions" : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Share device info" : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Force performance class" : null,
                            BuildVars.DEBUG_PRIVATE_VERSION && !InstantCameraView.allowBigSizeCameraDebug() ? (!SharedConfig.bigCameraForRound ? "Force big camera for round" : "Disable big camera for round") : null,
                            getString(DualCameraView.dualAvailableStatic(profileActivity.getContext()) ? "DebugMenuDualOff" : "DebugMenuDualOn"),
                            BuildVars.DEBUG_VERSION ? (SharedConfig.useSurfaceInStories ? "back to TextureView in stories" : "use SurfaceView in stories") : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? (SharedConfig.photoViewerBlur ? "do not blur in photoviewer" : "blur in photoviewer") : null,
                            !SharedConfig.payByInvoice ? "Enable Invoice Payment" : "Disable Invoice Payment",
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Update Attach Bots" : null,
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? (!SharedConfig.isUsingCamera2(UserConfig.selectedAccount) ? "Use Camera 2 API" : "Use old Camera 1 API") : null,
                            BuildVars.DEBUG_VERSION ? "Clear Mini Apps Permissions and Files" : null,
                            BuildVars.DEBUG_PRIVATE_VERSION ? "Clear all login tokens" : null,
                            SharedConfig.canBlurChat() && Build.VERSION.SDK_INT >= 31 ? (SharedConfig.useNewBlur ? "back to cpu blur" : "use new gpu blur") : null,
                            SharedConfig.adaptableColorInBrowser ? "Disabled adaptive browser colors" : "Enable adaptive browser colors",
                            SharedConfig.debugVideoQualities ? "Disable video qualities debug" : "Enable video qualities debug",
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? getString(SharedConfig.useSystemBoldFont ? R.string.DebugMenuDontUseSystemBoldFont : R.string.DebugMenuUseSystemBoldFont) : null,
                            "Reload app config",
                            !SharedConfig.forceForumTabs ? "Force Forum Tabs" : "Do Not Force Forum Tabs"
                    };

                    builder.setItems(items, (dialog, which) -> {
                        if (which == 0) { // Import Contacts
                            profileActivity.getUserConfig().syncContacts = true;
                            profileActivity.getUserConfig().saveConfig(false);
                            profileActivity.getAccountInstance().getContactsController().forceImportContacts();
                        } else if (which == 1) { // Reload Contacts
                            profileActivity.getAccountInstance().getContactsController().loadContacts(false, 0);
                        } else if (which == 2) { // Reset Imported Contacts
                            profileActivity.getAccountInstance().getContactsController().resetImportedContacts();
                        } else if (which == 3) { // Reset Dialogs
                            profileActivity.getMessagesController().forceResetDialogs();
                        } else if (which == 4) { // Logs
                            BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED;
                            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
                            sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).commit();
                            adapter.updateRowsIds();
                            listAdapter.notifyDataSetChanged();
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("app start time = " + ApplicationLoader.startTime);
                                try {
                                    FileLog.d("buildVersion = " + ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionCode);
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                            }
                        } else if (which == 5) { // In-app camera
                            SharedConfig.toggleInappCamera();
                        } else if (which == 6) { // Clear sent media cache
                            profileActivity.getMessagesStorage().clearSentMedia();
                            SharedConfig.setNoSoundHintShowed(false);
                            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                            editor.remove("archivehint").remove("proximityhint").remove("archivehint_l").remove("speedhint").remove("gifhint").remove("reminderhint").remove("soundHint").remove("themehint").remove("bganimationhint").remove("filterhint").remove("n_0").remove("storyprvhint").remove("storyhint").remove("storyhint2").remove("storydualhint").remove("storysvddualhint").remove("stories_camera").remove("dualcam").remove("dualmatrix").remove("dual_available").remove("archivehint").remove("askNotificationsAfter").remove("askNotificationsDuration").remove("viewoncehint").remove("voicepausehint").remove("taptostorysoundhint").remove("nothanos").remove("voiceoncehint").remove("savedhint").remove("savedsearchhint").remove("savedsearchtaghint").remove("groupEmojiPackHintShown").remove("newppsms").remove("monetizationadshint").remove("seekSpeedHintShowed").remove("unsupport_video/av01").remove("channelgifthint").remove("statusgiftpage").remove("multistorieshint").remove("channelsuggesthint").remove("trimvoicehint").apply();
                            MessagesController.getEmojiSettings(UserConfig.selectedAccount).edit().remove("featured_hidden").remove("emoji_featured_hidden").commit();
                            SharedConfig.textSelectionHintShows = 0;
                            SharedConfig.lockRecordAudioVideoHint = 0;
                            SharedConfig.stickersReorderingHintUsed = false;
                            SharedConfig.forwardingOptionsHintShown = false;
                            SharedConfig.replyingOptionsHintShown = false;
                            SharedConfig.messageSeenHintCount = 3;
                            SharedConfig.emojiInteractionsHintCount = 3;
                            SharedConfig.dayNightThemeSwitchHintCount = 3;
                            SharedConfig.fastScrollHintCount = 3;
                            SharedConfig.stealthModeSendMessageConfirm = 2;
                            SharedConfig.updateStealthModeSendMessageConfirm(2);
                            SharedConfig.setStoriesReactionsLongPressHintUsed(false);
                            SharedConfig.setStoriesIntroShown(false);
                            SharedConfig.setMultipleReactionsPromoShowed(false);
                            ChatThemeController.getInstance(UserConfig.selectedAccount).clearCache();
                            profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                            RestrictedLanguagesSelectActivity.cleanup();
                            PersistColorPalette.getInstance(UserConfig.selectedAccount).cleanup();
                            SharedPreferences prefs = profileActivity.getMessagesController().getMainSettings();
                            editor = prefs.edit();
                            editor.remove("peerColors").remove("profilePeerColors").remove("boostingappearance").remove("bizbothint").remove("movecaptionhint");
                            for (String key : prefs.getAll().keySet()) {
                                if (key.contains("show_gift_for_") || key.contains("bdayhint_") || key.contains("bdayanim_") || key.startsWith("ask_paid_message_") || key.startsWith("topicssidetabs")) {
                                    editor.remove(key);
                                }
                            }
                            editor.apply();
                            editor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
                            for (String key : MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getAll().keySet()) {
                                if (key.startsWith("dialog_bar_botver")) {
                                    editor.remove(key);
                                }
                            }
                            editor.apply();
                        } else if (which == 7) { // Call settings
                            VoIPHelper.showCallDebugSettings(profileActivity.getParentActivity());
                        } else if (which == 8) { // ?
                            SharedConfig.toggleRoundCamera16to9();
                        } else if (which == 9) { // Check app update
                            ((LaunchActivity) profileActivity.getParentActivity()).checkAppUpdate(true, null);
                        } else if (which == 10) { // Read all chats
                            profileActivity.getMessagesStorage().readAllDialogs(-1);
                        } else if (which == 11) { // Voip audio effects
                            SharedConfig.toggleDisableVoiceAudioEffects();
                        } else if (which == 12) { // Clean app update
                            SharedConfig.pendingAppUpdate = null;
                            SharedConfig.saveConfig();
                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                        } else if (which == 13) { // Reset suggestions
                            Set<String> suggestions = profileActivity.getMessagesController().pendingSuggestions;
                            suggestions.add("VALIDATE_PHONE_NUMBER");
                            suggestions.add("VALIDATE_PASSWORD");
                            profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
                        } else if (which == 14) { // WebView Cache
                            ApplicationLoader.applicationContext.deleteDatabase("webview.db");
                            ApplicationLoader.applicationContext.deleteDatabase("webviewCache.db");
                            WebStorage.getInstance().deleteAllData();
                            try {
                                WebView webView = new WebView(ApplicationLoader.applicationContext);
                                webView.clearHistory();
                                webView.destroy();
                            } catch (Exception e) {
                            }
                        } else if (which == 15) {
                            CookieManager cookieManager = CookieManager.getInstance();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                cookieManager.removeAllCookies(null);
                                cookieManager.flush();
                            }
                        } else if (which == 16) { // WebView debug
                            SharedConfig.toggleDebugWebView();
                            Toast.makeText(profileActivity.getParentActivity(), getString(SharedConfig.debugWebView ? R.string.DebugMenuWebViewDebugEnabled : R.string.DebugMenuWebViewDebugDisabled), Toast.LENGTH_SHORT).show();
                        } else if (which == 17) { // Tablet mode
                            SharedConfig.toggleForceDisableTabletMode();

                            Activity activity = AndroidUtilities.findActivity(profileActivity.getContext());
                            final PackageManager pm = activity.getPackageManager();
                            final Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
                            activity.finishAffinity(); // Finishes all activities.
                            activity.startActivity(intent);    // Start the launch activity
                            System.exit(0);
                        } else if (which == 18) {
                            FloatingDebugController.setActive((LaunchActivity) profileActivity.getParentActivity(), !FloatingDebugController.isActive());
                        } else if (which == 19) {
                            profileActivity.getMessagesController().loadAppConfig();
                            TLRPC.TL_help_dismissSuggestion req = new TLRPC.TL_help_dismissSuggestion();
                            req.suggestion = "VALIDATE_PHONE_NUMBER";
                            req.peer = new TLRPC.TL_inputPeerEmpty();
                            profileActivity.getConnectionsManager().sendRequest(req, (response, error) -> {
                                TLRPC.TL_help_dismissSuggestion req2 = new TLRPC.TL_help_dismissSuggestion();
                                req2.suggestion = "VALIDATE_PASSWORD";
                                req2.peer = new TLRPC.TL_inputPeerEmpty();
                                profileActivity.getConnectionsManager().sendRequest(req2, (res2, err2) -> {
                                    profileActivity.getMessagesController().loadAppConfig();
                                });
                            });
                        } else if (which == 20) {
                            int androidVersion = Build.VERSION.SDK_INT;
                            int cpuCount = ConnectionsManager.CPU_COUNT;
                            int memoryClass = ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
                            long minFreqSum = 0, minFreqCount = 0;
                            long maxFreqSum = 0, maxFreqCount = 0;
                            long curFreqSum = 0, curFreqCount = 0;
                            long capacitySum = 0, capacityCount = 0;
                            StringBuilder cpusInfo = new StringBuilder();
                            for (int i = 0; i < cpuCount; i++) {
                                Long minFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_min_freq");
                                Long curFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_cur_freq");
                                Long maxFreq = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                                Long capacity = AndroidUtilities.getSysInfoLong("/sys/devices/system/cpu/cpu" + i + "/cpu_capacity");
                                cpusInfo.append("#").append(i).append(" ");
                                if (minFreq != null) {
                                    cpusInfo.append("min=").append(minFreq / 1000L).append(" ");
                                    minFreqSum += (minFreq / 1000L);
                                    minFreqCount++;
                                }
                                if (curFreq != null) {
                                    cpusInfo.append("cur=").append(curFreq / 1000L).append(" ");
                                    curFreqSum += (curFreq / 1000L);
                                    curFreqCount++;
                                }
                                if (maxFreq != null) {
                                    cpusInfo.append("max=").append(maxFreq / 1000L).append(" ");
                                    maxFreqSum += (maxFreq / 1000L);
                                    maxFreqCount++;
                                }
                                if (capacity != null) {
                                    cpusInfo.append("cpc=").append(capacity).append(" ");
                                    capacitySum += capacity;
                                    capacityCount++;
                                }
                                cpusInfo.append("\n");
                            }
                            StringBuilder info = new StringBuilder();
                            info.append(Build.MANUFACTURER).append(", ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(", ").append(Build.DEVICE).append(") ").append(" (android ").append(Build.VERSION.SDK_INT).append(")\n");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                info.append("SoC: ").append(Build.SOC_MANUFACTURER).append(", ").append(Build.SOC_MODEL).append("\n");
                            }
                            String gpuModel = AndroidUtilities.getSysInfoString("/sys/kernel/gpu/gpu_model");
                            if (gpuModel != null) {
                                info.append("GPU: ").append(gpuModel);
                                Long minClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_min_clock");
                                Long mminClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_mm_min_clock");
                                Long maxClock = AndroidUtilities.getSysInfoLong("/sys/kernel/gpu/gpu_max_clock");
                                if (minClock != null) {
                                    info.append(", min=").append(minClock / 1000L);
                                }
                                if (mminClock != null) {
                                    info.append(", mmin=").append(mminClock / 1000L);
                                }
                                if (maxClock != null) {
                                    info.append(", max=").append(maxClock / 1000L);
                                }
                                info.append("\n");
                            }
                            ConfigurationInfo configurationInfo = ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
                            info.append("GLES Version: ").append(configurationInfo.getGlEsVersion()).append("\n");
                            info.append("Memory: class=").append(AndroidUtilities.formatFileSize(memoryClass * 1024L * 1024L));
                            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                            ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
                            info.append(", total=").append(AndroidUtilities.formatFileSize(memoryInfo.totalMem));
                            info.append(", avail=").append(AndroidUtilities.formatFileSize(memoryInfo.availMem));
                            info.append(", low?=").append(memoryInfo.lowMemory);
                            info.append(" (threshold=").append(AndroidUtilities.formatFileSize(memoryInfo.threshold)).append(")");
                            info.append("\n");
                            info.append("Current class: ").append(SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass())).append(", measured: ").append(SharedConfig.performanceClassName(SharedConfig.measureDevicePerformanceClass()));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                info.append(", suggest=").append(Build.VERSION.MEDIA_PERFORMANCE_CLASS);
                            }
                            info.append("\n");
                            info.append(cpuCount).append(" CPUs");
                            if (minFreqCount > 0) {
                                info.append(", avgMinFreq=").append(minFreqSum / minFreqCount);
                            }
                            if (curFreqCount > 0) {
                                info.append(", avgCurFreq=").append(curFreqSum / curFreqCount);
                            }
                            if (maxFreqCount > 0) {
                                info.append(", avgMaxFreq=").append(maxFreqSum / maxFreqCount);
                            }
                            if (capacityCount > 0) {
                                info.append(", avgCapacity=").append(capacitySum / capacityCount);
                            }
                            info.append("\n").append(cpusInfo);

                            CodecUtils.listCodecs("video/avc", info);
                            CodecUtils.listCodecs("video/hevc", info);
                            CodecUtils.listCodecs("video/x-vnd.on2.vp8", info);
                            CodecUtils.listCodecs("video/x-vnd.on2.vp9", info);

                            profileActivity.showDialog(new ShareAlert(profileActivity.getParentActivity(), null, info.toString(), false, null, false) {
                                @Override
                                protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                                    if (!showToast) return;
                                    AndroidUtilities.runOnUIThread(() -> {
                                        BulletinFactory.createInviteSentBulletin(profileActivity.getParentActivity(), viewsHolder.getContentView(), dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, getThemedColor(Theme.key_undo_background), getThemedColor(Theme.key_undo_infoColor)).show();
                                    }, 250);
                                }
                            });
                        } else if (which == 21) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
                            builder2.setTitle("Force performance class");
                            int currentClass = SharedConfig.getDevicePerformanceClass();
                            int trueClass = SharedConfig.measureDevicePerformanceClass();
                            builder2.setItems(new CharSequence[]{
                                    AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? "**HIGH**" : "HIGH") + (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? " (measured)" : "")),
                                    AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? "**AVERAGE**" : "AVERAGE") + (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? " (measured)" : "")),
                                    AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_LOW ? "**LOW**" : "LOW") + (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW ? " (measured)" : ""))
                            }, (dialog2, which2) -> {
                                int newClass = 2 - which2;
                                if (newClass == trueClass) {
                                    SharedConfig.overrideDevicePerformanceClass(-1);
                                } else {
                                    SharedConfig.overrideDevicePerformanceClass(newClass);
                                }
                            });
                            builder2.setNegativeButton(getString("Cancel", R.string.Cancel), null);
                            builder2.show();
                        } else if (which == 22) {
                            SharedConfig.toggleRoundCamera();
                        } else if (which == 23) {
                            boolean enabled = DualCameraView.dualAvailableStatic(profileActivity.getContext());
                            MessagesController.getGlobalMainSettings().edit().putBoolean("dual_available", !enabled).apply();
                            try {
                                Toast.makeText(profileActivity.getParentActivity(), getString(!enabled ? R.string.DebugMenuDualOnToast : R.string.DebugMenuDualOffToast), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                            }
                        } else if (which == 24) {
                            SharedConfig.toggleSurfaceInStories();
                            for (int i = 0; i < profileActivity.getParentLayout().getFragmentStack().size(); i++) {
                                profileActivity.getParentLayout().getFragmentStack().get(i).clearSheets();
                            }
                        } else if (which == 25) {
                            SharedConfig.togglePhotoViewerBlur();
                        } else if (which == 26) {
                            SharedConfig.togglePaymentByInvoice();
                        } else if (which == 27) {
                            profileActivity.getMediaDataController().loadAttachMenuBots(false, true);
                        } else if (which == 28) {
                            SharedConfig.toggleUseCamera2(UserConfig.selectedAccount);
                        } else if (which == 29) {
                            BotBiometry.clear();
                            BotLocation.clear();
                            BotDownloads.clear();
                            SetupEmojiStatusSheet.clear();
                        } else if (which == 30) {
                            AuthTokensHelper.clearLogInTokens();
                        } else if (which == 31) {
                            SharedConfig.toggleUseNewBlur();
                        } else if (which == 32) {
                            SharedConfig.toggleBrowserAdaptableColors();
                        } else if (which == 33) {
                            SharedConfig.toggleDebugVideoQualities();
                        } else if (which == 34) {
                            SharedConfig.toggleUseSystemBoldFont();
                        } else if (which == 35) {
                            MessagesController.getInstance(UserConfig.selectedAccount).loadAppConfig(true);
                        } else if (which == 36) {
                            SharedConfig.toggleForceForumTabs();
                        }
                    });
                    builder.setNegativeButton(getString("Cancel", R.string.Cancel), null);
                    profileActivity.showDialog(builder.create());
                } else {
                    try {
                        Toast.makeText(profileActivity.getParentActivity(), getString("DebugMenuLongPress", R.string.DebugMenuLongPress), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                return true;
            } else if (position >= rowsHolder.getMembersStartRow() && position < rowsHolder.getMembersEndRow()) {
                final TLRPC.ChatParticipant participant;
                if (!attributesHolder.getSortedUsers().isEmpty()) {
                    participant = attributesHolder.getVisibleChatParticipants().get(attributesHolder.getSortedUsers().get(position - rowsHolder.getMembersStartRow()));
                } else {
                    participant = attributesHolder.getVisibleChatParticipants().get(position - rowsHolder.getMembersStartRow());
                }
                return profileActivity.onMemberClick(participant, true, view);
            } else if (position == rowsHolder.getBirthdayRow()) {
                if (adapter.editRow( view, position)) return true;
                if (attributesHolder.getUserInfo() == null) return false;
                try {
                    AndroidUtilities.addToClipboard(UserInfoActivity.birthdayString(attributesHolder.getUserInfo().birthday));
                    BulletinFactory.of(profileActivity).createCopyBulletin(getString(R.string.BirthdayCopied)).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                return true;
            } else {
                if (adapter.editRow(view, position)) return true;
                return clicksAndPress.processOnClickOrPress(position, view, view.getWidth() / 2f, (int) (view.getHeight() * .75f));
            }

    }

    public void updateExceptions(ProfileActivity profileActivity, ListAdapter listAdapter) {
        checkForAttributesHolder();
        checkForRowsHolder();
        checkForViewsHolder();
        if (!rowsHolder.isTopic() && ChatObject.isForum(attributesHolder.getCurrentChat())) {
            profileActivity.getAccountInstance().getNotificationsController().loadTopicsNotificationsExceptions(-attributesHolder.getChatId(), (topics) -> {
                ArrayList<Integer> arrayList = new ArrayList<>(topics);
                for (int i = 0; i < arrayList.size(); i++) {
                    if (profileActivity.getMessagesController().getTopicsController().findTopic(attributesHolder.getChatId(), arrayList.get(i)) == null) {
                        arrayList.remove(i);
                        i--;
                    }
                }
                attributesHolder.getNotificationsExceptionTopics().clear();
                attributesHolder.getNotificationsExceptionTopics().addAll(arrayList);

                if (rowsHolder.getNotificationsRow() >= 0 && listAdapter != null) {
                    listAdapter.notifyItemChanged(rowsHolder.getNotificationsRow());
                }
            });
        }
    }


}


