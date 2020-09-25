package com.elearn.trainor;

import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.DashboardClasses.LoadingPageActivity;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.IClickListener;

import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class OfflineDownload extends AppCompatActivity implements View.OnClickListener {

    ImageView forward_arrow;
    TextView first_name;
    SwitchCompat diploma_switch, safetycard_switch, tools_switch, company_documents_switch;
    String token, username, loginforStattent, diplomaSwitch_status, safetyCradSwitch_status, toolsSwitch_status,
            companyDocumentSwitch_status, firstName, userID, lang_frm_apiResponse;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerSelect dbSelect;
    RelativeLayout rl_am_done;
    Long freeSpaceMB;
    Trace myTrace;
    FirebaseAnalytics analytics;
    ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lang_frm_apiResponse = getIntent().getStringExtra("APIResponseLanguage");
        if (!lang_frm_apiResponse.equals(" ") || !lang_frm_apiResponse.equals(null)) {
            setLocale(lang_frm_apiResponse);
        }
        setContentView(R.layout.activity_offline_download);
        getControls();
    }


    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        dbUpdate = new DataBaseHandlerUpdate(this);
        dbSelect = new DataBaseHandlerSelect(this);

        connectionDetector = new ConnectionDetector(OfflineDownload.this);
        token = getIntent().getStringExtra("token");
        username = getIntent().getStringExtra("username");
