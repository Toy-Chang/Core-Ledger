package com.coreledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coreledger.common.exception.BusinessException;
import com.coreledger.dto.request.RegisterRequest;
import com.coreledger.entity.Role;
import com.coreledger.entity.User;
import com.coreledger.entity.UserRole;
import com.coreledger.enums.ErrorCode;
import com.coreledger.mapper.RoleMapper;
import com.coreledger.mapper.UserMapper;
import com.coreledger.mapper.UserRoleMapper;
import com.coreledger.security.JwtTokenProvider;
import com.coreledger.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userMapper, roleMapper, userRoleMapper, passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    void registerSuccess() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        Role role = new Role();
        role.setId(10L);
        role.setRoleCode("ROLE_USER");
        when(roleMapper.selectOne(any())).thenReturn(role);

        var response = service.register(new RegisterRequest("alice", "password123", "alice@example.com"));

        assertEquals("alice", response.username());
        assertEquals("ROLE_USER", response.roles().get(0));
        verify(userRoleMapper).insert(any(UserRole.class));
    }

    @Test
    void duplicateUsernameThrowsBusinessException() {
        when(userMapper.selectOne(any())).thenReturn(new User());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(new RegisterRequest("alice", "password123", "alice@example.com")));

        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, ex.getErrorCode());
    }
}
