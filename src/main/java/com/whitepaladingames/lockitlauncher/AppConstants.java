package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class AppConstants {
    public static final String DEFAULT_PWD_HINT = "12345";
    public static final String EMPTY_STRING = "";
    public static final String CHARGING_CHAR = "+";
    public static final String NOT_CHARGING_CHAR = " ";
    public static final int MAIN_INTENT_CODE = 100;
    public static final int OPACITY_FULL = 255;
    public static final int OPACITY_NONE = 0;
    public static final String HOME_SCREEN_UNLOCKED = "Home screen unlocked";
    public static final String HOME_SCREEN_LOCKED = "Home screen locked";
    public static final String NOT_AUTHORIZED_TITLE = "Not Authorized";
    public static final String NOT_AUTHORIZED_MSG = "You are not authorized to do that!";
    public static final String APP_DIRECTORY = "LIL";
    public static final String APP_FILE = "s.txt";
    public static final String PASSWORD_EXTRA = "pwd";
    public static final String SETTINGS_PACKAGE = "com.android.settings";
    public static final String PLAY_STORE_PACKAGE = "com.android.vending";
    public static final String HIDDEN_TEXT_COLOR = "#555555";
    public static final String WHITE_TEXT_COLOR = "#FFFFFF";
    public static final String ADDED_APP = " (APPROVED)";
    public static final String BLOCKED_APP = " (BLOCKED)";
    public static final int POST_PHONE_CALL_WAIT = 5000;
    public static final String DELETE_FROM_HOME_PREFIX = "delete";
    public static final String SEPARATOR = "|";
    public static final String SEPARATOR_REGEX = "\\|";
    public static final int ALERT_BOX_CANCEL = -2;
    public static final int ALERT_BOX_OK = -1;
    public static final String TELEPHONE_APP_TYPE = "tel";
    public static final String TELEPHONE_APP_STARTNAME = "Call";
    public static final String TEXT_APP_STARTNAME = "Text";
    public static final String PASSWORD_RESET = "oops";
    public static final String PASSWORD_SAVED_MSG = "Password has been updated.";
    public static final String APP_APP_TYPE = "app";
    public static final String ACTIVITY_APP_TYPE = "act";
    public static final String TEXT_APP_TYPE = "txt";
    public static final String SMS_APP_TYPE = "sms";
    public static final String APP_DRAWER_NAME = "LockItAppDrawer";
    public static final String APP_DRAWER_ICON = "mipmap/appdrawer2";
    public static final String MONEY_ICON = "mipmap/money";
    public static final String CHECK_MARK_ICON = "mipmap/checkmark";
    public static final String DRAWABLE_RESOURCE = "drawable";
    public static final String BLOCKED_APP_TYPE = "blk";
    public static final String APP_RECEIVER = "com.whitepaladingames.lockitlauncher.CLEARED_LIST_UPDATE";
    public static final String APP_BLOCK_LIST_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.BLOCKED_LIST_UPDATE";
    public static final String APP_PAUSE_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.PAUSE_UPDATE";
    public static final String APP_INFO_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.APP_INFO_UPDATE";
    public static final String BLOCKED_APP_SHOWN_RECEIVER = "com.whitepaladingames.lockitlauncher.BLOCKED_APP_SHOWN";
    public static final String IN_APP_PURCHASES_DONE_RECEIVER = "com.whitepaladingames.lockitlauncher.IN_APP_PURCHASES_DONE";
    public static final String IS_ADMIN_MODE = "isAdmin";
    public static final String ADMIN_EMAIL = "adminEmail";
    public static final String BLOCKED_APPS_LIST = "blockedApps";
    public static final String ADMIN_MODE_LABEL = "* Administrator Mode *";
    public static final String PWD_IN_DB = "lookInDB";
    public static final int NOT_FIRST_TIME = 99;
    public static final int FIRST_TIME = 0;
    public static final String TASK_ID = "taskId";
    public static final String PACKAGE_NAME = "pName";
    public static final String APP_NAME = "aName";
    public static final String DEVICE_NAME = "dName";
    public static final String PAUSE_APP_CHECK = "pause";
    public static final String SKIPPED_APP_TYPE = "skp";
    public static final int DEFAULT_SCREEN_TIMEOUT = 60;
    public static final String SCREEN_TIMEOUT_TIME = "screenTimeoutTime";
    public static final String DEFAULT_USER = "default";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String ADMIN_MODE = "adminMode";
    //public static final String ADMIN_MODE_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.ADMIN_MODE_UPDATE";
    public static final String GOOGLE_PLAY_SERVICES_INTENT_ACTION = "com.android.vending.billing.InAppBillingService.BIND";
    public static final String GOOGLE_PLAY_SERVICES_PACKAGE = "com.android.vending";
    public static final String GOOGLE_PLAY_BILLING_ITEM_ID_LIST = "ITEM_ID_LIST";
    public static final int GOOGLE_PLAY_BILLING_RESPONSE_OK = 0;
    public static final String GOOGLE_PLAY_BILLING_RESPONSE_CODE = "RESPONSE_CODE";
    public static final int GOOGLE_PLAY_API_VERSION = 3;
    public static final String GOOGLE_PLAY_IN_APP_TYPE = "inapp";
    public static final String IN_APP_PURCHASE_DATA = "inAppPurchaseInfo";
    public static final int GOOGLE_PLAY_ACTIVITY_CODE = 1231;
    public static final int IN_APP_PURCHASE_ICON_SIZE = 28;
    public static final int INT_FALSE = 0;
    public static final String IN_APP_PURCHASE_GOLD_LEVEL = "gold_level_features";
    public static final String PURCHASED_GOLD_LEVEL = "com.whitepaladingames.lockitlauncher.PURCHASE_GOLD";
    public static final String USE_SCREEN_TIMEOUT = "useScreenTimeout";
    public static final String PAUSE_SCREEN_TIMEOUT = "pauseScreenTimeout";
    public static final String TIMER_TOGGLE_RECEIVER = "com.whitepaladingames.lockitlauncher.TIMER_TOGGLE";
    public static final String TIME = "currentTime";
    public static final String TOTAL_TIME = "totalTime";
    public static final String TIMER_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.TIMER_UPDATE";
    public static final String TIMER_TIME_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.TIMER_TIME_UPDATE";
    public static final String TIMER_CURRENT_TIME_UPDATE_RECEIVER = "com.whitepaladingames.lockitlauncher.TIMER_CURRENT_TIME_UDPATE";
    public static final String TIME_OUT_ACTIVITY_READY_RECEIVER = "com.whitepaladingames.lockitlauncher.TIME_OUT_ACTIVITY_READY";
    public static final String TIMER_PAUSED_MESSAGE = "Timer Paused";
    public static final String TIMER_FIRE_CAME_FROM = "cameFrom";
    public static final int SELECT_PICTURE_ACTIVITY_CODE = 1232;
    public static final String SELECT_PICTURE_STRING = "Select Background";
    public static final String IMAGE_SELECTION_LIST_ALL = "image/*";
    public static final String WALLPAPER_URL = "wallpaperURL";

    public static final String[] OK_APPS = {"com.android.vending","com.google.android.gms","com.android.deskclock","com.google.android.apps.maps","com.android.settings","com.google.android.calendar", "com.google.android.talk"};

    //"wewantthefunk73@gmail.com",
    public static final String[] DEBUG_EMAILS = {"wewantthefunk73@gmail.com", "marcsantini@gmail.com"};
    public AppConstants() {

    }

    public static void HideKeyboard(Context context, Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
