package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.WorkFlowDataArchive;


public interface WorkFlowDataArchiveService extends IService<WorkFlowDataArchive> {
    /**
     *更新
     */

     WorkFlowDataArchive getDataArchive(String orderId)throws Exception;

}
