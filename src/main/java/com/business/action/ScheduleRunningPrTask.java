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
import com.business.util.CheckStatusUtil;
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
import java.util.Random;

@Component
@Configuration
@EnableScheduling   //打开quartz定时器总开关
public class ScheduleRunningPrTask {
    private static final Logger logger = Logger.getLogger(ScheduleRunningPrTask.class);
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
    private CheckStatusUtil checkStatusUtil;

    //添加定时任务
    @Scheduled(cron = "0/3 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public synchronized void configureTasks()  {
        try {
            List<WorkflowOrder> productList = new ArrayList<>();
            productList = orderService.selectProductList("2");
            logger.info(DateUtil.getTime()+"当前执行的产品任务数量为："+productList.size());
            if (productList.size()!=0){
                for (int i =0;i<productList.size();i++){
                    WorkflowOrder order = productList.get(i);
                    order.setEndTime(DateUtil.getTime());
                    //todo 可能一级生产，也可能是二级生产
                    List<ProcessInfo> infoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                    if (infoList.size() != 0) {
                        if (infoList.get(0).getStatus().equals("Completed")) {
                            order.setOrderStatus("3");
                            order.setEndTime(DateUtil.getTime());
                            orderService.updateById(order);
                            //todo 产品压缩方法 toZip
                            //todo 先判断是否为空
                            //todo Move成功后，先获取产品表里面数据，然后进行存储
                            if (order.getOut_productdir() != null && !"".equals(order.getOut_productdir())) {
                                List<ProductUnzipConfig> productUnzipConfigList = productConfigManagerService.findByStatus("0");
                                Random random = new Random();
                                int number = random.nextInt(10);
                                ProductUnzipConfig unzipNode = productUnzipConfigList.get(number);
                                if (order.getProductLevel().equals("L1")) {
                                    String script = Config.product_compress +" "+order.getOut_productdir();
                                    checkStatusUtil.execShellscript(script, unzipNode.getIp());
                                    //todo move成功后，更新Oracle数据库表
                                    NomalProduct l1 = nomalManagerService.getL1product(order.getSceneID());
                                    oracleInfoImpl.delL1Product(order.getSceneID());
                                    oracleInfoImpl.insertL1product(l1);
                                } else if (order.getProductLevel().equals("L2")) {
                                    logger.info("二级产品开始压缩");
                                    //todo 执行L1
                                    String L1SourceDir = order.getOut_productdir().split(";")[1].replaceAll("L2DATA", "L1DATA").replaceAll("L2", "L1");
                                    String targetL1Dir = order.getOut_productdir().split(";")[0].replaceAll("L2", "L1");
                                    MyHelper.CreateDirectory(new File(targetL1Dir));
                                    String script1 = Config.product_compress +" "+targetL1Dir+";"+L1SourceDir;
                                    checkStatusUtil.execShellscript(script1, unzipNode.getIp());
                                    //todo move成功后，更新数据库表
                                    //todo 执行L2
                                    String script2 = Config.product_compress +" "+order.getOut_productdir();
                                    checkStatusUtil.execShellscript(script2, unzipNode.getIp());
                                    //todo 修改数据库
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
                }
            }
        }catch (Exception e){
            logger.error("更新产品任务失败："+e.getMessage());
        }
    }
    private void checkUnzip(String zipDir,String targetDir,String dir,FileOutputStream fos1,WorkflowOrder order,ProductUnzipConfig unzipNode)throws Exception{
        String dirName = new File(dir).getName();
        File zipDir1 = new File(zipDir);
        //todo 小于10M 说明压缩未成功，重新压缩一遍
        if (zipDir1.length() < 10240 || !zipDir1.isFile()) {
            logger.info( order.getProductLevel()+"级产品压缩未成功，再执行一遍");
            checkStatusUtil.toZip1(dir, fos1, true);
        } else {
            logger.info(order.getProductLevel()+"级产品压缩成功");
        }
        //todo 使用脚本的方式进行压缩文件
        String script = "cp -rf " + zipDir + " " + targetDir;
        logger.info("打印下移动命令" + script);
        checkStatusUtil.execShellscript(script, unzipNode.getIp());
        //todo 测试复制命令
        String script1 = "cp -rf " + zipDir + "  /ncsfs/product/";
        logger.info("打印下移动命令" + script1);
        checkStatusUtil.execShellscript(script1, unzipNode.getIp());
        //todo du显示文件所占磁盘大小
        String checkScript = "du -s " + targetDir + "/" + dirName + ".zip";
        logger.info("打印下check命令" + checkScript);
        String result = checkStatusUtil.execShellscript(checkScript, unzipNode.getIp());
        if (result != null) {
            logger.info("Move成功");
        } else if (result == null) {
            logger.info("Move失败,重新Move");
            for (int k = 0; k < Config.move_number; k++) {
                checkStatusUtil.execShellscript(script, unzipNode.getIp());
                String result1 = checkStatusUtil.execShellscript(checkScript, unzipNode.getIp());
                if (result1 != null) {
                    break;
                } else {
                    k++;
                }
            }
        }
    }
}

