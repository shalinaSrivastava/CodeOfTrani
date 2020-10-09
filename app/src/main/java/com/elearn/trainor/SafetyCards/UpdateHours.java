package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpdateHours extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header, txt_hour_count, txt_guest_count, txt_facility_update_entry, txt_facility_update_hours_des;
    private ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    String spentTime, leftTime, facilityName, entryId, spentHour, spentMin, actualDuration;
    RelativeLayout rl_minus_hour, rl_add_hour, rl_update_work_hr;
    int hourCount = 0;
    int workSeconds;
    ConnectionDetector connectionDetector;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_hours);
        if (getIntent().getStringExtra("FacilityName") != null && !Objects.equals(getIntent().getStringExtra("FacilityName"), "")) {
            facilityName = getIntent().getStringExtra("FacilityName");
        }
        if (getIntent().getStringExtra("EntryId") != null && !Objects.equals(getIntent().getStringExtra("EntryId"), "")) {
            entryId = getIntent().getStringExtra("EntryId");
        }
        if (getIntent().getStringExtra("SpentTime") != null && !Objects.equals(getIntent().getStringExtra("SpentTime"), "")) {
            spentTime = getIntent().getStringExtra("SpentTime");
        }
        if (getIntent().getStringExtra("LeftTime") != null && !Objects.equals(getIntent().getStringExtra("LeftTime"), "")) {
            leftTime = getIntent().getStringExtra("LeftTime");
            spentHour = leftTime.substring(0, leftTime.indexOf(","));
            spentMin = leftTime.substring(leftTime.indexOf(",") + 1);
        }
        if (getIntent().getStringExtra("ActualDuration") != null && !Objects.equals(getIntent().getStringExtra("ActualDuration"), "")) {
            actualDuration = getIntent().getStringExtra("ActualDuration");
        }
        getControls();
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(UpdateHours.this);
        connectionDetector = new ConnectionDetector(this);
        dbSelect = new DataBaseHandlerSelect(this);
        dbInsert = new DataBaseHandlerInsert(this);
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText(facilityName);
        txt_hour_count = findViewById(R.id.txt_hour_count);
        txt_guest_count = findViewById(R.id.txt_guest_count);
        txt_facility_update_entry = findViewById(R.id.txt_facility_update_entry);
        txt_facility_update_entry.setText(getString(R.string.reporting_entry_into) + facilityName);
        rl_minus_hour = findViewById(R.id.rl_minus_hour);
        rl_add_hour = findViewById(R.id.rl_add_hour);
        rl_update_work_hr = findViewById(R.id.rl_update_work_hr);
        txt_facility_update_hours_des = findViewById(R.id.txt_facility_update_hours_des);
        String leftDuration = "";
        if (!(spentHour + "").equals("0")) {
            leftDuration = spentHour + "hr and " + spentMin + "min";
        } else {
            leftDuration = spentMin + "min";
        }
        String spentNleftTime = "You have been in the facility for " + spentTime + ". " +
                "You have " + leftDuration + " left before being notified of exit.";
        txt_facility_update_hours_des.setText(spentNleftTime);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_minus_hour.setOnClickListener(this);
        rl_add_hour.setOnClickListener(this);
        rl_update_work_hr.setOnClickListener(this);
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
            case R.id.rl_minus_hour:
                if (hourCount > 0) {
                    hourCount--;
                }
                txt_hour_count.setText(hourCount + "");
                break;
            case R.id.rl_add_hour:
                hourCount++;
                txt_hour_count.setText(hourCount + "");
                break;
            case R.id.rl_update_work_hr:
                if (hourCount > 0) {
                    workSeconds = (hourCount * 3600)+Integer.parseInt(actualDuration);
                    Log.d("Seconds", workSeconds + "");
                    if (connectionDetector.isConnectingToInternet()) {
                        showWaitDialog();
                        callUpdateHourApi(entryId, workSeconds + "");
                        //callReportEntryAPI(facilityId,"5fbcf276-48ed-461e-bb20-6f3ce0b92ea5",workSeconds,guestCount);
                    } else {
                        Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please select valid hours.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void callUpdateHourApi(String entryId, String seconds) {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, WebServicesURL.UpdateVisitDuration, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObj = new JSONObject(response);
                        if (jsonObj != null) {
                            //String entryId = jsonObj.getString("id");
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                } finally {
                    dismissWaitDialog();
                    if (response.equals("200")) {
                        Intent intent = new Intent(UpdateHours.this, CheckedInFacility.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    int statuscode = error.networkResponse.statusCode;
                    if (statuscode == 403 || statuscode == 404 || statuscode == 400) {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        String message = data.getString("message");
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(ReportEntry.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    Toast.makeText(UpdateHours.this, "Server error. Please try again", Toast.LENGTH_SHORT).show();
                    Log.d("Exception: ", Objects.requireNonNull(ex.getMessage()));
                } catch (JSONException ex) {
                    Log.d("Exception: ", Objects.requireNonNull(ex.getMessage()));
                    Toast.makeText(UpdateHours.this, "Server error. Please try again", Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                }
                if (pDialog != null && pDialog.isShowing()) {
                    dismissWaitDialog();
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

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("entryId", entryId);
                    jsonBody.put("visitDurationSeconds", seconds);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(CheckedInFacility.class);
        super.onBackPressed();
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(UpdateHours.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(UpdateHours.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage(getString(R.string.please_wait));
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