package com.maahish.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FabricTypeResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Boolean active;
}
