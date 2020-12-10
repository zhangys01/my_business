package com.business.business.message;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */
public class SensorDataArchiveInfo {
    @XmlPath("sensorName/text()")
    public String sensorName;

    @XmlPath("sensorDataStartTime/text()")
    public String sensorDataStartTime;

    @XmlPath("sensorDataEndTime/text()")
    public String sensorDataEndTime;

    @Override
    public String toString() {
        return "SensorDataArchiveInfo{" +
                "sensorName=" + sensorName +
                ", sensorDataStartTime=" + sensorDataStartTime +
                ", sensorDataEndTime=" + sensorDataEndTime +
                '}';
    }
}
