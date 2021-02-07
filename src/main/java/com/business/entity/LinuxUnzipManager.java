package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 *  * Description: Linux版解压缩队列管理
 *  * <p>
 *  * Created by w_kiven on 2020/12/3 11:17
 */
@TableName(value = "linux_unzip_manager")
public class LinuxUnzipManager {
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
    @TableField("createTime")
    private String beginTime;
    @TableField("endTimel")
    private String endTimel;
    @TableField("status")
     private int status;

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

    public String getEndTimel() {
        return endTimel;
    }

    public void setEndTimel(String endTimel) {
        this.endTimel = endTimel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
