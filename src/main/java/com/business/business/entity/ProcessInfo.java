package com.business.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/2/22 15:00
 * 4
 */
@TableName(value = "pd_processinfo")
public class ProcessInfo {

    @TableId(value = "orderId")
    private String orderId;
    @TableField(value = "processType")
    private String processType;
    @TableField(value = "processName")
    private String processName;
    @TableField(value = "status")
    private String status;
    @TableField(value = "creator")
    private String creator;
    @TableField(value = "platform")
    private String platform;
    @TableField(value = "sensor")
    private String sensor;
    @TableField(value = "priority")
    private int priority;
    @TableField(value = "createTime")
    private String createTime;
    @TableField(value = "endTime")
    private String endTime;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
