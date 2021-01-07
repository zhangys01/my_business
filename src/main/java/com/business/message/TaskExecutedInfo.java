package com.business.message;

import com.business.enums.ExecutingState;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */
public class TaskExecutedInfo {
    @XmlPath("taskMode/text()")
    public String taskMode;

    @XmlPath("executingState/text()")
    public ExecutingState executingState;

    @XmlPath("errorReason/text()")
    public String errorReason;

    @XmlPath("executingStartTime/text()")
    public String executingStartTime;

    @XmlPath("executingEndTime/text()")
    public String executingEndTime;

    @XmlPath("dataExecuteInfo")
    public List<DataExecuteInfo> dataExecuteInfo;

    @XmlPath("QAreportFileName/text()")
    public String QAreportFileName;

    @Override
    public String toString() {
        return "TaskExecutedInfo{" +
                "taskMode=" + taskMode +
                ", executingState=" + executingState +
                ", errorReason=" + errorReason +
                ", executingStartTime=" + executingStartTime +
                ", executingEndTime=" + executingEndTime +
                ", dataExecuteInfo=" + dataExecuteInfo +
                ", QAreportFileName=" + QAreportFileName +
                '}';
    }
}
