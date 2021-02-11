package com.elearn.trainor.Diploma;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.BaseAdapters.DiplomaRecyclerViewAdapter;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ComparatorHelperDiploma;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PDFView;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.PropertyClasses.DownloadUrlProperty;
import com.elearn.trainor.PropertyClasses.NotificationProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.services.network.HttpRequest;
import me.leolin.shortcutbadger.ShortcutBadger;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class Diploma extends AppCompatActivity implements View.OnClickListener {
    public static Diploma instance;
    LinearLayout ll_back, llhome;
    RelativeLayout tbl_actionbar;
    TextView text_header;
    SharedPreferenceManager spManager;
    List<DiplomaProperty> diplomaPropertyList;
    List<DiplomaProperty> activeDiploma, expiredDiploma, newlyCompletedList;
    RecyclerView expiredRecyclerView, activeRecyclerView;
    DiplomaRecyclerViewAdapter diplomaRecyclerViewAdapter;
    ConnectionDetector connectionDetector;
    ProgressDialog pDialog;
    DataBaseHandlerInsert dataBaseHandlerInsert;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    DataBaseHandlerUpdate dataBaseHandlerUpdate;
    DataBaseHandlerSelect dataBaseHandlerSelect;
    LinearLayout no_diploma_view, ll_moreCourses, expired_diploma_header, active_diploma_header;
    SwipeRefreshLayout swiperefresh;
    DiplomaProperty diplomaProperty;
    String diplomaPDF_FileName = "", diplomaSwitch_Status, NetworkMode = "", From = "", notificationID = "";
    LinearLayout when_diploma;
    List<DownloadUrlProperty> downloadDiplomaUrlList = new ArrayList<>(); // new list for download
    Long freeSpaceMB;
    IntentFilter internet_intent_filter;
    boolean isWindowActive = false;
    Dialog alertDialog;
    public static int totalURLIndex = 0;
    Dialog syncIncompleteDialog;
    FirebaseAnalytics analytics;
    Trace myTrace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diploma);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        isWindowActive = true;
        getControls();
        instance = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        myTrace = FirebasePerformance.getInstance().newTrace("Diploma_trace");
        myTrace.start();
        instance = this;
        isWindowActive = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //analytics.setCurrentScreen(this, "Diploma", this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        myTrace.stop();
        dismissWaitDialog();
        if (internetInfoReceiver != null) {
            if (internetInfoReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        isWindowActive = false;
        super.onStop();
    }

    public static Diploma getInstance() {
        if (instance == null) {
            instance = new Diploma();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        //analytics.setCurrentScreen(this, "Diploma Page", this.getClass().getSimpleName());
        dataBaseHandlerInsert = new DataBaseHandlerInsert(Diploma.this);
        dataBaseHandlerDelete = new DataBaseHandlerDelete(Diploma.this);
        dataBaseHandlerUpdate = new DataBaseHandlerUpdate(Diploma.this);
        dataBaseHandlerSelect = new DataBaseHandlerSelect(Diploma.this);
        connectionDetector = new ConnectionDetector(Diploma.this);
        spManager = new SharedPreferenceManager(Diploma.this);
        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
        diplomaPropertyList = new ArrayList<>();
        activeDiploma = new ArrayList<>();
        expiredDiploma = new ArrayList<>();
        newlyCompletedList = new ArrayList<>();
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        when_diploma = (LinearLayout) findViewById(R.id.when_diploma);
        ll_moreCourses = (LinearLayout) findViewById(R.id.ll_moreCourses);
        expiredRecyclerView = (RecyclerView) findViewById(R.id.expiredDiplomaRecyclerView);
        expiredRecyclerView.setNestedScrollingEnabled(false);
        activeRecyclerView = (RecyclerView) findViewById(R.id.activeDiplomaRecyclerView);
        activeRecyclerView.setNestedScrollingEnabled(false);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        text_header = (TextView) findViewById(R.id.text_header);
        no_diploma_view = (LinearLayout) findViewById(R.id.no_diploma_view);
        expired_diploma_header = (LinearLayout) findViewById(R.id.expired_diploma_header);
        active_diploma_header = (LinearLayout) findViewById(R.id.active_diploma_header);
        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        no_diploma_view.setVisibility(View.GONE);
        tbl_actionbar.setBackgroundColor(getResources().getColor(R.color.diploma));
        text_header.setText(getString(R.string.diploma));
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        ll_moreCourses.setOnClickListener(this);
        From = getIntent().getStringExtra("From");
        if (From != null && From.equals("Notification")) {
            notificationID = getIntent().getStringExtra("NotificationID");
        } else {
            From = "";
        }
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        freeSpaceMB = availableSpace / SIZE_MB;
        diplomaSwitch_Status = dataBaseHandlerSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DiplomaSwitchStatus", " ");
        diplomaPropertyList = dataBaseHandlerSelect.getDiplomaPageDetailsList("", spManager.getUserID());
        if (!connectionDetector.isConnectingToInternet()) {
            if (diplomaPropertyList.size() > 0) {
                no_diploma_view.setVisibility(View.GONE);
                when_diploma.setVisibility(View.VISIBLE);
                bindDataOnRecyclerView();
            } else {
                no_diploma_view.setVisibility(View.VISIBLE);
                when_diploma.setVisibility(View.GONE);
            }
        }
        swiperefresh.setColorSchemeResources(R.color.diploma);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    getDiplomas();
                } else {
                    swiperefresh.setRefreshing(false);
                    AlertDialogManager.showDialog(Diploma.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
        String CurrentLang = Locale.getDefault().getLanguage();
        String sharedLanguage = spManager.getLanguage();
        if (!sharedLanguage.equals(CurrentLang)) {
            setLocale(sharedLanguage);
        }
    }

    public void getDiplomas() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Diploma_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    activeDiploma.clear();
                    expiredDiploma.clear();
                    diplomaPropertyList.clear();
                    downloadDiplomaUrlList.clear();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                DiplomaProperty diplomaProperty = new DiplomaProperty();
                                diplomaProperty.userID = spManager.getUserID();
                                diplomaProperty.expiresDate = jsonObject.getString("expiresDate") == null ? "" : jsonObject.getString("expiresDate").equals("null") ? "" : jsonObject.getString("expiresDate");
                                diplomaProperty.certificateAvailable = jsonObject.getString("certificateAvailable") == null ? "" : jsonObject.getString("certificateAvailable").equals("null") ? "" : jsonObject.getString("certificateAvailable");
                                diplomaProperty.courseId = jsonObject.getString("courseId") == null ? "" : jsonObject.getString("courseId").equals("null") ? "" : jsonObject.getString("courseId");
                                diplomaProperty.licenseId = jsonObject.getString("licenseId") == null ? "" : jsonObject.getString("licenseId").equals("null") ? "" : jsonObject.getString("licenseId");
                                diplomaProperty.validUntil = jsonObject.getString("validUntil") == null ? "" : jsonObject.getString("validUntil").equals("null") ? "" : jsonObject.getString("validUntil");
                                diplomaProperty.startCourseUrl = jsonObject.getString("startCourseUrl") == null ? "" : jsonObject.getString("startCourseUrl").equals("null") ? "" : jsonObject.getString("startCourseUrl");
                                diplomaProperty.completionDate = jsonObject.getString("completionDate") == null ? "" : jsonObject.getString("completionDate").equals("null") ? "" : jsonObject.getString("completionDate");
                                diplomaProperty.language = jsonObject.getString("language") == null ? "" : jsonObject.getString("language").equals("null") ? "" : jsonObject.getString("language");
                                diplomaProperty.status = jsonObject.getString("status") == null ? "" : jsonObject.getString("status").equals("null") ? "" : jsonObject.getString("status");
                                diplomaProperty.startDate = jsonObject.getString("startDate") == null ? "" : jsonObject.getString("startDate").equals("null") ? "" : jsonObject.getString("startDate");
                                diplomaProperty.courseName = jsonObject.getString("courseName") == null ? "" : jsonObject.getString("courseName").equals("null") ? "" : jsonObject.getString("courseName");

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
                                    boolean isNewlyCompleted = dataBaseHandlerSelect.getCompletionDateDifference(completionDate);
                                    if (isNewlyCompleted) {
                                        diplomaProperty.isNewlyCompleted = "true";
                                    } else {
                                        diplomaProperty.isNewlyCompleted = "false";
                                    }
                                }
                                diplomaProperty.notToBeDeleted = "false";
                                //<------------------------------------------    End ------------------------------------------------------------------------->
                                /*if (!diplomaProperty.validUntil.equals("")) {
                                    Date validTill = df.parse(diplomaProperty.validUntil);
                                    diplomaProperty.validUntil = df.format(validTill);
                                    String todayDate = df.format(Calendar.getInstance().getTime());
                                    Date currentDate = df.parse(todayDate);
                                    if (validTill.equals("") || validTill.before(currentDate)) {
                                        diplomaProperty.diplomaStatus = "expired";
                                        expiredDiploma.add(diplomaProperty);
                                    } else {
                                        diplomaProperty.diplomaStatus = "active";
                                        activeDiploma.add(diplomaProperty);
                                        // new
                                        diplomaProperty.diplomaDownloadURL = WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language;
                                        DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                        downloadUrlProperty.downloadURL = diplomaProperty.diplomaDownloadURL;
                                        downloadUrlProperty.licenseId = diplomaProperty.licenseId;
                                        downloadDiplomaUrlList.add(downloadUrlProperty);
                                    }
                                } else {
                                    diplomaProperty.diplomaStatus = "expired";
                                    expiredDiploma.add(diplomaProperty);
                                }*/

                                // Changed on 27-04-2020
                                DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                if (!diplomaProperty.validUntil.equals("")) {
                                    Date validTill = df.parse(diplomaProperty.validUntil);
                                    diplomaProperty.validUntil = df.format(validTill);
                                    String todayDate = df.format(Calendar.getInstance().getTime());
                                    Date currentDate = df.parse(todayDate);
                                    if (validTill.equals("") || validTill.before(currentDate)) {
                                        diplomaProperty.diplomaStatus = "expired";
                                        expiredDiploma.add(diplomaProperty);
                                    } else {
                                        diplomaProperty.diplomaStatus = "active";
                                        activeDiploma.add(diplomaProperty);
                                        // new
                                        diplomaProperty.diplomaDownloadURL = WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language;
                                        downloadUrlProperty.downloadURL = diplomaProperty.diplomaDownloadURL;
                                        downloadUrlProperty.licenseId = diplomaProperty.licenseId;
                                        downloadDiplomaUrlList.add(downloadUrlProperty);
                                    }
                                } else {
                                    diplomaProperty.diplomaStatus = "active";
                                    activeDiploma.add(diplomaProperty);
                                    // new
                                    diplomaProperty.diplomaDownloadURL = WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language;
                                    downloadUrlProperty.downloadURL = diplomaProperty.diplomaDownloadURL;
                                    downloadUrlProperty.licenseId = diplomaProperty.licenseId;
                                    downloadDiplomaUrlList.add(downloadUrlProperty);
                                }
                            }
                            Collections.sort(activeDiploma, new ComparatorHelperDiploma());
                            Collections.reverse(activeDiploma);
                            diplomaPropertyList.addAll(activeDiploma);
                            Collections.sort(expiredDiploma, new ComparatorHelperDiploma());
                            Collections.reverse(expiredDiploma);
                            diplomaPropertyList.addAll(expiredDiploma);
                            dataBaseHandlerDelete.deleteTableByName("DiplomasTable", spManager.getUserID());
                            dataBaseHandlerInsert.addDataIntoDiplomasTable(diplomaPropertyList);
                            if (diplomaPropertyList.size() > 0) {
                                swiperefresh.setRefreshing(false);
                                no_diploma_view.setVisibility(View.GONE);
                                when_diploma.setVisibility(View.VISIBLE);
                            } else {
                                swiperefresh.setRefreshing(false);
                                no_diploma_view.setVisibility(View.VISIBLE);
                                when_diploma.setVisibility(View.GONE);
                            }
                            bindDataOnRecyclerView();
                        } else {
                            no_diploma_view.setVisibility(View.VISIBLE);
                            when_diploma.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception ex) {
                    Log.d("", ex.getMessage().toString());
                } finally {
                    if (connectionDetector.isConnectingToInternet() && diplomaSwitch_Status.equals("ON") && downloadDiplomaUrlList.size() > 0) {
                        swiperefresh.setRefreshing(false);
                        totalURLIndex = downloadDiplomaUrlList.size();
                        pDialog.setMessage(getResources().getString(R.string.storing) + " " + totalURLIndex + " " + getResources().getString(R.string.storingDiploma));
                        only_downloadPDF_File();
                    } else {
                        dismissWaitDialog();
                        swiperefresh.setRefreshing(false);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swiperefresh.setRefreshing(false);
                dismissWaitDialog();
                if (alertDialog == null) {
                    alertDialog = AlertDialogManager.showDialog(Diploma.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(Diploma.this, error), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            alertDialog = null;
                            bindDataOnRecyclerView();
                        }
                    });
                }
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(Diploma.this);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                onBackPressed();
                break;
            case R.id.ll_moreCourses:
                if (connectionDetector.isConnectingToInternet()) {
                    commonIntentMethod(WebActivityDiploma.class, null);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    AlertDialogManager.showDialog(Diploma.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
        }
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
        commonIntentMethod(HomePage.class, "Diploma");
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void commonIntentMethod(Class activity, String From) {
        Intent intent = new Intent(Diploma.this, activity);
        intent.putExtra("From", From);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void updateNotificationCount(final String NotificationType, final String UserID, final String notificationIDs) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationCount_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    int diplomaNotificationCount = Integer.parseInt(dataBaseHandlerSelect.getNotificationData("NotificationCountTable", "NotificationCount", "New Diploma", spManager.getUserID(), "NotificationCount"));
                    dataBaseHandlerDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "New Diploma"});
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - diplomaNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(Diploma.this, total);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    getDiplomas();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getDiplomas();
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(Diploma.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(Diploma.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (isWindowActive) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    public void bindDataOnRecyclerView() {
        activeDiploma.clear();
        expiredDiploma.clear();
        newlyCompletedList.clear();
        newlyCompletedList = dataBaseHandlerSelect.getDiplomaListToShow("newlyCompleted", spManager.getUserID());
        activeDiploma = dataBaseHandlerSelect.getDiplomaListToShow("active", spManager.getUserID());
        expiredDiploma = dataBaseHandlerSelect.getDiplomaListToShow("expired", spManager.getUserID());
        if (newlyCompletedList.size() > 0) {
            newlyCompletedList.addAll(activeDiploma);
            activeDiploma.clear();
            activeDiploma.addAll(newlyCompletedList);
        }
        if (expiredDiploma.size() > 0 && activeDiploma.size() > 0) {
            activeRecyclerView.setLayoutManager(new LinearLayoutManager(Diploma.this));
            diplomaRecyclerViewAdapter = new DiplomaRecyclerViewAdapter(Diploma.this, activeDiploma, null);
            activeRecyclerView.setAdapter(diplomaRecyclerViewAdapter);
            expiredRecyclerView.setLayoutManager(new LinearLayoutManager(Diploma.this));
            diplomaRecyclerViewAdapter = new DiplomaRecyclerViewAdapter(Diploma.this, null, expiredDiploma);
            expiredRecyclerView.setAdapter(diplomaRecyclerViewAdapter);
        } else if (expiredDiploma.size() > 0 && activeDiploma.size() == 0) {
            expiredRecyclerView.setLayoutManager(new LinearLayoutManager(Diploma.this));
            diplomaRecyclerViewAdapter = new DiplomaRecyclerViewAdapter(Diploma.this, null, expiredDiploma);
            expiredRecyclerView.setAdapter(diplomaRecyclerViewAdapter);
            active_diploma_header.setVisibility(View.GONE);
        } else if (expiredDiploma.size() == 0 && activeDiploma.size() > 0) {
            activeRecyclerView.setLayoutManager(new LinearLayoutManager(Diploma.this));
            diplomaRecyclerViewAdapter = new DiplomaRecyclerViewAdapter(Diploma.this, null, activeDiploma);
            activeRecyclerView.setAdapter(diplomaRecyclerViewAdapter);
            expired_diploma_header.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DiplomaPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWaitDialog();
                    downloadPDF_File();
                } else {
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(Diploma.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(Diploma.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", Diploma.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                            }
                        }, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    } else {
                        dismissWaitDialog();
                        AlertDialogManager.showCustomDialog(Diploma.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                                ActivityCompat.requestPermissions(Diploma.this, new String[]{permission}, requestCode);
                            }
                        }, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                            }
                        }, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                        dismissWaitDialog();
                    }
                }
                break;
        }
    }

    public void startDownloadingWithPermission(DiplomaProperty DipomaPDFInfo, String diplomaPDF_FileName) {
        this.diplomaProperty = DipomaPDFInfo;
        this.diplomaPDF_FileName = diplomaPDF_FileName;
        DiplomaPermissionsDispatcher.startDownloadingWithPermissionCheck(Diploma.this);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startDownloading() {
        showWaitDialog();
        downloadPDF_File();
    }

    public void showDiplomaPDF_File(File file) {
        isWindowActive = true;
        dismissWaitDialog();
        if (!file.exists()) {
            AlertDialogManager.showDialog(Diploma.this, getResources().getString(R.string.bad_file_format), getResources().getString(R.string.no_available_diploma_show), false, null);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("DiplomaView", "Yes");
            analytics.logEvent("DiplomaView", bundle);
            if (Build.MANUFACTURER.equals("Pixavi")) {
                Intent pdfViewIntent = new Intent(Diploma.this, PDFView.class);
                pdfViewIntent.putExtra("FileName", file.toString());
                pdfViewIntent.putExtra("CustomerID", "");
                pdfViewIntent.putExtra("PDFfileURL", "");
                pdfViewIntent.putExtra("FromAcitivity", "Diploma");
                pdfViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pdfViewIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                Diploma.this.finishAffinity();
                try {
                    dismissWaitDialog();
                    startActivity(pdfViewIntent);
                } catch (Exception e) {
                    dismissWaitDialog();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                Uri uri = FileProvider.getUriForFile(Diploma.this,
                        getString(R.string.file_provider_authority),
                        file);
                intent.setDataAndType(uri, "application/pdf");
                try {
                    dismissWaitDialog();
                    startActivity(intent);
                } catch (Exception e) {
                    dismissWaitDialog();
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


                  /*  File direct = new File(Environment.getExternalStorageDirectory() + "/MyTrainor/"+ spManager.getUserID() + "/.Diplomas/");
                    File pdfDirectory;
                    if (!direct.exists()) {
                        pdfDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/MyTrainor/"+ spManager.getUserID() + "/.Diplomas/");
                        pdfDirectory.mkdirs();
                    }
                    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/MyTrainor/"+ spManager.getUserID() + "/.Diplomas/", fileName + ".pdf");*/
                    File rootDir = Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getPath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/");
                    String filePath = root.getPath();
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
                    //dismissWaitDialog();
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
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/" + diplomaPDF_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDiplomaPDF_File(file);
            } else {
                if ((diplomaProperty.licenseId != null && !diplomaProperty.licenseId.equals("")) && (diplomaProperty.language != null && !diplomaProperty.language.equals(""))) {
                    downloadDiplomaFromServer(WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language, diplomaPDF_FileName);
                } else {
                    AlertDialogManager.showDialog(Diploma.this, getResources().getString(R.string.download_error), getResources().getString(R.string.no_diploma_available_downloaded), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            dismissWaitDialog();
                        }
                    });
                }
            }
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/" + diplomaPDF_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDiplomaPDF_File(file);
            } else {
                AlertDialogManager.showDialog(Diploma.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        dismissWaitDialog();
                    }
                });
            }
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

    public void only_downloadPDF_File() {
        if (connectionDetector.isConnectingToInternet()) {
            if (downloadDiplomaUrlList != null && downloadDiplomaUrlList.size() > 0) {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Diplomas/" + downloadDiplomaUrlList.get(0).licenseId + ".pdf");
                if (!file.exists()) {
                    if (50 <= freeSpaceMB) {
                        downloadFileFromServer(downloadDiplomaUrlList.get(0).downloadURL, downloadDiplomaUrlList.get(0).licenseId);
                    } else {
                        AlertDialogManager.showDialog(Diploma.this, getString(R.string.not_enough_space), " ", false, null);
                    }
                } else {
                    downloadDiplomaUrlList.remove(downloadDiplomaUrlList.get(0));
                    if (downloadDiplomaUrlList.size() > 0) {
                        only_downloadPDF_File();
                    } else {
                        dismissWaitDialog();
                    }
                }
            } else {
                dismissWaitDialog();
            }
        } else {
            AlertDialogManager.showDialog(Diploma.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(Diploma.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    public void downloadFileFromServer(final String fileURL, final String fileName) {
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
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (connectionDetector.isConnectingToInternet()) {
                    if (downloadDiplomaUrlList.size() > 0) {
                        downloadDiplomaUrlList.remove(downloadDiplomaUrlList.get(0));
                        if (downloadDiplomaUrlList.size() > 0) {
                            only_downloadPDF_File();
                        } else {
                            dismissWaitDialog();
                        }
                    } else {
                        dismissWaitDialog();
                    }
                } else {
                    dismissWaitDialog();
                    if (syncIncompleteDialog == null) {
                        syncIncompleteDialog = AlertDialogManager.showDialog(Diploma.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.sync_incomplete), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                syncIncompleteDialog = null;
                            }
                        });
                    }
                }
            }
        }.execute();
    }


   /* public static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            } else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut","Network is available : FALSE ");
        return false;
    }*/

    private BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            NotificationProperty notificationData = dataBaseHandlerSelect.notificationDataToBeUpdated("NotificationCountTable", spManager.getUserID(), "New Diploma");
            if ((netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) || (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                if (syncIncompleteDialog != null) {
                    syncIncompleteDialog.dismiss();
                    syncIncompleteDialog = null;
                }
                showWaitDialog();
                if (notificationData.notification_id == null) {
                    notificationData = dataBaseHandlerSelect.notificationDataToBeUpdated("UpdateNotification", spManager.getUserID(), "New Diploma");
                }
                if (notificationData.notification_id != null) {
                    updateNotificationCount("New Diploma", spManager.getUserID(), notificationData.notification_id);
                } else {
                    getDiplomas();
                }
            } else {
                dismissWaitDialog();
                int diplomaNotificationCount = notificationData.notification_count;
                String notificationIDs = notificationData.notification_id;
                if (diplomaNotificationCount != 0) {
                    dataBaseHandlerDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "New Diploma"});
                    if (dataBaseHandlerSelect.getDataFromNotificationUpdateTable(spManager.getUserID(), "New Diploma").size() == 0) {
                        NotificationProperty notificationInfo = new NotificationProperty();
                        notificationInfo.user_id = spManager.getUserID();
                        notificationInfo.notification_type = "New Diploma";
                        notificationInfo.notification_count = diplomaNotificationCount;
                        notificationInfo.device_type = "android";
                        notificationInfo.device_id = connectionDetector.getAndroid_ID(Diploma.this);
                        notificationInfo.notification_id = notificationIDs;
                        dataBaseHandlerInsert.addDataIntoNotificationUpdateTable(notificationInfo);
                    }
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - diplomaNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(Diploma.this, total);
                }
            }
        }
    };
}
