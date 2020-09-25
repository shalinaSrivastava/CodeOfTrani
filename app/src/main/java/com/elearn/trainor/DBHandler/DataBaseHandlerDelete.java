package com.elearn.trainor.DBHandler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataBaseHandlerDelete extends DataBaseHandler {


    public DataBaseHandlerDelete(Context context) {
        super(context);
    }

    public long deleteTableByName(String TableName, String userID) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            synchronized ("dbLock") {
                db.beginTransaction();
                if (TableName.equals("CourseDownload")) {
                    Result = db.delete(TableName, "userID=?", new String[]{userID});
                } else if (TableName.equals("DiplomasTable")) {
                    Result = db.delete(TableName, "userID=? AND notToBeDeleted=?", new String[]{userID, "false"});
                } else if (TableName.equals("MyCompanyTable")) {
                    Result = db.delete(TableName, "userID=?", new String[]{userID});
                } else {
                    Result = db.delete(TableName, null, null);
                }
                db.setTransactionSuccessful();
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return Result;
    }

  /*  public long deleteTable(String TableName, String columnName, String[] args) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            synchronized ("dbLock") {
                if (TableName.equals("UpdateNotification")) {
                    String Query = "Delete From UpdateNotification where userID='" + args[0] + "' AND updatedOnServer='No' AND notificationType in (" + args[2] + ");";
                    try {
                        db.beginTransaction();
                        db.rawQuery(Query, null).moveToFirst();
                        db.setTransactionSuccessful();
                    } catch (Exception ex) {
                        Log.d("", ex.getMessage());
                    }
                } else {
                    db.beginTransaction();
                    Result = db.delete(TableName, columnName, args);
                    db.setTransactionSuccessful();
                }
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return Result;
    }*/

    public long deleteTable(String TableName, String columnName, String[] args) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            synchronized ("dbLock") {
                db.beginTransaction();
                Result = db.delete(TableName, columnName, args);
                db.setTransactionSuccessful();
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return Result;
    }

    public long deleteValueFromTable(String tableName, String keyName, String key_id) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        synchronized ("dbLock") {
            db.beginTransaction();
            Result = db.delete(tableName, keyName + "=?", new String[]{key_id});
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
        return Result;
    }

    public void deleteCoursesFromTable(String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (tableName.equals("SCORMTable")) {
            Query = "Delete From SCORMTable where StatusCompletedOnLive = 'Yes' AND CompletionDate < (Select DATE('now','-6 day'));";
        } else {
            if (tableName.equals("CompletionDateCourseTable")) {
                Query = "Delete From CourseDownload where CompletionDate > (Select DATE('now'));";
            } else if (tableName.equals("CompletionDateSCORMTable")) {
                Query = "Delete From SCORMTable where StatusCompletedOnLive = 'Yes' AND CompletionDate > (Select DATE('now'));";
            } else {
                if (tableName.equals("CourseDownload")) {
                    Query = "Delete From CourseDownload where CompletionDate < (Select DATE('now','-13 day'));";
                } else if (tableName.equals("offline")) {
                    Query = "Delete From CoursesTable where licenseId IN (Select licenseId From CourseDownload where CompletionDate < (Select DATE('now','-6 day')));";
                } else {
                    Query = "Delete From CoursesTable where licenseId NOT IN (Select licenseId From CourseDownload);";
                }
            }
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

    public void deleteMyCompanyIDsNotExists(String IDs, String CustomerID) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Delete From MyCompanyTable where Customer_id = '"+CustomerID+"' AND documemtId NOT IN (" + IDs + ");";
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

    public void deleteNotificationUpdatedData(String tableName, String userID, String IDs, String notificationType) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (tableName.equals("NotificationCountTable")) {
            Query = "Delete From " + tableName + " where userID= '" + userID + "' AND NotificationID_Server IN (" + IDs + ");";
        } else {
            //Query = "Delete From " + tableName + " where userID= '" + userID + "' AND notificationType = '" + notificationType + "' AND notificationIDs = '" + IDs + "' ;";
            Query = "Delete From " + tableName + " where userID= '" + userID + "' AND _ID IN (" + notificationType + ");";
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

    public void deleteUploadedCardFromCOP(int CardId) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Delete From CompUser where CardID = "+CardId;
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


    //18_Mar_2019
    public void deleteCoursesNotExist(String userID, String licenseIDs) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Delete From CoursesTable where userID = '" + userID + "' AND licenseId NOT IN (" + licenseIDs + ");";
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
}
