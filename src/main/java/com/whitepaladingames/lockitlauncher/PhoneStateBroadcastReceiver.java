package com.whitepaladingames.lockitlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LockItContact";
    Context mContext;
    String incoming_number;
    private int prev_state;

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); //TelephonyManager object
        CustomPhoneStateListener customPhoneListener = new CustomPhoneStateListener(context);
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager

        Bundle bundle = intent.getExtras();
        String phoneNr = bundle.getString("incoming_number");
        mContext = context;
    }

    /* Custom PhoneStateListener */
    public class CustomPhoneStateListener extends PhoneStateListener {

        private boolean _called;
        private Context _context;

        public CustomPhoneStateListener(Context context) {
            _context = context;
            _called = false;
        }
        private static final String TAG = "CustomPhoneStateListener";

        @Override
        public void onCallStateChanged(int state, String incomingNumber){

            if( incomingNumber != null && incomingNumber.length() > 0 )
                incoming_number = incomingNumber;

            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    prev_state=state;
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    prev_state=state;
                    break;

                case TelephonyManager.CALL_STATE_IDLE:

                    if((prev_state == TelephonyManager.CALL_STATE_OFFHOOK)){
                        prev_state=state;
                        //Answered Call which is ended
                        if (!_called) {
                            _called = true;
                            Bundle bundle = new Bundle();

                            Intent panel = new Intent(_context, PostPhoneCallActivity.class);
                            bundle.putInt("phoneOn", 3);
                            panel.replaceExtras(bundle);
                            panel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY);
                            try {
                                _context.startActivity(panel);
                            } catch (Exception e) {
                                Log.d("LIC", e.toString());
                            }
                        }
                    }
                    if((prev_state == TelephonyManager.CALL_STATE_RINGING)){
                        prev_state=state;
                        //Rejected or Missed call
                    }



                    break;
            }


        }
    }
    /*@Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, PhoneStateListenerService.class));
    }*/
}
