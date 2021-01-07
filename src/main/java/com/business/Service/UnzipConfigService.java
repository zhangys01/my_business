package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.UnzipConfig;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 15:37
 */
public interface UnzipConfigService extends IService<UnzipConfig> {
    UnzipConfig selectBySaliteName(String satelliteName);
}
