package com.elearn.trainor.DBHandler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.PropertyClasses.COPProperty;
import com.elearn.trainor.PropertyClasses.CheckedInFacilityProperty;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.DSBProperty;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.PropertyClasses.GetMoreCoursesProperty;
import com.elearn.trainor.PropertyClasses.MyCompanyProperty;
import com.elearn.trainor.PropertyClasses.NotificationProperty;
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.PropertyClasses.SCORMInfo;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.PropertyClasses.ToolsProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataBaseHandlerSelect extends DataBaseHandler {

    public DataBaseHandlerSelect(Context context) {
        super(context);
    }

    public String getProfileImageFromProfileDetail() {
        String imageString = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select * from " + Table_Profile;
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            imageString = cursor.getString(cursor.getColumnIndex("ProfilePic"));
                            Log.d("", "");
                        } while (cursor.moveToNext());
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return imageString;
    }

    public boolean isLicenseIDExistsInDiplomaTable(final String licenseID) {
        boolean isExist = false;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select * from DiplomasTable where licenseId = '" + licenseID + "' order by validUntil desc";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        isExist = true;
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return isExist;
    }

    public String getLastModifiedDataFromToolTable(String tool_id) {
        String last_modified = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select IfNull(LastModified,'') as last_modified  From ToolBox where ToolID = '" + tool_id + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            last_modified = cursor.getString(0);
                            Log.d("", "");
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return last_modified;
    }

    public String getFileDownloadedFromToolTable(String tool_id) {
        String last_modified = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select FileDownloaded From ToolBox where ToolID = '" + tool_id + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            last_modified = cursor.getString(0);
                            Log.d("", "");
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return last_modified;
    }

    public String getNotificationData(String tableName, String columnName, String type, String userID, String columnType) {
        String isExists = "";
        if (columnType.equals("NotificationCount") && isExists.equals("")) {
            isExists = "0";
        }
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (columnType.equals("NotificationCount")) {
            Query = "Select IfNull(" + columnName + ",'0') as colName From " + tableName + " where userID = '" + userID + "' AND NotificationType = '" + type + "'";
        } else {
            Query = "Select IfNull(" + columnName + ",'') as colName From " + tableName + " where userID = '" + userID + "' AND NotificationType = '" + type + "'";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            isExists = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
            } catch (Exception ex) {
                Log.d("", ex.getMessage());
            } finally {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }
        return isExists;
    }

    public List<ToolsProperty> getDownlodedToolDetails(String type, String deviceLang) {
        List<ToolsProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (type.equals("ToolBox")) {
            Query = "Select ToolID,FileName,FileSize,LastModified,Force_Download,Language_Code,Background_Colour,Is_Landscape,Customer_ids,File_Icon,IconString,FileURL from ToolBox";
        } else if (type.equals("Downloads")) {
            String isEnabled = "Yes";
            Query = "Select ToolID,FileName,FileSize,LastModified,Force_Download,Language_Code,Background_Colour,Is_Landscape,Customer_ids,File_Icon,IconString,FileURL from ToolBox Where Force_Download = '0' AND IfNull(FileDownloaded,'No') = '" + isEnabled + "' AND Language_Code = '" + deviceLang + "'";
        }/* else if (type.equals("Downloads")) {
            String isEnabled = "Yes";
            Query = "Select ToolID,FileName,FileSize,LastModified,Force_Download,Language_Code,Background_Colour,Is_Landscape,Customer_ids,File_Icon,IconString,FileURL from ToolBox Where Force_Download = '0' And IfNull(FileDownloaded,'No') = '" + isEnabled + "'";
        }*/ else {
            Query = "Select ToolID,FileName,FileSize,LastModified,Force_Download,Language_Code,Background_Colour,Is_Landscape,Customer_ids,File_Icon,IconString,FileURL from ToolBox Where IfNull(ToolID,'') = '" + type + "'";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            ToolsProperty property = new ToolsProperty();
                            property.id = cursor.getString(0);
                            property.name = cursor.getString(1);
                            property.file_size = cursor.getString(2);
                            property.last_modified = cursor.getString(3);
                            property.force_download = cursor.getString(4);
                            property.language_code = cursor.getString(5);
                            property.background_color = cursor.getString(6);
                            property.is_landscape = cursor.getString(7);
                            property.customer_ids = cursor.getString(8);
                            property.iconURL = cursor.getString(9);
                            property.iconString = cursor.getString(10);
                            property.file = cursor.getString(11);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<CustomerDetailsProperty> getCustomerIdFromCustomerDetails(String showDevButton) {
        List<CustomerDetailsProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (showDevButton.equals("Yes")) {
            Query = "Select Customer_id, customerName,workEmailAddress,departmentName,employeeNumber,title,workPhone from CustomerDetails where Customer_id = '" + WebServicesURL.DevBtn_Hide_Customer_ID + "'";
        } else {
            Query = "Select Customer_id, customerName,workEmailAddress,departmentName,employeeNumber,title,workPhone from CustomerDetails";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            CustomerDetailsProperty property = new CustomerDetailsProperty();
                            property.customer_id = cursor.getString(0);
                            property.customerName = cursor.getString(1);
                            property.workEmailAddress = cursor.getString(2);
                            property.departmentName = cursor.getString(3);
                            property.employeeNumber = cursor.getString(4);
                            property.title = cursor.getString(5);
                            property.workPhone = cursor.getString(6);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<SafetyCardProperty> getSafetyCardAttribute(String card_id) {
        List<SafetyCardProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (card_id.equals("")) {
            Query = "Select company_name,IfNull(valid_to,'null') as valid_to,card_id,ifNull(card_url,'') as card_url,IfNull(valid_from,'null') as valid_from,approval_status,location_name from SafetyCards";
        } else {
            Query = "Select company_name,IfNull(valid_to,'null') as valid_to,card_id,ifNull(card_url,'') as card_url,IfNull(valid_from,'null') as valid_from,approval_status,location_name from SafetyCards Where card_id = '" + card_id + "'";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            SafetyCardProperty property = new SafetyCardProperty();
                            property.company_name = cursor.getString(0);
                            property.valid_to = cursor.getString(1);
                            property.card_id = cursor.getString(2);
                            property.card_url = cursor.getString(3);
                            property.valid_from = cursor.getString(4);
                            property.approval_status = cursor.getString(5);
                            property.location_name = cursor.getString(6);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<DiplomaProperty> getDiplomaPageDetailsList(final String from, final String userID) {
        List<DiplomaProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (from.equals("CoursePage")) {
            Query = "Select expiresDate,certificateAvailable,courseId,licenseId,IfNull(validUntil,'') as validuntil,startCourseUrl,IfNull(completionDate,'') as completeddate,language,status,IfNull(startDate,'')as startdate,courseName,IfNull(DiplomaStatus,'')as diplomaStatus,availableOffline,IfNull(content,'') as courseContent,IfNull(goal,'')as courseGoal,IfNull(targetGroup,'') as courseTarget,IfNull(courseDuration,'') as courseDuration from DiplomasTable where userID = '" + userID + "' AND newlyCompleted = 'true' AND DiplomaStatus <> 'expired' order by validUntil desc";
        } else {
            Query = "Select expiresDate,certificateAvailable,courseId,licenseId,IfNull(validUntil,'') as validuntil,startCourseUrl,IfNull(completionDate,'') as completeddate,language,status,IfNull(startDate,'')as startdate,courseName,IfNull(DiplomaStatus,'')as diplomaStatus,availableOffline,IfNull(content,'') as courseContent,IfNull(goal,'')as courseGoal,IfNull(targetGroup,'') as courseTarget,IfNull(courseDuration,'') as courseDuration from DiplomasTable where userID = '" + userID + "' order by validUntil desc";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            DiplomaProperty property = new DiplomaProperty();
                            property.expiresDate = cursor.getString(0);
                            property.certificateAvailable = cursor.getString(1);
                            property.courseId = cursor.getString(2);
                            property.licenseId = cursor.getString(3);
                            property.validUntil = cursor.getString(4);
                            property.startCourseUrl = cursor.getString(5);
                            property.completionDate = cursor.getString(6);
                            property.language = cursor.getString(7);
                            property.status = cursor.getString(8);
                            property.startDate = cursor.getString(9);
                            property.courseName = cursor.getString(10);
                            property.diplomaStatus = cursor.getString(11);
                            property.availableOffline = cursor.getString(12);
                            property.info_content = cursor.getString(13);
                            property.info_goal = cursor.getString(14);
                            property.info_targetGroup = cursor.getString(15);
                            property.courseDuration = cursor.getString(16);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<DiplomaProperty> getCoursesDetailsList(String courseType, String courseName, String userID) {
        List<DiplomaProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (!courseName.equals("")) {
            Query = "Select expiresDate,courseDuration,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,availableOffline,holderState,CourseType,Location,content,goal,targetGroup,IfNull(CourseImageURL,'')as imgURL,IfNull(CourseFileSize,'') as fileSize,IfNull(CourseCity,'') as city from CoursesTable where userID = '" + userID + "' AND courseName = '" + courseName + "' order by DownloadTime desc";
        } else {
            if (courseType.equals("")) {
                Query = "Select expiresDate,courseDuration,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,availableOffline,holderState,CourseType,Location,content,goal,targetGroup,IfNull(CourseImageURL,'')as imgURL,IfNull(CourseFileSize,'') as fileSize,IfNull(CourseCity,'') as city from CoursesTable where  userID = '" + userID + "' order by DownloadTime desc";
            } else {
                Query = "Select expiresDate,courseDuration,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,availableOffline,holderState,CourseType,Location,content,goal,targetGroup,IfNull(CourseImageURL,'')as imgURL,IfNull(CourseFileSize,'') as fileSize,IfNull(CourseCity,'') as city from CoursesTable where  userID = '" + userID + "' AND  CourseType = '" + courseType + "' order by DownloadTime desc";
            }
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            DiplomaProperty property = new DiplomaProperty();
                            property.expiresDate = cursor.getString(0);
                            property.courseDuration = cursor.getString(1);
                            property.certificateAvailable = cursor.getString(2);
                            property.courseId = cursor.getString(3);
                            property.licenseId = cursor.getString(4);
                            property.validUntil = cursor.getString(5);
                            property.startCourseUrl = cursor.getString(6);
                            property.completionDate = cursor.getString(7);
                            property.language = cursor.getString(8);
                            property.status = cursor.getString(9);
                            property.startDate = cursor.getString(10);
                            property.courseName = cursor.getString(11);
                            property.availableOffline = cursor.getString(12);
                            property.HolderState = cursor.getInt(13);
                            property.courseType = cursor.getString(14);
                            property.location = cursor.getString(15);
                            property.info_content = cursor.getString(16);
                            property.info_goal = cursor.getString(17);
                            property.info_targetGroup = cursor.getString(18);
                            property.image_URL = cursor.getString(19);
                            property.fileSize = cursor.getString(20);
                            property.courseCity = cursor.getString(21);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
                String msg = e.getMessage();
                Log.d("", msg);
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public String getSCORMStatusFromSCORMTable(String userID, String licenceID, String columnName) {
        String scorm_status = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select IfNull(" + columnName + ",'undefined') From SCORMTable where userID='" + userID + "'  And LicenceID = '" + licenceID + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            scorm_status = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return scorm_status;
    }

    public boolean isSCORMCOontentExists(String userID, String licence_id) {
        boolean isExists = false;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select * From SCORMTable where userID='" + userID + "' And LicenceID = '" + licence_id + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            isExists = true;
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d("", e.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return isExists;
    }

    public List<MyCompanyProperty> getMyCompanyContent(String customerID, final String userID, String lang) {
        List<MyCompanyProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (lang.equals("")) {
            Query = "Select name,lastModified,description,downloadUrl,fileName,fileSize,MyCompanyId,Customer_id,IfNull(locale,'nb_No')as localeValue from MyCompanyTable where Customer_id = '" + customerID + "' and userID = '" + userID + "' Order By name";
        } else {
            Query = "Select name,lastModified,description,downloadUrl,fileName,fileSize,MyCompanyId,Customer_id,IfNull(locale,'nb_No')as localeValue from MyCompanyTable where Customer_id = '" + customerID + "' and userID = '" + userID + "' and locale ='" + lang + "' Order By name";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            MyCompanyProperty property = new MyCompanyProperty();
                            property.name = cursor.getString(0);
                            property.lastModified = cursor.getString(1);
                            property.description = cursor.getString(2);
                            property.downloadUrl = cursor.getString(3);
                            property.fileName = cursor.getString(4);
                            property.fileSize = cursor.getString(5);
                            property.MyCompanyId = cursor.getString(6);
                            property.customerID = cursor.getString(7);
                            property.locale = cursor.getString(8);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public CopyOnWriteArrayList<SCORMInfo> getCoursesFromSCORM(String userID, String From, String licenseIDs) {
        CopyOnWriteArrayList<SCORMInfo> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (From.equals("CompletedNotUpdated")) {
            if (licenseIDs.equals("")) {
                Query = "Select scormID,LicenceID,cmiLocation,cmiProgressMeasure,cmiCompletionStatus,cmiSuccessStatus,IfNull(secretKey,'') as SecretKey from SCORMTable where userID = '" + userID + "' AND cmiCompletionStatus = 'completed' AND  StatusCompletedOnLive = 'No'";
            } else {
                Query = "Select scormID,LicenceID,cmiLocation,cmiProgressMeasure,cmiCompletionStatus,cmiSuccessStatus,IfNull(secretKey,'') as SecretKey from SCORMTable where userID = '" + userID + "' AND cmiCompletionStatus = 'completed' AND  StatusCompletedOnLive = 'No' AND LicenceID NOT IN (" + licenseIDs + ")";
            }
        } else {
            if (From.equals("")) {
                Query = "Select scormID,LicenceID,cmiLocation,cmiProgressMeasure,cmiCompletionStatus,cmiSuccessStatus,IfNull(secretKey,'') as SecretKey from SCORMTable where userID = '" + userID + "' AND StatusCompletedOnLive = 'No'";
            } else if (From.equals("InsertCompletedCourseIntoDiploma")) {
                Query = "Select LicenceID from SCORMTable where userID = '" + userID + "' AND NewlyCompleted = 'Yes' ";
            } else {
                Query = "SELECT ST.LicenceID FROM SCORMTable ST Left Outer JOIN CourseDownload CD on ST.LicenceID=CD.licenseId Where ST.userID='" + userID + "' AND IfNull(CD.Status,'No')='No'";
            }
        }


        /*if (From.equals("")) {
            Query = "Select scormID,LicenceID,cmiLocation,cmiProgressMeasure,cmiCompletionStatus,cmiSuccessStatus,IfNull(secretKey,'') as SecretKey from SCORMTable where userID = '" + userID + "' AND StatusCompletedOnLive = 'No'";
        } else if (From.equals("InsertCompletedCourseIntoDiploma")) {
            Query = "Select LicenceID from SCORMTable where userID = '" + userID + "' AND NewlyCompleted = 'Yes' ";
        } else {
            Query = "SELECT ST.LicenceID FROM SCORMTable ST Left Outer JOIN CourseDownload CD on ST.LicenceID=CD.licenseId Where ST.userID='" + userID + "' AND IfNull(CD.Status,'No')='No'";
        }*/
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            SCORMInfo info = new SCORMInfo();
                            if (From.equals("") || From.equals("CompletedNotUpdated")) {
                                info.scormID = cursor.getString(0);
                                info.LicenceID = cursor.getString(1);
                                info.cmiLocation = cursor.getString(2);
                                info.cmiProgressMeasure = cursor.getString(3);
                                info.cmiCompletionStatus = cursor.getString(4);
                                info.cmiSuccessStatus = cursor.getString(5);
                                info.secretKey = cursor.getString(6);
                            } else {
                                info.LicenceID = cursor.getString(0);
                            }
                            list.add(info);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<DiplomaProperty> getDiplomaListToShow(final String from, final String userID) {
        List<DiplomaProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (from.equals("active")) {
            Query = "Select expiresDate,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,DiplomaStatus,availableOffline,IfNull(content,'') as courseContent,IfNull(goal,'')as courseGoal,IfNull(targetGroup,'') as courseTarget,IfNull(courseDuration,'') as courseDuration from DiplomasTable where userID = '" + userID + "' AND DiplomaStatus='active' order by completionDate desc";
        } else if (from.equals("expired")) {
            Query = "Select expiresDate,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,DiplomaStatus,availableOffline,IfNull(content,'') as courseContent,IfNull(goal,'')as courseGoal,IfNull(targetGroup,'') as courseTarget,IfNull(courseDuration,'') as courseDuration from DiplomasTable where userID = '" + userID + "' AND  DiplomaStatus='expired' order by validUntil desc";
        } else {
            Query = "Select expiresDate,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,DiplomaStatus,availableOffline,IfNull(content,'') as courseContent,IfNull(goal,'')as courseGoal,IfNull(targetGroup,'') as courseTarget,IfNull(courseDuration,'') as courseDuration from DiplomasTable where userID = '" + userID + "' AND  DiplomaStatus='Completed Offline' order by validUntil desc";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            DiplomaProperty property = new DiplomaProperty();
                            property.expiresDate = cursor.getString(0);
                            property.certificateAvailable = cursor.getString(1);
                            property.courseId = cursor.getString(2);
                            property.licenseId = cursor.getString(3);
                            property.validUntil = cursor.getString(4);
                            property.startCourseUrl = cursor.getString(5);
                            property.completionDate = cursor.getString(6);
                            property.language = cursor.getString(7);
                            property.status = cursor.getString(8);
                            property.startDate = cursor.getString(9);
                            property.courseName = cursor.getString(10);
                            property.diplomaStatus = cursor.getString(11);
                            property.availableOffline = cursor.getString(12);
                            property.info_content = cursor.getString(13);
                            property.info_goal = cursor.getString(14);
                            property.info_targetGroup = cursor.getString(15);
                            property.courseDuration = cursor.getString(16);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public String getDataFromCourseDownloadTable(String columnName, String userID, String course_id, String licence_id) {
        String dwTime = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select " + columnName + " From CourseDownload where userID = '" + userID + "' AND courseId = '" + course_id + "' And licenseId = '" + licence_id + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            dwTime = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return dwTime;
    }

    public String getDataFromCoursesTable(String columnName, String userID, String licenceID) {
        String isExists = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select " + columnName + " from CoursesTable where userID = '" + userID + "' AND licenseId='" + licenceID + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (columnName.equals("*")) {
                    if (cursor.getCount() > 0) {
                        if (cursor.moveToFirst()) {
                            do {
                                isExists = "Yes";
                            } while (cursor.moveToNext());
                        }
                    }
                } else {
                    if (cursor.getCount() > 0) {
                        if (cursor.moveToFirst()) {
                            do {
                                isExists = cursor.getString(0);
                            } while (cursor.moveToNext());
                        }
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return isExists;
    }

    public boolean getCompletionDateDifference(String completedDate) {
        boolean shouldSave = false;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select 'True' where DATE('" + completedDate + "') between DATE('now','-13 day') AND DATE('now')";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            shouldSave = true;
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return shouldSave;
    }

    public List<String> tempSCORMData(String userID, String licenceID, String columnName) {
        List<String> scorm_status = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select " + columnName + " From SCORMTable where userID='" + userID + "' And LicenceID = '" + licenceID + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            scorm_status.add(cursor.getString(0));
                            scorm_status.add(cursor.getString(1));
                            scorm_status.add(cursor.getString(2));
                            scorm_status.add(cursor.getString(3));
                            scorm_status.add(cursor.getString(4));
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return scorm_status;
    }

    public CopyOnWriteArrayList<SCORMInfo> getSCORMValuesForNewPostAPI(String userID, String licenseIDs, String type) {
        CopyOnWriteArrayList<SCORMInfo> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (type.equals("CompletionApprove")) {
            Query = "Select LicenceID,IfNull(secretKey,'') as SecretKey from SCORMTable where userID = '" + userID + "' AND LicenceID IN (" + licenseIDs + ") AND StatusCompletedOnLive = 'No' AND cmiProgressMeasure = '1.0'";
        } else {
            Query = "Select scormID,LicenceID,cmiLocation,cmiProgressMeasure,cmiCompletionStatus,cmiSuccessStatus,IfNull(secretKey,'') as SecretKey,IfNull(identifier,'') as Identifier from SCORMTable where userID = '" + userID + "' AND LicenceID IN (" + licenseIDs + ")";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (type.equals("CompletionApprove")) {
                                SCORMInfo info = new SCORMInfo();
                                info.LicenceID = cursor.getString(0);
                                info.secretKey = cursor.getString(1);
                                list.add(info);
                            } else {
                                SCORMInfo info = new SCORMInfo();
                                info.scormID = cursor.getString(0);
                                info.LicenceID = cursor.getString(1);
                                info.cmiLocation = cursor.getString(2);
                                info.cmiProgressMeasure = cursor.getString(3);
                                info.cmiCompletionStatus = cursor.getString(4);
                                info.cmiSuccessStatus = cursor.getString(5);
                                info.secretKey = cursor.getString(6);
                                info.identifier = cursor.getString(7);
                                list.add(info);
                            }
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<DiplomaProperty> getDownloadedCourseFilesFromCourseDownloadTable(String columnName, String userID, final String licenseId, final String from) {
        List<DiplomaProperty> columnValues = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";

        if (from.equals("DownloadedCourseDetailsFromDiploma")) {
            Query = "Select expiresDate,courseDuration,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,availableOffline from DiplomasTable where userID = '" + userID + "' AND licenseId = '" + licenseId + "'";
        } else if (from.equals("DownloadedCourseDetailsFromCourse")) {
            Query = "Select expiresDate,courseDuration,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,availableOffline from CoursesTable where userID = '" + userID + "' AND licenseId = '" + licenseId + "'";
        } else {
            Query = "Select " + columnName + " From CourseDownload where userID = '" + userID + "'";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            DiplomaProperty property = new DiplomaProperty();
                            if (from.equals("DownloadedCourseDetailsFromDiploma") || from.equals("DownloadedCourseDetailsFromCourse")) {
                                property.expiresDate = cursor.getString(0);
                                property.courseDuration = cursor.getString(1);
                                property.certificateAvailable = cursor.getString(2);
                                property.courseId = cursor.getString(3);
                                property.licenseId = cursor.getString(4);
                                property.validUntil = cursor.getString(5);
                                property.startCourseUrl = cursor.getString(6);
                                property.completionDate = cursor.getString(7);
                                property.language = cursor.getString(8);
                                property.status = cursor.getString(9);
                                property.startDate = cursor.getString(10);
                                property.courseName = cursor.getString(11);
                                property.availableOffline = cursor.getString(12);
                            } else {
                                property.downloadedStatus = cursor.getString(0);
                                property.licenseId = cursor.getString(1);
                                property.completionDate = cursor.getString(2);
                            }
                            columnValues.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return columnValues;
    }

    public String getStstusFromOfflineDownloadTable(String User_ID, String switchType, String from) {
        String switchStatus = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (from.equals("Login")) {
            Query = "Select " + switchType + " from OfflineDownload where UserId = '" + User_ID + "'";
        } else if (from.equals("LoginPage")) {
            Query = "Select isDownloadedCompletely from OfflineDownload where UserId = '" + User_ID + "'";
        } else {
            Query = "Select " + switchType + " from OfflineDownload where UserId = '" + User_ID + "'";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            switchStatus = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return switchStatus;
    }

    public boolean isCompanyDocDataExist(String User_ID, String customerID, String documentID) {
        boolean isExist = false;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select Customer_id from MyCompanyTable where userID = '" + User_ID + "' AND Customer_id = '" + customerID + "' AND documemtId='" + documentID + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    isExist = true;
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return isExist;
    }

    public String getLastModifiedDataFromMyCompanyTable(String documet_id) {
        String last_modified = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select IfNull(lastModified,'') as lm  From MyCompanyTable where documemtId = '" + documet_id + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            last_modified = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return last_modified;
    }

    public List<GetMoreCoursesProperty> getDataFromCoursePurchaseTable(final String condition, final String from) {
        List<GetMoreCoursesProperty> coursePurchaseList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (from.equals("")) {
            if (condition.equals("")) {
                Query = "Select courseID,uuid,language,internalName,Length,price,priceIncVat from CoursePurchaseInfo";
            } else {
                Query = "Select courseID,uuid,language,internalName,Length,price,priceIncVat from CoursePurchaseInfo where " + condition;
            }
        } else if (from.equals("FilteredData")) {
            Query = "SELECT CD.UUID,CD.languageType,CD.title,CD.intro,CD.goal,CD.targetGroup,CD.description,CP.Length,CP.priceIncVat,CP.courseID FROM CourseDetail as CD Inner Join CoursePurchaseInfo as CP on CP.uuid = CD.uuid where " + condition;
        } else {
            Query = "SELECT CD.UUID,CD.languageType,CD.title,CD.intro,CD.goal,CD.targetGroup,CD.description,CP.Length,CP.priceIncVat,CP.courseID FROM CourseDetail as CD Inner Join CoursePurchaseInfo as CP on CP.uuid = CD.uuid where " + condition;
        }

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (from.equals("")) {
                                GetMoreCoursesProperty coursePurchaseInfo = new GetMoreCoursesProperty();
                                coursePurchaseInfo.course_id = cursor.getString(0);
                                coursePurchaseInfo.uuid = cursor.getString(1);
                                coursePurchaseInfo.language = cursor.getString(2);
                                coursePurchaseInfo.internal_name = cursor.getString(3);
                                coursePurchaseInfo.length = cursor.getString(4);
                                coursePurchaseInfo.price = cursor.getString(5);
                                coursePurchaseInfo.price_inc_vat = cursor.getString(6);
                                coursePurchaseList.add(coursePurchaseInfo);
                            } else {
                                if (from.equals("GetMoreCourse")) {
                                    GetMoreCoursesProperty coursePurchaseInfo = new GetMoreCoursesProperty();
                                    coursePurchaseInfo.course_id = cursor.getString(9);
                                    coursePurchaseInfo.uuid = cursor.getString(0);
                                    coursePurchaseInfo.language = cursor.getString(1);
                                    coursePurchaseInfo.internal_name = cursor.getString(2);
                                    coursePurchaseInfo.length = cursor.getString(7);
                                    coursePurchaseInfo.price_inc_vat = cursor.getString(8);
                                    coursePurchaseList.add(coursePurchaseInfo);
                                } else if (from.equals("FilteredData")) {
                                    GetMoreCoursesProperty coursePurchaseInfo = new GetMoreCoursesProperty();
                                    coursePurchaseInfo.course_id = cursor.getString(9);
                                    coursePurchaseInfo.uuid = cursor.getString(0);
                                    coursePurchaseInfo.language = cursor.getString(1);
                                    coursePurchaseInfo.internal_name = cursor.getString(2);
                                    coursePurchaseInfo.length = cursor.getString(7);
                                    coursePurchaseInfo.price_inc_vat = cursor.getString(8);
                                    coursePurchaseList.add(coursePurchaseInfo);
                                } else {
                                    GetMoreCoursesProperty coursePurchaseInfo = new GetMoreCoursesProperty();
                                    coursePurchaseInfo.uuid = cursor.getString(0);
                                    coursePurchaseInfo.language = cursor.getString(1);
                                    coursePurchaseInfo.title = cursor.getString(2);
                                    coursePurchaseInfo.intro = cursor.getString(3);
                                    coursePurchaseInfo.goal = cursor.getString(4);
                                    coursePurchaseInfo.target_group = cursor.getString(5);
                                    coursePurchaseInfo.description = cursor.getString(6);
                                    coursePurchaseInfo.length = cursor.getString(7);
                                    coursePurchaseInfo.price_inc_vat = cursor.getString(8);
                                    coursePurchaseList.add(coursePurchaseInfo);
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return coursePurchaseList;
    }

    public String getLanguageFromTable_GetCoursePurchaseInfo(String uuID) {
        String language = "";
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select IfNull(language,'') as lang  From CoursePurchaseInfo where uuid = '" + uuID + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            language = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return language;
    }

    public boolean isCourseInfoTableExist() {
        boolean isExist = false;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select * from CoursePurchaseInfo";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    isExist = true;
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return isExist;
    }

    public List<DSBProperty> getDSBDataFromTable() {
        List<DSBProperty> dsbPropertyList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select DSBID,ifNull(name,'')as dsbName,IfNull(image,'') as dsbImage, ifNull(file,'') as dsbFile, releaseDate from DSBTable";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            DSBProperty info = new DSBProperty();
                            info.id = cursor.getString(0);
                            info.name = cursor.getString(1);
                            info.imageURL = cursor.getString(2);
                            info.fileURL = cursor.getString(3);
                            info.release_date = cursor.getString(4);
                            dsbPropertyList.add(info);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return dsbPropertyList;
    }

    public List<NotificationProperty> getNotificationListFromTable(String userId) {
        List<NotificationProperty> notificationPropertiesList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select NotificationID,userID,NotificationBody,NotificationCategory from NotificationCountTable  where userID = '" + userId + "' ";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            NotificationProperty info = new NotificationProperty();
                            info.notification_id = cursor.getString(0);
                            info.user_id = cursor.getString(1);
                            info.notification_category = cursor.getString(3);
                            info.notification_body = cursor.getString(2);
                            notificationPropertiesList.add(info);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return notificationPropertiesList;
    }

    public boolean isNotificationIDExists(String userId, String notificationID) {
        boolean isExists = false;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select NotificationID,userID,NotificationBody,NotificationCategory from NotificationCountTable  where userID = '" + userId + "' AND NotificationID_Server = '" + notificationID + "'";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            isExists = true;
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return isExists;
    }

    public List<NotificationProperty> getDataFromNotificationUpdateTable(String userId, String notificationType) {
        List<NotificationProperty> notificationPropertiesList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (notificationType.equals("ClearAll")) {
            Query = "Select group_concat(notificationType,',') , group_concat(notificationIDs,',') from UpdateNotification where userID = '" + userId + "' AND updatedOnServer = 'No'";
        } else {
            Query = "Select ,userID,notificationType,notificationCount,deviceType,deviceID from UpdateNotification where userID = '" + userId + "' AND notificationType = '" + notificationType + "' AND updatedOnServer = 'No'";
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            NotificationProperty info = new NotificationProperty();
                            if (notificationType.equals("ClearAll")) {
                                info.notification_type = cursor.getString(0);
                                info.notification_id = cursor.getString(1);
                            } else {
                                info.notification_id = cursor.getString(0);
                                info.user_id = cursor.getString(1);
                                info.notification_type = cursor.getString(2);
                                info.notification_count = cursor.getInt(3);
                                info.device_type = cursor.getString(4);
                                info.device_id = cursor.getString(5);
                            }
                            notificationPropertiesList.add(info);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return notificationPropertiesList;
    }

    public NotificationProperty notificationDataToBeUpdated(String tableName, String userID, String type) {
        NotificationProperty notificationProperty = null;
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        if (tableName.equals("UpdateNotification")) {
            if (type.equals("ClearAll")) {
                Query = "Select notificationCount, group_concat(notificationIDs,','),group_concat(notificationType,','),group_concat(_ID,',') From " + tableName + " where userID = '" + userID + "'";
            } else {
                Query = "Select notificationCount, group_concat(notificationIDs,','),group_concat(notificationType,','),group_concat(_ID,',') From " + tableName + " where userID = '" + userID + "' AND notificationType = '" + type + "'";
            }
        } else {
            if (type.equals("ClearAll")) {
                Query = "Select NotificationCount, group_concat(NotificationID_Server,',') From " + tableName + " where userID = '" + userID + "'";
            } else {
                Query = "Select NotificationCount, group_concat(NotificationID_Server,',') From " + tableName + " where userID = '" + userID + "' AND NotificationType = '" + type + "'";
            }
        }
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    notificationProperty = new NotificationProperty();
                    if (cursor.moveToFirst()) {
                        do {
                            notificationProperty.notification_count = Integer.parseInt(cursor.getString(0));
                            notificationProperty.notification_id = cursor.getString(1);
                            if (tableName.equals("UpdateNotification")) {
                                notificationProperty.notification_type = cursor.getString(2);
                                notificationProperty.user_id = cursor.getString(3);
                            }
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return notificationProperty;
    }

    public List<String> documentLocaleList(String customerID, final String userID) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select distinct IfNull(locale,'nb_NO')as localeValue from MyCompanyTable where Customer_id = '" + customerID + "' and userID = '" + userID + "' Order By name";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            list.add(cursor.getString(0));
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<CustomerDetailsProperty> getCompUser(String type) {
        List<CustomerDetailsProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        Query = "Select hasCopAccess from CustomerDetails where hasCopAccess = '" + type + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            CustomerDetailsProperty property = new CustomerDetailsProperty();
                            property.hasCopAccess = cursor.getString(0);

                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    public List<COPProperty> getNotUploadedCOPCards(final String userID) {
        List<COPProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";

        Query = "Select CardID,UserID,LisenceId,CardStatus,CourseStatus,RegdDate,PlacePlatform,Department,TopicDiscussed,RiskIdentified,PlannedFollowUp,HeatColdStatus,PressureStatus,ChemicalStatus,ElectricalStatus,GravityStatus,RadiationStatus,NoiseStatus,BiologicalStatus,EnergyMovementStatus,PresentMomentStatus from CompUser where UserID = '" + userID + "'";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {

                        do {
                            COPProperty copProperty = new COPProperty();
                            copProperty.cardID = cursor.getInt(0);
                            copProperty.userId = cursor.getString(1);
                            copProperty.lisenceId = cursor.getString(2);
                            copProperty.cardStatus = cursor.getString(3);
                            copProperty.courseStatus = cursor.getString(4);
                            copProperty.regdDate = cursor.getString(5);
                            copProperty.placePlatformId = cursor.getString(6);
                            copProperty.departmentId = cursor.getString(7);
                            copProperty.topicDiscussed = cursor.getString(8);
                            copProperty.riskIdentified = cursor.getString(9);
                            copProperty.plannedFollowUp = cursor.getString(10);
                            copProperty.heatColdStatus = cursor.getString(11);
                            copProperty.pressureStatus = cursor.getString(12);
                            copProperty.chemicalStatus = cursor.getString(13);
                            copProperty.electricalStatus = cursor.getString(14);
                            copProperty.gravityStatus = cursor.getString(15);
                            copProperty.radiationStatus = cursor.getString(16);
                            copProperty.noiseStatus = cursor.getString(17);
                            copProperty.biologicalStatus = cursor.getString(18);
                            copProperty.energyMovementStatus = cursor.getString(19);
                            copProperty.presentMomentStatus = cursor.getString(20);

                            list.add(copProperty);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }


    //added on 03-09-2020
    public List<CustomerDetailsProperty> getCustomerListForFacility() {
        List<CustomerDetailsProperty> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "";
        Query = "Select Customer_id,customerName,workEmailAddress,departmentName,employeeNumber,title,workPhone,emailVerified,phoneVerified,isPrivate from CustomerDetails";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            CustomerDetailsProperty property = new CustomerDetailsProperty();
                            property.customer_id = cursor.getString(0);
                            property.customerName = cursor.getString(1);
                            property.workEmailAddress = cursor.getString(2);
                            property.departmentName = cursor.getString(3);
                            property.employeeNumber = cursor.getString(4);
                            property.title = cursor.getString(5);
                            property.workPhone = cursor.getString(6);
                            property.emailVerified = cursor.getString(7);
                            property.phoneVerified = cursor.getString(8);
                            property.isPrivate = cursor.getString(9);
                            list.add(property);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }
    //added on 16-09-2020
    public List<String> getSafetyCardidByCustId(String customerID) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select id from SafetyCards where customerId = '" + customerID + "'";
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            list.add(cursor.getString(0));
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return list;
    }

    // added on 28-09-2020
    public String getCheckedInStatusFromEntryTable(String userID, String facilityID) {
        String scorm_status = "", Query ="";
        SQLiteDatabase db = getReadableDatabase();
        if(userID.equals("GetEntryId")){
            Query = "Select id From ReportEntry where facilityId = '" + facilityID + "'";
        }else{
           Query = "Select state From ReportEntry where userID='" + userID + "'  And facilityId = '" + facilityID + "'";
        }

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            scorm_status = cursor.getString(0);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return scorm_status;
    }

    public List<ReportEntryProperty> getFacilityListFromReportEntryTable(String userId, String facilityState) {
        List<ReportEntryProperty> reportEntryPropertyList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select userId,id,checkOutMessage,timestamp,state,numberOfGuests,employeeId,securityServicePhone,safetycardId,facilityName,facilityId,estimatedDurationOfVisitInSeconds,facilityLatitude,facilityLongitude from ReportEntry  where userID = '" + userId + "' AND state = '" +facilityState+ "' ";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            ReportEntryProperty info = new ReportEntryProperty();
                            info.userId = cursor.getString(0);
                            info.entryId = cursor.getString(1);
                            info.checkOutMessage = cursor.getString(2);
                            info.timestamp = cursor.getString(3);
                            info.state = cursor.getString(4);
                            info.numberOfGuests = cursor.getString(5);
                            info.employeeId = cursor.getString(6);
                            info.securityServicePhone = cursor.getString(7);
                            info.safetycardId = cursor.getString(8);
                            info.facilityName = cursor.getString(9);
                            info.facilityId = cursor.getString(10);
                            info.estimatedDurationOfVisitInSeconds = cursor.getString(11);
                            info.facilityLatitude = cursor.getString(12);
                            info.facilityLongitude = cursor.getString(13);
                            reportEntryPropertyList.add(info);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return reportEntryPropertyList;
    }

    public List<FacilityProperty> getFacilityListFromFacilityTable(String facilityState) {
        List<FacilityProperty> facilityPropertyList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select id,name,customerId,customerName,employeeCheckinState,imageUrl,distanceInKm,allowGuests,latitude,longitude from FacilityTable  where employeeCheckinState <> '" +facilityState+ "' ";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            FacilityProperty info = new FacilityProperty();
                            info.id = cursor.getString(0);
                            info.name = cursor.getString(1);
                            info.customerId = cursor.getString(2);
                            info.customerName = cursor.getString(3);
                            info.employeeCheckInState = cursor.getString(4);
                            info.imageUrl = cursor.getString(5);
                            info.distanceInKm = cursor.getString(6);
                            info.allowGuests = cursor.getString(7);
                            info.latitude = cursor.getString(8);
                            info.longitude = cursor.getString(9);

                            facilityPropertyList.add(info);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return facilityPropertyList;
    }

    /*public List<CheckedInFacilityProperty> getFacilityListFromCheckedInFacilityTable(String userId, String facilityState) {
        List<CheckedInFacilityProperty> checkedInFacilityList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select userId,id,checkOutMessage,timestamp,state,employeeId,facilityName,facilityId,estimatedDurationOfVisitInSeconds from CheckedInFacility  where userID = '" + userId + "' AND state = '" +facilityState+ "' ";

        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            CheckedInFacilityProperty info = new CheckedInFacilityProperty();
                            info.userId = cursor.getString(0);
                            info.entryId = cursor.getString(1);
                            info.checkOutMessage = cursor.getString(2);
                            info.timestamp = cursor.getString(3);
                            info.state = cursor.getString(4);
                            info.employeeId = cursor.getString(5);;
                            info.facilityName = cursor.getString(6);
                            info.facilityId = cursor.getString(7);
                            info.estimatedDurationOfVisitInSeconds = cursor.getString(8);
                        } while (cursor.moveToNext());
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return checkedInFacilityList;
    }*/
}