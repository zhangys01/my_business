package com.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.entity.ProductUnzipConfig;

public interface ProductConfigManagerService extends IService<ProductUnzipConfig> {
    /*根据状态来找能做压缩的节点*/
    ProductUnzipConfig findByStatus(String isUnzip);

}
