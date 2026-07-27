package com.maahish.settlement.service;

import com.maahish.admin.dto.response.AdminOrderSellerBreakdownResponse;
import com.maahish.admin.dto.response.AdminSellerOrderSummaryResponse;
import com.maahish.order.entity.Order;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.seller.dto.response.SellerSettlementResponse;
import com.maahish.settlement.enums.SettlementStatus;
import com.maahish.settlement.dto.request.SettlementStatusUpdateRequest;


import java.util.List;

public interface SettlementService {

    void createSettlementsForOrder(Order order);

    PageResponse<SellerSettlementResponse> adminListSettlements(SettlementStatus status, Long sellerId, int page, int size);

    SellerSettlementResponse adminUpdateSettlementStatus(Long settlementId, SettlementStatusUpdateRequest request);

    PageResponse<SellerSettlementResponse> sellerListSettlements(Long userId, SettlementStatus status, int page, int size);

    List<AdminOrderSellerBreakdownResponse> buildSellerBreakdownsForOrder(Order order);

    AdminSellerOrderSummaryResponse buildAdminSellerOrderSummary(Order order, Long sellerId);
}
