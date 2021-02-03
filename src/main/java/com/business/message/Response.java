package com.business.message;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午7:34
 * To change this template use File | Settings | File Templates.
 */
public abstract class Response {  //响应基类
    @XmlPath("fileHeader/title/text()")
    //@XmlNullPolicy(nullRepresentationForXml = XmlMarshalNullRepresentation.ABSENT_NODE)  //default
    public String title;

    @XmlPath("fileHeader/identificationCode/text()")
    public String identificationCode;

    @XmlPath("fileHeader/source/text()")
    public String source;

    @XmlPath("fileHeader/destination/text()")
    public String destination;

    @XmlPath("fileHeader/createdTime/text()")
    //@XmlJavaTypeAdapter(DateXmlAdapter.class)
    public String createdTime;

    @XmlPath("fileHeader/authorInfo/text()")
    public String authorInfo;

    @Override
    public String toString() {
        return "Response{" +
                "title='" + title + '\'' +
                ", identificationCode='" + identificationCode + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", authorInfo='" + authorInfo + '\'' +
                '}';
    }
}
