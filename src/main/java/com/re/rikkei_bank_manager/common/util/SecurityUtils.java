package com.re.rikkei_bank_manager.common.util;

import com.re.rikkei_bank_manager.security.CustomUserDetails;
import com.re.rikkei_bank_manager.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails detail) return detail.getUsername();
        if (principal instanceof String username) return username;
        return null;
    }

    public static User getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails detail) return detail.getUser();
        return null;
    }
}
