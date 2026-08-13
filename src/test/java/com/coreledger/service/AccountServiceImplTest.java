package com.coreledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coreledger.common.exception.BusinessException;
import com.coreledger.config.CoreLedgerProperties;
import com.coreledger.dto.request.CreateAccountRequest;
import com.coreledger.dto.request.TransactionRequest;
import com.coreledger.entity.Account;
import com.coreledger.entity.AccountTransaction;
import com.coreledger.enums.AccountStatus;
import com.coreledger.enums.ErrorCode;
import com.coreledger.enums.TransactionType;
import com.coreledger.mapper.AccountMapper;
import com.coreledger.mapper.AccountTransactionMapper;
import com.coreledger.security.CurrentUser;
import com.coreledger.service.impl.AccountServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private AccountTransactionMapper transactionMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AccountServiceImpl service;
    private CurrentUser user;

    @BeforeEach
    void setUp() {
        var properties = new CoreLedgerProperties(
                new CoreLedgerProperties.Jwt("change_me_to_a_long_random_secret_at_least_32_chars", 7200),
                new CoreLedgerProperties.Idempotency(3600),
                new CoreLedgerProperties.Cache(300, 60));
        service = new AccountServiceImpl(accountMapper, transactionMapper, redisTemplate, new ObjectMapper(), properties);
        user = new CurrentUser(1L, "alice", "", List.of("ROLE_USER"), List.of());
    }

    @Test
    void createAccountSuccess() {
        when(accountMapper.insert(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(100L);
            return 1;
        });

        var response = service.createAccount(new CreateAccountRequest("Main Account"), user);

        assertEquals(100L, response.id());
        assertEquals(BigDecimal.ZERO.setScale(2), response.balance());
        verify(redisTemplate).delete("cache:account:100");
    }

    @Test
    void depositSuccess() {
        mockIdempotencyAcquired();
        when(accountMapper.selectById(100L)).thenReturn(account(BigDecimal.valueOf(100)));
        when(accountMapper.updateById(any(Account.class))).thenReturn(1);
        when(transactionMapper.insert(any(AccountTransaction.class))).thenReturn(1);

        var response = service.deposit(100L, tx(BigDecimal.valueOf(50), "req-1"), user);

        assertEquals(TransactionType.DEPOSIT.name(), response.transactionType());
        assertEquals(BigDecimal.valueOf(150).setScale(2), response.balanceAfter());
        verify(transactionMapper).insert(any(AccountTransaction.class));
    }

    @Test
    void withdrawSuccess() {
        mockIdempotencyAcquired();
        when(accountMapper.selectById(100L)).thenReturn(account(BigDecimal.valueOf(100)));
        when(accountMapper.updateById(any(Account.class))).thenReturn(1);
        when(transactionMapper.insert(any(AccountTransaction.class))).thenReturn(1);

        var response = service.withdraw(100L, tx(BigDecimal.valueOf(40), "req-2"), user);

        assertEquals(TransactionType.WITHDRAW.name(), response.transactionType());
        assertEquals(BigDecimal.valueOf(60).setScale(2), response.balanceAfter());
    }

    @Test
    void insufficientBalanceDeletesIdempotencyKeyAndThrows() {
        mockIdempotencyAcquired();
        when(accountMapper.selectById(100L)).thenReturn(account(BigDecimal.valueOf(30)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.withdraw(100L, tx(BigDecimal.valueOf(40), "req-3"), user));

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ex.getErrorCode());
        verify(redisTemplate).delete("idempotency:transaction:100:req-3");
    }

    @Test
    void duplicateRequestThrows() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("idempotency:transaction:100:req-4"), eq("PROCESSING"), any(Duration.class)))
                .thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.deposit(100L, tx(BigDecimal.TEN, "req-4"), user));

        assertEquals(ErrorCode.DUPLICATE_REQUEST, ex.getErrorCode());
    }

    @Test
    void optimisticLockConflictThrows() {
        mockIdempotencyAcquired();
        when(accountMapper.selectById(100L)).thenReturn(account(BigDecimal.valueOf(100)));
        when(accountMapper.updateById(any(Account.class))).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.deposit(100L, tx(BigDecimal.TEN, "req-5"), user));

        assertEquals(ErrorCode.OPTIMISTIC_LOCK_CONFLICT, ex.getErrorCode());
        verify(redisTemplate).delete("idempotency:transaction:100:req-5");
    }

    private void mockIdempotencyAcquired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("PROCESSING"), any(Duration.class))).thenReturn(true);
    }

    private TransactionRequest tx(BigDecimal amount, String requestId) {
        return new TransactionRequest(amount, requestId, "test");
    }

    private Account account(BigDecimal balance) {
        Account account = new Account();
        account.setId(100L);
        account.setAccountNo("CL202608130001");
        account.setUserId(1L);
        account.setAccountName("Main");
        account.setBalance(balance.setScale(2));
        account.setStatus(AccountStatus.ACTIVE);
        account.setVersion(0);
        return account;
    }
}
