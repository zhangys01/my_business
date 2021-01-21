package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.SysDictionaries;

public interface SysDictionariesDao extends BaseMapper<SysDictionaries> {
    SysDictionaries findByName(String name);
}
