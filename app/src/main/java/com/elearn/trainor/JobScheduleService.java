package com.elearn.trainor;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.PropertyClasses.NotificationProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JobScheduleService extends JobService {
    SharedPreferenceManager spManager;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerInsert dbInsert;
    String body, category, notificationid;
    int safetyCardCounts = 0, courseCounts = 0, newdiplomaCounts = 0, documentCounts = 0, totalNotificationCount = 0;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        getControls();
        return true;
    }

    public void getControls() {
        spManager = new SharedPreferenceManager(this);
        dbDelete = new DataBaseHandlerDelete(this);
        dbSelect = new DataBaseHandlerSelect(this);
        dbUpdate = new DataBaseHandlerUpdate(this);
        dbInsert = new DataBaseHandlerInsert(this);
        NotificationProperty notificationProperty = dbSelect.notificationDataToBeUpdated("UpdateNotification", spManager.getUserID(), "ClearAll");
        if (notificationProperty.notification_id != null) {
            updateNotificationCount(notificationProperty.notification_type, spManager.getUserID(), notificationProperty.notification_id, notificationProperty.user_id);
        } else {
            getAllPendingNotification();
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public void updateNotificationCount(final String NotificationType, final String userID, final String notificationIDs, final String ids) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_NotificationCount_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    dbDelete.deleteNotificationUpdatedData("UpdateNotification", spManager.getUserID(), notificationIDs, ids);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    getAllPendingNotification();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getAllPendingNotification();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"NotificationType\":\"" + NotificationType + "\",\"userID\":\"" + userID + "\",\"NotificationIDs\":\"" + notificationIDs + "\"}";
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
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
                            }
                        }
                    }
                } catch (JSONException e) {

                } finally {
                    try {
                        if (totalNotificationCount != 0) {
                            final HomePage homePage = HomePage.getInstance();
                            try {
                                if (homePage != null) {
                                    homePage.callRunOnUiThreadInServices(totalNotificationCount);
                                }
                            } catch (Exception ex) {
                                Log.d("Error", ex.getMessage());
                            }
                            final HomeNotification homeNotification = HomeNotification.getInstance();
                            if (homeNotification != null) {
                                homeNotification.updateList();
                            }
                        } else {
                            final HomePage homePage = HomePage.getInstance();
                            homePage.ll_notifications.setVisibility(View.INVISIBLE);
                        }
                        spManager.removeSharedPreferenceByName("TotalNotification");
                        SharedPreferences.Editor editor = spManager.getTotalNotificationSharedPreference();
                        editor.putString("Total", totalNotificationCount + "");
                        editor.commit();
                    } catch (Exception ex) {

                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
