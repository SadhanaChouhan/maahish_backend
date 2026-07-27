package com.maahish.auth.util;

import com.maahish.common.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRateLimitBackendTest {

    private InMemoryRateLimitBackend backend;

    @BeforeEach
    void setUp() {
        backend = new InMemoryRateLimitBackend();
    }

    @Test
    void assertAllowed_allowsUpToMaxRequests() {
        String key = "test-key";
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> backend.assertAllowed(key, 3, Duration.ofHours(1)));
        }
    }

    @Test
    void assertAllowed_blocksWhenMaxExceeded() {
        String key = "test-key-block";
        for (int i = 0; i < 2; i++) {
            backend.assertAllowed(key, 2, Duration.ofHours(1));
        }
        assertThrows(BadRequestException.class,
                () -> backend.assertAllowed(key, 2, Duration.ofHours(1)));
    }
}
