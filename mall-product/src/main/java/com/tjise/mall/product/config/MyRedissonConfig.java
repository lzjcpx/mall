package com.tjise.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @auther 刘子敬
 * @create 2022-12-27-14:50
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对redisson的使用都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException{
        //1.创建配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.10.200:6379");

        //2.根据Config创建楚Redisson实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
