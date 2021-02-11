package com.elearn.trainor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DashboardClasses.LoadingPageActivity;
import com.elearn.trainor.HelperClasses.*;
import com.elearn.trainor.ToolBoxModule.ToolBox;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class Login extends AppCompatActivity implements View.OnClickListener {
    public static Login instance;
    ScalableVideoView mVideoView;
    LinearLayout llCredentials;
    Button btn_login, btn_register, btn_forget_pwd;
    TextInputEditText username;
    TextInputEditText user_password;
    TextInputLayout username_error, password_error;
    int pos;
    public boolean isActivityOnFront = false;
    ConnectionDetector connectionDetector;
    LinearLayout llToolBox, ll_forget_pwd;
    String devicelang, lang, lang_frm_apiResponse, firstName, userID, offlineDownloadedStatus = "";
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            devicelang = Resources.getSystem().getConfiguration().locale.getLanguage();
            if (devicelang.startsWith("en")) {
                lang = "en";
            } else if (devicelang.startsWith("nb")) {
                lang = "nb";
            } else if (devicelang.startsWith("pl")) {
                lang = "pl";
            } else if (devicelang.startsWith("ko")) {
                lang = "ko";
            } else if (devicelang.startsWith("sv")) {
                lang = "sv";
            } else if (devicelang.startsWith("pt")) {
                lang = "pt";
            } else {
                lang = "en";
            }

            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

            this.setContentView(R.layout.login);
            isActivityOnFront = true;
            getControls();
            if (savedInstanceState != null) {
                pos = savedInstanceState.getInt("pos");
            }

            mVideoView.setRawData(R.raw.trainor);
            mVideoView.setVolume(0, 0);
            mVideoView.setLooping(true);
            mVideoView.prepare(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoView.start();
                    mVideoView.seekTo(pos);
                }
            });
        } catch (Exception ex) {
            AlertDialogManager.showDialog(Login.this, getString(R.string.internetErrorTitle), ex.getMessage().toString(), false, null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mVideoView.isPlaying()) outState.putInt("pos", mVideoView.getCurrentPosition());
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
        isActivityOnFront = true;
        if (getIntent().getStringExtra("From") != null && getIntent().getStringExtra("From").equals("Toolbox")) {
            Intent intent = new Intent(Login.this, ToolBox.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    public static Login getInstance() {
        if (instance == null) {
            instance = new Login();
        }
        return instance;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityOnFront = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityOnFront = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(Login.this);
        /*Button crashButton = new Button(this);
        crashButton.setText("Crash!");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                throw new RuntimeException("Test Crash"); // Force a crash
            }
        });

        addContentView(crashButton, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));*/

        dbSelect = new DataBaseHandlerSelect(Login.this);
        dbInsert = new DataBaseHandlerInsert(Login.this);
        llToolBox = (LinearLayout) findViewById(R.id.llToolBox);
        connectionDetector = new ConnectionDetector(Login.this);
        pDialog = new ProgressDialog(this, R.style.progressDialoguetheme);
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        pDialog.setIndeterminate(true);
        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ll_forget_pwd = (LinearLayout) findViewById(R.id.ll_forget_pwd);
        ll_forget_pwd.setAlpha((float) 0.5);
        llCredentials = (LinearLayout) findViewById(R.id.llCredentials);
        llCredentials.setAlpha((float) 0.9);
        mVideoView = (ScalableVideoView) findViewById(R.id.video_view);
        pos = 0;
        username = (TextInputEditText) findViewById(R.id.login_username);
        user_password = (TextInputEditText) findViewById(R.id.user_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_forget_pwd = (Button) findViewById(R.id.btn_forget_pwd);
        btn_register = (Button) findViewById(R.id.btn_register);
        username_error = (TextInputLayout) findViewById(R.id.text_input_layout);
        password_error = (TextInputLayout) findViewById(R.id.text_input_layout2);
        btn_login.setOnClickListener(this);
        btn_login.setFocusable(false);
        btn_forget_pwd.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        llToolBox.setOnClickListener(this);
        user_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    if (s.toString().contains(" ")) {
                        String preValue = user_password.getText().toString().substring(0, user_password.getText().toString().indexOf(" "));
                        if (user_password.getText().length() > 1) {
                            String postValue = user_password.getText().toString().substring(user_password.getText().toString().indexOf(" ") + 1, user_password.getText().toString().length());
                            user_password.setText(preValue + postValue);
                            user_password.setSelection(preValue.length());
                        } else {
                            user_password.setText(preValue);
                        }
                    }
                    password_error.setError(null);
                }
                password_error.setError(null);
            }
        });
        username.addTextChangedListener(new TextWatcher() {
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
                        String preValue = username.getText().toString().substring(0, username.getText().toString().indexOf(" "));
                        if (username.getText().length() > 1) {
                            String postValue = username.getText().toString().substring(username.getText().toString().indexOf(" ") + 1, username.getText().toString().length());
                            username.setText(preValue + postValue);
                            username.setSelection(preValue.length());
                        } else {
                            username.setText(preValue);
                        }
                    }
                    username_error.setError(null);
                }
                username_error.setError(null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    public void getLoginDetails() {
        final String user = username.getText().toString();
        final String pass = user_password.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Login_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (pDialog.isShowing()) {
                    pDialog.cancel();
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("token")) {
                        String token = jsonObject.getString("token");
                        getUserDetails(token);
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage().toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.cancel();
                String errorMessage = VolleyErrorHandler.getErrorMessage(Login.this, error);
                if (errorMessage.equals("401")) {
                    AlertDialogManager.showDialog(Login.this, "", getResources().getString(R.string.invalid_uname_pwd), false, null);
                } else {
                    AlertDialogManager.showDialog(Login.this, getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(Login.this, error), false, null);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("content-type", "application/json");
                params.put("accept", "application/json");
                params.put("Device", "Android");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(Login.this);
        requestQueue11.add(stringRequest);
    }

    public boolean validateCredentials() {
        if (username.getText().toString().isEmpty()) {
            username_error.setError(getString(R.string.username_hint));
            username_error.requestFocus();
            return false;
        } else {
            username_error.setError(null);
        }
        if (user_password.getText().toString().isEmpty()) {
            password_error.setError(getString(R.string.password_hint));
            user_password.requestFocus();
            return false;
        } else {
            password_error.setError(null);
        }
        return true;
    }

    public void getUserDetails(final String token) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.FetchDetail_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    firstName = jsonObject.getString("firstname") == null ? "" : jsonObject.getString("firstname").equals("null") ? "" : jsonObject.getString("firstname");
                    userID = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("null") ? "" : jsonObject.getString("id");
                    String lang = jsonObject.getString("language") == null ? "en" : jsonObject.getString("language").equals("null") ? "en" : jsonObject.getString("language");
                    if (lang.startsWith("nb")) {
                        lang_frm_apiResponse = "nb";
                    } else if (lang.startsWith("en")) {
                        lang_frm_apiResponse = "en_US";
                    } else if (lang.startsWith("ko")) {
                        lang_frm_apiResponse = "ko_KR";
                    } else if (lang.startsWith("pl")) {
                        lang_frm_apiResponse = "pl_PL";
                    } else if (lang.startsWith("sv")) {
                        lang_frm_apiResponse = "sv_SE";
                    } else if (lang.startsWith("pt")) {
                        lang_frm_apiResponse = "pt_BR";
                    }
                    if (dbSelect.getStstusFromOfflineDownloadTable(userID, "UserId", "Login").equals("")) {
                        dbInsert.addDataIntoOfflineDownloadTable(userID, "ON", "ON", "ON", "ON", "NO", "NO");
                    }
                } catch (Exception ex) {

                } finally {
                    offlineDownloadedStatus = dbSelect.getStstusFromOfflineDownloadTable(userID, "", "LoginPage");
                    if (offlineDownloadedStatus.equals("Yes")) {
                        commonIntentMethod(Login.this, LoadingPageActivity.class, token);
                    } else {
                        commonIntentMethod(Login.this, OfflineDownload.class, token);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialogManager.showDialog(Login.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(Login.this, error), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(Login.this, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(Login.this);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (connectionDetector.isConnectingToInternet()) {
                    if (validateCredentials()) {
                        getLoginDetails();
                        try {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            if (imm.isAcceptingText()) {
                                imm.hideSoftInputFromWindow(username.getWindowToken(), 0);
                                imm.hideSoftInputFromWindow(user_password.getWindowToken(), 0);
                            }
                            pDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    AlertDialogManager.showDialog(Login.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
            case R.id.btn_forget_pwd:
                commonIntentMethod(Login.this, ForgetPassword.class, "");
                break;
            case R.id.llToolBox:
                if (connectionDetector.isConnectingToInternet() || dbSelect.getDownlodedToolDetails("ToolBox", " ").size() > 0) {
                    LoginPermissionsDispatcher.goToToolBoxPageWithPermissionCheck(Login.this);
                } else {
                    AlertDialogManager.showDialog(Login.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
            case R.id.btn_register:
                if (connectionDetector.isConnectingToInternet()) {
                    commonIntentMethod(Login.this, WebActivity.class, "");
                } else {
                    AlertDialogManager.showDialog(Login.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
                break;
        }
    }

    public void commonIntentMethod(Context con, Class activity, String token) {
        Intent intent = new Intent(con, activity);
        intent.putExtra("token", token);
        intent.putExtra("username", username.getText().toString().trim());
        intent.putExtra("From", "Login");
        intent.putExtra("firstName", firstName);
        intent.putExtra("userID", userID);
        intent.putExtra("APIResponseLanguage", lang_frm_apiResponse);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void goToToolBoxPage() {
        commonIntentMethod(Login.this, ToolBox.class, "");
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LoginPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    commonIntentMethod(Login.this, ToolBox.class, "");
                } else {
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(Login.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(Login.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", Login.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    } else {
                        AlertDialogManager.showCustomDialog(Login.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                ActivityCompat.requestPermissions(Login.this, new String[]{permission}, requestCode);
                            }
                        }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    }
                }
                break;
        }
    }
}
