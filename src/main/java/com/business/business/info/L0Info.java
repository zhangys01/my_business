package com.business.business.info;


/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:14
 * To change this template use File | Settings | File Templates.
 */
   //对应查询L0元数据表的select语句
public class L0Info {
    public String segmentID;

    public String sensor;

    public String dataStartTime;

    public String dataEndTime;

    @Override
    public String toString() {
        return "L0Info{" +
                "segmentID=" + segmentID +
                ", sensor=" + sensor +
                ", dataStartTime=" + dataStartTime +
                ", dataEndTime=" + dataEndTime +
                '}';
    }
}
