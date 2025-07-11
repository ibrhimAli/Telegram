package org.telegram.ui.profile.builder;

import androidx.annotation.Keep;

import org.telegram.ui.ProfileActivity;

public class RowsAndStatusComponentsHolder {

    private ProfileActivity profileActivity;
    private ComponentsFactory componentsFactory;

    private int bioRow;
    private int notificationRow;
    private int numberRow;
    private int privacyRow;
    private int languageRow;
    private int setUsernameRow;
    private int versionRow;
    private int dataRow;
    private int chatRow;
    private int questionRow;
    private int devicesRow;
    private int filtersRow;
    private int stickersRow;
    private int faqRow;
    private int policyRow;
    private int sendLogsRow;
    private int sendLastLogsRow;
    private int clearLogsRow;
    private int switchBackendRow;
    private int setAvatarRow;
    private int addToGroupButtonRow;
    private int premiumRow;
    private int premiumGiftingRow;
    private int businessRow;
    private int liteModeRow;
    private int birthdayRow;
    private int channelRow;
    private int starsRow;
    private int locationRow;
    private int infoHeaderRow;
    private int membersHeaderRow;
    private int numberSectionRow;
    private int settingsSectionRow2;
    private int helpHeaderRow;
    private int debugHeaderRow;
    private int botPermissionsHeader;
    private int phoneRow;
    private int usernameRow;
    private int userInfoRow;
    private int channelInfoRow;
    private int settingsTimerRow;
    private int settingsKeyRow;
    private int reportRow;
    private int reportReactionRow;
    private int subscribersRow;
    private int subscribersRequestsRow;
    private int administratorsRow;
    private int settingsRow;
    private int blockedUsersRow;
    private int addMemberRow;
    private int joinRow;
    private int unblockRow;
    private int sendMessageRow;
    private int addToContactsRow;
    private int botStarsBalanceRow;
    private int botTonBalanceRow;
    private int channelBalanceRow;
    @Keep
    private int botPermissionLocation;
    @Keep
    private int botPermissionBiometry;
    @Keep
    private int botPermissionEmojiStatus;
    private int notificationsDividerRow;
    private int notificationsRow;
    private int notificationsSimpleRow;
    private int lastSectionRow;
    private int membersSectionRow;
    private int secretSettingsSectionRow;
    private int settingsSectionRow;
    private int devicesSectionRow;
    private int helpSectionCell;
    private int setAvatarSectionRow;
    private int passwordSuggestionSectionRow;
    private int phoneSuggestionSectionRow;
    private int premiumSectionsRow;
    private int reportDividerRow;
    private int channelDividerRow;
    private int graceSuggestionSectionRow;
    private int balanceDividerRow;
    private int botPermissionsDivider;
    private int channelBalanceSectionRow;
    private int membersStartRow;
    private int membersEndRow;
    private int emptyRow;
    private int bottomPaddingRow;
    private int sharedMediaRow;
    private int passwordSuggestionRow;
    private int phoneSuggestionRow;
    private int graceSuggestionRow;
    private int addToGroupInfoRow;
    private int bizLocationRow;
    private int bizHoursRow;
    private int botAppRow;
    private int infoSectionRow;
    private int infoAffiliateRow;
    private int affiliateRow;

    private int infoStartRow, infoEndRow;
    private int savedScrollPosition;
    private int savedScrollOffset;
    private int listContentHeight;
    private int lastMeasuredContentWidth;
    private int onlineCount = -1;
    private float searchTransitionProgress;
    private boolean openAnimationInProgress;
    private float extraHeight;
    private int overlayCountVisible;
    private int actionBarBackgroundColor;
    private int searchTransitionOffset;
    private float mediaHeaderAnimationProgress;
    private float avatarAnimationProgress;
    private float avatarX;
    private float avatarY;
    private float expandProgress;
    private float avatarScale;
    private float currentExpanAnimatorFracture;
    private float onlineX;
    private float onlineY;
    private float nameX;
    private float nameY;
    private float lastEmojiStatusProgress;
    private float initialAnimationExtraHeight;
    private float listViewVelocityY;
    private float customPhotoOffset;
    private int collectibleHintBackgroundColor;
    private float currentExpandAnimatorValue;
    private float titleAnimationsYDiff;
    private Long emojiStatusGiftId;
    private int playProfileAnimation;
    private float floatingButtonHideProgress;

