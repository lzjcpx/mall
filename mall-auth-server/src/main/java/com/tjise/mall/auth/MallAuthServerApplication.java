package com.tjise.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 核心原理
 * 1）、@EnableRedisHttpSession导入RedisHttpSessionConfiguration
 *      1、给容器中添加了一个组件
 *          RedisIndexedSessionRepository：redis操作session，session的增删改查封装类
 *      2、SessionRepositoryFilter：session存储过滤器：每个请求过来都必须经过filter
 * @auther 刘子敬
 * @create 2023-02-27-15:17
 */
@EnableRedisHttpSession //整合redis作为session存储
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class MallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAuthServerApplication.class, args);
    }

}
