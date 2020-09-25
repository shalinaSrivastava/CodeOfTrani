package com.elearn.trainor.SafetyCards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.BaseAdapters.NearByFacilityAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportEntry extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header, txt_hour_count, txt_guest_count, txt_company_name;
    private ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    String companyName, facilityName, facilityId,customerId;
    RelativeLayout rl_remove_guest, rl_add_guest, rl_minus_hour, rl_add_hour, rl_report_entery;
    int guestCount = 0, hourCount = 0;
    int workSeconds;
    ConnectionDetector connectionDetector;
    DataBaseHandlerSelect dbSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_entry);

        if (getIntent().getStringExtra("CompanyName") != null && !Objects.equals(getIntent().getStringExtra("CompanyName"), "")) {
            companyName = getIntent().getStringExtra("CompanyName");
        }
        if (getIntent().getStringExtra("FacilityName") != null && !Objects.equals(getIntent().getStringExtra("FacilityName"), "")) {
            facilityName = getIntent().getStringExtra("FacilityName");
        }
        if (getIntent().getStringExtra("FacilityId") != null && !Objects.equals(getIntent().getStringExtra("FacilityId"), "")) {
            facilityId = getIntent().getStringExtra("FacilityId");
        }
        if (getIntent().getStringExtra("FacilityCustomerId") != null && !Objects.equals(getIntent().getStringExtra("FacilityCustomerId"), "")) {
            customerId = getIntent().getStringExtra("FacilityCustomerId");
        }
        getControls();
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(ReportEntry.this);
        connectionDetector =  new ConnectionDetector(this);
        dbSelect = new DataBaseHandlerSelect(this);
        ll_back = findViewById(R.id.ll_back);
        llhome = findViewById(R.id.llhome);
        text_header = findViewById(R.id.text_header);
        text_header.setText(companyName);
        txt_hour_count = findViewById(R.id.txt_hour_count);
        txt_guest_count = findViewById(R.id.txt_guest_count);
        txt_company_name = findViewById(R.id.txt_company_name);
        txt_company_name.setText(facilityName);
        rl_remove_guest = findViewById(R.id.rl_remove_guest);
        rl_add_guest = findViewById(R.id.rl_add_guest);
        rl_minus_hour = findViewById(R.id.rl_minus_hour);
        rl_add_hour = findViewById(R.id.rl_add_hour);
        rl_report_entery = findViewById(R.id.rl_report_entery);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_remove_guest.setOnClickListener(this);
        rl_add_guest.setOnClickListener(this);
        rl_minus_hour.setOnClickListener(this);
        rl_add_hour.setOnClickListener(this);
        rl_report_entery.setOnClickListener(this);

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
            case R.id.rl_remove_guest:
                if (guestCount > 0) {
                    guestCount--;
                }
                txt_guest_count.setText(guestCount + "");
                break;
            case R.id.rl_add_guest:
                guestCount++;
                txt_guest_count.setText(guestCount + "");
                break;
            case R.id.rl_minus_hour:
                if (hourCount > 1) {
                    hourCount--;
                }
                txt_hour_count.setText(hourCount + "");
                break;
            case R.id.rl_add_hour:
                hourCount++;
                txt_hour_count.setText(hourCount + "");
                break;
            case R.id.rl_report_entery:
                if (hourCount > 0) {
                    workSeconds = hourCount * 3600;
                    Log.d("Seconds", workSeconds + "");
                    if(connectionDetector.isConnectingToInternet()){
                        List<String> safetyCardIdList = dbSelect.getSafetyCardidByCustId(customerId);
                        if(safetyCardIdList.size()>0){
                            callReportEntryAPI(facilityId,safetyCardIdList.get(0),workSeconds,guestCount);
                        }else{
                            Toast.makeText(this, "Something went wrong. Please contact administrator!", Toast.LENGTH_SHORT).show();
                        }
                        //callReportEntryAPI(facilityId,"5fbcf276-48ed-461e-bb20-6f3ce0b92ea5",workSeconds,guestCount);
                    }else{
                        Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please select valid hours.", Toast.LENGTH_SHORT).show();
                }
                //commonIntentMethod(AwaitingApproval.class);
                break;
        }
    }

    public void callReportEntryAPI(String facilityId, String safetycardId, int time, int gusests) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.EntryFacility, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jArray = new JSONArray(response);
                        // Toast.makeText(getActivity(),"Api response:"+  jArray.toString(), Toast.LENGTH_SHORT).show();
                        if (jArray.length() > 0) {
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject jsonObj = jArray.getJSONObject(i);
                                String checkOutMessage = jsonObj.getString("checkOutMessage");
                                String timestamp = jsonObj.getString("timestamp");
                                String state = jsonObj.getString("state");
                                String id = jsonObj.getString("id");
                                String numberOfGuests = jsonObj.getString("numberOfGuests");
                                String employeeId = jsonObj.getString("employeeId");
                                String securityServicePhone = jsonObj.getString("securityServicePhone");
                                String safetycardId = jsonObj.getString("safetycardId");
                                String facilityName = jsonObj.getString("facilityName");
                                String facilityId = jsonObj.getString("facilityId");
                                String estimatedDurationOfVisitInSeconds = jsonObj.getString("estimatedDurationOfVisitInSeconds");

                                System.out.println(id);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                } finally {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               int statuscode =  error.networkResponse.statusCode;
               if(statuscode == 403){
                   Toast.makeText(ReportEntry.this, "Employee is already in facility with state checked_in.", Toast.LENGTH_SHORT).show();
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
                    jsonBody.put("facilityId", facilityId);
                    jsonBody.put("safetycardId", safetycardId);
                    jsonBody.put("numberOfGuests", gusests);
                    jsonBody.put("visitDurationSeconds", time);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(StartCheckInFacility.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(ReportEntry.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(ReportEntry.this);
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