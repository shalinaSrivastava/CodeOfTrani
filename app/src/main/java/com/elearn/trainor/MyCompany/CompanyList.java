package com.elearn.trainor.MyCompany;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.DBHandler.*;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.BaseAdapters.MyCompanyListRecyclerViewAdapter;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.DSBProperty;
import com.elearn.trainor.PropertyClasses.DownloadUrlProperty;
import com.elearn.trainor.PropertyClasses.MyCompanyProperty;
import com.elearn.trainor.PropertyClasses.NotificationProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

public class CompanyList extends AppCompatActivity implements View.OnClickListener {
    public static CompanyList instance;
    TextView text_header, tv_no_cmpny_list;
    LinearLayout ll_back, llhome, ll_dbs_row;
    RelativeLayout tbl_actionbar;
    MyCompanyListRecyclerViewAdapter myCompanyListRecyclerViewAdapter;
    List<CustomerDetailsProperty> myCompanyList;
    private RecyclerView companyRecyclerView;
    SwipeRefreshLayout swipelayout;
    ConnectionDetector connectionDetector;
    DataBaseHandlerInsert dataBaseHandlerInsert;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    SharedPreferenceManager spManager;
    List<String> customerIDList = new ArrayList<>();
    //String documentSwitch_Status, documentIDsToBeDeleted = "", From = "", notificationID = "";
    String documentIDsToBeDeleted = "", From = "", notificationID = "";
    List<DownloadUrlProperty> downloadDocumentUrlList = new ArrayList<>(); // new list for offline download
    private ProgressDialog pDialog;
    boolean isWindowActive = false;
    Long freeSpaceMB;
    IntentFilter internet_intent_filter;
    Dialog syncIncompleteDialog;
    public static int totalURLIndex = 0;
    List<DSBProperty> dsbList;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);
        isWindowActive = true;
        getControls();
        instance = this;
    }

    public static CompanyList getInstance() {
        if (instance == null) {
            instance = new CompanyList();
        }
        return instance;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isWindowActive = true;
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(CompanyList.this, "CompanyList", "Company Page");
    }
