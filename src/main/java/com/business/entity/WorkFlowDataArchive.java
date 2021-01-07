package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "workflow_dataarchive")
public class WorkFlowDataArchive {
    @TableId(value = "jobtaskid")
    private String jobtaskid;
    private String orderid;
    private String createtime;
    private String updatetime;
    private String replyfile;
    private int reply;
    private String datafile;

    public String getJobtaskid() {
        return jobtaskid;
    }

    public void setJobtaskid(String jobtaskid) {
        this.jobtaskid = jobtaskid;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getReplyfile() {
        return replyfile;
    }

    public void setReplyfile(String replyfile) {
        this.replyfile = replyfile;
    }

    public int getReply() {
        return reply;
    }

    public void setReply(int reply) {
        this.reply = reply;
    }

    public String getDatafile() {
        return datafile;
    }

    public void setDatafile(String datafile) {
        this.datafile = datafile;
    }
}
