package com.elearn.trainor.SettingModule;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.HelperClasses.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationSettings extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header;
    String notifications;
    TextView txtNewCourse, txtNewDiploma, txtCourseExpired, txtNewSafetyCard, txtClassroomCourse, txtDocuments;//txtSafetyCardExpired
    SwitchCompat switch_new_course, switch_diploma, switch_courses_expired, switch_safety_cards,
            switch_classRoom_course, switch_documents;//switch_expired_safety_cards
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    List<SwitchCompat> switchCompatList;
    List<TextView> textViewCompatList;
    public boolean isActivityLive = false;
    ProgressDialog pDialog;
    String deviceType;
    FirebaseAnalytics analytics;
    Trace myTrace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_setting);
        isActivityLive = true;
        getConrols();
    }

    @Override
    protected void onStart() {
        isActivityLive = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // analytics.setCurrentScreen(this, "NotificationSettings", this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        isActivityLive = false;
        super.onStop();
    }

    @SuppressLint("MissingPermission")
    public void getConrols() {
        analytics = FirebaseAnalytics.getInstance(this);
        myTrace = FirebasePerformance.getInstance().newTrace("Notification_trace");
        myTrace.start();

        switchCompatList = new ArrayList<>();
        textViewCompatList = new ArrayList<>();
        dbSelect = new DataBaseHandlerSelect(NotificationSettings.this);
        dbInsert = new DataBaseHandlerInsert(NotificationSettings.this);
        dbUpdate = new DataBaseHandlerUpdate(NotificationSettings.this);
        connectionDetector = new ConnectionDetector(NotificationSettings.this);
        spManager = new SharedPreferenceManager(NotificationSettings.this);
        notifications = getResources().getString(R.string.notifications);
        text_header = (TextView) findViewById(R.id.text_header);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        txtNewCourse = (TextView) findViewById(R.id.txtNewCourse);
        txtNewDiploma = (TextView) findViewById(R.id.txtNewDiploma);
        txtCourseExpired = (TextView) findViewById(R.id.txtCourseExpired);
        txtNewSafetyCard = (TextView) findViewById(R.id.txtNewSafetyCard);
        //txtSafetyCardExpired = (TextView) findViewById(R.id.txtSafetyCardExpired);
        txtClassroomCourse = (TextView) findViewById(R.id.txtClassroomCourse);
        txtDocuments = (TextView) findViewById(R.id.txtDocuments);
        switch_new_course = (SwitchCompat) findViewById(R.id.switch_new_course);
        switch_diploma = (SwitchCompat) findViewById(R.id.switch_diploma);
        switch_courses_expired = (SwitchCompat) findViewById(R.id.switch_courses_expired);
        switch_safety_cards = (SwitchCompat) findViewById(R.id.switch_safety_cards);
        //switch_expired_safety_cards = (SwitchCompat) findViewById(R.id.switch_expired_safety_cards);
        switch_classRoom_course = (SwitchCompat) findViewById(R.id.switch_classRoom_course);
        switch_documents = (SwitchCompat) findViewById(R.id.switch_documents);

        text_header.setText(notifications);
        llhome.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        switch_new_course.setOnClickListener(this);
        switch_diploma.setOnClickListener(this);
        switch_courses_expired.setOnClickListener(this);
        switch_safety_cards.setOnClickListener(this);
        //switch_expired_safety_cards.setOnClickListener(this);
        switch_classRoom_course.setOnClickListener(this);
        switch_documents.setOnClickListener(this);
        textViewCompatList.add(txtNewCourse);
        textViewCompatList.add(txtNewDiploma);
        textViewCompatList.add(txtCourseExpired);
        textViewCompatList.add(txtNewSafetyCard);
        //textViewCompatList.add(txtSafetyCardExpired);
        textViewCompatList.add(txtClassroomCourse);
        textViewCompatList.add(txtDocuments);
        switchCompatList.add(switch_new_course);
        switchCompatList.add(switch_diploma);
        switchCompatList.add(switch_courses_expired);
        switchCompatList.add(switch_safety_cards);
        // switchCompatList.add(switch_expired_safety_cards);
        switchCompatList.add(switch_classRoom_course);
        switchCompatList.add(switch_documents);
        setStateOfToggleButtons(switchCompatList, textViewCompatList);
    }

    @Override
    public void onBackPressed() {
        myTrace.stop();
        Intent intentback = new Intent(NotificationSettings.this, Settings.class);
        intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentback);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                myTrace.stop();
                Intent intentback = new Intent(NotificationSettings.this, HomePage.class);
                intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentback);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                break;
            case R.id.switch_new_course:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_new_course.isChecked()) {
                        switchNotification("New Course", "Yes", spManager.getUserID());
                    } else {
                        switchNotification("New Course", "No", spManager.getUserID());
                    }
                } else {
                    switch_new_course.setChecked(!switch_new_course.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;
            case R.id.switch_diploma:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_diploma.isChecked()) {
                        switchNotification("New Diploma", "Yes", spManager.getUserID());
                    } else {
                        switchNotification("New Diploma", "No", spManager.getUserID());
                    }
                } else {
                    switch_diploma.setChecked(!switch_diploma.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;
            case R.id.switch_courses_expired:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_courses_expired.isChecked()) {
                        switchNotification("Course expired", "Yes", spManager.getUserID());
                    } else {
                        switchNotification("Course expired", "No", spManager.getUserID());
                    }
                } else {
                    switch_courses_expired.setChecked(!switch_courses_expired.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;
            case R.id.switch_safety_cards:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_safety_cards.isChecked()) {
                        switchNotification("Safety card", "Yes", spManager.getUserID());
                        //switchNotification("New safety card", "Yes", spManager.getUserID());
                    } else {
                        //switchNotification("New safety card", "No", spManager.getUserID());
                        switchNotification("Safety card", "No", spManager.getUserID());
                    }
                } else {
                    switch_safety_cards.setChecked(!switch_safety_cards.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;
            /*case R.id.switch_expired_safety_cards:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_expired_safety_cards.isChecked()) {
                        switchNotification("Safety card expired", "Yes", spManager.getUserID());
                    } else {
                        switchNotification("Safety card expired", "No", spManager.getUserID());
                    }
                } else {
                    switch_expired_safety_cards.setChecked(!switch_expired_safety_cards.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;*/

            case R.id.switch_classRoom_course:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_classRoom_course.isChecked()) {
                        switchNotification("ClassRoomCourse", "Yes", spManager.getUserID());
                    } else {
                        switchNotification("ClassRoomCourse", "No", spManager.getUserID());
                    }
                } else {
                    switch_classRoom_course.setChecked(!switch_classRoom_course.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;
            case R.id.switch_documents:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    if (switch_documents.isChecked()) {
                        switchNotification("Documents", "Yes", spManager.getUserID());
                    } else {
                        switchNotification("Documents", "No", spManager.getUserID());
                    }
                } else {
                    switch_documents.setChecked(!switch_documents.isChecked());
                    AlertDialogManager.showDialog(NotificationSettings.this, CommonMessages.INTERNET_CONNECTION_TITLE, CommonMessages.INTERNET_CONNECTION_MESSAGE, false, null);
                }
                break;
        }
    }

    public void switchNotification(final String NotificationType, final String NotificationEnable, final String UserID) {
        deviceType = "android";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationMode_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String updateNotificationType = "";
                    if (NotificationType.equals("ClassRoomCourse")) {
                        updateNotificationType = "New ClassRoom Course";
                    } else if (NotificationType.equals("Documents")) {
                        updateNotificationType = "New Document";
                    } else {
                        updateNotificationType = NotificationType;
                    }

                    JSONArray jsonArrays = new JSONArray(response);
                    if (jsonArrays != null && jsonArrays.length() > 0) {
                        JSONObject jsonObject = jsonArrays.getJSONObject(0);
                        if (jsonObject.has("Error")) {
                            if (updateNotificationType.equals("New Course")) {
                                switch_new_course.setChecked(!switch_new_course.isChecked());
                            } else if (updateNotificationType.equals("Course expired")) {
                                switch_courses_expired.setChecked(!switch_courses_expired.isChecked());
                            } else if (updateNotificationType.equals("Safety card")) {
                                switch_safety_cards.setChecked(!switch_safety_cards.isChecked());
                            }
                            /*else if (updateNotificationType.equals("New safety card")) {
                                switch_safety_cards.setChecked(!switch_safety_cards.isChecked());
                            } else if (updateNotificationType.equals("Safety card expired")) {
                                switch_expired_safety_cards.setChecked(!switch_expired_safety_cards.isChecked());
                            } */
                            else if (updateNotificationType.equals("New Diploma")) {
                                switch_diploma.setChecked(!switch_diploma.isChecked());
                            } else if (updateNotificationType.equals("New ClassRoom Course")) {
                                //switch_classRoom_course.setChecked(!switch_expired_safety_cards.isChecked());
                            } else if (updateNotificationType.equals("New Document")) {
                                switch_documents.setChecked(!switch_documents.isChecked());
                            }
                        } else {
                            if (!dbSelect.getNotificationData("NotificationTable", "IsEnabled", updateNotificationType, spManager.getUserID(), "").equals("")) {
                                dbUpdate.updateNotificationTable(updateNotificationType, NotificationEnable, spManager.getUserID());
                            } else {
                                dbInsert.addDataIntoNotificationTable(updateNotificationType, NotificationEnable, spManager.getUserID());
                            }

                            if (updateNotificationType.equals("New Course")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("CourseNotification_ON", NotificationEnable);
                                analytics.logEvent("CourseNotification_ON", bundle);
                            } else if (updateNotificationType.equals("New ClassRoom Course")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("ClassRoomNotification_ON", NotificationEnable);
                                analytics.logEvent("ClassRoomNotification_ON", bundle);
                            } else if (updateNotificationType.equals("Course expired")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("CourseExpiredNotifica_ON", NotificationEnable);
                                analytics.logEvent("CourseExpiredNotification_ON", bundle);
                            } else if (updateNotificationType.equals("Safety card")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("SafetyCardNotfication_ON", NotificationEnable);
                                analytics.logEvent("SafetyCardNotification_ON", bundle);
                            } else if (updateNotificationType.equals("New Diploma")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("DiplomaNotification_ON", NotificationEnable);
                                analytics.logEvent("DiplomaNotification_ON", bundle);
                            } else if (updateNotificationType.equals("New Document")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("DocumentNotification_ON", NotificationEnable);
                                analytics.logEvent("DocumentNotification_ON", bundle);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                } finally {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                if (NotificationType.equals("New Course")) {
                    switch_new_course.setChecked(!switch_new_course.isChecked());
                } else if (NotificationType.equals("Course expired")) {
                    switch_courses_expired.setChecked(!switch_courses_expired.isChecked());
                } else if (NotificationType.equals("Safety card")) {
                    switch_safety_cards.setChecked(!switch_safety_cards.isChecked());
                }
                /*else if (NotificationType.equals("New safety card")) {
                    switch_safety_cards.setChecked(!switch_safety_cards.isChecked());
                } else if (NotificationType.equals("Safety card expired")) {
                    switch_expired_safety_cards.setChecked(!switch_expired_safety_cards.isChecked());
                } */
                else if (NotificationType.equals("New Diploma")) {
                    switch_diploma.setChecked(!switch_diploma.isChecked());
                } else if (NotificationType.equals("ClassRoomCourse")) {
                    switch_classRoom_course.setChecked(!switch_classRoom_course.isChecked());
                } else if (NotificationType.equals("Documents")) {
                    switch_documents.setChecked(!switch_documents.isChecked());
                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"NotificationType\":\"" + NotificationType + "\",\"NotificationEnable\":\"" + NotificationEnable + "\",\"userID\":\"" + UserID + "\",\"deviceType\":\"" + deviceType + "\",\"deviceid\":\"" + connectionDetector.getAndroid_ID(NotificationSettings.this) + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(NotificationSettings.this);
        requestQueue11.add(stringRequest);
    }

    public void setStateOfToggleButtons(List<SwitchCompat> swicthList, List<TextView> textviewList) {
        for (int i = 0; i < textviewList.size(); i++) {
            String type = "";
            if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.notification_new_courses))) {
                type = "New Course";
            } else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.notification_new_diploma))) {
                type = "New Diploma";
            } else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.notification_courses_expired))) {
                type = "Course expired";
            } else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.notification_safety_card))) {
                type = "Safety card";
            }
            /*else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.notification_safety_card))) {
                type = "New safety card";
            } else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.notification_expired_safety_card))) {
                type = "Safety card expired";
            } */
            else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.classroom_course_heading))) {
                type = "New ClassRoom Course";
            } else if (textviewList.get(i).getText().toString().equals(getResources().getString(R.string.new_documents))) {
                type = "New Document";
            }
            String isEnabled = dbSelect.getNotificationData("NotificationTable", "IsEnabled", type, spManager.getUserID(), "");
            if (!isEnabled.equals("")) {
                if (isEnabled.equals("Yes")) {
                    swicthList.get(i).setChecked(true);
                } else {
                    swicthList.get(i).setChecked(false);
                }
            }
        }
    }

    public void showWaitDialog() {
        if (isActivityLive) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(NotificationSettings.this);
                pDialog.setMessage(getString(R.string.please_wait));
                pDialog.setCancelable(false);
                if (!pDialog.isShowing()) {
                    pDialog.show();
                }
            } else {
                if (!pDialog.isShowing()) {
                    pDialog.show();
                }
            }
        }
    }

    public void dismissWaitDialog() {
        if (isActivityLive) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }
}