/*
    @Override
    protected void onPause() {
        super.onPause();
        if (documentSwitch_Status.equals("ON")) {
            isWindowActive = false;
        } else {
            isWindowActive = true;
        }
    }*/

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(internetInfoReceiver);
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
        dismissWaitDialog();
        super.onStop();
        isWindowActive = false;
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        connectionDetector = new ConnectionDetector(CompanyList.this);
        dbSelect = new DataBaseHandlerSelect(CompanyList.this);
        dataBaseHandlerDelete = new DataBaseHandlerDelete(CompanyList.this);
        dataBaseHandlerInsert = new DataBaseHandlerInsert(CompanyList.this);
        dbUpdate = new DataBaseHandlerUpdate(CompanyList.this);
        spManager = new SharedPreferenceManager(CompanyList.this);
        //documentSwitch_Status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DocumentSwitchStatus", " ");

        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
        myCompanyList = new ArrayList<>();
        dsbList = new ArrayList<>();
        companyRecyclerView = (RecyclerView) findViewById(R.id.company_list_recycler_view);
        companyRecyclerView.setNestedScrollingEnabled(false);

        int actionBarBackground = getResources().getColor(R.color.my_company);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(actionBarBackground);

        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipelayout.setColorSchemeResources(R.color.my_company);
        tv_no_cmpny_list = (TextView) findViewById(R.id.tv_no_cmpny_list);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getString(R.string.my_company));
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(this);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setOnClickListener(this);
        ll_dbs_row = (LinearLayout) findViewById(R.id.ll_dbs_row);
        ll_dbs_row.setOnClickListener(this);

        From = getIntent().getStringExtra("From");
        if (From != null && From.equals("Notification")) {
            notificationID = getIntent().getStringExtra("NotificationID");
        } else {
            From = "";
        }

        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        freeSpaceMB = availableSpace / SIZE_MB;

        // myCompanyList.clear();
        myCompanyList = dbSelect.getCustomerIdFromCustomerDetails("");
        if (!connectionDetector.isConnectingToInternet()) {
            companyRecyclerView.setLayoutManager(new LinearLayoutManager(CompanyList.this));
            myCompanyListRecyclerViewAdapter = new MyCompanyListRecyclerViewAdapter(CompanyList.this, myCompanyList);
            companyRecyclerView.setAdapter(myCompanyListRecyclerViewAdapter);
        }

        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    getSaveCustomerID(spManager.getUserID());
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(CompanyList.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
    }

    public void updateNotificationCount(final String NotificationType, final String UserID, final String notificationIDs) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationCount_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    int documentNotificationCount = Integer.parseInt(dbSelect.getNotificationData("NotificationCountTable", "NotificationCount", "New Document", spManager.getUserID(), "NotificationCount"));
                    dataBaseHandlerDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "New Document"});
                    dataBaseHandlerDelete.deleteTable("UpdateNotification", "userID=? AND notificationType=? AND updatedOnServer=?", new String[]{spManager.getUserID(), "Documents", "No"});
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - documentNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(CompanyList.this, total);
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                } finally {
                    getSaveCustomerID(spManager.getUserID());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getSaveCustomerID(spManager.getUserID());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"NotificationType\":\"" + NotificationType + "\",\"userID\":\"" + UserID + "\",\"NotificationIDs\":\"" + notificationIDs + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(CompanyList.this);
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
                onBackPressed();
                break;
            case R.id.ll_dbs_row:
                dsbList = dbSelect.getDSBDataFromTable();
                if (dsbList.size() > 0) {
                    Intent intent = new Intent(CompanyList.this, DSBActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    if (connectionDetector.isConnectingToInternet()) {
                        Intent intent = new Intent(CompanyList.this, DSBActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    } else {
                        swipelayout.setRefreshing(false);
                        AlertDialogManager.showDialog(CompanyList.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                    }
                }
                break;
        }
    }

    public void commonIntentMethod(Class activity, String From) {
        Intent intent = new Intent(CompanyList.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("From", From);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(HomePage.class, "MessageAndDocumentActivity");
    }

    public void getSaveCustomerID(final String userID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.CUSTOMERS_DETAIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    customerIDList.clear();
                    myCompanyList.clear();
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        dataBaseHandlerDelete.deleteTableByName("CustomerDetails", userID);
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

                            customerIDList.add(info.customer_id);
                            dataBaseHandlerInsert.addDataIntoCustomerDetailsTable(info);
                        }
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                    swipelayout.setRefreshing(false);
                } finally {
                    //dismissWaitDialog();
                    if (customerIDList != null && customerIDList.size() > 0) {
                        swipelayout.setRefreshing(false);
                        myCompanyList = dbSelect.getCustomerIdFromCustomerDetails("");
                        companyRecyclerView.setLayoutManager(new LinearLayoutManager(CompanyList.this));
                        myCompanyListRecyclerViewAdapter = new MyCompanyListRecyclerViewAdapter(CompanyList.this, myCompanyList);
                        companyRecyclerView.setAdapter(myCompanyListRecyclerViewAdapter);
                    } else {
                        if (dsbList.size() > 0) {
                            tv_no_cmpny_list.setVisibility(View.GONE);
                        } else {
                            tv_no_cmpny_list.setVisibility(View.VISIBLE);
                            ll_dbs_row.setVisibility(View.INVISIBLE);
                        }
                        companyRecyclerView.setVisibility(View.GONE);
                    }
                    if (customerIDList != null && customerIDList.size() > 0) {
                        getMyCompanyDetails(spManager.getUserID(), customerIDList.get(0));
                    } else {
                        dismissWaitDialog();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipelayout.setRefreshing(false);
                dismissWaitDialog();
                String errorMsg = error.getMessage().toString();
                if (errorMsg.equals("com.android.volley.AuthFailureError")) {
                    AlertDialogManager.showCustomDialog(CompanyList.this, "Error", "Authicatiion Error.", false, null, null, "Ok", "", null);
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(CompanyList.this);
        requestQueue11.add(stringRequest);
    }

    public void getMyCompanyDetails(final String userID, final String customerID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.My_Company_Details + customerID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                final MyCompanyProperty myCompanyProperty = new MyCompanyProperty();
                                myCompanyProperty.name = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("null") ? "" : jsonObject.getString("name");
                                myCompanyProperty.lastModified = jsonObject.getString("lastModified") == null ? "" : jsonObject.getString("lastModified").equals("null") ? "" : jsonObject.getString("lastModified");
                                myCompanyProperty.description = jsonObject.getString("description") == null ? "" : jsonObject.getString("description").equals("null") ? "" : jsonObject.getString("description");
                                myCompanyProperty.downloadUrl = jsonObject.getString("downloadUrl") == null ? "" : jsonObject.getString("downloadUrl").equals("null") ? "" : jsonObject.getString("downloadUrl");
                                myCompanyProperty.fileName = jsonObject.getString("fileName") == null ? "" : jsonObject.getString("fileName").equals("null") ? "" : jsonObject.getString("fileName");
                                myCompanyProperty.documentID = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("null") ? "" : jsonObject.getString("id");
                                myCompanyProperty.locale = jsonObject.getString("locale") == null ? "" : jsonObject.getString("locale").equals("null") ? "" : jsonObject.getString("locale");
                                if(myCompanyProperty.locale.equals("")){
                                    myCompanyProperty.locale = "nb_NO";
                                }
                                DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                downloadUrlProperty.downloadURL = myCompanyProperty.downloadUrl;
                                downloadUrlProperty.safetyCard_cardID = myCompanyProperty.fileName;
                                downloadDocumentUrlList.add(downloadUrlProperty);
                                double fileSize = Double.parseDouble(jsonObject.getString("fileSize") == null ? "0" : jsonObject.getString("fileSize").equals("null") ? "0" : jsonObject.getString("fileSize"));
                                double result_fileSize = (fileSize / 1048576);
                                String formattedFileSize = "";
                                if (result_fileSize > 1.0) {
                                    String stringFileSize = String.valueOf(result_fileSize);
                                    formattedFileSize = stringFileSize.substring(0, 4);
                                } else {
                                    if (result_fileSize == 0.0) {
                                        formattedFileSize = "0.0";
                                    } else {
                                        String stringFileSize = String.valueOf(result_fileSize);
                                        if (stringFileSize.contains("E")) {
                                            formattedFileSize = "0.0";
                                        } else {
                                            formattedFileSize = stringFileSize.substring(0, 4);
                                        }
                                    }
                                }
                                myCompanyProperty.fileSize = formattedFileSize + " mb";
                                myCompanyProperty.customerID = customerID;
                                myCompanyProperty.userID = userID;
                                String DbLastModified = dbSelect.getLastModifiedDataFromMyCompanyTable(myCompanyProperty.documentID);
                                if (DbLastModified.equals("")) {
                                    if (!dbSelect.isCompanyDocDataExist(userID, customerID, myCompanyProperty.documentID)) {
                                        dataBaseHandlerInsert.addDataIntoMyCompanyTable(myCompanyProperty);
                                    }
                                } else {
                                    if (!DbLastModified.equals(myCompanyProperty.lastModified)) {
                                        dbUpdate.updateLastModifiedDocumentData(userID, myCompanyProperty);
                                        File rootDir = android.os.Environment.getExternalStorageDirectory();
                                        File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
                                        String filePath = root.getAbsolutePath();
                                        File dir = new File(filePath);
                                        File file = new File(dir, myCompanyProperty.fileName);
                                        if (file.exists()) {
                                            DeleteFile(file);
                                        }
                                    }
                                }
                                if (!documentIDsToBeDeleted.contains(myCompanyProperty.documentID)) {
                                    if (documentIDsToBeDeleted.equals("")) {
                                        documentIDsToBeDeleted = "'" + myCompanyProperty.documentID + "'";
                                    } else {
                                        documentIDsToBeDeleted += "," + "'" + myCompanyProperty.documentID + "'";
                                    }
                                }
                            }
                            dataBaseHandlerDelete.deleteMyCompanyIDsNotExists(documentIDsToBeDeleted, customerID);
                            documentIDsToBeDeleted = "";
                        }
                    }
                } catch (Exception ex) {
                    swipelayout.setRefreshing(false);
                    dismissWaitDialog();
                    Log.d("", ex.getMessage());
                } finally {
                    swipelayout.setRefreshing(false);
                    if (customerIDList != null && customerIDList.size() > 0) {
                        if (customerIDList.contains(customerID)) {
                            customerIDList.remove(customerID);
                        }
                        if (customerIDList.size() > 0) {
                            getMyCompanyDetails(userID, customerIDList.get(0));
                        } else {
                            swipelayout.setRefreshing(false);
                            myCompanyList.clear();
                            myCompanyList = dbSelect.getCustomerIdFromCustomerDetails("");

                            companyRecyclerView.setLayoutManager(new LinearLayoutManager(CompanyList.this));
                            myCompanyListRecyclerViewAdapter = new MyCompanyListRecyclerViewAdapter(CompanyList.this, myCompanyList);
                            companyRecyclerView.setAdapter(myCompanyListRecyclerViewAdapter);
                            /*if (documentSwitch_Status.equals("ON") && downloadDocumentUrlList != null && downloadDocumentUrlList.size() > 0) {
                                totalURLIndex = downloadDocumentUrlList.size();
                                pDialog.setMessage(getResources().getString(R.string.storing) + " " + totalURLIndex + " " + getResources().getString(R.string.storingDocuments));
                                only_downloadFile();
                            } else {
                                dismissWaitDialog();
                            }*/
                            dismissWaitDialog();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                swipelayout.setRefreshing(false);
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(CompanyList.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void goToNext(CustomerDetailsProperty info) {
        List<String> localeList = dbSelect.documentLocaleList(info.customer_id, spManager.getUserID());
        Intent intent = null;
        if (localeList.size() == 1) {
            intent = new Intent(CompanyList.this, MessageAndDocumentActivity.class);
        } else {
            intent = new Intent(CompanyList.this, DocumentLocaleActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("CustomerID", info.customer_id);
        intent.putExtra("CompanyName", info.customerName);
        intent.putExtra("BackKey", "MultipleCompany");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    void DeleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteFile(child);
            }
        }
        fileOrDirectory.delete();
    }
/*
    void DeleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteFile(child);
            }
        }
        fileOrDirectory.delete();
    }

    public void only_downloadFile() {
        if (connectionDetector.isConnectingToInternet()) {
            if (downloadDocumentUrlList != null && downloadDocumentUrlList.size() > 0) {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.MyCompany/" + downloadDocumentUrlList.get(0).safetyCard_cardID);
                if (!file.exists()) {
                    if (50 <= freeSpaceMB) {
                        downloadFileFromServer(downloadDocumentUrlList.get(0).downloadURL, downloadDocumentUrlList.get(0).safetyCard_cardID);
                    } else {
                        dismissWaitDialog();
                        AlertDialogManager.showDialog(CompanyList.this, getString(R.string.not_enough_space), " ", false, null);
                    }
                } else {
                    downloadDocumentUrlList.remove(downloadDocumentUrlList.get(0));
                    if (downloadDocumentUrlList.size() > 0) {
                        only_downloadFile();
                    } else {
                        dismissWaitDialog();
                    }
                }
            } else {
                dismissWaitDialog();
            }
        } else {
            AlertDialogManager.showDialog(CompanyList.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(CompanyList.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }*/

    /*public void downloadFileFromServer(final String fileURL, final String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    String token = spManager.getToken();
                    request.authorization("Bearer " + token);
                    File rootDir = Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, fileName);
                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        DeleteFile(file);
                    }
                    //dismissWaitDialog();
                } catch (Exception ex) {
                    File rootDir = Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    File file = new File(dir, fileName);
                    DeleteFile(file);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (connectionDetector.isConnectingToInternet()) {
                    if (downloadDocumentUrlList.size() > 0) {
                        downloadDocumentUrlList.remove(downloadDocumentUrlList.get(0));
                        if (downloadDocumentUrlList.size() > 0) {
                            only_downloadFile();
                        } else {
                            dismissWaitDialog();

                        }
                    } else {
                        dismissWaitDialog();

                    }
                } else {
                    dismissWaitDialog();
                    if (syncIncompleteDialog == null) {
                        downloadDocumentUrlList.clear();
                        syncIncompleteDialog = AlertDialogManager.showDialog(CompanyList.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.sync_incomplete), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                syncIncompleteDialog = null;

                            }
                        });
                    }

                }
            }
        }.execute();
    }*/

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(CompanyList.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (isWindowActive) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    private BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            NotificationProperty notificationData = dbSelect.notificationDataToBeUpdated("NotificationCountTable", spManager.getUserID(), "New Document");
            if ((netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) || (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                if (syncIncompleteDialog != null) {
                    syncIncompleteDialog.dismiss();
                    syncIncompleteDialog = null;
                }
                showWaitDialog();
                if (notificationData.notification_id == null) {
                    notificationData = dbSelect.notificationDataToBeUpdated("UpdateNotification", spManager.getUserID(), "Documents");
                }
                if (notificationData.notification_id != null) {
                    updateNotificationCount("Documents", spManager.getUserID(), notificationData.notification_id);
                } else {
                    getSaveCustomerID(spManager.getUserID());
                }
            } else {
                dismissWaitDialog();
                int documentNotificationCount = notificationData.notification_count;
                String notificationIDs = notificationData.notification_id;
                if (documentNotificationCount != 0) {
                    dataBaseHandlerDelete.deleteTable("NotificationCountTable", "userID=? AND NotificationType=?", new String[]{spManager.getUserID(), "New Document"});
                    if (dbSelect.getDataFromNotificationUpdateTable(spManager.getUserID(), "Documents").size() == 0) {
                        NotificationProperty notificationInfo = new NotificationProperty();
                        notificationInfo.user_id = spManager.getUserID();
                        notificationInfo.notification_type = "Documents";
                        notificationInfo.notification_count = documentNotificationCount;
                        notificationInfo.device_type = "android";
                        notificationInfo.device_id = connectionDetector.getAndroid_ID(CompanyList.this);
                        notificationInfo.notification_id = notificationIDs;
                        dataBaseHandlerInsert.addDataIntoNotificationUpdateTable(notificationInfo);
                    }
                    int total = (Integer.parseInt(spManager.getTotalNotificationCount()) - documentNotificationCount);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", total + "");
                    editor.commit();
                    ShortcutBadger.applyCount(CompanyList.this, total);
                }
            }
        }
    };
}