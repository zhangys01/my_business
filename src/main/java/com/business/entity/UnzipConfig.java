package com.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 1 *
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/3/5 15:42
 * 4
 */
@TableName(value = "unzip_config")
public class UnzipConfig {
    @TableId(value = "Id",type = IdType.AUTO)
    private int Id;
    @TableField(value = "SatelliteId")
    private String SatelliteId;
    @TableField(value = "SensorList")
    private String SensorList;
    @TableField(value = "SkipHeadS1")
    private String SkipHeadS1;
    @TableField(value = "SkipHeadS2")
    private String SkipHeadS2;
    @TableField(value = "ReadBytesS1")
    private String ReadBytesS1;
    @TableField(value = "ReadBytesS2")
    private String ReadBytesS2;
    @TableField(value = "ReadBitrateS1")
    private String ReadBitrateS1;
    @TableField(value = "ReadBitrateS2")
    private String ReadBitrateS2;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getSatelliteId() {
        return SatelliteId;
    }

    public void setSatelliteId(String satelliteId) {
        SatelliteId = satelliteId;
    }

    public String getSensorList() {
        return SensorList;
    }

    public void setSensorList(String sensorList) {
        SensorList = sensorList;
    }

    public String getSkipHeadS1() {
        return SkipHeadS1;
    }

    public void setSkipHeadS1(String skipHeadS1) {
        SkipHeadS1 = skipHeadS1;
    }

    public String getSkipHeadS2() {
        return SkipHeadS2;
    }

    public void setSkipHeadS2(String skipHeadS2) {
        SkipHeadS2 = skipHeadS2;
    }

    public String getReadBytesS1() {
        return ReadBytesS1;
    }

    public void setReadBytesS1(String readBytesS1) {
        ReadBytesS1 = readBytesS1;
    }

    public String getReadBytesS2() {
        return ReadBytesS2;
    }

    public void setReadBytesS2(String readBytesS2) {
        ReadBytesS2 = readBytesS2;
    }

    public String getReadBitrateS1() {
        return ReadBitrateS1;
    }

    public void setReadBitrateS1(String readBitrateS1) {
        ReadBitrateS1 = readBitrateS1;
    }

    public String getReadBitrateS2() {
        return ReadBitrateS2;
    }

    public void setReadBitrateS2(String readBitrateS2) {
        ReadBitrateS2 = readBitrateS2;
    }
}
