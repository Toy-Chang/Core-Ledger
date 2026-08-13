package com.coreledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coreledger.dto.request.CreateAccountRequest;
import com.coreledger.dto.request.TransactionRequest;
import com.coreledger.dto.response.AccountResponse;
import com.coreledger.dto.response.TransactionResponse;
import com.coreledger.enums.TransactionType;
import com.coreledger.security.CurrentUser;
import java.time.LocalDateTime;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request, CurrentUser user);

    IPage<AccountResponse> listAccounts(long page, long size, CurrentUser user);

    AccountResponse getAccount(Long id, CurrentUser user);

    TransactionResponse deposit(Long accountId, TransactionRequest request, CurrentUser user);

    TransactionResponse withdraw(Long accountId, TransactionRequest request, CurrentUser user);

    IPage<TransactionResponse> listTransactions(Long accountId, long page, long size, TransactionType type,
                                                LocalDateTime startTime, LocalDateTime endTime, CurrentUser user);
}
