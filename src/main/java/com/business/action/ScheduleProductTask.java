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
    @Scheduled(cron = "0/3 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public synchronized void configureTasks()  {
        try {
            System.out.println(DateUtil.getTime()+"开始查询产品任务");
            List<WorkflowOrder> productList = new ArrayList<>();
            productList = orderService.selectProductList("2");
            logger.info("productList的长度"+productList.size());
            if (productList.size()!=0){
                WorkflowOrder order = productList.get(0);
                order.setEndTime(DateUtil.getTime());
                    //todo 可能一级生产，也可能是二级生产
                    List<ProcessInfo> infoList = processInfoService.selectProcess(order.getTaskSerialNumber());
                    //todo 如果输出文件夹为空， 则不需要压缩，则不用查unzipNode
                        ProductUnzipConfig unzipNode = new ProductUnzipConfig();
                    if (order.getOut_productdir()==null){
                        logger.info("infoList的长度"+infoList.size());
                        unzipNode = productConfigManagerService.findByStatus("0");
                        int j=0;
                        do {
                            logger.info(unzipNode+"压缩node");
                            if (unzipNode==null){
                                //Thread.sleep(1000);
                                unzipNode = productConfigManagerService.findByStatus("0");
                                j++;
                            }
                        }while (unzipNode==null);
                        unzipNode.setIs_unzip("1");
                        productConfigManagerService.updateById(unzipNode);
                    }
                    if (infoList.size() != 0) {
                        if (infoList.get(0).getStatus().equals("Completed")) {
                            order.setOrderStatus("3");
                            order.setEndTime(DateUtil.getTime());
                            orderService.updateById(order);
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
                                String zipDir = dir + ".zip";
                                FileOutputStream fos1 = new FileOutputStream(new File(zipDir));
                                //todo L1级别
                                if (order.getProductLevel().equals("L1")) {
                                    //todo 输入输出流压缩文件
                                    reportUtil.toZip1(dir, fos1, true);
                                    //todo 开始检验压缩是否成功
                                     checkUnzip(zipDir,targetDir,dir,fos1,order,unzipNode);
                                    //todo 压缩成功后，更改节点状态
                                    unzipNode.setIs_unzip("0");
                                    productConfigManagerService.updateById(unzipNode);
                                    //todo move成功后，更新Oracle数据库表
                                    NomalProduct l1 = nomalManagerService.getL1product(order.getSceneID());
                                    oracleInfoImpl.delL1Product(order.getSceneID());
                                    oracleInfoImpl.insertL1product(l1);
                                } else if (order.getProductLevel().equals("L2")) {
                                    logger.info("二级产品开始压缩");
                                    //todo 输入输出流压缩文件
                                    reportUtil.toZip1(dir, fos1, true);
                                    //todo 检测二级产品是否成功
                                    checkUnzip(zipDir,targetDir,dir,fos1,order,unzipNode);
                                    logger.info("一级产品压缩开始");
                                    String L1Dir = dir.replaceAll("L2DATA", "L1DATA").replaceAll("L2", "L1");
                                    String L1DirZip = L1Dir + ".zip";
                                    FileOutputStream fos2 = new FileOutputStream(new File(L1DirZip));
                                    reportUtil.toZip1(L1Dir, fos2, true);
                                    //todo 造一个L1的地址
                                    String targetL1Dir = targetDir.replaceAll("L2", "L1");
                                    MyHelper.CreateDirectory(new File(targetL1Dir));
                                    //todo 检测压缩是否成功
                                    checkUnzip(L1DirZip,targetL1Dir,L1Dir,fos2,order,unzipNode);
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
            reportUtil.toZip1(dir, fos1, true);
        } else {
            logger.info(order.getProductLevel()+"级产品压缩成功");
        }
        //todo 使用脚本的方式进行压缩文件
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
                if (result1 != null) {
                    break;
                } else {
                    k++;
                }
            }
        }
    }
}

