package com.whitepaladingames.lockitlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Created by Chris-laptop on 10/2/2015.
 */
public class UIBroadcastReceiver extends BroadcastReceiver {
    protected TextView _tv;
    public void setTextView(TextView tv) {
        _tv = tv;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
