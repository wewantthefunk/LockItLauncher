package com.whitepaladingames.lockitlauncher;

import android.content.Context;
import android.content.Intent;

public interface IAppInfoUpdateCaller {
    void fire(Context context, Intent intent);
}
