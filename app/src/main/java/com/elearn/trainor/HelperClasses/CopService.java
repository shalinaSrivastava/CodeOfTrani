package com.elearn.trainor.HelperClasses;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;


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
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CopService extends Service {
    public SharedPreferenceManager spManager;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerInsert dbInsert;
    List<String> customerIDList;
    //NetworkChangeReceiver networkChangeReceiver;
    InternetConnectivityReceiver networkChangeReceiver;

    public CopService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbDelete = new DataBaseHandlerDelete(this);
        dbInsert = new DataBaseHandlerInsert(this);
        customerIDList = new ArrayList();
        spManager = new SharedPreferenceManager(CopService.this);
        networkChangeReceiver = new InternetConnectivityReceiver();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, filter);
    }

    /*class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isConnected() || mobile.isConnected()) {
                Handler myHandler = new Handler();
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CopService.this, "Called After 10 min", Toast.LENGTH_SHORT).show();
                        getSaveCustomerID(spManager.getToken());
                    }
                }, 10000);
            }
        }
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyIBinder();
    }

    public class MyIBinder extends Binder {
        public CopService getService() {
            return CopService.this;
        }
    }

    public void callAPI() {
        getSaveCustomerID(spManager.getToken());
    }

    public void getSaveCustomerID(final String token) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.CUSTOMERS_DETAIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        customerIDList.clear();
                        dbDelete.deleteTableByName("CustomerDetails", "");
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

                            dbInsert.addDataIntoCustomerDetailsTable(info);
                            customerIDList.add(info.customer_id);
                        }
                    }
                } catch (Exception ex) {
                    ExceptionHandler.getErrorMessage(CopService.this, ex);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(CopService.this);
        requestQueue11.add(stringRequest);
    }
}
