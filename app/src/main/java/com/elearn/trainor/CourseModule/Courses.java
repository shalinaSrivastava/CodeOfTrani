package com.elearn.trainor.CourseModule;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.BaseAdapters.*;
import com.elearn.trainor.HelperClasses.*;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PDFView;
import com.elearn.trainor.R;
import com.elearn.trainor.PropertyClasses.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.fabric.sdk.android.services.network.HttpRequest;
import me.leolin.shortcutbadger.ShortcutBadger;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class Courses extends AppCompatActivity implements View.OnClickListener {
    public static Courses instance;
    TextView text_header, txtELearningCourse, txtClassRoomCourse, txtOtherCourses, more_course_url_link;
    LinearLayout llhome, ll_back, ll_yourOtherCourses, ll_classRoomCourses, ll_learningCourses, firstTimeView_elearnCourse;
    RelativeLayout tbl_actionbar;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerDelete dbDelete;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    RecyclerView elearningRecyclerView, classRoomRecyclerView, otherCoursesRecyclerView;
    CardView getCourses;
    List<DiplomaProperty> diplomaPropertyList, activeDiploma, expiredDiploma, eLearningCourseList, classRoomCourseList, blankDateDiploma;
    //List<String> courseIDList;
    List<GetMoreCoursesProperty> getMoreCoursesPropertyArrayList;
    CourseRecyclerViewAdapter adapter;
    CourseElearningRecyclerViewAdapter eleariningadapter;
    OtherCourseRecyclerViewAdapter otherCourseRecyclerViewAdapter;
    ProgressDialog pDialog;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    SwipeRefreshLayout swipelayout;
    public DiplomaProperty diplomaProperty, courseProperty;
    public String diplomaPDF_FileName;
    String FromOperation = "", adapter_course_ID = "", adapter_licence_ID = "", secretKey = "", CMI_Progress = "", currentDate = "", From = "", notificationID = "", deviceId;
    String address, postalCode, country, city;
    Object adapater_hlder;
    int adapater_position = 0;
    CopyOnWriteArrayList<SCORMInfo> completedCoursesList, coursesToBeDeleted, completionApproveList, completedCoursesIntoDiplomaList;
    MessageDigest digest = null;
    List<String> scormKeyList;
    IntentFilter internet_intent_filter;
    public boolean syncApiCalled = false, isActivityLive = false;
    String getAPI_Body_Parameter = "", LicenseID_SCORM_Post = "", postAPI_Body_Parameter = "", completionApproveIDs = "";
    RequestQueue requestQueue;
    List<NotificationProperty> notificationCountList;
    FirebaseAnalytics analytics;
    public String licenseIDsToBeDeleted = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_courses);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        isActivityLive = true;
        getControls();
        eLearningCourseList.clear();
        classRoomCourseList.clear();
        diplomaPropertyList.clear();
        activeDiploma.clear();
        expiredDiploma.clear();
        String sharedLanguage = spManager.getLanguage();
        setLocale(sharedLanguage);
    }

    @Override
    protected void onStart() {
        instance = this;
        isActivityLive = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        isActivityLive = true;
        super.onResume();
       // analytics.setCurrentScreen(Courses.this, "CourseModule", this.getClass().getSimpleName());
    }

    public static Courses getInstance() {
        if (instance == null) {
            instance = new Courses();
        }
        return instance;
    }

    @Override
    public void onBackPressed() {
        if (internetInfoReceiver != null) {
            if (internetInfoReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        Intent intent = new Intent(Courses.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        notificationCountList = new ArrayList<>();
        dbSelect = new DataBaseHandlerSelect(Courses.this);
        dbUpdate = new DataBaseHandlerUpdate(Courses.this);
        dbInsert = new DataBaseHandlerInsert(Courses.this);
        dbDelete = new DataBaseHandlerDelete(Courses.this);
        //Delete Files From Database
        dbDelete.deleteCoursesFromTable("offline");
        dbDelete.deleteCoursesFromTable("CourseDownload");
        dbDelete.deleteCoursesFromTable("SCORMTable");
        dbDelete.deleteCoursesFromTable("CompletionDateSCORMTable");
        spManager = new SharedPreferenceManager(Courses.this);
        requestQueue = Volley.newRequestQueue(Courses.this);
        From = getIntent().getStringExtra("From");
        if (From != null && From.equals("Notification")) {
            notificationID = getIntent().getStringExtra("NotificationID");
        } else {
            From = "";
        }
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(Color.parseColor("#4caf50"));
        txtELearningCourse = (TextView) findViewById(R.id.txtELearningCourse);
        txtELearningCourse.setText(getResources().getString(R.string.elearn_course_heading));
        txtClassRoomCourse = (TextView) findViewById(R.id.txtClassRoomCourse);
        txtClassRoomCourse.setText(getResources().getString(R.string.classroom_course_heading));
        txtOtherCourses = (TextView) findViewById(R.id.txtOtherCourses);
        txtOtherCourses.setText(getResources().getString(R.string.other_courses_heading));
        more_course_url_link = (TextView) findViewById(R.id.more_course_url_link);
        more_course_url_link.setText(getResources().getString(R.string.get_course_heading));
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getResources().getString(R.string.courses));

        completedCoursesList = new CopyOnWriteArrayList<>();
        completedCoursesIntoDiplomaList = new CopyOnWriteArrayList<>();
        coursesToBeDeleted = new CopyOnWriteArrayList<>();
        completionApproveList = new CopyOnWriteArrayList<>();
        //Select Courses From SCORM  Table
        completedCoursesList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "", "");
        completedCoursesIntoDiplomaList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "InsertCompletedCourseIntoDiploma", "");

         coursesToBeDeleted = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "DeleteInternalCourseFiles", "");
        connectionDetector = new ConnectionDetector(Courses.this);
        deviceId = connectionDetector.getAndroid_ID(Courses.this);

        //<----------------- Newly Completed Status Changed   -------------------->
        for (int i = 0; i < completedCoursesIntoDiplomaList.size(); i++) {
            String licenseID = completedCoursesIntoDiplomaList.get(i).LicenceID;
            if (!dbSelect.isLicenseIDExistsInDiplomaTable(licenseID)) {
                dbInsert.insertNewlyCompletedCourseDetailsIntoDiplomaTable(licenseID, "Completed Offline", spManager.getUserID());
            }
            dbDelete.deleteTable("CoursesTable", "userID=? AND licenseId=?", new String[]{spManager.getUserID(), licenseID});
        }
        dbUpdate.updateNewlyCompletedStatus("", spManager.getUserID());
        //<-----------------------  End  --------------------------------->

       // new DBCopierToSD().CopyDBToSDCard();
        scormKeyList = new ArrayList<>();
        eLearningCourseList = new ArrayList<>();
        classRoomCourseList = new ArrayList<>();
        diplomaPropertyList = new ArrayList<>();
        activeDiploma = new ArrayList<>();
        expiredDiploma = new ArrayList<>();
        blankDateDiploma = new ArrayList<>();
        getMoreCoursesPropertyArrayList = new ArrayList<>();
       // courseIDList = new ArrayList<>();
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_classRoomCourses = (LinearLayout) findViewById(R.id.ll_classRoomCourses);
        ll_learningCourses = (LinearLayout) findViewById(R.id.ll_learningCourses);
        ll_yourOtherCourses = (LinearLayout) findViewById(R.id.ll_yourOtherCourses);
        elearningRecyclerView = (RecyclerView) findViewById(R.id.elearningRecyclerView);
        classRoomRecyclerView = (RecyclerView) findViewById(R.id.classRoomRecyclerView);
        otherCoursesRecyclerView = (RecyclerView) findViewById(R.id.otherCoursesRecyclerView);
        firstTimeView_elearnCourse = (LinearLayout) findViewById(R.id.firstTimeView_elearnCourse);
        getCourses = (CardView) findViewById(R.id.getCourses);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        getCourses.setOnClickListener(this);
        elearningRecyclerView.setLayoutManager(new LinearLayoutManager(Courses.this));
        classRoomRecyclerView.setLayoutManager(new LinearLayoutManager(Courses.this));
        otherCoursesRecyclerView.setLayoutManager(new LinearLayoutManager(Courses.this));
        swipelayout.setColorSchemeResources(R.color.courses);
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    if (!syncApiCalled) {
                        if (completedCoursesList.size() > 0) {
                            syncSCORMApi();
                        } else {
                            getDiplomas();
                        }
                    }
                } else {
                    swipelayout.setRefreshing(false);
                    if (isActivityLive) {
                        AlertDialogManager.showDialog(Courses.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                    }
                }
            }
        });
        if (coursesToBeDeleted.size() > 0) {
            deleteCourseFileFromInternalStorage(coursesToBeDeleted.get(0).LicenceID);
        }
        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llhome:
                onBackPressed();
                break;
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.getCourses:
                getMoreCoursesPropertyArrayList = dbSelect.getDataFromCoursePurchaseTable("", "");
                if (getMoreCoursesPropertyArrayList.size() > 1 || connectionDetector.isConnectingToInternet()) {
                    Intent intent = new Intent(Courses.this, GetMoreCourses.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    AlertDialogManager.showDialog(Courses.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
        }
    }

    public void updateNotificationCount(final String NotificationType, final String UserID, final String notificationIDs, final int moduleNotificationCount) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationCount_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    requestQueue.getCache().clear();
                    dbDelete.deleteNotificationUpdatedData("NotificationCountTable", spManager.getUserID(), notificationIDs, "Course");
                    dbDelete.deleteNotificationUpdatedData("UpdateNotification", spManager.getUserID(), notificationIDs, "Course");
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - moduleNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(Courses.this, total);
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestQueue.getCache().clear();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"NotificationType\":\"" + NotificationType + "\",\"userID\":\"" + UserID + "\",\"NotificationIDs\":\"" + notificationIDs + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(Courses.this);
        }
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void getCourse() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.New_Active_Upcoming_Course_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    requestQueue.getCache().clear();
                    eLearningCourseList.clear();
                    classRoomCourseList.clear();
                    diplomaPropertyList.clear();
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            dbDelete.deleteCoursesFromTable("offline");
                            dbDelete.deleteCoursesFromTable("CourseDownload");
                            //dbDelete.deleteCoursesFromTable("CoursesTable");
                            /*//added on 05-02-2021
                            dbDelete.deleteCoursesFromTable("CoursesTable");
                            // end*/
                            dbDelete.deleteCoursesFromTable("CompletionDateSCORMTable");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                eLearningCourseList.clear();
                                classRoomCourseList.clear();
                                diplomaPropertyList.clear();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (!jsonObject.getString("status").equals("CANCELLED")) {
                                    final DiplomaProperty courseProperty = new DiplomaProperty();
                                    courseProperty.expiresDate = jsonObject.getString("expiresDate") == null ? "" : jsonObject.getString("expiresDate").equals("null") ? "" : jsonObject.getString("expiresDate");
                                    String Duration = jsonObject.getString("duration") == null ? "" : jsonObject.getString("duration").equals("null") ? "" : jsonObject.getString("duration");
                                    if (!Duration.equals("")) {
                                        courseProperty.courseDuration = "";
                                        String[] tokens = Duration.split(";");
                                        String firstToken = tokens[0];
                                        String secondToken = tokens[1];
                                        String thirdToken = tokens[2];
                                        int days_int = Integer.parseInt(firstToken);
                                        int hours_int = Integer.parseInt(secondToken);
                                        int minutes_int = Integer.parseInt(thirdToken);
                                        String showDays = days_int > 0 ? (days_int == 1 ? "day" : "days") : "";
                                        String showHours = hours_int > 0 ? (hours_int == 1 ? "hour" : "hours") : "";
                                        String showMinutes = minutes_int > 0 ? (minutes_int == 1 ? "minute" : "minutes") : "";
                                        if (!showDays.equals("")) {
                                            courseProperty.courseDuration = firstToken + " " + showDays;
                                        } else {
                                            if (!showHours.equals("")) {
                                                courseProperty.courseDuration = secondToken + " " + showHours;
                                            }
                                            if (!showMinutes.equals("")) {
                                                if (courseProperty.courseDuration != null && !showHours.equals("")) {
                                                    courseProperty.courseDuration += " " + thirdToken + " " + showMinutes;
                                                } else {
                                                    courseProperty.courseDuration = thirdToken + " " + showMinutes;
                                                }
                                            }
                                        }
                                    } else {
                                        courseProperty.courseDuration = "";
                                    }
                                    if (courseProperty.courseDuration == null) {
                                        courseProperty.courseDuration = "";
                                    }
                                    courseProperty.certificateAvailable = jsonObject.getString("certificateAvailable") == null ? "" : jsonObject.getString("certificateAvailable").equals("null") ? "" : jsonObject.getString("certificateAvailable");
                                    if (jsonObject.getString("location") != null && !jsonObject.getString("location").equals("null")) {
                                        JSONObject jsonObjectlLOC = jsonObject.getJSONObject("location");
                                        if (jsonObjectlLOC != null) {
                                            if (jsonObjectlLOC.has("city")) {
                                                city = jsonObjectlLOC.getString("city") == null ? "" : jsonObjectlLOC.getString("city").equals("null") ? "" : jsonObjectlLOC.getString("city");
                                                courseProperty.courseCity = city;
                                            }
                                        }
                                        // nitish 20-11-2017
                                        if (jsonObjectlLOC != null) {
                                            if (jsonObjectlLOC.has("address")) {
                                                address = jsonObjectlLOC.getString("address") == null ? "" : jsonObjectlLOC.getString("address").equals("null") ? "" : jsonObjectlLOC.getString("address");
                                            }
                                        }
                                        if (jsonObjectlLOC != null) {
                                            if (jsonObjectlLOC.has("postalCode")) {
                                                postalCode = jsonObjectlLOC.getString("postalCode") == null ? "" : jsonObjectlLOC.getString("postalCode").equals("null") ? "" : jsonObjectlLOC.getString("postalCode");
                                            }
                                        }
                                        if (jsonObjectlLOC != null) {
                                            if (jsonObjectlLOC.has("country")) {
                                                country = jsonObjectlLOC.getString("country") == null ? "" : jsonObjectlLOC.getString("country").equals("null") ? "" : jsonObjectlLOC.getString("country");
                                            }
                                        }
                                        if (country != null && country.contains("NOR")) {
                                            country = "NORWAY";
                                        }
                                        courseProperty.location = address + ", " + postalCode + ", " + city + ", " + country;
                                    } else {
                                        courseProperty.location = "";
                                    }
                                    if (jsonObject.getString("description") != null && !jsonObject.getString("description").equals("null")) {
                                        JSONObject jsonObjectDescription = jsonObject.getJSONObject("description");
                                        if (jsonObjectDescription.has("content") && jsonObjectDescription.getString("content") != null) {
                                            courseProperty.info_content = jsonObjectDescription.getString("content").equals("null") ? "" : jsonObjectDescription.getString("content");
                                        } else {
                                            courseProperty.info_content = "";
                                        }
                                        if (jsonObjectDescription.has("goal") && jsonObjectDescription.getString("goal") != null) {
                                            courseProperty.info_goal = jsonObjectDescription.getString("goal").equals("null") ? "" : jsonObjectDescription.getString("goal");
                                        } else {
                                            courseProperty.info_goal = "";
                                        }
                                        if (jsonObjectDescription.has("targetGroup") && jsonObjectDescription.getString("targetGroup") != null) {
                                            courseProperty.info_targetGroup = jsonObjectDescription.getString("targetGroup").equals("null") ? "" : jsonObjectDescription.getString("targetGroup");
                                        } else {
                                            courseProperty.info_targetGroup = "";
                                        }
                                    }
                                    courseProperty.courseId = jsonObject.getString("courseId") == null ? "" : jsonObject.getString("courseId").equals("null") ? "" : jsonObject.getString("courseId");
                                    courseProperty.licenseId = jsonObject.getString("licenseId") == null ? "" : jsonObject.getString("licenseId").equals("null") ? "" : jsonObject.getString("licenseId");
                                    //Delete removed/cancelled courses from Database
                                    if (licenseIDsToBeDeleted.equals("")) {
                                        licenseIDsToBeDeleted = "'" + courseProperty.licenseId + "'";
                                    } else {
                                        licenseIDsToBeDeleted += "," + "'" + courseProperty.licenseId + "'";
                                    }
                                    //End
                                    courseProperty.validUntil = jsonObject.getString("validUntil") == null ? "" : jsonObject.getString("validUntil").equals("null") ? "" : jsonObject.getString("validUntil");
                                    courseProperty.startCourseUrl = jsonObject.getString("startCourseUrl") == null ? "" : jsonObject.getString("startCourseUrl").equals("null") ? "" : jsonObject.getString("startCourseUrl");
                                    courseProperty.completionDate = jsonObject.getString("completionDate") == null ? "" : jsonObject.getString("completionDate").equals("null") ? "" : jsonObject.getString("completionDate");
                                    courseProperty.language = jsonObject.getString("language") == null ? "" : jsonObject.getString("language").endsWith("null") ? "" : jsonObject.getString("language");
                                    courseProperty.status = jsonObject.getString("status") == null ? "" : jsonObject.getString("status").equals("null") ? "" : jsonObject.getString("status");
                                    courseProperty.startDate = jsonObject.getString("startDate") == null ? "" : jsonObject.getString("startDate").equals("null") ? "" : jsonObject.getString("startDate");
                                    courseProperty.availableOffline = jsonObject.getString("availableOffline") == null ? "" : jsonObject.getString("availableOffline").equals("null") ? "" : jsonObject.getString("availableOffline");
                                    courseProperty.courseName = jsonObject.getString("courseName") == null ? "" : jsonObject.getString("courseName").equals("null") ? "" : jsonObject.getString("courseName");
                                    courseProperty.HolderState = 0;
                                    if (!courseProperty.startCourseUrl.equals("")) {
                                        courseProperty.courseType = "E-Learning";
                                    } else {
                                        courseProperty.courseType = "Classroom";
                                    }
                                    if (dbSelect.getDataFromCoursesTable("*", spManager.getUserID(), courseProperty.licenseId.trim()).equals("")) {
                                        courseProperty.userID = spManager.getUserID();
                                        if (jsonObject.has("isEpkg")) {
                                            if (jsonObject.getString("isEpkg").equals("true")) {
                                                long timeStamp = System.currentTimeMillis();
                                                courseProperty.timeStamp = String.valueOf(timeStamp);
                                                dbInsert.addDataIntoCoursesTable(courseProperty);
                                            } else if (courseProperty.startCourseUrl.equals("")) {
                                                long timeStamp = System.currentTimeMillis();
                                                courseProperty.timeStamp = String.valueOf(timeStamp);
                                                dbInsert.addDataIntoCoursesTable(courseProperty);
                                            }
                                        } else {
                                            long timeStamp = System.currentTimeMillis();
                                            courseProperty.timeStamp = String.valueOf(timeStamp);
                                            dbInsert.addDataIntoCoursesTable(courseProperty);
                                        }
                                    }else{ // added on 05-02-2021, else part because course status not updating
                                        dbUpdate.updateTable("CoursesTable", spManager.getUserID(), courseProperty.licenseId.trim(), "status", courseProperty.status);
                                    }

                                    //Get Course Image URL and update into Courses Table 14_Mar_2019
                                    if (jsonObject.has("courseMediaFiles")) {
                                        JSONArray jsonImgArr = jsonObject.getJSONArray("courseMediaFiles");
                                        if (jsonImgArr != null && jsonImgArr.length() > 0) {
                                            for (int j = 0; j < jsonImgArr.length(); j++) {
                                                String _courseImageURL = jsonImgArr.getJSONObject(j).getString("downloadUrl");
                                                _courseImageURL = _courseImageURL == null ? "" : _courseImageURL;
                                                String checkFileName = _courseImageURL.toLowerCase();
                                                if (checkFileName.contains(".jpg") || checkFileName.contains(".jpeg") || checkFileName.contains(".png")) {
                                                    dbUpdate.updateImageURLInCourseTable(courseProperty.courseId, _courseImageURL);
                                                    break;
                                                }
                                            }
                                        } else {
                                            dbUpdate.updateImageURLInCourseTable(courseProperty.courseId, "");
                                        }
                                    } else {
                                        dbUpdate.updateImageURLInCourseTable(courseProperty.courseId, "");
                                    }
                                    //End

                                    String dwTime = dbSelect.getDataFromCourseDownloadTable("IfNull(DownloadTime,'')as dwtime", spManager.getUserID(), courseProperty.courseId, courseProperty.licenseId);
                                    if (!dwTime.equals("")) {
                                        dbUpdate.updateTable("CoursesTable", spManager.getUserID(), courseProperty.licenseId, "DownloadTime", dwTime);
                                    }
                                    //<--------------------SCORM Insertion For Online Course Percentage------------------------->
                                    if (jsonObject.has("isEpkg")) {
                                        if (jsonObject.getString("isEpkg").equals("true")) {
                                            /*if (!courseProperty.startCourseUrl.equals("")) {
                                                courseIDList.add(courseProperty.courseId);
                                            }*/
                                            if (!dbSelect.isSCORMCOontentExists(spManager.getUserID(), courseProperty.licenseId)) {
                                                dbInsert.addDataIntoSCORMTable(courseProperty.licenseId, spManager.getUserID(), "");
                                            }
                                        }
                                        /*if (courseProperty.startCourseUrl.equals("")) {
                                            courseIDList.add(courseProperty.courseId);
                                        }*/
                                    } else {
                                        /*courseIDList.add(courseProperty.courseId);*/
                                        if (!dbSelect.isSCORMCOontentExists(spManager.getUserID(), courseProperty.licenseId)) {
                                            dbInsert.addDataIntoSCORMTable(courseProperty.licenseId, spManager.getUserID(), "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    /*if (courseIDList != null && courseIDList.size() > 0) {
                        getCourseImageURL(courseIDList.get(0));
                    } else {
                        completedCoursesList.clear();
                        completedCoursesList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "", "");
                        showListOnCourse();
                    }*/
                    dbDelete.deleteCoursesNotExist(spManager.getUserID(), licenseIDsToBeDeleted);
                    completedCoursesList.clear();
                    completedCoursesList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "", "");
                    showListOnCourse();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestQueue.getCache().clear();
                completedCoursesList.clear();
                completedCoursesList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "", "");
                showListOnCourse();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(Courses.this);
        }
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void getDiplomas() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Diploma_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                swipelayout.setRefreshing(false);
                showWaitDialog();
                try {
                    requestQueue.getCache().clear();
                    activeDiploma.clear();
                    expiredDiploma.clear();
                    blankDateDiploma.clear();
                    diplomaPropertyList.clear();
                    //courseIDList.clear();
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        dbDelete.deleteCoursesFromTable("CompletionDateCourseTable");
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                DiplomaProperty diplomaProperty = new DiplomaProperty();
                                diplomaProperty.userID = spManager.getUserID();
                                diplomaProperty.expiresDate = jsonObject.getString("expiresDate").equals("null") ? "" : jsonObject.getString("expiresDate") == null ? "" : jsonObject.getString("expiresDate");
                                diplomaProperty.certificateAvailable = jsonObject.getString("certificateAvailable").equals("null") ? "" : jsonObject.getString("certificateAvailable") == null ? "" : jsonObject.getString("certificateAvailable");
                                diplomaProperty.courseId = jsonObject.getString("courseId").equals("null") ? "" : jsonObject.getString("courseId") == null ? "" : jsonObject.getString("courseId");
                                diplomaProperty.licenseId = jsonObject.getString("licenseId").equals("null") ? "" : jsonObject.getString("licenseId") == null ? "" : jsonObject.getString("licenseId");
                                diplomaProperty.validUntil = jsonObject.getString("validUntil").equals("null") ? "" : jsonObject.getString("validUntil") == null ? "" : jsonObject.getString("validUntil");
                                diplomaProperty.startCourseUrl = jsonObject.getString("startCourseUrl").equals("null") ? "" : jsonObject.getString("startCourseUrl") == null ? "" : jsonObject.getString("startCourseUrl");
                                diplomaProperty.completionDate = jsonObject.getString("completionDate").equals("null") ? "" : jsonObject.getString("completionDate") == null ? "" : jsonObject.getString("completionDate");
                                diplomaProperty.language = jsonObject.getString("language").equals("null") ? "" : jsonObject.getString("language") == null ? "" : jsonObject.getString("language");
                                diplomaProperty.status = jsonObject.getString("status").equals("null") ? "" : jsonObject.getString("status") == null ? "" : jsonObject.getString("status");
                                diplomaProperty.startDate = jsonObject.getString("startDate").equals("null") ? "" : jsonObject.getString("startDate") == null ? "" : jsonObject.getString("startDate");
                                diplomaProperty.courseName = jsonObject.getString("courseName").equals("null") ? "" : jsonObject.getString("courseName") == null ? "" : jsonObject.getString("courseName");
                                diplomaProperty.availableOffline = jsonObject.getString("availableOffline") == null ? "" : jsonObject.getString("availableOffline").equals("null") ? "" : jsonObject.getString("availableOffline");
                                if (jsonObject.getString("duration") != null && !jsonObject.getString("duration").equals("null")) {
                                    String[] tokens = jsonObject.getString("duration").split(";");
                                    int days_int = Integer.parseInt(tokens[0]);
                                    int hours_int = Integer.parseInt(tokens[1]);
                                    int minutes_int = Integer.parseInt(tokens[2]);
                                    String showDays = days_int > 0 ? (days_int == 1 ? "day" : "days") : "";
                                    String showHours = hours_int > 0 ? (hours_int == 1 ? "hour" : "hours") : "";
                                    String showMinutes = minutes_int > 0 ? (minutes_int == 1 ? "minute" : "minutes") : "";
                                    if (!showDays.equals("")) {
                                        diplomaProperty.courseDuration = tokens[0] + " " + showDays;
                                    } else {
                                        if (!showHours.equals("")) {
                                            diplomaProperty.courseDuration = hours_int + " " + showHours;
                                        }
                                        if (!showMinutes.equals("")) {
                                            if (diplomaProperty.courseDuration != null && !showHours.equals("")) {
                                                diplomaProperty.courseDuration += " " + minutes_int + " " + showMinutes;
                                            } else {
                                                diplomaProperty.courseDuration = minutes_int + " " + showMinutes;
                                            }
                                        }
                                    }
                                } else {
                                    diplomaProperty.courseDuration = "";
                                }
                                if (diplomaProperty.courseDuration == null) {
                                    diplomaProperty.courseDuration = "";
                                }
                                if (jsonObject.getString("description") != null && !jsonObject.getString("description").equals("null")) {
                                    JSONObject jsonObjectDescription = jsonObject.getJSONObject("description");
                                    if (jsonObjectDescription.has("content") && jsonObjectDescription.getString("content") != null) {
                                        diplomaProperty.info_content = jsonObjectDescription.getString("content") == null ? "" : jsonObjectDescription.getString("content").equals("null") ? "" : jsonObjectDescription.getString("content");
                                    } else {
                                        diplomaProperty.info_content = "";
                                    }
                                    if (jsonObjectDescription.has("goal") && jsonObjectDescription.getString("goal") != null) {
                                        diplomaProperty.info_goal = jsonObjectDescription.getString("goal") == null ? "" : jsonObjectDescription.getString("goal").equals("null") ? "" : jsonObjectDescription.getString("goal");
                                    } else {
                                        diplomaProperty.info_goal = "";
                                    }
                                    if (jsonObjectDescription.has("targetGroup") && jsonObjectDescription.getString("targetGroup") != null) {
                                        diplomaProperty.info_targetGroup = jsonObjectDescription.getString("targetGroup") == null ? "" : jsonObjectDescription.getString("targetGroup").equals("null") ? "" : jsonObjectDescription.getString("targetGroup");
                                    } else {
                                        diplomaProperty.info_targetGroup = "";
                                    }
                                }

                                //    <------------------------------------------------        Online Completion 1 Week Logic           -------------------------------------------------->
                                if (diplomaProperty.completionDate != null && !diplomaProperty.completionDate.equals("")) {
                                    String completionDate = "";
                                    try {
                                        SimpleDateFormat completedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        Date completedDate = completedDateFormat.parse(diplomaProperty.completionDate.substring(0, 10));
                                        completionDate = completedDateFormat.format(completedDate);
                                    } catch (Exception ex) {
                                        Log.d("Error", ex.getMessage());
                                    }
                                    diplomaProperty.completionDate = completionDate;
                                    boolean isNewlyCompleted = dbSelect.getCompletionDateDifference(completionDate);
                                    if (isNewlyCompleted) {
                                        diplomaProperty.isNewlyCompleted = "true";
                                    } else {
                                        diplomaProperty.isNewlyCompleted = "false";
                                    }
                                }
                                diplomaProperty.notToBeDeleted = "false";
                                //<------------------------------------------    End ------------------------------------------------------------------------->

                                if (!diplomaProperty.validUntil.equals("")) {
                                    Date validTill = df.parse(diplomaProperty.validUntil);
                                    diplomaProperty.validUntil = df.format(validTill);
                                    String todayDate = df.format(Calendar.getInstance().getTime());
                                    Date currentDate = df.parse(todayDate);
                                    if (validTill.before(currentDate) || validTill.equals("null") || validTill.equals("")) {
                                        diplomaProperty.diplomaStatus = "expired";
                                        expiredDiploma.add(diplomaProperty);
                                    } else {
                                        diplomaProperty.diplomaStatus = "active";
                                        activeDiploma.add(diplomaProperty);
                                    }
                                } else {
                                    // changed on 27-04-2020
                                    diplomaProperty.diplomaStatus = "active";
                                    activeDiploma.add(diplomaProperty);
                                    /*diplomaProperty.diplomaStatus = "expired";
                                    blankDateDiploma.add(diplomaProperty);*/
                                }
                            }
                            diplomaPropertyList.clear();
                            diplomaPropertyList.addAll(activeDiploma);
                            diplomaPropertyList.addAll(expiredDiploma);
                            Collections.sort(diplomaPropertyList, new ComparatorHelperDiploma());
                            Collections.reverse(diplomaPropertyList);
                            diplomaPropertyList.addAll(blankDateDiploma);
                            if (diplomaPropertyList.size() > 0) {
                                long result = dbDelete.deleteTableByName("DiplomasTable", spManager.getUserID());
                                dbInsert.addDataIntoDiplomasTable(diplomaPropertyList);
                            } else {
                                ll_yourOtherCourses.setVisibility(View.GONE);
                            }
                        } else {
                            ll_yourOtherCourses.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    getCourse();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestQueue.getCache().clear();
                getCourse();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(Courses.this);
        }
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void showWaitDialog() {
        if (isActivityLive) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(Courses.this);
            }
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
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

    public void showListOnCourse() {
        completionApproveList.clear();
        eLearningCourseList.clear();
        classRoomCourseList.clear();
        List<DiplomaProperty> diplomaPropertyListNew = dbSelect.getCoursesDetailsList("", "", spManager.getUserID());
        for (DiplomaProperty courseProperty : diplomaPropertyListNew) {
            if (!courseProperty.startCourseUrl.equals("")) {
                eLearningCourseList.add(courseProperty);
            } else {
                classRoomCourseList.add(courseProperty);
            }
        }
        if (eLearningCourseList.size() > 0) {
            if (checkWriteExternalPermission()) {
                eleariningadapter = new CourseElearningRecyclerViewAdapter(Courses.this, eLearningCourseList, "E-Learning", true);
                elearningRecyclerView.setAdapter(eleariningadapter);
                eleariningadapter.notifyDataSetChanged();
            } else {
                eleariningadapter = new CourseElearningRecyclerViewAdapter(Courses.this, eLearningCourseList, "E-Learning", false);
                elearningRecyclerView.setAdapter(eleariningadapter);
                eleariningadapter.notifyDataSetChanged();
            }
        } else {
            ll_learningCourses.setVisibility(View.GONE);
        }
        if (classRoomCourseList.size() > 0) {
            adapter = new CourseRecyclerViewAdapter(Courses.this, classRoomCourseList, "Classroom");
            classRoomRecyclerView.setAdapter(adapter);
        } else {
            ll_classRoomCourses.setVisibility(View.GONE);
        }
        if (eLearningCourseList.size() == 0 && classRoomCourseList.size() == 0) {
            firstTimeView_elearnCourse.setVisibility(View.VISIBLE);
            ll_learningCourses.setVisibility(View.GONE);
            ll_classRoomCourses.setVisibility(View.GONE);
        } else {
            firstTimeView_elearnCourse.setVisibility(View.GONE);
        }
        diplomaPropertyList.clear();
        diplomaPropertyList = dbSelect.getDiplomaPageDetailsList("CoursePage", spManager.getUserID());
        if (diplomaPropertyList != null && diplomaPropertyList.size() > 0) {
            otherCourseRecyclerViewAdapter = new OtherCourseRecyclerViewAdapter(Courses.this, diplomaPropertyList);
            otherCoursesRecyclerView.setAdapter(otherCourseRecyclerViewAdapter);
            otherCourseRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            ll_yourOtherCourses.setVisibility(View.GONE);
        }
        syncApiCalled = false;
        swipelayout.setRefreshing(false);
        dismissWaitDialog();
    }

    public void askForPermission(String courseID, String licenceid, Object hlder, int pos) {
        this.adapter_course_ID = courseID;
        this.adapter_licence_ID = licenceid;
        this.adapater_hlder = hlder;
        this.adapater_position = pos;
        CoursesPermissionsDispatcher.refreshElearningAdapterWithPermissionCheck(Courses.this);
    }

    public void askForWritePermission(DiplomaProperty info) {
        FromOperation = "JustWritPermission";
        courseProperty = info;
        CoursesPermissionsDispatcher.refreshElearningAdapterWithPermissionCheck(Courses.this);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void refreshElearningAdapter() {
        List<DiplomaProperty> diplomaPropertyListNew = dbSelect.getCoursesDetailsList("", "", spManager.getUserID());
        for (DiplomaProperty courseProperty : diplomaPropertyListNew) {
            eLearningCourseList.clear();
            if (!courseProperty.startCourseUrl.equals("")) {
                eLearningCourseList.add(courseProperty);
            }
        }
        if (eLearningCourseList.size() > 0) {
            eleariningadapter = new CourseElearningRecyclerViewAdapter(Courses.this, eLearningCourseList, "E-Learning", true);
            elearningRecyclerView.setAdapter(eleariningadapter);
        } else {
            ll_learningCourses.setVisibility(View.GONE);
        }
    }

    public boolean checkWriteExternalPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            CoursesPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
            final String permission = permissions[0];
            switch (permissions[0]) {
                case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //dismissWaitDialog();
                        if (FromOperation != null && FromOperation.equals("diploma")) {
                            showWaitDialog();
                            downloadPDF_File();
                        } else if (FromOperation != null && FromOperation.equals("JustWritPermission")) {
                            dismissWaitDialog();
                            eleariningadapter.startCourse(courseProperty);
                        } else {
                            dismissWaitDialog();
                            eLearningCourseList.clear();
                            List<DiplomaProperty> diplomaPropertyListNew = dbSelect.getCoursesDetailsList("", "", spManager.getUserID());
                            for (DiplomaProperty courseProperty : diplomaPropertyListNew) {
                                if (!courseProperty.startCourseUrl.equals("")) {
                                    eLearningCourseList.add(courseProperty);
                                }
                            }
                            if (eLearningCourseList.size() > 0) {
                                eleariningadapter = new CourseElearningRecyclerViewAdapter(Courses.this, eLearningCourseList, "E-Learning", true);
                                elearningRecyclerView.setAdapter(eleariningadapter);
                            } else {
                                ll_learningCourses.setVisibility(View.GONE);
                                firstTimeView_elearnCourse.setVisibility(View.VISIBLE);
                            }
                            eleariningadapter.getVideoFileSize(adapter_course_ID, adapter_licence_ID, (CourseElearningRecyclerViewAdapter.ViewHolder) adapater_hlder);
                        }
                    } else {
                        boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(Courses.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (!neverAskAgainIsEnabled) {
                            dismissWaitDialog();
                            if (isActivityLive) {
                                AlertDialogManager.showCustomDialog(Courses.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                                    @Override
                                    public void onClick() {
                                        Intent intent = new Intent();
                                        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", Courses.this.getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.cancel), "");
                            }
                        } else {
                            dismissWaitDialog();
                            if (isActivityLive) {
                                AlertDialogManager.showCustomDialog(Courses.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                                    @Override
                                    public void onClick() {
                                        ActivityCompat.requestPermissions(Courses.this, new String[]{permission}, requestCode);
                                    }
                                }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                            }
                        }
                    }
                    break;
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    public void startDownloadingWithPermission(DiplomaProperty DipomaPDFInfo, String diplomaPDF_FileName) {
        this.diplomaProperty = DipomaPDFInfo;
        this.diplomaPDF_FileName = diplomaPDF_FileName;
        FromOperation = "diploma";
        CoursesPermissionsDispatcher.startDownloadingWithPermissionCheck(Courses.this);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startDownloading() {
        showWaitDialog();
        downloadPDF_File();
    }

    public void showDiplomaPDF_File(File file) {
        dismissWaitDialog();
        if (!file.exists()) {
            if (isActivityLive) {
                AlertDialogManager.showDialog(Courses.this, getResources().getString(R.string.bad_file_format), getResources().getString(R.string.no_available_diploma_show), false, null);
            }
        } else {
            //Start Download Diploa Analytics
            Bundle bundle = new Bundle();
            bundle.putString("DiplomaView", "Yes");
            analytics.logEvent("DiplomaView", bundle);
            //End Download Diploma Analytics
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0 && file.isFile()) {
                if (Build.MANUFACTURER.equals("Pixavi")) {
                    Intent pdfViewIntent = new Intent(Courses.this, PDFView.class);
                    pdfViewIntent.putExtra("FileName", file.toString());
                    pdfViewIntent.putExtra("CustomerID", "");
                    pdfViewIntent.putExtra("PDFfileURL", "");
                    pdfViewIntent.putExtra("FromAcitivity", "CourseActivity");
                    pdfViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(pdfViewIntent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    Courses.this.finishAffinity();
                } else {
                    try {
                        Uri uri = FileProvider.getUriForFile(Courses.this,
                                getString(R.string.file_provider_authority),
                                file);
                        intent.setDataAndType(uri, "application/pdf");
                        startActivity(intent);
                    } catch (Exception ex) {
                        Log.d("Error", ex.getMessage().toString());
                    }
                }
            } else {
                if (isActivityLive) {
                    AlertDialogManager.showDialog(Courses.this, getResources().getString(R.string.no_any_supported_application), "", false, null);
                }
            }
        }
    }

    public void downloadDiplomaFromServer(final String fileURL, final String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    request.accept("application/pdf");
                    request.contentType("application/pdf");
                    request.authorization("Bearer " + spManager.getToken());
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, fileName + ".pdf");
                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        DeleteFile(file);
                    }
                } catch (Exception ex) {
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    File file = new File(dir, fileName + ".pdf");
                    DeleteFile(file);
                    dismissWaitDialog();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/" + fileName + ".pdf");
                if (file.exists()) {
                    showDownloadedPDFFile(file);
                }
            }
        }.execute();
    }

    public void downloadPDF_File() {
        writeNoMediaFile();
        if (connectionDetector.isConnectingToInternet()) {
            Bundle bundle = new Bundle();
            bundle.putString("CourseDiplomaView", "Yes");
            analytics.logEvent("CourseDiplomaView", bundle);
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/" + diplomaPDF_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDiplomaPDF_File(file);
            } else {
                if ((diplomaProperty.licenseId != null && !diplomaProperty.licenseId.equals("")) && (diplomaProperty.language != null && !diplomaProperty.language.equals(""))) {
                    downloadDiplomaFromServer(WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language, diplomaPDF_FileName);
                } else {
                    if (isActivityLive) {
                        AlertDialogManager.showDialog(Courses.this, getResources().getString(R.string.download_error), getResources().getString(R.string.no_diploma_available_downloaded), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                            }
                        });
                    }
                }
            }
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/" + diplomaPDF_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDiplomaPDF_File(file);
            } else {
                if (isActivityLive) {
                    AlertDialogManager.showDialog(Courses.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            dismissWaitDialog();
                        }
                    });
                }
            }
        }
    }

    public String getSecretKey(final String Salt_lisencID) {
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(Salt_lisencID.getBytes());
            secretKey = bytesToHexString(digest.digest()).toUpperCase();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return secretKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public void syncSCORMApi() {
        showWaitDialog();
        for (int i = 0; i < completedCoursesList.size(); i++) {
            String LisenceID_from_list = completedCoursesList.get(i).LicenceID;
            secretKey = dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LisenceID_from_list, "secretKey");
            if (secretKey.equals("") || secretKey.equals("undefined")) {
                secretKey = getSecretKey("Otlq8k9Az7cXcr0sKo5v" + LisenceID_from_list);
                dbUpdate.updateSCORMTable(spManager.getUserID(), LisenceID_from_list, "secretKey", secretKey);
            }
            if (getAPI_Body_Parameter.equals("")) {
                getAPI_Body_Parameter = " {\"licenseId\" :   \"" + LisenceID_from_list + "\"  , \"secret\" : \"" + secretKey + "\"}";
            } else {
                getAPI_Body_Parameter += ", {\"licenseId\" :   \"" + LisenceID_from_list + "\"  , \"secret\" : \"" + secretKey + "\"}";
            }
        }
        getScrormValues(getAPI_Body_Parameter);
        getAPI_Body_Parameter = "";
    }

    public void getScrormValues(final String bodyParameterStructure) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.NEW_SCORM_GET_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    requestQueue.getCache().clear();
                    String cmiLocation = "", cmiSuccessStatus = "", cmiCompletionStatus = "", cmi_progress = "", identifier = "";
                    if (response != null && !response.equals("")) {
                        LicenseID_SCORM_Post = "";
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String licenseId = jsonObject.getString("licenseId");
                            JSONArray itemsJsonArray = jsonObject.getJSONArray("items");
                            if (itemsJsonArray != null && itemsJsonArray.length() > 0) {
                                JSONObject jsonObjectItems = itemsJsonArray.getJSONObject(0);
                                if (jsonObjectItems.has("identifier")) {
                                    identifier = jsonObjectItems.getString("identifier") == null ? "" : jsonObjectItems.getString("identifier").equals("null") ? "" : jsonObjectItems.getString("identifier");
                                }
                                JSONArray valuesJsonArray = jsonObjectItems.getJSONArray("values");
                                if (valuesJsonArray != null && valuesJsonArray.length() > 0) {
                                    for (int j = 0; j < valuesJsonArray.length(); j++) {
                                        JSONObject jsonObj = valuesJsonArray.getJSONObject(j);
                                        if (jsonObj.getString("key") != null && jsonObj.getString("key").equals("cmi.location")) {
                                            cmiLocation = jsonObj.getString("value") == null ? "" : jsonObj.getString("value").equals("null") ? "" : jsonObj.getString("value").equals("unknown") ? "" : jsonObj.getString("value");
                                        }
                                        if (jsonObj.getString("key") != null && jsonObj.getString("key").equals("cmi.completion_status")) {
                                            cmiCompletionStatus = jsonObj.getString("value") == null ? "" : jsonObj.getString("value").equals("null") ? "" : jsonObj.getString("value").equals("unknown") ? "" : jsonObj.getString("value");
                                        }
                                        if (jsonObj.getString("key") != null && jsonObj.getString("key").equals("cmi.success_status")) {
                                            cmiSuccessStatus = jsonObj.getString("value") == null ? "" : jsonObj.getString("value").equals("null") ? "" : jsonObj.getString("value").equals("unknown") ? "" : jsonObj.getString("value");
                                        }
                                        if (jsonObj.getString("key") != null && jsonObj.getString("key").equals("cmi.progress_measure")) {
                                            cmi_progress = jsonObj.getString("value") == null ? "0" : jsonObj.getString("value").equals("null") ? "0" : jsonObj.getString("value").equals("unknown") ? "0" : jsonObj.getString("value").equals("") ? "0" : jsonObj.getString("value");
                                        }
                                    }
                                    CMI_Progress = dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), licenseId, "cmiProgressMeasure");
                                    String cmiProgress_Local = CMI_Progress.toString().trim().equals("") ? "0" : CMI_Progress.equals("undefined") ? "0" : CMI_Progress;
                                    dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "identifier", identifier);
                                    if ((Double.valueOf(cmiProgress_Local)) < (Double.valueOf(cmi_progress))) {
                                        if ((Double.valueOf(cmi_progress)) == 1.0) {
                                            dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "StatusCompletedOnLive", "Yes");
                                            dbDelete.deleteTable("CoursesTable", "userID=? AND licenseId=?", new String[]{spManager.getUserID(), licenseId});
                                        }
                                        dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "cmiLocation", cmiLocation);
                                        dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "cmiCompletionStatus", cmiCompletionStatus);
                                        dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "cmiSuccessStatus", cmiSuccessStatus);
                                        dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "cmiProgressMeasure", cmi_progress);
                                    } else if ((Double.valueOf(cmiProgress_Local)) > (Double.valueOf(cmi_progress))) {
                                        if (LicenseID_SCORM_Post.equals("")) {
                                            LicenseID_SCORM_Post = "'" + licenseId + "'";
                                        } else {
                                            LicenseID_SCORM_Post += "," + "'" + licenseId + "'";
                                        }
                                    } else if (Double.parseDouble(cmiProgress_Local)==(Double.parseDouble(cmi_progress))) {
                                        if ((Double.parseDouble(cmiProgress_Local) == 1.0) && (Double.parseDouble(cmi_progress) == 1.0)) {
                                            dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "StatusCompletedOnLive", "Yes");
                                            dbUpdate.updateNewlyCompletedStatus("SyncedForDiploma", spManager.getUserID());
                                        }else if((Double.parseDouble(cmiProgress_Local) == 0.0) && (Double.parseDouble(cmi_progress) == 0.0)){ // newly added else if condition 10-02-2021
                                            dbUpdate.updateSCORMTable(spManager.getUserID(), licenseId, "cmiProgressMeasure", cmi_progress);
                                        }
                                    }
                                }
                            }
                        }
                        /*Newly added code by lov tyagi 13-12-2018*/
                        CopyOnWriteArrayList<SCORMInfo> completedNotUpdatedList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "CompletedNotUpdated", LicenseID_SCORM_Post);
                        if (completedNotUpdatedList != null && completedNotUpdatedList.size() > 0) {
                            for (SCORMInfo item : completedNotUpdatedList) {
                                if (LicenseID_SCORM_Post.equals("")) {
                                    LicenseID_SCORM_Post = "'" + item.LicenceID + "'";
                                } else {
                                    LicenseID_SCORM_Post += "," + "'" + item.LicenceID + "'";
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    List<SCORMInfo> scormList = dbSelect.getSCORMValuesForNewPostAPI(spManager.getUserID(), LicenseID_SCORM_Post, "");
                    LicenseID_SCORM_Post = "";
                    completionApproveIDs = "";
                    postAPI_Body_Parameter = "";
                    if (scormList.size() > 0) {
                        // nitish 06-12-2017 append string
                        for (int i = 0; i < scormList.size(); i++) {
                            String LisenceID_from_scormList = scormList.get(i).LicenceID;
                            String secretKey_from_scormList = scormList.get(i).secretKey;
                            String cmiCompletionStatus = scormList.get(i).cmiCompletionStatus;
                            String cmiLocation = scormList.get(i).cmiLocation;
                            String cmiSuccessStatus = scormList.get(i).cmiSuccessStatus;
                            String cmiProgressMeasure = scormList.get(i).cmiProgressMeasure;
                            String identifier = scormList.get(i).identifier;
                            String startedDate = "";
                            if (!dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LisenceID_from_scormList, "started").equals("undefined") || !dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LisenceID_from_scormList, "started").equals("")) {
                                startedDate = dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LisenceID_from_scormList, "started");
                            }
                            if (cmiProgressMeasure.equals("1.0")) {
                                if (completionApproveIDs.equals("")) {
                                    completionApproveIDs = "'" + LisenceID_from_scormList + "'";
                                } else {
                                    completionApproveIDs += "," + "'" + LisenceID_from_scormList + "'";
                                }
                            }
                            if (postAPI_Body_Parameter.equals("")) {
                                postAPI_Body_Parameter = " { \"licenseAndSecret\": { \"licenseId\": \"" + LisenceID_from_scormList + "\", \"secret\": \"" + secretKey_from_scormList + "\" }, \"request\": { \"items\": [ { \"identifier\": \"" + identifier + "\", \"values\": [ { \"key\": \"cmi.completion_status\", \"value\": \"" + cmiCompletionStatus + "\" }, { \"key\": \"cmi.location\", \"value\": \"" + cmiLocation + "\" }, { \"key\": \"cmi.success_status\", \"value\": \"" + cmiSuccessStatus + "\" }, { \"key\": \"cmi.progress_measure\", \"value\": \"" + cmiProgressMeasure + "\"} ] } ], \"started\": \"" + startedDate + "\" } }";
                            } else {
                                postAPI_Body_Parameter += ", { \"licenseAndSecret\": { \"licenseId\": \"" + LisenceID_from_scormList + "\", \"secret\": \"" + secretKey_from_scormList + "\" }, \"request\": { \"items\": [ { \"identifier\": \"" + identifier + "\", \"values\": [ { \"key\": \"cmi.completion_status\", \"value\": \"" + cmiCompletionStatus + "\" }, { \"key\": \"cmi.location\", \"value\": \"" + cmiLocation + "\" }, { \"key\": \"cmi.success_status\", \"value\": \"" + cmiSuccessStatus + "\" }, { \"key\": \"cmi.progress_measure\", \"value\": \"" + cmiProgressMeasure + "\"} ] } ], \"started\": \"" + startedDate + "\" } }";
                            }
                        }
                        postSCORM_Values(postAPI_Body_Parameter, completionApproveIDs);
                    } else {
                        getDiplomas();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestQueue.getCache().clear();
                getDiplomas();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String strParameters = "[ " + bodyParameterStructure + " ]";
                return strParameters.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(Courses.this);
        }
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void postSCORM_Values(final String bodyParameterStructure, final String postLicenseIDs) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.NEW_SCORM_Update_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    requestQueue.getCache().clear();
                    String lincenseIDsNotUpdated = "";
                    JSONArray jsonArray = new JSONArray(response);
                    dbUpdate.updateNewlyCompletedStatus("SyncedForDiploma", spManager.getUserID());
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (lincenseIDsNotUpdated.equals("")) {
                                lincenseIDsNotUpdated = "'" + jsonArray.getString(i) + "'";
                            } else {
                                lincenseIDsNotUpdated += "," + "'" + jsonArray.getString(i) + "'";
                            }
                        }
                        // If License IDs retuned from api as response then what should we do ???
                        if (!lincenseIDsNotUpdated.equals("")) {

                        } else {

                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    completionApproveList = dbSelect.getSCORMValuesForNewPostAPI(spManager.getUserID(), postLicenseIDs, "CompletionApprove");
                    if (completionApproveList.size() > 0) {
                        courseCompletionApproveApi(completionApproveList.get(0).LicenceID, completionApproveList.get(0).secretKey);
                    } else {
                        getDiplomas();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestQueue.getCache().clear();
                if (completionApproveList.size() > 0) {
                    courseCompletionApproveApi(completionApproveList.get(0).LicenceID, completionApproveList.get(0).secretKey);
                } else {
                    getDiplomas();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String strParameters = "[ " + bodyParameterStructure + " ]";
                return strParameters.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(Courses.this);
        }
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void courseCompletionApproveApi(final String licenceID, final String secretKeyApproval) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Calendar calendar = Calendar.getInstance();
            currentDate = sdf.format(calendar.getTime());
        } catch (Exception ex) {
            swipelayout.setRefreshing(false);
            dismissWaitDialog();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Approve_Course_URL + licenceID + "/" + secretKeyApproval, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                requestQueue.getCache().clear();
                dbUpdate.updateSCORMTable(spManager.getUserID(), licenceID, "StatusCompletedOnLive", "Yes");
                completionApproveList.remove(0);
                if (completionApproveList.size() > 0) {
                    courseCompletionApproveApi(completionApproveList.get(0).LicenceID, completionApproveList.get(0).secretKey);
                } else {
                    getDiplomas();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestQueue.getCache().clear();
                completionApproveList.remove(0);
                if (completionApproveList.size() > 0) {
                    courseCompletionApproveApi(completionApproveList.get(0).LicenceID, completionApproveList.get(0).secretKey);
                } else {
                    getDiplomas();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                params.put("Accept", "application/json");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{ \"completedDate\": \"" + currentDate + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(Courses.this);
        }
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            NotificationProperty notificationData = dbSelect.notificationDataToBeUpdated("NotificationCountTable", spManager.getUserID(), "Course");
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                if (!syncApiCalled) {
                    showWaitDialog();
                    if (notificationData.notification_id == null) {
                        notificationData = dbSelect.notificationDataToBeUpdated("UpdateNotification", spManager.getUserID(), "Course");
                    }
                    if (notificationData.notification_id != null) {
                        updateNotificationCount("Course", spManager.getUserID(), notificationData.notification_id, notificationData.notification_count);
                    }
                    if (completedCoursesList.size() > 0) {
                        syncSCORMApi();
                    } else {
                        getDiplomas();
                    }
                }
            } else if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (!syncApiCalled) {
                    showWaitDialog();
                    if (notificationData.notification_id == null) {
                        notificationData = dbSelect.notificationDataToBeUpdated("UpdateNotification", spManager.getUserID(), "Course");
                    }
                    if (notificationData.notification_id != null) {
                        updateNotificationCount("Course", spManager.getUserID(), notificationData.notification_id, notificationData.notification_count);
                    }

                    if (completedCoursesList.size() > 0) {
                        syncSCORMApi();
                    } else {
                        getDiplomas();
                    }
                }
            } else {
                int courseNotificationCount = notificationData.notification_count;
                String notificationIDs = notificationData.notification_id;
                if (courseNotificationCount != 0) {
                    dbDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "Course"});
                    if (dbSelect.getDataFromNotificationUpdateTable(spManager.getUserID(), "Course").size() == 0) {
                        NotificationProperty notificationInfo = new NotificationProperty();
                        notificationInfo.user_id = spManager.getUserID();
                        notificationInfo.notification_type = "Course";
                        notificationInfo.notification_count = courseNotificationCount;
                        notificationInfo.device_type = "android";
                        notificationInfo.device_id = deviceId;
                        notificationInfo.notification_id = notificationIDs;
                        dbInsert.addDataIntoNotificationUpdateTable(notificationInfo);
                    }
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - courseNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(Courses.this, total);
                }
                completedCoursesList.clear();
                completedCoursesList = dbSelect.getCoursesFromSCORM(spManager.getUserID(), "", "");
                showListOnCourse();
            }
        }
    };

    @Override
    protected void onStop() {
        dismissWaitDialog();
        isActivityLive = false;
        if (internetInfoReceiver != null) {
            if (internetInfoReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        super.onStop();
    }

    public void deleteCourseFileFromInternalStorage(String licenseID) {
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/" + licenseID);
            String filePath = root.getAbsolutePath();
            File file = new File(filePath);
            if (file.exists()) {
                DeleteCoursesFile(file);
            } else {
                coursesToBeDeleted.remove(0);
                if (coursesToBeDeleted.size() > 0) {
                    deleteCourseFileFromInternalStorage(coursesToBeDeleted.get(0).LicenceID);
                }
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    void DeleteCoursesFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteCoursesFile(child);
            }
        }
        fileOrDirectory.delete();
        coursesToBeDeleted.remove(0);
        if (coursesToBeDeleted.size() > 0) {
            deleteCourseFileFromInternalStorage(coursesToBeDeleted.get(0).LicenceID);
        }
    }

    public void writeNoMediaFile() {
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/");
            if (!root.exists()) {
                root.mkdirs();
            }
            String filePath = root.getAbsolutePath();
            File file = new File(filePath, ".nomedia");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    void DeleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteFile(child);
            }
        }
        fileOrDirectory.delete();
    }

    public void setLocale(String language) {
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public void showDownloadedPDFFile(final File pdfFile) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismissWaitDialog();
                showDiplomaPDF_File(pdfFile);
            }
        }.execute();
    }
}
