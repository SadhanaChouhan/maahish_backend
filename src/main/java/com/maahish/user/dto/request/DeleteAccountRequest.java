package com.maahish.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountRequest {

    @NotBlank(message = "Password is required to confirm account deletion")
    private String password;
}
