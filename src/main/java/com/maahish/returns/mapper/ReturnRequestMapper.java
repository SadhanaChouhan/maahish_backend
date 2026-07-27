package com.maahish.returns.mapper;

import com.maahish.order.entity.OrderItem;
import com.maahish.returns.repository.RefundTransactionRepository;
import com.maahish.returns.dto.response.RefundTransactionResponse;
import com.maahish.returns.dto.response.ReturnEligibilityResponse;
import com.maahish.returns.entity.ReturnHistory;
import com.maahish.returns.dto.response.ReturnHistoryResponse;
import com.maahish.returns.entity.ReturnImage;
import com.maahish.returns.dto.response.ReturnImageResponse;
import com.maahish.returns.entity.ReturnRequest;
import com.maahish.returns.dto.response.ReturnRequestResponse;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.returns.entity.ReturnShipment;
import com.maahish.returns.dto.response.ReturnShipmentResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReturnRequestMapper {

    private static final String RETURN_INSTRUCTIONS = """
            Your return has been approved. Please ship the product using any courier service \
            (Blue Dart, DTDC, Delhivery, India Post, etc.) to the address provided by Maahish support.

            Pack the item securely in its original packaging with all tags and accessories.
            After dispatch, submit your courier details (company name, AWB/tracking number, and dispatch date) \
            from your return request page within 7 days.""";

    private final RefundTransactionRepository refundTransactionRepository;

    public ReturnRequestResponse toResponse(ReturnRequest entity) {
        return toResponse(entity, null);
    }

    public ReturnRequestResponse toResponse(ReturnRequest entity, ReturnEligibilityResponse eligibility) {
        OrderItem item = entity.getOrderItem();
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQty()));

        ReturnRequestResponse.ReturnRequestResponseBuilder builder = ReturnRequestResponse.builder()
                .id(entity.getId())
                .returnNumber(entity.getReturnNumber())
                .orderId(entity.getOrder().getId())
                .orderNumber(entity.getOrder().getOrderNumber())
                .orderItemId(item.getId())
                .productName(item.getProductName())
                .productImageUrl(item.getProductImageUrl())
                .qty(item.getQty())
                .lineTotal(lineTotal)
                .sellerId(entity.getSeller().getId())
                .sellerBusinessName(entity.getSeller().getBusinessName())
                .returnType(entity.getReturnType())
                .reason(entity.getReason())
                .description(entity.getDescription())
                .preferredResolution(entity.getPreferredResolution())
                .status(entity.getStatus())
                .adminRemarks(entity.getAdminRemarks())
                .rejectionReason(entity.getRejectionReason())
                .customerRemarks(entity.getCustomerRemarks())
                .refundAmount(entity.getRefundAmount())
                .exchangeTrackingNumber(entity.getExchangeTrackingNumber())
                .deliveredAtSnapshot(entity.getDeliveredAtSnapshot())
                .eligibility(eligibility)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getStatus() == ReturnRequestStatus.RETURN_APPROVED) {
            builder.returnInstructions(RETURN_INSTRUCTIONS);
        }

        if (entity.getImages() != null) {
            builder.images(entity.getImages().stream()
                    .sorted(Comparator.comparingInt(ReturnImage::getSortOrder))
                    .map(img -> ReturnImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .sortOrder(img.getSortOrder())
                            .build())
                    .toList());
        }

        if (entity.getShipment() != null) {
            ReturnShipment s = entity.getShipment();
            builder.shipment(ReturnShipmentResponse.builder()
                    .id(s.getId())
                    .courierCompany(s.getCourierCompany())
                    .trackingNumber(s.getTrackingNumber())
                    .dispatchDate(s.getDispatchDate())
                    .receiptUrl(s.getReceiptUrl())
                    .createdAt(s.getCreatedAt())
                    .build());
        }

        refundTransactionRepository.findByReturnRequestId(entity.getId())
                .ifPresent(refund -> builder.refundTransaction(RefundTransactionResponse.builder()
                        .id(refund.getId())
                        .razorpayRefundId(refund.getRazorpayRefundId())
                        .amount(refund.getAmount())
                        .status(refund.getStatus())
                        .createdAt(refund.getCreatedAt())
                        .build()));

        if (entity.getHistory() != null) {
            builder.history(entity.getHistory().stream()
                    .sorted(Comparator.comparing(ReturnHistory::getCreatedAt))
                    .map(h -> ReturnHistoryResponse.builder()
                            .fromStatus(h.getFromStatus())
                            .toStatus(h.getToStatus())
                            .remarks(h.getRemarks())
                            .changedByUserId(h.getChangedByUserId())
                            .changedByRole(h.getChangedByRole())
                            .createdAt(h.getCreatedAt())
                            .build())
                    .toList());
        }

        return builder.build();
    }

    public List<ReturnRequestResponse> toResponseList(List<ReturnRequest> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}
