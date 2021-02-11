package com.elearn.trainor;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DashboardClasses.LoadingPageActivity;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.ToolBoxModule.ToolboxLoadingActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    SharedPreferenceManager spManager;
    Handler handler;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;


    String uniq_id, email;
    private ProgressDialog pDialog;
    String lang_frm_apiResponse, firstName, userID, offlineDownloadedStatus = "";
    ConnectionDetector connectionDetector;
    private BroadcastReceiver mBroadcastReceiver;
    private static final String TAG = "AppRestrictionSchema";
    private static final String Unique_Email = "user_email";
    private static final String Unique_ID = "unique_id";


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash);
        dbSelect = new DataBaseHandlerSelect(SplashActivity.this);
        dbInsert = new DataBaseHandlerInsert(SplashActivity.this);
        spManager = new SharedPreferenceManager(SplashActivity.this);
        connectionDetector = new ConnectionDetector(SplashActivity.this);

        long downloadReference = 0;
        if (!spManager.getValueByKeyNameFromDownload("DownloadingReference").equals("")) {
            downloadReference = Long.parseLong(spManager.getValueByKeyNameFromDownload("DownloadingReference"));
            Intent intentToolBox = new Intent(SplashActivity.this, ToolboxLoadingActivity.class);
            intentToolBox.putExtra("DownloadingReference", spManager.getValueByKeyNameFromDownload("From"));
            intentToolBox.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentToolBox.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentToolBox.putExtra("DownloadingReference", downloadReference + "");
            startActivity(intentToolBox);
        } else {
            String userid = spManager.getUserID();
            if (userid == null || userid.equals("")) {
                userid = "1";
            }
            String isLoggedIn = dbSelect.getStstusFromOfflineDownloadTable(userid, "isLoggedIn", "Login");
            if (spManager.getSharedPreferenceExistence() && isLoggedIn.equals("Yes")) {
                Intent HomePageIntent = new Intent(SplashActivity.this, HomePage.class);
                HomePageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(HomePageIntent);
                finish();
            } else {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @SuppressLint("WrongThread")
                    @Override
                    protected Void doInBackground(Void... params) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (email.equals("not form mdm") && uniq_id.equals("not form mdm")) {
                                    Intent intent = new Intent(SplashActivity.this, Login.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    if (email != null && uniq_id != null) {
                                        if (email.equals("") && uniq_id.equals("")) {
                                            AlertDialogManager.showDialog(SplashActivity.this, "", getResources().getString(R.string.mdm_value_error_message) + "\n" + getResources().getString(R.string.quit_app), false, new IClickListener() {
                                                @Override
                                                public void onClick() {
                                                    finishAffinity();
                                                    finish();
                                                }
                                            });
                                        } else {
                                            System.out.println(" Automatic Login Unique Email = " + email);
                                            if (connectionDetector.isConnectingToInternet()) {
                                                getLoginDetails();
                                            } else {
                                                AlertDialogManager.showDialog(SplashActivity.this, getResources().getString(R.string.internetErrorTitle_mdm), getResources().getString(R.string.internet_error_message_mdm), false, new IClickListener() {
                                                    @Override
                                                    public void onClick() {
                                                        if (connectionDetector.isConnectingToInternet()) {
                                                            getLoginDetails();
                                                        } else {
                                                            finishAffinity();
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }

                                        }

                                    } else {
                                        AlertDialogManager.showDialog(SplashActivity.this, "", getResources().getString(R.string.mdm_value_error_message) + "\n" + getResources().getString(R.string.quit_app), false, new IClickListener() {
                                            @Override
                                            public void onClick() {
                                                finishAffinity();
                                                finish();
                                            }
                                        });
                                    }
                                }
                            }
                        }, 3000);
                    }
                }.execute();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                resolveRestrictions();
            }
        };
        this.registerReceiver(mBroadcastReceiver,
                new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED));


    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBroadcastReceiver != null) {
            this.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resolveRestrictions();
    }

    private void resolveRestrictions() {
        RestrictionsManager manager = (RestrictionsManager) this.getSystemService(Context.RESTRICTIONS_SERVICE);
        Bundle restrictions = manager.getApplicationRestrictions();
        List<RestrictionEntry> entries = manager.getManifestRestrictions(
                this.getApplicationContext().getPackageName());
        for (RestrictionEntry entry : entries) {
            String key = entry.getKey();
            Log.d(TAG, "key: " + key);
            if (key.equals(Unique_Email)) {
                updateResEmail(entry, restrictions);
            } else if (key.equals(Unique_ID)) {
                updateResUID(entry, restrictions);
            }
        }

    }

    private void updateResEmail(RestrictionEntry entry, Bundle restrictions) {
        if (restrictions == null || !restrictions.containsKey(Unique_Email)) {
            email = "not form mdm";
//            email = "test.test@statnett.no";
        } else {
            email = restrictions.getString(Unique_Email);
        }
    }

    private void updateResUID(RestrictionEntry entry, Bundle restrictions) {

        if (restrictions == null || !restrictions.containsKey(Unique_ID)) {
//            uniq_id = "3i2WtxWxGz1Kx8kcMfaQ";
            uniq_id = "not form mdm";
        } else {
            uniq_id = restrictions.getString(Unique_ID);

        }
    }

    public void getLoginDetails() {
        showWaitDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Login_URL_WITH_EMAIL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("token")) {
                        String token = jsonObject.getString("token");
                        getUserDetails(token);
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                // enableButtonClick();
                AlertDialogManager.showDialog(SplashActivity.this, "", getResources().getString(R.string.stattnet_login_api_error), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        finishAffinity();
                        finish();
                    }
                });
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
                String str = "{\"email\":\"" + email + "\",\"uniqueId\":\"" + uniq_id + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        RequestQueue requestQueue11 = Volley.newRequestQueue(SplashActivity.this);
        requestQueue11.add(stringRequest);
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
                    dismissWaitDialog();
                    offlineDownloadedStatus = dbSelect.getStstusFromOfflineDownloadTable(userID, "", "LoginPage");
                    if (offlineDownloadedStatus.equals("Yes")) {

                        commonIntentMethod(SplashActivity.this, LoadingPageActivity.class, token);
                    } else {
                        commonIntentMethod(SplashActivity.this, OfflineDownload.class, token);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                AlertDialogManager.showDialog(SplashActivity.this, "", getResources().getString(R.string.mdm_value_error_message) + "\n" + getResources().getString(R.string.quit_app), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        finishAffinity();
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(SplashActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void commonIntentMethod(Context con, Class activity, String token) {
        Intent intent = new Intent(con, activity);
        intent.putExtra("token", token);
//        intent.putExtra("LoginType", loginType);
        intent.putExtra("From", "StattnetLogin");
        intent.putExtra("firstName", firstName);
        intent.putExtra("userID", userID);
        intent.putExtra("APIResponseLanguage", lang_frm_apiResponse);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(SplashActivity.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage("Logging in..");
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {

        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

    }

}
