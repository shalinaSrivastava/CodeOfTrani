package com.elearn.trainor.DBHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.elearn.trainor.PropertyClasses.COPProperty;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.PropertyClasses.DSBProperty;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.PropertyClasses.GetMoreCoursesProperty;
import com.elearn.trainor.PropertyClasses.MyCompanyProperty;
import com.elearn.trainor.PropertyClasses.NotificationProperty;
import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.PropertyClasses.ToolsProperty;

import java.util.List;

public class DataBaseHandlerInsert extends DataBaseHandler {

    public DataBaseHandlerInsert(Context context) {
        super(context);
    }

    public void addDataIntoProofileTable(String imageString) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(Key_ProfilePic, imageString);
                Result = db.insert(Table_Profile, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void addDataIntoToolBoxTable(ToolsProperty info) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblTool_Key_ID, info.id);
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
                Result = db.insert(Table_Tools, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void addDataIntoNotificationTable(String type, String isEnabled, String userID) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblNotification_Key_TYPE, type);
                values.put(TblNotification_Key_IS_ENABLED, isEnabled);
                values.put(TblNotification_Key_User_ID, userID);
                Result = db.insert(Table_Notification, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void addDataIntoNotificationCountTable(String type, String userID, String body, String category, String notificationId) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblNotificationCount_Key_User_ID, userID);
                values.put(TblNotificationCount_Key_TYPE, type);
                values.put(TblNotificationCount_Body, body);
                values.put(TblNotificationCount_Category, category);
                values.put(TblNotificationCount_NotificationId_Server, notificationId);
                Result = db.insert(Table_Notification_Count, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void addDataIntoCustomerDetailsTable(CustomerDetailsProperty cust_id) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(CUSTOMER_ID, cust_id.customer_id);
                values.put(TblCustomer_Details_Key_CUSTOMER_NAME, cust_id.customerName);
                values.put(TblCustomer_Details_Key_WORK_EMAIL_ADD, cust_id.workEmailAddress);
                values.put(TblCustomer_Details_Key_DEPARTMENT_NAME, cust_id.departmentName);
                values.put(TblCustomer_Details_Key_EMPLOYEE_NUMBER, cust_id.employeeNumber);
                values.put(TblCustomer_Details_Key_TITLE, cust_id.title);
                values.put(TblCustomer_Details_Key_WORKPHONE, cust_id.workPhone);
                values.put(TblCustomer_Details_Key_HAS_COMP_ACCESS, cust_id.hasCopAccess);
                values.put(TblCustomer_Details_Key_EMAIL_VERIFIED, cust_id.emailVerified);
                values.put(TblCustomer_Details_Key_PHONE_VERIFIED, cust_id.phoneVerified);
                values.put(TblCustomer_Details_Key_IS_PRIVATE, cust_id.isPrivate);

                Result = db.insert(TABLE_CUSTOMERS_DETILS, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    //method for inserting Safety cards data into Safety Cards Table
    public void addDataIntoSafetyCardTable(List<SafetyCardProperty> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                for (SafetyCardProperty content : list) {
                    values.put(TblSafetyCard_Key_Valid_To, content.valid_to);
                    values.put(TblSafetyCard_Key_Valid_From, content.valid_from);
                    values.put(TblSafetyCard_Key_Card_ID, content.card_id);
                    values.put(TblSafetyCard_Key_Company_Name, content.company_name);
                    values.put(TblSafetyCard_Key_Approval_Status, content.approval_status);
                    values.put(TblSafetyCard_Key_Location_Name, content.location_name);
                    values.put(TblSafetyCard_Key_Active_Status, content.active_status);
                    values.put(TblSafetyCard_Key_Card_URL, content.card_url);
                    values.put(TblSafetyCard_Key_ID, content.id);
                    values.put(TblSafetyCard_Key_EmployeeID, content.employeeId);
                    values.put(TblSafetyCard_Key_CustomerId, content.customerId);
                    db.insert(TABLE_SAFETY_CARDS, null, values);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public long addDataIntoDiplomasTable(List<DiplomaProperty> diplomaPropertyList) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                for (DiplomaProperty content : diplomaPropertyList) {
                    values.put(TblDiplomas_Key_Expires_Date, content.expiresDate);
                    values.put(TblDiplomas_Key_Certificate_Available, content.certificateAvailable);
                    values.put(TblDiplomas_Key_User_ID, content.userID);
                    values.put(TblDiplomas_Key_Course_ID, content.courseId);
                    values.put(TblDiplomas_Key_License_ID, content.licenseId);
                    values.put(TblDiplomas_Valid_Until, content.validUntil);
                    values.put(TblDiplomas_Start_Course_URL, content.startCourseUrl);
                    values.put(TblDiplomas_Completion_Date, content.completionDate);
                    values.put(TblDiplomas_Language, content.language);
                    values.put(TblDiplomas_Status, content.status);
                    values.put(TblDiplomas_Start_Date, content.startDate);
                    values.put(TblDiplomas_Course_Name, content.courseName);
                    values.put(TblDiplomas_Available_Offline, content.availableOffline);
                    values.put(TblDiplomas_Diploma_Status, content.diplomaStatus);
                    values.put(TblDiplomas_DES_Content, content.info_content);
                    values.put(TblDiplomas_DES_Goal, content.info_goal);
                    values.put(TblDiplomas_DES_TargetGroup, content.info_targetGroup);
                    values.put(TblDiplomas_Course_Duration, content.courseDuration);
                    values.put(TblDiplomas_Newly_Completed, content.isNewlyCompleted);
                    values.put(TblDiplomas_Not_Delete, content.notToBeDeleted);
                    Result = db.insert(TABLE_DIPLOMAS, null, values);
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

    public long addDataIntoCoursesTable(DiplomaProperty content) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblCOURSES_Key_User_ID, content.userID);
                values.put(TblCOURSES_Key_Expires_Date, content.expiresDate);
                values.put(TblCOURSES_Course_Duration, content.courseDuration);
                values.put(TblCOURSES_Key_Certificate_Available, content.certificateAvailable);
                values.put(TblCOURSES_Key_Course_ID, content.courseId);
                values.put(TblCOURSES_Key_License_ID, content.licenseId);
                values.put(TblCOURSES_Valid_Until, content.validUntil);
                values.put(TblCOURSES_Start_Course_URL, content.startCourseUrl);
                values.put(TblCOURSES_Completion_Date, content.completionDate);
                values.put(TblCOURSES_Language, content.language);
                values.put(TblCOURSES_Status, content.status);
                values.put(TblCOURSES_Start_Date, content.startDate);
                values.put(TblCOURSES_Course_Name, content.courseName);
                values.put(TblCOURSES_Available_Offline, content.availableOffline);
                values.put(TblCOURSES_Holder_State, content.HolderState);
                values.put(TblCOURSES_CourseType, content.courseType);
                values.put(TblCOURSES_Location, content.location);
                values.put(TblCOURSES_DES_Content, content.info_content);
                values.put(TblCOURSES_DES_Goal, content.info_goal);
                values.put(TblCOURSES_DES_TargetGroup, content.info_targetGroup);
                values.put(TblCOURSES_DES_ImageUrl, content.image_URL);
                values.put(TblCOURSES_City, content.courseCity);
                values.put(TblCOURSES_DownloadTime, content.timeStamp);
                Result = db.insert(TABLE_COURSES, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long addDataIntoSCORMTable(String licenceID, String userID, String secretKey) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblSCORM_Key_LicenceID, licenceID);
                values.put(TblSCORM_Key_User_ID, userID);
                values.put(TblSCORM_CMI_Completion_Status, "incomplete");
                values.put(TblSCORM_Status_Completed_On_Live, "No");
                values.put(TblSCORM_SecretKey, secretKey);
                values.put(TblSCORM_Identifier, "");
                values.put(TblSCORM_NewlyCompleted, "No");
                Result = db.insert(TABLE_SCORM, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long addDataIntoMyCompanyTable(MyCompanyProperty content) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
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
                values.put(TblMyCompany_Key_Locale, content.locale);
                Result = db.insert(TABLE_MY_COMPANY, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long addDataIntoCourseDownloadTable(String courseID, String licenceID, String userID) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblCourseDW_CourseID, courseID);
                values.put(TblCourseDW_LicenceID, licenceID);
                values.put(TblCourseDW_User_ID, userID);
                Result = db.insert(TABLE_CourseDownload, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }


    public void insertNewlyCompletedCourseDetailsIntoDiplomaTable(final String licenseID, final String diplomaStatus, final String userID) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Insert into DiplomasTable(userID,expiresDate,courseDuration,certificateAvailable,courseId,licenseId,validUntil,startCourseUrl,completionDate,language,status,startDate,courseName,availableOffline,content,goal,targetGroup,courseDuration,newlyCompleted,DiplomaStatus,notToBeDeleted)\n" +
                " Select CT.userID,CT.expiresDate,CT.courseDuration,CT.certificateAvailable,CT.courseId,CT.licenseId,CT.validUntil,CT.startCourseUrl,CT.completionDate,CT.language,CT.status,CT.startDate,CT.courseName,CT.availableOffline,CT.content,CT.goal,CT.targetGroup,CT.courseDuration,'true','" + diplomaStatus + "','true' From\n" +
                " CoursesTable CT Where CT.userID = '" + userID + "' AND CT.licenseId = '" + licenseID + "'";
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

    public long addDataIntoOfflineDownloadTable(String userId, String diplomaStatus, String toolsStatus, String safetyCardStatus, String documentStatus, String isCompletelyDownloaded, String isLoggedIn) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblUserID_Key_ID, userId);
                values.put(Diploma_Switch_Status_Key_ID, diplomaStatus);
                values.put(Tools_Switch_Status_Key_ID, toolsStatus);
                values.put(SafetyCard_Switch_Status_Key_ID, safetyCardStatus);
                values.put(Documents_Switch_Status_Key_ID, documentStatus);
                values.put(is_downloaded_completely_Key_ID, isCompletelyDownloaded);
                values.put(is_downloaded_Is_Logged_In, isLoggedIn);
                Result = db.insert(Table_OfflineDownload, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long addDataIntoCoursePurchaseTable(final GetMoreCoursesProperty courseDetailProp) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblCPI_CourseID, courseDetailProp.course_id);
                values.put(TblCPI_UUID, courseDetailProp.uuid);
                values.put(TblCPI_Language, courseDetailProp.language);
                values.put(TblCPI_Internal_Name, courseDetailProp.internal_name);
                values.put(TblCPI_Length, courseDetailProp.length);
                values.put(TblCPI_Price, courseDetailProp.price);
                values.put(TblCPI_Price_Inc_Vat, courseDetailProp.price_inc_vat);
                Result = db.insert(Table_GetCoursePurchaseInfo, null, values);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error",ex.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }


    public long addDataIntoCourseDetailTable(final GetMoreCoursesProperty courseDetailProp) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(Tbl_CourseID, courseDetailProp.course_id);
                values.put(Tbl_UUID, courseDetailProp.uuid);
                values.put(Tbl_Language_Type, courseDetailProp.language);
                values.put(Tbl_Title, courseDetailProp.title);
                values.put(Tbl_Intro, courseDetailProp.intro);
                values.put(Tbl_Goal, courseDetailProp.goal);
                values.put(Tbl_Target_Group, courseDetailProp.target_group);
                values.put(Tbl_Description, courseDetailProp.description);
                Result = db.insert(Table_GetCourseDetails, null, values);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error",ex.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long addDataIntoDSBTable(final DSBProperty dsbInfo) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblDSB_Key_ID, dsbInfo.id);
                values.put(TblDSB_last_modified, dsbInfo.last_modified);
                values.put(TblDSB_name, dsbInfo.name);
                values.put(TblDSB_release_date, dsbInfo.release_date);
                values.put(TblDSB_image, dsbInfo.imageURL);
                values.put(TblDSB_file, dsbInfo.fileURL);
                values.put(TblDSB_file_size, dsbInfo.file_size);
                values.put(TblDSB_dsb_order, dsbInfo.order);
                Result = db.insert(Table_DSB, null, values);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error",ex.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public long addDataIntoNotificationUpdateTable(final NotificationProperty notificationProperty) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblUpdNtfction_userID, notificationProperty.user_id);
                values.put(TblUpdNtfction_notificationType, notificationProperty.notification_type);
                values.put(TblUpdNtfction_notificationCount, notificationProperty.notification_count);
                values.put(TblUpdNtfction_deviceType, notificationProperty.device_type);
                values.put(TblUpdNtfction_deviceID, notificationProperty.device_id);
                values.put(TblUpdNtfction_updatedOnServer, "No");
                values.put(TblUpdNtfction_Notification_IDs, notificationProperty.notification_id);
                Result = db.insert(Table_UpdateNotification, null, values);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage().toString());
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return Result;
    }

    public void addDataIntoCOPTable(COPProperty info) {
        long Result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        synchronized ("dbLock") {
            try {
                db.beginTransaction();
                values.put(TblUserId, info.userId);
                values.put(TblLisenceId, info.lisenceId);
                values.put(TblCardStatus, info.cardStatus);
                values.put(TblCourseStatus, info.courseStatus);
                values.put(TblRegistrationDate, info.regdDate);
                values.put(TblPlacePlatform, info.placePlatformId);
                values.put(TblDepartment, info.departmentId);
                values.put(TblTopicDiscussed, info.topicDiscussed);
                values.put(TblRiskIdentified, info.riskIdentified);
                values.put(TblPlannedFollowUp, info.plannedFollowUp);
                values.put(TblSwitchHeatCold, info.heatColdStatus);
                values.put(TblSwitchPressure, info.pressureStatus);
                values.put(TblSwitchChemical, info.chemicalStatus);
                values.put(TblSwitchElectrical, info.electricalStatus);
                values.put(TblSwitchGravity, info.gravityStatus);
                values.put(TblSwitchRadiation, info.radiationStatus);
                values.put(TblSwitchNoise, info.noiseStatus);
                values.put(TblSwitchBiological, info.biologicalStatus);
                values.put(TblSwitchEnergyMovement, info.energyMovementStatus);
                values.put(TblSwitchPresentMoment, info.presentMomentStatus);
                Result = db.insert(Table_COMP, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }
}


