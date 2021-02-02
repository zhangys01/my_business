package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.WorkflowOrder;

import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 15:39
 */
public interface WorkFlowOrderService extends IService<WorkflowOrder> {
    List<WorkflowOrder> selectList(String status);

    WorkflowOrder findById(String taskId);

    /*查询执行中的产品订单*/
    List<WorkflowOrder> selectProductList(String orderStatus);
    /*查询执行中的归档订单*/
    List<WorkflowOrder> selectDataskList(String orderStatus);
    /*查询执行中的QA订单*/
    List<WorkflowOrder> selectQataskList(String orderStatus);

    WorkflowOrder findDataskByJobId(String jobTaskId);


}
