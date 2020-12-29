package com.business.business.Service.impl;

import com.baomidou.mybatisplus.core.injector.methods.DeleteById;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.business.Service.GtRr0ManagerService;
import com.business.business.config.Config;
import com.business.business.dao.GtRr0ManagerDao;
import com.business.business.entity.GtRr0;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/13 12:30
 */
@Service
public class GtRr0ManagerServiceImpl extends ServiceImpl<GtRr0ManagerDao, GtRr0> implements GtRr0ManagerService {
    @Override
    public List<GtRr0> listByJobId(String taskId) throws Exception {
        return baseMapper.listByJobId(taskId);
    }
}