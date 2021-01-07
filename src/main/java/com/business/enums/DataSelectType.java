package com.business.enums;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-12
 * Time: 下午12:15
 * To change this template use File | Settings | File Templates.
 */
public enum DataSelectType {
    AutoType,  //表示从一轨数据上自动取第一景、中间一景、最后一景进行质量监测分析
    Time,     //表示从一轨数据上按照时间进行质量监测分析
    Full;     //表示一轨数据全部进行质量监测分析
}
