package com.business.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.business.entity.UnzipConfig;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 15:38
 */
public interface UnzipConfigDao extends BaseMapper<UnzipConfig> {
    UnzipConfig selectBySaliteName(String satelliteName);
}
