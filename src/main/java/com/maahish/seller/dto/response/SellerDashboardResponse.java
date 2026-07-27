package com.maahish.seller.dto.response;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



import java.math.BigDecimal;

import java.util.List;



@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class SellerDashboardResponse {



    private long totalProducts;

    private long activeProducts;

    private long totalOrders;

    private long pendingOrders;

    private BigDecimal totalRevenue;

    private BigDecimal totalSales;

    private BigDecimal platformCommission;

    private BigDecimal netEarnings;

    private BigDecimal pendingSettlement;

    private BigDecimal paidSettlement;

    private long lowStockProducts;

    private SellerSummaryResponse seller;

    private List<SellerOrderSummaryResponse> recentOrders;

}

