package com.business.business.action;

import com.business.business.entity.WorkflowOrder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     *//*
    public void doTriggerQATask( WorkflowOrder t) throws Exception {
        switch (t.getSatelliteName()){
            case "GF-1B":
            case"GF-1C":
            case"GF-1D":
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
        Mcat mcat = catManager.selectBysceneId(t.getSceneID());
        processInfoimpl.deleteProductIdByL1A("GT_M_L2",mcat.getSceneid()+"_L2A");
        processInfoimpl.deleteProductIdByL1A("GT_R_L2",mcat.getSceneid()+"_L2A");
        processInfoimpl.deleteProductIdByL1A("GT_R_L1",mcat.getSceneid()+"_L1A");
        processInfoimpl.deleteProductIdByL1A("GT_M_L1",mcat.getSceneid()+"_L1A");
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
                orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(generateOrderParamsForGF_L1A_TO_L2A(t,jobTaskID,mcat,time));
            }else{
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(generateOrderParamsForGF_L1A_TO_L2A(t,jobTaskID,mcat,time));
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            String orderId = ProcessUtil.submitProcess(orderXml,Config.submit_order_timeout);
            subOrderIds.add(orderId);
            subInfos.add(mcat.getSceneid());    //子流程信息字段填入景ID，便于后期查询相关信息
        }else {
            String orderXml = "";
            if (t.getSceneID().split("_")[1].equals("PA")){
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(generateOrderParamsForGF_L1A_TO_L2A(t,jobTaskID,mcat,time));
            }else {
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(generateOrderParamsForGF_L1A_TO_L2A(t,jobTaskID,mcat,time));
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
        qataskManager.saveQaTask(qatask);
        // treeManager.saveTreeSubWorkFlow(virtualId,DateUtil.getTime(),subOrderIds,subInfos);
        //db.saveQATaskWorkflow_Q63(jobTaskID, virtualId,0, generateTaskInfo(jobTaskID, ls, time),time,subOrderIds, subInfos);
    }
    public void ZYProductL2A(WorkflowOrder t)throws Exception{
        //todo  生产订单没有jobTaskId
        processInfoimpl = new ProcessInfoImpl();
        catManager = new mCatManager();
        qataskManager = new QATaskWorkFlowManager();
        treeManager = new WorkFlowTreeManager();
        Mcat s = catManager.selectBysceneId(t.getSceneID());
        if (t.getProductLevel().equals("L1")){
            processInfoimpl.deleteProductIdByL1A("GT_R_L1",s.getSceneid()+"_L1A");
            processInfoimpl.deleteProductIdByL1A("GT_M_L1",s.getSceneid()+"_L1A");
        }else{
            processInfoimpl.deleteProductIdByL1A("GT_R_L1",s.getSceneid()+"_L1A");
            processInfoimpl.deleteProductIdByL1A("GT_M_L1",s.getSceneid()+"_L1A");
            processInfoimpl.deleteProductIdByL1A("GT_M_L2",s.getSceneid()+"_L2A");
            processInfoimpl.deleteProductIdByL1A("GT_R_L2",s.getSceneid()+"_L2A");
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
            String orderXml = ProcessType.ZY3B_CAT_TO_L1A.generateOrderXml(generateOrderParamsForGF_L1A_TO_L2A(t,jobTaskID,s,time));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            String orderId = ProcessUtil.submitProcess(orderXml,Config.submit_order_timeout);
            subOrderIds.add(orderId);
            subInfos.add(s.getSceneid());
        }else {
            String orderXml = ProcessType.ZY3B_CAT_TO_L2A.generateOrderXml(generateOrderParamsForGF_L1A_TO_L2A(t,jobTaskID,s,time));
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
        qataskManager.saveQaTask(qatask);
        //treeManager.saveTreeSubWorkFlow(virtualId,DateUtil.getTime(),subOrderIds,subInfos);
        //db.saveQATaskWorkflow_Q63(jobTaskID, virtualId,0, generateTaskInfo(jobTaskID, ls, time),time,subOrderIds, subInfos);
    }*/
}
