package com.maahish.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
    private String photoUrl;
}
