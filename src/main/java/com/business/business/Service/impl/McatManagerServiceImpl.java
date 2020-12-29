package com.business.business.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.business.Service.McatManagerService;
import com.business.business.config.Config;
import com.business.business.dao.McatManagerDao;
import com.business.business.entity.Mcat;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/13 12:30
 */
@Service
public class McatManagerServiceImpl extends ServiceImpl<McatManagerDao, Mcat> implements McatManagerService {
    private static final Logger logger = Logger.getLogger(McatManagerServiceImpl.class);
    @Override
    public List<Mcat> selectCatbyJobId(String taskId) throws Exception {
        return baseMapper.selectCatbyJobId(taskId);
    }

    @Override
    public List<Mcat> selectByFull(Map<String, String> map) throws Exception {
        return baseMapper.selectByFull(map);
    }

    @Override
    public List<Mcat> selectByTime(Map<String, String> map) throws Exception {
        return baseMapper.selectByTime(map);
    }

    @Override
    public List<Mcat> selectByAuto(Map<String, String> map) throws Exception {
        return baseMapper.selectByAuto(map);
    }

    @Override
    public List<Mcat> selectByCustom(String[] sceneID) throws Exception {
        List<Mcat> mcatList = new ArrayList<>();
        Mcat cat = new Mcat();
        for (int i=0;i<sceneID.length;i++){
            cat = selectBysceneId(sceneID[i]);
            mcatList.add(cat);
        }
        return mcatList;
    }



    @Override
    public Mcat selectBysceneId(String secenID) throws Exception {
        return baseMapper.selectBysceneId(secenID);
    }

    public  List<Mcat> selectSceneByFull(String jobTaskId, List<String> sensors) throws Exception {
        List<Mcat> catInfoList = new ArrayList<>();
        Map<String,String>map = new HashMap<>();
        try {
            map.put("taskid",jobTaskId);
            String sensor = "";
            if(sensors!=null && !sensors.isEmpty()){
                for (int i=0;i<sensors.size();i++){
                    sensor +=sensors.get(i)+"',";
                }
            }
            map.put("sensor",sensor);
            catInfoList = selectByFull(map);
            if (catInfoList.isEmpty())throw new Exception("no CAT-meta record found for message=normal, taskid="+jobTaskId+", sensorid="+sensors);

        }catch (Exception e){
            logger.error(e);
        }
        return catInfoList;
    }
    public  List<Mcat> selectSceneByTime(String jobTaskID, List<String> sensors, String startTime, String endTime) {
        List<Mcat> catList = new ArrayList<>();
        Map<String,String>map = new HashMap<>();
        try {
            map.put("taskid",jobTaskID);
            map.put("startTime",startTime);
            map.put("endTime",endTime);
            String sensor = "";
            if (sensors!=null&&!sensors.isEmpty()){
                for (int i=0;i<sensors.size();i++){
                    sensor +=sensors.get(i)+",";
                }
            }
            map.put("sensor",sensor);
            catList = selectByTime(map);
        }catch (Exception e){
            logger.error(e);
        }
        return catList;
    }
    public  List<Mcat> selectSceneByAuto(String jobTaskID, List<String> sensors) throws Exception  {
        List<Mcat>ret = new ArrayList<>();
        Map<String ,String>map = new HashMap<>();
        try {
            map.put("taskid",jobTaskID);
            for (String sensor1:sensors){
                List<Mcat>ls = new ArrayList<>();
                map.put("sensor",sensor1);
                ls = selectByAuto(map);
                if(ls.isEmpty()){
                    continue;
                }else if(ls.size()>3){
                    Set<Integer> selectedIndex=new HashSet<>();    //避免选景重复
                    //直接取头部景
                    if(Config.scene_select_head>=1 && Config.scene_select_head <=ls.size()){
                        ret.add(ls.get(Config.scene_select_head-1));        //todo 当做批量性能测试只选取中间一景时，可临时注掉此行！！！
                        selectedIndex.add(Config.scene_select_head-1);
                    }else{
                        ret.add(ls.get(0));     //头配置不正确时，默认选第1景        //todo 当做批量性能测试只选取中间一景时，可临时注掉此行！！！
                        selectedIndex.add(0);
                    }
                    //找中间景，初始位置为总景数/2取整
                    int mid=ls.size() / 2;
                    int i=mid;
                    while(selectedIndex.contains(i)){    //重复
                        if(i<=mid){     //先依次往前找景
                            i--;
                        }else{      //再依次往后找景
                            i++;
                        }
                        if(i<0){   //往前到头都没找到，则转为往后找
                            i=mid+1;
                        }else if(i==ls.size()){   //impossible
                            throw new Exception("cannot select mid-scene!");
                        }
                    }
                    ret.add(ls.get(i));
                    selectedIndex.add(i);
                    //找尾部景
                    int tail;    //初始位置
                    if(Config.scene_select_tail>=1 && Config.scene_select_tail <=ls.size()){
                        tail=ls.size()-Config.scene_select_tail;
                    }else{
                        tail=ls.size() - 1;     //尾配置不正确时，默认选倒数第1景
                    }
                    i=tail;
                    while(selectedIndex.contains(i)){    //重复
                        if(i>=tail){     //先依次往后找景
                            i++;
                        }else{      //再依次往前找景
                            i--;
                        }
                        if(i==ls.size()){   //往后到尾都没找到，则转为往前找
                            i=tail-1;
                        }else if(i<0){   //impossible
                            throw new Exception("cannot select tail-scene!");
                        }
                    }
                    ret.add(ls.get(i));       //todo 当做批量性能测试只选取中间一景时，可临时注掉此行！！！
                }else{  //少于或等于三景则全部选取
                    //ret.add(ls.get(0));      //todo 当做批量性能测试只选取中间一景时，可临时用此行替换掉下面一行！！！
                    ret.addAll(ls);
                }
            }
        }catch (Exception e){
            logger.error(e);
        }
        if(ret.isEmpty()) throw new Exception("no CAT-meta record found for message=normal, taskid="+jobTaskID+", sensorid ="+sensors);
        return ret;
    }
}
