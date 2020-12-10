package com.business.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.business.Service.TableManagerService;
import com.business.business.dao.TableManagerDao;
import com.business.business.entity.TableManager;
import org.springframework.stereotype.Service;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 16:25
 */
@Service
public class TableManagerServiceImpl extends ServiceImpl<TableManagerDao, TableManager> implements TableManagerService {
    @Override
    public void deleteByJobtaskId(String jobtaskId) {
        baseMapper.deleteByJobtaskId(jobtaskId);
    }

    @Override
    public void deleteByTaskId(String taskId) {
        baseMapper.deleteByTaskId(taskId);
    }
}
