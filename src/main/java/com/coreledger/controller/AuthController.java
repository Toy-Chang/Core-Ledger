package com.coreledger.controller;

import com.coreledger.annotation.Audit;
import com.coreledger.common.api.ApiResponse;
import com.coreledger.dto.request.LoginRequest;
import com.coreledger.dto.request.RegisterRequest;
import com.coreledger.dto.response.TokenResponse;
import com.coreledger.dto.response.UserResponse;
import com.coreledger.security.CurrentUser;
import com.coreledger.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @Audit(operation = "USER_REGISTER", resource = "USER")
    @PostMapping("/auth/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @Operation(summary = "Login and issue JWT")
    @Audit(operation = "USER_LOGIN", resource = "USER")
    @PostMapping("/auth/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "Get current user")
    @GetMapping("/users/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(authService.currentUser(user));
    }
}
