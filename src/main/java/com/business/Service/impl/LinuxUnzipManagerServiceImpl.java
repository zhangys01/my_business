package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.LinuxUnzipManagerService;
import com.business.dao.LinuxUnzipManagerDao;
import com.business.entity.LinuxUnzipManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinuxUnzipManagerServiceImpl extends ServiceImpl<LinuxUnzipManagerDao,LinuxUnzipManager> implements LinuxUnzipManagerService {
    @Override
    public List<LinuxUnzipManager> selectQueueList() {
        return baseMapper.selectQueueList();
    }

    @Override
    public List<LinuxUnzipManager> selectByTaskId(String taskSerialNumber) {
        return baseMapper.selectByTaskId(taskSerialNumber);
    }
}
