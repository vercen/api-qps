package com.ksyun.train.util;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)=\\{.*?\\}");
    public static String convertToStandardJson(String nonStandardJson) {
        // 找到所有的非标准 JSON 对象
        Matcher matcher = PATTERN.matcher(nonStandardJson);
        List<String> nonStandardJsonList = new ArrayList<>();
        while (matcher.find()) {
            nonStandardJsonList.add(matcher.group());
        }
        // 构造标准的 JSON 字符串
        Map<String, Object> map = new HashMap<>();
        for (String nonStandardJsonItem : nonStandardJsonList) {
            // 先去掉字符串中的空格和换行符
            String json = nonStandardJsonItem.replaceAll("\\s+", "");
            // 找到键名和值之间的等号
            int equalIndex = json.indexOf("=");
            if (equalIndex == -1) {
                throw new IllegalArgumentException("Invalid input: " + nonStandardJson);
            }
            // 提取键名和值
            String key = json.substring(0, equalIndex);
            String value = json.substring(equalIndex + 1);
            map.put(key, value);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }
}