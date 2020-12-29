package com.business.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.business.entity.QATaskWorkFlow;

public interface QATaskWorkFlowService extends IService<QATaskWorkFlow> {
    /**
     * 保存方法
     */
    void saveQaTask(QATaskWorkFlow QaTask)throws Exception;
    void insertQaTask(QATaskWorkFlow qaTaskWorkFlow)throws Exception;
    /**
     * 更新
     */
    void updateQaTask(QATaskWorkFlow QaTask)throws Exception;


}
