package com.business.enums;

import java.util.ArrayList;
import java.util.List;

public enum TableName {
    gt_m_l2,
    gt_r_l2,
    gt_m_l1,
    gt_r_l1,
    gt_m_cat,
    gt_m_l0,
    gt_r_l0,
    gt_m_r0;
    public static List<String> getTableName(String type) {
        List<String>tableList = new ArrayList<>();
        switch (type){
            case"all":
                tableList.add("gt_m_l2");
                tableList.add("gt_r_l2");
                tableList.add("gt_m_l1");
                tableList.add("gt_r_l1");
                tableList.add("gt_m_cat");
                tableList.add("gt_m_l0");
                tableList.add("gt_r_l0");
                tableList.add("gt_m_r0");
                break;
            case"L1A":
                tableList.add("gt_m_l1");
                tableList.add("gt_r_l1");
                break;
            case"L2A":
                tableList.add("gt_m_l2");
                tableList.add("gt_r_l2");
                break;
        }
        return tableList;
    }

}