    private boolean isTopic;
    private boolean savedScrollToSharedMedia;
    private boolean allowPullingDown;
    private boolean isBot;
    private boolean userBlocked;
    private boolean needSendMessage;
    private boolean showAddToContacts;
    private boolean sharedMediaLayoutAttached;
    private boolean isFragmentPhoneNumber;
    private boolean fragmentOpened;
    private boolean isPulledDown;
    private boolean editItemVisible;
    private boolean hoursShownMine;
    private boolean hoursExpanded;
    private boolean floatingHidden;
    private boolean waitCanSendStoryRequest;
    private boolean searchMode;
    private boolean callItemVisible;
    private boolean videoCallItemVisible;
    private boolean expandPhoto;
    private boolean hasFallbackPhoto;
    private boolean hasCustomPhoto;
    private boolean preloadedChannelEmojiStatuses;
    private boolean isInLandscapeMode;
    private boolean doNotSetForeground;
    private Boolean collectibleHintVisible;
    private boolean isQrItemVisible = true;
    private boolean openingAvatar;
    private boolean scrolling;
    private boolean fragmentViewAttached;
    private boolean mediaHeaderVisible;
    private boolean allowProfileAnimation = true;
    private boolean transitionAnimationInProress;
    private boolean invalidateScroll = true;
    private boolean canSearchMembers;
    private boolean hasVoiceChatItem;
    private boolean needTimerImage;
    private boolean needStarImage;
    private boolean reportSpam;
    private boolean disableProfileAnimation = false;
    private boolean creatingChat;
    private boolean firstLayout = true;
    private int reportReactionMessageId = 0;
    private int usersForceShowingIn = 0;
    private String vcardPhone;
    private long reportReactionFromDialogId = 0;

    public RowsAndStatusComponentsHolder(ProfileActivity profileActivity){
        this.profileActivity = profileActivity;
    }

    public void setComponents(ComponentsFactory componentsFactory) {
        this.componentsFactory = componentsFactory;
    }

    public int getBioRow() {
        return bioRow;
    }

    public void setBioRow(int bioRow) {
        this.bioRow = bioRow;
    }

    public int getNotificationRow() {
        return notificationRow;
    }

    public void setNotificationRow(int notificationRow) {
        this.notificationRow = notificationRow;
    }

    public int getNumberRow() {
        return numberRow;
    }

    public void setNumberRow(int numberRow) {
        this.numberRow = numberRow;
    }

    public int getPrivacyRow() {
        return privacyRow;
    }

    public void setPrivacyRow(int privacyRow) {
        this.privacyRow = privacyRow;
    }

    public int getLanguageRow() {
        return languageRow;
    }

    public void setLanguageRow(int languageRow) {
        this.languageRow = languageRow;
    }

    public int getSetUsernameRow() {
        return setUsernameRow;
    }

    public void setSetUsernameRow(int setUsernameRow) {
        this.setUsernameRow = setUsernameRow;
    }

    public int getVersionRow() {
        return versionRow;
    }

    public void setVersionRow(int versionRow) {
        this.versionRow = versionRow;
    }

    public int getDataRow() {
        return dataRow;
    }

    public void setDataRow(int dataRow) {
        this.dataRow = dataRow;
    }

    public int getChatRow() {
        return chatRow;
    }

    public void setChatRow(int chatRow) {
        this.chatRow = chatRow;
    }

    public int getQuestionRow() {
        return questionRow;
    }

    public void setQuestionRow(int questionRow) {
        this.questionRow = questionRow;
    }

    public int getDevicesRow() {
        return devicesRow;
    }

    public void setDevicesRow(int devicesRow) {
        this.devicesRow = devicesRow;
    }

    public int getFiltersRow() {
        return filtersRow;
    }

    public void setFiltersRow(int filtersRow) {
        this.filtersRow = filtersRow;
    }

    public int getStickersRow() {
        return stickersRow;
    }

    public void setStickersRow(int stickersRow) {
        this.stickersRow = stickersRow;
    }

    public int getFaqRow() {
        return faqRow;
    }

    public void setFaqRow(int faqRow) {
        this.faqRow = faqRow;
    }

    public int getPolicyRow() {
        return policyRow;
    }

    public void setPolicyRow(int policyRow) {
        this.policyRow = policyRow;
    }

    public int getSendLogsRow() {
        return sendLogsRow;
    }

    public void setSendLogsRow(int sendLogsRow) {
        this.sendLogsRow = sendLogsRow;
    }

    public int getSendLastLogsRow() {
        return sendLastLogsRow;
    }

    public void setSendLastLogsRow(int sendLastLogsRow) {
        this.sendLastLogsRow = sendLastLogsRow;
    }

    public int getClearLogsRow() {
        return clearLogsRow;
    }

    public void setClearLogsRow(int clearLogsRow) {
        this.clearLogsRow = clearLogsRow;
    }

    public int getSwitchBackendRow() {
        return switchBackendRow;
    }

    public void setSwitchBackendRow(int switchBackendRow) {
        this.switchBackendRow = switchBackendRow;
    }

    public int getSetAvatarRow() {
        return setAvatarRow;
    }

    public void setSetAvatarRow(int setAvatarRow) {
        this.setAvatarRow = setAvatarRow;
    }

    public int getAddToGroupButtonRow() {
        return addToGroupButtonRow;
    }

    public void setAddToGroupButtonRow(int addToGroupButtonRow) {
        this.addToGroupButtonRow = addToGroupButtonRow;
    }

    public int getPremiumRow() {
        return premiumRow;
    }

    public void setPremiumRow(int premiumRow) {
        this.premiumRow = premiumRow;
    }

    public int getPremiumGiftingRow() {
        return premiumGiftingRow;
    }

    public void setPremiumGiftingRow(int premiumGiftingRow) {
        this.premiumGiftingRow = premiumGiftingRow;
    }

    public int getBusinessRow() {
        return businessRow;
    }

