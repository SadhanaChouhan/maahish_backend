package com.maahish.security;

import com.maahish.enums.UserRole;
import com.maahish.exception.ForbiddenException;
import com.maahish.exception.UnauthorizedException;
import com.maahish.constants.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new UnauthorizedException("Authentication required");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static void requireCustomerRole() {
        if (getCurrentUser().getRole() != UserRole.ROLE_USER) {
            throw new ForbiddenException(AppConstants.CUSTOMER_PURCHASE_DENIED_MESSAGE);
        }
    }
}
