package com.business.business.Service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.business.Service.QATaskWorkFlowService;
import com.business.business.dao.QATaskWorkFlowDao;
import com.business.business.entity.QATaskWorkFlow;
import com.business.business.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;


/**
 * 2 * @Author: kiven
 * 3 * @Date: 2019/1/17 15:05
 * 4
 */
@Service
public class QATaskWorkFlowServiceImpl extends ServiceImpl<QATaskWorkFlowDao, QATaskWorkFlow> implements QATaskWorkFlowService {
    private static final Logger logger = Logger.getLogger(QATaskWorkFlowServiceImpl.class);
    /**
     * 保存到Qatask
     */
    @Override
    public void saveQaTask(QATaskWorkFlow qaTask)throws Exception{
        try {
            QATaskWorkFlow task = baseMapper.findById(qaTask.getTaskid());
            if (task!=null){
                baseMapper.updateQaTask(qaTask);
            }else{
                baseMapper.insertQaTask(qaTask);
            }
        }catch (Exception e){
            logger.error(e);
        }
    }
    @Override
    public void insertQaTask(QATaskWorkFlow qaTask)throws Exception{
        try {
            baseMapper.insert(qaTask);
        }catch (Exception e){
            logger.error(e);
        }
    }
    /**
     * 更新状态
     */
    @Override
    public void updateQaTask(QATaskWorkFlow qaTask)throws Exception{
        try {
            baseMapper.updateById(qaTask);
        }catch (Exception e){
            logger.error(e);
        }
    }

}
