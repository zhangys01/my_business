package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.Ml2Info;
import com.business.entity.NomalProduct;
import com.business.entity.TableManager;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 16:26
 */
public interface NomalManagerDao extends BaseMapper<TableManager> {
    /**
     * 根据JobTaskId删除信息
     */
    void deleteByJobtaskId(String tableName,String jobtaskId);
    /**
     * 删除L1表内的数据
     */
    void deleteProductIdByL1A(String tableName,String PRODUCTID_L1A);
    /**
     * 删除L2表内的数据信息
     */
    void deleteProductIdByL2A(String tableName,String PRODUCTID_L2A);

    Ml2Info getL2AInfo(String jobtaskid, String scenid);
    /**
     *根据productId查询数据信息
     */
    NomalProduct getL1product(String productId);

    NomalProduct getL2product(String productId);
}
