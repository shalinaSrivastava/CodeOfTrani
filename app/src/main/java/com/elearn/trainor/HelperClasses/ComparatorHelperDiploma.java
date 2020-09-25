package com.elearn.trainor.HelperClasses;

import com.elearn.trainor.PropertyClasses.DiplomaProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class ComparatorHelperDiploma implements Comparator {

    @Override
    public int compare(Object o, Object t1) {
        DiplomaProperty property1 = (DiplomaProperty)o;
        DiplomaProperty property2 = (DiplomaProperty)t1;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date DiplomaPropertyValidUntil = format.parse(property1.completionDate);
            Date propertyValidUntil = format.parse(property2.completionDate);
            int res = DiplomaPropertyValidUntil.compareTo(propertyValidUntil);
            //int res = propertyValidUntil.compareTo(DiplomaPropertyValidUntil);
            return res;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
