package com.business.business.action;

import com.business.business.Service.ProcessInfoService;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.config.Config;
import com.business.business.db.OracleProcessInfoImpl;
import com.business.business.db.ProcessInfoImpl;
import com.business.business.entity.*;
import com.business.business.info.QATaskWorkflowInfo;
import com.business.business.util.DateUtil;
import com.business.business.util.MyHelper;
import com.business.business.util.ReportUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ProcessInfoService processInfoService;
    @Resource
    private ProcessInfoImpl infoImpl;
    @Resource
    private OracleProcessInfoImpl oracleInfoImpl;

    //添加定时任务
    @Scheduled(cron = "0/5 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public void configureTasks() throws Exception {
        System.out.println(DateUtil.getTime() + "running任务");
        ExecutorService threadPool = Executors.newCachedThreadPool();
        int i = 99;
        do {
            Thread.sleep(5000);
            List<WorkflowOrder> orderList = orderService.selectRunList("2");
            if (orderList.size() != 0) {
                for (int j = 0; j < orderList.size(); j++)
                    threadPool.execute(SchedulRunningTask(orderList.get(j)));
            }
        } while (i != 1);
    }

    public Thread SchedulRunningTask(WorkflowOrder order) throws Exception {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> nodelist = new ArrayList<>();
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
                                        ReportUtil.ReportStatusQ61(order, dataInfoList, wi);
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
                                        ReportUtil.ReportStatus(order, "Q62");
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q63")) {
                                switch (order.getSatelliteName()) {
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        List<ProcessInfo> infoQ63GF = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusSuccess(order, "Q63", infoQ63GF);
                                        break;
                                    case "CASEARTH":
                                        List<ProcessInfo> infoQ63casearth = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusSuccess(order, "Q63", infoQ63casearth);

                                        break;
                                    case "ZY-3B":
                                        List<ProcessInfo> infoQ63ZY = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusSuccess(order, "Q63", infoQ63ZY);
                                        break;
                                    case "ZY1E":
                                        List<ProcessInfo> infoQ63ZY1E = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusSuccess(order, "Q63", infoQ63ZY1E);
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
                                        ReportUtil.ReportStatusSuccess(order, "Q61;Q62", reportInfoList);
                                        break;

                                }
                            }
                            if (order.getTaskMode().equals("Q61;Q63")) {
                                String status1 = "";
                                List<ProcessInfo> reportInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                                if (reportInfoList.size() != 0) {
                                    status1 = ReportUtil.getProcessStatus(reportInfoList, order);
                                }
                                switch (order.getSatelliteName()) {
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q63(status1, order, Q63infoList, wi);
                                        break;
                                    case "CASEARTH":
                                        List<ProcessInfo> Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q63(status1, order, Q63casearthinfoList, wi);
                                        break;
                                    case "ZY-3B":
                                        List<ProcessInfo> Q63zyInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q63(status1, order, Q63zyInfoList, wi);
                                        break;
                                    case "ZY1E":
                                        List<ProcessInfo> Q63zy1eInfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q63(status1, order, Q63zy1eInfoList, wi);
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q62;Q63")) {
                                switch (order.getSatelliteName()) {
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ62Q63(order, Q63infoList, wi);
                                        break;
                                    case "CASEARTH":
                                        List<ProcessInfo> Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ62Q63(order, Q63casearthinfoList, wi);
                                        break;
                                    case "ZY-3B":
                                        List<ProcessInfo> Q63zyinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ62Q63(order, Q63zyinfoList, wi);
                                        break;
                                    case "ZY1E":
                                        List<ProcessInfo> Q63zy1einfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ62Q63(order, Q63zy1einfoList, wi);
                                        break;
                                }
                            }
                            if (order.getTaskMode().equals("Q61;Q62;Q63")) {
                                String status1 = "";
                                List<ProcessInfo> Q61infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "KJ125_R0_TO_R0REPORT");
                                if (Q61infoList.size() != 0) {
                                    status1 = ReportUtil.getProcessStatus(Q61infoList, order);
                                }
                                switch (order.getSatelliteName()) {
                                    case "GF-1B":
                                    case "GF-1C":
                                    case "GF-1D":
                                        List<ProcessInfo> Q63infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q62Q63(status1, order, Q63infoList, wi);
                                        break;
                                    case "CASEARTH":
                                        List<ProcessInfo> Q63casearthinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q62Q63(status1, order, Q63casearthinfoList, wi);
                                        break;
                                    case "ZY-3B":
                                        List<ProcessInfo> Q63ZYinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q62Q63(status1, order, Q63ZYinfoList, wi);
                                        break;
                                    case "ZY1E":
                                        List<ProcessInfo> Q63ZY1EinfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_Q63_CAT_TO_L2A");
                                        ReportUtil.ReportStatusQ61Q62Q63(status1, order, Q63ZY1EinfoList, wi);
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
                                ReportUtil.ReportStatusQ64(Q64info, order, wi);
                            }
                            if (order.getTaskMode().equals("Q65")) {
                                dataInfoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                                ReportUtil.ReportStatusQ65(dataInfoList, order, wi);
                            }
                            break;
                        case "DATask":
                            switch (order.getSatelliteName()) {
                                case "GF-1B":
                                case "GF-1C":
                                case "GF-1D":
                                    String status1 = "";
                                    List<ProcessInfo> gfL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_R0_TO_L0");
                                    if (gfL0InfoList.size() != 0) {
                                        status1 = ReportUtil.getProcessStatus(gfL0InfoList, order);
                                    }
                                    if (status1.equals("success")) {
                                        List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "GF1_L0_TO_CAT");
                                        if (infoList.size() != 0) {
                                            String[] sensorStr = {"PAN1", "PAN2", "MSS1", "MSS2"};
                                            ReportUtil.modifyTask(infoList, order, sensorStr, node);              //node为10.5.6.*
                                        }
                                    }
                                    break;
                                case "CASEARTH":
                                    String status2 = "";
                                    List<ProcessInfo> casearthL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_R0_TO_L0");
                                    if (casearthL0InfoList.size() != 0) {
                                        status2 = ReportUtil.getProcessStatus(casearthL0InfoList, order);
                                    }
                                    if (status2.equals("success")) {
                                        List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "CASEARTH_L0_TO_CAT");
                                        if (infoList.size() != 0) {
                                            String[] sensorStr = {"PAN1", "PAN2", "MSS1", "MSS2"};
                                            ReportUtil.modifyTask(infoList, order, sensorStr, node);
                                        }
                                    }
                                    break;
                                case "ZY-3B":
                                    String ZYstatus1 = "";
                                    List<ProcessInfo> zyL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_R0_TO_L0");
                                    if (zyL0InfoList.size() != 0) {
                                        ZYstatus1 = ReportUtil.getProcessStatus(zyL0InfoList, order);
                                    }
                                    if (ZYstatus1.equals("success")) {
                                        List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY3_L0_TO_CAT");
                                        String[] sensorStr = {"MUX", "TLC"};
                                        ReportUtil.modifyTask(infoList, order, sensorStr, node);
                                    }
                                    break;
                                case "ZY1E":
                                    String ZY1EStatus = "";
                                    List<ProcessInfo> ZY1EL0InfoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_R0_TO_L0");
                                    if (ZY1EL0InfoList.size() != 0) {
                                        ZY1EStatus = ReportUtil.getProcessStatus(ZY1EL0InfoList, order);
                                    }
                                    if (ZY1EStatus.equals("success")) {
                                        List<ProcessInfo> infoList = processInfoService.getProcessList(order.getTaskSerialNumber(), "ZY1E_L0_TO_CAT");
                                        String[] sensorStr = {"MSS", "PAN"};
                                        ReportUtil.modifyTask(infoList, order, sensorStr, node);
                                    }
                                    break;
                            }
                            break;
                        case "PRTask":
                            //todo 可能一级生产，也可能是二级生产
                            List<ProcessInfo> infoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                            //todo 压缩的时候添加个线程
                            ExecutorService threadPool = Executors.newSingleThreadExecutor();
                            if (infoList.size() != 0) {
                                if (infoList.get(0).getStatus().equals("Completed")) {
                                    order.setOrderStatus("3");
                                    order.setEndTime(DateUtil.getTime());
                                    orderService.updateById(order);
                                    //todo 修改成功后，休息两秒进行压缩
                                    Thread.sleep(2000);
                                    //todo 产品压缩方法 toZip
                                    //todo 先判断是否为空
                                    //todo Move成功后，先获取产品表里面数据，然后进行存储
                                    if (order.getOut_productdir() != null && !order.getOut_productdir().equals("")) {
                                        logger.info("进行压缩的节点为" + node);


                                        String targetDir = order.getOut_productdir().split(";")[0];
                                        //todo 先创建目录
                                        MyHelper.CreateDirectory(new File(targetDir));

                                        //todo 如果是做的二级产品则dir是二级产品的本地路径，否则是一级产品的路径
                                        String dir = order.getOut_productdir().split(";")[1];

                                        String dirName = new File(dir).getName();
                                        String zipDir = dir + ".zip";
                                        FileOutputStream fos1 = new FileOutputStream(new File(zipDir));
                                        if (order.getProductLevel().equals("L1")) {
                                            //todo 输入输出流压缩文件
                                            ReportUtil.toZip1(dir, fos1, true);
                                            File zipDir1 = new File(zipDir);
                                            if (zipDir1.length() == 0 || !zipDir1.isFile()) {
                                                logger.info("1级产品压缩未成功，再执行一遍");
                                                ReportUtil.toZip1(dir, fos1, true);
                                            } else {
                                                logger.info("1级产品压缩成功");
                                            }
                                            //todo 使用脚本的方式进行压缩文件
                                            Thread.sleep(10000);
                                            String script = "cp -rf " + zipDir + " " + targetDir;
                                            logger.info("打印下移动命令" + script);
                                            ReportUtil.execShellscript(script, node);
                                            //todo 测试复制命令
                                            String script1 = "cp -rf " + zipDir + "  /ncsfs/product/";
                                            logger.info("打印下移动命令" + script1);
                                            ReportUtil.execShellscript(script1, node);
                                            //todo du显示文件所占磁盘大小
                                            String checkScript = "du -s " + targetDir + "/" + dirName + ".zip";
                                            logger.info("打印下check命令" + checkScript);
                                            String result = ReportUtil.execShellscript(checkScript, node);
                                            if (result != null) {
                                                logger.info("Move成功");
                                            } else if (result == null) {
                                                logger.info("Move失败,重新Move");
                                                for (int k = 0; k < Config.move_number; k++) {
                                                    ReportUtil.execShellscript(script, node);
                                                    String result1 = ReportUtil.execShellscript(checkScript, node);
                                                    if (result != null) {
                                                        break;
                                                    } else {
                                                        k++;
                                                    }
                                                }
                                            }
                                            //todo move成功后，更新数据库表
                                            L1Product l1 = infoImpl.getL1product(order.getSceneID());
                                            oracleInfoImpl.delL1Product(order.getSceneID());
                                            oracleInfoImpl.insertL1product(l1);
                                        } else if (order.getProductLevel().equals("L2")) {
                                            //todo 二级产品是否要连一级产品也打包？
                                            String L1Dir = dir.replaceAll("L2DATA", "L1DATA").replaceAll("L2", "L1");
                                            String L1DirZip = L1Dir + ".zip";
                                            FileOutputStream fos2 = new FileOutputStream(new File(L1DirZip));
                                            logger.info("二级产品开始压缩");
                                            //todo 两种方式可以随意切换，到底用哪个呢，使用Java自带
                                            ReportUtil.toZip1(dir, fos1, true);
                                            File zipDir1 = new File(zipDir);
                                            if (zipDir1.length() == 0 || !zipDir1.isFile()) {
                                                logger.info("二级产品压缩未成功，重新压缩");
                                                ReportUtil.toZip1(dir, fos1, true);
                                            } else {
                                                logger.info("压缩成功");
                                            }
                                            logger.info("一级产品压缩开始");
                                            //threadPool.execute();
                                            ReportUtil.toZip1(L1Dir, fos2, true);
                                            File L1dirZip1 = new File(L1DirZip);
                                            if (L1dirZip1.length() == 0 || !L1dirZip1.isFile()) {
                                                logger.info("1级产品压缩未成功，重新压缩");
                                                ReportUtil.toZip1(L1Dir, fos2, true);
                                            } else {
                                                logger.info("一级产品压缩成功");
                                            }

                                            //todo 造一个L1的地址
                                            String targetL1Dir = targetDir.replaceAll("L2", "L1");
                                            MyHelper.CreateDirectory(new File(targetL1Dir));
                                            Thread.sleep(10000);
                                            //todo mv 改为cp -rf
                                            String L1script = "cp -rf " + L1DirZip + " " + targetL1Dir;
                                            logger.info("打印下1级的产品移动命令" + L1script);
                                            ReportUtil.execShellscript(L1script, node);
                                            String script = "cp -rf " + zipDir + " " + targetDir;
                                            logger.info("打印下2级的产品移动命令" + script);
                                            ReportUtil.execShellscript(script, node);
                                            String checkScript = "du -s " + targetDir + "/" + dirName + ".zip";
                                            logger.info("打印下check命令" + checkScript);
                                            String result = ReportUtil.execShellscript(checkScript, node);
                                            if (result != null) {
                                                logger.info("Move成功");
                                            } else if (result == null) {
                                                logger.info("Move失败,重新Move");
                                                for (int k = 0; k < Config.move_number; k++) {
                                                    ReportUtil.execShellscript(script, node);
                                                    String result1 = ReportUtil.execShellscript(checkScript, node);
                                                    if (result1 != null) {
                                                        break;
                                                    } else {
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
                                } else if (infoList.get(0).getStatus().equals("Aborted")) {
                                    order.setOrderStatus("4");
                                    order.setEndTime(DateUtil.getTime());
                                    orderService.updateById(order);
                                } else if (infoList.get(0).getStatus().equals("Running")) {
                                    logger.info("PRTask is running");
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    logger.info(e);
                }
            }
        });
    }

}