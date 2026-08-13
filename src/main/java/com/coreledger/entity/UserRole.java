package com.coreledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("user_roles")
public class UserRole {
    private Long userId;
    private Long roleId;
}
