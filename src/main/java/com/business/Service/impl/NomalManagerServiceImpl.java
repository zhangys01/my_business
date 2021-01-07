package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.NomalManagerService;
import com.business.dao.NomalManagerDao;
import com.business.entity.Ml2Info;
import com.business.entity.NomalProduct;
import com.business.entity.TableManager;
import org.springframework.stereotype.Service;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 16:25
 */
@Service
public class NomalManagerServiceImpl extends ServiceImpl<NomalManagerDao, TableManager> implements NomalManagerService {
    @Override
    public void deleteByJobtaskId(String tableName,String jobtaskId) {
        baseMapper.deleteByJobtaskId(tableName,jobtaskId);
    }

    @Override
    public  void  deleteProductIdByL1A(String tableName,String PRODUCTID_L1A){
        baseMapper.deleteProductIdByL1A(tableName,PRODUCTID_L1A);
    }

    @Override
    public void deleteProductIdByL2A(String tableName,String PRODUCTID_L2A){
        baseMapper.deleteProductIdByL2A(tableName,PRODUCTID_L2A);
    }

    @Override
    public Ml2Info getL2AInfo(String jobtaskid, String scenid) {
        return baseMapper.getL2AInfo(jobtaskid,scenid);
    }

    @Override
    public NomalProduct getL1product(String productId) {
        return baseMapper.getL1product(productId);
    }

    @Override
    public NomalProduct getL2product(String productId) {
        return baseMapper.getL2product(productId+"_L2A");
    }
}
