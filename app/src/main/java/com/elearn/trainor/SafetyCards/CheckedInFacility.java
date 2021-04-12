package com.elearn.trainor.SafetyCards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.elearn.trainor.BaseAdapters.CheckedInFacilityAdapter;
import com.elearn.trainor.BaseAdapters.NearByFacilityAdapter;
import com.elearn.trainor.BaseAdapters.SafetyCardRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.DashboardClasses.LoadingPageActivity;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.AppConstants;
import com.elearn.trainor.HelperClasses.ComparatorHelperSafetyCards;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.GpsUtils;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.Login;
import com.elearn.trainor.PropertyClasses.CheckedInFacilityProperty;
import com.elearn.trainor.PropertyClasses.DownloadUrlProperty;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.R;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.fabric.sdk.android.services.network.HttpRequest;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CheckedInFacility extends AppCompatActivity implements View.OnClickListener{
    public static CheckedInFacility instance;
    LinearLayout ll_back, llhome;
    TextView text_header;
    RelativeLayout rl_checked_in_facility,rl_enter_new_facility,rl_add_more_cards;
    TextView txt_companyName,txt_hour_spent,txt_guest;
    String updateHourVisibility = "";
    SharedPreferenceManager spManager;
    SharedPreferences.Editor editor;
    ConnectionDetector connectionDetector;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    List<ReportEntryProperty> checkedInFacilityList;
    RecyclerView rv_checked_in_facility, rv_safetyCards;
    CheckedInFacilityAdapter checkedInFacilityAdapter;
    ProgressDialog pDialog;
    private boolean isGPS = false;
    List<SafetyCardProperty> safetyCardListForRecyclerView;
    List<SafetyCardProperty> approvedCardList;
    List<SafetyCardProperty> unApprovedCardList;
    List<SafetyCardProperty> nullValidToApprovedList;
    SafetyCardRecyclerViewAdapter safetycardAdapter;
    Boolean hasEntery;
    SafetyCardProperty safetyCardProperty;
    String safetycardPDF_FileName = "", fromPage;
    LinearLayout safetyCardRow,ll_checked_in_facility;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_in_facility);
        instance = this;
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });
        getControls();
    }

    public static CheckedInFacility getInstance() {
        if (instance == null) {
            instance = new CheckedInFacility();
        }
        return instance;
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(CheckedInFacility.this);
        editor = spManager.backToSafetCardPref();
        connectionDetector = new ConnectionDetector(this);
        dbInsert = new DataBaseHandlerInsert(this);
        dbDelete = new DataBaseHandlerDelete(this);
        dbSelect = new DataBaseHandlerSelect(this);
        dbUpdate = new DataBaseHandlerUpdate(this);
        checkedInFacilityList = new ArrayList<>();

        safetyCardListForRecyclerView = new ArrayList<>();
        approvedCardList = new ArrayList<>();
        nullValidToApprovedList = new ArrayList<>();
        unApprovedCardList = new ArrayList<>();
        rv_safetyCards = (RecyclerView) findViewById(R.id.rv_safetyCards);
        rv_safetyCards.setNestedScrollingEnabled(false);

        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText("Sikkerhetskort.no");
        rl_enter_new_facility = findViewById(R.id.rl_enter_new_facility);
        rl_checked_in_facility = findViewById(R.id.rl_checked_in_facility);
        txt_companyName = findViewById(R.id.txt_companyName);
        txt_hour_spent = findViewById(R.id.txt_hour_spent);
        txt_guest = findViewById(R.id.txt_guest);
        rv_checked_in_facility = findViewById(R.id.rv_checked_in_facilities);
        ll_checked_in_facility = findViewById(R.id.ll_checked_in_facility);
        rl_add_more_cards = findViewById(R.id.rl_add_more_cards);

        rl_add_more_cards.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_enter_new_facility.setOnClickListener(this);

        //rl_checked_in_facility.setVisibility(View.VISIBLE);
        //rl_enter_new_facility.setVisibility(View.VISIBLE);

        if(connectionDetector.isConnectingToInternet()){
            showWaitDialog();
            callgetActiveEntryAPI();
        }else{
            checkedInFacilityList.clear();
            checkedInFacilityList = dbSelect.getFacilityListFromReportEntryTable(spManager.getUserID(),"checked_in");
            if(checkedInFacilityList.size()>0){
                ll_checked_in_facility.setVisibility(View.VISIBLE);
                final LinearLayoutManager layoutManager = new LinearLayoutManager(CheckedInFacility.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rv_checked_in_facility.setLayoutManager(layoutManager);
                checkedInFacilityAdapter = new CheckedInFacilityAdapter(CheckedInFacility.this, checkedInFacilityList);
                rv_checked_in_facility.setAdapter(checkedInFacilityAdapter);
                checkedInFacilityAdapter.notifyDataSetChanged();
            }else{
                ll_checked_in_facility.setVisibility(View.GONE);
              /*  AlertDialogManager.showDialog(CheckedInFacility.this, "", "No data for checked in facility offline", false, new IClickListener() {
                    @Override
                    public void onClick() {
                        commonIntentMethod(HomePage.class);
                    }
                });*/
            }
            safetyCardListForRecyclerView.clear();
            safetyCardListForRecyclerView = dbSelect.getSafetyCardAttribute("");
            if(safetyCardListForRecyclerView.size()>0){
                rv_safetyCards.setLayoutManager(new LinearLayoutManager(CheckedInFacility.this));
                safetycardAdapter = new SafetyCardRecyclerViewAdapter(CheckedInFacility.this, safetyCardListForRecyclerView,"CheckedInFacility");
                rv_safetyCards.setAdapter(safetycardAdapter);
                rl_enter_new_facility.setVisibility(View.VISIBLE);
            }else{
                rl_enter_new_facility.setVisibility(View.GONE);
            }

        }
        getReportEntryButtonStatus();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(CheckedInFacility.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(CheckedInFacility.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CheckedInFacility.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            commonIntentMethod(StartCheckInFacility.class);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        }
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CheckedInFacilityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    commonIntentMethod(StartCheckInFacility.class);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    //todo
                }
                break;
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWaitDialog();
                    safetyCardRow.setClickable(true);
                    downloadPDF_File();
                } else {
                    safetyCardRow.setClickable(true);
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(CheckedInFacility.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(CheckedInFacility.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", CheckedInFacility.this.getPackageName(), null);
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
                        AlertDialogManager.showCustomDialog(CheckedInFacility.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                                ActivityCompat.requestPermissions(CheckedInFacility.this, new String[]{permission}, requestCode);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class);
                break;
            case R.id.rl_enter_new_facility:
                if (isGPS) {
                    getLocation();
                }
                //commonIntentMethod(StartCheckInFacility.class);
                break;

            case R.id.rl_update_work_hr:
                commonIntentMethod(CheckedInFacility.class);
                break;
            case R.id.rl_add_more_cards:
                if (connectionDetector.isConnectingToInternet()) {
                    editor.putString("GoToSafetyCard", "False");
                    editor.commit();
                    commonIntentMethod(VerifyInfo.class);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    AlertDialogManager.showDialog(CheckedInFacility.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(updateHourVisibility.equals("visibleUpdateHour")){
            updateHourVisibility = "";
            commonIntentMethod(CheckedInFacility.class);
        }else{
            commonIntentMethod(HomePage.class);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }

    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(CheckedInFacility.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
                            checkedInFacilityList.clear();
                            //dbDelete.deleteTableByName("ReportEntry", "");
                            //dbDelete.deleteTableByName("CheckedInFacility","");
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
                                dbUpdate.updateFacilityData("FacilityTable",spManager.getUserID(),property.facilityId,"employeeCheckinState",property.state);
                                if(property.state.equals("checked_in")){
                                    checkedInFacilityList.add(property);
                                }
                            }
                        }

                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                } finally {
                    //dismissWaitDialog();
                   if(checkedInFacilityList.size()>0){
                       ll_checked_in_facility.setVisibility(View.VISIBLE);
                       final LinearLayoutManager layoutManager = new LinearLayoutManager(CheckedInFacility.this);
                       layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                       rv_checked_in_facility.setLayoutManager(layoutManager);
                       checkedInFacilityAdapter = new CheckedInFacilityAdapter(CheckedInFacility.this, checkedInFacilityList);
                       rv_checked_in_facility.setAdapter(checkedInFacilityAdapter);
                       checkedInFacilityAdapter.notifyDataSetChanged();
                   }else{
                       ll_checked_in_facility.setVisibility(View.GONE);
                   }
                    getSafetyCards();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               /* int statuscode = error.networkResponse.statusCode;
                if (statuscode == 403 || statuscode == 404 || statuscode == 400) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        String message = data.getString("message");
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        Log.d("Exception: ", Objects.requireNonNull(e.getMessage()));
                    }//Toast.makeText(ReportEntry.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(CheckedInFacility.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }*/
                getSafetyCards();
                Toast.makeText(CheckedInFacility.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                dismissWaitDialog();
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

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(CheckedInFacility.this);
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

    public void dismissWaitDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public void getSafetyCards() {
        showWaitDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Upcoming_SafetyCards_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dbDelete.deleteTableByName("SafetyCards", "");
                safetyCardListForRecyclerView.clear();
                approvedCardList.clear();
                unApprovedCardList.clear();
                nullValidToApprovedList.clear();
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
                                property.confirmed = jsonObject.getString("confirmed");
                                if (property.approval_status != null && property.approval_status.equals("true")) {
                                    approvedCardList.add(property);
                                    DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                    downloadUrlProperty.downloadURL = property.card_url;
                                    downloadUrlProperty.safetyCard_cardID = property.card_id;
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
                            dbInsert.addDataIntoSafetyCardTable(safetyCardListForRecyclerView);
                        }

                        if (safetyCardListForRecyclerView.size() > 0) {
                            rv_safetyCards.setLayoutManager(new LinearLayoutManager(CheckedInFacility.this));
                            safetycardAdapter = new SafetyCardRecyclerViewAdapter(CheckedInFacility.this, safetyCardListForRecyclerView,"CheckedInFacility");
                            rv_safetyCards.setAdapter(safetycardAdapter);
                            rl_enter_new_facility.setVisibility(View.VISIBLE);
                        } else {
                            rl_enter_new_facility.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                    Log.d("Error", ex.getMessage());
                } finally {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(CheckedInFacility.this);
        requestQueue11.add(stringRequest);
    }

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
                        rl_enter_new_facility.setVisibility(View.VISIBLE);
                    } else {
                        //rl_enter_new_facility.setVisibility(View.VISIBLE);
                        rl_enter_new_facility.setVisibility(View.GONE);
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
                rl_enter_new_facility.setVisibility(View.GONE);
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

    // ***************************************

    public void startDownloadingWithPermission(SafetyCardProperty SafetyCardInfo, String safetycardPDF_FileName, LinearLayout safetyCradRow, String from) {
        this.safetyCardProperty = SafetyCardInfo;
        this.safetycardPDF_FileName = safetycardPDF_FileName;
        this.safetyCardRow = safetyCradRow;
        this.fromPage = from;
        CheckedInFacilityPermissionsDispatcher.startDownloadingFacilityWithPermissionCheck(CheckedInFacility.this);

    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startDownloadingFacility() {
        showWaitDialog();
        downloadPDF_File();
    }

    public void showDiplomaPDF_File(File file) {
        dismissWaitDialog();
        if (!file.exists()) {
            AlertDialogManager.showDialog(CheckedInFacility.this, getResources().getString(R.string.bad_file_format), "", false, null);
        } else {
            //Start DOwnload And VIew Safety Card Analytics
            Bundle bundle = new Bundle();
            bundle.putString("SafetyCardView", "Yes");
            Intent intent = new Intent(CheckedInFacility.this, SafetyCardsDetails.class);
            intent.putExtra("FileName", file.toString());
            intent.putExtra("pdfFileURL", safetyCardProperty.card_url);
            intent.putExtra("FROM", "CheckedInFacility");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            finish();
        }
    }

    public void downloadPDF_File() {
        if (connectionDetector.isConnectingToInternet()) {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + safetycardPDF_FileName + ".pdf");
            String filePath = root.getAbsolutePath();
            File file = new File(filePath);
            if (file.exists()) {
                DeleteFile(file);
                downloadSafetCardFromServer(safetyCardProperty.card_url, safetycardPDF_FileName);
            } else {
                downloadSafetCardFromServer(safetyCardProperty.card_url, safetycardPDF_FileName);
            }
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + safetycardPDF_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDiplomaPDF_File(file);
            } else {
                AlertDialogManager.showDialog(CheckedInFacility.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        dismissWaitDialog();
                    }
                });
            }
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
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/");
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
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.SafetyCards/" + fileName + ".pdf");
                if (file.exists()) {
                    dismissWaitDialog();
                    Intent intent = new Intent(CheckedInFacility.this, SafetyCardsDetails.class);
                    intent.putExtra("FileName", file.getAbsolutePath());
                    intent.putExtra("pdfFileURL", safetyCardProperty.card_url);
                    intent.putExtra("FROM", fromPage);
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

    void DeleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteFile(child);
            }
        }
        fileOrDirectory.delete();
    }
}