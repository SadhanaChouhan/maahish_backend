package com.maahish.notification.service;

import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.common.constants.AppConstants;
import com.maahish.notification.enums.NotificationReferenceType;
import com.maahish.notification.enums.NotificationType;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.returns.entity.ReturnRequest;
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
        String registrationDate = seller.getCreatedAt().format(DATE_FORMAT);
        String message = """
                A new seller has registered and is waiting for approval.

                Seller Name: %s
                Business Name: %s
                Registration Date: %s""".formatted(
                seller.getOwnerName(),
                seller.getBusinessName(),
                registrationDate);

        String sellerUrl = AppConstants.BRAND_WEBSITE_URL + "/admin/sellers/" + seller.getId();

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
                    .mandatory(true)
                    .actionPath("/admin/sellers/" + seller.getId())
                    .build());

            mailService.sendAdminNewSellerEmail(
                    admin.getEmail(),
                    seller.getOwnerName(),
                    seller.getBusinessName(),
                    registrationDate,
                    sellerUrl
            );
        }
    }

    public void notifySellerApproved(Seller seller) {
        mailService.sendSellerApprovalEmail(seller.getEmail(), seller.getBusinessName());
    }

    public void notifySellerRejected(Seller seller, String reason) {
        mailService.sendSellerRejectionEmail(seller.getEmail(), seller.getBusinessName(), reason);
    }

    public void notifySellerDeactivated(Seller seller, SellerStatus status, String reason) {
        String statusLabel = status.name().toLowerCase().replace('_', ' ');
        mailService.sendSellerDeactivationEmail(seller.getEmail(), seller.getBusinessName(), statusLabel, reason);
    }

    public void notifyNewOrder(Order order) {
        notifyAdminsNewOrder(order);
        notifySellersNewOrder(order);
    }

    private void notifyAdminsNewOrder(Order order) {
        String sellerNames = collectSellerNames(order);
        String orderDate = order.getCreatedAt().format(DATE_FORMAT);
        String message = """
                A new order has been placed.

                Order ID: %s
                Customer Name: %s
                Seller Name(s): %s
                Total Order Value: Rs %s
                Order Date: %s""".formatted(
                order.getOrderNumber(),
                order.getUser().getName(),
                sellerNames,
                order.getTotal(),
                orderDate);

        String actionUrl = AppConstants.BRAND_WEBSITE_URL + "/admin/orders";

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
                    .mandatory(true)
                    .actionPath("/admin/orders")
                    .build());

            mailService.sendNewOrderEmail(admin.getEmail(), admin.getName(), order, sellerNames, actionUrl);
        }
    }

    private void notifySellersNewOrder(Order order) {
        Set<Long> notifiedSellerIds = new LinkedHashSet<>();
        String orderDate = order.getCreatedAt().format(DATE_FORMAT);
        String actionUrl = AppConstants.BRAND_WEBSITE_URL + "/seller/orders";

        for (OrderItem item : order.getItems()) {
            Seller seller = item.getProduct() != null ? item.getProduct().getSeller() : null;
            if (seller == null || Boolean.TRUE.equals(seller.getPlatformOwned())
                    || !notifiedSellerIds.add(seller.getId())
                    || !ACTIVE_SELLER_STATUSES.contains(seller.getStatus())) {
                continue;
            }

            String message = """
                    You have received a new order.

                    Order ID: %s
                    Customer Name: %s
                    Order Value: Rs %s
                    Order Date: %s""".formatted(
                    order.getOrderNumber(),
                    order.getUser().getName(),
                    order.getTotal(),
                    orderDate);

            dispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                    .user(seller.getUser())
                    .type(NotificationType.NEW_ORDER)
                    .title("New order received")
                    .message(message)
                    .referenceType(NotificationReferenceType.ORDER)
                    .referenceId(order.getId())
                    .inApp(true)
                    .email(false)
                    .mandatory(true)
                    .actionPath("/seller/orders")
                    .build());

            mailService.sendNewOrderEmail(
                    seller.getEmail(),
                    seller.getBusinessName(),
                    order,
                    seller.getBusinessName(),
                    actionUrl
            );
        }
    }

    public void notifyCustomerOrderConfirmed(Order order, Seller seller) {
        User customer = order.getUser();
        mailService.sendOrderConfirmedEmail(customer.getEmail(), customer.getName(), order.getOrderNumber());
    }

    public void notifyCustomerOrderShipped(Order order) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber()
                + " has been shipped and is on the way.";
        if (order.getTrackingNumber() != null && !order.getTrackingNumber().isBlank()) {
            message += " Tracking: " + order.getTrackingNumber() + ".";
        }

        dispatcher.dispatch(inAppOnly(customer, NotificationType.ORDER_SHIPPED,
                "Order shipped", message, NotificationReferenceType.ORDER, order.getId(),
                "/orders/" + order.getOrderNumber()));
    }

    public void notifyCustomerOutForDelivery(Order order) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber() + " is out for delivery.";

        dispatcher.dispatch(inAppOnly(customer, NotificationType.ORDER_OUT_FOR_DELIVERY,
                "Out for delivery", message, NotificationReferenceType.ORDER, order.getId(),
                "/orders/" + order.getOrderNumber()));
    }

    public void notifyCustomerOrderDelivered(Order order) {
        User customer = order.getUser();
        String message = "Your order " + order.getOrderNumber()
                + " has been delivered successfully. Thank you for shopping with Maahish.";

        dispatcher.dispatch(inAppOnly(customer, NotificationType.ORDER_DELIVERED,
                "Order delivered", message, NotificationReferenceType.ORDER, order.getId(),
                "/orders/" + order.getOrderNumber()));

        mailService.sendOrderDeliveredEmail(customer.getEmail(), customer.getName(), order.getOrderNumber());
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

            dispatcher.dispatch(inAppOnly(seller.getUser(), NotificationType.ORDER_DELIVERED,
                    "Order delivered", message, NotificationReferenceType.ORDER, order.getId(),
                    "/seller/orders"));
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
                .mandatory(false)
                .actionPath("/seller/products")
                .build());
    }

    public void notifyCustomerReturnSubmitted(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your " + returnRequest.getReturnType().name().toLowerCase()
                + " request " + returnRequest.getReturnNumber()
                + " for order " + returnRequest.getOrder().getOrderNumber()
                + " has been submitted. We will review it shortly.";

        dispatchReturnInApp(customer, NotificationType.RETURN_REQUEST_SUBMITTED,
                "Return request submitted", message, returnRequest);
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
            dispatchReturnInApp(admin, NotificationType.NEW_RETURN_REQUEST,
                    "New return request", message, returnRequest, "/admin/returns");
        }
    }

    public void notifyCustomerReturnApproved(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your return request " + returnRequest.getReturnNumber()
                + " has been approved. Please ship the product and submit tracking details from your return page.";

        dispatchReturnInApp(customer, NotificationType.RETURN_APPROVED,
                "Return approved", message, returnRequest);
    }

    public void notifyCustomerReturnRejected(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your return request " + returnRequest.getReturnNumber() + " was not approved."
                + (returnRequest.getRejectionReason() != null
                ? " Reason: " + returnRequest.getRejectionReason() : "");

        dispatchReturnInApp(customer, NotificationType.RETURN_REJECTED,
                "Return rejected", message, returnRequest);
    }

    public void notifyCustomerReturnShipReminder(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Reminder: Your approved return " + returnRequest.getReturnNumber()
                + " is awaiting shipment. Please dispatch the product and submit courier details.";

        dispatchReturnInApp(customer, NotificationType.RETURN_SHIP_REMINDER,
                "Ship your return", message, returnRequest);
    }

    public void notifyAdminsCustomerShippedReturn(ReturnRequest returnRequest) {
        String message = "Customer shipped return " + returnRequest.getReturnNumber()
                + " for order " + returnRequest.getOrder().getOrderNumber() + ". Please track incoming parcel.";

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatchReturnInApp(admin, NotificationType.CUSTOMER_SHIPPED_RETURN,
                    "Customer shipped return", message, returnRequest, "/admin/returns");
        }
    }

    public void notifyCustomerParcelReceived(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "We have received your returned parcel for request "
                + returnRequest.getReturnNumber() + ". Quality inspection is in progress.";

        dispatchReturnInApp(customer, NotificationType.RETURN_PARCEL_RECEIVED,
                "Product received", message, returnRequest);
    }

    public void notifyAdminsRefundPending(ReturnRequest returnRequest) {
        String message = "Refund pending approval for return " + returnRequest.getReturnNumber()
                + ". Amount: Rs " + returnRequest.getRefundAmount();

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatchReturnInApp(admin, NotificationType.REFUND_PENDING,
                    "Refund pending approval", message, returnRequest, "/admin/returns");
        }
    }

    public void notifyCustomerRefundCompleted(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Refund of Rs " + returnRequest.getRefundAmount()
                + " for return " + returnRequest.getReturnNumber() + " has been processed.";

        dispatchReturnInApp(customer, NotificationType.REFUND_COMPLETED,
                "Refund processed", message, returnRequest);
    }

    public void notifySellerExchangeRequested(ReturnRequest returnRequest) {
        Seller seller = returnRequest.getSeller();
        if (seller == null || seller.getUser() == null) {
            return;
        }

        String message = "An exchange has been approved for order "
                + returnRequest.getOrder().getOrderNumber()
                + ". Please prepare the replacement product.";

        dispatchReturnInApp(seller.getUser(), NotificationType.EXCHANGE_REQUESTED,
                "Exchange product required", message, returnRequest, "/seller/returns");
    }

    public void notifyAdminsExchangePending(ReturnRequest returnRequest) {
        String message = "Exchange processing for return " + returnRequest.getReturnNumber()
                + " is pending admin/seller action.";

        for (User admin : userRepository.findByRole(UserRole.ROLE_ADMIN)) {
            dispatchReturnInApp(admin, NotificationType.EXCHANGE_PENDING,
                    "Exchange pending", message, returnRequest, "/admin/returns");
        }
    }

    public void notifyCustomerExchangeShipped(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your exchange replacement for " + returnRequest.getReturnNumber()
                + " has been shipped.";
        if (returnRequest.getExchangeTrackingNumber() != null) {
            message += " Tracking: " + returnRequest.getExchangeTrackingNumber() + ".";
        }

        dispatchReturnInApp(customer, NotificationType.EXCHANGE_SHIPPED,
                "Exchange shipped", message, returnRequest);
    }

    public void notifyCustomerExchangeCompleted(ReturnRequest returnRequest) {
        User customer = returnRequest.getCustomer();
        String message = "Your exchange for " + returnRequest.getReturnNumber() + " has been delivered.";

        dispatchReturnInApp(customer, NotificationType.EXCHANGE_COMPLETED,
                "Exchange delivered", message, returnRequest);
    }

    private void dispatchReturnInApp(User user, NotificationType type, String title,
                                     String message, ReturnRequest returnRequest) {
        dispatchReturnInApp(user, type, title, message, returnRequest, "/returns/" + returnRequest.getId());
    }

    private void dispatchReturnInApp(User user, NotificationType type, String title,
                                     String message, ReturnRequest returnRequest, String actionPath) {
        dispatcher.dispatch(inAppOnly(user, type, title, message,
                NotificationReferenceType.RETURN, returnRequest.getId(), actionPath));
    }

    private NotificationDispatcher.DispatchRequest inAppOnly(User user, NotificationType type, String title,
                                                             String message, NotificationReferenceType referenceType,
                                                             Long referenceId, String actionPath) {
        return NotificationDispatcher.DispatchRequest.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .inApp(true)
                .email(false)
                .mandatory(true)
                .actionPath(actionPath)
                .build();
    }

    private String collectSellerNames(Order order) {
        List<String> names = order.getItems().stream()
                .map(item -> item.getProduct() != null && item.getProduct().getSeller() != null
                        ? item.getProduct().getSeller().getBusinessName()
                        : null)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.toList());
        return names.isEmpty() ? AppConstants.BRAND_NAME : String.join(", ", names);
    }
}
