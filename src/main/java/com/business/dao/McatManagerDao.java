package com.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.business.entity.Mcat;

import java.util.List;
import java.util.Map;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/13 12:30
 */
public interface McatManagerDao extends BaseMapper<Mcat> {
    List<Mcat> selectCatbyJobId(String taskId)throws Exception;
    /**
     * 全景
     */
    List<Mcat> selectByFull(Map<String,String> map)throws Exception;
    /**
     * 时间段内的景数
     * String jobTaskID, List<String> sensors, String startTime, String endTime
     */
    List<Mcat> selectByTime(Map<String,String> map)throws Exception;
    /**
     * 自动选三景
     * String jobTaskID,List<String> sensors
     */
    List<Mcat> selectByAuto(Map<String,String> map)throws Exception;

    /**
     * 根据景ID获取
     */
    Mcat selectBysceneId(String secenID);

    List<Mcat>getCatInfo(String jobTaskID,String segmentID);

}
