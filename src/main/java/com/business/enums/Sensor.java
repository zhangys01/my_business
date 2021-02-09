package com.business.enums;



import com.business.message.SensorDataArchiveInfo;

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


    public static List<String> fromOMOSensor(String satellite) {
        List<String>sensorList = new ArrayList<>();
        switch (satellite){
            case"GF-6":
                sensorList.add("WF");
                sensorList.add("PAN");
                sensorList.add("MSS");
                break;
            case "GF-7":
                sensorList.add("NAD");
                sensorList.add("FWD");
                sensorList.add("BWD");
                sensorList.add("MSS");
                break;
            case"GF-1B":
            case"GF-1C":
            case"GF-1D":
                sensorList.add("PAN1");
                sensorList.add("MSS1");
                sensorList.add("PAN2");
                sensorList.add("MSS2");
                break;
            case "ZY-3B":
                sensorList.add("MUX");
                sensorList.add("TLC");
                break;
            case"ZY-1E":
            case"ZY1E":
                sensorList.add("PAN");
                sensorList.add("PAN");
                sensorList.add("MSS");
                sensorList.add("MSS");
                break;
            case"CBERS04A":
                sensorList.add("PAN");
                sensorList.add("MSS");
                sensorList.add("PAN");
                sensorList.add("MSS");
                sensorList.add("WPM");
                break;
            case"ZY302":
                sensorList.add("NAD");
                sensorList.add("MUX");
                sensorList.add("FWD");
                sensorList.add("BWD");
                break;
            case"CBERS04A01":
                sensorList.add("PAN1");
                sensorList.add("MUX1");
                sensorList.add("PAN2");
                sensorList.add("MUX2");
                break;
            case"CBERS04A02":
                sensorList.add("PAN3");
                sensorList.add("MUX3");
                sensorList.add("PAN4");
                sensorList.add("MUX4");
                break;
        }
        return sensorList;
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
