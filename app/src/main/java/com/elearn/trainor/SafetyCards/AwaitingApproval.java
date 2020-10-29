package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.CourseModule.CourseDownloadingActivity;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AwaitingApproval extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header, txt_skip;
    String entryId, state;
    int loopForStatus = 0;
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awaiting_approval);
        if (getIntent().getStringExtra("EntryId") != null && !Objects.equals(getIntent().getStringExtra("EntryId"), "")) {
            entryId = getIntent().getStringExtra("EntryId");
        }
        getControls();
    }

    public void getControls() {
        connectionDetector =  new ConnectionDetector(this);
        spManager = new SharedPreferenceManager(this);
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText(R.string.waiting_for_response);
        txt_skip = findViewById(R.id.txt_skip);
        ll_back.setVisibility(View.GONE);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        txt_skip.setOnClickListener(this);
       callgetStatusApi();
    }

    public void callApiAfte10Sec(){
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                callgetStatusApi();
            }
        }, 10000);
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

    public void callgetStatusApi(){
        loopForStatus = loopForStatus+1;
        String url = WebServicesURL.GetFacilityState + entryId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url , new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        state = jsonObject.getString("state");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if(state.equals("awaiting_approval")){
                        if(loopForStatus==6){
                            AlertDialogManager.showDialog(AwaitingApproval.this, getString(R.string.approval_issue), getString(R.string.contact_customer_care), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    commonIntentMethod(HomePage.class);
                                }
                            });
                        }else{
                            if(connectionDetector.isConnectingToInternet()){
                                callApiAfte10Sec();
                            }else{
                                AlertDialogManager.showDialog(AwaitingApproval.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, new IClickListener() {
                                    @Override
                                    public void onClick() {
                                        commonIntentMethod(StartCheckInFacility.class);
                                    }
                                });
                            }
                            //callgetStatusApi();
                        }
                    }else if(state.equals("checked_in")){
                        commonIntentMethod(CheckedInFacility.class);
                    }else if(state.equals("checked_out")){
                        commonIntentMethod(HomePage.class);
                    }else if(state.equals("rejected")){
                        AlertDialogManager.showDialog(AwaitingApproval.this, "", getString(R.string.facility_rejected), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                commonIntentMethod(HomePage.class);
                            }
                        });

                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               /* int statuscode = error.networkResponse.statusCode;
                if (statuscode == 403 || statuscode == 404 || statuscode == 400) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        String message = data.getString("message");
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        Log.d("Exception: ", Objects.requireNonNull(e.getMessage()));
                    }
                }*/
                if(loopForStatus==6){
                    AlertDialogManager.showDialog(AwaitingApproval.this, getString(R.string.approval_issue), getString(R.string.contact_customer_care), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            commonIntentMethod(StartCheckInFacility.class);
                        }
                    });
                }else{
                    if(connectionDetector.isConnectingToInternet()){
                        callApiAfte10Sec();
                    }else{
                        AlertDialogManager.showDialog(AwaitingApproval.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                commonIntentMethod(StartCheckInFacility.class);
                            }
                        });
                    }
                    //callgetStatusApi();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                params.put("Accept", "application/json");
                return params;
            }

          /*  @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                assert response != null;
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }*/
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        /*commonIntentMethod(ReportEntry.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);*/
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(AwaitingApproval.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}