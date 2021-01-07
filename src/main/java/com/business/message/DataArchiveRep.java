package com.business.message;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午8:49
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "DataArchiveRep")
public class DataArchiveRep extends Response{
    @XmlPath("content/dataStatusRepInfo")
    public List<DataStatusRepInfo> dataStatusRepInfo;

    @XmlPath("content/description/text()")
    public String description;

    @XmlTransient
    public String replyFileName;  //（临时变量）完成通知文件名

    @Override
    public String toString() {
        return "DataArchiveRep{" +
                "dataStatusRepInfo=" + dataStatusRepInfo +
                ", description=" + description +
                "} " + super.toString();
    }
}
