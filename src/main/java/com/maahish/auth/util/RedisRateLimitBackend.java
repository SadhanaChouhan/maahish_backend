package com.maahish.auth.util;

import com.maahish.common.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "maahish.rate-limit.backend", havingValue = "redis")
public class RedisRateLimitBackend implements RateLimitBackend {

    private static final String KEY_PREFIX = "maahish:rate:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void assertAllowed(String key, int maxRequests, Duration window) {
        String redisKey = KEY_PREFIX + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                log.warn("Redis rate limit increment returned null for key {}", key);
                return;
            }
            if (count == 1L) {
                redisTemplate.expire(redisKey, window);
            }
            if (count > maxRequests) {
                throw new BadRequestException("Too many requests. Please try again later.");
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Redis rate limit check failed for key {}: {}", key, ex.getMessage());
            throw new BadRequestException("Service temporarily unavailable. Please try again later.");
        }
    }
}
