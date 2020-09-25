package com.elearn.trainor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elearn.trainor.CourseModule.GetMoreCourses;
import com.elearn.trainor.HelperClasses.*;
import com.google.firebase.analytics.FirebaseAnalytics;

public class NeedSupport extends AppCompatActivity implements View.OnClickListener {

    TextView text_header, txtDescription;
    LinearLayout support_mail, ll_support_phone, ll_back, llhome;
    String need_support1, From = "", userID = "", courseID = "", orderID = "", timePurchase = "";
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_support);
        getControls();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "NeedSupport", this.getClass().getSimpleName());
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(NeedSupport.this);
        need_support1 = getResources().getString(R.string.need_support1);
        txtDescription = (TextView) findViewById(R.id.tv2);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setVisibility(View.INVISIBLE);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        text_header = (TextView) findViewById(R.id.text_header);
        support_mail = (LinearLayout) findViewById(R.id.ll_support_mail);
        ll_support_phone = (LinearLayout) findViewById(R.id.ll_support_phone);
        text_header.setText(need_support1);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        support_mail.setOnClickListener(this);
        ll_support_phone.setOnClickListener(this);
        From = getIntent().getStringExtra("From");
        if (From != null && From.equals("OrderCourse")) {
            userID = getIntent().getStringExtra("UserID");
            courseID = getIntent().getStringExtra("CourseID");
            orderID = getIntent().getStringExtra("OrderID");
            timePurchase = getIntent().getStringExtra("TimePurchase");
            txtDescription.setText(R.string.payment_need_support);
        }

        //Start Contact US Analytics
        Bundle bundle = new Bundle();
        bundle.putString("Contact_US", "Yes");
        analytics.logEvent("Contact_US", bundle);
        //End Contact US Analytics
    }

    @Override
    public void onBackPressed() {
        if (From != null && From.equals("OrderCourse")) {
            commonIntentMethod(NeedSupport.this, GetMoreCourses.class, "NeedSupport");
        } else {
            if (getIntent().getStringExtra("From") != null && getIntent().getStringExtra("From").equals("HomePage")) {
                commonIntentMethod(NeedSupport.this, HomePage.class, "NeedSupport");
            } else {
                commonIntentMethod(NeedSupport.this, ForgetPassword.class, "");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.ll_support_mail:
                try {
                    if (From != null && From.equals("OrderCourse")) {
                        String bodyText = getResources().getString(R.string.course_undelivered_mail_body);
                        String _userID = getResources().getString(R.string.payment_user_id);
                        String _courseID = getResources().getString(R.string.payment_course_id);
                        String _orderId = getResources().getString(R.string.payment_order_id);;
                        String _timeOfPurchase = getResources().getString(R.string.time_of_purchase);
                        String _fullBodyText = bodyText + _userID +" "+ userID + "\n" + _courseID +" "+ courseID + "\n"+ _orderId +" "+ orderID + "\n" + _timeOfPurchase +" "+ timePurchase;
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@trainor.no", ""));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.course_undelivered_mail_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, _fullBodyText);
                        startActivity(Intent.createChooser(emailIntent, ""));
                    } else {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@trainor.no", ""));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.android_mail_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(""));
                        startActivity(Intent.createChooser(emailIntent, ""));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ll_support_phone:
                try {
                    if (isTelephonyEnabled()) {
                        makeCall();
                    } else {
                        AlertDialogManager.showDialog(NeedSupport.this, "", getResources().getString(R.string.device_not_support_call_fun), false, null);
                    }
                } catch (Exception ex) {
                    AlertDialogManager.showDialog(NeedSupport.this, getResources().getString(R.string.call_permission_error), ex.getMessage().toString(), false, null);
                }
                break;
        }
    }

    public void makeCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:+4733378910"));
        startActivity(callIntent);
    }


    public void commonIntentMethod(Context con, Class activity, String From) {
        Intent intent = new Intent(con, activity);
        intent.putExtra("From", From);
        if (From.equals("")) {
            intent.putExtra("From", getIntent().getStringExtra("username"));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private boolean isTelephonyEnabled() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

}
