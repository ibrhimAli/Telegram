package org.telegram.ui.profile.utils;

import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.ProfileActivity;

public class URLs {

    public static void openUrl(ProfileActivity profileActivity, String url, Browser.Progress progress) {
        if (url.startsWith("@")) {
            profileActivity.getMessagesController().openByUserName(url.substring(1), profileActivity, 0, progress);
        } else if (url.startsWith("#") || url.startsWith("$")) {
            DialogsActivity fragment = new DialogsActivity(null);
            fragment.setSearchString(url);
            profileActivity.presentFragment(fragment);
        } else if (url.startsWith("/")) {
            if (profileActivity.getParentLayout().getFragmentStack().size() > 1) {
                BaseFragment previousFragment = profileActivity.getParentLayout().getFragmentStack().get(profileActivity.getParentLayout().getFragmentStack().size() - 2);
                if (previousFragment instanceof ChatActivity) {
                    profileActivity.finishFragment();
                    ((ChatActivity) previousFragment).getChatActivityEnterView().setCommand(null, url, false, false);
                }
            }
        }
    }

}
