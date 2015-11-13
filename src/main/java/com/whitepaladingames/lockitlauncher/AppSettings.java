package com.whitepaladingames.lockitlauncher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AppSettings extends Activity implements IAlertBoxCaller {
    private String _password;
    private String _adminEmail;
    private String _deviceName;
    private ArrayList<LockItInAppPurchase> _availablePurchases;
    private ListView list;

    private IInAppBillingService _service;
    private ServiceConnection _serviceConn;
    Thread _playServicesThread;

    private static final String TAG = "LockIt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_settings);
        _password = getIntent().getStringExtra(AppConstants.PASSWORD_EXTRA);
        _adminEmail = getIntent().getStringExtra(AppConstants.ADMIN_EMAIL);
        _deviceName = getIntent().getStringExtra(AppConstants.DEVICE_NAME);
        InAppPurchaseDataWrapper dw = (InAppPurchaseDataWrapper)getIntent().getSerializableExtra(AppConstants.IN_APP_PURCHASE_DATA);
        _availablePurchases = dw.getLockItInAppPurchases();
        ((TextView)findViewById(R.id.appAdminEmail)).setText(_adminEmail);
        ((TextView)findViewById(R.id.appDeviceName)).setText(_deviceName);
        LoadInAppPurchases();
        addClickListener();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                _serviceConn = new ServiceConnection() {
                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        _service = null;
                    }

                    @Override
                    public void onServiceConnected(ComponentName name,
                                                   IBinder service) {
                        _service = IInAppBillingService.Stub.asInterface(service);
                    }
                };

                Intent serviceIntent = new Intent(AppConstants.GOOGLE_PLAY_SERVICES_INTENT_ACTION);
                serviceIntent.setPackage(AppConstants.GOOGLE_PLAY_SERVICES_PACKAGE);
                bindService(serviceIntent, _serviceConn, Context.BIND_AUTO_CREATE);
            }
        };

        _playServicesThread = new Thread(runnable);
        _playServicesThread.start();
    }

    private void LoadInAppPurchases() {
        list = (ListView) findViewById(R.id.inapppurchase_list);
        final Context context = this;
        ArrayAdapter<LockItInAppPurchase> adapter = new ArrayAdapter<LockItInAppPurchase>(this, R.layout.list_item, _availablePurchases) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                convertView.setId(position);

                ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                if (_availablePurchases.get(position).purchased != AppConstants.INT_FALSE) {
                    appIcon.setImageDrawable(getDrawableImaage(AppConstants.CHECK_MARK_ICON, context));
                } else {
                    appIcon.setImageDrawable(getDrawableImaage(AppConstants.MONEY_ICON, context));
                }
                RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(AppConstants.IN_APP_PURCHASE_ICON_SIZE, AppConstants.IN_APP_PURCHASE_ICON_SIZE);
                appIcon.setLayoutParams(parms);

                TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
                appLabel.setText(_availablePurchases.get(position).name);
                appLabel.setTypeface(null, Typeface.BOLD);
                appLabel.setTextColor(Color.parseColor(AppConstants.WHITE_TEXT_COLOR));

                TextView appName = (TextView) convertView.findViewById(R.id.item_app_name);
                appName.setText(_availablePurchases.get(position).description);

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    private void addClickListener() {
        final IAlertBoxCaller caller = this;
        final Context context = this;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                if (_availablePurchases.get(pos).purchased == 0) {
                    AlertBox.ShowOKCancel(context, "Purchase this upgrade?", "Do you wish to purchase " + _availablePurchases.get(pos).name + "?", caller, _availablePurchases.get(pos).productId);
                } else {
                }
            }
        });
    }

    private Drawable getDrawableImaage(String name, Context context) {
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(name, AppConstants.DRAWABLE_RESOURCE, context.getPackageName());
        return resources.getDrawable(resourceId);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstants.GOOGLE_PLAY_ACTIVITY_CODE) {
            int responseCode = data.getIntExtra(AppConstants.GOOGLE_PLAY_BILLING_RESPONSE_CODE, AppConstants.GOOGLE_PLAY_BILLING_RESPONSE_OK);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    AlertBox.ShowAlert(this, "Success!","You have bought the " + sku + ". Excellent choice, adventurer!",this);
                }
                catch (JSONException e) {
                    Log.d(TAG, "Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }

            Intent i = new Intent(AppConstants.PURCHASED_GOLD_LEVEL);
            i.putExtra(AppConstants.IN_APP_PURCHASE_GOLD_LEVEL, true);
            sendBroadcast(i);
        }
    }

    @Override
    public void AlertBoxCallback(DialogInterface dialog, int which, String extra) {

    }

    @Override
    public void MessageBoxCallback(DialogInterface dialog, int which, String extra) {

    }

    @Override
    public void OKCancelCallback(DialogInterface dialog, int which, String extra) {
        if (which == AppConstants.ALERT_BOX_OK) {
            try {
                Bundle buyIntentBundle = _service.getBuyIntent(AppConstants.GOOGLE_PLAY_API_VERSION, getPackageName(), extra, AppConstants.GOOGLE_PLAY_IN_APP_TYPE, extra);

                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                startIntentSenderForResult(pendingIntent.getIntentSender(),AppConstants.GOOGLE_PLAY_ACTIVITY_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0),Integer.valueOf(0));
            } catch (RemoteException os) {
                AlertBox.ShowAlert(this, "Unable to reach the Google Play Store", os.getMessage(), this);
            } catch (IntentSender.SendIntentException sie) {
                AlertBox.ShowAlert(this, "Unable to reach the Google Play Store", sie.getMessage(), this);
            }
        }
    }

    @Override
    public void OKCancelCallback(DialogInterface dialog, int which, Object extra) {
    }

    @Override
    public void ThreeButtonCallback(DialogInterface dialog, int which, String extra) {

    }

    @Override
    public void TextEntryBoxCallback(DialogInterface dialog, int which, String text, String extra) {

    }
}
