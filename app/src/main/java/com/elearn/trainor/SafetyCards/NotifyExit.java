package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotifyExit extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    LinearLayout ll_back, llhome;
    TextView text_header, txt_facilityName,txt_hour_spent;
    RelativeLayout rl_notify_exit;
    private Location currentLocation;
    ConnectionDetector connectionDetector;
    String facilityName, entryId, spentTime,latitude,longitude;
    SharedPreferenceManager spManager;
    ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_exit);
        getControls();
    }

    public void getControls() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        connectionDetector = new ConnectionDetector(this);
        spManager = new SharedPreferenceManager(this);
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText(R.string.notify_exit_header);
        rl_notify_exit = findViewById(R.id.rl_notify_exit);
        txt_facilityName = findViewById(R.id.txt_facilityName);
        txt_hour_spent = findViewById(R.id.txt_hour_spent);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_notify_exit.setOnClickListener(this);
        if (getIntent().getStringExtra("FacilityName") != null && !Objects.equals(getIntent().getStringExtra("FacilityName"), "")) {
            facilityName = getIntent().getStringExtra("FacilityName");
        }
        if (getIntent().getStringExtra("EntryId") != null && !Objects.equals(getIntent().getStringExtra("EntryId"), "")) {
            entryId = getIntent().getStringExtra("EntryId");
        }
        if (getIntent().getStringExtra("SpentTime") != null && !Objects.equals(getIntent().getStringExtra("SpentTime"), "")) {
            spentTime = getIntent().getStringExtra("SpentTime");
        }
        if (getIntent().getStringExtra("Latitude") != null && !Objects.equals(getIntent().getStringExtra("Latitude"), "")) {
            latitude = getIntent().getStringExtra("Latitude");
        }
        if (getIntent().getStringExtra("Longitude") != null && !Objects.equals(getIntent().getStringExtra("Longitude"), "")) {
            longitude = getIntent().getStringExtra("Longitude");
        }
        txt_facilityName.setText(facilityName);
        txt_hour_spent.setText(spentTime+" since entry.");
        fetchLocation();
    }

    private void fetchLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(homelocation -> {
            if (homelocation != null) {
                currentLocation = homelocation;
                SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_facility));
                map.getMapAsync(this);

            }

        });
    }

    @Override
    public void onMapReady(GoogleMap homeMap) {
       /* homeMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        homeMap.clear();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        homeMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        homeMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        homeMap.addMarker(markerOptions);*/
        LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(facilityName);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon));
        homeMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        homeMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        homeMap.setBuildingsEnabled(true);
        homeMap.setIndoorEnabled(true);
        homeMap.addMarker(markerOptions);
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
            case R.id.rl_notify_exit:
                if(connectionDetector.isConnectingToInternet()){
                    showWaitDialog();
                    callCheckOutApi(entryId);
                }else{
                    AlertDialogManager.showDialog(NotifyExit.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, null);
                }
                break;
        }
    }

    public void callCheckOutApi(String entryId){
        String url = WebServicesURL.CheckOut + entryId;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {

                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);

                    }
                } catch (JSONException e) {
                    dismissWaitDialog();
                    e.printStackTrace();
                } finally {
                    if(response.equals("200")){
                        AlertDialogManager.showDialog(NotifyExit.this, "", facilityName +" is sucessfully checked out.", false, new IClickListener() {
                            @Override
                            public void onClick() {
                                commonIntentMethod(CheckedInFacility.class);
                            }
                        });
                       /* Intent intent = new Intent(NotifyExit.this, CheckedInFacility.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("RemainedTimer","2hr and 13min since entry");
                        startActivity(intent);*/
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int statuscode = error.networkResponse.statusCode;
                if (statuscode == 403 || statuscode == 404 || statuscode == 400) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        //JSONArray errors = data.getJSONArray("errors");
                        //JSONObject jsonMessage = errors.getJSONObject(0);
                        String message = data.getString("message");
                        AlertDialogManager.showDialog(NotifyExit.this, "", message, false, new IClickListener() {
                            @Override
                            public void onClick() {
                                commonIntentMethod(CheckedInFacility.class);
                            }
                        });
                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        Log.d("Exception: ", Objects.requireNonNull(e.getMessage()));
                    }
                }
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
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(CheckedInFacility.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(NotifyExit.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(NotifyExit.this);
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
}