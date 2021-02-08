package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 *  * Description: Linux版解压缩队列管理
 *  * <p>
 *  * Created by w_kiven on 2020/12/3 11:17
 */
@TableName("linux_unzip_manager")
public class LinuxUnzipManager {
    @TableId(value = "id")
    private int id;
    @TableField("taskSerialnumber")
    private String taskSerialNumber;
    @TableField("prioprity")
    private String prioprity;
    @TableField("atelliteName")
    private String atelliteName;
    @TableField("exshellScript")
    private String exshellScript;
    @TableField("runningNode")
    private String runningNode;
    @TableField("createTime")
    private String createTime;
    @TableField("beginTime")
    private String beginTime;
    @TableField("endTime")
    private String endTime;
    @TableField("status")
     private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskSerialNumber() {
        return taskSerialNumber;
    }

    public void setTaskSerialNumber(String taskSerialNumber) {
        this.taskSerialNumber = taskSerialNumber;
    }

    public String getPrioprity() {
        return prioprity;
    }

    public void setPrioprity(String prioprity) {
        this.prioprity = prioprity;
    }

    public String getAtelliteName() {
        return atelliteName;
    }

    public void setAtelliteName(String atelliteName) {
        this.atelliteName = atelliteName;
    }

    public String getExshellScript() {
        return exshellScript;
    }

    public void setExshellScript(String exshellScript) {
        this.exshellScript = exshellScript;
    }

    public String getRunningNode() {
        return runningNode;
    }

    public void setRunningNode(String runningNode) {
        this.runningNode = runningNode;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
