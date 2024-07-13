package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {

    private Integer database;
    //端口号和ip分开
    private String host;
    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient(){//注意：！！@Bean将方法名作为bean加入ioc容器，到时候注入的时候就是注入这个bean（方法名）
        // 1. Create config object
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://"+host+":" + port)
                .setPassword(password);

        return Redisson.create(config);
    }
}
