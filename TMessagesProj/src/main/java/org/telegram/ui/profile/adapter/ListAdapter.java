package org.telegram.ui.profile.adapter;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Stars.StarsIntroActivity.formatStarsAmountShort;
import static org.telegram.ui.bots.AffiliateProgramFragment.percents;
import static org.telegram.ui.profile.utils.ColorsUtils.dontApplyPeerColor;
import static org.telegram.ui.profile.utils.ColorsUtils.getThemedColor;
import static org.telegram.ui.profile.utils.URLs.openUrl;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Business.ProfileLocationCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChannelMonetizationLayout;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.Premium.ProfilePremiumCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.TwoStepVerificationSetupActivity;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;
import org.telegram.ui.profile.builder.AttributesComponentsHolder;
import org.telegram.ui.profile.builder.ProfileParams;
import org.telegram.ui.profile.builder.RowsAndStatusComponentsHolder;
import org.telegram.ui.profile.builder.ViewComponentsHolder;
import org.telegram.ui.profile.utils.Adapter;
import org.telegram.ui.profile.builder.ComponentsFactory;
import org.telegram.ui.profile.utils.Loaders;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ListAdapter extends RecyclerListView.SelectionAdapter {
    public final static int VIEW_TYPE_HEADER = 1,
            VIEW_TYPE_TEXT_DETAIL = 2,
            VIEW_TYPE_ABOUT_LINK = 3,
            VIEW_TYPE_TEXT = 4,
            VIEW_TYPE_DIVIDER = 5,
            VIEW_TYPE_NOTIFICATIONS_CHECK = 6,
            VIEW_TYPE_SHADOW = 7,
            VIEW_TYPE_USER = 8,
            VIEW_TYPE_EMPTY = 11,
            VIEW_TYPE_BOTTOM_PADDING = 12,
            VIEW_TYPE_SHARED_MEDIA = 13,
            VIEW_TYPE_VERSION = 14,
            VIEW_TYPE_SUGGESTION = 15,
            VIEW_TYPE_ADDTOGROUP_INFO = 17,
            VIEW_TYPE_PREMIUM_TEXT_CELL = 18,
            VIEW_TYPE_TEXT_DETAIL_MULTILINE = 19,
            VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE = 20,
            VIEW_TYPE_LOCATION = 21,
            VIEW_TYPE_HOURS = 22,
            VIEW_TYPE_CHANNEL = 23,
            VIEW_TYPE_STARS_TEXT_CELL = 24,
            VIEW_TYPE_BOT_APP = 25,
            VIEW_TYPE_SHADOW_TEXT = 26,
            VIEW_TYPE_COLORFUL_TEXT = 27;


    
  
    private final ProfileActivity profileActivity;
    private final Adapter profileAdapter;
    private final ComponentsFactory components;
    private final ViewComponentsHolder viewComponentsHolder;
    private RecyclerListView listView;
    private RowsAndStatusComponentsHolder rowsHolder;
    private final AttributesComponentsHolder attributesHolder;
    public ListAdapter(ComponentsFactory componentsFactory) {
        profileActivity = componentsFactory.getProfileActivity();
        components = componentsFactory;
        profileAdapter = componentsFactory.getAdapter();
        listView = componentsFactory.getListView().getClippedList();
        viewComponentsHolder = components.getViewComponentsHolder();
        attributesHolder = components.getAttributesComponentsHolder();
        rowsHolder = componentsFactory.getRowsAndStatusComponentsHolder();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (listView == null) {
            listView = components.getListView().getClippedList();
        }
        View view;
        switch (viewType) {
            case VIEW_TYPE_HEADER: {
                view = new HeaderCell(profileActivity.getContext(), 23, attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
            case VIEW_TYPE_TEXT_DETAIL:
                final TextDetailCell textDetailCell = new TextDetailCell(profileActivity.getContext(), attributesHolder.getResourcesProvider(), viewType == VIEW_TYPE_TEXT_DETAIL_MULTILINE) {
                    @Override
                    protected int processColor(int color) {
                        return dontApplyPeerColor(color, false);
                    }
                };
                textDetailCell.setContentDescriptionValueFirst(true);
                view = textDetailCell;
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            case VIEW_TYPE_ABOUT_LINK: {
                view = new AboutLinkCell(profileActivity.getContext(), profileActivity, attributesHolder.getResourcesProvider()) {
                    @Override
                    protected void didPressUrl(String url, Browser.Progress progress) {
                        openUrl(profileActivity, url, progress);
                    }

                    @Override
                    protected void didResizeEnd() {
                        viewComponentsHolder.getLayoutManager().mIgnoreTopPadding = false;
                    }

                    @Override
                    protected void didResizeStart() {
                        viewComponentsHolder.getLayoutManager().mIgnoreTopPadding = true;
                    }

                    @Override
                    protected int processColor(int color) {
                        return dontApplyPeerColor(color, false);
                    }
                };
                viewComponentsHolder.setAboutLinkCell((AboutLinkCell) view);
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_TEXT: {
                view = new TextCell(profileActivity.getContext(), attributesHolder.getResourcesProvider()) {
                    @Override
                    protected int processColor(int color) {
                        return dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_DIVIDER: {
                view = new DividerCell(profileActivity.getContext(), attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                view.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), 0, 0);
                break;
            }
            case VIEW_TYPE_NOTIFICATIONS_CHECK: {
                view = new NotificationsCheckCell(profileActivity.getContext(), 23, 70, false, attributesHolder.getResourcesProvider()) {
                    @Override
                    protected int processColor(int color) {
                        return dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE: {
                view = new TextCheckCell(profileActivity.getContext(), attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_SHADOW: {
                view = new ShadowSectionCell(profileActivity.getContext(), attributesHolder.getResourcesProvider());
                break;
            }
            case VIEW_TYPE_SHADOW_TEXT: {
                view = new TextInfoPrivacyCell(profileActivity.getContext(), attributesHolder.getResourcesProvider());
                break;
            }
            case VIEW_TYPE_COLORFUL_TEXT: {
                view = new AffiliateProgramFragment.ColorfulTextCell(profileActivity.getContext(), attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_USER: {
                view = new UserCell( profileActivity.getContext(), rowsHolder.getAddMemberRow() == -1 ? 9 : 6, 0, true, attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_EMPTY: {
                view = new View( profileActivity.getContext()) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
                    }
                };
                break;
            }
            case VIEW_TYPE_BOTTOM_PADDING: {
                view = new View( profileActivity.getContext()) {

                    private int lastPaddingHeight = 0;
                    private int lastListViewHeight = 0;

                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        if (lastListViewHeight != listView.getMeasuredHeight()) {
                            lastPaddingHeight = 0;
                        }
                        lastListViewHeight = listView.getMeasuredHeight();
                        int n = listView.getChildCount();
                        if (n == getItemCount()) {
                            int totalHeight = 0;
                            for (int i = 0; i < n; i++) {
                                View view = listView.getChildAt(i);
                                int p = listView.getChildAdapterPosition(view);
                                if (p >= 0 && p != rowsHolder.getBottomPaddingRow()) {
                                    totalHeight += listView.getChildAt(i).getMeasuredHeight();
                                }
                            }
                            int paddingHeight = (profileActivity.fragmentView == null ? 0 : profileActivity.fragmentView.getMeasuredHeight()) - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight - totalHeight;
                            if (paddingHeight > AndroidUtilities.dp(ProfileParams.VIRTUAL_TOP_BAR_HEIGHT)) {
                                paddingHeight = 0;
                            }
                            if (paddingHeight <= 0) {
                                paddingHeight = 0;
                            }
                            setMeasuredDimension(listView.getMeasuredWidth(), lastPaddingHeight = paddingHeight);
                        } else {
                            setMeasuredDimension(listView.getMeasuredWidth(), lastPaddingHeight);
                        }
                    }
                };
                view.setBackground(new ColorDrawable(Color.TRANSPARENT));
                break;
            }
            case VIEW_TYPE_SHARED_MEDIA: {
                if (viewComponentsHolder.getSharedMediaLayout().getParent() != null) {
                    ((ViewGroup) viewComponentsHolder.getSharedMediaLayout().getParent()).removeView(viewComponentsHolder.getSharedMediaLayout());
                }
                view = viewComponentsHolder.getSharedMediaLayout();
                break;
            }
            case VIEW_TYPE_ADDTOGROUP_INFO: {
                view = new TextInfoPrivacyCell( profileActivity.getContext(), attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            }
            case VIEW_TYPE_LOCATION:
                view = new ProfileLocationCell( profileActivity.getContext(), attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            case VIEW_TYPE_HOURS:
                view = new ProfileHoursCell( profileActivity.getContext(), attributesHolder.getResourcesProvider()) {
                    @Override
                    protected int processColor(int color) {
                        return dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            case VIEW_TYPE_VERSION:
            default: {
                TextInfoPrivacyCell cell = new TextInfoPrivacyCell( profileActivity.getContext(), 10, attributesHolder.getResourcesProvider());
                cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                cell.getTextView().setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText3, attributesHolder.getResourcesProvider()));
                cell.getTextView().setMovementMethod(null);
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    int code = pInfo.versionCode / 10;
                    String abi = "";
                    switch (pInfo.versionCode % 10) {
                        case 1:
                        case 2:
                            abi = "store bundled " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            break;
                        default:
                        case 9:
                            if (ApplicationLoader.isStandaloneBuild()) {
                                abi = "direct " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            } else {
                                abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            }
                            break;
                    }
                    cell.setText(formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)));
                } catch (Exception e) {
                    FileLog.e(e);
                }
                cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                view = cell;
                view.setBackgroundDrawable(Theme.getThemedDrawable( profileActivity.getContext(), R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow, attributesHolder.getResourcesProvider())));
                break;
            }
            case VIEW_TYPE_SUGGESTION: {
                view = new SettingsSuggestionCell( profileActivity.getContext(), attributesHolder.getResourcesProvider()) {
                    @Override
                    protected void onYesClick(int type) {
                        AndroidUtilities.runOnUIThread(() -> {
                            profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.newSuggestionsAvailable);
                            if (type == SettingsSuggestionCell.TYPE_GRACE) {
                                profileActivity.getMessagesController().removeSuggestion(0, "PREMIUM_GRACE");
                                Browser.openUrl(getContext(),  profileActivity.getMessagesController().premiumManageSubscriptionUrl);
                            } else {
                                 profileActivity.getMessagesController().removeSuggestion(0, type == SettingsSuggestionCell.TYPE_PHONE ? "VALIDATE_PHONE_NUMBER" : "VALIDATE_PASSWORD");
                            }
                            profileActivity.getNotificationCenter().addObserver(profileActivity, NotificationCenter.newSuggestionsAvailable);
                            profileAdapter.updateListAnimated(false);
                        });
                    }

                    @Override
                    protected void onNoClick(int type) {
                        if (type == SettingsSuggestionCell.TYPE_PHONE) {
                            profileActivity.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
                        } else {
                            profileActivity.presentFragment(new TwoStepVerificationSetupActivity(TwoStepVerificationSetupActivity.TYPE_VERIFY, null));
                        }
                    }
                };
                break;
            }
            case VIEW_TYPE_PREMIUM_TEXT_CELL:
            case VIEW_TYPE_STARS_TEXT_CELL:
                view = new ProfilePremiumCell( profileActivity.getContext(), viewType == VIEW_TYPE_PREMIUM_TEXT_CELL ? 0 : 1, attributesHolder.getResourcesProvider());
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            case VIEW_TYPE_CHANNEL:
                view = new ProfileChannelCell(profileActivity) {
                    @Override
                    public int processColor(int color) {
                        return dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
            case VIEW_TYPE_BOT_APP:
                FrameLayout frameLayout = new FrameLayout(profileActivity.getContext());
                ButtonWithCounterView button = new ButtonWithCounterView(profileActivity.getContext(), attributesHolder.getResourcesProvider());
                button.setText(LocaleController.getString(R.string.ProfileBotOpenApp), false);
                button.setOnClickListener(v -> {
                    TLRPC.User bot =  profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                     profileActivity.getMessagesController().openApp(profileActivity, bot, null, profileActivity.getClassGuid(), null);
                });
                frameLayout.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 18, 14, 18, 14));
                view = frameLayout;
                view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite, attributesHolder.getResourcesProvider()));
                break;
        }
        if (viewType != VIEW_TYPE_SHARED_MEDIA) {
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView == viewComponentsHolder.getSharedMediaLayout()) {
            rowsHolder.setSharedMediaLayoutAttached(true);
        }
        if (holder.itemView instanceof TextDetailCell) {
            ((TextDetailCell) holder.itemView).textView.setLoading(attributesHolder.getLoadingSpan());
            ((TextDetailCell) holder.itemView).valueTextView.setLoading(attributesHolder.getLoadingSpan());
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView == viewComponentsHolder.getSharedMediaLayout()) {
            rowsHolder.setSharedMediaLayoutAttached(false);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                HeaderCell headerCell = (HeaderCell) holder.itemView;
                if (position == rowsHolder.getInfoHeaderRow()) {
                    if (ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup && rowsHolder.getChannelInfoRow() != -1) {
                        headerCell.setText(LocaleController.getString(R.string.ReportChatDescription));
                    } else {
                        headerCell.setText(LocaleController.getString(R.string.Info));
                    }
                } else if (position == rowsHolder.getMembersHeaderRow()) {
                    headerCell.setText(LocaleController.getString(R.string.ChannelMembers));
                } else if (position == rowsHolder.getSettingsSectionRow2()) {
                    headerCell.setText(LocaleController.getString(R.string.SETTINGS));
                } else if (position == rowsHolder.getNumberSectionRow()) {
                    headerCell.setText(LocaleController.getString(R.string.Account));
                } else if (position == rowsHolder.getHelpHeaderRow()) {
                    headerCell.setText(LocaleController.getString(R.string.SettingsHelp));
                } else if (position == rowsHolder.getDebugHeaderRow()) {
                    headerCell.setText(LocaleController.getString(R.string.SettingsDebug));
                } else if (position == rowsHolder.getBotPermissionsHeader()) {
                    headerCell.setText(LocaleController.getString(R.string.BotProfilePermissions));
                }
                headerCell.setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader, attributesHolder.getResourcesProvider()), false));
                break;
            case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
            case VIEW_TYPE_TEXT_DETAIL:
                TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                boolean containsQr = false;
                boolean containsGift = false;
                if (position == rowsHolder.getBirthdayRow()) {
                    TLRPC.UserFull userFull =  profileActivity.getMessagesController().getUserFull(attributesHolder.getUserId());
                    if (userFull != null && userFull.birthday != null) {
                        final boolean today = BirthdayController.isToday(userFull);
                        final boolean withYear = (userFull.birthday.flags & 1) != 0;
                        final int age = withYear ? Period.between(LocalDate.of(userFull.birthday.year, userFull.birthday.month, userFull.birthday.day), LocalDate.now()).getYears() : -1;

                        String text = UserInfoActivity.birthdayString(userFull.birthday);

                        if (withYear) {
                            text = LocaleController.formatPluralString(today ? "ProfileBirthdayTodayValueYear" : "ProfileBirthdayValueYear", age, text);
                        } else {
                            text = LocaleController.formatString(today ? R.string.ProfileBirthdayTodayValue : R.string.ProfileBirthdayValue, text);
                        }

                        detailCell.setTextAndValue(
                                Emoji.replaceWithRestrictedEmoji(text, detailCell.textView, () -> {
                                    if (holder.getAdapterPosition() == position && rowsHolder.getBirthdayRow() == position && holder.getItemViewType() == VIEW_TYPE_TEXT_DETAIL) {
                                        onBindViewHolder(holder, position);
                                    }
                                }),
                                LocaleController.getString(today ? R.string.ProfileBirthdayToday : R.string.ProfileBirthday),
                                rowsHolder.isTopic() || rowsHolder.getBizHoursRow() != -1 || rowsHolder.getBizLocationRow() != -1
                        );

                        containsGift = !attributesHolder.isMyProfile() && today && ! profileActivity.getMessagesController().premiumPurchaseBlocked();
                    }
                } else if (position == rowsHolder.getPhoneRow()) {
                    String text;
                    TLRPC.User user =  profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                    String phoneNumber;
                    if (user != null && !TextUtils.isEmpty(rowsHolder.getVcardPhone())) {
                        text = PhoneFormat.getInstance().format("+" + rowsHolder.getVcardPhone());
                        phoneNumber = rowsHolder.getVcardPhone();
                    } else if (user != null && !TextUtils.isEmpty(user.phone)) {
                        text = PhoneFormat.getInstance().format("+" + user.phone);
                        phoneNumber = user.phone;
                    } else {
                        text = LocaleController.getString(R.string.PhoneHidden);
                        phoneNumber = null;
                    }
                    rowsHolder.setFragmentPhoneNumber(phoneNumber != null && phoneNumber.matches("888\\d{8}"));
                    detailCell.setTextAndValue(text, LocaleController.getString(rowsHolder.isFragmentPhoneNumber() ? R.string.AnonymousNumber : R.string.PhoneMobile), false);
                } else if (position == rowsHolder.getUsernameRow()) {
                    String username = null;
                    CharSequence text;
                    CharSequence value;
                    ArrayList<TLRPC.TL_username> usernames = new ArrayList<>();
                    if (attributesHolder.getUserId() != 0) {
                        final TLRPC.User user =  profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                        if (user != null) {
                            usernames.addAll(user.usernames);
                        }
                        TLRPC.TL_username usernameObj = null;
                        if (user != null && !TextUtils.isEmpty(user.username)) {
                            usernameObj = DialogObject.findUsername(user.username, usernames);
                            username = user.username;
                        }
                        usernames = user == null ? new ArrayList<>() : new ArrayList<>(user.usernames);
                        if (TextUtils.isEmpty(username) && usernames != null) {
                            for (int i = 0; i < usernames.size(); ++i) {
                                TLRPC.TL_username u = usernames.get(i);
                                if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                    usernameObj = u;
                                    username = u.username;
                                    break;
                                }
                            }
                        }
                        value = LocaleController.getString(R.string.Username);
                        if (username != null) {
                            text = "@" + username;
                            if (usernameObj != null && !usernameObj.editable) {
                                text = new SpannableString(text);
                                ((SpannableString) text).setSpan(makeUsernameLinkSpan(usernameObj), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        } else {
                            text = "—";
                        }
                        containsQr = true;
                    } else if (attributesHolder.getCurrentChat() != null) {
                        TLRPC.Chat chat =  profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                        username = ChatObject.getPublicUsername(chat);
                        if (chat != null) {
                            usernames.addAll(chat.usernames);
                        }
                        if (ChatObject.isPublic(chat)) {
                            containsQr = true;
                            text =  profileActivity.getMessagesController().linkPrefix + "/" + username + (attributesHolder.getTopicId() != 0 ? "/" + attributesHolder.getTopicId() : "");
                            value = LocaleController.getString(R.string.InviteLink);
                        } else {
                            text =  profileActivity.getMessagesController().linkPrefix + "/c/" + attributesHolder.getChatId() + (attributesHolder.getTopicId() != 0 ? "/" + attributesHolder.getTopicId() : "");
                            value = LocaleController.getString(R.string.InviteLinkPrivate);
                        }
                    } else {
                        text = "";
                        value = "";
                        usernames = new ArrayList<>();
                    }
                    detailCell.setTextAndValue(text, alsoUsernamesString(username, usernames, value), (rowsHolder.isTopic() || rowsHolder.getBizHoursRow() != -1 || rowsHolder.getBizLocationRow() != -1) && rowsHolder.getBirthdayRow() < 0);
                } else if (position == rowsHolder.getLocationRow()) {
                    if (attributesHolder.getChatInfo() != null && attributesHolder.getChatInfo().location instanceof TLRPC.TL_channelLocation) {
                        TLRPC.TL_channelLocation location = (TLRPC.TL_channelLocation) attributesHolder.getChatInfo().location;
                        detailCell.setTextAndValue(location.address, LocaleController.getString(R.string.AttachLocation), false);
                    }
                } else if (position == rowsHolder.getNumberRow()) {
                    TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
                    String value;
                    if (user != null && user.phone != null && user.phone.length() != 0) {
                        value = PhoneFormat.getInstance().format("+" + user.phone);
                    } else {
                        value = LocaleController.getString(R.string.NumberUnknown);
                    }
                    detailCell.setTextAndValue(value, LocaleController.getString(R.string.TapToChangePhone), true);
                    detailCell.setContentDescriptionValueFirst(false);
                } else if (position == rowsHolder.getSetUsernameRow()) {
                    TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
                    String text = "";
                    CharSequence value = LocaleController.getString(R.string.Username);
                    String username = null;
                    if (user != null && user.usernames.size() > 0) {
                        for (int i = 0; i < user.usernames.size(); ++i) {
                            TLRPC.TL_username u = user.usernames.get(i);
                            if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                username = u.username;
                                break;
                            }
                        }
                        if (username == null) {
                            username = user.username;
                        }
                        if (username == null || TextUtils.isEmpty(username)) {
                            text = LocaleController.getString(R.string.UsernameEmpty);
                        } else {
                            text = "@" + username;
                        }
                        value = alsoUsernamesString(username, user.usernames, value);
                    } else {
                        username = UserObject.getPublicUsername(user);
                        if (user != null && !TextUtils.isEmpty(username)) {
                            text = "@" + username;
                        } else {
                            text = LocaleController.getString(R.string.UsernameEmpty);
                        }
                    }
                    detailCell.setTextAndValue(text, value, true);
                    detailCell.setContentDescriptionValueFirst(true);
                }
                if (containsGift) {
                    Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_input_gift);
                    drawable.setColorFilter(new PorterDuffColorFilter(dontApplyPeerColor(getThemedColor(Theme.key_switch2TrackChecked, attributesHolder.getResourcesProvider()), false), PorterDuff.Mode.MULTIPLY));
                    if (UserObject.areGiftsDisabled(attributesHolder.getUserInfo())) {
                        detailCell.setImage(null);
                        detailCell.setImageClickListener(null);
                    } else {
                        detailCell.setImage(drawable, LocaleController.getString(R.string.GiftPremium));
                        detailCell.setImageClickListener(profileActivity::onTextDetailCellImageClicked);
                    }
                } else if (containsQr) {
                    Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_qr_mini);
                    drawable.setColorFilter(new PorterDuffColorFilter(dontApplyPeerColor(getThemedColor(Theme.key_switch2TrackChecked, attributesHolder.getResourcesProvider()), false), PorterDuff.Mode.MULTIPLY));
                    detailCell.setImage(drawable, LocaleController.getString(R.string.GetQRCode));
                    detailCell.setImageClickListener(profileActivity::onTextDetailCellImageClicked);
                } else {
                    detailCell.setImage(null);
                    detailCell.setImageClickListener(null);
                }
                detailCell.setTag(position);
                detailCell.textView.setLoading(attributesHolder.getLoadingSpan());
                detailCell.valueTextView.setLoading(attributesHolder.getLoadingSpan());
                break;
            case VIEW_TYPE_ABOUT_LINK:
                AboutLinkCell aboutLinkCell = (AboutLinkCell) holder.itemView;
                if (position == rowsHolder.getUserInfoRow()) {
                    TLRPC.User user = attributesHolder.getUserInfo().user != null ? attributesHolder.getUserInfo().user :  profileActivity.getMessagesController().getUser(attributesHolder.getUserInfo().id);
                    boolean addlinks = rowsHolder.isBot() || (user != null && user.premium && attributesHolder.getUserInfo().about != null);
                    aboutLinkCell.setTextAndValue(attributesHolder.getUserInfo().about, LocaleController.getString(R.string.UserBio), addlinks);
                } else if (position == rowsHolder.getChannelInfoRow()) {
                    String text = attributesHolder.getChatInfo().about;
                    while (text.contains("\n\n\n")) {
                        text = text.replace("\n\n\n", "\n\n");
                    }
                    aboutLinkCell.setText(text, ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup);
                } else if (position == rowsHolder.getBioRow()) {
                    String value;
                    if (attributesHolder.getUserInfo() == null || !TextUtils.isEmpty(attributesHolder.getUserInfo().about)) {
                        value = attributesHolder.getUserInfo() == null ? LocaleController.getString(R.string.Loading) : attributesHolder.getUserInfo().about;
                        aboutLinkCell.setTextAndValue(value, LocaleController.getString(R.string.UserBio), profileActivity.getUserConfig().isPremium());
                        attributesHolder.setCurrentBio(attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().about : null);
                    } else {
                        aboutLinkCell.setTextAndValue(LocaleController.getString(R.string.UserBio), LocaleController.getString(R.string.UserBioDetail), false);
                        attributesHolder.setCurrentBio(null);
                    }
                    aboutLinkCell.setMoreButtonDisabled(true);
                }
                break;
            case VIEW_TYPE_PREMIUM_TEXT_CELL:
            case VIEW_TYPE_STARS_TEXT_CELL:
            case VIEW_TYPE_TEXT:
                TextCell textCell = (TextCell) holder.itemView;
                textCell.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                textCell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                if (position == rowsHolder.getSettingsTimerRow()) {
                    TLRPC.EncryptedChat encryptedChat =  profileActivity.getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(attributesHolder.getDialogId()));
                    String value;
                    if (encryptedChat.ttl == 0) {
                        value = LocaleController.getString(R.string.ShortMessageLifetimeForever);
                    } else {
                        value = LocaleController.formatTTLString(encryptedChat.ttl);
                    }
                    textCell.setTextAndValue(LocaleController.getString(R.string.MessageLifetime), value, false,false);
                } else if (position == rowsHolder.getUnblockRow()) {
                    textCell.setText(LocaleController.getString(R.string.Unblock), false);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                } else if (position == rowsHolder.getSettingsKeyRow()) {
                    IdenticonDrawable identiconDrawable = new IdenticonDrawable();
                    TLRPC.EncryptedChat encryptedChat =  profileActivity.getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(attributesHolder.getDialogId()));
                    identiconDrawable.setEncryptedChat(encryptedChat);
                    textCell.setTextAndValueDrawable(LocaleController.getString(R.string.EncryptionKey), identiconDrawable, false);
                } else if (position == rowsHolder.getJoinRow()) {
                    textCell.setColors(-1, Theme.key_windowBackgroundWhiteBlueText2);
                    if (attributesHolder.getCurrentChat().megagroup) {
                        textCell.setText(LocaleController.getString(R.string.ProfileJoinGroup), false);
                    } else {
                        textCell.setText(LocaleController.getString(R.string.ProfileJoinChannel), false);
                    }
                } else if (position == rowsHolder.getSubscribersRow()) {
                    if (attributesHolder.getChatInfo() != null) {
                        if (ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelSubscribers), LocaleController.formatNumber(attributesHolder.getChatInfo().participants_count, ','), R.drawable.msg_groups, position != rowsHolder.getMembersSectionRow() - 1);
                        } else {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelMembers), LocaleController.formatNumber(attributesHolder.getChatInfo().participants_count, ','), R.drawable.msg_groups, position != rowsHolder.getMembersSectionRow() - 1);
                        }
                    } else {
                        if (ChatObject.isChannel(attributesHolder.getCurrentChat()) && !attributesHolder.getCurrentChat().megagroup) {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelSubscribers), R.drawable.msg_groups, position != rowsHolder.getMembersSectionRow() - 1);
                        } else {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelMembers), R.drawable.msg_groups, position != rowsHolder.getMembersSectionRow() - 1);
                        }
                    }
                } else if (position == rowsHolder.getSubscribersRequestsRow()) {
                    if (attributesHolder.getChatInfo() != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.SubscribeRequests), String.format("%d", attributesHolder.getChatInfo().requests_pending), R.drawable.msg_requests, position != rowsHolder.getMembersSectionRow() - 1);
                    }
                } else if (position == rowsHolder.getAdministratorsRow()) {
                    if (attributesHolder.getChatInfo() != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelAdministrators), String.format("%d", attributesHolder.getChatInfo().admins_count), R.drawable.msg_admins, position != rowsHolder.getMembersSectionRow() - 1);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdministrators), R.drawable.msg_admins, position != rowsHolder.getMembersSectionRow() - 1);
                    }
                } else if (position == rowsHolder.getSettingsRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdminSettings), R.drawable.msg_customize, position != rowsHolder.getMembersSectionRow() - 1);
                } else if (position == rowsHolder.getChannelBalanceRow()) {
                    final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(UserConfig.selectedAccount).getBotStarsBalance(-attributesHolder.getChatId());
                    final long ton_balance = BotStarsController.getInstance(UserConfig.selectedAccount).getTONBalance(-attributesHolder.getChatId());
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ton_balance > 0) {
                        if (ton_balance / 1_000_000_000.0 > 1000.0) {
                            ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                        } else {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setDecimalSeparator('.');
                            DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                            formatterTON.setMinimumFractionDigits(2);
                            formatterTON.setMaximumFractionDigits(3);
                            formatterTON.setGroupingUsed(false);
                            ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                        }
                    }
                    if (stars_balance.amount > 0) {
                        if (ssb.length() > 0) ssb.append(" ");
                        ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.ChannelStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_feature_paid, true);
                } else if (position == rowsHolder.getBotStarsBalanceRow()) {
                    final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(UserConfig.selectedAccount).getBotStarsBalance(attributesHolder.getUserId());
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (stars_balance.amount > 0) {
                        ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_premium_main, true);
                } else if (position == rowsHolder.getBotTonBalanceRow()) {
                    long ton_balance = BotStarsController.getInstance(UserConfig.selectedAccount).getTONBalance(attributesHolder.getUserId());
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ton_balance > 0) {
                        if (ton_balance / 1_000_000_000.0 > 1000.0) {
                            ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                        } else {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setDecimalSeparator('.');
                            DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                            formatterTON.setMinimumFractionDigits(2);
                            formatterTON.setMaximumFractionDigits(3);
                            formatterTON.setGroupingUsed(false);
                            ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                        }
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceTON), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.msg_ton, true);
                } else if (position == rowsHolder.getBlockedUsersRow()) {
                    if (attributesHolder.getChatInfo() != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelBlacklist), String.format("%d", Math.max(attributesHolder.getChatInfo().banned_count, attributesHolder.getChatInfo().kicked_count)), R.drawable.msg_user_remove, position != rowsHolder.getMembersSectionRow() - 1);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelBlacklist), R.drawable.msg_user_remove, position != rowsHolder.getMembersSectionRow() - 1);
                    }
                } else if (position == rowsHolder.getAddMemberRow()) {
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    boolean isNextPositionMember = position + 1 >= rowsHolder.getMembersStartRow() && position + 1 < rowsHolder.getMembersEndRow();
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddMember), R.drawable.msg_contact_add, rowsHolder.getMembersSectionRow() == -1 || isNextPositionMember);
                } else if (position == rowsHolder.getSendMessageRow()) {
                    textCell.setText(LocaleController.getString(R.string.SendMessageLocation), true);
                } else if (position == rowsHolder.getAddToContactsRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddToContacts), R.drawable.msg_contact_add, false);
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                } else if (position == rowsHolder.getReportReactionRow()) {
                    TLRPC.Chat chat =  profileActivity.getMessagesController().getChat(-rowsHolder.getReportReactionFromDialogId());
                    if (chat != null && ChatObject.canBlockUsers(chat)) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReactionAndBan), R.drawable.msg_block2, false);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReaction), R.drawable.msg_report,false);
                    }

                    textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                    textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                } else if (position == rowsHolder.getReportRow()) {
                    textCell.setText(LocaleController.getString(R.string.ReportUserLocation), false);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                } else if (position == rowsHolder.getLanguageRow()) {
                    textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.Language), LocaleController.getCurrentLanguageName(), false, R.drawable.msg2_language, false);
                    textCell.setImageLeft(23);
                } else if (position == rowsHolder.getNotificationRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.NotificationsAndSounds), R.drawable.msg2_notifications, true);
                } else if (position == rowsHolder.getPrivacyRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacySettings), R.drawable.msg2_secret, true);
                } else if (position == rowsHolder.getDataRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.DataSettings), R.drawable.msg2_data, true);
                } else if (position == rowsHolder.getChatRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.ChatSettings), R.drawable.msg2_discussion, true);
                } else if (position == rowsHolder.getFiltersRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.Filters), R.drawable.msg2_folder, true);
                } else if (position == rowsHolder.getStickersRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.StickersName), R.drawable.msg2_sticker, true);
                } else if (position == rowsHolder.getLiteModeRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PowerUsage), R.drawable.msg2_battery, true);
                } else if (position == rowsHolder.getQuestionRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AskAQuestion), R.drawable.msg2_ask_question, true);
                } else if (position == rowsHolder.getFaqRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramFAQ), R.drawable.msg2_help, true);
                } else if (position == rowsHolder.getPolicyRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacyPolicy), R.drawable.msg2_policy, false);
                } else if (position == rowsHolder.getSendLogsRow()) {
                    textCell.setText(LocaleController.getString(R.string.DebugSendLogs), true);
                } else if (position == rowsHolder.getSendLastLogsRow()) {
                    textCell.setText(LocaleController.getString(R.string.DebugSendLastLogs), true);
                } else if (position == rowsHolder.getClearLogsRow()) {
                    textCell.setText(LocaleController.getString(R.string.DebugClearLogs), rowsHolder.getSwitchBackendRow() != -1);
                } else if (position == rowsHolder.getSwitchBackendRow()) {
                    textCell.setText("Switch Backend", false);
                } else if (position == rowsHolder.getDevicesRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.Devices), R.drawable.msg2_devices, true);
                } else if (position == rowsHolder.getSetAvatarRow()) {
                    attributesHolder.getCellCameraDrawable().setCustomEndFrame(86);
                    attributesHolder.getCellCameraDrawable().setCurrentFrame(85, false);
                    textCell.setTextAndIcon(LocaleController.getString(R.string.SetProfilePhoto), attributesHolder.getCellCameraDrawable(), false);
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    textCell.getImageView().setPadding(0, 0, 0, AndroidUtilities.dp(8));
                    textCell.setImageLeft(12);
                    viewComponentsHolder.setSetAvatarCell(textCell);
                } else if (position == rowsHolder.getAddToGroupButtonRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddToGroupOrChannel), R.drawable.msg_groups_create, false);
                } else if (position == rowsHolder.getPremiumRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramPremium), new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().premiumStarMenuDrawable, dp(24), dp(24)), true);
                    textCell.setImageLeft(23);
                } else if (position == rowsHolder.getStarsRow()) {
                    StarsController c = StarsController.getInstance(UserConfig.selectedAccount);
                    long balance = c.getBalance().amount;
                    textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.MenuTelegramStars), c.balanceAvailable() && balance > 0 ? LocaleController.formatNumber((int) balance, ',') : "", new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().goldenStarMenuDrawable, dp(24), dp(24)), true);
                    textCell.setImageLeft(23);
                } else if (position == rowsHolder.getBusinessRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramBusiness), R.drawable.menu_shop, true);
                    textCell.setImageLeft(23);
                } else if (position == rowsHolder.getPremiumGiftingRow()) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);
                    textCell.setImageLeft(23);
                } else if (position == rowsHolder.getBotPermissionLocation()) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionLocation), viewComponentsHolder.getBotLocation() != null && viewComponentsHolder.getBotLocation().granted(), R.drawable.filled_access_location, getThemedColor(Theme.key_color_green, attributesHolder.getResourcesProvider()), rowsHolder.getBotPermissionBiometry() != -1);
                } else if (position == rowsHolder.getBotPermissionBiometry()) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionBiometry), viewComponentsHolder.getBotBiometry() != null && viewComponentsHolder.getBotBiometry().granted(), R.drawable.filled_access_fingerprint, getThemedColor(Theme.key_color_orange, attributesHolder.getResourcesProvider()), false);
                } else if (position == rowsHolder.getBotPermissionEmojiStatus()) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionEmojiStatus), attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().bot_can_manage_emoji_status, R.drawable.filled_access_sleeping, getThemedColor(Theme.key_color_lightblue, attributesHolder.getResourcesProvider()), rowsHolder.getBotPermissionLocation() != -1 || rowsHolder.getBotPermissionBiometry() != -1);
                }
                textCell.valueTextView.setTextColor(dontApplyPeerColor(getThemedColor(Theme.key_windowBackgroundWhiteValueText, attributesHolder.getResourcesProvider()), false));
                break;
