package com.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.omg.CORBA.IDLType;

/**
 * 1 *
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/4/15 16:18
 * 4
 */
@TableName(value = "unzip_confirm")
public class UnzipConfirm {
    @TableId(value = "ID",type = IdType.AUTO)
    private int id;
    @TableField(value = "taskId")
    private String taskId;
    @TableField(value = "activityId")
    private String activityId;//原路径
    @TableField(value = "cancelActivityId")
    private String cancelActivityId;//被取消的路径
    @TableField(value = "status")
    private int status;//0-未执行，1-被取消，2-完成

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getCancelActivityId() {
        return cancelActivityId;
    }

    public void setCancelActivityId(String cancelActivityId) {
        this.cancelActivityId = cancelActivityId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
