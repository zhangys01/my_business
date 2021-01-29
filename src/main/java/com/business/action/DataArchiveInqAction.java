package com.business.action;

import com.business.Service.McatManagerService;
import com.business.Service.Ml0InfoService;
import com.business.Service.Mr0InfoService;
import com.business.Service.WorkFlowOrderService;
import com.business.constants.Constants;
import com.business.entity.Mcat;
import com.business.entity.Ml0Info;
import com.business.entity.Mr0Info;
import com.business.info.*;
import com.business.entity.WorkflowOrder;
import com.business.enums.*;
import com.business.message.*;
import com.business.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class DataArchiveInqAction {
        private static final Logger logger = Logger.getLogger(DataArchiveInqAction.class);
        //todo 每个action都创建新实例，因此域变量不会共享冲突

        @Autowired
        private WorkFlowOrderService orderService;
        @Autowired
        private Ml0InfoService ml0InfoService;
        @Autowired
        private Mr0InfoService mr0InfoService;
        @Autowired
        private McatManagerService mcatManagerService;


        //此方法也被TaskCheckThread调用，用于生成自动回复
        public DataStatusRepInfo generateJobInfo(ArchiveWorkflowInfo wi)throws Exception{
            String jobTaskID=wi.jobTaskID;
            DataStatusRepInfo ret = new DataStatusRepInfo();
            ret.jobTaskID=jobTaskID;
            logger.info("调用生成归档完成通知方法，生成rep"+wi);
            //分情况
            WorkflowOrder order =  orderService.findDataskByJobId(jobTaskID);
            if (order.getOrderStatus().equals("3")){
                wi.state = 2;
            }else if (order.getOrderStatus().equals("4")){
                wi.state=4;
            }else if (order.getOrderStatus().equals("1")){
                wi.state=0;
            }else if (order.getOrderStatus().equals("2")){
                wi.state=1;
            }
            if (wi.state== ExecutingState.Processing.ordinal()){  //WorkflowInfo.STATE_RUNNING
                ret.executingState=ExecutingState.Processing;
                return ret;
            }
            List<Ml0Info> lis = new ArrayList<>();
           // Thread.sleep(120000);
            lis = ml0InfoService.getL0Info(wi.jobTaskID,"");
            if (lis.size()==0||lis==null){
                if (wi.state== ExecutingState.Failed.ordinal() ){    //WorkflowInfo.STATE_FAILURE
                    String errorReason = "";
                    try{
                        errorReason = "工作流处理失败";
                    } catch (Exception e) {
                        logger.warn("failed to getSubWorkflowInfo: " + wi.orderId, e);
                    }
                    ret.executingState=ExecutingState.Failed;
                    ret.errorReason=errorReason;            //"工作流处理失败！";  //todo 具体错误如何填写？？？
                    return ret;
                }
            }

            //todo 不再依据工作流的状态、而是根据元数据库记录的完整性来决定返回的状态
            //todo 20190822新增hold状态
            List<Mr0Info> ris;
            try {
                ris=mr0InfoService.getMr0Info(jobTaskID);
            } catch (Throwable e) {
                logger.error("failed to getR0Info: " + jobTaskID, e);
                ret.executingState=ExecutingState.Failed;
                ret.errorReason="查询原始码流元数据失败！"+e.getMessage();
                return ret;
            }
            if(ris.size()>2){   //目前来看，一个jobTaskID最多包括两个原始码流
                logger.error("R0Info incorrect for: " + jobTaskID);
                ret.executingState=ExecutingState.Failed;
                ret.errorReason="原始码流元数据记录数不正确！";
                return ret;
            }
            //生成每个R0文件的状态信息
            List<DataArchiveInfo> das=new ArrayList<>();
            List<DataExecutingState> dataExecutingStates= new ArrayList<>();

            DataArchiveInfo s1 =null;
            DataArchiveInfo s2 = null;
            try {
                if (ris.size()==1){
                    String channelId =ris.get(0).channelID;
                    if (channelId.equals("S1")){
                        s1 = generateFileInfo(order,ris.get(0),"S1",wi);
                    }else{
                        s1 = generateFileInfo(order,ris.get(0),"S2",wi);
                    }
                }else if (ris.size()==2){
                    //todo 根据轨道号获取正确的数据
                    String channelId = ris.get(1).channelID;
                    if (channelId.equals("S2")){
                        s1 = generateFileInfo(order,ris.get(0),"S1",wi);
                        s2 = generateFileInfo(order,ris.get(1),"S2",wi);
                        s2.channelID = ris.get(1).channelID;
                        s2.dataFileName = ris.get(1).signalID+"."+ Constants.EXT_DAT;
                    }else{
                        s1 = generateFileInfo(order,ris.get(1),"S1",wi);
                        s2 = generateFileInfo(order,ris.get(0),"S2",wi);
                        s2.channelID = ris.get(0).channelID;
                        s2.dataFileName = ris.get(0).signalID+"."+ Constants.EXT_DAT;
                    }
                }

            } catch (Throwable e) {
                logger.error("failed to generate DataArchiveInfo: " + jobTaskID+", "+ris.get(0),e);
                ret.executingState=ExecutingState.Failed;
                ret.errorReason="系统内部错误！"+e.getMessage();
                return ret;
            }
            if(s1!=null){
                das.add(s1);
                dataExecutingStates.add(s1.dataExecutingState);
            }if (s2!=null){
                das.add(s2);
                dataExecutingStates.add(s2.dataExecutingState);
            }
            ret.dataArchiveInfo=das;
            //综合判断job的状态。
            ret.executingState=ExecutingState.mergeState(dataExecutingStates);
            switch (ret.executingState){  //不会出现Processing的情况
                case Failed:
                    //ret.errorReason="原始码流解压失败或原始条带编目失败！";       //todo 若部分通道解压成功也继续触发编目，则使用此失败原因！！！
                    ret.errorReason="原始条带编目失败！";
                    ret.dataArchiveInfo=null;
                    return ret;
                case PartialSuccess:
                    //ret.errorReason="部分原始码流解压失败或部分原始条带编目失败！";       //todo 若部分通道解压成功也继续触发编目，则使用此失败原因！！！
                    ret.errorReason="部分原始条带编目失败！";
                    return ret;
            }
            return ret;  //Completed
        }


        private DataArchiveInfo generateFileInfo(WorkflowOrder order,Mr0Info ri,String chanelId,ArchiveWorkflowInfo wi){
            DataArchiveInfo ret = new DataArchiveInfo();
            ret.satellite= Satellite.valueOf(ri.satellite);
            ret.channelID=ri.channelID;
            ret.dataFileName=ri.signalID+"."+ Constants.EXT_DAT;
            ret.receiveDataStartTime=ri.receiveStartTime.substring(0,19);
            ret.receiveDataEndTime=ri.receiveEndTime.substring(0,19);
            ret.executingStartTime=order.getStartTime().substring(0,19);
            ret.executingEndTime=order.getEndTime().substring(0,19);
            DataArchiveInfo dataStatus = new DataArchiveInfo();
            //查询L0信息，通过查询结果情况，判断该R0文件的处理状态
            List<Ml0Info> lis = new ArrayList<>();
            try {
                switch (ret.satellite){
                    case GF1B:
                    case GF1C:
                    case GF1D:
                    case CASEARTH:
                        //Thread.sleep(240000);
                        lis = ml0InfoService.getL0Info(wi.jobTaskID,ri.signalID);      //表gt_m_l0中
                        break;
                    case ZY3B:
                        lis = ml0InfoService.getL0Info(wi.jobTaskID,"");
                        break;
                }
            } catch (Throwable e) {
                logger.error("failed to getL0Info: " + wi.jobTaskID + "," + ri.signalID, e);
                List<SensorDataArchiveInfo> sas = new ArrayList<>();
                SensorDataArchiveInfo sa = new SensorDataArchiveInfo();
                sa.sensorName = "";
                sa.sensorDataStartTime = DateUtil.getTime();
                sa.sensorDataEndTime = DateUtil.getTime();
                sas.add(sa);
                ret.dataExecutingState=DataExecutingState.Failed;
                return ret;
            }

            //一个原始码流下的每个条带都必须有对应的编目记录，该原始码流数据才算处理成功
            List<SensorDataArchiveInfo> sas = new ArrayList<>();
            if (!ret.satellite.equals("ZY3B")&&lis.size()<=1){
                Ml0Info li = new Ml0Info();
                li.segmentid = "test";
                li.dataStarttime= order.getStartTime().substring(0,19);
                li.dataEndtime =  order.getEndTime().substring(0,19);
                lis.add(li);
            }
            for (Ml0Info i : lis) {
                try {
                    //Thread.sleep(12000);
                    List<Mcat> sis = mcatManagerService.getCatInfo(wi.jobTaskID, i.segmentid);          //表gt_m_cat中
                    if (sis.size()==0||sis==null){
                        dataStatus.dataExecutingState = DataExecutingState.Failed;
                    }else {
                        dataStatus.dataExecutingState = DataExecutingState.Completed;
                    }
                } catch (Throwable e) {
                    logger.error("failed to getCatInfo: " + wi.jobTaskID + "," + i.segmentid, e);
                    ret.dataExecutingState = DataExecutingState.Failed;
                    return ret;
                }
                SensorDataArchiveInfo sa = new SensorDataArchiveInfo();
                try {
                    switch (ret.satellite){
                        case GF1B:
                        case GF1C:
                        case GF1D:
                        case CASEARTH:
                            sa=getSensorData("2m8mTDICCD",i.dataStarttime,i.dataEndtime);
                            sas.add(sa);
                            break;
                        case ZY3B:
                            if (chanelId.equals("S1")){
                                if (i.sensor.equals("NAD")){
                                    sa=getSensorData("NAD",i.dataStarttime,i.dataEndtime);
                                }if (i.sensor.equals("MUX")){
                                    sa=getSensorData("MUX",i.dataStarttime,i.dataEndtime);
                                }
                                sas.add(sa);
                            }else if (chanelId.equals("S2")){
                                if (i.sensor.equals("NAD")){
                                    sa=getSensorData("FWD",i.dataStarttime,i.dataEndtime);
                                    sas.add(sa);
                                    sa=getSensorData("BWD",i.dataStarttime,i.dataEndtime);
                                    sas.add(sa);
                                }
                            }
                            break;
                    }
                }catch (Exception e){
                    logger.info(e);
                }
            }
            List<SensorDataArchiveInfo> sasSingle = Sensor.toSimplify(sas);             //参数获取
            if (ret.satellite!=Satellite.ZY3B){
                if (sasSingle.size()==2){
                    sasSingle.remove(0);
                }
            }
            ret.sensorDataArchiveInfo = sasSingle;
            ret.dataExecutingState = dataStatus.dataExecutingState;
            if (ret.satellite==Satellite.ZY3B){
                ret.satellite=Satellite.ZY302;
            }
            return ret;
        }
    public SensorDataArchiveInfo getSensorData(String sensor,String dataStartTime, String dataEndTime)throws Exception{
        SensorDataArchiveInfo sa = new SensorDataArchiveInfo();
        try {
            Date startTime = null,endTime = null;
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startTime = sd.parse(dataStartTime);
            endTime = sd.parse(dataEndTime);
            long startTime1 = (long)(startTime.getTime()+8*60*60*1000);
            long endTime1 = (long)(endTime.getTime()+8*60*60*1000);
            sa.sensorName = sensor;
            sa.sensorDataStartTime = sd.format(startTime1);
            sa.sensorDataEndTime = sd.format(endTime1);
        }catch (Exception e){
            logger.info(e);
        }
        return sa;
    }

}