//            case VIEW_TYPE_NOTIFICATIONS_CHECK:
//                NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;
//                if (position == rowsHolder.getNotificationsRow()) {
//                    SharedPreferences preferences = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
//                    long did;
//                    if (attributesHolder.getDialogId() != 0) {
//                        did = attributesHolder.getDialogId();
//                    } else if (attributesHolder.getUserId() != 0) {
//                        did = attributesHolder.getUserId();
//                    } else {
//                        did = -attributesHolder.getChatId();
//                    }
//                    String key = NotificationsController.getSharedPrefKey(did, attributesHolder.getTopicId());
//                    boolean enabled = false;
//                    boolean custom = preferences.getBoolean("custom_" + key, false);
//                    boolean hasOverride = preferences.contains("notify2_" + key);
//                    int value = preferences.getInt("notify2_" + key, 0);
//                    int delta = preferences.getInt("notifyuntil_" + key, 0);
//                    String val;
//                    if (value == 3 && delta != Integer.MAX_VALUE) {
//                        delta -= profileActivity.getConnectionsManager().getCurrentTime();
//                        if (delta <= 0) {
//                            if (custom) {
//                                val = LocaleController.getString(R.string.NotificationsCustom);
//                            } else {
//                                val = LocaleController.getString(R.string.NotificationsOn);
//                            }
//                            enabled = true;
//                        } else if (delta < 60 * 60) {
//                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Minutes", delta / 60));
//                        } else if (delta < 60 * 60 * 24) {
//                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Hours", (int) Math.ceil(delta / 60.0f / 60)));
//                        } else if (delta < 60 * 60 * 24 * 365) {
//                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Days", (int) Math.ceil(delta / 60.0f / 60 / 24)));
//                        } else {
//                            val = null;
//                        }
//                    } else {
//                        if (value == 0) {
//                            if (hasOverride) {
//                                enabled = true;
//                            } else {
//                                enabled = profileActivity.getAccountInstance().getNotificationsController().isGlobalNotificationsEnabled(did, false, false);
//                            }
//                        } else if (value == 1) {
//                            enabled = true;
//                        }
//                        if (enabled && custom) {
//                            val = LocaleController.getString(R.string.NotificationsCustom);
//                        } else {
//                            val = enabled ? LocaleController.getString(R.string.NotificationsOn) : LocaleController.getString(R.string.NotificationsOff);
//                        }
//                    }
//                    if (val == null) {
//                        val = LocaleController.getString(R.string.NotificationsOff);
//                    }
//                    if (attributesHolder.getNotificationsExceptionTopics() != null && !attributesHolder.getNotificationsExceptionTopics().isEmpty()) {
//                        val = String.format(Locale.US, LocaleController.getPluralString("NotificationTopicExceptionsDesctription", attributesHolder.getNotificationsExceptionTopics().size()), val, attributesHolder.getNotificationsExceptionTopics().size());
//                    }
//                    checkCell.setAnimationsEnabled(rowsHolder.isFragmentOpened());
//                    checkCell.setTextAndValueAndCheck(LocaleController.getString(R.string.Notifications), val, enabled, rowsHolder.getBotAppRow() >= 0);
//                }
//                break;
            case VIEW_TYPE_SHADOW:
                View sectionCell = holder.itemView;
                sectionCell.setTag(position);
                Drawable drawable;
                if (position == rowsHolder.getInfoSectionRow() && rowsHolder.getLastSectionRow() == -1 && rowsHolder.getSecretSettingsSectionRow() == -1 && rowsHolder.getSharedMediaRow() == -1 && rowsHolder.getMembersSectionRow() == -1 || position == rowsHolder.getSecretSettingsSectionRow() || position == rowsHolder.getLastSectionRow() || position == rowsHolder.getMembersSectionRow() && rowsHolder.getLastSectionRow() == -1 && rowsHolder.getSharedMediaRow() == -1) {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable( profileActivity.getContext(), R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow, attributesHolder.getResourcesProvider())));
                } else {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable( profileActivity.getContext(), R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow, attributesHolder.getResourcesProvider())));
                }
                break;
            case VIEW_TYPE_SHADOW_TEXT: {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setLinkTextRippleColor(null);
                if (position == rowsHolder.getInfoSectionRow()) {
                    final long did = profileActivity.getDialogId();
                    TLObject obj =  profileActivity.getMessagesController().getUserOrChat(did);
                    TL_bots.botVerification bot_verification = attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().bot_verification : attributesHolder.getChatInfo() != null ? attributesHolder.getChatInfo().bot_verification : null;
                    if (rowsHolder.getBotAppRow() >= 0 || bot_verification != null) {
                        cell.setFixedSize(0);
                        final TLRPC.User user =  profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                        final boolean botOwner = user != null && user.bot && user.bot_can_edit;
                        SpannableStringBuilder sb = new SpannableStringBuilder();

                        if (rowsHolder.getBotAppRow() >= 0) {
                            sb.append(AndroidUtilities.replaceSingleTag(getString(botOwner ? R.string.ProfileBotOpenAppInfoOwner : R.string.ProfileBotOpenAppInfo), () -> {
                                Browser.openUrl(profileActivity.getContext(), getString(botOwner ? R.string.ProfileBotOpenAppInfoOwnerLink : R.string.ProfileBotOpenAppInfoLink));
                            }));
                            if (bot_verification != null) {
                                sb.append("\n\n\n");
                            }
                        }
                        if (bot_verification != null) {
                            sb.append("x");
                            sb.setSpan(new AnimatedEmojiSpan(bot_verification.icon, cell.getTextView().getPaint().getFontMetricsInt()), sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.append(" ");
                            SpannableString description = new SpannableString(bot_verification.description);
                            try {
                                AndroidUtilities.addLinksSafe(description, Linkify.WEB_URLS, false, false);
                                URLSpan[] spans = description.getSpans(0, description.length(), URLSpan.class);
                                for (int i = 0; i < spans.length; ++i) {
                                    URLSpan span = spans[i];
                                    int start = description.getSpanStart(span);
                                    int end = description.getSpanEnd(span);
                                    final String url = span.getURL();

                                    description.removeSpan(span);
                                    description.setSpan(new URLSpan(url) {
                                        @Override
                                        public void onClick(View widget) {
                                            Browser.openUrl(profileActivity.getContext(), url);
                                        }
                                        @Override
                                        public void updateDrawState(@NonNull TextPaint ds) {
                                            ds.setUnderlineText(true);
                                        }
                                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            sb.append(description);
                        }

                        cell.setLinkTextRippleColor(Theme.multAlpha(getThemedColor(Theme.key_windowBackgroundWhiteGrayText4, attributesHolder.getResourcesProvider()), 0.2f));
                        cell.setText(sb);
                    } else {
                        cell.setFixedSize(14);
                        cell.setText(null);
                    }
                } else if (position == rowsHolder.getInfoAffiliateRow()) {
                    final TLRPC.User botUser =  profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                    if (botUser != null && botUser.bot && botUser.bot_can_edit) {
                        cell.setFixedSize(0);
                        cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfoOwner, UserObject.getUserName(botUser), percents(attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().starref_program != null ? attributesHolder.getUserInfo().starref_program.commission_permille : 0)));
                    } else {
                        cell.setFixedSize(0);
                        cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfo, UserObject.getUserName(botUser), percents(attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().starref_program != null ? attributesHolder.getUserInfo().starref_program.commission_permille : 0)));
                    }
                }
                if (position == rowsHolder.getInfoSectionRow() && rowsHolder.getLastSectionRow() == -1 && rowsHolder.getSecretSettingsSectionRow() == -1 && rowsHolder.getSharedMediaRow() == -1 && rowsHolder.getMembersSectionRow() == -1 || position == rowsHolder.getSecretSettingsSectionRow() || position == rowsHolder.getLastSectionRow() || position == rowsHolder.getMembersSectionRow() && rowsHolder.getLastSectionRow() == -1 && rowsHolder.getSharedMediaRow() == -1) {
                    cell.setBackgroundDrawable(Theme.getThemedDrawable( profileActivity.getContext(), R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow, attributesHolder.getResourcesProvider())));
                } else {
                    cell.setBackgroundDrawable(Theme.getThemedDrawable( profileActivity.getContext(), R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow, attributesHolder.getResourcesProvider())));
                }
                break;
            }
            case VIEW_TYPE_COLORFUL_TEXT: {
                AffiliateProgramFragment.ColorfulTextCell cell = (AffiliateProgramFragment.ColorfulTextCell) holder.itemView;
                cell.set(getThemedColor(Theme.key_color_green, attributesHolder.getResourcesProvider()), R.drawable.filled_affiliate, getString(R.string.ProfileBotAffiliateProgram), null);
                cell.setPercent(attributesHolder.getUserInfo() != null && attributesHolder.getUserInfo().starref_program != null ? percents(attributesHolder.getUserInfo().starref_program.commission_permille) : null);
                break;
            }
            case VIEW_TYPE_USER:
                UserCell userCell = (UserCell) holder.itemView;
                TLRPC.ChatParticipant part;
                try {
                    if (!attributesHolder.getVisibleSortedUsers().isEmpty()) {
                        part = attributesHolder.getVisibleChatParticipants().get(attributesHolder.getVisibleSortedUsers().get(position - rowsHolder.getMembersStartRow()));
                    } else {
                        part = attributesHolder.getVisibleChatParticipants().get(position - rowsHolder.getMembersStartRow());
                    }
                } catch (Exception e) {
                    part = null;
                    FileLog.e(e);
                }
                if (part != null) {
                    String role;
                    if (part instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                        if (!TextUtils.isEmpty(channelParticipant.rank)) {
                            role = channelParticipant.rank;
                        } else {
                            if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                                role = LocaleController.getString(R.string.ChannelCreator);
                            } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin) {
                                role = LocaleController.getString(R.string.ChannelAdmin);
                            } else {
                                role = null;
                            }
                        }
                    } else {
                        if (part instanceof TLRPC.TL_chatParticipantCreator) {
                            role = LocaleController.getString(R.string.ChannelCreator);
                        } else if (part instanceof TLRPC.TL_chatParticipantAdmin) {
                            role = getString(R.string.ChannelAdmin);
                        } else {
                            role = null;
                        }
                    }
                    userCell.setAdminRole(role);
                    userCell.setData( profileActivity.getMessagesController().getUser(part.user_id), null, null, 0, position != rowsHolder.getMembersEndRow() - 1);
                }
                break;
            case VIEW_TYPE_BOTTOM_PADDING:
                holder.itemView.requestLayout();
                break;
            case VIEW_TYPE_SUGGESTION:
                SettingsSuggestionCell suggestionCell = (SettingsSuggestionCell) holder.itemView;
                if (position == rowsHolder.getPasswordSuggestionRow()) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_PASSWORD);
                } else if (position == rowsHolder.getPhoneSuggestionRow()) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_PHONE);
                } else if (position == rowsHolder.getGraceSuggestionRow()) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_GRACE);
                }
                break;
            case VIEW_TYPE_ADDTOGROUP_INFO:
                TextInfoPrivacyCell addToGroupInfo = (TextInfoPrivacyCell) holder.itemView;
                addToGroupInfo.setBackground(Theme.getThemedDrawable( profileActivity.getContext(), R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow, attributesHolder.getResourcesProvider())));
                addToGroupInfo.setText(LocaleController.getString(R.string.BotAddToGroupOrChannelInfo));
                break;
            case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE:
                TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                textCheckCell.setTextAndCheck(LocaleController.getString(R.string.Notifications), ! profileActivity.getMessagesController().isDialogMuted(profileActivity.getDialogId(), attributesHolder.getTopicId()), false);
                break;
            case VIEW_TYPE_LOCATION:
                ((ProfileLocationCell) holder.itemView).set(attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().business_location : null, rowsHolder.getNotificationsDividerRow() < 0 && !attributesHolder.isMyProfile());
                break;
            case VIEW_TYPE_HOURS:
                ProfileHoursCell hoursCell = (ProfileHoursCell) holder.itemView;
                hoursCell.setOnTimezoneSwitchClick(view -> {
                    rowsHolder.setHoursShownMine(!rowsHolder.isHoursShownMine());
                    if (!rowsHolder.isHoursExpanded()) {
                        rowsHolder.setHoursExpanded(true);
                    }
                    profileAdapter.saveScrollPosition();
                    view.requestLayout();
                    notifyItemChanged(rowsHolder.getBizHoursRow());
                    if (rowsHolder.getSavedScrollPosition() >= 0) {
                        viewComponentsHolder.getLayoutManager().scrollToPositionWithOffset(rowsHolder.getSavedScrollPosition(), rowsHolder.getSavedScrollOffset() - listView.getPaddingTop());
                    }
                });
                hoursCell.set(attributesHolder.getUserInfo() != null ? attributesHolder.getUserInfo().business_work_hours : null, rowsHolder.isHoursExpanded(), rowsHolder.isHoursShownMine(), rowsHolder.getNotificationsDividerRow() < 0 && !attributesHolder.isMyProfile() || rowsHolder.getBizLocationRow() >= 0);
                break;
            case VIEW_TYPE_CHANNEL:
                ((ProfileChannelCell) holder.itemView).set(
                         profileActivity.getMessagesController().getChat(attributesHolder.getUserInfo().personal_channel_id),
                        viewComponentsHolder.getProfileChannelMessageFetcher() != null ? viewComponentsHolder.getProfileChannelMessageFetcher().messageObject : null
                );
                break;
            case VIEW_TYPE_BOT_APP:

                break;
        }
    }

    private CharSequence alsoUsernamesString(String originalUsername, ArrayList<TLRPC.TL_username> alsoUsernames, CharSequence fallback) {
        if (alsoUsernames == null) {
            return fallback;
        }
        alsoUsernames = new ArrayList<>(alsoUsernames);
        for (int i = 0; i < alsoUsernames.size(); ++i) {
            if (
                    !alsoUsernames.get(i).active ||
                            originalUsername != null && originalUsername.equals(alsoUsernames.get(i).username)
            ) {
                alsoUsernames.remove(i--);
            }
        }
        if (alsoUsernames.size() > 0) {
            SpannableStringBuilder usernames = new SpannableStringBuilder();
            for (int i = 0; i < alsoUsernames.size(); ++i) {
                TLRPC.TL_username usernameObj = alsoUsernames.get(i);
                final String usernameRaw = usernameObj.username;
                SpannableString username = new SpannableString("@" + usernameRaw);
                username.setSpan(makeUsernameLinkSpan(usernameObj), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                username.setSpan(new ForegroundColorSpan(dontApplyPeerColor(getThemedColor(Theme.key_chat_messageLinkIn, attributesHolder.getResourcesProvider()), false)), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                usernames.append(username);
                if (i < alsoUsernames.size() - 1) {
                    usernames.append(", ");
                }
            }
            String string = getString(R.string.UsernameAlso);
            SpannableStringBuilder finalString = new SpannableStringBuilder(string);
            final String toFind = "%1$s";
            int index = string.indexOf(toFind);
            if (index >= 0) {
                finalString.replace(index, index + toFind.length(), usernames);
            }
            return finalString;
        } else {
            return fallback;
        }
    }

    private final HashMap<TLRPC.TL_username, ClickableSpan> usernameSpans = new HashMap<TLRPC.TL_username, ClickableSpan>();
    public ClickableSpan makeUsernameLinkSpan(TLRPC.TL_username usernameObj) {
        ClickableSpan span = usernameSpans.get(usernameObj);
        if (span != null) return span;

        final String usernameRaw = usernameObj.username;
        span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (!usernameObj.editable) {
                    if (attributesHolder.getLoadingSpan() == this) return;
                    Loaders.setLoadingSpan(components, listView,this);
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        Loaders.setLoadingSpan(components, listView,null);
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (attributesHolder.getUserId() != 0) {
                                obj =  profileActivity.getMessagesController().getUser(attributesHolder.getUserId());
                            } else {
                                obj =  profileActivity.getMessagesController().getChat(attributesHolder.getChatId());
                            }
                            if (profileActivity.getContext() == null) {
                                return;
                            }
                            FragmentUsernameBottomSheet.open(profileActivity.getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, attributesHolder.getResourcesProvider());
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    profileActivity.getConnectionsManager().bindRequestToGuid(reqId, profileActivity.getClassGuid());
                } else {
                    Loaders.setLoadingSpan(components, listView,null);
                    String urlFinal =  profileActivity.getMessagesController().linkPrefix + "/" + usernameRaw;
                    if (attributesHolder.getCurrentChat() == null || !attributesHolder.getCurrentChat().noforwards) {
                        AndroidUtilities.addToClipboard(urlFinal);
                        viewComponentsHolder.getUndoView().showWithAction(0, UndoView.ACTION_USERNAME_COPIED, null);
                    }
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(ds.linkColor);
            }
        };
        usernameSpans.put(usernameObj, span);
        return span;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder.getAdapterPosition() == rowsHolder.getSetAvatarRow()) {
            viewComponentsHolder.setSetAvatarCell(null);
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        if (rowsHolder.getNotificationRow() != -1) {
            int position = holder.getAdapterPosition();
            return position == rowsHolder.getNotificationRow() || position == rowsHolder.getNumberRow() || position == rowsHolder.getPrivacyRow() ||
                    position == rowsHolder.getLanguageRow() || position == rowsHolder.getSetUsernameRow() || position == rowsHolder.getBioRow() ||
                    position == rowsHolder.getVersionRow() || position == rowsHolder.getDataRow() || position == rowsHolder.getChatRow() ||
                    position == rowsHolder.getQuestionRow() || position == rowsHolder.getDevicesRow() || position == rowsHolder.getFiltersRow() || position == rowsHolder.getStickersRow() ||
                    position == rowsHolder.getFaqRow() || position == rowsHolder.getPolicyRow() || position == rowsHolder.getSendLogsRow() || position == rowsHolder.getSendLastLogsRow() ||
                    position == rowsHolder.getClearLogsRow() || position == rowsHolder.getSwitchBackendRow() || position == rowsHolder.getSetAvatarRow() ||
                    position == rowsHolder.getAddToGroupButtonRow() || position == rowsHolder.getPremiumRow() || position == rowsHolder.getPremiumGiftingRow() ||
                    position == rowsHolder.getBusinessRow() || position == rowsHolder.getLiteModeRow() || position == rowsHolder.getBirthdayRow() || position == rowsHolder.getChannelRow() ||
                    position == rowsHolder.getStarsRow();
        }
        if (holder.itemView instanceof UserCell) {
            UserCell userCell = (UserCell) holder.itemView;
            Object object = userCell.getCurrentObject();
            if (object instanceof TLRPC.User) {
                TLRPC.User user = (TLRPC.User) object;
                if (UserObject.isUserSelf(user)) {
                    return false;
                }
            }
        }
        int type = holder.getItemViewType();
        return type != VIEW_TYPE_HEADER && type != VIEW_TYPE_DIVIDER && type != VIEW_TYPE_SHADOW &&
                type != VIEW_TYPE_EMPTY && type != VIEW_TYPE_BOTTOM_PADDING && type != VIEW_TYPE_SHARED_MEDIA &&
                type != 9 && type != 10 && type != VIEW_TYPE_BOT_APP; // These are legacy ones, left for compatibility
    }

    @Override
    public int getItemCount() {
        return attributesHolder.getRowCount();
    }

    @Override
    public int getItemViewType(int position) {
        checkForRowsHolder();
        if (position == rowsHolder.getInfoHeaderRow() || position == rowsHolder.getMembersHeaderRow() || position == rowsHolder.getSettingsSectionRow2() ||
                position == rowsHolder.getNumberSectionRow() || position == rowsHolder.getHelpHeaderRow() || position == rowsHolder.getDebugHeaderRow() || position == rowsHolder.getBotPermissionsHeader()) {
            return VIEW_TYPE_HEADER;
        } else if (position == rowsHolder.getPhoneRow() || position == rowsHolder.getLocationRow() || position == rowsHolder.getNumberRow() || position == rowsHolder.getBirthdayRow()) {
            return VIEW_TYPE_TEXT_DETAIL;
        } else if (position == rowsHolder.getUsernameRow() || position == rowsHolder.getSetUsernameRow()) {
            return VIEW_TYPE_TEXT_DETAIL_MULTILINE; //TODO: Refactor all ProfileParams with rowsHolder
        } else if (position == rowsHolder.getUserInfoRow() || position == rowsHolder.getChannelInfoRow() || position == rowsHolder.getBioRow()) {
            return VIEW_TYPE_ABOUT_LINK;
        } else if (position == rowsHolder.getSettingsTimerRow() || position == rowsHolder.getSettingsKeyRow() || position == rowsHolder.getReportRow() || position == rowsHolder.getReportReactionRow() ||
                position == rowsHolder.getSubscribersRow() || position == rowsHolder.getSubscribersRequestsRow() || position == rowsHolder.getAdministratorsRow() ||
                position == rowsHolder.getSettingsRow() || position == rowsHolder.getBlockedUsersRow() || position == rowsHolder.getAddMemberRow() || position == rowsHolder.getJoinRow() || position == rowsHolder.getUnblockRow() ||
                position == rowsHolder.getSendMessageRow() || position == rowsHolder.getNotificationRow() || position == rowsHolder.getPrivacyRow() ||
                position == rowsHolder.getLanguageRow() || position == rowsHolder.getDataRow() || position == rowsHolder.getChatRow() ||
                position == rowsHolder.getQuestionRow() || position == rowsHolder.getDevicesRow() || position == rowsHolder.getFiltersRow() || position == rowsHolder.getStickersRow() ||
                position == rowsHolder.getFaqRow() || position == rowsHolder.getPolicyRow() || position == rowsHolder.getSendLogsRow() || position == rowsHolder.getSendLastLogsRow() ||
                position == rowsHolder.getClearLogsRow() || position == rowsHolder.getSwitchBackendRow() || position == rowsHolder.getSetAvatarRow() || position == rowsHolder.getAddToGroupButtonRow() ||
                position == rowsHolder.getAddToContactsRow() || position == rowsHolder.getLiteModeRow() || position == rowsHolder.getPremiumGiftingRow() || position == rowsHolder.getBusinessRow() ||
                position == rowsHolder.getBotStarsBalanceRow() || position == rowsHolder.getBotTonBalanceRow() || position == rowsHolder.getChannelBalanceRow() || position == rowsHolder.getBotPermissionLocation() ||
                position == rowsHolder.getBotPermissionBiometry() || position == rowsHolder.getBotPermissionEmojiStatus()) {
            return VIEW_TYPE_TEXT;
        } else if (position == rowsHolder.getNotificationsDividerRow()) {
            return VIEW_TYPE_DIVIDER;
        } else if (position == rowsHolder.getNotificationsRow()) {
            return VIEW_TYPE_NOTIFICATIONS_CHECK;
        } else if (position == rowsHolder.getNotificationsSimpleRow()) {
            return VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE;
        } else if (position == rowsHolder.getLastSectionRow() || position == rowsHolder.getMembersSectionRow() ||
                position == rowsHolder.getSecretSettingsSectionRow() || position == rowsHolder.getSettingsSectionRow() || position == rowsHolder.getDevicesSectionRow() ||
                position == rowsHolder.getHelpSectionCell() || position == rowsHolder.getSetAvatarSectionRow() || position == rowsHolder.getPasswordSuggestionSectionRow() ||
                position == rowsHolder.getPhoneSuggestionSectionRow() || position == rowsHolder.getPremiumSectionsRow() || position == rowsHolder.getReportDividerRow() ||
                position == rowsHolder.getChannelDividerRow() || position == rowsHolder.getGraceSuggestionSectionRow() || position == rowsHolder.getBalanceDividerRow() ||
                position == rowsHolder.getBotPermissionsDivider() || position == rowsHolder.getChannelBalanceSectionRow()
        ) {
            return VIEW_TYPE_SHADOW;
        } else if (position >= rowsHolder.getMembersStartRow() && position < rowsHolder.getMembersEndRow()) {
            return VIEW_TYPE_USER;
        } else if (position == rowsHolder.getEmptyRow()) {
            return VIEW_TYPE_EMPTY;
        } else if (position == rowsHolder.getBottomPaddingRow()) {
            return VIEW_TYPE_BOTTOM_PADDING;
        } else if (position == rowsHolder.getSharedMediaRow()) {
            return VIEW_TYPE_SHARED_MEDIA;
        } else if (position == rowsHolder.getVersionRow()) {
            return VIEW_TYPE_VERSION;
        } else if (position == rowsHolder.getPasswordSuggestionRow() || position == rowsHolder.getPhoneSuggestionRow() || position == rowsHolder.getGraceSuggestionRow()) {
            return VIEW_TYPE_SUGGESTION;
        } else if (position == rowsHolder.getAddToGroupInfoRow()) {
            return VIEW_TYPE_ADDTOGROUP_INFO;
        } else if (position == rowsHolder.getPremiumRow()) {
            return VIEW_TYPE_PREMIUM_TEXT_CELL;
        } else if (position == rowsHolder.getStarsRow()) {
            return VIEW_TYPE_STARS_TEXT_CELL;
        } else if (position == rowsHolder.getBizLocationRow()) {
            return VIEW_TYPE_LOCATION;
        } else if (position == rowsHolder.getBizHoursRow()) {
            return VIEW_TYPE_HOURS;
        } else if (position == rowsHolder.getChannelRow()) {
            return VIEW_TYPE_CHANNEL;
        } else if (position == rowsHolder.getBotAppRow()) {
            return VIEW_TYPE_BOT_APP;
        } else if (position == rowsHolder.getInfoSectionRow() || position == rowsHolder.getInfoAffiliateRow()) {
            return VIEW_TYPE_SHADOW_TEXT;
        } else if (position == rowsHolder.getAffiliateRow()) {
            return VIEW_TYPE_COLORFUL_TEXT;
        }
        return 0;
    }

    private void checkForRowsHolder() {
        if (rowsHolder == null) {
            rowsHolder = components.getRowsAndStatusComponentsHolder();
        }
    }
}
