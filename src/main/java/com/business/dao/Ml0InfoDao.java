package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.Ml0Info;

import java.util.List;

public interface Ml0InfoDao extends BaseMapper<Ml0Info> {
    List<Ml0Info> getL0Info(String jobTaskId, String signalId);
}
