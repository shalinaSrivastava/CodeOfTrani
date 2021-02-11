package com.elearn.trainor.CourseModule;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsIntent;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.R;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CourseWebActivity extends Activity {
    String BaseURL;
    String NetworkMode = "", CourseID = "", LicenceID = "", CourseFolderName = "";
    WebView webView;
    ProgressDialog progressDialog;
    ConnectionDetector connectionDetector;
    DataBaseHandlerInsert DBInsert;
    DataBaseHandlerUpdate DBUpdate;
    DataBaseHandlerSelect DBSelect;
    ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    MessageDigest digest = null;
    String secretKey, identifier = "", scormValue = "", scorm_path = "", jquery_path = "", Salt_Lisence_Id = "", CMI_Progress = "", currentDate = "", restartCompletionStatus = "", courseStartedDate = "";
    List<String> scormKeyList;
    boolean isActivityLive = false;
    IntentFilter internet_intent_filter;
    FirebaseAnalytics analytics;
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        isActivityLive = true;
        getControl();
        if (NetworkMode.equals("Online")) {
            if (connectionDetector.isConnectingToInternet()) {
                playCourseOnline();
            } else {
                AlertDialogManager.showDialog(CourseWebActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
            }
        } else {
            bundle.putString("CourseViewedOffline", "Yes");
            analytics.logEvent("CourseViewedOffline", bundle);
            restartCompletionStatus = DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "cmiCompletionStatus");
            scorm_path = "file:///android_asset/scorm_wrapper.js";
            jquery_path = "file:///android_asset/jquery.js";
            //Salt_Lisence_Id = "Otlq8k9Az7cXcr0sKo5v" + LicenceID;
            showWaitDialog();
            playCourseOffline(jquery_path, scorm_path, LicenceID);
        }
    }

    @Override
    protected void onStart() {
        isActivityLive = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        isActivityLive = true;
       // analytics.setCurrentScreen(CourseWebActivity.this, "CoursePlayer", this.getClass().getSimpleName());
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    public void getControl() {
        analytics = FirebaseAnalytics.getInstance(this);
        spManager = new SharedPreferenceManager(CourseWebActivity.this);
        scormKeyList = new ArrayList<>();
        BaseURL = getIntent().getStringExtra("CourseUrl");
        CourseID = getIntent().getStringExtra("CourseID");
        LicenceID = getIntent().getStringExtra("LicenceID");
        NetworkMode = getIntent().getStringExtra("NetworkMode");
        CourseFolderName = getIntent().getStringExtra("CourseFolderName");
        DBInsert = new DataBaseHandlerInsert(CourseWebActivity.this);
        DBUpdate = new DataBaseHandlerUpdate(CourseWebActivity.this);
        DBSelect = new DataBaseHandlerSelect(CourseWebActivity.this);
        connectionDetector = new ConnectionDetector(CourseWebActivity.this);
        progressDialog = new ProgressDialog(CourseWebActivity.this);
        webView = (WebView) findViewById(R.id.webView);
        scormKeyList.add("cmi.location");
        scormKeyList.add("cmi.progress_measure");
        scormKeyList.add("cmi.completion_status");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Calendar calendar = Calendar.getInstance();
            courseStartedDate = sdf.format(calendar.getTime());
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
    }

    public void playCourseOnline() {
        bundle.putString("CourseViewedOnline", "Yes");
        analytics.logEvent("CourseViewedOnline", bundle);
        if (Build.MANUFACTURER.equals("Pixavi")) {
            playCourseOnlinePIXAVI();
        } else {
            playCourseOnlineNonPIXAVI();
        }
    }

    public void playCourseOnlineNonPIXAVI() {
        progressDialog.setTitle("");
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                Log.d("Progress", progress + "");
                if (progress == 100) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                super.onPageFinished(view, url);
            }
        });

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setEnableSmoothTransition(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.clearCache(true);
        webView.clearHistory();
        webView.loadUrl(BaseURL + "?showExit=true");
    }

    public void playCourseOnlinePIXAVI() {
        webView.setVisibility(View.GONE);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.courses));
        builder.enableUrlBarHiding().setShowTitle(false);
        builder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back));
        CustomTabsIntent customTabsIntent = builder.enableUrlBarHiding().setShowTitle(false).build();
        customTabsIntent.intent.setData(Uri.parse(BaseURL + "?showExit=true"));
        startActivityForResult(customTabsIntent.intent, 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CourseWebActivity.this, Courses.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    /*public void playCourseOffline(String jquery_path, String scorm_wrapper_path, String CourseFolderName) {
        File rootDir = Environment.getExternalStorageDirectory();
        File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/");
        String path = root.getAbsolutePath() + "/" + CourseFolderName + "/UnZipped/html5.html";
        File fileToShow = new File(path);
        Uri uri = FileProvider.getUriForFile(CourseWebActivity.this,
                getString(R.string.file_provider_authority),
                fileToShow);
        if (fileToShow.exists()) {
            WebSettings webSetting = webView.getSettings();
            webSetting.setDatabaseEnabled(true);
            webSetting.setJavaScriptEnabled(true);
            webSetting.setDomStorageEnabled(true);
            webSetting.setAllowContentAccess(true);
            webSetting.setAllowFileAccess(true);
            webSetting.setAllowFileAccessFromFileURLs(true);
            webSetting.setAllowUniversalAccessFromFileURLs(true);
            webSetting.setLoadsImagesAutomatically(true);
            webSetting.setDisplayZoomControls(true);
            webView.setWebViewClient(new WebViewClient());
            webView.addJavascriptInterface(new JsHandler(), "JsHandler");
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head>");
                sb.append("<Title>Please wait...</Title>");
                sb.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
                sb.append("<meta http-equiv='cache-control' content='max-age=0' />");
                sb.append("<meta http-equiv='cache-control' content='no-cache' />");
                sb.append("<meta http-equiv='expires' content='0' />");
                sb.append("<meta http-equiv='expires' content='Tue, 01 Jan 1980 1:00:00 GMT' />");
                sb.append("<meta http-equiv='pragma' content='no-cache' />");
                sb.append("<meta charset='UTF-8'>");
                sb.append("<meta name='Generator' content='Cocoa HTML Writer'>");
                sb.append("<meta name='CocoaVersion' content='1504.81'>");
                sb.append("<meta name='viewport' content='width=device-width,height=device-height, initial-scale=1.0' />");
                sb.append(String.format("<script type='text/javascript' src='%s'>", jquery_path));
                sb.append("</script>");
                sb.append(String.format("<script type='text/javascript' src='%s'>", scorm_wrapper_path));
                sb.append("</script>");
                sb.append("<style type='text/css'>\n" +
                        " html,body,iframe {\n" +
                        "\theight: 100%;\n" +
                        "\twidth: 100%;\n" +
                        "\tmargin: 0;\n" +
                        "\tpadding: 0;\n" +
                        "\tmin-height :100%;\n" +
                        "}\n" +
                        "</style>\n");
                sb.append("</head>");
                sb.append("<body>");
                sb.append(String.format(" <iframe id='myFrame' frameborder='0' src='%s'>", uri + "?showExit=true"));
                sb.append("</iframe></body></html>");
                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                    }

                    @Override
                    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return super.shouldOverrideUrlLoading(view, url);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        dismissWaitDialog();
                        super.onPageFinished(view, url);
                    }
                });
                webView.loadDataWithBaseURL("", sb.toString(), "text/html", "utf-8", "");
            } catch (Exception ex) {
                Log.d("", ex.getMessage());
            }
        } else {
            dismissWaitDialog();
            AlertDialogManager.showDialog(CourseWebActivity.this, getResources().getString(R.string.file_not_found), getResources().getString(R.string.course_file_not_exist), false, null);
        }
    }*/

    public void playCourseOffline(String jquery_path, String scorm_wrapper_path, String CourseFolderName) {
        File rootDir = android.os.Environment.getExternalStorageDirectory();
        //File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/");
        File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/");
        String path = root.getAbsolutePath() + "/" + CourseFolderName + "/UnZipped/html5.html";
        File fileToShow = new File(path);
        if (fileToShow.exists()) {
            WebSettings webSetting = webView.getSettings();
            webSetting.setDatabaseEnabled(true);
            webSetting.setJavaScriptEnabled(true);
            webSetting.setDomStorageEnabled(true);
            webSetting.setAllowContentAccess(true);
            webSetting.setAllowFileAccess(true);
            webSetting.setAllowFileAccessFromFileURLs(true);
            webSetting.setAllowUniversalAccessFromFileURLs(true);
            webSetting.setLoadsImagesAutomatically(true);
            webSetting.setDisplayZoomControls(true);
            webView.setWebViewClient(new WebViewClient());
            webView.addJavascriptInterface(new JsHandler(), "JsHandler");
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head>");
                sb.append("<Title>Please wait...</Title>");
                sb.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
                sb.append("<meta http-equiv='cache-control' content='max-age=0' />");
                sb.append("<meta http-equiv='cache-control' content='no-cache' />");
                sb.append("<meta http-equiv='expires' content='0' />");
                sb.append("<meta http-equiv='expires' content='Tue, 01 Jan 1980 1:00:00 GMT' />");
                sb.append("<meta http-equiv='pragma' content='no-cache' />");
                sb.append("<meta charset='UTF-8'>");
                sb.append("<meta name='Generator' content='Cocoa HTML Writer'>");
                sb.append("<meta name='CocoaVersion' content='1504.81'>");
                sb.append("<meta name='viewport' content='width=device-width,height=device-height, initial-scale=1.0' />");
                sb.append(String.format("<script type='text/javascript' src='%s'>", jquery_path));
                sb.append("</script>");
                sb.append(String.format("<script type='text/javascript' src='%s'>", scorm_wrapper_path));
                sb.append("</script>");
                sb.append("<style type='text/css'>\n" +
                        " html,body,iframe {\n" +
                        "\theight: 100%;\n" +
                        "\twidth: 100%;\n" +
                        "\tmargin: 0;\n" +
                        "\tpadding: 0;\n" +
                        "\tmin-height :100%;\n" +
                        "}\n" +
                        "</style>\n");
                sb.append("</head>");
                sb.append("<body>");
                sb.append(String.format(" <iframe id='myFrame' frameborder='0' src='%s'>", "file://" + path + "?showExit=true"));
                sb.append("</iframe></body></html>");
                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                    }

                    @Override
                    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return super.shouldOverrideUrlLoading(view, url);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        dismissWaitDialog();
                        super.onPageFinished(view, url);
                    }
                });
                webView.loadDataWithBaseURL("", sb.toString(), "text/html", "utf-8", "");
            } catch (Exception ex) {
                Log.d("", ex.getMessage());
            }
        } else {
            dismissWaitDialog();
            AlertDialogManager.showDialog(CourseWebActivity.this, getResources().getString(R.string.file_not_found), getResources().getString(R.string.course_file_not_exist), false, null);
        }
    }

    public class JsHandler {
        @JavascriptInterface
        public void updateSCORMStatus(String paramater, String value) {
            if (!restartCompletionStatus.equals("completed")) {
                if (paramater.equals("cmi.location")) {
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "cmiLocation", value);
                } else if (paramater.equals("cmi.progress_measure")) {
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "cmiProgressMeasure", value);
                } else if (paramater.equals("cmi.completion_status")) {
                    DBUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "CompletionDate", getCurrentDate());
                    DBUpdate.updateTable("CoursesTable", spManager.getUserID(), LicenceID, "completionDate", getCurrentDate());
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "cmiCompletionStatus", value);
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "NewlyCompleted", "Yes");
                } else if (paramater.equals("cmi.success_status")) {
                    DBUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "CompletionDate", getCurrentDate());
                    DBUpdate.updateTable("CoursesTable", spManager.getUserID(), LicenceID, "completionDate", getCurrentDate());
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "cmiSuccessStatus", value);
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "NewlyCompleted", "Yes");
                } else if (paramater.equals("adl.nav.request")) {
                    DBUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "CompletionDate", getCurrentDate());
                    DBUpdate.updateTable("CoursesTable", spManager.getUserID(), LicenceID, "completionDate", getCurrentDate());
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "adlNavRequest", value);
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "NewlyCompleted", "Yes");
                    Intent intent = new Intent(CourseWebActivity.this, Courses.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                }
                if (DBSelect.getDataFromCoursesTable("IfNull(startDate,'')as courseStartDate", spManager.getUserID(), LicenceID).equals("")) {
                    if (DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "started").equals("undefined") || DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "started").equals("")) {
                        DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "started", courseStartedDate);
                    }
                } else {
                    DBUpdate.updateSCORMTable(spManager.getUserID(), LicenceID, "started", DBSelect.getDataFromCoursesTable("IfNull(startDate,'')as courseStartDate", spManager.getUserID(), LicenceID));
                }
            }
            if (paramater.equals("adl.nav.request") && value.equals("continue")) {
                onBackPressed();
            }
        }

        @JavascriptInterface
        public String getSCORMStatus(String parameter) {
            String Result = "";
            if (restartCompletionStatus.equals("completed")) {
                Result = "undefined";
            } else {
                if (parameter.equals("cmi.location")) {
                    Result = DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "cmiLocation");
                } else if (parameter.equals("cmi.progress_measure")) {
                    Result = DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "cmiProgressMeasure");
                } else if (parameter.equals("cmi.completion_status")) {
                    Result = DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "cmiCompletionStatus");
                } else if (parameter.equals("cmi.success_status")) {
                    Result = DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "cmiSuccessStatus");
                } else if (parameter.equals("adl.nav.request")) {
                    Result = DBSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), LicenceID, "adlNavRequest");
                }
            }
            return Result;
        }
    }

    public void showWaitDialog() {
        if (isActivityLive) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(CourseWebActivity.this);
            }
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
        }
    }

    public void dismissWaitDialog() {
        if (isActivityLive) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    @Override
    protected void onStop() {
        dismissWaitDialog();
        isActivityLive = false;
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
    }

    public String getCurrentDate() {
        String currentDate = "";
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            currentDate = df.format(c.getTime());
        } catch (Exception ex) {

        }
        return currentDate;
    }

    private final BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {

            } else if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            } else {
                if (NetworkMode.equals("Online")) {
                    AlertDialogManager.showDialog(CourseWebActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        }
    };
}
