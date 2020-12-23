package com.business.business.action;


import com.business.business.Service.McatManagerService;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.config.Config;
import com.business.business.entity.GtRr0;
import com.business.business.entity.Mcat;
import com.business.business.entity.WorkflowOrder;
import com.business.business.enums.*;
import com.business.business.util.DateUtil;
import com.business.business.util.ProcessUtil;
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
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private McatManagerService mcatManagerService;
    public  void process(WorkflowOrder order)throws Exception{
        String taskMode = order.taskMode;
        if (order.getTaskStatus().equals("New")){
            try{
                //先生成报告文件路径
                QAReportFile = ResponseType.buildQAReportFileRelativePath(order.getJobTaskID(),order.getSatelliteName().replace("-",""),order.getTaskSerialNumber());
                if (order.taskMode.contains("Q63")){
                    switch (order.getSatelliteName()){
                        case "GF-1B":
                        case "GF-1C":
                        case "GF-1D":
                            processQ63(order);
                            break;
                        case "ZY-3B":
                            processQ63(order);
                            break;
                    }
                }
                if(order.taskMode.contains(TaskMode.Q64.name())) {  //特殊处理，需先触发差异性分析流程
                    processQ64(order);
                }
                    Date time=new Date();  //任务创建时间
                    ProcessType processType;
                    Map orderParams;
                    if (order.taskMode.contains("Q65")) {  //直接触发Q65评价流程
                        //todo q65直接触发流程
                        processType = ProcessType.KJ125_Q65;
                        orderParams = generateOrderParamsForGF_Q65(order,time);
                        //构建流程订单
                        String orderXml = processType.generateOrderXml(orderParams);
                        processUtil.submitProcess(orderXml, Config.submit_order_timeout);
                        //todo 提交流程 by 2019/2/22 kiven

                    }
                    if (order.taskMode.contains(TaskMode.Q61.name())){  //Q61/62组合，直接触发生成综合质量报告流程
                        switch (order.getSatelliteName()){
                            case "GF-1B":
                            case "GF-1C":
                            case "GF-1D":
                            case "ZY-3B":
                            case"CSES":
                                //todo 归档任务不发起R0Report任务
                                List<GtRr0> gtrR0List = gtrR0Manager.listByJobId(order.getJobTaskID());
                                if (gtrR0List.size()==0){
                                    //todo S1Fili和S2File 查询集中存储？
                                    List<String> subOrderIds = new ArrayList<>();
                                    List<String> subInfos = new ArrayList<>();
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

                                   // treeManager.saveTreeSubWorkFlow(order.getTaskSerialNumber(),DateUtil.getTime(),subOrderIds,subInfos);
                                }
                                    break;
                                }
                        }
                    }
                     if (taskMode.contains("Q62")){
                        if (taskMode.equals("Q61;Q62;Q63")){
                        }else if (taskMode.equals("Q62")){
                            processType = ProcessType.KJ125_Q61_62_63_QAReport;
                            orderParams = generateOrderParamsForGF_Q61_62_63_QAReport(order,time);
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
        map.put("PINFILE", Config.diff_pinfile_05);
        //todo 更换路径
        File dir = new File(Config.dataBank_dir+"/"+satellite+"/SIGNAL/"+item[3].substring(0,6)+"/"+item[3]);
        if (!dir.exists()||!dir.isDirectory()){
            Files.createDirectories(dir.toPath());
        }
        map.put("REPORT",dir+"/"+signalFile.getName().replace(".dat", ".report.xml"));
      //  map.put("REPORT", signalFile.getPath().replace(".dat", ".report.xml"));
        return map;
    }

    public synchronized List<String> processQATask(WorkflowOrder t)throws Exception{
        //先删除R0表
        if (t.getSatelliteName().equals("ZY-3B"))t.setSatelliteName("ZY302");
        processInfoimpl = new ProcessInfoImpl();
        processInfoimpl.deleteSignalAuto("gt_r_r0",t.getJobTaskID());
        List<String>datList = new ArrayList<>();
        boolean result = false;
        orderManager = new WorkFlowOrderManager();
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
                //   if(!Config.isMonitorTarget(sub)) continue ; //不是自己的监控目标数据，则忽略之
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
                    orderManager.updateOrder(t);
                    logger.error("failed to parse DESC file: " + desc, e);
                }
            }else {
                continue;
            }
        }
        return  datList;
    }

    public void processQ64(WorkflowOrder t)throws Exception{
        r0InfoManager = new R0InfoManager();
        treeManager = new WorkFlowTreeManager();
        qaTaskWorkFlowManager = new QATaskWorkFlowManager();
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

        List<R0Info> ls = r0InfoManager.getR0Info(jobtaskId1);
        File dat = null;
        for(R0Info r:ls){
           // File dat=new File(new File(Config.archive_root,r.metaFilePath).getParentFile(),r.signalID+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
            if (t.getJobTaskID().split("/").length>1){
               dat=new File(t.getJobTaskID().split(";")[0]+"/"+r.signalID+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
           }else {
                dat = new File(Config.data_dir+"/"+satelliteName+"/"+jobtaskId1+"/"+r.signalID+"."+Constants.EXT_DAT);
            }
            if(Channel.S1.name().equals(r.channelID)){
                job1S1=dat;
            }else{
                job1S2=dat;
            }
        }
        ls = r0InfoManager.getR0Info(jobtaskId2);
        for(R0Info r:ls){
           // File dat=new File(new File(Config.archive_root,r.metaFilePath).getParentFile(),r.signalID+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
            if (t.getJobTaskID().split("/").length>1){
                dat=new File(t.getJobTaskID().split(";")[1]+"/"+r.signalID+"."+Constants.EXT_DAT);   //原始码流文件：meta文件同目录下signalID.dat
            }else {
                dat = new File(Config.data_dir+"/"+satelliteName+"/"+jobtaskId2+"/"+r.signalID+"."+Constants.EXT_DAT);
            }            if(Channel.S1.name().equals(r.channelID)){
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
        //在TaskCheckThread.checkOMOQATask()方法判断是否已触发了生成报表子流程时，需依赖此父子关系！
        Date time=new Date();  //统一用一个任务创建时间
        Map map=generateOrderParamsForGF_Q64_DIFF(t,job1S1, job2S1, job1S2, job2S2,time,jobtaskId1,jobtaskId2);
        String orderXml = ProcessType.KJ125_Q64_DIFF.generateOrderXml(map);
        logger.debug("generate process order: \n" + orderXml);
        //提交流程
        processUtil.submitProcess(orderXml,Config.submit_order_timeout);
    }


    public void processQ63(WorkflowOrder t)throws Exception{
        //选取若干景。此时jobTaskID肯定只有一个
        String jobTaskID= t.getJobTaskID();
//        List<CatInfo> ls = null;
        List<Mcat> ls = null;
        switch (t.getDataSelectType()) {
            case "Full":
                ls = mcatManagerService.selectSceneByFull(jobTaskID,Sensor.fromOMOSensor(t.getSatelliteName(),""));
                //ls = db.selectSceneByFull(jobTaskID,Sensor.fromOMOSensors(t.satellite.name(),String2ListListXmlAdapter.toList(t.sensorName)));
                break;
            case "Time":
                ls = mcatManagerService.selectSceneByTime(jobTaskID, Sensor.fromOMOSensor(t.getSatelliteName(),""),t.getSensorStartTime(),t.getSensorEndTime());
               // ls = db.selectSceneByTime(jobTaskID,Sensor.fromOMOSensors(t.satellite.name(),String2ListListXmlAdapter.toList(t.sensorName)),t.sensorStartTime,t.sensorEndTime);
                break;
            case "AutoType":  //AutoType
                ls = mcatManagerService.selectSceneByAuto(jobTaskID,Sensor.fromOMOSensor(t.getSatelliteName(),""));
                //ls = db.selectSceneByAuto(jobTaskID, Sensor.fromOMOSensors(t.satellite.name(),String2ListListXmlAdapter.toList(t.sensorName)));
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
        Date time=new Date();  //统一用一个任务创建时间
        //处理每一景
        List<String> subOrderIds = new ArrayList<>();
        List<String> subInfos = new ArrayList<>();  //景ID列表
        String orderXml = null;
        for (Mcat s : ls) {
            //构建流程订单
            Map map=generateCommonOrderParamsForGF_CAT_TO_L2A(DateUtil.getSdfDate(),t,s);
             processInfoimpl.deleteProductIdByL1A("GT_M_L2",map.get("PRODUCTID_L2A").toString());
            processInfoimpl.deleteProductIdByL1A("GT_R_L2",map.get("PRODUCTID_L2A").toString());
            processInfoimpl.deleteProductIdByL1A("GT_R_L1",map.get("PRODUCTID_L1A").toString());
            processInfoimpl.deleteProductIdByL1A("GT_M_L1",map.get("PRODUCTID_L1A").toString());

            //todo 暂时这么写，03/28
            if (s.getSatelliteid().contains("GF1")){
                orderXml = ProcessType.GF1_Q63_CAT_TO_L2A.generateOrderXml(map);
            }else if (s.getSatelliteid().contains("ZY3")){
                orderXml = ProcessType.ZY3B_Q63_CAT_TO_L2A.generateOrderXml(map);
            }
            logger.debug("generate process order: \n" + orderXml);
            //提交流程
            processUtil.submitProcess(orderXml,Config.submit_order_timeout);
        }
    }

    private String generateTaskInfo(WorkflowOrder t) {
        StringBuffer sb=new StringBuffer();
        sb.append("<t>");
        sb.append("<satellite>"+t.getSatelliteName()+"</satellite>");
        sb.append("<taskMode>"+ t.getTaskMode()+"</taskMode>");

        if(t.getJobTaskID()!=null&&!"".equals(t.getJobTaskID()))
            sb.append("<jobTaskID>"+t.getJobTaskID()+"</jobTaskID>");

        if(t.getChannelID()!=null&&!"".equals(t.getChannelID()))
            sb.append("<channel>"+t.getChannelID()+"</channel>");

        sb.append("<sensor>"+ t.getSensorName()+"</sensor>");

        if(t.getDataSelectType()!=null&&!"".equals(t.getDataSelectType()))
            sb.append("<dataSelectType>"+t.getDataSelectType()+"</dataSelectType>");

        if(sceneCountQ63!=null)
            sb.append("<sceneCountQ63>"+sceneCountQ63+"</sceneCountQ63>");

        if(t.getOrbitNumber()!=null&&!"".equals(t.getOrbitNumber()))
            sb.append("<orbit>"+t.getOrbitNumber()+"</orbit>");
        QAReportFile = ResponseType.buildQAReportFileRelativePath(t.getJobTaskID(),t.getSatelliteName().replace("-",""),t.getTaskSerialNumber());
        sb.append("<QAReportFile>"+QAReportFile+"</QAReportFile>");
        sb.append("</t>");
        return sb.toString();
    }
    //获取路径
    public static String getFileName(String []names,String sensor,File l0Dir)throws Exception{
        String name = names[0]+"_"+sensor+"_"+names[2]+"_"+names[3]+"_"+names[4]+"_R0";
        String file = l0Dir+"/"+sensor+"/"+name+"_01.dat,"+l0Dir+"/"+sensor+"/"+name+"_02.dat,"+l0Dir+"/"+sensor+"/"+name+"_03.dat";
        return file;
    }


    private Map generateCommonOrderParamsForGF_CAT_TO_L2A(String orderIdSuffix,WorkflowOrder t,Mcat scene) throws Exception {
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

        File l0Dir = new File(Config.archive_root,"/"+scene.getSatelliteid()+"/"+items[3].substring(0,6)+"/"+items[3]+"/"+t.getJobTaskID());    //条带目录

        map.put("RAWFILE",l0Dir);
        map.put("METAFILE",scene.getFilepath());
        String day= orderIdSuffix.split("_")[0];
        File L1Dir = null;
        //todo 默认为空的话，把默认路径写进去
        String outDir = "";
        //todo 判断输出路径是否为空，不为空则指定路径
        if (t.getProductLevel().equals("L1")){
            if (t.getOut_productdir().equals("")){
                L1Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId);
                Files.createDirectories(L1Dir.toPath());  //必须先尝试创建各级目录
            }else{
                //L1Dir = new File(t.getOut_productdir());
                L1Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId);
                Files.createDirectories(L1Dir.toPath());
                outDir = t.getOut_productdir()+";"+Config.dataBank_dir+"/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId;

            }
        }else if (t.getProductLevel().equals("L2")){
            L1Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId);
            Files.createDirectories(L1Dir.toPath());  //必须先尝试创建各级目录
           // outDir = Config.archive_root+"/DataBank/"+scene.getSatelliteid() + "/L1DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L1ProductId+";";
        }
        map.put("DIR_L1A", L1Dir);
        map.put("WORK_DIRL1A", Config.work_dir+"/"+L1ProductId);
        switch (t.getSatelliteName()){
            case "GF-1B":
            case "GF-1C":
            case "GF-1D":
                map = generateProductForGF(map,names,scene,l0Dir,L1Dir,L1ProductId);
                break;
            case "ZY-3B":
                map = generateProductForZY(map,names,scene,l0Dir,L1Dir,L1ProductId);
                break;
        }
        map.put("L1REPORT", new File(L1Dir, L1ProductId + ".report.xml"));
        String L2ProductId=scene.getSceneid()+"_L2A";
        map.put("PRODUCTID_L2A", L2ProductId);
        if (t.getProductLevel().equals("L2")){
            File L2Dir = null;
           //todo 判断输出路径是否为空，为空则默认路径 推翻之前的，重新一版
            if (t.getOut_productdir()!=null&&!t.getOut_productdir().equals("")){
               //L2Dir = new File(t.getOut_productdir());
                L2Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L2DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L2ProductId);
                Files.createDirectories(L2Dir.toPath());
                outDir = t.getOut_productdir()+";"+Config.dataBank_dir+"/"+scene.getSatelliteid() + "/L2DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L2ProductId;
            }else{
                L2Dir = new File(Config.dataBank_dir, "/"+scene.getSatelliteid() + "/L2DATA/" +day.substring(0,6) + "/" + day + "/" + taskId+"/"+L2ProductId);
                Files.createDirectories(L2Dir.toPath());  //必须先尝试创建各级目录
            }
            map.put("DIR_L2A", L2Dir);
            map.put("WORK_DIRL2A",Config.work_dir+"/"+L2ProductId);//工作目录。
            if (map.get("SENSOR").equals("TLC")){
                map = generateProductL2ForZY(map,scene,L2Dir,L2ProductId);
            }else {
                map.put("IMAGEFILE_L2A",new File(L2Dir, L2ProductId + ".tiff"));
                map.put("L2REPORT", new File(L2Dir, L2ProductId + ".report.xml"));
                map.put("L2META", new File(L2Dir, L2ProductId + ".meta.xml"));
            }


        }
        //todo  更新产品的输出地址，压缩的时候用
        t.setOut_productdir(outDir);
        t.setOrderStatus("2");
        orderService.updateById(t);
        return map;
    }
    //二级几何校正
    private static Map generateProductL2ForZY( Map<String,Object>map,Mcat scene,File L2Dir,String L2ProductId)throws Exception{
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

    private static Map generateProductForZY( Map<String,Object>map,String []names, Mcat scene,File l0Dir,File L1Dir,String L1ProductId)throws Exception{
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
    private static Map generateProductForGF( Map<String,Object>map,String []names, Mcat scene,File l0Dir,File L1Dir,String L1ProductId)throws Exception{
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

        String s1 = t.getJobTaskID().split(";")[0];
        String s2 = t.getJobTaskID().split(";")[1];
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
        Files.createDirectories(diff.getParentFile().toPath());  //必须先尝试创建各级目录
        //todo 每次做之前做一个清空操作
        MyHelper.emptyDir(new File(diff.getParent()));
        ret.put("DIFFTXT", diff.getPath());
        return ret;
    }

    private Map generateOrderParamsForGF_Q65(WorkflowOrder t,Date time) throws Exception{

        File reportFile = new File(Config.dataBank_dir, "/"+t.getSatelliteName().replaceAll("-","")+"/REPORT/"+
                new SimpleDateFormat("yyyyMM").format(time)+"/"+
                new SimpleDateFormat("yyyyMMdd").format(time)+"/"+t.getTaskSerialNumber()+"/"+t.getTaskSerialNumber()+".xlsx");
        Files.createDirectories(reportFile.getParentFile().toPath());  //必须先尝试创建各级目录

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

    private Map generateOrderParamsForGF_Q61_62_63_QAReport(WorkflowOrder t,Date time) throws Exception {
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
        Files.createDirectories(reportFile.getParentFile().toPath());  //必须先尝试创建各级目录
        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }
}

