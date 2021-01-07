package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 2 * @Author: zys
 * 3 * @Date: 2020/12/25 15:00
 * 4
 */
@TableName(value = "workflow_qatask")
public class QATaskWorkFlow {
    @TableId(value = "taskid")
    private String taskid;
    private String orderid;
    private String taskinfo;
    private String createtime;
    private String updatetime;
    private String originator;
    private String replyfile;
    private String reply;//0.ready 1.hold 2.running 3.completed 4.aborted，5suspend

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getTaskinfo() {
        return taskinfo;
    }

    public void setTaskinfo(String taskinfo) {
        this.taskinfo = taskinfo;
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

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getReplyfile() {
        return replyfile;
    }

    public void setReplyfile(String replyfile) {
        this.replyfile = replyfile;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
