package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:14
 * To change this template use File | Settings | File Templates.
 */
  //对应查询R0元数据表的select语句
@TableName(value = "gt_m_r0")
public class Mr0Info {
    @TableId(value = "jobtaskid")
    public String jobtaskid;
    public String signalID;
    public String satellite;  //卫星简称
    public String channelID;
    public String receiveStartTime;
    public String receiveEndTime;
    public String metaFilePath;   //注意，数据库中记录的是相对路径

}
