package com.ksyun.train.pojo;

import java.util.HashMap;

public class UserApiCountMap {
//    private Integer id;
    private HashMap<String, Integer> userApiCountMap;



    public HashMap<String, Integer> getUserApiCountMap() {
        return userApiCountMap;
    }

    public void setUserApiCountMap(HashMap<String, Integer> userApiCountMap) {
        this.userApiCountMap = userApiCountMap;
    }

    @Override
    public String toString() {
        return "user{" +
                "userApiCountMap=" + userApiCountMap +
                '}';
    }
}
