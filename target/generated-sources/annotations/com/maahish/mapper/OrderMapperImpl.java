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
import com.maahish.entity.Product;
import com.maahish.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T21:32:43+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
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
