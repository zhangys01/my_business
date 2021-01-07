package com.business.action;

import com.business.Service.McatManagerService;
import com.business.Service.NomalManagerService;
import com.business.config.Config;
import com.business.entity.Mcat;
import com.business.entity.WorkflowOrder;
import com.business.enums.ProcessType;
import com.business.enums.TableName;
import com.business.util.DateUtil;
import com.business.util.ProcessUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
     */
    private static final Logger logger= Logger.getLogger(PRtaskAction.class);
    @Resource
    ProcessUtil processUtil;
    @Autowired
    private McatManagerService mcatManagerService;
    @Autowired
    private NomalManagerService nomalManagerService;
    @Resource
    private QATaskAction qaTaskAction;
    public void doTriggerQATask( WorkflowOrder t) throws Exception {
        switch (t.getSatelliteName()){
            case "GF-1B":
            case "GF-1C":
            case "GF-1D":
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
        deleteProduct("L2",mcat.getSceneid());
        //处理每一景
        //todo 根据是生产L1A还是都生产构建订单
        if (t.getProductLevel().equals("L1")) {
            String orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, mcat));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        } else {
            String orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, mcat));
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
          }
    }
    public void ZYProductL2A(WorkflowOrder t)throws Exception{
        //todo  生产订单没有jobTaskId
        Mcat s = mcatManagerService.selectBysceneId(t.getSceneID());
        if (t.getProductLevel().equals("L1")){
            deleteProduct("L1",s.getSceneid());
        }else{
            deleteProduct("L2",s.getSceneid());
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
            deleteProduct("L1",s.getSceneid());
        }else{
            deleteProduct("L2",s.getSceneid());
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
    public boolean deleteProduct(String type,String sceneid)throws Exception{
        switch (type){
            case "L1":
                List<String> tableList = TableName.getTableName("L1A");
                for (int i=0;i<tableList.size();i++){
                    nomalManagerService.deleteProductIdByL1A(tableList.get(i),sceneid+"_L1A");
                }
                break;
            case "L2":
                List<String> tableList1 = TableName.getTableName("L1A");
                for (int i=0;i<tableList1.size();i++){
                    nomalManagerService.deleteProductIdByL1A(tableList1.get(i),sceneid+"_L1A");
                }
                List<String>tableList2 = TableName.getTableName("L2A");
                for (int j=0;j<tableList2.size();j++){
                    nomalManagerService.deleteProductIdByL2A(tableList2.get(j),sceneid+"_L2A");
                }
                break;
        }
        return true;
    }
}
