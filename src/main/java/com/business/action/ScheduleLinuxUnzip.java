package com.business.action;


import com.business.Service.LinuxUnzipManagerService;
import com.business.Service.LinuxUnzipNodeService;
import com.business.entity.LinuxUnzipManager;
import com.business.entity.LinuxUnzipNodes;
import com.business.util.CheckStatusUtil;
import com.business.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Configuration
@EnableScheduling   //打开quartz定时器总开关
public class ScheduleLinuxUnzip {
    private static final Logger logger = Logger.getLogger(ScheduleLinuxUnzip.class);
    @Autowired
    private LinuxUnzipManagerService linuxUnzipManagerService;
    @Autowired
    private LinuxUnzipNodeService linuxUnzipNodeService;
    @Resource
    private CheckStatusUtil checkStatusUtil;
    //添加定时任务
    @Scheduled(cron = "0/10 * * * * ?")   //第10秒钟触发，每10秒中触发一次
    public synchronized void queueUnzipTasks() throws Exception {
        try {
            logger.info("开始查询解压缩等待队列");
            List<LinuxUnzipManager> queueList = linuxUnzipManagerService.selectQueueList();
            if (queueList.size()!=0){
                logger.info("当前解压缩等待队列数为"+queueList.size());
                List<LinuxUnzipNodes>nodesList = new ArrayList<>();
                nodesList  = linuxUnzipNodeService.selectIpnodes("0");
                if (nodesList.size()!=0){
                    List<LinuxUnzipManager> runUnzip = linuxUnzipManagerService.selectByTaskId(queueList.get(0).getTaskSerialNumber());
                    //todo 创建线程池来执行脚本
                    ExecutorService threadPool = Executors.newScheduledThreadPool(2);
                    for (int i=0;i<runUnzip.size();i++){
                        LinuxUnzipManager unzipManager = runUnzip.get(i);
                        LinuxUnzipNodes node = nodesList.get(i);
                        node.setNodeStatus("1");
                        linuxUnzipNodeService.updateById(node);
                        logger.info("当前可以解压缩的节点为"+node.getNodeIp());
                        //todo 置为执行状态
                        unzipManager.setBeginTime(DateUtil.getTime());
                        unzipManager.setStatus(1);
                        unzipManager.setRunningNode(node.getNodeIp());
                        linuxUnzipManagerService.updateById(unzipManager);
                        //todo 开始执行解压缩
                        logger.info("解压缩命令为"+unzipManager.getExshellScript());
                        threadPool.execute(unzipStart(unzipManager.getExshellScript(),node.getNodeIp()));
                        logger.info("执行成功");
                    }
                }
                logger.info("当前解压缩可用节点数量为"+nodesList.size());

            }else {
                logger.info("暂时没有解压缩等待执行");
            }
        }catch (Exception e){
            logger.error("执行解压缩遭遇异常为"+e.getMessage());
        }
    }
    private Thread unzipStart(String cmd,String nodeIp)throws Exception{
        return new Thread(new Runnable() {
            @Override
            public void run() {
                checkStatusUtil.execShellscript(cmd,nodeIp);
            }
        });
    }
}
