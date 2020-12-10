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
    WorkflowOrder findOrderById(@Param("taskSerialNumber") String taskSerialNumber);
    List<WorkflowOrder> selectList(String status);
}
