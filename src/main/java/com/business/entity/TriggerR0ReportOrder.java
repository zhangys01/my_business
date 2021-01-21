package com.business.entity;

import org.eclipse.persistence.oxm.annotations.XmlNullPolicy;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-8-21
 * Time: 下午12:15
 * To change this template use File | Settings | File Templates.
 */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "task")
public class TriggerR0ReportOrder {
    @XmlPath("@name")
    public String name;

    @XmlPath("@id")
    public String id;        //步骤标识

    @XmlPath("@orderid")
    public String orderid;  //工作流标识

    @XmlPath("params/satellite/text()")
    public String satellite;      //卫星简称

    @XmlPath("params/taskSerialNumber/text()")
    public String taskSerialNumber;

    @XmlPath("params/taskId/text()")
    public String taskId;       //任务单流水号

    @XmlPath("params/jobTaskId1/text()")
    public String jobTaskId1;      //第1个jobTaskID

    @XmlPath("params/jobTaskId2/text()")
    public String jobTaskId2;      //第2个jobTaskID

    @XmlPath("params/job1_S1/text()")
    @XmlNullPolicy(emptyNodeRepresentsNull = true)
    public String job1_S1;        //以下分别为两个jobTaskID对应的两个通道的原始码流文件路径。可能为null

    @XmlPath("params/job2_S1/text()")
    @XmlNullPolicy(emptyNodeRepresentsNull = true)
    public String job2_S1;        //可能为null

    @XmlPath("params/job1_S2/text()")
    @XmlNullPolicy(emptyNodeRepresentsNull = true)
    public String job1_S2;        //可能为null

    @XmlPath("params/job2_S2/text()")
    @XmlNullPolicy(emptyNodeRepresentsNull = true)
    public String job2_S2;        //可能为null

    @XmlPath("params/diffTxt/text()")
    public String diffTxt;        //差异性文本文件路径

    @XmlPath("params/segment/text()")
    public Integer segmentSize;        //分段大小

    @XmlPath("params/server_address/text()")
    public String server_address;      //udp日志发送地址

    @XmlPath("params/port_number/text()")
    public Integer port_number;      //udp日志发送端口

    @Override
    public String toString() {
        return "TriggerR0ReportOrder{" +
                "name=" + name +
                ", id=" + id +
                ", orderid=" + orderid +
                ", satellite=" + satellite +
                ", taskSerialNumber=" + taskSerialNumber +
                ", taskId=" + taskId +
                ", jobTaskId1=" + jobTaskId1 +
                ", jobTaskId2=" + jobTaskId2 +
                ", job1_S1=" + job1_S1 +
                ", job2_S1=" + job2_S1 +
                ", job1_S2=" + job1_S2 +
                ", job2_S2=" + job2_S2 +
                ", diffTxt=" + diffTxt +
                ", segmentSize=" + segmentSize +
                ", server_address=" + server_address +
                ", port_number=" + port_number +
                '}';
    }
}
