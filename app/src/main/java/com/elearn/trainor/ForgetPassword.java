package com.elearn.trainor;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.google.firebase.analytics.FirebaseAnalytics;

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener {
    TextView text_header, need_support, needSupport_matched, needSupport_unmatched, discriptionError;
    LinearLayout ll_back, llhome;
    Button btn_pwd_reset, backButton_matched, backButton_unmatched;
    EditText edt_username;
    RelativeLayout custom_alert_matched, main_layout, custom_alert_unmatched;
    boolean backflag = false;
    String not_found_msg, need_help_contact, forgot_pwd_header_text;
    ConnectionDetector connectionDetector;
    FirebaseAnalytics analytics;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        getControls();
        edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edt_username.getText().length() < 1) {
                    btn_pwd_reset.setAlpha((float) 0.4);
                    btn_pwd_reset.setEnabled(false);
                } else {
                    btn_pwd_reset.setAlpha((float) 1);
                    btn_pwd_reset.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "ForgetPassword", this.getClass().getSimpleName());
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        forgot_pwd_header_text = getResources().getString(R.string.forgot_pwd_header_text);
        not_found_msg = getResources().getString(R.string.not_found_msg);
        need_help_contact = getResources().getString(R.string.need_help_contact);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setVisibility(View.INVISIBLE);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        custom_alert_matched = (RelativeLayout) findViewById(R.id.custom_alert);
        custom_alert_unmatched = (RelativeLayout) findViewById(R.id.custom_alert1);
        text_header = (TextView) findViewById(R.id.text_header);
        backButton_matched = (Button) findViewById(R.id.back_button_matched);
        backButton_unmatched = (Button) findViewById(R.id.back_button_unmatched);
        needSupport_matched = (TextView) findViewById(R.id.needSupport_matched);
        needSupport_unmatched = (TextView) findViewById(R.id.needSupport_unmatched);
        need_support = (TextView) findViewById(R.id.tv_need_support);
        discriptionError = (TextView) findViewById(R.id.discriptionError);
        edt_username = (EditText) findViewById(R.id.edt_username);
        btn_pwd_reset = (Button) findViewById(R.id.btn_pwd_reset);
        connectionDetector = new ConnectionDetector(ForgetPassword.this);

        edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    if (s.toString().contains(" ")) {
                        String preValue = edt_username.getText().toString().substring(0, edt_username.getText().toString().indexOf(" "));
                        if (edt_username.getText().length() > 1) {
                            String postValue = edt_username.getText().toString().substring(edt_username.getText().toString().indexOf(" ") + 1, edt_username.getText().toString().length());
                            edt_username.setText(preValue + postValue);
                            edt_username.setSelection(preValue.length());
                        } else {
                            edt_username.setText(preValue);
                        }
                    }
                    // edt_username.setSelection(edt_username.getText().toString().length());
                }
            }
        });

        text_header.setText(forgot_pwd_header_text);
        edt_username.setText(getIntent().getStringExtra("username"));
        if (edt_username.getText().toString().isEmpty()) {
            btn_pwd_reset.setAlpha((float) 0.4);
            btn_pwd_reset.setEnabled(false);
        } else {
            btn_pwd_reset.setAlpha((float) 1);
            btn_pwd_reset.setEnabled(true);
        }
        btn_pwd_reset.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        backButton_matched.setOnClickListener(this);
        backButton_unmatched.setOnClickListener(this);
        needSupport_matched.setOnClickListener(this);
        needSupport_unmatched.setOnClickListener(this);
        need_support.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pwd_reset:
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    if (connectionDetector.isConnectingToInternet()) {
                        callForgetPasswordApi();
                        edt_username.setEnabled(false);
                    } else {
                        AlertDialogManager.showDialog(ForgetPassword.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                    }
                } catch (Exception e) {
                    Log.d("Error", e.getMessage());
                }
                break;
            case R.id.ll_back:
                if (backflag == true) {
                    main_layout.setVisibility(View.VISIBLE);
                    custom_alert_matched.setVisibility(View.GONE);
                    custom_alert_unmatched.setVisibility(View.GONE);
                    backflag = false;
                } else {
                    commonIntentMethod(ForgetPassword.this, Login.class);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                }
                break;
            case R.id.back_button_matched:
                commonIntentMethod(ForgetPassword.this, Login.class);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.back_button_unmatched:
                commonIntentMethod(ForgetPassword.this, ForgetPassword.class);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.needSupport_matched:
                commonIntentMethod(ForgetPassword.this, NeedSupport.class);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.needSupport_unmatched:
                commonIntentMethod(ForgetPassword.this, NeedSupport.class);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.tv_need_support:
                commonIntentMethod(ForgetPassword.this, NeedSupport.class);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (backflag) {
            main_layout.setVisibility(View.VISIBLE);
            custom_alert_matched.setVisibility(View.GONE);
            custom_alert_unmatched.setVisibility(View.GONE);
            backflag = false;
        } else {
            commonIntentMethod(ForgetPassword.this, Login.class);
        }
    }

    public void callForgetPasswordApi() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Forget_Password_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                main_layout.setVisibility(View.INVISIBLE);
                custom_alert_matched.setVisibility(View.VISIBLE);
                custom_alert_unmatched.setVisibility(View.INVISIBLE);
                edt_username.setEnabled(true);
                backflag = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 404) {
                    backflag = true;
                    String msg = not_found_msg + " '" + edt_username.getText().toString().trim() + "'" + ". ";
                    discriptionError.setText(msg + need_help_contact);
                    main_layout.setVisibility(View.INVISIBLE);
                    custom_alert_matched.setVisibility(View.INVISIBLE);
                    custom_alert_unmatched.setVisibility(View.VISIBLE);
                    edt_username.setEnabled(true);
                    backflag = true;
                }
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"username\":\"" + edt_username.getText().toString() + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(ForgetPassword.this);
        requestQueue11.add(stringRequest);
    }

    public void commonIntentMethod(Context con, Class activity) {
        Intent intent = new Intent(con, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("username", getIntent().getStringExtra("username"));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        this.finishAffinity();
    }
}
