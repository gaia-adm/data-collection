package com.hp.gaia.provider.alm.util;

import org.json.JSONObject;

/**
 * Created by kornfeld on 25/01/2016.
 */
public class JSONUtils {

    public static int getIntValue(String json, String field) {

        JSONObject jsonObj = new JSONObject(json);

        return jsonObj.getInt(field);
    }

    public static int getArraySize(String json, String entities) {

        JSONObject jsonObj = new JSONObject(json);

        return jsonObj.getJSONArray(entities).length();
    }
}
