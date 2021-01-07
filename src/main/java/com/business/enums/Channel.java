package com.business.enums;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-24
 * Time: 下午12:31
 * To change this template use File | Settings | File Templates.
 */
public enum Channel {   //枚举名：通道标识
    S0("00"),
    S1("01"),
    S2("02");

    private String id;   //原始码流文件名中的通道号

    private Channel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Channel fromId(String id){
        //通过原始码流文件名中的通道号获得通道标识
        for(Channel c:Channel.values()){
            if(c.getId().equals(id)) return c;
        }
        throw new IllegalArgumentException("invalid channel id: "+id);
    }
}
