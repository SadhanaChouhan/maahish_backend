package com.maahish.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @Size(max = 500, message = "Order notes must not exceed 500 characters")
    private String orderNotes;

    /** When set, checkout uses these items directly and does not read or modify the cart. */
    @Valid
    private List<BuyNowItemRequest> buyNowItems;
}
