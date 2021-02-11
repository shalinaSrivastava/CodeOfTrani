package com.elearn.trainor;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//import com.crashlytics.android.Crashlytics;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;

import java.util.Locale;

//import io.fabric.sdk.android.Fabric;
import me.leolin.shortcutbadger.ShortcutBadger;

import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;

public class ApplicationClass extends Application {
    public static ApplicationClass instance;
    Locale myLocale;
    private SharedPreferenceManager spManager;
    DataBaseHandlerSelect dbSelect;
    IntentFilter internet_intent_filter;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        dbSelect = new DataBaseHandlerSelect(ApplicationClass.this);
        spManager = new SharedPreferenceManager(ApplicationClass.this);
        if (spManager.getSharedPreferenceExistence()) {
            Configuration config = getBaseContext().getResources().getConfiguration();
            myLocale = new Locale(spManager.getLanguage());
            Locale.setDefault(myLocale);
            config.locale = myLocale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        String totalNotification = spManager.getTotalNotificationCount();
        try {
            if (totalNotification != null && !totalNotification.equals("")) {
                ShortcutBadger.applyCount(ApplicationClass.this, Integer.parseInt(totalNotification));
            } else {
                ShortcutBadger.applyCount(ApplicationClass.this, 0);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
    }

    public static ApplicationClass getInstance() {
        return instance;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (spManager.getSharedPreferenceExistence()) {
            Configuration config = getBaseContext().getResources().getConfiguration();
            myLocale = new Locale(spManager.getLanguage());
            Locale.setDefault(myLocale);
            config.locale = myLocale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public void updateCountToServer() {
        ComponentName componentName = new ComponentName(this, JobScheduleService.class);
        final JobInfo jobInfo = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    private BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if ((netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) || (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                updateCountToServer();
            }
        }
    };
}
