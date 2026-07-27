package com.maahish.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FabricTypeRequest {

    @NotBlank(message = "Fabric type name is required")
    private String name;

    private String description;
    private Boolean active;
}
