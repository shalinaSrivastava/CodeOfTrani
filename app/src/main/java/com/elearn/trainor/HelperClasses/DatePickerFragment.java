package com.elearn.trainor.HelperClasses;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Context;
import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {
    public static DatePickerFragment instance;
    DatePickerDialog.OnDateSetListener listener;
    public static DatePickerFragment getInstance() {
        if (instance == null) {
            instance = new DatePickerFragment();
        }
        return instance;
    }

    public DatePickerDialog showDatePicker(Context context, DatePickerDialog.OnDateSetListener listener) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        Date today = new Date();
        c.setTime(today);
        long maxDate = c.getTime().getTime();

        DatePickerDialog dpd4 = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, listener, day, month, year);
        dpd4.getDatePicker().setMaxDate(maxDate);
        return dpd4;

    }

}