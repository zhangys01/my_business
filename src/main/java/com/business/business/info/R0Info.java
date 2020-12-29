package com.business.business.info;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:14
 * To change this template use File | Settings | File Templates.
 */
  //对应查询R0元数据表的select语句
public class R0Info {
    public String signalID;

    public String satellite;  //卫星简称

    public String channelID;

    public String receiveStartTime;

    public String receiveEndTime;

    public String metaFilePath;   //注意，数据库中记录的是相对路径


    @Override
    public String toString() {
        return "R0Info{" +
                "signalID=" + signalID +
                ", satellite=" + satellite +
                ", channelID=" + channelID +
                ", receiveStartTime=" + receiveStartTime +
                ", receiveEndTime=" + receiveEndTime +
                ", metaFilePath=" + metaFilePath +
                '}';
    }
}
