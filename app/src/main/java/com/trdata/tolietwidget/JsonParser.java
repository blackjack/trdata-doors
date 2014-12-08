package com.trdata.tolietwidget;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by blackjack on 06.12.14.
 */
public class JsonParser {
    static boolean parseDoorJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        return !root.getBoolean("wc_locked");
    }
}
