package com.business.business.action;

import com.business.business.constants.Constants;
import com.business.business.Service.McatManagerService;
import com.business.business.Service.QATaskWorkFlowService;
import com.business.business.Service.TableManagerService;
import com.business.business.config.Config;
import com.business.business.entity.Mcat;
import com.business.business.entity.QATaskWorkFlow;
import com.business.business.entity.WorkflowOrder;
import com.business.business.enums.DataSelectType;
import com.business.business.enums.ProcessType;
import com.business.business.enums.ResponseType;
import com.business.business.enums.TaskMode;
import com.business.business.util.DateUtil;
import com.business.business.util.ProcessUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 11:20
 */
@Component
public class PRtaskAction {
    /**
     * 进行2级产品生产
     * @throws Exception
     */
    private static final Logger logger= Logger.getLogger(PRtaskAction.class);
    @Autowired
    private McatManagerService mcatManagerService;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private QATaskWorkFlowService qaTaskWorkFlowManagerService;
    @Autowired
    private QATaskAction qaTaskAction;
    public void doTriggerQATask( WorkflowOrder t) throws Exception {
        switch (t.getSatelliteName()){
            case "GF-1B":
            case"GF-1C":
            case"GF-1D":
            case "CESEARTH":
                GFProductL2A(t);
                break;
            case"ZY-3B":
                ZYProductL2A(t);
                break;
        }
    }
    public void GFProductL2A(WorkflowOrder t)throws Exception{
        //todo 根据订单景ID获取景
        // List<Mcat> ls = catManager.selectSceneByAuto(jobTaskID,null);
        Mcat mcat = mcatManagerService.selectBysceneId(t.getSceneID());
        tableManagerService.deleteProductIdByL2A(mcat.getSceneid()+"_L2A");
        tableManagerService.deleteProductIdByL1A(mcat.getSceneid()+"_L1A");
        String jobTaskID = mcat.getJobtaskid();
        Date time=new Date();  //统一用一个任务创建时间
        //处理每一景
        List<String> subOrderIds = new ArrayList<>();
        List<String> subInfos = new ArrayList<>();  //景ID列表
        //todo 根据是生产L1A还是都生产构建订单
        if (t.getProductLevel().equals("L1")){
            String orderXml = "";
            if (t.getSceneID().split("_")[1].equals("PA")){
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,mcat));
            }else{
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,mcat));
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            String orderId = ProcessUtil.submitProcess(orderXml, Config.submit_order_timeout);
            subOrderIds.add(orderId);
            subInfos.add(mcat.getSceneid());    //子流程信息字段填入景ID，便于后期查询相关信息
        }else {
            String orderXml = "";
            if (t.getSceneID().split("_")[1].equals("PA")){
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,mcat));
            }else {
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,mcat));
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            String orderId = ProcessUtil.submitProcess(orderXml,Config.submit_order_timeout);
            subOrderIds.add(orderId);
            subInfos.add(mcat.getSceneid());    //子流程信息字段填入景ID，便于后期查询相关信息
        }
        //记录虚拟主流程及子流程信息。如果记录失败，前面提交的工作流仍然会处理，只是生成垃圾数据
        String virtualId =t.getTaskSerialNumber();  //虚拟主流程的orderid格式为：DUMMY_YYYYMMDD_XXXXXX
        //内部自动触发时，taskId必须为作业任务编号，以便归档任务能够与评价任务关联获取报表
        QATaskWorkFlow qatask = new QATaskWorkFlow();
        qatask.setTaskid(t.getTaskSerialNumber());
        qatask.setOrderid(jobTaskID);
        qatask.setOriginator("0");
        qatask.setTaskinfo(generateTaskInfo(jobTaskID,mcat,time));
        qatask.setCreatetime(DateUtil.getTime());
        qatask.setUpdatetime(DateUtil.getTime());
        qaTaskWorkFlowManagerService.saveQaTask(qatask);
        // treeManager.saveTreeSubWorkFlow(virtualId,DateUtil.getTime(),subOrderIds,subInfos);
        //db.saveQATaskWorkflow_Q63(jobTaskID, virtualId,0, generateTaskInfo(jobTaskID, ls, time),time,subOrderIds, subInfos);
    }
    public void ZYProductL2A(WorkflowOrder t)throws Exception{
        //todo  生产订单没有jobTaskId
        Mcat s = mcatManagerService.selectBysceneId(t.getSceneID());
        if (t.getProductLevel().equals("L1")){
            tableManagerService.deleteProductIdByL1A(s.getSceneid()+"_L1A");
            tableManagerService.deleteProductIdByL1A(s.getSceneid()+"_L1A");
        }else{
            tableManagerService.deleteProductIdByL1A(s.getSceneid()+"_L1A");
            tableManagerService.deleteProductIdByL1A(s.getSceneid()+"_L2A");
        }
        String jobTaskID = s.getJobtaskid();
        logger.info("auto-triggering QATask for: " + jobTaskID);
        Date time=new Date();  //统一用一个任务创建时间
        //处理每一景
        List<String> subOrderIds = new ArrayList<>();
        List<String> subInfos = new ArrayList<>();  //景ID列表
        String sensorName = t.getSceneID().split("_")[1];
        //todo 根据是生产L1A还是都生产构建订单
        if (t.getProductLevel().equals("L1")){
            String orderXml = ProcessType.ZY3B_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            String orderId = ProcessUtil.submitProcess(orderXml,Config.submit_order_timeout);
            subOrderIds.add(orderId);
            subInfos.add(s.getSceneid());
        }else {
            String orderXml = ProcessType.ZY3B_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            String orderId = ProcessUtil.submitProcess(orderXml,Config.submit_order_timeout);
            subOrderIds.add(orderId);
            subInfos.add(s.getSceneid());
        }
        //记录虚拟主流程及子流程信息。如果记录失败，前面提交的工作流仍然会处理，只是生成垃圾数据
        String virtualId =t.getTaskSerialNumber();  //虚拟主流程的orderid格式为：DUMMY_YYYYMMDD_XXXXXX
        //内部自动触发时，taskId必须为作业任务编号，以便归档任务能够与评价任务关联获取报表
        QATaskWorkFlow qatask = new QATaskWorkFlow();
        qatask.setTaskid(t.getTaskSerialNumber());
        qatask.setOrderid(jobTaskID);
        qatask.setUpdatetime(DateUtil.getTime());
        qaTaskWorkFlowManagerService.saveQaTask(qatask);
        //treeManager.saveTreeSubWorkFlow(virtualId,DateUtil.getTime(),subOrderIds,subInfos);
        //db.saveQATaskWorkflow_Q63(jobTaskID, virtualId,0, generateTaskInfo(jobTaskID, ls, time),time,subOrderIds, subInfos);
    }

    private String generateTaskInfo(String jobTaskID,Mcat ls,Date time) {
        /**
         * 常规流程自动触发Q61、62、63组合模式、所有通道和传感器、自动选景方式的评价任务
         *
         *taskinfo格式如下（根据需要可增加项目）：
         *<t>
         *  <satellite>GF01</satellite>    #注意是卫星简称
         *  <taskMode>Q61;Q62;Q63</taskMode>
         *  <jobTaskID>JOB201309230001001</jobTaskID>
         *  <channelID></channelID>
         *  <sensor></sensor>
         *  <dataSelectType>AutoType</dataSelectType>
         *  <sceneCountQ63>12</sceneCountQ63>    #Q63模式下生成的单景评价流程个数
         *  <QAReportFile>GF01/REPORT/201312/20131212/QAReport_GF01_QA0000000000_20131212235959/QAReport_GF01_QA0000000000_20131212235959.xls</QAReportFile>  #相对路径
         *</t>
         */
        WorkflowOrder order = new WorkflowOrder();
        StringBuffer sb=new StringBuffer();
        String satellite=ls.getSatelliteid();
        sb.append("<t>");
        sb.append("<satellite>"+ satellite+"</satellite>");
        sb.append("<taskMode>"+ TaskMode.toAutoTaskModes()+"</taskMode>");
        sb.append("<jobTaskID>"+jobTaskID+"</jobTaskID>");
        sb.append("<channel></channel>");
        sb.append("<sensor></sensor>");
        sb.append("<dataSelectType>"+ DataSelectType.AutoType+"</dataSelectType>");
        sb.append("<sceneCountQ63>"+1+"</sceneCountQ63>");
        sb.append("<QAReportFile>"+ ResponseType.buildQAReportFileRelativePath(jobTaskID,ls.getSatelliteid(), Constants.TASK_SERIAL_NUMBER)+"</QAReportFile>"); //常规评价时，报表文件命名中使用特殊的任务单流水号
        sb.append("</t>");
        return sb.toString();
    }

}
