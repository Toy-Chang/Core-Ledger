package com.coreledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("audit_logs")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String resource;
    private String requestMethod;
    private String requestUri;
    private String requestIp;
    private String requestId;
    private String result;
    private LocalDateTime createdAt;
}
