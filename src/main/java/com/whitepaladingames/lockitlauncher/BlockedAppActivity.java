package com.whitepaladingames.lockitlauncher;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class BlockedAppActivity extends Activity {
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };
    private String name;
    private int pid;
    private String label;
    private String _adminEmail;
    private String _appName;
    private String _deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blocked_app);

        pid = getIntent().getIntExtra(AppConstants.TASK_ID, 0);
        name = getIntent().getStringExtra(AppConstants.PACKAGE_NAME);
        label = getIntent().getStringExtra(AppConstants.APP_NAME);
        _adminEmail = getIntent().getStringExtra(AppConstants.ADMIN_EMAIL);
        _appName = getIntent().getStringExtra(AppConstants.APP_NAME);
        _deviceName = getIntent().getStringExtra(AppConstants.DEVICE_NAME);
        if (_appName.equals(AppConstants.EMPTY_STRING)) {
            cancelButton(null);
        }
        else {
            ((TextView) findViewById(R.id.adminEmail)).setText(_adminEmail);
            ((TextView) findViewById(R.id.appName)).setText(_appName);

            if (_adminEmail.equals(AppConstants.EMPTY_STRING)) {
                (findViewById(R.id.adminLayout)).setVisibility(View.GONE);
            }
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(name);
        }
        Intent i = new Intent(AppConstants.BLOCKED_APP_SHOWN_RECEIVER);
        sendBroadcast(i);
    }

    public void cancelButton(View v) {
        finish();
    }

    public void sendRequest(View v) {
        final Context context = this;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    String possibleEmail = "test@aol.com";
                    Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
                    Account[] accounts = AccountManager.get(context).getAccounts();
                    for (Account account : accounts) {
                        if (emailPattern.matcher(account.name).matches()) {
                            possibleEmail = account.name;
                        }
                    }
                    GMailSender sender = new GMailSender("lockitlauncher@yahoo.com", "Gr@dl3L0ck1t");
                    sender.sendMail("LockIt Launcher App approval Request.", "There is a request for an app approval:\n\n"+_appName+"\n\nFor Device: " + _deviceName, possibleEmail, _adminEmail);
                    ShowToast("App Approval Request Sent.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void ShowToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
