package com.business.enums;


import com.business.adapter.String2ListXmlAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午11:00
 * To change this template use File | Settings | File Templates.
 */
public enum TaskMode {
    Q61,
    Q62,
    Q63,
    Q64,
    Q65;

    public static String toAutoTaskModes(){
        //返回常规流程下的Q61、62、63组合模式串。半角分号分隔
        return Q61+String2ListXmlAdapter.DELIMIT+Q62+String2ListXmlAdapter.DELIMIT+Q63;
    }
}
