package com.business.business.action;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.business.business.Service.McatManagerService;
import com.business.business.Service.ProcessInfoService;
import com.business.business.Service.WorkFlowDataArchiveService;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.Service.impl.WorkFlowOrderServiceImpl;
import com.business.business.adapter.String2ListXmlAdapter;
import com.business.business.config.Config;
import com.business.business.constants.Constants;
import com.business.business.db.OracleProcessInfoImpl;
import com.business.business.db.ProcessInfoImpl;
import com.business.business.entity.*;
import com.business.business.enums.Channel;
import com.business.business.enums.ProcessType;
import com.business.business.enums.ResponseType;
import com.business.business.info.ArchiveWorkflowInfo;
import com.business.business.info.QATaskWorkflowInfo;
import com.business.business.message.DataArchiveRep;
import com.business.business.message.QATaskRep;
import com.business.business.message.Response;
import com.business.business.util.DateUtil;
import com.business.business.util.MyHelper;
import com.business.business.util.ProcessUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 1 *
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/3/28 14:35
 * 4
 */
@Component
@Configuration
@EnableScheduling
public class SchedulRunningTask extends Thread {
    private static final Logger logger = Logger.getLogger(SchedulRunningTask.class);
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private McatManagerService mcatManagerService;
    @Autowired
    private WorkFlowDataArchiveService workFlowDataArchiveService;
    @Autowired
    private ProcessInfoService processInfoService;

    private ProcessInfoImpl infoImpl;
    private OracleProcessInfoImpl oracleInfoImpl;
    private String QAReportFile;    //相对路径
    private static final int  BUFFER_SIZE = 2 * 1024;

    private Unmarshaller unmarshaller;
    private Marshaller marshaller;

