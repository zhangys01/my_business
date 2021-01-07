package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.Ml0InfoService;
import com.business.dao.Ml0InfoDao;
import com.business.entity.Ml0Info;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class Ml0InfoServiceImpl extends ServiceImpl<Ml0InfoDao,Ml0Info>implements Ml0InfoService
{
    @Override
    public List<Ml0Info> getL0Info(String jobTaskId, String signalId) {
        return baseMapper.getL0Info(jobTaskId,signalId);
    }
}
