package com.business.business.info;



/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-6
 * Time: 下午10:55
 * To change this template use File | Settings | File Templates.
 */
public class CatInfo {
    public String sceneID;       //景ID
    public String segmentID;    //所对应的原始条带ID，多个时分号分隔

    public String satellite;

    public String sensor;

    public String sceneMetaFilePath;   //注意，数据库中记录的是相对路径

    @Override
    public String toString() {
        return "CatInfo{" +
                "sceneID=" + sceneID +
                ", segmentID=" + segmentID +
                ", satellite=" + satellite +
                ", sensor=" + sensor +
                ", sceneMetaFilePath=" + sceneMetaFilePath +
                '}';
    }
}