    public void setBusinessRow(int businessRow) {
        this.businessRow = businessRow;
    }

    public int getLiteModeRow() {
        return liteModeRow;
    }

    public void setLiteModeRow(int liteModeRow) {
        this.liteModeRow = liteModeRow;
    }

    public int getBirthdayRow() {
        return birthdayRow;
    }

    public void setBirthdayRow(int birthdayRow) {
        this.birthdayRow = birthdayRow;
    }

    public int getChannelRow() {
        return channelRow;
    }

    public void setChannelRow(int channelRow) {
        this.channelRow = channelRow;
    }

    public int getStarsRow() {
        return starsRow;
    }

    public void setStarsRow(int starsRow) {
        this.starsRow = starsRow;
    }

    public int getLocationRow() {
        return locationRow;
    }

    public void setLocationRow(int locationRow) {
        this.locationRow = locationRow;
    }

    public int getInfoHeaderRow() {
        return infoHeaderRow;
    }

    public void setInfoHeaderRow(int infoHeaderRow) {
        this.infoHeaderRow = infoHeaderRow;
    }

    public int getMembersHeaderRow() {
        return membersHeaderRow;
    }

    public void setMembersHeaderRow(int membersHeaderRow) {
        this.membersHeaderRow = membersHeaderRow;
    }

    public int getNumberSectionRow() {
        return numberSectionRow;
    }

    public void setNumberSectionRow(int numberSectionRow) {
        this.numberSectionRow = numberSectionRow;
    }

    public int getSettingsSectionRow2() {
        return settingsSectionRow2;
    }

    public void setSettingsSectionRow2(int settingsSectionRow2) {
        this.settingsSectionRow2 = settingsSectionRow2;
    }

    public int getHelpHeaderRow() {
        return helpHeaderRow;
    }

    public void setHelpHeaderRow(int helpHeaderRow) {
        this.helpHeaderRow = helpHeaderRow;
    }

    public int getDebugHeaderRow() {
        return debugHeaderRow;
    }

    public void setDebugHeaderRow(int debugHeaderRow) {
        this.debugHeaderRow = debugHeaderRow;
    }

    public int getBotPermissionsHeader() {
        return botPermissionsHeader;
    }

    public void setBotPermissionsHeader(int botPermissionsHeader) {
        this.botPermissionsHeader = botPermissionsHeader;
    }

    public int getPhoneRow() {
        return phoneRow;
    }

    public void setPhoneRow(int phoneRow) {
        this.phoneRow = phoneRow;
    }

    public int getUsernameRow() {
        return usernameRow;
    }

    public void setUsernameRow(int usernameRow) {
        this.usernameRow = usernameRow;
    }

    public int getUserInfoRow() {
        return userInfoRow;
    }

    public void setUserInfoRow(int userInfoRow) {
        this.userInfoRow = userInfoRow;
    }

    public int getChannelInfoRow() {
        return channelInfoRow;
    }

    public void setChannelInfoRow(int channelInfoRow) {
        this.channelInfoRow = channelInfoRow;
    }

    public int getSettingsTimerRow() {
        return settingsTimerRow;
    }

    public void setSettingsTimerRow(int settingsTimerRow) {
        this.settingsTimerRow = settingsTimerRow;
    }

    public int getSettingsKeyRow() {
        return settingsKeyRow;
    }

    public void setSettingsKeyRow(int settingsKeyRow) {
        this.settingsKeyRow = settingsKeyRow;
    }

    public int getReportRow() {
        return reportRow;
    }

    public void setReportRow(int reportRow) {
        this.reportRow = reportRow;
    }

    public int getReportReactionRow() {
        return reportReactionRow;
    }

    public void setReportReactionRow(int reportReactionRow) {
        this.reportReactionRow = reportReactionRow;
    }

    public int getSubscribersRow() {
        return subscribersRow;
    }

    public void setSubscribersRow(int subscribersRow) {
        this.subscribersRow = subscribersRow;
    }

    public int getSubscribersRequestsRow() {
        return subscribersRequestsRow;
    }

    public void setSubscribersRequestsRow(int subscribersRequestsRow) {
        this.subscribersRequestsRow = subscribersRequestsRow;
    }

    public int getAdministratorsRow() {
        return administratorsRow;
    }

    public void setAdministratorsRow(int administratorsRow) {
        this.administratorsRow = administratorsRow;
    }

    public int getSettingsRow() {
        return settingsRow;
    }

    public void setSettingsRow(int settingsRow) {
        this.settingsRow = settingsRow;
    }

    public int getBlockedUsersRow() {
        return blockedUsersRow;
    }

    public void setBlockedUsersRow(int blockedUsersRow) {
        this.blockedUsersRow = blockedUsersRow;
    }

    public int getAddMemberRow() {
        return addMemberRow;
    }

    public void setAddMemberRow(int addMemberRow) {
        this.addMemberRow = addMemberRow;
    }

    public int getJoinRow() {
        return joinRow;
    }

    public void setJoinRow(int joinRow) {
        this.joinRow = joinRow;
    }

    public int getUnblockRow() {
        return unblockRow;
    }

    public void setUnblockRow(int unblockRow) {
        this.unblockRow = unblockRow;
    }

