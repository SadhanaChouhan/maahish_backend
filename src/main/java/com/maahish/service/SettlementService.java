package com.maahish.service;

import com.maahish.dto.request.SettlementStatusUpdateRequest;
import com.maahish.dto.response.AdminOrderSellerBreakdownResponse;
import com.maahish.dto.response.AdminSellerOrderSummaryResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.dto.response.SellerSettlementResponse;
import com.maahish.entity.Order;
import com.maahish.enums.SettlementStatus;

import java.util.List;

public interface SettlementService {

    void createSettlementsForOrder(Order order);

    PageResponse<SellerSettlementResponse> adminListSettlements(SettlementStatus status, Long sellerId, int page, int size);

    SellerSettlementResponse adminUpdateSettlementStatus(Long settlementId, SettlementStatusUpdateRequest request);

    PageResponse<SellerSettlementResponse> sellerListSettlements(Long userId, SettlementStatus status, int page, int size);

    List<AdminOrderSellerBreakdownResponse> buildSellerBreakdownsForOrder(Order order);

    AdminSellerOrderSummaryResponse buildAdminSellerOrderSummary(Order order, Long sellerId);
}
