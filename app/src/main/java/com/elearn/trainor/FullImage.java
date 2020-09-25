package com.elearn.trainor;

import android.content.Intent;
import com.elearn.trainor.HelperClasses.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.SettingModule.Settings;
import com.google.firebase.analytics.FirebaseAnalytics;

import it.sephiroth.android.library.picasso.Callback;

public class FullImage extends AppCompatActivity {
    ImageView fullimage;
    SharedPreferenceManager spManager;
    TextView text_header;
    LinearLayout ll_back, llhome;
    ConnectionDetector connectionDetector;
    DataBaseHandlerSelect dbSelect;
    FirebaseAnalytics analytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        getControls();
        if (spManager.getSharedPreferenceExistence()) {
            if (spManager.getProfileURL() != null && !spManager.getProfileURL().equals("invalid URL")) {
                PicasoImageLoader.setOfflineImage(FullImage.this, spManager.getProfileURL(), fullimage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        fullimage.setImageResource(R.drawable.ic_default_profile_pic);
                    }
                });
            } else {
                fullimage.setImageResource(R.drawable.ic_default_profile_pic);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "ProfileImage", this.getClass().getSimpleName());
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        dbSelect = new DataBaseHandlerSelect(FullImage.this);
        connectionDetector = new ConnectionDetector(FullImage.this);
        spManager = new SharedPreferenceManager(FullImage.this);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getResources().getString(R.string.profile_image));
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setVisibility(View.INVISIBLE);
        fullimage = (ImageView) findViewById(R.id.fullimage);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intentback = new Intent(FullImage.this, Settings.class);
        intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentback);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
