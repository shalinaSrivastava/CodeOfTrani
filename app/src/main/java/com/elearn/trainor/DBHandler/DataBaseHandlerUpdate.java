package com.elearn.trainor.DBHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.elearn.trainor.PropertyClasses.MyCompanyProperty;
import com.elearn.trainor.PropertyClasses.ToolsProperty;

public class DataBaseHandlerUpdate extends DataBaseHandler {

    public DataBaseHandlerUpdate(Context context) {
        super(context);
    }

    public long updateToolBoxDetails(ToolsProperty info, String Type) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                if (Type.equals("Image")) {
                    values.put(TblTool_Key_ICON_STRING, info.iconString);
                    Result = db.update(Table_Tools, values, "ToolID=?", new String[]{info.id});
                } else if (Type.equals("FileDownloaded")) {
                    values.put(TblTool_Key_FILE_DOWNLOADED, "Yes");
                    Result = db.update(Table_Tools, values, "ToolID=?", new String[]{info.id});
                } else if (Type.equals("ToolBoxContent")) {
                    values.put(TblTool_Key_ZIP_EXTRACTED, "Yes");
                    Result = db.update(Table_Tools, values, "ToolID=?", new String[]{info.id});
                } else if (Type.equals("FileDownloadedUpdate")) {
                    values.put(TblTool_Key_FILE_DOWNLOADED, "");
                    Result = db.update(Table_Tools, values, "ToolID=?", new String[]{info.id});
                } else if (Type.equals("ToolBoxUpdated")) {
                    values.put(TblTool_Key_Last_Modified, info.last_modified);
                    values.put(TblTool_Key_FILE_URL, info.file);
                    values.put(TblTool_Key_FILE_NAME, info.name);
                    values.put(TblTool_Key_FILE_SIZE, info.file_size);
                    values.put(TblTool_Key_FORCE_DOWNLOAD, info.force_download);
                    values.put(TblTool_Key_LANGUAGE_CODE, info.language_code);
                    values.put(TblTool_Key_BACKGROUND_COLOUR, info.background_color);
                    values.put(TblTool_Key_IS_LANDSCAPE, info.is_landscape);
                    values.put(TblTool_Key_CUSTOMER_IDS, info.customer_ids);
                    values.put(TblTool_Key_FILE_ICON, info.iconURL);
                    Result = db.update(Table_Tools, values, "ToolID=?", new String[]{info.id});
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long updateNotificationTable(String type, String isEnabled, String userID) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblNotification_Key_IS_ENABLED, isEnabled);
                Result = db.update(Table_Notification, values, "NotificationType=? AND userID=?", new String[]{type, userID});
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long updateNotificationCount(String type, String count, String userID) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblNotificationCount_Key_Notification_Count, count);
                Result = db.update(Table_Notification_Count, values, TblNotificationCount_Key_TYPE + "=? AND userID=?", new String[]{type, userID});
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long updateCourseTableDuration(String language, String duration, int hldrState, String course_id, String from) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                if (from.equals("UpdateDuration")) {
                    values.put(TblCOURSES_Course_Length, duration);
                    Result = db.update(TABLE_COURSES, values, TblCOURSES_Language + "=?", new String[]{language});
                } else if (from.equals("UpdateAll")) {
                    db.execSQL("UPDATE CoursesTable set holderState='" + hldrState + "' ");
                } else {
                    values.put(TblCOURSES_Holder_State, hldrState);
                    Result = db.update(TABLE_COURSES, values, TblCOURSES_Key_Course_ID + "=?", new String[]{course_id});
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long updateImageURLInCourseTable(String courseID, String imageurl) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblCOURSES_DES_ImageUrl, imageurl);
                Result = db.update(TABLE_COURSES, values, "courseId=?", new String[]{courseID});
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long updateSCORMTable(String userID, String licenceID, String columnName, String value) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(columnName, value);
                Result = db.update(TABLE_SCORM, values, "userID=? And LicenceID=?", new String[]{userID, licenceID});
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    // update secretkey
   /* public long updateSecretKeyINSCORMTable(String licenceID, String columnName, String value) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(columnName, value);
                Result = db.update(TABLE_SCORM, values, "LicenceID=?", new String[]{licenceID});
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }*/

    public long updateTable(String tableName, String userID, String licenceID, String columnName, String value) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(columnName, value);
                if (tableName.equals("CoursesTable") || tableName.equals("CourseDownload")) {
                    Result = db.update(tableName, values, "userID=? AND licenseId=?", new String[]{userID, licenceID});
                } else if (tableName.equals("SCORMTable")) {
                    Result = db.update(tableName, values, "userID=? AND LicenceID=?", new String[]{userID, licenceID});
                } else if (tableName.equals("OfflineDownload")) {
                    Result = db.update(tableName, values, "UserId=?", new String[]{userID});
                }
                db.setTransactionSuccessful();
            } catch (Exception ex) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public void updateNewlyCompletedStatus(final String from, final String userID) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (from.equals("SyncedForDiploma")) {
            Query = "Update DiplomasTable set newlyCompleted = 'false' , notToBeDeleted = 'false'  where userID = '" + userID + "' ;";
        } else {
            Query = "Update DiplomasTable set newlyCompleted = 'false' where userID = '" + userID + "' AND completionDate < (Select DATE('now','-13 day')) ;";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                db.rawQuery(Query, null).moveToFirst();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void updateLastModifiedDocumentData(final String userID, MyCompanyProperty content) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues values = new ContentValues();
        long Result = 0;
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblMyCompany_CUSTOMER_ID, content.customerID);
                values.put(TblMyCompany_Key_User_ID, content.userID);
                values.put(TblMyCompany_Key_name, content.name);
                values.put(TblMyCompany_Key_lastModified, content.lastModified);
                values.put(TblMyCompany_Key_description, content.description);
                values.put(TblMyCompany_Key_downloadUrl, content.downloadUrl);
                values.put(TblMyCompany_Key_fileName, content.fileName);
                values.put(TblMyCompany_Key_fileSize, content.fileSize);
                values.put(TblMyCompany_Key_DocumentID, content.documentID);
                Result = db.update(TABLE_MY_COMPANY, values, "userID=? AND documemtId=?", new String[]{userID,content.documentID});
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }
// added new 20-10-2020
public long updateFacilityData(String tableName, String userID, String facilityId, String columnName, String value) {
    long Result = -1;
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    synchronized ("dbLock") {
        try {
            db.beginTransaction();
            values.put(columnName, value);
            Result = db.update(tableName, values, "id=?", new String[]{facilityId});
            db.setTransactionSuccessful();
        } catch (Exception ex) {
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    return Result;
}

}
