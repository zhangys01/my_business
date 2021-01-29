package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.UnzipConfirmService;
import com.business.dao.UnzipConfirmDao;
import com.business.entity.UnzipConfirm;
import org.springframework.stereotype.Service;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 15:19
 */
@Service
public class UnzipConfirmServiceImpl extends ServiceImpl<UnzipConfirmDao, UnzipConfirm> implements UnzipConfirmService {
    @Override
    public void saveConfrim(int id,String activitId,String cancelId,int status) {
        baseMapper.saveConfrim( id,activitId, cancelId,status);
    }

    @Override
    public int selectMaxId() {
        return baseMapper.selectMaxId();
    }
}
