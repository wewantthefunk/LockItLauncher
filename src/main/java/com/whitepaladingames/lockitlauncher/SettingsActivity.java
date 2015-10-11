package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

/**
 * Created by Chris-laptop on 9/29/2015.
 */
public class SettingsActivity extends Activity {
    private String _password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);

        _password = getIntent().getStringExtra(AppConstants.PASSWORD_EXTRA);
        ((EditText)findViewById(R.id.passwordText)).setText(_password);
        loadApps();
        loadListView(0);
        addClickListener();

    }

    private PackageManager manager;
    private List<AppDetail> apps;
    private void loadApps(){
        manager = getPackageManager();
        apps = new ArrayList();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
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

    private ListView list;
    private void loadListView(int position){
        list = (ListView)findViewById(R.id.apps_list);

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        final List<AppDetail> savedApps = db.getAllApps();

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.list_item,
                apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                convertView.setId(position);

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageDrawable(apps.get(position).icon);

                TextView appLabel = (TextView)convertView.findViewById(R.id.item_app_label);
                appLabel.setText(apps.get(position).label);

                TextView appName = (TextView)convertView.findViewById(R.id.item_app_name);
                appName.setText(apps.get(position).name);

                if (!apps.get(position).type.equals("tel"))
                    appName.setTextColor(Color.parseColor(AppConstants.HIDDEN_TEXT_COLOR));

                if (compareApps(apps.get(position).name, savedApps)) {
                    appLabel.setText(apps.get(position).label + AppConstants.ADDED_APP);
                }

                return convertView;
            }
        };

        list.setAdapter(adapter);
        list.setSelection(position);
    }

    private void addClickListener(){
        final DatabaseHandler db = DatabaseHandler.getInstance(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                db.addorUpdateUser(apps.get(pos));
                loadListView(list.getFirstVisiblePosition());
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
        startActivityForResult(i, AppConstants.MAIN_INTENT_CODE);
    }

    public void setPassword(View v) {
        try {
            _password = ((EditText)findViewById(R.id.passwordText)).getText().toString();
            File sdcard = Environment.getExternalStorageDirectory();

            File file = new File(sdcard, String.format("%s/%s",AppConstants.APP_DIRECTORY,AppConstants.APP_FILE));

            FileWriter fw = new FileWriter(file);
            fw.write(_password);
            fw.flush();
            fw.close();
            ShowToast(AppConstants.PASSWORD_SAVED_MSG);
        } catch (IOException e) {

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
        final View f1 = inflater.inflate(R.layout.phone_contact_layout,null);
        new AlertDialog.Builder(this)
                .setTitle("Add Quick Call to Home Screen")
                .setMessage("Enter Information for Quick Call Shortcut")
                .setView(f1)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final DatabaseHandler db = DatabaseHandler.getInstance(context);
                        AppDetail app = new AppDetail();
                        app.name = ((EditText)f1.findViewById(R.id.contactPhone)).getText().toString();
                        app.label = AppConstants.TELEPHONE_APP_STARTNAME + " " + ((EditText)f1.findViewById(R.id.contactName)).getText().toString();
                        app.type = AppConstants.TELEPHONE_APP_TYPE;
                        db.addorUpdateUser(app);
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
        Intent intent =  new Intent();
        intent.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        setResult(RESULT_OK, intent);
        finish();
    }
}
