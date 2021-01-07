package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.WorkFlowDataArchiveService;
import com.business.dao.WorkFlowDataArchiveDao;
import com.business.entity.WorkFlowDataArchive;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class WorkFlowDataArchiveServiceImpl extends ServiceImpl<WorkFlowDataArchiveDao, WorkFlowDataArchive> implements WorkFlowDataArchiveService {
    private static final Logger logger = Logger.getLogger(WorkFlowDataArchiveServiceImpl.class);

    public WorkFlowDataArchive getDataArchive(String orderId)throws Exception{
        WorkFlowDataArchive archive = new WorkFlowDataArchive();
        try {
            archive = baseMapper.selectById(orderId);
        }catch (Exception e){
            logger.error(e);
        }
        return archive;
    }
}
