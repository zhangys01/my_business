package com.business.business.enums;

/**
 * 1 *
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/3/7 15:50
 * 4
 */
public class CatalogConfig {
    private int ID;
    private String satellite;
    private int PA;
    private int MS;
    private int FWD;
    private int NAD;
    private int BWD;
    private int MUX;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSatellite() {
        return satellite;
    }

    public void setSatellite(String satellite) {
        this.satellite = satellite;
    }

    public int getPA() {
        return PA;
    }

    public void setPA(int PA) {
        this.PA = PA;
    }

    public int getMS() {
        return MS;
    }

    public void setMS(int MS) {
        this.MS = MS;
    }

    public int getFWD() {
        return FWD;
    }

    public void setFWD(int FWD) {
        this.FWD = FWD;
    }

    public int getNAD() {
        return NAD;
    }

    public void setNAD(int NAD) {
        this.NAD = NAD;
    }

    public int getBWD() {
        return BWD;
    }

    public void setBWD(int BWD) {
        this.BWD = BWD;
    }

    public int getMUX() {
        return MUX;
    }

    public void setMUX(int MUX) {
        this.MUX = MUX;
    }
}
