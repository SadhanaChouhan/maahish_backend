package com.maahish.returns.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnImageResponse {

    private Long id;
    private String imageUrl;
    private Integer sortOrder;
}
