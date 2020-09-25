package com.elearn.trainor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import com.elearn.trainor.BaseAdapters.HomeNotificationRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.PropertyClasses.NotificationProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

//import static com.google.android.gms.internal.zzahn.runOnUiThread;

public class HomeNotification extends AppCompatActivity implements View.OnClickListener {
    public static HomeNotification instance = null;
    public List<NotificationProperty> notificationsList, courseList, diplomaList, safetyCardList;
    RecyclerView recyclerView;
    public HomeNotificationRecyclerViewAdapter notificationAdapter;
    LinearLayout ll_clearList, ll_back, llhome;
    public TextView text_header, txt_clear_list_count;
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;
    public DataBaseHandlerInsert dbInsert;
    public DataBaseHandlerSelect dbSelect;
    public DataBaseHandlerDelete dbDelete;
    public DataBaseHandlerUpdate dbUpdate;
    String deviceID = "";
    SwipeRefreshLayout swiperefresh;
    String body, category, notificationid;
    int safetyCardCounts = 0, courseCounts = 0, newdiplomaCounts = 0, documentCounts = 0, totalNotificationCount = 0;
    TextView no_undread_notification;
    RelativeLayout rl_notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_notification);
        getControls();
        instance = this;
    }

    public static HomeNotification getInstance() {
        if (instance == null) {
            instance = new HomeNotification();
        }
        return instance;
    }

    public void getControls() {
        dbInsert = new DataBaseHandlerInsert(HomeNotification.this);
        dbDelete = new DataBaseHandlerDelete(HomeNotification.this);
        dbSelect = new DataBaseHandlerSelect(HomeNotification.this);
        dbUpdate = new DataBaseHandlerUpdate(HomeNotification.this);
        spManager = new SharedPreferenceManager(HomeNotification.this);
        connectionDetector = new ConnectionDetector(HomeNotification.this);
        swiperefresh = findViewById(R.id.swiperefresh);
        notificationsList = new ArrayList<>();
        courseList = new ArrayList<>();
        diplomaList = new ArrayList<>();
        safetyCardList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.notification_recyclerView);
        text_header = (TextView) findViewById(R.id.text_header);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_clearList = (LinearLayout) findViewById(R.id.ll_clearList);
        txt_clear_list_count = (TextView) findViewById(R.id.txt_clear_list_count);
        no_undread_notification = (TextView) findViewById(R.id.no_undread_notification);
        no_undread_notification.setVisibility(View.GONE);
        rl_notification = (RelativeLayout) findViewById(R.id.rl_notification);
        text_header.setText(getString(R.string.new_notification_header));
        deviceID = connectionDetector.getAndroid_ID(HomeNotification.this);
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        ll_clearList.setOnClickListener(this);
        if (connectionDetector.isConnectingToInternet()) {
            getAllPendingNotification();
        } else {
            updateList();
        }

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    getAllPendingNotification();
                } else {
                    swiperefresh.setRefreshing(false);
                    AlertDialogManager.showDialog(HomeNotification.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
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
            case R.id.ll_clearList:
                NotificationProperty notificationProperty = dbSelect.notificationDataToBeUpdated("NotificationCountTable", spManager.getUserID(), "ClearAll");
                if (connectionDetector.isConnectingToInternet()) {
                    // clearAllNotification(spManager.getUserID());
                    if (notificationProperty.notification_id != null) {
                        clearAllNotification(spManager.getUserID(), notificationProperty.notification_id, notificationProperty.user_id);
                    }
                } else {
                    notificationProperty.user_id = spManager.getUserID();
                    notificationProperty.notification_type = "ClearAll";
                    notificationProperty.device_id = connectionDetector.getAndroid_ID(this);
                    notificationProperty.device_type = "android";
                    dbInsert.addDataIntoNotificationUpdateTable(notificationProperty);
                    clearListAction();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(HomePage.class);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(HomeNotification.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void clearAllNotification(final String userID, final String notificationIDs, final String ids) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationCount_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dbDelete.deleteNotificationUpdatedData("UpdateNotification", spManager.getUserID(), notificationIDs, ids);
                clearListAction();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String erro = "error";
                Log.d("Error", erro);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                String str = "{\"NotificationType\":\"ClearAll\",\"userID\":\"" + userID + "\",\"NotificationIDs\":\"" + notificationIDs + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(HomeNotification.this);
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void updateList() {

        try {
            if (txt_clear_list_count != null) {
                txt_clear_list_count.setText(getResources().getString(R.string.clear_list));
            }
        } catch (Exception ex) {
            // Log.d("Error", ex.getMessage());
        } finally {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (notificationsList != null) {
                        notificationsList.clear();
                        notificationsList = dbSelect.getNotificationListFromTable(spManager.getUserID());
                        recyclerView.setLayoutManager(new LinearLayoutManager(HomeNotification.this));
                        notificationAdapter = new HomeNotificationRecyclerViewAdapter(HomeNotification.this, notificationsList);
                        recyclerView.setAdapter(notificationAdapter);
                        notificationAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public void clearListAction() {
        try {
            dbDelete.deleteTable("NotificationCountTable", "userID=?", new String[]{spManager.getUserID()});
            spManager.removeSharedPreferenceByName("TotalNotification");
            SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
            editor.putString(spManager.getUserID(), "0");
            editor.commit();
            ShortcutBadger.applyCount(HomeNotification.this, 0);
        } catch (Exception ex) {
            //  Log.d("", ex.getMessage());
        } finally {
            notificationsList.clear();
            notificationAdapter.notifyDataSetChanged();
            ll_clearList.setVisibility(View.GONE);
            Intent intent = new Intent(HomeNotification.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void getAllPendingNotification() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.GetAllPendingNotification_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dbDelete.deleteValueFromTable("NotificationCountTable", "userID", spManager.getUserID());
                try {
                    if (response != null && !response.equals("")) {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject != null) {
                            JSONArray jsonArray = jsonObject.getJSONArray("PendingNotification");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    category = jsonObject1.getString("notificationType");
                                    body = jsonObject1.getString("notificationMessage");
                                    notificationid = jsonObject1.getString("notificationID");

                                    JSONArray jsonArray2 = jsonObject.getJSONArray("NotificationCount");
                                    if (jsonArray2 != null && jsonArray2.length() > 0) {
                                        for (int j = 0; j < jsonArray2.length(); j++) {
                                            JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                            courseCounts = jsonObject2.getInt("CourseCount");
                                            safetyCardCounts = jsonObject2.getInt("SafetyCardCount");
                                            documentCounts = jsonObject2.getInt("DocumentCount");
                                            newdiplomaCounts = jsonObject2.getInt("DiplomaCount");
                                            totalNotificationCount = jsonObject2.getInt("totalNotification");
                                        }
                                    }
                                    if (dbSelect.isNotificationIDExists(spManager.getUserID(), notificationid) == false) {
                                        addNotification(courseCounts, newdiplomaCounts, safetyCardCounts, documentCounts, body, category, notificationid);
                                    }
                                }
                            } else {
                                no_undread_notification.setVisibility(View.VISIBLE);
                                rl_notification.setVisibility(View.GONE);
                            }
                        } else {
                            no_undread_notification.setVisibility(View.VISIBLE);
                            rl_notification.setVisibility(View.GONE);
                        }
                    } else {
                        no_undread_notification.setVisibility(View.VISIBLE);
                        rl_notification.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    swiperefresh.setRefreshing(false);
                    e.printStackTrace();
                } finally {
                    swiperefresh.setRefreshing(false);
                    spManager.removeSharedPreferenceByName("TotalNotification");
                    SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                    editor.putString("Total", totalNotificationCount + "");
                    editor.commit();
                    if (totalNotificationCount != 0) {
                        ShortcutBadger.applyCount(HomeNotification.this, totalNotificationCount);
                    } else {
                        ShortcutBadger.applyCount(HomeNotification.this, 0);
                    }
                    updateList();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swiperefresh.setRefreshing(false);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"userID\":\"" + spManager.getUserID() + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void addNotification(int courseCount, int newDiplomaCount, int safetyCardCount,
                                int documentCount, String body, String category, String notificationid) {
        if (category.equals("ClassRoomCourse") || category.equals("E-Learning Course")) {
            dbInsert.addDataIntoNotificationCountTable("Course", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("Course", courseCount + "", spManager.getUserID());
        }
        /*if (category.equals("New safety card") || category.equals("Safety card expired")) {
            dbInsert.addDataIntoNotificationCountTable("SafetyCard", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("SafetyCard", safetyCardCount + "", spManager.getUserID());
        }*/
        if (category.equals("Safety card")) {
            dbInsert.addDataIntoNotificationCountTable("SafetyCard", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("SafetyCard", safetyCardCount + "", spManager.getUserID());
        }
        if (category.equals("New Diploma")) {
            dbInsert.addDataIntoNotificationCountTable("New Diploma", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("New Diploma", newDiplomaCount + "", spManager.getUserID());
        }
        if (category.equals("Documents")) {
            dbInsert.addDataIntoNotificationCountTable("New Document", spManager.getUserID(), body, category, notificationid);
            dbUpdate.updateNotificationCount("New Document", documentCount + "", spManager.getUserID());
        }
    }
}