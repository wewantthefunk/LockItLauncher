package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LockItAppDrawer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_drawer_launcher);
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

        Collections.sort(apps);
    }

    private ListView list;
    private void loadListView(int position){
        list = (ListView)findViewById(R.id.drawer_apps_list);

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

                return convertView;
            }
        };

        list.setAdapter(adapter);
        list.setSelection(position);
    }

    private void addClickListener(){
        final Context context = this;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Intent i;
                if (apps.get(pos).label.startsWith(AppConstants.TELEPHONE_APP_STARTNAME)) {
                    i = new Intent(Intent.ACTION_CALL, Uri.parse(AppConstants.TELEPHONE_APP_TYPE + ":" + apps.get(pos).name));
                } else if (apps.get(pos).type.equals(AppConstants.ACTIVITY_APP_TYPE)) {
                    Class c = null;
                    switch (apps.get(pos).name) {
                        case AppConstants.APP_DRAWER_NAME:
                            c = LockItAppDrawer.class;
                            break;
                    }
                    i = new Intent(context, c);
                } else {
                    i = manager.getLaunchIntentForPackage(apps.get(pos).name);
                }

                startActivity(i);
            }
        });
    }
}
