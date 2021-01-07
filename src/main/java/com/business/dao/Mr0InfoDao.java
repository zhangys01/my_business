package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.Mr0Info;

import java.util.List;

public interface Mr0InfoDao extends BaseMapper<Mr0Info> {
    List<Mr0Info> getMr0Info(String jobTaskId);
}