//        fromActivity = getIntent().getStringExtra("From");
        firstName = getIntent().getStringExtra("firstName");
        userID = getIntent().getStringExtra("userID");
        if (getIntent().getStringExtra("From") != null && getIntent().getStringExtra("From").equals("StattnetLogin")) {
            loginforStattent = getIntent().getStringExtra("From");
        }


        rl_am_done = (RelativeLayout) findViewById(R.id.rl_am_done);
        forward_arrow = (ImageView) findViewById(R.id.forward_arrow);
        first_name = (TextView) findViewById(R.id.first_name);
        diploma_switch = (SwitchCompat) findViewById(R.id.diploma_switch);
        safetycard_switch = (SwitchCompat) findViewById(R.id.safetycard_switch);
        tools_switch = (SwitchCompat) findViewById(R.id.tools_switch);
        company_documents_switch = (SwitchCompat) findViewById(R.id.company_documents_switch);

        first_name.setText(getResources().getString(R.string.hello) + " " + firstName + "!");
        maintainSwitchStatus();

        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        freeSpaceMB = availableSpace / SIZE_MB;

        rl_am_done.setOnClickListener(this);
        diploma_switch.setOnClickListener(this);
        safetycard_switch.setOnClickListener(this);
        tools_switch.setOnClickListener(this);
        company_documents_switch.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        myTrace = FirebasePerformance.getInstance().newTrace("Offline_Download");
        myTrace.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "OfflineDownload", this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        myTrace.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_am_done:
                if (diplomaSwitch_status.equals("OFF") && safetyCradSwitch_status.equals("OFF") && toolsSwitch_status.equals("OFF") && companyDocumentSwitch_status.equals("OFF")) {
                    if(connectionDetector.isConnectingToInternet()){
                        commonIntentMethod(OfflineDownload.this, LoadingPageActivity.class);
                    }else{
                        AlertDialogManager.showDialog(OfflineDownload.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                    }

                } else {
                    if (50 <= freeSpaceMB) {
                        if(connectionDetector.isConnectingToInternet()){
                            OfflineDownloadPermissionsDispatcher.gotoLoadingPageWithPermissionCheck(OfflineDownload.this);
                        }else{
                            AlertDialogManager.showDialog(OfflineDownload.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                        }

                    } else {
                        AlertDialogManager.showDialog(OfflineDownload.this, getString(R.string.not_enough_space), " ", false, null);
                    }
                }
                //commonIntentMethod(OfflineDownload.this,LoadingPageActivity.class);
                //startDownloading();
                break;
            case R.id.diploma_switch:
                if (diploma_switch.isChecked()) {
                    diplomaSwitch_status = "ON";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "DiplomaSwitchStatus", diplomaSwitch_status);
                } else {
                    diplomaSwitch_status = "OFF";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "DiplomaSwitchStatus", diplomaSwitch_status);
                }
                break;
            case R.id.safetycard_switch:
                if (safetycard_switch.isChecked()) {
                    safetyCradSwitch_status = "ON";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "SafetyCardSwitchStatus", safetyCradSwitch_status);

                } else {
                    safetyCradSwitch_status = "OFF";
                    //dbUpdate.updateTable("OfflineDownload", userID, "", "SafetyCardSwitchStatus", safetyCradSwitch_status);

                }
                break;
            case R.id.tools_switch:
                if (tools_switch.isChecked()) {
                    toolsSwitch_status = "ON";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "ToolsSwitchStatus", toolsSwitch_status);

                } else {
                    toolsSwitch_status = "OFF";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "ToolsSwitchStatus", toolsSwitch_status);

                }
                break;
            case R.id.company_documents_switch:
                if (company_documents_switch.isChecked()) {
                    companyDocumentSwitch_status = "ON";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "DocumentSwitchStatus", companyDocumentSwitch_status);

                } else {
                    companyDocumentSwitch_status = "OFF";
                    // dbUpdate.updateTable("OfflineDownload", userID, "", "DocumentSwitchStatus", companyDocumentSwitch_status);

                }
                break;
        }
    }

    public void commonIntentMethod(Context con, Class activity) {
        dbUpdate.updateTable("OfflineDownload", userID, "", "DiplomaSwitchStatus", diplomaSwitch_status);
        dbUpdate.updateTable("OfflineDownload", userID, "", "ToolsSwitchStatus", toolsSwitch_status);
        dbUpdate.updateTable("OfflineDownload", userID, "", "SafetyCardSwitchStatus", safetyCradSwitch_status);
        dbUpdate.updateTable("OfflineDownload", userID, "", "DocumentSwitchStatus", companyDocumentSwitch_status);
        Intent intent = new Intent(con, activity);
        intent.putExtra("token", token);
        intent.putExtra("username", username);
        intent.putExtra("From", "Login");
        intent.putExtra("LoginType", loginforStattent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        OfflineDownloadPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    commonIntentMethod(OfflineDownload.this, LoadingPageActivity.class);

                } else {
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(OfflineDownload.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(OfflineDownload.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", OfflineDownload.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }, new IClickListener() {
                            @Override
                            public void onClick() {
                            }
                        }, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    } else {
                        AlertDialogManager.showCustomDialog(OfflineDownload.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                ActivityCompat.requestPermissions(OfflineDownload.this, new String[]{permission}, requestCode);
                            }
                        }, new IClickListener() {
                            @Override
                            public void onClick() {
                            }
                        }, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    }
                }
                break;
        }
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void gotoLoadingPage() {
        commonIntentMethod(OfflineDownload.this, LoadingPageActivity.class);

    }

    public void setLocale(String localLang) {
        if (localLang.startsWith("nb")) {
            localLang = "nb";
        } else if (localLang.startsWith("en")) {
            localLang = "en";
        } else if (localLang.startsWith("ko")) {
            localLang = "ko";
        } else if (localLang.startsWith("pl")) {
            localLang = "pl";
        } else if (localLang.startsWith("sv")) {
            localLang = "sv";
        }
        Locale myLocale = new Locale(localLang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public void maintainSwitchStatus() {
        diplomaSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(userID, "DiplomaSwitchStatus", " ");
        safetyCradSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(userID, "SafetyCardSwitchStatus", " ");
        toolsSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(userID, "ToolsSwitchStatus", " ");
        companyDocumentSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(userID, "DocumentSwitchStatus", " ");

        if (diplomaSwitch_status.equals("ON")) {
            diploma_switch.setChecked(true);
        } else if (diplomaSwitch_status.equals("OFF")) {
            diploma_switch.setChecked(false);
        }

        if (safetyCradSwitch_status.equals("ON")) {
            safetycard_switch.setChecked(true);
        } else if (safetyCradSwitch_status.equals("OFF")) {
            safetycard_switch.setChecked(false);
        }

        if (toolsSwitch_status.equals("ON")) {
            tools_switch.setChecked(true);
        } else if (toolsSwitch_status.equals("OFF")) {
            tools_switch.setChecked(false);
        }
        if (companyDocumentSwitch_status.equals("ON")) {
            company_documents_switch.setChecked(true);
        } else if (companyDocumentSwitch_status.equals("OFF")) {
            company_documents_switch.setChecked(false);
        }

    }

}
