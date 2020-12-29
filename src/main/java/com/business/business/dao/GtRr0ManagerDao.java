package com.business.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.business.entity.GtRr0;

import java.util.List;

public interface GtRr0ManagerDao extends BaseMapper<GtRr0> {
    List<GtRr0> listByJobId(String taskId)throws Exception;
}
