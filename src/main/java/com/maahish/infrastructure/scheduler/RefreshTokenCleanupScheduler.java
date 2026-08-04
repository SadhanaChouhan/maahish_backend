package com.maahish.infrastructure.scheduler;

import com.maahish.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeStaleRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = refreshTokenRepository.deleteByExpiresAtBeforeOrRevokedTrue(now);
        if (deleted > 0) {
            log.info("event=refresh_token_cleanup deletedCount={}", deleted);
        }
    }
}
