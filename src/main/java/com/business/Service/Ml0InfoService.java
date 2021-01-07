package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.Ml0Info;

import java.util.List;

public interface Ml0InfoService extends IService<Ml0Info> {
    List<Ml0Info> getL0Info(String jobTaskId, String signalId);
}
