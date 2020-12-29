package com.business.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.business.entity.WorkflowOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 15:43
 */
@Mapper
public interface WorkFlowOrderDao extends BaseMapper<WorkflowOrder> {
    List<WorkflowOrder> selectList(String status);
    /**
     * 根据ID获取任务
     */
    WorkflowOrder findById(@Param("jobTaskID") String taskId)throws Exception;

    /*查询等待的订单，改为Running*/
    List<WorkflowOrder> selectRunList(String orderStatus)throws Exception;

    WorkflowOrder findDataskByJobId(@Param("jobTaskID") String jobTaskId)throws Exception;

}
