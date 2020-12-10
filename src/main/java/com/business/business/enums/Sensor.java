package com.business.business.enums;



import com.business.business.message.SensorDataArchiveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-8
 * Time: 下午1:46
 * To change this template use File | Settings | File Templates.
 */
public enum Sensor {    //内部用传感器标识。注意，不同于运管接口定义的传感器标识

    VN,	//可见光近红外VNIR
    SW,	//短波红外SWIR
    AHSI,
    VIMS; //多光谱VIMS


    public static List<String> fromOMOSensor(String satellite, String omoSensor) {
        List<String>sensorList = new ArrayList<>();
        switch (satellite){
            case"GF-1B":
            case"GF-1C":
            case"GF-1D":
                sensorList.add("PAN1");
                sensorList.add("PAN2");
                sensorList.add("MSS1");
                sensorList.add("MSS2");
                break;
            case "ZY-3B":
                sensorList.add("MUX");
                sensorList.add("TLC");
                break;
        }
        return sensorList;
    }

    public static List<String> fromOMOSensors(String satellite, List<String> omoSensors) {
        //将运管接口定义的传感器标识，转换为内部用传感器标识
        ArrayList<String> ret = new ArrayList<>();
        for (String s : omoSensors) {
            ret.addAll(fromOMOSensor(satellite, s));
        }
        return ret;
    }

    public static List<SensorDataArchiveInfo> toSimplify(List<SensorDataArchiveInfo> sas) {
        if(sas.size() <= 1)
            return sas;
        List<SensorDataArchiveInfo> sasRs = new ArrayList<>();
        for (int i=0;i<sas.size();i++){
            SensorDataArchiveInfo sa = new SensorDataArchiveInfo();
            sa.sensorDataStartTime = sas.get(i).sensorDataStartTime;
            sa.sensorDataEndTime = sas.get(i).sensorDataEndTime;
            sa.sensorName = sas.get(i).sensorName;
            sasRs.add(sa);
        }
        return sasRs;
    }


}
