package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.Ml2Info;
import com.business.entity.NomalProduct;
import com.business.entity.TableManager;
import org.apache.ibatis.annotations.Param;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 16:26
 */
public interface NomalManagerDao extends BaseMapper<TableManager> {
    /**
     * 根据JobTaskId删除信息
     */
    void deleteByJobtaskId( @Param("tablename") String tablename, @Param("jobtaskid") String jobtaskid);
    /**
     * 删除L1表内的数据
     */
    void deleteProductIdByL1A( @Param("tablename") String tablename,@Param("PRODUCTID_L1A") String PRODUCTID_L1A);
    /**
     * 删除L2表内的数据信息
     */
    void deleteProductIdByL2A( @Param("tablename") String tablename,@Param("PRODUCTID_L2A") String PRODUCTID_L2A);

    Ml2Info getL2AInfo(@Param("jobtaskid") String jobtaskid,@Param("scenid") String scenid);
    /**
     *根据productId查询数据信息
     */
    NomalProduct getL1product(@Param("productId") String productId);

    NomalProduct getL2product(@Param("productId") String productId);
}
