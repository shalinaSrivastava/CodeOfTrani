package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.elearn.trainor.BaseAdapters.NearByFacilityAdapter;
import com.elearn.trainor.BaseAdapters.SearchFacilityAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DashboardClasses.LoadingPageActivity;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.Login;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StartCheckInFacility extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    LinearLayout ll_back, llhome;
    TextView text_header, txt_select_near_by, txt_no_facility_found;
    View line_view1, line_view4;
    private Location currentLocation;
    private ProgressDialog pDialog;
    List<FacilityProperty> nearByFacilityListToShow, searchFacilityList;
    NearByFacilityAdapter nearByFacilityAdapter;
    SearchFacilityAdapter searchFacilityAdapter;
    RecyclerView rv_facility_nearby, rv_facility_search;
    Button btn_search;
    EditText edit_text_search;
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerSelect dbSelect;
    GoogleMap map;
    int checkedInFacilityCount = 0;
    List<ReportEntryProperty> checkedInFacilityList;
    int mapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_facility);
        getControls();
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(StartCheckInFacility.this);
        nearByFacilityListToShow = new ArrayList<>();
        searchFacilityList = new ArrayList<>();
        checkedInFacilityList = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        connectionDetector = new ConnectionDetector(this);
        dbInsert = new DataBaseHandlerInsert(this);
        dbDelete = new DataBaseHandlerDelete(this);
        dbSelect = new DataBaseHandlerSelect(this);
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText(R.string.checkin_facility);
        rv_facility_nearby = findViewById(R.id.rv_facility_nearby);
        btn_search = findViewById(R.id.btn_search);
        txt_select_near_by = findViewById(R.id.txt_select_near_by);
        line_view1 = findViewById(R.id.line_view1);
        edit_text_search = findViewById(R.id.edit_text_search);
        rv_facility_search = findViewById(R.id.rv_facility_search);
        txt_no_facility_found = findViewById(R.id.txt_no_facility_found);
        line_view4 = findViewById(R.id.line_view4);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        fetchLocation();
