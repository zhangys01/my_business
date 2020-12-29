package com.business.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.business.dao.GtRr0ManagerDao;
import com.business.business.entity.GtRr0;

import java.util.List;
public interface GtRr0ManagerService extends IService<GtRr0> {

    List<GtRr0> listByJobId(String taskId)throws Exception;

}
