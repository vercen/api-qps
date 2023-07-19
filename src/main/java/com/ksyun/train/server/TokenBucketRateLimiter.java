package com.ksyun.train.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ksyun.train.exception.RateLimitException;
import com.ksyun.train.util.JsonUtils;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vercen
 * @version 1.0
 */
@Component
@Slf4j
public class TokenBucketRateLimiter {

    private final Map<String, ConcurrentHashMap<String, Integer>> qpsLimits = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter() {
        try {
            String configPath = System.getProperty("config_path", "ratelimiter.properties");
            String configJson = StreamUtils.copyToString(new ClassPathResource(configPath).getInputStream(), StandardCharsets.UTF_8);
            String s = JsonUtils.convertToStandardJson(configJson);
            TypeToken<Map<String, Integer>> token = new TypeToken<Map<String, Integer>>() {
            };
            Map<String, String> configMap = new Gson().fromJson(s, Map.class);
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                String userId = entry.getKey();
                Map<String, Integer> apiLimits = new Gson().fromJson(entry.getValue(), token.getType());
                ConcurrentHashMap<String, Integer> apiBucket = new ConcurrentHashMap<>();
                apiBucket.putAll(apiLimits);
                apiBucket.put("lastRefillTime", (int) (System.currentTimeMillis() / 1000));
                qpsLimits.put(userId, apiBucket);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rate limiter config", e);
        }
    }

    public void checkRequest(String userId, String apiName) throws RateLimitException {
        ConcurrentHashMap<String, Integer> apiBucket = qpsLimits.get(userId);
        if (apiBucket == null || !apiBucket.containsKey(apiName)) {
            return;
        }
        int limit = apiBucket.get(apiName);
        int refillRate = limit / 10; // Assuming the limit is for 10 second period
        long now = System.currentTimeMillis();
        int lastRefillTime = apiBucket.get("lastRefillTime");
        int elapsedTime = (int) ((now / 1000) - lastRefillTime);

        // Refill tokens
        if (elapsedTime > 0) {
            int refillTokens = elapsedTime * refillRate;
            int currentBucketSize = Math.min(limit, apiBucket.get(apiName) + refillTokens);
            apiBucket.put(apiName, currentBucketSize);
            apiBucket.put("lastRefillTime", (int) (now / 1000));
        }

        // Check if any token is left in the bucket
        int currentBucketSize = apiBucket.get(apiName);
        if (currentBucketSize <= 0) {
            throw new RateLimitException();
        }

        // Consume a token
        apiBucket.put(apiName, currentBucketSize - 1);
    }

}