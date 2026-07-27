package com.maahish.order.mapper;

import com.maahish.user.entity.Address;
import com.maahish.user.dto.response.AddressResponse;
import com.maahish.user.dto.response.CustomerSummaryResponse;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.order.dto.response.OrderItemResponse;
import com.maahish.order.dto.response.OrderResponse;
import com.maahish.payment.entity.Payment;
import com.maahish.payment.dto.response.PaymentResponse;
import com.maahish.user.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    AddressResponse toAddressResponse(Address address);

    CustomerSummaryResponse toCustomerSummary(User user);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSlug", source = "product.slug")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    @Mapping(target = "customer", source = "user")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "payment", source = "payment")
    OrderResponse toOrderResponse(Order order);

    PaymentResponse toPaymentResponse(Payment payment);
}
