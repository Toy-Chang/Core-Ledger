package com.coreledger.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coreledger.dto.response.AccountResponse;
import com.coreledger.dto.response.AuditLogResponse;
import com.coreledger.dto.response.UserResponse;
import com.coreledger.entity.Account;
import com.coreledger.entity.AuditLog;
import com.coreledger.entity.User;
import com.coreledger.mapper.AccountMapper;
import com.coreledger.mapper.AuditLogMapper;
import com.coreledger.mapper.UserMapper;
import com.coreledger.service.AdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final AuditLogMapper auditLogMapper;

    @Override
    public IPage<UserResponse> listUsers(long page, long size) {
        return userMapper.selectPage(Page.of(page, size), null)
                .convert(user -> AuthServiceImpl.toUserResponse(user, List.of()));
    }

    @Override
    public IPage<AccountResponse> listAccounts(long page, long size) {
        return accountMapper.selectPage(Page.of(page, size), null).convert(this::toAccountResponse);
    }

    @Override
    public IPage<AuditLogResponse> listAuditLogs(long page, long size) {
        return auditLogMapper.selectPage(Page.of(page, size), null).convert(this::toAuditLogResponse);
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(account.getId(), account.getAccountNo(), account.getUserId(), account.getAccountName(),
                account.getBalance(), account.getStatus().name(), account.getVersion(), account.getCreatedAt(), account.getUpdatedAt());
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUserId(), log.getUsername(), log.getOperation(), log.getResource(),
                log.getRequestMethod(), log.getRequestUri(), log.getRequestIp(), log.getRequestId(), log.getResult(), log.getCreatedAt());
    }
}
