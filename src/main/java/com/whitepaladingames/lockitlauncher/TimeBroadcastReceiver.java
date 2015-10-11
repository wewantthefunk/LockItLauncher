package com.whitepaladingames.lockitlauncher;

import android.content.Context;
import android.content.Intent;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Chris-laptop on 10/2/2015.
 */
public class TimeBroadcastReceiver extends UIBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        StringBuilder sb = new StringBuilder(24);
        sb.append(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
        sb.append(" ");
        sb.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
        _tv.setText(sb.toString());
    }
}
