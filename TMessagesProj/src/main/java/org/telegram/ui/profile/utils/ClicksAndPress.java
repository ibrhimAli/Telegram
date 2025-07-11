package org.telegram.ui.profile.utils;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.ui.profile.builder.ProfileParams.PHONE_OPTION_CALL;
import static org.telegram.ui.profile.builder.ProfileParams.PHONE_OPTION_COPY;
import static org.telegram.ui.profile.builder.ProfileParams.PHONE_OPTION_TELEGRAM_CALL;
import static org.telegram.ui.profile.builder.ProfileParams.PHONE_OPTION_TELEGRAM_VIDEO_CALL;
import static org.telegram.ui.profile.utils.QR.isQrNeedVisible;
import static org.telegram.ui.profile.view.button.ButtonViewEnum.*;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.collection.LongSparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BillingController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Business.OpeningHoursActivity;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatNotificationsPopupWrapper;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProfileNotificationsActivity;
import org.telegram.ui.QrActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.TopicsNotifySettingsFragments;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.view.button.ButtonView;
import org.telegram.ui.profile.view.button.ButtonViewEnum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClicksAndPress {

    private final ProfileActivity profileActivity;
    private final Adapter profileAdapter;
    private AttributesComponentsHolder attributesHolder;
    private ViewComponentsHolder viewsHolder;
    private RowsAndStatusComponentsHolder rowsHolder;
    private ComponentsFactory components;
    public ClicksAndPress(ComponentsFactory componentsFactory){
        profileActivity = componentsFactory.getProfileActivity();
        profileAdapter = componentsFactory.getAdapter();
        components = componentsFactory;
        attributesHolder = componentsFactory.getAttributesComponentsHolder();
        viewsHolder = componentsFactory.getViewComponentsHolder();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
    }
    public void leaveChatPressed() {
        boolean isForum = ChatObject.isForum(attributesHolder.getCurrentChat());
        AlertsCreator.createClearOrDeleteDialogAlert(profileActivity, false, attributesHolder.getCurrentChat(), null, false, isForum, !isForum, (param) -> {
            rowsHolder.setPlayProfileAnimation(0);
            profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.closeChats);
            profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            profileActivity.finishFragment();
            profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, -attributesHolder.getCurrentChat().id, null, attributesHolder.getCurrentChat(), param);
        }, attributesHolder.getResourcesProvider());
    }

    public boolean processOnClickOrPress(final int position, final View view, final float x, final float y) {
        if (position == rowsHolder.getUsernameRow() || position == rowsHolder.getSetUsernameRow()) {
            final String username;
            final TLRPC.TL_username usernameObj;
            if (attributesHolder.getUserId() != 0) {
                final TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                String username1 = UserObject.getPublicUsername(user);
                if (user == null || username1 == null) {
                    return false;
                }
                username = username1;
                usernameObj = DialogObject.findUsername(username, user);
            } else if (attributesHolder.getChatId() != 0) {
                final TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                if (chat == null || attributesHolder.getTopicId() == 0 && !ChatObject.isPublic(chat)) {
                    return false;
                }
                username = ChatObject.getPublicUsername(chat);
                usernameObj = DialogObject.findUsername(username, chat);
            } else {
                return false;
            }
            if (attributesHolder.getUserId() == 0) {
                TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                String link;
                if (ChatObject.isPublic(chat)) {
                    link = "https://" + profileActivity.getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(chat) + (attributesHolder.getTopicId() != 0 ? "/" + attributesHolder.getTopicId() : "");
                } else {
                    link = "https://" + profileActivity.getMessagesController().linkPrefix + "/c/" + chat.id + (attributesHolder.getTopicId() != 0 ? "/" + attributesHolder.getTopicId() : "");
                }
                ShareAlert shareAlert = new ShareAlert(profileActivity.getParentActivity(), null, link, false, link, false) {
                    @Override
                    protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                        if (!showToast) return;
                        AndroidUtilities.runOnUIThread(() -> {
                            BulletinFactory.createInviteSentBulletin(profileActivity.getParentActivity(), viewsHolder.getContentView(), dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, components.getColorsUtils().getThemedColor(Theme.key_undo_background), components.getColorsUtils().getThemedColor(Theme.key_undo_infoColor)).show();
                        }, 250);
                    }
                };
                profileActivity.showDialog(shareAlert);
                if (usernameObj != null && !usernameObj.editable) {
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TL_fragment.TL_collectibleInfo info = (TL_fragment.TL_collectibleInfo) res;
                            TLObject obj;
                            if (attributesHolder.getUserId() != 0) {
                                obj = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                            } else {
                                obj = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                            }
                            final String usernameStr = "@" + usernameObj.username;
                            final String date = LocaleController.getInstance().getFormatterBoostExpired().format(new Date(info.purchase_date * 1000L));
                            final String cryptoAmount = BillingController.getInstance().formatCurrency(info.crypto_amount, info.crypto_currency);
                            final String amount = BillingController.getInstance().formatCurrency(info.amount, info.currency);
                            BulletinFactory.of(shareAlert.bulletinContainer2, attributesHolder.getResourcesProvider())
                                    .createImageBulletin(
                                            R.drawable.filled_username,
                                            AndroidUtilities.withLearnMore(AndroidUtilities.replaceTags(formatString(R.string.FragmentChannelUsername, usernameStr, date, cryptoAmount, TextUtils.isEmpty(amount) ? "" : "("+amount+")")), () -> {
                                                Bulletin.hideVisible();
                                                Browser.openUrl(profileActivity.getContext(), info.url);
                                            })
                                    )
                                    .setOnClickListener(v -> {
                                        Bulletin.hideVisible();
                                        Browser.openUrl(profileActivity.getContext(), info.url);
                                    })
                                    .show(false);
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    profileActivity.getConnectionsManager().bindRequestToGuid(reqId, profileActivity.getClassGuid());
                }
            } else {
                if (profileAdapter.editRow(view, position)) return true;

                if (usernameObj != null && !usernameObj.editable) {
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (attributesHolder.getUserId() != 0) {
                                obj = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                            } else {
                                obj = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                            }
                            FragmentUsernameBottomSheet.open(profileActivity.getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, profileActivity.getResourceProvider());
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    profileActivity.getConnectionsManager().bindRequestToGuid(reqId, profileActivity.getClassGuid());
                    return true;
                }

                try {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    String text = "@" + username;
                    BulletinFactory.of(profileActivity).createCopyBulletin(LocaleController.getString(R.string.UsernameCopied), attributesHolder.getResourcesProvider()).show();
                    android.content.ClipData clip = android.content.ClipData.newPlainText("label", text);
                    clipboard.setPrimaryClip(clip);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            return true;
        } else if (position == rowsHolder.getPhoneRow() || position == rowsHolder.getNumberRow()) {
            if (profileAdapter.editRow(view, position)) return true;

            final TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (user == null || user.phone == null || user.phone.length() == 0 || profileActivity.getParentActivity() == null) {
                return false;
            }
            if (position == rowsHolder.getPhoneRow() && user.phone.startsWith("888")) {
                TL_fragment.TL_inputCollectiblePhone input = new TL_fragment.TL_inputCollectiblePhone();
                final String phone = input.phone = user.phone;
                TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                req.collectible = input;
                int reqId = profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res instanceof TL_fragment.TL_collectibleInfo) {
                        FragmentUsernameBottomSheet.open(profileActivity.getContext(), FragmentUsernameBottomSheet.TYPE_PHONE, phone, user, (TL_fragment.TL_collectibleInfo) res, profileActivity.getResourceProvider());
                    } else {
                        BulletinFactory.showError(err);
                    }
                }));
                profileActivity.getConnectionsManager().bindRequestToGuid(reqId, profileActivity.getClassGuid());
                return true;
            }

            ArrayList<CharSequence> items = new ArrayList<>();
            ArrayList<Integer> actions = new ArrayList<>();
            List<Integer> icons = new ArrayList<>();
            if (position == rowsHolder.getPhoneRow()) {
                if (attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().phone_calls_available) {
                    icons.add(R.drawable.msg_calls);
                    items.add(LocaleController.getString(R.string.CallViaTelegram));
                    actions.add(PHONE_OPTION_TELEGRAM_CALL);
                    if (Build.VERSION.SDK_INT >= 18 && attributesHolder.getUserInfo().video_calls_available) {
                        icons.add(R.drawable.msg_videocall);
                        items.add(LocaleController.getString(R.string.VideoCallViaTelegram));
                        actions.add(PHONE_OPTION_TELEGRAM_VIDEO_CALL);
                    }
                }
                if (!rowsHolder.isFragmentPhoneNumber()) {
                    icons.add(R.drawable.msg_calls_regular);
                    items.add(LocaleController.getString(R.string.Call));
                    actions.add(PHONE_OPTION_CALL);
                }
            }
            icons.add(R.drawable.msg_copy);
            items.add(LocaleController.getString(R.string.Copy));
            actions.add(PHONE_OPTION_COPY);

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(profileActivity.getContext(), R.drawable.popup_fixed_alert, attributesHolder.getResourcesProvider()) {
                final Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            for (int i = 0; i < icons.size(); i++) {
                int action = actions.get(i);
                ActionBarMenuItem.addItem(popupLayout, icons.get(i), items.get(i), false, attributesHolder.getResourcesProvider()).setOnClickListener(v -> {
                    popupWindowRef.get().dismiss();
                    switch (action) {
                        case PHONE_OPTION_CALL:
                            try {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + user.phone));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                profileActivity.getParentActivity().startActivityForResult(intent, 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_COPY:
                            try {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("label", "+" + user.phone);
                                clipboard.setPrimaryClip(clip);
                                if (AndroidUtilities.shouldShowClipboardToast()) {
                                    BulletinFactory.of(profileActivity).createCopyBulletin(LocaleController.getString(R.string.PhoneCopied)).show();
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_TELEGRAM_CALL:
                        case PHONE_OPTION_TELEGRAM_VIDEO_CALL:
                            if (profileActivity.getParentActivity() == null) {
                                return;
                            }
                            VoIPHelper.startCall(user, action == PHONE_OPTION_TELEGRAM_VIDEO_CALL, attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().video_calls_available, profileActivity.getParentActivity(), attributesHolder.getUserInfo(), profileActivity.getAccountInstance());
                            break;
                    }
                });
            }
            if (rowsHolder.isFragmentPhoneNumber()) {
                FrameLayout gap = new FrameLayout(profileActivity.getContext());
                gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, attributesHolder.getResourcesProvider()));
                popupLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

                TextView fragmentInfoView = new TextView(profileActivity.getContext());
                fragmentInfoView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
                fragmentInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                fragmentInfoView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, attributesHolder.getResourcesProvider()));
                fragmentInfoView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText, attributesHolder.getResourcesProvider()));
                fragmentInfoView.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector, attributesHolder.getResourcesProvider()), 0,6));

                SpannableStringBuilder spanned = new SpannableStringBuilder(AndroidUtilities.replaceTags(LocaleController.getString(R.string.AnonymousNumberNotice)));

                int startIndex = TextUtils.indexOf(spanned, '*');
                int lastIndex = TextUtils.lastIndexOf(spanned, '*');
                if (startIndex != -1 && lastIndex != -1 && startIndex != lastIndex) {
                    spanned.replace(lastIndex, lastIndex + 1, "");
                    spanned.replace(startIndex, startIndex + 1, "");
                    spanned.setSpan(new TypefaceSpan(AndroidUtilities.bold()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanned.setSpan(new ForegroundColorSpan(fragmentInfoView.getLinkTextColors().getDefaultColor()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                fragmentInfoView.setText(spanned);
                fragmentInfoView.setOnClickListener(v -> {
                    try {
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fragment.com")));
                    } catch (ActivityNotFoundException e) {
                        FileLog.e(e);
                    }
                });

                gap.setTag(R.id.fit_width_tag, 1);
                fragmentInfoView.setTag(R.id.fit_width_tag, 1);
                popupLayout.addView(fragmentInfoView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            }

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != profileActivity.getFragmentView() && v != null) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = profileActivity.getParentLayout().getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(profileActivity.getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
        } else if (position == rowsHolder.getChannelInfoRow() || position == rowsHolder.getUserInfoRow() || position == rowsHolder.getLocationRow() || position == rowsHolder.getBioRow()) {
            if (position == rowsHolder.getBioRow() && (attributesHolder.getUserInfo() == null || TextUtils.isEmpty(attributesHolder.getUserInfo().about))) {
                return false;
            }
            if (profileAdapter.editRow(view, position)) return true;
            if (view instanceof AboutLinkCell && ((AboutLinkCell) view).onClick()) {
                return false;
            }
            String text;
            if (position == rowsHolder.getLocationRow()) {
                text = attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().location instanceof TLRPC.TL_channelLocation ? ((TLRPC.TL_channelLocation) attributesHolder.getChatInfo().location).address : null;
            } else if (position == rowsHolder.getChannelInfoRow()) {
                text = attributesHolder.getChatInfo() != null ? attributesHolder.getChatInfo().about : null;
            } else {
                text = attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().about : null;
            }
            final String finalText = text;
            if (TextUtils.isEmpty(finalText)) {
                return false;
            }
            final String[] fromLanguage = new String[1];
            fromLanguage[0] = "und";
            final boolean translateButtonEnabled = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().isContextTranslateEnabled();
            final boolean[] withTranslate = new boolean[1];
            withTranslate[0] = position == rowsHolder.getBioRow() || position == rowsHolder.getChannelInfoRow() || position == rowsHolder.getUserInfoRow();
            final String toLang = LocaleController.getInstance().getCurrentLocale().getLanguage();
            Runnable showMenu = () -> {
                if (profileActivity.getParentActivity() == null) {
                    return;
                }
                CharSequence[] items = withTranslate[0] ? new CharSequence[]{LocaleController.getString(R.string.Copy), LocaleController.getString(R.string.TranslateMessage)} : new CharSequence[]{LocaleController.getString(R.string.Copy)};
                int[] icons = withTranslate[0] ? new int[] {R.drawable.msg_copy, R.drawable.msg_translate} : new int[] {R.drawable.msg_copy};

                AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
                ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(profileActivity.getContext(), R.drawable.popup_fixed_alert, attributesHolder.getResourcesProvider()) {
                    final Path path = new Path();

                    @Override
                    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                        canvas.save();
                        path.rewind();
                        AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                        path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                        canvas.clipPath(path);
                        boolean draw = super.drawChild(canvas, child, drawingTime);
                        canvas.restore();
                        return draw;
                    }
                };
                popupLayout.setFitItems(true);

                for (int i = 0; i < icons.length; i++) {
                    int j = i;
                    ActionBarMenuItem.addItem(popupLayout, icons[i], items[i], false, attributesHolder.getResourcesProvider()).setOnClickListener(v -> {
                        popupWindowRef.get().dismiss();
                        try {
                            if (j == 0) {
                                AndroidUtilities.addToClipboard(finalText);
                                if (position == rowsHolder.getBioRow()) {
                                    BulletinFactory.of(profileActivity).createCopyBulletin(LocaleController.getString(R.string.BioCopied)).show();
                                } else {
                                    BulletinFactory.of(profileActivity).createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
                                }
                            } else if (j == 1) {
                                TranslateAlert2.showAlert(profileActivity.fragmentView.getContext(), profileActivity, UserConfig.selectedAccount, fromLanguage[0], toLang, finalText, null, false, span -> {
                                    if (span != null) {
                                        URLs.openUrl(profileActivity, span.getURL(), null);
                                        return true;
                                    }
                                    return false;
                                }, null);
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });
                }

                ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
                popupWindow.setPauseNotifications(true);
                popupWindow.setDismissAnimationDuration(220);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setClippingEnabled(true);
                popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
                popupWindow.setFocusable(true);
                popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
                popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popupWindow.getContentView().setFocusableInTouchMode(true);
                popupWindowRef.set(popupWindow);

                float px = x, py = y;
                View v = view;
                while (v != null && v != profileActivity.getFragmentView()) {
                    px += v.getX();
                    py += v.getY();
                    v = (View) v.getParent();
                }
                if (AndroidUtilities.isTablet()) {
                    View pv = profileActivity.getParentLayout().getView();
                    if (pv != null) {
                        px += pv.getX() + pv.getPaddingLeft();
                        py += pv.getY() + pv.getPaddingTop();
                    }
                }
                px -= popupLayout.getMeasuredWidth() / 2f;
                popupWindow.showAtLocation(profileActivity.getFragmentView(), 0, (int) px, (int) py);
                popupWindow.dimBehind();
            };
            if (withTranslate[0]) {
                if (LanguageDetector.hasSupport()) {
                    LanguageDetector.detectLanguage(finalText, (fromLang) -> {
                        fromLanguage[0] = fromLang;
                        withTranslate[0] = fromLang != null && (!fromLang.equals(toLang) || fromLang.equals("und")) && (
                                translateButtonEnabled && !RestrictedLanguagesSelectActivity.getRestrictedLanguages().contains(fromLang) ||
                                        (attributesHolder.getCurrentChat() != null && (attributesHolder.getCurrentChat().has_link || ChatObject.isPublic(attributesHolder.getCurrentChat()))) && ("uk".equals(fromLang) || "ru".equals(fromLang)));
                        showMenu.run();
                    }, (error) -> {
                        FileLog.e("mlkit: failed to detect language in selection", error);
                        showMenu.run();
                    });
                } else {
                    showMenu.run();
                }
            } else {
                showMenu.run();
            }
            return true;
        } else if (position == rowsHolder.getBizHoursRow() || position == rowsHolder.getBizLocationRow()) {
            if (profileActivity.getParentActivity() == null || attributesHolder.getUserInfo() == null) {
                return false;
            }
            final String finalText;
            if (position == rowsHolder.getBizHoursRow()) {
                if (attributesHolder.getUserInfo().business_work_hours == null) return false;
                finalText = OpeningHoursActivity.toString(UserConfig.selectedAccount, attributesHolder.getUserInfo().user, attributesHolder.getUserInfo().business_work_hours);
            } else if (position == rowsHolder.getBizLocationRow()) {
                if (profileAdapter.editRow(view, position)) return true;
                if (attributesHolder.getUserInfo().business_location == null) return false;
                finalText = attributesHolder.getUserInfo().business_location.address;
            } else return true;

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(profileActivity.getContext(), R.drawable.popup_fixed_alert, attributesHolder.getResourcesProvider()) {
                final Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            ActionBarMenuItem.addItem(popupLayout, R.drawable.msg_copy, LocaleController.getString(R.string.Copy), false, attributesHolder.getResourcesProvider()).setOnClickListener(v -> {
                popupWindowRef.get().dismiss();
                try {
                    AndroidUtilities.addToClipboard(finalText);
                    if (position == rowsHolder.getBizHoursRow()) {
                        BulletinFactory.of(profileActivity).createCopyBulletin(LocaleController.getString(R.string.BusinessHoursCopied)).show();
                    } else {
                        BulletinFactory.of(profileActivity).createCopyBulletin(LocaleController.getString(R.string.BusinessLocationCopied)).show();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            });

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != null && v != profileActivity.getFragmentView()) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = profileActivity.getParentLayout().getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(profileActivity.getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
        }
        return false;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void onProfileButtonClicked(ButtonViewEnum button) {
        checkForHolders();
        Context context = profileActivity.getContext();
        BaseFragment lastFragment = profileActivity.getParentLayout().getLastFragment();

        switch (button) {
            case CHANGE_AVATAR:
            case MESSAGE:
            case DISCUSS:
                components.getOpenAndWrite().onWriteButtonClick();
                break;
            case QR_CODE:
                Bundle args = new Bundle();
                args.putLong("chat_id", attributesHolder.getChatId());
                args.putLong("user_id", attributesHolder.getUserId());
                profileActivity.presentFragment(new QrActivity(args));
                break;
            case JOIN:
                profileActivity.getMessagesController().addUserToChat(attributesHolder.getCurrentChat().id, profileActivity.getUserConfig().getCurrentUser(), 0, null, profileActivity, true, () -> {
                    profileAdapter.updateRowsIds();
                    if (components.getListAdapter() != null) {
                        components.getListAdapter().notifyDataSetChanged();
                    }
                }, err -> {
                    if (err != null && "INVITE_REQUEST_SENT".equals(err.text)) {
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(profileActivity.getCurrentAccount());
                        preferences.edit().putLong("dialog_join_requested_time_" + attributesHolder.getDialogId(), System.currentTimeMillis()).commit();
                        JoinGroupAlert.showBulletin(context, profileActivity, ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup);
                        profileAdapter.updateRowsIds();
                        if (components.getListAdapter() != null) {
                            components.getListAdapter().notifyDataSetChanged();
                        }
                        if (lastFragment instanceof ChatActivity) {
                            ((ChatActivity) lastFragment).showBottomOverlayProgress(false, true);
                        }
                        return false;
                    }
                    return true;
                });
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction);
                break;
            case LIVE_STREAM:
                if (attributesHolder.getChatId() != 0) {
                    ChatObject.Call call = profileActivity.getMessagesController().getGroupCall(attributesHolder.getChatId(), false);
                    if (call == null) {
                        VoIPHelper.showGroupCallAlert(profileActivity, attributesHolder.getCurrentChat(), null, false, profileActivity.getAccountInstance());
                    } else {
                        VoIPHelper.startCall(attributesHolder.getCurrentChat(), null, null, false, profileActivity.getParentActivity(), profileActivity, profileActivity.getAccountInstance());
                    }
                }
                break;
            case MUTE:
            case UNMUTE: {
                long did = profileActivity.getDialogId();
                View v = viewsHolder.getButtonsGroup().getButtonView(button);
                float x = v.getWidth() / 2f;
                float y = v.getHeight() / 2f;
                ChatNotificationsPopupWrapper chatNotificationsPopupWrapper = new ChatNotificationsPopupWrapper(context, profileActivity.getCurrentAccount(), null, true, true, new ChatNotificationsPopupWrapper.Callback() {
                    @Override
                    public void toggleSound() {
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(profileActivity.getCurrentAccount());
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
                            components.getListViewClick().updateExceptions(profileActivity, components.getListAdapter());
                            updateProfileButtons(true);
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
                        components.getListViewClick().updateExceptions(profileActivity, components.getListAdapter());
                        updateProfileButtons(true);
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
                    View dv = profileActivity.getParentLayout().getView();
                    x += dv.getX() + dv.getPaddingLeft();
                    y += dv.getY() + dv.getPaddingTop();
                }
                chatNotificationsPopupWrapper.showAsOptions(profileActivity, v, x, y);
                break;
            }
            case ADD_STORY:
//                viewsHolder.openStoryRecorder(storyRecorder -> storyRecorder.open(null));
                viewsHolder.getSharedMediaLayout().openStoryRecorder();
                break;
            case CALL:
            case VIDEO:
                if (attributesHolder.getUserId() != 0) {
                    TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                    if (user != null) {
                        VoIPHelper.startCall(user, button == VIDEO, attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().video_calls_available, profileActivity.getParentActivity(), attributesHolder.getUserInfo(), profileActivity.getAccountInstance());
                    }
                } else if (attributesHolder.getChatId() != 0) {
                    ChatObject.Call call = profileActivity.getMessagesController().getGroupCall(attributesHolder.getChatId(), false);
                    if (call == null) {
                        VoIPHelper.showGroupCallAlert(profileActivity, attributesHolder.getCurrentChat(), null, false, profileActivity.getAccountInstance());
                    } else {
                        VoIPHelper.startCall(attributesHolder.getCurrentChat(), null, null, false, profileActivity.getParentActivity(), profileActivity, profileActivity.getAccountInstance());
                    }
                }
                break;
            case GIFT:
                if (UserObject.areGiftsDisabled(attributesHolder.getUserInfo())) {
                    BaseFragment lastFragmentSafe = LaunchActivity.getSafeLastFragment();
                    if (lastFragmentSafe != null) {
                        BulletinFactory.of(lastFragmentSafe).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(profileActivity.getDialogId())))).show();
                    }
                    return;
                }
                if (attributesHolder.getCurrentChat() != null) {
                    MessagesController.getGlobalMainSettings().edit().putInt("channelgifthint", 3).apply();
                }
                profileActivity.showDialog(new GiftSheet(profileActivity.getContext(), profileActivity.getCurrentAccount(), profileActivity.getDialogId(), null, null));
                break;
            case SHARE:
                try {
                    String text = null;
                    if (attributesHolder.getUserId() != 0) {
                        TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                        if (user == null) {
                            return;
                        }
                        if (profileActivity.botInfo != null && attributesHolder.getUserInfo() != null && !TextUtils.isEmpty(attributesHolder.getUserInfo().about)) {
                            text = String.format("%s https://" + profileActivity.getMessagesController().linkPrefix + "/%s", attributesHolder.getUserInfo().about, UserObject.getPublicUsername(user));
                        } else {
                            text = String.format("https://" + profileActivity.getMessagesController().linkPrefix + "/%s", UserObject.getPublicUsername(user));
                        }
                    } else if (attributesHolder.getChatId() != 0) {
                        TLRPC.Chat chat = profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                        if (chat == null) {
                            return;
                        }
                        if (attributesHolder.getChatInfo() != null && !TextUtils.isEmpty(attributesHolder.getChatInfo().about)) {
                            text = String.format("%s\nhttps://" + profileActivity.getMessagesController().linkPrefix + "/%s", attributesHolder.getChatInfo().about, ChatObject.getPublicUsername(chat));
                        } else {
                            text = String.format("https://" + profileActivity.getMessagesController().linkPrefix + "/%s", ChatObject.getPublicUsername(chat));
                        }
                    }
                    if (TextUtils.isEmpty(text)) {
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    profileActivity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString(R.string.BotShare)), 500);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                break;
            case BLOCK:
                TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                if (user == null) {
                    return;
                }
                if (!rowsHolder.isBot() || MessagesController.isSupportUser(user)) {
                    if (rowsHolder.isUserBlocked()) {
                        profileActivity.getMessagesController().unblockPeer(attributesHolder.getUserId());
                        if (BulletinFactory.canShowBulletin(profileActivity)) {
                            BulletinFactory.createBanBulletin(profileActivity, false).show();
                        }
                    } else {
                        if (rowsHolder.isReportSpam()) {
                            AlertsCreator.showBlockReportSpamAlert(profileActivity, attributesHolder.getUserId(), user, null, attributesHolder.getCurrentEncryptedChat(), false, null, param -> {
                                if (param == 1) {
                                    profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.closeChats);
                                    profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                    rowsHolder.setPlayProfileAnimation(0);
                                    profileActivity.finishFragment();
                                } else {
                                    profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.peerSettingsDidLoad, attributesHolder.getUserId());
                                }
                            }, attributesHolder.getResourcesProvider());
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.getParentActivity(), attributesHolder.getResourcesProvider());
                            builder.setTitle(LocaleController.getString(R.string.BlockUser));
                            builder.setMessage(AndroidUtilities.replaceTags(formatString("AreYouSureBlockContact2", R.string.AreYouSureBlockContact2, ContactsController.formatName(user.first_name, user.last_name))));
                            builder.setPositiveButton(LocaleController.getString(R.string.BlockContact), (dialogInterface, i) -> {
                                profileActivity.getMessagesController().blockPeer(attributesHolder.getUserId());
                                if (BulletinFactory.canShowBulletin(profileActivity)) {
                                    BulletinFactory.createBanBulletin(profileActivity, true).show();
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                            AlertDialog dialog = builder.create();
                            profileActivity.showDialog(dialog);
                            TextView buttonView = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if (buttonView != null) {
                                buttonView.setTextColor(components.getColorsUtils().getThemedColor(Theme.key_text_RedBold));
                            }
                        }
                    }
                } else {
                    if (!rowsHolder.isUserBlocked()) {
                        AlertsCreator.createClearOrDeleteDialogAlert(profileActivity, false, attributesHolder.getCurrentChat(), user, attributesHolder.getCurrentEncryptedChat() != null, true, true, (param) -> {
                            if (profileActivity.getParentLayout() != null) {
                                List<BaseFragment> fragmentStack = profileActivity.getParentLayout().getFragmentStack();
                                BaseFragment prevFragment = fragmentStack == null || fragmentStack.size() < 2 ? null : fragmentStack.get(fragmentStack.size() - 2);
                                if (prevFragment instanceof ChatActivity) {
                                    profileActivity.getParentLayout().removeFragmentFromStack(fragmentStack.size() - 2);
                                }
                            }
                            profileActivity.finishFragment();
                            profileActivity.getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, attributesHolder.getDialogId(), user, attributesHolder.getCurrentChat(), param);
                        }, profileActivity.getResourceProvider());
                    } else {
                        profileActivity.getMessagesController().unblockPeer(attributesHolder.getUserId(), ()-> profileActivity.getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of("/start", attributesHolder.getUserId(), null, null, null, false, null, null, null, true, 0, null, false)));
                        profileActivity.finishFragment();
                    }
                }
                break;
            case REPORT:
                ReportBottomSheet.openChat(profileActivity, profileActivity.getDialogId());
                break;
            case LEAVE:
                leaveChatPressed();
                break;
        }
    }

    public void checkForHolders() {
        checkForViewHolder();
        checkForRowsHolder();
        checkForAttributesHolder();
    }
    private void checkForAttributesHolder() {
        if (attributesHolder == null) {
            attributesHolder = components.getAttributesComponentsHolder();
        }
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }

    private void checkForViewHolder() {
        if (viewsHolder == null) {
            viewsHolder = components.getViewComponentsHolder();
        }
    }

    public void updateProfileButtons(boolean animate) {
        checkForHolders();
        List<ButtonViewEnum> buttons = new ArrayList<>();
        if (attributesHolder.getUserId() == profileActivity.getUserConfig().clientUserId) {
            buttons.add(ButtonViewEnum.CHANGE_AVATAR);
            if (isQrNeedVisible()) {
                buttons.add(ButtonViewEnum.QR_CODE);
            }
        }
        if (attributesHolder.getUserId() != 0 && attributesHolder.getUserId() != profileActivity.getUserConfig().clientUserId || attributesHolder.getChatId() != 0 && ChatObject.isForum(attributesHolder.getCurrentChat())) {
            buttons.add(ButtonViewEnum.MESSAGE);
        }
        if (attributesHolder.getChatId() != 0 && ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup && attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().linked_chat_id != 0 && rowsHolder.getInfoHeaderRow() != -1) {
            buttons.add(ButtonViewEnum.DISCUSS);
        }
        if (attributesHolder.getChatId() != 0 && ChatObject.isChannel(attributesHolder.getCurrentChat()) && attributesHolder.getCurrentChat().left && !attributesHolder.getCurrentChat().kicked) {
            long requestedTime = MessagesController.getNotificationsSettings(profileActivity.getCurrentAccount()).getLong("dialog_join_requested_time_" + attributesHolder.getDialogId(), -1);
            if (!(requestedTime > 0 && System.currentTimeMillis() - requestedTime < 1000 * 60 * 2)) {
                buttons.add(ButtonViewEnum.JOIN);
            }
        }
        if (attributesHolder.getChatInfo() != null) {
            if (ChatObject.canManageCalls(attributesHolder.getCurrentChat()) && attributesHolder.getChatInfo().call == null || profileActivity.getMessagesController().getGroupCall(attributesHolder.getChatId(), false) != null) {
                buttons.add(ButtonViewEnum.LIVE_STREAM);
            }
        }
        if (attributesHolder.getUserId() == 0 || attributesHolder.getUserId() != profileActivity.getUserConfig().clientUserId) {
            buttons.add(isNotificationsEnabled() ? ButtonViewEnum.MUTE : ButtonViewEnum.UNMUTE);
        }
        StoriesController storiesController = profileActivity.getMessagesController().getStoriesController();
        if ((attributesHolder.getUserId() == profileActivity.getUserConfig().clientUserId && (profileActivity.getMessagesController().storiesEnabled() || !profileActivity.getMessagesController().premiumPurchaseBlocked())) || storiesController.canPostStories(profileActivity.getDialogId())) {
            buttons.add(ButtonViewEnum.ADD_STORY);
            profileActivity.checkCanSendStoryForPosting();
        }

        if (attributesHolder.getUserInfo() != null) {
            if (attributesHolder.getUserInfo().phone_calls_available) {
                buttons.add(ButtonViewEnum.CALL);
                if (attributesHolder.getUserInfo().video_calls_available) {
                    buttons.add(ButtonViewEnum.VIDEO);
                }
            }

            TLRPC.User user = profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
            if (!BuildVars.IS_BILLING_UNAVAILABLE && !user.self && !user.bot && !MessagesController.isSupportUser(user) && !profileActivity.getMessagesController().premiumPurchaseBlocked()) {
                buttons.add(ButtonViewEnum.GIFT);
            }
        }
        if (attributesHolder.getChatId() != 0 && ChatObject.isChannel(attributesHolder.getCurrentChat()) && !BuildVars.IS_BILLING_UNAVAILABLE && !profileActivity.getMessagesController().premiumPurchaseBlocked()) {
            StarsController.getInstance(profileActivity.getCurrentAccount()).loadStarGifts();
            if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().stargifts_available) {
                buttons.add(ButtonViewEnum.GIFT);
            }
        }

        if (attributesHolder.getChatId() != 0 && ChatObject.isPublic(attributesHolder.getCurrentChat()) || rowsHolder.isBot()) {
            buttons.add(ButtonViewEnum.SHARE);
        }
        if (attributesHolder.getUserId() != 0 && !rowsHolder.isUserBlocked() && rowsHolder.isBot() && !MessagesController.isSupportUser(profileActivity.getMessagesController().getUser(attributesHolder.getUserId())) && profileActivity.getDialogId() != UserObject.VERIFY) {
            buttons.add(ButtonViewEnum.BLOCK);
        }

        long chatInviterId = 0;
        if (attributesHolder.getCurrentChat() != null && attributesHolder.getChatInfo() != null && !ChatObject.isNotInChat(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().creator) {
            if (attributesHolder.getChatInfo().inviterId != 0) {
                chatInviterId = attributesHolder.getChatInfo().inviterId;
            } else if (attributesHolder.getChatInfo().participants != null) {
                if (attributesHolder.getChatInfo().participants.self_participant != null) {
                    chatInviterId = attributesHolder.getChatInfo().participants.self_participant.inviter_id;
                } else {
                    long selfId = profileActivity.getUserConfig().getClientUserId();
                    for (int a = 0, N = attributesHolder.getChatInfo().participants.participants.size(); a < N; a++) {
                        TLRPC.ChatParticipant participant = attributesHolder.getChatInfo().participants.participants.get(a);
                        if (participant.user_id == selfId) {
                            chatInviterId = participant.inviter_id;
                            break;
                        }
                    }
                }
            }
        }

        if (rowsHolder.isBot() || chatInviterId != 0 && chatInviterId != profileActivity.getUserConfig().clientUserId) {
            buttons.add(ButtonViewEnum.REPORT);
        }

        if (attributesHolder.getChatId() != 0 && !attributesHolder.getCurrentChat().creator && !attributesHolder.getCurrentChat().left && !attributesHolder.getCurrentChat().kicked && !rowsHolder.isTopic()) {
            buttons.add(ButtonViewEnum.LEAVE);
        }
        if (viewsHolder.getButtonsGroup() != null) {
            viewsHolder.getButtonsGroup().setButtons(buttons, animate);
        }
    }


    private boolean isNotificationsEnabled() {
        SharedPreferences preferences = MessagesController.getNotificationsSettings(profileActivity.getCurrentAccount());
        long did;
        if (attributesHolder.getDialogId() != 0) {
            did = attributesHolder.getDialogId();
        } else if (attributesHolder.getUserId() != 0) {
            did = attributesHolder.getUserId();
        } else {
            did = -attributesHolder.getChatId();
        }

        String key = NotificationsController.getSharedPrefKey(did, attributesHolder.getTopicId());
        boolean custom = preferences.getBoolean("custom_" + key, false);
        boolean hasOverride = preferences.contains("notify2_" + key);
        int value = preferences.getInt("notify2_" + key, 0);
        int delta = preferences.getInt("notifyuntil_" + key, 0);
        if (value == 3 && delta != Integer.MAX_VALUE) {
            delta -= profileActivity.getConnectionsManager().getCurrentTime();
            return delta <= 0;
        } else {
            if (value == 0) {
                if (hasOverride) {
                    return true;
                } else {
                    return profileActivity.getAccountInstance().getNotificationsController().isGlobalNotificationsEnabled(did, false, false);
                }
            } else if (value == 1) {
                return true;
            }
        }
        return false;
    }
}
