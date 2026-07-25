package com.maahish.util;

import com.maahish.entity.SellerSettlement;
import com.maahish.enums.SettlementStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public final class SettlementAggregateUtil {

    private SettlementAggregateUtil() {
    }

    public static SettlementStatus aggregateStatus(Collection<SellerSettlement> settlements) {
        if (settlements == null || settlements.isEmpty()) {
            return null;
        }
        List<SettlementStatus> statuses = settlements.stream()
                .map(SellerSettlement::getSettlementStatus)
                .toList();
        if (statuses.stream().allMatch(SettlementStatus.PAID::equals)) {
            return SettlementStatus.PAID;
        }
        if (statuses.stream().anyMatch(SettlementStatus.FAILED::equals)) {
            return SettlementStatus.FAILED;
        }
        if (statuses.stream().anyMatch(SettlementStatus.PROCESSING::equals)) {
            return SettlementStatus.PROCESSING;
        }
        if (statuses.stream().anyMatch(SettlementStatus.PENDING::equals)) {
            return SettlementStatus.PENDING;
        }
        return statuses.getFirst();
    }

    public static BigDecimal sumGross(List<SellerSettlement> settlements) {
        return settlements.stream()
                .map(SellerSettlement::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal sumCommission(List<SellerSettlement> settlements) {
        return settlements.stream()
                .map(SellerSettlement::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal sumNet(List<SellerSettlement> settlements) {
        return settlements.stream()
                .map(SellerSettlement::getNetSellerAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal effectiveCommissionPercentage(List<SellerSettlement> settlements) {
        BigDecimal gross = sumGross(settlements);
        if (gross.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return sumCommission(settlements)
                .multiply(BigDecimal.valueOf(100))
                .divide(gross, 2, RoundingMode.HALF_UP);
    }

    public static boolean allPaid(Collection<SellerSettlement> settlements) {
        return settlements != null
                && !settlements.isEmpty()
                && settlements.stream().allMatch(s -> s.getSettlementStatus() == SettlementStatus.PAID);
    }

    public static boolean hasPendingOrProcessing(Collection<SellerSettlement> settlements) {
        if (settlements == null || settlements.isEmpty()) {
            return false;
        }
        EnumSet<SettlementStatus> open = EnumSet.of(SettlementStatus.PENDING, SettlementStatus.PROCESSING);
        return settlements.stream().anyMatch(s -> open.contains(s.getSettlementStatus()));
    }
}
