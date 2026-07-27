package com.maahish.returns.service;

import com.maahish.returns.dto.request.AdminExchangeShipRequest;
import com.maahish.returns.dto.request.AdminReturnQualityCheckRequest;
import com.maahish.returns.dto.request.AdminReturnReviewRequest;
import com.maahish.returns.dto.request.CreateReturnRequestRequest;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.returns.dto.response.ReturnEligibilityResponse;
import com.maahish.returns.dto.response.ReturnRequestResponse;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.returns.dto.request.ReturnShipmentRequest;
import com.maahish.returns.dto.request.SellerExchangeReadyRequest;

import org.springframework.web.multipart.MultipartFile;

public interface ReturnRequestService {

    ReturnEligibilityResponse checkEligibility(Long userId, Long orderItemId);

    ReturnRequestResponse createReturnRequest(Long userId, CreateReturnRequestRequest request);

    FileUploadResponse uploadReturnImage(Long userId, MultipartFile file);

    ReturnRequestResponse submitShipment(Long userId, Long returnId, ReturnShipmentRequest request,
                                           MultipartFile receipt);

    PageResponse<ReturnRequestResponse> getCustomerReturns(Long userId, int page, int size);

    ReturnRequestResponse getCustomerReturn(Long userId, Long returnId);

    PageResponse<ReturnRequestResponse> getAdminReturns(ReturnRequestStatus status, int page, int size);

    ReturnRequestResponse getAdminReturn(Long returnId);

    ReturnRequestResponse reviewReturn(Long adminUserId, Long returnId, AdminReturnReviewRequest request);

    ReturnRequestResponse markParcelReceived(Long adminUserId, Long returnId, String adminRemarks);

    ReturnRequestResponse completeQualityCheck(Long adminUserId, Long returnId,
                                               AdminReturnQualityCheckRequest request);

    ReturnRequestResponse processRefund(Long adminUserId, Long returnId);

    ReturnRequestResponse shipExchange(Long adminUserId, Long returnId, AdminExchangeShipRequest request);

    ReturnRequestResponse completeExchange(Long adminUserId, Long returnId);

    PageResponse<ReturnRequestResponse> getSellerReturns(Long sellerUserId, int page, int size);

    ReturnRequestResponse getSellerReturn(Long sellerUserId, Long returnId);

    ReturnRequestResponse markExchangeReady(Long sellerUserId, Long returnId, SellerExchangeReadyRequest request);

    void sendShipReminders();
}
