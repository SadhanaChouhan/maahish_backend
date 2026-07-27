package com.maahish.order.service;

import com.maahish.common.constants.AppConstants;
import com.maahish.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final long INITIAL_SEQUENCE = 10001L;

    private final OrderRepository orderRepository;

    /**
     * Generates the next order number for today, e.g. ORD2026071210002.
     * Reads the latest number from the database so restarts do not reuse IDs.
     */
    public synchronized String generate() {
        String datePrefix = AppConstants.ORDER_NUMBER_PREFIX
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        long nextSequence = orderRepository
                .findFirstByOrderNumberStartingWithOrderByOrderNumberDesc(datePrefix)
                .map(order -> parseSequence(order.getOrderNumber(), datePrefix))
                .orElse(INITIAL_SEQUENCE - 1)
                + 1;

        return datePrefix + nextSequence;
    }

    private long parseSequence(String orderNumber, String datePrefix) {
        if (!orderNumber.startsWith(datePrefix) || orderNumber.length() <= datePrefix.length()) {
            return INITIAL_SEQUENCE - 1;
        }
        try {
            return Long.parseLong(orderNumber.substring(datePrefix.length()));
        } catch (NumberFormatException ex) {
            return INITIAL_SEQUENCE - 1;
        }
    }
}
