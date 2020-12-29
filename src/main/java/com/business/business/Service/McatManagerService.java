package com.business.business.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.business.business.entity.Mcat;

import java.util.List;
import java.util.Map;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/13 12:28
 */
 public interface McatManagerService extends IService<Mcat> {
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
     * 根据景ID
     */
     List<Mcat>selectByCustom(String [] sceneID)throws Exception;

    /**
     * 根据景ID获取
     */
     Mcat selectBysceneId(String secenID)throws Exception;

    List<Mcat> selectSceneByFull(String jobTaskId, List<String> sensors) throws Exception;

    List<Mcat> selectSceneByTime(String jobTaskID, List<String> sensors, String startTime, String endTime);

    List<Mcat> selectSceneByAuto(String jobTaskID, List<String> sensors) throws Exception;
}
