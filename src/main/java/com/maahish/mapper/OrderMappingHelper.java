package com.maahish.mapper;

import com.maahish.dto.response.OrderItemResponse;
import com.maahish.dto.response.OrderResponse;
import com.maahish.entity.Order;
import com.maahish.entity.OrderItem;
import com.maahish.entity.Product;
import com.maahish.entity.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OrderMappingHelper {

    private final OrderMapper orderMapper;

    public OrderResponse toOrderResponse(Order order) {
        OrderResponse response = orderMapper.toOrderResponse(order);
        if (order.getItems() != null) {
            response.setItems(order.getItems().stream().map(this::toOrderItemResponse).toList());
        }
        return response;
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        OrderItemResponse response = orderMapper.toOrderItemResponse(item);
        if (item.getPrice() != null && item.getQty() != null) {
            response.setLineTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
        }
        Product product = item.getProduct();
        if (product != null && product.getSeller() != null) {
            Seller seller = product.getSeller();
            response.setSellerId(seller.getId());
            response.setSellerOwnerName(seller.getOwnerName());
            response.setSellerBusinessName(seller.getBusinessName());
            response.setSellerName(seller.getBusinessName());
        }
        return response;
    }
}
