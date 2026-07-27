package com.maahish.common.security;

import com.maahish.common.constants.AppConstants;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShoppingAccessValidator {

    private final UserRepository userRepository;

    public void requireCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != UserRole.ROLE_USER) {
            throw new ForbiddenException(AppConstants.CUSTOMER_PURCHASE_DENIED_MESSAGE);
        }
    }
}
