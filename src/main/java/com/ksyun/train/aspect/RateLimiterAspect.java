package com.ksyun.train.aspect;

import com.google.gson.Gson;
import com.ksyun.train.config.RateLimiterAnnotation;
import com.ksyun.train.pojo.UserApiCountMap;
import com.ksyun.train.util.ApiCounter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.aspectj.lang.annotation.Aspect;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);

    /**
     * 存储每个用户在一定时间内访问 API 的次数
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> userApiCountMap = new ConcurrentHashMap<>();

    /**
     * 切入点：所有使用 @RateLimiterAnnotation 注解的方法
     */
    @Pointcut("@annotation(com.ksyun.train.config.RateLimiterAnnotation)")
    public void rateLimiterPointcut() {
    }

    /**
     * 环绕通知：对使用 @RateLimiterAnnotation 注解的方法进行 QPS 控制
     *
     * @param joinPoint 切入点
     * @return 切入点方法的返回结果
     * @throws Throwable 异常信息
     */
    @Around("rateLimiterPointcut()")
    public Object rateLimiterAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法名称
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getName();
        // 获取请求头中的用户 ID
        System.out.println(methodName);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("X-KSC-ACCOUNT-ID");
        System.out.println(userId);
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("User ID is missing in request header.");
        }
        // 获取 @RateLimiterAnnotation 注解的 ttl 属性值
        Method method = methodSignature.getMethod();
        RateLimiterAnnotation rateLimiterAnnotation = method.getAnnotation(RateLimiterAnnotation.class);
        long ttl = rateLimiterAnnotation.ttl();
        // 获取用户在 ttl 时间内可访问的 API 次数
        ConcurrentHashMap<String, Long> apiCountMap = userApiCountMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        Long apiQps = apiCountMap.get(methodName);
        if (apiQps == null) {
            apiQps = Long.valueOf(getApiQps(userId, methodName));
            if (apiQps == null) {
                logger.warn("API QPS configuration not found for user {} and API {}", userId, methodName);
                return joinPoint.proceed();
            }
            apiCountMap.put(methodName, apiQps);
        }
        // 判断是否超过了 QPS 限制
        String key = userId + ":" + methodName;
        Long count = apiCountMap.compute(key, (k, v) -> {
            if (v == null) {
                return 1L;
            } else {
                return v + 1L;
            }
        });
        if (count > apiQps) {
            logger.warn("API QPS limit exceeded for user {} and API {}", userId, methodName);
            throw new RuntimeException("request too much");
        }
        // 设置过期时间
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> {
            apiCountMap.remove(methodName);
            logger.info("API count for user {} and API {} has been reset.", userId, methodName);
        }, ttl, TimeUnit.SECONDS);
        // 调用切入点方法并返回结果
        return joinPoint.proceed();
    }

    /**
     * 获取用户在 ttl 时间内可访问的 API 次数
     *
     * @param userId     用户 ID
     * @param methodName API 方法名称
     * @return 可访问的 API 次数
     */

    private String getApiQps(String userId, String methodName) {
        Properties props = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ratelimiter.properties")) {
            props.load(inputStream);
        } catch (IOException e) {
            // 处理异常
        }
        String jsonConfig = props.getProperty(userId);
        if (jsonConfig != null) {
            Gson gson = new Gson();
            System.out.println(jsonConfig);
            String str = ApiCounter.convertString(jsonConfig);
            System.out.println(str);
            UserApiCountMap jsonObject = gson.fromJson(str, UserApiCountMap.class);

            System.out.println(jsonObject);
            System.out.println(jsonObject.getUserApiCountMap());
            if (jsonObject != null && jsonObject.getUserApiCountMap().containsKey(methodName)) {
                Integer qps = jsonObject.getUserApiCountMap().get(methodName);
                String qpsString = String.valueOf(qps);
                logger.info("QPS value for user {} and method {}: {}", userId, methodName, qpsString);
                return qpsString;
            }
        }

        return null;
    }

}
