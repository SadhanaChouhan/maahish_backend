package com.maahish.user.service;

import com.maahish.auth.repository.RefreshTokenRepository;
import com.maahish.cart.repository.CartRepository;
import com.maahish.cart.repository.WishlistRepository;
import com.maahish.catalog.repository.ReviewRepository;
import com.maahish.catalog.service.ProductStockService;
import com.maahish.common.enums.UserRole;
import com.maahish.common.enums.UserStatus;
import com.maahish.common.exception.BadRequestException;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.common.exception.UnauthorizedException;
import com.maahish.notification.repository.NotificationPreferenceRepository;
import com.maahish.notification.repository.NotificationRepository;
import com.maahish.order.entity.PendingCheckout;
import com.maahish.order.enums.OrderStatus;
import com.maahish.order.enums.PendingCheckoutStatus;
import com.maahish.order.repository.OrderRepository;
import com.maahish.order.repository.PendingCheckoutRepository;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.returns.repository.ReturnRequestRepository;
import com.maahish.user.dto.request.DeleteAccountRequest;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    static final List<OrderStatus> BLOCKING_ORDER_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING,
            OrderStatus.SHIPPED,
            OrderStatus.OUT_FOR_DELIVERY
    );

    static final List<ReturnRequestStatus> OPEN_RETURN_STATUSES = List.of(
            ReturnRequestStatus.RETURN_REQUESTED,
            ReturnRequestStatus.RETURN_APPROVED,
            ReturnRequestStatus.CUSTOMER_SHIPPED,
            ReturnRequestStatus.PARCEL_RECEIVED,
            ReturnRequestStatus.QUALITY_CHECK,
            ReturnRequestStatus.REFUND_INITIATED,
            ReturnRequestStatus.EXCHANGE_PROCESSING,
            ReturnRequestStatus.EXCHANGE_SHIPPED
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WishlistRepository wishlistRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final ReviewRepository reviewRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final PendingCheckoutRepository pendingCheckoutRepository;
    private final ProductStockService productStockService;

    @Override
    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new ForbiddenException("Admin accounts cannot be deleted from the app");
        }
        if (user.getRole() == UserRole.ROLE_SELLER) {
            throw new BadRequestException(
                    "Seller accounts cannot be self-deleted. Please contact support@themaahish.com");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("This account has already been deleted");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Incorrect password");
        }

        assertNoBlockingActivity(user);

        expirePendingCheckouts(userId);
        purgePersonalData(user);

        if (mustAnonymize(user)) {
            anonymizeUser(user);
            log.info("event=account_anonymized userId={}", userId);
            return;
        }

        userRepository.delete(user);
        log.info("event=account_deleted userId={}", userId);
    }

    private void assertNoBlockingActivity(User user) {
        if (orderRepository.existsByUserAndStatusIn(user, BLOCKING_ORDER_STATUSES)) {
            throw new BadRequestException(
                    "You have active orders in progress. Please wait until they are delivered or cancelled.");
        }
        if (returnRequestRepository.existsByCustomerIdAndStatusIn(user.getId(), OPEN_RETURN_STATUSES)) {
            throw new BadRequestException(
                    "You have an open return or exchange request. Please resolve it before deleting your account.");
        }
    }

    private void expirePendingCheckouts(Long userId) {
        List<PendingCheckout> pending = pendingCheckoutRepository.findByUserIdAndStatus(
                userId, PendingCheckoutStatus.PENDING);
        for (PendingCheckout checkout : pending) {
            checkout.getItems().forEach(item ->
                    productStockService.releaseReservation(item.getProduct().getId(), item.getQty()));
            checkout.setStatus(PendingCheckoutStatus.EXPIRED);
        }
        if (!pending.isEmpty()) {
            pendingCheckoutRepository.saveAll(pending);
        }
    }

    private void purgePersonalData(User user) {
        refreshTokenRepository.deleteByUser(user);
        wishlistRepository.deleteByUser(user);
        notificationRepository.deleteByUser(user);
        notificationPreferenceRepository.findByUserId(user.getId())
                .ifPresent(notificationPreferenceRepository::delete);
        reviewRepository.deleteByUser(user);
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
        user.getAddresses().clear();
    }

    private boolean mustAnonymize(User user) {
        return orderRepository.countByUser(user) > 0
                || returnRequestRepository.countByCustomerId(user.getId()) > 0;
    }

    private void anonymizeUser(User user) {
        user.setName("Deleted User");
        user.setEmail(uniqueDeletedEmail(user.getId()));
        user.setMobile(null);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private String uniqueDeletedEmail(Long userId) {
        String candidate = "deleted-" + userId + "@deleted.maahish.local";
        if (!userRepository.existsByEmail(candidate)) {
            return candidate;
        }
        return "deleted-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8) + "@deleted.maahish.local";
    }
}
