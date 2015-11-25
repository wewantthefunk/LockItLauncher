package com.whitepaladingames.lockitlauncher;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class LauncherActivity extends Activity implements IAlertBoxCaller {
    private boolean _isGearVisible;
    private String _passwordHint;
    private String _password;
    private boolean _isHomeUnlocked;
    private CountDownTimer _countdown;
    private List<AppDetail> apps;
    private ArrayList<String> _blockedApps;
    private PackageManager manager;
    private ListView list;
    private String _adminEmail;
    private String _deviceName;
    private boolean _useTimeout;
    private ArrayList<LockItInAppPurchase> _availablePurchases;
    private Bundle UPGRADE_LIST_BUNDLE;
    private ArrayList<String> UPGRADE_ITEM_LIST;
    private boolean _timeoutPaused;
    private String marketAssociatedEmailId;
    private AppInfo _appInfo;

    private static String TAG = "LockIt";

    private BatteryBroadcastReceiver _batteryReceiver = new BatteryBroadcastReceiver() {
    };

    private TimeBroadcastReceiver _timeReceiver;

    private AppInfoUpdateReceiver _appInfoReceiver = new AppInfoUpdateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _adminEmail = intent.getStringExtra(AppConstants.ADMIN_EMAIL);
            _deviceName = intent.getStringExtra(AppConstants.DEVICE_NAME);
        }
    };

    private BroadcastReceiver _timerToggleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _useTimeout = intent.getBooleanExtra(AppConstants.USE_SCREEN_TIMEOUT, false);
            _timeoutPaused = intent.getBooleanExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, false);
        }
    };

    private BroadcastReceiver _dailyTimerFireReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (_useTimeout || _timeoutPaused) {
                String time = intent.getStringExtra(AppConstants.TIME);
                String total = intent.getStringExtra(AppConstants.TOTAL_TIME);
                String s = AppConstants.EMPTY_STRING;
                if (!total.equals("1")) s = "s";
                ((TextView) findViewById(R.id.totalTimeView)).setText(String.format("%s of %s minute%s today", time, total, s));
            } else {
                ((TextView) findViewById(R.id.totalTimeView)).setText(AppConstants.EMPTY_STRING);
            }
        }
    };

    IInAppBillingService _service;

    ServiceConnection _serviceConn;

    Thread _playServicesThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        _timeoutPaused = false;

        final Context context = this;
        final IAlertBoxCaller caller = this;

        marketAssociatedEmailId = "";
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        if (accounts.length > 0) {
            marketAssociatedEmailId = accounts[0].name;
        }

        final String mainEmail = marketAssociatedEmailId;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                _serviceConn = new ServiceConnection() {
                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        _service = null;
                    }

                    @Override
                    public void onServiceConnected(ComponentName name,
                                                   IBinder service) {
                        _service = IInAppBillingService.Stub.asInterface(service);
                        try {
                            UPGRADE_ITEM_LIST = new ArrayList<>();
                            UPGRADE_ITEM_LIST.add(AppConstants.IN_APP_PURCHASE_GOLD_LEVEL);
                            UPGRADE_LIST_BUNDLE = new Bundle();
                            UPGRADE_LIST_BUNDLE.putStringArrayList(AppConstants.GOOGLE_PLAY_BILLING_ITEM_ID_LIST, UPGRADE_ITEM_LIST);
                            Bundle inAppPurchase = _service.getSkuDetails(AppConstants.GOOGLE_PLAY_API_VERSION, getPackageName(), AppConstants.GOOGLE_PLAY_IN_APP_TYPE, UPGRADE_LIST_BUNDLE);

                            int response = inAppPurchase.getInt(AppConstants.GOOGLE_PLAY_BILLING_RESPONSE_CODE);
                            if (response == AppConstants.GOOGLE_PLAY_BILLING_RESPONSE_OK) {
                                ArrayList<String> responseList = inAppPurchase.getStringArrayList("DETAILS_LIST");

                                if (responseList != null) {
                                    for (String thisResponse : responseList) {
                                        JSONObject object = new JSONObject(thisResponse);
                                        LockItInAppPurchase purchase = new LockItInAppPurchase();
                                        purchase.currency = object.getString("price_currency_code");
                                        purchase.productId = object.getString("productId");
                                        purchase.price = object.getString("price");
                                        purchase.name = object.getString("title").replace("(LockIt Launcher)", AppConstants.EMPTY_STRING);
                                        purchase.description = object.getString("description");
                                        purchase.type = object.getString("type");
                                        _availablePurchases.add(purchase);
                                    }
                                }
                            }

                            String continuationToken = AppConstants.EMPTY_STRING;
                            int length = _availablePurchases.size();

                            while (continuationToken != null) {
                                Bundle ownedItems = _service.getPurchases(AppConstants.GOOGLE_PLAY_API_VERSION, getPackageName(), AppConstants.GOOGLE_PLAY_IN_APP_TYPE, null);

                                response = ownedItems.getInt(AppConstants.GOOGLE_PLAY_BILLING_RESPONSE_CODE);
                                if (response == AppConstants.GOOGLE_PLAY_BILLING_RESPONSE_OK) {
                                    ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                                    ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                                    //ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                                    continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                                    if (purchaseDataList != null) {
                                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                                            //String purchaseData = purchaseDataList.get(i);
                                            //String signature = signatureList.get(i);
                                            String sku = null;
                                            if (ownedSkus != null) {
                                                sku = ownedSkus.get(i);
                                            }

                                            for (int y = 0; y < length; y++) {
                                                if (_availablePurchases.get(y).productId.equals(sku)) {
                                                    _availablePurchases.get(y).purchased = 1;
                                                    if (_availablePurchases.get(y).productId.equals(AppConstants.IN_APP_PURCHASE_GOLD_LEVEL)) {
                                                        _useTimeout = true;
                                                        View v = findViewById(R.id.main_apps_list);
                                                        v.setPadding(0, 0, 0, 50);
                                                        View v1 = findViewById(R.id.main_extra_actions);
                                                        v1.setVisibility(View.VISIBLE);
                                                    }
                                                    y = length + 1;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (Arrays.asList(AppConstants.DEBUG_EMAILS).contains(mainEmail)) {
                                _useTimeout = true;
                                View v = findViewById(R.id.main_apps_list);
                                v.setPadding(0, 0, 0, 50);
                                View v1 = findViewById(R.id.main_extra_actions);
                                v1.setVisibility(View.VISIBLE);
                                for (int y = 0; y < length; y++) {
                                    _availablePurchases.get(y).purchased = 1;
                                }
                            }

                            Intent i = new Intent(AppConstants.IN_APP_PURCHASES_DONE_RECEIVER);
                            i.putExtra(AppConstants.IN_APP_PURCHASE_DATA, new InAppPurchaseDataWrapper(_availablePurchases));
                            i.putExtra(AppConstants.USE_SCREEN_TIMEOUT, _useTimeout);
                            sendBroadcast(i);
                        } catch (RemoteException ex) {
                            AlertBox.ShowAlert(context, "Unable to reach Google Play Store", "We are unable to reach the Google Play Store to verify your In-App purchases.\nSorry for the inconvenience.", caller);
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    }
                };

                Intent serviceIntent = new Intent(AppConstants.GOOGLE_PLAY_SERVICES_INTENT_ACTION);
                serviceIntent.setPackage(AppConstants.GOOGLE_PLAY_SERVICES_PACKAGE);
                bindService(serviceIntent, _serviceConn, Context.BIND_AUTO_CREATE);
            }
        };

        _availablePurchases = new ArrayList<>();
        _playServicesThread = new Thread(runnable);
        _playServicesThread.start();

        finishInitializing();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //no apps are running, so stop counting time
        Intent i = new Intent();
        i.putExtra(AppConstants.ADMIN_MODE, false);
        i.putExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, true);
        _timeReceiver.notShown(this, i);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        //switched to an app are running, so start counting time again

        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isPhoneLocked = myKM.inKeyguardRestrictedInputMode();

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean isSceenAwake;
        isSceenAwake = powerManager.isScreenOn();

        Intent i = new Intent();
        i.putExtra(AppConstants.ADMIN_MODE, false);
        i.putExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, (isPhoneLocked || !isSceenAwake));
        _timeReceiver.notShown(this, i);
    }

    private void finishInitializing() {
        _passwordHint = AppConstants.DEFAULT_PWD_HINT;
        _password = new StringBuilder(AppConstants.DEFAULT_PWD_HINT).reverse().toString();

        // get basic app info
        DatabaseHandler db = DatabaseHandler.getInstance(this);

        _appInfo = db.getAppInfo();
        setViewBackground(_appInfo.wallpaper.replace("~", "/"), findViewById(R.id.main_apps_list));
        _adminEmail = _appInfo.adminEmail;
        _deviceName = _appInfo.deviceName;
        _useTimeout = _appInfo.useTimout;

        if (Arrays.asList(AppConstants.DEBUG_EMAILS).contains(marketAssociatedEmailId)) {
            _useTimeout = true;
            View v = findViewById(R.id.main_apps_list);
            v.setPadding(0, 0, 0, 50);
            View v1 = findViewById(R.id.main_extra_actions);
            v1.setVisibility(View.VISIBLE);
        }

        AppTimer _appTimer = db.getTimer(_appInfo.lastUser);
        _appTimer._totalTime = _appInfo.totalTime;

        File sdcard = Environment.getExternalStorageDirectory();

        String folder_main = AppConstants.APP_DIRECTORY;

        StringBuilder text = new StringBuilder();

        boolean dirOk = true;

        File f = new File(sdcard, folder_main);
        if (!f.exists()) {
            dirOk = f.mkdirs();
        }

        if (dirOk) {

            try {
                File file = new File(sdcard, String.format("%s/%s", folder_main, AppConstants.APP_FILE));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                }

                br.close();

                if (!text.toString().equals(AppConstants.EMPTY_STRING) && !text.toString().equals(AppConstants.PWD_IN_DB)) {
                    _passwordHint = AppConstants.EMPTY_STRING;
                    _password = text.toString();
                } else {
                    if (!_appInfo.password.equals(AppConstants.EMPTY_STRING)) {
                        _passwordHint = AppConstants.EMPTY_STRING;
                        _password = _appInfo.password;
                    }
                }
            } catch (IOException fe) {
                Log.d(TAG, fe.toString());
            }
        }
        //hide parental controls
        _isHomeUnlocked = false;
        _isGearVisible = false;
        findViewById(R.id.apps_button).getBackground().setAlpha(AppConstants.OPACITY_NONE);
        findViewById(R.id.lock_button).getBackground().setAlpha(AppConstants.OPACITY_NONE);

        //show approved apps
        initializeApps();

        //battery level
        _batteryReceiver.setTextView((TextView) findViewById(R.id.batteryLevel));
        _batteryReceiver.setChargingTextView((TextView) findViewById(R.id.batteryCharge));
        this.registerReceiver(this._batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //time and date
        _timeReceiver = new TimeBroadcastReceiver(this, _password, _adminEmail, _deviceName, _useTimeout) {
        };
        _timeReceiver.setTextView((TextView) findViewById(R.id.currentTime));
        _timeReceiver.setTimerInfo(_appTimer);
        this.registerReceiver(this._timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        _timeReceiver.fire(this, getIntent());

        //timer for hiding the settings button
        _countdown = new CountDownTimer(AppConstants.POST_PHONE_CALL_WAIT, AppConstants.POST_PHONE_CALL_WAIT) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                findViewById(R.id.apps_button).getBackground().setAlpha(AppConstants.OPACITY_NONE);
                _isGearVisible = false;
            }
        };

        //callback from app info update
        this.registerReceiver(this._appInfoReceiver, new IntentFilter(AppConstants.APP_INFO_UPDATE_RECEIVER));

        this.registerReceiver(this._timerToggleReceiver, new IntentFilter(AppConstants.TIMER_TOGGLE_RECEIVER));

        this.registerReceiver(this._dailyTimerFireReceiver, new IntentFilter(AppConstants.TIMER_UPDATE_RECEIVER));

        Intent _serviceIntent = new Intent(this, AppStartService.class);
        _serviceIntent.putStringArrayListExtra(AppConstants.BLOCKED_APPS_LIST, _blockedApps);
        _serviceIntent.putExtra(AppConstants.ADMIN_EMAIL, _appInfo.adminEmail);
        _serviceIntent.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        startService(_serviceIntent);
    }

    private void initializeApps() {
        loadApps();
        loadListView();
        addClickListener();
    }

    public void timerClick(View v) {
        _timeReceiver.set_shown();
        Intent panel = new Intent(this, TimerUpActivity.class);
        panel.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        panel.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
        panel.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        panel.putExtra(AppConstants.USE_SCREEN_TIMEOUT, _useTimeout);
        panel.putExtra(AppConstants.IN_APP_PURCHASE_DATA, new InAppPurchaseDataWrapper(_availablePurchases));
        panel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            this.startActivity(panel);
        } catch (Exception e) {
            Log.d("LIC", e.toString());
        }
    }

    public void showApps(View v) {
        _countdown.cancel();
        if (!_isGearVisible) {
            _isGearVisible = true;
            findViewById(R.id.apps_button).getBackground().setAlpha(AppConstants.OPACITY_FULL);
            _countdown.start();
        } else {
            final IAlertBoxCaller activity = this;
            final Context context = this;
            AlertBox.ShowTextEntry3Button(context, "Authentication Required", "Enter Your Password", _passwordHint, "Unlock Home", "Settings", true, activity, null);
        }
    }

    public void setWallpaper(View v) {
        Intent i = new Intent(AppConstants.APP_PAUSE_UPDATE_RECEIVER);
        i.putExtra(AppConstants.PAUSE_APP_CHECK, true);
        sendBroadcast(i);

        _timeReceiver.pauseTimer();

        Intent intent = new Intent();
        intent.setType(AppConstants.IMAGE_SELECTION_LIST_ALL);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, AppConstants.SELECT_PICTURE_STRING), AppConstants.SELECT_PICTURE_ACTIVITY_CODE);
    }

    //load the approved apps
    private void loadApps() {
        manager = getPackageManager();
        apps = new ArrayList<>();
        _blockedApps = new ArrayList<>();
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<AppDetail> savedApps = db.getAllApps();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {
            if (ri != null) {
                AppDetail a = getApp(ri.activityInfo.packageName, savedApps);
                AppDetail app = new AppDetail();
                app.label = ri.loadLabel(manager).toString();
                app.name = ri.activityInfo.packageName;
                app.icon = ri.activityInfo.loadIcon(manager);
                app.type = AppConstants.APP_APP_TYPE;
                app.launchActivity = manager.getLaunchIntentForPackage(ri.activityInfo.packageName).toString();
                if (compareApps(ri.activityInfo.packageName, savedApps)) {
                    if (a != null && !a.type.equals(AppConstants.BLOCKED_APP_TYPE) && !a.type.equals(AppConstants.SKIPPED_APP_TYPE)) {
                        apps.add(app);
                    }
                } else {
                    app.type = AppConstants.BLOCKED_APP_TYPE;
                    db.addorUpdateApp(app);
                }
            }
        }

        for (int x = 0; x < savedApps.size(); x++) {
            AppDetail a = savedApps.get(x);
            if (a.type != null && a.type.equals(AppConstants.TELEPHONE_APP_TYPE)) {
                AppDetail app = new AppDetail();
                app.label = a.label;
                app.name = a.name;
                app.icon = getDrawableImaage("mipmap/phone_icon", this);
                app.type = AppConstants.TELEPHONE_APP_TYPE;
                apps.add(app);
            } else if (a.type != null && a.type.equals(AppConstants.TEXT_APP_TYPE)) {
                AppDetail app = new AppDetail();
                app.label = a.label;
                app.name = a.name;
                app.icon = getDrawableImaage("mipmap/text", this);
                app.type = AppConstants.TEXT_APP_TYPE;
                apps.add(app);
            } else if (a.type != null && a.type.equals(AppConstants.ACTIVITY_APP_TYPE)) {
                AppDetail app = new AppDetail();
                app.label = a.label;
                app.name = a.name;
                app.icon = getDrawableImaage("mipmap/appdrawer2", this);
                app.type = AppConstants.ACTIVITY_APP_TYPE;
                apps.add(app);
            } else if (a.type != null && a.type.equals(AppConstants.BLOCKED_APP_TYPE)) {
                AppDetail app = new AppDetail();
                app.label = a.label;
                app.name = a.name;
                app.icon = getDrawableImaage("mipmap/no", this);
                app.type = AppConstants.BLOCKED_APP_TYPE;
                _blockedApps.add(app.name);
            }
        }

        Collections.sort(apps);
    }

    private Drawable getDrawableImaage(String name, Context context) {
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(name, "drawable",
                context.getPackageName());
        return resources.getDrawable(resourceId);
    }

    private boolean compareApps(String app, List<AppDetail> list) {
        for (int x = 0; x < list.size(); x++) {
            if (list.get(x).name.equals(app)) {
                return true;
            }
        }

        return false;
    }

    private AppDetail getApp(String app, List<AppDetail> list) {
        for (int x = 0; x < list.size(); x++) {
            if (list.get(x).name.equals(app)) {
                return list.get(x);
            }
        }

        return null;
    }

    private void loadListView() {
        list = (ListView) findViewById(R.id.main_apps_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.list_item,
                apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (apps.get(position).type.equals(AppConstants.BLOCKED_APP_TYPE) || apps.get(position).type.equals(AppConstants.SKIPPED_APP_TYPE))
                    return null;
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageDrawable(apps.get(position).icon);

                TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
                appLabel.setText(apps.get(position).label);

                TextView appName = (TextView) convertView.findViewById(R.id.item_app_name);
                String name = apps.get(position).name;

                if (name.startsWith(AppConstants.TEXT_APP_TYPE))
                    appName.setText(name.replace(AppConstants.TEXT_APP_TYPE, AppConstants.EMPTY_STRING));
                else
                    appName.setText(name);

                if (apps.get(position).type.equals(AppConstants.APP_APP_TYPE) || apps.get(position).type.equals(AppConstants.ACTIVITY_APP_TYPE))
                    appName.setTextColor(Color.parseColor(AppConstants.HIDDEN_TEXT_COLOR));

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    private void addClickListener() {
        final IAlertBoxCaller activity = this;
        final Context context = this;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                if (_isHomeUnlocked) return;

                //starting an app, so start the timer countdown
                Intent intent = new Intent();
                intent.putExtra(AppConstants.ADMIN_MODE, false);
                intent.putExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, false);
                _timeReceiver.notShown(context, intent);

                Intent i;
                if (apps.get(pos).label.startsWith(AppConstants.TELEPHONE_APP_STARTNAME)) {
                    i = new Intent(Intent.ACTION_CALL, Uri.parse(AppConstants.TELEPHONE_APP_TYPE + ":" + apps.get(pos).name));
                } else if (apps.get(pos).label.startsWith(AppConstants.TEXT_APP_STARTNAME)) {
                    i = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.SMS_APP_TYPE + ":" + apps.get(pos).name));
                } else if (apps.get(pos).type.equals(AppConstants.ACTIVITY_APP_TYPE)) {
                    Class c = null;
                    switch (apps.get(pos).name) {
                        case AppConstants.APP_DRAWER_NAME:
                            c = LockItAppDrawer.class;
                            break;
                    }
                    i = new Intent(context, c);
                    i.putExtra(AppConstants.PASSWORD_EXTRA, _password);
                    i.putExtra(AppConstants.IS_ADMIN_MODE, false);
                    i.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
                    i.putExtra(AppConstants.DEVICE_NAME, _deviceName);
                    i.putStringArrayListExtra(AppConstants.BLOCKED_APPS_LIST, _blockedApps);
                } else {
                    i = manager.getLaunchIntentForPackage(apps.get(pos).name);
                }

                startActivity(i);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
                if (_isHomeUnlocked) {
                    AlertBox.ShowOKCancel(context, "Delete App from Home Screen?", "Do you want to delete this app from the Home Screen?", activity, AppConstants.DELETE_FROM_HOME_PREFIX + AppConstants.SEPARATOR + apps.get(position).name);
                }
                return true;
            }
        });
    }

    public void lockScreen(View v) {
        ShowToast(AppConstants.HOME_SCREEN_LOCKED);
        _isHomeUnlocked = false;
        findViewById(R.id.lock_button).getBackground().setAlpha(AppConstants.OPACITY_NONE);
    }

    private void ShowToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @SuppressWarnings("deprecation")
    private String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    @SuppressWarnings("deprecation")
    private void setViewBackground(String path, View view) {
        File imgFile = new File(path);

        if(imgFile.exists()) {
            Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
            view.setBackgroundDrawable(d);
        }
    }

    //AlertBox Callback Methods
    @Override
    public void AlertBoxCallback(DialogInterface dialog, int which, String extra) {

    }

    @Override
    public void MessageBoxCallback(DialogInterface dialog, int which, String extra) {
    }

    @Override
    public void OKCancelCallback(DialogInterface dialog, int which, String extra) {
        if (which == -1) {
            if (extra.startsWith(AppConstants.DELETE_FROM_HOME_PREFIX)) {
                String[] ex = extra.split(AppConstants.SEPARATOR_REGEX);
                DatabaseHandler db = DatabaseHandler.getInstance(this);
                if (!db.deleteApp(ex[1])) {
                    final IAlertBoxCaller activity = this;
                    final Context context = this;
                    AlertBox.ShowMessageBox(context, "App not removed!", "Unable to remove app from Home Screen", activity);
                } else {
                    initializeApps();
                }
            }
        }
    }

    @Override
    public void OKCancelCallback(DialogInterface dialog, int which, Object extra) {

    }

    @Override
    public void ThreeButtonCallback(DialogInterface dialog, int which, String extra) {

    }

    @Override
    public void TextEntryBoxCallback(DialogInterface dialog, int which, String text, String extra) {
        findViewById(R.id.apps_button).getBackground().setAlpha(AppConstants.OPACITY_NONE);
        _isGearVisible = false;
        if (which == AppConstants.ALERT_BOX_CANCEL) return;
        final IAlertBoxCaller activity = this;
        final Context context = this;
        if (text.equals(AppConstants.PASSWORD_RESET)) {
            try {
                File sdcard = Environment.getExternalStorageDirectory();

                File file = new File(sdcard, String.format("%s/%s", AppConstants.APP_DIRECTORY, AppConstants.APP_FILE));

                boolean b = file.delete();
                if (!b)
                    Log.d(TAG, "Unable to delete file.");

                _password = new StringBuilder(AppConstants.DEFAULT_PWD_HINT).reverse().toString();
                _passwordHint = AppConstants.DEFAULT_PWD_HINT;
                ShowToast(AppConstants.PASSWORD_SAVED_MSG);
            } catch (Exception e) {
                Log.d(TAG, "Unable to delete file.");
            }
        } else if (!text.equals(_password)) {
            AlertBox.ShowMessageBox(context, AppConstants.NOT_AUTHORIZED_TITLE, AppConstants.NOT_AUTHORIZED_MSG, activity, null);
        } else {
            if (which == AppConstants.ALERT_BOX_OK) {
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                Intent i = new Intent(context, SettingsActivity.class);
                i.putExtra(AppConstants.PASSWORD_EXTRA, _password);
                i.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
                i.putExtra(AppConstants.USE_SCREEN_TIMEOUT, _useTimeout);
                i.putExtra(AppConstants.IN_APP_PURCHASE_DATA, new InAppPurchaseDataWrapper(_availablePurchases));
                startActivityForResult(i, AppConstants.MAIN_INTENT_CODE);
            } else {
                _isHomeUnlocked = true;
                findViewById(R.id.lock_button).getBackground().setAlpha(AppConstants.OPACITY_FULL);
                ShowToast(AppConstants.HOME_SCREEN_UNLOCKED);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.MAIN_INTENT_CODE:
                    _password = data.getStringExtra("pwd");
                    if (!_password.equals(new StringBuilder(AppConstants.DEFAULT_PWD_HINT).reverse().toString())) {
                        _passwordHint = AppConstants.EMPTY_STRING;
                    }
                    initializeApps();
                    break;
                case AppConstants.SELECT_PICTURE_ACTIVITY_CODE:
                    try {
                        Uri selectedImageUri = data.getData();
                        String selectedImagePath = getPath(selectedImageUri);
                        setViewBackground(selectedImagePath, findViewById(R.id.main_apps_list));
                        _appInfo.wallpaper = selectedImagePath.replace("/", "~");
                        DatabaseHandler db = DatabaseHandler.getInstance(this);
                        db.updateAppInfo(_appInfo);
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                    Intent i = new Intent(AppConstants.APP_PAUSE_UPDATE_RECEIVER);
                    i.putExtra(AppConstants.PAUSE_APP_CHECK, false);
                    sendBroadcast(i);

                    _timeReceiver.resumeTimer();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_service != null) {
            unbindService(_serviceConn);
        }
    }
}
