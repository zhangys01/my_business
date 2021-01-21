package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.SysDictionaries;

public interface SysDictionariesService extends IService<SysDictionaries> {
    SysDictionaries findByName(String name);
}
