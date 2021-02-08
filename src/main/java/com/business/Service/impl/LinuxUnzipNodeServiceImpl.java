package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.LinuxUnzipNodeService;
import com.business.dao.LinuxUnzipNodeDao;
import com.business.entity.LinuxUnzipNodes;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class LinuxUnzipNodeServiceImpl extends ServiceImpl<LinuxUnzipNodeDao, LinuxUnzipNodes> implements LinuxUnzipNodeService {
    @Override
    public List<LinuxUnzipNodes> selectIpnodes(String nodeSatus) {
        return baseMapper.selectIpnodes(nodeSatus);
    }
}
