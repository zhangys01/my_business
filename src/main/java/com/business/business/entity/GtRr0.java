package com.business.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "gt_r_r0")
public class GtRr0 {
    @TableId(value = "id",type = IdType.AUTO)
    private String id;
    private String segmentid;
    private String signalid;
    private String jobtaskid;
    private String satelliteid;
    private String sensorid;
    private String filepath;
    private String notecreatetime;
    private String filecreatetime;
    private String xmltypedata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSegmentid() {
        return segmentid;
    }

    public void setSegmentid(String segmentid) {
        this.segmentid = segmentid;
    }

    public String getSignalid() {
        return signalid;
    }

    public void setSignalid(String signalid) {
        this.signalid = signalid;
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

    public String getSensorid() {
        return sensorid;
    }

    public void setSensorid(String sensorid) {
        this.sensorid = sensorid;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
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

    public String getXmltypedata() {
        return xmltypedata;
    }

    public void setXmltypedata(String xmltypedata) {
        this.xmltypedata = xmltypedata;
    }
}
