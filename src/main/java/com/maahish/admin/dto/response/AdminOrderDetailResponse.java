package com.maahish.admin.dto.response;

import com.maahish.order.dto.response.OrderResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDetailResponse {

    private OrderResponse order;
    private List<AdminOrderSellerBreakdownResponse> sellerBreakdowns;
}
