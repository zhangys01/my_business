package com.business.business.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("workflow_order")
public class WorkflowOrder {
    @TableField(value = "orderType")
    private String orderType;//从继承Instruction的三个数据找
    @TableId(value = "taskSerialNumber",type = IdType.INPUT)
    private String taskSerialNumber;//各类任务单流水号
    @TableField(value = "taskPriority")
    public String taskPriority;//normal：按正常顺序处理；preference：优先启动处理；urgency：加急处理
    @TableField(value = "taskStatus")
    public String taskStatus;//New：该任务单是新增的。Canceled：该任务单被取消，任务单一旦取消，
    @TableField(value = "taskMode")
    public String taskMode;//任务类型
    @TableField(value = "jobTaskID")
    private String jobTaskID;
    @TableField(value = "channelID")
    private String channelID;
    @TableField(value = "satelliteName")
    private String satelliteName;//卫星名
    @TableField(value = "sensorName")
    private String sensorName;
    @TableField(value = "orbitNumber")
    private Integer orbitNumber;
    @TableField(value = "dataSelectType")
    public String dataSelectType;
    @TableField(value = "receiveStartTime")
    public String receiveStartTime;
    @TableField(value = "receiveEndTime")
    public String receiveEndTime;
    @TableField(value = "sensorStartTime")
    public String sensorStartTime;
    @TableField(value = "sensorEndTime")
    public String sensorEndTime;
    @TableField(value = "sceneID")
    private String sceneID;
    @TableField(value = "signalID")
    private String signalID;
    @TableField(value = "productLevel")
    private String productLevel;
    @TableField(value = "out_productdir")
    private String out_productdir;
    @TableField(value = "orderStatus")
    private String orderStatus;//Ready进入准备状态Hold（任务单处于排队中）；Running（任务单处于执行中）;Aborted（任务单处于放弃状态）；Completed（任务单处于完成状态）
    @TableField(value = "xmltypedata")
    private String xmltypedata;//任务单内容
    @TableField(value = "startTime")
    private String startTime;//开始时间
    @TableField(value = "endTime")
    private String endTime;//结束时间
    @TableField(value = "comment")
    private String comment;//备注
    @TableField(value = "oper")
    private String oper;//操作员
    @TableField(value = "resampleKernal")
    private String resampleKernal;
    @TableField(value = "station")
    private String station;//接收站
    @TableField(value = "logName")
    private String logName;
    @TableField(value = "logPath")
    private String logPath;
    @TableField(value = "fileResource")
    private String fileResource;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getTaskSerialNumber() {
        return taskSerialNumber;
    }

    public void setTaskSerialNumber(String taskSerialNumber) {
        this.taskSerialNumber = taskSerialNumber;
    }

    public String getFileResource() {
        return fileResource;
    }

    public void setFileResource(String fileResource) {
        this.fileResource = fileResource;
    }

    public String getJobTaskID() {
        return jobTaskID;
    }

    public void setJobTaskID(String jobTaskID) {
        this.jobTaskID = jobTaskID;
    }

    public String getTaskMode() {
        return taskMode;
    }

    public void setTaskMode(String taskMode) {
        this.taskMode = taskMode;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public Integer getOrbitNumber() {
        return orbitNumber;
    }

    public void setOrbitNumber(Integer orbitNumber) {
        this.orbitNumber = orbitNumber;
    }

    public String getSceneID() {
        return sceneID;
    }

    public void setSceneID(String sceneID) {
        this.sceneID = sceneID;
    }

    public String getSignalID() {
        return signalID;
    }

    public void setSignalID(String signalID) {
        this.signalID = signalID;
    }

    public String getProductLevel() {
        return productLevel;
    }

    public void setProductLevel(String productLevel) {
        this.productLevel = productLevel;
    }


    public String getOut_productdir() {
        return out_productdir;
    }

    public void setOut_productdir(String out_productdir) {
        this.out_productdir = out_productdir;
    }

    public String getResampleKernal() {
        return resampleKernal;
    }

    public void setResampleKernal(String resampleKernal) {
        this.resampleKernal = resampleKernal;
    }
/* public String getUnzipStartByte() {
        return unzipStartByte;
    }

    public void setUnzipStartByte(String unzipStartByte) {
        this.unzipStartByte = unzipStartByte;
    }

    public String getUnzipEndByte() {
        return unzipEndByte;
    }

    public void setUnzipEndByte(String unzipEndByte) {
        this.unzipEndByte = unzipEndByte;
    }*/

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getXmltypedata() {
        return xmltypedata;
    }

    public void setXmltypedata(String xmltypedata) {
        this.xmltypedata = xmltypedata;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getReceiveStartTime() {
        return receiveStartTime;
    }

    public void setReceiveStartTime(String receiveStartTime) {
        this.receiveStartTime = receiveStartTime;
    }

    public String getReceiveEndTime() {
        return receiveEndTime;
    }

    public void setReceiveEndTime(String receiveEndTime) {
        this.receiveEndTime = receiveEndTime;
    }

    public String getSensorStartTime() {
        return sensorStartTime;
    }

    public void setSensorStartTime(String sensorStartTime) {
        this.sensorStartTime = sensorStartTime;
    }

    public String getSensorEndTime() {
        return sensorEndTime;
    }

    public void setSensorEndTime(String sensorEndTime) {
        this.sensorEndTime = sensorEndTime;
    }

    public String getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDataSelectType() {
        return dataSelectType;
    }

    public void setDataSelectType(String dataSelectType) {
        this.dataSelectType = dataSelectType;
    }

}
