package com.business.business;

import com.business.business.Service.WorkFlowOrderService;
import com.business.business.action.ScheduleHoldTask;
import com.business.business.entity.WorkflowOrder;
import org.apache.log4j.Logger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.util.List;

@SpringBootApplication
@MapperScan(basePackages = {"com.business.business.dao"})
public class BusinessApplication implements CommandLineRunner {
    @Resource ScheduleHoldTask task;
    private static Logger logger = Logger.getLogger(BusinessApplication.class.getClass());
    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        task.configureTasks();
        /*try {
            logger.info("loading config...");
            System.out.println("loading config...");
            PropertyConfigurator.configure(ClassLoader.getSystemResourceAsStream("log4j.properties"));
            Config.loadConfig();
        } catch (Throwable e) {
            logger.error("failed to load config!", e);
            System.out.println("failed to load config!");
            e.printStackTrace();
            System.exit(100);
        }
        //todo 监听数据库workflow_order表，放任务
        try{
            logger.info("initializing Task receiver");
            System.out.println("initializing Task receiver");
            //,Config.fromDTC_temp
            new TaskReceiver();
        }catch (Throwable e){
            System.out.println("failed to init Task receiver");
            e.printStackTrace();
            System.exit(105);
        }*/
    }
}
