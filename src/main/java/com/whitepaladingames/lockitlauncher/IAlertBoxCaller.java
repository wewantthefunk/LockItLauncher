package com.whitepaladingames.lockitlauncher;

import android.content.DialogInterface;

/**
 * Created by Chris-laptop on 1/6/2015.
 */
public interface IAlertBoxCaller {
    void AlertBoxCallback(DialogInterface dialog, int which, String extra);
    void MessageBoxCallback(DialogInterface dialog, int which, String extra);
    void OKCancelCallback(DialogInterface dialog, int which, String extra);
    void OKCancelCallback(DialogInterface dialog, int which, Object extra);
    void ThreeButtonCallback(DialogInterface dialog, int which, String extra);
    void TextEntryBoxCallback(DialogInterface dialog, int which, String text, String extra);
}
