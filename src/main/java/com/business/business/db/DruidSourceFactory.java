package com.business.business.db;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;

/**
 * @author w_kiven
 * @title: DruidSourceFactory
 * @projectName BusinessControl125
 * @description: TODO
 * @date 2019/12/3114:51
 */
public class DruidSourceFactory extends PooledDataSourceFactory {
    public DruidSourceFactory() {
        this.dataSource = new DruidDataSource();
    }
}
