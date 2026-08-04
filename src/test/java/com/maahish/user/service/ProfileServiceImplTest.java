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
import com.maahish.common.exception.UnauthorizedException;
import com.maahish.notification.repository.NotificationPreferenceRepository;
import com.maahish.notification.repository.NotificationRepository;
import com.maahish.order.repository.OrderRepository;
import com.maahish.order.repository.PendingCheckoutRepository;
import com.maahish.returns.repository.ReturnRequestRepository;
import com.maahish.user.dto.request.DeleteAccountRequest;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ReturnRequestRepository returnRequestRepository;
    @Mock private PendingCheckoutRepository pendingCheckoutRepository;
    @Mock private ProductStockService productStockService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void deleteAccount_whenAdminRole_throwsForbidden() {
        User admin = User.builder()
                .id(1L)
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .password("hash")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ForbiddenException.class, () ->
                profileService.deleteAccount(1L, requestWithPassword("secret")));
    }

    @Test
    void deleteAccount_whenWrongPassword_throwsUnauthorized() {
        User user = customer(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                profileService.deleteAccount(2L, requestWithPassword("wrong")));
    }

    @Test
    void deleteAccount_whenNoHistory_hardDeletesUser() {
        User user = customer(3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(orderRepository.existsByUserAndStatusIn(user, ProfileServiceImpl.BLOCKING_ORDER_STATUSES)).thenReturn(false);
        when(returnRequestRepository.existsByCustomerIdAndStatusIn(3L, ProfileServiceImpl.OPEN_RETURN_STATUSES)).thenReturn(false);
        when(pendingCheckoutRepository.findByUserIdAndStatus(3L, com.maahish.order.enums.PendingCheckoutStatus.PENDING))
                .thenReturn(java.util.List.of());
        when(cartRepository.findByUserId(3L)).thenReturn(Optional.empty());
        when(orderRepository.countByUser(user)).thenReturn(0L);
        when(returnRequestRepository.countByCustomerId(3L)).thenReturn(0L);

        profileService.deleteAccount(3L, requestWithPassword("secret"));

        verify(userRepository).delete(user);
    }

    private static User customer(Long id) {
        return User.builder()
                .id(id)
                .role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .password("hash")
                .email("user@example.com")
                .name("Test User")
                .build();
    }

    private static DeleteAccountRequest requestWithPassword(String password) {
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword(password);
        return request;
    }
}
