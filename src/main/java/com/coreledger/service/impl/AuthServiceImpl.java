package com.coreledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coreledger.common.exception.BusinessException;
import com.coreledger.dto.request.LoginRequest;
import com.coreledger.dto.request.RegisterRequest;
import com.coreledger.dto.response.TokenResponse;
import com.coreledger.dto.response.UserResponse;
import com.coreledger.entity.Role;
import com.coreledger.entity.User;
import com.coreledger.entity.UserRole;
import com.coreledger.enums.ErrorCode;
import com.coreledger.enums.UserStatus;
import com.coreledger.mapper.RoleMapper;
import com.coreledger.mapper.UserMapper;
import com.coreledger.mapper.UserRoleMapper;
import com.coreledger.security.CurrentUser;
import com.coreledger.security.JwtTokenProvider;
import com.coreledger.service.AuthService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (existing != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Role userRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ROLE_USER"));
        UserRole relation = new UserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(userRole.getId());
        userRoleMapper.insert(relation);
        log.info("User registered username={}", user.getUsername());
        return toUserResponse(user, List.of("ROLE_USER"));
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
            log.info("User login username={}", currentUser.username());
            return new TokenResponse(jwtTokenProvider.generateToken(currentUser), "Bearer", jwtTokenProvider.expirationSeconds());
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public UserResponse currentUser(CurrentUser currentUser) {
        User user = userMapper.selectById(currentUser.id());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toUserResponse(user, currentUser.roles());
    }

    static UserResponse toUserResponse(User user, List<String> roles) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getStatus().name(), roles, user.getCreatedAt());
    }
}
