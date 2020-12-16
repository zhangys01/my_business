package com.business.business.action;

import com.business.business.Service.WorkFlowOrderService;
import com.business.business.config.Config;
import com.business.business.entity.WorkflowOrder;
import com.business.business.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 9:27
 */
@Component
@Configuration
@EnableScheduling
public class ScheduleHoldTask {
    @Autowired
    private WorkFlowOrderService orderService;
    @Resource
    DataArchiveAction dataArchiveAction;
    @Resource
    QATaskAction qaTaskAction;
    //3.添加定时任务
    @Scheduled(cron = "0/5 * * * * ?")
    public void configureTasks()throws Exception{
        System.out.println(DateUtil.getTime()+"开始获取等待状态任务");
        List<WorkflowOrder>runList = orderService.selectList("2");
        if (runList.size()<5){
            List<WorkflowOrder>holdList = orderService.selectList("1");
            int size = 5-runList.size();
            if (holdList.size()!=0){
                //todo 小心越界
                if (size>holdList.size())size=holdList.size();
                for (int i=0;i<size;i++){
                    WorkflowOrder order = holdList.get(i);
                    String orderType = order.getOrderType().split("_")[0];
                    order.setOrderStatus("2");
                    switch (orderType){
                        case "QATask":
                            orderService.updateById(order);
                            qaTaskAction.process(order);
                            break;
                        case "DATask":
                            orderService.updateById(order);
                            processDATask(order);
                            break;
                       /* case "PRTask":
                            orderService.updateById(order);
                            PRtaskAction pRtaskAction = new PRtaskAction();
                            pRtaskAction.doTriggerQATask(order);
                            break;*/
                    }
                }
            }
        }
        }
    public synchronized boolean processDATask(WorkflowOrder t)throws Exception{
        Config.loadConfig();
        boolean result = false;
        try{
            //todo 三种情况
            switch (t.getFileResource()){
                case "onfilepath":
                    File receiver_Dir1 = new File(t.getJobTaskID());
                    dataArchiveAction.processDataArchive(receiver_Dir1,t);
                    result = true;
                    break;
                case "ondisk":
                    File receiver_Dir2 = new File(Config.local_dir +"/"+t.getJobTaskID());
                    dataArchiveAction.processDataArchive(receiver_Dir2,t);
                    result = true;
                    break;
                case "ondatabase":
                    if (t.getSatelliteName().equals("ZY-3B"))t.setSatelliteName("ZY302");
                    File receiver_Dir3 = new File(Config.local_dir+"/"+t.getSatelliteName()+"/"+t.getJobTaskID().substring(3,7)+"/"+t.getJobTaskID());
                    //todo 再把卫星给换回来
                    if (t.getSatelliteName().equals("ZY302"))t.setSatelliteName("ZY-3B");
                    dataArchiveAction.processDataArchive(receiver_Dir3,t);
                    result = true;
                    break;
            }
        } catch (Exception e) {  //失败下轮会重试
            t.setOrderStatus("4");
            t.setEndTime(DateUtil.getTime());
            orderService.updateById(t);
            // logger.error("failed to parse DESC file: " + desc, e);
        }
        return  result;
    }
    }
