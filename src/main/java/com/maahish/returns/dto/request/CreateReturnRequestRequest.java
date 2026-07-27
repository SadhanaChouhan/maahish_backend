package com.maahish.returns.dto.request;

import com.maahish.returns.enums.PreferredResolution;
import com.maahish.returns.enums.ReturnReason;
import com.maahish.returns.enums.ReturnType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateReturnRequestRequest {

    @NotNull
    private Long orderItemId;

    @NotNull
    private ReturnType returnType;

    @NotNull
    private ReturnReason reason;

    @Size(max = 2000)
    private String description;

    @NotNull
    private PreferredResolution preferredResolution;

    @Size(min = 1, max = 5, message = "Upload between 1 and 5 images")
    private List<@NotBlank String> imageUrls;

    @Size(max = 1000)
    private String customerRemarks;
}
