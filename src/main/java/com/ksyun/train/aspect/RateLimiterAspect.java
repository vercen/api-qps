package com.ksyun.train.aspect;

/**
 * @author vercen
 * @version 1.0
 */

import com.ksyun.train.server.TokenBucketRateLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimiterAspect {

    @Autowired
    private TokenBucketRateLimiter rateLimiter;

    @Before("@annotation(com.ksyun.train.annotation.RateLimiterAnnotation)")
    public void beforeRateLimit(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader("X-KSC-ACCOUNT-ID");
        String apiName = joinPoint.getSignature().getName();
        rateLimiter.checkRequest(userId, apiName);
    }

}
