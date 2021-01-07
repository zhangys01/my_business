package com.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.Service.ProcessInfoService;
import com.business.dao.ProcessInfoDao;
import com.business.entity.ProcessInfo;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/4 16:14
 */
@Service
public class ProcessInfoServiceImpl extends ServiceImpl<ProcessInfoDao, ProcessInfo> implements ProcessInfoService {
    private static final Logger logger = Logger.getLogger(ProcessInfoServiceImpl.class);

    @Override
    public synchronized List<ProcessInfo> selectProcess(String platform) throws Exception {
        List<ProcessInfo>infoList = new ArrayList<>();
        try {
            infoList = baseMapper.selectProcess(platform);
        }catch (Exception e){
            logger.error(e);
        }
        return infoList;
    }

    public synchronized ProcessInfo getProcessByName(String platfrom,String processType)throws Exception{
        ProcessInfo info = new ProcessInfo();
        try {
            info = baseMapper.getProcessByName(platfrom,processType);
        }catch (Exception e){
            logger.error(e);
        }
        return info;
    }
    public synchronized List<ProcessInfo>getProcessList(String platfrom,String processType)throws Exception{
        List<ProcessInfo>infoList = new ArrayList<>();
        try {
            infoList = baseMapper.getProcessList(platfrom,processType);
        }catch (Exception e){
            logger.error(e);
        }
        return infoList;
    }
}
