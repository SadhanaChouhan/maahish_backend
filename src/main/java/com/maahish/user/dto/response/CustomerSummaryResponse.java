package com.maahish.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String mobile;
}
