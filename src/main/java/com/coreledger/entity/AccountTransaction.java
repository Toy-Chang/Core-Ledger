package com.coreledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.coreledger.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("account_transactions")
public class AccountTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String transactionNo;
    private Long accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String requestId;
    private String remark;
    private LocalDateTime createdAt;
}
