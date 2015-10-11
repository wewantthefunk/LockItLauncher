package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class PostPhoneCallActivity extends Activity {

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };
    Handler timerHandler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_phone_call);

        timerHandler.postDelayed(runnable, 4500);

    }
}
