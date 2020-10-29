package com.elearn.trainor.MyCompany;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.BaseAdapters.DSBGridViewAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PDFView;
import com.elearn.trainor.PropertyClasses.DSBProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.services.network.HttpRequest;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DSBActivity extends AppCompatActivity implements View.OnClickListener {
    public static DSBActivity instance;
    SwipeRefreshLayout swipelayout;
    TextView text_header;
    LinearLayout ll_back, llhome;
    GridView DSBgridView;
    List<DSBProperty> DSBList;
    RelativeLayout tbl_actionbar;
    DSBGridViewAdapter dsbGridViewAdapter;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    ProgressDialog pDialog;
    String URL = "", DSB_FileName = "";
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsb);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        instance = this;
        spManager = new SharedPreferenceManager(DSBActivity.this);
        String sharedLanguage = spManager.getLanguage();
        setLocale(sharedLanguage);
        getControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    public static DSBActivity getInstance() {
        if (instance == null) {
            instance = new DSBActivity();
        }
        return instance;
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "DSBDocuments", this.getClass().getSimpleName());
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        DSBList = new ArrayList<>();
        dbDelete = new DataBaseHandlerDelete(this);
        dbInsert = new DataBaseHandlerInsert(this);
        dbSelect = new DataBaseHandlerSelect(this);
        connectionDetector = new ConnectionDetector(DSBActivity.this);
        int actionBarBackground = getResources().getColor(R.color.my_company);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(actionBarBackground);
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        DSBgridView = (GridView) findViewById(R.id.dsb_gridView);
        swipelayout.setColorSchemeResources(R.color.my_company);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText("Nyhetsbladet Elsikkerhet");
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(this);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setOnClickListener(this);
        if (connectionDetector.isConnectingToInternet()) {
            getDSBDetails();
        } else {
            DSBList = dbSelect.getDSBDataFromTable();
            if (DSBList.size() > 0) {
                dsbGridViewAdapter = new DSBGridViewAdapter(DSBActivity.this, DSBList);
                DSBgridView.setAdapter(dsbGridViewAdapter);
                dsbGridViewAdapter.notifyDataSetChanged();
            } else {
                AlertDialogManager.showDialog(DSBActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
            }
        }

        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    getDSBDetails();
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(DSBActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
    }

    public void getDSBDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.DSBMagzineURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DSBList.clear();
                try {
                    if (response != null && !response.equals("")) {
                        dbDelete.deleteTableByName("DSBTable", "");
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                DSBProperty property = new DSBProperty();
                                property.id = jsonObject.getString("id") == null ? "" : jsonObject.getString("id").equals("null") ? "" : jsonObject.getString("id");
                                property.last_modified = jsonObject.getString("last_modified") == null ? "" : jsonObject.getString("last_modified").equals("null") ? "" : jsonObject.getString("last_modified");
                                String filename = jsonObject.getString("name") == null ? "" : jsonObject.getString("name").equals("null") ? "" : jsonObject.getString("name");
                                String convertedFileName = filename.replaceAll("[-+.^:,]", " ");
                                property.name = convertedFileName;
                                property.release_date = jsonObject.getString("release_date") == null ? "" : jsonObject.getString("release_date").equals("null") ? "" : jsonObject.getString("release_date");
                                property.imageURL = jsonObject.getString("image") == null ? "" : jsonObject.getString("image").equals("null") ? "" : jsonObject.getString("image");
                                property.fileURL = jsonObject.getString("file") == null ? "" : jsonObject.getString("file").equals("null") ? "" : jsonObject.getString("file");
                                property.file_size = jsonObject.getString("file_size") == null ? "" : jsonObject.getString("file_size").equals("null") ? "" : jsonObject.getString("file_size");
                                property.order = jsonObject.getString("order") == null ? "" : jsonObject.getString("order").equals("null") ? "" : jsonObject.getString("order");
                                dbInsert.addDataIntoDSBTable(property);
                                DSBList.add(property);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    swipelayout.setRefreshing(false);
                    dsbGridViewAdapter = new DSBGridViewAdapter(DSBActivity.this, DSBList);
                    DSBgridView.setAdapter(dsbGridViewAdapter);
                    dsbGridViewAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialogManager.showDialog(DSBActivity.this, getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(DSBActivity.this, error), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(DSBActivity.this, CompanyList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }) {
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(DSBActivity.this);
        requestQueue11.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class);
                break;
        }
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(DSBActivity.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        this.finishAffinity();
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(CompanyList.class);
    }

    public void setLocale(String language) {
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DSBActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWaitDialog();
                    downloadPDF_File();
                } else {
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(DSBActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(DSBActivity.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", DSBActivity.this.getPackageName(), null);
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
                        AlertDialogManager.showCustomDialog(DSBActivity.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                dismissWaitDialog();
                                ActivityCompat.requestPermissions(DSBActivity.this, new String[]{permission}, requestCode);
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

    public void startDownloadingWithPermission(String URL, String DSB_FileName) {
        this.URL = URL;
        this.DSB_FileName = DSB_FileName;
        DSBActivityPermissionsDispatcher.startDownloadingWithPermissionCheck(DSBActivity.this);
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startDownloading() {
        showWaitDialog();
        downloadPDF_File();
    }

    public void showDSB_File(File file) {
        Bundle bundle = new Bundle();
        bundle.putString("DsbView", "Yes");
        analytics.logEvent("DsbView", bundle);
        dismissWaitDialog();
        if (!file.exists()) {
            AlertDialogManager.showDialog(DSBActivity.this, getResources().getString(R.string.bad_file_format), "", false, null);
        } else {
            if (Build.MANUFACTURER.equals("Pixavi")) {
                Intent pdfViewIntent = new Intent(DSBActivity.this, PDFView.class);
                pdfViewIntent.putExtra("FileName", file.toString());
                pdfViewIntent.putExtra("DocumentName", DSB_FileName);
                pdfViewIntent.putExtra("FromAcitivity", "DSBActivity");
                pdfViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(pdfViewIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                DSBActivity.this.finishAffinity();
                try {
                    dismissWaitDialog();
                    startActivity(pdfViewIntent);
                } catch (Exception e) {
                    dismissWaitDialog();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                Uri uri = FileProvider.getUriForFile(DSBActivity.this,
                        getString(R.string.file_provider_authority),
                        file);
                intent.setDataAndType(uri, "application/pdf");
                try {
                    dismissWaitDialog();
                    startActivity(intent);
                } catch (Exception e) {
                    dismissWaitDialog();
                }
            }
        }
    }


    public void downloadDSBFromServer(final String fileURL, final String fileName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    request.accept("application/pdf");
                    request.contentType("application/pdf");
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor" + "/.DSB/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, fileName + ".pdf");
                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        DeleteFile(file);
                    }
                } catch (Exception ex) {
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/MyTrainor" + "/.DSB/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    File file = new File(dir, fileName + ".pdf");
                    DeleteFile(file);
                    dismissWaitDialog();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor" + "/.DSB/" + fileName + ".pdf");
                if (file.exists()) {
                    showDownloadedPDFFile(file);
                }
            }
        }.execute();
    }

    public void downloadPDF_File() {
        writeNoMediaFile();
        if (connectionDetector.isConnectingToInternet()) {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor" + "/.DSB/" + DSB_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDSB_File(file);
            } else {
                downloadDSBFromServer(URL, DSB_FileName);
            }
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyTrainor" + "/.DSB/" + DSB_FileName + ".pdf");
            if (file.exists()) {
                dismissWaitDialog();
                showDSB_File(file);
            } else {
                AlertDialogManager.showDialog(DSBActivity.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        dismissWaitDialog();
                    }
                });
            }
        }
    }

    public void writeNoMediaFile() {
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/MyTrainor" + "/.DSB/");
            if (!root.exists()) {
                root.mkdirs();
            }
            String filePath = root.getAbsolutePath();
            File file = new File(filePath, ".nomedia");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
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

    public void showDownloadedPDFFile(final File pdfFile) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismissWaitDialog();
                showDSB_File(pdfFile);
            }
        }.execute();
    }

    public void showWaitDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(DSBActivity.this);
        }
        if (!pDialog.isShowing()) {
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 146) {
            if (data != null) {
                Toast.makeText(DSBActivity.this, "Result " + resultCode, Toast.LENGTH_SHORT).show();
            }
        }
    }
}


