package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends Activity {
    private String _password;
    private String _adminEmail;
    private String _deviceName;

    private static final String TAG = "LockIt";

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
        setContentView(R.layout.activity_apps_list);

        manager = getPackageManager();

        _password = getIntent().getStringExtra(AppConstants.PASSWORD_EXTRA);
        _adminEmail = getIntent().getStringExtra(AppConstants.ADMIN_EMAIL);
        _deviceName = getIntent().getStringExtra(AppConstants.DEVICE_NAME);

        ((EditText) findViewById(R.id.passwordText)).setText(_password);
        if (!checkTelephony()) {
            findViewById(R.id.phoneLayout).setVisibility(View.GONE);
            findViewById(R.id.quickPhoneLayout).setVisibility(View.GONE);
        }
        loadApps();
        loadListView(0);
        addClickListener();

        //callback from app info update
        this.registerReceiver(this._appInfoReceiver, new IntentFilter(AppConstants.APP_INFO_UPDATE_RECEIVER));

        Intent i = new Intent(AppConstants.APP_PAUSE_UPDATE_RECEIVER);
        i.putExtra("pause", true);
        sendBroadcast(i);
    }

    private boolean checkTelephony() {
        return manager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    private PackageManager manager;
    private List<AppDetail> apps;

    private void loadApps() {
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager).toString();
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            app.type = AppConstants.APP_APP_TYPE;
            apps.add(app);
        }

        AppDetail app = new AppDetail();
        app.label = "App Drawer";
        app.name = AppConstants.APP_DRAWER_NAME;
        app.icon = getDrawableImaage(AppConstants.APP_DRAWER_ICON, this);
        app.type = AppConstants.ACTIVITY_APP_TYPE;
        apps.add(app);

        Collections.sort(apps);
    }

    private Drawable getDrawableImaage(String name, Context context) {
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(name, AppConstants.DRAWABLE_RESOURCE, context.getPackageName());
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

    private ListView list;

    private void loadListView(int position) {
        list = (ListView) findViewById(R.id.apps_list);

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        final List<AppDetail> savedApps = db.getAllApps();

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.list_item,
                apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                convertView.setId(position);

                ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageDrawable(apps.get(position).icon);

                TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
                appLabel.setText(apps.get(position).label);

                TextView appName = (TextView) convertView.findViewById(R.id.item_app_name);
                appName.setText(apps.get(position).name);
                appName.setTextColor(Color.parseColor(AppConstants.HIDDEN_TEXT_COLOR));

                if (compareApps(apps.get(position).name, savedApps)) {
                    AppDetail sa = getApp(apps.get(position).name, savedApps);
                    if (sa != null && sa.type.equals(AppConstants.BLOCKED_APP_TYPE)) {
                        appLabel.setText(apps.get(position).label + AppConstants.BLOCKED_APP);
                    } else if (sa != null && sa.type.equals(AppConstants.APP_APP_TYPE)) {
                        appLabel.setText(apps.get(position).label + AppConstants.ADDED_APP);
                    }
                    apps.get(position).added = true;
                }

                return convertView;
            }
        };

        list.setAdapter(adapter);
        list.setSelection(position);
    }

    private void addClickListener() {
        final DatabaseHandler db = DatabaseHandler.getInstance(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                if (apps.get(pos).added) {
                    AppDetail app = apps.get(pos).copy();
                    db.deleteApp(app.name);
                    app.type = AppConstants.SKIPPED_APP_TYPE;
                    db.addorUpdateApp(app);
                    apps.get(pos).added = false;
                } else {
                    db.addorUpdateApp(apps.get(pos));
                }

                loadListView(list.getFirstVisiblePosition());
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
                AppDetail app = apps.get(position).copy();
                boolean add = false;
                if (app.added) {
                    add = true;
                    db.deleteApp(app.name);
                    app.type = AppConstants.SKIPPED_APP_TYPE;
                    db.addorUpdateApp(app);
                    apps.get(position).added = false;
                } else {
                    add = true;
                    app.type = AppConstants.BLOCKED_APP_TYPE;
                    db.addorUpdateApp(app);
                }

                loadListView(list.getFirstVisiblePosition());
                Intent i = new Intent(AppConstants.APP_BLOCK_LIST_UPDATE_RECEIVER);
                i.putExtra("add", add);
                i.putExtra("pName", app.name);
                sendBroadcast(i);
                return true;
            }
        });
    }

    public void openSettings(View v) {
        Intent i = manager.getLaunchIntentForPackage(AppConstants.SETTINGS_PACKAGE);
        SettingsActivity.this.startActivity(i);
    }

    public void openAppSettings(View v) {
        Intent i = new Intent(this, AppSettings.class);
        i.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        i.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
        i.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        startActivityForResult(i, AppConstants.MAIN_INTENT_CODE);
    }

    public void openPlayStore(View v) {
        Intent i = manager.getLaunchIntentForPackage(AppConstants.PLAY_STORE_PACKAGE);
        SettingsActivity.this.startActivity(i);
    }

    public void openAppDrawer(View v) {
        Intent i = new Intent(this, LockItAppDrawer.class);
        i.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        i.putExtra(AppConstants.IS_ADMIN_MODE, true);
        i.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
        i.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        startActivityForResult(i, AppConstants.MAIN_INTENT_CODE);
    }

    public void setPassword(View v) {
        try {
            _password = ((EditText) findViewById(R.id.passwordText)).getText().toString();
            File sdcard = Environment.getExternalStorageDirectory();

            File file = new File(sdcard, String.format("%s/%s", AppConstants.APP_DIRECTORY, AppConstants.APP_FILE));

            FileWriter fw = new FileWriter(file);
            fw.write(AppConstants.PWD_IN_DB);
            fw.flush();
            fw.close();
            DatabaseHandler db = DatabaseHandler.getInstance(this);
            AppInfo appInfo = new AppInfo();
            appInfo.password = _password;
            appInfo.firstTime = AppConstants.NOT_FIRST_TIME;
            db.updateAppInfo(appInfo);
            ShowToast(AppConstants.PASSWORD_SAVED_MSG);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    public void replaceContactsApp(View v) {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"));
        startActivity(i);
    }

    public void openPhone(View v) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        startActivityForResult(intent, 1);
    }

    public void addPhone(View v) {
        final Context context = this;
        LayoutInflater inflater = getLayoutInflater();
        final View f1 = inflater.inflate(R.layout.phone_contact_layout, null);
        new AlertDialog.Builder(this)
                .setTitle("Add Quick Call to Home Screen")
                .setMessage("Enter Information for Quick Call Shortcut")
                .setView(f1)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final DatabaseHandler db = DatabaseHandler.getInstance(context);
                        AppDetail app = new AppDetail();
                        app.name = ((EditText) f1.findViewById(R.id.contactPhone)).getText().toString();
                        app.label = AppConstants.TELEPHONE_APP_STARTNAME + " " + ((EditText) f1.findViewById(R.id.contactName)).getText().toString();
                        app.type = AppConstants.TELEPHONE_APP_TYPE;
                        db.addorUpdateApp(app);
                        CheckBox cb = (CheckBox) f1.findViewById(R.id.addTextMsg);
                        if (cb.isChecked()) {
                            app = new AppDetail();
                            app.name = AppConstants.TEXT_APP_TYPE + ((EditText) f1.findViewById(R.id.contactPhone)).getText().toString();
                            app.label = AppConstants.TEXT_APP_STARTNAME + " " + ((EditText) f1.findViewById(R.id.contactName)).getText().toString();
                            app.type = AppConstants.TEXT_APP_TYPE;
                            db.addorUpdateApp(app);
                        }
                        ShowToast("Quick Call shortcut added to approved list.");
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void ShowToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        done();
    }

    private void done() {
        Intent intent = new Intent();
        intent.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        setResult(RESULT_OK, intent);
        Intent i = new Intent(AppConstants.APP_PAUSE_UPDATE_RECEIVER);
        i.putExtra("pause", false);
        sendBroadcast(i);
        finish();
    }
}
