package com.maahish.returns.service;

import com.maahish.returns.dto.request.AdminExchangeShipRequest;
import com.maahish.returns.dto.request.AdminReturnQualityCheckRequest;
import com.maahish.returns.dto.request.AdminReturnReviewRequest;
import com.maahish.common.exception.BadRequestException;
import com.maahish.infrastructure.storage.service.CloudinaryService;
import com.maahish.returns.dto.request.CreateReturnRequestRequest;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.notification.service.MarketplaceNotificationService;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.order.repository.OrderItemRepository;
import com.maahish.order.enums.OrderStatus;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.payment.entity.Payment;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.returns.enums.PreferredResolution;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.service.ProductStockService;
import com.maahish.infrastructure.payment.service.RazorpayService;
import com.maahish.returns.entity.RefundTransaction;
import com.maahish.returns.repository.RefundTransactionRepository;
import com.maahish.returns.enums.RefundTransactionStatus;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.returns.dto.response.ReturnEligibilityResponse;
import com.maahish.returns.entity.ReturnHistory;
import com.maahish.returns.entity.ReturnImage;
import com.maahish.returns.entity.ReturnRequest;
import com.maahish.returns.mapper.ReturnRequestMapper;
import com.maahish.returns.repository.ReturnRequestRepository;
import com.maahish.returns.dto.response.ReturnRequestResponse;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.returns.entity.ReturnShipment;
import com.maahish.returns.dto.request.ReturnShipmentRequest;
import com.maahish.common.security.SecurityUtil;
import com.maahish.seller.entity.Seller;
import com.maahish.returns.dto.request.SellerExchangeReadyRequest;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.common.enums.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private static final int RETURN_WINDOW_DAYS = 7;
    private static final String RETURN_NUMBER_PREFIX = "RET";

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final ReturnRequestMapper returnRequestMapper;
    private final CloudinaryService cloudinaryService;
    private final RazorpayService razorpayService;
    private final ProductStockService productStockService;
    private final MarketplaceNotificationService marketplaceNotificationService;

    @Override
    @Transactional(readOnly = true)
    public ReturnEligibilityResponse checkEligibility(Long userId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        Order order = orderItem.getOrder();

        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this order item");
        }

        return buildEligibility(order, orderItem);
    }

    @Override
    @Transactional
    public ReturnRequestResponse createReturnRequest(Long userId, CreateReturnRequestRequest request) {
        SecurityUtil.requireCustomerRole();

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        Order order = orderItem.getOrder();

        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this order item");
        }

        ReturnEligibilityResponse eligibility = buildEligibility(order, orderItem);
        if (!eligibility.isEligible()) {
            throw new BadRequestException(eligibility.getReason());
        }

        if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new BadRequestException("Upload at least one image");
        }
        if (request.getImageUrls().size() > 5) {
            throw new BadRequestException("Maximum 5 images allowed");
        }

        Product product = orderItem.getProduct();
        if (product == null || product.getSeller() == null) {
            throw new BadRequestException("Cannot create return for this item");
        }

        BigDecimal lineTotal = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQty()));
        LocalDateTime deliveredAt = resolveDeliveredAt(order);

        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnNumber(generateReturnNumber())
                .order(order)
                .orderItem(orderItem)
                .customer(order.getUser())
                .seller(product.getSeller())
                .returnType(request.getReturnType())
                .reason(request.getReason())
                .description(request.getDescription())
                .preferredResolution(request.getPreferredResolution())
                .customerRemarks(request.getCustomerRemarks())
                .refundAmount(lineTotal)
                .deliveredAtSnapshot(deliveredAt)
                .status(ReturnRequestStatus.RETURN_REQUESTED)
                .build();

        List<ReturnImage> images = new ArrayList<>();
        for (int i = 0; i < request.getImageUrls().size(); i++) {
            String url = request.getImageUrls().get(i);
            images.add(ReturnImage.builder()
                    .returnRequest(returnRequest)
                    .imageUrl(url.trim())
                    .publicId(cloudinaryService.extractPublicIdFromUrl(url))
                    .sortOrder(i)
                    .build());
        }
        returnRequest.setImages(images);

        returnRequest = returnRequestRepository.save(returnRequest);
        recordHistory(returnRequest, null, ReturnRequestStatus.RETURN_REQUESTED,
                "Return request submitted", userId, UserRole.ROLE_USER);

        marketplaceNotificationService.notifyAdminsNewReturnRequest(returnRequest);
        marketplaceNotificationService.notifyCustomerReturnSubmitted(returnRequest);

        log.info("event=return_request_created returnId={} returnNumber={} orderItemId={}",
                returnRequest.getId(), returnRequest.getReturnNumber(), orderItem.getId());

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    public FileUploadResponse uploadReturnImage(Long userId, MultipartFile file) {
        SecurityUtil.requireCustomerRole();
        return cloudinaryService.uploadReturnImage(file);
    }

    @Override
    @Transactional
    public ReturnRequestResponse submitShipment(Long userId, Long returnId, ReturnShipmentRequest request,
                                                MultipartFile receipt) {
        SecurityUtil.requireCustomerRole();
        ReturnRequest returnRequest = loadReturnForCustomer(userId, returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.RETURN_APPROVED) {
            throw new BadRequestException("Shipment details can only be submitted after return approval");
        }
        if (returnRequest.getShipment() != null) {
            throw new BadRequestException("Shipment details have already been submitted");
        }

        ReturnShipment shipment = ReturnShipment.builder()
                .returnRequest(returnRequest)
                .courierCompany(request.getCourierCompany())
                .trackingNumber(request.getTrackingNumber().trim())
                .dispatchDate(request.getDispatchDate())
                .build();

        if (receipt != null && !receipt.isEmpty()) {
            FileUploadResponse upload = cloudinaryService.uploadCourierReceipt(receipt);
            shipment.setReceiptUrl(upload.getUrl());
            shipment.setReceiptPublicId(upload.getPublicId());
        }

        returnRequest.setShipment(shipment);
        transitionStatus(returnRequest, ReturnRequestStatus.CUSTOMER_SHIPPED,
                "Customer shipped parcel via " + request.getCourierCompany(), userId, UserRole.ROLE_USER);

        returnRequest = returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifyAdminsCustomerShippedReturn(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReturnRequestResponse> getCustomerReturns(Long userId, int page, int size) {
        Page<ReturnRequest> result = returnRequestRepository.findByCustomerIdOrderByCreatedAtDesc(
                userId, PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(result, returnRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getCustomerReturn(Long userId, Long returnId) {
        ReturnRequest returnRequest = loadReturnForCustomer(userId, returnId);
        ReturnEligibilityResponse eligibility = buildEligibility(returnRequest.getOrder(), returnRequest.getOrderItem());
        return returnRequestMapper.toResponse(returnRequest, eligibility);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReturnRequestResponse> getAdminReturns(ReturnRequestStatus status, int page, int size) {
        Page<ReturnRequest> result = status != null
                ? returnRequestRepository.findByStatusOrderByCreatedAtDesc(status, PaginationUtil.createPageable(page, size, "createdAt", "desc"))
                : returnRequestRepository.findAllByOrderByCreatedAtDesc(PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(result, returnRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getAdminReturn(Long returnId) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);
        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse reviewReturn(Long adminUserId, Long returnId, AdminReturnReviewRequest request) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.RETURN_REQUESTED) {
            throw new BadRequestException("Only pending return requests can be reviewed");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            if (request.getAdminRemarks() != null) {
                returnRequest.setAdminRemarks(request.getAdminRemarks().trim());
            }
            transitionStatus(returnRequest, ReturnRequestStatus.RETURN_APPROVED,
                    request.getAdminRemarks(), adminUserId, UserRole.ROLE_ADMIN);
            returnRequestRepository.save(returnRequest);
            marketplaceNotificationService.notifyCustomerReturnApproved(returnRequest);
        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new BadRequestException("Rejection reason is required");
            }
            returnRequest.setRejectionReason(request.getRejectionReason().trim());
            if (request.getAdminRemarks() != null) {
                returnRequest.setAdminRemarks(request.getAdminRemarks().trim());
            }
            transitionStatus(returnRequest, ReturnRequestStatus.RETURN_REJECTED,
                    request.getRejectionReason(), adminUserId, UserRole.ROLE_ADMIN);
            returnRequestRepository.save(returnRequest);
            marketplaceNotificationService.notifyCustomerReturnRejected(returnRequest);
        }

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse markParcelReceived(Long adminUserId, Long returnId, String adminRemarks) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.CUSTOMER_SHIPPED) {
            throw new BadRequestException("Parcel can only be marked received after customer shipment");
        }

        if (adminRemarks != null && !adminRemarks.isBlank()) {
            returnRequest.setAdminRemarks(adminRemarks.trim());
        }
        transitionStatus(returnRequest, ReturnRequestStatus.PARCEL_RECEIVED,
                adminRemarks, adminUserId, UserRole.ROLE_ADMIN);
        returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifyCustomerParcelReceived(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse completeQualityCheck(Long adminUserId, Long returnId,
                                                      AdminReturnQualityCheckRequest request) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.PARCEL_RECEIVED) {
            throw new BadRequestException("Quality check can only be performed after parcel is received");
        }

        if (adminRemarksPresent(request.getAdminRemarks())) {
            returnRequest.setAdminRemarks(request.getAdminRemarks().trim());
        }

        if (!Boolean.TRUE.equals(request.getPassed())) {
            transitionStatus(returnRequest, ReturnRequestStatus.RETURN_REJECTED,
                    request.getAdminRemarks() != null ? request.getAdminRemarks() : "Failed quality inspection",
                    adminUserId, UserRole.ROLE_ADMIN);
            returnRequest.setRejectionReason("Product did not pass quality inspection");
            returnRequestRepository.save(returnRequest);
            marketplaceNotificationService.notifyCustomerReturnRejected(returnRequest);
            return returnRequestMapper.toResponse(returnRequest);
        }

        transitionStatus(returnRequest, ReturnRequestStatus.QUALITY_CHECK,
                request.getAdminRemarks(), adminUserId, UserRole.ROLE_ADMIN);

        if (returnRequest.getPreferredResolution() == PreferredResolution.REFUND) {
            transitionStatus(returnRequest, ReturnRequestStatus.REFUND_INITIATED,
                    "Refund initiated after quality check", adminUserId, UserRole.ROLE_ADMIN);
            returnRequestRepository.save(returnRequest);
            marketplaceNotificationService.notifyAdminsRefundPending(returnRequest);
            return processRefundInternal(returnRequest, adminUserId);
        }

        transitionStatus(returnRequest, ReturnRequestStatus.EXCHANGE_PROCESSING,
                "Exchange processing started", adminUserId, UserRole.ROLE_ADMIN);
        returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifySellerExchangeRequested(returnRequest);
        marketplaceNotificationService.notifyAdminsExchangePending(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse processRefund(Long adminUserId, Long returnId) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.REFUND_INITIATED) {
            throw new BadRequestException("Refund can only be processed when status is REFUND_INITIATED");
        }

        return processRefundInternal(returnRequest, adminUserId);
    }

    @Override
    @Transactional
    public ReturnRequestResponse shipExchange(Long adminUserId, Long returnId, AdminExchangeShipRequest request) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.EXCHANGE_PROCESSING) {
            throw new BadRequestException("Exchange can only be shipped when processing is in progress");
        }

        returnRequest.setExchangeTrackingNumber(request.getTrackingNumber().trim());
        if (adminRemarksPresent(request.getAdminRemarks())) {
            returnRequest.setAdminRemarks(request.getAdminRemarks().trim());
        }
        transitionStatus(returnRequest, ReturnRequestStatus.EXCHANGE_SHIPPED,
                "Replacement shipped. Tracking: " + request.getTrackingNumber(), adminUserId, UserRole.ROLE_ADMIN);
        returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifyCustomerExchangeShipped(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse completeExchange(Long adminUserId, Long returnId) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (returnRequest.getStatus() != ReturnRequestStatus.EXCHANGE_SHIPPED) {
            throw new BadRequestException("Exchange can only be completed after replacement is shipped");
        }

        transitionStatus(returnRequest, ReturnRequestStatus.EXCHANGE_COMPLETED,
                "Exchange completed", adminUserId, UserRole.ROLE_ADMIN);
        returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifyCustomerExchangeCompleted(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReturnRequestResponse> getSellerReturns(Long sellerUserId, int page, int size) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        Page<ReturnRequest> result = returnRequestRepository.findBySellerIdOrderByCreatedAtDesc(
                seller.getId(), PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(result, returnRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getSellerReturn(Long sellerUserId, Long returnId) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (!returnRequest.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not have access to this return request");
        }

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public ReturnRequestResponse markExchangeReady(Long sellerUserId, Long returnId, SellerExchangeReadyRequest request) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);

        if (!returnRequest.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not have access to this return request");
        }
        if (returnRequest.getStatus() != ReturnRequestStatus.EXCHANGE_PROCESSING) {
            throw new BadRequestException("Replacement can only be prepared during exchange processing");
        }

        String remarks = request.getSellerRemarks() != null ? request.getSellerRemarks().trim() : "Replacement ready";
        recordHistory(returnRequest, returnRequest.getStatus(), returnRequest.getStatus(),
                "Seller prepared replacement: " + remarks, sellerUserId, UserRole.ROLE_SELLER);
        returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifyAdminsExchangePending(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    @Override
    @Transactional
    public void sendShipReminders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
        List<ReturnRequest> pending = returnRequestRepository.findApprovedAwaitingShipment(
                ReturnRequestStatus.RETURN_APPROVED, cutoff);

        for (ReturnRequest returnRequest : pending) {
            returnRequest.setShipReminderSent(true);
            returnRequestRepository.save(returnRequest);
            marketplaceNotificationService.notifyCustomerReturnShipReminder(returnRequest);
        }
    }

    private ReturnRequestResponse processRefundInternal(ReturnRequest returnRequest, Long adminUserId) {
        if (refundTransactionRepository.findByReturnRequestId(returnRequest.getId()).isPresent()) {
            throw new BadRequestException("Refund has already been processed for this return");
        }

        Order order = returnRequest.getOrder();
        Payment payment = order.getPayment();
        if (payment == null || payment.getTransactionId() == null || payment.getTransactionId().isBlank()) {
            throw new BadRequestException("Payment record not found for refund");
        }

        BigDecimal refundAmount = returnRequest.getRefundAmount();
        RefundTransaction refundTransaction = RefundTransaction.builder()
                .returnRequest(returnRequest)
                .amount(refundAmount)
                .status(RefundTransactionStatus.PENDING)
                .build();

        try {
            RazorpayService.RazorpayRefundResult result = razorpayService.refundPayment(
                    payment.getTransactionId(),
                    refundAmount,
                    "Return " + returnRequest.getReturnNumber());
            refundTransaction.setRazorpayRefundId(result.razorpayRefundId());
            refundTransaction.setRawResponse(result.rawResponse());
            refundTransaction.setStatus(RefundTransactionStatus.COMPLETED);
        } catch (BadRequestException ex) {
            refundTransaction.setStatus(RefundTransactionStatus.FAILED);
            refundTransaction.setRawResponse(ex.getMessage());
            refundTransactionRepository.save(refundTransaction);
            throw ex;
        }

        refundTransactionRepository.save(refundTransaction);
        transitionStatus(returnRequest, ReturnRequestStatus.REFUND_COMPLETED,
                "Refund of Rs " + refundAmount + " completed", adminUserId, UserRole.ROLE_ADMIN);

        OrderItem item = returnRequest.getOrderItem();
        if (item.getProduct() != null) {
            productStockService.restoreStock(item.getProduct().getId(), item.getQty());
        }

        returnRequestRepository.save(returnRequest);
        marketplaceNotificationService.notifyCustomerRefundCompleted(returnRequest);

        return returnRequestMapper.toResponse(returnRequest);
    }

    private ReturnRequest loadReturnWithDetails(Long returnId) {
        return returnRequestRepository.findWithDetailsById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
    }

    private ReturnRequest loadReturnForCustomer(Long userId, Long returnId) {
        ReturnRequest returnRequest = loadReturnWithDetails(returnId);
        if (!returnRequest.getCustomer().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this return request");
        }
        return returnRequest;
    }

    private ReturnEligibilityResponse buildEligibility(Order order, OrderItem orderItem) {
        Optional<ReturnRequest> existing = returnRequestRepository.findByOrderItemId(orderItem.getId());

        if (existing.isPresent()) {
            ReturnRequest req = existing.get();
            return ReturnEligibilityResponse.builder()
                    .eligible(false)
                    .reason("A return/exchange request already exists for this item")
                    .existingReturnId(req.getId())
                    .existingReturnNumber(req.getReturnNumber())
                    .canReturn(false)
                    .canExchange(false)
                    .build();
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            return ReturnEligibilityResponse.builder()
                    .eligible(false)
                    .reason("Return/exchange is only available after delivery")
                    .canReturn(false)
                    .canExchange(false)
                    .build();
        }

        if (order.getPaymentStatus() != PaymentStatus.COMPLETED) {
            return ReturnEligibilityResponse.builder()
                    .eligible(false)
                    .reason("Payment must be completed before requesting a return")
                    .canReturn(false)
                    .canExchange(false)
                    .build();
        }

        LocalDateTime deliveredAt = resolveDeliveredAt(order);
        long daysSinceDelivery = ChronoUnit.DAYS.between(deliveredAt.toLocalDate(), LocalDateTime.now().toLocalDate());
        long daysRemaining = RETURN_WINDOW_DAYS - daysSinceDelivery;

        if (daysSinceDelivery > RETURN_WINDOW_DAYS) {
            return ReturnEligibilityResponse.builder()
                    .eligible(false)
                    .reason("Return/exchange window of 7 days has expired")
                    .daysRemaining(0L)
                    .canReturn(false)
                    .canExchange(false)
                    .build();
        }

        return ReturnEligibilityResponse.builder()
                .eligible(true)
                .daysRemaining(Math.max(daysRemaining, 0))
                .canReturn(true)
                .canExchange(true)
                .build();
    }

    private LocalDateTime resolveDeliveredAt(Order order) {
        if (order.getDeliveredAt() != null) {
            return order.getDeliveredAt();
        }
        return order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
    }

    private void transitionStatus(ReturnRequest returnRequest, ReturnRequestStatus newStatus,
                                  String remarks, Long userId, UserRole role) {
        ReturnRequestStatus previous = returnRequest.getStatus();
        returnRequest.setStatus(newStatus);
        recordHistory(returnRequest, previous, newStatus, remarks, userId, role);
    }

    private void recordHistory(ReturnRequest returnRequest, ReturnRequestStatus from,
                               ReturnRequestStatus to, String remarks, Long userId, UserRole role) {
        ReturnHistory history = ReturnHistory.builder()
                .returnRequest(returnRequest)
                .fromStatus(from)
                .toStatus(to)
                .remarks(remarks)
                .changedByUserId(userId)
                .changedByRole(role)
                .build();
        returnRequest.getHistory().add(history);
    }

    private String generateReturnNumber() {
        return RETURN_NUMBER_PREFIX + "-" + System.currentTimeMillis();
    }

    private boolean adminRemarksPresent(String remarks) {
        return remarks != null && !remarks.isBlank();
    }
}
