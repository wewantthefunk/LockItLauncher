package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AppSettings extends Activity {
    private String _password;
    private String _adminEmail;
    private String _deviceName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_settings);
        _password = getIntent().getStringExtra(AppConstants.PASSWORD_EXTRA);
        _adminEmail = getIntent().getStringExtra(AppConstants.ADMIN_EMAIL);
        _deviceName = getIntent().getStringExtra(AppConstants.DEVICE_NAME);
        ((TextView)findViewById(R.id.appAdminEmail)).setText(_adminEmail);
        ((TextView)findViewById(R.id.appDeviceName)).setText(_deviceName);
    }

    public void saveAppSettings(View v) {
        _adminEmail = ((TextView)findViewById(R.id.appAdminEmail)).getText().toString();
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        AppInfo appInfo = new AppInfo();
        appInfo.adminEmail = ((TextView)findViewById(R.id.appAdminEmail)).getText().toString();
        appInfo.password = _password;
        appInfo.firstTime = AppConstants.NOT_FIRST_TIME;
        appInfo.deviceName = ((TextView)findViewById(R.id.appDeviceName)).getText().toString();
        db.updateAppInfo(appInfo);
        ShowToast("App Settings Saved");

        Intent i = new Intent(AppConstants.APP_INFO_UPDATE_RECEIVER);
        i.putExtra(AppConstants.ADMIN_EMAIL, _adminEmail);
        i.putExtra(AppConstants.PASSWORD_EXTRA, _password);
        i.putExtra(AppConstants.DEVICE_NAME, _deviceName);
        sendBroadcast(i);
    }

    private void ShowToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
