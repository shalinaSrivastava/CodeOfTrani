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
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportEntry extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    TextView text_header, txt_hour_count, txt_guest_count, txt_company_name, txt_guest_numb;
    private ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    String companyName, facilityName, facilityId,customerId, facilityStatus, entryId, allowGuest;
    RelativeLayout rl_remove_guest, rl_add_guest, rl_minus_hour, rl_add_hour, rl_report_entery,rl_guest_count;
    int guestCount = 0, hourCount = 1;
    int workSeconds;
    ConnectionDetector connectionDetector;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;

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
        if (getIntent().getStringExtra("AllowGuest") != null && !Objects.equals(getIntent().getStringExtra("AllowGuest"), "")) {
            allowGuest = getIntent().getStringExtra("AllowGuest");
        }
        getControls();
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(ReportEntry.this);
        connectionDetector =  new ConnectionDetector(this);
        dbSelect = new DataBaseHandlerSelect(this);
        dbInsert = new DataBaseHandlerInsert(this);
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
        txt_guest_numb = findViewById(R.id.txt_guest_numb);
        rl_guest_count = findViewById(R.id.rl_guest_count);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_remove_guest.setOnClickListener(this);
        rl_add_guest.setOnClickListener(this);
        rl_minus_hour.setOnClickListener(this);
        rl_add_hour.setOnClickListener(this);
        rl_report_entery.setOnClickListener(this);

        if(allowGuest.equals("true")){
            txt_guest_numb.setVisibility(View.VISIBLE);
            rl_guest_count.setVisibility(View.VISIBLE);
        }else{
            txt_guest_numb.setVisibility(View.GONE);
            rl_guest_count.setVisibility(View.GONE);
        }

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
                            showWaitDialog();
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
                        JSONObject jsonObj = new JSONObject(response);
                        if (jsonObj != null) {
                            ReportEntryProperty property = new ReportEntryProperty();
                            property.userId = spManager.getUserID();
                            property.entryId = jsonObj.getString("id");
                            entryId =  property.entryId;
                            property.checkOutMessage = jsonObj.getString("checkOutMessage");
                            property.timestamp = jsonObj.getString("timestamp");
                            property.state = jsonObj.getString("state");
                            facilityStatus = property.state;
                            property.numberOfGuests = jsonObj.getString("numberOfGuests");
                            property.employeeId = jsonObj.getString("employeeId");
                            property.securityServicePhone = jsonObj.getString("securityServicePhone");
                            property.safetycardId = jsonObj.getString("safetycardId");
                            property.facilityName = jsonObj.getString("facilityName");
                            property.facilityId = jsonObj.getString("facilityId");
                            property.estimatedDurationOfVisitInSeconds = jsonObj.getString("estimatedDurationOfVisitInSeconds");
                            property.facilityLatitude = jsonObj.getString("facilityLatitude");
                            property.facilityLongitude = jsonObj.getString("facilityLongitude");
                            dbInsert.addDataIntoReportEntryTable(property);
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Exception", ex.getMessage());
                } finally {
                    if(facilityStatus.equals("awaiting_approval")){
                        Intent intent = new Intent(ReportEntry.this, AwaitingApproval.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("EntryId",entryId);
                        startActivity(intent);
                    }else if(facilityStatus.equals("checked_in")){
                        commonIntentMethod(CheckedInFacility.class);
                    }
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               int statuscode =  error.networkResponse.statusCode;
               if(statuscode == 403 || statuscode == 404){
                   try {
                       String responseBody = new String(error.networkResponse.data, "utf-8");
                       JSONObject data = new JSONObject(responseBody);
                       //JSONArray errors = data.getJSONArray("errors");
                       //JSONObject jsonMessage = errors.getJSONObject(0);
                       String message = data.getString("message");
                       Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                   } catch (JSONException | UnsupportedEncodingException e) {
                       Log.d("Exception: ", Objects.requireNonNull(e.getMessage()));
                   }

                   //Toast.makeText(ReportEntry.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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