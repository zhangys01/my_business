package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.LinuxUnzipNodes;

import java.util.List;

public interface LinuxUnzipNodeDao extends BaseMapper<LinuxUnzipNodes> {
    List<LinuxUnzipNodes> selectIpnodes(String nodeSatus);
}
