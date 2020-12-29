package com.business.business.info;


import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
   //对应查询Workflow_Tree表的select语句
public class WorkflowInfo {
    /*public static final int STATE_RUNNING=0;  //正在处理
    public static final int STATE_SUCCESS=1;  //成功
    public static final int STATE_PARTIAL=2;  //部分成功
    public static final int STATE_FAILURE=3;  //失败*/

    public String orderId;
    public Date createTime;
    public Date updateTime;
    public Integer state;  //流程状态。参看enums.ExecutingState
    public String info;    //对于编目流程为sensor；对于生产流程为sceneID；对于关键子流程，看Constants定义

    @Override
    public String toString() {
        return "WorkflowInfo{" +
                "orderId=" + orderId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", info=" + info +
                '}';
    }
}
