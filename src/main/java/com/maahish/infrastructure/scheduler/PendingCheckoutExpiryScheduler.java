package com.maahish.infrastructure.scheduler;

import com.maahish.catalog.service.ProductStockService;
import com.maahish.order.entity.PendingCheckout;
import com.maahish.order.enums.PendingCheckoutStatus;
import com.maahish.order.repository.PendingCheckoutRepository;
import com.maahish.order.util.CheckoutFlowLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingCheckoutExpiryScheduler {

    private final PendingCheckoutRepository pendingCheckoutRepository;
    private final ProductStockService productStockService;
    private final CheckoutFlowLogger checkoutFlowLogger;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void expireStaleCheckouts() {
        List<PendingCheckout> expired = pendingCheckoutRepository.findByStatusAndExpiresAtBefore(
                PendingCheckoutStatus.PENDING, LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        for (PendingCheckout checkout : expired) {
            releaseReservations(checkout);
            checkout.setStatus(PendingCheckoutStatus.EXPIRED);
        }
        pendingCheckoutRepository.saveAll(expired);
        log.info("event=checkout_expiry_scheduler expiredCount={}", expired.size());
    }

    private void releaseReservations(PendingCheckout checkout) {
        checkout.getItems().forEach(item ->
                productStockService.releaseReservation(item.getProduct().getId(), item.getQty()));
        checkoutFlowLogger.checkoutReservationReleased(
                checkout.getCheckoutReference(), checkout.getItems().size());
    }
}
