package org.telegram.ui.profile.view.button;


import org.telegram.messenger.R;

public enum ButtonViewEnum {
    QR_CODE(R.drawable.msg_qr_mini, R.string.QrCode),
    JOIN(R.drawable.mini_call_join, R.string.VoipChatJoin),
    MESSAGE(R.drawable.msg_message_s, R.string.TypeMessage),
    CHANGE_AVATAR(R.drawable.msg_edit, R.string.Edit),
    UNMUTE(R.drawable.list_unmute, R.string.Unmute),
    GIFT(R.drawable.filled_gift_simple, R.string.ActionStarGift),
    CALL(R.drawable.calls_menu_phone, R.string.Call),
    VIDEO(R.drawable.chat_calls_video, R.string.GroupCallCreateVideo),
    SHARE(R.drawable.share_arrow, R.string.VoipChatShare),
    LEAVE(R.drawable.msg_leave, R.string.VoipGroupLeave),
    LIVE_STREAM(R.drawable.msg_noise_on, R.string.StartVoipChannelTitle),
    MUTE(R.drawable.list_mute, R.string.Mute),
    DISCUSS(R.drawable.msg_discuss, R.string.Discussion),
    ADD_STORY(R.drawable.msg_mini_addstory, R.string.AddStory),
    BLOCK(R.drawable.msg_block, R.string.BizBotStop),
    REPORT(R.drawable.msg_report_other, R.string.ReportChat);

    public final int icon;
    public final int title;
    ButtonViewEnum(int icon, int title) {
        this.icon = icon;
        this.title = title;
    }
}
