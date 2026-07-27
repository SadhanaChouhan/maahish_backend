package com.maahish.auth.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitBackend backend;

    public void assertAllowed(String key, int maxRequests, Duration window) {
        backend.assertAllowed(key, maxRequests, window);
    }
}
