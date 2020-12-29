package com.business.business.message;

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
@XmlRootElement(name = "QATaskRep")
public class QATaskRep extends Response{
    @XmlPath("content/taskBasicRepInfo")
    //todo 注意，若unmarshal复杂对象类型集合，必须先初始化集合实例才可填入对象，否则集合总为null
    public List<TaskBasicRepInfo> taskBasicRepInfo; //=new ArrayList<TaskBasicRepInfo>();

    @XmlTransient
    public String replyFileName;  //（临时变量）完成通知文件名

    @Override
    public String toString() {
        return "QATaskRep{" +
                "taskBasicRepInfo=" + taskBasicRepInfo +
                "} " + super.toString();
    }
}
