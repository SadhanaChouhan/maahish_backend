package com.maahish.dto.response;

import com.maahish.enums.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSummaryResponse {

    private Long id;
    private String businessName;
    private String ownerName;
    private String email;
    private String mobile;
    private String city;
    private String state;
    private SellerStatus status;
    private String businessLogoUrl;
    private Long productCount;
    private LocalDateTime createdAt;
    private Boolean platformOwned;
}
