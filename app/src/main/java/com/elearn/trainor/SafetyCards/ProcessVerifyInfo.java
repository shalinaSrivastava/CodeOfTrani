package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.elearn.trainor.BaseAdapters.MyCompanyListRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DashboardClasses.LoadingPageActivity;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.Login;
import com.elearn.trainor.MyCompany.CompanyList;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProcessVerifyInfo extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header, txt_verify_email_des1, txt_verify_phone_des1;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dataBaseHandlerInsert;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    ProgressDialog pDialog;
    public boolean isActivityLive = false;
    FirebaseAnalytics analytics;
    Trace myTrace;
    String type, customerId, typeName, requestId, userEmailVerified,userPhoneVerified;
    RelativeLayout rl_frequently_asked_qus, rl_verify_phone_view, rl_verify_email_view, rl_verify_more_contacts;
    EditText edit_code;
    Button btn_submit_code;
    JSONArray arrJsonemails,arrJsonPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_verify_info);
        isActivityLive = true;
        getControls();
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(ProcessVerifyInfo.this);
        myTrace = FirebasePerformance.getInstance().newTrace("Register_SafetyCard_trace");
        myTrace.start();

        dbSelect = new DataBaseHandlerSelect(ProcessVerifyInfo.this);
        dataBaseHandlerInsert = new DataBaseHandlerInsert(ProcessVerifyInfo.this);
        dataBaseHandlerDelete = new DataBaseHandlerDelete(ProcessVerifyInfo.this);
        connectionDetector = new ConnectionDetector(ProcessVerifyInfo.this);
        spManager = new SharedPreferenceManager(ProcessVerifyInfo.this);

        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(R.string.verify_info_header);
        rl_frequently_asked_qus = findViewById(R.id.rl_frequently_asked_qus);
        rl_verify_phone_view = findViewById(R.id.rl_verify_phone_view);
        rl_verify_email_view = findViewById(R.id.rl_verify_email_view);
        txt_verify_email_des1 = findViewById(R.id.txt_verify_email_des1);
        txt_verify_phone_des1 = findViewById(R.id.txt_verify_phone_des1);
        rl_verify_more_contacts = findViewById(R.id.rl_verify_more_contacts);
        btn_submit_code = findViewById(R.id.btn_submit_code);
        edit_code = findViewById(R.id.edit_code);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_verify_more_contacts.setOnClickListener(this);
        btn_submit_code.setOnClickListener(this);

        if (getIntent().getStringExtra("Type") != null && !Objects.equals(getIntent().getStringExtra("Type"), "")) {
            type = getIntent().getStringExtra("Type");
        }
        if (getIntent().getStringExtra("CustomerId") != null && !Objects.equals(getIntent().getStringExtra("CustomerId"), "")) {
            customerId = getIntent().getStringExtra("CustomerId");
        }
        if (getIntent().getStringExtra("TypeName") != null && !Objects.equals(getIntent().getStringExtra("TypeName"), "")) {
            typeName = getIntent().getStringExtra("TypeName");
        }
