package com.business.business.message;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午5:24
 * To change this template use File | Settings | File Templates.
 */
public class QATaskInq extends Instruction {
    @XmlPath("content/taskBasicInqInfo/firstStartTime/text()")
    public String firstStartTime;

    @XmlPath("content/taskBasicInqInfo/lastestStartTime/text()")
    public String lastestStartTime;

    @XmlPath("content/taskBasicInqInfo/taskSerialNumber/text()")
    public List<String> taskSerialNumber;

    @XmlPath("content/taskBasicInqInfo/description/text()")
    public String description;

    @Override
    public String toString() {
        return "QATaskInq{" +
                "firstStartTime=" + firstStartTime +
                ", lastestStartTime=" + lastestStartTime +
                ", taskSerialNumber=" + taskSerialNumber +
                ", description=" + description +
                "} " + super.toString();
    }
}
