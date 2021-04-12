package com.elearn.trainor;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
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
import com.elearn.trainor.DashboardClasses.DashboardFragement;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.ExceptionHandler;
import com.elearn.trainor.HelperClasses.InternetConnectivityReceiver;
import com.elearn.trainor.HelperClasses.PicasoImageLoader;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.SettingModule.Settings;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kogitune.activity_transition.ActivityTransition;

import de.hdodenhof.circleimageview.CircleImageView;
import it.sephiroth.android.library.picasso.Callback;
import me.leolin.shortcutbadger.ShortcutBadger;

import com.elearn.trainor.DBHandler.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HomePage extends AppCompatActivity implements View.OnClickListener {
    public static HomePage instance;
    Fragment fragment;
    FragmentManager manager;
    public TextView txtProfile, txtUserName, txt_setting, txt_notification;
    public boolean isDashboarNotdAnimated = true;
    public CircleImageView profile_image;
    public SharedPreferenceManager spManager;
    private ProgressDialog pDialog;
    ConnectionDetector connectionDetector;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerInsert dbInsert;
    public LinearLayout ll_settings, ll_notifications;
    String totalNotification = "", category, body, notificationid;
    int courseCounts = 0, safetyCardCounts = 0, documentCounts = 0, newdiplomaCounts = 0, totalNotificationCount = 0;
    FirebaseAnalytics analytics;
    IntentFilter internet_intent_filter;
    InternetConnectivityReceiver internetConnectivityReceiver;
    List<String> customerIDList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_view_transform));
        }
        setContentView(R.layout.activity_home);
        spManager = new SharedPreferenceManager(HomePage.this);
        String lang = spManager.getLanguage();
        setLocale(lang);
        getControls();
        if (getIntent().getStringExtra("From") != null && getIntent().getStringExtra("From").equals("LoadingPage")) {
            if (spManager.getSharedPreferenceExistence()) {
                txtUserName.setText(spManager.getFirstname());
                if (spManager.getProfileURL() != null && !spManager.getProfileURL().equals("invalid URL")) {
                    PicasoImageLoader.getImagesFromURL(HomePage.this, spManager.getProfileURL(), profile_image, 500, 500, new Callback() {
                        @Override
                        public void onSuccess() {
                            addFragment("Yes");
                        }

                        @Override
                        public void onError() {

                        }
                    });
                } else {
                    addFragment("Yes");
                }
            }
            if (Build.VERSION.SDK_INT < 21) {
                ActivityTransition.with(getIntent()).to(findViewById(R.id.profile_image)).start(savedInstanceState);
            }
        } else {
            addFragment("No");
            if (spManager.getSharedPreferenceExistence()) {
                txtUserName.setText(spManager.getFirstname());
                if (spManager.getProfileURL() != null && !spManager.getProfileURL().equals("invalid URL")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        PicasoImageLoader.getImagesFromURL(HomePage.this, spManager.getProfileURL(), profile_image, 500, 500, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                addFragment("Yes");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                PicasoImageLoader.setOfflineImage(HomePage.this, spManager.getProfileURL(), profile_image, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        profile_image.setImageResource(R.drawable.ic_default_profile_pic);
                                    }
                                });
                            }
                        });
                    }
                }
            } else {
                profile_image.setImageResource(R.drawable.ic_default_profile_pic);
            }
        }
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //analytics.setCurrentScreen(this, "HomeScreen", this.getClass().getSimpleName());
        if (!connectionDetector.isConnectingToInternet()) {
            if (!spManager.getProfileURL().equals("invalid URL")) {
                PicasoImageLoader.setOfflineImage(HomePage.this, spManager.getProfileURL(), profile_image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        profile_image.setImageResource(R.drawable.ic_default_profile_pic);
                    }
                });
            }
        }
    }

    public static HomePage getInstance() {
        if (instance == null) {
            instance = new HomePage();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        dbUpdate = new DataBaseHandlerUpdate(this);
        dbSelect = new DataBaseHandlerSelect(this);
        dbDelete = new DataBaseHandlerDelete(this);
        dbInsert = new DataBaseHandlerInsert(this);
        connectionDetector = new ConnectionDetector(HomePage.this);
        customerIDList = new ArrayList();

        manager = getSupportFragmentManager();
        txt_notification = (TextView) findViewById(R.id.txt_notification);
        ll_notifications = (LinearLayout) findViewById(R.id.ll_notifications);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtProfile = (TextView) findViewById(R.id.txtProfile);
        txt_setting = (TextView) findViewById(R.id.txt_setting);
        profile_image = (CircleImageView) findViewById(R.id.circleImageView);
        ll_settings = (LinearLayout) findViewById(R.id.ll_settings);
        dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "isDownloadedCompletely", "Yes");
        dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "isLoggedIn", "Yes");
        profile_image.setOnClickListener(this);
        txtUserName.setOnClickListener(this);
        txtProfile.setOnClickListener(this);
        ll_settings.setOnClickListener(this);
        ll_notifications.setOnClickListener(this);
        if (connectionDetector.isConnectingToInternet()) {
            getAllPendingNotification();
        } else {
            totalNotification = spManager.getTotalNotificationCount();
            if (!totalNotification.equals("") && !totalNotification.equals("0")) {
                try {
                    ll_notifications.setVisibility(View.VISIBLE);
                    if (totalNotification.equals("1")) {
                        txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_singular));
                    } else {
                        txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_plural));
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            } else {
                ll_notifications.setVisibility(View.GONE);
            }
        }

        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        internetConnectivityReceiver = new InternetConnectivityReceiver();
        this.registerReceiver(internetConnectivityReceiver, internet_intent_filter);

    }

    public void addFragment(String showAnimation) {
        try {
            if (isDashboarNotdAnimated) {
                fragment = DashboardFragement.getInstance();
                Bundle bundle = new Bundle();
                bundle.putString("ShowAnimation", showAnimation);
                fragment.setArguments(bundle);
                String backStateName = fragment.getClass().getName();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.llFragment, fragment);
                ft.addToBackStack(backStateName);
                ft.commit();
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getStringExtra("From") != null && !getIntent().getStringExtra("From").equals("LoadingPage")) {
            showWaitDialog();
            if (spManager.getSharedPreferenceExistence()) {
                txtUserName.setText(spManager.getFirstname());
                if (spManager.getProfileURL() != null && !spManager.getProfileURL().equals("invalid URL")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        getUserImageFromLive(spManager.getProfileURL());
                    } else {
                        dismissWaitDialog();
                    }
                } else {
                    dismissWaitDialog();
                }
            } else {
                dismissWaitDialog();
            }
        }

        if (connectionDetector.isConnectingToInternet()) {
            getAllPendingNotification();
        } else {
            totalNotification = spManager.getTotalNotificationCount();
            if (!totalNotification.equals("") && !totalNotification.equals("0")) {
                try {
                    ll_notifications.setVisibility(View.VISIBLE);
                    if (totalNotification.equals("1")) {
                        txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_singular));
                    } else {
                        txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_plural));
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            } else {
                ll_notifications.setVisibility(View.GONE);
            }
        }
    }

    public void getUserImageFromLive(final String ImageUrl) {
        PicasoImageLoader.getImagesFromURL(HomePage.this, ImageUrl, profile_image, 110, 110, new Callback() {
            @Override
            public void onSuccess() {
                dismissWaitDialog();
            }

            @Override
            public void onError() {
                dismissWaitDialog();
                profile_image.setImageResource(R.drawable.ic_default_profile_pic);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.circleImageView:
                commonIntentMethod(Settings.class);
                break;
            case R.id.txtProfile:
                commonIntentMethod(Settings.class);
                break;
            case R.id.txtUserName:
                Intent intent2 = new Intent(HomePage.this, Settings.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.ll_settings:
                Intent intent = new Intent(HomePage.this, Settings.class);
                intent.putExtra("From", "");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.ll_notifications:
                Intent intentNotification = new Intent(HomePage.this, HomeNotification.class);
                intentNotification.putExtra("From", "");
                intentNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentNotification);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(HomePage.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (pDialog != null)
            if (pDialog.isShowing())
                pDialog.dismiss();
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(HomePage.this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void getAllPendingNotification() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.GetAllPendingNotification_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                dbDelete.deleteValueFromTable("NotificationCountTable", "userID", spManager.getUserID());
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            JSONArray jsonArray = jsonObject.getJSONArray("PendingNotification");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    category = jsonObject1.getString("notificationType");
                                    body = jsonObject1.getString("notificationMessage");
                                    notificationid = jsonObject1.getString("notificationID");
                                    JSONArray jsonArray2 = jsonObject.getJSONArray("NotificationCount");
                                    if (jsonArray2 != null && jsonArray2.length() > 0) {
                                        for (int j = 0; j < jsonArray2.length(); j++) {
                                            JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                            courseCounts = jsonObject2.getInt("CourseCount");
                                            safetyCardCounts = jsonObject2.getInt("SafetyCardCount");
                                            documentCounts = jsonObject2.getInt("DocumentCount");
                                            newdiplomaCounts = jsonObject2.getInt("DiplomaCount");
                                            totalNotificationCount = jsonObject2.getInt("totalNotification");
                                        }
                                    }
                                    if (dbSelect.isNotificationIDExists(spManager.getUserID(), notificationid) == false) {
                                        addNotification(courseCounts, newdiplomaCounts, safetyCardCounts, documentCounts, body, category, notificationid);
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.d("Error", e.getMessage());
                } finally {
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", totalNotificationCount + "");
                    editor.commit();
                    totalNotification = spManager.getTotalNotificationCount();
                    if (!totalNotification.equals("") && !totalNotification.equals("0")) {
                        try {
                            ll_notifications.setVisibility(View.VISIBLE);
                            if (totalNotification.equals("1")) {
                                txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_singular));
                            } else {
                                txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_plural));
                            }
                        } catch (Exception ex) {
                            Log.d("Error", ex.getMessage());
                        }
                    } else {
                        ll_notifications.setVisibility(View.GONE);
                    }
                    if (totalNotificationCount != 0) {
                        ShortcutBadger.applyCount(HomePage.this, totalNotificationCount);
                    } else {
                        ShortcutBadger.applyCount(HomePage.this, 0);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                totalNotification = spManager.getTotalNotificationCount();
                if (!totalNotification.equals("") && !totalNotification.equals("0")) {
                    try {
                        ll_notifications.setVisibility(View.VISIBLE);
                        if (totalNotification.equals("1")) {
                            txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_singular));
                        } else {
                            txt_notification.setText(Integer.parseInt(totalNotification) + " " + getResources().getString(R.string.new_plural));
                        }
                    } catch (Exception ex) {
                        Log.d("Error", ex.getMessage());
                    }
                } else {
                    ll_notifications.setVisibility(View.GONE);
                }
                if (totalNotificationCount != 0) {
                    ShortcutBadger.applyCount(HomePage.this, totalNotificationCount);
                } else {
                    ShortcutBadger.applyCount(HomePage.this, 0);
                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"userID\":\"" + spManager.getUserID() + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void addNotification(int courseCount, int newDiplomaCount, int safetyCardCount,
                                int documentCount, String body, String category, String notificationid) {
        if (category.equals("ClassRoomCourse") || category.equals("E-Learning Course")) {
            dbInsert.addDataIntoNotificationCountTable("Course", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("Course", courseCount + "", spManager.getUserID());
        }
        if (category.equals("Safety card")) {
            dbInsert.addDataIntoNotificationCountTable("SafetyCard", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("SafetyCard", safetyCardCount + "", spManager.getUserID());
        }
        if (category.equals("New Diploma")) {
            dbInsert.addDataIntoNotificationCountTable("New Diploma", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("New Diploma", newDiplomaCount + "", spManager.getUserID());
        }
        if (category.equals("Documents")) {
            dbInsert.addDataIntoNotificationCountTable("New Document", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("New Document", documentCount + "", spManager.getUserID());
        }
    }

    public void callRunOnUiThreadInServices(final int totalNotificationCount) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ll_notifications != null) {
                        ll_notifications.setVisibility(View.VISIBLE);
                        if (totalNotificationCount == 1) {
                            txt_notification.setText(totalNotificationCount + " " + getResources().getString(R.string.new_singular));
                        } else {
                            txt_notification.setText(totalNotificationCount + " " + getResources().getString(R.string.new_plural));
                        }
                    }
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setLocale(String language) {
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }


    public void callSaveCustomerIdToAccessCOP() {
        getSaveCustomerID(spManager.getToken());
    }

    public void getSaveCustomerID(final String token) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.CUSTOMERS_DETAIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        customerIDList.clear();
                        dbDelete.deleteTableByName("CustomerDetails", "");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CustomerDetailsProperty info = new CustomerDetailsProperty();
                            info.customer_id = jsonObject.getString("customerId") == null ? "" : jsonObject.getString("customerId").equals("") ? "" : jsonObject.getString("customerId");
                            info.customerName = jsonObject.getString("customerName") == null ? "" : jsonObject.getString("customerName").equals("") ? "" : jsonObject.getString("customerName");
                            info.workEmailAddress = jsonObject.getString("workEmailAddress") == null ? "" : jsonObject.getString("workEmailAddress").equals("") ? "" : jsonObject.getString("workEmailAddress");
                            info.departmentName = jsonObject.getString("departmentName") == null ? "" : jsonObject.getString("departmentName").equals("") ? "" : jsonObject.getString("departmentName");
                            info.employeeNumber = jsonObject.getString("employeeNumber") == null ? "" : jsonObject.getString("employeeNumber").equals("") ? "" : jsonObject.getString("employeeNumber");
                            info.title = jsonObject.getString("title") == null ? "" : jsonObject.getString("title").equals("") ? "" : jsonObject.getString("title");
                            info.workPhone = jsonObject.getString("workPhone") == null ? "" : jsonObject.getString("workPhone").equals("") ? "" : jsonObject.getString("workPhone");
                            info.hasCopAccess = jsonObject.getString("hasCopAccess") == null ? "" : jsonObject.getString("hasCopAccess").equals("") ? "" : jsonObject.getString("hasCopAccess");
                            //added 3 field 03-09-2020
                            info.emailVerified = jsonObject.getString("emailVerified") == null ? "" : jsonObject.getString("emailVerified").equals("") ? "" : jsonObject.getString("emailVerified");
                            info.phoneVerified = jsonObject.getString("phoneVerified") == null ? "" : jsonObject.getString("phoneVerified").equals("") ? "" : jsonObject.getString("phoneVerified");
                            info.isPrivate = jsonObject.getString("isPrivate") == null ? "" : jsonObject.getString("isPrivate").equals("") ? "" : jsonObject.getString("isPrivate");

                            dbInsert.addDataIntoCustomerDetailsTable(info);
                            customerIDList.add(info.customer_id);
                        }
                    }
                } catch (Exception ex) {
                    ExceptionHandler.getErrorMessage(HomePage.this, ex);
                }finally {
                    callgetActiveEntryAPI(); //new added 30-09-2020
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callgetActiveEntryAPI(); //new added 30-09-2020
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        requestQueue11.add(stringRequest);
    }

    public void callgetActiveEntryAPI() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.GetActiveEntries, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        dbDelete.deleteTableByName("ReportEntry", "");
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                ReportEntryProperty property = new ReportEntryProperty();
                                property.userId = spManager.getUserID();
                                property.entryId = jsonObject.getString("id");
                                property.checkOutMessage = jsonObject.getString("checkOutMessage");
                                property.timestamp = jsonObject.getString("timestamp");
                                property.state = jsonObject.getString("state");
                                property.numberOfGuests = jsonObject.getString("numberOfGuests");
                                property.employeeId = jsonObject.getString("employeeId");
                                property.securityServicePhone = jsonObject.getString("securityServicePhone");
                                property.safetycardId = jsonObject.getString("safetycardId");
                                property.facilityName = jsonObject.getString("facilityName");
                                property.facilityId = jsonObject.getString("facilityId");
                                property.estimatedDurationOfVisitInSeconds = jsonObject.getString("estimatedDurationOfVisitInSeconds");
                                property.facilityLatitude = jsonObject.getString("facilityLatitude");
                                property.facilityLongitude = jsonObject.getString("facilityLongitude");
                                dbInsert.addDataIntoReportEntryTable(property);
                            }
                        }

                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               /* int statuscode = error.networkResponse.statusCode;
                if (statuscode == 403 || statuscode == 404) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        String message = data.getString("message");
                    } catch (JSONException | UnsupportedEncodingException e) {
                        Log.d("Exception: ", Objects.requireNonNull(e.getMessage()));
                    }
                }*/
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                params.put("Accept", "application/json");
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }
}
