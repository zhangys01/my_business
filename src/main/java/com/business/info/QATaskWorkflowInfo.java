package com.business.info;


/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
  //对应查询Workflow_QATask及Workflow_Tree表的select语句
public class QATaskWorkflowInfo {
    /**** 来自Workflow_QATask表 *****/
    public String taskId;    //运管触发时为任务单流水号(taskSerialNumber)；常规流程自动触发时为作业任务编号(jobTaskID)，以便归档任务能够与之关联获取报表
    public String orderId;     //对应的主流程ID
    public String createTime;      //用任务初始创建时间，不要用关联的流程记录插入时间
    public Integer originator;  //任务发起者。0：常规流程自动触发；1：运管触发
    public String replyFile;   //生成的运管质量监测任务完成通知文件名，为null说明还未生成。生成了完成通知，并不代表已发给运管，还需看reply字段的状态。（对于常规流程自动触发的任务，始终为null）
    public Integer reply;      //是否回复运管。0：未回复（对于常规流程自动触发的任务总是为0）；1：已回复；2：运管取消（无需再生成完成通知回复运管）

    /**** 来自Workflow_Tree表 *****/
    public Integer state;    //流程状态。参看enums.ExecutingState
    public String updateTime;  //用关联的流程记录更新时间，不要用任务记录更新时间

    /**
     * 来自Workflow_QATask表taskinfo字段中的信息
     * taskinfo格式如下（根据需要可增加项目）：
     * <t>
     *  <satellite>GF01</satellite>    #注意是卫星简称
     *  <taskMode>Q61;Q62;Q63</taskMode>
     *  <jobTaskID>JOB201309230001001</jobTaskID>
     *  <channel>S1;S2</channel>
     *  <sensor>2mCCD/8mCCD</sensor>   #注意，是运管接口定义的传感器标识，不是内部用传感器标识
     *  <dataSelectType>Time</dataSelectType>
     *  <sceneCountQ63>100</sceneCountQ63>    #Q63模式下选择的单景个数
     *  <orbit>7960</orbit>                #Q64模式下的轨道号
     *  <QAReportFile>GF01/REPORT/201312/20131212/QAReport_GF01_QA2013000001_20131212235959/QAReport_GF01_QA2013000001_20131212235959.xls</QAReportFile>  #相对路径
     * </t>
     */
    public String satellite;
    public String taskMode;
    public String jobTaskID;
    public String channel;
    public String sensor ;
    public String dataSelectType ;
    public Integer sceneCountQ63;
    public Integer orbit;
    public String QAReportFile;

    @Override
    public String toString() {
        return "QATaskWorkflowInfo{" +
                "taskId=" + taskId +
                ", orderId=" + orderId +
                ", originator=" + originator +
                ", replyFile=" + replyFile +
                ", reply=" + reply +
                ", state=" + state +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", satellite=" + satellite +
                ", taskMode=" + taskMode +
                ", jobTaskID=" + jobTaskID +
                ", channel=" + channel +
                ", sensor=" + sensor +
                ", dataSelectType=" + dataSelectType +
                ", sceneCountQ63=" + sceneCountQ63 +
                ", orbit=" + orbit +
                ", QAReportFile=" + QAReportFile +
                '}';
    }
}
