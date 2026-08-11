package com.maahish.order.mapper;

import com.maahish.catalog.entity.Product;
import com.maahish.order.dto.response.OrderItemResponse;
import com.maahish.order.dto.response.OrderResponse;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.payment.dto.response.PaymentResponse;
import com.maahish.payment.entity.Payment;
import com.maahish.user.dto.response.AddressResponse;
import com.maahish.user.dto.response.CustomerSummaryResponse;
import com.maahish.user.entity.Address;
import com.maahish.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-11T23:33:00+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public AddressResponse toAddressResponse(Address address) {
        if ( address == null ) {
            return null;
        }

        AddressResponse.AddressResponseBuilder addressResponse = AddressResponse.builder();

        addressResponse.id( address.getId() );
        addressResponse.countryCode( address.getCountryCode() );
        addressResponse.country( address.getCountry() );
        addressResponse.fullName( address.getFullName() );
        addressResponse.mobile( address.getMobile() );
        addressResponse.alternateMobile( address.getAlternateMobile() );
        addressResponse.pincode( address.getPincode() );
        addressResponse.state( address.getState() );
        addressResponse.city( address.getCity() );
        addressResponse.district( address.getDistrict() );
        addressResponse.addressLine( address.getAddressLine() );
        addressResponse.landmark( address.getLandmark() );
        addressResponse.isDefault( address.getIsDefault() );

        return addressResponse.build();
    }

    @Override
    public CustomerSummaryResponse toCustomerSummary(User user) {
        if ( user == null ) {
            return null;
        }

        CustomerSummaryResponse.CustomerSummaryResponseBuilder customerSummaryResponse = CustomerSummaryResponse.builder();

        customerSummaryResponse.id( user.getId() );
        customerSummaryResponse.name( user.getName() );
        customerSummaryResponse.email( user.getEmail() );
        customerSummaryResponse.mobile( user.getMobile() );

        return customerSummaryResponse.build();
    }

    @Override
    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.productId( itemProductId( item ) );
        orderItemResponse.productSlug( itemProductSlug( item ) );
        orderItemResponse.id( item.getId() );
        orderItemResponse.productName( item.getProductName() );
        orderItemResponse.productImageUrl( item.getProductImageUrl() );
        orderItemResponse.qty( item.getQty() );
        orderItemResponse.price( item.getPrice() );

        return orderItemResponse.build();
    }

    @Override
    public OrderResponse toOrderResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.customer( toCustomerSummary( order.getUser() ) );
        orderResponse.address( toAddressResponse( order.getAddress() ) );
        orderResponse.items( orderItemListToOrderItemResponseList( order.getItems() ) );
        orderResponse.payment( toPaymentResponse( order.getPayment() ) );
        orderResponse.id( order.getId() );
        orderResponse.orderNumber( order.getOrderNumber() );
        orderResponse.status( order.getStatus() );
        orderResponse.paymentStatus( order.getPaymentStatus() );
        orderResponse.subtotal( order.getSubtotal() );
        orderResponse.shippingCharge( order.getShippingCharge() );
        orderResponse.total( order.getTotal() );
        orderResponse.trackingNumber( order.getTrackingNumber() );
        orderResponse.statusNote( order.getStatusNote() );
        orderResponse.orderNotes( order.getOrderNotes() );
        orderResponse.deliveredAt( order.getDeliveredAt() );
        orderResponse.createdAt( order.getCreatedAt() );

        return orderResponse.build();
    }

    @Override
    public PaymentResponse toPaymentResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder paymentResponse = PaymentResponse.builder();

        paymentResponse.id( payment.getId() );
        paymentResponse.transactionId( payment.getTransactionId() );
        paymentResponse.razorpayOrderId( payment.getRazorpayOrderId() );
        paymentResponse.amount( payment.getAmount() );
        paymentResponse.status( payment.getStatus() );
        paymentResponse.method( payment.getMethod() );

        return paymentResponse.build();
    }

    private Long itemProductId(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }

    private String itemProductSlug(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getSlug();
    }

    protected List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( toOrderItemResponse( orderItem ) );
        }

        return list1;
    }
}
