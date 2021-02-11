package com.elearn.trainor.CourseModule;

import com.elearn.trainor.Diploma.Diploma;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
//import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.elearn.trainor.HelperClasses.*;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.services.network.HttpRequest;

public class CourseDownloadingActivity extends AppCompatActivity {
    TextView ll_cancel;
    SharedPreferenceManager spManager;
    List<String> url_list;
    ConnectionDetector connectionDetector;
    TextView txtCourseName, loadingTxt;
    CircleImageView courseImage;
    long fileSize = 0;
    String fileSizeInMB = "", CourseId = "", fileName = "", LicenceID = "", downloadFailedMessage = "";
    DownloadManager.Query q;
    private long downloadReference;
    boolean downloading = false;
    long downloadedFileSize, total;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerDelete dbDelete;
    Cursor cursor;
    IntentFilter internet_intent_filter;
    boolean isActivityLive = false, isDownloadFailed = false;
    FirebaseAnalytics analytics;
    //Trace myTrace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_course_loading);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isActivityLive = true;
        GetControls();
        spManager = new SharedPreferenceManager(CourseDownloadingActivity.this);
        q = new DownloadManager.Query();
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
    }

    @Override
    protected void onStop() {
        isActivityLive = false;
        super.onStop();
    }

    public void GetControls() {
        internet_intent_filter = new IntentFilter();
        internet_intent_filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.internetInfoReceiver, internet_intent_filter);
        CourseId = getIntent().getStringExtra("CourseId");
        LicenceID = getIntent().getStringExtra("LisenceId");
        dbSelect = new DataBaseHandlerSelect(CourseDownloadingActivity.this);
        dbUpdate = new DataBaseHandlerUpdate(CourseDownloadingActivity.this);
        dbInsert = new DataBaseHandlerInsert(CourseDownloadingActivity.this);
        dbDelete = new DataBaseHandlerDelete(CourseDownloadingActivity.this);
        fileSize = Long.parseLong(getIntent().getStringExtra("fileSize"));
        fileSizeInMB = getIntent().getStringExtra("fileSizeInMB");
        connectionDetector = new ConnectionDetector(CourseDownloadingActivity.this);
        ll_cancel = (TextView) findViewById(R.id.ll_cancel);
        spManager = new SharedPreferenceManager(CourseDownloadingActivity.this);
        loadingTxt = (TextView) findViewById(R.id.loading);
        courseImage = (CircleImageView) findViewById(R.id.circleImageView);
        if (connectionDetector.isConnectingToInternet()) {
            if (getIntent().getStringExtra("ImageUrl") != null && !getIntent().getStringExtra("ImageUrl").equals("")) {
                Glide.with(this).load(getIntent().getStringExtra("ImageUrl")).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        courseImage.setImageResource(R.drawable.elarning_course);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(courseImage);
            } else {
                courseImage.setImageResource(R.drawable.elarning_course);
            }
        }
        fileName = getIntent().getStringExtra("FileName");
        txtCourseName = (TextView) findViewById(R.id.txtCourseName);
        txtCourseName.setText(fileName);
        loadingTxt.setText(getResources().getString(R.string.loading_course_pls_wait));
        spManager.removeSharedPreferenceByName("DownloadCourse");
        //writeNoMediaFile("");
        downloadCourseFromServer(WebServicesURL.Video_File_Size_URL + CourseId + "/" + LicenceID + "/360p/file.zip");
        url_list = new ArrayList<>();
        ll_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDownload();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void downloadCourseFromServer(final String fileURL) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL u = new URL(fileURL);
                    HttpRequest request = HttpRequest.get(u);
                    request.accept("application/zip");
                    request.contentType("application/zip");
                    request.authorization("Bearer " + spManager.getToken());
                    File rootDir = Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID + "/");
                    String filePath = root.getAbsolutePath();
                    File dir = new File(filePath);
                    request.connectTimeout(10000);
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, LicenceID + ".zip");
                    if (request.ok()) {
                        request.receive(file);
                    } else {
                        isDownloadFailed = true;
                    }
                    request.disconnect();
                } catch (Exception ex) {
                    isDownloadFailed = true;
                } finally {
                    downloadingFailed();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (isDownloadFailed && isActivityLive) {
                    downloadingFailed();
                } else {
                    unzipMethod(downloadedFileSize);
                }
            }
        }.execute();
    }

    public void unzipMethod(final long downloaded_data_size) {
        File rootDir = android.os.Environment.getExternalStorageDirectory();
        File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID);
        String filePath = root.getAbsolutePath() + "/" + LicenceID + ".zip";
        File file = new File(filePath);
        if (file.exists()) {
            unzip(file, root.getAbsolutePath() + "/UnZipped/", downloaded_data_size);
        }
    }

    public void unzip(final File file, final String destinationPath, final long download_data_size) {
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
                        cancelDownload();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    readPropertiesFile(download_data_size);
                }
            }.execute();
        } catch (Exception ex) {
            cancelDownload();
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

    public void readPropertiesFile(final long downloaded_data_size) {
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/");
            String filePath = root.getAbsolutePath() + "/" + LicenceID + "/UnZipped/" + "__download.properties";
            File file = new File(filePath);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    url_list.add(line);
                }
            }
            if (url_list != null && url_list.size() > 0) {
                if (dbSelect.getDataFromCourseDownloadTable("IfNull(Status,'No')as dwStatus", spManager.getUserID(), CourseId, LicenceID).equals("")) {
                    dbInsert.addDataIntoCourseDownloadTable(CourseId, LicenceID, spManager.getUserID());
                }
                String videoUrl = url_list.get(0);
                String video_file_name = videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.lastIndexOf("."));
                IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                registerReceiver(downloadReceiver, filter);
                showDownloadProgress(downloaded_data_size, video_file_name, videoUrl);
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    public void showDownloadProgress(final long downloaded_data_size, String video_name, final String courseVideoURL) {
        String download_url = courseVideoURL.substring(0, courseVideoURL.lastIndexOf("="));
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (!spManager.getValueByKeyNameFromDownloadCoursePage("CourseURL").equals(courseVideoURL)) {
            try {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadReference);
                cursor = downloadManager.query(q);
                if ((cursor != null && cursor.moveToFirst()) && cursor.getCount() > 0) {
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        if (!spManager.getValueByKeyNameFromDownloadCoursePage("CourseURL").equals(courseVideoURL)) {
                            downloadReference = 0;
                            spManager.removeSharedPreferenceByName("DownloadCourse");
                            String extension = courseVideoURL.substring(courseVideoURL.lastIndexOf("."), courseVideoURL.length()).trim();
                            String dirPath = courseVideoURL.substring(courseVideoURL.lastIndexOf("=") + 1, courseVideoURL.lastIndexOf("/") + 1);
                            File rootDir = android.os.Environment.getExternalStorageDirectory();
                            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/");
                            String fileToHide = root.getAbsolutePath() + "/" + LicenceID + "/UnZipped/" + dirPath;
                            //writeNoMediaFile(fileToHide);
                            String filePath = root.getAbsolutePath() + "/" + LicenceID + "/UnZipped/" + dirPath + video_name + extension;
                            File file = new File(filePath);
                            if (!file.exists()) {
                                downloading = true;
                                Uri Download_Uri = Uri.parse(download_url);
                                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                request.setAllowedOverRoaming(true);
                                request.setVisibleInDownloadsUi(false);
                                request.setMimeType("application/octet-stream");
                                request.setDestinationInExternalFilesDir(this,"/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID + "/UnZipped/" + dirPath + "/" ,  video_name+extension );

                                //request.setDestinationUri(Uri.parse("file://" + android.os.Environment.getExternalStorageDirectory() + "/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID + "/UnZipped/" + dirPath + "/" + video_name + extension));
                                //request.setDestinationInExternalPublicDir("/MyTrainor/Course/" + LicenceID + "/UnZipped/" + dirPath, video_name + extension);
                                downloadReference = downloadManager.enqueue(request);
                                SharedPreferences.Editor editor = spManager.getDownloadCourseSharedPreference();
                                editor.putString(video_name, downloadReference + "");
                                editor.putString("CourseURL", courseVideoURL);
                                editor.putString("ReferenceID", downloadReference + "");
                                editor.commit();
                                updateDownloadingProgress(downloaded_data_size, downloadReference);
                            } else {
                                removeDuplicateURL(courseVideoURL); //Same URL Problem
                            }
                        } else {
                            if (isActivityLive) {
                                AlertDialogManager.showDialog(CourseDownloadingActivity.this, getResources().getString(R.string.download_error), download_url, false, new IClickListener() {
                                    @Override
                                    public void onClick() {
                                        cancelDownload();
                                    }
                                });
                            }
                        }
                    }
                } else {
                    downloadReference = 0;
                    spManager.removeSharedPreferenceByName("DownloadCourse");
                    String extension = courseVideoURL.substring(courseVideoURL.lastIndexOf("."), courseVideoURL.length()).trim();
                    String dirPath = courseVideoURL.substring(courseVideoURL.lastIndexOf("=") + 1, courseVideoURL.lastIndexOf("/") + 1);
                    File rootDir = android.os.Environment.getExternalStorageDirectory();
                    File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/");
                    String fileToHide = root.getAbsolutePath() + "/" + LicenceID + "/UnZipped/" + dirPath;
                    writeNoMediaFile(fileToHide);
                    String filePath = root.getAbsolutePath() + "/" + LicenceID + "/UnZipped/" + dirPath + video_name + extension;
                    File file = new File(filePath);
                    if (!file.exists()) {
                        downloading = true;
                        Uri Download_Uri = Uri.parse(download_url);
                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setAllowedOverRoaming(true);
                        request.setVisibleInDownloadsUi(false);
                        request.setMimeType("application/octet-stream");
                        request.setDestinationInExternalFilesDir(this,"/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID + "/UnZipped/" + dirPath + "/" ,  video_name+extension );

                        //request.setDestinationUri(Uri.parse("file://" + android.os.Environment.getExternalStorageDirectory() + "/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID + "/UnZipped/" + dirPath + "/" + video_name + extension));
                        //request.setDestinationInExternalPublicDir("/MyTrainor/Course/" + LicenceID + "/UnZipped/" + dirPath, video_name + extension);
                        downloadReference = downloadManager.enqueue(request);
                        SharedPreferences.Editor editor = spManager.getDownloadCourseSharedPreference();
                        editor.putString(video_name, downloadReference + "");
                        editor.putString("CourseURL", courseVideoURL);
                        editor.putString("ReferenceID", downloadReference + "");
                        editor.commit();
                        updateDownloadingProgress(downloaded_data_size, downloadReference);
                    }
                }
            } catch (Exception ex) {
                Log.d("", ex.getMessage());
            }
        }
    }

    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                DeleteRecursive(child);
            }
        fileOrDirectory.delete();
    }

    public void updateDownloadingProgress(final long totalDownloadedSize, final long downloadReferenceID) {
        Handler progressHandler = new Handler();
        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Integer, Integer, String>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected String doInBackground(Integer... params) {
                        try {
                            while (downloading) {
                                DownloadManager.Query q = new DownloadManager.Query();
                                q.setFilterById(downloadReferenceID);
                                Cursor cursor = downloadManager.query(q);
                                cursor.moveToFirst();
                                final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                    downloading = false;
                                }
                                if ((cursor != null && cursor.moveToFirst()) && cursor.getCount() > 0) {
                                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {

                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {

                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_PAUSED) {
                                        String msg = "Status Paused";
                                        Log.d("", msg);
                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_PENDING) {
                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                                        //cancelDownload();
                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.ERROR_FILE_ERROR) {
                                        String msg = "ERROR_FILE_ERROR";
                                        Log.d("", msg);
                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.ERROR_UNKNOWN) {
                                        String msg = "ERROR_UNKNOWN";
                                        Log.d("", msg);
                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.ERROR_HTTP_DATA_ERROR) {
                                        String msg = "ERROR_HTTP_DATA_ERROR";
                                        Log.d("", msg);
                                    } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.ERROR_UNHANDLED_HTTP_CODE) {
                                        String msg = "ERROR_UNHANDLED_HTTP_CODE";
                                        Log.d("", msg);
                                    }
                                }
                                final int dl_progress = (int) (((bytes_downloaded + totalDownloadedSize) * 100) / fileSize);
                                final int percentage = (int) ((bytes_downloaded * 100) / bytes_total);
                                total = bytes_downloaded + totalDownloadedSize;
                                if (percentage == 100) {
                                    downloading = false;
                                    publishProgress((int) percentage);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dl_progress >= 100) {
                                            loadingTxt.setText(getResources().getString(R.string.hundered_percent) + " " + fileSizeInMB);
                                        } else {
                                            loadingTxt.setText(dl_progress + " " + getResources().getString(R.string.percent_of) + " " + fileSizeInMB);
                                        }
                                    }
                                });
                                cursor.close();
                            }
                        } catch (Exception ex) {
                            Log.d("Error", ex.getMessage());
                            cancelDownload();
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(final Integer... progress) {
                        super.onProgressUpdate(progress);
                        final int percentage = progress[0];
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                    }
                }.execute();
            }
        }, 0);
    }

    public void cancelDownload() {
        try {
            if (internetInfoReceiver != null) {
                try {
                    unregisterReceiver(internetInfoReceiver);
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                }
            }
            if (downloadReference != 0) {
                DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                manager.remove(downloadReference);
            }
            spManager.removeSharedPreferenceByName("DownloadCourse");
            dbDelete.deleteValueFromTable("CourseDownload", "licenseId", LicenceID);
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/" + LicenceID);
            String filePath = root.getAbsolutePath();
            File dir = new File(filePath);
            DeleteRecursive(dir);
            try {
                if (downloadReceiver != null) {
                    unregisterReceiver(downloadReceiver);
                }
            } catch (Exception ex) {
                String reciverException = ex.getMessage();
                Log.d("downloadReceiver",reciverException);
            }
            Intent intent = new Intent(CourseDownloadingActivity.this, Courses.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    private final BroadcastReceiver internetInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {

            } else if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            } else {
                if (isActivityLive) {
                    AlertDialogManager.showDialog(CourseDownloadingActivity.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            cancelDownload();
                        }
                    });
                }
            }
        }
    };

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (!spManager.getValueByKeyNameFromDownloadCoursePage("ReferenceID").equals("")) {
                long download_id = Long.parseLong(spManager.getValueByKeyNameFromDownloadCoursePage("ReferenceID"));
                String videoURL = spManager.getValueByKeyNameFromDownloadCoursePage("CourseURL");
                if (referenceId == download_id) {
                    downloading = false;
                    if (url_list.contains(videoURL)) {
                        url_list.remove(url_list.indexOf(videoURL));
                    }
                    if (url_list.size() > 0) {
                        String videoUrl = url_list.get(0);
                        String video_file_name = videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.lastIndexOf("."));
                        spManager.removeValueByKeyNameFromDownloadCoursePage("CourseURL"); //13-Mar-2019 Same URL problem
                        showDownloadProgress(total, video_file_name, videoUrl);
                    } else {
                        loadingTxt.setText(getResources().getString(R.string.downloads_completed));
                        spManager.removeSharedPreferenceByName("DownloadCourse");
                        dbUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "DownloadTime", System.currentTimeMillis() + "");
                        dbUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "Status", "Yes");
                        dbUpdate.updateTable("CoursesTable", spManager.getUserID(), LicenceID, "DownloadTime", System.currentTimeMillis() + "");
                        if (!dbSelect.isSCORMCOontentExists(spManager.getUserID(), LicenceID)) {
                            dbInsert.addDataIntoSCORMTable(LicenceID, spManager.getUserID(), "");
                        }
                        if (!connectionDetector.isConnectingToInternet()) {
                            File rootDir = Environment.getExternalStorageDirectory();
                            File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course");
                            String filePath = root.getAbsolutePath();
                            File dir = new File(filePath);
                            DeleteRecursive(dir);
                        }
                        try {
                            if (internetInfoReceiver != null) {
                                unregisterReceiver(internetInfoReceiver);
                            }
                        } catch (Exception ex) {
                            String reciverException = ex.getMessage();
                            Log.d("internetInfoReceiver",reciverException);
                        }
                        try {
                            if (downloadReceiver != null) {
                                unregisterReceiver(downloadReceiver);
                            }
                        } catch (Exception ex) {
                            String reciverException = ex.getMessage();
                            Log.d("downloadReceiver",reciverException);
                        }
                        Intent goto_intent = new Intent(CourseDownloadingActivity.this, Courses.class);
                        goto_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(goto_intent);
                    }
                }
            }
        }
    };

    public void writeNoMediaFile(String path) {
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            if (path.equals("")) {
                File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course/");
                if (!root.exists()) {
                    root.mkdirs();
                }
                String filePath = root.getAbsolutePath();
                File file = new File(filePath, ".nomedia");
                if (!file.exists()) {
                    file.createNewFile();
                }
            } else {
                File fileToWrite = new File(path);
                if (!fileToWrite.exists()) {
                    fileToWrite.mkdirs();
                }
                File file = new File(path, ".nomedia");
                if (!file.exists()) {
                    file.createNewFile();
                }
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    public void downloadingFailed() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (isDownloadFailed && isActivityLive) {
                    AlertDialogManager.showDialog(CourseDownloadingActivity.this, getResources().getString(R.string.course_download_failed_header), getResources().getString(R.string.course_download_failed_message), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            cancelDownload();
                        }
                    });
                }
            }
        }.execute();
    }

    public void removeDuplicateURL(String videoURL) {
        downloading = false;
        if (url_list.contains(videoURL)) {
            url_list.remove(url_list.indexOf(videoURL));
        }
        if (url_list.size() > 0) {
            String videoUrl = url_list.get(0);
            String video_file_name = videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.lastIndexOf("."));
            spManager.removeValueByKeyNameFromDownloadCoursePage("CourseURL"); //13-Mar-2019 Same URL problem
            showDownloadProgress(total, video_file_name, videoUrl);
        } else {
            loadingTxt.setText(getResources().getString(R.string.downloads_completed));
            spManager.removeSharedPreferenceByName("DownloadCourse");
            dbUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "DownloadTime", System.currentTimeMillis() + "");
            dbUpdate.updateTable("CourseDownload", spManager.getUserID(), LicenceID, "Status", "Yes");
            dbUpdate.updateTable("CoursesTable", spManager.getUserID(), LicenceID, "DownloadTime", System.currentTimeMillis() + "");
            if (!dbSelect.isSCORMCOontentExists(spManager.getUserID(), LicenceID)) {
                dbInsert.addDataIntoSCORMTable(LicenceID, spManager.getUserID(), "");
            }
            if (!connectionDetector.isConnectingToInternet()) {
                File rootDir = Environment.getExternalStorageDirectory();
                File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.Course");
                String filePath = root.getAbsolutePath();
                File dir = new File(filePath);
                DeleteRecursive(dir);
            }
            try {
                if (internetInfoReceiver != null) {
                    unregisterReceiver(internetInfoReceiver);
                }
            } catch (Exception ex) {
            }
            try {
                if (downloadReceiver != null) {
                    unregisterReceiver(downloadReceiver);
                }
            } catch (Exception ex) {
                String reciverException = ex.getMessage();
                Log.d("downloadReceiver",reciverException);
            }
            Intent goto_intent = new Intent(CourseDownloadingActivity.this, Courses.class);
            goto_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goto_intent);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (downloadReceiver != null) {
                unregisterReceiver(downloadReceiver);
            }
        } catch (Exception ex) {
            String reciverException = ex.getMessage();
            Log.d("downloadReceiver",reciverException);
        }
        super.onDestroy();
    }
}