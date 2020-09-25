package com.elearn.trainor.ToolBoxModule;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.HelperClasses.*;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.PropertyClasses.ToolsProperty;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.List;

import it.sephiroth.android.library.picasso.Callback;

public class ToolboxLoadingActivity extends AppCompatActivity {
    RelativeLayout rl_root;
    LinearLayout ll_cancel;
    TextView txtPercentage, txtToolName;
    Handler handler;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    ImageView img_tool;
    ConnectionDetector connectionDetector;
    private DownloadManager downloadManager;
    private long downloadReference;
    SharedPreferenceManager spManager;
    List<ToolsProperty> toolsPropertiesList;
    String BGColor, ZipFileURL, ZipFileName, Tool_ID, From, Force_Download, Tool_Icon_URL;
    boolean downloading = false;
    Cursor cursor = null;
    Thread downloadThread;
    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_toolbox_loading);
        Intent getIntent = getIntent();
        if (getIntent != null && getIntent.hasExtra("ZipFileName")) {
            ZipFileName = getIntent().getStringExtra("ZipFileName");
        }
        GetControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setCurrentScreen(this, "ToolsDownloading", this.getClass().getSimpleName());
    }

    public void GetControls() {
        writeNoMediaFile();
        analytics = FirebaseAnalytics.getInstance(this);
        dbSelect = new DataBaseHandlerSelect(ToolboxLoadingActivity.this);
        dbUpdate = new DataBaseHandlerUpdate(ToolboxLoadingActivity.this);
        spManager = new SharedPreferenceManager(ToolboxLoadingActivity.this);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        connectionDetector = new ConnectionDetector(ToolboxLoadingActivity.this);
        handler = new Handler();
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);
        ll_cancel = (LinearLayout) findViewById(R.id.ll_cancel);
        txtToolName = (TextView) findViewById(R.id.txtToolName);
        img_tool = (ImageView) findViewById(R.id.img_tool);
        DownloadManager.Query q = new DownloadManager.Query();
        if (!spManager.getValueByKeyNameFromDownload("DownloadingReference").equals("")) {
            downloadReference = Long.parseLong(spManager.getValueByKeyNameFromDownload("DownloadingReference"));
        }
        q.setFilterById(downloadReference);
        try {
            cursor = downloadManager.query(q);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!spManager.getValueByKeyNameFromDownload(spManager.getValueByKeyNameFromDownload("DownloadingReference")).equals("")) {
            toolsPropertiesList = dbSelect.getDownlodedToolDetails(spManager.getValueByKeyNameFromDownload(spManager.getValueByKeyNameFromDownload("DownloadingReference"))," ");
        } else {
            String toolid = getIntent().getStringExtra("Tool_ID");
            if (toolid != null && !toolid.equals("")) {
                toolsPropertiesList = dbSelect.getDownlodedToolDetails(getIntent().getStringExtra("Tool_ID")," ");
            }
        }
        if (toolsPropertiesList != null && toolsPropertiesList.size() > 0) {
            for (ToolsProperty info : toolsPropertiesList) {
                BGColor = info.background_color;
                ZipFileURL = info.file;
                ZipFileName = info.name;
                Tool_ID = info.id;
                if (spManager.getValueByKeyNameFromDownload("From").equals("")) {
                    From = getIntent().getStringExtra("From");
                } else {
                    From = spManager.getValueByKeyNameFromDownload("From");
                }
                Force_Download = info.force_download;
                Tool_Icon_URL = info.iconURL;
            }
        }

        if ((cursor != null && cursor.moveToFirst()) && cursor.getCount() > 0) {
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                Intent intent = new Intent(ToolboxLoadingActivity.this, Tools_Content_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Tool_ID", Tool_ID);
                intent.putExtra("ZipFile", ZipFileName);
                intent.putExtra("Force_Download", Force_Download);
                intent.putExtra("BGColor", BGColor);
                intent.putExtra("From", From);
                startActivity(intent);
                finish();
            } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {
                PicasoImageLoader.getImagesFromURL(ToolboxLoadingActivity.this, Tool_Icon_URL, img_tool, 0, 0, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("myDownloadReferenece", "first");
                        updatepercentage(downloadReference);
                    }

                    @Override
                    public void onError() {
                        Log.d("myDownloadReferenece", "Second");
                        updatepercentage(downloadReference);
                    }
                });
            }
        } else {
            startDownloadingFile();
        }
        if (BGColor != null && ZipFileName != null) {
            rl_root.setBackgroundColor(Color.parseColor(BGColor));
            txtToolName.setText(ZipFileName);
        }
        ll_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (downloadThread != null) {
                        downloadThread.stop();
                        downloadThread.destroy();
                    }
                } catch (Exception ex) {

                } finally {

                    Intent intent = new Intent(ToolboxLoadingActivity.this, ToolBox.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("From", From);
                    startActivity(intent);
                    downloadManager.remove(downloadReference);
                    Log.d("myDownloadReferenece", downloadReference + "");
                    spManager.removeSharedPreferenceByName("Download");
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void showDownloadProgress(String downloadURL) {
        if (spManager.getValueByKeyNameFromDownload("DownloadingReference").equals("")) {
            downloading = true;
            Uri Download_Uri = Uri.parse(downloadURL);
            Log.d("myDownloadReferenece", Download_Uri + "");
            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
            if (dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", spManager.getUserID(), "").equals("Yes")) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            }
            request.setAllowedOverRoaming(false);
            request.setVisibleInDownloadsUi(true);
            //request.setDestinationInExternalPublicDir("/MyTrainor/.movetools/", ZipFileName);
            request.setDestinationInExternalPublicDir("/MyTrainor/.tools/", ZipFileName);
            downloadReference = downloadManager.enqueue(request);
            Log.d("myDownloadReferenece", downloadReference + "");
            spManager.removeSharedPreferenceByName("Download");
            SharedPreferences.Editor editor = spManager.getDownloadToolsSharedPreference();
            editor.putString(String.valueOf(downloadReference), Tool_ID);
            editor.putString("DownloadingReference", downloadReference + "");
            editor.putString("From", getIntent().getStringExtra("From"));
            editor.putString("FileName", ZipFileName);
            editor.commit();
            Log.d("myDownloadReferenece", "third");
            updatepercentage(downloadReference);
        }
    }

    public void startDownloadingFile() {
        if (connectionDetector.isConnectingToInternet()) {
            PicasoImageLoader.getImagesFromURL(ToolboxLoadingActivity.this, Tool_Icon_URL, img_tool, 0, 0, new Callback() {
                @Override
                public void onSuccess() {
                    if (ZipFileURL != null && ZipFileName != null) {
                        showDownloadProgress(ZipFileURL);
                    }
                }

                @Override
                public void onError() {
                    if (ZipFileURL != null && ZipFileName != null) {
                        showDownloadProgress(ZipFileURL);
                    }
                }
            });
        } else {
            AlertDialogManager.showDialog(ToolboxLoadingActivity.this, getResources().getString(R.string.internetErrorTitle), getResources().getString(R.string.internetErrorMessage), false, null);
        }
    }

    public void updatepercentage(final long downloadReferenceID) {
        final DownloadManager _downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final int dl_progress = msg.what;
                try {
                    txtPercentage.setText(dl_progress + " %");
                    if (dl_progress == 100) {
                        if (downloadThread != null) {
                            downloadThread.stop();
                            downloadThread.destroy();
                        }
                    }
                } catch (Exception ex) {

                } finally {
                    if (dl_progress == 100) {
                        downloading = false;
                        ToolsProperty info = new ToolsProperty();
                        info.id = spManager.getValueByKeyNameFromDownload(spManager.getValueByKeyNameFromDownload("DownloadingReference"));
                        dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                        spManager.removeSharedPreferenceByName("Download");
                        Intent goto_intent = new Intent(ToolboxLoadingActivity.this, Tools_Content_Activity.class);
                        goto_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        goto_intent.putExtra("Tool_ID", Tool_ID);
                        goto_intent.putExtra("ZipFile", ZipFileName);
                        goto_intent.putExtra("Force_Download", Force_Download);
                        goto_intent.putExtra("BGColor", BGColor);
                        goto_intent.putExtra("From", From);
                        startActivity(goto_intent);
                        finish();
                    }
                }
                super.handleMessage(msg);
            }
        };

        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (downloading) {
                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadReferenceID);
                        Cursor cursor = null;
                        try {
                            cursor = _downloadManager.query(q);
                            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                    downloading = false;
                                }
                                Message msg = handler.obtainMessage();
                                final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                                msg.what = dl_progress;
                                handler.sendMessage(msg);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if(cursor != null)
                                cursor.close();
                        }
                    }
                } catch (Exception ex) {
                    downloading = false;
                }
            }
        });
        downloadThread.start();
    }

    public void writeNoMediaFile() {
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/");
            String filePath = root.getAbsolutePath();
            File file = new File(filePath, ".nomedia");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }
}
