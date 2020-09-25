package com.elearn.trainor.HelperClasses;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.view.WindowManager;

public class ExceptionHandler {

    public static String getErrorMessage(Context context, Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return "Bad Format Type ";
        } else if (ex instanceof NumberFormatException) {
            return "Navigate home";
        } else if (ex instanceof NullPointerException) {
            return "Navigate home";
        } else if (ex instanceof WindowManager.BadTokenException) {
            return "Navigate home";
        } else if (ex instanceof ArrayIndexOutOfBoundsException) {
            return "Navigate home";
        } else if (ex instanceof IndexOutOfBoundsException) {
            return "Navigate home";
        } else if (ex instanceof ActivityNotFoundException) {
            return "Navigate home";
        }
        return "";
    }
}
