package com.business.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.dao.WorkFlowOrderDao;
import com.business.business.entity.WorkflowOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 15:42
 */
@Service
public class WorkFlowOrderServiceImpl extends ServiceImpl<WorkFlowOrderDao,WorkflowOrder> implements WorkFlowOrderService {
    @Override
    public WorkflowOrder findOrderById(String taskSerialNumber) {
        return baseMapper.findOrderById(taskSerialNumber);
    }
    @Override
    public  List<WorkflowOrder> selectList(String status){
        return baseMapper.selectList(status);
    }
}