//Test();
        if (type.equals("FrequentlyAsked")) {
            rl_frequently_asked_qus.setVisibility(View.VISIBLE);
            rl_verify_phone_view.setVisibility(View.GONE);
            rl_verify_email_view.setVisibility(View.GONE);
        } else if (type.equals("E-mail")) {
            showWaitDialog();
            getCode(WebServicesURL.VerificationOTPEmail + "?customerId=" + customerId);
        }else if(type.equals("Phone")){
            showWaitDialog();
            getCode(WebServicesURL.VerificationOTPPhone + "?customerId=" + customerId);
        }
    }

   /* public void Test(){
        try {
            String emails = "[c32be323-7be4-46b1-a8ec-8543bfe876f1, 6fa3e54b-f99a-44ee-8789-4ae8e421e9c4,844e0564-b6bd-11e6-bb41-00505601092f]";
            arrJsonemails = (JSONArray) new JSONObject(new JSONTokener("{data:"+emails+"}")).get("data");

            String phones = "[]";
            arrJsonPhone = (JSONArray) new JSONObject(new JSONTokener("{data:"+phones+"}")).get("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("emailLength",arrJsonemails.length()+"");
        Log.d("phoneLength",arrJsonPhone.length()+"");
        if(arrJsonemails.length()==0){
            Log.d("emailLength",arrJsonemails.length()+"");
        }
        if(arrJsonPhone.length()==0){
            Log.d("phoneLength",arrJsonPhone.length()+"");
        }

    }*/

    public void getCode(String URL) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("requestId")) {
                                requestId = jsonObject.getString("requestId");
                            }
                        }
                    }
                } catch (JSONException e) {
                    dismissWaitDialog();
                    e.printStackTrace();
                } finally {
                    dismissWaitDialog();
                    if (!requestId.equals("")) {
                        if (type.equals("E-mail")) {
                            rl_frequently_asked_qus.setVisibility(View.GONE);
                            rl_verify_phone_view.setVisibility(View.VISIBLE);
                            rl_verify_email_view.setVisibility(View.GONE);
                            txt_verify_phone_des1.setText(getString(R.string.email_sent_to)+ typeName + getString(R.string.verify_ur_email));
                        } else if (type.equals("Phone")) {
                            rl_frequently_asked_qus.setVisibility(View.GONE);
                            rl_verify_phone_view.setVisibility(View.VISIBLE);
                            rl_verify_email_view.setVisibility(View.GONE);
                            txt_verify_phone_des1.setText(getString(R.string.msg_sent_to) + typeName + ".");
                        }
                    } else {
                        AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.try_after_some_time), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
               /* if (error == null || error.networkResponse == null) {
                    return;
                }*/
                final String status_Code = String.valueOf(error.networkResponse.statusCode);
                Log.d("Satus Code= ", status_Code);
                AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.not_having_valid_email_phone), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                params.put("Accept", "application/json");
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
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
            case R.id.rl_verify_more_contacts:
                commonIntentMethod(VerifyInfo.class);
                break;
            case R.id.btn_submit_code:
                if(connectionDetector.isConnectingToInternet()){
                    String code = edit_code.getText().toString().trim();
                    if(!code.equals("")){
                        showWaitDialog();
                        callSubmitCodeApi(code);
                    }else {
                        AlertDialogManager.showDialog(this, "", getString(R.string.input_valid_code), false, null);
                    }
                }else{
                    AlertDialogManager.showDialog(this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }

                break;
        }
    }

    public void callSubmitCodeApi(String code){
        String url = WebServicesURL.VerifiyCode + "?requestId=" + requestId+"&code="+code;

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("userEmailVerified")) {
                                userEmailVerified = jsonObject.getString("userEmailVerified");
                            }
                            if (jsonObject.has("userPhoneVerified")) {
                                userPhoneVerified = jsonObject.getString("userPhoneVerified");
                            }
                            if (jsonObject.has("emailVerifiedCustomerIds")) {
                                String emails = jsonObject.getString("emailVerifiedCustomerIds");
                                //String emails = "[c32be323-7be4-46b1-a8ec-8543bfe876f1, 6fa3e54b-f99a-44ee-8789-4ae8e421e9c4,844e0564-b6bd-11e6-bb41-00505601092f]";
                                 arrJsonemails = (JSONArray) new JSONObject(new JSONTokener("{data:"+emails+"}")).get("data");
                            }
                            if (jsonObject.has("phoneVerifiedCustomerIds")) {
                                String phones = jsonObject.getString("phoneVerifiedCustomerIds");
                                //String phones = "[]";
                                arrJsonPhone = (JSONArray) new JSONObject(new JSONTokener("{data:"+phones+"}")).get("data");

                            }
                        }
                    }
                } catch (JSONException e) {
                    dismissWaitDialog();
                    e.printStackTrace();
                } finally {
                    dismissWaitDialog();
                    if(type.equals("E-mail")){
                        if(userEmailVerified.equals("false") && arrJsonemails.length()==0){
                            AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.not_verified_try_again), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }else{
                            AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.verified), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }else if (type.equals("Phone")){
                        if(userPhoneVerified.equals("false") && arrJsonPhone.length()==0){
                            AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.not_verified_try_again), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }else{
                            AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.verified), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                    /*if ((userEmailVerified.equals("false") ||userPhoneVerified.equals("false")) && (arrJsonemails.length()==0 || arrJsonPhone.length()==0)) {
                        AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.not_verified_try_again), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.verified), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }*/
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
               /* if (error == null || error.networkResponse == null) {
                    return;
                }*/
                final String status_Code = String.valueOf(error.networkResponse.statusCode);
                Log.d("Satus Code= ", status_Code);
                AlertDialogManager.showDialog(ProcessVerifyInfo.this, "", getString(R.string.request_id_code_invalid), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(ProcessVerifyInfo.this, VerifyInfo.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                params.put("Accept", "application/json");
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
       /* if (connectionDetector.isConnectingToInternet()) {
            saveCustomerDetails(spManager.getUserID());
        } else {
            commonIntentMethod(VerifyInfo.class);
        }*/
        commonIntentMethod(VerifyInfo.class);
    }


    public void commonIntentMethod(Class activity) {
        myTrace.stop();
        Intent intent = new Intent(ProcessVerifyInfo.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onStart() {
        isActivityLive = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        isActivityLive = true;
        super.onResume();
        //analytics.setCurrentScreen(this, "RegisterSafetyCard", this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        myTrace.stop();
        isActivityLive = false;
        dismissWaitDialog();
        super.onStop();
    }

    public void showWaitDialog() {
        if (isActivityLive) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(ProcessVerifyInfo.this);
                pDialog.setMessage(getString(R.string.please_wait));
                pDialog.setCancelable(false);
                if (!pDialog.isShowing()) {
                    pDialog.show();
                }
            } else {
                if (!pDialog.isShowing()) {
                    pDialog.show();
                }
            }
        }
    }

    public void dismissWaitDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}