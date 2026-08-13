package com.coreledger.service;

import com.coreledger.dto.request.LoginRequest;
import com.coreledger.dto.request.RegisterRequest;
import com.coreledger.dto.response.TokenResponse;
import com.coreledger.dto.response.UserResponse;
import com.coreledger.security.CurrentUser;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    UserResponse currentUser(CurrentUser currentUser);
}
