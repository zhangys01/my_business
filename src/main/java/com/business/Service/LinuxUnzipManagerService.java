package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.LinuxUnzipManager;

import java.util.List;

public interface LinuxUnzipManagerService extends IService<LinuxUnzipManager> {

    List<LinuxUnzipManager> selectQueueList();

    List<LinuxUnzipManager> selectByTaskId(String taskSerialNumber);
}
