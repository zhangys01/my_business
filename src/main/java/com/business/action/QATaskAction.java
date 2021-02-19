package com.business.action;


import com.business.Service.*;
import com.business.config.Config;
import com.business.constants.Constants;
import com.business.entity.GtRr0;
import com.business.entity.Mcat;
import com.business.entity.WorkflowOrder;
import com.business.enums.*;
import com.business.util.DateUtil;
import com.business.util.MyHelper;
import com.business.util.ProcessUtil;
import com.business.util.CheckStatusUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * input->文件名称：QATask_OMO_DQA_任务单流水号_YYYYMMDDhhmmss.xml
 * 如：QATask_OMO_DQA_QA2010000001_20100101200000.xml
 * <p/>
 * output->文件名称：QATaskCon_DQA_OMO_任务单流水号_YYYYMMDDhhmmss.xml
 * 如：QATaskCon_DQA_OMO_QA2010000001_20100101200000.xml
 * <p/>
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-11
 * Time: 下午7:31
 * To change this template use File | Settings | File Templates.
 */
@Component
public class QATaskAction{
    private static final Logger logger = Logger.getLogger(QATaskAction.class);
    //todo 每个action都创建新实例，因此域变量不会共享冲突
    private String QAReportFile;    //相对路径
    private Integer sceneCountQ63;
    @Resource
    ProcessUtil processUtil;
    @Resource
    private CheckStatusUtil checkStatusUtil;
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private McatManagerService mcatManagerService;
    @Autowired
    private GtRr0ManagerService gtRr0ManagerService;
    @Autowired
    private NomalManagerService nomalManagerService;

    public synchronized  void process(WorkflowOrder order)throws Exception{
        String taskMode = order.taskMode;
        if (order.getTaskStatus().equals("New")){
            try{
                //先生成报告文件路径
                QAReportFile = ResponseType.buildQAReportFileRelativePath(order.getJobTaskID(),order.getSatelliteName().replace("-",""),order.getTaskSerialNumber());
                if (order.taskMode.contains("Q63")){
                    processQ63(order);
                }
                if(order.taskMode.contains(TaskMode.Q64.name())) {  //特殊处理，需先触发差异性分析流程
                    processQ64(order);
                }
                    ProcessType processType;
                    Map orderParams;
                    if (order.taskMode.contains("Q65")) {  //直接触发Q65评价流程
                        //todo q65直接触发流程
                        processType = ProcessType.KJ125_Q65;
                        orderParams = generateOrderParamsForGF_Q65(order);
                        //构建流程订单
                        String orderXml = processType.generateOrderXml(orderParams);
                        processUtil.submitProcess(orderXml, Config.submit_order_timeout);
                        //todo 提交流程 by 2019/2/22 kiven

                    }
                    if (order.taskMode.contains(TaskMode.Q61.name())){  //Q61/62组合，直接触发生成综合质量报告流程
                            //todo S1Fili和S2File 查询集中存储？
                        String S1ReportOrderXml=null,S2ReportOrderXml=null;
                        String satellite = order.getSatelliteName().replace("-","");
                        List<String>datList = new ArrayList<>();
                        datList = processQATask(order);
                        if (datList.size()==0||datList==null){
                            logger.info("原始数据不存在，无法发起Q61流程");
                        }else{
                            if (order.getSatelliteName().equals("CSES")){
                                if (datList.size()!=1){
                                    logger.info("电磁卫星原始数据不存在，无法发起Q61流程");
                                }else{
                                    File S1File = new File(datList.get(0));
                                    S1ReportOrderXml=ProcessType.KJ125_R0_TO_R0REPORT.generateOrderXml(generateOrderParamsForGF_R0_TO_R0REPORT(satellite,order,S1File,DateUtil.getSdfDate()));
                                    if(S1ReportOrderXml!=null) {
                                        processUtil.submitProcess(S1ReportOrderXml,Config.submit_order_timeout);
                                    }
                                }
                            }else{
                                File S1File = new File(datList.get(0));
                                File S2File = new File(datList.get(1));
                                S1ReportOrderXml=ProcessType.KJ125_R0_TO_R0REPORT.generateOrderXml(generateOrderParamsForGF_R0_TO_R0REPORT(satellite,order,S1File,DateUtil.getSdfDate()));
                                Thread.sleep(10000);
                                S2ReportOrderXml=ProcessType.KJ125_R0_TO_R0REPORT.generateOrderXml(generateOrderParamsForGF_R0_TO_R0REPORT(satellite,order, S2File, DateUtil.getSdfDate()));
                                if(S1ReportOrderXml!=null) {
                                    processUtil.submitProcess(S1ReportOrderXml,Config.submit_order_timeout);
                                }
                                if(S2ReportOrderXml!=null) {
                                    processUtil.submitProcess(S2ReportOrderXml,Config.submit_order_timeout);
                                }
                            }
                        }
                    }
                     if (taskMode.contains("Q62")){
                        if (taskMode.equals("Q61;Q62;Q63")){
                        }else if (taskMode.equals("Q62")){
                            processType = ProcessType.KJ125_Q61_62_63_QAReport;
                            orderParams = generateOrderParamsForGF_Q61_62_63_QAReport(order);
                            //构建流程订单
                            String orderXml = processType.generateOrderXml(orderParams);
                            processUtil.submitProcess(orderXml,Config.submit_order_timeout);
                        }
                    }
            } catch (Throwable e) {
                order.setOrderStatus("4");
                order.setEndTime(DateUtil.getTime());
                orderService.updateById(order);
                logger.error("cannot process QATask: " + order.getTaskSerialNumber(), e);
            }
        }
    }
    private Map<String,Object> generateOrderParamsForGF_R0_TO_R0REPORT(String satellite,WorkflowOrder order,File signalFile,String time) throws Exception {
        String[] item = signalFile.getName().split("_");
        Map<String, Object> map = new HashMap<>();
        map.put("TASKSERIALNUMBER",order.getTaskSerialNumber());
        map.put("YYYYMMDD_XXXXXX",time);
        map.put("SATELLITE", satellite);
        map.put("CHANNEL", Channel.fromId(signalFile.getName().split("_")[1]).name());
        map.put("JOBTASKID", order.getJobTaskID());
        map.put("SIGNALID", signalFile.getName().replace(".dat", ""));
        map.put("SIGNALFILE", signalFile);
        map.put("PINFILE", "pin.dat");
        //todo 更换路径
        File dir = new File(Config.dataBank_dir+"/"+satellite+"/SIGNAL/"+item[3].substring(0,6)+"/"+item[3]);
        MyHelper.CreateDirectory(dir);
        map.put("REPORT",dir+"/"+signalFile.getName().replace(".dat", ".report.xml"));
        return map;
    }

