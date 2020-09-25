package com.elearn.trainor.SettingModule;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elearn.trainor.BaseAdapters.DownloadCourseRecyclerViewAdapter;
import com.elearn.trainor.BaseAdapters.DownloadToolsRecyclerViewAdapter;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.PropertyClasses.ToolsProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Downloads extends AppCompatActivity implements View.OnClickListener {
    public static Downloads instance;
    RecyclerView recycler_view_downloaded_courses, recycler_view_downloaded_tools;
    SwitchCompat switch_wifi, syncDiploma, syncSafetyCard, syncTools, syncDocument;
    RelativeLayout rlSwitch;
    TextView text_header, txt_downloaded_tools, txt_downloaded_course;
    LinearLayout ll_back, llhome, ll_devButton;
    List<ToolsProperty> toolsPropertyList, renderToolList;
    List<SafetyCardProperty> listSafetyCards;
    List<DiplomaProperty> offlineVideoList;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerDelete dbDelete;
    DownloadCourseRecyclerViewAdapter adapter;
    List<CustomerDetailsProperty> customerIDList;
    SharedPreferenceManager spManager;
    String diplomaSwitch_status, safetyCardSwitch_Status, toolsSwitch_Status, documentsSwitch_Status;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        getControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "DownloadedCourses&Tools", this.getClass().getSimpleName());
    }

    public static Downloads getInstance() {
        if (instance == null) {
            instance = new Downloads();
        }
        return instance;
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(Downloads.this);
        spManager = new SharedPreferenceManager(Downloads.this);
        dbSelect = new DataBaseHandlerSelect(Downloads.this);
        dbInsert = new DataBaseHandlerInsert(Downloads.this);
        dbUpdate = new DataBaseHandlerUpdate(Downloads.this);
        dbDelete = new DataBaseHandlerDelete(Downloads.this);
        offlineVideoList = new ArrayList<>();
        customerIDList = new ArrayList<>();
        renderToolList = new ArrayList<ToolsProperty>();
        rlSwitch = (RelativeLayout) findViewById(R.id.rlSwitch);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        ll_devButton = (LinearLayout) findViewById(R.id.ll_devButton);
        text_header = (TextView) findViewById(R.id.text_header);
        txt_downloaded_tools = (TextView) findViewById(R.id.txt_downloaded_tools);
        txt_downloaded_course = (TextView) findViewById(R.id.txt_downloaded_course);
        switch_wifi = (SwitchCompat) findViewById(R.id.switch_wifi);
        recycler_view_downloaded_courses = (RecyclerView) findViewById(R.id.recycler_view_downloaded_courses);
        recycler_view_downloaded_tools = (RecyclerView) findViewById(R.id.recycler_view_downloaded_tools);
        customerIDList = dbSelect.getCustomerIdFromCustomerDetails("Yes");
        //customerId = "5fbd5d70-a673-11e6-a423-005056010872";
        String languageProfile = spManager.getLanguage();
        if (languageProfile.startsWith("nb")) {
            languageProfile = "nb_NO";
        } else if (languageProfile.startsWith("en")) {
            languageProfile = "en_US";
        } else if (languageProfile.startsWith("ko")) {
            languageProfile = "ko_KR";
        } else if (languageProfile.startsWith("pl")) {
            languageProfile = "pl_PL";
        } else if (languageProfile.startsWith("sv")) {
            languageProfile = "nb_NO";
        }
        toolsPropertyList = dbSelect.getDownlodedToolDetails("Downloads", languageProfile);
        listSafetyCards = dbSelect.getSafetyCardAttribute("");
        text_header.setText(getResources().getString(R.string.text_header_download));
        ll_back.setOnClickListener(this);
        llhome.setOnClickListener(this);
        switch_wifi.setOnClickListener(this);
        recycler_view_downloaded_courses.setLayoutManager(new LinearLayoutManager(Downloads.this));
        recycler_view_downloaded_tools.setLayoutManager(new LinearLayoutManager(Downloads.this));
        if (toolsPropertyList.size() > 0) {
            for (ToolsProperty info : toolsPropertyList) {
                File rootDir = android.os.Environment.getExternalStorageDirectory();
                File toolFile = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/" + info.name);
                if (!toolFile.exists()) {
                    dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                } else {
                    renderToolList.add(info);
                }
            }
            DownloadToolsRecyclerViewAdapter adapter = new DownloadToolsRecyclerViewAdapter(Downloads.this, renderToolList);
            recycler_view_downloaded_tools.setAdapter(adapter);
        }
        // offline video
        getfile();
        // offline video ends
        String isEnabled = dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", spManager.getUserID(), "");
        if (isEnabled != null && !isEnabled.equals("")) {
            if (isEnabled.equals("Yes")) {
                switch_wifi.setChecked(true);
            } else {
                switch_wifi.setChecked(false);
            }
        } else {
            saveSwitchState();
        }
        ll_devButton.setVisibility(View.GONE);
        ll_devButton.setOnClickListener(this);
        txt_downloaded_tools.setText(getResources().getString(R.string.no_tools_dwnloaded));
        txt_downloaded_course.setText(getResources().getString(R.string.no_course_dwnloaded));
        if (customerIDList.size() > 0 && ((toolsPropertyList.size() > 0) || (offlineVideoList.size() > 0))) {
            ll_devButton.setVisibility(View.VISIBLE);
            if ((toolsPropertyList.size() > 0)) {
                txt_downloaded_tools.setText(getResources().getString(R.string.downloaded_tools));
            }
            if (offlineVideoList.size() > 0) {
                txt_downloaded_course.setText(getResources().getString(R.string.downloaded_courses));
            }
        }else{
            ll_devButton.setVisibility(View.GONE);
        }
        if ((toolsPropertyList.size() > 0)) {
            txt_downloaded_tools.setText(getResources().getString(R.string.downloaded_tools));
        }
        if (offlineVideoList.size() > 0) {
            txt_downloaded_course.setText(getResources().getString(R.string.downloaded_courses));
        }

        syncDiploma = (SwitchCompat) findViewById(R.id.syncDiploma);
        syncSafetyCard = (SwitchCompat) findViewById(R.id.syncSafetyCard);
        syncTools = (SwitchCompat) findViewById(R.id.syncTools);
        syncDocument = (SwitchCompat) findViewById(R.id.syncDocument);

        syncDiploma.setOnClickListener(this);
        syncSafetyCard.setOnClickListener(this);
        syncTools.setOnClickListener(this);
        syncDocument.setOnClickListener(this);

        diplomaSwitch_status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DiplomaSwitchStatus", " ");
        safetyCardSwitch_Status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "SafetyCardSwitchStatus", " ");
        toolsSwitch_Status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "ToolsSwitchStatus", " ");
        documentsSwitch_Status = dbSelect.getStstusFromOfflineDownloadTable(spManager.getUserID(), "DocumentSwitchStatus", " ");

        if (diplomaSwitch_status.equals("ON")) {
            syncDiploma.setChecked(true);
        } else {
            syncDiploma.setChecked(false);
        }

        if (safetyCardSwitch_Status.equals("ON")) {
            syncSafetyCard.setChecked(true);
        } else {
            syncSafetyCard.setChecked(false);
        }
        if (toolsSwitch_Status.equals("ON")) {
            syncTools.setChecked(true);
        } else {
            syncTools.setChecked(false);
        }
        if (documentsSwitch_Status.equals("ON")) {
            syncDocument.setChecked(true);
        } else {
            syncDocument.setChecked(false);
        }
    }

    public void getfile() {
        offlineVideoList.clear();
        try {
            List<DiplomaProperty> dwStatusList = dbSelect.getDownloadedCourseFilesFromCourseDownloadTable("IfNull(Status,'No')as dwStatus, licenseId, IfNull(CompletionDate,'') as completedDate ", spManager.getUserID(), "", "");
            for (DiplomaProperty info : dwStatusList) {
                File rootDir = android.os.Environment.getExternalStorageDirectory();
                File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/" + info.licenseId);
                String filePath = root.getAbsolutePath();
                File file = new File(filePath);
                if (file.exists() && info.downloadedStatus.equals("Yes")) {
                    info.fileSize = Long.toString(getFileSize(file) / (1024 * 1024)) + " MB";
                    List<DiplomaProperty> diplomaInfoList = null;
                    if (!info.completionDate.equals("")) {
                        diplomaInfoList = dbSelect.getDownloadedCourseFilesFromCourseDownloadTable("", spManager.getUserID(), info.licenseId, "DownloadedCourseDetailsFromDiploma");
                    } else {
                        if (!dbSelect.getDataFromCoursesTable("*", spManager.getUserID(), info.licenseId.toString().trim()).equals("")) {
                            diplomaInfoList = dbSelect.getDownloadedCourseFilesFromCourseDownloadTable("", spManager.getUserID(), info.licenseId, "DownloadedCourseDetailsFromCourse");
                        } else {
                            diplomaInfoList = dbSelect.getDownloadedCourseFilesFromCourseDownloadTable("", spManager.getUserID(), info.licenseId, "DownloadedCourseDetailsFromDiploma");
                        }
                    }
                    if (diplomaInfoList != null && diplomaInfoList.size() > 0) {
                        DiplomaProperty downloadedCourseInfo = diplomaInfoList.get(0);
                        downloadedCourseInfo.downloadedStatus = "Yes";
                        downloadedCourseInfo.fileSize = info.fileSize;
                        offlineVideoList.add(downloadedCourseInfo);
                    }
                } else {
                    DeleteCoursesFile(file);
                }
            }
            if (offlineVideoList.size() > 0) {
                recycler_view_downloaded_courses.setLayoutManager(new LinearLayoutManager(Downloads.this));
                adapter = new DownloadCourseRecyclerViewAdapter(Downloads.this, offlineVideoList);
                recycler_view_downloaded_courses.setAdapter(adapter);
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    public long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<File>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                gotTo(HomePage.class);
                break;
            case R.id.switch_wifi:
                saveSwitchState();
                break;
            case R.id.ll_devButton:
                //hideDevButton();   todo
                hideDevButtonCourseSection(0);
                hideDevButtonToolSection(0);
                customtoast();
                for (int i = 0; i < toolsPropertyList.size(); i++) {
                    ToolsProperty info = toolsPropertyList.get(i);
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File rootDiploma = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Diplomas/");
                    File rootTools = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/");
                    File downloadToolsDir = new File(rootTools.getAbsolutePath());
                    File downloadDiplomasDir = new File(rootDiploma.getAbsolutePath());
                    File downloadUnzippedDir = new File(rootTools.getAbsolutePath() + "/UnZipped/");
                    getFilesFromDir(downloadDiplomasDir, downloadToolsDir, downloadUnzippedDir, info.name, info.id, i);
                }
                for (int i = 0; i < offlineVideoList.size(); i++) {
                    offlineVideoList.remove(i);
                }
                offlineVideoList.clear();
                File rootDir = android.os.Environment.getExternalStorageDirectory();
                File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course");
                String filePath = root.getAbsolutePath();
                File dir = new File(filePath);
                DeleteRecursive(dir);
                dbDelete.deleteTableByName("CourseDownload", spManager.getUserID());
                break;

            case R.id.syncDiploma:
                if (syncDiploma.isChecked()) {
                    diplomaSwitch_status = "ON";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "DiplomaSwitchStatus", diplomaSwitch_status);
                } else {
                    diplomaSwitch_status = "OFF";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "DiplomaSwitchStatus", diplomaSwitch_status);
                }
                Bundle bundle = new Bundle();
                bundle.putString("Diploma_Sync", diplomaSwitch_status);
                analytics.logEvent("Diploma_Sync", bundle);
                break;
            case R.id.syncSafetyCard:
                if (syncSafetyCard.isChecked()) {
                    safetyCardSwitch_Status = "ON";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "SafetyCardSwitchStatus", safetyCardSwitch_Status);
                } else {
                    safetyCardSwitch_Status = "OFF";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "SafetyCardSwitchStatus", safetyCardSwitch_Status);
                }
                Bundle bundle2 = new Bundle();
                bundle2.putString("SafetyCard_Sync", safetyCardSwitch_Status);
                analytics.logEvent("SafetyCard_Sync", bundle2);
                break;
            case R.id.syncTools:
                if (syncTools.isChecked()) {
                    toolsSwitch_Status = "ON";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "ToolsSwitchStatus", toolsSwitch_Status);
                } else {
                    toolsSwitch_Status = "OFF";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "ToolsSwitchStatus", toolsSwitch_Status);
                }
                Bundle bundle3 = new Bundle();
                bundle3.putString("Tools_Sync", toolsSwitch_Status);
                analytics.logEvent("Tools_Sync", bundle3);
                break;
            case R.id.syncDocument:
                if (syncDocument.isChecked()) {
                    documentsSwitch_Status = "ON";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "DocumentSwitchStatus", documentsSwitch_Status);

                } else {
                    documentsSwitch_Status = "OFF";
                    dbUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "DocumentSwitchStatus", documentsSwitch_Status);

                }
                Bundle bundle4 = new Bundle();
                bundle4.putString("Document_Sync", documentsSwitch_Status);
                analytics.logEvent("Document_Sync", bundle4);
                break;
        }
    }

    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                DeleteRecursive(child);
            }
        fileOrDirectory.delete();
        // customtoast();
    }

    @Override
    public void onBackPressed() {
        gotTo(Settings.class);
    }

    public void saveSwitchState() {
        String isEnabled = dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", spManager.getUserID(), "");
        if (isEnabled.equals("")) {
            if (switch_wifi.isChecked()) {
                dbInsert.addDataIntoNotificationTable("DownloadOverWifi", "Yes", spManager.getUserID());
            } else {
                dbInsert.addDataIntoNotificationTable("DownloadOverWifi", "No", spManager.getUserID());
            }
        } else {
            if (switch_wifi.isChecked()) {
                dbUpdate.updateNotificationTable("DownloadOverWifi", "Yes", spManager.getUserID());
            } else {
                dbUpdate.updateNotificationTable("DownloadOverWifi", "No", spManager.getUserID());
            }
        }
    }

    public void getFilesFromDir(File diplomasFromSD, File filesFromSD, File fileUnZipped, String fileName, String tool_id, int position) {
        File listAllDiplomasFiles[] = diplomasFromSD.listFiles();
        File listAllFiles[] = filesFromSD.listFiles();
        File listAllUnZippedFiles[] = fileUnZipped.listFiles();
        if (listAllDiplomasFiles != null && listAllDiplomasFiles.length > 0) {
            for (File currentFile : listAllDiplomasFiles) {
                currentFile.delete();
            }
        }
        if (listAllFiles != null && listAllFiles.length > 0) {
            for (File currentFile : listAllFiles) {
                if (currentFile.getName().equals(fileName)) {
                    if (currentFile.delete()) {
                        if (renderToolList.size() > position && renderToolList.get(position) != null) {
                            ToolsProperty info = new ToolsProperty();
                            info.id = tool_id;
                            dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                            renderToolList.remove(position);
                            DownloadToolsRecyclerViewAdapter adapter = new DownloadToolsRecyclerViewAdapter(Downloads.this, renderToolList);
                            recycler_view_downloaded_tools.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }

        if (listAllUnZippedFiles != null && listAllUnZippedFiles.length > 0) {
            for (File currentFile : listAllUnZippedFiles) {
                if (currentFile.getName().equals(fileName)) {
                    if (currentFile.isDirectory()) {
                        boolean isDeleted = false;
                        File[] children = currentFile.listFiles();
                        for (int i = 0; i < children.length; i++) {
                            if (children[i].isDirectory()) {
                                isDeleted = delete(children[i], children[i].listFiles());
                            }
                            children[i].delete();
                        }
                        if (isDeleted) {
                            if (renderToolList.size() > position && renderToolList.get(position) != null) {
                                ToolsProperty info = new ToolsProperty();
                                info.id = tool_id;
                                dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                                renderToolList.remove(position);
                                DownloadToolsRecyclerViewAdapter adapter = new DownloadToolsRecyclerViewAdapter(Downloads.this, renderToolList);
                                recycler_view_downloaded_tools.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        currentFile.delete();
                    } else {
                        boolean isDeleted = currentFile.delete();
                        if (isDeleted) {
                            if (renderToolList.size() > position && renderToolList.get(position) != null) {
                                ToolsProperty info = new ToolsProperty();
                                info.id = tool_id;
                                dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                                renderToolList.remove(position);
                                DownloadToolsRecyclerViewAdapter adapter = new DownloadToolsRecyclerViewAdapter(Downloads.this, renderToolList);
                                recycler_view_downloaded_tools.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean delete(File currentFile, File[] fileList) {
        boolean isDeleted = false;
        if (currentFile.isDirectory()) {
            File[] children = currentFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                if (children[i].isDirectory()) {
                    isDeleted = delete(children[i], children[i].listFiles());
                }
                children[i].delete();
            }
        } else {
            for (int i = 0; i < fileList.length; i++) {
                isDeleted = new File(currentFile, fileList[i].getName()).delete();
            }
        }
        return isDeleted;
    }

    public void customtoast() {
        LayoutInflater inflater = getLayoutInflater();
        View customToastroot = inflater.inflate(R.layout.custom_tost_deleted_sucess, null);
        Toast customtoast = new Toast(Downloads.this);
        customtoast.setView(customToastroot);
        customtoast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_SHORT);
        customtoast.show();
    }

    public void hideDevButtonToolSection(int listSize) {
        ll_devButton.setVisibility(View.GONE);
        ll_devButton.setEnabled(false);
        if (listSize == 0) {
            txt_downloaded_tools.setText(getResources().getString(R.string.no_tools_dwnloaded));
            recycler_view_downloaded_tools.setVisibility(View.GONE);
        }
    }

    public void hideDevButtonCourseSection(int listSize) {
        ll_devButton.setVisibility(View.GONE);
        ll_devButton.setEnabled(false);
        if (listSize == 0) {
            txt_downloaded_course.setText(getResources().getString(R.string.no_course_dwnloaded));
            recycler_view_downloaded_courses.setVisibility(View.GONE);
        }
    }

    public void gotTo(Class activity) {
        Intent intentSetting = new Intent(Downloads.this, activity);
        intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentSetting);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    void DeleteCoursesFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                DeleteCoursesFile(child);
            }
        fileOrDirectory.delete();
    }
}
