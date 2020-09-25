package com.elearn.trainor.CourseModule;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.BaseAdapters.CustomSpinnerAdapter;
import com.elearn.trainor.BaseAdapters.GetMoreCourseRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.GetMoreCoursesProperty;
import com.elearn.trainor.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GetMoreCourses extends AppCompatActivity implements View.OnClickListener {
    RelativeLayout tbl_actionbar;
    TextView text_header, no_courses, select_language, home, text_below_search;
    ConnectionDetector connectionDetector;
    RecyclerView courses_recyclerView;
    LinearLayout llhome, ll_back;
    ProgressDialog pDialog;
    Spinner languageSpinner;
    List<GetMoreCoursesProperty> spinnerFilteredList;
    GetMoreCourseRecyclerViewAdapter getMoreCourseRecyclerViewAdapter;
    EditText edit_text_search;
    ImageView clear_img;
    Button btn_search;
    public static String textTobeSearched, selectedLanguage = "", searchText, filteredLang, backLanguage;
    String[] Languages = {"English", "Norwegian", "Swedish", "Polish", "Korean", "Portuguese"};
    int[] images = {R.drawable.us_flag, R.drawable.norway_flag, R.drawable.swedish_flag, R.drawable.polish_flag, R.drawable.korean_flag, R.drawable.portugal_flag};
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerSelect dbSelect;
    SharedPreferenceManager spManager;
    boolean courseDataExists = false;
    SwipeRefreshLayout swipelayout;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_more_courses);
        getControls();
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        dbDelete = new DataBaseHandlerDelete(GetMoreCourses.this);
        dbInsert = new DataBaseHandlerInsert(GetMoreCourses.this);
        dbSelect = new DataBaseHandlerSelect(GetMoreCourses.this);
        connectionDetector = new ConnectionDetector(GetMoreCourses.this);
        spManager = new SharedPreferenceManager(GetMoreCourses.this);
        languageSpinner = (Spinner) findViewById(R.id.spinner1);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(GetMoreCourses.this, images, Languages);
        languageSpinner.setAdapter(adapter);
        edit_text_search = (EditText) findViewById(R.id.edit_text_search);
        searchText = getIntent().getStringExtra("SearchText");
        backLanguage = getIntent().getStringExtra("backLanguage");
        if (searchText != null) {
            edit_text_search.setText(searchText);
        } else {
            searchText = "";
        }
        courseDataExists = dbSelect.isCourseInfoTableExist();
        spinnerFilteredList = new ArrayList<>();
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        select_language = (TextView) findViewById(R.id.select_language);
        tbl_actionbar.setBackgroundColor(Color.parseColor("#4caf50"));
        courses_recyclerView = (RecyclerView) findViewById(R.id.courses_recyclerView);
        courses_recyclerView.setNestedScrollingEnabled(true);
        text_header = (TextView) findViewById(R.id.text_header);
        btn_search = (Button) findViewById(R.id.btn_search);
        clear_img = (ImageView) findViewById(R.id.clear_img);
        text_below_search = (TextView) findViewById(R.id.text_below_search);
        text_header.setText(getResources().getString(R.string.courses));
        no_courses = (TextView) findViewById(R.id.no_courses);
        home = (TextView) findViewById(R.id.home);
        no_courses.setVisibility(View.GONE);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        clear_img.setOnClickListener(this);

        edit_text_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && (charSequence.toString().equals("") || charSequence.toString().equals(" "))) {
                    clear_img.setVisibility(View.GONE);
                    spinnerFilteredList.clear();
                    spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' Order By CD.title asc", "FilteredData");
                    courses_recyclerView.setLayoutManager(new LinearLayoutManager(GetMoreCourses.this));
                    getMoreCourseRecyclerViewAdapter = new GetMoreCourseRecyclerViewAdapter(GetMoreCourses.this, spinnerFilteredList, false);
                    courses_recyclerView.setAdapter(getMoreCourseRecyclerViewAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    clear_img.setVisibility(View.VISIBLE);
                    textTobeSearched = edit_text_search.getText().toString();
                } else {
                    no_courses.setVisibility(View.GONE);
                    courses_recyclerView.setVisibility(View.VISIBLE);
                    textTobeSearched = "";
                    searchText = "";
                    spinnerFilteredList.clear();
                    spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' Order By CD.title asc", "FilteredData");
                    courses_recyclerView.setLayoutManager(new LinearLayoutManager(GetMoreCourses.this));
                    getMoreCourseRecyclerViewAdapter = new GetMoreCourseRecyclerViewAdapter(GetMoreCourses.this, spinnerFilteredList, false);
                    courses_recyclerView.setAdapter(getMoreCourseRecyclerViewAdapter);
                }
            }
        });

        if (spManager.getLanguage().startsWith("en") || (backLanguage != null && backLanguage.equals("English"))) {
            languageSpinner.setSelection(0);
        } else if (spManager.getLanguage().startsWith("nb") || (backLanguage != null && backLanguage.equals("Norwegian"))) {
            languageSpinner.setSelection(1);
        } else if (spManager.getLanguage().startsWith("pl") || (backLanguage != null && backLanguage.equals("Polish"))) {
            languageSpinner.setSelection(3);
        } else if (spManager.getLanguage().startsWith("ko") || (backLanguage != null && backLanguage.equals("Korean"))) {
            languageSpinner.setSelection(4);
        } else if (spManager.getLanguage().startsWith("sv") || (backLanguage != null && backLanguage.equals("Swedish"))) {
            languageSpinner.setSelection(2);
        } else if (spManager.getLanguage().startsWith("pt") || (backLanguage != null && backLanguage.equals("Portuguese"))) {
            languageSpinner.setSelection(5);
        }

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CustomSpinnerAdapter customerAdapter = (CustomSpinnerAdapter) parent.getAdapter();
                selectedLanguage = customerAdapter.countryNames[position];
                spinnerFilteredList.clear();
                if (selectedLanguage.equals("English")) {
                    spinnerLanguageBasedText("English");
                    filteredLang = "en_US";
                    if (searchText != null && !searchText.equals("")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    } else {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"en_US\" Order By CD.title asc", "GetMoreCourse");
                    }
                } else if (selectedLanguage.equals("Norwegian")) {
                    spinnerLanguageBasedText("Norwegian");
                    filteredLang = "nb_NO";
                    if (searchText != null && !searchText.equals("")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    } else {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"nb_NO\" Order By CD.title asc", "GetMoreCourse");
                    }
                } else if (selectedLanguage.equals("Swedish")) {
                    spinnerLanguageBasedText("Swedish");
                    filteredLang = "sv_SE";
                    if (searchText != null && !searchText.equals("")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    } else {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"sv_SE\" Order By CD.title asc", "GetMoreCourse");
                    }
                } else if (selectedLanguage.equals("Polish")) {
                    spinnerLanguageBasedText("Polish");
                    filteredLang = "pl_PL";
                    if (searchText != null && !searchText.equals("")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    } else {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"pl_PL\" Order By CD.title asc", "GetMoreCourse");
                    }
                } else if (selectedLanguage.equals("Korean")) {
                    spinnerLanguageBasedText("Korean");
                    filteredLang = "ko_KR";
                    if (searchText != null && !searchText.equals("")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    } else {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"ko_KR\" Order By CD.title asc", "GetMoreCourse");
                    }
                } else if (selectedLanguage.equals("Portuguese")) {
                    spinnerLanguageBasedText("Portuguese");
                    filteredLang = "pt_BR";
                    if (searchText != null && !searchText.equals("")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    } else {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"pt_BR\" Order By CD.title asc", "GetMoreCourse");
                    }
                }

                if (spinnerFilteredList.size() > 0) {
                    courses_recyclerView.setVisibility(View.VISIBLE);
                    no_courses.setVisibility(View.GONE);
                    courses_recyclerView.setLayoutManager(new LinearLayoutManager(GetMoreCourses.this));
                    getMoreCourseRecyclerViewAdapter = new GetMoreCourseRecyclerViewAdapter(GetMoreCourses.this, spinnerFilteredList, false);
                    getMoreCourseRecyclerViewAdapter.notifyDataSetChanged();
                    courses_recyclerView.setAdapter(getMoreCourseRecyclerViewAdapter);
                } else {
                    courses_recyclerView.setVisibility(View.GONE);
                    if (courseDataExists == false) {
                        no_courses.setVisibility(View.GONE);
                    } else {
                        no_courses.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (backLanguage != null) {
            spinnerFilteredList.clear();
            if (backLanguage.equals("English")) {
                languageSpinner.setSelection(0);
                filteredLang = "en_US";
                spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"en_US\" Order By CD.title asc", "GetMoreCourse");
            } else if (backLanguage.equals("Norwegian")) {
                languageSpinner.setSelection(1);
                filteredLang = "nb_NO";
                spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"nb_NO\" Order By CD.title asc", "GetMoreCourse");
            } else if (backLanguage.equals("Swedish")) {
                languageSpinner.setSelection(2);
                filteredLang = "sv_SE";
                spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"sv_SE\" Order By CD.title asc", "GetMoreCourse");
            } else if (backLanguage.equals("Polish")) {
                languageSpinner.setSelection(3);
                filteredLang = "pl_PL";
                spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"pl_PL\" Order By CD.title asc", "GetMoreCourse");
            } else if (backLanguage.equals("Korean")) {
                languageSpinner.setSelection(4);
                filteredLang = "ko_KR";
                spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"ko_KR\" Order By CD.title asc", "GetMoreCourse");
            } else if (backLanguage.equals("Portuguese")) {
                languageSpinner.setSelection(5);
                filteredLang = "pt_BR";
                spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"pt_BR\" Order By CD.title asc", "GetMoreCourse");
            }
            if (spinnerFilteredList.size() > 0) {
                courses_recyclerView.setVisibility(View.VISIBLE);
                no_courses.setVisibility(View.GONE);
                courses_recyclerView.setLayoutManager(new LinearLayoutManager(GetMoreCourses.this));
                if (searchText != null && !searchText.equals("")) {
                    spinnerFilteredList.clear();
                    spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType='" + filteredLang + "' and CD.title LIKE '%" + searchText + "%' Order By CD.title asc", "FilteredData");
                    getMoreCourseRecyclerViewAdapter = new GetMoreCourseRecyclerViewAdapter(GetMoreCourses.this, spinnerFilteredList, false);
                    courses_recyclerView.setAdapter(getMoreCourseRecyclerViewAdapter);
                } else {
                    getMoreCourseRecyclerViewAdapter = new GetMoreCourseRecyclerViewAdapter(GetMoreCourses.this, spinnerFilteredList, false);
                    courses_recyclerView.setAdapter(getMoreCourseRecyclerViewAdapter);
                }
            } else {
                courses_recyclerView.setVisibility(View.GONE);
                no_courses.setVisibility(View.VISIBLE);
            }
        } else {
            getMoreCoursesList();
        }

        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    getMoreCoursesList();
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(GetMoreCourses.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(GetMoreCourses.this, "CourseList", this.getClass().getSimpleName());
    }

    @Override
    public void onBackPressed() {
        goToActivity(Courses.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llhome:
                goToActivity(HomePage.class);
                break;
            case R.id.ll_back:
                onBackPressed();
                break;

            case R.id.btn_search:
                hideKeyBoard();
                textTobeSearched = edit_text_search.getText().toString();
                if (!textTobeSearched.isEmpty() && textTobeSearched != null) {
                    searchText = textTobeSearched;
                    getMoreCourseRecyclerViewAdapter.getFilter().filter(textTobeSearched);
                }
                break;
            case R.id.clear_img:
                searchText = "";
                clear_img.setVisibility(View.GONE);
                edit_text_search.setText("");
                hideKeyBoard();
                edit_text_search.clearFocus();
                break;
        }
    }

    public void getMoreCoursesList() {
        showWaitDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.GetMoreCoursesURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                swipelayout.setRefreshing(false);
                showWaitDialog();
                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        //finalDataList.clear();
                        if (jsonArray != null && jsonArray.length() > 0) {
                            dbDelete.deleteTableByName("CourseDetail", "");
                            dbDelete.deleteTableByName("CoursePurchaseInfo", "");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                GetMoreCoursesProperty getMoreCoursesProperty = new GetMoreCoursesProperty();
                                getMoreCoursesProperty.course_id = jsonObject.getString("course_id").equals("null") ? "" : jsonObject.getString("course_id") == null ? "" : jsonObject.getString("course_id");
                                getMoreCoursesProperty.uuid = jsonObject.getString("uuid").equals("null") ? "" : jsonObject.getString("uuid") == null ? "" : jsonObject.getString("uuid");
                                getMoreCoursesProperty.language = jsonObject.getString("language").equals("null") ? "" : jsonObject.getString("language") == null ? "" : jsonObject.getString("language");
                                getMoreCoursesProperty.internal_name = jsonObject.getString("internal_name").equals("null") ? "" : jsonObject.getString("internal_name") == null ? "" : jsonObject.getString("internal_name");
                                getMoreCoursesProperty.length = jsonObject.getString("length").equals("null") ? "" : jsonObject.getString("length") == null ? "" : jsonObject.getString("length");
                                getMoreCoursesProperty.price = jsonObject.getString("price").equals("null") ? "" : jsonObject.getString("price") == null ? "" : jsonObject.getString("price");
                                getMoreCoursesProperty.price_inc_vat = jsonObject.getString("price_inc_vat").equals("null") ? "" : jsonObject.getString("price_inc_vat") == null ? "" : jsonObject.getString("price_inc_vat");

                                if (!getMoreCoursesProperty.length.equals("")) {
                                    String[] tokens = getMoreCoursesProperty.length.split(";");
                                    String firstToken = tokens[0];
                                    String secondToken = tokens[1];
                                    String thirdToken = tokens[2];
                                    int days_int = Integer.parseInt(firstToken);
                                    int hours_int = Integer.parseInt(secondToken);
                                    int minutes_int = Integer.parseInt(thirdToken);
                                    String showDays = days_int > 0 ? (days_int == 1 ? "day" : "days") : "";
                                    String showHours = hours_int > 0 ? (hours_int == 1 ? "hour" : "hours") : "";
                                    String showMinutes = minutes_int > 0 ? (minutes_int == 1 ? "minute" : "minutes") : "";
                                    if (!showDays.equals("")) {
                                        getMoreCoursesProperty.length = firstToken + " " + showDays;
                                    } else {
                                        if (!showHours.equals("")) {
                                            getMoreCoursesProperty.length = secondToken + " " + showHours;
                                        }
                                        if (!showMinutes.equals("")) {
                                            if (getMoreCoursesProperty.length != null && !showHours.equals("")) {
                                                getMoreCoursesProperty.length += " " + thirdToken + " " + showMinutes;
                                            } else {
                                                getMoreCoursesProperty.length = thirdToken + " " + showMinutes;
                                            }
                                        }
                                    }
                                }

                                if (jsonObject.has("info") && jsonObject.getString("info") != null) {
                                    JSONObject jsonObjectinfo = jsonObject.getJSONObject("info");
                                    JSONObject jsonObjectInfo = jsonObjectinfo.getJSONObject(getMoreCoursesProperty.language);
                                    GetMoreCoursesProperty infoLangProperty = new GetMoreCoursesProperty();
                                    infoLangProperty.uuid = getMoreCoursesProperty.uuid;
                                    infoLangProperty.course_id = getMoreCoursesProperty.course_id;
                                    infoLangProperty.language = getMoreCoursesProperty.language;
                                    if (jsonObjectInfo.has("title")) {
                                        infoLangProperty.title = jsonObjectInfo.getString("title") == null ? "" : jsonObjectInfo.getString("title").equals("null") ? "" : jsonObjectInfo.getString("title");
                                    }
                                    if (jsonObjectInfo.has("intro")) {
                                        infoLangProperty.intro = jsonObjectInfo.getString("intro") == null ? "" : jsonObjectInfo.getString("intro").equals("null") ? "" : jsonObjectInfo.getString("intro");
                                    }
                                    if (jsonObjectInfo.has("goal")) {
                                        infoLangProperty.goal = jsonObjectInfo.getString("goal") == null ? "" : jsonObjectInfo.getString("goal").equals("null") ? "" : jsonObjectInfo.getString("goal");
                                    }
                                    if (jsonObjectInfo.has("target_group")) {
                                        infoLangProperty.target_group = jsonObjectInfo.getString("target_group") == null ? "" : jsonObjectInfo.getString("target_group").equals("null") ? "" : jsonObjectInfo.getString("target_group");
                                    }
                                    if (jsonObjectInfo.has("description")) {
                                        infoLangProperty.description = jsonObjectInfo.getString("description") == null ? "" : jsonObjectInfo.getString("description").equals("null") ? "" : jsonObjectInfo.getString("description");
                                    }
                                    dbInsert.addDataIntoCourseDetailTable(infoLangProperty);
                                }
                                String db_condition = "courseID = '" + getMoreCoursesProperty.course_id + "' AND uuid = '" + getMoreCoursesProperty.uuid + "'";
                                if (dbSelect.getDataFromCoursePurchaseTable(db_condition, "").size() == 0) {
                                    dbInsert.addDataIntoCoursePurchaseTable(getMoreCoursesProperty);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    swipelayout.setRefreshing(false);
                    dismissWaitDialog();
                } finally {
                    spinnerFilteredList.clear();
                    courseDataExists = dbSelect.isCourseInfoTableExist();
                    if (selectedLanguage.equals("English")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"en_US\" Order By CD.title asc", "GetMoreCourse");
                    } else if (selectedLanguage.equals("Norwegian")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"nb_NO\" Order By CD.title asc", "GetMoreCourse");
                    } else if (selectedLanguage.equals("Swedish")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"sv_SE\" Order By CD.title asc", "GetMoreCourse");
                    } else if (selectedLanguage.equals("Polish")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"pl_PL\" Order By CD.title asc", "GetMoreCourse");
                    } else if (selectedLanguage.equals("Korean")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"ko_KR\" Order By CD.title asc", "GetMoreCourse");
                    } else if (selectedLanguage.equals("Portuguese")) {
                        spinnerFilteredList = dbSelect.getDataFromCoursePurchaseTable("CD.languageType=\"pt_BR\" Order By CD.title asc", "GetMoreCourse");
                    }
                    if (spinnerFilteredList.size() > 0) {
                        courses_recyclerView.setVisibility(View.VISIBLE);
                        no_courses.setVisibility(View.GONE);
                        courses_recyclerView.setLayoutManager(new LinearLayoutManager(GetMoreCourses.this));
                        getMoreCourseRecyclerViewAdapter = new GetMoreCourseRecyclerViewAdapter(GetMoreCourses.this, spinnerFilteredList, false);
                        courses_recyclerView.setAdapter(getMoreCourseRecyclerViewAdapter);
                    } else {
                        courses_recyclerView.setVisibility(View.GONE);
                        no_courses.setVisibility(View.VISIBLE);
                    }
                    swipelayout.setRefreshing(false);
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(GetMoreCourses.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(GetMoreCourses.this);
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

    public void setLocalizedTextOnView(TextView txtView, Button btn, EditText edtView, String language, int id, String originalLang) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language); // whatever you want here
        res.updateConfiguration(conf, null); // second arg null means don't change
        String str = res.getString(id);
        if (edtView != null) {
            edtView.setHint(str);
        }
        if (btn != null) {
            btn.setText(str);
        }
        if (txtView != null) {
            txtView.setText(str);
        }
        conf.locale = new Locale(originalLang);
        res.updateConfiguration(conf, null);
    }

    public void spinnerLanguageBasedText(String language) {
        String userLanguage = spManager.getLanguage();
        if (language.startsWith("Eng")) {
            language = "en";
        } else if (language.startsWith("Nor")) {
            language = "nb";
        } else if (language.startsWith("Pol")) {
            language = "pl";
        } else if (language.startsWith("Kor")) {
            language = "ko";
        } else if (language.startsWith("Swe")) {
            language = "sv";
        } else if (language.startsWith("Por")) {
            language = "pt";
        }
        setLocalizedTextOnView(text_header, null, null, language, R.string.courses, userLanguage);
        setLocalizedTextOnView(select_language, null, null, language, R.string.select_language, userLanguage);
        setLocalizedTextOnView(null, btn_search, null, language, R.string.search, userLanguage);
        setLocalizedTextOnView(null, null, edit_text_search, language, R.string.search_hint, userLanguage);
        setLocalizedTextOnView(home, null, null, language, R.string.home, userLanguage);
        setLocalizedTextOnView(no_courses, null, null, language, R.string.no_course_available, userLanguage);
        setLocalizedTextOnView(text_below_search, null, null, language, R.string.below_search_text, userLanguage);
    }

    public void goToActivity(Class activityClass) {
        Intent intentback = new Intent(GetMoreCourses.this, activityClass);
        intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentback);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void hideKeyBoard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(edit_text_search.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage().toString());
        }
    }
}
