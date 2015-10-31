package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        _passwordHint = AppConstants.DEFAULT_PWD_HINT;
        _password = new StringBuilder(AppConstants.DEFAULT_PWD_HINT).reverse().toString();

        // get basic app info
        DatabaseHandler db = DatabaseHandler.getInstance(this);

        AppInfo appInfo = db.getAppInfo();
        _adminEmail = appInfo.adminEmail;
        _deviceName = appInfo.deviceName;

        AppTimer appTimer = db.getTimer(appInfo.lastUser);
        appTimer._totalTime = appInfo.totalTime;

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
                    if (!appInfo.password.equals(AppConstants.EMPTY_STRING)) {
                        _passwordHint = AppConstants.EMPTY_STRING;
                        _password = appInfo.password;
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
        _timeReceiver = new TimeBroadcastReceiver(this, _password, _adminEmail, _deviceName) {
        };
        _timeReceiver.setTextView((TextView) findViewById(R.id.currentTime));
        _timeReceiver.setTimerInfo(appTimer);
        this.registerReceiver(this._timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        //run the timer for the first time
        _timeReceiver.onReceive(this, getIntent());

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

        Intent _serviceIntent = new Intent(this, AppStartService.class);
        _serviceIntent.putStringArrayListExtra(AppConstants.BLOCKED_APPS_LIST, _blockedApps);
        _serviceIntent.putExtra(AppConstants.ADMIN_EMAIL, appInfo.adminEmail);
        _serviceIntent.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        startService(_serviceIntent);
    }

    private void initializeApps() {
        loadApps();
        loadListView();
        addClickListener();
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
                if (apps.get(position).type.equals(AppConstants.BLOCKED_APP_TYPE) || apps.get(position).type.equals(AppConstants.SKIPPED_APP_TYPE)) return null;
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
                i.putExtra(AppConstants.DEVICE_NAME, _deviceName);
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
            if (requestCode == AppConstants.MAIN_INTENT_CODE) {
                _password = data.getStringExtra("pwd");
                if (!_password.equals(new StringBuilder(AppConstants.DEFAULT_PWD_HINT).reverse().toString())) {
                    _passwordHint = AppConstants.EMPTY_STRING;
                }
                initializeApps();
            }
        }
    }

}
