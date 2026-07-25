package com.maahish.mapper;

import com.maahish.dto.response.SellerResponse;
import com.maahish.dto.response.SellerSummaryResponse;
import com.maahish.entity.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

    public SellerSummaryResponse toSummary(Seller seller) {
        if (seller == null) {
            return null;
        }
        return SellerSummaryResponse.builder()
                .id(seller.getId())
                .businessName(seller.getBusinessName())
                .ownerName(seller.getOwnerName())
                .email(seller.getEmail())
                .mobile(seller.getMobile())
                .city(seller.getCity())
                .state(seller.getState())
                .status(seller.getStatus())
                .businessLogoUrl(seller.getBusinessLogoUrl())
                .productCount(seller.getProducts() != null ? (long) seller.getProducts().size() : null)
                .createdAt(seller.getCreatedAt())
                .platformOwned(seller.getPlatformOwned())
                .build();
    }

    public SellerSummaryResponse toSummary(Seller seller, long productCount) {
        SellerSummaryResponse summary = toSummary(seller);
        if (summary != null) {
            summary.setProductCount(productCount);
            summary.setPlatformOwned(seller.getPlatformOwned());
        }
        return summary;
    }

    public SellerResponse toResponse(Seller seller) {
        if (seller == null) {
            return null;
        }
        return SellerResponse.builder()
                .id(seller.getId())
                .userId(seller.getUser() != null ? seller.getUser().getId() : null)
                .businessName(seller.getBusinessName())
                .ownerName(seller.getOwnerName())
                .email(seller.getEmail())
                .mobile(seller.getMobile())
                .businessAddress(seller.getBusinessAddress())
                .city(seller.getCity())
                .state(seller.getState())
                .pincode(seller.getPincode())
                .gst(seller.getGst())
                .pan(seller.getPan())
                .bankAccountHolder(seller.getBankAccountHolder())
                .bankAccountNumber(maskAccountNumber(seller.getBankAccountNumber()))
                .bankIfsc(seller.getBankIfsc())
                .bankName(seller.getBankName())
                .upiId(seller.getUpiId())
                .businessLogoUrl(seller.getBusinessLogoUrl())
                .profileImageUrl(seller.getProfileImageUrl())
                .status(seller.getStatus())
                .rejectionReason(seller.getRejectionReason())
                .platformOwned(seller.getPlatformOwned())
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .build();
    }

    public SellerResponse toDetailedResponse(Seller seller, long productCount) {
        SellerResponse response = toResponse(seller);
        if (response != null) {
            response.setProductCount(productCount);
        }
        return response;
    }

    public SellerResponse toAdminResponse(Seller seller, long productCount) {
        SellerResponse response = SellerResponse.builder()
                .id(seller.getId())
                .userId(seller.getUser() != null ? seller.getUser().getId() : null)
                .businessName(seller.getBusinessName())
                .ownerName(seller.getOwnerName())
                .email(seller.getEmail())
                .mobile(seller.getMobile())
                .businessAddress(seller.getBusinessAddress())
                .city(seller.getCity())
                .state(seller.getState())
                .pincode(seller.getPincode())
                .gst(seller.getGst())
                .pan(seller.getPan())
                .bankAccountHolder(seller.getBankAccountHolder())
                .bankAccountNumber(seller.getBankAccountNumber())
                .bankIfsc(seller.getBankIfsc())
                .bankName(seller.getBankName())
                .upiId(seller.getUpiId())
                .businessLogoUrl(seller.getBusinessLogoUrl())
                .profileImageUrl(seller.getProfileImageUrl())
                .status(seller.getStatus())
                .rejectionReason(seller.getRejectionReason())
                .platformOwned(seller.getPlatformOwned())
                .productCount(productCount)
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .build();
        return response;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }
}
