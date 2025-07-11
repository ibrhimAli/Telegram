package org.telegram.ui.profile.builder;

import org.telegram.ui.ProfileActivity;
import org.telegram.ui.profile.adapter.ListAdapter;
import org.telegram.ui.profile.utils.ActionBar;
import org.telegram.ui.profile.utils.Adapter;
import org.telegram.ui.profile.utils.Animation;
import org.telegram.ui.profile.utils.ClicksAndPress;
import org.telegram.ui.profile.utils.ColorsUtils;
import org.telegram.ui.profile.utils.Layouts;
import org.telegram.ui.profile.utils.ListViewClick;
import org.telegram.ui.profile.utils.OpenAndWrite;
import org.telegram.ui.profile.utils.ProfileData;
import org.telegram.ui.profile.view.ClippedListView;

import java.util.function.Function;

public class ComponentsFactory {

    private ProfileActivity profileActivity;
    private OpenAndWrite openAndWrite;
    private Adapter adapter;
    private ActionBar actionBar;
    private ProfileData profileData;
    private Animation animation;
    private ClicksAndPress clicksAndPress;
    private ListViewClick listViewClick;
    private ClippedListView listView;
    //private ChatData chatData;
    private ColorsUtils colorsUtils;
    private Layouts layouts;
    private ViewComponentsHolder viewComponentsHolder;
    private AttributesComponentsHolder attributesComponentsHolder;
    private ListAdapter listAdapter;
    private RowsAndStatusComponentsHolder rowsAndStatusComponentsHolder;





    private ComponentsFactory() {
        // Private constructor for use in builder
    }

    public ProfileActivity getProfileActivity() {
        return profileActivity;
    }

    public OpenAndWrite getOpenAndWrite() {
        return openAndWrite;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public ProfileData getProfileData() {
        return profileData;
    }

    public Animation getAnimation() {
        return animation;
    }

    public ClicksAndPress getClicksAndPress() {
        return clicksAndPress;
    }

    public ListViewClick getListViewClick() {
        return listViewClick;
    }

    public ClippedListView getListView() {
        return listView;
    }

//    public ChatData getChatData() {
//        return chatData;
//    }

    public ColorsUtils getColorsUtils() {
        return colorsUtils;
    }

    public Layouts getLayouts() {
        return layouts;
    }

    public ViewComponentsHolder getViewComponentsHolder() {
        return viewComponentsHolder;
    }

    public AttributesComponentsHolder getAttributesComponentsHolder() {
        return attributesComponentsHolder;
    }

    public ListAdapter getListAdapter() {
        return listAdapter;
    }

    public RowsAndStatusComponentsHolder getRowsAndStatusComponentsHolder() {
        return rowsAndStatusComponentsHolder;
    }

    public static class ComponentsFactoryBuilder {
        private final ProfileActivity profileActivity;
        private Function<ComponentsFactory, OpenAndWrite> openAndWriteFactory;
        private Function<ComponentsFactory, Adapter> adapterFactory;
        private Function<ComponentsFactory, ActionBar> actionBarFactory;
        private Function<ComponentsFactory, ProfileData> profileDataFactory;
        //private Function<ComponentsFactory, ChatData> chatDataFactory;
        private Function<ComponentsFactory, ColorsUtils> colorsUtilsFactory;
        private Function<ComponentsFactory, ClippedListView> listViewFactory;
        private Function<ComponentsFactory, Animation> animationFactory;
        private Function<ComponentsFactory, ClicksAndPress> clicksAndPressFactory;
        private Function<ComponentsFactory, ListViewClick> listViewClickFactory;
        private Function<ComponentsFactory, Layouts> layoutsFactory;
        private Function<ComponentsFactory, ViewComponentsHolder> viewComponentsHolderFactory;
        private Function<ComponentsFactory, AttributesComponentsHolder> attributesComponentsHolderFactory;
        private Function<ComponentsFactory, ListAdapter> listAdapterFactory;
        private Function<ComponentsFactory, RowsAndStatusComponentsHolder> rowsAndStatusComponentsHolderFactory;

        public ComponentsFactoryBuilder(ProfileActivity profileActivity) {
            this.profileActivity = profileActivity;
        }

        public ComponentsFactoryBuilder setAdapterFactory(Function<ComponentsFactory, Adapter> factory) {
            this.adapterFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setOpenAndWriteFactory(Function<ComponentsFactory, OpenAndWrite> factory) {
            this.openAndWriteFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setActionBarFactory(Function<ComponentsFactory, ActionBar> factory) {
            this.actionBarFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setProfileDataFactory(Function<ComponentsFactory, ProfileData> factory) {
            this.profileDataFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setAnimationFactory(Function<ComponentsFactory, Animation> factory) {
            this.animationFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setClicksAndPressFactory(Function<ComponentsFactory, ClicksAndPress> factory) {
            this.clicksAndPressFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setListViewClickFactory(Function<ComponentsFactory, ListViewClick> factory) {
            this.listViewClickFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setListViewFactory(Function<ComponentsFactory, ClippedListView> factory) {
            this.listViewFactory = factory;
            return this;
        }

//        public ComponentsFactoryBuilder setChatDataFactory(Function<ComponentsFactory, ChatData> factory) {
//            this.chatDataFactory = factory;
//            return this;
//        }

        public ComponentsFactoryBuilder setColorsUtilsFactory(Function<ComponentsFactory, ColorsUtils> factory) {
            this.colorsUtilsFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setLayoutsFactory(Function<ComponentsFactory, Layouts> factory) {
            this.layoutsFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setViewComponentsHolderFactory(Function<ComponentsFactory, ViewComponentsHolder> factory) {
            this.viewComponentsHolderFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setAttributesComponentsHolderFactory(Function<ComponentsFactory, AttributesComponentsHolder> factory) {
            this.attributesComponentsHolderFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setListAdapterFactory(Function<ComponentsFactory, ListAdapter> factory) {
            this.listAdapterFactory = factory;
            return this;
        }

        public ComponentsFactoryBuilder setRowsAndStatusComponentsHolderFactory(Function<ComponentsFactory, RowsAndStatusComponentsHolder> factory) {
            this.rowsAndStatusComponentsHolderFactory = factory;
            return this;
        }

        public ComponentsFactory build() {
            ComponentsFactory factory = new ComponentsFactory();
            factory.profileActivity = this.profileActivity;

            // Components can safely reference `factory` here
            factory.actionBar = actionBarFactory.apply(factory);
            factory.adapter = adapterFactory.apply(factory);
            factory.animation = animationFactory.apply(factory);
            factory.clicksAndPress = clicksAndPressFactory.apply(factory);
            factory.listViewClick = listViewClickFactory.apply(factory);
            factory.openAndWrite = openAndWriteFactory.apply(factory);
            factory.profileData = profileDataFactory.apply(factory);
            factory.listView = listViewFactory.apply(factory);
            //factory.chatData = chatDataFactory.apply(factory);
            factory.colorsUtils = colorsUtilsFactory.apply(factory);
            factory.layouts = layoutsFactory.apply(factory);
            factory.viewComponentsHolder = viewComponentsHolderFactory.apply(factory);
            factory.attributesComponentsHolder = attributesComponentsHolderFactory.apply(factory);
            factory.listAdapter = listAdapterFactory.apply(factory);
            factory.rowsAndStatusComponentsHolder = rowsAndStatusComponentsHolderFactory.apply(factory);

            return factory;
        }
    }
}
