package com.business.entity;

public class workFlowTree {
    private String orderid;
    private String parentorderid;
    private String createtime;
    private String updatetime;
    private int state;
    private String info;

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getParentorderid() {
        return parentorderid;
    }

    public void setParentorderid(String parentorderid) {
        this.parentorderid = parentorderid;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
