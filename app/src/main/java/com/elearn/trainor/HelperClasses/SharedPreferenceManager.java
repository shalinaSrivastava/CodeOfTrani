package com.elearn.trainor.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;

import com.elearn.trainor.PropertyClasses.SharedPreferenceInfo;

public class SharedPreferenceManager {

    private Context context;

    public SharedPreferenceManager(Context context) {
        this.context = context;
    }

    public SharedPreferences.Editor getProfileInfoSharedPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("ProfileInfo", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public boolean getSharedPreferenceExistence() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.contains("Initialized");
    }

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.contains("isLoggedIn");
    }

    public String getUserID() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("UserID", "");
    }

    public String getProfileURL() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("ProfilePicURL", "");
    }

    public String getUsername() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("UserName", "");
    }

    public String getFirstname() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("FirstName", "");
    }

    public String getLastname() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("LastName", "");
    }

    public String getPhone() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("Phone_no", "");
    }

    public String getEmail() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("Email", "");
    }

    public String getDOB() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("dob", "");
    }

    public String getLanguage() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("language", "");
    }

    public String getToken() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("Token", "");
    }
    // new added 27-10-2020
    public String getProfileEmailVerified() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("EmailVerified", "");
    }
    public String getProfilePhoneVerified() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        return sharedPreferences.getString("PhoneVerified", "");
    }

    public void removeSharedPreference() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("ProfileInfo", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
    }

    public SharedPreferences.Editor getTotalNotificationSharedPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("TotalNotification", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public String getTotalNotificationCount() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("TotalNotification", 0);
        return sharedPreferences.getString("Total", "0");
    }

 /* public String getNotificationUserID() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("TotalNotification", 0);
        return sharedPreferences.getString("userIDNotification", "");
    }*/

    public SharedPreferences.Editor getDownloadToolsSharedPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("Download", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public String getValueByKeyNameFromDownload(String key) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("Download", 0);
        return sharedPreferences.getString(key, "");
    }

    public SharedPreferences.Editor getDownloadCourseSharedPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("DownloadCourse", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public String getValueByKeyNameFromDownloadCoursePage(String key) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("DownloadCourse", 0);
        return sharedPreferences.getString(key, "");
    }

    public SharedPreferences.Editor getNotificationHandlerSharedPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("NotificationHandler", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public String getFirebaseToken() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("NotificationHandler", 0);
        return sharedPreferences.getString("Token", "");
    }

    //COP
    public SharedPreferences.Editor COPSharedPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("COPCardDetails", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public String getCOPlisenceId() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("COPLisenceID", "");
    }

    public String getCOPcardStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("COPCardStatus", "");
    }
    public String getCOPcourseStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("COPCourseStatus", "");
    }
    public String getCOPregdDate() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("RegdDate", "");
    }
    public String getCOPplacePlatformName() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("PlacePlatformName", "");
    }
    public String getCOPplacePlatformId() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("PlacePlatformID", "");
    }
    public String getCOPdepartmentName() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("DepartmentName", "");
    }
    public String getCOPGroupObject() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("GroupObject", "");
    }
    public String getCOPdepartmentId() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("DepartmentID", "");
    }
    public String getCOPtopicDiscussed() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("TopicDiscussed", "");
    }
    public String getCOPriskIdentified() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("RiskIdentified", "false");
    }
    public String getCOPplannedFollowUp() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("PlannedFollowUp", "");
    }
    public String getCOPheatColdStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("HeatColdStatus", "");
    }
    public String getCOPpressureStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("PressureStatus", "");
    }
    public String getCOPchemicalStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("ChemicalStatus", "");
    }
    public String getCOPelectricalStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("ElectricalStatus", "");
    }
    public String getCOPgravityStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("GravityStatus", "");
    }
    public String getCOPradiationStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("RadiationStatus", "");
    }
    public String getCOPnoiseStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("NoiseStatus", "");
    }
    public String getCOPbiologicalStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("BiologicalStatus", "");
    }
    public String getCOPenergyMovementStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("EnergyMovementStatus", "");
    }
    public String getCOPpresentMomentStatus() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        return sharedPreferences.getString("PresentMomemtStatus", "false");
    }

    public SharedPreferences.Editor COPFacilityPreference() {
        SharedPreferences.Editor editor = context.getSharedPreferences("COPFacilityResponse", Context.MODE_PRIVATE).edit();
        return editor;
    }

    public String getCOPFacilityResponse() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPFacilityResponse", 0);
        return sharedPreferences.getString("FacilityResponse", "[]");
    }


    public void removeSharedPreferenceByName(String Type) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(Type, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
    }

    public static void insertValuesIntoSharedPreference(SharedPreferences.Editor editor, SharedPreferenceInfo info) {
        editor.putString("Initialized", "1");
        editor.putString("isLoggedIn", "1");
        editor.putString("Token", info.Token);
        if (info.ProfilePicURL.equals("")) {
            info.ProfilePicURL = "invalid URL";
        }
        editor.putString("ProfilePicURL", info.ProfilePicURL);
        editor.putString("FirstName", info.FirstName);
        editor.putString("LastName", info.LastName);
        editor.putString("Email", info.Email);
        editor.putString("dob", info.dob);
        editor.putString("UserID", info.UserID);
        if (info.language.startsWith("nb")) {
            editor.putString("language", "nb");
        } else if (info.language.startsWith("en")) {
            editor.putString("language", "en");
        } else if (info.language.startsWith("ko")) {
            editor.putString("language", "ko");
        } else if (info.language.startsWith("pl")) {
            editor.putString("language", "pl");
        } else if (info.language.startsWith("sv")) {
            editor.putString("language", "sv");
        } else if (info.language.startsWith("pt")) {
            editor.putString("language", "pt");
        }
        editor.putString("Phone_no", info.Phone_no);
        editor.putString("UserName", info.UserName);
        editor.putString("EmailVerified", info.emailVerified);
        editor.putString("PhoneVerified", info.phoneVerified);
        editor.commit();
    }

    // Remove Course URL from SharedPreference for Same URL Problem
    public void removeValueByKeyNameFromDownloadCoursePage(String key) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("DownloadCourse", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void removeValueByKeyNameFromCOPCardDetailsPref(String key) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPCardDetails", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void removeValuefromCOPFacilityPref(String key) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("COPFacilityResponse", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    //verify info back safetycard
    public SharedPreferences.Editor backToSafetCardPref() {
        SharedPreferences.Editor editor = context.getSharedPreferences("FacilitySafetyCardBack", Context.MODE_PRIVATE).edit();
        return editor;
    }
    public String goBacktoSafetyCard() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("FacilitySafetyCardBack", 0);
        return sharedPreferences.getString("GoToSafetyCard", "");
    }
}