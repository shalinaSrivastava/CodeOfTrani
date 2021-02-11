package com.elearn.trainor.CourseModule;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.vending.billing.IInAppBillingService;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.elearn.trainor.BaseAdapters.Buy_companyListRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.NeedSupport;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.GetMoreCoursesProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class
OrderCourses extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener {
    public static OrderCourses instance;
    RelativeLayout tbl_actionbar;
    TextView text_header, course_name, intro, price, course_Name_info, duration, goal_des, target_group_des, description_des;
    Button btn_buy;
    ImageView course_image, norwegian_flag, english_flag, korean_flag, swedish_flag, polish_flag, portguese_flag;
    LinearLayout llhome, ll_back;
    ProgressDialog pDialog;
    AlertDialog courseInfoDialog, afterBuyCoursePopup;
    Buy_companyListRecyclerViewAdapter buy_companyListRecyclerViewAdapter;
    List<CustomerDetailsProperty> myCompanyList;
    List<GetMoreCoursesProperty> courseInfoList;
    private RecyclerView companyRecyclerView;
    DataBaseHandlerSelect dbSelect;
    String CourseID, productUUID, intentLanguage, image_url = "", selectedlanguage_Code, customerID, txttoBeSearched, OrderID = "", courseDescription = "";
    SharedPreferenceManager spManager;
    int responseCode;
    public static int apiCalledCount = 1, newAPICalledCount = 1;
    Boolean callApiOnce = false, newPurchasedAPICalled = true;
    String day_text = "", hour_text = "", minute_text = "", purchaseTime = "", userID = "";
    private BillingClient mBillingClient;
    IInAppBillingService inAppBillingService;
    ServiceConnection serviceConnection;
    SwipeRefreshLayout swipelayout;
    //private static final String BASE_64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmghDuV2q+NSChzLjEkWXKeOTfASf9Ii3lesddtADdGmtrSamFiyVcbWuH1Ri3cOZ6EMOHi+HlqJShNcUJQ7C5mFsDZsLu3PDQJyYz86vuGdrPagQerZOMQnO28Y9lMzcR8dycr/tHupTULWL4Hduyh3EFKkW1VKO6He26ICbRnlTklSMJ7T1GGR+vzsrOiqZbEK0bWvoEaKNAxi5Vz8Z3RRbUr/uviNiw2kFGEttXbZnBreQndsYit/oxTITKsuYJP6BDU2h8xCb2mUd1VXOKRDuu2DxU5CyJ3WaiEb1VaAN14XYmk3kxvl8kzh+CwJKCpAn4aPWDWP1zIfoyFhgdQIDAQAB"; // for tarinor testing
    private static final String BASE_64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwK7dJUxHplcaAPFpG3Ms1mqCCp0uaM+2UmWK88mJdsrRhtBAjsXsmhcipu92XXoEarXiPr35tJeHMTP1JxJ/JDZQ5/oUSY39Xb5sXxnZPiH86GcmFHGJyVvyuuN23TD31xJ2OxO+No.+F8mBCffP/YqU4av3LUzYL+yap6RTK9w3arDZuIrDrTcW3Sxh0FfkbruF2yLZhKMHpQrk2kg/8PaSY9Rf54o8icDeuaYEpfvKSmHkrB3WG3PG5ANrOnhGQnJs7Iqa9UpO1yIS7KJZeMZCR2o1gLQjUixUil+1YArI0tbDr9PectMPmtwlAtQ06Jtthnl7yRuQRtn1jmMw76wIDAQAB";
    JSONObject jsonObject;
    ConnectionDetector connectionDetector;
    Context context;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productUUID = getIntent().getStringExtra("ProductUUID");
        CourseID = getIntent().getStringExtra("CourseID");
        txttoBeSearched = getIntent().getStringExtra("SearchText");
        intentLanguage = getIntent().getStringExtra("selectedlanguage");
        if (intentLanguage.equals("English")) {
            selectedlanguage_Code = "en_US";
            setLocale("en");
        } else if (intentLanguage.equals("Norwegian")) {
            selectedlanguage_Code = "nb_NO";
            setLocale("nb");
        } else if (intentLanguage.equals("Swedish")) {
            selectedlanguage_Code = "sv_SE";
            setLocale("sv");
        } else if (intentLanguage.equals("Polish")) {
            selectedlanguage_Code = "pl_PL";
            setLocale("pl");
        } else if (intentLanguage.equals("Korean")) {
            selectedlanguage_Code = "ko_KR";
            setLocale("ko");
        } else if (intentLanguage.equals("Portuguese")) {
            selectedlanguage_Code = "pt_BR";
            setLocale("pt");
        }
        setContentView(R.layout.activity_order_courses);
        getControls();
        instance = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //analytics.setCurrentScreen(OrderCourses.this, "BuyCourse", this.getClass().getSimpleName());
    }

    public static OrderCourses getInstance() {
        if (instance == null) {
            instance = new OrderCourses();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        connectionDetector = new ConnectionDetector(OrderCourses.this);
        spManager = new SharedPreferenceManager(OrderCourses.this);
        dbSelect = new DataBaseHandlerSelect(OrderCourses.this);
        userID = spManager.getUserID();

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                inAppBillingService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                inAppBillingService = IInAppBillingService.Stub.asInterface(service);
            }
        };
        bindService(serviceConnection);

        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        text_header = (TextView) findViewById(R.id.text_header);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        btn_buy = (Button) findViewById(R.id.btn_buy);
        goal_des = (TextView) findViewById(R.id.goal_des);
        target_group_des = (TextView) findViewById(R.id.target_group_des);
        description_des = (TextView) findViewById(R.id.description_des);
        intro = (TextView) findViewById(R.id.intro);
        course_name = (TextView) findViewById(R.id.course_name);
        course_Name_info = (TextView) findViewById(R.id.course_Name_info);
        price = (TextView) findViewById(R.id.price);
        duration = (TextView) findViewById(R.id.duration);
        course_image = (ImageView) findViewById(R.id.course_image);
        norwegian_flag = (ImageView) findViewById(R.id.norwegian_flag);
        english_flag = (ImageView) findViewById(R.id.english_flag);
        korean_flag = (ImageView) findViewById(R.id.korean_flag);
        swedish_flag = (ImageView) findViewById(R.id.swedish_flag);
        polish_flag = (ImageView) findViewById(R.id.polish_flag);
        portguese_flag = (ImageView) findViewById(R.id.portguese_flag);
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        tbl_actionbar.setBackgroundColor(Color.parseColor("#4caf50"));
        text_header.setText(getResources().getString(R.string.order_courses));
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
        myCompanyList = new ArrayList<>();
        myCompanyList.clear();
        myCompanyList = dbSelect.getCustomerIdFromCustomerDetails("");
        String condition = "CD.uuid = '" + productUUID + "' AND CD.languageType = '" + selectedlanguage_Code + "'";
        courseInfoList = dbSelect.getDataFromCoursePurchaseTable(condition, "OrderPage");

        if (courseInfoList.size() > 0) {
            GetMoreCoursesProperty courseDetail = courseInfoList.get(0);
            goal_des.setText(courseDetail.goal.replaceAll("<br>", "\n"));
            String targetGroupContent = courseDetail.target_group.replaceAll("<br>", "\n");
            target_group_des.setText(targetGroupContent);
            //description_des.setText(courseDetail.description.replaceAll("<br>", "\n"));
            if (courseDetail.description != null && !courseDetail.description.equals("")) {
                courseDescription = courseDetail.description.replaceAll("<br>", "\n");
                String modifiedUnicodeCharater = courseDescription.replaceAll("[*]", "\u00B7");
                description_des.setText(modifiedUnicodeCharater);
            }
            intro.setText(courseDetail.intro);
            course_name.setText(courseDetail.title);
            course_Name_info.setText(" " + courseDetail.title);
            price.setText("NOK " + courseDetail.price_inc_vat + ",-");

            if (!courseDetail.length.equals("")) {
                List<String> durationTextList = Arrays.asList(courseDetail.length.split(" "));
                if (durationTextList.size() > 0) {
                    for (int i = 0; i < durationTextList.size(); i++) {
                        if (durationTextList.get(i).equals("day") || durationTextList.get(i).equals("days")) {
                            String day = durationTextList.get(i).equals("day") ? getResources().getString(R.string.day) : getResources().getString(R.string.days);
                            day_text = durationTextList.get(i - 1) + " " + day;
                            courseDetail.length = day_text;
                        } else if (durationTextList.get(i).equals("hour") || durationTextList.get(i).equals("hours")) {
                            String hour = durationTextList.get(i).equals("hour") ? getResources().getString(R.string.hour) : getResources().getString(R.string.hours);
                            hour_text = durationTextList.get(i - 1) + " " + hour + " ";
                            courseDetail.length = hour_text;
                        } else if (durationTextList.get(i).equals("minute") || durationTextList.get(i).equals("minutes")) {
                            String minute = durationTextList.get(i).equals("minute") ? getResources().getString(R.string.minute) : getResources().getString(R.string.minutes);
                            minute_text = durationTextList.get(i - 1) + " " + minute;
                            if (!hour_text.equals("")) {
                                hour_text += minute_text;
                                courseDetail.length = hour_text;
                            } else {
                                courseDetail.length = minute_text;
                            }
                        }
                    }
                }
                duration.setText(" " + courseDetail.length);
            }
            course_image.setImageResource(R.drawable.elarning_course);
            getCourseImageURL(productUUID);
        }

        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    swipelayout.setRefreshing(false);
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(OrderCourses.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });

        if (intentLanguage.equals("English")) {
            english_flag.setVisibility(View.VISIBLE);
        } else if (intentLanguage.equals("Norwegian")) {
            norwegian_flag.setVisibility(View.VISIBLE);
        } else if (intentLanguage.equals("Swedish")) {
            swedish_flag.setVisibility(View.VISIBLE);
        } else if (intentLanguage.equals("Polish")) {
            polish_flag.setVisibility(View.VISIBLE);
        } else if (intentLanguage.equals("Korean")) {
            korean_flag.setVisibility(View.VISIBLE);
        } else if (intentLanguage.equals("Portuguese")) {
            portguese_flag.setVisibility(View.VISIBLE);
        }
    }

    public void bindService(ServiceConnection serviceConnection) {
        try {
            bindService(getBindServiceIntent(), serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //if (!serviceConnection.equals(null)) {
        try {
            if (serviceConnection != null) {
                unbindService(serviceConnection);
                inAppBillingService = null;
            }
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (serviceConnection != null) {
                unbindService(serviceConnection);
                inAppBillingService = null;
            }
        } catch (Exception ex) {

        }
    }

    private static Intent getBindServiceIntent() {
        Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        intent.setPackage("com.android.vending");
        return intent;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llhome:
                Intent intent = new Intent(OrderCourses.this, HomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                break;

            case R.id.ll_back:
                onBackPressed();
                break;

            case R.id.btn_buy:
                popupAfterBuyCourseListDialog();
                break;
        }
    }

    public void addCourseToCoursePage(final String token, final String customer_ID, final String uuid, final String txnID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.OrderCourse_PostApi, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (newPurchasedAPICalled) {
                    newPurchasedAPI(userID, txnID, CourseID, token, customerID, "success");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (connectionDetector.isConnectingToInternet()) {
                    if (apiCalledCount >= 4) {
                        if (newPurchasedAPICalled) {
                            newPurchasedAPI(userID, txnID, CourseID, token, customerID, "failed");
                        }
                    } else {
                        apiCalledCount += 1;
                        addCourseToCoursePage(token, customer_ID, uuid, txnID);
                    }
                } else {
                    goToNeedSupport(OrderID, userID, CourseID);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String strParameters = "{\"userId\":\"" + userID + "\",\"courseId\":\"" + uuid + "\",\"customerId\":\"" + customer_ID + "\"}";
                return strParameters.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(OrderCourses.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void openCompanyListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderCourses.this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_cmpny_list_popup, null, false);
        companyRecyclerView = (RecyclerView) view.findViewById(R.id.popup_company_list_recycler_view);
        companyRecyclerView.setLayoutManager(new LinearLayoutManager(OrderCourses.this));
        companyRecyclerView.setNestedScrollingEnabled(true);
        buy_companyListRecyclerViewAdapter = new Buy_companyListRecyclerViewAdapter(OrderCourses.this, myCompanyList);
        companyRecyclerView.setAdapter(buy_companyListRecyclerViewAdapter);
        ImageView imgBtnCloseDialog = (ImageView) view.findViewById(R.id.imgBtnCloseDialog);
        imgBtnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseInfoDialog != null && courseInfoDialog.isShowing()) {
                    courseInfoDialog.dismiss();
                }
            }
        });
        builder.setView(view);
        courseInfoDialog = builder.create();
        courseInfoDialog.show();
    }

    public void popupAfterBuyCourseListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderCourses.this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_popup_aftr_buy_course, null, false);
        Button btn_i_understand = view.findViewById(R.id.btn_i_understand);
        LinearLayout ll_close = view.findViewById(R.id.ll_close);
        btn_i_understand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (afterBuyCoursePopup != null && afterBuyCoursePopup.isShowing()) {
                    afterBuyCoursePopup.dismiss();
                }
                if (myCompanyList.size() > 1) {
                    openCompanyListDialog();
                } else if (myCompanyList.size() == 1) {
                    if (inAppBillingService != null) {
                        customerID = myCompanyList.get(0).customer_id;
                        try {
                            int result = inAppBillingService.isBillingSupported(3, getPackageName(), "inapp");
                            if (result == 0) {
                                paymentMethod(customerID);
                            } else {
                                //Start event Analytics
                                Bundle bundle = new Bundle();
                                bundle.putString("CourseTxnDeclined", "Yes");
                                analytics.logEvent("CourseTxnDeclined", bundle);
                                //End event Analytics
                                Toast.makeText(OrderCourses.this, "Billing not supported", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Log.d("Error", ex.getMessage());
                        }
                    }
                }
            }
        });
        ll_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (afterBuyCoursePopup != null && afterBuyCoursePopup.isShowing()) {
                    afterBuyCoursePopup.dismiss();
                }
            }
        });
        builder.setView(view);
        afterBuyCoursePopup = builder.create();
        afterBuyCoursePopup.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OrderCourses.this, GetMoreCourses.class);
        intent.putExtra("backLanguage", intentLanguage);
        intent.putExtra("SearchText", txttoBeSearched);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(OrderCourses.this);
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

    public void getCourseImageURL(final String courseID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Course_image_URL + courseID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    image_url = jsonObject1.getString("image") == null ? "" : (jsonObject1.getString("image").equals("null") ? "" : jsonObject1.getString("image"));
                    if (!image_url.equals("")) {
                        Glide.with(OrderCourses.this).load(image_url).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                course_image.setImageResource(R.drawable.elarning_course);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        }).into(course_image);
                    }
                } catch (Exception ex) {
                    course_image.setImageResource(R.drawable.elarning_course);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                course_image.setImageResource(R.drawable.elarning_course);
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(OrderCourses.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == 0) {
            btn_buy.setClickable(false);
            btn_buy.setAlpha((0.5f));
        } else {
            //Start event Analytics
            Bundle bundle = new Bundle();
            bundle.putString("CourseTxnDeclined", "Yes");
            analytics.logEvent("CourseTxnDeclined", bundle);
            //End event Analytics
        }
        if (responseCode == 7) {
            AlertDialogManager.showDialog(OrderCourses.this, "", "Product is not available in the current storefront", false, null);
        }
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                try {
                    if (inAppBillingService != null) {
                        //Start Download Course Analytics
                        Bundle bundle = new Bundle();
                        bundle.putString("CoursePurchase", "Yes");
                        analytics.logEvent("CoursePurchase", bundle);
                        //End Download Course Analytics

                        jsonObject = new JSONObject(purchase.getOriginalJson());
                        String token = jsonObject.getString("purchaseToken");
                        OrderID = jsonObject.getString("orderId");
                        Long purchseDate = jsonObject.getLong("purchaseTime");
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(purchseDate);
                        purchaseTime = DateFormat.format("yyyy-MM-dd hh:mm:ss", cal).toString();   //changed time format
                        showWaitDialog();
                        int result = inAppBillingService.consumePurchase(3, "com.elearn.trainor", token);
                        if (callApiOnce && responseCode == 0) {
                            callApiOnce = false;
                            addCourseToCoursePage(token, customerID, productUUID, OrderID);
                        }
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                    Log.d("Error", ex.getMessage());
                }
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            Log.d("Error", " ");
            //Start Download Course Analytics
            Bundle bundle = new Bundle();
            bundle.putString("CoursePurchaseCancelled", "Yes");
            analytics.logEvent("CoursePurchase", bundle);
            //End Download Course Analytics
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            Log.d("Error", " ");
            // Handle any other error codes.
        }
    }

    public void paymentMethod(final String customer_ID) {
        if (courseInfoDialog != null && courseInfoDialog.isShowing()) {
            courseInfoDialog.dismiss();
        }
        if (afterBuyCoursePopup != null && afterBuyCoursePopup.isShowing()) {
            afterBuyCoursePopup.dismiss();
        }
        final String PRODUCT_ID = CourseID;
        customerID = customer_ID;
        mBillingClient = BillingClient.newBuilder(OrderCourses.this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    final BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSku(PRODUCT_ID)
                            .setType(BillingClient.SkuType.INAPP) // SkuType.SUBS for subscription
                            .build();
                    responseCode = mBillingClient.launchBillingFlow(OrderCourses.this, flowParams);
                    if (responseCode == 0) {
                        callApiOnce = true;
                    } else {
                        callApiOnce = false;
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("Result", "connection lost");
            }
        });
    }

    public void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        this.getApplicationContext().getResources().updateConfiguration(config, null);
    }

    public void goToNeedSupport(String orderID, String userID, String courseID) {
        dismissWaitDialog();
        Intent intent = new Intent(OrderCourses.this, NeedSupport.class);
        intent.putExtra("From", "OrderCourse");
        intent.putExtra("UserID", userID);
        intent.putExtra("CourseID", courseID);
        intent.putExtra("OrderID", orderID);
        intent.putExtra("TimePurchase", purchaseTime);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void newPurchasedAPI(final String userID, final String txnID, final String productID, final String purchaseToken, final String companyID, final String status) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.NewCoursePurchasedAPI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (status.equals("failed")) {
                    goToNeedSupport(OrderID, userID, CourseID);
                } else {
                    try {
                        newPurchasedAPICalled = false;
                        dismissWaitDialog();
                        Intent intent = new Intent(OrderCourses.this, Courses.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    } catch (Exception ex) {
                        Log.d("", ex.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (connectionDetector.isConnectingToInternet()) {
                    if (newAPICalledCount >= 4) {
                        newPurchasedAPICalled = false;
                        if (status.equals("failed")) {
                            goToNeedSupport(OrderID, userID, CourseID);
                        } else {
                            try {
                                newPurchasedAPICalled = false;
                                dismissWaitDialog();
                                Intent intent = new Intent(OrderCourses.this, Courses.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            } catch (Exception ex) {
                                Log.d("", ex.getMessage());
                            }
                        }
                    } else {
                        newAPICalledCount += 1;
                        newPurchasedAPI(userID, txnID, productID, purchaseToken, companyID, status);
                    }
                } else {
                    newPurchasedAPICalled = false;
                    if (status.equals("failed")) {
                        goToNeedSupport(OrderID, userID, CourseID);
                    } else {
                        try {
                            newPurchasedAPICalled = false;
                            dismissWaitDialog();
                            Intent intent = new Intent(OrderCourses.this, Courses.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                        } catch (Exception ex) {
                            Log.d("", ex.getMessage());
                        }
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("hash", "b118b6b902558e486e465260c34998d4");
                hashMap.put("userId", userID);
                hashMap.put("courseId", CourseID);
                hashMap.put("txnId", txnID);
                hashMap.put("productId", CourseID);
                hashMap.put("purchaseDate", purchaseTime);
                hashMap.put("purchaseToken", purchaseToken);
                hashMap.put("deviceType", "android");
                hashMap.put("companyId", companyID);
                hashMap.put("productUUID", productID);
                hashMap.put("status", "0");
                return hashMap;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(OrderCourses.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }
}