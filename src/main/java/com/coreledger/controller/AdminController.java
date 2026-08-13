package com.coreledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coreledger.annotation.Audit;
import com.coreledger.common.api.ApiResponse;
import com.coreledger.dto.response.AccountResponse;
import com.coreledger.dto.response.AuditLogResponse;
import com.coreledger.dto.response.UserResponse;
import com.coreledger.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "Admin list users")
    @Audit(operation = "ADMIN_LIST_USERS", resource = "USER")
    @GetMapping("/users")
    public ApiResponse<IPage<UserResponse>> users(@RequestParam(defaultValue = "1") @Min(1) long page,
                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size) {
        return ApiResponse.success(adminService.listUsers(page, size));
    }

    @Operation(summary = "Admin list accounts")
    @Audit(operation = "ADMIN_LIST_ACCOUNTS", resource = "ACCOUNT")
    @GetMapping("/accounts")
    public ApiResponse<IPage<AccountResponse>> accounts(@RequestParam(defaultValue = "1") @Min(1) long page,
                                                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size) {
        return ApiResponse.success(adminService.listAccounts(page, size));
    }

    @Operation(summary = "Admin list audit logs")
    @Audit(operation = "ADMIN_LIST_AUDIT_LOGS", resource = "AUDIT_LOG")
    @GetMapping("/audit-logs")
    public ApiResponse<IPage<AuditLogResponse>> auditLogs(@RequestParam(defaultValue = "1") @Min(1) long page,
                                                          @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size) {
        return ApiResponse.success(adminService.listAuditLogs(page, size));
    }
}
