package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.ProductUnzipConfig;

import java.util.List;

public interface ProductConfigManagerService extends IService<ProductUnzipConfig> {
    /*根据状态来找能做压缩的节点*/
    List<ProductUnzipConfig> findByStatus(String isUnzip);

}
