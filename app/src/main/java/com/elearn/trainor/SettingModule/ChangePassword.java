package com.elearn.trainor.SettingModule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
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
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.Login;
import com.elearn.trainor.R;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.SplashActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity implements View.OnClickListener {
    Button btn_ChangePassword;
    EditText edt_oldPassword, edt_newPassword, edt_repeatNewPassword;
    TextView txtResetLink, tv_settings;
    LinearLayout ll_back, llhome, main_layout;
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;
    String change_password, change_pwd_success, change_pwd_faliure, enter_old_pwd, enter_new_pwd, enter_repeate_new_pwd, password_not_matched;
    DataBaseHandlerDelete dbDelete;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "ChangePassword", this.getClass().getSimpleName());
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        dbDelete = new DataBaseHandlerDelete(ChangePassword.this);
        change_password = getResources().getString(R.string.change_password);
        change_pwd_success = getResources().getString(R.string.change_pwd_success);
        change_pwd_faliure = getResources().getString(R.string.change_pwd_faliure);
        //response = getResources().getString(R.string.response);
        enter_old_pwd = getResources().getString(R.string.enter_old_pwd);
        enter_new_pwd = getResources().getString(R.string.enter_new_pwd);
        enter_repeate_new_pwd = getResources().getString(R.string.enter_repeate_new_pwd);
        password_not_matched = getResources().getString(R.string.password_not_matched);
        spManager = new SharedPreferenceManager(ChangePassword.this);
        btn_ChangePassword = (Button) findViewById(R.id.btn_ChangePassword);
        edt_oldPassword = (EditText) findViewById(R.id.edt_oldPassword);
        edt_newPassword = (EditText) findViewById(R.id.edt_newPassword);
        edt_repeatNewPassword = (EditText) findViewById(R.id.edt_repeatNewPassword);
        txtResetLink = (TextView) findViewById(R.id.txtResetLink);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        main_layout = (LinearLayout) findViewById(R.id.main_layout);
        tv_settings = (TextView) findViewById(R.id.text_header);
        tv_settings.setText(change_password);
        connectionDetector = new ConnectionDetector(ChangePassword.this);
        btn_ChangePassword.setOnClickListener(this);
        txtResetLink.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ChangePassword:
                if (connectionDetector.isConnectingToInternet()) {
                    if (validation()) {
                        btn_ChangePassword.setClickable(false);
                        changePassword(edt_oldPassword.getText().toString().trim(), edt_newPassword.getText().toString().trim());
                    }
                } else {
                    AlertDialogManager.showDialog(ChangePassword.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                goTo(HomePage.class);
                break;
            case R.id.txtResetLink:
                if (connectionDetector.isConnectingToInternet()) {
                    callForgetPasswordApi(spManager.getUsername());
                } else {
                    AlertDialogManager.showDialog(ChangePassword.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
        }
    }

    public void changePassword(final String oldPassword, final String newPassword) {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, WebServicesURL.Change_Password_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                btn_ChangePassword.setClickable(true);
                try {
                    AlertDialogManager.showDialog(ChangePassword.this, "", change_pwd_success, false, new IClickListener() {
                        @Override
                        public void onClick() {
                            try {
                                dbDelete.deleteTableByName("SafetyCards", "");
                                dbDelete.deleteTableByName("DiplomasTable", spManager.getUserID());
                                dbDelete.deleteTableByName("CoursesTable", "");
                                dbDelete.deleteTableByName("MyCompanyTable", spManager.getUserID());
                                dbDelete.deleteTableByName("CustomerDetails", "");
                            } catch (Exception ex) {
                                Log.d("Error", ex.getMessage());
                            }
                            //stattnet functionality
                            SharedPreferences sharedpreferences = getSharedPreferences("StattnetPref", Context.MODE_PRIVATE);
                            if (sharedpreferences.contains("StattnetPrefValue")) {
                                String loginType = sharedpreferences.getString("StattnetPrefValue", "");
                                if (loginType != null && loginType.equals("StattnetLogin")) {
                                    goTo(SplashActivity.class);
                                } else {
                                    goTo(Login.class);
                                }
                            }else{
                                goTo(Login.class);
                            }
                        }
                    });
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                btn_ChangePassword.setClickable(true);
                try {
                    AlertDialogManager.showDialog(ChangePassword.this, "", change_pwd_faliure, false, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                params.put("Content-Type", "application/json");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"oldPassword\":\"" + oldPassword + "\",\"newPassword\":\"" + newPassword + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(ChangePassword.this);
        requestQueue11.add(stringRequest);
    }

    public void callForgetPasswordApi(final String username) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Forget_Password_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ChangePassword.this, getResources().getString(R.string.pwd_reset_link_sent), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChangePassword.this, getResources().getString(R.string.try_after_some_time), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"username\":\"" + username + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(ChangePassword.this);
        requestQueue11.add(stringRequest);
    }

    public boolean validation() {
        if (edt_oldPassword.getText().toString().trim().isEmpty()) {
            AlertDialogManager.showDialog(ChangePassword.this, "", enter_old_pwd, false, new IClickListener() {
                @Override
                public void onClick() {
                    edt_oldPassword.requestFocus();
                }
            });
            return false;
        }
        if (edt_newPassword.getText().toString().trim().isEmpty()) {
            AlertDialogManager.showDialog(ChangePassword.this, "", enter_new_pwd, false, new IClickListener() {
                @Override
                public void onClick() {
                    edt_newPassword.requestFocus();
                }
            });
            return false;
        }
        if (edt_repeatNewPassword.getText().toString().trim().isEmpty()) {
            AlertDialogManager.showDialog(ChangePassword.this, "", enter_repeate_new_pwd, false, new IClickListener() {
                @Override
                public void onClick() {
                    edt_repeatNewPassword.requestFocus();
                }
            });
            return false;
        }
        if (!edt_repeatNewPassword.getText().toString().trim().equals(edt_newPassword.getText().toString().trim())) {
            AlertDialogManager.showDialog(ChangePassword.this, "", password_not_matched, false, null);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        goTo(Settings.class);
    }

    public void goTo(Class activity) {
        Intent intentSetting = new Intent(ChangePassword.this, activity);
        intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentSetting);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
