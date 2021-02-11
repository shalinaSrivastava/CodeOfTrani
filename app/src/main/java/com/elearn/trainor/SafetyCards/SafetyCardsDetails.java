package com.elearn.trainor.SafetyCards;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.Telephony;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SafetyCardsDetails extends AppCompatActivity implements View.OnClickListener {
    public static SafetyCardsDetails instance;
    LinearLayout ll_back, llhome, ll_save;
    TextView text_header;
    ImageView shareIcon;
    private ProgressDialog pDialog;
    String BaseURL;
    WebView webView;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    PDFView pdfView;
    String fileName, fromPage = "";
    Trace myTrace;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_cards_details);
        BaseURL = getIntent().getStringExtra("pdfFileURL");
        if (getIntent().getStringExtra("FROM") != null && !Objects.equals(getIntent().getStringExtra("FROM"), "")) {
            fromPage = getIntent().getStringExtra("FROM");
        }
        getControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        myTrace = FirebasePerformance.getInstance().newTrace("SafetyCard_trace");
        myTrace.start();
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //analytics.setCurrentScreen(this, "SafetyCardDetails", this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        myTrace.stop();
    }

    public static SafetyCardsDetails getInstance() {
        if (instance == null) {
            instance = new SafetyCardsDetails();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("SafetyCardView", "Yes");
        analytics.logEvent("SafetyCardView", bundle);
        connectionDetector = new ConnectionDetector(SafetyCardsDetails.this);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        spManager = new SharedPreferenceManager(this);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_save = (LinearLayout) findViewById(R.id.ll_save);
        webView = (WebView) findViewById(R.id.webView);
        text_header = (TextView) findViewById(R.id.text_header);
        shareIcon = (ImageView) findViewById(R.id.share);
        text_header.setText(getResources().getString(R.string.saftey_cards));
        llhome.setVisibility(View.GONE);
        shareIcon.setVisibility(View.VISIBLE);
        ll_save.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        fileName = getIntent().getStringExtra("FileName");
        File filePath = new File(fileName);
        if (filePath.exists()) {
           // pdfView.fromFile(filePath).enableSwipe(false).swipeVertical(true).load();
            pdfView.fromFile(filePath).enableAnnotationRendering(true).swipeHorizontal(false)
                    .enableDoubletap(true).load();
           /* pdfView.fromFile(filePath)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(false)
                    .defaultPage(0)
                    // allows to draw something on the current page, usually visible in the middle of the screen
                    .onTap(new OnTapListener() {
                        @Override
                        public boolean onTap(MotionEvent e) {
                            return false;
                        }
                    })
                    .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    // spacing between pages in dp. To define spacing color, set view background
                    .spacing(0)
                    .load();*/
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
                        .putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.saftey_cards))
                        .putExtra(Intent.EXTRA_TEXT, "");
                Uri uriPath = Uri.fromFile(new File(fileName));
                targetedOpenIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
                targetShareIntents.add(targetedOpenIntent);
                /*
                Intent targetedOpenIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
                targetedOpenIntent.setType("application/pdf");
                targetedOpenIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"abc@gmail.com"});
                targetedOpenIntent.putExtra(Intent.EXTRA_SUBJECT, "Safety Card");
                //targetedOpenIntent.putExtra(Intent.EXTRA_TEXT, "test");
                Uri uriPath = Uri.fromFile(new File(fileName));
                targetedOpenIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
                targetShareIntents.add(targetedOpenIntent);*/
            }
        }

        String defaultSMSPackage = Telephony.Sms.getDefaultSmsPackage(this);
        Intent shareSMS = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", "", null));
        shareSMS.addCategory(Intent.CATEGORY_DEFAULT);
        shareSMS.putExtra("sms_body", BaseURL);
        shareSMS.setPackage(defaultSMSPackage);
        targetShareIntents.add(shareSMS);
        Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Share via");
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Send request");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
        startActivity(chooserIntent);
    }

    @Override
    public void onBackPressed() {
        if(fromPage.equals("CheckedInFacility")){
            Intent intentback = new Intent(SafetyCardsDetails.this, CheckedInFacility.class);
            intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentback);
            finish();
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }else{
            Intent intentback = new Intent(SafetyCardsDetails.this, SafetyCards.class);
            intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentback);
            finish();
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }

    }

    public void showWaitDialog() {
        pDialog = new ProgressDialog(SafetyCardsDetails.this);
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