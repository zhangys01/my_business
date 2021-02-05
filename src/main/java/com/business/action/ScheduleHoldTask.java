package com.business.action;

import com.business.Service.WorkFlowOrderService;
import com.business.config.Config;
import com.business.entity.WorkflowOrder;
import com.business.util.DateUtil;
import org.apache.log4j.Logger;
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
@EnableScheduling   //打开quartz定时器总开关
public class ScheduleHoldTask {
    private static final Logger logger= Logger.getLogger(ScheduleHoldTask.class);
    @Autowired
    private WorkFlowOrderService orderService;
    @Resource
    DataArchiveAction dataArchiveAction;
    @Resource
    QATaskAction qaTaskAction;
    @Resource
    PRtaskAction prTaskAction;
    //3.添加定时任务

    @Scheduled(cron = "0/4 * * * * ? ")   //第0秒钟触发，每5秒中触发一次
    public synchronized void configureTasks()throws Exception{
        System.out.println(DateUtil.getTime()+"开始获取等待状态任务");
        //todo 紧急任务不受限制
        List<WorkflowOrder>emergencyList = orderService.selectList("8");
        if (emergencyList!=null&&emergencyList.size()!=0){
            readyTask(emergencyList,"8",emergencyList.size());
        }
        //todo 普通任务
        List<WorkflowOrder>runList = orderService.selectList("2");
        int size = Config.running_number;
        if (runList.size()<size){
            size = size-runList.size();
            List<WorkflowOrder>holdList = orderService.selectList("1");
            if (holdList!=null&&holdList.size()!=0){
                readyTask(holdList,"1",size);
            }
        }
    }
    public void readyTask(List<WorkflowOrder> orderList,String orderStatus,int size)throws Exception{
        if (orderList.size()!=0){
            //todo 小心越界
            if (orderStatus.equals("8")){
                size=orderList.size()+1;
            }else if (orderStatus.equals("1")){
                if (size>orderList.size())size=orderList.size();
            }
            for (int i=0;i<size;i++){
                WorkflowOrder order = orderList.get(i);
                logger.info("发现任务"+order.getTaskMode()+"的任务号是"+order.getTaskSerialNumber()+"任务类型是："+orderStatus);
                String orderType = order.getOrderType().split("_")[0];
                order.setOrderStatus("2");
                switch (orderType){
                    case "QATask":
                        orderService.updateById(order);
                        logger.info("开始处理QA任务"+order.getTaskSerialNumber());
                        qaTaskAction.process(order);
                        break;
                    case "DATask":
                        orderService.updateById(order);
                        logger.info("开始处理归档任务"+order.getTaskSerialNumber());
                        processDATask(order);
                        break;
                    case "PRTask":
                        orderService.updateById(order);
                        logger.info("开始处理产品任务"+order.getTaskSerialNumber());
                        prTaskAction.doTriggerQATask(order);
                        break;
                }
            }
        }
    }
        public synchronized boolean processDATask(WorkflowOrder t)throws Exception{
            boolean result = false;
            try{
                //todo 三种情况
                logger.info("当前归档任务路径为:"+t.getFileResource());
                switch (t.getFileResource()){
                    case "onfilepath":
                        File receiver_Dir1 = new File(t.getJobTaskID());
                        dataArchiveAction.processDataArchive(receiver_Dir1,t);
                        result = true;
                        break;
                    case "ondisk":

                        String local_dir = Config.local_dir+"/"+DateUtil.getSdfMonths();
                        logger.info("local_dir is "+local_dir);
                        File receiver_Dir2 = new File( local_dir+"/"+t.getJobTaskID());
                        dataArchiveAction.processDataArchive(receiver_Dir2,t);
                        result = true;
                        break;
                    case "ondatabase":
                        if (t.getSatelliteName().equals("ZY-3B"))t.setSatelliteName("ZY302");
                        File receiver_Dir3 = new File(Config.data_dir+"/"+t.getSatelliteName()+"/"+t.getJobTaskID().substring(3,7)+"/"+t.getJobTaskID());
                        //todo 再把卫星给换回来
                        logger.info(receiver_Dir3.toString()+"鲁宁是这个");
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
