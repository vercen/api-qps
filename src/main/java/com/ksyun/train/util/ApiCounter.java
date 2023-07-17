package com.ksyun.train.util;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ApiCounter {
    public static String convertString(String apiString) {
        Gson gson = new Gson();
        Map<String, Integer> apiMap = gson.fromJson(apiString, HashMap.class);

        Map<String, Object> userApiCountMap = new HashMap<>();
        userApiCountMap.put("userApiCountMap", apiMap);

        return gson.toJson(userApiCountMap);
    }
}
