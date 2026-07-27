package com.maahish.auth.util;

import com.maahish.common.exception.BadRequestException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "maahish.rate-limit.backend", havingValue = "memory", matchIfMissing = true)
public class InMemoryRateLimitBackend implements RateLimitBackend {

    private final Map<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();

    @Override
    public void assertAllowed(String key, int maxRequests, Duration window) {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);
        Deque<Instant> timestamps = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                throw new BadRequestException("Too many requests. Please try again later.");
            }
            timestamps.addLast(now);
        }
    }
}
