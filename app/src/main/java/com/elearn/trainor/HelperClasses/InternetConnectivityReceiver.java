package com.elearn.trainor.HelperClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.elearn.trainor.ConcoPhilips.ConcoPhilips;
import com.elearn.trainor.HomePage;

public class InternetConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && (netInfo.getType() == ConnectivityManager.TYPE_WIFI || netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
            if (context instanceof ConcoPhilips) {
                ConcoPhilips concoPhilips = (ConcoPhilips) context;
                concoPhilips.callAPI();
            }
            if (context instanceof CopService) {
                CopService copService = (CopService) context;
                copService.callAPI();
            }

            if(context instanceof HomePage){
                HomePage concoPhilips = (HomePage) context;
                concoPhilips.callSaveCustomerIdToAccessCOP();
            }
        }
    }
}
