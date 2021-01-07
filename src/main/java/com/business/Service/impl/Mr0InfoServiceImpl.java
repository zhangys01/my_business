package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.Mr0InfoService;
import com.business.dao.Mr0InfoDao;
import com.business.entity.Mr0Info;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class Mr0InfoServiceImpl extends ServiceImpl<Mr0InfoDao, Mr0Info> implements Mr0InfoService {
    @Override
    public List<Mr0Info> getMr0Info(String jobTaskId) {
        return baseMapper.getMr0Info(jobTaskId);
    }
}
