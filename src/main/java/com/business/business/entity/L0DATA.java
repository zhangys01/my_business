package com.business.business.entity;

/**
 * @author w_kiven
 * @title: L0DATA
 * @projectName BusinessControl125
 * @description: TODO
 * @date 2019/12/1614:37
 */
public class L0DATA {
    private String JOBTASKID;
    private String SATELLITEID;
    private String ORBIT;
    private String DATASTARTTIME;//todo 开始时间
    private String DATAENDTIME;//todo 结束时间
    private String FILEPATH;
    private String STORAGE_STATUS;

    public String getJOBTASKID() {
        return JOBTASKID;
    }

    public void setJOBTASKID(String JOBTASKID) {
        this.JOBTASKID = JOBTASKID;
    }

    public String getSATELLITEID() {
        return SATELLITEID;
    }

    public void setSATELLITEID(String SATELLITEID) {
        this.SATELLITEID = SATELLITEID;
    }



    public String getORBIT() {
        return ORBIT;
    }

    public void setORBIT(String ORBIT) {
        this.ORBIT = ORBIT;
    }

    public String getDATASTARTTIME() {
        return DATASTARTTIME;
    }

    public void setDATASTARTTIME(String DATASTARTTIME) {
        this.DATASTARTTIME = DATASTARTTIME;
    }

    public String getDATAENDTIME() {
        return DATAENDTIME;
    }

    public void setDATAENDTIME(String DATAENDTIME) {
        this.DATAENDTIME = DATAENDTIME;
    }

    public String getFILEPATH() {
        return FILEPATH;
    }

    public void setFILEPATH(String FILEPATH) {
        this.FILEPATH = FILEPATH;
    }

    public String getSTORAGE_STATUS() {
        return STORAGE_STATUS;
    }

    public void setSTORAGE_STATUS(String STORAGE_STATUS) {
        this.STORAGE_STATUS = STORAGE_STATUS;
    }
}
