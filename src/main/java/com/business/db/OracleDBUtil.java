package com.business.db;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 2 * @Author: kiven
 * 3 * @Date: 2018/12/20 11:22
 * 4
 */
//todo 连接Oracle数据库
public class OracleDBUtil {

    public static DruidDataSource dataSource;

    //1.初始化Druid连接池
    static {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application_windows.properties"));
            dataSource = new DruidDataSource();
            //四个基本属性
            dataSource.setDriverClassName(properties.getProperty("oracle_driver"));
            dataSource.setUrl(properties.getProperty("oracle_url"));
            dataSource.setUsername(properties.getProperty("oracle_user"));
            dataSource.setPassword(properties.getProperty("oracle_password"));
            //其他属性
            //初始大小
            dataSource.setInitialSize(Integer.parseInt(properties.getProperty("InitialSize")));
            //最大大小
            dataSource.setMaxActive(Integer.parseInt(properties.getProperty("MaxActive")));
            //最小大小
            dataSource.setMinIdle(Integer.parseInt(properties.getProperty("MinIdle")));
            //检查时间
            dataSource.setMaxWait(Integer.parseInt(properties.getProperty("MaxWait")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //3.关闭连接
    public static void closeAll(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
