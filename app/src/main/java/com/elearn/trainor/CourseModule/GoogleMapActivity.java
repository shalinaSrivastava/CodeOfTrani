package com.elearn.trainor.CourseModule;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double latitude, longitude;
    String address, markerAddress;
    SupportMapFragment mapFragment;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        analytics = FirebaseAnalytics.getInstance(this);

        address = getIntent().getStringExtra("CourseLocation");
        markerAddress = address;
        address = address.replaceAll(" ", "%20");
        String url = "https://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false&key=AIzaSyBtI3avY8gxHkxLA6JNwlBCnpsEt8ZzFzA";
        getLatLngFromAddress(url);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "ClassroomMap", this.getClass().getSimpleName());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(markerAddress));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        mMap.setMinZoomPreference(18.0f);
    }

    public void getLatLngFromAddress(final String google_api_url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, google_api_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        JSONObject jsonResultObject = jsonArray.getJSONObject(0);
                        if (jsonResultObject.has("geometry")) {
                            JSONObject jsonGelometryObject = jsonResultObject.getJSONObject("geometry");
                            JSONObject jsonLocationObject = jsonGelometryObject.getJSONObject("location");
                            latitude = Double.parseDouble(jsonLocationObject.getString("lat"));
                            longitude = Double.parseDouble(jsonLocationObject.getString("lng"));
                            mapFragment.getMapAsync(GoogleMapActivity.this);
                        }
                    }
                } catch (Exception ex) {
                    Log.d("", "");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialogManager.showDialog(GoogleMapActivity.this, "", getResources().getString(R.string.address_not_found), false, null);
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(GoogleMapActivity.this);
        requestQueue11.add(stringRequest);
    }
}
