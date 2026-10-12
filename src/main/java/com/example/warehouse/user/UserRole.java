package com.example.warehouse.user;

import java.util.Set;

public final class UserRole {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String USER = "USER";

    private static final Set<String> SUPPORTED_ROLES = Set.of(SUPER_ADMIN, USER);

    private UserRole() {
    }

    public static boolean isSupported(String role) {
        return SUPPORTED_ROLES.contains(role);
    }
}
