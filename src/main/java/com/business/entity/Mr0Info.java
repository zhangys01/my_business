package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField(value = "satelliteid")
    public String satelliteid;  //卫星简称
    public String channelID;
    public String receiveStartTime;
    public String receiveEndTime;
    @TableField(value = "filepath")
    public String metaFilePath;   //注意，数据库中记录的是相对路径
    public String notecreatetime;
    private String filecreatetime;

    public String getJobtaskid() {
        return jobtaskid;
    }

    public void setJobtaskid(String jobtaskid) {
        this.jobtaskid = jobtaskid;
    }

    public String getSignalID() {
        return signalID;
    }

    public void setSignalID(String signalID) {
        this.signalID = signalID;
    }


    public String getSatelliteid() {
        return satelliteid;
    }

    public void setSatelliteid(String satelliteid) {
        this.satelliteid = satelliteid;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getReceiveStartTime() {
        return receiveStartTime;
    }

    public void setReceiveStartTime(String receiveStartTime) {
        this.receiveStartTime = receiveStartTime;
    }

    public String getReceiveEndTime() {
        return receiveEndTime;
    }

    public void setReceiveEndTime(String receiveEndTime) {
        this.receiveEndTime = receiveEndTime;
    }

    public String getMetaFilePath() {
        return metaFilePath;
    }

    public void setMetaFilePath(String metaFilePath) {
        this.metaFilePath = metaFilePath;
    }

    public String getNotecreatetime() {
        return notecreatetime;
    }

    public void setNotecreatetime(String notecreatetime) {
        this.notecreatetime = notecreatetime;
    }

    public String getFilecreatetime() {
        return filecreatetime;
    }

    public void setFilecreatetime(String filecreatetime) {
        this.filecreatetime = filecreatetime;
    }
}
