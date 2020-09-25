package com.elearn.trainor.HelperClasses;

public class WebServicesURL {
    public static String Base_URL = "https://dev-mobileapi.trainor.no/v1/";
    // public static String Base_URL = "https://mobileapi.trainor.no/v1/";
    public static String Login_URL_WITH_EMAIL = Base_URL + "loginWithEmail";
    public static String Login_URL = Base_URL + "login";
    public static String FetchDetail_URL = Base_URL + "profile";
    public static String Forget_Password_URL = Base_URL + "login/forgotpassword";
    public static String Upcoming_Course_URL = Base_URL + "coursecompletion/upcoming";
    public static String Approve_Course_URL = Base_URL + "coursecompletion/approve/";
    public static String Diploma_URL = Base_URL + "coursecompletion/completed";
    public static String Upcoming_SafetyCards_URL = Base_URL + "safetycards";
    public static String Update_Profile_Pic_URL = Base_URL + "profile/picture";
    public static String Change_Password_URL = Base_URL + "profile/password";
    public static String OrderCourse_PostApi = Base_URL + "license/issueelearning";
    //public static String Tools_URL = "https://www.trainor.no/cms/app/tools.php?hash=b118b6b902558e486e465260c34998d4";
    public static String Tools_URL = "https://dev.trainor.no/cms/app/tools.php?hash=b118b6b902558e486e465260c34998d4";
    public static String CUSTOMERS_DETAIL_URL = Base_URL + "customers";
    public static String NEW_SCORM_GET_URL = Base_URL + "scorm/get";
    public static String NEW_SCORM_Update_URL = Base_URL + "scorm/update";
    public static String My_Company_Details = Base_URL + "customers/documents/";
    // public static String Course_image_URL = "https://www.trainor.no/cms/app/courseimage.php?uuid=";
    public static String Course_image_URL = "https://dev.trainor.no/cms/app/courseimage.php?uuid=";


    //public static String DevBtn_Hide_Customer_ID = "5fbd5d70-a673-11e6-a423-005056010872";  // dev
    public static String DevBtn_Hide_Customer_ID = "844e0564-b6bd-11e6-bb41-00505601092f";  //production

    // New Api updation
    public static String Video_File_Size_URL = Base_URL + "coursedownload/";
    public static String Course_Completion_URL = Base_URL + "coursecompletion/certificate/";

    // Push Notification Module
    public static String Notification_Base_URL = "http://pushapi.trainor.no/api/NotificationAPI/";
    public static String Update_FirebaseToken_URL = Notification_Base_URL + "insertIntoFirebaseToken";
    public static String Update_NotificationMode_URL = Notification_Base_URL + "updateNotificationMode";
    public static String Update_NotificationCount_URL = Notification_Base_URL + "newUpdateNotificationCountTable";
    public static String GetAllPendingNotification_URL = Notification_Base_URL + "getAllPendingNotification";

    public static String GetMoreCoursesURL = "https://dev.trainor.no/cms/app/course_catalogue.php?hash=b118b6b902558e486e465260c34998d4"; //for dev
    //public static String GetMoreCoursesURL = "https://www.trainor.no/cms/app/course_catalogue.php?hash=b118b6b902558e486e465260c34998d4";  //for prodction
    public static String DSBMagzineURL = "https://www.trainor.no/cms/app/magazine.php?hash=b118b6b902558e486e465260c34998d4";


    //public static String NewCoursePurchasedAPI = "https://www.trainor.no/cms/app/app_purchases.php";
    public static String NewCoursePurchasedAPI = "https://dev.trainor.no/cms/app/app_purchases.php";// for dev (purchase details saved to trainor)
    public static String RegisterCOPCard = Base_URL + "cop/psi/registerNewPsiConversation";
    public static String PsiCourseCompletion = Base_URL + "cop/coursecompletion/getPsiCourseCompletion";
    public static String EnrollPSICourse = Base_URL + "cop/coursecompletion/enrollInPsiCourse";
    public static String GetFacility = Base_URL + "cop/facility/getFacilityHierarchy";

    // added on 28-08-2020
    public static String GetReportEntery = Base_URL + "facilityEntry/hasAccessToFacilityEntry";
    public static String NearByFacility = Base_URL + "facilityEntry/searchByLatLong";
    public static String SearchFacility = Base_URL + "facilityEntry/searchByIdentifier";
    public static String VerificationOTPPhone = Base_URL + "verification/request/phone";
    public static String VerificationOTPEmail = Base_URL + "verification/request/email";
    public static String VerifiyCode = Base_URL + "verification/process";
    public static String EntryFacility = Base_URL + "facilityEntry/enterFacility";
}