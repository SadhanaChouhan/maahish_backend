package com.maahish.admin.service;

import com.maahish.admin.dto.response.AdminDashboardResponse;
import com.maahish.order.repository.OrderRepository;
import com.maahish.order.enums.OrderStatus;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.settlement.repository.SellerSettlementRepository;
import com.maahish.seller.enums.SellerStatus;
import com.maahish.settlement.enums.SettlementStatus;
import com.maahish.user.entity.User;
import com.maahish.user.mapper.UserMapper;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.dto.response.UserResponse;
import com.maahish.common.enums.UserRole;
import com.maahish.common.enums.UserStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final List<UserRole> NON_SELLER_ROLES = List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;
    private final SellerSettlementRepository settlementRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        BigDecimal revenue = orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(o -> o.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = settlementRepository.sumTotalCommissionEarned();
        long pendingSettlements = settlementRepository.countBySettlementStatus(SettlementStatus.PENDING)
                + settlementRepository.countBySettlementStatus(SettlementStatus.PROCESSING);
        long paidSettlements = settlementRepository.countBySettlementStatus(SettlementStatus.PAID);
        BigDecimal pendingAmount = settlementRepository.sumNetAmountByStatuses(
                List.of(SettlementStatus.PENDING, SettlementStatus.PROCESSING));
        BigDecimal paidAmount = settlementRepository.sumNetAmountByStatuses(List.of(SettlementStatus.PAID));

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countByStatus(ProductStatus.ACTIVE))
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .totalRevenue(revenue)
                .totalSellers(sellerRepository.count())
                .pendingSellers(sellerRepository.countByStatus(SellerStatus.PENDING))
                .totalCommissionEarned(totalCommission != null ? totalCommission : BigDecimal.ZERO)
                .pendingSettlements(pendingSettlements)
                .paidSettlements(paidSettlements)
                .pendingSettlementAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO)
                .paidSettlementAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(UserStatus status, int page, int size) {
        var pageable = PaginationUtil.createPageable(page, size, "createdAt", "desc");
        Page<User> users = status != null
                ? userRepository.findByStatusAndRoleIn(status, NON_SELLER_ROLES, pageable)
                : userRepository.findByRoleIn(NON_SELLER_ROLES, pageable);
        return PageMapper.toPageResponse(users, userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(status);
        userRepository.save(user);

        if (user.getRole() == UserRole.ROLE_SELLER
                && (status == UserStatus.INACTIVE || status == UserStatus.BLOCKED)) {
            sellerRepository.findByUserId(user.getId()).ifPresent(seller -> {
                if (!Boolean.TRUE.equals(seller.getPlatformOwned())) {
                    seller.setStatus(SellerStatus.INACTIVE);
                    sellerRepository.save(seller);
                }
            });
        }

        return userMapper.toUserResponse(user);
    }
}