/*
        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            case ConnectionResult.SUCCESS:
                fetchLocation();
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(this, "Google Play Services Missing", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                //The user has to update play services
                Toast.makeText(this, "Update Google Play Services", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, GooglePlayServicesUtil.isGooglePlayServicesAvailable(this), Toast.LENGTH_SHORT).show();
        }*/
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(homelocation -> {
            if (homelocation != null) {
                currentLocation = homelocation;
                SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_facility));
                map.getMapAsync(this);
                showWaitDialog();
            } else {
                SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_facility));
                map.getMapAsync(this);
                LocationRequest mLocationRequest = LocationRequest.create();
                mLocationRequest.setInterval(60000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationCallback mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            if (location != null) {
                                //TODO: UI updates.
                                currentLocation = location;
                                showWaitDialog();
                            }
                        }
                    }
                };
                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap homeMap) {
        map = homeMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.clear();

        try {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            map.addMarker(markerOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (connectionDetector.isConnectingToInternet()) {
            callgetActiveEntryAPI();
        } else {
            nearByFacilityListToShow.clear();
            nearByFacilityListToShow = dbSelect.getFacilityListFromFacilityTable("checked_in");
            if (nearByFacilityListToShow.size() > 0) {
                addLocationsToMap();
                final LinearLayoutManager layoutManager = new LinearLayoutManager(StartCheckInFacility.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rv_facility_nearby.setLayoutManager(layoutManager);
                nearByFacilityAdapter = new NearByFacilityAdapter(StartCheckInFacility.this, nearByFacilityListToShow);
                rv_facility_nearby.setAdapter(nearByFacilityAdapter);
                nearByFacilityAdapter.notifyDataSetChanged();
            } else {
                AlertDialogManager.showDialog(StartCheckInFacility.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(StartCheckInFacility.this, HomePage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
            dismissWaitDialog();
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
            case R.id.btn_search:
                String stringToSearch = edit_text_search.getText().toString().trim();
                if (!stringToSearch.equals("")) {
                    hideKeyboard(this);
                    if (connectionDetector.isConnectingToInternet()) {
                        showWaitDialog();
                        callSearchByIdentifier(stringToSearch);
                    } else {
                        AlertDialogManager.showDialog(StartCheckInFacility.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.input_valid_text), Toast.LENGTH_SHORT).show();
                }
                //commonIntentMethod(ReportEntry.class);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        checkedInFacilityList.clear();
        checkedInFacilityList = dbSelect.getFacilityListFromReportEntryTable(spManager.getUserID(),"checked_in");
        if(checkedInFacilityCount>0||checkedInFacilityList.size()>0){
            commonIntentMethod(CheckedInFacility.class);
        }else{
            commonIntentMethod(SafetyCards.class);
        }
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(StartCheckInFacility.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(StartCheckInFacility.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public void callSearchByLatLong() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.NearByFacility, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        nearByFacilityListToShow.clear();
                        dbDelete.deleteTableByName("FacilityTable", "");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            FacilityProperty facility = new FacilityProperty();
                            facility.name = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("") ? "" : jsonObject.getString("name");
                            facility.customerName = jsonObject.getString("customerName") == null ? "" : jsonObject.getString("customerName").equals("") ? "" : jsonObject.getString("customerName");
                            facility.distanceInKm = jsonObject.getString("distanceInKm") == null ? "" : jsonObject.getString("distanceInKm").equals("") ? "" : jsonObject.getString("distanceInKm");
                            facility.customerId = jsonObject.getString("customerId") == null ? "" : jsonObject.getString("customerId").equals("") ? "" : jsonObject.getString("customerId");
                            facility.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("") ? "" : jsonObject.getString("id");
                            facility.employeeCheckInState = jsonObject.getString("employeeCheckinState") == null ? "" : jsonObject.getString("employeeCheckinState").equals("") ? "" : jsonObject.getString("employeeCheckinState");
                            facility.imageUrl = jsonObject.getString("imageUrl") == null ? "" : jsonObject.getString("imageUrl").equals("") ? "" : jsonObject.getString("imageUrl");
                            facility.allowGuests = jsonObject.getString("allowGuests") == null ? "" : jsonObject.getString("allowGuests").equals("") ? "" : jsonObject.getString("allowGuests");
                            facility.latitude = jsonObject.getString("latitude") == null ? "" : jsonObject.getString("latitude").equals("") ? "" : jsonObject.getString("latitude");
                            facility.longitude = jsonObject.getString("longitude") == null ? "" : jsonObject.getString("longitude").equals("") ? "" : jsonObject.getString("longitude");
                            //added on 02-03-2021
                            //facility.requireProjectNumber = jsonObject.getString("requireProjectNumber") == null ? "" : jsonObject.getString("requireProjectNumber").equals("") ? "" : jsonObject.getString("requireProjectNumber");
                            //facility.requireProjectNumber = "true"; // for testing only
                            if (jsonObject.has("requireProjectNumber")) {
                                facility.requireProjectNumber = jsonObject.getString("requireProjectNumber") == null ? "" : jsonObject.getString("requireProjectNumber").equals("") ? "" : jsonObject.getString("requireProjectNumber");
                            } else {
                                facility.requireProjectNumber ="false";
                            }
                            dbInsert.addDataIntoFacilityTable(facility);
                            String checkedInStatus = dbSelect.getCheckedInStatusFromEntryTable(spManager.getUserID(), facility.id);
                            if (!checkedInStatus.equals("checked_in")) {
                                nearByFacilityListToShow.add(facility);
                            }
                            //nearByFacilityListToShow.add(facility);
                        }
                    }
                } catch (Exception ex) {
                    line_view1.setVisibility(View.GONE);
                    txt_select_near_by.setVisibility(View.GONE);
                    Log.d("Exception", ex.getMessage());
                } finally {
                    if (nearByFacilityListToShow.size() > 0) {
                        line_view1.setVisibility(View.VISIBLE);
                        txt_select_near_by.setVisibility(View.VISIBLE);
                        final LinearLayoutManager layoutManager = new LinearLayoutManager(StartCheckInFacility.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        rv_facility_nearby.setLayoutManager(layoutManager);
                        nearByFacilityAdapter = new NearByFacilityAdapter(StartCheckInFacility.this, nearByFacilityListToShow);
                        rv_facility_nearby.setAdapter(nearByFacilityAdapter);
                        nearByFacilityAdapter.notifyDataSetChanged();
                    } else {
                        line_view1.setVisibility(View.GONE);
                        txt_select_near_by.setVisibility(View.GONE);
                    }
                    addLocationsToMap();
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                line_view1.setVisibility(View.GONE);
                txt_select_near_by.setVisibility(View.GONE);
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

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    //jsonBody.put("lat", "59.266832");
                    //jsonBody.put("long", "10.409415");
                    jsonBody.put("lat",String.valueOf( currentLocation.getLatitude()));
                    jsonBody.put("long",String.valueOf(currentLocation.getLongitude()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    private final void addLocationsToMap() {
        for (FacilityProperty facility : nearByFacilityListToShow) {
            LatLng latLng = new LatLng(Double.parseDouble(facility.latitude), Double.parseDouble(facility.longitude));
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(facility.name);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            //map.animateCamera(CameraUpdateFactory.newLatLng(latLng)); // for testing only
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // for testing only

            map.setBuildingsEnabled(true);
            map.setIndoorEnabled(true);
            map.addMarker(markerOptions);
        }

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                String title = marker.getTitle();
                int position = -1;
                for (int i = 0; i < nearByFacilityListToShow.size(); i++) {
                    if (nearByFacilityListToShow.get(i).name.equals(title)) {
                        position = i;
                        break;  // uncomment to get the first instance
                    }
                }
                try {
                    FacilityProperty property = nearByFacilityListToShow.get(position);
                    if(property.employeeCheckInState.equals("checked_in")){
                        Intent intent = new Intent(StartCheckInFacility.this, CheckedInFacility.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else if(property.employeeCheckInState.equals("awaiting_approval")){
                        Intent intent = new Intent(StartCheckInFacility.this, ReportEntry.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("CompanyName",property.customerName);
                        intent.putExtra("FacilityName",property.name);
                        intent.putExtra("FacilityId", property.id);
                        intent.putExtra("FacilityCustomerId", property.customerId);
                        intent.putExtra("AllowGuest", property.allowGuests);
                        intent.putExtra("RequireProjectNum", property.requireProjectNumber); //added on 02-03-2021
                        startActivity(intent);

                    }else{
                        Intent intent = new Intent(StartCheckInFacility.this, ReportEntry.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("CompanyName",property.customerName);
                        intent.putExtra("FacilityName",property.name);
                        intent.putExtra("FacilityId", property.id);
                        intent.putExtra("FacilityCustomerId", property.customerId);
                        intent.putExtra("AllowGuest", property.allowGuests);
                        intent.putExtra("RequireProjectNum", property.requireProjectNumber); //added on 02-03-2021
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.d("Exception", " Occured");
                }

            }
        });

    }

    public void callSearchByIdentifier(String identifier) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.SearchFacility, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        searchFacilityList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            FacilityProperty facility = new FacilityProperty();
                            facility.name = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("") ? "" : jsonObject.getString("name");
                            facility.customerName = jsonObject.getString("customerName") == null ? "" : jsonObject.getString("customerName").equals("") ? "" : jsonObject.getString("customerName");
                            facility.distanceInKm = jsonObject.getString("distanceInKm") == null ? "" : jsonObject.getString("distanceInKm").equals("") ? "" : jsonObject.getString("distanceInKm");
                            facility.customerId = jsonObject.getString("customerId") == null ? "" : jsonObject.getString("customerId").equals("") ? "" : jsonObject.getString("customerId");
                            facility.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("") ? "" : jsonObject.getString("id");
                            facility.employeeCheckInState = jsonObject.getString("employeeCheckinState") == null ? "" : jsonObject.getString("employeeCheckinState").equals("") ? "" : jsonObject.getString("employeeCheckinState");
                            facility.imageUrl = jsonObject.getString("imageUrl") == null ? "" : jsonObject.getString("imageUrl").equals("") ? "" : jsonObject.getString("imageUrl");
                            facility.allowGuests = jsonObject.getString("allowGuests") == null ? "" : jsonObject.getString("allowGuests").equals("") ? "" : jsonObject.getString("allowGuests");
                            facility.latitude = jsonObject.getString("latitude") == null ? "" : jsonObject.getString("latitude").equals("") ? "" : jsonObject.getString("latitude");
                            facility.longitude = jsonObject.getString("longitude") == null ? "" : jsonObject.getString("longitude").equals("") ? "" : jsonObject.getString("longitude");

                            //added on 02-03-2021
                            //facility.requireProjectNumber = jsonObject.getString("requireProjectNumber") == null ? "" : jsonObject.getString("requireProjectNumber").equals("") ? "" : jsonObject.getString("requireProjectNumber");
                            //facility.requireProjectNumber = "true"; // for testing only
                            if (jsonObject.has("requireProjectNumber")) {
                                facility.requireProjectNumber = jsonObject.getString("requireProjectNumber") == null ? "" : jsonObject.getString("requireProjectNumber").equals("") ? "" : jsonObject.getString("requireProjectNumber");
                            } else {
                                facility.requireProjectNumber ="false";
                            }
                            searchFacilityList.add(facility);
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                } finally {
                    if (searchFacilityList.size() == 1) {
                        line_view4.setVisibility(View.VISIBLE);
                        txt_no_facility_found.setVisibility(View.GONE);
                       /* final LinearLayoutManager layoutManager = new LinearLayoutManager(StartCheckInFacility.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        rv_facility_search.setLayoutManager(layoutManager);
                        searchFacilityAdapter = new SearchFacilityAdapter(StartCheckInFacility.this, searchFacilityList);
                        rv_facility_search.setAdapter(searchFacilityAdapter);
                        searchFacilityAdapter.notifyDataSetChanged();*/
                       if(searchFacilityList.get(0).employeeCheckInState.equals("checked_in")){
                           AlertDialogManager.showDialog(StartCheckInFacility.this, "", searchFacilityList.get(0).name +" is already checked in.", false, new IClickListener() {
                               @Override
                               public void onClick() {
                                   commonIntentMethod(CheckedInFacility.class);
                               }
                           });
                           //commonIntentMethod(CheckedInFacility.class);
                       }else{
                           Intent intent = new Intent(StartCheckInFacility.this, ReportEntry.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           intent.putExtra("CompanyName",searchFacilityList.get(0).customerName);
                           intent.putExtra("FacilityName",searchFacilityList.get(0).name);
                           intent.putExtra("FacilityId", searchFacilityList.get(0).id);
                           intent.putExtra("FacilityCustomerId", searchFacilityList.get(0).customerId);
                           intent.putExtra("AllowGuest", searchFacilityList.get(0).allowGuests);
                           intent.putExtra("RequireProjectNum", searchFacilityList.get(0).requireProjectNumber); // added on 02-03-2021
                           startActivity(intent);
                       }
                    } else {
                        line_view4.setVisibility(View.VISIBLE);
                        txt_no_facility_found.setVisibility(View.VISIBLE);
                    }
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog != null && pDialog.isShowing()) {
                    line_view4.setVisibility(View.VISIBLE);
                    txt_no_facility_found.setVisibility(View.VISIBLE);
                    dismissWaitDialog();
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
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("facilityIdentifier", identifier);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
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
                                if(property.state.equals("checked_in")){
                                    checkedInFacilityCount++;
                                }
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
                } finally {
                    callSearchByLatLong();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int statuscode = error.networkResponse.statusCode;
                if (statuscode == 403 || statuscode == 404) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        String message = data.getString("message");
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        Log.d("Exception: ", Objects.requireNonNull(e.getMessage()));
                    }
                }
                callSearchByLatLong();
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

    public static void hideKeyboard(Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(((Activity) mContext).getWindow()
                    .getCurrentFocus().getWindowToken(), 0);
        }
    }
}