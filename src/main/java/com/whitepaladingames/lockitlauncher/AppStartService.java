package com.whitepaladingames.lockitlauncher;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AppStartService extends Service {
    private ArrayList<String> _blockedApps;
    private final String TAG = "LockIt";
    private ArrayList<String> _running;
    private ArrayList<String> _cleared;
    private boolean _pause;
    Context _context;
    private String _adminEmail;
    private String _deviceName;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        class ReceiveMessages extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                _cleared.add(intent.getStringExtra("pName"));
            }
        }
        class ReceiveBlockListUpdate extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean add = intent.getBooleanExtra("add", false);
                String name = intent.getStringExtra("pName");
                if (add) {
                    _blockedApps.add(name);
                } else {
                    _blockedApps.remove(name);
                }
            }
        }
        class ReceivePauseUpdate extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                _pause = intent.getBooleanExtra(AppConstants.PAUSE_APP_CHECK, false);
            }
        }
        ReceiveMessages receiveMessages = new ReceiveMessages();
        this.registerReceiver(receiveMessages, new IntentFilter(AppConstants.APP_RECEIVER));
        ReceiveBlockListUpdate receiveBlockListUpdate = new ReceiveBlockListUpdate();
        this.registerReceiver(receiveBlockListUpdate, new IntentFilter(AppConstants.APP_BLOCK_LIST_UPDATE_RECEIVER));
        ReceivePauseUpdate receivePauseUpdate = new ReceivePauseUpdate();
        this.registerReceiver(receivePauseUpdate, new IntentFilter(AppConstants.APP_PAUSE_UPDATE_RECEIVER));

        _context = this;
        _cleared = new ArrayList<>();
        _running = new ArrayList<>();
        _blockedApps = intent.getStringArrayListExtra(AppConstants.BLOCKED_APPS_LIST);
        _adminEmail = intent.getStringExtra(AppConstants.ADMIN_EMAIL);
        _deviceName = intent.getStringExtra(AppConstants.DEVICE_NAME);
        _pause = false;
        Timer _appStartTimer = new Timer();
        _appStartTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (_pause) return;
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();
                ArrayList<String> current = new ArrayList<>();
                for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcessInfo) {
                    current.add(appProcess.processName);

                    if (!_running.contains(appProcess.processName))
                        _running.add(appProcess.processName);

                    if (_blockedApps.contains(appProcess.processName)) {
                        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            if (!_cleared.contains(appProcess.processName) && !Arrays.asList(AppConstants.OK_APPS).contains(appProcess.processName)) {
                                Intent panel = new Intent(_context, BlockedAppActivity.class);
                                panel.putExtra(AppConstants.TASK_ID, appProcess.pid);
                                panel.putExtra(AppConstants.PACKAGE_NAME, appProcess.processName);
                                panel.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
                                panel.putExtra(AppConstants.APP_NAME, appProcess.processName + " " + appProcess.uid);
                                panel.putExtra(AppConstants.DEVICE_NAME, _deviceName);
                                panel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                try {
                                    _context.startActivity(panel);
                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                            }
                        } else {
                            _cleared.remove(appProcess.processName);
                        }
                    }
                }

                ArrayList<String> names = new ArrayList<>();
                for(String app : _running) {
                    if (!current.contains(app)) {
                        names.add(app);
                    }
                }
                for (String app : names) {
                    _running.remove(app);
                    _cleared.remove(app);
                }
            }
        }, 0, 2500);

        return START_STICKY;
    }
}
