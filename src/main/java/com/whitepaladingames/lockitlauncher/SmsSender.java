package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsSender {
    private Context _context;
    private String _type;

    public SmsSender(Context context, String type) {
        _context = context;
        _type = type;
    }

    //---sends an SMS message to another device---
    public void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(_context, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(_context, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        _context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String msg = "";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        msg = _type + " Sent";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        msg = "Generic failure";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        msg = "No service";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        msg = "Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        msg = "Radio off";
                        break;
                }
                Toast.makeText(_context, msg,
                        Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        _context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String msg = "";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        msg = _type + " delivered";
                        break;
                    case Activity.RESULT_CANCELED:
                        msg = _type + " not delivered";
                        break;
                }
                Toast.makeText(_context, msg,
                        Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        try {
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        } catch (Exception e) {
            String msg = e.toString();
            Toast.makeText(_context, msg,
                    Toast.LENGTH_SHORT).show();
        }
    }
}

