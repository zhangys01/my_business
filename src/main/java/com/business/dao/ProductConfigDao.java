package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.ProductUnzipConfig;

import java.util.List;

public interface ProductConfigDao extends BaseMapper<ProductUnzipConfig> {
    /*根据状态来找能做压缩的节点*/
    List<ProductUnzipConfig> findByStatus(String isUnzip);
}
