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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @Scheduled(cron = "0/2 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public synchronized void configureTasks()  {
        try {
            List<WorkflowOrder> productList = new ArrayList<>();
            productList = orderService.selectProductList("2");
           // logger.info(DateUtil.getTime()+"当前执行的产品任务数量为："+productList.size());
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
                            if (order.getOutProductdir() != null && !"".equals(order.getOutProductdir())) {
                                //todo 创建线程池来执行脚本
                                ExecutorService threadPool = Executors.newCachedThreadPool();
                                List<ProductUnzipConfig> productUnzipConfigList = productConfigManagerService.findByStatus("0");
                                Random random = new Random();
                                int number = random.nextInt(15);
                                ProductUnzipConfig unzipNode = productUnzipConfigList.get(number);
                                if (order.getProductLevel().equals("L1")) {
                                    String outDir = order.getOutProductdir().replace(";",",");
                                    String script = Config.product_compress +" "+outDir;
                                    logger.info("将在IP:"+unzipNode.getIp()+"上执行"+order.getProductLevel()+"的产品压缩"+script);
                                    //todo 线程池压缩
                                    threadPool.execute(compressPRStart(script,unzipNode.getIp()));
                                    //checkStatusUtil.execShellscript(script, unzipNode.getIp());
                                    //todo move成功后，更新Oracle数据库表
                                    NomalProduct l1 = nomalManagerService.getL1product(order.getSceneID());
                                    oracleInfoImpl.delL1Product(order.getSceneID());
                                    oracleInfoImpl.insertL1product(l1);
                                } else if (order.getProductLevel().equals("L2")) {
                                    logger.info("二级产品开始压缩");
                                    //todo 执行L1
                                    String L1SourceDir = order.getOutProductdir().split(";")[1].replaceAll("L2DATA", "L1DATA").replaceAll("L2", "L1");
                                    String targetL1Dir = order.getOutProductdir().split(";")[0].replaceAll("L2", "L1");
                                    MyHelper.CreateDirectory(new File(targetL1Dir));
                                    String script1 = Config.product_compress +" "+targetL1Dir+","+L1SourceDir;
                                    logger.info("将在IP:"+unzipNode.getIp()+"上执行"+order.getProductLevel()+"的产品压缩"+script1);
                                    //todo 线程池压缩
                                    threadPool.execute(compressPRStart(script1,unzipNode.getIp()));
                                    //checkStatusUtil.execShellscript(script1, unzipNode.getIp());
                                    //todo move成功后，更新数据库表
                                    //todo 执行L2
                                    String outDir = order.getOutProductdir().replace(";",",");
                                    String script2 = Config.product_compress +" "+outDir;
                                    logger.info("将在IP:"+unzipNode.getIp()+"上执行"+order.getProductLevel()+"的产品压缩"+script2);
                                    //todo 线程池压缩
                                    threadPool.execute(compressPRStart(script2,unzipNode.getIp()));
                                    //checkStatusUtil.execShellscript(script2, unzipNode.getIp());
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
                        }

                    }
                }
            }
        }catch (Exception e){
            logger.error("更新产品任务失败："+e.getMessage());
        }
    }
    private Thread compressPRStart(String cmd,String nodeIp)throws Exception{
        return new Thread(new Runnable() {
            @Override
            public void run() {
                checkStatusUtil.execShellscript(cmd,nodeIp);
            }
        });
    }
   }

