package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.LinuxUnzipManager;

import java.util.List;

public interface LinuxUnzipManagerDao extends BaseMapper<LinuxUnzipManager> {
    List<LinuxUnzipManager> selectQueueList();

    List<LinuxUnzipManager> selectByTaskId(String taskSerialNumber);
}
