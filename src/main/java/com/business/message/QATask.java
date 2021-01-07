package com.business.message;

import com.business.adapter.String2ListXmlAdapter;
import com.business.enums.DataSelectType;
import com.business.enums.Satellite;
import com.business.adapter.String2ListListXmlAdapter;
import com.business.enums.TaskPriority;
import com.business.enums.TaskStatus;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午5:24
 * To change this template use File | Settings | File Templates.
 */
public class QATask extends Instruction {
    @XmlPath("content/taskBasicInfo/taskSerialNumber/text()")
    public String taskSerialNumber;

    @XmlPath("content/taskBasicInfo/taskPriority/text()")
    public TaskPriority taskPriority;

    @XmlPath("content/taskBasicInfo/taskStatus/text()")
    public TaskStatus taskStatus;

    @XmlPath("content/taskBasicInfo/taskMode/text()")
    @XmlJavaTypeAdapter(String2ListXmlAdapter.class)
    public List<String> taskMode;

    @XmlPath("content/taskBasicInfo/jobTaskID/text()")
    @XmlJavaTypeAdapter(String2ListXmlAdapter.class)
    public List<String> jobTaskID;

    @XmlPath("content/taskBasicInfo/satelliteName/text()")
    public Satellite satellite;
    //public String satellite;

    @XmlPath("content/taskBasicInfo/channelID/text()")
    @XmlJavaTypeAdapter(String2ListXmlAdapter.class)
    public List<String> channelID;

    @XmlPath("content/taskBasicInfo/sensorName/text()")
    @XmlJavaTypeAdapter(String2ListListXmlAdapter.class)
    public List<List<String>> sensorName;      //注意，是运管接口定义的传感器标识，不是内部用传感器标识

    @XmlPath("content/taskBasicInfo/dataSelectType/text()")
    public DataSelectType dataSelectType;

    @XmlPath("content/taskBasicInfo/receiveStartTime/text()")
    public Date receiveStartTime;

    @XmlPath("content/taskBasicInfo/receiveEndTime/text()")
    public Date receiveEndTime;

    @XmlPath("content/taskBasicInfo/sensorStartTime/text()")
    public Date sensorStartTime;

    @XmlPath("content/taskBasicInfo/sensorEndTime/text()")
    public Date sensorEndTime;

    @XmlPath("content/taskBasicInfo/orbitNumber/text()")
    public Integer orbitNumber;

    @XmlPath("content/taskBasicInfo/receiveStation/text()")
    public String receiveStation;

    @XmlPath("content/taskBasicInfo/recorderID/text()")
    public String recorderID;

    @Override
    public String toString() {
        return "QATask{" +
                "taskSerialNumber=" + taskSerialNumber +
                ", taskPriority=" + taskPriority +
                ", taskStatus=" + taskStatus +
                ", taskMode=" + taskMode +
                ", jobTaskID=" + jobTaskID +
                ", satellite=" + satellite +
                ", channelID=" + channelID +
                ", sensorName=" + sensorName +
                ", dataSelectType=" + dataSelectType +
                ", receiveStartTime=" + receiveStartTime +
                ", receiveEndTime=" + receiveEndTime +
                ", sensorStartTime=" + sensorStartTime +
                ", sensorEndTime=" + sensorEndTime +
                ", orbitNumber=" + orbitNumber +
                ", receiveStation=" + receiveStation +
                ", recorderID=" + recorderID +
                "} " + super.toString();
    }
}
