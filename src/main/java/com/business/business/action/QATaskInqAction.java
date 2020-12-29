package com.business.business.action;

import com.business.business.Service.McatManagerService;
import com.business.business.Service.ProcessInfoService;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.config.Config;
import com.business.business.constants.Constants;
import com.business.business.entity.*;
import com.business.business.enums.Sensor;
import com.business.business.db.ProcessInfoImpl;
import com.business.business.info.*;
import com.business.business.adapter.String2ListListXmlAdapter;
import com.business.business.adapter.String2ListXmlAdapter;
import com.business.business.entity.workFlowTree;
import com.business.business.enums.*;
import com.business.business.message.*;
import com.business.business.util.DateUtil;
import com.business.business.util.MyHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * input->文件名称：QATaskInq_OMO_DQA_YYYYMMDDhhmmss.xml
 *             如：QATaskInq_OMO_DQA_20100101200000.xml
 *
 * output->文件名称：QATaskRep_DQA_OMO_任务单流水号_YYYYMMDDhhmmss.xml
 *              如：QATaskRep_DQA_OMO_QA2010000001_20100101200000.xml   (查出一个任务单)
 *              如：QATaskRep_DQA_OMO_QA0000000000_20100101200000.xml   (查出多个任务单时)
 *        报告名称：QAReport_卫星简称_任务单流水号_YYYYMMDDhhmmss.xls
 *                 QAReport_GF01_QA20100001_20100101200000.xls
 *        注意，Completed时，才生成<QAreportFileName>标签，但不用生成实际的报告文件。
 *
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午7:31
 * To change this template use File | Settings | File Templates.
 */
public class QATaskInqAction  {
    private static final Logger logger = Logger.getLogger(QATaskInqAction.class);
    private ProcessInfoImpl processInfoImpl;
    private WorkFlowOrderService orderService;
    private McatManagerService mcatService;
    private Marshaller marshaller;
    private static Unmarshaller unmarshaller;
    private ProcessInfoService processService;
    private Properties properties = new Properties();
    public QATaskInqAction() throws Exception {
        //初始化schema
        // InstructionType.initializeSchemas();

        //注意，以包的形式构建，需在包目录下的jaxb.index文件中加入所有的映射类名（只需加最外层类，而父类和引用的类都不需要加入）
        JAXBContext jc = JAXBContext.newInstance(QATask.class.getPackage().getName());      //以包形式构建xml，JAXBC可将xml与类相互转换
        unmarshaller = jc.createUnmarshaller();
        marshaller = jc.createMarshaller();
        //standard properties
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //eclipse-specified properties  todo 如何设置输出empty标签的格式？默认为<e/>，如何设为<e></e>？赋值""而不是null？
        //marshaller.setProperty(MarshallerProperties.INDENT_STRING, "    "); //TAB

        //启动后台检查线程
//        new TaskCheckThread(this);
    }




