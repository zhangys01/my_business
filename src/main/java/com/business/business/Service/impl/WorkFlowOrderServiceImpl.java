package com.business.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.dao.WorkFlowOrderDao;
import com.business.business.entity.WorkflowOrder;
import com.business.business.util.LogUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 15:42
 */
@Service
public class WorkFlowOrderServiceImpl extends ServiceImpl<WorkFlowOrderDao,WorkflowOrder> implements WorkFlowOrderService {
    private static final Logger logger = Logger.getLogger(WorkFlowOrderServiceImpl.class);

    @Override
    public List<WorkflowOrder> selectList(String status) {
        List<WorkflowOrder> orderList = new ArrayList<>();
        try {
            orderList = baseMapper.selectList(status);
        } catch (Exception e) {
            logger.info("查询执行列表出现了错误" + e + "当时的状态是" + status);
        }
        return orderList;
    }

    @Override
    public synchronized List<WorkflowOrder> selectRunList(String orderStatus) throws Exception {
        List<WorkflowOrder> orderList = new ArrayList<>();
        try {
            orderList = baseMapper.selectRunList(orderStatus);
        } catch (Exception e) {
            logger.info("查询执行列表出现了错误" + e + "当时的状态是" + orderStatus);
        }
        return orderList;
    }

    /*根据taskSerialNumber查找任务单*/
    @Override
    public WorkflowOrder findById(String taskId) throws Exception {
        return baseMapper.findById(taskId);
    }

    //todo 根据jobtaskID获取最新的DA任务
    @Override
    public WorkflowOrder findDataskByJobId(String jobTaskId) throws Exception {
        WorkflowOrder order = new WorkflowOrder();
        try {
            order = baseMapper.findDataskByJobId(jobTaskId);
        } catch (Exception e) {
            logger.info(e);
        } finally {
            logger.info("查找DA任务关闭");
        }
        return order;
    }
}