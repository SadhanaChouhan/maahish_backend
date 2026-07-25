package com.maahish.mapper;

import com.maahish.dto.response.AddressResponse;
import com.maahish.dto.response.CustomerSummaryResponse;
import com.maahish.dto.response.OrderItemResponse;
import com.maahish.dto.response.OrderResponse;
import com.maahish.dto.response.PaymentResponse;
import com.maahish.entity.Address;
import com.maahish.entity.Order;
import com.maahish.entity.OrderItem;
import com.maahish.entity.Payment;
import com.maahish.entity.User;
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
