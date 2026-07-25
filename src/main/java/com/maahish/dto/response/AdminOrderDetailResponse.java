package com.maahish.dto.response;

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
