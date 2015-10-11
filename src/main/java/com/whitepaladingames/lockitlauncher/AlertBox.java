package com.whitepaladingames.lockitlauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

public class AlertBox {
    public static void ShowAlert(Context context, String title, String message, final IAlertBoxCaller caller) {
        ShowAlert(context, title, message, caller, "");
    }

    public static void ShowAlert(Context context, String title, String message, final IAlertBoxCaller caller, final String extra) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.AlertBoxCallback(dialog, which, extra);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void ShowOKCancel(Context context, String title, String message, final IAlertBoxCaller caller) {
        ShowOKCancel(context, title, message, caller, "");
    }

    public static void ShowOKCancel(Context context, String title, String message, final IAlertBoxCaller caller, final String extra) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.OKCancelCallback(dialog, which, extra);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.OKCancelCallback(dialog, which, extra);
                    }})
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void ShowOKCancel(Context context, String title, String message, final IAlertBoxCaller caller, final Object extra) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.OKCancelCallback(dialog, which, extra);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.OKCancelCallback(dialog, which, extra);
                    }})
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void Show3Button(Context context, String title, String message, final IAlertBoxCaller caller, String positiveText, String negativeText, String neutralText, final String extra) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.ThreeButtonCallback(dialog, which, extra);
                    }
                })
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.ThreeButtonCallback(dialog, which, extra);
                    }
                })
                .setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.ThreeButtonCallback(dialog, which, extra);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void ShowMessageBox(Context context, String title, String message, final IAlertBoxCaller caller) {
        ShowMessageBox(context, title, message, caller, "");
    }

    public static void ShowMessageBox(Context context, String title, String message, final IAlertBoxCaller caller, final String extra) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.MessageBoxCallback(dialog, which, extra);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void ShowTextEntry(Context context, String title, String message, String hint, boolean isPassword, final IAlertBoxCaller caller, final String extra) {
        final EditText txtUrl = new EditText(context);

        if (isPassword) txtUrl.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        else txtUrl.setInputType(InputType.TYPE_CLASS_TEXT);

// Set the default text to a link of the Queen
        txtUrl.setHint(hint);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(txtUrl)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.TextEntryBoxCallback(dialog, which, txtUrl.getText().toString(), extra);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.TextEntryBoxCallback(dialog, which, "", extra);
                    }
                })
                .show();
    }

    public static void ShowTextEntry3Button(Context context, String title, String message, String hint, String neutralButton, String positiveButton, boolean isPassword, final IAlertBoxCaller caller, final String extra) {
        final EditText txtUrl = new EditText(context);

        if (isPassword) {
            txtUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        else {
            txtUrl.setInputType(InputType.TYPE_CLASS_TEXT);
        }

// Set the default text to a link of the Queen
        txtUrl.setHint(hint);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(txtUrl)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.TextEntryBoxCallback(dialog, which, txtUrl.getText().toString(), extra);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.TextEntryBoxCallback(dialog, which, "", extra);
                    }
                })
                .setNeutralButton(neutralButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        caller.TextEntryBoxCallback(dialog, which, txtUrl.getText().toString(), extra);
                    }
                })
                .show();
    }
}