    public int getSendMessageRow() {
        return sendMessageRow;
    }

    public void setSendMessageRow(int sendMessageRow) {
        this.sendMessageRow = sendMessageRow;
    }

    public int getAddToContactsRow() {
        return addToContactsRow;
    }

    public void setAddToContactsRow(int addToContactsRow) {
        this.addToContactsRow = addToContactsRow;
    }

    public int getBotStarsBalanceRow() {
        return botStarsBalanceRow;
    }

    public void setBotStarsBalanceRow(int botStarsBalanceRow) {
        this.botStarsBalanceRow = botStarsBalanceRow;
    }

    public int getBotTonBalanceRow() {
        return botTonBalanceRow;
    }

    public void setBotTonBalanceRow(int botTonBalanceRow) {
        this.botTonBalanceRow = botTonBalanceRow;
    }

    public int getChannelBalanceRow() {
        return channelBalanceRow;
    }

    public void setChannelBalanceRow(int channelBalanceRow) {
        this.channelBalanceRow = channelBalanceRow;
    }

    public int getNotificationsDividerRow() {
        return notificationsDividerRow;
    }

    public void setNotificationsDividerRow(int notificationsDividerRow) {
        this.notificationsDividerRow = notificationsDividerRow;
    }

    public int getNotificationsRow() {
        return notificationsRow;
    }

    public void setNotificationsRow(int notificationsRow) {
        this.notificationsRow = notificationsRow;
    }

    public int getNotificationsSimpleRow() {
        return notificationsSimpleRow;
    }

    public void setNotificationsSimpleRow(int notificationsSimpleRow) {
        this.notificationsSimpleRow = notificationsSimpleRow;
    }

    public int getLastSectionRow() {
        return lastSectionRow;
    }

    public void setLastSectionRow(int lastSectionRow) {
        this.lastSectionRow = lastSectionRow;
    }

    public int getMembersSectionRow() {
        return membersSectionRow;
    }

    public void setMembersSectionRow(int membersSectionRow) {
        this.membersSectionRow = membersSectionRow;
    }

    public int getSecretSettingsSectionRow() {
        return secretSettingsSectionRow;
    }

    public void setSecretSettingsSectionRow(int secretSettingsSectionRow) {
        this.secretSettingsSectionRow = secretSettingsSectionRow;
    }

    public int getSettingsSectionRow() {
        return settingsSectionRow;
    }

    public void setSettingsSectionRow(int settingsSectionRow) {
        this.settingsSectionRow = settingsSectionRow;
    }

    public int getDevicesSectionRow() {
        return devicesSectionRow;
    }

    public void setDevicesSectionRow(int devicesSectionRow) {
        this.devicesSectionRow = devicesSectionRow;
    }

    public int getHelpSectionCell() {
        return helpSectionCell;
    }

    public void setHelpSectionCell(int helpSectionCell) {
        this.helpSectionCell = helpSectionCell;
    }

    public int getSetAvatarSectionRow() {
        return setAvatarSectionRow;
    }

    public void setSetAvatarSectionRow(int setAvatarSectionRow) {
        this.setAvatarSectionRow = setAvatarSectionRow;
    }

    public int getPasswordSuggestionSectionRow() {
        return passwordSuggestionSectionRow;
    }

    public void setPasswordSuggestionSectionRow(int passwordSuggestionSectionRow) {
        this.passwordSuggestionSectionRow = passwordSuggestionSectionRow;
    }

    public int getPhoneSuggestionSectionRow() {
        return phoneSuggestionSectionRow;
    }

    public void setPhoneSuggestionSectionRow(int phoneSuggestionSectionRow) {
        this.phoneSuggestionSectionRow = phoneSuggestionSectionRow;
    }

    public int getPremiumSectionsRow() {
        return premiumSectionsRow;
    }

    public void setPremiumSectionsRow(int premiumSectionsRow) {
        this.premiumSectionsRow = premiumSectionsRow;
    }

    public int getReportDividerRow() {
        return reportDividerRow;
    }

    public void setReportDividerRow(int reportDividerRow) {
        this.reportDividerRow = reportDividerRow;
    }

    public int getChannelDividerRow() {
        return channelDividerRow;
    }

    public void setChannelDividerRow(int channelDividerRow) {
        this.channelDividerRow = channelDividerRow;
    }

    public int getGraceSuggestionSectionRow() {
        return graceSuggestionSectionRow;
    }

    public void setGraceSuggestionSectionRow(int graceSuggestionSectionRow) {
        this.graceSuggestionSectionRow = graceSuggestionSectionRow;
    }

    public int getBalanceDividerRow() {
        return balanceDividerRow;
    }

    public void setBalanceDividerRow(int balanceDividerRow) {
        this.balanceDividerRow = balanceDividerRow;
    }

    public int getBotPermissionsDivider() {
        return botPermissionsDivider;
    }

    public void setBotPermissionsDivider(int botPermissionsDivider) {
        this.botPermissionsDivider = botPermissionsDivider;
    }

    public int getChannelBalanceSectionRow() {
        return channelBalanceSectionRow;
    }

