package com.business.business.message;

import com.business.business.enums.DataExecutingState;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */
public class DataExecuteInfo {
    @XmlPath("dataFileName/text()")
    public String dataFileName;

    @XmlPath("dataExecutingState/text()")
    public DataExecutingState dataExecutingState;

    @XmlPath("dataExecutingStartTime/text()")
    public String dataExecutingStartTime;

    @XmlPath("dataExecutingEndTime/text()")
    public String dataExecutingEndTime;

    @XmlPath("dataErrorReason/text()")
    public String dataErrorReason;

    @Override
    public String toString() {
        return "DataExecuteInfo{" +
                "dataFileName=" + dataFileName +
                ", dataExecutingState=" + dataExecutingState +
                ", dataExecutingStartTime=" + dataExecutingStartTime +
                ", dataExecutingEndTime=" + dataExecutingEndTime +
                ", dataErrorReason=" + dataErrorReason +
                '}';
    }
}
