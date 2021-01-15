package com.business.action;

import com.business.Service.NomalManagerService;
import com.business.Service.ProcessInfoService;
import com.business.Service.ProductConfigManagerService;
import com.business.Service.WorkFlowOrderService;
import com.business.config.Config;
import com.business.db.OracleProcessInfoImpl;
import com.business.entity.NomalProduct;
import com.business.entity.ProcessInfo;
import com.business.entity.ProductUnzipConfig;
import com.business.entity.WorkflowOrder;
import com.business.util.DateUtil;
import com.business.util.MyHelper;
import com.business.util.ReportUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Configuration
@EnableScheduling   //打开quartz定时器总开关
public class ScheduleProductTask {
    private static final Logger logger = Logger.getLogger(ScheduleProductTask.class);
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Autowired
    private ProductConfigManagerService productConfigManagerService;
    @Autowired
    private NomalManagerService nomalManagerService;
    @Resource
    private OracleProcessInfoImpl oracleInfoImpl;
    @Resource
    private ReportUtil reportUtil;

    //添加定时任务
    @Scheduled(cron = "0/2 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public void configureTasks()  {
        try {
            System.out.println(DateUtil.getTime()+"开始查询产品任务");
            List<WorkflowOrder> productList = new ArrayList<>();
            productList = orderService.selectProductList("2");
            if (productList.size()!=0){
                WorkflowOrder order = productList.get(0);
                order.setEndTime(DateUtil.getTime());
                String orderType = order.getOrderType().split("_")[0];
                switch (orderType){
                    case "PRTask":
                        //todo 可能一级生产，也可能是二级生产
                        List<ProcessInfo> infoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                        ProductUnzipConfig unzipNode = productConfigManagerService.findByStatus("0");
                        int j=0;
                        do {
                            if (unzipNode==null){
                                Thread.sleep(1000);
                                unzipNode = productConfigManagerService.findByStatus("0");
                                j++;
                            }
                        }while (unzipNode==null);
                        unzipNode.setIs_unzip("1");
                        productConfigManagerService.updateById(unzipNode);
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
                                if (order.getOut_productdir() != null && !"".equals(order.getOut_productdir())) {
                                    logger.info("进行压缩的节点为" + unzipNode.getIp());
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
                                        reportUtil.toZip1(dir, fos1, true);
                                        File zipDir1 = new File(zipDir);
                                        if (zipDir1.length() == 0 || !zipDir1.isFile()) {
                                            logger.info("1级产品压缩未成功，再执行一遍");
                                            reportUtil.toZip1(dir, fos1, true);
                                        } else {
                                            logger.info("1级产品压缩成功");
                                        }
                                        //todo 使用脚本的方式进行压缩文件
                                        Thread.sleep(10000);
                                        String script = "cp -rf " + zipDir + " " + targetDir;
                                        logger.info("打印下移动命令" + script);
                                        reportUtil.execShellscript(script, unzipNode.getIp());
                                        //todo 测试复制命令
                                        String script1 = "cp -rf " + zipDir + "  /ncsfs/product/";
                                        logger.info("打印下移动命令" + script1);
                                        reportUtil.execShellscript(script1, unzipNode.getIp());
                                        //todo du显示文件所占磁盘大小
                                        String checkScript = "du -s " + targetDir + "/" + dirName + ".zip";
                                        logger.info("打印下check命令" + checkScript);
                                        String result = reportUtil.execShellscript(checkScript, unzipNode.getIp());
                                        if (result != null) {
                                            logger.info("Move成功");
                                        } else if (result == null) {
                                            logger.info("Move失败,重新Move");
                                            for (int k = 0; k < Config.move_number; k++) {
                                                reportUtil.execShellscript(script, unzipNode.getIp());
                                                String result1 = reportUtil.execShellscript(checkScript, unzipNode.getIp());
                                                if (result != null) {
                                                    break;
                                                } else {
                                                    k++;
                                                }
                                            }
                                        }
                                        //todo 压缩成功后，记得变回之前的
                                        unzipNode.setIs_unzip("0");
                                        productConfigManagerService.updateById(unzipNode);
                                        //todo move成功后，更新数据库表
                                        NomalProduct l1 = nomalManagerService.getL1product(order.getSceneID());
                                        oracleInfoImpl.delL1Product(order.getSceneID());
                                        oracleInfoImpl.insertL1product(l1);
                                    } else if (order.getProductLevel().equals("L2")) {
                                        //todo 二级产品是否要连一级产品也打包？
                                        String L1Dir = dir.replaceAll("L2DATA", "L1DATA").replaceAll("L2", "L1");
                                        String L1DirZip = L1Dir + ".zip";
                                        FileOutputStream fos2 = new FileOutputStream(new File(L1DirZip));
                                        logger.info("二级产品开始压缩");
                                        //todo 两种方式可以随意切换，到底用哪个呢，使用Java自带
                                        reportUtil.toZip1(dir, fos1, true);
                                        File zipDir1 = new File(zipDir);
                                        if (zipDir1.length() == 0 || !zipDir1.isFile()) {
                                            logger.info("二级产品压缩未成功，重新压缩");
                                            reportUtil.toZip1(dir, fos1, true);
                                        } else {
                                            logger.info("压缩成功");
                                        }
                                        logger.info("一级产品压缩开始");
                                        //threadPool.execute();
                                        reportUtil.toZip1(L1Dir, fos2, true);
                                        File L1dirZip1 = new File(L1DirZip);
                                        if (L1dirZip1.length() == 0 || !L1dirZip1.isFile()) {
                                            logger.info("1级产品压缩未成功，重新压缩");
                                            reportUtil.toZip1(L1Dir, fos2, true);
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
                                        reportUtil.execShellscript(L1script, unzipNode.getIp());
                                        String script = "cp -rf " + zipDir + " " + targetDir;
                                        logger.info("打印下2级的产品移动命令" + script);
                                        reportUtil.execShellscript(script, unzipNode.getIp());
                                        String checkScript = "du -s " + targetDir + "/" + dirName + ".zip";
                                        logger.info("打印下check命令" + checkScript);
                                        String result = reportUtil.execShellscript(checkScript, unzipNode.getIp());
                                        if (result != null) {
                                            logger.info("Move成功");
                                        } else if (result == null) {
                                            logger.info("Move失败,重新Move");
                                            for (int k = 0; k < Config.move_number; k++) {
                                                reportUtil.execShellscript(script, unzipNode.getIp());
                                                String result1 = reportUtil.execShellscript(checkScript, unzipNode.getIp());
                                                if (result1 != null) {
                                                    break;
                                                } else {
                                                    k++;
                                                }
                                            }
                                        }
                                        //todo 压缩成功后，记得变回之前的
                                        unzipNode.setIs_unzip("0");
                                        productConfigManagerService.updateById(unzipNode);
                                        //todo move成功后，更新数据库表
                                        NomalProduct l1 = nomalManagerService.getL1product(order.getSceneID());
                                        oracleInfoImpl.delL1Product(order.getSceneID());
                                        oracleInfoImpl.insertL1product(l1);
                                        NomalProduct l2 = nomalManagerService.getL2product(order.getSceneID());
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
            }
        }catch (Exception e){
            logger.error("更新产品任务失败："+e.getMessage());
        }
    }
}

