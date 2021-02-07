package com.business.action;


import com.business.Service.LinuxUnzipManagerService;
import com.business.entity.LinuxUnzipManager;
import com.business.entity.LinuxUnzipNodes;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
@EnableScheduling   //打开quartz定时器总开关
public class ScheduleLinuxUnzip {
    private static final Logger logger = Logger.getLogger(ScheduleLinuxUnzip.class);
    @Autowired
    private LinuxUnzipManagerService linuxUnzipManagerService;
    //添加定时任务
    @Scheduled(cron = "0/10 * * * * ?")   //第0秒钟触发，每5秒中触发一次
    public synchronized void queueUnzipTasks() throws Exception {
        logger.info("开始查询解压缩等待队列");
        List<LinuxUnzipManager> queueList = linuxUnzipManagerService.selectQueueList();
        if (queueList.size()!=0){
            logger.info("当前解压缩等待队列数为"+queueList.size());
            //List<LinuxUnzipNodes>nodesList
         }else {
            logger.info("暂时没有解压缩等待执行");
        }

    }
}
