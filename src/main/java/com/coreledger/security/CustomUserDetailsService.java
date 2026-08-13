package com.coreledger.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coreledger.entity.Role;
import com.coreledger.entity.User;
import com.coreledger.entity.UserRole;
import com.coreledger.enums.UserStatus;
import com.coreledger.mapper.RoleMapper;
import com.coreledger.mapper.UserMapper;
import com.coreledger.mapper.UserRoleMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("user not found");
        }
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()))
                .stream()
                .map(UserRole::getRoleId)
                .toList();
        List<String> roles = roleIds.isEmpty()
                ? List.of()
                : roleMapper.selectBatchIds(roleIds).stream().map(Role::getRoleCode).toList();
        return new CurrentUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                roles,
                roles.stream().map(SimpleGrantedAuthority::new).toList()
        );
    }
}
