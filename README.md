# 令牌桶算法实现用户API访问QPS控制器

本项目是一个基于Spring Boot框架的Web应用程序，用于演示如何使用令牌桶算法实现用户API访问QPS控制。

## 令牌桶算法

令牌桶算法是一种流量控制算法，它可以限制单位时间内的请求数量。该算法将请求视为令牌，将令牌放入一个桶中，并限制桶的容量，当桶满时，不再接受新的令牌。每个请求在到达时都需要从桶中获取一个令牌，如果桶中没有令牌，则请求被拒绝。

## 实现

本项目使用Spring AOP实现令牌桶算法。在controller层的方法上添加`@RateLimiterAnnotation`注解，该注解包含一个`ttl`属性，用于指定令牌桶的时间窗口大小。本项目中时间窗口固定为10秒。

用户的QPS配置信息存储在`ratelimiter.properties`配置文件中，其中每个用户对应一个JSON字符串，该JSON字符串是一个Map，其中每个键表示一个API，值表示该API在时间窗口内的请求次数。本项目使用Gson反序列化`ratelimiter.properties`配置文件。

用户的身份信息存储在请求头中的`X-KSC-ACCOUNT-ID`字段中。

每次请求到达时，判断该请求是否超出限流阈值，如果超出则返回HTTP状态码409和响应体"request too much"，否则放行该请求。

本项目中使用本地缓存实现令牌桶算法，缓存使用ConcurrentHashMap实现。

## 配置

在启动应用程序时，可以通过`config_path`参数指定`ratelimiter.properties`配置文件的路径。例如：

```shell
java -Dconfig_path=/path/to/ratelimiter.properties -jar api-qps.jar
```

## 使用

在`controller`层的方法上添加`@RateLimiterAnnotation`注解，例如：

```java
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
```

## 参考资料

- [令牌桶算法 - 维基百科，自由的百科全书 ↗](https://zh.wikipedia.org/wiki/%E4%BB%A4%E7%89%8C%E6%A1%B6%E7%AE%97%E6%B3%95)