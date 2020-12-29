package com.business.business.message;

import com.business.business.enums.ExecutingState;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */
public class DataStatusRepInfo {
    @XmlPath("jobTaskID/text()")
    public String jobTaskID;

    @XmlPath("executingState/text()")
    public ExecutingState executingState;

    @XmlPath("dataArchiveInfo")
    public List<DataArchiveInfo> dataArchiveInfo;

    @XmlPath("errorReason/text()")
    public String errorReason;

    @Override
    public String toString() {
        return "DataStatusRepInfo{" +
                "jobTaskID=" + jobTaskID +
                ", executingState=" + executingState +
                ", dataArchiveInfo=" + dataArchiveInfo +
                ", errorReason=" + errorReason +
                '}';
    }
}
