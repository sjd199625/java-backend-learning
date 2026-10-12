package com.example.warehouse.user;

import com.example.warehouse.auth.AuthContext;
import com.example.warehouse.auth.LoginUser;
import com.example.warehouse.auth.UserResponse;
import com.example.warehouse.common.BusinessException;
import com.example.warehouse.common.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listVisibleUsers() {
        User currentUser = requireCurrentUser();
        if (isSuperAdmin(currentUser)) {
            return userRepository.findAll().stream().map(this::toResponse).toList();
        }
        return List.of(toResponse(currentUser));
    }

    public UserResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        User currentUser = requireCurrentUser();
        if (!isSuperAdmin(currentUser) && !Objects.equals(currentUser.getId(), userId)) {
            throw BusinessException.forbidden("普通用户只能修改自己的资料");
        }

        User targetUser = requireUser(userId);
        targetUser.setDisplayName(request.displayName().trim());
        return toResponse(userRepository.save(targetUser));
    }

    public UserResponse updateRole(Long userId, UpdateUserRoleRequest request) {
        User currentUser = requireCurrentUser();
        requireSuperAdmin(currentUser);

        String role = request.role().trim().toUpperCase(Locale.ROOT);
        if (!UserRole.isSupported(role)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "角色只能是 SUPER_ADMIN 或 USER",
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }
        if (Objects.equals(currentUser.getId(), userId) && !UserRole.SUPER_ADMIN.equals(role)) {
            throw BusinessException.conflict(ErrorCode.FORBIDDEN, "不能取消自己的超级管理员权限");
        }

        User targetUser = requireUser(userId);
        targetUser.setRole(role);
        return toResponse(userRepository.save(targetUser));
    }

    public void changeOwnPassword(ChangePasswordRequest request) {
        User currentUser = requireCurrentUser();
        if (!passwordEncoder.matches(request.oldPassword(), currentUser.getPasswordHash())) {
            throw new BusinessException(
                    ErrorCode.INVALID_OLD_PASSWORD,
                    "当前密码错误",
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }
        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }

    private User requireCurrentUser() {
        LoginUser loginUser = AuthContext.requireUser();
        return requireUser(loginUser.id());
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }

    private void requireSuperAdmin(User user) {
        if (!isSuperAdmin(user)) {
            throw BusinessException.forbidden("仅超级管理员可以配置用户权限");
        }
    }

    private boolean isSuperAdmin(User user) {
        return UserRole.SUPER_ADMIN.equals(user.getRole());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
