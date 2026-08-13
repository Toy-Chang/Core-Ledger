package com.coreledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coreledger.entity.AccountTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountTransactionMapper extends BaseMapper<AccountTransaction> {
}
