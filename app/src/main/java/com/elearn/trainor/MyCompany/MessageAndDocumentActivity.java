package com.elearn.trainor.MyCompany;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.WindowManager;
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
import com.elearn.trainor.BaseAdapters.MyCmpnyDocumentRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PDFView;
import com.elearn.trainor.PropertyClasses.DownloadUrlProperty;
import com.elearn.trainor.PropertyClasses.MyCompanyProperty;
import com.elearn.trainor.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.services.network.HttpRequest;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MessageAndDocumentActivity extends AppCompatActivity implements View.OnClickListener {
    public static MessageAndDocumentActivity instance;
    TextView text_header;
    LinearLayout ll_back, llhome;
    RelativeLayout tbl_actionbar;
    String CustomerID = "", companyName = "", BackKey = "", Locale = "";
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    DataBaseHandlerUpdate dbUpdate;
    List<MyCompanyProperty> myCompanyPropertyList;
    RecyclerView recyclerViewDocument;
    MyCmpnyDocumentRecyclerViewAdapter myCmpnyDocumentRecyclerViewAdapter;
    ProgressDialog pDialog;
    MyCompanyProperty myCompanyProperty;
    String documentPDF_FileName = "", documentPDF_URL = "", documentSwitch_Status, NetworkMode = "", userID = "", documentIDsToBeDeleted = "";
    SwipeRefreshLayout swipelayout;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    TextView tv_no_document;
    List<DownloadUrlProperty> downloadDocumentUrlList = new ArrayList<>(); // new list for offline download
    boolean isWindowActive = false, showBadFormatDialog;
    Long freeSpaceMB;
    IntentFilter internet_intent_filter;
    Dialog syncIncompleteDialog;
    public static int totalURLIndex = 0;
    Trace myTrace;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meaasge_documents);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getControls();
        instance = this;
        isWindowActive = true;
        CustomerID = MessageAndDocumentActivity.getInstance().CustomerID;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //analytics.setCurrentScreen(this, "CompanyBasedDocuments", this.getClass().getSimpleName());
    }

    public static MessageAndDocumentActivity getInstance() {
        if (instance == null) {
            instance = new MessageAndDocumentActivity();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        CustomerID = getIntent().getStringExtra("CustomerID");
        analytics = FirebaseAnalytics.getInstance(this);
        myTrace = FirebasePerformance.getInstance().newTrace("Document_trace");
        myTrace.start();
        companyName = getIntent().getStringExtra("CompanyName");
        BackKey = getIntent().getStringExtra("BackKey");
        Locale = getIntent().getStringExtra("Locale");
        Locale = Locale == null ? "" : Locale;
        int actionBarBackground = getResources().getColor(R.color.my_company);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(actionBarBackground);
        text_header = (TextView) findViewById(R.id.text_header);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(this);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setOnClickListener(this);
        text_header.setText(companyName);
        dbSelect = new DataBaseHandlerSelect(this);
        dbInsert = new DataBaseHandlerInsert(this);
        dbUpdate = new DataBaseHandlerUpdate(this);
        dataBaseHandlerDelete = new DataBaseHandlerDelete(this);
        spManager = new SharedPreferenceManager(this);
        documentSwitch_Status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DocumentSwitchStatus", " ");
        connectionDetector = new ConnectionDetector(this);
        userID = spManager.getUserID();
        tv_no_document = (TextView) findViewById(R.id.tv_no_document);
        myCompanyPropertyList = new ArrayList<>();
        recyclerViewDocument = (RecyclerView) findViewById(R.id.recyclerViewDocument);
        recyclerViewDocument.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDocument.setNestedScrollingEnabled(false);
        if (!connectionDetector.isConnectingToInternet()) {
            myCompanyPropertyList.clear();
            myCompanyPropertyList = dbSelect.getMyCompanyContent(CustomerID, userID, Locale);
            if (myCompanyPropertyList.size() > 0) {
                myCmpnyDocumentRecyclerViewAdapter = new MyCmpnyDocumentRecyclerViewAdapter(this, myCompanyPropertyList, "Documents");
                recyclerViewDocument.setAdapter(myCmpnyDocumentRecyclerViewAdapter);
                myCmpnyDocumentRecyclerViewAdapter.notifyDataSetChanged();
                dismissWaitDialog();
            } else {
                tv_no_document.setVisibility(View.VISIBLE);
                dismissWaitDialog();
            }
        }
        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        freeSpaceMB = availableSpace / SIZE_MB;
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipelayout.setColorSchemeResources(R.color.my_company);
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    getMyCompanyDetails(spManager.getUserID(), CustomerID);
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(MessageAndDocumentActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MessageAndDocumentActivityPermissionsDispatcher.onRequestPermissionsResult(MessageAndDocumentActivity.this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadPDF_File(showBadFormatDialog);
                } else {
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(MessageAndDocumentActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(MessageAndDocumentActivity.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", MessageAndDocumentActivity.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                            }
                        }, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    } else {
                        dismissWaitDialog();
                        AlertDialogManager.showCustomDialog(this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                                ActivityCompat.requestPermissions(MessageAndDocumentActivity.this, new String[]{permission}, requestCode);
                            }
                        }, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                            }
                        }, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                        dismissWaitDialog();
                    }
                }
                break;
        }
    }

    public void startDownloadingWithPermission(MyCompanyProperty myCompanyProperty, String fileURL, String documentPDF_FileName, boolean showBadFormatDialog) {
        this.myCompanyProperty = myCompanyProperty;
        this.documentPDF_FileName = documentPDF_FileName;
        showWaitDialog();
        this.documentPDF_URL = fileURL;
        this.showBadFormatDialog = showBadFormatDialog;
        MessageAndDocumentActivityPermissionsDispatcher.startDownloadingWithPermissionCheck(MessageAndDocumentActivity.this, showBadFormatDialog);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startDownloading(boolean showDialog) {
        downloadPDF_File(showDialog);
    }

    public void showDocumentPDF_File(final File file, final String filename) {
        Bundle bundle = new Bundle();
        bundle.putString("DocumentView", "Yes");
        analytics.logEvent("DocumentView", bundle);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismissWaitDialog();
                if (!file.exists()) {
                    AlertDialogManager.showDialog(MessageAndDocumentActivity.this, getResources().getString(R.string.bad_file_format), getResources().getString(R.string.try_after_some_time), false, null);
                } else {
                    try {
                        String fileEtension = filename.substring(filename.lastIndexOf("."), filename.length());
                        fileEtension = fileEtension.toLowerCase();
                        if (Build.MANUFACTURER.equals("Pixavi")) {
                            Intent intentPixavi = null;
                            if (fileEtension.toString().contains(".pdf")) {
                                intentPixavi = new Intent(MessageAndDocumentActivity.this, PDFView.class);
                                intentPixavi.putExtra("FileName", file.toString());
                                intentPixavi.putExtra("CustomerID", CustomerID);
                                intentPixavi.putExtra("PDFfileURL", documentPDF_URL);
                                intentPixavi.putExtra("DocumentName", documentPDF_FileName);
                                intentPixavi.putExtra("FromAcitivity", "Documents");
                                intentPixavi.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intentPixavi);
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            } else if (fileEtension.toString().contains(".doc") || fileEtension.toString().contains(".docx")) {
                                // Word document
                                intentPixavi.setDataAndType(Uri.fromFile(file), "application/msword");
                            } else if (fileEtension.toString().contains(".pdf")) {
                                // Powerpoint file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "application/pdf");
                            } else if (fileEtension.toString().contains(".ppt") || fileEtension.toString().contains(".pptx")) {
                                // Powerpoint file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "application/vnd.ms-powerpoint");
                            } else if (fileEtension.toString().contains(".xls") || fileEtension.toString().contains(".xlsx")) {
                                // Excel file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
                            } else if (fileEtension.toString().contains(".zip") || fileEtension.toString().contains(".rar")) {
                                // ZIP Files
                                intentPixavi.setDataAndType(Uri.fromFile(file), "application/zip");
                            } else if (fileEtension.toString().contains(".rtf")) {
                                // RTF file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "application/rtf");
                            } else if (fileEtension.toString().contains(".wav") || fileEtension.toString().contains(".mp3")) {
                                // WAV audio file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "audio/x-wav");
                            } else if (fileEtension.toString().contains(".gif")) {
                                // GIF file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "image/gif");
                            } else if (fileEtension.toString().contains(".jpg") || fileEtension.toString().contains(".jpeg") || fileEtension.toString().contains(".png")) {
                                // JPG file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "image/jpeg");
                            } else if (fileEtension.toString().contains(".txt")) {
                                // Text file
                                intentPixavi.setDataAndType(Uri.fromFile(file), "text/plain");
                            } else if (fileEtension.toString().contains(".3gp") || fileEtension.toString().contains(".mpg") || fileEtension.toString().contains(".mpeg") || fileEtension.toString().contains(".mpe") || fileEtension.toString().contains(".mp4") || filename.toString().contains(".avi")) {
                                // Video files
                                intentPixavi.setDataAndType(Uri.fromFile(file), "video");
                            } else {
                                intentPixavi.setDataAndType(Uri.fromFile(file), "*/*");
                            }
                            dismissWaitDialog();
                            intentPixavi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentPixavi);
                        } else {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                            Uri uri = FileProvider.getUriForFile(MessageAndDocumentActivity.this,
                                    getString(R.string.file_provider_authority),
                                    file);
                            if (fileEtension.toString().contains(".doc") || fileEtension.toString().contains(".docx")) {
                                // Word document
                                intent.setDataAndType(uri, "application/msword");
                            } else if (fileEtension.toString().contains(".pdf")) {
                                // Portable Document File
                                intent.setDataAndType(uri, "application/pdf");
                            } else if (fileEtension.toString().contains(".ppt") || fileEtension.toString().contains(".pptx")) {
                                // Powerpoint file
                                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                            } else if (fileEtension.toString().contains(".xls") || fileEtension.toString().contains(".xlsx")) {
                                // Excel file
                                intent.setDataAndType(uri, "application/vnd.ms-excel");
                            } else if (fileEtension.toString().contains(".zip") || fileEtension.toString().contains(".rar")) {
                                // ZIP Files
                                intent.setDataAndType(uri, "application/zip");
                            } else if (fileEtension.toString().contains(".rtf")) {
                                // RTF file
                                intent.setDataAndType(uri, "application/rtf");
                            } else if (fileEtension.toString().contains(".wav") || fileEtension.toString().contains(".mp3")) {
                                // WAV audio file
                                intent.setDataAndType(uri, "audio/x-wav");
                            } else if (fileEtension.toString().contains(".gif")) {
                                // GIF file
                                intent.setDataAndType(uri, "image/gif");
                            } else if (fileEtension.toString().contains(".jpg") || fileEtension.toString().contains(".jpeg") || fileEtension.toString().contains(".png")) {
                                // JPG file
                                intent.setDataAndType(uri, "image/jpeg");
                            } else if (fileEtension.toString().contains(".txt")) {
                                // Text file
                                intent.setDataAndType(uri, "text/plain");
                            } else if (fileEtension.toString().contains(".3gp") || fileEtension.toString().contains(".mpg") || fileEtension.toString().contains(".mpeg") || fileEtension.toString().contains(".mpe") || fileEtension.toString().contains(".mp4") || filename.toString().contains(".avi")) {
                                // Video files
                                intent.setDataAndType(uri, "video");
                            } else {
                                intent.setDataAndType(uri, "*/*");
                            }
                            dismissWaitDialog();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MessageAndDocumentActivity.this, getResources().getString(R.string.no_app_to_handle_activity) + " " + filename, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.execute();
    }

    public void downloadPDFFromServer(final String fileURL, final String fileName, final boolean showDialog) {
        final Boolean[] requestError = {false};
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    String token = spManager.getToken();
                    request.authorization("Bearer " + token);
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, fileName);
                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        requestError[0] = true;
                        DeleteFile(file);
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
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
                if (documentSwitch_Status.equals("ON")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        swipelayout.setRefreshing(false);
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
                        swipelayout.setRefreshing(false);
                        dismissWaitDialog();
                        if (syncIncompleteDialog == null) {
                            syncIncompleteDialog = AlertDialogManager.showDialog(MessageAndDocumentActivity.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.sync_incomplete), false, new IClickListener() {
                                @Override
                                public void onClick() {
                                    syncIncompleteDialog = null;
                                    myCompanyPropertyList.clear();
                                    myCompanyPropertyList = dbSelect.getMyCompanyContent(CustomerID, userID, Locale);
                                    if (myCompanyPropertyList.size() > 0) {
                                        myCmpnyDocumentRecyclerViewAdapter = new MyCmpnyDocumentRecyclerViewAdapter(MessageAndDocumentActivity.this, myCompanyPropertyList, "Documents");
                                        recyclerViewDocument.setAdapter(myCmpnyDocumentRecyclerViewAdapter);
                                        myCmpnyDocumentRecyclerViewAdapter.notifyDataSetChanged();
                                        dismissWaitDialog();
                                    } else {
                                        tv_no_document.setVisibility(View.VISIBLE);
                                        dismissWaitDialog();
                                    }
                                }
                            });
                        }
                    }
                    if (showDialog && requestError[0]) {
                        dismissWaitDialog();
                        showBadFormatDialog = false;
                        AlertDialogManager.showDialog(MessageAndDocumentActivity.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_not_responding), false, null);
                    }
                } else {
                    if (showDialog && requestError[0]) {
                        dismissWaitDialog();
                        showBadFormatDialog = false;
                        AlertDialogManager.showDialog(MessageAndDocumentActivity.this, getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_not_responding), false, null);
                    } else {
                        File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/" + fileName);
                        showDocumentPDF_File(file, fileName);
                    }
                }
            }
        }.execute();
    }

    public void downloadPDF_File(boolean shoDialog) {
        if (connectionDetector.isConnectingToInternet()) {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/" + documentPDF_FileName);
            if (file.exists()) {
                dismissWaitDialog();
                showDocumentPDF_File(file, documentPDF_FileName);
            } else {
                downloadPDFFromServer(documentPDF_URL, documentPDF_FileName, shoDialog);
            }
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/" + documentPDF_FileName);
            if (file.exists()) {
                dismissWaitDialog();
                showDocumentPDF_File(file, documentPDF_FileName);
            } else {
                AlertDialogManager.showDialog(this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        dismissWaitDialog();
                    }
                });
            }
        }
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(this);
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

    public void getMyCompanyDetails(final String userID, final String customerID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.My_Company_Details + customerID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            downloadDocumentUrlList.clear();
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
                                if (myCompanyProperty.locale.equals("")) {
                                    myCompanyProperty.locale = "nb_NO";
                                }
                                DownloadUrlProperty downloadUrlProperty = new DownloadUrlProperty();
                                downloadUrlProperty.downloadURL = myCompanyProperty.downloadUrl;
                                downloadUrlProperty.safetyCard_cardID = myCompanyProperty.fileName;
                                downloadDocumentUrlList.add(downloadUrlProperty);
                                double fileSize = Long.parseLong(jsonObject.getString("fileSize") == null ? "0" : jsonObject.getString("fileSize").equals("null") ? "0" : jsonObject.getString("fileSize"));
                                String _fileSize = String.valueOf(fileSize / 1048576);
                                double result_fileSize = Double.parseDouble(_fileSize);
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
                                        dbInsert.addDataIntoMyCompanyTable(myCompanyProperty);
                                    }
                                } else {
                                    if (!DbLastModified.equals(myCompanyProperty.lastModified)) {
                                        dbUpdate.updateLastModifiedDocumentData(userID, myCompanyProperty);
                                        File rootDir = android.os.Environment.getExternalStorageDirectory();
                                        File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/");
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
                        }
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                } finally {
                    dataBaseHandlerDelete.deleteMyCompanyIDsNotExists(documentIDsToBeDeleted, CustomerID);
                    documentIDsToBeDeleted = "";
                    myCompanyPropertyList.clear();
                    myCompanyPropertyList = dbSelect.getMyCompanyContent(customerID, userID, Locale);
                    if (myCompanyPropertyList.size() > 0) {
                        myCmpnyDocumentRecyclerViewAdapter = new MyCmpnyDocumentRecyclerViewAdapter(MessageAndDocumentActivity.this, myCompanyPropertyList, "Documents");
                        recyclerViewDocument.setAdapter(myCmpnyDocumentRecyclerViewAdapter);
                        myCmpnyDocumentRecyclerViewAdapter.notifyDataSetChanged();
                    } else {
                        tv_no_document.setVisibility(View.VISIBLE);
                    }
                    if (connectionDetector.isConnectingToInternet() && documentSwitch_Status.equals("ON")) {
                        totalURLIndex = downloadDocumentUrlList.size();
                        pDialog.setMessage(getResources().getString(R.string.storing) + " " + getResources().getString(R.string.storingDocuments));
                        only_downloadFile();
                    } else {
                        dismissWaitDialog();
                        swipelayout.setRefreshing(false);
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(MessageAndDocumentActivity.this);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public void only_downloadFile() {
        if (connectionDetector.isConnectingToInternet()) {
            if (downloadDocumentUrlList != null && downloadDocumentUrlList.size() > 0) {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/" + downloadDocumentUrlList.get(0).safetyCard_cardID);
                if (!file.exists()) {
                    if (50 <= freeSpaceMB) {
                        downloadPDFFromServer(downloadDocumentUrlList.get(0).downloadURL, downloadDocumentUrlList.get(0).safetyCard_cardID, showBadFormatDialog);
                    } else {
                        swipelayout.setRefreshing(false);
                        dismissWaitDialog();
                        AlertDialogManager.showDialog(this, getString(R.string.not_enough_space), " ", false, null);
                    }
                } else {
                    downloadDocumentUrlList.remove(downloadDocumentUrlList.get(0));
                    if (downloadDocumentUrlList.size() > 0) {
                        only_downloadFile();
                    } else {
                        dismissWaitDialog();
                        swipelayout.setRefreshing(false);
                        myCompanyPropertyList.clear();
                        myCompanyPropertyList = dbSelect.getMyCompanyContent(CustomerID, userID, Locale);
                        if (myCompanyPropertyList.size() > 0) {
                            myCmpnyDocumentRecyclerViewAdapter = new MyCmpnyDocumentRecyclerViewAdapter(this, myCompanyPropertyList, "Documents");
                            recyclerViewDocument.setAdapter(myCmpnyDocumentRecyclerViewAdapter);
                            myCmpnyDocumentRecyclerViewAdapter.notifyDataSetChanged();
                        } else {
                            tv_no_document.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } else {
                myCompanyPropertyList.clear();
                myCompanyPropertyList = dbSelect.getMyCompanyContent(CustomerID, userID, Locale);
                if (myCompanyPropertyList.size() > 0) {
                    myCmpnyDocumentRecyclerViewAdapter = new MyCmpnyDocumentRecyclerViewAdapter(this, myCompanyPropertyList, "Documents");
                    recyclerViewDocument.setAdapter(myCmpnyDocumentRecyclerViewAdapter);
                    myCmpnyDocumentRecyclerViewAdapter.notifyDataSetChanged();
                    dismissWaitDialog();
                } else {
                    tv_no_document.setVisibility(View.VISIBLE);
                    dismissWaitDialog();
                }
            }
        } else {
            dismissWaitDialog();
            swipelayout.setRefreshing(false);
            AlertDialogManager.showDialog(this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.sync_incomplete), false, null);
        }
    }

    void DeleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteFile(child);
            }
        }
        fileOrDirectory.delete();
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
                getMyCompanyDetails(userID, CustomerID);
            } else {
                dismissWaitDialog();
                if (NetworkMode.equals("Online")) {
                    AlertDialogManager.showDialog(MessageAndDocumentActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class,"Home");
                break;
        }
    }

    public void commonIntentMethod(Class activity,String from) {
        Intent intent = null;
        if(from.equals("back")){
            if (BackKey != null && BackKey.equals("LocalePage")) {
                intent = new Intent(MessageAndDocumentActivity.this, DocumentLocaleActivity.class);
                intent.putExtra("CustomerID", CustomerID);
                intent.putExtra("CompanyName", companyName);
            } else {
                intent = new Intent(MessageAndDocumentActivity.this, activity);
            }
        }else{
            intent = new Intent(MessageAndDocumentActivity.this, activity);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        this.finishAffinity();
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(CompanyList.class,"back");
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(internetInfoReceiver);
        } catch (Exception ex) {
        }
    }
}