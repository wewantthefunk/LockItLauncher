package com.whitepaladingames.lockitlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppInfoUpdateReceiver extends BroadcastReceiver {
    IAppInfoUpdateCaller _caller;
    public AppInfoUpdateReceiver(IAppInfoUpdateCaller caller) {
        super();
        _caller = caller;
    }

    public AppInfoUpdateReceiver() {
        super();
        _caller = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
