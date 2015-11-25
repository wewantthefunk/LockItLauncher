package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class TimerUpActivity extends Activity {
    private String _password;
    private String _adminEmail;
    private String _deviceName;
    private ArrayList<LockItInAppPurchase> _availablePurchases;
    private boolean _useTimeout;

    private final int ADDED_TIME = 10;

    private BroadcastReceiver _appInfoReceiver;

    private BroadcastReceiver _dailyTimerFireReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (_useTimeout) {
                String time = intent.getStringExtra(AppConstants.TIME);
                String total = intent.getStringExtra(AppConstants.TOTAL_TIME);
                String cameFrom = intent.getStringExtra(AppConstants.TIMER_FIRE_CAME_FROM);
                Log.d("TIMER_UP", cameFrom);
                String s = AppConstants.EMPTY_STRING;
                if (!total.equals("1")) s = "s";
                StringBuilder builder = new StringBuilder();
                builder.append(time);
                builder.append(" of ");
                builder.append(total);
                builder.append(" minute");
                builder.append(s);
                builder.append(" today ");
                ((TextView) findViewById(R.id.totalTimeMessage)).setText(builder.toString());
            } else {
                ((TextView)findViewById(R.id.totalTimeMessage)).setText(AppConstants.EMPTY_STRING);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_up);
        String s;
        if (ADDED_TIME > 1) s = "s";
        ((TextView)findViewById(R.id.addMinutes)).setText("Add " + Integer.toString(ADDED_TIME) + " Minute" + s);
        _password = getIntent().getStringExtra(AppConstants.PASSWORD_EXTRA);
        _adminEmail = getIntent().getStringExtra(AppConstants.ADMIN_EMAIL);
        _deviceName = getIntent().getStringExtra(AppConstants.DEVICE_NAME);
        _useTimeout = getIntent().getBooleanExtra(AppConstants.USE_SCREEN_TIMEOUT, false);
        boolean _pauseTimeout = getIntent().getBooleanExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, false);

        if (_pauseTimeout) {
            setTimerPausedMessage(AppConstants.TIMER_PAUSED_MESSAGE);
        } else {
            setTimerPausedMessage(AppConstants.EMPTY_STRING);
        }

        InAppPurchaseDataWrapper dw = (InAppPurchaseDataWrapper)getIntent().getSerializableExtra(AppConstants.IN_APP_PURCHASE_DATA);
        _availablePurchases = dw.getLockItInAppPurchases();
        _appInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                InAppPurchaseDataWrapper dw = (InAppPurchaseDataWrapper)intent.getSerializableExtra(AppConstants.IN_APP_PURCHASE_DATA);
                _availablePurchases = dw.getLockItInAppPurchases();
                _useTimeout = intent.getBooleanExtra(AppConstants.USE_SCREEN_TIMEOUT, false);
            }
        };
        this.registerReceiver(_appInfoReceiver, new IntentFilter(AppConstants.IN_APP_PURCHASES_DONE_RECEIVER));

        BroadcastReceiver _timerToggleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _useTimeout = intent.getBooleanExtra(AppConstants.USE_SCREEN_TIMEOUT, false);
            }
        };
        this.registerReceiver(_timerToggleReceiver, new IntentFilter(AppConstants.TIMER_TOGGLE_RECEIVER));
        this.registerReceiver(this._dailyTimerFireReceiver, new IntentFilter(AppConstants.TIMER_UPDATE_RECEIVER));
        Intent i = new Intent(AppConstants.TIME_OUT_ACTIVITY_READY_RECEIVER);
        sendBroadcast(i);
    }

    public void timeUpOK(View v) {
        String p = ((EditText)findViewById(R.id.timeUpPassword)).getText().toString();
        if (p.equals(_password)) {
            AppConstants.HideKeyboard(this, this);
            findViewById(R.id.timeUpLayout2).setVisibility(View.VISIBLE);
        }
    }

    public void timeUpCancel(View v) {
        findViewById(R.id.timeUpLayout2).setVisibility(View.INVISIBLE);
        finish();
    }

    public void goToSettings(View v) {
        Intent panel = new Intent(this, SettingsActivity.class);
        panel.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        panel.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
        panel.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        panel.putExtra(AppConstants.USE_SCREEN_TIMEOUT, _useTimeout);
        panel.putExtra(AppConstants.IN_APP_PURCHASE_DATA, new InAppPurchaseDataWrapper(_availablePurchases));
        try {
            this.startActivity(panel);
        } catch (Exception e) {
            Log.d("LIC", e.toString());
        }
    }

    public void pauseTimer(View v) {
        Intent i = new Intent(AppConstants.TIMER_TOGGLE_RECEIVER);
        i.putExtra(AppConstants.USE_SCREEN_TIMEOUT, true);
        i.putExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, true);
        sendBroadcast(i);
        setTimerPausedMessage(AppConstants.TIMER_PAUSED_MESSAGE);
        finish();
    }

    public void unpauseTimer(View v) {
        Intent i = new Intent(AppConstants.TIMER_TOGGLE_RECEIVER);
        i.putExtra(AppConstants.USE_SCREEN_TIMEOUT, true);
        i.putExtra(AppConstants.PAUSE_SCREEN_TIMEOUT, false);
        sendBroadcast(i);
        setTimerPausedMessage(AppConstants.EMPTY_STRING);
        finish();
    }

    private void setTimerPausedMessage(String message) {
        ((TextView)findViewById(R.id.timerPausedMessage)).setText(message);
    }

    public void resetTimer(View v) {
        Intent i = new Intent(AppConstants.TIMER_CURRENT_TIME_UPDATE_RECEIVER);
        i.putExtra(AppConstants.SCREEN_TIMEOUT_TIME, 0);
        sendBroadcast(i);
        finish();
    }

    public void addTime(View v) {
        Intent i = new Intent(AppConstants.TIMER_TIME_UPDATE_RECEIVER);
        i.putExtra(AppConstants.SCREEN_TIMEOUT_TIME, ADDED_TIME);
        sendBroadcast(i);
        finish();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(_appInfoReceiver);
    }
}
