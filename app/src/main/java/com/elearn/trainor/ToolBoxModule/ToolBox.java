package com.elearn.trainor.ToolBoxModule;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.Login;
import com.elearn.trainor.PropertyClasses.*;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.elearn.trainor.BaseAdapters.*;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.services.network.HttpRequest;

public class ToolBox extends AppCompatActivity implements View.OnClickListener {
    public static ToolBox instance;
    RecyclerView recycler_view;
    LinearLayout ll_back, llhome;
    RelativeLayout tbl_actionbar;
    TextView text_header;
    List<ToolsProperty> toolsList, downloadToolsList;
    List<ToolsProperty> toolListForRecyclerView;
    List<ToolsProperty> toolListOffline;
    ConnectionDetector connectionDetector;
    ToolsRecyclerViewAdapter adapter;
    String From, toolsSwitch_status, NetworkMode = "";
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerDelete dbDelete;
    SharedPreferenceManager spManager;
    SwipeRefreshLayout swipelayout;
    private ProgressDialog pDialog;
    boolean isWindowActive = false, refreshTag = false;
    List<DownloadUrlProperty> downloadUrlList = new ArrayList<>(); // new list for download
    Long freeSpaceMB;
    IntentFilter internet_intent_filter;
    Dialog syncIncompleteDialog;
    public static int totalURLIndex;
    FirebaseAnalytics analytics;
    Trace myTrace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_box);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        isWindowActive = true;
        getControls();

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
        //analytics.setCurrentScreen(this, "ToolBox", null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (toolsSwitch_status.equals("ON")) {
            isWindowActive = false;
        } else {
            isWindowActive = true;
        }

    }

    @Override
    protected void onStop() {
        dismissWaitDialog();
        if (internetInfoReceiver != null) {
            if (internetInfoReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        super.onStop();
        isWindowActive = false;
    }

    public static ToolBox getInstance() {
        if (instance == null) {
            instance = new ToolBox();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        myTrace = FirebasePerformance.getInstance().newTrace("ToolBox_trace");
        myTrace.start();
        spManager = new SharedPreferenceManager(ToolBox.this);
        dbInsert = new DataBaseHandlerInsert(ToolBox.this);
        dbSelect = new DataBaseHandlerSelect(ToolBox.this);
        dbUpdate = new DataBaseHandlerUpdate(ToolBox.this);
        dbDelete = new DataBaseHandlerDelete(ToolBox.this);
        toolsSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "ToolsSwitchStatus", " ");
        connectionDetector = new ConnectionDetector(ToolBox.this);
        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
        downloadToolsList = new ArrayList<>();
        toolsList = new ArrayList<>();
        toolListForRecyclerView = new ArrayList<>();
        toolListOffline = new ArrayList<>();
        int actionBarBackground = getResources().getColor(R.color.tools);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(actionBarBackground);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        text_header = (TextView) findViewById(R.id.text_header);
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipelayout.setColorSchemeResources(R.color.tools);
        text_header.setText(getResources().getString(R.string.tools));

        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        freeSpaceMB = availableSpace / SIZE_MB;
        if (getIntent().getStringExtra("From") != null && getIntent().getStringExtra("From").equals("Login")) {
            Bundle bundle = new Bundle();
            bundle.putString("NotLoggedInToolView", "Yes");
            analytics.logEvent("NotLoggedInToolView", bundle);
            From = getIntent().getStringExtra("From").toString();
            llhome.setVisibility(View.INVISIBLE);
        } else if (getIntent().getStringExtra("From") != null && getIntent().getStringExtra("From").equals("HomePage")) {
            Bundle bundle = new Bundle();
            bundle.putString("LoggedInToolView", "Yes");
            analytics.logEvent("LoggedInToolView", bundle);
            From = getIntent().getStringExtra("From").toString();
        }
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        if (!connectionDetector.isConnectingToInternet()) {
            bindToolData();
        }
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    refreshTag = true;
                    showWaitDialog();
                    getTools();
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(ToolBox.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
        String CurrentLang = Locale.getDefault().getLanguage();
        String sharedLanguage = spManager.getLanguage();
        if (!sharedLanguage.equals(CurrentLang)) {
            if (sharedLanguage.equals("")) {
                setLocale(CurrentLang);
            } else {
                setLocale(sharedLanguage);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                if (From.equals("Login")) {
                    commonIntentMethod(Login.class);
                } else if (From.equals("HomePage")) {
                    commonIntentMethod(HomePage.class);
                }
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class);
                break;
        }
    }

    public void commonIntentMethod(Class activity) {
        myTrace.stop();
        Intent intent = new Intent(ToolBox.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void getTools() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Tools_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        toolsList.clear();
                        toolListForRecyclerView.clear();
                        downloadUrlList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ToolsProperty property = new ToolsProperty();
                            property.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id");
                            property.last_modified = jsonObject.getString("last_modified") == null ? "" : jsonObject.getString("last_modified");
                            String filename = jsonObject.getString("name") == null ? "" : jsonObject.getString("name");
                            String convertedFileName = filename.replaceAll("[-+.^:,]", " ");
                            property.name = convertedFileName;
                            property.force_download = jsonObject.getString("force_download") == null ? "" : jsonObject.getString("force_download");
                            property.language_code = jsonObject.getString("language_code") == null ? "" : jsonObject.getString("language_code");
                            property.background_color = jsonObject.getString("background_color") == null ? "" : jsonObject.getString("background_color");
                            property.is_landscape = jsonObject.getString("is_landscape") == null ? "" : jsonObject.getString("is_landscape").equals("") ? "" : jsonObject.getString("is_landscape");
                            property.customer_ids = jsonObject.getString("customer_ids") == null ? "" : jsonObject.getString("customer_ids");
                            property.iconURL = jsonObject.getString("icon") == null ? "" : jsonObject.getString("icon");
                            property.file = jsonObject.getString("file") == null ? "" : jsonObject.getString("file");
                            property.file_size = jsonObject.getString("file_size") == null ? "" : jsonObject.getString("file_size");

                            // new 22-02-2018
                            DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                            downloadUrlProperty.downloadURL = property.file;
                            downloadUrlProperty.safetyCard_cardID = property.name;
                            downloadUrlProperty.licenseId = property.id;
                            downloadUrlList.add(downloadUrlProperty);
                            toolsList.add(property);
                        }
                        if (toolsList.size() > 0) {
                            String locale = Resources.getSystem().getConfiguration().locale.getLanguage();
                            String locale2 = spManager.getLanguage();
                            for (int i = 0; i < toolsList.size(); i++) {
                                saveToolboxAccodingToLanguage(locale, locale2, i, toolsList, toolListForRecyclerView);
                            }
                            if (toolListForRecyclerView.size() > 0) {
                                downloadToolsList.clear();
                                for (ToolsProperty info : toolListForRecyclerView) {
                                    String last_modified = dbSelect.getLastModifiedDataFromToolTable(info.id);
                                    if (last_modified.equals("")) {
                                        dbInsert.addDataIntoToolBoxTable(info);
                                    } else if (!last_modified.equals(info.last_modified)) {
                                        dbUpdate.updateToolBoxDetails(info, "ToolBoxUpdated");
                                    }
                                    info.last_modified_From_DB = last_modified;
                                    if (info.force_download.equals("1")) {
                                        downloadToolsList.add(info);
                                    }

                                    if (!last_modified.equals(info.last_modified) && info.force_download.equals("0")) {
                                        File rootDir = android.os.Environment.getExternalStorageDirectory();
                                        File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/UnZipped/" + info.name);
                                        File rootZipped = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/" + info.name);
                                        String filePath = root.getAbsolutePath();
                                        File dir = new File(filePath);
                                        String filePath1 = rootZipped.getAbsolutePath();
                                        File dir1 = new File(filePath1);
                                        deleteSpecificTool(dir);
                                        deleteSpecificTool(dir1);
                                    }
                                }
                                final LinearLayoutManager layoutManager = new LinearLayoutManager(ToolBox.this);
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                recycler_view.setLayoutManager(layoutManager);
                                adapter = new ToolsRecyclerViewAdapter(ToolBox.this, toolListForRecyclerView, From);
                                recycler_view.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (Exception ex) {
                    swipelayout.setRefreshing(false);
                    dismissWaitDialog();
                    Log.d("Error", ex.getMessage());
                } finally {
                    swipelayout.setRefreshing(false);
                    if (connectionDetector.isConnectingToInternet() && toolsSwitch_status.equals("ON") && downloadUrlList.size() > 0) {
                        totalURLIndex = toolListForRecyclerView.size();
                        pDialog.setMessage(getResources().getString(R.string.storing) + " " + totalURLIndex + " " + getResources().getString(R.string.storingtools));
                        only_downloadFile();
                    } else {
                        if (downloadToolsList != null && downloadToolsList.size() > 0) {
                            toolsFileDownload(downloadToolsList.get(0));
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                swipelayout.setRefreshing(false);
                AlertDialogManager.showDialog(ToolBox.this, "", VolleyErrorHandler.getErrorMessage(ToolBox.this, error), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        if (From.equals("Login")) {
                            commonIntentMethod(Login.class);
                        } else if (From.equals("HomePage")) {
                            commonIntentMethod(HomePage.class);
                        }
                    }
                });
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(ToolBox.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    private void downloadFile(final String url, final String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(url);
                    URLConnection conn = u.openConnection();
                    int contentLength = conn.getContentLength();
                    DataInputStream stream = new DataInputStream(u.openStream());
                    byte[] buffer = new byte[contentLength];
                    stream.readFully(buffer);
                    stream.close();
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, fileName);
                    DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                    fos.write(buffer);
                    fos.flush();
                    fos.close();
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (downloadToolsList != null && downloadToolsList.size() > 0) {
                    downloadToolsList.remove(0);
                }
                if (downloadToolsList.size() > 0) {
                    toolsFileDownload(downloadToolsList.get(0));
                } else {
                    dismissWaitDialog();
                }
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if (internetInfoReceiver != null) {
            if (internetInfoReceiver.isInitialStickyBroadcast()) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
        }
        if (From.equals("Login")) {
            commonIntentMethod(Login.class);
        } else if (From.equals("HomePage")) {
            commonIntentMethod(HomePage.class);
        }
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(ToolBox.this);
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

    public void finishActivity() {
        finish();
    }

    public void saveToolboxAccodingToLanguage(String locale, String locale2, int i, List<ToolsProperty> toolList, List<ToolsProperty> languageBasedToolList) {
        if (From.equals("Login")) {
            if (locale.substring(0, 2).contains("en") || locale.substring(0, 2).contains("nb")) {
                if (toolList.get(i).language_code.contains(locale.substring(0, 2))) {
                    languageBasedToolList.add(toolList.get(i));
                }
            } else if (locale.substring(0, 2).contains("sv") || locale.substring(0, 2).contains("nb")) {
                if (toolList.get(i).language_code.contains("nb")) {
                    languageBasedToolList.add(toolList.get(i));
                }
            } else {
                if (toolList.get(i).language_code.startsWith("en")) {
                    languageBasedToolList.add(toolList.get(i));
                }
            }
        } else if (From.equals("HomePage")) {
            if (locale2.substring(0, 2).contains("en") || locale2.substring(0, 2).contains("nb")) {
                if (toolList.get(i).language_code.contains(locale2.substring(0, 2))) {
                    languageBasedToolList.add(toolList.get(i));
                }
            } else if (locale2.substring(0, 2).contains("sv") || locale2.substring(0, 2).contains("nb")) {
                if (toolList.get(i).language_code.contains("nb")) {
                    languageBasedToolList.add(toolList.get(i));
                }
            } else {
                if (toolList.get(i).language_code.startsWith("en")) {
                    languageBasedToolList.add(toolList.get(i));
                }
            }
        }
    }

    public void setLocale(String language) {
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public void bindToolData() {
        List<ToolsProperty> toolsList = dbSelect.getDownlodedToolDetails("ToolBox", " ");
        if (toolsList.size() > 0) {
            String locale = Resources.getSystem().getConfiguration().locale.getLanguage();
            String locale2 = spManager.getLanguage();
            toolListOffline.clear();
            for (int i = 0; i < toolsList.size(); i++) {
                saveToolboxAccodingToLanguage(locale, locale2, i, toolsList, toolListOffline);
            }
            if (toolListOffline.size() > 0) {
                final LinearLayoutManager layoutManager = new LinearLayoutManager(ToolBox.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recycler_view.setLayoutManager(layoutManager);
                adapter = new ToolsRecyclerViewAdapter(ToolBox.this, toolListOffline, From);
                recycler_view.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } else {
                AlertDialogManager.showDialog(ToolBox.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        if (From.equals("Login")) {
                            commonIntentMethod(Login.class);
                        } else {
                            commonIntentMethod(HomePage.class);
                        }
                    }
                });
            }
        } else {
            AlertDialogManager.showDialog(ToolBox.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                @Override
                public void onClick() {
                    if (From.equals("Login")) {
                        commonIntentMethod(Login.class);
                    } else {
                        commonIntentMethod(HomePage.class);
                    }
                }
            });
        }
    }

    void deleteSpecificTool(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                deleteSpecificTool(child);
            }
        fileOrDirectory.delete();
    }

    public void only_downloadFile() {
        if (connectionDetector.isConnectingToInternet()) {
            if (downloadUrlList != null && downloadUrlList.size() > 0) {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/" + downloadUrlList.get(0).safetyCard_cardID);
                if (!file.exists()) {
                    if (50 <= freeSpaceMB) {
                        downloadFileFromServer(downloadUrlList.get(0).downloadURL, downloadUrlList.get(0).safetyCard_cardID);
                    } else {
                        AlertDialogManager.showDialog(ToolBox.this, getString(R.string.not_enough_space), " ", false, null);
                    }
                } else {
                    downloadUrlList.remove(downloadUrlList.get(0));
                    if (downloadUrlList.size() > 0) {
                        only_downloadFile();
                    } else {
                        dismissWaitDialog();
                    }
                }
            } else {
                dismissWaitDialog();
            }
        } else {
            AlertDialogManager.showDialog(ToolBox.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_msg), false, new IClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(ToolBox.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    public void downloadFileFromServer(final String fileURL, final String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    request.accept("application/zip");
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, fileName);
                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        deleteSpecificTool(file);
                    }
                    //dismissWaitDialog();
                } catch (Exception ex) {
                    dismissWaitDialog();
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    File file = new File(dir, fileName);
                    deleteSpecificTool(file);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (connectionDetector.isConnectingToInternet()) {
                    if (downloadUrlList.size() > 0) {
                        ToolsProperty info = new ToolsProperty();
                        info.id = downloadUrlList.get(0).licenseId;
                        dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                        downloadUrlList.remove(downloadUrlList.get(0));
                        if (downloadUrlList.size() > 0) {
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
                        syncIncompleteDialog = AlertDialogManager.showDialog(ToolBox.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.sync_incomplete), false, new IClickListener() {
                            @Override
                            public void onClick() {
                                syncIncompleteDialog = null;
                                //syncIncompleteDialog.dismiss();
                            }
                        });
                    }
                }
            }
        }.execute();
    }

    private BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if ((netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) || (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                if (syncIncompleteDialog != null) {
                    syncIncompleteDialog.dismiss();
                    syncIncompleteDialog = null;
                }
                showWaitDialog();
                getTools();
            } else {
                if (NetworkMode.equals("Online")) {
                    AlertDialogManager.showDialog(ToolBox.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        }
    };

    public void toolsFileDownload(ToolsProperty info) {
        String last_modified_From_DB = info.last_modified_From_DB;
        if (info.force_download.equals("1")) {
            if (connectionDetector.isConnectingToInternet()) {
                dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                File rootDir = android.os.Environment.getExternalStorageDirectory();
                File toolFile = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/" + info.name);
                if ((!last_modified_From_DB.equals(info.last_modified)) || (!(toolFile.exists()))) {
                    downloadFile(info.file, info.name);
                } else {
                    if (downloadToolsList != null && downloadToolsList.size() > 0) {
                        downloadToolsList.remove(0);
                    }
                    if (downloadToolsList.size() > 0) {
                        toolsFileDownload(downloadToolsList.get(0));
                    } else {
                        dismissWaitDialog();
                    }
                }
            } else {
                dismissWaitDialog();
            }
        }
    }
}