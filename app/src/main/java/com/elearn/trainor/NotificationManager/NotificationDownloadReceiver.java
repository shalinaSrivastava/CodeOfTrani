package com.elearn.trainor.NotificationManager;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import androidx.legacy.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.*;

import com.elearn.trainor.ToolBoxModule.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static android.content.Context.DOWNLOAD_SERVICE;

public class NotificationDownloadReceiver extends WakefulBroadcastReceiver {
    SharedPreferenceManager spManager;
    DataBaseHandlerUpdate dbUpdate;

    @Override
    public void onReceive(Context context, Intent intent) {
        spManager = new SharedPreferenceManager(context);
        long downloadReference = 0, downloadCourseReference = 0;
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        if (!spManager.getValueByKeyNameFromDownload("DownloadingReference").equals("")) {
            downloadReference = Long.parseLong(spManager.getValueByKeyNameFromDownload("DownloadingReference"));
            getDownloadStatus(downloadReference, downloadManager, context);
        }
        String course_id = spManager.getValueByKeyNameFromDownloadCoursePage("DownloadCourseID");
        if (!course_id.equals("")) {
            downloadCourseReference = Long.parseLong(spManager.getValueByKeyNameFromDownloadCoursePage(course_id));
            showCOurseDownloadStatus(downloadCourseReference, downloadManager, context, course_id);
        }
    }

    public void getDownloadStatus(long downloadReference, DownloadManager downloadManager, Context context) {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadReference);
        Cursor cursor = downloadManager.query(q);
        if ((cursor != null && cursor.moveToFirst()) && cursor.getCount() > 0) {
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                /*
                String filename = spManager.getValueByKeyNameFromDownload("FileName");
                File rootDir = android.os.Environment.getExternalStorageDirectory();
                File srcFilePath = new File(rootDir.getAbsolutePath() + "/MyTrainor/.movetools/" + filename);
                File desFilePath = new File(rootDir.getAbsolutePath() + "/MyTrainor/.tools/");
                moveFile(srcFilePath, desFilePath);
                downloadManager.remove(downloadReference);
                dbUpdate = new DataBaseHandlerUpdate(context);
                ToolsProperty info = new ToolsProperty();
                info.id = spManager.getValueByKeyNameFromDownload(spManager.getValueByKeyNameFromDownload("DownloadingReference"));
                dbUpdate.updateToolBoxDetails(info, "FileDownloaded");
                spManager.removeSharedPreferenceByName("Download");*/
            } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {
                Intent intentToolBox = new Intent(context, ToolboxLoadingActivity.class);
                intentToolBox.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentToolBox.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentToolBox.putExtra("DownloadingReference", spManager.getValueByKeyNameFromDownload("DownloadingReference"));
                context.startActivity(intentToolBox);
            }
        }
    }

    public void showCOurseDownloadStatus(long downloadReference, DownloadManager downloadManager, Context context, String course_id) {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadReference);
        Cursor cursor = downloadManager.query(q);
        if ((cursor != null && cursor.moveToFirst()) && cursor.getCount() > 0) {
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                dbUpdate = new DataBaseHandlerUpdate(context);
                dbUpdate.updateCourseTableDuration("", "", 3, course_id, "");
                downloadManager.remove(downloadReference);
                spManager.removeSharedPreferenceByName("DownloadCourse");
                //spManager.removeSharedPreferenceByName("DownloadCourse");
            } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {
              /*  Intent intentToolBox = new Intent(context, ToolboxLoadingActivity.class);
                intentToolBox.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentToolBox.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentToolBox.putExtra("DownloadingReference", spManager.getValueByKeyNameFromDownload("DownloadingReference"));
                context.startActivity(intentToolBox);*/
            }
        }
    }

    private void moveFile(File file, File dir) {
        try {
            File newFile = new File(dir, file.getName());
            FileChannel outputChannel = null;
            FileChannel inputChannel = null;
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }
}
