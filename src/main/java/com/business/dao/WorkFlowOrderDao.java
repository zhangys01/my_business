package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.WorkflowOrder;
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
    WorkflowOrder findById(@Param("jobTaskID") String taskId);

    /*查询执行中的产品订单*/
    List<WorkflowOrder> selectProductList(String orderStatus);
    /*查询执行中的归档订单*/
    List<WorkflowOrder> selectDataskList(String orderStatus);
    /*查询执行中的QA订单*/
    List<WorkflowOrder> selectQataskList(String orderStatus);

    WorkflowOrder findDataskByJobId(@Param("jobTaskID") String jobTaskId);

}