    public synchronized List<String> processQATask(WorkflowOrder t)throws Exception{
        //先删除R0表
        if (t.getSatelliteName().equals("ZY-3B"))t.setSatelliteName("ZY302");
        gtRr0ManagerService.removeById(t.getJobTaskID());
        List<String>datList = new ArrayList<>();
        boolean result = false;
        //按原始的列举法，WatchService只是用于快速响应
        //todo,现在监控目录变了，/raw/zone_H是data_dir,
        File datadir = new File(Config.data_dir+"/"+t.getSatelliteName()+"/"+t.getJobTaskID().substring(3,7));
        File[] files = datadir.listFiles();
        if (files == null) {
            logger.warn("failed to list files in receive-dir: " + datadir);
        }
        for (File sub : files) {
            if (!sub.isDirectory()) continue;   //不是目录跳过
            String jobTaskID = sub.getName();
            //TODO 判断是否有需要执行的源文件
            if (t.getJobTaskID().equals(jobTaskID)){
                File ok = new File(sub, jobTaskID + ".OK");
                if (!ok.isFile()) continue;   //无OK文件
                logger.info("found OK file: " + ok);
                File desc = new File(sub, jobTaskID + ".DESC");
                try {
                    List<String> lines = Files.readAllLines(desc.toPath(), Charset.defaultCharset());//todo 没有中文情况，用系统缺省字符集应该不会有问题
                    if (lines.isEmpty()) {
                        logger.error("no content in DESC file: " + desc);
                        continue;
                    }
                    //验证每一行数据文件
                    for (String line : lines) {
                        String name = line.substring(0, line.indexOf("\t"));     //Tab字符分隔
                        long size = Long.parseLong(line.substring(line.indexOf("\t") + 1));    //Tab字符分隔
                        File dat = new File(sub, name);
                        File fin = new File(sub, name.replace(".dat", ".FIN"));
                        if (!dat.isFile()) {
                            logger.error("data file not found: " + dat);
                        }else{
                            datList.add(dat.toString());
                            result = true;
                        }
                        if (!fin.isFile()) {
                            logger.error("FIN file not found: " + fin);
                        }else{
                            result = true;
                        }
                        long datsize = dat.length(); //需避免共享文件系统获取文件大小的延迟
                        if (datsize != size) {
                            logger.error("data file size (" + datsize + ") is different from the number (" + size + ") in DESC: " + dat);
                        }else{
                            result = true;
                        }
                    }
                } catch (Exception e) {  //失败下轮会重试
                    t.setOrderStatus("4");
                    t.setEndTime(DateUtil.getTime());
                    orderService.updateById(t);
                    logger.error("failed to parse DESC file: " + desc, e);
                }
            }else {
                continue;
            }
        }
        return  datList;
    }

