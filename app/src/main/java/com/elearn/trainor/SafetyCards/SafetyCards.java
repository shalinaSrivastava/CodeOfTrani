package com.elearn.trainor.SafetyCards;

import android.Manifest;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.elearn.trainor.BaseAdapters.SafetyCardRecyclerViewAdapter;
import com.elearn.trainor.HelperClasses.AppConstants;
import com.elearn.trainor.HelperClasses.GpsUtils;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ComparatorHelperSafetyCards;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.DownloadUrlProperty;
import com.elearn.trainor.PropertyClasses.NotificationProperty;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.services.network.HttpRequest;
import me.leolin.shortcutbadger.ShortcutBadger;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SafetyCards extends AppCompatActivity implements View.OnClickListener {
    public static SafetyCards instance;
    LinearLayout ll_back, llhome;
    private RecyclerView recyclerView;
    SafetyCardRecyclerViewAdapter safetycardAdapter;
    TextView text_header;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerInsert dataBaseHandlerInsert;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    List<SafetyCardProperty> safetyCardListForRecyclerView;
    List<SafetyCardProperty> approvedCardList;
    List<SafetyCardProperty> unApprovedCardList;
    List<SafetyCardProperty> nullValidToApprovedList;
    SwipeRefreshLayout swipelayout;
    Button btn_register_card;
    LinearLayout firstTimeView;
    private ProgressDialog pDialog;
    boolean isWindowActive = false;
    private static final int BUFFER_SIZE = 4096;
    RelativeLayout rl_safetyCard_list, rl_report_entery, rl_add_more_cards;
    LinearLayout safetyCardRow;
    SafetyCardProperty safetyCardProperty;
    String safetycardPDF_FileName = "", safetyCardSwitch_Status, NetworkMode = "", From = "", notificationID = "";
    ActionBar actionBar;
    List<DownloadUrlProperty> downloadSafetyCardUrlList = new ArrayList<>(); // new list for offline download
    Long freeSpaceMB;
    IntentFilter internet_intent_filter;
    Dialog syncIncompleteDialog;
    FirebaseAnalytics analytics;
    Boolean hasEntery;


    private boolean isGPS = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_cards);
        isWindowActive = true;
        getControls();
        instance = this;
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        isWindowActive = true;
        instance = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (safetyCardSwitch_Status.equals("ON")) {
            isWindowActive = false;
        } else {
            isWindowActive = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "SafetyCards", null);
    }

    @Override
    protected void onStop() {
        if (internetInfoReceiver != null) {
            if (internetInfoReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        dismissWaitDialog();
        super.onStop();
        isWindowActive = false;
    }

    public static SafetyCards getInstance() {
        if (instance == null) {
            instance = new SafetyCards();
        }
        return instance;
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(SafetyCards.this);

        // actionBar = getSupportActionBar();
        connectionDetector = new ConnectionDetector(SafetyCards.this);
        dbUpdate = new DataBaseHandlerUpdate(SafetyCards.this);
        dbSelect = new DataBaseHandlerSelect(SafetyCards.this);
        dbDelete = new DataBaseHandlerDelete(SafetyCards.this);
        dataBaseHandlerInsert = new DataBaseHandlerInsert(SafetyCards.this);
        dataBaseHandlerDelete = new DataBaseHandlerDelete(SafetyCards.this);
        spManager = new SharedPreferenceManager(SafetyCards.this);
        safetyCardSwitch_Status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "SafetyCardSwitchStatus", " ");

        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);

        safetyCardListForRecyclerView = new ArrayList<>();
        approvedCardList = new ArrayList<>();
        nullValidToApprovedList = new ArrayList<>();
        unApprovedCardList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.safety_cards_recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        btn_register_card = (Button) findViewById(R.id.btn_register_card);
        rl_add_more_cards =  (RelativeLayout) findViewById(R.id.rl_add_more_cards);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getString(R.string.saftey_cards));
        firstTimeView = (LinearLayout) findViewById(R.id.firstTimeView);
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        rl_safetyCard_list = (RelativeLayout) findViewById(R.id.rl_safetyCard_list);
        rl_report_entery = (RelativeLayout) findViewById(R.id.rl_report_entery);
        //rl_report_entery.setVisibility(View.GONE);
        //swipelayout.setVisibility(View.GONE);
        rl_safetyCard_list.setVisibility(View.GONE);
        firstTimeView.setVisibility(View.GONE);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        btn_register_card.setOnClickListener(this);
        rl_add_more_cards.setOnClickListener(this);
        rl_report_entery.setOnClickListener(this);
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

        safetyCardListForRecyclerView = dbSelect.getSafetyCardAttribute("");

        if (getIntent().getStringExtra("RegisterSafetyCard") != null && getIntent().getStringExtra("RegisterSafetyCard").equals("GetSafetyCards")) {
            if (connectionDetector.isConnectingToInternet()) {
//                showWaitDialog();

            } else {
                if (safetyCardListForRecyclerView.size() > 0) {
                    //swipelayout.setVisibility(View.VISIBLE);
                    rl_safetyCard_list.setVisibility(View.VISIBLE);
                    firstTimeView.setVisibility(View.GONE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(SafetyCards.this));
                    safetycardAdapter = new SafetyCardRecyclerViewAdapter(SafetyCards.this, safetyCardListForRecyclerView);
                    recyclerView.setAdapter(safetycardAdapter);
                }
            }
        } else if (safetyCardListForRecyclerView.size() > 0 && !connectionDetector.isConnectingToInternet()) {
            rl_safetyCard_list.setVisibility(View.VISIBLE);
            firstTimeView.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(SafetyCards.this));
            safetycardAdapter = new SafetyCardRecyclerViewAdapter(SafetyCards.this, safetyCardListForRecyclerView);
            recyclerView.setAdapter(safetycardAdapter);
        } else {
            if (connectionDetector.isConnectingToInternet()) {
               /* int safetyCradNotificationCount = Integer.parseInt(dbSelect.getNotificationData("NotificationCountTable", "NotificationCount", "SafetyCard", spManager.getUserID(), "NotificationCount"));
                safetyCradNotificationCount -= 1;
                showWaitDialog();
                updateNotificationCount("SafetyCard", spManager.getUserID(),safetyCradNotificationCount);*/
            } else {
                rl_safetyCard_list.setVisibility(View.GONE);
                rl_report_entery.setVisibility(View.GONE);
                firstTimeView.setVisibility(View.VISIBLE);
            }
        }
        swipelayout.setColorSchemeResources(R.color.colorPageHeader);
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    getSafetyCards();
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(SafetyCards.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
        String CurrentLang = Locale.getDefault().getLanguage();
        String sharedLanguage = spManager.getLanguage();
        if (!sharedLanguage.equals(CurrentLang)) {
            setLocale(sharedLanguage);
        }
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
            case R.id.btn_register_card:
            case R.id.rl_add_more_cards:
               /* if (connectionDetector.isConnectingToInternet()) {
                    commonIntentMethod(VerifyInfo.class);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    AlertDialogManager.showDialog(SafetyCards.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }*/
                getLocation();
                break;

            case R.id.rl_report_entery:
                if (isGPS) {
                    getLocation();
                }
                break;
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(SafetyCards.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SafetyCards.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SafetyCards.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            commonIntentMethod(StartCheckInFacility.class);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

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
        commonIntentMethod(HomePage.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void getSafetyCards() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Upcoming_SafetyCards_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dbDelete.deleteTableByName("SafetyCards", "");
                swipelayout.setRefreshing(false);
                safetyCardListForRecyclerView.clear();
                approvedCardList.clear();
                unApprovedCardList.clear();
                nullValidToApprovedList.clear();
                downloadSafetyCardUrlList.clear();
                try {
                    if (response != null && !response.equals("")) {
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
                                    downloadSafetyCardUrlList.add(downloadUrlProperty);
                                } else {
                                    unApprovedCardList.add(property);
                                }
                            }
                            Collections.sort(approvedCardList, new ComparatorHelperSafetyCards());
                            Collections.reverse(approvedCardList);
                            safetyCardListForRecyclerView.addAll(approvedCardList);
                            safetyCardListForRecyclerView.addAll(nullValidToApprovedList);
                            safetyCardListForRecyclerView.addAll(unApprovedCardList);
                            dbDelete.deleteTableByName("SafetyCards", "");
                            dataBaseHandlerInsert.addDataIntoSafetyCardTable(safetyCardListForRecyclerView);
                        }
                        if (safetyCardListForRecyclerView.size() > 0) {
                            recyclerView.setLayoutManager(new LinearLayoutManager(SafetyCards.this));
                            safetycardAdapter = new SafetyCardRecyclerViewAdapter(SafetyCards.this, safetyCardListForRecyclerView);
                            recyclerView.setAdapter(safetycardAdapter);
                            rl_safetyCard_list.setVisibility(View.VISIBLE);
                            firstTimeView.setVisibility(View.GONE);
                        } else {
                            rl_safetyCard_list.setVisibility(View.GONE);
                            rl_report_entery.setVisibility(View.GONE);
                            firstTimeView.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                    swipelayout.setRefreshing(false);
                    Log.d("Error", ex.getMessage());
                } finally {
                    dismissWaitDialog();
                    swipelayout.setRefreshing(false);
                    getReportEntryButtonStatus();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                swipelayout.setRefreshing(false);
                getReportEntryButtonStatus();
                AlertDialogManager.showDialog(SafetyCards.this, getResources().getString(R.string.internetErrorTitle), VolleyErrorHandler.getErrorMessage(SafetyCards.this, error), false, null);

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
        RequestQueue requestQueue11 = Volley.newRequestQueue(SafetyCards.this);
        requestQueue11.add(stringRequest);
    }

    public void updateNotificationCount(final String NotificationType, final String UserID, final String notificationIDs) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationCount_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    int safetyCardNotificationCount = Integer.parseInt(dbSelect.getNotificationData("NotificationCountTable", "NotificationCount", "SafetyCard", spManager.getUserID(), "NotificationCount"));
                    dbDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "SafetyCard"});
                    int spTotalCount = Integer.parseInt(spManager.getTotalNotificationCount());
                    int total = spTotalCount - safetyCardNotificationCount;
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(SafetyCards.this, total);
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                } finally {
                    getSafetyCards();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getSafetyCards();
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(SafetyCards.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(SafetyCards.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(SafetyCards.this);
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

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SafetyCardsPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWaitDialog();
                    safetyCardRow.setClickable(true);
                    downloadPDF_File();
                } else {
                    safetyCardRow.setClickable(true);
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(SafetyCards.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(SafetyCards.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", SafetyCards.this.getPackageName(), null);
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
                        AlertDialogManager.showCustomDialog(SafetyCards.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                                ActivityCompat.requestPermissions(SafetyCards.this, new String[]{permission}, requestCode);
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

            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    commonIntentMethod(StartCheckInFacility.class);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    //todo
                }
                break;
        }
    }

    public void startDownloadingWithPermission(SafetyCardProperty SafetyCardInfo, String safetycardPDF_FileName, LinearLayout safetyCradRow) {
        this.safetyCardProperty = SafetyCardInfo;
        this.safetycardPDF_FileName = safetycardPDF_FileName;
        this.safetyCardRow = safetyCradRow;
        SafetyCardsPermissionsDispatcher.startDownloadingWithPermissionCheck(SafetyCards.this);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startDownloading() {
        showWaitDialog();
        downloadPDF_File();
    }

    public void showDiplomaPDF_File(File file) {
        dismissWaitDialog();
        if (!file.exists()) {
            AlertDialogManager.showDialog(SafetyCards.this, getResources().getString(R.string.bad_file_format), "No safetycard to show .", false, null);
        } else {
            //Start DOwnload And VIew Safety Card Analytics
            Bundle bundle = new Bundle();
            bundle.putString("SafetyCardView", "Yes");
            analytics.logEvent("SafetyCardView", bundle);
            //End DOwnload And VIew Safety Card Analytics

            Intent intent = new Intent(SafetyCards.this, SafetyCardsDetails.class);
            intent.putExtra("FileName", file.toString());
            intent.putExtra("pdfFileURL", safetyCardProperty.card_url);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            finish();
        }
    }

    public void downloadSafetCardFromServer(final String fileURL, final String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    request.accept("application/pdf");
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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
                    dismissWaitDialog();
                } catch (Exception ex) {
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + fileName + ".pdf");
                if (file.exists()) {
                    dismissWaitDialog();
                    Intent intent = new Intent(SafetyCards.this, SafetyCardsDetails.class);
                    intent.putExtra("FileName", file.getAbsolutePath());
                    intent.putExtra("pdfFileURL", safetyCardProperty.card_url);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    finish();
                } else {
                    dismissWaitDialog();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_safety_card), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void downloadPDF_File() {
        writeNoMediaFile();
        if (connectionDetector.isConnectingToInternet()) {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + safetycardPDF_FileName + ".pdf");
            String filePath = root.getAbsolutePath();
            File file = new File(filePath);
            if (file.exists()) {
                DeleteFile(file);
                downloadSafetCardFromServer(safetyCardProperty.card_url, safetycardPDF_FileName);
            } else {
                downloadSafetCardFromServer(safetyCardProperty.card_url, safetycardPDF_FileName);
            }
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + safetycardPDF_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDiplomaPDF_File(file);
            } else {
                AlertDialogManager.showDialog(SafetyCards.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
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
            File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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

    public void only_downloadPDF_File() {
        if (connectionDetector.isConnectingToInternet()) {
            if (downloadSafetyCardUrlList != null && downloadSafetyCardUrlList.size() > 0) {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + downloadSafetyCardUrlList.get(0).safetyCard_cardID + ".pdf");
                if (!file.exists()) {
                    if (50 <= freeSpaceMB) {
                        downloadFileFromServer(downloadSafetyCardUrlList.get(0).downloadURL, downloadSafetyCardUrlList.get(0).safetyCard_cardID);
                    } else {
                        AlertDialogManager.showDialog(SafetyCards.this, getString(R.string.not_enough_space), " ", false, null);
                    }
                } else {
                    // new functinality added to delete file when already downloaded
                    DeleteFile(file);
                    if (50 <= freeSpaceMB) {
                        downloadFileFromServer(downloadSafetyCardUrlList.get(0).downloadURL, downloadSafetyCardUrlList.get(0).safetyCard_cardID);
                    } else {
                        AlertDialogManager.showDialog(SafetyCards.this, getString(R.string.not_enough_space), " ", false, null);
                    }
                    downloadSafetyCardUrlList.remove(downloadSafetyCardUrlList.get(0));
                    if (downloadSafetyCardUrlList.size() > 0) {
                        only_downloadPDF_File();
                    } else {
                        dismissWaitDialog();
                    }
                }
            } else {
                dismissWaitDialog();
            }
        } else {
            AlertDialogManager.showDialog(SafetyCards.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(SafetyCards.this, HomePage.class);
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
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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
                    //dismissWaitDialog();
                } catch (Exception ex) {
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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
                    if (downloadSafetyCardUrlList.size() > 0) {
                        downloadSafetyCardUrlList.remove(downloadSafetyCardUrlList.get(0));
                        if (downloadSafetyCardUrlList.size() > 0) {
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
                        syncIncompleteDialog = AlertDialogManager.showDialog(SafetyCards.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.sync_incomplete), false, new IClickListener() {
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

    private BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            NotificationProperty notificationData = dbSelect.notificationDataToBeUpdated("NotificationCountTable", spManager.getUserID(), "SafetyCard");
            if ((netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) || (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                if (syncIncompleteDialog != null) {
                    syncIncompleteDialog.dismiss();
                    syncIncompleteDialog = null;
                }
                showWaitDialog();
                if (notificationData.notification_id == null) {
                    notificationData = dbSelect.notificationDataToBeUpdated("UpdateNotification", spManager.getUserID(), "SafetyCard");
                }
                if (notificationData.notification_id != null) {
                    updateNotificationCount("SafetyCard", spManager.getUserID(), notificationData.notification_id);
                } else {
                    getSafetyCards();
                }
            } else {
                dismissWaitDialog();
                int safetyCardNotificationCount = notificationData.notification_count;
                String notificationIDs = notificationData.notification_id;
                if (safetyCardNotificationCount != 0) {
                    dbDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "SafetyCard"});
                    if (dbSelect.getDataFromNotificationUpdateTable(spManager.getUserID(), "SafetyCard").size() == 0) {
                        NotificationProperty notificationInfo = new NotificationProperty();
                        notificationInfo.user_id = spManager.getUserID();
                        notificationInfo.notification_type = "SafetyCard";
                        notificationInfo.notification_count = safetyCardNotificationCount;
                        notificationInfo.device_type = "android";
                        notificationInfo.device_id = connectionDetector.getAndroid_ID(SafetyCards.this);
                        notificationInfo.notification_id = notificationIDs;
                        dataBaseHandlerInsert.addDataIntoNotificationUpdateTable(notificationInfo);
                    }
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - safetyCardNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(SafetyCards.this, total);
                }
            }
        }
    };

    class test extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    // added on 28-08-2020
    public void getReportEntryButtonStatus() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.GetReportEntery, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("hasAccess")) {
                                hasEntery = jsonObject.getBoolean("hasAccess");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (hasEntery) {
                        rl_report_entery.setVisibility(View.VISIBLE);
                    } else {
                        rl_report_entery.setVisibility(View.GONE);
                    }

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
