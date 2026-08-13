package com.coreledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coreledger.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
