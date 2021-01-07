package com.business.message;

import com.business.enums.DataExecutingState;
import com.business.enums.Satellite;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */
public class DataArchiveInfo {
    @XmlPath("satelliteName/text()")
    public Satellite satellite;
    //public String satellite;

    @XmlPath("channelID/text()")
    public String channelID;

    @XmlPath("dataFileName/text()")
    public String dataFileName;

    @XmlPath("dataExecutingState/text()")
    public DataExecutingState dataExecutingState;

    @XmlPath("receiveDataStartTime/text()")
    public String receiveDataStartTime;

    @XmlPath("receiveDataEndTime/text()")
    public String receiveDataEndTime;

    @XmlPath("executingStartTime/text()")
    public String executingStartTime;

    @XmlPath("executingEndTime/text()")
    public String executingEndTime;

    @XmlPath("sensorDataArchiveInfo")
    public List<SensorDataArchiveInfo> sensorDataArchiveInfo;

    @Override
    public String toString() {
        return "DataArchiveInfo{" +
                "satellite=" + satellite +
                ", channelID=" + channelID +
                ", dataFileName=" + dataFileName +
                ", dataExecutingState=" + dataExecutingState +
                ", receiveStartTime=" + receiveDataStartTime +
                ", receiveEndTime=" + receiveDataEndTime +
                ", executingStartTime=" + executingStartTime +
                ", executingEndTime=" + executingEndTime +
                ", sensorDataArchiveInfo=" + sensorDataArchiveInfo +
                '}';
    }
}
