package com.ksyun.train.annotation;

import java.lang.annotation.*;

/**
 * @author vercen
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiterAnnotation {

    // unit is second
    long ttl() default 10L;
}
