package com.example.warehouse.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 32, message = "昵称最多32个字符")
        String displayName
) {
}
