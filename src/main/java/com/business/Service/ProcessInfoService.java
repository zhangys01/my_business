package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.ProcessInfo;

import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 16:13
 */
public interface ProcessInfoService extends IService<ProcessInfo> {
    //查询任务是否完成
    public List<ProcessInfo> selectProcess(String platform)throws Exception;

    public ProcessInfo getProcessByName(String platfrom,String processType)throws Exception;

    public List<ProcessInfo>getProcessList(String platfrom,String processType)throws Exception;

    public ProcessInfo getProcessByOrderId(String orderID);
    ProcessInfo getProcessByPlatfrom(String taskId);
}
