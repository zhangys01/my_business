package com.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 2 * @Author: kiven
 * 3 * @Date: 2019/1/17 14:08
 * 4
 */
@TableName(value = "gt_m_cat")
public class Mcat {
    @TableId(value = "id",type = IdType.AUTO)
    private int id;
    private String sceneid;
    private String segmentid;
    private String jobtaskid;
    private String satelliteid;
    private String sensorid;
    private String band;
    private String scenestarttime;
    private String sceneendtime;
    private String filepath;
    private String notecreatetime;
    private String filecreatetime;
    private int l1acounter;
    private int l2acounter;
    private String station;
    private String content;
    private String stauts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSceneid() {
        return sceneid;
    }

    public void setSceneid(String sceneid) {
        this.sceneid = sceneid;
    }

    public String getSegmentid() {
        return segmentid;
    }

    public void setSegmentid(String segmentid) {
        this.segmentid = segmentid;
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

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String getScenestarttime() {
        return scenestarttime;
    }

    public void setScenestarttime(String scenestarttime) {
        this.scenestarttime = scenestarttime;
    }

    public String getSceneendtime() {
        return sceneendtime;
    }

    public void setSceneendtime(String sceneendtime) {
        this.sceneendtime = sceneendtime;
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

    public String getStauts() {
        return stauts;
    }

    public void setStauts(String stauts) {
        this.stauts = stauts;
    }

    public int getL1acounter() {
        return l1acounter;
    }

    public void setL1acounter(int l1acounter) {
        this.l1acounter = l1acounter;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getL2acounter() {
        return l2acounter;
    }

    public void setL2acounter(int l2acounter) {
        this.l2acounter = l2acounter;
    }
}