    public void setChannelBalanceSectionRow(int channelBalanceSectionRow) {
        this.channelBalanceSectionRow = channelBalanceSectionRow;
    }

    public int getMembersStartRow() {
        return membersStartRow;
    }

    public void setMembersStartRow(int membersStartRow) {
        this.membersStartRow = membersStartRow;
    }

    public int getMembersEndRow() {
        return membersEndRow;
    }

    public void setMembersEndRow(int membersEndRow) {
        this.membersEndRow = membersEndRow;
    }

    public int getEmptyRow() {
        return emptyRow;
    }

    public void setEmptyRow(int emptyRow) {
        this.emptyRow = emptyRow;
    }

    public int getBottomPaddingRow() {
        return bottomPaddingRow;
    }

    public void setBottomPaddingRow(int bottomPaddingRow) {
        this.bottomPaddingRow = bottomPaddingRow;
    }

    public int getSharedMediaRow() {
        return sharedMediaRow;
    }

    public void setSharedMediaRow(int sharedMediaRow) {
        this.sharedMediaRow = sharedMediaRow;
    }

    public int getPasswordSuggestionRow() {
        return passwordSuggestionRow;
    }

    public void setPasswordSuggestionRow(int passwordSuggestionRow) {
        this.passwordSuggestionRow = passwordSuggestionRow;
    }

    public int getPhoneSuggestionRow() {
        return phoneSuggestionRow;
    }

    public void setPhoneSuggestionRow(int phoneSuggestionRow) {
        this.phoneSuggestionRow = phoneSuggestionRow;
    }

    public int getGraceSuggestionRow() {
        return graceSuggestionRow;
    }

    public void setGraceSuggestionRow(int graceSuggestionRow) {
        this.graceSuggestionRow = graceSuggestionRow;
    }

    public int getAddToGroupInfoRow() {
        return addToGroupInfoRow;
    }

    public void setAddToGroupInfoRow(int addToGroupInfoRow) {
        this.addToGroupInfoRow = addToGroupInfoRow;
    }

    public int getBizLocationRow() {
        return bizLocationRow;
    }

    public void setBizLocationRow(int bizLocationRow) {
        this.bizLocationRow = bizLocationRow;
    }

    public int getBizHoursRow() {
        return bizHoursRow;
    }

    public void setBizHoursRow(int bizHoursRow) {
        this.bizHoursRow = bizHoursRow;
    }

    public int getBotAppRow() {
        return botAppRow;
    }

    public void setBotAppRow(int botAppRow) {
        this.botAppRow = botAppRow;
    }

    public int getInfoSectionRow() {
        return infoSectionRow;
    }

    public void setInfoSectionRow(int infoSectionRow) {
        this.infoSectionRow = infoSectionRow;
    }

    public int getInfoAffiliateRow() {
        return infoAffiliateRow;
    }

    public void setInfoAffiliateRow(int infoAffiliateRow) {
        this.infoAffiliateRow = infoAffiliateRow;
    }

    public int getAffiliateRow() {
        return affiliateRow;
    }

    public void setAffiliateRow(int affiliateRow) {
        this.affiliateRow = affiliateRow;
    }

    public int getInfoStartRow() {
        return infoStartRow;
    }

    public void setInfoStartRow(int infoStartRow) {
        this.infoStartRow = infoStartRow;
    }

    public int getInfoEndRow() {
        return infoEndRow;
    }

    public void setInfoEndRow(int infoEndRow) {
        this.infoEndRow = infoEndRow;
    }

    public int getBotPermissionLocation() {
        return botPermissionLocation;
    }

    public void setBotPermissionLocation(int botPermissionLocation) {
        this.botPermissionLocation = botPermissionLocation;
    }

    public int getBotPermissionBiometry() {
        return botPermissionBiometry;
    }

    public void setBotPermissionBiometry(int botPermissionBiometry) {
        this.botPermissionBiometry = botPermissionBiometry;
    }

    public int getBotPermissionEmojiStatus() {
        return botPermissionEmojiStatus;
    }

    public void setBotPermissionEmojiStatus(int botPermissionEmojiStatus) {
        this.botPermissionEmojiStatus = botPermissionEmojiStatus;
    }

    public int getSavedScrollPosition() {
        return savedScrollPosition;
    }

    public void setSavedScrollPosition(int savedScrollPosition) {
        this.savedScrollPosition = savedScrollPosition;
    }

    public int getSavedScrollOffset() {
        return savedScrollOffset;
    }

    public void setSavedScrollOffset(int savedScrollOffset) {
        this.savedScrollOffset = savedScrollOffset;
    }

    public int getListContentHeight() {
        return listContentHeight;
    }

    public void setListContentHeight(int listContentHeight) {
        this.listContentHeight = listContentHeight;
    }

    public int getLastMeasuredContentWidth() {
        return lastMeasuredContentWidth;
    }

