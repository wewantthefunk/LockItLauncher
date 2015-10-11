package com.whitepaladingames.lockitlauncher;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Created by Chris-laptop on 10/2/2015.
 */
public class BatteryBroadcastReceiver extends UIBroadcastReceiver {
    private TextView _chargingTv;

    public void setChargingTextView(TextView tv) {
        _chargingTv = tv;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra("level", 0);
        _tv.setText(Integer.toString(level)+"%");
        int plugged = intent.getIntExtra("plugged", 0);
        if (plugged == 2 || plugged == 1)
            _chargingTv.setText(AppConstants.CHARGING_CHAR);
        else _chargingTv.setText(AppConstants.NOT_CHARGING_CHAR);
    }
}
