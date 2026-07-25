package com.maahish.security;

import com.maahish.constants.AppConstants;
import com.maahish.entity.User;
import com.maahish.enums.UserRole;
import com.maahish.exception.ForbiddenException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.repository.UserRepository;
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
