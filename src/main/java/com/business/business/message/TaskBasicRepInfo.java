package com.business.business.message;

import com.business.business.adapter.String2ListXmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlMarshalNullRepresentation;
import org.eclipse.persistence.oxm.annotations.XmlNullPolicy;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午8:49
 * To change this template use File | Settings | File Templates.
 */
public class TaskBasicRepInfo extends Response{
    @XmlPath("taskSerialNumber/text()")
    public String taskSerialNumber;
    @XmlPath("taskStatus/text()")
    public String taskStatus;
    @XmlPath("jobTaskID/text()")
    @XmlJavaTypeAdapter(String2ListXmlAdapter.class)
    //todo 注意，当使用了XmlJavaTypeAdapter时，默认的XmlNullPolicy变为XmlMarshalNullRepresentation.XSI_NIL。所以此处必须显视设置
    @XmlNullPolicy(nullRepresentationForXml = XmlMarshalNullRepresentation.ABSENT_NODE)  //default
    public List<String> jobTaskID;

    @XmlPath("taskExecutedInfo")
    public List<TaskExecutedInfo> taskExecutedInfo;

    @Override
    public String toString() {
        return "TaskBasicRepInfo{" +
                "taskSerialNumber=" + taskSerialNumber +
                ", taskStatus=" + taskStatus +
                ", jobTaskID=" + jobTaskID +
                ", TaskExecutedInfo=" + taskExecutedInfo +
                "} " + super.toString();
    }
}
