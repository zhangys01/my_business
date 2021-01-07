package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.GtRr0;

import java.util.List;
public interface GtRr0ManagerService extends IService<GtRr0> {

    List<GtRr0> listByJobId(String taskId)throws Exception;

}
