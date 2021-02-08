package com.business.action;

import com.business.Service.ProcessInfoService;
import com.business.Service.WorkFlowOrderService;
import com.business.entity.ProcessInfo;
import com.business.entity.WorkflowOrder;
import com.business.util.DateUtil;
import com.business.util.CheckStatusUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
@Configuration
@EnableScheduling   //打开quartz定时器总开关
public class ScheduleRunningDaTask {
    private static final Logger logger = Logger.getLogger(ScheduleRunningDaTask.class);
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Resource
    private CheckStatusUtil checkStatusUtil;

    //添加定时任务
    @Scheduled(cron = "0/5 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public synchronized void configureTasks() throws Exception {
        try {
            List<WorkflowOrder> daTaskList = new ArrayList<>();
            daTaskList = orderService.selectDataskList("2");
          //  logger.info(DateUtil.getTime()+"当前执行中的归档任务数量为"+daTaskList.size());
            if (daTaskList.size()!=0){
                for (int i=0;i<daTaskList.size();i++){
                    WorkflowOrder order = daTaskList.get(i);
                    order.setEndTime(DateUtil.getTime());
                    String satelliteName = "";
                    switch (order.getSatelliteName()) {
                        case "GF-1B":
                        case "GF-1C":
                        case "GF-1D":
                            satelliteName = "GF1";
                            break;
                        case "CASEARTH":
                        case "ZY1E":
                            satelliteName = order.getSatelliteName();
                            break;
                        case "ZY-3B":
                            satelliteName = "ZY3";
                            break;
                        case "CBERS04A":
                            satelliteName = "CB4A";
                            break;
                    }
                    String status2 = "";
                    List<ProcessInfo> L0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), satelliteName+"_R0_TO_L0");
                    if (L0InfoList.size()!= 0) {
                        status2 = checkStatusUtil.getProcessStatus(L0InfoList, order);
                    }
                    if (status2.equals("success")) {
                        List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), satelliteName+"_L0_TO_CAT");
                        if (infoList.size() != 0) {
                            checkStatusUtil.modifyTask(infoList, order);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("修改归档任务状态失败："+e.getMessage());
        }
    }
}
