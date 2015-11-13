package com.whitepaladingames.lockitlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
    private ArrayList<LockItInAppPurchase> _availablePurchases;

    public TimeBroadcastReceiver(Context context, String password, String adminEmail, String deviceName, boolean useTimeout, ArrayList<LockItInAppPurchase> availablePurchases) {
        super();
        _availablePurchases = availablePurchases;
        _context = context;
        _password = password;
        _adminEmail = adminEmail;
        _deviceName = deviceName;
        _useTimeout = useTimeout;
        AppInfoUpdateReceiver _appInfoReceiver = new AppInfoUpdateReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                _admin = intent.getBooleanExtra(AppConstants.ADMIN_MODE, false);

                if (!_admin) _caller.fire(_context, null);
                else _shown = false;
            }
        };
        _context.registerReceiver(_appInfoReceiver, new IntentFilter(AppConstants.APP_PAUSE_UPDATE_RECEIVER));
        AppInfoUpdateReceiver _appTimeoutReceiver = new AppInfoUpdateReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                _useTimeout = intent.getBooleanExtra(AppConstants.IN_APP_PURCHASE_GOLD_LEVEL, false);
            }
        };
        _context.registerReceiver(_appTimeoutReceiver, new IntentFilter(AppConstants.PURCHASED_GOLD_LEVEL));
    }

    public void setTimerInfo(AppTimer appTimer) {
        _appTimer = appTimer;
    }

    public void fire(Context context, Intent intent) {
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.saveTimer(_appTimer);
        if (_useTimeout && _appTimer._time >= _appTimer._totalTime && !_shown && !_admin) {
            _shown = true;

            Intent panel = new Intent(context, TimerUpActivity.class);
            panel.putExtra(AppConstants.PASSWORD_EXTRA, _password);
            panel.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
            panel.putExtra(AppConstants.DEVICE_NAME, _deviceName);
            panel.putExtra(AppConstants.IN_APP_PURCHASE_DATA, new InAppPurchaseDataWrapper(_availablePurchases));
            panel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                context.startActivity(panel);
            } catch (Exception e) {
                Log.d("LIC", e.toString());
            }
        }
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String sb = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        sb += " ";
        sb += DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        _tv.setText(sb);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!_admin) _appTimer._time += 1;
       fire(context, intent);
    }
}