    public TaskBasicRepInfo generateTaskInfo(QATaskWorkflowInfo wi)throws Exception{
        //orderService = new WorkFlowOrderService();
        TaskBasicRepInfo ret=new TaskBasicRepInfo();
        ret.taskSerialNumber=wi.taskId;
        ret.jobTaskID= MyHelper.string2StringList(wi.jobTaskID, String2ListXmlAdapter.DELIMIT);
        ret.taskExecutedInfo=new ArrayList<>();
        logger.info("调用生成完成通知方法，生成rep"+wi);
        for(String taskMode:MyHelper.string2StringList(wi.taskMode,String2ListXmlAdapter.DELIMIT)){
            TaskExecutedInfo t=new TaskExecutedInfo();
            ret.taskExecutedInfo.add(t);
            t.taskMode=taskMode;
            t.executingStartTime=wi.createTime.substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
            //分情况
            if (wi.reply== 2){
                t.executingState= ExecutingState.Failed;
                t.errorReason="任务已被运管取消！";
                t.executingEndTime=wi.updateTime;
                continue;
            }
            //todo 根据任务状态进行判断state
            WorkflowOrder order = orderService.findById(wi.taskId);
            if (order!=null){
                ret.taskStatus="Exist";
            }else{
                ret.taskStatus="Non-existent";
            }
            if (order.getOrderStatus().equals("3")){
                wi.state = 2;
            }else if (order.getOrderStatus().equals("4")){
                wi.state=4;
            }else if (order.getOrderStatus().equals("1")){
                wi.state=0;
                t.executingStartTime = null;
            }else if (order.getOrderStatus().equals("2")){
                wi.state=1;
            }
            if (wi.state== ExecutingState.Processing.ordinal()){  //WorkflowInfo.STATE_RUNNING，正在处理
                t.executingState=ExecutingState.Processing;
                continue;
            }

            //剩下的是wi.state== QATaskWorkflowInfo.STATE_PARTIAL||QATaskWorkflowInfo.STATE_SUCCESS
            //todo 简化处理，只有成功或部分成功时，才列出详细的DataExecuteInfo
            //如果查询元数据失败，则不列出详细的DataExecuteInfo
            //分模式

                File reportFile = null;
                try{
                    t.QAreportFileName=new File(wi.QAReportFile).getName();
                    File ttt = new File(Config.dataBank_dir,wi.QAReportFile);
                    File qaFile = new File(ttt.getParent());            //得到父目录路径
                    boolean flag = true;
                    int i=0;
                    while (flag){
                        i++;
                        Thread.sleep(2000);
                        if (qaFile.listFiles().length!=0||i==100){
                            flag = false;
                        }
                    }
                    if (qaFile.listFiles().length==1){
                        reportFile = qaFile.listFiles()[0];
                    }else if (qaFile.listFiles().length>1){
                        reportFile = qaFile.listFiles()[0];
                        for (int j=0;j<qaFile.listFiles().length-1;j++){
                            if (reportFile.lastModified()<qaFile.listFiles()[j+1].lastModified()){    //lastModified()返回表示此抽象路径名的文件的最后修改时间。
                                reportFile = qaFile.listFiles()[j+1];                                   //只要最后修改的报告文件
                            }
                        }
                    }
                    t.QAreportFileName = reportFile.getName();
                    String[]typestr = order.getOrderType().split("_");
                    String name1 = typestr[0]+"_"+typestr[2]+"_"+typestr[1]+"_";
                    String name =Config.toOMO_backup+"/sendingBak/"+name1+t.QAreportFileName;
                    FileUtils.copyFile(reportFile,new File(name));
                }catch (Exception e){
                    logger.info("copy file Exception is"+e);
                }
            if (order.getEndTime()!=null){
                t.executingEndTime=order.getEndTime().substring(0,19);
            }else {
                t.executingEndTime=DateUtil.getTime();
            }if (order.getOrderStatus().equals("2")||order.getOrderStatus().equals("1")){
                t.executingEndTime = null;
            }
            //t.executingEndTime=wi.updateTime;
            if (TaskMode.Q65.name().equals(taskMode)){ //没有部分成功情况；没有DataExecuteInfo
                t.executingState=ExecutingState.Completed;
                t.executingStartTime = order.getStartTime().substring(0,19);
                t.executingEndTime = order.getEndTime().substring(0,19);
            }else if (TaskMode.Q64.name().equals(taskMode)){ //没有部分成功情况
                if (order.getOrderStatus().equals("1")){
                    t.executingState=ExecutingState.Hold;
                }else{
                    t.executingState=ExecutingState.Completed;
                    t.dataExecuteInfo=generateFileInfoQ64(wi);
                }
            }else if (TaskMode.Q61.name().equals(taskMode)){ //没有部分成功情况
                if (order.getOrderStatus().equals("1")){
                    t.executingState=ExecutingState.Hold;
                }else {
                    t.dataExecuteInfo = generateFileInfoQ61(order,wi);
                    TaskExecutedInfo tQ61 = getTaskInfo(t.dataExecuteInfo);
                    t.executingState = tQ61.executingState;
                    if (tQ61.errorReason!=null&&!tQ61.equals("")){
                        t.QAreportFileName = null;
                        t.errorReason = tQ61.errorReason;
                    }
                }
            }else if (TaskMode.Q62.name().equals(taskMode)){ //没有部分成功情况
                if (order.getOrderStatus().equals("1")){
                    t.executingState=ExecutingState.Hold;
                }else {
                    t.dataExecuteInfo = generateFileInfoQ62(order,wi);
                    TaskExecutedInfo tQ62 = getTaskInfo(t.dataExecuteInfo);
                    t.executingState = tQ62.executingState;
                    if (tQ62.errorReason!=null&&!tQ62.equals("")){
                        t.QAreportFileName = null;
                        t.errorReason = tQ62.errorReason;
                    }
                }
            }else if (TaskMode.Q63.name().equals(taskMode)){ //存在部分成功情况
                //t.executingState=(wi.state==WorkflowInfo.STATE_PARTIAL?ExecutingState.PartialSuccess:ExecutingState.Completed);
                if (order.getOrderStatus().equals("1")){
                    t.executingState=ExecutingState.Hold;
                }else {
                   // t.executingState = (wi.state == ExecutingState.PartialSuccess.ordinal() ? ExecutingState.PartialSuccess : ExecutingState.Completed);
                    t.dataExecuteInfo = generateFileInfoQ63(order,wi);
                    TaskExecutedInfo tQ63 = getTaskInfo(t.dataExecuteInfo);
                    t.executingState = tQ63.executingState;
                    if (tQ63.errorReason!=null&&!tQ63.equals("")){
                        t.QAreportFileName = null;
                        t.errorReason = tQ63.errorReason;
                    }
                }
            }
        }
        return ret;
    }
    public TaskExecutedInfo getTaskInfo(List<DataExecuteInfo> dsList)throws Exception{
        TaskExecutedInfo t = new TaskExecutedInfo();
        String status = "";
        for (DataExecuteInfo ds:dsList){
            if (ds.dataExecutingState==DataExecutingState.Completed){
                status = "success";
                if (status.equals("success")||status.equals("")){
                    t.executingState = ExecutingState.Completed;
                }
                if (status.equals("failed")){
                    t.executingState = ExecutingState.PartialSuccess;
                    break;
                }
                continue;
            }else if (ds.dataExecutingState==DataExecutingState.Failed){
                if (status.equals("failed")||status.equals("")){
                    t.executingState = ExecutingState.Failed;
                    status = "failed";
                }
                if (status.equals("success")){
                    t.executingState = ExecutingState.PartialSuccess;
                    break;
                }

            }else if (ds.dataExecutingState==DataExecutingState.Processing){
                t.executingState = ExecutingState.Processing;
                break;
            }
        }
        if (t.executingState==ExecutingState.PartialSuccess||t.executingState==ExecutingState.Failed){
            t.errorReason="工作流处理失败";
        }

        return t;
    }
    private List<DataExecuteInfo> generateFileInfoQ64(QATaskWorkflowInfo wi){
        processInfoImpl = new ProcessInfoImpl();
        //TODO ？？？差异性分析是针对原始码流文件。jobTaskID必须为两个
        List<String> jobTaskIDs= MyHelper.string2StringList(wi.jobTaskID,String2ListXmlAdapter.DELIMIT);
        List<R0Info> infos;
        try {
            infos=processInfoImpl.getR0Info(jobTaskIDs);
        } catch (Exception e) {
            logger.warn("failed to getR0Info: " + jobTaskIDs, e);
            return null;
        }
        List<DataExecuteInfo> ret=new ArrayList<>();
        for(R0Info i:infos){
            DataExecuteInfo d=new DataExecuteInfo();
            d.dataFileName=i.signalID+"."+Constants.EXT_DAT;
            d.dataExecutingState= DataExecutingState.Completed;
            d.dataExecutingStartTime=wi.createTime.substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
            d.dataExecutingEndTime=wi.updateTime.substring(0,19);
            ret.add(d);
        }
        return ret;
    }
    public List<DataExecuteInfo> getdata(List<R0Info> infos,WorkflowOrder order,String taskMode)throws Exception{
        List<DataExecuteInfo> ret = new ArrayList<>();
        //processService = new ProcessInfoManager();
        List<ProcessInfo>Q61infoList = processService.getProcessList(order.getTaskSerialNumber(),"KJ125_R0_TO_R0REPORT");
        if (Q61infoList.size()==1&&Q61infoList.get(0).getStatus().equals("Completed")){
            DataExecuteInfo d=new DataExecuteInfo();
            if (taskMode.equals("Q61")){
                d.dataExecutingState=DataExecutingState.Completed;
            }else {
                d.dataExecutingState=DataExecutingState.Failed;
            }
            d.dataFileName=infos.get(0).signalID+"."+Constants.EXT_DAT;
            d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
            d.dataExecutingEndTime=order.getEndTime().substring(0,19);
            ret.add(d);
        }else if (Q61infoList.size()==1&&Q61infoList.get(0).getStatus().equals("Aborted")){
            DataExecuteInfo d=new DataExecuteInfo();
            d.dataFileName=infos.get(0).signalID+"."+Constants.EXT_DAT;
            d.dataExecutingState=DataExecutingState.Failed;
            d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
            d.dataExecutingEndTime=order.getEndTime().substring(0,19);
            ret.add(d);
        }else if (Q61infoList.size()==2&&infos.size()==1&&order.getOrderStatus().equals("4")){
            String signalID2 = infos.get(0).signalID;
            String signalId ="";
            if (signalID2.substring(5,7).equals("01")){
                signalId = signalID2.replace("_01","_02").replace("1_R","2_R").replace("0_R","1_R");
            }else {
                signalId = signalID2.replace("_02","_01").replace("2_R","1_R").replace("1_R","0_R");
            }
            DataExecuteInfo d=new DataExecuteInfo();
            if (taskMode.equals("Q61")){
                d.dataExecutingState=DataExecutingState.Completed;
            }else {
                d.dataExecutingState=DataExecutingState.Failed;
            }
            d.dataFileName=infos.get(0).signalID+"."+Constants.EXT_DAT;
            d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
            d.dataExecutingEndTime=order.getEndTime().substring(0,19);
            ret.add(d);
            DataExecuteInfo d2=new DataExecuteInfo();
            d2.dataFileName = signalId+"."+Constants.EXT_DAT;
            d2.dataExecutingState=DataExecutingState.Failed;
            d2.dataErrorReason = "工作流处理失败";
            d2.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
            d2.dataExecutingEndTime=order.getEndTime().substring(0,19);
            ret.add(d2);
        }
        return ret;
    }
    private List<DataExecuteInfo> generateFileInfoQ61(WorkflowOrder order,QATaskWorkflowInfo wi) throws Exception{
        processInfoImpl = new ProcessInfoImpl();
        String status1 = "";
        List<R0Info> infos = null;
        try {
            for (int k=0;k!=50;k++){
                Thread.sleep(2000);
                infos = processInfoImpl.getR0Info(order.getJobTaskID(), MyHelper.string2StringList(wi.channel, String2ListXmlAdapter.DELIMIT));
                if (infos.size()!=0){
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn("failed to getR0Info: " + wi.jobTaskID + ", " + wi.channel, e);
            return null;
        }
        List<DataExecuteInfo> ret = new ArrayList<>();
        if (infos.size()==0||infos==null){
            List<R0Info>infoList = processInfoImpl.getR0Info(wi.jobTaskID);
            for (R0Info i:infoList){
                DataExecuteInfo d=new DataExecuteInfo();
                d.dataFileName = i.signalID+"."+Constants.EXT_DAT;
                d.dataExecutingState=DataExecutingState.Failed;
                d.dataErrorReason = "归档流程处理失败";
                d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
                d.dataExecutingEndTime=order.getEndTime().substring(0,19);
                ret.add(d);
            }
        }
        if (infos.size()==1&&!order.getSatelliteName().equals("CSES")){
           ret = getdata(infos,order,"Q61");
        }else {
            for (R0Info i : infos) {
                DataExecuteInfo d = new DataExecuteInfo();
                d.dataFileName = i.signalID + "." + Constants.EXT_DAT;
                switch (order.getOrderStatus()) {
                    case "2":
                        d.dataExecutingState = DataExecutingState.Processing;
                        d.dataExecutingStartTime = order.getStartTime().substring(0, 19);
                        break;
                    case "3":
                        d.dataExecutingState = DataExecutingState.Completed;
                        d.dataExecutingStartTime = order.getStartTime().substring(0, 19);
                        d.dataExecutingEndTime = order.getEndTime().substring(0, 19);
                        break;
                    case "4":
                        if (infos.size()==2){
                            d.dataExecutingState=DataExecutingState.Completed;
                        }else {
                            d.dataExecutingState=DataExecutingState.Failed;
                            d.dataErrorReason = "工作流处理失败";
                        }
                        d.dataExecutingStartTime = order.getStartTime().substring(0, 19);
                        d.dataExecutingEndTime = order.getEndTime().substring(0, 19);
                        break;
                }
                ret.add(d);
            }
    }
        return ret;
    }

    private List<DataExecuteInfo> generateFileInfoQ62(WorkflowOrder order,QATaskWorkflowInfo wi)throws Exception{
        List<L0Info> infos = null;
        processInfoImpl = new ProcessInfoImpl();
        try {
            infos=processInfoImpl.getL0Info(wi.jobTaskID, Sensor.fromOMOSensors(wi.satellite, String2ListListXmlAdapter.toList(wi.sensor)));
        } catch (Exception e) {
            logger.warn("failed to getL0Info: " + wi.jobTaskID + ", " + wi.sensor, e);
            return null;
        }
        List<DataExecuteInfo> ret=new ArrayList<>();
        if (infos.size()==0||infos==null){
            List<R0Info>infoList = processInfoImpl.getR0Info(order.getJobTaskID(), MyHelper.string2StringList(wi.channel, String2ListXmlAdapter.DELIMIT));
            ret = getdata(infoList,order,"");
        }else {
            for(L0Info i:infos){
                DataExecuteInfo d=new DataExecuteInfo();
                d.dataFileName=i.segmentID+"."+Constants.EXT_DAT;
                switch (order.getOrderStatus()){
                    case "2":
                        d.dataExecutingState=DataExecutingState.Processing;
                        d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
                        break;
                    case"3":
                        d.dataExecutingState=DataExecutingState.Completed;
                        d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
                        d.dataExecutingEndTime=order.getEndTime().substring(0,19);
                        break;
                    case"4":
                        if (infos.size()!=0){
                            d.dataExecutingState=DataExecutingState.Completed;
                        }else {
                            d.dataExecutingState=DataExecutingState.Failed;
                            d.dataErrorReason = "工作流处理失败";
                        }
                        d.dataExecutingStartTime=order.getStartTime().substring(0,19); //todo 统一按主流程的开始、结束时间吧！有必要细分吗？？？
                        d.dataExecutingEndTime=order.getEndTime().substring(0,19);
                        break;
                }

                ret.add(d);
            }
        }
        return ret;
    }

    private List<DataExecuteInfo> generateFileInfoQ63(WorkflowOrder order,QATaskWorkflowInfo wi){
        //orderService = new WorkFlowOrderManager();
        processInfoImpl = new ProcessInfoImpl();
        //mcatService = new mCatManager();
        List<workFlowTree> subs;
        WorkflowOrder t = new WorkflowOrder();
        List<Mcat> ls = null;
        List<L0Info>infos = null;
        List<DataExecuteInfo> ret=new ArrayList<>();
        try {
            infos=processInfoImpl.getL0Info(wi.jobTaskID, Sensor.fromOMOSensors(wi.satellite, String2ListListXmlAdapter.toList(wi.sensor)));
            if (infos.size()==0||infos==null){
                List<R0Info>infoList = processInfoImpl.getR0Info(order.getJobTaskID(), MyHelper.string2StringList(wi.channel, String2ListXmlAdapter.DELIMIT));
                ret = getdata(infoList,order,"");
            }else{
                switch (order.getDataSelectType()){
                    case"AutoType":
                        ls = mcatService.selectSceneByAuto(order.getJobTaskID(),Sensor.fromOMOSensor(order.getSatelliteName(),""));
                        break;
                    case"Time":
                        ls = mcatService.selectSceneByTime(order.getJobTaskID(),Sensor.fromOMOSensor(order.getSatelliteName(),""),order.getSensorStartTime(),order.getSensorEndTime());
                        break;
                    case "Full":
                        ls = mcatService.selectSceneByFull(order.getJobTaskID(),Sensor.fromOMOSensor(order.getSatelliteName(),""));
                        break;
                    case"custom":
                        ls = mcatService.selectByCustom(order.getSceneID().split(";"));
                        break;
                }
                for (Mcat s : ls) {
                    DataExecuteInfo d=new DataExecuteInfo();
                    L2AInfo info = new L2AInfo();
                    try {
                        info=processInfoImpl.getL2AInfo(order.getJobTaskID(),s.getSceneid());  //Workflow_Tree表的info字段，对于Q63生产子流程为编目景ID
                        d.dataFileName=s.getSceneid()+"_"+Constants.LEVEL_L2A+"."+Constants.EXT_TIF;
                        if (info!=null){
                            d.dataExecutingState=DataExecutingState.Completed;
                            d.dataExecutingEndTime = info.notecreatetime.substring(0,19);
                        }else if (order.getOrderStatus().equals("2")&&info==null){
                            d.dataExecutingState=DataExecutingState.Processing;
                        }else if (!order.getOrderStatus().equals("2")&&info==null){
                            d.dataExecutingState=DataExecutingState.Failed;
                            String errorReason = "工作流处理失败";
                            errorReason += " jobtaskId:"+order.getJobTaskID()+" info:"+s.getSceneid();
                            d.dataErrorReason=errorReason;   //todo 具体错误如何填写？？？
                            d.dataExecutingEndTime = order.getEndTime().substring(0,19);
                        }
                    } catch (Throwable e) {
                        logger.warn("cannot query L2A product name!",e);
                        //todo 查询失败时，按命名规范构建一个L2A产品文件名，生成次数固定填01。运管并不实际验证此文件名，因此无关紧要

                    }
                    d.dataExecutingStartTime = order.getStartTime().substring(0,19);
                    ret.add(d);
                }
            }

        } catch (Exception e) {
            logger.warn("failed to getSubWorkflowInfo: " + wi.orderId, e);
            return null;
        }
        return ret;
    }

}