    public void processQ64(WorkflowOrder t)throws Exception{
        //差异性分析必须包含两个作业任务编号。查询两个jobTaskID各自对应的原始码流文件（不必检查文件是否存在）
        File job1S1=null,job1S2=null,job2S1=null,job2S2=null;
        String jobtaskId1 = null,jobtaskId2 = null;
        String jobstr[] = t.getJobTaskID().split(";");
        if (t.getJobTaskID().split("/").length>1){
            jobtaskId1 = jobstr[0].split("/")[jobstr[0].split("/").length-1];
            jobtaskId2 = jobstr[1].split("/")[jobstr[1].split("/").length-1];
        }else{
            jobtaskId1 = jobstr[0];
            jobtaskId2 = jobstr[1];
        }
        String satelliteName = null;
        if (t.getSatelliteName().equals("ZY-3B")){
            satelliteName = "ZY302";
        }else{
            satelliteName=t.getSatelliteName();
        }

        List<GtRr0> ls = gtRr0ManagerService.listByJobId(jobtaskId1);
        File dat = null;
        for(GtRr0 r:ls){
           // File dat=new File(new File(Config.archive_root,r.metaFilePath).getParentFile(),r.signalID+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
            if (t.getJobTaskID().split("/").length>1){
               dat=new File(t.getJobTaskID().split(";")[0]+"/"+r.getSignalid()+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
           }else {
                dat = new File(Config.data_dir+"/"+satelliteName+"/"+jobtaskId1+"/"+r.getSignalid()+"."+ Constants.EXT_DAT);
            }
            if(Channel.S1.name().equals(r.getChannelid())){
                job1S1=dat;
            }else{
                job1S2=dat;
            }
        }
        ls = gtRr0ManagerService.listByJobId(jobtaskId2);
        for(GtRr0 r:ls){
           // File dat=new File(new File(Config.archive_root,r.metaFilePath).getParentFile(),r.signalID+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
            if (t.getJobTaskID().split("/").length>1){
                dat=new File(t.getJobTaskID().split(";")[1]+"/"+r.getSignalid()+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
            }else {
                dat = new File(Config.data_dir+"/"+satelliteName+"/"+jobtaskId2+"/"+r.getSignalid()+"."+Constants.EXT_DAT);
            }            if(Channel.S1.name().equals(r.getChannelid())){
                job2S1=dat;
            }else{
                job2S2=dat;
            }
        }
        //检查配对。一个通道必须存在两个数据集的原始码流文件，否则该通道无法进行差异分析
        if(job1S1==null || job2S1==null){
            job1S1=null;
            job2S1=null;
        }
        if(job1S2==null || job2S2==null){
            job1S2=null;
            job2S2=null;
        }
        if(job1S1==null && job2S1==null && job1S2==null && job2S2==null){
            throw new Exception("no matched singal files for jobTaskID: "+t.getJobTaskID());
        }

        //触发差异性分析流程。注意，不要使用虚拟主流程，必须直接用差异性分析流程作为主流程，
        //后续的原始码流质量分析子流程及生成报表子流程都直接以它作为父流程！
        Date time=new Date();  //统一用一个任务创建时间
        Map map=generateOrderParamsForGF_Q64_DIFF(t,job1S1, job2S1, job1S2, job2S2,time,jobtaskId1,jobtaskId2);
        String orderXml = "";
        if (t.getSatelliteName().equals("ZY-3B")||t.getSatelliteName().equals("ZY302")){
            orderXml = ProcessType.ZY3_Q64_DIFF.generateOrderXml(map);
        }else {
            orderXml = ProcessType.GF1_Q64_DIFF.generateOrderXml(map);
        }
        logger.debug("generate process order: \n" + orderXml);
        //提交流程
        processUtil.submitProcess(orderXml,Config.submit_order_timeout);
    }


    public void processQ63(WorkflowOrder t)throws Exception{
        //选取若干景。此时jobTaskID肯定只有一个
        String jobTaskID= t.getJobTaskID();
        List<Mcat> ls = null;
        switch (t.getDataSelectType()) {
            case "Full":
                ls = mcatManagerService.selectSceneByFull(jobTaskID,Sensor.fromOMOSensor(t.getSatelliteName()));
                break;
            case "Time":
                ls = mcatManagerService.selectSceneByTime(jobTaskID, Sensor.fromOMOSensor(t.getSatelliteName()),t.getSensorStartTime(),t.getSensorEndTime());
                break;
            case "AutoType":  //AutoType
                ls = mcatManagerService.selectSceneByAuto(jobTaskID,Sensor.fromOMOSensor(t.getSatelliteName()));
                break;
            case "custom":
                ls = mcatManagerService.selectByCustom(t.getSceneID().split(";"));
                break;
        }
        logger.info("打印下ls的size："+ls.size());
        if (ls.get(0).getSceneid().equals("")){
            logger.info("景ID错误");
            t.setOrderStatus("4");
            orderService.updateById(t);
        }
        sceneCountQ63=ls.size();
        logger.info("select "+sceneCountQ63+" scenes by "+t.dataSelectType);
        //处理每一景
        String orderXml = null;
        for (Mcat s : ls) {
            //构建流程订单
            Map map=generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s);
            //todo 删除表内文件
            List<String>tableList = TableName.getTableName("L1A");
            for (int i=0;i<tableList.size();i++){
                nomalManagerService.deleteProductIdByL1A(tableList.get(i),map.get("PRODUCTID_L1A").toString());
            }
            List<String>tableList2 = TableName.getTableName("L2A");
            for (int j=0;j<tableList2.size();j++){
                nomalManagerService.deleteProductIdByL2A(tableList.get(j),map.get("PRODUCTID_L2A").toString());
            }
            //todo 暂时这么写，03/28
            switch (s.getSatelliteid()){
                case"GF1B":
                case"GF1C":
                case"GF1D":
                    orderXml = ProcessType.GF1_Q63_CAT_TO_L2A.generateOrderXml(map);
                    break;
                case "ZY3B":
                    orderXml = ProcessType.CAS_Q63_CAT_TO_L2A.generateOrderXml(map);
                    break;
                case"ZY1E":
                    orderXml = ProcessType.ZY1E_Q63_CAT_TO_L2A.generateOrderXml(map);
                    break;
                case "CB4A":
                case "CBERS04A":
                    orderXml = ProcessType.CB4A_Q63_CAT_TO_L2A.generateOrderXml(map);
                    break;
                case "HJ2A":
                case "HJ2B":
                    orderXml = ProcessType.HJ_Q63_CAT_TO_L2A.generateOrderXml(map);
                    break;
                case"CASEARTH":
                    orderXml = ProcessType.CAS_Q63_CAT_TO_L2A.generateOrderXml(map);
                    break;
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            Thread.sleep(1000);
            processUtil.submitProcess(orderXml,Config.submit_order_timeout);
        }
    }

    //获取路径
    public String getFileName(String []names,String sensor,File l0Dir)throws Exception{
        logger.info("获取dat路径"+l0Dir);
        String name = names[0]+"_"+sensor+"_"+names[2]+"_"+names[3]+"_"+names[4]+"_R0";
        String file = l0Dir+"/"+sensor+"/"+name+"_01.dat,"+l0Dir+"/"+sensor+"/"+name+"_02.dat,"+l0Dir+"/"+sensor+"/"+name+"_03.dat";
        logger.info("查看下file"+file);
        return file;
    }

    public Map generateCommonOrderParamsForGF_CAT_TO_L2A(String orderIdSuffix,WorkflowOrder t, Mcat scene) throws Exception {
        String taskId = t.getTaskSerialNumber();
        Map<String, Object> map = new HashMap();
        String names[] = scene.getSceneid().split("_");
        String[] items=scene.getSegmentid().split("_");
        map.put("RESAMPLE_KERNAL",t.getResampleKernal());
        map.put("TASKSERIALNUMBER",t.getTaskSerialNumber());
        map.put("YYYYMMDD_XXXXXX", orderIdSuffix);
        map.put("SATELLITE", scene.getSatelliteid());
        map.put("SENSOR", scene.getSensorid());
        map.put("STATION",items[4].substring(0,2));
        String L1ProductId=scene.getSceneid()+"_L1A";
        map.put("PRODUCTID_L1A", L1ProductId);
        map.put("SCENEID", scene.getSceneid());
        map.put("TASKID", t.getJobTaskID());
        //todo CB4A需要的
        if (scene.getSensorid().equals("MSS")){
            map.put("BAND","8");
        }else if (scene.getSensorid().equals("PAN")){
            map.put("BAND","B1");
        }else if (scene.getSensorid().equals("WPM")){
            map.put("BAND","5");
        }
        //
        File l0Dir = new File(Config.archive_root,"/"+scene.getSceneid().split("_")[0]+"/"+items[3].substring(0,6)+"/"+items[3]+"/"+t.getJobTaskID());    //条带目录
        map.put("METAFILE",scene.getFilepath());
        String day= orderIdSuffix.split("_")[0];
        File L1Dir = null;
        //todo 默认为空的话，把默认路径写进去
        String outDir = "";
        //todo 判断输出路径是否为空，不为空则指定路径
        if (t.getProductLevel().equals("L1")){
            if ("".equals(t.getOutProductdir())||t.getOutProductdir()==null){
                L1Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId);
                MyHelper.CreateDirectory(L1Dir);
            }else{
                L1Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId);
                MyHelper.CreateDirectory(L1Dir);
                outDir = t.getOutProductdir()+";"+Config.dataBank_dir+"/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId;

            }
        }else if (t.getProductLevel().equals("L2")){
            L1Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId);
            MyHelper.CreateDirectory(L1Dir);
        }
        map.put("DIR_L1A", L1Dir);
        map.put("WORK_DIRL1A", Config.work_dir+"/"+L1ProductId);
        //switch (reportUtil.findBianma(t.getSatelliteName())){
        switch (t.getSatelliteName()){
            case "GF-1B":
            case "GF-1C":
            case "GF-1D":
                map = generateProductForGF(map,names,scene,l0Dir,L1Dir,L1ProductId);
                break;
            case "ZY-3B":
                map = generateProductForZY(map,names,scene,l0Dir,L1Dir,L1ProductId);
                break;
            case "ZY-1E":
            case "CBERS04A":
            case "HJ-2A":
            case "HJ-2B":
                map = generateOrderParamsForZY1E(map,names,scene,l0Dir,L1Dir,L1ProductId);
                break;
        }
        map.put("L1REPORT", new File(L1Dir, L1ProductId + ".report.xml"));
        String L2ProductId=scene.getSceneid()+"_L2A";
        map.put("PRODUCTID_L2A", L2ProductId);
        if (t.getProductLevel().equals("L2")){
            File L2Dir = null;
           //todo 判断输出路径是否为空，为空则默认路径 推翻之前的，重新一版
            if (t.getOutProductdir()!=null&&!t.getOutProductdir().equals("")){
                L2Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L2DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L2ProductId);
                MyHelper.CreateDirectory(L2Dir);
                outDir = t.getOutProductdir()+";"+Config.dataBank_dir+"/"+scene.getSatelliteid() + "/L2DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L2ProductId;
            }else{
                L2Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L2DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L2ProductId);
                MyHelper.CreateDirectory(L2Dir);
            }
            map.put("DIR_L2A", L2Dir);
            map.put("WORK_DIRL2A",Config.work_dir+"/"+L2ProductId);//工作目录。
            if (map.get("SENSOR").equals("TLC")){
                map = generateProductL2ForZY(map,scene,L2Dir,L2ProductId);
            }else if (map.get("SENSOR").equals("WPM")){
                map = generateProductL2ForCBERS04A(map,scene,L2Dir,L2ProductId);
            }else {
                map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + ".tiff"));
                map.put("L2REPORT", new File(L2Dir, L2ProductId + ".report.xml"));
                map.put("L2META", new File(L2Dir, L2ProductId + ".meta.xml"));
            }
        }
        logger.info("打印下outDir"+outDir);
        //todo  更新产品的输出地址，压缩的时候用
        t.setOutProductdir(outDir);

        orderService.updateById(t);
        return map;
    }
