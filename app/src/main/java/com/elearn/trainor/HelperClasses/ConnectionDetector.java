package com.elearn.trainor.HelperClasses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

public class ConnectionDetector {

    private Context _context;

    public ConnectionDetector(Context Context) {
        this._context = Context;
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            //NetworkInfo[] info = connectivity.getAllNetworkInfo();
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            /*if (info != null){
                for (int i = 0; i < info.length; i++){
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }*/
            if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean isConnectedToWifi() {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED && info[i].getType() == ConnectivityManager.TYPE_WIFI) {
                        return true;
                    }
        }
        return false;
    }

 /*   public String getDeviceIMEINo() {
        String IMEINo = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
            IMEINo = telephonyManager.getDeviceId();
        } catch (Exception e) {
            Log.e("Error ", e.getMessage());
        }
        return IMEINo;
    }*/

    public String getAndroid_ID(Context context) {
        String device_unique_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }
}
