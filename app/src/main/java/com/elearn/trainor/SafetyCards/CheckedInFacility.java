package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;

public class CheckedInFacility extends AppCompatActivity implements View.OnClickListener{

    LinearLayout ll_back, llhome;
    TextView text_header;
    RelativeLayout rl_checked_in_facility,rl_enter_new_facility,rl_notify_exit,rl_extend_time, rl_update_hours_des, rl_update_work_hr;
    TextView txt_companyName,txt_hour_spent,txt_guest;
    String updateHourVisibility = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_in_facility);
        getControls();
    }

    public void getControls() {
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText("Sikkerhetskort.no");
        rl_enter_new_facility = findViewById(R.id.rl_enter_new_facility);
        rl_checked_in_facility = findViewById(R.id.rl_checked_in_facility);
        rl_notify_exit = findViewById(R.id.rl_notify_exit);
        rl_extend_time = findViewById(R.id.rl_extend_time);
        txt_companyName = findViewById(R.id.txt_companyName);
        txt_hour_spent = findViewById(R.id.txt_hour_spent);
        txt_guest = findViewById(R.id.txt_guest);
        rl_update_hours_des = findViewById(R.id.rl_update_hours_des);
        rl_update_work_hr = findViewById(R.id.rl_update_work_hr);


        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_enter_new_facility.setOnClickListener(this);
        rl_notify_exit.setOnClickListener(this);
        rl_extend_time.setOnClickListener(this);
        rl_update_work_hr.setOnClickListener(this);

        rl_checked_in_facility.setVisibility(View.VISIBLE);
        rl_enter_new_facility.setVisibility(View.VISIBLE);
        rl_update_hours_des.setVisibility(View.GONE);
        rl_update_work_hr.setVisibility(View.GONE);

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
                commonIntentMethod(StartCheckInFacility.class);
                break;
            case R.id.rl_notify_exit:
                commonIntentMethod(NotifyExit.class);
                break;
            case R.id.rl_extend_time:
                updateHourVisibility = "visibleUpdateHour";
                rl_checked_in_facility.setVisibility(View.GONE);
                rl_enter_new_facility.setVisibility(View.GONE);
                rl_update_hours_des.setVisibility(View.VISIBLE);
                rl_update_work_hr.setVisibility(View.VISIBLE);
                break;
            case R.id.rl_update_work_hr:
                commonIntentMethod(CheckedInFacility.class);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(updateHourVisibility.equals("visibleUpdateHour")){
            updateHourVisibility = "";
            commonIntentMethod(CheckedInFacility.class);
        }else{
            commonIntentMethod(SafetyCards.class);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }

    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(CheckedInFacility.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}