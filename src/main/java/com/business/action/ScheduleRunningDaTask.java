package com.business.action;

import com.business.Service.ProcessInfoService;
import com.business.Service.WorkFlowOrderService;
import com.business.db.OracleProcessInfoImpl;
import com.business.entity.ProcessInfo;
import com.business.entity.WorkflowOrder;
import com.business.util.DateUtil;
import com.business.util.ReportUtil;
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
    private ReportUtil reportUtil;

    //添加定时任务
    @Scheduled(cron = "0/3 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public void configureTasks() throws Exception {
        try {
            System.out.println(DateUtil.getTime()+"开始查询归档任务");
            List<WorkflowOrder> productList = new ArrayList<>();
            productList = orderService.selectDataskList("2");
            if (productList.size()!=0){
                WorkflowOrder order = productList.get(0);
                order.setEndTime(DateUtil.getTime());
                String orderType = order.getOrderType().split("_")[0];
                switch (orderType){
                    case "DATask":
                        switch (order.getSatelliteName()) {
                            case "GF-1B":
                            case "GF-1C":
                            case "GF-1D":
                                String status1 = "";
                                List<ProcessInfo> gfL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_R0_TO_L0");
                                if (gfL0InfoList.size() != 0) {
                                    status1 = reportUtil.getProcessStatus(gfL0InfoList, order);
                                }
                                if (status1.equals("success")) {
                                    List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_L0_TO_CAT");
                                    if (infoList.size() != 0) {
                                        reportUtil.modifyTask(infoList, order);              //node为10.5.6.*
                                    }
                                }
                                break;
                            case "CASEARTH":
                                String status2 = "";
                                List<ProcessInfo> casearthL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_R0_TO_L0");
                                if (casearthL0InfoList.size() != 0) {
                                    status2 = reportUtil.getProcessStatus(casearthL0InfoList, order);
                                }
                                if (status2.equals("success")) {
                                    List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_L0_TO_CAT");
                                    if (infoList.size() != 0) {
                                        reportUtil.modifyTask(infoList, order);
                                    }
                                }
                                break;
                            case "ZY-3B":
                                String ZYstatus1 = "";
                                List<ProcessInfo> zyL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_R0_TO_L0");
                                if (zyL0InfoList.size() != 0) {
                                    ZYstatus1 = reportUtil.getProcessStatus(zyL0InfoList, order);
                                }
                                if (ZYstatus1.equals("success")) {
                                    List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_L0_TO_CAT");
                                    reportUtil.modifyTask(infoList, order);
                                }
                                break;
                            case "ZY1E":
                                String ZY1EStatus = "";
                                List<ProcessInfo> ZY1EL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_R0_TO_L0");
                                if (ZY1EL0InfoList.size() != 0) {
                                    ZY1EStatus = reportUtil.getProcessStatus(ZY1EL0InfoList, order);
                                }
                                if (ZY1EStatus.equals("success")) {
                                    List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_L0_TO_CAT");
                                    reportUtil.modifyTask(infoList, order);
                                }
                                break;
                        }
                        break;
                }
            }
        }catch (Exception e){
            logger.error("修改归档任务状态失败："+e.getMessage());
        }
    }
}
