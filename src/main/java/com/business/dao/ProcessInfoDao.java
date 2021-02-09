package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.ProcessInfo;

import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 16:15
 */
public interface ProcessInfoDao  extends BaseMapper<ProcessInfo> {
    //查询任务是否完成
    public List<ProcessInfo> selectProcess(String platform)throws Exception;

    public ProcessInfo getProcessByName(String platfrom,String processType)throws Exception;

    public List<ProcessInfo>getProcessList(String platfrom,String processType)throws Exception;
    public ProcessInfo getProcessByOrderId(String orderID);
    ProcessInfo getProcessByPlatfrom(String taskId);
}
