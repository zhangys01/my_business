package com.business.entity;


import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-10
 * Time: 下午3:14
 * To change this template use File | Settings | File Templates.
 */
//todo 涉及到联合表查询，暂时不用tablename
public class Ml2Info {
    public String productID;

    public String sceneID;

    public String notecreatetime;

    @Override
    public String toString() {
        return "L2AInfo{" +
                "productID=" + productID +
                ", sceneID=" + sceneID +
                ", notecreatetime=" + notecreatetime +
                '}';
    }
}
