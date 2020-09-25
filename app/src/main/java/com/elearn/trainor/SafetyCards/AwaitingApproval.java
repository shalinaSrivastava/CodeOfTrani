package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;

public class AwaitingApproval extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header, txt_skip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awaiting_approval);
        getControls();
    }

    public void getControls() {
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText("Waiting for response");
        txt_skip = findViewById(R.id.txt_skip);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        txt_skip.setOnClickListener(this);
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
            case R.id.txt_skip:
                commonIntentMethod(CheckedInFacility.class);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(ReportEntry.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(AwaitingApproval.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}