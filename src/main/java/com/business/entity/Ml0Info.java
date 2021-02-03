package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "gt_m_l0")
public class Ml0Info {

    @TableField(value = "signalid")
    public String signalid;
    @TableField(value = "segmentid")
    public String segmentid;
    @TableField(value = "sensorid")
    public String sensor;
    @TableField(value = "datastarttime")
    public String dataStarttime;
    @TableField(value = "dataendtime")
    public String dataEndtime;
    @TableField(value = "jobtaskid")
    private String jobtaskid;
    @TableField(value = "satelliteid")
    private String satelliteid;

    private String filepath;
    private String STORAGE_STATUS;
    private String ORBIT;

    public String getSegmentid() {
        return segmentid;
    }

    public void setSegmentid(String segmentid) {
        this.segmentid = segmentid;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getDataStarttime() {
        return dataStarttime;
    }

    public void setDataStarttime(String dataStarttime) {
        this.dataStarttime = dataStarttime;
    }

    public String getDataEndtime() {
        return dataEndtime;
    }

    public void setDataEndtime(String dataEndtime) {
        this.dataEndtime = dataEndtime;
    }

    public String getJobtaskid() {
        return jobtaskid;
    }

    public void setJobtaskid(String jobtaskid) {
        this.jobtaskid = jobtaskid;
    }

    public String getSatelliteid() {
        return satelliteid;
    }

    public void setSatelliteid(String satelliteid) {
        this.satelliteid = satelliteid;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getSTORAGE_STATUS() {
        return STORAGE_STATUS;
    }

    public String getSignalid() {
        return signalid;
    }

    public void setSignalid(String signalid) {
        this.signalid = signalid;
    }

    public void setSTORAGE_STATUS(String STORAGE_STATUS) {
        this.STORAGE_STATUS = STORAGE_STATUS;
    }

    public String getORBIT() {
        return ORBIT;
    }

    public void setORBIT(String ORBIT) {
        this.ORBIT = ORBIT;
    }

    @Override
    public String toString() {
        return "L0Info{" +
                "segmentID=" + segmentid +
                ", sensor=" + sensor +
                ", dataStartTime=" + dataStarttime +
                ", dataEndTime=" + dataEndtime +
                '}';
    }
}
