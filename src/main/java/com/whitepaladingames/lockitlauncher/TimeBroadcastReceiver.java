package com.whitepaladingames.lockitlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeBroadcastReceiver extends UIBroadcastReceiver implements IAppInfoUpdateCaller {
    private AppTimer _appTimer;
    private boolean _shown = false;
    private boolean _admin = false;
    private Context _context;
    private String _password;
    private String _adminEmail;
    private String _deviceName;
    private boolean _useTimeout;
    private boolean _pauseTimeout;
    private ArrayList<LockItInAppPurchase> _availablePurchases;

    private final String TAG = "TIMER";

    public TimeBroadcastReceiver(Context context, String password, String adminEmail, String deviceName, boolean useTimeout) {
        super();
        _availablePurchases = null;
        _context = context;
        _password = password;
        _adminEmail = adminEmail;
        _deviceName = deviceName;
        _useTimeout = useTimeout;
        _pauseTimeout = false;
        AppInfoUpdateReceiver _appInfoReceiver = new AppInfoUpdateReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                _admin = intent.getBooleanExtra(AppConstants.ADMIN_MODE, false);

                Intent i = new Intent();
                i.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "APP_PAUSE_UPDATE");
                if (!_admin) _caller.fire(_context, i);
                else _shown = false;
            }
        };
        _context.registerReceiver(_appInfoReceiver, new IntentFilter(AppConstants.APP_PAUSE_UPDATE_RECEIVER));
        final AppInfoUpdateReceiver _appTimeoutReceiver = new AppInfoUpdateReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                _useTimeout = intent.getBooleanExtra(AppConstants.IN_APP_PURCHASE_GOLD_LEVEL, false);
            }
        };
        _context.registerReceiver(_appTimeoutReceiver, new IntentFilter(AppConstants.PURCHASED_GOLD_LEVEL));
        AppInfoUpdateReceiver _appBlockedReceiver = new AppInfoUpdateReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                _shown = false;
            }
        };
        _context.registerReceiver(_appBlockedReceiver, new IntentFilter(AppConstants.BLOCKED_APP_SHOWN_RECEIVER));
        BroadcastReceiver _inappPurchaseDoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                InAppPurchaseDataWrapper dw = (InAppPurchaseDataWrapper)intent.getSerializableExtra(AppConstants.IN_APP_PURCHASE_DATA);
                _availablePurchases = dw.getLockItInAppPurchases();
                _useTimeout = intent.getBooleanExtra(AppConstants.USE_SCREEN_TIMEOUT, false);
                intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "IN_APP_PURCHASES_DONE");
                fire(context, intent);
            }
        };
        _context.registerReceiver(_inappPurchaseDoneReceiver, new IntentFilter(AppConstants.IN_APP_PURCHASES_DONE_RECEIVER));
        BroadcastReceiver _timerToggleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _useTimeout = intent.getBooleanExtra(AppConstants.USE_SCREEN_TIMEOUT, false);
                intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "TIMER_TOGGLE");
                fire(context, intent);
            }
        };
        _context.registerReceiver(_timerToggleReceiver, new IntentFilter(AppConstants.TIMER_TOGGLE_RECEIVER));
        BroadcastReceiver _timerTimeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (_appTimer._time >= _appTimer._totalTime)
                    _appTimer._time = _appTimer._totalTime - intent.getIntExtra(AppConstants.SCREEN_TIMEOUT_TIME, 0);
                else {
                    _appTimer._time -= intent.getIntExtra(AppConstants.SCREEN_TIMEOUT_TIME, 0);
                }
                _shown = false;
                intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "TIMER_TIME_UPDATE give more time");
                fire(context, intent);
            }
        };
        _context.registerReceiver(_timerTimeUpdateReceiver, new IntentFilter(AppConstants.TIMER_TIME_UPDATE_RECEIVER));
        BroadcastReceiver _timerCurrentTimeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _appTimer._time = intent.getIntExtra(AppConstants.SCREEN_TIMEOUT_TIME, 0);
                _shown = false;
                intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "TIMER_TIME_UPDATE reset time");
                fire(context, intent);
            }
        };
        _context.registerReceiver(_timerCurrentTimeReceiver, new IntentFilter(AppConstants.TIMER_CURRENT_TIME_UPDATE_RECEIVER));
        BroadcastReceiver _timeOutActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _shown = true;
                intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "TIME_OUT_ACTIVITY_READY");
                fire(context, intent);
            }
        };
        _context.registerReceiver(_timeOutActivityReceiver, new IntentFilter(AppConstants.TIME_OUT_ACTIVITY_READY_RECEIVER));
    }

    public void setTimerInfo(AppTimer appTimer) {
        _appTimer = appTimer;
    }

    public void set_shown() {
        _shown = true;
        _pauseTimeout = true;
    }

    public void pauseTimer() {
        _pauseTimeout = true;
    }

    public void resumeTimer() {
        _pauseTimeout = false;
    }

    public void notShown(Context context, Intent intent) {
        _shown = false;
        _admin = intent.getBooleanExtra(AppConstants.ADMIN_MODE, false);
        _pauseTimeout = intent.getBooleanExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, false);
        intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "notShown");
        fire(context, intent);
    }
    public void fire(Context context, Intent intent) {
        if (_appTimer == null) return;
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.saveTimer(_appTimer);

        if (_useTimeout && !_pauseTimeout && _appTimer._time >= _appTimer._totalTime && !_shown && !_admin && _availablePurchases != null) {
            _shown = true;

            Intent panel = new Intent(context, TimerUpActivity.class);
            panel.putExtra(AppConstants.PASSWORD_EXTRA, _password);
            panel.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
            panel.putExtra(AppConstants.DEVICE_NAME, _deviceName);
            panel.putExtra(AppConstants.USE_SCREEN_TIMEOUT, _useTimeout);
            panel.putExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, _pauseTimeout);
            panel.putExtra(AppConstants.IN_APP_PURCHASE_DATA, new InAppPurchaseDataWrapper(_availablePurchases));
            panel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                context.startActivity(panel);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
        try {
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            String sb = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
            sb += " ";
            sb += DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
            _tv.setText(sb);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        Intent i = new Intent(AppConstants.TIMER_UPDATE_RECEIVER);
        i.putExtra(AppConstants.TIME, Integer.toString(_appTimer._time));
        i.putExtra(AppConstants.TOTAL_TIME, Integer.toString(_appTimer._totalTime));
        i.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, intent.getStringExtra(AppConstants.TIMER_FIRE_CAME_FROM));
        _context.sendBroadcast(i);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (!_admin) {
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                String cdate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
                if (!_appTimer._date.equals(cdate)) {
                    _appTimer._time = 0;
                    _appTimer._date = cdate;
                }
                if (!_pauseTimeout) _appTimer._time += 1;
            }
            intent.putExtra(AppConstants.TIMER_FIRE_CAME_FROM, "onReceive");
            fire(context, intent);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}
