package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.ProductConfigManagerService;
import com.business.dao.ProductConfigDao;
import com.business.entity.ProductUnzipConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductConfigManagerServiceImpl extends ServiceImpl<ProductConfigDao, ProductUnzipConfig> implements ProductConfigManagerService {
    @Override
    public List<ProductUnzipConfig> findByStatus(String isUnzip) {
        return baseMapper.findByStatus(isUnzip);
    }
}
