package com.example.warehouse.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequest(
        @NotBlank(message = "角色不能为空")
        String role
) {
}
