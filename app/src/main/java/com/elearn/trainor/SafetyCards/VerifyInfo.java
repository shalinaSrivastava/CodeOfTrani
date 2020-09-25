package com.elearn.trainor.SafetyCards;

import com.elearn.trainor.BaseAdapters.NearByFacilityAdapter;
import com.elearn.trainor.BaseAdapters.UnVerifiedContactFacilityAdapter;
import com.elearn.trainor.BaseAdapters.VerifiedContactFacilityAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.VerifyUnverifyProperty;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyInfo extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome, ll_verify_email,ll_verify_phone,ll_verified_email,ll_verified_phone;
    TextView text_header;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerDelete dbDelete;
    ProgressDialog pDialog;
    public boolean isActivityLive = false;
    FirebaseAnalytics analytics;
    Trace myTrace;
    RelativeLayout rl_frequently_asked_qus, rl_unveified, rl_veified;
    List<CustomerDetailsProperty> customerList;
    List<VerifyUnverifyProperty> verifiedList;
    List<VerifyUnverifyProperty> notVerifiedList;

    VerifiedContactFacilityAdapter verifiedContactFacilityAdapter;
    RecyclerView rv_unverified;
    UnVerifiedContactFacilityAdapter unVerifiedContactFacilityAdapter;
    RecyclerView rv_verified;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_info);
        isActivityLive = true;
        getControls();
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(VerifyInfo.this);
        myTrace = FirebasePerformance.getInstance().newTrace("Register_SafetyCard_trace");
        myTrace.start();

        dbSelect = new DataBaseHandlerSelect(VerifyInfo.this);
        dbInsert = new DataBaseHandlerInsert(VerifyInfo.this);
        dbDelete = new DataBaseHandlerDelete(VerifyInfo.this);
        connectionDetector = new ConnectionDetector(VerifyInfo.this);
        spManager = new SharedPreferenceManager(VerifyInfo.this);
        customerList = new ArrayList<>();
        verifiedList = new ArrayList<>();
        notVerifiedList = new ArrayList<>();

        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText("Verify info");
        rl_frequently_asked_qus = findViewById(R.id.rl_frequently_asked_qus);
        rl_unveified = findViewById(R.id.rl_unveified);
        rl_veified = findViewById(R.id.rl_veified);
        rv_unverified = findViewById(R.id.rv_unverified);
        rv_verified = findViewById(R.id.rv_verified);


        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        rl_frequently_asked_qus.setOnClickListener(this);
        /*ll_verify_email.setOnClickListener(this);
        ll_verify_phone.setOnClickListener(this);
        ll_verified_email.setOnClickListener(this);
        ll_verified_phone.setOnClickListener(this);*/
        if(connectionDetector.isConnectingToInternet()){
            showWaitDialog();
            saveCustomerDetails(spManager.getUserID());
        }else{
            customerList.clear();
            customerList = dbSelect.getCustomerListForFacility();
            showSpecificEntityVerification((ArrayList<CustomerDetailsProperty>) customerList);
        }

    }

    public void showSpecificEntityVerification(ArrayList<CustomerDetailsProperty> customerList){


        for(int i=0;i<customerList.size();i++){

            if((!customerList.get(i).workEmailAddress.equals("") && !customerList.get(i).workEmailAddress.equals("null"))&& customerList.get(i).emailVerified.equals("true")){
                VerifyUnverifyProperty property = new VerifyUnverifyProperty();
                property.customer_id = customerList.get(i).customer_id;
                property.typeCategory = "E-mail";
                property.typeName = customerList.get(i).workEmailAddress;
                if(customerList.get(i).isPrivate.equals("true")){
                    property.customerName = "Private";
                }else{
                    property.customerName = customerList.get(i).customerName;
                }
                verifiedList.add(property);
            }
            if ((!customerList.get(i).workEmailAddress.equals("") && !customerList.get(i).workEmailAddress.equals("null"))&& customerList.get(i).emailVerified.equals("false")){
                VerifyUnverifyProperty property = new VerifyUnverifyProperty();
                property.customer_id = customerList.get(i).customer_id;
                property.typeCategory = "E-mail";
                property.typeName = customerList.get(i).workEmailAddress;
                if(customerList.get(i).isPrivate.equals("true")){
                    property.customerName = "Private";
                }else{
                    property.customerName = customerList.get(i).customerName;
                }
                notVerifiedList.add(property);
            }
            if ((!customerList.get(i).workPhone.equals("") && !customerList.get(i).workPhone.equals("null"))&& customerList.get(i).phoneVerified.equals("true")) {
                VerifyUnverifyProperty property = new VerifyUnverifyProperty();
                property.customer_id = customerList.get(i).customer_id;
                property.typeCategory = "Phone";
                property.typeName = customerList.get(i).workPhone;
                if(customerList.get(i).isPrivate.equals("true")){
                    property.customerName = "Private";
                }else{
                    property.customerName = customerList.get(i).customerName;
                }
                verifiedList.add(property);
            }
            if ((!customerList.get(i).workPhone.equals("") && !customerList.get(i).workPhone.equals("null"))&& customerList.get(i).phoneVerified.equals("false")){
                VerifyUnverifyProperty property = new VerifyUnverifyProperty();
                property.customer_id = customerList.get(i).customer_id;
                property.typeCategory = "Phone";
                property.typeName = customerList.get(i).workPhone;
                if(customerList.get(i).isPrivate.equals("true")){
                    property.customerName = "Private";
                }else{
                    property.customerName = customerList.get(i).customerName;
                }
                notVerifiedList.add(property);
            }
        }

        if(notVerifiedList.size()>0){
            rv_unverified.setVisibility(View.VISIBLE);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(VerifyInfo.this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rv_unverified.setLayoutManager(layoutManager);
            unVerifiedContactFacilityAdapter = new UnVerifiedContactFacilityAdapter(VerifyInfo.this, notVerifiedList);
            rv_unverified.setAdapter(unVerifiedContactFacilityAdapter);
            unVerifiedContactFacilityAdapter.notifyDataSetChanged();
        }else{
            rl_unveified.setVisibility(View.GONE);
        }
        if(verifiedList.size()>0){
            rl_veified.setVisibility(View.VISIBLE);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(VerifyInfo.this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rv_verified.setLayoutManager(layoutManager);
            verifiedContactFacilityAdapter = new VerifiedContactFacilityAdapter(VerifyInfo.this, verifiedList);
            rv_verified.setAdapter(verifiedContactFacilityAdapter);
            verifiedContactFacilityAdapter.notifyDataSetChanged();
        }else{
            rl_veified.setVisibility(View.GONE);
        }
    }
    public void saveCustomerDetails(final String userID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.CUSTOMERS_DETAIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        customerList.clear();
                        dbDelete.deleteTableByName("CustomerDetails", userID);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CustomerDetailsProperty info = new CustomerDetailsProperty();
                            info.customer_id = jsonObject.getString("customerId") == null ? "" : jsonObject.getString("customerId").equals("") ? "" : jsonObject.getString("customerId");
                            info.customerName = jsonObject.getString("customerName") == null ? "" : jsonObject.getString("customerName").equals("") ? "" : jsonObject.getString("customerName");
                            info.workEmailAddress = jsonObject.getString("workEmailAddress") == null ? "" : jsonObject.getString("workEmailAddress").equals("") ? "" : jsonObject.getString("workEmailAddress");
                            info.departmentName = jsonObject.getString("departmentName") == null ? "" : jsonObject.getString("departmentName").equals("") ? "" : jsonObject.getString("departmentName");
                            info.employeeNumber = jsonObject.getString("employeeNumber") == null ? "" : jsonObject.getString("employeeNumber").equals("") ? "" : jsonObject.getString("employeeNumber");
                            info.title = jsonObject.getString("title") == null ? "" : jsonObject.getString("title").equals("") ? "" : jsonObject.getString("title");
                            info.workPhone = jsonObject.getString("workPhone") == null ? "" : jsonObject.getString("workPhone").equals("") ? "" : jsonObject.getString("workPhone");
                            info.hasCopAccess = jsonObject.getString("hasCopAccess") == null ? "" : jsonObject.getString("hasCopAccess").equals("") ? "" : jsonObject.getString("hasCopAccess");
                            //added 3 field 03-09-2020
                            info.emailVerified = jsonObject.getString("emailVerified") == null ? "" : jsonObject.getString("emailVerified").equals("") ? "" : jsonObject.getString("emailVerified");
                            info.phoneVerified = jsonObject.getString("phoneVerified") == null ? "" : jsonObject.getString("phoneVerified").equals("") ? "" : jsonObject.getString("phoneVerified");
                            info.isPrivate = jsonObject.getString("isPrivate") == null ? "" : jsonObject.getString("isPrivate").equals("") ? "" : jsonObject.getString("isPrivate");
                            customerList.add(info);
                            dbInsert.addDataIntoCustomerDetailsTable(info);
                        }
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                    dismissWaitDialog();
                } finally {
                    dismissWaitDialog();
                   if(customerList.size()>0){
                       showSpecificEntityVerification((ArrayList<CustomerDetailsProperty>) customerList);
                   }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                String errorMsg = error.getMessage().toString();
                if (errorMsg.equals("com.android.volley.AuthFailureError")) {
                    AlertDialogManager.showCustomDialog(VerifyInfo.this, "Error", "Authicatiion Error.", false, null, null, "Ok", "", null);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(VerifyInfo.this);
        requestQueue11.add(stringRequest);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class,"");
                break;
            case R.id.rl_frequently_asked_qus:
                commonIntentMethod(ProcessVerifyInfo.class,"FrequentlyAsked");
                break;

        }
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(SafetyCards.class,"");
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }


    public void commonIntentMethod(Class activity, String type) {
        myTrace.stop();
        Intent intent = new Intent(VerifyInfo.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("Type",type);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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
        analytics.setCurrentScreen(this, "RegisterSafetyCard", this.getClass().getSimpleName());
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
                pDialog = new ProgressDialog(VerifyInfo.this);
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
