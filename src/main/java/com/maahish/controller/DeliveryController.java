package com.maahish.controller;

import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.DeliveryEstimateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/delivery")
@Tag(name = "Delivery", description = "Delivery estimation by PIN code")
public class DeliveryController {

    private static final Map<String, String[]> PINCODE_REGIONS = Map.of(
            "45", new String[]{"Indore", "Madhya Pradesh"},
            "11", new String[]{"Delhi", "Delhi"},
            "40", new String[]{"Mumbai", "Maharashtra"},
            "56", new String[]{"Bengaluru", "Karnataka"},
            "60", new String[]{"Chennai", "Tamil Nadu"}
    );

    @GetMapping("/estimate/{pincode}")
    @Operation(summary = "Estimate delivery by PIN code")
    public ResponseEntity<ApiResponse<DeliveryEstimateResponse>> estimate(@PathVariable String pincode) {
        if (!pincode.matches("^[1-9][0-9]{5}$")) {
            DeliveryEstimateResponse invalid = DeliveryEstimateResponse.builder()
                    .pincode(pincode)
                    .serviceable(false)
                    .deliveryMessage("Invalid PIN code format")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(invalid));
        }

        String prefix = pincode.substring(0, 2);
        String[] region = PINCODE_REGIONS.getOrDefault(prefix, new String[]{"Your City", "India"});
        int minDays = prefix.equals("45") ? 2 : 4;
        int maxDays = prefix.equals("45") ? 4 : 7;

        DeliveryEstimateResponse response = DeliveryEstimateResponse.builder()
                .pincode(pincode)
                .city(region[0])
                .state(region[1])
                .estimatedDaysMin(minDays)
                .estimatedDaysMax(maxDays)
                .serviceable(true)
                .deliveryMessage("Delivery expected in " + minDays + "-" + maxDays + " business days")
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
