package com.elearn.trainor.DashboardClasses;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.*;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HelperClasses.*;
import com.elearn.trainor.Login;
import com.elearn.trainor.R;
import com.elearn.trainor.SplashActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kogitune.activity_transition.ActivityTransitionLauncher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.services.network.HttpRequest;
import it.sephiroth.android.library.picasso.Callback;
import me.leolin.shortcutbadger.ShortcutBadger;

import com.elearn.trainor.DBHandler.*;

public class LoadingPageActivity extends AppCompatActivity {
    CircleImageView circleImageView;
    TextView txtWelcome;
    TextView txtUserName;
    TextView txtMessage;
    CardView cardView;
    public boolean isAnimationStarted = true;
    public boolean isActivityVisible = false;
    public boolean isProcessCompleted = false;
    SharedPreferenceManager spManager;
    Locale myLocale;
    DataBaseHandlerInsert dataBaseHandlerInsert;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    String token, FirebaseToken, address, postalCode, country, city, diplomaSwitch_status, safetyCradSwitch_status, toolsSwitch_status,
            companyDocumentSwitch_status, documentIDsToBeDeleted = "", loginforStattent;
    ConnectionDetector connectionDetector;
    final Handler handler = new Handler();
    List<SafetyCardProperty> safetyCardListForRecyclerView;
    List<SafetyCardProperty> approvedCardList;
    List<SafetyCardProperty> unApprovedCardList;
    List<SafetyCardProperty> nullValidToApprovedList;
    List<DiplomaProperty> diplomaPropertyList, activeDiploma, expiredDiploma, eLearningCourseList, classRoomCourseList, blankDateDiploma;
    List<String> courseIDList = new ArrayList<>();
    List<String> customerIDList = new ArrayList<>();
    List<DownloadUrlProperty> downloadUrlList = new ArrayList<>(); // new list for download
    Boolean clearListTag = true;
    List<ToolsProperty> toolsList;
    public static int currentFileIndex = 0;
    public static int totalURLIndex = 0;
    FirebaseAnalytics firebaseAnalytics;
    List<CustomerDetailsProperty> copUserList;

