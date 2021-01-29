package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.UnzipConfirm;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 15:20
 */
public interface UnzipConfirmDao extends BaseMapper<UnzipConfirm> {
    void saveConfrim(int id,String activitId,String cancelId,int status);
    int selectMaxId();

}
