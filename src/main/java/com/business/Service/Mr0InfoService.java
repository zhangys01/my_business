package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.Mr0Info;

import java.util.List;

public interface Mr0InfoService extends IService<Mr0Info> {
    List<Mr0Info> getMr0Info(String jobTaskId);
}
