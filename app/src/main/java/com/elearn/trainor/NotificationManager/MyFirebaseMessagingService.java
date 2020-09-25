package com.elearn.trainor.NotificationManager;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.elearn.trainor.DBHandler.*;

import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomeNotification;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
//import static com.google.android.gms.internal.zzahn.runOnUiThread;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> params = null;
        connectionDetector = new ConnectionDetector(this);
        String safetyCardCounts = "", courseCounts = "", newdiplomaCounts = "", documentCounts = "", body, category, notificationid;
        spManager = new SharedPreferenceManager(this);
        params = remoteMessage.getData();
        if (params != null && !params.equals("")) {
            try {
                if (!spManager.getUserID().equals("")) {
                    JSONObject object = new JSONObject(params);
                    category = object.getString("type");
                    body = object.getString("body");
                    sendNotification(object.getString("body"), object.getString("type"));
                    Integer total_count_int = object.getInt("badge");
                    setTotalNotification(Integer.toString(total_count_int));
                    safetyCardCounts = Integer.toString(object.getInt("safetyCardCount"));
                    courseCounts = Integer.toString(object.getInt("courseCount"));
                    newdiplomaCounts = Integer.toString(object.getInt("newDiplomaCount"));
                    documentCounts = Integer.toString(object.getInt("documentCount"));
                    notificationid = Integer.toString(object.getInt("notificationid"));
                    addNotification(courseCounts, newdiplomaCounts, safetyCardCounts, documentCounts, Integer.toString(total_count_int), body, category, notificationid);
                }
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
        }
    }


    private void sendNotification(String messageBody, String categoryTitle) {
        Context c = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // The id of the channel.
        final String CHANNEL_ID = "Trainor";
        // The user-visible name of the channel.
        final String CHANNEL_NAME = "Default";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        Intent intent = new Intent(this, HomePage.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (categoryTitle.equals("E-Learning Course")) {
            categoryTitle = getResources().getString(R.string.elearn_course_heading);
        } else if (categoryTitle.equals("ClassRoomCourse")) {
            categoryTitle = getResources().getString(R.string.classroom_course_heading);
        }else if (categoryTitle.equals("Safety card")) {
            categoryTitle = getResources().getString(R.string.notification_safety_card);
        }
        else if (categoryTitle.equals("New Diploma")) {
            categoryTitle = getResources().getString(R.string.notification_new_diploma);
        } else if (categoryTitle.equals("Documents")) {
            categoryTitle = getResources().getString(R.string.new_documents);
        }

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(c, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(ContextCompat.getColor(c, R.color.notification_color))
                .setContentTitle(categoryTitle)
                .setContentText(messageBody)
                .setWhen(System.currentTimeMillis())
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLights(ContextCompat.getColor(c, R.color.notification_color), 5000, 5000)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify("", 0, notificationBuilder.build());
    }


    public void addNotification(String courseCount, String newDiplomaCount, String safetyCardCount,
                                String documentCount, final String totalNotifications, String body, String category, String notificationid) {
        dbUpdate = new DataBaseHandlerUpdate(this);
        dbSelect = new DataBaseHandlerSelect(this);
        dbInsert = new DataBaseHandlerInsert(this);
        int countOfCourse = Integer.parseInt(courseCount);
        int countOfDiploma = Integer.parseInt(newDiplomaCount);
        int countOfSafetyCard = Integer.parseInt(safetyCardCount);
        int countOfDocument = Integer.parseInt(documentCount);

        if (category.equals("ClassRoomCourse") || category.equals("E-Learning Course")) {
            if (dbSelect.isNotificationIDExists(spManager.getUserID(), notificationid) == false) {
                dbInsert.addDataIntoNotificationCountTable("Course", spManager.getUserID(), body, category, notificationid);
                dbUpdate.updateNotificationCount("Course", countOfCourse + "", spManager.getUserID());
            }
        }

        if (category.equals("Safety card")) {
            if (dbSelect.isNotificationIDExists(spManager.getUserID(), notificationid) == false) {
                dbInsert.addDataIntoNotificationCountTable("SafetyCard", spManager.getUserID(), body, category, notificationid);
                dbUpdate.updateNotificationCount("SafetyCard", countOfSafetyCard + "", spManager.getUserID());
            }
        }
        if (category.equals("New Diploma")) {
            if (dbSelect.isNotificationIDExists(spManager.getUserID(), notificationid) == false) {
                dbInsert.addDataIntoNotificationCountTable("New Diploma", spManager.getUserID(), body, category, notificationid);
                dbUpdate.updateNotificationCount("New Diploma", countOfDiploma + "", spManager.getUserID());
            }
        }
        if (category.equals("Documents")) {
            if (dbSelect.isNotificationIDExists(spManager.getUserID(), notificationid) == false) {
                dbInsert.addDataIntoNotificationCountTable("New Document", spManager.getUserID(), body, category, notificationid);
                dbUpdate.updateNotificationCount("New Document", countOfDocument + "", spManager.getUserID());
            }
        }
        final HomePage homePage = HomePage.getInstance();


        if (!isAppIsInBackground(this)) {
            try {
                if (homePage != null) {
                    homePage.callRunOnUiThreadInServices(Integer.parseInt(totalNotifications));
                }
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
        }

        final HomeNotification homeNotification = HomeNotification.getInstance();
        if (homeNotification != null) {
            homeNotification.updateList();
        }
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    public void setTotalNotification(String totalNotification) {
        try {
            spManager = new SharedPreferenceManager(this);
            spManager.removeSharedPreferenceByName("TotalNotification");
            SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
            editor.putString("Total", totalNotification);
            editor.commit();
            if (totalNotification != null && !totalNotification.equals("")) {
                ShortcutBadger.applyCount(this, Integer.parseInt(totalNotification));
            } else {
                ShortcutBadger.applyCount(this, 0);
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }
}