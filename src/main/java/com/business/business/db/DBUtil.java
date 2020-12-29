package com.business.business.db;

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
public class DBUtil {

    public static DruidDataSource dataSource;

    //1.初始化Druid连接池
    static {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("BusinessControl.properties"));
            dataSource = new DruidDataSource();
            //四个基本属性
            dataSource.setDriverClassName(properties.getProperty("db_driver"));
            dataSource.setUrl(properties.getProperty("db_url"));
            dataSource.setUsername(properties.getProperty("db_user"));
            dataSource.setPassword(properties.getProperty("db_password"));
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

    /*private static Connection conn = null;
    static
    {
        try
        {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("BusinessControl.properties"));
            String db_url = properties.getProperty("db_url");
            String db_user = properties.getProperty("db_user");
            String db_password =  properties.getProperty("db_password");
            // 1.加载驱动程序
            Class.forName(properties.getProperty("db_driver"));
            // 2.获得数据库的连接
            conn = DriverManager.getConnection(db_url, db_user, db_password);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection()
    {
        return conn;
    }*/
}
