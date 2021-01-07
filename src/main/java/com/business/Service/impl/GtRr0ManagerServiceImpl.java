package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.GtRr0ManagerService;
import com.business.dao.GtRr0ManagerDao;
import com.business.entity.GtRr0;
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