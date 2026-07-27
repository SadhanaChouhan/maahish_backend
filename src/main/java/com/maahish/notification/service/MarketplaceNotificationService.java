package com.maahish.notification.service;

import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.notification.enums.NotificationReferenceType;
import com.maahish.notification.enums.NotificationType;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.catalog.entity.Product;
import com.maahish.returns.entity.ReturnRequest;
import com.maahish.catalog.entity.Review;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.enums.SellerStatus;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketplaceNotificationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final Set<SellerStatus> ACTIVE_SELLER_STATUSES = Set.of(SellerStatus.APPROVED, SellerStatus.ACTIVE);

    private final NotificationDispatcher dispatcher;
    private final UserRepository userRepository;
    private final MailService mailService;

    public void notifyAdminsSellerRegistered(Seller seller) {
        String message = """
                A new seller has registered and is waiting for approval.

                Seller Name: %s
                Business Name: %s
                Registration Date: %s

                Review the seller profile from your admin dashboard.""".formatted(
                seller.getOwnerName(),
                seller.getBusinessName(),
                seller.getCreatedAt().format(DATE_FORMAT));

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(admin)
                    .type(NotificationType.SELLER_REGISTRATION_PENDING)
                    .title("New seller registration")
                    .message(message)
                    .referenceType(NotificationReferenceType.SELLER)
                    .referenceId(seller.getId())
                    .inApp(true)
                    .email(false)
                    .sms(true)
                    .smsMobile(admin.getMobile())
                    .mandatory(true)
                    .actionPath("/admin/sellers/" + seller.getId())
                    .build());
        }
    }

    public void notifySellerApproved(Seller seller) {
        String message = "Congratulations! Your seller account has been approved. "
                + "You can now log in and start listing your products on Maahish.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(seller.getUser())
                .type(NotificationType.SELLER_APPROVED)
                .title("Seller account approved")
                .message(message)
                .referenceType(NotificationReferenceType.SELLER)
                .referenceId(seller.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(seller.getMobile())
                .mandatory(true)
                .actionPath("/seller/dashboard")
                .build());

        mailService.sendSellerApprovalEmail(seller.getEmail(), seller.getBusinessName());
    }

    public void notifySellerRejected(Seller seller, String reason) {
        String message = "Your seller registration on Maahish was not approved."
                + (reason != null && !reason.isBlank() ? " Reason: " + reason : "")
                + " Contact support if you need assistance.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(seller.getUser())
                .type(NotificationType.SELLER_REJECTED)
                .title("Seller registration rejected")
                .message(message)
                .referenceType(NotificationReferenceType.SELLER)
                .referenceId(seller.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(seller.getMobile())
                .mandatory(true)
                .actionPath("/seller/profile")
                .build());

        mailService.sendSellerRejectionEmail(seller.getEmail(), seller.getBusinessName(), reason);
    }

    public void notifySellerDeactivated(Seller seller, SellerStatus status) {
        String statusLabel = status.name().toLowerCase().replace('_', ' ');
        String message = "Your Maahish seller account has been " + statusLabel
                + ". You cannot access the seller dashboard until your account is reactivated. Contact support for help.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(seller.getUser())
                .type(NotificationType.SELLER_SUSPENDED)
                .title("Seller account " + statusLabel)
                .message(message)
                .referenceType(NotificationReferenceType.SELLER)
                .referenceId(seller.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(seller.getMobile())
                .mandatory(true)
                .actionPath("/seller/profile")
                .build());
    }

    public void notifyNewOrder(Order order) {
        notifyAdminsNewOrder(order);
        notifySellersNewOrder(order);
    }

    private void notifyAdminsNewOrder(Order order) {
        String sellerNames = collectSellerNames(order);
        String message = """
                A new order has been placed.

                Order ID: %s
                Customer Name: %s
                Seller Name(s): %s
                Total Order Value: Rs %s
                Order Date: %s

                View the order from your admin dashboard.""".formatted(
                order.getOrderNumber(),
                order.getUser().getName(),
                sellerNames,
                order.getTotal(),
                order.getCreatedAt().format(DATE_FORMAT));

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(admin)
                    .type(NotificationType.NEW_ORDER)
                    .title("New order placed")
                    .message(message)
                    .referenceType(NotificationReferenceType.ORDER)
                    .referenceId(order.getId())
                    .inApp(true)
                    .email(false)
                    .sms(true)
                    .smsMobile(admin.getMobile())
                    .mandatory(true)
                    .actionPath("/admin/orders")
                    .build());
        }
    }

    private void notifySellersNewOrder(Order order) {
        Set<Long> notifiedSellerIds = new LinkedHashSet<>();
        for (OrderItem item : order.getItems()) {
            Seller seller = item.getProduct() != null ? item.getProduct().getSeller() : null;
            if (seller == null || Boolean.TRUE.equals(seller.getPlatformOwned())
                    || !notifiedSellerIds.add(seller.getId())
                    || !ACTIVE_SELLER_STATUSES.contains(seller.getStatus())) {
                continue;
            }

            String message = "You have received a new order.";

            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(seller.getUser())
                    .type(NotificationType.NEW_ORDER)
                    .title("New order received")
                    .message(message)
                    .referenceType(NotificationReferenceType.ORDER)
                    .referenceId(order.getId())
                    .inApp(true)
                    .email(false)
                    .sms(true)
                    .smsMobile(seller.getMobile())
                    .mandatory(true)
                    .actionPath("/seller/orders")
                    .build());
        }
    }

    public void notifyCustomerOrderConfirmed(Order order, Seller seller) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber()
                + " has been confirmed by the seller and is being prepared for dispatch.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(customer)
                .type(NotificationType.ORDER_CONFIRMED)
                .title("Order confirmed")
                .message(message)
                .referenceType(NotificationReferenceType.ORDER)
                .referenceId(order.getId())
                .inApp(true)
                .email(false)
                .sms(false)
                .smsMobile(resolveCustomerMobile(order))
                .mandatory(true)
                .actionPath("/orders/" + order.getOrderNumber())
                .build());

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(), order.getOrderNumber(),
                "Order confirmed", message);
    }

    public void notifyCustomerOrderShipped(Order order) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber()
                + " has been shipped. You can track its delivery from your Orders page.";
        if (order.getTrackingNumber() != null && !order.getTrackingNumber().isBlank()) {
            message += " Tracking: " + order.getTrackingNumber() + ".";
        }

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(customer)
                .type(NotificationType.ORDER_SHIPPED)
                .title("Order shipped")
                .message(message)
                .referenceType(NotificationReferenceType.ORDER)
                .referenceId(order.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(resolveCustomerMobile(order))
                .mandatory(true)
                .actionPath("/orders/" + order.getOrderNumber())
                .build());

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(), order.getOrderNumber(),
                "Order shipped", message);
    }

    public void notifyCustomerOutForDelivery(Order order) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber() + " is out for delivery.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(customer)
                .type(NotificationType.ORDER_OUT_FOR_DELIVERY)
                .title("Out for delivery")
                .message(message)
                .referenceType(NotificationReferenceType.ORDER)
                .referenceId(order.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(resolveCustomerMobile(order))
                .mandatory(true)
                .actionPath("/orders/" + order.getOrderNumber())
                .build());

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(), order.getOrderNumber(),
                "Out for delivery", message);
    }

    public void notifyCustomerOrderDelivered(Order order) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber()
                + " has been successfully delivered. Thank you for shopping with Maahish.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(customer)
                .type(NotificationType.ORDER_DELIVERED)
                .title("Order delivered")
                .message(message)
                .referenceType(NotificationReferenceType.ORDER)
                .referenceId(order.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(resolveCustomerMobile(order))
                .mandatory(true)
                .actionPath("/orders/" + order.getOrderNumber())
                .build());

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(), order.getOrderNumber(),
                "Order delivered",
                message + " You can request a return/exchange within 7 days and leave a product review from your order details page.");
    }

    public void notifySellersOrderDelivered(Order order) {
        Set<Long> notifiedSellerIds = new LinkedHashSet<>();
        for (OrderItem item : order.getItems()) {
            Seller seller = item.getProduct() != null ? item.getProduct().getSeller() : null;
            if (seller == null || Boolean.TRUE.equals(seller.getPlatformOwned())
                    || !notifiedSellerIds.add(seller.getId())
                    || !ACTIVE_SELLER_STATUSES.contains(seller.getStatus())) {
                continue;
            }

            String message = "Order " + order.getOrderNumber()
                    + " has been delivered to the customer. This order is now complete.";

            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(seller.getUser())
                    .type(NotificationType.ORDER_DELIVERED)
                    .title("Order delivered")
                    .message(message)
                    .referenceType(NotificationReferenceType.ORDER)
                    .referenceId(order.getId())
                    .inApp(true)
                    .email(false)
                    .sms(false)
                    .smsMobile(seller.getMobile())
                    .mandatory(true)
                    .actionPath("/seller/orders")
                    .build());
        }
    }

    public void notifySellerOutOfStock(Seller seller, String productName, Long productId) {
        String message = productName + " is now out of stock. Restock to continue selling.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(seller.getUser())
                .type(NotificationType.PRODUCT_OUT_OF_STOCK)
                .title("Product out of stock")
                .message(message)
                .referenceType(NotificationReferenceType.PRODUCT)
                .referenceId(productId)
                .inApp(true)
                .email(false)
                .sms(false)
                .smsMobile(seller.getMobile())
                .mandatory(false)
                .actionPath("/seller/products")
                .build());
    }

    private String collectSellerNames(Order order) {
        List<String> names = order.getItems().stream()
                .map(item -> item.getProduct() != null && item.getProduct().getSeller() != null
                        ? item.getProduct().getSeller().getBusinessName()
                        : null)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.toList());
        return names.isEmpty() ? "Maahish" : String.join(", ", names);
    }

    private String resolveCustomerMobile(Order order) {
        User customer = order.getUser();
        if (customer.getMobile() != null && !customer.getMobile().isBlank()) {
            return customer.getMobile();
        }
        return order.getAddress() != null ? order.getAddress().getMobile() : null;
    }

    public void notifyCustomerReturnSubmitted(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your " + returnRequest.getReturnType().name().toLowerCase()
                + " request " + returnRequest.getReturnNumber()
                + " for order " + returnRequest.getOrder().getOrderNumber()
                + " has been submitted. We will review it shortly.";

        dispatchCustomerReturnNotification(customer, NotificationType.RETURN_REQUEST_SUBMITTED,
                "Return request submitted", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));
    }

    public void notifyAdminsNewReturnRequest(ReturnRequest returnRequest) {
        String message = """
                A new return/exchange request requires review.

                Return ID: %s
                Order: %s
                Customer: %s
                Type: %s
                Reason: %s""".formatted(
                returnRequest.getReturnNumber(),
                returnRequest.getOrder().getOrderNumber(),
                returnRequest.getCustomer().getName(),
                returnRequest.getReturnType(),
                returnRequest.getReason());

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(admin)
                    .type(NotificationType.NEW_RETURN_REQUEST)
                    .title("New return request")
                    .message(message)
                    .referenceType(NotificationReferenceType.RETURN)
                    .referenceId(returnRequest.getId())
                    .inApp(true)
                    .email(false)
                    .sms(true)
                    .smsMobile(admin.getMobile())
                    .mandatory(true)
                    .actionPath("/admin/returns")
                    .build());
        }
    }

    public void notifyCustomerReturnApproved(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your return request " + returnRequest.getReturnNumber()
                + " has been approved. Please ship the product using any courier service "
                + "(Blue Dart, DTDC, Delhivery, India Post, etc.) and submit tracking details from your return page.";

        dispatchCustomerReturnNotification(customer, NotificationType.RETURN_APPROVED,
                "Return approved", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(),
                returnRequest.getOrder().getOrderNumber(), "Return approved", message);
    }

    public void notifyCustomerReturnRejected(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your return request " + returnRequest.getReturnNumber() + " was not approved."
                + (returnRequest.getRejectionReason() != null
                ? " Reason: " + returnRequest.getRejectionReason() : "");

        dispatchCustomerReturnNotification(customer, NotificationType.RETURN_REJECTED,
                "Return rejected", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(),
                returnRequest.getOrder().getOrderNumber(), "Return rejected", message);
    }

    public void notifyCustomerReturnShipReminder(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Reminder: Your approved return " + returnRequest.getReturnNumber()
                + " is awaiting shipment. Please dispatch the product and submit courier details.";

        dispatchCustomerReturnNotification(customer, NotificationType.RETURN_SHIP_REMINDER,
                "Ship your return", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));
    }

    public void notifyAdminsCustomerShippedReturn(ReturnRequest returnRequest) {
        String message = "Customer shipped return " + returnRequest.getReturnNumber()
                + " for order " + returnRequest.getOrder().getOrderNumber() + ". Please track incoming parcel.";

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(admin)
                    .type(NotificationType.CUSTOMER_SHIPPED_RETURN)
                    .title("Customer shipped return")
                    .message(message)
                    .referenceType(NotificationReferenceType.RETURN)
                    .referenceId(returnRequest.getId())
                    .inApp(true)
                    .email(false)
                    .sms(true)
                    .smsMobile(admin.getMobile())
                    .mandatory(true)
                    .actionPath("/admin/returns")
                    .build());
        }
    }

    public void notifyCustomerParcelReceived(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "We have received your returned parcel for request "
                + returnRequest.getReturnNumber() + ". Quality inspection is in progress.";

        dispatchCustomerReturnNotification(customer, NotificationType.RETURN_PARCEL_RECEIVED,
                "Parcel received", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));
    }

    public void notifyAdminsRefundPending(ReturnRequest returnRequest) {
        String message = "Refund pending for return " + returnRequest.getReturnNumber()
                + ". Amount: Rs " + returnRequest.getRefundAmount();

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(admin)
                    .type(NotificationType.REFUND_PENDING)
                    .title("Refund pending")
                    .message(message)
                    .referenceType(NotificationReferenceType.RETURN)
                    .referenceId(returnRequest.getId())
                    .inApp(true)
                    .email(false)
                    .sms(false)
                    .mandatory(true)
                    .actionPath("/admin/returns")
                    .build());
        }
    }

    public void notifyCustomerRefundCompleted(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Refund of Rs " + returnRequest.getRefundAmount()
                + " for return " + returnRequest.getReturnNumber() + " has been completed.";

        dispatchCustomerReturnNotification(customer, NotificationType.REFUND_COMPLETED,
                "Refund completed", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));

        mailService.sendOrderUpdateEmail(customer.getEmail(), customer.getName(),
                returnRequest.getOrder().getOrderNumber(), "Refund completed", message);
    }

    public void notifySellerExchangeRequested(ReturnRequest returnRequest) {
        Seller seller = returnRequest.getSeller();
        if (seller == null || seller.getUser() == null) {
            return;
        }

        String message = "An exchange has been approved for order "
                + returnRequest.getOrder().getOrderNumber()
                + ". Please prepare the replacement product.";

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(seller.getUser())
                .type(NotificationType.EXCHANGE_REQUESTED)
                .title("Exchange requested")
                .message(message)
                .referenceType(NotificationReferenceType.RETURN)
                .referenceId(returnRequest.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(seller.getMobile())
                .mandatory(true)
                .actionPath("/seller/returns")
                .build());

        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(seller.getUser())
                .type(NotificationType.REPLACEMENT_REQUIRED)
                .title("Replacement product required")
                .message("Prepare replacement for " + returnRequest.getOrderItem().getProductName())
                .referenceType(NotificationReferenceType.RETURN)
                .referenceId(returnRequest.getId())
                .inApp(true)
                .email(false)
                .sms(false)
                .mandatory(true)
                .actionPath("/seller/returns")
                .build());
    }

    public void notifyAdminsExchangePending(ReturnRequest returnRequest) {
        String message = "Exchange processing for return " + returnRequest.getReturnNumber()
                + " is pending admin/seller action.";

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(admin)
                    .type(NotificationType.EXCHANGE_PENDING)
                    .title("Exchange pending")
                    .message(message)
                    .referenceType(NotificationReferenceType.RETURN)
                    .referenceId(returnRequest.getId())
                    .inApp(true)
                    .email(false)
                    .sms(false)
                    .mandatory(true)
                    .actionPath("/admin/returns")
                    .build());
        }
    }

    public void notifyCustomerExchangeShipped(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your exchange replacement for " + returnRequest.getReturnNumber()
                + " has been shipped.";
        if (returnRequest.getExchangeTrackingNumber() != null) {
            message += " Tracking: " + returnRequest.getExchangeTrackingNumber() + ".";
        }

        dispatchCustomerReturnNotification(customer, NotificationType.EXCHANGE_SHIPPED,
                "Exchange shipped", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));
    }

    public void notifyCustomerExchangeCompleted(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your exchange for " + returnRequest.getReturnNumber() + " has been completed.";

        dispatchCustomerReturnNotification(customer, NotificationType.EXCHANGE_COMPLETED,
                "Exchange delivered", message, returnRequest, resolveCustomerMobile(returnRequest.getOrder()));
    }

    private void dispatchCustomerReturnNotification(User customer, NotificationType type, String title,
                                                    String message, ReturnRequest returnRequest, String mobile) {
        dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(customer)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(NotificationReferenceType.RETURN)
                .referenceId(returnRequest.getId())
                .inApp(true)
                .email(false)
                .sms(true)
                .smsMobile(mobile)
                .mandatory(true)
                .actionPath("/returns/" + returnRequest.getId())
                .build());
    }
}
