package com.maahish.infrastructure.scheduler;

import com.maahish.returns.service.ReturnRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnShipReminderScheduler {

    private final ReturnRequestService returnRequestService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendShipReminders() {
        try {
            returnRequestService.sendShipReminders();
        } catch (Exception ex) {
            log.warn("Failed to send return ship reminders: {}", ex.getMessage());
        }
    }
}
