package com.business.info;


/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
  //对应查询Workflow_DataArchive及Workflow_Tree表的select语句
public class ArchiveWorkflowInfo {
    /**** 来自Workflow_DataArchive表 *****/
    public String jobTaskID;
    public String orderId;
    public String createTime;  //用任务初始创建时间，不要用关联的流程记录插入时间
    public String dataFile;
    public Integer reply;   //是否回复运管。0：未回复；1：已回复

    /**** 来自Workflow_Tree表 *****/
    public Integer state;    //流程状态。参看enums.ExecutingState
    public String updateTime;  //用关联的流程记录更新时间，不要用任务记录更新时间

    @Override
    public String toString() {
        return "ArchiveWorkflowInfo{" +
                "jobTaskID=" + jobTaskID +
                ", orderId=" + orderId +
                ", dataFile=" + dataFile +
                ", reply=" + reply +
                ", state=" + state +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
