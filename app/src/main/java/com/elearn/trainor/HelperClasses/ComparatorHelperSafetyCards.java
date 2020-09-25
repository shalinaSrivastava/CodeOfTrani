package com.elearn.trainor.HelperClasses;

import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class ComparatorHelperSafetyCards implements Comparator {

    @Override
    public int compare(Object o, Object t1) {
        SafetyCardProperty property1 = (SafetyCardProperty)o;
        SafetyCardProperty property2 = (SafetyCardProperty)t1;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date safetyCardPropertyValidTo = format.parse(property1.valid_to);
            Date propertyValidTo = format.parse(property2.valid_to);
            return safetyCardPropertyValidTo.compareTo(propertyValidTo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
