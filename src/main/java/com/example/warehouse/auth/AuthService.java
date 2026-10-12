package com.example.warehouse.auth;

import com.example.warehouse.common.BusinessException;
import com.example.warehouse.common.ErrorCode;
import com.example.warehouse.user.User;
import com.example.warehouse.user.UserRepository;
import com.example.warehouse.user.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录业务。
 * Controller 只负责收参数、返回结果；密码比对、签发 Token 都放在这里。
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(BusinessException::invalidCredentials);

        // 用户名不存在和密码错误返回同一句话，避免被用来枚举账号。
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw BusinessException.invalidCredentials();
        }

        String token = jwtService.createToken(user);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpireSeconds(),
                toUserResponse(user)
        );
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw BusinessException.conflict(ErrorCode.USERNAME_DUPLICATE, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRole(UserRole.USER);

        try {
            return toUserResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw BusinessException.conflict(ErrorCode.USERNAME_DUPLICATE, "用户名已存在");
        }
    }

    public UserResponse currentUser() {
        LoginUser loginUser = AuthContext.requireUser();
        User user = userRepository.findById(loginUser.id())
                .orElseThrow(() -> BusinessException.unauthorized("登录用户不存在"));
        return toUserResponse(user);
    }

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith(AuthInterceptor.BEARER_PREFIX)) {
            jwtService.blacklist(authorization.substring(AuthInterceptor.BEARER_PREFIX.length()).trim());
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
