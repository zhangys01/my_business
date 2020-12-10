package com.business.business.Controller;

import com.business.business.Service.WorkFlowOrderService;
import com.business.business.entity.WorkflowOrder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 15:55
 */
public class WorkFlowOrderController {
    @Autowired
    public static WorkFlowOrderService workFlowOrderService;
    public static void main(){
        WorkflowOrder order = workFlowOrderService.findOrderById("DA20200310132152");
        System.out.println(order.getTaskSerialNumber());
    };
}
