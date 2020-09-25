package com.elearn.trainor.HelperClasses;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.elearn.trainor.R;

public class VolleyErrorHandler {

    public static String getErrorMessage(Context context, VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            return context.getResources().getString(R.string.try_after_some_time);
        } else if (error instanceof ServerError) {
            return context.getResources().getString(R.string.internalServerError);
        } else if (error instanceof AuthFailureError) {
            return "401";
        } else if (error instanceof NetworkError) {
            return context.getResources().getString(R.string.internalServerError);
        } else if (error instanceof ParseError) {
            return context.getResources().getString(R.string.internalServerError);
        } else {
            return context.getResources().getString(R.string.internalServerError);
        }
    }

    public static String getErrorCode(VolleyError error) {
        if(error.networkResponse != null ){
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                return error.networkResponse.statusCode+"";
            } else if (error instanceof ServerError) {
                return error.networkResponse.statusCode+"";
            } else if (error instanceof AuthFailureError) {
                return error.networkResponse.statusCode+"";
            } else if (error instanceof NetworkError) {
                return error.networkResponse.statusCode+"";
            } else if (error instanceof ParseError) {
                return error.networkResponse.statusCode+"";
            } else {
                return error.networkResponse.statusCode+"";
            }
        }else{
            return "Timeout Error";
        }
    }

}
