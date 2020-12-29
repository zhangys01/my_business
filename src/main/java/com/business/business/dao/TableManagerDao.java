package com.business.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.business.entity.TableManager;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 16:26
 */
public interface TableManagerDao extends BaseMapper<TableManager> {
    void deleteByJobtaskId(String jobtaskId);
    void deleteByTaskId(String taskId);
    void deleteProductIdByL1A(String PRODUCTID_L1A);
    void deleteProductIdByL2A(String PRODUCTID_L2A);

}
