package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elearn.trainor.HomePage;
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

public class NotifyExit extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationProviderClient;
    LinearLayout ll_back, llhome;
    TextView text_header;
    private Location currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_exit);
        getControls();
    }

    public void getControls() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText("Notify exit of facility");

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
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
        homeMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        homeMap.clear();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        homeMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        homeMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        homeMap.addMarker(markerOptions);
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

        }
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
}