    //添加定时任务
    @Scheduled(cron = "0/5 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public void  configureTasks() throws Exception {
        System.out.println(DateUtil.getTime()+"running任务");
        ExecutorService threadPool = Executors.newCachedThreadPool();
        int i=99;
        do {
            Thread.sleep(5000);
            List<WorkflowOrder> orderList = orderService.selectRunList("2");
            if (orderList.size()!=0){
                for (int j=0;j<orderList.size();j++)
                threadPool.execute(SchedulRunningTask(orderList.get(j)));
            }
        }while (i!=1);
    }
    public Thread SchedulRunningTask(WorkflowOrder order)throws Exception{
        return new Thread(new Runnable() {
            @Override
            public void run() {
                //infoImpl = new ProcessInfoImpl();
                //mcatManagerService = new McatManagerServiceImpl();
                //orderService = new WorkFlowOrderServiceImpl();
                //processInfoService = new ProcessInfoServiceImpl();
                //workFlowDataArchiveService = new WorkFlowDataArchiveServiceImpl();
                try {
                    List<String>nodelist = new ArrayList<>();
                    nodelist.add("10.5.6.226");
                    nodelist.add("10.5.6.205");
                    nodelist.add("10.5.6.206");
                    nodelist.add("10.5.6.207");
                    nodelist.add("10.5.6.208");
                    nodelist.add("10.5.6.209");
                    nodelist.add("10.5.6.210");
                    nodelist.add("10.5.6.211");
                    nodelist.add("10.5.6.212");
                    Random r = new Random();            //生成一个随机数
                    int number = r.nextInt(8);  //随机数为0-8
                    String node = nodelist.get(number);
                    //WorkflowOrder order = orderList.get(0);
                    order.setEndTime(DateUtil.getTime());
                    String orderType = order.getOrderType().split("_")[0];
                    ProcessType processType;
                    Map orderParams;
                    switch (orderType) {
                        //todo 分Q61,62,63,64,65情况
                        case "QATask":
                            //先生成报告文件路径
                            Date time = new Date();
                            QAReportFile = ResponseType.buildQAReportFileRelativePath(order.getJobTaskID(),order.getSatelliteName().replace("-",""),order.getTaskSerialNumber());
                            QATaskRep rep1;
                            QATaskWorkflowInfo wi = new QATaskWorkflowInfo();
                            List<ProcessInfo>dataInfoList = new ArrayList<>();
                            if (order.getTaskMode().equals("Q61")){
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                    case "CASEARTH":
                                    case "ZY-3B":
                                    case "ZY1E":
                                        dataInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_R0_TO_R0REPORT");
                                        if (dataInfoList.size()==2){
                                            if (dataInfoList.get(0).getStatus().equals("Completed")&&dataInfoList.get(1).getStatus().equals("Completed")){
                                                getReportStatus(order,"Q61");
                                            }else if(dataInfoList.get(0).getStatus().equals("Aborted")||dataInfoList.get(1).getStatus().equals("Aborted")){
                                                order.setOrderStatus("4");
                                                orderService.updateById(order);
                                                //todo 生成QATaskRep
                                                wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                                wi.state = 0;
                                                rep1 = generateQATaskRep(wi,order);
                                            }
                                        }else if (dataInfoList.size()==1){
                                            if (dataInfoList.get(0).getStatus().equals("Completed")){
                                                getReportStatus(order,"Q61");
                                            }else if(dataInfoList.get(0).getStatus().equals("Aborted")){
                                                order.setOrderStatus("4");
                                                orderService.updateById(order);
                                                //todo 生成QATaskRep
                                                wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                                wi.state = 0;
                                                rep1 = generateQATaskRep(wi,order);
                                            }
                                        }
                                        break;
                                    case "CSES":
                                        dataInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_R0_TO_R0REPORT");
                                        if (dataInfoList.size()==1){
                                            if (dataInfoList.get(0).getStatus().equals("Completed")){
                                                getReportStatus(order,"Q61");
                                            }else if(dataInfoList.get(0).getStatus().equals("Aborted")){
                                                order.setOrderStatus("4");
                                                orderService.updateById(order);
                                                //todo 生成QATaskRep
                                                wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                                wi.state = 0;
                                                rep1 = generateQATaskRep(wi,order);
                                            }
                                        }
                                    break;
                                }
                            }
                            if (order.getTaskMode().equals("Q62")){
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                    case "CASEARTH":
                                    case "ZY-3B":
                                    case "ZY1E":
                                        getReportStatus(order,"Q62");
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q63")){
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        String gfStatus1 = "";
                                        List<ProcessInfo>infoQ63GF = processInfoService.getProcessList(order.getTaskSerialNumber(),"GF1_Q63_CAT_TO_L2A");
                                        if (infoQ63GF.size()!=0){
                                            gfStatus1 = getProcessStatus(infoQ63GF,order);
                                        }
                                        if (gfStatus1.equals("success")){
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "CASEARTH":
                                        String casearthStatus1 = "";
                                        List<ProcessInfo>infoQ63casearth = processInfoService.getProcessList(order.getTaskSerialNumber(),"CASEARTH_Q63_CAT_TO_L2A");
                                        if (infoQ63casearth.size()!=0){
                                            casearthStatus1 = getProcessStatus(infoQ63casearth,order);
                                        }
                                        if (infoQ63casearth.equals("success")){
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "ZY-3B":
                                        String zyStatus1 = "";
                                        List<ProcessInfo>infoQ63ZY = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY3_Q63_CAT_TO_L2A");
                                        if (infoQ63ZY.size()!=0){
                                            zyStatus1 = getProcessStatus(infoQ63ZY,order);
                                        }
                                        if (zyStatus1.equals("success")){
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "ZY1E":
                                        String ZY1EStatus1 = "";
                                        List<ProcessInfo>infoQ63ZY1E = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY1E_Q63_CAT_TO_L2A");
                                        if (infoQ63ZY1E.size()!=0){
                                            ZY1EStatus1 = getProcessStatus(infoQ63ZY1E,order);
                                        }
                                        if (ZY1EStatus1.equals("success")){
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q61;Q62")){
                                String status1 = "";
                                List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_R0_TO_R0REPORT");
                                if (reportInfoList.size()!=0){
                                    status1 = getProcessStatus(reportInfoList,order);
                                }
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                    case "CASEARTH":
                                    case "ZY-3B":
                                    case "ZY1E":
                                        if (status1.equals("success")){
                                            getReportStatus(order,"Q61;Q62");
                                        }
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q61;Q63")){
                                String status1 = "";
                                List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_R0_TO_R0REPORT");
                                if (reportInfoList.size()!=0){
                                    status1 = getProcessStatus(reportInfoList,order);
                                }
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        String status2 = "";
                                        List<ProcessInfo>Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"GF1_Q63_CAT_TO_L2A");
                                        if (Q63infoList.size()!=0){
                                            status2 = getProcessStatus(Q63infoList,order);
                                        }
                                        if (status1.equals("success")&&status2.equals("success")){
                                            getReportStatus(order,"Q61;Q63");
                                        }else if (status1.equals("error")||status2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&status2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&status2.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "CASEARTH":
                                        String casearthstatus2 = "";
                                        List<ProcessInfo>Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"CASEARTH_Q63_CAT_TO_L2A");
                                        if (Q63casearthinfoList.size()!=0){
                                            status2 = getProcessStatus(Q63casearthinfoList,order);
                                        }
                                        if (status1.equals("success")&&casearthstatus2.equals("success")){
                                            getReportStatus(order,"Q61;Q63");
                                        }else if (status1.equals("error")||casearthstatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&casearthstatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&casearthstatus2.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                    case "ZY-3B":
                                        String zyStatus2 = "";
                                        List<ProcessInfo>Q63zyInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY3_Q63_CAT_TO_L2A");
                                        if (Q63zyInfoList.size()!=0){
                                            zyStatus2 = getProcessStatus(Q63zyInfoList,order);
                                        }
                                        if (status1.equals("success")&&zyStatus2.equals("success")){
                                            getReportStatus(order,"Q61;Q63");
                                        }else if (status1.equals("error")||zyStatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&zyStatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&zyStatus2.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                    break;
                                    case "ZY1E":
                                        String ZY1EStatus2 = "";
                                        List<ProcessInfo>Q63zy1eInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY1E_Q63_CAT_TO_L2A");
                                        if (Q63zy1eInfoList.size()!=0){
                                            ZY1EStatus2 = getProcessStatus(Q63zy1eInfoList,order);
                                        }
                                        if (status1.equals("success")&&ZY1EStatus2.equals("success")){
                                            getReportStatus(order,"Q61;Q63");
                                        }else if (status1.equals("error")||ZY1EStatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&ZY1EStatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&ZY1EStatus2.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q62;Q63")){
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        String status2 = "";
                                        List<ProcessInfo>Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"GF1_Q63_CAT_TO_L2A");
                                        if (Q63infoList.size()!=0){
                                            status2 = getProcessStatus(Q63infoList,order);
                                        }
                                        if (status2.equals("success")) {
                                            getReportStatus(order,"Q62;Q63");
                                        } else if (status2.equals("errors")) {
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q62");
                                        }
                                        break;
                                    case "CASEARTH":
                                        String casearthstatus2 = "";
                                        List<ProcessInfo>Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"CASEARTH_Q63_CAT_TO_L2A");
                                        if (Q63casearthinfoList.size()!=0){
                                            status2 = getProcessStatus(Q63casearthinfoList,order);
                                        }
                                        if (casearthstatus2.equals("success")) {
                                            getReportStatus(order,"Q62;Q63");
                                        } else if (casearthstatus2.equals("errors")) {
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q62");
                                        }
                                        break;
                                    case "ZY-3B":
                                        String zyStatus2 = "";
                                        List<ProcessInfo>Q63zyinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY3_Q63_CAT_TO_L2A");
                                        if (Q63zyinfoList.size()!=0){
                                            zyStatus2 = getProcessStatus(Q63zyinfoList,order);
                                        }
                                        if (zyStatus2.equals("success")){
                                            getReportStatus(order,"Q62;Q63");
                                        }else if (zyStatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q62");
                                        }
                                    break;
                                    case "ZY1E":
                                        String zy1eStatus2 = "";
                                        List<ProcessInfo>Q63zy1einfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY1E_Q63_CAT_TO_L2A");
                                        if (Q63zy1einfoList.size()!=0){
                                            zy1eStatus2 = getProcessStatus(Q63zy1einfoList,order);
                                        }
                                        if (zy1eStatus2.equals("success")){
                                            getReportStatus(order,"Q62;Q63");
                                        }else if (zy1eStatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q62");
                                        }
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q61;Q62;Q63")){
                                String status1 = "";
                                List<ProcessInfo>Q61infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_R0_TO_R0REPORT");
                                if (Q61infoList.size()!=0){
                                    status1 = getProcessStatus(Q61infoList,order);
                                }
                                switch (order.getSatelliteName()){
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        String status2 = "";
                                        List<ProcessInfo>Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"GF1_Q63_CAT_TO_L2A");
                                        if (Q63infoList.size()!=0){
                                            status2 = getProcessStatus(Q63infoList,order);
                                        }
                                        if (status1.equals("success")&&status2.equals("success")){
                                            getReportStatus(order,"Q61;Q62;Q63");
                                        }else if (status1.equals("error")||status2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&status2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&status2.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "CASEARTH":
                                        String casearthstatus2 = "";
                                        List<ProcessInfo>Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"CASEARTH_Q63_CAT_TO_L2A");
                                        if (Q63casearthinfoList.size()!=0){
                                            status2 = getProcessStatus(Q63casearthinfoList,order);
                                        }
                                        if (status1.equals("success")&&casearthstatus2.equals("success")){
                                            getReportStatus(order,"Q61;Q62;Q63");
                                        }else if (status1.equals("error")||casearthstatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&casearthstatus2.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&casearthstatus2.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "ZY-3B":
                                        String status3 = "";
                                        List<ProcessInfo>Q63ZYinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY3_Q63_CAT_TO_L2A");
                                        if (Q63ZYinfoList.size()!=0){
                                            status3 = getProcessStatus(Q63ZYinfoList,order);
                                        }
                                        if (status1.equals("success")&&status3.equals("success")){
                                            getReportStatus(order,"Q61;Q62;Q63");
                                        }else if (status1.equals("error")&&status3.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&status3.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&status3.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                    case "ZY1E":
                                        String status4 = "";
                                        List<ProcessInfo>Q63ZY1EinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY1E_Q63_CAT_TO_L2A");
                                        if (Q63ZY1EinfoList.size()!=0){
                                            status4 = getProcessStatus(Q63ZY1EinfoList,order);
                                        }
                                        if (status1.equals("success")&&status4.equals("success")){
                                            getReportStatus(order,"Q61;Q62;Q63");
                                        }else if (status1.equals("error")&&status4.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }else if (status1.equals("success")&&status4.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q61");
                                        }else if (status1.equals("error")&&status4.equals("success")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            getReportStatus(order,"Q63");
                                        }
                                        break;
                                }
                            }
                            if(order.getTaskMode().equals("Q64")){
                                ProcessInfo Q64info =null;
                                String status1 = "";
                                if (order.getSatelliteName().equals("ZY-3B")){
                                    Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(),"ZY3_Q64_DIFF");
                                }else if(order.getSatelliteName().equals("CASEARTH")){
                                    Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(),"CASEARTH_Q64_DIFF");
                                }else{
                                    Q64info = processInfoService.getProcessByName(order.getTaskSerialNumber(),"GF_Q64_DIFF");
                                }
                                if (Q64info!=null){
                                    if (Q64info.getStatus().equals("Completed")){
                                        List<ProcessInfo>reportList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_Q64_R0REPORT");
                                        status1 = getProcessStatus(reportList,order);
                                        if (status1.equals("success")){
                                            ProcessInfo info = processInfoService.getProcessByName(order.getTaskSerialNumber(),"KJ125_Q64");
                                            if (info==null){
                                                doTriggerQ64(order);
                                            }else {
                                                if(info.getStatus().equals("Completed")){
                                                    order.setOrderStatus("3");
                                                    orderService.updateById(order);
                                                    //todo 生成QATaskRep
                                                    wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                                    wi.state = 1;
                                                    rep1 = generateQATaskRep(wi,order);
                                                }else if (info.getStatus().equals("Aborted")){
                                                    order.setOrderStatus("4");
                                                    orderService.updateById(order);
                                                    //todo 生成QATaskRep
                                                    wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                                    wi.state = 0;
                                                    rep1 = generateQATaskRep(wi,order);
                                                }
                                            }
                                        }else if (status1.equals("error")){
                                            order.setOrderStatus("4");
                                            orderService.updateById(order);
                                            //todo 生成QATaskRep
                                            wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                            wi.state = 0;
                                            rep1 = generateQATaskRep(wi,order);
                                        }
                                    }else if (Q64info.getStatus().equals("Aborted")){
                                        order.setOrderStatus("4");
                                        orderService.updateById(order);
                                        //todo 生成QATaskRep
                                        wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                        wi.state = 0;
                                        rep1 = generateQATaskRep(wi,order);;
                                    }
                                }
                            }
                            if (order.getTaskMode().equals("Q65")){
                                dataInfoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                                if (dataInfoList.size()!=0){
                                    if (dataInfoList.get(0).getStatus().equals("Completed")){
                                        order.setOrderStatus("3");
                                        orderService.updateById(order);
                                        //todo 生成QATaskRep
                                        wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                        wi.state = 1;
                                        rep1 = generateQATaskRep(wi,order);
                                    }else if (dataInfoList.get(0).getStatus().equals("Aborted")){
                                        order.setOrderStatus("4");
                                        orderService.updateById(order);
                                        //todo 生成QATaskRep
                                        wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                                        wi.state = 0;
                                        rep1 = generateQATaskRep(wi,order);
                                    }
                                }
                            }
                            break;
                        case"DATask":
                            DataArchiveRep rep;
                            switch (order.getSatelliteName()){
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                    String status1 = "";
                                    List<ProcessInfo>gfL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"GF1_R0_TO_L0");
                                    if (gfL0InfoList.size()!=0){
                                        status1 = getProcessStatus(gfL0InfoList,order);
                                    }
                                    if (status1.equals("success")){
                                        List<ProcessInfo>infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"GF1_L0_TO_CAT");
                                        if (infoList.size()!=0){
                                            String [] sensorStr = {"PAN1","PAN2","MSS1","MSS2"};
                                            modifyTask(infoList,order,sensorStr,node);              //node为10.5.6.*
                                        }
                                    }
                                    break;
                                case "CASEARTH":
                                    String status2 = "";
                                    List<ProcessInfo>casearthL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"CASEARTH_R0_TO_L0");
                                    if (casearthL0InfoList.size()!=0){
                                        status2 = getProcessStatus(casearthL0InfoList,order);
                                    }
                                    if (status2.equals("success")){
                                        List<ProcessInfo>infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"CASEARTH_L0_TO_CAT");
                                        if (infoList.size()!=0){
                                            String [] sensorStr = {"PAN1","PAN2","MSS1","MSS2"};
                                            modifyTask(infoList,order,sensorStr,node);
                                        }
                                    }
                                    break;
                                case "ZY-3B":
                                    String ZYstatus1 = "";
                                    List<ProcessInfo>zyL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY3_R0_TO_L0");
                                    if (zyL0InfoList.size()!=0){
                                        ZYstatus1 = getProcessStatus(zyL0InfoList,order);
                                    }
                                    if (ZYstatus1.equals("success")){
                                        List<ProcessInfo>infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY3_L0_TO_CAT");
                                        String [] sensorStr = {"MUX","TLC"};
                                        modifyTask(infoList,order,sensorStr,node);

                                    }
                                    break;
                                case "ZY1E":
                                    String ZY1EStatus = "";
                                    List<ProcessInfo>ZY1EL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY1E_R0_TO_L0");
                                    if (ZY1EL0InfoList.size()!=0){
                                        ZY1EStatus = getProcessStatus(ZY1EL0InfoList,order);
                                    }
                                    if (ZY1EStatus.equals("success")){
                                        List<ProcessInfo>infoList = processInfoService.getProcessList(order.getTaskSerialNumber(),"ZY1E_L0_TO_CAT");
                                        String [] sensorStr = {"MSS","PAN"};
                                        modifyTask(infoList,order,sensorStr,node);
                                    }
                                    break;
                                }
                            break;
                        case"PRTask":
                            //todo 可能一级生产，也可能是二级生产
                            List<ProcessInfo> infoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                            //todo 压缩的时候添加个线程
                            ExecutorService threadPool = Executors.newSingleThreadExecutor();
                            if (infoList.size()!=0){
                                if (infoList.get(0).getStatus().equals("Completed")){
                                    order.setOrderStatus("3");
                                    order.setEndTime(DateUtil.getTime());
                                    orderService.updateById(order);
                                    //todo 修改成功后，休息两秒进行压缩
                                    Thread.sleep(2000);
                                    //todo 产品压缩方法 toZip
                                    //todo 先判断是否为空
                                    //todo Move成功后，先获取产品表里面数据，然后进行存储
                                    if (order.getOut_productdir()!=null&&!order.getOut_productdir().equals("")){
                                        logger.info("进行压缩的节点为"+node);


                                        String targetDir = order.getOut_productdir().split(";")[0];
                                        //todo 先创建目录
                                        Files.createDirectories(new File(targetDir).toPath());
                                        //todo 如果是做的二级产品则dir是二级产品的本地路径，否则是一级产品的路径
                                        String dir = order.getOut_productdir().split(";")[1];

                                        //todo 暂时别加判断了，应该会生成的
                                      /*  File[] listFile = new File(dir).listFiles();
                                        if (listFile.length==0){
                                            logger.info("未生成产品文件，ERROR");
                                        }else {

                                        }*/
                                        String dirName = new File(dir).getName();
                                        String zipDir = dir+".zip";
                                        FileOutputStream fos1 = new FileOutputStream(new File(zipDir));
                                        if (order.getProductLevel().equals("L1")){
                                            //todo 输入输出流压缩文件
                                            toZip1(dir,fos1,true);
                                            File zipDir1 = new File(zipDir);
                                            if (zipDir1.length()==0||!zipDir1.isFile()){
                                                logger.info("1级产品压缩未成功，再执行一遍");
                                                toZip1(dir,fos1,true);
                                            }else {
                                                logger.info("1级产品压缩成功");
                                            }
                                            //todo 使用脚本的方式进行压缩文件
                                          /* String unzipScript = "zip -r "+zipDir+" "+dir+" ";
                                           logger.info("打印下压缩命令"+unzipScript);
                                           execShellscriptUnzip(unzipScript,Config.node_host);*/
                                            Thread.sleep(10000);
                                            String script = "cp -rf "+zipDir+" "+targetDir;
                                            logger.info("打印下移动命令"+script);
                                            execShellscript(script,node);
                                            //todo 测试复制命令
                                            String script1 = "cp -rf "+zipDir+"  /ncsfs/product/";
                                            logger.info("打印下移动命令"+script1);
                                            execShellscript(script1,node);
                                            //todo du显示文件所占磁盘大小
                                            String checkScript = "du -s "+targetDir+"/"+dirName+".zip";
                                            logger.info("打印下check命令"+checkScript  );
                                            String result = execShellscript(checkScript,node);
                                            if (result!=null){
                                                logger.info("Move成功");
                                            }else if (result==null){
                                                logger.info("Move失败,重新Move");
                                                for (int k = 0; k< Config.move_number; k++){
                                                    execShellscript(script,node);
                                                    String result1 = execShellscript(checkScript,node);
                                                    if (result!=null){
                                                        break;
                                                    }else{
                                                        k++;
                                                    }
                                                }
                                            }
                                            //todo move成功后，更新数据库表
                                            L1Product l1 = infoImpl.getL1product(order.getSceneID());
                                            oracleInfoImpl.delL1Product(order.getSceneID());
                                            oracleInfoImpl.insertL1product(l1);
                                        }else if (order.getProductLevel().equals("L2")){
                                            //todo 二级产品是否要连一级产品也打包？
                                            String L1Dir = dir.replaceAll("L2DATA","L1DATA").replaceAll("L2","L1");
                                            String L1DirZip = L1Dir+".zip";
                                            FileOutputStream fos2 = new FileOutputStream(new File(L1DirZip));
                                            logger.info("二级产品开始压缩");
                                            //todo 两种方式可以随意切换，到底用哪个呢，使用Java自带
                                            toZip1(dir,fos1,true);
                                            File zipDir1 = new File(zipDir);
                                            if (zipDir1.length()==0||!zipDir1.isFile()){
                                                logger.info("二级产品压缩未成功，重新压缩");
                                                toZip1(dir,fos1,true);
                                            }else {
                                                logger.info("压缩成功");
                                            }
                                            logger.info("一级产品压缩开始");
                                            //threadPool.execute();
                                            toZip1(L1Dir,fos2,true);
                                            File L1dirZip1 = new File(L1DirZip);
                                            if (L1dirZip1.length()==0||!L1dirZip1.isFile()){
                                                logger.info("1级产品压缩未成功，重新压缩");
                                                toZip1(L1Dir,fos2,true);
                                            }else {
                                                logger.info("一级产品压缩成功");
                                            }
                                            //todo 使用脚本进行压缩
                                         /*  String unzipScript1 = "zip -r "+L1DirZip+" "+L1Dir+" ";
                                           logger.info("打印下1级压缩命令"+unzipScript1);
                                           execShellscriptUnzip(unzipScript1,Config.node_host);
                                           String unzipScript2 = "zip -r "+zipDir+" "+dir+" ";
                                           logger.info("打印下2级压缩命令"+unzipScript2);
                                           execShellscriptUnzip(unzipScript2,Config.node_host);*/

                                            //todo 造一个L1的地址
                                            String targetL1Dir = targetDir.replaceAll("L2","L1");
                                            Files.createDirectories(new File(targetL1Dir).toPath());

                                            Thread.sleep(10000);
                                            //todo mv 改为cp -rf
                                            String L1script = "cp -rf "+L1DirZip+" "+targetL1Dir;
                                            logger.info("打印下1级的产品移动命令"+L1script);
                                            execShellscript(L1script,node);
                                            String script = "cp -rf "+zipDir+" "+targetDir;
                                            logger.info("打印下2级的产品移动命令"+script);
                                            execShellscript(script,node);
                                            String checkScript = "du -s "+targetDir+"/"+dirName+".zip";
                                            logger.info("打印下check命令"+checkScript  );
                                            String result = execShellscript(checkScript,node);
                                            if (result!=null){
                                                logger.info("Move成功");
                                            }else if (result==null){
                                                logger.info("Move失败,重新Move");
                                                for (int k=0;k<Config.move_number;k++){
                                                    execShellscript(script,node);
                                                    String result1 = execShellscript(checkScript,node);
                                                    if (result1!=null){
                                                        break;
                                                    }else{
                                                        k++;
                                                    }
                                                }
                                            }
                                            //todo move成功后，更新数据库表
                                            L1Product l1 = infoImpl.getL1product(order.getSceneID());
                                            oracleInfoImpl.delL1Product(order.getSceneID());
                                            oracleInfoImpl.insertL1product(l1);
                                            L1Product l2 = infoImpl.getL2product(order.getSceneID());
                                            oracleInfoImpl.delL2Product(order.getSceneID());
                                            oracleInfoImpl.insertL2product(l2);
                                        }
                                    }
                                }else if (infoList.get(0).getStatus().equals("Aborted")){
                                    order.setOrderStatus("4");
                                    order.setEndTime(DateUtil.getTime());
                                    orderService.updateById(order);
                                }else if(infoList.get(0).getStatus().equals("Running")){
                                    logger.info("PRTask is running");
                                }/*else if (infoList.get(0).getStatus().equals("Cancelled")){
                                    order.setOrderStatus("5");
                                    order.setEndTime(DateUtil.getTime());
                                    orderService.updateById(order);
                                }*/
                            }
                            break;
                    }
                }catch (Exception e){
                    logger.info(e);
                }
            }
        });
    }

    public void getReportStatus(WorkflowOrder order,String orderType)throws Exception{
        QATaskRep rep1;
        QATaskWorkflowInfo wi = new QATaskWorkflowInfo();
        ProcessType processType;
        Map orderParams;
        Date time = new Date();
        ProcessInfo QaInfo = processInfoService.getProcessByName(order.getTaskSerialNumber(), "KJ125_Q61_62_63_QAReport");
        if (QaInfo == null) {
            processType = ProcessType.KJ125_Q61_62_63_QAReport;
            orderParams = generateOrderParamsForGF_Q61_62_63_QAReport(order, time,orderType);
            //构建流程订单
            String orderXml = processType.generateOrderXml(orderParams);
            String orderId = ProcessUtil.submitProcess(orderXml, Config.submit_order_timeout);
        } else {
            if (QaInfo.getStatus().equals("Completed")) {
                order.setOrderStatus("3");
                order.setEndTime(DateUtil.getTime());
                orderService.updateById(order);
                //todo 生成QATaskRep
                wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                wi.state = 1;
                rep1 = generateQATaskRep(wi, order);
            } else if (QaInfo.getStatus().equals("Aborted")) {
                order.setOrderStatus("4");
                order.setEndTime(DateUtil.getTime());
                orderService.updateById(order);
                //todo 生成QATaskRep
                wi = infoImpl.getQaTaskWorkFlowInfo(order.getTaskSerialNumber());
                wi.state = 0;
                rep1 = generateQATaskRep(wi, order);
            }
        }
    }
    public String getProcessStatus(List<ProcessInfo> infoList, WorkflowOrder order)throws Exception{
        String status = "";
        for (int i = 0; i < infoList.size(); i++) {
            if (infoList.get(i).getStatus().equals("Completed")) {
                status = "success";
            } else if (infoList.get(i).getStatus().equals("Aborted")) {
                status = "error";
                order.setEndTime(DateUtil.getTime());
                order.setOrderStatus("4");
                orderService.updateById(order);
                break;
            } else {
                status = "running";
                break;
            }
        }
        return status;
    }
    public void modifyTask(List<ProcessInfo> infoList, WorkflowOrder order, String [] sensorStr, String node)throws Exception{
        String status = "";
        DataArchiveRep rep;
        infoImpl =new ProcessInfoImpl();
        //catManager = new mCatManager();
        oracleInfoImpl = new OracleProcessInfoImpl();
        //orderService = new WorkFloworderService();
        //processInfoService = new ProcessInfoManager();
        //workFlowDataArchiveService = new WorkFlowDataArchiveManager();
        for (int i = 0; i < infoList.size(); i++) {
            if (infoList.get(i).getStatus().equals("Completed")) {
                status = "success";
            } else if (infoList.get(i).getStatus().equals("Aborted")) {
                status = "error";
                break;
            } else {
                status = "running";
                break;
            }
        }
        if (status.equals("success")) {
            order.setOrderStatus("3");
            order.setEndTime(DateUtil.getTime());
            orderService.updateById(order);
            //todo 更新集中存储里面的表里为已归档
            oracleInfoImpl.updateRaw_data(order.getJobTaskID());
            logger.info("更新归档状态成功");
            //todo 查询gt_m_l0表，然后对集中存储L0表进行删除，重新插入操作
            L0DATA l0data = infoImpl.getL0Data(order.getJobTaskID());
            oracleInfoImpl.delL0data(order.getJobTaskID());
            oracleInfoImpl.insertL0Data(l0data);
            //todo 生成DataArchiveRep
            WorkFlowDataArchive dataArchive = workFlowDataArchiveService.getDataArchive(order.getTaskSerialNumber());
            //todo 发给OMO一份
            /*  if (!order.getOrderType().split("_")[1].equals("OMO")){
                  rep = generateDataArchiveRepforOMO(order,dataArchive);
              }*/
            //todo 归档复制操作
           /* Mcat GfMcat = new Mcat();
            Map<String,String>map = new HashMap<>();
            for (int k=0;k<sensorStr.length;k++){
                map.put("jobTaskId",order.getJobTaskID());
                map.put("sensorId",sensorStr[k]);
                GfMcat = catManager.selectUrlByJobtaskId(map);
                String filePath = new File(GfMcat.getFilepath()).getParent();
                File newPath = new File("/ncsfs/work_zone/ZL/DataBank/CATALOG/Incoming/"+order.getJobTaskID());
                if (!newPath.isDirectory()){
                    newPath.mkdirs();
                }
                String cmd = "cp -rf "+filePath+"/*  "+newPath;
                execShellscript(cmd,node);
                logger.info("打印下执行脚本:"+cmd);
            }*/

            rep = generateDataArchiveRep(order,dataArchive);
            WorkFlowDataArchive archiveInfo = new WorkFlowDataArchive();
            archiveInfo.setReplyfile(rep.replyFileName);
            archiveInfo.setUpdatetime(DateUtil.getTime());
            archiveInfo.setReply(1);
            workFlowDataArchiveService.updateById(archiveInfo);
        } else if (status.equals("error")) {
            order.setEndTime(DateUtil.getTime());
            order.setOrderStatus("4");
            orderService.updateById(order);
            //生成DataArchiveRep
            WorkFlowDataArchive dataArchive = workFlowDataArchiveService.getDataArchive(order.getTaskSerialNumber());
            //todo 发给OMO一份
          /* if (!order.getOrderType().split("_")[1].equals("OMO")){
               rep = generateDataArchiveRepforOMO(order,dataArchive);
           }*/
            rep = generateDataArchiveRep(order, dataArchive);
            WorkFlowDataArchive archiveInfo = new WorkFlowDataArchive();
            archiveInfo.setReplyfile(rep.replyFileName);
            archiveInfo.setUpdatetime(DateUtil.getTime());
            archiveInfo.setReply(1);
            workFlowDataArchiveService.updateById(archiveInfo);
        }
    }

    public Thread toZip(String srcDir, OutputStream out, boolean KeepDirStructure)throws RuntimeException{
        return new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                ZipOutputStream zos = null ;
                try {
                    logger.info("开始进行压缩");
                    zos = new ZipOutputStream(out);
                    File sourceFile = new File(srcDir);
                    compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
                    long end = System.currentTimeMillis();
                    logger.info("压缩完成，耗时：" + (end - start) +" ms");
                } catch (Exception e) {
                    throw new RuntimeException("zip error from ZipUtils",e);
                }finally{
                    if(zos != null){
                        try {
                            zos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
    //todo java输入输出流进行压缩文件
    public static void toZip1(String srcDir, OutputStream out, boolean KeepDirStructure)
            throws RuntimeException{
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try {
            logger.info("开始进行压缩");
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
            long end = System.currentTimeMillis();
            logger.info("压缩完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception{
        FileInputStream in = null;
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            if(sourceFile.isFile()){
                // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
                zos.putNextEntry(new ZipEntry(name));
                // copy文件到zip输出流中
                int len;
                 in = new FileInputStream(sourceFile);
                while ((len = in.read(buf)) != -1){
                    zos.write(buf, 0, len);
                }
                // Complete the entry
                zos.closeEntry();
            } else {
                File[] listFiles = sourceFile.listFiles();
                if(listFiles == null || listFiles.length == 0){
                    // 需要保留原来的文件结构时,需要对空文件夹进行处理
                    if(KeepDirStructure){
                        // 空文件夹的处理
                        zos.putNextEntry(new ZipEntry(name + "/"));
                        // 没有文件，不需要文件的copy
                        zos.closeEntry();
                    }
                }else {
                    for (File file : listFiles) {
                        // 判断是否需要保留原来的文件结构
                        if (KeepDirStructure) {
                            // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                            // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                            compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                        } else {
                            compress(file, zos, file.getName(),KeepDirStructure);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error(e);
        }finally {
            if (in!=null){
                in.close();
            }
        }

    }
    public static String execShellscript(String cmd, String hostAddr) {
        Connection conn = null;
        Session sess = null;
        String line = "";
        InputStream stdout = null;
        BufferedReader br = null;
        StringBuffer buffer = new StringBuffer("exec result:");
        buffer.append(System.getProperty("line.separtor"));				//换行
        try {
            conn = getOpenConnection(hostAddr);
            sess = conn.openSession();
            sess.execCommand(cmd);
            stdout = new StreamGobbler(sess.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            while(true) {
                line = br.readLine();
                if(line != null)
                    break;
                else if (line == null)
                    break;
            }
            sess.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stdout != null)
                    stdout.close();
                if(br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }
    /***
     * @param host 对应节点的主机地址 10.5.6.223
     */
    private static Connection getOpenConnection(String host) throws IOException {
        // create connection.
        Connection conn = new Connection(host, 22);
        conn.connect();// make sure the connection is opened. this is necessary.
        boolean isAuthenticate = conn.authenticateWithPassword("oper", "12qwas");
        // authenticate this connection is connected.
        if(isAuthenticate == false)
            throw new IOException("Authentication failed.");
        return conn;
       /* Connection conn = new Connection(host, Config.node_port);
        conn.connect();// make sure the connection is opened. this is necessary.
        boolean isAuthenticate = conn.authenticateWithPassword(Config.node_username, Config.node_password);
        // authenticate this connection is connected.
        if(isAuthenticate == false)
            throw new IOException("Authentication failed.");
        return conn;*/
    }

    public QATaskRep generateQATaskRep(QATaskWorkflowInfo wi,WorkflowOrder order) throws Exception {
        //对每个已完成的任务生成完成通知
        /**
         *  output->文件名称：QATaskRep_DQA_OMO_任务单流水号_YYYYMMDDhhmmss.xml
         *               如：QATaskRep_DQA_OMO_QA2010000001_20100101200000.xml
         *         报告名称：QAReport_卫星简称_任务单流水号_YYYYMMDDhhmmss.xls
         *                  QAReport_GF01_QA20100001_20100101200000.xls
         */
        wi.taskMode = order.getTaskMode();
        wi.satellite = order.getSatelliteName();
        wi.jobTaskID = order.getJobTaskID();
        wi.channel  = order.getChannelID();
        wi.sensor = order.getSensorName();
        wi.dataSelectType = order.getDataSelectType();
        if (QAReportFile==null){
            QAReportFile = ResponseType.buildQAReportFileRelativePath(order.getJobTaskID(),order.getSatelliteName().replace("-",""),order.getTaskSerialNumber());
        }
        wi.QAReportFile = QAReportFile;
        // "QAReport_"+order.getSatelliteName().replaceAll("-","")+"_"+order.getTaskSerialNumber()+"_"+DateUtil.getSdfTimes()+".xlsx";
        QATaskRep rep=new QATaskRep();
        //header
        ResponseType.QATaskRep.setHeaderInfo(order.getOrderType().split("_")[1],rep, Config.toOMO_author);
        //content
        rep.taskBasicRepInfo=new ArrayList<>();
        //重用QATaskInqAction的方法，需每次创建新实例
        rep.taskBasicRepInfo.add(new QATaskInqAction().generateTaskInfo(wi));
        //生成响应文件
        String fileName=ResponseType.QATaskRep.buildResponseFileName(order.getOrderType().split("_")[1],wi.taskId);
        generateReponseFile(rep,fileName);
        rep.replyFileName=fileName;
        logger.info("响应文件"+rep);
        return rep;
    }

    public DataArchiveRep generateDataArchiveRep(WorkflowOrder order, WorkFlowDataArchive wi) throws Exception {
        //对每个已完成的任务生成完成通知
        /**
         *  output->文件名称：DataArchiveRep_DQA_OMO_YYYYMMDDhhmmss.xml
         *               如：DataArchiveRep_DQA_OMO_20100101200000.xml
         */
        ArchiveWorkflowInfo archiveWorkflowInfo = new ArchiveWorkflowInfo();
        archiveWorkflowInfo.jobTaskID = wi.getJobtaskid();
        archiveWorkflowInfo.orderId = wi.getOrderid();
        archiveWorkflowInfo.reply = wi.getReply();
        archiveWorkflowInfo.dataFile = wi.getDatafile();
        archiveWorkflowInfo.createTime = wi.getCreatetime();
        archiveWorkflowInfo.updateTime = wi.getUpdatetime();
        DataArchiveRep rep=new DataArchiveRep();
        //header
        ResponseType.DataArchiveRep.setHeaderInfo(order.getOrderType().split("_")[1],rep, Config.toOMO_author);
        //content
        rep.description = "自动回复";
        rep.dataStatusRepInfo = new ArrayList<>();
        //重用DataArchiveInqAction的方法，需每次创建新实例
        rep.dataStatusRepInfo.add(new DataArchiveInqAction().generateJobInfo(archiveWorkflowInfo));
        //生成响应文件
        String fileName=ResponseType.DataArchiveRep.buildResponseFileName(order.getOrderType().split("_")[1],null);
        generateReponseFile(rep,fileName);
        rep.replyFileName=fileName;
        return rep;
    }
    public  void doTriggerQ64(WorkflowOrder wi) throws Exception {
        logger.info("auto-triggering Q64 for: " + wi);
        ProcessType processType = ProcessType.KJ125_Q64;
        Map<String, Object> orderParams = generateOrderParamsForGF_Q64(wi);
        //构建流程订单
        String orderXml = processType.generateOrderXml(orderParams);
        logger.debug("generate process order: \n" + orderXml);
        //提交流程
        String orderId = ProcessUtil.submitProcess(orderXml,Config.submit_order_timeout);
    }
    private static Map<String, Object> generateOrderParamsForGF_Q64(WorkflowOrder wi) throws Exception{
        Date time=new Date();
        Map<String, Object> ret = new HashMap<>();
        String satelliteName = wi.getSatelliteName().replaceAll("-","");
        ret.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        ret.put("TASKSERIALNUMBER",wi.getTaskSerialNumber());
        ret.put("SATELLITE", satelliteName);
        ret.put("SENSOR", wi.getSensorName());
        ret.put("ORBIT", wi.getOrbitNumber());

        List<String> jobs= MyHelper.string2StringList(wi.getJobTaskID(), String2ListXmlAdapter.DELIMIT);
        String job1=jobs.get(0), job2=jobs.get(1);   //差异分析必然有两个作业任务编号
        if (job1.split("/").length>1){
            job1 = job1.split("/")[job1.split("/").length-1];
        }
        if (job2.split("/").length>1){
            job2 = job2.split("/")[job2.split("/").length-1];
        }
        ret.put("JOBTASKID1", job1);
        ret.put("JOBTASKID2", job2);

        //按命名规范构建质量分析报告文件及差异文件的路径
        String date = new SimpleDateFormat("yyyyMM").format(time)+"/"+new SimpleDateFormat("yyyyMMdd").format(time);
        File report1_S1=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job1+"_"+ Channel.S1.name()+".report.xml");
        File report2_S1=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job2+"_"+Channel.S1.name()+".report.xml");
        File report1_S2=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job1+"_"+Channel.S2.name()+".report.xml");
        File report2_S2=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job2+"_"+Channel.S2.name()+".report.xml");
        File diffTxt=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job1+"_"+job2+".diff.txt");
        ret.put("REPORT1_S1", report1_S1.isFile()?report1_S1:null);    //质量分析报告文件可能不全（单通道情况），实际不存在时填空
        ret.put("REPORT2_S1", report2_S1.isFile()?report2_S1:null);
        ret.put("REPORT1_S2", report1_S2.isFile()?report1_S2:null);
        ret.put("REPORT2_S2", report2_S2.isFile()?report2_S2:null);
        ret.put("DIFFTXT", diffTxt);       //差异文件必须存在
        //todo Q64 暂时不知道写啥 先瞎写
        File reportFile = new File(Config.dataBank_dir, "/"+satelliteName+"/REPORT/"+
                new SimpleDateFormat("yyyyMM").format(time)+"/"+
                new SimpleDateFormat("yyyyMMdd").format(time)+"/"+wi.getTaskSerialNumber()+"/"+wi.getTaskSerialNumber()+".xlsx");
        Files.createDirectories(reportFile.getParentFile().toPath());  //必须先尝试创建各级目录
        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }
    private Map generateOrderParamsForGF_Q61_62_63_QAReport(WorkflowOrder t,Date time,String taskmode) throws Exception {
        /**
         *  %YYYYMMDD_XXXXXX%        订单ID后缀
         *  %SATELLITE%          卫星简称
         *  %TASKMODE%           作业模式
         *  %JOBTASKID%          jobTaskId
         *  %TASKID%             就是taskSerialNumber
         *  %CHANNEL%            通道
         *  %SENSOR%             传感器
         *  %DATASELECTTYPE%     数据选取方式
         *
         *  %QAREPORT%           生成报表文件的绝对路径
         *
         *  报表文件归档路径规范参看：ResponseType.buildQAReportFile()
         */
        Map<String, Object> ret = new HashMap();
        ret.put("TASKSERIALNUMBER",t.getTaskSerialNumber());
        ret.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        ret.put("SATELLITE", t.getSatelliteName().replace("-",""));
        ret.put("TASKMODE", taskmode);
        ret.put("JOBTASKID", t.getJobTaskID());
        ret.put("TASKID", t.getTaskSerialNumber());
        ret.put("CHANNEL", t.getChannelID());
        ret.put("SENSOR", t.getSensorName());
        if(t.getDataSelectType() != null&&!"".equals(t.getDataSelectType()))
            ret.put("DATASELECTTYPE", t.getDataSelectType());
        else
            ret.put("DATASELECTTYPE", "AutoType");
        File reportFile = new File(Config.dataBank_dir+"/", QAReportFile);
        Files.createDirectories(reportFile.getParentFile().toPath());  //必须先尝试创建各级目录
        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }

    public void generateReponseFile(Response response, String fileName) throws Exception {
        if (Config.toOMO_sendingDir==null){
            Config.toOMO_sendingDir=new File("/KJ125ZL/125interface/toOMO_backup","sending");
        }
        //所注册的类必须存在@XmlRootElement标注，否则marshal时无法获知根标签名
        File tmp=new File(Config.toOMO_sendingDir, Constants.TEMP_FILE_PREFIX+fileName); //先写为临时文件名(TEMP_FILE_PREFIX为在前面加！)
        synchronized (marshaller){  //注意同步使用，看其它线程是否在用
            marshaller.marshal(response,tmp);
        }
        File dest=new File(Config.toOMO_sendingDir, fileName);
        logger.info("destttt"+dest.getPath());
        Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING); //序列化成功后恢复原名。重名替换，省得麻烦
        logger.debug("generated response file: "+dest.getPath());       //debug时才会输出
    }
}
