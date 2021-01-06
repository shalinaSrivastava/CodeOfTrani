package com.elearn.trainor.ConcoPhilips;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.elearn.trainor.ConcoPhilips.nLevel.NLevelAdapter;
import com.elearn.trainor.ConcoPhilips.nLevel.NLevelItem;
import com.elearn.trainor.ConcoPhilips.nLevel.NLevelView;
import com.elearn.trainor.ConcoPhilips.nLevel.SomeObject;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.CustomTag;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.COPProperty;
import com.elearn.trainor.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterPSI extends AppCompatActivity implements View.OnClickListener {
    public static RegisterPSI instance;
    LinearLayout ll_back, llhome;
    RelativeLayout tbl_actionbar;
    TextView text_header, txt_name, txt_place, txt_department;
    EditText edt_date, edt_topic_discussed, edt_planned_folow_up;
    SharedPreferenceManager spManager;
    SharedPreferences.Editor editor, COPfacilityEditor;
    Locale myLocale;
    SwitchCompat btn_risk_indentified, switch_heat_cold, switch_pressure, switch_chemical, switch_electrical, switch_radiation, switch_gravity,
            switch_noise, switch_biological, switch_energy_movement, switch_present_in_moment;
    String currentDate, formattedDate, uploadExceptionString = "", selectedFacilityGroup = "";
    Button btn_register;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerUpdate dbUpdate;
    ConnectionDetector connectionDetector;
    COPProperty copProperty = new COPProperty();
    List<NLevelItem> list;
    ListView listView;
    AlertDialog placePlatformListDialog;
    JSONObject groupArray = null;
    ProgressDialog pDialog;
    String spArrayObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_psi);
        getControls();
        instance = this;
        if (spManager.getSharedPreferenceExistence()) {
            Configuration config = getBaseContext().getResources().getConfiguration();
            myLocale = new Locale(spManager.getLanguage());
            Locale.setDefault(myLocale);
            config.locale = myLocale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public static RegisterPSI getInstance() {
        if (instance == null) {
            instance = new RegisterPSI();
        }
        return instance;
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(RegisterPSI.this);
        editor = spManager.COPSharedPreference();
        COPfacilityEditor = spManager.COPFacilityPreference();
        dbSelect = new DataBaseHandlerSelect(this);
        dbInsert = new DataBaseHandlerInsert(this);
        dbUpdate = new DataBaseHandlerUpdate(this);
        dbDelete = new DataBaseHandlerDelete(this);
        connectionDetector = new ConnectionDetector(RegisterPSI.this);
        int actionBarBackground = getResources().getColor(R.color.color_conco_philips);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getString(R.string.register_psi));
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        edt_date = (EditText) findViewById(R.id.edt_date);
        edt_planned_folow_up = (EditText) findViewById(R.id.edt_planned_folow_up);
        edt_topic_discussed = (EditText) findViewById(R.id.edt_topic_discussed);
        txt_name = (TextView) findViewById(R.id.txt_name);
        switch_heat_cold = (SwitchCompat) findViewById(R.id.switch_heat_cold);
        switch_pressure = (SwitchCompat) findViewById(R.id.switch_pressure);
        switch_chemical = (SwitchCompat) findViewById(R.id.switch_chemical);
        switch_electrical = (SwitchCompat) findViewById(R.id.switch_electrical);
        switch_radiation = (SwitchCompat) findViewById(R.id.switch_radiation);
        switch_gravity = (SwitchCompat) findViewById(R.id.switch_gravity);
        switch_noise = (SwitchCompat) findViewById(R.id.switch_noise);
        switch_biological = (SwitchCompat) findViewById(R.id.switch_biological);
        switch_energy_movement = (SwitchCompat) findViewById(R.id.switch_energy_movement);
        switch_present_in_moment = (SwitchCompat) findViewById(R.id.switch_present_in_moment);
        btn_risk_indentified = (SwitchCompat) findViewById(R.id.btn_risk_indentified);
        //btn_no = (Button) findViewById(R.id.btn_no);
        btn_register = (Button) findViewById(R.id.btn_register);
        txt_place = (TextView) findViewById(R.id.txt_place);
        txt_department = (TextView) findViewById(R.id.txt_department);

        tbl_actionbar.setBackgroundColor(actionBarBackground);
        txt_name.setText(spManager.getFirstname() + " " + spManager.getLastname());
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        edt_date.setOnClickListener(this);
        switch_heat_cold.setOnClickListener(this);
        switch_pressure.setOnClickListener(this);
        switch_chemical.setOnClickListener(this);
        switch_electrical.setOnClickListener(this);
        switch_radiation.setOnClickListener(this);
        switch_gravity.setOnClickListener(this);
        switch_noise.setOnClickListener(this);
        switch_biological.setOnClickListener(this);
        switch_energy_movement.setOnClickListener(this);
        switch_present_in_moment.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        btn_risk_indentified.setOnClickListener(this);
        txt_place.setOnClickListener(this);
        txt_department.setOnClickListener(this);
        if (connectionDetector.isConnectingToInternet()) {
            showWaitDialog();
            getFacilitynGroups();
        }
        setDefault();
        edt_planned_folow_up.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString().trim();
                editor.putString("PlannedFollowUp", val);
                editor.commit();
            }
        });

        edt_topic_discussed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString().trim();
                editor.putString("TopicDiscussed", val);
                editor.commit();
            }
        });
    }

    public void getFacilitynGroups() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.GetFacility, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    response = new String(response.getBytes("ISO-8859-1"), "UTF-8");
                    if (response != null && !response.equals("")) {
                        response = response.replaceAll("\n", "");
                        spManager.removeValuefromCOPFacilityPref("FacilityResponse");
                        COPfacilityEditor.putString("FacilityResponse", response);
                        COPfacilityEditor.commit();
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
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

    public void setDefault() {
        SimpleDateFormat changedDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        if (spManager.getCOPregdDate().equals("")) {
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            currentDate = dateFormater.format(date);
            formattedDate = changedDateFormat.format(date);
            edt_date.setText(formattedDate);
            editor.putString("RegdDate", currentDate);
            editor.commit();
        } else {
            Date convertedDate = new Date();
            try {
                convertedDate = dateFormater.parse(spManager.getCOPregdDate());
                formattedDate = dateFormater.format(convertedDate);
                formattedDate = changedDateFormat.format(convertedDate);
                edt_date.setText(formattedDate);
            } catch (ParseException ex) {
                Log.d("Error", ex.getMessage());
            }
        }
        if (!spManager.getCOPplacePlatformName().equals("") || spManager.getCOPplacePlatformName() == null) {
            txt_place.setText(spManager.getCOPplacePlatformName());
            txt_place.setTextColor(getResources().getColor(R.color.color_black));
        } else {
            txt_place.setText(getResources().getString(R.string.placeplatform));
            txt_place.setTextColor(getResources().getColor(R.color.color_gray));
        }

        if (!spManager.getCOPdepartmentName().equals("") || spManager.getCOPdepartmentName() == null) {
            txt_department.setTextColor(getResources().getColor(R.color.color_black));
            txt_department.setText(spManager.getCOPdepartmentName());
        } else {
            txt_department.setText(getResources().getString(R.string.department));
            txt_department.setTextColor(getResources().getColor(R.color.color_gray));
        }
        String follow_up_string = spManager.getCOPplannedFollowUp();
        edt_planned_folow_up.setText(spManager.getCOPplannedFollowUp());
        edt_planned_folow_up.setSelection(follow_up_string.length());
        String val_edt_topic_discussed = spManager.getCOPtopicDiscussed();
        edt_topic_discussed.setText(val_edt_topic_discussed);
        edt_topic_discussed.setSelection(val_edt_topic_discussed.length());
        System.out.println(val_edt_topic_discussed);
        if (spManager.getCOPriskIdentified().equals("true")) {
            btn_risk_indentified.setChecked(true);
        } else {
            btn_risk_indentified.setChecked(false);
        }
        if (spManager.getCOPheatColdStatus().equals("HEAT_OR_COLD")) {
            switch_heat_cold.setChecked(true);
        } else {
            switch_heat_cold.setChecked(false);
        }
        if (spManager.getCOPpressureStatus().equals("PRESSURE")) {
            switch_pressure.setChecked(true);
        } else {
            switch_pressure.setChecked(false);
        }
        if (spManager.getCOPchemicalStatus().equals("CHEMICAL")) {
            switch_chemical.setChecked(true);
        } else {
            switch_chemical.setChecked(false);
        }
        if (spManager.getCOPelectricalStatus().equals("ELECTRICAL")) {
            switch_electrical.setChecked(true);
        } else {
            switch_electrical.setChecked(false);
        }
        if (spManager.getCOPradiationStatus().equals("RADIATION")) {
            switch_radiation.setChecked(true);
        } else {
            switch_radiation.setChecked(false);
        }
        if (spManager.getCOPgravityStatus().equals("GRAVITY")) {
            switch_gravity.setChecked(true);
        } else {
            switch_gravity.setChecked(false);
        }
        if (spManager.getCOPnoiseStatus().equals("NOISE")) {
            switch_noise.setChecked(true);
        } else {
            switch_noise.setChecked(false);
        }
        if (spManager.getCOPbiologicalStatus().equals("BIOLOGICAL")) {
            switch_biological.setChecked(true);
        } else {
            switch_biological.setChecked(false);
        }
        if (spManager.getCOPenergyMovementStatus().equals("MOVEMENT")) {
            switch_energy_movement.setChecked(true);
        } else {
            switch_energy_movement.setChecked(false);
        }
        if (spManager.getCOPpresentMomentStatus().equals("true")) {
            switch_present_in_moment.setChecked(true);
        } else {
            switch_present_in_moment.setChecked(false);
        }
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(ConcoPhilips.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                commonIntentMethod(ConcoPhilips.class);
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class);
                break;
            case R.id.switch_heat_cold:
                if (switch_heat_cold.isChecked()) {
                    editor.putString("HeatColdStatus", "HEAT_OR_COLD");
                } else {
                    editor.putString("HeatColdStatus", "");
                }
                break;
            case R.id.switch_pressure:
                if (switch_pressure.isChecked()) {
                    editor.putString("PressureStatus", "PRESSURE");
                } else {
                    editor.putString("PressureStatus", "");
                }
                break;
            case R.id.switch_chemical:
                if (switch_chemical.isChecked()) {
                    editor.putString("ChemicalStatus", "CHEMICAL");
                } else {
                    editor.putString("ChemicalStatus", "");
                }
                break;
            case R.id.switch_electrical:
                if (switch_electrical.isChecked()) {
                    editor.putString("ElectricalStatus", "ELECTRICAL");
                } else {
                    editor.putString("ElectricalStatus", "");
                }
                break;
            case R.id.switch_gravity:
                if (switch_gravity.isChecked()) {
                    editor.putString("GravityStatus", "GRAVITY");
                } else {
                    editor.putString("GravityStatus", "");
                }
                break;
            case R.id.switch_radiation:
                if (switch_radiation.isChecked()) {
                    editor.putString("RadiationStatus", "RADIATION");
                } else {
                    editor.putString("RadiationStatus", "");
                }
                break;

            case R.id.switch_noise:
                if (switch_noise.isChecked()) {
                    editor.putString("NoiseStatus", "NOISE");
                } else {
                    editor.putString("NoiseStatus", "");
                }
                break;
            case R.id.switch_biological:
                if (switch_biological.isChecked()) {
                    editor.putString("BiologicalStatus", "BIOLOGICAL");
                } else {
                    editor.putString("BiologicalStatus", "");
                }
                break;
            case R.id.switch_energy_movement:
                if (switch_energy_movement.isChecked()) {
                    editor.putString("EnergyMovementStatus", "MOVEMENT");
                } else {
                    editor.putString("EnergyMovementStatus", "");
                }
                break;
            case R.id.switch_present_in_moment:
                if (switch_present_in_moment.isChecked()) {
                    copProperty.presentMomentStatus = "true";
                    editor.putString("PresentMomemtStatus", "true");
                } else {
                    copProperty.presentMomentStatus = "false";
                    editor.putString("PresentMomemtStatus", "false");
                }
                break;
            case R.id.btn_risk_indentified:
                if (btn_risk_indentified.isChecked()) {
                    copProperty.riskIdentified = "true";
                    editor.putString("RiskIdentified", "true");
                } else {
                    copProperty.riskIdentified = "false";
                    editor.putString("RiskIdentified", "false");
                }
                break;
            case R.id.btn_register:
                registerCard();
                break;

            case R.id.edt_date:
                Calendar mcurrentDate = Calendar.getInstance();
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mYear = mcurrentDate.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        selectedMonth = selectedMonth + 1;
                        String date = "", tempDay = selectedDay + "", tempMonth = selectedMonth + "";
                        if (selectedDay >= 0 && selectedDay < 10) {
                            tempDay = "0" + selectedDay;
                        } else {
                            tempDay = selectedDay + "";
                        }
                        if (selectedMonth >= 0 && selectedMonth < 10) {
                            tempMonth = "0" + selectedMonth;
                        } else {
                            tempMonth = selectedMonth + "";
                        }
                        date = tempDay + "." + tempMonth + "." + selectedYear;
                        edt_date.setText(date);
                        String formateddate_to_store = selectedYear + "-" + tempMonth + "-" + tempDay;
                        editor.putString("RegdDate", formateddate_to_store);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
                break;
            case R.id.txt_place:
                showWaitDialog();
                openCompanyListDialog("placePlatform");
                break;
            case R.id.txt_department:
                //if (!spManager.getCOPplacePlatformId().equals("") || spManager.getCOPplacePlatformId() != null) {
                if (!txt_place.getText().toString().trim().equals(getResources().getString(R.string.placeplatform))) {
                    spArrayObject = spManager.getCOPGroupObject();
                    if (groupArray != null || spArrayObject != null) {
                        openCompanyListDialog("department");
                    } else {
                        AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.not_assocaiated_dept), false, null);
                    }
                } else {
                    AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_place_platform_first), false, null);
                }
                break;
        }
        editor.commit();
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(RegisterPSI.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void registerCard() {
        final COPProperty copProperty = new COPProperty();
        copProperty.userId = spManager.getUserID();
        copProperty.lisenceId = spManager.getCOPlisenceId();
        copProperty.cardStatus = "notUploaded";
        copProperty.courseStatus = "completed";
        copProperty.regdDate = spManager.getCOPregdDate();
        copProperty.placePlatformName = spManager.getCOPplacePlatformName();
        copProperty.placePlatformId = spManager.getCOPplacePlatformId();
        copProperty.departmentName = spManager.getCOPdepartmentName();
        copProperty.departmentId = spManager.getCOPdepartmentId();
        copProperty.topicDiscussed = spManager.getCOPtopicDiscussed();
        copProperty.riskIdentified = spManager.getCOPriskIdentified();
        copProperty.plannedFollowUp = spManager.getCOPplannedFollowUp();
        copProperty.heatColdStatus = spManager.getCOPheatColdStatus();
        copProperty.pressureStatus = spManager.getCOPpressureStatus();
        copProperty.chemicalStatus = spManager.getCOPchemicalStatus();
        copProperty.electricalStatus = spManager.getCOPelectricalStatus();
        copProperty.gravityStatus = spManager.getCOPgravityStatus();
        copProperty.radiationStatus = spManager.getCOPradiationStatus();
        copProperty.noiseStatus = spManager.getCOPnoiseStatus();
        copProperty.biologicalStatus = spManager.getCOPbiologicalStatus();
        copProperty.energyMovementStatus = spManager.getCOPenergyMovementStatus();
        copProperty.presentMomentStatus = spManager.getCOPpresentMomentStatus();

        if (connectionDetector.isConnectingToInternet()) {
            if (copProperty.regdDate.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_regd_date), false, null);
            } else if (copProperty.placePlatformId.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_place_platform), false, null);
            } else if (!selectedFacilityGroup.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_dept), false, null);
            } else if (copProperty.topicDiscussed.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.blank_topic_discussed), false, null);
            } else {
                callRegisterAPI(copProperty);
            }
        } else {
            if (copProperty.regdDate.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_regd_date), false, null);
            } else if (copProperty.placePlatformId.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_place_platform), false, null);
            } else if (!selectedFacilityGroup.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_dept), false, null);
            } else if (copProperty.topicDiscussed.equals("")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.blank_topic_discussed), false, null);
            } /*else if (copProperty.departmentId.equals("") && (groupArray != null || spArrayObject != null) ) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.select_valid_dept), false, null);
            }*/ else {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.saved_offline_msg), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        dbInsert.addDataIntoCOPTable(copProperty);
                        removePreferenceFieldsData();
                        commonIntentMethod(ConcoPhilips.class);
                    }
                });
            }
            dismissWaitDialog();
        }
    }

    public void callRegisterAPI(final COPProperty copProperty) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.RegisterCOPCard, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && response.equals("")) {
                        Log.d("Uploaded", "Uploaded Sucessfully");
                    }
                } catch (Exception e) {
                    uploadExceptionString = "UploadException";
                } finally {
                    dismissWaitDialog();
                    if (uploadExceptionString.equals("UploadException")) {
                        AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.something_went_wrong), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                dbInsert.addDataIntoCOPTable(copProperty);
                                commonIntentMethod(ConcoPhilips.class);
                            }
                        });
                    } else {
                        AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.registered_successfully), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                removePreferenceFieldsData();
                                commonIntentMethod(ConcoPhilips.class);
                            }
                        });
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.something_went_wrong), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        dbInsert.addDataIntoCOPTable(copProperty);
                        removePreferenceFieldsData();
                        commonIntentMethod(ConcoPhilips.class);
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

            @Override
            public byte[] getBody() throws AuthFailureError {
                String departmentid;
                if (copProperty.departmentId.equals("")) {
                    departmentid = "null";
                } else {
                    departmentid = copProperty.departmentId;
                }
                String energyTypes = "[\"" + copProperty.heatColdStatus + "\",\"" + copProperty.pressureStatus + "\",\"" + copProperty.chemicalStatus + "\",\"" +
                        copProperty.electricalStatus + "\",\"" + copProperty.gravityStatus + "\",\"" + copProperty.radiationStatus + "\",\"" + copProperty.noiseStatus + "\",\"" +
                        copProperty.biologicalStatus + "\",\"" + copProperty.energyMovementStatus + "\"]";
                energyTypes = energyTypes.replace(",\"\"", "");
                // energyTypes = energyTypes.replace("\"\"","");
                energyTypes = energyTypes.replace("\"\",", "");
                if (energyTypes.equals("[\"\"]")) {
                    energyTypes = "[]";
                }
                String str = "{ \"plannedFollowUp\": \"" + copProperty.plannedFollowUp + "\",\"conversedAt\": \"" + copProperty.regdDate + "\",\"groupId\":" + departmentid + ",\"didConversationIdentifyNewRisks\": " + copProperty.riskIdentified + " ,\"presentInTheMomentMarked\": " + copProperty.presentMomentStatus + ",\"energyTypes\": " + energyTypes + ", \"facilityId\": " + copProperty.placePlatformId + " ,\"issueDiscussed\": \"" + copProperty.topicDiscussed + "\"}";
                String replacedString = str.replace("\n",  "\\n");
                return replacedString.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    private void nestedLoop(String levelList, NLevelItem nLevelItem, final LayoutInflater inflater, int level, String Type) {
        try {
            JSONArray jsonArrayStringList = new JSONArray(levelList);
            int length = jsonArrayStringList.length();
            if (Type.equals("placePlatform")) {
                for (int i = 0; i < length; i++) {
                    JSONObject itemObject = jsonArrayStringList.getJSONObject(i);
                    JSONObject facObj = itemObject.getJSONObject("facility");
                    String val = facObj.getString("name");
                    String id = facObj.getString("id");
                    boolean closedForRegistration = facObj.getBoolean("closedForRegistration");
                    int childrenSize = itemObject.getJSONArray("subFacilities").length();
                    NLevelItem Parent = itemView(i, val, id, closedForRegistration, itemObject, nLevelItem, inflater, level, !(childrenSize > 0), "placePlatform");
                    list.add(Parent);
                    if (childrenSize > 0) {
                        nestedLoop(itemObject.getJSONArray("subFacilities").toString(), Parent, inflater, level + 1, Type);
                    }
                }
            } else if (Type.equals("department")) {
                JSONObject iObject = jsonArrayStringList.getJSONObject(0);
                JSONArray itemObject = iObject.getJSONArray("groups");
                for (int i = 0; i < itemObject.length(); i++) {
                    JSONObject jsonObject = itemObject.getJSONObject(i);
                    String val = jsonObject.getString("name");
                    String id = jsonObject.getString("id");
                    boolean closedForRegistration = jsonObject.getBoolean("closedForRegistration");
                    NLevelItem Parent = itemView(i, val, id, closedForRegistration, null, nLevelItem, inflater, level, true, "department");
                    list.add(Parent);
                }
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    private NLevelItem itemView(int itemRow, final String Title, final String id, final boolean closed_for_registration, final JSONObject jsonObject, NLevelItem nLevelItem, final LayoutInflater inflater, final int level, final boolean isLast, final String type) {
        NLevelItem superChild = new NLevelItem(new SomeObject(Title, id, closed_for_registration, jsonObject), nLevelItem, new NLevelView() {
            @Override
            public View getView(NLevelItem item) {
                View view = inflater.inflate(R.layout.list_item, null);
                TextView tv = (TextView) view.findViewById(R.id.textView);
                ImageView iv = (ImageView) view.findViewById(R.id.img_expand_view);
                String name = (String) ((SomeObject) item.getWrappedObject()).getName();
                tv.setText(name);
                /*if (closed_for_registration) {
                    tv.setTextColor(getResources().getColor(R.color.color_gray));
                    tv.setClickable(false);
                } else {
                    tv.setTextColor(getResources().getColor(R.color.color_black));
                }*/
                tv.setTextColor(getResources().getColor(R.color.color_black));
                //tv.setBackgroundColor(colors[level]);
                CustomTag customTag = new CustomTag(jsonObject, closed_for_registration);
                //tv.setTag(jsonObject);
                tv.setTag(customTag);
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) tv.getLayoutParams();
                mlp.setMargins(level * 50, 5, 5, 5);
                //System.out.println("is expandable: "+item.isExpanded());
                if (isLast) {
                    iv.setVisibility(View.GONE);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                CustomTag _customTag = (CustomTag) v.getTag();
                                JSONObject _jsonObj_ = _customTag.jsonObject;
                                boolean _closedForRegistration = _customTag.closedForRegistration;
                                if (type.equals("placePlatform")) {
                                    //JSONObject _jsonObj_ = (JSONObject) v.getTag();
                                    if (_closedForRegistration) {
                                        return;
                                    } else {
                                        groupArray = _jsonObj_;
                                        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PlacePlatformName");
                                        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PlacePlatformID");
                                        spManager.removeValueByKeyNameFromCOPCardDetailsPref("DepartmentName");
                                        spManager.removeValueByKeyNameFromCOPCardDetailsPref("DepartmentID");
                                        spManager.removeValueByKeyNameFromCOPCardDetailsPref("GroupObject");
                                        editor.putString("PlacePlatformName", Title);
                                        editor.putString("PlacePlatformID", id);
                                        editor.putString("GroupObject", groupArray.toString());
                                        txt_place.setText(Title);
                                        txt_place.setTextColor(getResources().getColor(R.color.color_black));
                                        txt_department.setText(getResources().getString(R.string.department));
                                        JSONArray jsonArray = groupArray.getJSONArray("groups");
                                        if (jsonArray != null && jsonArray.length() > 0) {
                                            selectedFacilityGroup = groupArray.toString();
                                        } else {
                                            selectedFacilityGroup = "";
                                        }
                                    }
                                } else if (type.equals("department")) {
                                    if (_closedForRegistration) {
                                        return;
                                    }
                                    txt_department.setText(Title);
                                    txt_department.setTextColor(getResources().getColor(R.color.color_black));
                                    editor.putString("DepartmentName", Title);
                                    editor.putString("DepartmentID", id);
                                    selectedFacilityGroup = "";
                                }
                                editor.commit();

                                if (placePlatformListDialog.isShowing()) {
                                    placePlatformListDialog.cancel();
                                }
                            } catch (Exception ex) {
                                Log.d("Error", ex.getMessage());
                            }
                        }
                    });
                } else {
                    iv.setVisibility(View.VISIBLE);
                }
                return view;
            }
        });
        return superChild;
    }

    public void openCompanyListDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterPSI.this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.cop_place_platform_popup, null, false);
        listView = (ListView) view.findViewById(R.id.listView1);
        list = new ArrayList<NLevelItem>();
        final LayoutInflater inflaterInner = LayoutInflater.from(this);
        if (type.equals("placePlatform")) {
            String spResponse = spManager.getCOPFacilityResponse();
            nestedLoop(spResponse, null, inflaterInner, 0, type);
        } else if (type.equals("department")) {
            if (!spManager.getCOPGroupObject().equals("") || spManager.getCOPGroupObject() != null) {
                String _val = spManager.getCOPGroupObject();
                //String _val = groupArray.toString();
                _val = "[" + _val + "]";
                nestedLoop(_val, null, inflaterInner, 0, type);
            } else {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.not_assocaiated_dept), false, null);
            }
        }

        NLevelAdapter adapter = new NLevelAdapter(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                ((NLevelAdapter) listView.getAdapter()).toggle(arg2);
                ((NLevelAdapter) listView.getAdapter()).getFilter().filter();
            }
        });
        if (list.size() > 0) {
            builder.setView(view);
            placePlatformListDialog = builder.create();
            placePlatformListDialog.show();
            placePlatformListDialog.setCancelable(true);
        } else {
            if (type.equals("department")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.not_assocaiated_dept), false, null);
            } else if (type.equals("placePlatform")) {
                AlertDialogManager.showDialog(RegisterPSI.this, "", getResources().getString(R.string.no_place_platform_associated), false, null);
            }
        }
        dismissWaitDialog();
        setDefault();
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(RegisterPSI.this);
        }
        pDialog.setMessage(getString(R.string.please_wait));
        pDialog.setCancelable(false);
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public void removePreferenceFieldsData() {
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("RegdDate");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PlacePlatformName");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PlacePlatformID");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("DepartmentName");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("GroupObject");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("DepartmentID");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("TopicDiscussed");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("RiskIdentified");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PlannedFollowUp");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("HeatColdStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PressureStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("ChemicalStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("ElectricalStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("GravityStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("RadiationStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("NoiseStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("BiologicalStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("EnergyMovementStatus");
        spManager.removeValueByKeyNameFromCOPCardDetailsPref("PresentMomemtStatus");
    }
}
