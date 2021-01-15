package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "product_unzip_config")
public class ProductUnzipConfig {
    @TableId(value = "id")
    public int id;
    public String ip;//压缩的ip
    @TableField(value = "is_unzip")
    public String is_unzip;//是否能压缩，0可以，1被占用

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIs_unzip() {
        return is_unzip;
    }

    public void setIs_unzip(String is_unzip) {
        this.is_unzip = is_unzip;
    }
}
