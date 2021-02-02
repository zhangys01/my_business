package com.business.enums;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-12
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
public enum  Satellite {  //枚举名：卫星简称
	
    GF06("GF-6"),
    GF07("GF-7"),
    GF1B("GF-1B"),
    GF1C("GF-1C"),
    GF1D("GF-1D"),
    ZY3B("ZY-3B"),
    ZY302("ZY302"),
    ZY1E("ZY1E"),
    CB4A("CB4A"),
    CBERS04A("CBERS04A"),
    CASEARTH("CASEARTH");


    private String fullName;  //卫星名称(全称)

    private Satellite(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName(){
        return fullName;
    }

    public static Satellite valueOfFullName(String satelliteFullName) throws Exception {
        for(Satellite s:Satellite.values()){
            if(s.fullName.equals(satelliteFullName)) return s;
        }
        throw new Exception("unknown satellite name: "+satelliteFullName);
    }

}
