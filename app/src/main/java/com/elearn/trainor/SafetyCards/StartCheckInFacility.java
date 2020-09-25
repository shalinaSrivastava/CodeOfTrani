package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartCheckInFacility extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    LinearLayout ll_back, llhome;
    TextView text_header, txt_select_near_by, txt_no_facility_found;
    View line_view1, line_view4;
    SwipeRefreshLayout swipelayout;
    private Location currentLocation;
    private ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    List<FacilityProperty> nearByFacilityList, searchFacilityList;
    NearByFacilityAdapter nearByFacilityAdapter;
    SearchFacilityAdapter searchFacilityAdapter;
    RecyclerView rv_facility_nearby, rv_facility_search;
    Button btn_search;
    EditText edit_text_search;
    ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_facility);
        getControls();
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(StartCheckInFacility.this);
        nearByFacilityList = new ArrayList<>();
        searchFacilityList = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        connectionDetector = new ConnectionDetector(this);
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText("Check in to facility");
        swipelayout = findViewById(R.id.swiperefresh);
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

        swipelayout.setRefreshing(false);
        fetchLocation();
    }

    private void fetchLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(homelocation -> {
            if (homelocation != null) {
                currentLocation = homelocation;
                SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_facility));
                map.getMapAsync(this);
                showWaitDialog();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap homeMap) {
        homeMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        homeMap.clear();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        homeMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        homeMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        homeMap.addMarker(markerOptions);
        callSearchByLatLong();
        //saveLocationDataInPref(currentLocation.getLatitude(), currentLocation.getLongitude());
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
                    if (connectionDetector.isConnectingToInternet()) {
                        showWaitDialog();
                        callSearchByIdentifier(stringToSearch);
                    } else {
                        AlertDialogManager.showDialog(StartCheckInFacility.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                    }
                } else {
                    Toast.makeText(this, "Please input valid text", Toast.LENGTH_SHORT).show();
                }
                //commonIntentMethod(ReportEntry.class);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(SafetyCards.class);
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
                        nearByFacilityList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            FacilityProperty facility = new FacilityProperty();
                            facility.name = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("") ? "" : jsonObject.getString("name");
                            facility.customerName = jsonObject.getString("customerName") == null ? "" : jsonObject.getString("customerName").equals("") ? "" : jsonObject.getString("customerName");
                            facility.distanceInKm = jsonObject.getString("distanceInKm") == null ? "" : jsonObject.getString("distanceInKm").equals("") ? "" : jsonObject.getString("distanceInKm");
                            facility.customerId = jsonObject.getString("customerId") == null ? "" : jsonObject.getString("customerId").equals("") ? "" : jsonObject.getString("customerId");
                            facility.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("") ? "" : jsonObject.getString("id");
                            //facility.employeeCheckInState = jsonObject.getString("employeeCheckInState") == null ? "" : jsonObject.getString("employeeCheckInState").equals("") ? "" : jsonObject.getString("employeeCheckInState");
                            facility.imageUrl = jsonObject.getString("imageUrl") == null ? "" : jsonObject.getString("imageUrl").equals("") ? "" : jsonObject.getString("imageUrl");

                            nearByFacilityList.add(facility);
                        }
                    }
                } catch (Exception ex) {
                    line_view1.setVisibility(View.GONE);
                    txt_select_near_by.setVisibility(View.GONE);
                    Log.d("Exception", ex.getMessage());
                } finally {
                    if (nearByFacilityList.size() > 0) {
                        line_view1.setVisibility(View.VISIBLE);
                        txt_select_near_by.setVisibility(View.VISIBLE);
                        final LinearLayoutManager layoutManager = new LinearLayoutManager(StartCheckInFacility.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        rv_facility_nearby.setLayoutManager(layoutManager);
                        nearByFacilityAdapter = new NearByFacilityAdapter(StartCheckInFacility.this, nearByFacilityList);
                        rv_facility_nearby.setAdapter(nearByFacilityAdapter);
                        nearByFacilityAdapter.notifyDataSetChanged();
                    } else {
                        line_view1.setVisibility(View.GONE);
                        txt_select_near_by.setVisibility(View.GONE);
                    }
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog != null && pDialog.isShowing()) {
                    line_view1.setVisibility(View.GONE);
                    txt_select_near_by.setVisibility(View.GONE);
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
                    jsonBody.put("lat", "59.266832");
                    jsonBody.put("long", "10.409415");
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
                            //facility.employeeCheckInState = jsonObject.getString("employeeCheckInState") == null ? "" : jsonObject.getString("employeeCheckInState").equals("") ? "" : jsonObject.getString("employeeCheckInState");
                            //
                            // facility.imageUrl = jsonObject.getString("imageUrl") == null ? "" : jsonObject.getString("imageUrl").equals("") ? "" : jsonObject.getString("imageUrl");

                            searchFacilityList.add(facility);
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                } finally {
                    if (searchFacilityList.size() > 0) {
                        line_view4.setVisibility(View.VISIBLE);
                        txt_no_facility_found.setVisibility(View.GONE);
                        final LinearLayoutManager layoutManager = new LinearLayoutManager(StartCheckInFacility.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        rv_facility_search.setLayoutManager(layoutManager);
                        searchFacilityAdapter = new SearchFacilityAdapter(StartCheckInFacility.this, searchFacilityList);
                        rv_facility_search.setAdapter(searchFacilityAdapter);
                        searchFacilityAdapter.notifyDataSetChanged();
                    }else{
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
}