//二级for CB4A
private static Map generateProductL2ForCBERS04A( Map<String,Object>map,Mcat scene,File L2Dir,String L2ProductId)throws Exception{
    if("WPM".equals(scene.getSensorid())){
        String []contentStr = scene.getContent().split(",");
        //todo 单个传感器 处理
        if (contentStr.length==2){
            if (contentStr[0].equals("PAN") && contentStr[1].equals("")) {
                map.put("IMAGEFILE_L2A", new File(L2Dir, L2ProductId + "_PAN.tiff"));
                map.put("L2REPORT", new File(L2Dir, L2ProductId + "_PAN.report.xml"));
                map.put("L2META",new File(L2Dir, L2ProductId + "_PAN.meta.xml"));
            } else if (contentStr[0].equals("") && contentStr[1].equals("MSS")) {
                map.put("IMAGEFILE_L2A", new File(L2Dir, L2ProductId + "_MSS.tiff"));
                map.put("L2REPORT", new File(L2Dir, L2ProductId + "_MSS.report.xml"));
                map.put("L2META",new File(L2Dir, L2ProductId + "_MSS.meta.xml"));
            } else if (contentStr[0].equals("PAN") && contentStr[1].equals("MSS")) {
                logger.info("进入PAN,MSS");
                map.put("IMAGEFILE_L2A", new File(L2Dir, L2ProductId + "_MSS.tiff"));
                map.put("L2REPORT", new File(L2Dir, L2ProductId + "_MSS.report.xml"));
                map.put("L2META",new File(L2Dir, L2ProductId + "_MSS.meta.xml" ));
            }
        }else {
            logger.info("Content error.");
        }
    }
    return  map;
}
    //二级几何校正
    private static Map generateProductL2ForZY(Map<String,Object>map,Mcat scene,File L2Dir,String L2ProductId)throws Exception{
        if("TLC".equals(scene.getSensorid())){
            String []contentStr = scene.getContent().split(",");
            //todo 单个传感器 处理
            if (contentStr.length==2){
                if (contentStr[0].equals("NAD")&&contentStr[1].equals("")){
                    map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + "-NAD.tiff"));
                    map.put("L2REPORT", new File(L2Dir, L2ProductId + "-NAD.report.xml"));
                    map.put("L2META", new File(L2Dir, L2ProductId + "-NAD.meta.xml"));
                }else if (contentStr[0].equals("")&&contentStr[1].equals("FWD")){
                    map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + "-FWD.tiff"));
                    map.put("L2REPORT", new File(L2Dir, L2ProductId + "-FWD.report.xml"));
                    map.put("L2META", new File(L2Dir, L2ProductId + "-FWD.meta.xml"));
                }
            }else if (contentStr.length==3){
                if (contentStr[0].equals("")&&contentStr[1].equals("")&&contentStr[2].equals("BWD")){
                    map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + "-BWD.tiff"));
                    map.put("L2REPORT", new File(L2Dir, L2ProductId + "-BWD.report.xml"));
                    map.put("L2META", new File(L2Dir, L2ProductId + "-BWD.meta.xml"));
                }else{
                    if (contentStr[0].equals("NAD")){
                        map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + "-NAD.tiff"));
                        map.put("L2REPORT", new File(L2Dir, L2ProductId + "-NAD.report.xml"));
                        map.put("L2META", new File(L2Dir, L2ProductId + "-NAD.meta.xml"));
                    }else {
                        map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + "-FWD.tiff"));
                        map.put("L2REPORT", new File(L2Dir, L2ProductId + "-FWD.report.xml"));
                        map.put("L2META", new File(L2Dir, L2ProductId + "-FWD.meta.xml"));
                    }
                }
            }
        }
        return  map;
    }
    public String orderByName(String [] files)throws Exception{
        String tt = "";
        Arrays.sort(files);
        for (int i=0;i<files.length;i++){
            tt +=files[i]+",";
        }
        return tt;
    }
    private Map generateOrderParamsForZY1E(Map<String,Object>map,String []names, Mcat scene,File l0Dir,File L1Dir,String L1ProductId)throws Exception{
        String [] numStrs = scene.getSceneid().split("_");
        String numStr = numStrs[numStrs.length-2];
        int num = Integer.parseInt(numStr);
        if (scene.getSensorid().equals("WPM")){
            String []sensorStr = scene.getContent().split(",");
            for (int i=0;i<sensorStr.length;i++){
                File file = new File(l0Dir,sensorStr[i]);
                map = getZY1EandCb4aFile(map,file,num,sensorStr[i], L1ProductId,scene.getSensorid());
            }
        }else {
            File file = new File(l0Dir,scene.getSensorid());
            map = getZY1EandCb4aFile(map,file,num,scene.getSensorid(),L1ProductId,scene.getSensorid());
        }
        if (!scene.getSatelliteid().equals("CB4A")){
            map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + ".tiff"));
            map.put("RPCFILE_L1A", new File(L1Dir, L1ProductId + "."+Constants.EXT_RPC));
            map.put("L1META",new File(L1Dir, L1ProductId + ".meta.xml"));
        }else {
            String[] contentStr = scene.getContent().split(",");
            logger.info("Content is "+scene.getContent());
            //todo 单个传感器 处理
            if (contentStr.length == 2) {
                if (contentStr[0].equals("PAN") && contentStr[1].equals("")) {
                    map.put("IMAGEFILE_L1A", new File(L1Dir, L1ProductId + "_PAN.tiff")+",");
                    map.put("RPCFILE_L1A", new File(L1Dir, L1ProductId + "_PAN.rpb")+",");
                    map.put("L1META",new File(L1Dir, L1ProductId + "_PAN.meta.xml"));
                    map.put("IMAGEFILE_L1A_Q63", new File(L1Dir, L1ProductId + "_PAN.tiff"));
                } else if (contentStr[0].equals("") && contentStr[1].equals("MSS")) {
                    map.put("IMAGEFILE_L1A", ","+new File(L1Dir, L1ProductId + "_MSS.tiff"));
                    map.put("RPCFILE_L1A", ","+new File(L1Dir, L1ProductId + "_MSS.rpb"));
                    map.put("L1META",new File(L1Dir, L1ProductId + "_MSS.meta.xml"));
                    map.put("IMAGEFILE_L1A_Q63", ","+new File(L1Dir, L1ProductId + "_MSS.tiff"));
                } else if (contentStr[0].equals("PAN") && contentStr[1].equals("MSS")) {
                    logger.info("进入PAN,MSS");
                    map.put("IMAGEFILE_L1A", new File(L1Dir, L1ProductId + "_PAN.tiff")+","+new File(L1Dir, L1ProductId + "_MSS.tiff"));
                    map.put("RPCFILE_L1A", new File(L1Dir, L1ProductId + "_PAN.rpb" )+","+new File(L1Dir, L1ProductId + "_MSS.rpb"));
                    map.put("L1META",new File(L1Dir, L1ProductId + "_MSS.meta.xml"));
                    map.put("IMAGEFILE_L1A_Q63",new File(L1Dir, L1ProductId + "_MSS.tiff"));
                }
            }
        }
        return map;
    }
    private Map getZY1EandCb4aFile(Map<String,Object>map,File file,int num,String sensor,String L1ProductId,String sensorID)throws Exception{

        File [] fileStr = file.listFiles();
        String fileName = "";
        for (int i=0;i<fileStr.length;i++){
            if (!fileStr[i].isDirectory()){
                if (fileStr[i].getName().endsWith(".dat")){
                    if (fileStr[i].getName().contains("format")&&fileStr[i].getName().contains(num+".dat")){
                        if (fileName==""){
                            fileName = fileStr[i].toString()+",";
                        }else {
                            fileName = fileName+fileStr[i].toString()+",";
                        }

                    }
                }
            }
        }
        String paStr [] = fileName.split(",");
        fileName = orderByName(paStr);
        switch (sensor){
            case"MSS":
                map.put("UNPACKFILE_MS",fileName);
                break;
            case"PAN":
                map.put("UNPACKFILE_PA",fileName);
                break;
        }

        return  map;
    }
    private Map generateProductForZY( Map<String,Object>map,String []names, Mcat scene,File l0Dir,File L1Dir,String L1ProductId)throws Exception{
        if("TLC".equals(scene.getSensorid())){
            String []contentStr = scene.getContent().split(",");
            //todo 单个传感器 处理
            if (contentStr.length==2){
                if (contentStr[0].equals("NAD")&&contentStr[1].equals("")){
                    String nadFile = getFileName(names,"NAD",l0Dir);
                    map.put("UNPACKFILE_NAD",nadFile);
                    map.put("IMAGEFILE_NAD",new File(L1Dir, L1ProductId + "-NAD.tiff"));
                    map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-NAD.tiff"));
                    map.put("RPCFILE_NAD", new File(L1Dir, L1ProductId + "-NAD."+Constants.EXT_RPC));
                    map.put("L1META",new File(L1Dir, L1ProductId + "-NAD.meta.xml"));
                }else if (contentStr[0].equals("")&&contentStr[1].equals("FWD")){
                    String fwdFile = getFileName(names,"FWD",l0Dir);
                    map.put("UNPACKFILE_FWD",fwdFile);
                    map.put("IMAGEFILE_FWD",new File(L1Dir, L1ProductId + "-FWD.tiff"));
                    map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-FWD.tiff"));
                    map.put("RPCFILE_FWD", new File(L1Dir, L1ProductId + "-FWD."+Constants.EXT_RPC));
                    map.put("L1META",new File(L1Dir, L1ProductId + "-FWD.meta.xml"));
                }
            }else if (contentStr.length==3){
                if (contentStr[0].equals("")&&contentStr[1].equals("")&&contentStr[2].equals("BWD")){
                    String bwdFile = getFileName(names,"BWD",l0Dir);
                    map.put("UNPACKFILE_BWD",bwdFile);
                    map.put("IMAGEFILE_BWD",new File(L1Dir, L1ProductId + "-BWD.tiff"));
                    map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-BWD.tiff"));
                    map.put("RPCFILE_BWD", new File(L1Dir, L1ProductId + "-BWD."+Constants.EXT_RPC));
                    //
                    map.put("L1META",new File(L1Dir, L1ProductId + "-BWD.meta.xml"));
                }else{
                    //todo 20190409 kiven
                    String nadFile =getFileName(names,"NAD",l0Dir);
                    map.put("UNPACKFILE_NAD",nadFile);
                    String fwdFile = getFileName(names,"FWD",l0Dir);
                    map.put("UNPACKFILE_FWD",fwdFile);
                    String bwdFile = getFileName(names,"BWD",l0Dir);
                    map.put("UNPACKFILE_BWD",bwdFile);
                    //todo 20190427 kiven
                    map.put("IMAGEFILE_BWD",new File(L1Dir, L1ProductId + "-BWD.tiff"));
                    map.put("RPCFILE_BWD", new File(L1Dir, L1ProductId + "-BWD."+Constants.EXT_RPC));
                    map.put("IMAGEFILE_FWD",new File(L1Dir, L1ProductId + "-FWD.tiff"));
                    map.put("RPCFILE_FWD", new File(L1Dir, L1ProductId + "-FWD."+Constants.EXT_RPC));
                    map.put("IMAGEFILE_NAD",new File(L1Dir, L1ProductId + "-NAD.tiff"));
                    map.put("RPCFILE_NAD", new File(L1Dir, L1ProductId + "-NAD."+Constants.EXT_RPC));
                    if (contentStr[0].equals("NAD")){
                        map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-NAD.tiff"));
                        map.put("L1META",new File(L1Dir, L1ProductId + "-NAD.meta.xml"));
                    }else {
                        map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-FWD.tiff"));
                        map.put("L1META",new File(L1Dir, L1ProductId + "-FWD.meta.xml"));
                    }
                }
            }
        }else if ("NAD".equals(scene.getSensorid())){
            String nadFile = getFileName(names,"NAD",l0Dir);
            map.put("UNPACKFILE_NAD",nadFile);
            map.put("IMAGEFILE_NAD",new File(L1Dir, L1ProductId + "-NAD.tiff"));
            map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-NAD.tiff"));
            map.put("RPCFILE_NAD", new File(L1Dir, L1ProductId + "-NAD."+Constants.EXT_RPC));
            map.put("L1META",new File(L1Dir, L1ProductId + ".meta.xml"));
        }else if ("BWD".equals(scene.getSensorid())){
            String bwdFile = getFileName(names,"BWD",l0Dir);
            map.put("UNPACKFILE_BWD",bwdFile);
            map.put("IMAGEFILE_BWD",new File(L1Dir, L1ProductId + "-BWD.tiff"));
            map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-BWD.tiff"));
            map.put("RPCFILE_BWD", new File(L1Dir, L1ProductId + "-BWD."+Constants.EXT_RPC));
            map.put("L1META",new File(L1Dir, L1ProductId + ".meta.xml"));
        }else if ("FWD".equals(scene.getSensorid())){
            String fwdFile = getFileName(names,"FWD",l0Dir);
            map.put("UNPACKFILE_FWD",fwdFile);
            map.put("IMAGEFILE_FWD",new File(L1Dir, L1ProductId + "-FWD.tiff"));
            map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + "-FWD.tiff"));
            map.put("RPCFILE_FWD", new File(L1Dir, L1ProductId + "-FWD."+Constants.EXT_RPC));
            map.put("L1META",new File(L1Dir, L1ProductId + ".meta.xml"));
        }else if ("MUX".equals(scene.getSensorid())){
            String muxName = names[0]+"_MUX_"+names[2]+"_"+names[3]+"_"+names[4]+"_R0";
            String muxFile = l0Dir+"/MUX/"+muxName+"_01.dat,"+l0Dir+"/MUX/"+muxName+"_02.dat";
            map.put("UNPACKFILE_MUX",muxFile);
            map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + ".tiff"));
            map.put("RPCFILE_L1A", new File(L1Dir, L1ProductId + "."+Constants.EXT_RPC));
            map.put("L1META",new File(L1Dir, L1ProductId + ".meta.xml"));
        }
        return map;
    }

    private  Map generateProductForGF( Map<String,Object>map,String []names, Mcat scene,File l0Dir,File L1Dir,String L1ProductId)throws Exception{
        if ("PAN1".equals(scene.getSensorid())){
            String pan1File = getFileName(names,"PAN1",l0Dir);
            map.put("UNPACKFILE_PA",pan1File);
        }else if("PAN2".equals(scene.getSensorid())){
            String pan2File = getFileName(names,"PAN2",l0Dir);
            map.put("UNPACKFILE_PA",pan2File);
        }else if ("MSS1".equals(scene.getSensorid())){
            String mss1Name = names[0]+"_MSS1_"+names[2]+"_"+names[3]+"_"+names[4]+"_R0";
            String mss1File = l0Dir+"/MSS1/"+mss1Name+"_01.dat";
            map.put("UNPACKFILE_MS",mss1File);
        }else if("MSS2".equals(scene.getSensorid())){
            String mss2Name = names[0]+"_MSS2_"+names[2]+"_"+names[3]+"_"+names[4]+"_R0";
            String mss2File = l0Dir+"/MSS2/"+mss2Name+"_01.dat";
            map.put("UNPACKFILE_MS",mss2File);
        }
        map.put("IMAGEFILE_L1A",new File(L1Dir, L1ProductId + ".tiff"));
        map.put("RPCFILE_L1A", new File(L1Dir, L1ProductId + "."+Constants.EXT_RPC));
        map.put("L1META",new File(L1Dir, L1ProductId + ".meta.xml"));
        return map;
    }


    private Map generateOrderParamsForGF_Q64_DIFF(WorkflowOrder t,File job1S1,File  job2S1, File job1S2, File job2S2,Date time,String jobtaskId1,String jobtaskId2) throws Exception{

        Map<String, Object> ret = new HashMap<>();
        ret.put("YYYYMMDD_XXXXXX",DateUtil.getSdfDate());
        ret.put("SATELLITE", t.getSatelliteName());
        ret.put("TASKSERIALNUMBER", t.getTaskSerialNumber());
        ret.put("JOBTASKID1", jobtaskId1);  //差异性分析必须包含两个作业任务编号
        ret.put("JOBTASKID2", jobtaskId2);
        ret.put("JOB1_S1", job1S1);
        ret.put("JOB2_S1", job2S1);
        ret.put("JOB1_S2", job1S2);
        ret.put("JOB2_S2", job2S2);
        File diff = new File(Config.dataBank_dir,  "/"+t.getSatelliteName().replaceAll("-","")+"/REPORT/"+
                new SimpleDateFormat("yyyyMM").format(time)+"/"+
                new SimpleDateFormat("yyyyMMdd").format(time)+"/"+t.getTaskSerialNumber()+"/"+jobtaskId1+"_"+jobtaskId2+".diff.txt");
        MyHelper.CreateDirectory(diff.getParentFile());

        //todo 每次做之前做一个清空操作
        MyHelper.emptyDir(new File(diff.getParent()));
        ret.put("DIFFTXT", diff.getPath());
        return ret;
    }

    private Map generateOrderParamsForGF_Q65(WorkflowOrder t) throws Exception{

        File reportFile = new File(Config.dataBank_dir, "/"+t.getSatelliteName().replaceAll("-","")+"/REPORT/"+
                new SimpleDateFormat("yyyyMM").format(DateUtil.getSdfDate())+"/"+
                new SimpleDateFormat("yyyyMMdd").format(DateUtil.getSdfDate())+"/"+t.getTaskSerialNumber()+"/"+t.getTaskSerialNumber()+".xlsx");
        MyHelper.CreateDirectory(reportFile.getParentFile());

        Map<String, Object> ret = new HashMap();
        ret.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        ret.put("TASKSERIALNUMBER", t.getTaskSerialNumber());
        ret.put("SATELLITE", t.getSatelliteName());
        ret.put("SENSOR", t.getSensorName());
        ret.put("STARTTIME", t.receiveStartTime);
        ret.put("ENDTIME", t.receiveEndTime);
        //todo 有station/recoreder这个字段吗？ 2019/2/21 by wk
        ret.put("STATION", t.getStation());
        ret.put("RECORDER", "");
        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }

    private Map generateOrderParamsForGF_Q61_62_63_QAReport(WorkflowOrder t) throws Exception {
        Map<String, Object> ret = new HashMap();
        ret.put("TASKSERIALNUMBER",t.getTaskSerialNumber());
        ret.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        ret.put("SATELLITE", t.getSatelliteName().replace("-",""));
        ret.put("TASKMODE", t.taskMode);
        ret.put("JOBTASKID", t.getJobTaskID());
        ret.put("TASKID", t.getTaskSerialNumber());
        ret.put("CHANNEL", t.getChannelID());
        ret.put("SENSOR", t.getSensorName());
        if(t.getDataSelectType() != null&&!"".equals(t.getDataSelectType()))
            ret.put("DATASELECTTYPE", t.getDataSelectType());
        else
            ret.put("DATASELECTTYPE", "AutoType");
        File reportFile = new File(Config.dataBank_dir+"/", QAReportFile);
        logger.info(reportFile.getParentFile().toPath());
        MyHelper.CreateDirectory(reportFile.getParentFile());
        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }
}

