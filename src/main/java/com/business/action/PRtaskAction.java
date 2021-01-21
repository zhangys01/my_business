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
import com.business.util.ReportUtil;
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
    @Resource
    private ReportUtil reportUtil;
    public void doTriggerQATask( WorkflowOrder t) throws Exception {
        //todo 根据订单景ID获取景
        //todo  生产订单没有jobTaskId
        Mcat s = mcatManagerService.selectBysceneId(t.getSceneID());
        if (t.getProductLevel().equals("L1")){
            deleteProduct("L1",s.getSceneid());
        }else{
            deleteProduct("L2",s.getSceneid());
        }
        //todo 根据是生产L1A还是都生产构建订单
        String orderXml = "";
        if (t.getProductLevel().equals("L1")) {
            switch (t.getSatelliteName()){
                case "GF-1B":
                case "GF-1C":
                case "GF-1D":
                    orderXml = ProcessType.GF1_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, s));
                break;
                case "ZY-3B":
                    orderXml = ProcessType.ZY3B_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, s));
                    break;
                case "ZY-1E":
                    orderXml = ProcessType.ZY1E_CAT_TO_L1A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, s));
                    break;
            }
            //提交流程
            processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        } else {
            switch (t.getSatelliteName()){
                case "GF-1B":
                case "GF-1C":
                case "GF-1D":
                    orderXml = ProcessType.GF1_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, s));
                    break;
                case "ZY-3B":
                    orderXml = ProcessType.ZY3B_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, s));
                    break;
                case "ZY-1E":
                    orderXml = ProcessType.ZY1E_CAT_TO_L2A.generateOrderXml(qaTaskAction.generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(), t, s));
                    break;
            }
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
