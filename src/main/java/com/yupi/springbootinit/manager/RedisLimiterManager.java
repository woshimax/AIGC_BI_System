package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.config.RedissonConfig;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专门提供RedisLimiter限流基础服务（提供通用的能力——相当一种可以移植的中间件）
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作，key区分不同限流器，比如不同用户id分开统计
     * @param key
     */
    public void doRateLimit(String key){
        // Creating a RateLimiter
        RRateLimiter limiter = redissonClient.getRateLimiter(key);

        // Setting the rate: 2 permits per second
        limiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        //每个操作占用一个令牌——不同种类用户可以用不同数量令牌
        boolean canOp = limiter.tryAcquire(1);
        if(!canOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}

