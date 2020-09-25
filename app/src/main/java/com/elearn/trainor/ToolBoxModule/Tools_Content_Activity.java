package com.elearn.trainor.ToolBoxModule;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;

import com.elearn.trainor.DBHandler.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Tools_Content_Activity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout ll_back, llhome;
    RelativeLayout tbl_actionbar;
    TextView text_header;
    WebView htmlWebView;
    DataBaseHandlerUpdate dbUpdate;
    private ProgressDialog pDialog;
    boolean isActivityLive = false;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools__content_);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        isActivityLive = true;
        GetControls();
        unzipMethod();
    }

    @Override
    protected void onStart() {
        isActivityLive = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        isActivityLive = true;
        super.onResume();
        analytics.setCurrentScreen(this, "ToolsDisplay", this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        dismissWaitDialog();
        isActivityLive = false;
        super.onStop();
    }

    public void GetControls() {
        analytics = FirebaseAnalytics.getInstance(Tools_Content_Activity.this);
        dbUpdate = new DataBaseHandlerUpdate(Tools_Content_Activity.this);
        htmlWebView = (WebView) findViewById(R.id.webView);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        text_header = (TextView) findViewById(R.id.text_header);
        text_header.setText(getIntent().getStringExtra("ZipFile").toString());
        tbl_actionbar.setBackgroundColor(Color.parseColor(getIntent().getStringExtra("BGColor")));
        if (getIntent().getStringExtra("From") != null && !getIntent().getStringExtra("From").equals("HomePage")) {
            llhome.setVisibility(View.INVISIBLE);
        }
        llhome.setOnClickListener(this);
        ll_back.setOnClickListener(this);
    }

    public void unzip(final File file, final String destinationPath) {
        showWaitDialog();
        try {
            final ZipFile zipFile = new ZipFile(file);
            final List fileHeaderList = zipFile.getFileHeaders();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        ZipInputStream is = null;
                        OutputStream os = null;
                        for (int i = 0; i < fileHeaderList.size(); i++) {
                            FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                            if (fileHeader.getFileName().contains("__MACOSX")) {
                                String outFilePath = destinationPath + fileHeader.getFileName();
                                File file1 = new File(outFilePath);
                                file1.delete();
                            } else {
                                if (fileHeader != null) {
                                    String outFilePath = destinationPath + fileHeader.getFileName();
                                    File outFile = new File(outFilePath);
                                    if (fileHeader.isDirectory()) {
                                        outFile.mkdirs();
                                        continue;
                                    }
                                    File parentDir = outFile.getParentFile();
                                    if (!parentDir.exists()) {
                                        parentDir.mkdirs();
                                    }
                                    is = zipFile.getInputStream(fileHeader);
                                    os = new FileOutputStream(outFile);
                                    int readLen = -1;
                                    byte[] buff = new byte[Integer.parseInt(fileHeader.getUncompressedSize() + "")];
                                    while ((readLen = is.read(buff)) != -1) {
                                        os.write(buff, 0, readLen);
                                    }
                                    closeFileHandlers(is, os);
                                    UnzipUtil.applyFileAttributes(fileHeader, outFile);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.d("Error", ex.getMessage());
                        dismissWaitDialog();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        for (int i = 0; i < fileHeaderList.size(); i++) {
                            FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                            if (fileHeader.getFileName().contains("index") && fileHeader.getFileName().contains(".html")) {
                                String path = destinationPath + fileHeader.getFileName();
                                File fileToShow = new File(path);
                                if (fileToShow.exists()) {
                                    WebView htmlWebView = (WebView) findViewById(R.id.webView);
                                    openToolOnWebView(htmlWebView, path);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        dismissWaitDialog();
                    }
                }
            }.execute();
        } catch (Exception ex) {
            dismissWaitDialog();
        }
    }

    private void closeFileHandlers(ZipInputStream is, OutputStream os) throws IOException {
        if (os != null) {
            os.close();
        }
        if (is != null) {
            is.close();
        }
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(ToolBox.class);
    }

    public void unzipMethod() {
        File rootDir = android.os.Environment.getExternalStorageDirectory();
        File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/");
        String filePath = root.getAbsolutePath() + "/" + getIntent().getStringExtra("ZipFile");
        File file = new File(filePath);
        if (file.exists()) {
            unzip(file, root.getAbsolutePath() + "/" + "UnZipped/" + getIntent().getStringExtra("ZipFile") + "/");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                commonIntentMethod(ToolBox.class);
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class);
                break;
        }
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(Tools_Content_Activity.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("From", getIntent().getStringExtra("From"));
        startActivity(intent);
        this.finishAffinity();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void showWaitDialog() {
        if (isActivityLive) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(Tools_Content_Activity.this);
            }
            pDialog.setMessage(getString(R.string.please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    public void dismissWaitDialog() {
        if (isActivityLive) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    public void openToolOnWebView(WebView webView, String videoPath) {
        //Start Downloaded Tools View Analytics
        Bundle bundle = new Bundle();
        bundle.putString("ToolsView", "Yes");
        analytics.logEvent("ToolsView", bundle);
        //End Downloaded Tools View Analytics

        File fileToShow = new File(videoPath);
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
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head>");
                sb.append("<Title>Please wait...</Title>");
                sb.append("<meta name='viewport' content='width=device-width,height=device-height,initial-scale=1.0'/>");
                sb.append("<meta http-equiv='pragma' content='no-cache'/>");
                sb.append("<style type='text/css'>\n" +
                        " html,body,iframe {\n" +
                        "\toverflow: scroll;\n" +
                        "\theight: 104%;\n" +
                        "\twidth: 100%;\n" +
                        "\tmargin: 0;\n" +
                        "\tpadding: 0;\n" +
                        "\tmin-height :100%;\n" +
                        "}\n" +
                        "</style>\n");
                sb.append("</head>");
                sb.append("<body>");
                sb.append(String.format(" <iframe id='myFrame' frameborder='0' src='%s'>", "file://" + videoPath));
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
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        dismissWaitDialog();
                        super.onPageFinished(view, url);
                    }
                });
                webView.loadDataWithBaseURL("", sb.toString(), "text/html", "utf-8", "");
            } catch (Exception ex) {
                dismissWaitDialog();
                Log.d("", ex.getMessage());
            }
        } else {
            dismissWaitDialog();
            AlertDialogManager.showDialog(Tools_Content_Activity.this, getResources().getString(R.string.file_not_found), "", false, null);
        }
    }
}
