package com.elearn.trainor.ConcoPhilips;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.elearn.trainor.CourseModule.Courses;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.InternetConnectivityReceiver;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.NeedSupport;
import com.elearn.trainor.PropertyClasses.COPProperty;
import com.elearn.trainor.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConcoPhilips extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome, ll_register_psi, firstTimeView, ll_go_to_help, ll_start_psi_course;
    RelativeLayout tbl_actionbar, rl_when_psi_course_taken;
    TextView text_header, tv_try_again, tv_psi_not_uploaded;
    Button btn_start_psi_course;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dbDelete;
    SharedPreferenceManager spManager;
    SharedPreferences.Editor editor;
    String psiCourseLisenceId = "", dbCourseStatus = "", spCourseStatus;
    ConnectionDetector connectionDetector;
    ProgressDialog pDialog;
    List<COPProperty> notUploadedCardsList, temproryCradList;
    IntentFilter internet_intent_filter;
    InternetConnectivityReceiver internetConnectivityReceiver;
    ImageView psi_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conco_philips);
        getControls();
    }

    public void getControls() {
        dbSelect = new DataBaseHandlerSelect(this);
        dbDelete = new DataBaseHandlerDelete(this);
        spManager = new SharedPreferenceManager(ConcoPhilips.this);
        editor = spManager.COPSharedPreference();
        notUploadedCardsList = new ArrayList<>();
        connectionDetector = new ConnectionDetector(ConcoPhilips.this);
        int actionBarBackground = getResources().getColor(R.color.color_conco_philips);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(actionBarBackground);

        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getString(R.string.conocophillips));
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_register_psi = (LinearLayout) findViewById(R.id.ll_register_psi);
        tv_try_again = (TextView) findViewById(R.id.tv_try_again);
        tv_psi_not_uploaded = (TextView) findViewById(R.id.tv_psi_not_uploaded);
        firstTimeView = (LinearLayout) findViewById(R.id.firstTimeView);
        ll_go_to_help = (LinearLayout) findViewById(R.id.ll_go_to_help);
        btn_start_psi_course = (Button) findViewById(R.id.btn_start_psi_course);
        rl_when_psi_course_taken = (RelativeLayout) findViewById(R.id.rl_when_psi_course_taken);
        ll_start_psi_course = findViewById(R.id.ll_start_psi_course);
        psi_image = (ImageView) findViewById(R.id.iv_psi_logo);

        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        ll_register_psi.setOnClickListener(this);
        tv_try_again.setOnClickListener(this);
        btn_start_psi_course.setOnClickListener(this);
        ll_go_to_help.setOnClickListener(this);
        psi_image.setOnClickListener(this);
        ll_start_psi_course.setOnClickListener(this);
        dbCourseStatus = dbSelect.getDataFromCoursesTable("status", spManager.getUserID(), spManager.getCOPlisenceId());
        dbCourseStatus = dbCourseStatus.equals("") ? dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), spManager.getCOPlisenceId(), "cmiCompletionStatus") : "";
        showHideCradsNumbers();

        //dbSelect.getDataFromCoursesTable("*", spManager.getUserID(), );

        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        internetConnectivityReceiver = new InternetConnectivityReceiver();
        this.registerReceiver(internetConnectivityReceiver, internet_intent_filter);
    }

    public void showHideCradsNumbers() {
        notUploadedCardsList = dbSelect.getNotUploadedCOPCards(spManager.getUserID());
        if (notUploadedCardsList.size() > 0) {
            tv_try_again.setVisibility(View.VISIBLE);
            tv_psi_not_uploaded.setVisibility(View.VISIBLE);
            int cardsToBeUploaded = notUploadedCardsList.size();
            if (cardsToBeUploaded == 1) {
                tv_psi_not_uploaded.setText(cardsToBeUploaded + " " + getResources().getString(R.string.psi_card_not_uploaded));
            } else {
                tv_psi_not_uploaded.setText(cardsToBeUploaded + " " + getResources().getString(R.string.psi_cards_not_uploaded));
            }
        } else {
            tv_try_again.setVisibility(View.GONE);
            tv_psi_not_uploaded.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                //commonIntentMethod(HomePage.class);
                onBackPressed();
                break;
            case R.id.llhome:
                //commonIntentMethod(HomePage.class);
                onBackPressed();
                break;
            case R.id.ll_register_psi:
                commonIntentMethod(RegisterPSI.class);
                break;
            case R.id.tv_try_again:
                //AlertDialogManager.showDialog(ConcoPhilips.this, "", "Yet to implement", false, null);
                if (connectionDetector.isConnectingToInternet()) {
                    getDataFromList();
                } else {
                    AlertDialogManager.showDialog(ConcoPhilips.this, getResources().getString(R.string.not_connected_internet_title), getResources().getString(R.string.not_connected_internet_msg), false, null);
                }

                break;
           /* case R.id.btn_start_psi_course:
                String PSIlisenceId = spManager.getCOPlisenceId();
                dbCourseStatus = dbSelect.getDataFromCoursesTable("status", spManager.getUserID(), spManager.getCOPlisenceId());
                if (dbCourseStatus.equals("NONE") || dbCourseStatus.equals("STARTED") || spManager.getCOPcourseStatus().equals("started")
                        || spManager.getCOPcourseStatus().equals("none")) {
                    commonIntentMethod(Courses.class);
                } else if (PSIlisenceId.equals("") || PSIlisenceId == null || dbCourseStatus.equals("")) {
                    AlertDialogManager.showDialog(ConcoPhilips.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, null);
                }

                break;*/
            case R.id.ll_go_to_help:
                commonIntentMethod(NeedSupport.class);
                break;
            case R.id.iv_psi_logo:
                commonIntentMethod(RegisterPSI.class);
                break;
            case R.id.ll_start_psi_course:
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    getPSICourseLisence();
                } else {
                    String spCourseStatus = spManager.getCOPlisenceId();
                    if(!spCourseStatus.equals("")){
                        commonIntentMethod(Courses.class);
                    }else{
                        AlertDialogManager.showDialog(ConcoPhilips.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, null);
                    }
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (internetConnectivityReceiver != null) {
            if (internetConnectivityReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetConnectivityReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        commonIntentMethod(HomePage.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        super.onBackPressed();
    }

    // chnages has to be done 23-04-2020
    public void getPSICourseLisence() {
        // chnages has to be done 23-04-2020
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.PsiCourseCompletion, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("licenseId")) {
                                psiCourseLisenceId = jsonObject.getString("licenseId");
                                editor.putString("COPLisenceID", psiCourseLisenceId);
                            }
                            if (jsonObject.has("status")) {
                                spCourseStatus = jsonObject.getString("status");
                                editor.putString("COPCourseStatus", spCourseStatus);
                            }
                            editor.commit();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    /*if (!psiCourseLisenceId.equals("")) {
                        String spCourseStatus = spManager.getCOPcourseStatus();
                        if (spCourseStatus.equals("approved") || dbCourseStatus.equalsIgnoreCase("completed")) {
                            firstTimeView.setVisibility(View.GONE);
                            rl_when_psi_course_taken.setVisibility(View.VISIBLE);
                        } else {
                            firstTimeView.setVisibility(View.VISIBLE);
                            rl_when_psi_course_taken.setVisibility(View.GONE);
                            if (spCourseStatus.equals("started")) {
                                btn_start_psi_course.setText(getResources().getString(R.string.resume_courses));
                            }
                        }
                        //commonIntentMethod(Courses.class);
                        //commonIntentMethod(RegisterPSI.class);

                    }*/
                    if (!psiCourseLisenceId.equals("")) {
                        commonIntentMethod(Courses.class);
                    }
                    if (pDialog != null && pDialog.isShowing()) {
                        dismissWaitDialog();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }

                final String status_Code = String.valueOf(error.networkResponse.statusCode);
                Log.d("Satus Code= ", status_Code);
                if (status_Code.equals("404")) {
                    //txtMessage.setText(getResources().getString(R.string.ready));
                    callEnrollInPsiCourse();
                } else if (status_Code.equals("500")) {
                    Toast.makeText(ConcoPhilips.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void callEnrollInPsiCourse() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.EnrollPSICourse, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            if (jsonObject.has("licenseId")) {
                                psiCourseLisenceId = jsonObject.getString("licenseId");
                                editor.putString("COPLisenceID", psiCourseLisenceId);
                            }
                            if (jsonObject.has("status")) {
                                spCourseStatus = jsonObject.getString("status");
                                editor.putString("COPCourseStatus", spCourseStatus);
                            }
                            editor.commit();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                   /* if (!psiCourseLisenceId.equals("")) {
                        String spCourseStatus = spManager.getCOPcourseStatus();
                        if (spCourseStatus.equals("approved") || dbCourseStatus.equalsIgnoreCase("completed")) {
                            firstTimeView.setVisibility(View.GONE);
                            rl_when_psi_course_taken.setVisibility(View.VISIBLE);
                        } else {
                            firstTimeView.setVisibility(View.VISIBLE);
                            rl_when_psi_course_taken.setVisibility(View.GONE);
                            if (spCourseStatus.equals("started")) {
                                btn_start_psi_course.setText(getResources().getString(R.string.resume_courses));
                            }
                        }
                        //commonIntentMethod(Courses.class);
                        //commonIntentMethod(RegisterPSI.class);

                    }*/
                    if (!psiCourseLisenceId.equals("")) {
                        commonIntentMethod(Courses.class);
                    }
                    if (pDialog != null && pDialog.isShowing()) {
                        dismissWaitDialog();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ConcoPhilips.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(ConcoPhilips.this, activity);
        //intent.putExtra("FromCOP", "True");
        intent.putExtra("From", "COP");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void showWaitDialog() {

        if (pDialog == null) {
            pDialog = new ProgressDialog(ConcoPhilips.this);
        }
        try {
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dismissWaitDialog() {

        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

    }

    public void getDataFromList() {

        COPProperty copProperty = new COPProperty();
        copProperty.cardID = notUploadedCardsList.get(0).cardID;
        copProperty.userId = notUploadedCardsList.get(0).userId;
        copProperty.lisenceId = notUploadedCardsList.get(0).lisenceId;
        copProperty.cardStatus = notUploadedCardsList.get(0).cardStatus;
        copProperty.courseStatus = notUploadedCardsList.get(0).courseStatus;
        copProperty.regdDate = notUploadedCardsList.get(0).regdDate;
        copProperty.placePlatformName = notUploadedCardsList.get(0).placePlatformName;
        copProperty.placePlatformId = notUploadedCardsList.get(0).placePlatformId;
        copProperty.departmentName = notUploadedCardsList.get(0).departmentName;
        copProperty.departmentId = notUploadedCardsList.get(0).departmentId;
        if (copProperty.departmentId.equals("")) {
            copProperty.departmentId = "null";
        }
        copProperty.topicDiscussed = notUploadedCardsList.get(0).topicDiscussed;
        copProperty.riskIdentified = notUploadedCardsList.get(0).riskIdentified;
        copProperty.plannedFollowUp = notUploadedCardsList.get(0).plannedFollowUp;
        copProperty.heatColdStatus = notUploadedCardsList.get(0).heatColdStatus;
        copProperty.pressureStatus = notUploadedCardsList.get(0).pressureStatus;
        copProperty.chemicalStatus = notUploadedCardsList.get(0).chemicalStatus;
        copProperty.electricalStatus = notUploadedCardsList.get(0).electricalStatus;
        copProperty.gravityStatus = notUploadedCardsList.get(0).gravityStatus;
        copProperty.radiationStatus = notUploadedCardsList.get(0).radiationStatus;
        copProperty.noiseStatus = notUploadedCardsList.get(0).noiseStatus;
        copProperty.biologicalStatus = notUploadedCardsList.get(0).biologicalStatus;
        copProperty.energyMovementStatus = notUploadedCardsList.get(0).energyMovementStatus;
        copProperty.presentMomentStatus = notUploadedCardsList.get(0).presentMomentStatus;

        callRegisterAPI(copProperty);
    }

    public void callRegisterAPI(final COPProperty copProperty) {
        showWaitDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.RegisterCOPCard, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (response.equals("")) {
                        int cardIdToDeleteFrmDB = notUploadedCardsList.get(0).cardID;
                        dbDelete.deleteUploadedCardFromCOP(cardIdToDeleteFrmDB);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (notUploadedCardsList.size() > 0) {
                        notUploadedCardsList.remove(0);
                        if (notUploadedCardsList.size() > 0) {
                            getDataFromList();
                        } else {
                            showHideCradsNumbers();
                            dismissWaitDialog();
                        }
                    }
                } finally {
                    if (notUploadedCardsList.size() > 0) {
                        //int cardIdToDeleteFrmDB = notUploadedCardsList.get(0).cardID;
                        notUploadedCardsList.remove(0);
                        if (notUploadedCardsList.size() > 0) {
                            getDataFromList();
                        } else {
                            showHideCradsNumbers();
                            dismissWaitDialog();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // only for testing , have to be removed after proper reposne from post api
                if (notUploadedCardsList.size() > 0) {
                    //int cardIdToDeleteFrmDB = notUploadedCardsList.get(0).cardID;
                    notUploadedCardsList.remove(0);
                    if (notUploadedCardsList.size() > 0) {
                        getDataFromList();
                    } else {
                        showHideCradsNumbers();
                        dismissWaitDialog();
                    }
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
                String energyTypes = "[\"" + copProperty.heatColdStatus + "\",\"" + copProperty.pressureStatus + "\",\"" + copProperty.chemicalStatus + "\",\"" +
                        copProperty.electricalStatus + "\",\"" + copProperty.gravityStatus + "\",\"" + copProperty.radiationStatus + "\",\"" + copProperty.noiseStatus + "\",\"" +
                        copProperty.biologicalStatus + "\",\"" + copProperty.energyMovementStatus + "\"]";
                energyTypes = energyTypes.replace(",\"\"", "");
                energyTypes = energyTypes.replace("\"\",", "");
                if (energyTypes.equals("[\"\"]")) {
                    energyTypes = "[]";
                }
                String str = "{ \"plannedFollowUp\": \"" + copProperty.plannedFollowUp + "\",\"conversedAt\": \"" + copProperty.regdDate + "\",\"groupId\":" + copProperty.departmentId + ",\"didConversationIdentifyNewRisks\": " + copProperty.riskIdentified + " ,\"presentInTheMomentMarked\": " + copProperty.presentMomentStatus + ",\"energyTypes\": " + energyTypes + ", \"facilityId\": " + copProperty.placePlatformId + " ,\"issueDiscussed\": \"" + copProperty.topicDiscussed + "\"}";
                String replacedString = str.replace("\n",  "\\n");
                return replacedString.getBytes();
                //return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void callAPI() {
        if (notUploadedCardsList != null && notUploadedCardsList.size() > 0) {
            getDataFromList();
        }
    }
}