    public void setLastMeasuredContentWidth(int lastMeasuredContentWidth) {
        this.lastMeasuredContentWidth = lastMeasuredContentWidth;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public float getSearchTransitionProgress() {
        return searchTransitionProgress;
    }

    public void setSearchTransitionProgress(float searchTransitionProgress) {
        this.searchTransitionProgress = searchTransitionProgress;
    }

    public boolean isOpenAnimationInProgress() {
        return openAnimationInProgress;
    }

    public void setOpenAnimationInProgress(boolean openAnimationInProgress) {
        this.openAnimationInProgress = openAnimationInProgress;
    }

    public float getExtraHeight() {
        return extraHeight;
    }

    public void setExtraHeight(float extraHeight) {
        this.extraHeight = extraHeight;
    }

    public int getOverlayCountVisible() {
        return overlayCountVisible;
    }

    public void setOverlayCountVisible(int overlayCountVisible) {
        this.overlayCountVisible = overlayCountVisible;
    }

    public int getActionBarBackgroundColor() {
        return actionBarBackgroundColor;
    }

    public void setActionBarBackgroundColor(int actionBarBackgroundColor) {
        this.actionBarBackgroundColor = actionBarBackgroundColor;
    }

    public int getSearchTransitionOffset() {
        return searchTransitionOffset;
    }

    public void setSearchTransitionOffset(int searchTransitionOffset) {
        this.searchTransitionOffset = searchTransitionOffset;
    }

    public float getMediaHeaderAnimationProgress() {
        return mediaHeaderAnimationProgress;
    }

    public void setMediaHeaderAnimationProgress(float mediaHeaderAnimationProgress) {
        this.mediaHeaderAnimationProgress = mediaHeaderAnimationProgress;
    }

    public float getAvatarAnimationProgress() {
        return avatarAnimationProgress;
    }

    public void setAvatarAnimationProgress(float avatarAnimationProgress) {
        this.avatarAnimationProgress = avatarAnimationProgress;
    }

    public float getAvatarX() {
        return avatarX;
    }

    public void setAvatarX(float avatarX) {
        this.avatarX = avatarX;
    }

    public float getAvatarY() {
        return avatarY;
    }

    public void setAvatarY(float avatarY) {
        this.avatarY = avatarY;
    }

    public float getExpandProgress() {
        return expandProgress;
    }

    public void setExpandProgress(float expandProgress) {
        this.expandProgress = expandProgress;
    }

    public float getAvatarScale() {
        return avatarScale;
    }

    public void setAvatarScale(float avatarScale) {
        this.avatarScale = avatarScale;
    }

    public float getCurrentExpanAnimatorFracture() {
        return currentExpanAnimatorFracture;
    }

    public void setCurrentExpanAnimatorFracture(float currentExpanAnimatorFracture) {
        this.currentExpanAnimatorFracture = currentExpanAnimatorFracture;
    }

    public float setCurrentExpanAnimatorFractureAndReturn(float currentExpanAnimatorFracture) {
        return this.currentExpanAnimatorFracture = currentExpanAnimatorFracture;
    }

    public float getOnlineX() {
        return onlineX;
    }

    public void setOnlineX(float onlineX) {
        this.onlineX = onlineX;
    }

    public float getOnlineY() {
        return onlineY;
    }

    public void setOnlineY(float onlineY) {
        this.onlineY = onlineY;
    }

    public float getNameX() {
        return nameX;
    }

    public void setNameX(float nameX) {
        this.nameX = nameX;
    }

    public float getNameY() {
        return nameY;
    }

    public void setNameY(float nameY) {
        this.nameY = nameY;
    }
    public float setNameYAndReturn(float nameY) {
        return this.nameY = nameY;
    }

    public float getLastEmojiStatusProgress() {
        return lastEmojiStatusProgress;
    }

    public void setLastEmojiStatusProgress(float lastEmojiStatusProgress) {
        this.lastEmojiStatusProgress = lastEmojiStatusProgress;
    }

    public float getInitialAnimationExtraHeight() {
        return initialAnimationExtraHeight;
    }

    public void setInitialAnimationExtraHeight(float initialAnimationExtraHeight) {
        this.initialAnimationExtraHeight = initialAnimationExtraHeight;
    }

    public float getListViewVelocityY() {
        return listViewVelocityY;
    }

    public void setListViewVelocityY(float listViewVelocityY) {
        this.listViewVelocityY = listViewVelocityY;
    }

    public float getCustomPhotoOffset() {
        return customPhotoOffset;
    }

    public void setCustomPhotoOffset(float customPhotoOffset) {
        this.customPhotoOffset = customPhotoOffset;
    }

    public int getCollectibleHintBackgroundColor() {
        return collectibleHintBackgroundColor;
    }

    public void setCollectibleHintBackgroundColor(int collectibleHintBackgroundColor) {
        this.collectibleHintBackgroundColor = collectibleHintBackgroundColor;
    }

    public float getCurrentExpandAnimatorValue() {
        return currentExpandAnimatorValue;
    }

    public void setCurrentExpandAnimatorValue(float currentExpandAnimatorValue) {
        this.currentExpandAnimatorValue = currentExpandAnimatorValue;
    }
    public float setCurrentExpandAnimatorValueAndReturn(float currentExpandAnimatorValue) {
        return this.currentExpandAnimatorValue = currentExpandAnimatorValue;
    }

    public float getTitleAnimationsYDiff() {
        return titleAnimationsYDiff;
    }

    public void setTitleAnimationsYDiff(float titleAnimationsYDiff) {
        this.titleAnimationsYDiff = titleAnimationsYDiff;
    }

    public Long getEmojiStatusGiftId() {
        return emojiStatusGiftId;
    }

    public void setEmojiStatusGiftId(Long emojiStatusGiftId) {
        this.emojiStatusGiftId = emojiStatusGiftId;
    }

    public int getPlayProfileAnimation() {
        return playProfileAnimation;
    }

    public void setPlayProfileAnimation(int playProfileAnimation) {
        this.playProfileAnimation = playProfileAnimation;
    }

    public float getFloatingButtonHideProgress() {
        return floatingButtonHideProgress;
    }

    public void setFloatingButtonHideProgress(float floatingButtonHideProgress) {
        this.floatingButtonHideProgress = floatingButtonHideProgress;
    }

    public boolean isTopic() {
        return isTopic;
    }

    public void setTopic(boolean topic) {
        isTopic = topic;
    }

    public boolean isSavedScrollToSharedMedia() {
        return savedScrollToSharedMedia;
    }

    public void setSavedScrollToSharedMedia(boolean savedScrollToSharedMedia) {
        this.savedScrollToSharedMedia = savedScrollToSharedMedia;
    }

    public boolean isAllowPullingDown() {
        return allowPullingDown;
    }

    public void setAllowPullingDown(boolean allowPullingDown) {
        this.allowPullingDown = allowPullingDown;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public boolean isUserBlocked() {
        return userBlocked;
    }

    public void setUserBlocked(boolean userBlocked) {
        this.userBlocked = userBlocked;
    }

    public boolean isNeedSendMessage() {
        return needSendMessage;
    }

    public void setNeedSendMessage(boolean needSendMessage) {
        this.needSendMessage = needSendMessage;
    }

    public boolean isShowAddToContacts() {
        return showAddToContacts;
    }

    public void setShowAddToContacts(boolean showAddToContacts) {
        this.showAddToContacts = showAddToContacts;
    }

    public boolean isSharedMediaLayoutAttached() {
        return sharedMediaLayoutAttached;
    }

    public void setSharedMediaLayoutAttached(boolean sharedMediaLayoutAttached) {
        this.sharedMediaLayoutAttached = sharedMediaLayoutAttached;
    }

    public boolean isFragmentPhoneNumber() {
        return isFragmentPhoneNumber;
    }

    public void setFragmentPhoneNumber(boolean fragmentPhoneNumber) {
        isFragmentPhoneNumber = fragmentPhoneNumber;
    }

    public boolean isFragmentOpened() {
        return fragmentOpened;
    }

    public void setFragmentOpened(boolean fragmentOpened) {
        this.fragmentOpened = fragmentOpened;
    }

    public boolean isPulledDown() {
        return isPulledDown;
    }

    public void setPulledDown(boolean pulledDown) {
        isPulledDown = pulledDown;
    }

    public boolean isEditItemVisible() {
        return editItemVisible;
    }

    public void setEditItemVisible(boolean editItemVisible) {
        this.editItemVisible = editItemVisible;
    }

    public boolean isHoursShownMine() {
        return hoursShownMine;
    }

    public void setHoursShownMine(boolean hoursShownMine) {
        this.hoursShownMine = hoursShownMine;
    }

    public boolean isHoursExpanded() {
        return hoursExpanded;
    }

    public void setHoursExpanded(boolean hoursExpanded) {
        this.hoursExpanded = hoursExpanded;
    }

    public boolean isFloatingHidden() {
        return floatingHidden;
    }

    public void setFloatingHidden(boolean floatingHidden) {
        this.floatingHidden = floatingHidden;
    }

    public boolean isWaitCanSendStoryRequest() {
        return waitCanSendStoryRequest;
    }

    public void setWaitCanSendStoryRequest(boolean waitCanSendStoryRequest) {
        this.waitCanSendStoryRequest = waitCanSendStoryRequest;
    }

    public boolean isSearchMode() {
        return searchMode;
    }

    public void setSearchMode(boolean searchMode) {
        this.searchMode = searchMode;
    }

    public boolean isCallItemVisible() {
        return callItemVisible;
    }

    public void setCallItemVisible(boolean callItemVisible) {
        this.callItemVisible = callItemVisible;
    }

    public boolean isVideoCallItemVisible() {
        return videoCallItemVisible;
    }

    public void setVideoCallItemVisible(boolean videoCallItemVisible) {
        this.videoCallItemVisible = videoCallItemVisible;
    }

    public boolean isExpandPhoto() {
        return expandPhoto;
    }

    public void setExpandPhoto(boolean expandPhoto) {
        this.expandPhoto = expandPhoto;
    }

    public boolean isHasFallbackPhoto() {
        return hasFallbackPhoto;
    }

    public void setHasFallbackPhoto(boolean hasFallbackPhoto) {
        this.hasFallbackPhoto = hasFallbackPhoto;
    }

    public boolean isHasCustomPhoto() {
        return hasCustomPhoto;
    }

    public void setHasCustomPhoto(boolean hasCustomPhoto) {
        this.hasCustomPhoto = hasCustomPhoto;
    }

    public boolean isPreloadedChannelEmojiStatuses() {
        return preloadedChannelEmojiStatuses;
    }

    public void setPreloadedChannelEmojiStatuses(boolean preloadedChannelEmojiStatuses) {
        this.preloadedChannelEmojiStatuses = preloadedChannelEmojiStatuses;
    }

    public boolean isInLandscapeMode() {
        return isInLandscapeMode;
    }

    public void setInLandscapeMode(boolean inLandscapeMode) {
        isInLandscapeMode = inLandscapeMode;
    }

    public boolean isDoNotSetForeground() {
        return doNotSetForeground;
    }

    public void setDoNotSetForeground(boolean doNotSetForeground) {
        this.doNotSetForeground = doNotSetForeground;
    }

    public Boolean getCollectibleHintVisible() {
        return collectibleHintVisible;
    }

    public void setCollectibleHintVisible(Boolean collectibleHintVisible) {
        this.collectibleHintVisible = collectibleHintVisible;
    }

    public Boolean setCollectibleHintVisibleAndReturn(Boolean collectibleHintVisible) {
        return this.collectibleHintVisible = collectibleHintVisible;
    }

    public boolean isQrItemVisible() {
        return isQrItemVisible;
    }

    public void setQrItemVisible(boolean qrItemVisible) {
        isQrItemVisible = qrItemVisible;
    }

    public boolean isOpeningAvatar() {
        return openingAvatar;
    }

    public void setOpeningAvatar(boolean openingAvatar) {
        this.openingAvatar = openingAvatar;
    }

    public boolean isScrolling() {
        return scrolling;
    }

    public void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }

    public boolean isFragmentViewAttached() {
        return fragmentViewAttached;
    }

    public void setFragmentViewAttached(boolean fragmentViewAttached) {
        this.fragmentViewAttached = fragmentViewAttached;
    }

    public boolean isMediaHeaderVisible() {
        return mediaHeaderVisible;
    }

    public void setMediaHeaderVisible(boolean mediaHeaderVisible) {
        this.mediaHeaderVisible = mediaHeaderVisible;
    }

    public boolean isAllowProfileAnimation() {
        return allowProfileAnimation;
    }

    public void setAllowProfileAnimation(boolean allowProfileAnimation) {
        this.allowProfileAnimation = allowProfileAnimation;
    }

    public boolean isTransitionAnimationInProress() {
        return transitionAnimationInProress;
    }

    public void setTransitionAnimationInProress(boolean transitionAnimationInProress) {
        this.transitionAnimationInProress = transitionAnimationInProress;
    }

    public boolean isInvalidateScroll() {
        return invalidateScroll;
    }

    public void setInvalidateScroll(boolean invalidateScroll) {
        this.invalidateScroll = invalidateScroll;
    }

    public boolean isCanSearchMembers() {
        return canSearchMembers;
    }

    public void setCanSearchMembers(boolean canSearchMembers) {
        this.canSearchMembers = canSearchMembers;
    }

    public boolean isHasVoiceChatItem() {
        return hasVoiceChatItem;
    }

    public void setHasVoiceChatItem(boolean hasVoiceChatItem) {
        this.hasVoiceChatItem = hasVoiceChatItem;
    }

    public boolean isNeedTimerImage() {
        return needTimerImage;
    }

    public void setNeedTimerImage(boolean needTimerImage) {
        this.needTimerImage = needTimerImage;
    }

    public boolean isNeedStarImage() {
        return needStarImage;
    }

    public void setNeedStarImage(boolean needStarImage) {
        this.needStarImage = needStarImage;
    }

    public boolean isReportSpam() {
        return reportSpam;
    }

    public void setReportSpam(boolean reportSpam) {
        this.reportSpam = reportSpam;
    }

    public boolean isDisableProfileAnimation() {
        return disableProfileAnimation;
    }

    public void setDisableProfileAnimation(boolean disableProfileAnimation) {
        this.disableProfileAnimation = disableProfileAnimation;
    }

    public boolean isCreatingChat() {
        return creatingChat;
    }

    public void setCreatingChat(boolean creatingChat) {
        this.creatingChat = creatingChat;
    }

    public boolean isFirstLayout() {
        return firstLayout;
    }

    public void setFirstLayout(boolean firstLayout) {
        this.firstLayout = firstLayout;
    }

    public int getReportReactionMessageId() {
        return reportReactionMessageId;
    }

    public void setReportReactionMessageId(int reportReactionMessageId) {
        this.reportReactionMessageId = reportReactionMessageId;
    }

    public int getUsersForceShowingIn() {
        return usersForceShowingIn;
    }

    public void setUsersForceShowingIn(int usersForceShowingIn) {
        this.usersForceShowingIn = usersForceShowingIn;
    }

    public String getVcardPhone() {
        return vcardPhone;
    }

    public void setVcardPhone(String vcardPhone) {
        this.vcardPhone = vcardPhone;
    }

    public long getReportReactionFromDialogId() {
        return reportReactionFromDialogId;
    }

    public void setReportReactionFromDialogId(long reportReactionFromDialogId) {
        this.reportReactionFromDialogId = reportReactionFromDialogId;
    }
}
