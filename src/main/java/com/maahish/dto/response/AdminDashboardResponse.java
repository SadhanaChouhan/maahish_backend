package com.maahish.dto.response;



import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



import java.math.BigDecimal;



@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class AdminDashboardResponse {



    private long totalUsers;

    private long activeUsers;

    private long totalProducts;

    private long activeProducts;

    private long totalOrders;

    private long pendingOrders;

    private long deliveredOrders;

    private BigDecimal totalRevenue;

    private long totalSellers;

    private long pendingSellers;

    private BigDecimal totalCommissionEarned;

    private long pendingSettlements;

    private long paidSettlements;

    private BigDecimal pendingSettlementAmount;

    private BigDecimal paidSettlementAmount;

}

