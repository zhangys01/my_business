package com.business.business;

import com.business.business.action.SchedulRunningTask;
import com.business.business.action.ScheduleHoldTask;
import com.business.business.config.Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.Resource;

@SpringBootApplication
@MapperScan(basePackages = {"com.business.business.dao"})
public class BusinessApplication implements CommandLineRunner {
    @Resource
    ScheduleHoldTask task;
    @Resource
    SchedulRunningTask runningtask;
    @Bean
    public TaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("poolScheduler");
        scheduler.setPoolSize(10);
        return scheduler;
    }
    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        Config.loadConfig();
        task.configureTasks();
        runningtask.configureTasks();//里面并新建了一个线程池
    }
}
