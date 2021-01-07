package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.UnzipConfigService;
import com.business.dao.UnzipConfigDao;
import com.business.entity.UnzipConfig;
import org.springframework.stereotype.Service;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 15:37
 */
@Service
public class UnzipConfigServiceImpl extends ServiceImpl<UnzipConfigDao,UnzipConfig> implements UnzipConfigService {
    @Override
    public UnzipConfig selectBySaliteName(String satelliteName) {
        return baseMapper.selectBySaliteName(satelliteName);
    }
}
