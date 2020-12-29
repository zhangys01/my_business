package com.business.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.business.entity.TableManager;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 16:24
 */
public interface TableManagerService extends IService<TableManager> {
   void deleteByJobtaskId(String jobtaskId);
    void deleteByTaskId(String taskId);
    void deleteProductIdByL1A(String PRODUCTID_L1A);
    void deleteProductIdByL2A(String PRODUCTID_L2A);
}
