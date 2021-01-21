package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.SysDictionariesService;
import com.business.dao.SysDictionariesDao;
import com.business.entity.SysDictionaries;
import org.springframework.stereotype.Service;

@Service
public class SysDictionariesServiceImpl extends ServiceImpl<SysDictionariesDao, SysDictionaries> implements SysDictionariesService {
    @Override
    public SysDictionaries findByName(String name) {
        return baseMapper.findByName(name);
    }
}
