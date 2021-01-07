package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.ProductUnzipConfig;

public interface ProductConfigDao extends BaseMapper<ProductUnzipConfig> {
    /*根据状态来找能做压缩的节点*/
    ProductUnzipConfig findByStatus(String isUnzip);
}