    //cop
    SharedPreferences.Editor editor, COPfacilityEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_view_transform));
        }
        isActivityVisible = true;
        setContentView(R.layout.activity_loading_page);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getControls();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getSaveCustomerID(getIntent().getStringExtra("token"));
                getUserDetails();
            }
        }, 300);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityVisible = true;
        if (isProcessCompleted) {
            executeAsynTask();
        }
    }

    @Override
    protected void onStop() {
        isActivityVisible = false;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAnalytics.setCurrentScreen(LoadingPageActivity.this, "LoadingPage", this.getClass().getSimpleName());

    }

    public void showCircleImageViewAnimation() {
        txtMessage.setText(getResources().getString(R.string.sync_courses));
        if (isAnimationStarted && isActivityVisible) {
            isAnimationStarted = false;
            Animation bottomUp = AnimationUtils.loadAnimation(LoadingPageActivity.this, R.anim.bottom_up_anim);
            circleImageView.setAnimation(bottomUp);
            circleImageView.setVisibility(View.VISIBLE);
            bottomUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    txtMessage.setText(getResources().getString(R.string.sync_courses));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    getUpcomingCourse();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            bottomUp.start();
        }
    }

    public void getControls() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(LoadingPageActivity.this);
        connectionDetector = new ConnectionDetector(LoadingPageActivity.this);
        dbSelect = new DataBaseHandlerSelect(LoadingPageActivity.this);
        dataBaseHandlerDelete = new DataBaseHandlerDelete(LoadingPageActivity.this);
        dataBaseHandlerInsert = new DataBaseHandlerInsert(LoadingPageActivity.this);
        spManager = new SharedPreferenceManager(LoadingPageActivity.this);
        editor = spManager.COPSharedPreference();
        COPfacilityEditor = spManager.COPFacilityPreference();
        toolsList = new ArrayList<>();
        FirebaseToken = spManager.getFirebaseToken();
        if (FirebaseToken.equals("")) {
            getFirebaseToken();
        }
        diplomaPropertyList = new ArrayList<>();
        activeDiploma = new ArrayList<>();
        expiredDiploma = new ArrayList<>();
        safetyCardListForRecyclerView = new ArrayList<>();
        approvedCardList = new ArrayList<>();
        nullValidToApprovedList = new ArrayList<>();
        unApprovedCardList = new ArrayList<>();
        eLearningCourseList = new ArrayList<>();
        classRoomCourseList = new ArrayList<>();
        blankDateDiploma = new ArrayList<>();
        dbUpdate = new DataBaseHandlerUpdate(LoadingPageActivity.this);
        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        circleImageView = (CircleImageView) findViewById(R.id.circleImageView);
        cardView = (CardView) findViewById(R.id.card_view);
        circleImageView.setVisibility(View.GONE);
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPageHeader));
        token = getIntent().getStringExtra("token").toString();
        if (getIntent().getStringExtra("LoginType") != null && getIntent().getStringExtra("LoginType").equals("StattnetLogin")) {
            loginforStattent = getIntent().getStringExtra("LoginType");
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void getUserDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.FetchDetail_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("profilePictureUrl").toString() != null && !jsonObject.getString("profilePictureUrl").equals("")) {
                        String username = getIntent().getStringExtra("username");
                        getUserImageFromLive(jsonObject.getString("profilePictureUrl"), username,
                                jsonObject.getString("emailAddress"), jsonObject.getString("phone"), jsonObject.getString("birthDate"), jsonObject.getString("language"), jsonObject.getString("firstname"), jsonObject.getString("lastname"), jsonObject.getString("id"));
                        updateFirebaseTokenOnServer(jsonObject.getString("id"), "not applicable", connectionDetector.getAndroid_ID(LoadingPageActivity.this), FirebaseToken, "android", "insert", jsonObject.getString("language"));
                    } else {
                        updateFirebaseTokenOnServer(jsonObject.getString("id"), "not applicable", connectionDetector.getAndroid_ID(LoadingPageActivity.this), FirebaseToken, "android", "insert", jsonObject.getString("language"));
                        String firstName = jsonObject.getString("firstname") == null ? "" : jsonObject.getString("firstname").equals("null") ? "" : jsonObject.getString("lastname");
                        String lastName = jsonObject.getString("lastname") == null ? "" : jsonObject.getString("lastname").equals("null") ? "" : jsonObject.getString("lastname");
                        String lang = jsonObject.getString("language") == null ? "en" : jsonObject.getString("language").equals("null") ? "en" : jsonObject.getString("language");
                        if (lang.startsWith("nb")) {
                            lang = "nb";
                        } else if (lang.startsWith("en")) {
                            lang = "en_US";
                        } else if (lang.startsWith("ko")) {
                            lang = "ko_KR";
                        } else if (lang.startsWith("pl")) {
                            lang = "pl_PL";
                        } else if (lang.startsWith("sv")) {
                            lang = "sv_SE";
                        } else if (lang.startsWith("pt")) {
                            lang = "pt_BR";
                        }
                        saveSharedPreferenceValues("", firstName, lastName, jsonObject.getString("emailAddress") == null ? "" : jsonObject.getString("emailAddress").equals("null") ? "" : jsonObject.getString("emailAddress"), jsonObject.getString("birthDate") == null ? "" : jsonObject.getString("birthDate").equals("null") ? "" : jsonObject.getString("birthDate"), lang, jsonObject.getString("phone") == null ? "" : jsonObject.getString("phone").equals("null") ? "" : jsonObject.getString("phone"), (firstName + " " + lastName), jsonObject.getString("id"));
                        txtWelcome.setText(getResources().getString(R.string.welcome_loading));
                        txtUserName.setText(jsonObject.getString("firstname"));
                        showCircleImageViewAnimation();
                    }
                } catch (Exception ex) {
                    Log.d("Exception Meaasge", ex.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(LoadingPageActivity.this, error), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void getSafetyCards() {
        safetyCradSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "SafetyCardSwitchStatus", " ");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Upcoming_SafetyCards_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (safetyCradSwitch_status.equals("ON")) {
                    txtMessage.setText(getResources().getString(R.string.downloading_safetyCard));
                } else {
                    txtMessage.setText(getResources().getString(R.string.sync_safety_card));
                }
                safetyCardListForRecyclerView.clear();
                approvedCardList.clear();
                unApprovedCardList.clear();
                nullValidToApprovedList.clear();
                downloadUrlList.clear();
                currentFileIndex = 0;
                totalURLIndex = 0;
                if (response != null && !response.equals("")) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                SafetyCardProperty property = new SafetyCardProperty();
                                property.valid_to = jsonObject.getString("validTo");
                                property.card_id = jsonObject.getString("cardId");
                                property.valid_from = jsonObject.getString("validFrom");
                                property.company_name = jsonObject.getString("companyName");
                                property.approval_status = jsonObject.getString("approved");
                                property.location_name = jsonObject.getString("locationName");
                                property.active_status = jsonObject.getString("active");
                                property.id = jsonObject.getString("id");
                                property.employeeId = jsonObject.getString("employeeId");
                                property.card_url = jsonObject.getString("downloadUrl");
                                property.customerId = jsonObject.getString("customerId");
                                if (property.approval_status != null && property.approval_status.equals("true")) {
                                    approvedCardList.add(property);
                                    DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                    downloadUrlProperty.downloadURL = property.card_url;
                                    downloadUrlProperty.safetyCard_cardID = property.card_id;
                                    downloadUrlList.add(downloadUrlProperty);
                                } else {
                                    unApprovedCardList.add(property);
                                }
                            }
                            Collections.sort(approvedCardList, new ComparatorHelperSafetyCards());
                            Collections.reverse(approvedCardList);
                            safetyCardListForRecyclerView.addAll(approvedCardList);
                            safetyCardListForRecyclerView.addAll(nullValidToApprovedList);
                            safetyCardListForRecyclerView.addAll(unApprovedCardList);
                            dataBaseHandlerDelete.deleteTableByName("SafetyCards", "");
                            dataBaseHandlerInsert.addDataIntoSafetyCardTable(safetyCardListForRecyclerView);
                        }
                    } catch (Exception ex) {
                        if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                            backToSplashScreen();
                        } else {
                            AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.safety_card_exception), ex.getMessage().toString(), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    } finally {
                        if (safetyCradSwitch_status.equals("ON") && downloadUrlList != null && downloadUrlList.size() > 0) {
                            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
                            DeleteFile(file);
                            totalURLIndex = downloadUrlList.size();
                            downloadFile("/.SafetyCards/", downloadUrlList.get(0).safetyCard_cardID);
                        } else {
                            if (customerIDList != null && customerIDList.size() > 0) {
                                getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                            } else {
                                //executeAsynTask(); Lov Tyagi
                            }
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void getDiplomas() {
        diplomaSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DiplomaSwitchStatus", " ");
        if (diplomaSwitch_status.equals("ON")) {
            txtMessage.setText(getResources().getString(R.string.sync_diploma));
        } else {
            txtMessage.setText(getResources().getString(R.string.syncronizing_diploma));
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Diploma_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    activeDiploma.clear();
                    expiredDiploma.clear();
                    diplomaPropertyList.clear();
                    blankDateDiploma.clear();
                    downloadUrlList.clear();
                    currentFileIndex = 0;
                    totalURLIndex = 0;
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
                                diplomaProperty.availableOffline = jsonObject.getString("availableOffline") == null ? "" : jsonObject.getString("availableOffline").equals("null") ? "" : jsonObject.getString("availableOffline");
                                if (jsonObject.getString("duration") != null && !jsonObject.getString("duration").equals("null")) {
                                    String Duration = jsonObject.getString("duration");
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
                                        diplomaProperty.courseDuration = firstToken + " " + showDays;
                                    } else {
                                        if (!showHours.equals("")) {
                                            diplomaProperty.courseDuration = secondToken + " " + showHours;
                                        }
                                        if (!showMinutes.equals("")) {
                                            if (diplomaProperty.courseDuration != null && !showHours.equals("")) {
                                                diplomaProperty.courseDuration += " " + thirdToken + " " + showMinutes;
                                            } else {
                                                diplomaProperty.courseDuration = thirdToken + " " + showMinutes;
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
                                if (jsonObject.getString("location") != null && !jsonObject.getString("location").equals("null")) {
                                    JSONObject jsonObjectlLOC = jsonObject.getJSONObject("location");
                                    if (jsonObjectlLOC != null) {
                                        if (jsonObjectlLOC.has("city")) {
                                            city = jsonObjectlLOC.getString("city") == null ? "" : jsonObjectlLOC.getString("city").equals("null") ? "" : jsonObjectlLOC.getString("city");
                                            diplomaProperty.courseCity = city;
                                        }
                                    }
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
                                    diplomaProperty.location = address + ", " + postalCode + ", " + city + ", " + country;
                                } else {
                                    diplomaProperty.location = "";
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

                                /*if (!diplomaProperty.validUntil.equals("")) {
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
                                        // new
                                        diplomaProperty.diplomaDownloadURL = WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language;
                                        DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                        downloadUrlProperty.downloadURL = diplomaProperty.diplomaDownloadURL;
                                        downloadUrlProperty.licenseId = diplomaProperty.licenseId;
                                        downloadUrlList.add(downloadUrlProperty);
                                    }
                                } else {
                                    diplomaProperty.diplomaStatus = "expired";
                                    blankDateDiploma.add(diplomaProperty);
                                }*/
// changed on 27-04-2020
                                DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
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
                                        // new
                                        diplomaProperty.diplomaDownloadURL = WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language;
                                        downloadUrlProperty.downloadURL = diplomaProperty.diplomaDownloadURL;
                                        downloadUrlProperty.licenseId = diplomaProperty.licenseId;
                                        downloadUrlList.add(downloadUrlProperty);
                                    }
                                } else {
                                    diplomaProperty.diplomaStatus = "active";
                                    activeDiploma.add(diplomaProperty);

                                    diplomaProperty.diplomaDownloadURL = WebServicesURL.Course_Completion_URL + diplomaProperty.licenseId + "/" + diplomaProperty.language;
                                    downloadUrlProperty.downloadURL = diplomaProperty.diplomaDownloadURL;
                                    downloadUrlProperty.licenseId = diplomaProperty.licenseId;
                                    downloadUrlList.add(downloadUrlProperty);
                                }
                            }
                            diplomaPropertyList.addAll(activeDiploma);
                            diplomaPropertyList.addAll(expiredDiploma);
                            Collections.sort(diplomaPropertyList, new ComparatorHelperDiploma());
                            Collections.reverse(diplomaPropertyList);
                            diplomaPropertyList.addAll(blankDateDiploma);
                            long result = dataBaseHandlerDelete.deleteTableByName("DiplomasTable", spManager.getUserID());
                            dataBaseHandlerInsert.addDataIntoDiplomasTable(diplomaPropertyList);
                        }
                    }
                } catch (Exception ex) {
                    if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                        backToSplashScreen();
                    } else {
                        AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.diploma_not_found), ex.getMessage().toString(), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                } finally {
                    if (diplomaSwitch_status.equals("ON") && downloadUrlList.size() > 0 && downloadUrlList != null) {
                        totalURLIndex = downloadUrlList.size();
                        downloadFile("/.Diplomas/", downloadUrlList.get(0).licenseId);
                    } else {
                        getSafetyCards();
                        //getDSBDetails();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void downloadFile(final String module, String fileName) {
        if (connectionDetector.isConnectingToInternet()) {
            if (downloadUrlList != null && downloadUrlList.size() > 0) {
                currentFileIndex += 1;

                if (module.equals("/.Diplomas/")) {
                    txtMessage.setText(getResources().getString(R.string.getting_diploma) + " " + currentFileIndex + " " + getResources().getString(R.string.of) + " " + totalURLIndex);
                } else if (module.equals("/.SafetyCards/")) {
                    txtMessage.setText(getResources().getString(R.string.getting_safetycard) + " " + currentFileIndex + " " + getResources().getString(R.string.of) + " " + totalURLIndex);
                } else if (module.equals("/.MyCompany/")) {
                    txtMessage.setText(getResources().getString(R.string.getting_document) + " " + currentFileIndex + " " + getResources().getString(R.string.of) + " " + totalURLIndex);
                } else if (module.equals("/.tools/")) {
                    txtMessage.setText(getResources().getString(R.string.getting_tools) + " " + currentFileIndex + " " + getResources().getString(R.string.of) + " " + totalURLIndex);
                }

                File file;
                if (module.equals("/.MyCompany/")) {
                    file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + module + fileName);
                } else if (module.equals("/.tools/")) {
                    file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/.tools/" + fileName);
                } else {
                    file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + module + fileName + ".pdf");
                }

                if (!file.exists()) {
                    downloadFileFromServer(downloadUrlList.get(0).downloadURL, fileName, module);
                } else {
                    if (module.equals("/.tools/")) {
                        ToolsProperty info = new ToolsProperty();
                        info.id = downloadUrlList.get(0).licenseId;
                        dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                    }
                    downloadUrlList.remove(downloadUrlList.get(0));
                    if (module.equals("/.Diplomas/") && downloadUrlList != null && downloadUrlList.size() > 0) {
                        fileName = downloadUrlList.get(0).licenseId;
                    } else if ((module.equals("/.SafetyCards/") || module.equals("/.MyCompany/") || module.equals("/.tools/")) && downloadUrlList != null && downloadUrlList.size() > 0) {
                        fileName = downloadUrlList.get(0).safetyCard_cardID;
                    }
                    if (downloadUrlList.size() > 0) {
                        downloadFile(module, fileName);
                    } else {
                        if (module.equals("/.Diplomas/")) {
                            getSafetyCards();
                        } else if (module.equals("/.SafetyCards/")) {
                            if (customerIDList != null && customerIDList.size() > 0) {
                                getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                            }
                        } else if (module.equals("/.MyCompany/")) {
                            getTools();
                        } else {
                            // 14-01-2020
                            if (hasCopAccess()) {
                                getPsiCourseCompletion();
                            } else {
                                txtMessage.setText(getResources().getString(R.string.ready));
                                isProcessCompleted = true;
                                executeAsynTask();
                            }
                            /*txtMessage.setText(getResources().getString(R.string.ready));
                            isProcessCompleted = true;
                            executeAsynTask();*/
                        }
                    }
                }
            } else {
                currentFileIndex = 0;
                totalURLIndex = 0;
                if (module.equals("/.Diplomas/") && downloadUrlList != null && downloadUrlList.size() > 0) {
                    getSafetyCards();
                } else if (module.equals("/.SafetyCards/")) {
                    if (customerIDList != null && customerIDList.size() > 0) {
                        getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                    }
                } else if (module.equals("/.MyCompany/")) {
                    getTools();
                } else {
                    // 14-01-2020
                    if (hasCopAccess()) {
                        getPsiCourseCompletion();
                    } else {
                        txtMessage.setText(getResources().getString(R.string.ready));
                        isProcessCompleted = true;
                        executeAsynTask();
                    }
                    /*txtMessage.setText(getResources().getString(R.string.ready));
                    isProcessCompleted = true;
                    executeAsynTask();*/
                }
            }
        } else {
            if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                backToSplashScreen();
            } else {
                AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }

    public void downloadFileFromServer(final String fileURL, final String fileName, final String module) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    File root, file;
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    if (module.equals("/.SafetyCards/")) {
                        request.accept("application/pdf");
                    } else if (module.equals("/.MyCompany/")) {
                        request.authorization("Bearer " + token);
                    } else if (module.equals("/.Diplomas/")) {
                        request.accept("application/pdf");
                        request.contentType("application/pdf");
                        request.authorization("Bearer " + token);
                    } else if (module.equals("/.tools/")) {
                        request.accept("application/zip");
                    }
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    if (module.equals("/.SafetyCards/") || module.equals("/.MyCompany/") || module.equals("/.Diplomas/")) {
                        root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + module);
                    } else {
                        root = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/");
                    }
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    if (module.equals("/.MyCompany/")) {
                        file = new File(dir, fileName);
                    } else if (module.equals("/.Diplomas/") || module.equals("/.SafetyCards/")) {
                        file = new File(dir, fileName + ".pdf");
                    } else {
                        file = new File(dir, fileName);
                    }

                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        DeleteFile(file);
                    }
                } catch (Exception ex) {
                    File root;
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    if (module.equals("/.tools/")) {
                        root = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/");
                    } else {
                        root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + module);
                    }
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    File file1;
                    if (module.equals("/.MyCompany/") || module.equals("/.tools/")) {
                        file1 = new File(dir, fileName);
                    } else {
                        file1 = new File(dir, fileName + ".pdf");
                    }
                    DeleteFile(file1);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (connectionDetector.isConnectingToInternet()) {
                    String file_name = "";
                    if (downloadUrlList.size() > 0) {
                        if (module.equals("/.tools/")) {
                            ToolsProperty info = new ToolsProperty();
                            info.id = downloadUrlList.get(0).licenseId;
                            dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                        }
                        downloadUrlList.remove(downloadUrlList.get(0));
                        if (downloadUrlList.size() > 0) {
                            if (module.equals("/.Diplomas/")) {
                                file_name = downloadUrlList.get(0).licenseId;
                            } else {
                                file_name = downloadUrlList.get(0).safetyCard_cardID;
                            }
                            downloadFile(module, file_name);
                        } else {
                            if (module.equals("/.Diplomas/")) {
                                getSafetyCards();
                            } else if (module.equals("/.SafetyCards/")) {
                                if (customerIDList != null && customerIDList.size() > 0) {
                                    getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                                }
                            } else if (module.equals("/.MyCompany/")) {
                                getTools();
                            } else if (module.equals("/.tools/")) {
                                isProcessCompleted = true;
                                //executeAsynTask();Lov Tyagi
                                if (hasCopAccess()) {
                                    getPsiCourseCompletion();
                                } else {
                                    txtMessage.setText(getResources().getString(R.string.ready));
                                    isProcessCompleted = true;
                                    executeAsynTask();
                                }
                            }
                        }
                    } else {
                        if (module.equals("/.Diplomas/")) {
                            getSafetyCards();
                        } else if (module.equals("/.SafetyCards/")) {
                            if (customerIDList != null && customerIDList.size() > 0) {
                                getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                            }
                        } else if (module.equals("/.MyCompany/")) {
                            getTools();
                        } else if (module.equals("/.tools/")) {
                            // 14-01-2020
                            if (hasCopAccess()) {
                                getPsiCourseCompletion();
                            } else {
                                txtMessage.setText(getResources().getString(R.string.ready));
                                isProcessCompleted = true;
                                executeAsynTask();
                            }
                           /* txtMessage.setText(getResources().getString(R.string.ready));
                            isProcessCompleted = true;
                            executeAsynTask();*/
                        }
                    }
                } else {
                    if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                        backToSplashScreen();
                    } else {
                        Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }.execute();
    }

    public void getCourseImageUrl(final String courseId) {
        // StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://dev.trainor.no/cms/app/courseimage.php?uuid=" + courseId, new Response.Listener<String>() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Course_image_URL + courseId, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String image_URL = jsonObject.getString("image") == null ? "" : (jsonObject.getString("image").equals("null") ? "" : jsonObject.getString("image"));
                    long result = dbUpdate.updateImageURLInCourseTable(courseId, image_URL);
                    if (courseIDList.contains(courseId)) {
                        courseIDList.remove(courseIDList.indexOf(courseId));
                    }
                } catch (Exception ex) {
                    Log.d("", ex.getMessage().toString());
                } finally {
                    if (courseIDList.size() > 0) {
                        getCourseImageUrl(courseIDList.get(0));
                    } else {
                        // getDiplomas();
                        getDSBDetails();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                long result = dbUpdate.updateImageURLInCourseTable(courseId, "");
                if (courseIDList.contains(courseId)) {
                    courseIDList.remove(courseIDList.indexOf(courseId));
                }
                if (courseIDList.size() > 0) {
                    getCourseImageUrl(courseIDList.get(0));
                } else {
                    // getDiplomas();
                    getDSBDetails();
                }
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void getUserImageFromLive(final String ImageUrl, final String username, final String email, final String phone, final String birthdate, final String language, final String FirstName, final String LastName, final String UserID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PicasoImageLoader.getImagesFromURL(LoadingPageActivity.this, ImageUrl, circleImageView, 500, 500, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (isActivityVisible) {
                            saveSharedPreferenceValues(ImageUrl, FirstName, LastName, email, birthdate, language, phone, username, UserID);
                            txtWelcome.setText(getResources().getString(R.string.welcome_loading));
                            txtUserName.setText(FirstName);
                            showCircleImageViewAnimation();
                        }
                    }

                    @Override
                    public void onError() {
                        if (isActivityVisible) {
                            saveSharedPreferenceValues("", FirstName, LastName, email, birthdate, language, phone, username, UserID);
                            txtWelcome.setText(getResources().getString(R.string.welcome_loading));
                            txtUserName.setText(FirstName);
                            circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                            showCircleImageViewAnimation();
                        }
                    }
                });
            }
        });
    }

    public void setLocale(String language) {
        String localLang = language;
        if (localLang.startsWith("nb")) {
            localLang = "nb";
        } else if (localLang.startsWith("en")) {
            localLang = "en";
        } else if (localLang.startsWith("ko")) {
            localLang = "ko";
        } else if (localLang.startsWith("pl")) {
            localLang = "pl";
        } else if (localLang.startsWith("sv")) {
            localLang = "sv";
        } else if (localLang.startsWith("pt")) {
            localLang = "pt";
        }
        myLocale = new Locale(localLang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        txtMessage.setText(getResources().getString(R.string.logging_in));
    }

    public void getFirebaseToken() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FirebaseToken = FirebaseInstanceId.getInstance().getToken();
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage().toString());
                }

                return null;
            }
        }.execute();
    }

    public void updateFirebaseTokenOnServer(final String userid, final String uuid, final String imeino, final String token, final String deviceType, final String methodType, final String userLanguage) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_FirebaseToken_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            if (jsonObject.get("Result").equals("inserted successfully")) {
                                String imeiNo = connectionDetector.getAndroid_ID(LoadingPageActivity.this);
                                SharedPreferences.Editor editor = spManager.getNotificationHandlerSharedPreference();
                                editor.putString("Token", FirebaseToken);
                                editor.putString("IMEINo", imeiNo);
                                editor.commit();
                            } else if (jsonObject.get("Result").equals("token updated successfully.")) {
                                String imeiNo = connectionDetector.getAndroid_ID(LoadingPageActivity.this);
                                SharedPreferences.Editor editor = spManager.getNotificationHandlerSharedPreference();
                                editor.putString("Token", FirebaseToken);
                                editor.putString("IMEINo", imeiNo);
                                editor.commit();
                            }
                        }
                    }
                } catch (Exception ex) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.print(error);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("userID", userid);
                params.put("uuid", uuid);
                params.put("imeiNo", imeino);
                params.put("token", token);
                params.put("deviceType", deviceType);
                params.put("methodType", methodType);
                params.put("userLanguage", userLanguage);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void saveSharedPreferenceValues(final String ImageUrl, final String FirstName, final String LastName, final String email, final String birthdate, final String language, final String phone, final String username, final String UserID) {
        if (spManager.getSharedPreferenceExistence()) {
            if (firebaseAnalytics != null) {
                firebaseAnalytics.setUserProperty("user_id", UserID);
                firebaseAnalytics.setUserProperty("language", language);
                firebaseAnalytics.setUserId(UserID);
            }
            spManager.removeSharedPreference();
            SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
            SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
            sharedPreferenceInfo.Token = getIntent().getStringExtra("token");
            sharedPreferenceInfo.ProfilePicURL = ImageUrl;
            sharedPreferenceInfo.FirstName = FirstName == null ? "" : FirstName.equals("null") ? "" : FirstName;
            sharedPreferenceInfo.LastName = LastName == null ? "" : LastName.equals("null") ? "" : LastName;
            sharedPreferenceInfo.Email = email == null ? "" : email.equals("null") ? "" : email;
            sharedPreferenceInfo.dob = birthdate == null ? "" : birthdate.equals("null") ? "" : birthdate;
            sharedPreferenceInfo.language = language;
            sharedPreferenceInfo.Phone_no = phone == null ? "" : phone.equals("null") ? "" : phone;
            sharedPreferenceInfo.UserName = getIntent().getStringExtra("username");
            sharedPreferenceInfo.UserID = UserID;
            SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
            if (language == null || language.equals("null") || language.equals("")) {
                setLocale("en");
            } else {
                setLocale(language);
            }
        } else {
            spManager.removeSharedPreference();
            SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
            SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
            sharedPreferenceInfo.Token = getIntent().getStringExtra("token");
            sharedPreferenceInfo.ProfilePicURL = ImageUrl;
            sharedPreferenceInfo.FirstName = FirstName == null ? "" : FirstName.equals("null") ? "" : FirstName;
            sharedPreferenceInfo.LastName = LastName == null ? "" : LastName.equals("null") ? "" : LastName;
            sharedPreferenceInfo.Email = email == null ? "" : email.equals("null") ? "" : email;
            sharedPreferenceInfo.dob = birthdate == null ? "" : birthdate.equals("null") ? "" : birthdate;
            sharedPreferenceInfo.language = language;
            sharedPreferenceInfo.Phone_no = phone == null ? "" : phone.equals("null") ? "" : phone;
            sharedPreferenceInfo.UserName = getIntent().getStringExtra("username");
            sharedPreferenceInfo.UserID = UserID;
            SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
            if (language == null || language.equals("null") || language.equals("")) {
                setLocale("en");
            } else {
                setLocale(language);
            }
            if (firebaseAnalytics != null) {
                firebaseAnalytics.setUserProperty("user_id", UserID);
                firebaseAnalytics.setUserProperty("language", language);
                firebaseAnalytics.setUserId(UserID);
            }
        }
        String totalNotification = spManager.getTotalNotificationCount();
        try {
            if (totalNotification != null && !totalNotification.equals("")) {
                ShortcutBadger.applyCount(LoadingPageActivity.this, Integer.parseInt(totalNotification));
            } else {
                ShortcutBadger.applyCount(LoadingPageActivity.this, 0);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String downOverWIFIIsEnabled = dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", UserID, "");
        if (downOverWIFIIsEnabled == null || downOverWIFIIsEnabled.equals("")) {
            dataBaseHandlerInsert.addDataIntoNotificationTable("DownloadOverWifi", "Yes", UserID);
        }
    }

    public void getUpcomingCourse() {
        txtMessage.setText(getResources().getString(R.string.sync_courses));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Upcoming_Course_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        dataBaseHandlerDelete.deleteCoursesFromTable("CourseDownload");
                        dataBaseHandlerDelete.deleteCoursesFromTable("offline");
                        dataBaseHandlerDelete.deleteCoursesFromTable("CoursesTable");
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (!jsonObject.getString("status").equals("CANCELLED")) {
                                DiplomaProperty courseProperty = new DiplomaProperty();
                                courseProperty.expiresDate = jsonObject.getString("expiresDate") == null ? "" : jsonObject.getString("expiresDate").equals("null") ? "" : jsonObject.getString("expiresDate");
                                String duration = jsonObject.getString("duration") == null ? "" : jsonObject.getString("duration").equals("null") ? "" : jsonObject.getString("duration");

                                if (!duration.equals("")) {
                                    courseProperty.courseDuration = "";
                                    String[] tokens = duration.split(";");
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
                                        courseProperty.info_content = jsonObjectDescription.getString("content") == null ? "" : jsonObjectDescription.getString("content").equals("null") ? "" : jsonObjectDescription.getString("content");
                                    } else {
                                        courseProperty.info_content = "";
                                    }
                                    if (jsonObjectDescription.has("goal") && jsonObjectDescription.getString("goal") != null) {
                                        courseProperty.info_goal = jsonObjectDescription.getString("goal") == null ? "" : jsonObjectDescription.getString("goal").equals("null") ? "" : jsonObjectDescription.getString("goal");
                                    } else {
                                        courseProperty.info_goal = "";
                                    }
                                    if (jsonObjectDescription.has("targetGroup") && jsonObjectDescription.getString("targetGroup") != null) {
                                        courseProperty.info_targetGroup = jsonObjectDescription.getString("targetGroup") == null ? "" : jsonObjectDescription.getString("targetGroup").equals("null") ? "" : jsonObjectDescription.getString("targetGroup");
                                    } else {
                                        courseProperty.info_targetGroup = "";
                                    }
                                } else {
                                    courseProperty.info_content = "";
                                    courseProperty.info_goal = "";
                                    courseProperty.info_targetGroup = "";
                                }

                                courseProperty.courseId = jsonObject.getString("courseId") == null ? "" : jsonObject.getString("courseId").equals("null") ? "" : jsonObject.getString("courseId");
                                courseProperty.licenseId = jsonObject.getString("licenseId") == null ? "" : jsonObject.getString("licenseId").equals("null") ? "" : jsonObject.getString("licenseId");
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

                                if (dbSelect.getDataFromCoursesTable("*", spManager.getUserID(), courseProperty.licenseId.toString().trim()).equals("")) {
                                    courseProperty.userID = spManager.getUserID();
                                    if (jsonObject.has("isEpkg")) {
                                        if (jsonObject.getString("isEpkg").equals("true")) {
                                            dataBaseHandlerInsert.addDataIntoCoursesTable(courseProperty);
                                        }
                                    } else {
                                        dataBaseHandlerInsert.addDataIntoCoursesTable(courseProperty);
                                    }
                                }

                                String dwTime = dbSelect.getDataFromCourseDownloadTable("IfNull(DownloadTime,'')as dwtime", spManager.getUserID(), courseProperty.courseId, courseProperty.licenseId);
                                if (!dwTime.equals("")) {
                                    dbUpdate.updateTable("CoursesTable", spManager.getUserID(), courseProperty.licenseId, "DownloadTime", dwTime);
                                }

                                //<--------------------SCORM Insertion For Online Course Percentage------------------------->
                                if (jsonObject.has("isEpkg")) {
                                    if (jsonObject.getString("isEpkg").equals("true")) {
                                        if (!courseProperty.startCourseUrl.equals("")) {
                                            courseIDList.add(jsonObject.getString("courseId"));
                                        }
                                        if (!dbSelect.isSCORMCOontentExists(spManager.getUserID(), courseProperty.licenseId)) {
                                            dataBaseHandlerInsert.addDataIntoSCORMTable(courseProperty.licenseId, spManager.getUserID(), "");
                                        }
                                    }
                                    if (courseProperty.startCourseUrl.equals("")) {
                                        courseIDList.add(courseProperty.courseId);
                                    }
                                } else {
                                    courseIDList.add(jsonObject.getString("courseId"));
                                    if (!dbSelect.isSCORMCOontentExists(spManager.getUserID(), courseProperty.licenseId)) {
                                        dataBaseHandlerInsert.addDataIntoSCORMTable(courseProperty.licenseId, spManager.getUserID(), "");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    if (courseIDList != null && courseIDList.size() > 0) {
                        getCourseImageUrl(courseIDList.get(0));
                    } else {
                        getDSBDetails();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(LoadingPageActivity.this, error), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void getSaveCustomerID(final String token) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.CUSTOMERS_DETAIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        customerIDList.clear();
                        dataBaseHandlerDelete.deleteTableByName("CustomerDetails", "");
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

                            dataBaseHandlerInsert.addDataIntoCustomerDetailsTable(info);
                            customerIDList.add(info.customer_id);
                        }
                    }
                } catch (Exception ex) {
                    ExceptionHandler.getErrorMessage(LoadingPageActivity.this, ex);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void getMyCompanyDetails(final String userID, final String customerID) {
        companyDocumentSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DocumentSwitchStatus", " ");
        if (companyDocumentSwitch_status.equals("ON")) {
            txtMessage.setText(getResources().getString(R.string.downloading_documents));
        } else {
            txtMessage.setText(getResources().getString(R.string.syncronizing_documents));
        }
        if (clearListTag) {
            downloadUrlList.clear();
            currentFileIndex = 0;
            totalURLIndex = 0;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.My_Company_Details + customerID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            final MyCompanyProperty myCompanyProperty = new MyCompanyProperty();
                            myCompanyProperty.name = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("null") ? "" : jsonObject.getString("name");
                            myCompanyProperty.lastModified = jsonObject.getString("lastModified") == null ? "" : jsonObject.getString("lastModified").equals("null") ? "" : jsonObject.getString("lastModified");
                            myCompanyProperty.description = jsonObject.getString("description") == null ? "" : jsonObject.getString("description").equals("null") ? "" : jsonObject.getString("description");
                            myCompanyProperty.downloadUrl = jsonObject.getString("downloadUrl") == null ? "" : jsonObject.getString("downloadUrl").equals("null") ? "" : jsonObject.getString("downloadUrl");
                            myCompanyProperty.fileName = jsonObject.getString("fileName") == null ? "" : jsonObject.getString("fileName").equals("null") ? "" : jsonObject.getString("fileName");
                            myCompanyProperty.documentID = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("null") ? "" : jsonObject.getString("id");
                            myCompanyProperty.locale = jsonObject.getString("locale") == null ? "" : jsonObject.getString("locale").equals("null") ? "" : jsonObject.getString("locale");
                            if (myCompanyProperty.locale.equals("")) {
                                myCompanyProperty.locale = "nb_NO";
                            }
                            DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                            downloadUrlProperty.downloadURL = myCompanyProperty.downloadUrl;
                            downloadUrlProperty.safetyCard_cardID = myCompanyProperty.fileName;
                            downloadUrlList.add(downloadUrlProperty);

                            double fileSize = Double.parseDouble(jsonObject.getString("fileSize") == null ? "0" : jsonObject.getString("fileSize").equals("null") ? "0" : jsonObject.getString("fileSize"));
                            double result_fileSize = (fileSize / 1048576);
                            String formattedFileSize = "";
                            if (result_fileSize > 1.0) {
                                String stringFileSize = String.valueOf(result_fileSize);
                                formattedFileSize = stringFileSize.substring(0, 4);
                            } else {
                                if (result_fileSize == 0.0) {
                                    formattedFileSize = "0.0";
                                } else {
//                                    String stringFileSize = String.valueOf(result_fileSize);
//                                    formattedFileSize = stringFileSize.substring(0, 4);
                                    String stringFileSize = String.valueOf(result_fileSize);
                                    if (stringFileSize.contains("E")) {
                                        formattedFileSize = "0.0";
                                    } else {
                                        formattedFileSize = stringFileSize.substring(0, 4);
                                    }
                                }
                            }
                            myCompanyProperty.fileSize = formattedFileSize + " mb";
                            myCompanyProperty.customerID = customerID;
                            myCompanyProperty.userID = userID; // user id
                            String DbLastModified = dbSelect.getLastModifiedDataFromMyCompanyTable(myCompanyProperty.documentID);
                            if (DbLastModified.equals("")) {
                                if (!dbSelect.isCompanyDocDataExist(userID, customerID, myCompanyProperty.documentID)) {
                                    dataBaseHandlerInsert.addDataIntoMyCompanyTable(myCompanyProperty);
                                }
                            } else {
                                if (!DbLastModified.equals(myCompanyProperty.lastModified)) {
                                    dbUpdate.updateLastModifiedDocumentData(userID, myCompanyProperty);
                                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
                                    String filePath = root.getAbsolutePath();
                                    File dir = new File(filePath);
                                    File file = new File(dir, myCompanyProperty.fileName);
                                    if (file.exists()) {
                                        DeleteFile(file);
                                    }
                                }
                            }

                            if (!documentIDsToBeDeleted.contains(myCompanyProperty.documentID)) {
                                if (documentIDsToBeDeleted.equals("")) {
                                    documentIDsToBeDeleted = "'" + myCompanyProperty.documentID + "'";
                                } else {
                                    documentIDsToBeDeleted += "," + "'" + myCompanyProperty.documentID + "'";
                                }
                            }
                        }
                        dataBaseHandlerDelete.deleteMyCompanyIDsNotExists(documentIDsToBeDeleted, customerID);
                        documentIDsToBeDeleted = "";
                    }
                } catch (Exception ex) {
                    Log.d("", ex.getMessage().toString());
                } finally {
                    clearListTag = false;
                    if (customerIDList != null && customerIDList.size() > 0) {
                        if (customerIDList.contains(customerID)) {
                            customerIDList.remove(customerID);
                        }
                        if (customerIDList.size() > 0) {
                            getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                        } else {
                            if (companyDocumentSwitch_status.equals("ON") && downloadUrlList != null && downloadUrlList.size() > 0) {
                                totalURLIndex = downloadUrlList.size();
                                currentFileIndex = 0;
                                downloadFile("/.MyCompany/", downloadUrlList.get(0).safetyCard_cardID);
                            } else {
                                /*txtMessage.setText(getResources().getString(R.string.ready));
                                isProcessCompleted = true;*/
                                getTools();
                            }
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                        backToSplashScreen();
                    } else {
                        AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(LoadingPageActivity.this, error), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    void DeleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteFile(child);
            }
        }
        fileOrDirectory.delete();
    }

    public void getTools() {
        toolsSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "ToolsSwitchStatus", " ");
        if (toolsSwitch_status.equals("ON")) {
            txtMessage.setText(getResources().getString(R.string.downloading_tools));
        } else {
            txtMessage.setText(getResources().getString(R.string.syncronizing_tools));
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Tools_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        toolsList.clear();
                        downloadUrlList.clear();
                        currentFileIndex = 0;
                        totalURLIndex = 0;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ToolsProperty property = new ToolsProperty();
                            property.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id");
                            property.last_modified = jsonObject.getString("last_modified") == null ? "" : jsonObject.getString("last_modified");
                            String filename = jsonObject.getString("name") == null ? "" : jsonObject.getString("name");
                            String convertedFileName = filename.replaceAll("[-+.^:,]", " ");
                            property.name = convertedFileName;
                            property.force_download = jsonObject.getString("force_download") == null ? "" : jsonObject.getString("force_download");
                            property.language_code = jsonObject.getString("language_code") == null ? "" : jsonObject.getString("language_code");
                            property.background_color = jsonObject.getString("background_color") == null ? "" : jsonObject.getString("background_color");
                            property.is_landscape = jsonObject.getString("is_landscape") == null ? "" : jsonObject.getString("is_landscape").equals("") ? "" : jsonObject.getString("is_landscape");
                            property.customer_ids = jsonObject.getString("customer_ids") == null ? "" : jsonObject.getString("customer_ids");
                            property.iconURL = jsonObject.getString("icon") == null ? "" : jsonObject.getString("icon");
                            property.file = jsonObject.getString("file") == null ? "" : jsonObject.getString("file");
                            property.file_size = jsonObject.getString("file_size") == null ? "" : jsonObject.getString("file_size");

                            DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                            downloadUrlProperty.downloadURL = property.file;
                            downloadUrlProperty.safetyCard_cardID = property.name;
                            downloadUrlProperty.licenseId = property.id;
                            downloadUrlList.add(downloadUrlProperty);
                            toolsList.add(property);
                        }

                        for (ToolsProperty info : toolsList) {
                            String last_modified = dbSelect.getLastModifiedDataFromToolTable(info.id);
                            if (last_modified.equals("")) {
                                dataBaseHandlerInsert.addDataIntoToolBoxTable(info);
                                File rootDir = android.os.Environment.getExternalStorageDirectory();
                                File rootZipped = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/" + info.name);
                                if (rootZipped.exists()) {
                                    dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                                }
                            } else if (!last_modified.equals(info.last_modified)) {
                                dbUpdate.updateToolBoxDetails(info, "ToolBoxUpdated");
                                File rootDir = android.os.Environment.getExternalStorageDirectory();
                                File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/UnZipped/" + info.name);
                                File rootZipped = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/" + info.name);
                                if (root.exists()) {
                                    DeleteFile(root);
                                }
                                if (rootZipped.exists()) {
                                    DeleteFile(rootZipped);
                                }
                            } else {
                                File rootDir = android.os.Environment.getExternalStorageDirectory();
                                File rootZipped = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/" + info.name);
                                if (rootZipped.exists()) {
                                    dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                                } else {
                                    dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    if (toolsSwitch_status.equals("ON") && downloadUrlList != null && downloadUrlList.size() > 0) {
                        // 22-02-2018
                        totalURLIndex = downloadUrlList.size();
                        currentFileIndex = 0;
                        downloadFile("/.tools/", downloadUrlList.get(0).safetyCard_cardID);
                    } else {
                        // 14-01-2020
                        if (hasCopAccess()) {
                            //first api call when download is off
                            getPsiCourseCompletion();
                        } else {
                            txtMessage.setText(getResources().getString(R.string.ready));
                            isProcessCompleted = true;
                            executeAsynTask();
                        }


                        /*txtMessage.setText(getResources().getString(R.string.ready));
                        isProcessCompleted = true;

                        executeAsynTask();*/
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(LoadingPageActivity.this, error), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void getDSBDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.DSBMagzineURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        dataBaseHandlerDelete.deleteTableByName("DSBTable", "");
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                DSBProperty property = new DSBProperty();
                                property.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("null") ? "" : jsonObject.getString("id");
                                property.last_modified = jsonObject.getString("last_modified") == null ? "" : jsonObject.getString("last_modified").equals("null") ? "" : jsonObject.getString("last_modified");
                                String filename = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("null") ? "" : jsonObject.getString("name");
                                String convertedFileName = filename.replaceAll("[-+.^:,]", " ");
                                property.name = convertedFileName;
                                property.release_date = jsonObject.getString("release_date") == null ? "" : jsonObject.getString("release_date").equals("null") ? "" : jsonObject.getString("release_date");
                                property.imageURL = jsonObject.getString("image") == null ? "" : jsonObject.getString("image").equals("null") ? "" : jsonObject.getString("image");
                                property.fileURL = jsonObject.getString("file") == null ? "" : jsonObject.getString("file").equals("null") ? "" : jsonObject.getString("file");
                                property.file_size = jsonObject.getString("file_size") == null ? "" : jsonObject.getString("file_size").equals("null") ? "" : jsonObject.getString("file_size");
                                property.order = jsonObject.getString("order") == null ? "" : jsonObject.getString("order").equals("null") ? "" : jsonObject.getString("order");
                                dataBaseHandlerInsert.addDataIntoDSBTable(property);
                            }

                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    // executeAsynTask();
                    getDiplomas();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                    backToSplashScreen();
                } else {
                    AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(LoadingPageActivity.this, error), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(LoadingPageActivity.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        }) {
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(LoadingPageActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void executeAsynTask() {
        // getAllPendingNotification();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isActivityVisible) {
                    if (loginforStattent != null && loginforStattent.equals("StattnetLogin")) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("StattnetPref", 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("StattnetPrefValue", "StattnetLogin");
                        editor.commit();
                    }

                    try {
                        if (Build.VERSION.SDK_INT >= 21) {
                            Intent intent = new Intent(LoadingPageActivity.this, HomePage.class);
                            intent.putExtra("From", "LoadingPage");
                            intent.putExtra("token", token);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Pair<View, String> pair1 = Pair.create(findViewById(R.id.llFrameLayout), "FrameLayout");
                            Pair<View, String> pair2 = Pair.create(findViewById(R.id.circleImageView), "CircleImageView");
                            Pair<View, String> pair3 = Pair.create(findViewById(R.id.txtUserName), "UserName");
                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(LoadingPageActivity.this, pair1, pair3, pair2);
                            startActivity(intent, options.toBundle());
                        } else {
                            final Intent intent1 = new Intent(LoadingPageActivity.this, HomePage.class);
                            intent1.putExtra("From", "LoadingPage");
                            intent1.putExtra("token", token);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            ActivityTransitionLauncher.with(LoadingPageActivity.this).from(circleImageView).launch(intent1);
                            overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                        }
                    } catch (Exception ex) {
                        Log.d("Animation exception", ex.toString());
                    }
                }
            }
        });
    }

    public void backToSplashScreen() {
        AlertDialogManager.showDialog(LoadingPageActivity.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
            @Override
            public void onClick() {
                Intent intent = new Intent(LoadingPageActivity.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

            }
        });
    }

    public void getPsiCourseCompletion() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.PsiCourseCompletion, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("licenseId")) {
                                String psiCourseLisenceId = jsonObject.getString("licenseId");
                                editor.putString("COPLisenceID", psiCourseLisenceId);
                            }
                            if (jsonObject.has("status")) {
                                String PSiCourseStatus = jsonObject.getString("status");
                                editor.putString("COPCourseStatus", PSiCourseStatus);
                            }
                            editor.commit();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    getFacilitynGroups();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }

                final String status_Code = String.valueOf(error.networkResponse.statusCode);
                Log.d("Satus Code= ", status_Code);
                if (status_Code.equals("404")) {
                    txtMessage.setText(getResources().getString(R.string.ready));
                    callEnrollInPsiCourse();
                } else {
                    getFacilitynGroups();
                }
                //isProcessCompleted = true;
                //executeAsynTask();
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

    public void callEnrollInPsiCourse() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.EnrollPSICourse, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("licenseId")) {
                                String psiCourseLisenceId = jsonObject.getString("licenseId");
                                editor.putString("COPLisenceID", psiCourseLisenceId);
                            }
                            if (jsonObject.has("status")) {
                                String PSiCourseStatus = jsonObject.getString("status");
                                editor.putString("COPCourseStatus", PSiCourseStatus);
                            }
                            editor.commit();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    getFacilitynGroups();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }

                final String status_Code = String.valueOf(error.networkResponse.statusCode);
                Log.d("Satus Code= ", status_Code);
                if (status_Code.equals("404")) {
                    //txtMessage.setText(getResources().getString(R.string.ready));
                }
                isProcessCompleted = true;
                executeAsynTask();
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

    public void getFacilitynGroups() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.GetFacility, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    response = new String(response.getBytes("ISO-8859-1"), "UTF-8");
                    if (response != null && !response.equals("")) {
                        response = response.replaceAll("\n", "");
                        spManager.removeValuefromCOPFacilityPref("FacilityResponse");
                        COPfacilityEditor.putString("FacilityResponse", response);
                        COPfacilityEditor.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    txtMessage.setText(getResources().getString(R.string.ready));
                    isProcessCompleted = true;
                    executeAsynTask();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtMessage.setText(getResources().getString(R.string.ready));
                isProcessCompleted = true;
                executeAsynTask();
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

    public boolean hasCopAccess() {
        copUserList = new ArrayList<>();
        copUserList.clear();
        copUserList = dbSelect.getCompUser("true");

        if (copUserList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

}
