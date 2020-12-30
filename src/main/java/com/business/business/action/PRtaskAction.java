package com.business.business.action;

import com.business.business.Service.McatManagerService;
import com.business.business.Service.TableManagerService;
import com.business.business.config.Config;
import com.business.business.entity.Mcat;
import com.business.business.entity.WorkflowOrder;
import com.business.business.enums.ProcessType;

import com.business.business.util.DateUtil;
import com.business.business.util.ProcessUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
    @Resource
    ProcessUtil processUtil;
    @Autowired
    private McatManagerService mcatManagerService;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private QATaskAction qaTaskAction;
    public void doTriggerQATask( WorkflowOrder t) throws Exception {
        switch (t.getSatelliteName()){
            case "GF-1B":
            case"GF-1C":
            case"GF-1D":
                GFProductL2A(t);
                break;
            case "CESEARTH":
                CASProductL2A(t);
                break;
            case"ZY-3B":
                ZYProductL2A(t);
                break;
        }
    }
    public void GFProductL2A(WorkflowOrder t)throws Exception {
        //todo 根据订单景ID获取景
        Mcat mcat = mcatManagerService.selectBysceneId(t.getSceneID());
        tableManagerService.deleteProductIdByL2A(mcat.getSceneid() + "_L2A");
        tableManagerService.deleteProductIdByL1A(mcat.getSceneid() + "_L1A");
        //处理每一景
        //todo 根据是生产L1A还是都生产构建订单
        if (t.getProductLevel().equals("L1")) {
            String orderXml = "";
            if (t.getSceneID().split("_")[1].equals("PA")) {
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, mcat));
            } else {
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, mcat));
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        } else {
            String orderXml = "";
            if (t.getSceneID().split("_")[1].equals("PA")) {
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, mcat));
            } else {
                //构建流程订单
                orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, mcat));
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
          }
    }
    public void ZYProductL2A(WorkflowOrder t)throws Exception{
        //todo  生产订单没有jobTaskId
        Mcat s = mcatManagerService.selectBysceneId(t.getSceneID());
        if (t.getProductLevel().equals("L1")){
            tableManagerService.deleteProductIdByL1A(s.getSceneid()+"_L1A");
        }else{
            tableManagerService.deleteProductIdByL2A(s.getSceneid()+"_L2A");
        }
        logger.info("auto-triggering QATask for: " + s.getJobtaskid());
        //todo 根据是生产L1A还是都生产构建订单
        if (t.getProductLevel().equals("L1")){
            String orderXml = ProcessType.ZY3B_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        }else {
            String orderXml = ProcessType.ZY3B_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        }
    }
    public void CASProductL2A(WorkflowOrder t)throws Exception{
        //todo  生产订单没有jobTaskId
        Mcat s = mcatManagerService.selectBysceneId(t.getSceneID());
        if (t.getProductLevel().equals("L1")){

            tableManagerService.deleteProductIdByL1A(s.getSceneid()+"_L1A");
        }else{
            tableManagerService.deleteProductIdByL2A(s.getSceneid()+"_L2A");
        }
        logger.info("auto-triggering QATask for: " + s.getJobtaskid());
        //todo 根据是生产L1A还是都生产构建订单
        if (t.getProductLevel().equals("L1")){
            String orderXml = ProcessType.CAS_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        }else {
            String orderXml = ProcessType.CAS_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        }
    }
}
