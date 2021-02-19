package com.business.action;

import com.business.Service.ProcessInfoService;
import com.business.Service.WorkFlowOrderService;
import com.business.db.OracleProcessInfoImpl;
import com.business.entity.*;
import com.business.info.QATaskWorkflowInfo;
import com.business.util.DateUtil;
import com.business.util.CheckStatusUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 1 *
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/3/28 14:35
 * 4
 */
@Component
@Configuration
@EnableScheduling
public class ScheduleRunningQaTask extends Thread {
    private static final Logger logger = Logger.getLogger(ScheduleRunningQaTask.class);
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Resource
    private OracleProcessInfoImpl oracleInfoImpl;
    @Resource
    private CheckStatusUtil checkStatusUtil;

    //添加定时任务
    @Scheduled(cron = "0/10 * * * * ?")   //第4秒钟触发，每5秒中触发一次
    public synchronized void configureTasks() throws Exception {
         try{
            List<WorkflowOrder> orderList = new ArrayList<>();
            orderList = orderService.selectQataskList("2");
            //logger.info(DateUtil.getTime()+"当前执行中的QA任务数量为"+orderList.size());
            if (orderList.size()!=0){
                for (int i=0;i<orderList.size();i++){
                    WorkflowOrder order = orderList.get(i);
                    order.setEndTime(DateUtil.getTime());
                    //先生成报告文件路径
                    QATaskWorkflowInfo wi = new QATaskWorkflowInfo();
                    List<ProcessInfo> dataInfoList = new ArrayList<>();
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
                        case "HJ2A":
                        case "HJ2B":
                            satelliteName = "HJ";
                            break;
                    }
                    if (order.getTaskMode().equals("Q61")) {
                        dataInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                        checkStatusUtil.ReportStatusQ61(order, dataInfoList, wi);
                    }
                    if (order.getTaskMode().equals("Q62")) {
                        checkStatusUtil.getReportStatus(order, "Q62");
                    }
                    if (order.getTaskMode().equals("Q63")) {
                        List<ProcessInfo> infoQ63 = processInfoService.getProcessList(order.getTaskSerialNumber(), satelliteName+"_Q63_CAT_TO_L2A");
                        checkStatusUtil.ReportStatusSuccess(order, "Q63", infoQ63);
                    }
                    if (order.getTaskMode().equals("Q61;Q62")) {
                        List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                        checkStatusUtil.ReportStatusSuccess(order, "Q61;Q62", reportInfoList);
                    }
                    if (order.getTaskMode().equals("Q61;Q63")) {
                        String status1 = "";
                        List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                        if (reportInfoList.size() != 0) {
                            status1 = checkStatusUtil.getProcessStatus(reportInfoList, order);
                        }
                        List<ProcessInfo> Q63InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), satelliteName+"_Q63_CAT_TO_L2A");
                        checkStatusUtil.ReportStatusQ61Q63(status1, order, Q63InfoList, wi);
                    }
                    if (order.getTaskMode().equals("Q62;Q63")) {
                        List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), satelliteName+"_Q63_CAT_TO_L2A");
                        checkStatusUtil.ReportStatusQ62Q63(order, Q63infoList, wi);
                    }
                    if (order.getTaskMode().equals("Q61;Q62;Q63")) {
                        String status1 = "";
                        List<ProcessInfo> Q61infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                        if (Q61infoList.size() != 0) {
                            status1 = checkStatusUtil.getProcessStatus(Q61infoList, order);
                        }
                        List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), satelliteName+"_Q63_CAT_TO_L2A");
                        checkStatusUtil.ReportStatusQ61Q62Q63(status1, order, Q63infoList, wi);
                    }
                    if (order.getTaskMode().equals("Q64")) {
                        ProcessInfo Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(), satelliteName+"_Q64_DIFF");
                        checkStatusUtil.ReportStatusQ64(Q64info, order, wi);
                    }
                    if (order.getTaskMode().equals("Q65")) {
                        dataInfoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                        checkStatusUtil.ReportStatusQ65(dataInfoList, order, wi);
                    }
                }
            }
         } catch (Exception e) {
             logger.error("更新QA任务失败："+e.getMessage());
         }
    }

}