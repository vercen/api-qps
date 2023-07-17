package com.ksyun.train.controller;

import com.ksyun.train.config.RateLimiterAnnotation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RestController
public class RateLimiterTestController {

    @RateLimiterAnnotation
    @RequestMapping("/test/instance/query")
    public String describeInstance() {
        return "describe instance success";
    }

    @RateLimiterAnnotation
    @RequestMapping("/test/instance/create")
    public String createInstance() {
        return "create instance success";
    }
}