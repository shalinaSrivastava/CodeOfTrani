package com.elearn.trainor.ConcoPhilips.nLevel;

import org.json.JSONObject;

public class SomeObject {
    public String name;
    public String id;
    public boolean closedForRegistration;
    public JSONObject jsonObject;

    public SomeObject(String name, String id, boolean _closedForRegistration, JSONObject _jsonObject) {
        this.name = name;
        this.id = id;
        this.closedForRegistration = _closedForRegistration;
        this.jsonObject = _jsonObject;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean getClosedForRegistration() {
        return closedForRegistration;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
