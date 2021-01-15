package com.business.action;

import com.business.Service.ProcessInfoService;
import com.business.Service.WorkFlowOrderService;
import com.business.db.OracleProcessInfoImpl;
import com.business.entity.*;
import com.business.info.QATaskWorkflowInfo;
import com.business.util.DateUtil;
import com.business.util.ReportUtil;
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
    private ReportUtil reportUtil;

    //添加定时任务
    @Scheduled(cron = "0/4 * * * * ?")   //第4秒钟触发，每5秒中触发一次
    public void configureTasks() throws Exception {
         try{
             System.out.println(DateUtil.getTime()+"开始查询QA任务");
            List<WorkflowOrder> orderList = new ArrayList<>();
            orderList = orderService.selectQataskList("2");
            if (orderList.size()!=0){
                WorkflowOrder order = orderList.get(0);
                order.setEndTime(DateUtil.getTime());
                String orderType = order.getOrderType().split("_")[0];
                switch (orderType) {
                    //todo 分Q61,62,63,64,65情况
                    case "QATask":
                        //先生成报告文件路径
                        QATaskWorkflowInfo wi = new QATaskWorkflowInfo();
                        List<ProcessInfo> dataInfoList = new ArrayList<>();
                        if (order.getTaskMode().equals("Q61")) {
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                case "CASEARTH":
                                case "ZY-3B":
                                case "ZY1E":
                                case "CSES":
                                    dataInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                                    reportUtil.ReportStatusQ61(order, dataInfoList, wi);
                                    break;
                            }
                        }
                        if (order.getTaskMode().equals("Q62")) {
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                case "CASEARTH":
                                case "ZY-3B":
                                case "ZY1E":
                                    reportUtil.getReportStatus(order, "Q62");
                                    break;
                            }
                        }
                        if (order.getTaskMode().equals("Q63")) {
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                    List<ProcessInfo> infoQ63GF = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusSuccess(order, "Q63", infoQ63GF);
                                    break;
                                case "CASEARTH":
                                    List<ProcessInfo> infoQ63casearth = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusSuccess(order, "Q63", infoQ63casearth);

                                    break;
                                case "ZY-3B":
                                    List<ProcessInfo> infoQ63ZY = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusSuccess(order, "Q63", infoQ63ZY);
                                    break;
                                case "ZY1E":
                                    List<ProcessInfo> infoQ63ZY1E = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusSuccess(order, "Q63", infoQ63ZY1E);
                                    break;
                            }
                        }
                        if (order.getTaskMode().equals("Q61;Q62")) {
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                case "CASEARTH":
                                case "ZY-3B":
                                case "ZY1E":
                                    List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                                    reportUtil.ReportStatusSuccess(order, "Q61;Q62", reportInfoList);
                                    break;

                            }
                        }
                        if (order.getTaskMode().equals("Q61;Q63")) {
                            String status1 = "";
                            List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                            if (reportInfoList.size() != 0) {
                                status1 = reportUtil.getProcessStatus(reportInfoList, order);
                            }
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                    List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q63(status1, order, Q63infoList, wi);
                                    break;
                                case "CASEARTH":
                                    List<ProcessInfo> Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q63(status1, order, Q63casearthinfoList, wi);
                                    break;
                                case "ZY-3B":
                                    List<ProcessInfo> Q63zyInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q63(status1, order, Q63zyInfoList, wi);
                                    break;
                                case "ZY1E":
                                    List<ProcessInfo> Q63zy1eInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q63(status1, order, Q63zy1eInfoList, wi);
                                    break;
                            }
                        }
                        if (order.getTaskMode().equals("Q62;Q63")) {
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                    List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ62Q63(order, Q63infoList, wi);
                                    break;
                                case "CASEARTH":
                                    List<ProcessInfo> Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ62Q63(order, Q63casearthinfoList, wi);
                                    break;
                                case "ZY-3B":
                                    List<ProcessInfo> Q63zyinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ62Q63(order, Q63zyinfoList, wi);
                                    break;
                                case "ZY1E":
                                    List<ProcessInfo> Q63zy1einfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ62Q63(order, Q63zy1einfoList, wi);
                                    break;
                            }
                        }
                        if (order.getTaskMode().equals("Q61;Q62;Q63")) {
                            String status1 = "";
                            List<ProcessInfo> Q61infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                            if (Q61infoList.size() != 0) {
                                status1 = reportUtil.getProcessStatus(Q61infoList, order);
                            }
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                    List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q62Q63(status1, order, Q63infoList, wi);
                                    break;
                                case "CASEARTH":
                                    List<ProcessInfo> Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q62Q63(status1, order, Q63casearthinfoList, wi);
                                    break;
                                case "ZY-3B":
                                    List<ProcessInfo> Q63ZYinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q62Q63(status1, order, Q63ZYinfoList, wi);
                                    break;
                                case "ZY1E":
                                    List<ProcessInfo> Q63ZY1EinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                    reportUtil.ReportStatusQ61Q62Q63(status1, order, Q63ZY1EinfoList, wi);
                                    break;
                            }
                        }
                        if (order.getTaskMode().equals("Q64")) {
                            ProcessInfo Q64info = null;
                            //                                String status1 = "";
                            if (order.getSatelliteName().equals("ZY-3B")) {
                                Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(), "ZY3_Q64_DIFF");
                            } else if (order.getSatelliteName().equals("CASEARTH")) {
                                Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(), "CASEARTH_Q64_DIFF");
                            } else {
                                Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(), "GF_Q64_DIFF");
                            }
                            reportUtil.ReportStatusQ64(Q64info, order, wi);
                        }
                        if (order.getTaskMode().equals("Q65")) {
                            dataInfoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                            reportUtil.ReportStatusQ65(dataInfoList, order, wi);
                        }
                        break;
                }
            }
         } catch (Exception e) {
             logger.error("更新QA任务失败："+e.getMessage());
         }
    }

}