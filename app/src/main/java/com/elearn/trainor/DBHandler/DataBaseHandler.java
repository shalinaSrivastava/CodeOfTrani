package com.elearn.trainor.DBHandler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHandler extends SQLiteOpenHelper {
    protected static final int DATABASE_VERSION = 40;
    protected static final String DATABASE_NAME = "TrainorDB.db";

    protected static String Table_Profile = "ProfileDetail";
    protected static String Key_ProfilePic = "ProfilePic";

    protected static String Table_Tools = "ToolBox";
    protected static String TblTool_Key_ID = "ToolID";
    protected static String TblTool_Key_Last_Modified = "LastModified";
    protected static String TblTool_Key_FILE_URL = "FileURL";
    protected static String TblTool_Key_FILE_NAME = "FileName";
    protected static String TblTool_Key_FILE_SIZE = "FileSize";
    protected static String TblTool_Key_FORCE_DOWNLOAD = "Force_Download";
    protected static String TblTool_Key_LANGUAGE_CODE = "Language_Code";
    protected static String TblTool_Key_BACKGROUND_COLOUR = "Background_Colour";
    protected static String TblTool_Key_IS_LANDSCAPE = "Is_Landscape";
    protected static String TblTool_Key_CUSTOMER_IDS = "Customer_ids";
    protected static String TblTool_Key_FILE_ICON = "File_Icon";
    protected static String TblTool_Key_FILE_DOWNLOADED = "FileDownloaded";
    protected static String TblTool_Key_ICON_STRING = "IconString";
    protected static String TblTool_Key_ZIP_EXTRACTED = "ZipExtracted";

    protected static String Table_Notification = "NotificationTable";
    protected static String TblNotification_Key_ID = "NotificationID";
    protected static String TblNotification_Key_User_ID = "userID";
    protected static String TblNotification_Key_TYPE = "NotificationType";
    protected static String TblNotification_Key_IS_ENABLED = "IsEnabled";

    protected static String Table_Notification_Count = "NotificationCountTable";
    protected static String TblNotificationCount_Key_ID = "NotificationID";
    protected static String TblNotificationCount_Key_User_ID = "userID";
    protected static String TblNotificationCount_Key_TYPE = "NotificationType";
    protected static String TblNotificationCount_Key_Notification_Count = "NotificationCount";
    protected static String TblNotificationCount_Body = "NotificationBody";  // new added 13-08-2017
    protected static String TblNotificationCount_Category = "NotificationCategory"; // new added 13-08-2017
    protected static String TblNotificationCount_NotificationId_Server = "NotificationID_Server"; // new added 11-09-2018

    protected static String TABLE_CUSTOMERS_DETILS = "CustomerDetails";
    protected static String CUSTOMER_ID = "Customer_id";
    protected static String TblCustomer_Details_Key_CUSTOMER_NAME = "customerName";
    protected static String TblCustomer_Details_Key_WORK_EMAIL_ADD = "workEmailAddress";
    protected static String TblCustomer_Details_Key_DEPARTMENT_NAME = "departmentName";
    protected static String TblCustomer_Details_Key_EMPLOYEE_NUMBER = "employeeNumber";
    protected static String TblCustomer_Details_Key_TITLE = "title";
    protected static String TblCustomer_Details_Key_WORKPHONE = "workPhone";
    protected static String TblCustomer_Details_Key_HAS_COMP_ACCESS = "hasCopAccess"; // new added 23-12-2019
    protected static String TblCustomer_Details_Key_EMAIL_VERIFIED = "emailVerified"; // new added 03-09-2020
    protected static String TblCustomer_Details_Key_PHONE_VERIFIED = "phoneVerified"; // new added 03-09-2020
    protected static String TblCustomer_Details_Key_IS_PRIVATE = "isPrivate"; // new added 03-09-2020

    protected static String TABLE_SAFETY_CARDS = "SafetyCards";
    protected static String TblSafetyCard_Key_Valid_To = "valid_to";
    protected static String TblSafetyCard_Key_Valid_From = "valid_from";
    protected static String TblSafetyCard_Key_Card_ID = "card_id";
    protected static String TblSafetyCard_Key_Company_Name = "company_name";
    protected static String TblSafetyCard_Key_Approval_Status = "approval_status";
    protected static String TblSafetyCard_Key_Location_Name = "location_name";
    protected static String TblSafetyCard_Key_Active_Status = "active_status";
    protected static String TblSafetyCard_Key_Card_URL = "card_url";
    protected static String TblSafetyCard_Key_ID = "id";
    protected static String TblSafetyCard_Key_EmployeeID = "employeeId";
    protected static String TblSafetyCard_Key_CustomerId = "customerId";// new added 16-09-2020
    protected static String TblSafetyCard_Key_Confirmed = "confirmed";// new added 26-10-2020


    protected static String TABLE_DIPLOMAS = "DiplomasTable";
    protected static String TblDiplomas_Key_Expires_Date = "expiresDate";
    protected static String TblDiplomas_Key_Certificate_Available = "certificateAvailable";
    protected static String TblDiplomas_Key_User_ID = "userID";   // new field added
    protected static String TblDiplomas_Key_Course_ID = "courseId";
    protected static String TblDiplomas_Key_License_ID = "licenseId";
    protected static String TblDiplomas_Valid_Until = "validUntil";
    protected static String TblDiplomas_Start_Course_URL = "startCourseUrl";
    protected static String TblDiplomas_Completion_Date = "completionDate";
    protected static String TblDiplomas_Language = "language";
    protected static String TblDiplomas_Status = "status";
    protected static String TblDiplomas_Start_Date = "startDate";
    protected static String TblDiplomas_Course_Name = "courseName";
    protected static String TblDiplomas_Available_Offline = "availableOffline";
    protected static String TblDiplomas_Diploma_Status = "DiplomaStatus";
    protected static String TblDiplomas_DES_Content = "content";
    protected static String TblDiplomas_DES_Goal = "goal";
    protected static String TblDiplomas_DES_TargetGroup = "targetGroup";
    protected static String TblDiplomas_Course_Duration = "courseDuration";
    protected static String TblDiplomas_Newly_Completed = "newlyCompleted";   // new field added
    protected static String TblDiplomas_Not_Delete = "notToBeDeleted";   // new field added

    protected static String TABLE_COURSES = "CoursesTable";
    protected static String TblCOURSES_Key_User_ID = "userID";
    protected static String TblCOURSES_Key_Expires_Date = "expiresDate";
    protected static String TblCOURSES_Course_Duration = "courseDuration";
    protected static String TblCOURSES_Key_Certificate_Available = "certificateAvailable";
    protected static String TblCOURSES_Key_Course_ID = "courseId";
    protected static String TblCOURSES_Key_License_ID = "licenseId";
    protected static String TblCOURSES_Valid_Until = "validUntil";
    protected static String TblCOURSES_Start_Course_URL = "startCourseUrl";
    protected static String TblCOURSES_Completion_Date = "completionDate";
    protected static String TblCOURSES_Language = "language";
    protected static String TblCOURSES_Status = "status";
    protected static String TblCOURSES_Start_Date = "startDate";
    protected static String TblCOURSES_Course_Name = "courseName";
    protected static String TblCOURSES_Available_Offline = "availableOffline";
    protected static String TblCOURSES_Course_Length = "courseLength";
    protected static String TblCOURSES_Holder_State = "holderState";
    protected static String TblCOURSES_CourseType = "CourseType";
    protected static String TblCOURSES_Location = "Location";
    protected static String TblCOURSES_DES_Content = "content";
    protected static String TblCOURSES_DES_Goal = "goal";
    protected static String TblCOURSES_DES_TargetGroup = "targetGroup";
    protected static String TblCOURSES_DES_ImageUrl = "CourseImageURL";
    protected static String TblCOURSES_FileSize = "CourseFileSize";
    protected static String TblCOURSES_DownloadTime = "DownloadTime";
    protected static String TblCOURSES_City = "CourseCity";

    protected static String TABLE_SCORM = "SCORMTable";
    protected static String TblSCORM_Key_SCORMID = "scormID";
    protected static String TblSCORM_Key_User_ID = "userID";
    protected static String TblSCORM_Key_LicenceID = "LicenceID";
    protected static String TblSCORM_Key_CMILocation = "cmiLocation";
    protected static String TblSCORM_Key_Length_CMI_Progress_Measure = "cmiProgressMeasure";
    protected static String TblSCORM_CMI_Completion_Status = "cmiCompletionStatus";
    protected static String TblSCORM_CMI_Success_Status = "cmiSuccessStatus";
    protected static String TblSCORM_ADL_Nav_Request = "adlNavRequest";
    protected static String TblSCORM_Started_Time = "started";
    protected static String TblSCORM_Status_Completed_On_Live = "StatusCompletedOnLive";
    protected static String TblSCORM_CompletionDate = "CompletionDate";
    protected static String TblSCORM_SecretKey = "secretKey";
    protected static String TblSCORM_Identifier = "identifier";
    protected static String TblSCORM_NewlyCompleted = "NewlyCompleted";   // new field added

    protected static String TABLE_MY_COMPANY = "MyCompanyTable";
    protected static String TblMyCompany_Key_User_ID = "userID";        // New Field
    protected static String TblMyCompany_ID = "MyCompanyId";
    protected static String TblMyCompany_CUSTOMER_ID = "Customer_id";
    protected static String TblMyCompany_Key_name = "name";
    protected static String TblMyCompany_Key_lastModified = "lastModified";
    protected static String TblMyCompany_Key_description = "description";
    protected static String TblMyCompany_Key_downloadUrl = "downloadUrl";
    protected static String TblMyCompany_Key_fileName = "fileName";
    protected static String TblMyCompany_Key_fileSize = "fileSize";
    protected static String TblMyCompany_Key_DocumentID = "documemtId";  // new field 07-05-2018
    protected static String TblMyCompany_Key_Locale = "locale";      // new field 01-10-2019

    protected static String TABLE_CourseDownload = "CourseDownload";
    protected static String TblCourseDW_ID = "CourseDownloadID";
    protected static String TblCourseDW_User_ID = "userID";
    protected static String TblCourseDW_CourseID = "courseId";
    protected static String TblCourseDW_LicenceID = "licenseId";
    protected static String TblCourseDW_Status = "Status";
    protected static String TblCourseDW_DWTime = "DownloadTime";
    protected static String TblCourseDW_CompletionDate = "CompletionDate";
    protected static String TblCourseDW_CompletionDateStatus = "CompletionDateStatus";

    protected static String Table_Firebase = "FirebaseTable";
    protected static String TblFirebase_Key_ID = "FirebaseID";
    protected static String TblFirebase_Firebase_Token = "FirebaseToken";
    protected static String TblFirebase_Token_Inserted = "FirebaseTokenInserted";

    protected static String Table_OfflineDownload = "OfflineDownload";
    protected static String TblUserID_Key_ID = "UserId";
    protected static String Diploma_Switch_Status_Key_ID = "DiplomaSwitchStatus";
    protected static String Tools_Switch_Status_Key_ID = "ToolsSwitchStatus";
    protected static String SafetyCard_Switch_Status_Key_ID = "SafetyCardSwitchStatus";
    protected static String Documents_Switch_Status_Key_ID = "DocumentSwitchStatus";
    protected static String is_downloaded_completely_Key_ID = "isDownloadedCompletely";
    protected static String is_downloaded_Is_Logged_In = "isLoggedIn";

    protected static String Table_GetCoursePurchaseInfo = "CoursePurchaseInfo";
    protected static String TblCPI_CourseID = "courseID";
    protected static String TblCPI_UUID = "uuid";
    protected static String TblCPI_Language = "language";
    protected static String TblCPI_Internal_Name = "internalName";
    protected static String TblCPI_Length = "Length";
    protected static String TblCPI_Price = "price";
    protected static String TblCPI_Price_Inc_Vat = "priceIncVat";

    protected static String Table_GetCourseDetails = "CourseDetail";
    protected static String Tbl_CourseID = "courseID";
    protected static String Tbl_UUID = "uuid";
    protected static String Tbl_Language_Type = "languageType";
    protected static String Tbl_Title = "title";
    protected static String Tbl_Intro = "intro";
    protected static String Tbl_Goal = "goal";
    protected static String Tbl_Target_Group = "targetGroup";
    protected static String Tbl_Description = "description";

    protected static String Table_DSB = "DSBTable"; // new table created 07-08-2018
    protected static String TblDSB_Key_ID = "DSBID";
    protected static String TblDSB_last_modified = "lastModified";
    protected static String TblDSB_name = "name";
    protected static String TblDSB_release_date = "releaseDate";
    protected static String TblDSB_image = "image";
    protected static String TblDSB_file = "file";
    protected static String TblDSB_file_size = "fileSize";
    protected static String TblDSB_dsb_order = "dsbOrder";

    protected static String Table_UpdateNotification = "UpdateNotification";  // new table created 27-08-2018
    protected static String TblUpdNtfction_Key_ID = "_ID";
    protected static String TblUpdNtfction_userID = "userID";
    protected static String TblUpdNtfction_notificationType = "notificationType";
    protected static String TblUpdNtfction_notificationCount = "notificationCount";
    protected static String TblUpdNtfction_deviceType = "deviceType";
    protected static String TblUpdNtfction_deviceID = "deviceID";
    protected static String TblUpdNtfction_updatedOnServer = "updatedOnServer";
    protected static String TblUpdNtfction_Notification_IDs = "notificationIDs";

    protected static String Table_COMP = "CompUser";
    protected static String TblID = "CardID";
    protected static String TblUserId = "UserID";
    protected static String TblLisenceId = "LisenceId";
    protected static String TblCardStatus = "CardStatus";
    protected static String TblCourseStatus = "CourseStatus";
    protected static String TblRegistrationDate = "RegdDate";
    protected static String TblPlacePlatform = "PlacePlatform";
    protected static String TblDepartment = "Department";
    protected static String TblTopicDiscussed = "TopicDiscussed";
    protected static String TblRiskIdentified = "RiskIdentified";
    protected static String TblPlannedFollowUp = "PlannedFollowUp";
    protected static String TblSwitchHeatCold = "HeatColdStatus";
    protected static String TblSwitchPressure = "PressureStatus";
    protected static String TblSwitchChemical = "ChemicalStatus";
    protected static String TblSwitchElectrical = "ElectricalStatus";
    protected static String TblSwitchGravity = "GravityStatus";
    protected static String TblSwitchRadiation = "RadiationStatus";
    protected static String TblSwitchNoise = "NoiseStatus";
    protected static String TblSwitchBiological = "BiologicalStatus";
    protected static String TblSwitchEnergyMovement = "EnergyMovementStatus";
    protected static String TblSwitchPresentMoment = "PresentMomentStatus";
    protected String CREATE_COP_TABLE;

    protected String CREATE_FACILITY_TABLE;// new table created 28-09-2020
    protected static String Table_Facility = "FacilityTable";
    protected static String FACILITY_ID = "id";
    protected static String FACILITY_Name = "name";
    protected static String FACILITY_CustomerId = "customerId";
    protected static String FACILITY_CustomerName = "customerName";
    protected static String FACILITY_EmployeeCheckinState = "employeeCheckinState";
    protected static String FACILITY_ImageUrl = "imageUrl";
    protected static String FACILITY_DistanceInKm = "distanceInKm";
    protected static String FACILITY_AllowGuests = "allowGuests";
    protected static String FACILITY_Latitude = "latitude";
    protected static String FACILITY_Longitude = "longitude";

    protected String CREATE_REPORTENTRY_TABLE;// new table created 28-09-2020
    protected static String Table_ReportEntry = "ReportEntry";
    protected static String REPORTENTRY_userId = "userId";
    protected static String REPORTENTRY_EntryId = "id";
    protected static String REPORTENTRY_checkOutMessage = "checkOutMessage";
    protected static String REPORTENTRY_timestamp = "timestamp";
    protected static String REPORTENTRY_state = "state";
    protected static String REPORTENTRY_numberOfGuests = "numberOfGuests";
    protected static String REPORTENTRY_employeeId = "employeeId";
    protected static String REPORTENTRY_securityServicePhone = "securityServicePhone";
    protected static String REPORTENTRY_safetycardId = "safetycardId";
    protected static String REPORTENTRY_facilityName = "facilityName";
    protected static String REPORTENTRY_facilityId = "facilityId";
    protected static String REPORTENTRY_estimatedDurationOfVisitInSeconds = "estimatedDurationOfVisitInSeconds";
    protected static String REPORTENTRY_facilityLatitude = "facilityLatitude";
    protected static String REPORTENTRY_facilityLongitude = "facilityLongitude";

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_PROFILE_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Profile + " ( " + Key_ProfilePic + " Text )";

            String CREATE_TOOLBOX_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Tools + " ( " + TblTool_Key_ID + " TEXT, "
                    + TblTool_Key_Last_Modified + " TEXT, "
                    + TblTool_Key_FILE_URL + " TEXT, "
                    + TblTool_Key_FILE_NAME + " TEXT, "
                    + TblTool_Key_FILE_SIZE + " TEXT, "
                    + TblTool_Key_FORCE_DOWNLOAD + " TEXT, "
                    + TblTool_Key_LANGUAGE_CODE + " TEXT, "
                    + TblTool_Key_BACKGROUND_COLOUR + " TEXT, "
                    + TblTool_Key_IS_LANDSCAPE + " TEXT, "
                    + TblTool_Key_CUSTOMER_IDS + " TEXT, "
                    + TblTool_Key_FILE_ICON + " TEXT, "
                    + TblTool_Key_FILE_DOWNLOADED + " TEXT, "
                    + TblTool_Key_ICON_STRING + " TEXT, "
                    + TblTool_Key_ZIP_EXTRACTED + " TEXT ) ";

            String CREATE_NOTIFICATION_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Notification + " ( " + TblNotification_Key_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblNotification_Key_User_ID + " TEXT, "
                    + TblNotification_Key_TYPE + " TEXT, "
                    + TblNotification_Key_IS_ENABLED + " TEXT ) ";

            String CREATE_NOTIFICATION_COUNT_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Notification_Count + " ( " + TblNotificationCount_Key_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblNotificationCount_Key_User_ID + " TEXT, "
                    + TblNotificationCount_Key_TYPE + " TEXT, "
                    + TblNotificationCount_Key_Notification_Count + " TEXT, "
                    + TblNotificationCount_Body + " TEXT, "
                    + TblNotificationCount_Category + " TEXT, "
                    + TblNotificationCount_NotificationId_Server + " TEXT ) ";


            String CREATE_CUSTOMER_DETAILS_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_CUSTOMERS_DETILS + " ( " + CUSTOMER_ID + " Text, "
                    + TblCustomer_Details_Key_CUSTOMER_NAME + " TEXT, "
                    + TblCustomer_Details_Key_WORK_EMAIL_ADD + " TEXT, "
                    + TblCustomer_Details_Key_DEPARTMENT_NAME + " TEXT, "
                    + TblCustomer_Details_Key_EMPLOYEE_NUMBER + " TEXT, "
                    + TblCustomer_Details_Key_TITLE + " TEXT, "
                    + TblCustomer_Details_Key_WORKPHONE + " TEXT, "
                    + TblCustomer_Details_Key_HAS_COMP_ACCESS + " TEXT, "
                    + TblCustomer_Details_Key_EMAIL_VERIFIED + " TEXT, "
                    + TblCustomer_Details_Key_PHONE_VERIFIED + " TEXT, "
                    + TblCustomer_Details_Key_IS_PRIVATE + " TEXT )";

            String CREATE_SAFETY_CARD_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_SAFETY_CARDS + "(" + TblSafetyCard_Key_Valid_To + " TEXT, "
                    + TblSafetyCard_Key_Valid_From + " TEXT, "
                    + TblSafetyCard_Key_Card_ID + " TEXT, "
                    + TblSafetyCard_Key_Company_Name + " TEXT, "
                    + TblSafetyCard_Key_Approval_Status + " TEXT, "
                    + TblSafetyCard_Key_Location_Name + " TEXT, "
                    + TblSafetyCard_Key_Active_Status + " TEXT, "
                    + TblSafetyCard_Key_Card_URL + " TEXT, "
                    + TblSafetyCard_Key_ID + " TEXT, "
                    + TblSafetyCard_Key_EmployeeID + " TEXT, "
                    + TblSafetyCard_Key_CustomerId + " TEXT, "
                    + TblSafetyCard_Key_Confirmed + " TEXT ) ";

            String CREATE_DIPLOMAS_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_DIPLOMAS + " ( " + TblDiplomas_Key_Expires_Date + " TEXT, "
                    + TblDiplomas_Key_Certificate_Available + " TEXT, "
                    + TblDiplomas_Key_User_ID + " TEXT, "
                    + TblDiplomas_Key_Course_ID + " TEXT, "
                    + TblDiplomas_Key_License_ID + " TEXT, "
                    + TblDiplomas_Valid_Until + " TEXT, "
                    + TblDiplomas_Start_Course_URL + " TEXT, "
                    + TblDiplomas_Completion_Date + " TEXT, "
                    + TblDiplomas_Language + " TEXT, "
                    + TblDiplomas_Status + " TEXT, "
                    + TblDiplomas_Start_Date + " TEXT, "
                    + TblDiplomas_Course_Name + " TEXT, "
                    + TblDiplomas_Available_Offline + " TEXT, "
                    + TblDiplomas_Diploma_Status + " TEXT, "
                    + TblDiplomas_DES_Content + " TEXT, "
                    + TblDiplomas_DES_Goal + " TEXT, "
                    + TblDiplomas_DES_TargetGroup + " TEXT, "
                    + TblDiplomas_Course_Duration + " TEXT, "
                    + TblDiplomas_Newly_Completed + " TEXT, "
                    + TblDiplomas_Not_Delete + " TEXT ) ";

            String CREATE_COURSES_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_COURSES + " ( " + TblCOURSES_Key_Expires_Date + " TEXT, "
                    + TblCOURSES_Key_User_ID + " TEXT, "
                    + TblCOURSES_Course_Duration + " TEXT, "
                    + TblCOURSES_Key_Certificate_Available + " TEXT, "
                    + TblCOURSES_Key_Course_ID + " TEXT, "
                    + TblCOURSES_Key_License_ID + " TEXT, "
                    + TblCOURSES_Valid_Until + " TEXT, "
                    + TblCOURSES_Start_Course_URL + " TEXT, "
                    + TblCOURSES_Completion_Date + " TEXT, "
                    + TblCOURSES_Language + " TEXT, "
                    + TblCOURSES_Status + " TEXT, "
                    + TblCOURSES_Start_Date + " TEXT, "
                    + TblCOURSES_Course_Name + " TEXT, "
                    + TblCOURSES_Available_Offline + " TEXT, "
                    + TblCOURSES_Course_Length + " TEXT, "
                    + TblCOURSES_Holder_State + " INTEGER, "
                    + TblCOURSES_CourseType + " TEXT, "
                    + TblCOURSES_Location + " TEXT, "
                    + TblCOURSES_DES_Content + " TEXT, "
                    + TblCOURSES_DES_Goal + " TEXT, "
                    + TblCOURSES_DES_TargetGroup + " TEXT ,"
                    + TblCOURSES_DES_ImageUrl + " TEXT,"
                    + TblCOURSES_FileSize + " TEXT, "
                    + TblCOURSES_DownloadTime + " TEXT,"
                    + TblCOURSES_City + " TEXT ) ";

            String CREATE_SCORM_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_SCORM + " ( " + TblSCORM_Key_SCORMID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblSCORM_Key_User_ID + " TEXT, "
                    + TblSCORM_Key_LicenceID + " TEXT, "
                    + TblSCORM_Key_CMILocation + " TEXT, "
                    + TblSCORM_Key_Length_CMI_Progress_Measure + " TEXT, "
                    + TblSCORM_CMI_Completion_Status + " TEXT, "
                    + TblSCORM_CMI_Success_Status + " TEXT, "
                    + TblSCORM_ADL_Nav_Request + " TEXT, "
                    + TblSCORM_Status_Completed_On_Live + " TEXT, "
                    + TblSCORM_Started_Time + " TEXT, "
                    + TblSCORM_CompletionDate + " TEXT, "
                    + TblSCORM_SecretKey + " TEXT, "
                    + TblSCORM_Identifier + " TEXT, "
                    + TblSCORM_NewlyCompleted + " TEXT ) ";

            String CREATE_MY_COMPANY_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_MY_COMPANY + " ( " + TblMyCompany_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblMyCompany_Key_User_ID + " TEXT, "
                    + TblMyCompany_CUSTOMER_ID + " TEXT, "
                    + TblMyCompany_Key_name + " TEXT, "
                    + TblMyCompany_Key_lastModified + " TEXT, "
                    + TblMyCompany_Key_description + " TEXT, "
                    + TblMyCompany_Key_downloadUrl + " TEXT, "
                    + TblMyCompany_Key_fileName + " TEXT, "
                    + TblMyCompany_Key_fileSize + " TEXT, "
                    + TblMyCompany_Key_DocumentID + " TEXT, "
                    + TblMyCompany_Key_Locale + " TEXT )";

            String CREATE_COURSE_DOWNLOAD_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_CourseDownload + " ( " + TblCourseDW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblCourseDW_User_ID + " TEXT, "
                    + TblCourseDW_CourseID + " TEXT, "
                    + TblCourseDW_LicenceID + " TEXT, "
                    + TblCourseDW_Status + " TEXT, "
                    + TblCourseDW_DWTime + " TEXT, "
                    + TblCourseDW_CompletionDate + " TEXT, "
                    + TblCourseDW_CompletionDateStatus + " TEXT )";

            String CREATE_FIREBASE_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Firebase + " ( " + TblFirebase_Key_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblFirebase_Firebase_Token + " TEXT, "
                    + TblFirebase_Token_Inserted + " TEXT ) ";

            String CREATE_OfflineDownloads_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_OfflineDownload + " ( " + TblUserID_Key_ID + " TEXT, "
                    + Diploma_Switch_Status_Key_ID + " TEXT, "
                    + Tools_Switch_Status_Key_ID + " TEXT, "
                    + SafetyCard_Switch_Status_Key_ID + " TEXT, "
                    + Documents_Switch_Status_Key_ID + " TEXT, "
                    + is_downloaded_completely_Key_ID + " TEXT, "
                    + is_downloaded_Is_Logged_In + " TEXT ) ";

            String CREATE_GET_COURSE_DETAIL_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_GetCourseDetails + " ( " + Tbl_CourseID + " TEXT, "
                    + Tbl_UUID + " TEXT, "
                    + Tbl_Language_Type + " TEXT, "
                    + Tbl_Title + " TEXT, "
                    + Tbl_Intro + " TEXT, "
                    + Tbl_Goal + " TEXT, "
                    + Tbl_Target_Group + " TEXT, "
                    + Tbl_Description + " TEXT ) ";

            String CREATE_CUSTOMER_PURCHASE_DETAILS_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_GetCoursePurchaseInfo + " ( " + TblCPI_CourseID + " Text, "
                    + TblCPI_UUID + " TEXT, "
                    + TblCPI_Language + " TEXT, "
                    + TblCPI_Internal_Name + " TEXT, "
                    + TblCPI_Length + " TEXT, "
                    + TblCPI_Price + " TEXT, "
                    + TblCPI_Price_Inc_Vat + " TEXT )";

            String CREATE_DSB_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_DSB + " ( " + TblDSB_Key_ID + " Text, "
                    + TblDSB_last_modified + " TEXT, "
                    + TblDSB_name + " TEXT, "
                    + TblDSB_release_date + " TEXT, "
                    + TblDSB_image + " TEXT, "
                    + TblDSB_file + " TEXT, "
                    + TblDSB_file_size + " TEXT, "
                    + TblDSB_dsb_order + " TEXT ) ";

            String CREATE_NOTIFICATION_UPDATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_UpdateNotification + " ( " + TblUpdNtfction_Key_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblUpdNtfction_userID + " TEXT, "
                    + TblUpdNtfction_notificationType + " TEXT, "
                    + TblUpdNtfction_notificationCount + " INTEGER, "
                    + TblUpdNtfction_deviceType + " TEXT, "
                    + TblUpdNtfction_deviceID + " TEXT, "
                    + TblUpdNtfction_updatedOnServer + " TEXT, "
                    + TblUpdNtfction_Notification_IDs + " TEXT ) ";

            CREATE_COP_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_COMP + " ( " + TblID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TblUserId + " TEXT, "
                    + TblLisenceId + " TEXT, "
                    + TblCardStatus + " TEXT, "
                    + TblCourseStatus + " TEXT, "
                    + TblRegistrationDate + " TEXT, "
                    + TblPlacePlatform + " TEXT, "
                    + TblDepartment + " TEXT, "
                    + TblTopicDiscussed + " TEXT, "
                    + TblRiskIdentified + " TEXT, "
                    + TblPlannedFollowUp + " TEXT, "
                    + TblSwitchHeatCold + " TEXT, "
                    + TblSwitchPressure + " TEXT, "
                    + TblSwitchChemical + " TEXT, "
                    + TblSwitchElectrical + " TEXT, "
                    + TblSwitchGravity + " TEXT, "
                    + TblSwitchRadiation + " TEXT, "
                    + TblSwitchNoise + " TEXT, "
                    + TblSwitchBiological + " TEXT, "
                    + TblSwitchEnergyMovement + " TEXT, "
                    + TblSwitchPresentMoment + " TEXT ) ";

            CREATE_FACILITY_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Facility + " ( " + FACILITY_ID + " Text, "
                    + FACILITY_Name + " TEXT, "
                    + FACILITY_CustomerId + " TEXT, "
                    + FACILITY_CustomerName + " TEXT, "
                    + FACILITY_EmployeeCheckinState + " TEXT, "
                    + FACILITY_ImageUrl + " TEXT, "
                    + FACILITY_DistanceInKm + " TEXT, "
                    + FACILITY_AllowGuests + " TEXT, "
                    + FACILITY_Latitude + " TEXT, "
                    + FACILITY_Longitude + " TEXT ) ";

            CREATE_REPORTENTRY_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_ReportEntry + " ( " + REPORTENTRY_userId + " Text, "
                    + REPORTENTRY_EntryId + " TEXT, "
                    + REPORTENTRY_checkOutMessage + " TEXT, "
                    + REPORTENTRY_timestamp + " TEXT, "
                    + REPORTENTRY_state + " TEXT, "
                    + REPORTENTRY_numberOfGuests + " TEXT, "
                    + REPORTENTRY_employeeId + " TEXT, "
                    + REPORTENTRY_securityServicePhone + " TEXT, "
                    + REPORTENTRY_safetycardId + " TEXT, "
                    + REPORTENTRY_facilityName + " TEXT, "
                    + REPORTENTRY_facilityId + " TEXT, "
                    + REPORTENTRY_estimatedDurationOfVisitInSeconds + " TEXT, "
                    + REPORTENTRY_facilityLatitude + " TEXT, "
                    + REPORTENTRY_facilityLongitude + " TEXT ) ";

            /*CREATE_CHECKEDIN_FACILITY_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + Table_Checked_In_Facility + " ( " + CHECKEDIN_FACILITY_userId + " Text, "
                    + CHECKEDIN_FACILITY_EntryId + " TEXT, "
                    + CHECKEDIN_FACILITY_checkOutMessage + " TEXT, "
                    + CHECKEDIN_FACILITY_timestamp + " TEXT, "
                    + CHECKEDIN_FACILITY_state + " TEXT, "
                    + CHECKEDIN_FACILITY_employeeId + " TEXT, "
                    + CHECKEDIN_FACILITY_facilityName + " TEXT, "
                    + CHECKEDIN_FACILITY_facilityId + " TEXT, "
                    + CHECKEDIN_FACILITY_estimatedDurationOfVisitInSeconds + " TEXT ) ";*/

            db.execSQL(CREATE_PROFILE_TABLE);
            db.execSQL(CREATE_TOOLBOX_TABLE);
            db.execSQL(CREATE_NOTIFICATION_TABLE);
            db.execSQL(CREATE_NOTIFICATION_COUNT_TABLE);
            db.execSQL(CREATE_CUSTOMER_DETAILS_TABLE);
            db.execSQL(CREATE_SAFETY_CARD_TABLE);
            db.execSQL(CREATE_DIPLOMAS_TABLE);
            db.execSQL(CREATE_COURSES_TABLE);
            db.execSQL(CREATE_SCORM_TABLE);
            db.execSQL(CREATE_MY_COMPANY_TABLE);
            db.execSQL(CREATE_COURSE_DOWNLOAD_TABLE);
            db.execSQL(CREATE_FIREBASE_TABLE);
            db.execSQL(CREATE_OfflineDownloads_TABLE);
            db.execSQL(CREATE_GET_COURSE_DETAIL_TABLE);
            db.execSQL(CREATE_CUSTOMER_PURCHASE_DETAILS_TABLE);
            db.execSQL(CREATE_DSB_TABLE);
            db.execSQL(CREATE_NOTIFICATION_UPDATE_TABLE);
            db.execSQL(CREATE_COP_TABLE);
            db.execSQL(CREATE_FACILITY_TABLE);
            db.execSQL(CREATE_REPORTENTRY_TABLE);
            //db.execSQL(CREATE_CHECKEDIN_FACILITY_TABLE);

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            try {
                db.execSQL("Alter Table DiplomasTable add COLUMN userID Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table DiplomasTable add COLUMN newlyCompleted Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table DiplomasTable add COLUMN notToBeDeleted Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table SCORMTable add COLUMN NewlyCompleted Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table MyCompanyTable add COLUMN userID Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table MyCompanyTable add COLUMN documemtId Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table NotificationCountTable add COLUMN NotificationBody Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table NotificationCountTable add COLUMN NotificationCategory Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table NotificationCountTable add COLUMN NotificationID_Server Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table MyCompanyTable add COLUMN locale Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table CustomerDetails add COLUMN emailVerified Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CustomerDetails add COLUMN phoneVerified Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CustomerDetails add COLUMN isPrivate Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }


            //new
            try {
                db.execSQL(CREATE_COP_TABLE);
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table CustomerDetails add COLUMN hasCopAccess Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table CompUser add COLUMN CardID Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN UserID Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN LisenceId Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN CardStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN CourseStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN RegdDate Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN PlacePlatform Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN Department Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table CompUser add COLUMN TopicDiscussed Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN RiskIdentified Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN PlannedFollowUp Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN HeatColdStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN PressureStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN ChemicalStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN ElectricalStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN GravityStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try {
                db.execSQL("Alter Table CompUser add COLUMN RadiationStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN NoiseStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN BiologicalStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN EnergyMovementStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }
            try {
                db.execSQL("Alter Table CompUser add COLUMN PresentMomentStatus Text");
            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }

            try { // added on 29-10-2020
                db.execSQL(CREATE_FACILITY_TABLE);
                db.execSQL("Alter Table FacilityTable add COLUMN id Text");
                db.execSQL("Alter Table FacilityTable add COLUMN name Text");
                db.execSQL("Alter Table FacilityTable add COLUMN customerId Text");
                db.execSQL("Alter Table FacilityTable add COLUMN customerName Text");
                db.execSQL("Alter Table FacilityTable add COLUMN employeeCheckinState Text");
                db.execSQL("Alter Table FacilityTable add COLUMN imageUrl Text");
                db.execSQL("Alter Table FacilityTable add COLUMN distanceInKm Text");
                db.execSQL("Alter Table FacilityTable add COLUMN allowGuests Text");
                db.execSQL("Alter Table FacilityTable add COLUMN latitude Text");
                db.execSQL("Alter Table FacilityTable add COLUMN longitude Text");

                db.execSQL(CREATE_REPORTENTRY_TABLE);
                db.execSQL("Alter Table ReportEntry add COLUMN userId Text");
                db.execSQL("Alter Table ReportEntry add COLUMN id Text");
                db.execSQL("Alter Table ReportEntry add COLUMN checkOutMessage Text");
                db.execSQL("Alter Table ReportEntry add COLUMN timestamp Text");
                db.execSQL("Alter Table ReportEntry add COLUMN state Text");
                db.execSQL("Alter Table ReportEntry add COLUMN numberOfGuests Text");
                db.execSQL("Alter Table ReportEntry add COLUMN employeeId Text");
                db.execSQL("Alter Table ReportEntry add COLUMN securityServicePhone Text");
                db.execSQL("Alter Table ReportEntry add COLUMN safetycardId Text");
                db.execSQL("Alter Table ReportEntry add COLUMN facilityName Text");
                db.execSQL("Alter Table ReportEntry add COLUMN facilityId Text");
                db.execSQL("Alter Table ReportEntry add COLUMN estimatedDurationOfVisitInSeconds Text");
                db.execSQL("Alter Table ReportEntry add COLUMN facilityLatitude Text");
                db.execSQL("Alter Table ReportEntry add COLUMN facilityLongitude Text");

                db.execSQL("Alter Table CustomerDetails add COLUMN emailVerified Text");
                db.execSQL("Alter Table CustomerDetails add COLUMN phoneVerified Text");
                db.execSQL("Alter Table CustomerDetails add COLUMN isPrivate Text");
                db.execSQL("Alter Table SafetyCards add COLUMN customerId Text");
                db.execSQL("Alter Table SafetyCards add COLUMN confirmed Text");

            } catch (Exception ex) {
                Log.d("Error", ex.getMessage());
            }


        }
        onCreate(db);
    }
}
