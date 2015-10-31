package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class TimerUpActivity extends Activity {
    private String _password;
    private String _adminEmail;
    private String _deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_up);
        _password = getIntent().getStringExtra(AppConstants.PASSWORD_EXTRA);
        _adminEmail = getIntent().getStringExtra(AppConstants.ADMIN_EMAIL);
        _deviceName = getIntent().getStringExtra(AppConstants.DEVICE_NAME);
    }

    public void timeUpOK(View v) {
        String p = ((EditText)findViewById(R.id.timeUpPassword)).getText().toString();
        if (p.equals(_password)) {
            Intent panel = new Intent(this, SettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(AppConstants.PASSWORD_EXTRA, _password);
            bundle.putString(AppConstants.ADMIN_EMAIL, _adminEmail);
            bundle.putString(AppConstants.DEVICE_NAME, _deviceName);
            panel.replaceExtras(bundle);
            try {
                this.startActivity(panel);
            } catch (Exception e) {
                Log.d("LIC", e.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}
