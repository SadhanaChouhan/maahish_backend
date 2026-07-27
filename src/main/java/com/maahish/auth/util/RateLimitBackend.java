package com.maahish.auth.util;

import java.time.Duration;

public interface RateLimitBackend {

    void assertAllowed(String key, int maxRequests, Duration window);
}
