package com.elearn.trainor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Telephony;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.CourseModule.Courses;
import com.elearn.trainor.Diploma.Diploma;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.MyCompany.DSBActivity;
import com.elearn.trainor.MyCompany.MessageAndDocumentActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import com.github.barteksc.pdfviewer.ScrollBar;

public class PDFView extends AppCompatActivity implements View.OnClickListener {
    public static PDFView instance;
    LinearLayout ll_back, llhome, ll_save;
    TextView text_header;
    ImageView shareIcon;
    private ProgressDialog pDialog;
    String FileURL, fromActivity, custID;
    WebView webView;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    com.github.barteksc.pdfviewer.PDFView pdfView;
    String fileName, file_Path;
    RelativeLayout tbl_actionbar;
    int actionBarBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activit_pdf_view);

        fileName = getIntent().getStringExtra("DocumentName");
        FileURL = getIntent().getStringExtra("PDFfileURL");
        fromActivity = getIntent().getStringExtra("FromAcitivity");
        custID = getIntent().getStringExtra("CustomerID");
        file_Path = getIntent().getStringExtra("FileName");
        getControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    public static PDFView getInstance() {
        if (instance == null) {
            instance = new PDFView();
        }
        return instance;
    }

    public void getControls() {
        connectionDetector = new ConnectionDetector(PDFView.this);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        pdfView = (com.github.barteksc.pdfviewer.PDFView) findViewById(R.id.pdfView);
        spManager = new SharedPreferenceManager(this);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_save = (LinearLayout) findViewById(R.id.ll_save);
        webView = (WebView) findViewById(R.id.webView);
        text_header = (TextView) findViewById(R.id.text_header);
        shareIcon = (ImageView) findViewById(R.id.share);
        if (fromActivity.equals("Documents") && fromActivity != null) {
            text_header.setText(fileName);
            actionBarBackground = getResources().getColor(R.color.my_company);
        } else if (fromActivity.equals("Diploma") && fromActivity != null) {
            text_header.setText(getString(R.string.diploma));
            actionBarBackground = getResources().getColor(R.color.diploma);
        } else if (fromActivity.equals("CourseActivity") && fromActivity != null) {
            text_header.setText(getResources().getString(R.string.courses));
            actionBarBackground = getResources().getColor(R.color.courses);
        }else if (fromActivity.equals("DSBActivity") && fromActivity != null) {
            text_header.setText(fileName);
            actionBarBackground = getResources().getColor(R.color.my_company);
        }
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tbl_actionbar.setBackgroundColor(actionBarBackground);
        llhome.setVisibility(View.GONE);
        shareIcon.setVisibility(View.VISIBLE);
        ll_save.setOnClickListener(this);
        ll_back.setOnClickListener(this);

        File filePath = new File(file_Path);
        if (filePath.exists()) {
            // pdfView.fromFile(filePath).enableSwipe(false).swipeVertical(true).load();
            pdfView.fromFile(filePath).enableAnnotationRendering(true).swipeHorizontal(false)
                    .enableDoubletap(true).load();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.ll_save:
                onShareClick();
                break;
        }
    }

    public void onShareClick() {
        List<Intent> targetShareIntents = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(shareIntent, 0);

        for (ResolveInfo resInfo : resolveInfoList) {
            String packageName = resInfo.activityInfo.packageName;
            if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                    || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.apps.plus")
                    || packageName.contains("com.google.android.talk") || packageName.contains("com.slack")
                    || packageName.contains("com.facebook.orca")
                    || packageName.contains("com.skype.raider")
                    || packageName.contains("com.linkedin.android")
                    ) {
            } else {
                Intent targetedOpenIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null))
                        .setPackage(resInfo.activityInfo.packageName)
                        .putExtra(Intent.EXTRA_EMAIL, "support@trainor.no")
                        .putExtra(Intent.EXTRA_SUBJECT, "")
                        .putExtra(Intent.EXTRA_TEXT, "");
                Uri uriPath = Uri.fromFile(new File(file_Path));
                targetedOpenIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
                targetShareIntents.add(targetedOpenIntent);
            }
        }

        String defaultSMSPackage = Telephony.Sms.getDefaultSmsPackage(this);
        Intent shareSMS = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", "", null));
        shareSMS.addCategory(Intent.CATEGORY_DEFAULT);
        shareSMS.putExtra("sms_body", FileURL);
        shareSMS.setPackage(defaultSMSPackage);
        targetShareIntents.add(shareSMS);
        Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Share via");
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Send request");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
        startActivity(chooserIntent);
    }

    @Override
    public void onBackPressed() {
        Intent intentback = null;

        if (fromActivity.equals("Documents") && fromActivity != null) {
            intentback = new Intent(PDFView.this, MessageAndDocumentActivity.class);
            intentback.putExtra("CustomerID", custID);
        } else if (fromActivity.equals("Diploma") && fromActivity != null) {
            intentback = new Intent(PDFView.this, Diploma.class);
        } else if (fromActivity.equals("CourseActivity") && fromActivity != null) {
            intentback = new Intent(PDFView.this, Courses.class);
        } else if (fromActivity.equals("DSBActivity") && fromActivity != null) {
            intentback = new Intent(PDFView.this, DSBActivity.class);
        }else{
            intentback = new Intent(PDFView.this, HomePage.class);
        }
        intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentback);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void showWaitDialog() {
        pDialog = new ProgressDialog(PDFView.this);
        pDialog.setMessage(getString(R.string.please_wait));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void dismissWaitDialog() {
        if (pDialog != null)
            if (pDialog.isShowing())
                pDialog.dismiss();
    }


}
