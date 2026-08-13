package com.coreledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coreledger.common.exception.BusinessException;
import com.coreledger.common.util.AccountNumberGenerator;
import com.coreledger.common.util.TransactionNoGenerator;
import com.coreledger.config.CoreLedgerProperties;
import com.coreledger.dto.request.CreateAccountRequest;
import com.coreledger.dto.request.TransactionRequest;
import com.coreledger.dto.response.AccountResponse;
import com.coreledger.dto.response.TransactionResponse;
import com.coreledger.entity.Account;
import com.coreledger.entity.AccountTransaction;
import com.coreledger.enums.AccountStatus;
import com.coreledger.enums.ErrorCode;
import com.coreledger.enums.TransactionType;
import com.coreledger.mapper.AccountMapper;
import com.coreledger.mapper.AccountTransactionMapper;
import com.coreledger.security.CurrentUser;
import com.coreledger.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final String NULL_CACHE_VALUE = "__NULL__";

    private final AccountMapper accountMapper;
    private final AccountTransactionMapper transactionMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CoreLedgerProperties properties;

    @Override
    public AccountResponse createAccount(CreateAccountRequest request, CurrentUser user) {
        Account account = new Account();
        account.setAccountNo(AccountNumberGenerator.next());
        account.setUserId(user.id());
        account.setAccountName(request.accountName());
        account.setBalance(BigDecimal.ZERO.setScale(2));
        account.setStatus(AccountStatus.ACTIVE);
        account.setVersion(0);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        accountMapper.insert(account);
        evictAccountCache(account.getId());
        log.info("Account created accountId={} userId={}", account.getId(), user.id());
        return toAccountResponse(account);
    }

    @Override
    public IPage<AccountResponse> listAccounts(long page, long size, CurrentUser user) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<Account>().orderByDesc(Account::getCreatedAt);
        if (!isAdmin(user)) {
            wrapper.eq(Account::getUserId, user.id());
        }
        return accountMapper.selectPage(Page.of(page, size), wrapper).convert(this::toAccountResponse);
    }

    @Override
    public AccountResponse getAccount(Long id, CurrentUser user) {
        Account account = getAccountFromCache(id);
        if (account == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        assertAccountReadable(account, user);
        return toAccountResponse(account);
    }

    @Override
    @Transactional
    public TransactionResponse deposit(Long accountId, TransactionRequest request, CurrentUser user) {
        return changeBalance(accountId, request, user, TransactionType.DEPOSIT);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(Long accountId, TransactionRequest request, CurrentUser user) {
        return changeBalance(accountId, request, user, TransactionType.WITHDRAW);
    }

    @Override
    public IPage<TransactionResponse> listTransactions(Long accountId, long page, long size, TransactionType type,
                                                       LocalDateTime startTime, LocalDateTime endTime, CurrentUser user) {
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        assertAccountReadable(account, user);
        LambdaQueryWrapper<AccountTransaction> wrapper = new LambdaQueryWrapper<AccountTransaction>()
                .eq(AccountTransaction::getAccountId, accountId)
                .eq(type != null, AccountTransaction::getTransactionType, type)
                .ge(startTime != null, AccountTransaction::getCreatedAt, startTime)
                .le(endTime != null, AccountTransaction::getCreatedAt, endTime)
                .orderByDesc(AccountTransaction::getCreatedAt);
        return transactionMapper.selectPage(Page.of(page, size), wrapper).convert(this::toTransactionResponse);
    }

    private TransactionResponse changeBalance(Long accountId, TransactionRequest request, CurrentUser user, TransactionType type) {
        String idempotencyKey = "idempotency:transaction:" + accountId + ":" + request.requestId();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(idempotencyKey, "PROCESSING", Duration.ofSeconds(properties.idempotency().ttlSeconds()));
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST);
        }
        boolean completed = false;
        try {
            Account account = accountMapper.selectById(accountId);
            if (account == null) {
                throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
            }
            assertAccountReadable(account, user);
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
            }

            BigDecimal amount = request.amount().setScale(2);
            BigDecimal before = account.getBalance();
            BigDecimal after = type == TransactionType.DEPOSIT ? before.add(amount) : before.subtract(amount);
            if (type == TransactionType.WITHDRAW && after.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
            }

            account.setBalance(after);
            account.setUpdatedAt(LocalDateTime.now());
            int updated = accountMapper.updateById(account);
            if (updated != 1) {
                throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
            }

            AccountTransaction transaction = new AccountTransaction();
            transaction.setTransactionNo(TransactionNoGenerator.next());
            transaction.setAccountId(accountId);
            transaction.setTransactionType(type);
            transaction.setAmount(amount);
            transaction.setBalanceBefore(before);
            transaction.setBalanceAfter(after);
            transaction.setRequestId(request.requestId());
            transaction.setRemark(request.remark());
            transaction.setCreatedAt(LocalDateTime.now());
            transactionMapper.insert(transaction);
            redisTemplate.opsForValue().set(idempotencyKey, "COMPLETED", Duration.ofSeconds(properties.idempotency().ttlSeconds()));
            evictAccountCache(accountId);
            completed = true;
            log.info("Account transaction type={} accountId={} requestId={}", type, accountId, request.requestId());
            return toTransactionResponse(transaction);
        } finally {
            if (!completed) {
                redisTemplate.delete(idempotencyKey);
            }
        }
    }

    private Account getAccountFromCache(Long id) {
        String key = accountCacheKey(id);
        String cached = redisTemplate.opsForValue().get(key);
        if (NULL_CACHE_VALUE.equals(cached)) {
            return null;
        }
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Account.class);
            } catch (JsonProcessingException ex) {
                redisTemplate.delete(key);
            }
        }
        Account account = accountMapper.selectById(id);
        try {
            if (account == null) {
                redisTemplate.opsForValue().set(key, NULL_CACHE_VALUE, Duration.ofSeconds(properties.cache().nullTtlSeconds()));
            } else {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(account),
                        Duration.ofSeconds(properties.cache().accountTtlSeconds()));
            }
        } catch (JsonProcessingException ex) {
            log.warn("Failed to write account cache accountId={}", id);
        }
        return account;
    }

    private void evictAccountCache(Long accountId) {
        redisTemplate.delete(accountCacheKey(accountId));
    }

    private String accountCacheKey(Long id) {
        return "cache:account:" + id;
    }

    private void assertAccountReadable(Account account, CurrentUser user) {
        if (!isAdmin(user) && !Objects.equals(account.getUserId(), user.id())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    private boolean isAdmin(CurrentUser user) {
        return user.roles().contains("ROLE_ADMIN");
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(account.getId(), account.getAccountNo(), account.getUserId(), account.getAccountName(),
                account.getBalance(), account.getStatus().name(), account.getVersion(), account.getCreatedAt(), account.getUpdatedAt());
    }

    private TransactionResponse toTransactionResponse(AccountTransaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getTransactionNo(), transaction.getAccountId(),
                transaction.getTransactionType().name(), transaction.getAmount(), transaction.getBalanceBefore(),
                transaction.getBalanceAfter(), transaction.getRequestId(), transaction.getRemark(), transaction.getCreatedAt());
    }
}
