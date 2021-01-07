package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.QATaskWorkFlow;

/**
 * 2 * @Author: zys
 * 3 * @Date: 2020/12/25 15:03
 * 4
 */
public interface  QATaskWorkFlowDao extends BaseMapper<QATaskWorkFlow> {
    /**
     * 保存方法
     */
     void insertQaTask(QATaskWorkFlow qaTaskWorkFlow)throws Exception;
    /**
     * 更新
     */
     void updateQaTask(QATaskWorkFlow QaTask)throws Exception;

    QATaskWorkFlow findById(String orderId)throws Exception;

}
