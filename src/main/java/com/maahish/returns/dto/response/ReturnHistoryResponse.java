package com.maahish.returns.dto.response;

import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.common.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnHistoryResponse {

    private ReturnRequestStatus fromStatus;
    private ReturnRequestStatus toStatus;
    private String remarks;
    private Long changedByUserId;
    private UserRole changedByRole;
    private LocalDateTime createdAt;
}
