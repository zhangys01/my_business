package com.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "sys_dictionaries")
public class SysDictionaries {
    @TableId(value = "dictionaries_id")
    private String dictionariesId;
    private String name;
    private String nameEn;
    private String bianma;
    private String parentId;

    public String getDictionariesId() {
        return dictionariesId;
    }

    public void setDictionariesId(String dictionariesId) {
        this.dictionariesId = dictionariesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getBianma() {
        return bianma;
    }

    public void setBianma(String bianma) {
        this.bianma = bianma;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
