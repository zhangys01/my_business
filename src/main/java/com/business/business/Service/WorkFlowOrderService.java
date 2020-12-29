package com.business.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.business.entity.WorkflowOrder;

import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 15:39
 */
public interface WorkFlowOrderService extends IService<WorkflowOrder> {
    List<WorkflowOrder> selectList(String status);

    WorkflowOrder findById(String taskId)throws Exception;

    /*查询等待的订单，改为Running*/
    List<WorkflowOrder> selectRunList(String orderStatus)throws Exception;

    WorkflowOrder findDataskByJobId(String jobTaskId)throws Exception;


}
