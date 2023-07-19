package com.ksyun.train.controller;

import com.ksyun.train.annotation.RateLimiterAnnotation;
import com.ksyun.train.vo.RespBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vercen
 * @version 1.0
 */
@RestController
public class RateLimiterTestController {

    @RateLimiterAnnotation
    @RequestMapping("/test/instance/query")
    public RespBean describeInstance() {
        return RespBean.success("describe instance success");
    }

    @RateLimiterAnnotation
    @RequestMapping("/test/instance/create")
    public RespBean createInstance() {
        return RespBean.success("create instance success");
    }


}
