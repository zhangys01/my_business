package com.business.entity;

import org.eclipse.persistence.oxm.annotations.XmlMarshalNullRepresentation;
import org.eclipse.persistence.oxm.annotations.XmlNullPolicy;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-8-21
 * Time: 下午12:15
 * To change this template use File | Settings | File Templates.
 */
//@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task")
public class Result {
    public static final String SUCCESS="success";
    public static final String FAIL="fail";

    @XmlPath("@name")
    public String name;

    @XmlPath("@id")
    public String id;

    @XmlPath("@orderid")
    @XmlNullPolicy(nullRepresentationForXml = XmlMarshalNullRepresentation.ABSENT_NODE)
    public String orderid;

    @XmlPath("result/text()")
    public String result;

    @XmlElement(name = "message")
    @XmlNullPolicy(nullRepresentationForXml = XmlMarshalNullRepresentation.ABSENT_NODE)
    public String message;

    @Override
    public String toString() {
        return "Result{" +
                "name=" + name +
                ", id=" + id +
                ", orderid=" + orderid +
                ", result=" + result +
                ", message=" + message +
                '}';
    }
}
