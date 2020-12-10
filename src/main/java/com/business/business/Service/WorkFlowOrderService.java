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
    WorkflowOrder findOrderById(String taskSerialNumber);
    List<WorkflowOrder> selectList(String status);
}
