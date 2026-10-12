package com.example.warehouse.user;

import com.example.warehouse.auth.UserResponse;
import com.example.warehouse.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<UserResponse>> list() {
        return Result.success(userService.listVisibleUsers());
    }

    @PutMapping("/{id}/profile")
    public Result<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return Result.success("用户资料已更新", userService.updateProfile(id, request));
    }

    @PutMapping("/{id}/role")
    public Result<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return Result.success("用户权限已更新", userService.updateRole(id, request));
    }

    @PutMapping("/me/password")
    public Result<Void> changeOwnPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeOwnPassword(request);
        return Result.success("密码修改成功", null);
    }
}
