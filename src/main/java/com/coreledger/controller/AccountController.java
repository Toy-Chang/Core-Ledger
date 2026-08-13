package com.coreledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coreledger.annotation.Audit;
import com.coreledger.common.api.ApiResponse;
import com.coreledger.dto.request.CreateAccountRequest;
import com.coreledger.dto.request.TransactionRequest;
import com.coreledger.dto.response.AccountResponse;
import com.coreledger.dto.response.TransactionResponse;
import com.coreledger.enums.TransactionType;
import com.coreledger.security.CurrentUser;
import com.coreledger.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Create account for current user")
    @Audit(operation = "ACCOUNT_CREATE", resource = "ACCOUNT")
    @PostMapping
    public ApiResponse<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request,
                                               @AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(accountService.createAccount(request, user));
    }

    @Operation(summary = "List accounts")
    @GetMapping
    public ApiResponse<IPage<AccountResponse>> list(@RequestParam(defaultValue = "1") @Min(1) long page,
                                                    @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
                                                    @AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(accountService.listAccounts(page, size, user));
    }

    @Operation(summary = "Get account by id")
    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> get(@PathVariable Long id, @AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(accountService.getAccount(id, user));
    }

    @Operation(summary = "Deposit into account")
    @Audit(operation = "ACCOUNT_DEPOSIT", resource = "ACCOUNT")
    @PostMapping("/{id}/deposit")
    public ApiResponse<TransactionResponse> deposit(@PathVariable Long id,
                                                    @Valid @RequestBody TransactionRequest request,
                                                    @AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(accountService.deposit(id, request, user));
    }

    @Operation(summary = "Withdraw from account")
    @Audit(operation = "ACCOUNT_WITHDRAW", resource = "ACCOUNT")
    @PostMapping("/{id}/withdraw")
    public ApiResponse<TransactionResponse> withdraw(@PathVariable Long id,
                                                     @Valid @RequestBody TransactionRequest request,
                                                     @AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(accountService.withdraw(id, request, user));
    }

    @Operation(summary = "List account transactions")
    @GetMapping("/{id}/transactions")
    public ApiResponse<IPage<TransactionResponse>> transactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.success(accountService.listTransactions(id, page, size, transactionType, startTime, endTime, user));
    }
}
