package com.elearn.trainor.HelperClasses;

import org.json.JSONObject;

public class CustomTag {
    public JSONObject jsonObject;
    public boolean closedForRegistration;

    public CustomTag(JSONObject _jsonObj, boolean _closedForRegistration) {
        this.jsonObject = _jsonObj;
        this.closedForRegistration = _closedForRegistration;
    }
}
