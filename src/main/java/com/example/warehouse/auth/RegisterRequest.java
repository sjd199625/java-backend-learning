package com.example.warehouse.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @NotBlank(message = "用户名不能为空")
  @Size(max = 32, message = "用户名最多32个字符")
  String username,

  @NotBlank(message = "密码不能为空")
  @Size(min = 8, max = 32, message = "密码长度至少8位，最多32位")
  String password,

  @NotBlank(message = "昵称不能为空")
  @Size(max = 32, message = "昵称最多32个字符")
  String displayName
) {
}
