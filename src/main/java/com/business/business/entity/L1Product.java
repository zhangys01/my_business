package com.business.business.entity;

/**
 * @author w_kiven
 * @title: L1Product
 * @projectName BusinessControl125
 * @description: TODO L1和L2的表结构一样，改下productLevel就可以了
 * @date 2019/12/179:07
 */
public class L1Product {
    private String SCENEID;
    private String JOBTASKID;
    private String PRODUCTLEVEL;
    private String SATELLITEID;
    private String SENSORID;
    private String STATIONID;
    private String STARTTIME;
    private String ENDTIME;
    private double SCENECENTERLAT;
    private double SCENECENTERLONG;
    private double UPPERLEFTLAT;
    private double UPPERLEFTLONG;
    private double UPPERRIGHTLAT;
    private double UPPERRIGHTLONG;
    private double LOWERLEFTLAT;
    private double LOWERLEFTLONG;
    private double LOWERRIGHTLAT;
    private double LOWERRIGHTLONG;
    private String  CLOUDCOVER;
    private String STORAGE_STATUS;


    public String getSCENEID() {
        return SCENEID;
    }

    public void setSCENEID(String SCENEID) {
        this.SCENEID = SCENEID;
    }

    public String getJOBTASKID() {
        return JOBTASKID;
    }

    public void setJOBTASKID(String JOBTASKID) {
        this.JOBTASKID = JOBTASKID;
    }

    public String getPRODUCTLEVEL() {
        return PRODUCTLEVEL;
    }

    public void setPRODUCTLEVEL(String PRODUCTLEVEL) {
        this.PRODUCTLEVEL = PRODUCTLEVEL;
    }

    public String getSATELLITEID() {
        return SATELLITEID;
    }

    public void setSATELLITEID(String SATELLITEID) {
        this.SATELLITEID = SATELLITEID;
    }

    public String getSENSORID() {
        return SENSORID;
    }

    public void setSENSORID(String SENSORID) {
        this.SENSORID = SENSORID;
    }

    public String getSTATIONID() {
        return STATIONID;
    }

    public void setSTATIONID(String STATIONID) {
        this.STATIONID = STATIONID;
    }

    public String getSTARTTIME() {
        return STARTTIME;
    }

    public void setSTARTTIME(String STARTTIME) {
        this.STARTTIME = STARTTIME;
    }

    public String getENDTIME() {
        return ENDTIME;
    }

    public void setENDTIME(String ENDTIME) {
        this.ENDTIME = ENDTIME;
    }

    public double getSCENECENTERLAT() {
        return SCENECENTERLAT;
    }

    public void setSCENECENTERLAT(double SCENECENTERLAT) {
        this.SCENECENTERLAT = SCENECENTERLAT;
    }

    public double getSCENECENTERLONG() {
        return SCENECENTERLONG;
    }

    public void setSCENECENTERLONG(double SCENECENTERLONG) {
        this.SCENECENTERLONG = SCENECENTERLONG;
    }

    public double getUPPERLEFTLAT() {
        return UPPERLEFTLAT;
    }

    public void setUPPERLEFTLAT(double UPPERLEFTLAT) {
        this.UPPERLEFTLAT = UPPERLEFTLAT;
    }

    public double getUPPERLEFTLONG() {
        return UPPERLEFTLONG;
    }

    public void setUPPERLEFTLONG(double UPPERLEFTLONG) {
        this.UPPERLEFTLONG = UPPERLEFTLONG;
    }

    public double getUPPERRIGHTLAT() {
        return UPPERRIGHTLAT;
    }

    public void setUPPERRIGHTLAT(double UPPERRIGHTLAT) {
        this.UPPERRIGHTLAT = UPPERRIGHTLAT;
    }

    public double getUPPERRIGHTLONG() {
        return UPPERRIGHTLONG;
    }

    public void setUPPERRIGHTLONG(double UPPERRIGHTLONG) {
        this.UPPERRIGHTLONG = UPPERRIGHTLONG;
    }

    public double getLOWERLEFTLAT() {
        return LOWERLEFTLAT;
    }

    public void setLOWERLEFTLAT(double LOWERLEFTLAT) {
        this.LOWERLEFTLAT = LOWERLEFTLAT;
    }

    public double getLOWERLEFTLONG() {
        return LOWERLEFTLONG;
    }

    public void setLOWERLEFTLONG(double LOWERLEFTLONG) {
        this.LOWERLEFTLONG = LOWERLEFTLONG;
    }

    public double getLOWERRIGHTLAT() {
        return LOWERRIGHTLAT;
    }

    public void setLOWERRIGHTLAT(double LOWERRIGHTLAT) {
        this.LOWERRIGHTLAT = LOWERRIGHTLAT;
    }

    public double getLOWERRIGHTLONG() {
        return LOWERRIGHTLONG;
    }

    public void setLOWERRIGHTLONG(double LOWERRIGHTLONG) {
        this.LOWERRIGHTLONG = LOWERRIGHTLONG;
    }

    public String getCLOUDCOVER() {
        return CLOUDCOVER;
    }

    public void setCLOUDCOVER(String CLOUDCOVER) {
        this.CLOUDCOVER = CLOUDCOVER;
    }

    public String getSTORAGE_STATUS() {
        return STORAGE_STATUS;
    }

    public void setSTORAGE_STATUS(String STORAGE_STATUS) {
        this.STORAGE_STATUS = STORAGE_STATUS;
    }
}
