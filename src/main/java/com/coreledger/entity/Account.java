package com.coreledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.coreledger.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("accounts")
public class Account {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountNo;
    private Long userId;
    private String accountName;
    private BigDecimal balance;
    private AccountStatus status;
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
