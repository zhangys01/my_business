package com.business;

import com.business.action.ScheduleRunningPrTask;
import com.business.action.ScheduleRunningDaTask;
import com.business.action.ScheduleRunningQaTask;
import com.business.action.ScheduleHoldTask;
import com.business.config.Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
@MapperScan(basePackages = {"com.business.dao"})
public class BusinessApplication implements CommandLineRunner {
    @Resource
    ScheduleHoldTask holdTask;
    @Resource
    ScheduleRunningPrTask productTask;
    @Resource
    ScheduleRunningDaTask runningDaTask;
    @Resource
    ScheduleRunningQaTask runningQaTask;

    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        Config.loadConfig();
        holdTask.configureTasks();
        productTask.configureTasks();
        runningDaTask.configureTasks();
        runningQaTask.configureTasks();
    }
}
