package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.LinuxUnzipNodes;

import java.util.List;

public interface LinuxUnzipNodeService extends IService<LinuxUnzipNodes> {
    List<LinuxUnzipNodes> selectIpnodes(String nodeSatus);
}
