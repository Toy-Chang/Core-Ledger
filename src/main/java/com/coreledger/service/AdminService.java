package com.coreledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coreledger.dto.response.AccountResponse;
import com.coreledger.dto.response.AuditLogResponse;
import com.coreledger.dto.response.UserResponse;

public interface AdminService {
    IPage<UserResponse> listUsers(long page, long size);

    IPage<AccountResponse> listAccounts(long page, long size);

    IPage<AuditLogResponse> listAuditLogs(long page, long size);
}
