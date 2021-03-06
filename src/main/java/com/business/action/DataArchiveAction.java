package com.business.action;

import com.business.Service.NomalManagerService;
import com.business.Service.ProcessInfoService;
import com.business.Service.UnzipConfigService;
import com.business.Service.UnzipConfirmService;
import com.business.config.Config;
import com.business.entity.ProcessInfo;
import com.business.entity.UnzipConfig;
import com.business.entity.UnzipConfirm;
import com.business.entity.WorkflowOrder;
import com.business.enums.*;
import com.business.util.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.*;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/3 11:17
 */
@Component
public class DataArchiveAction {
    private static final Logger logger= Logger.getLogger(DataArchiveAction.class);
    @Autowired
    private NomalManagerService nomalManagerService;
    @Autowired
    private UnzipConfirmService unzipConfirmService;
    @Autowired
    private UnzipConfigService unzipConfigService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Resource
    private ProcessUtil processUtil;

    public synchronized void processDataArchive(File dataTmpDir, WorkflowOrder t) throws Exception {

        logger.info("进入归档处理步骤，开始处理归档流程");
        File[] datFiles = dataTmpDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".dat");
            }
        });
        if (datFiles.length == 0 || datFiles.length > 2)
            throw new Exception("incorrect dat file count: " + datFiles.length);

        String jobTaskId = dataTmpDir.getName();
        //todo
        logger.info("清理历史数据"+t.getJobTaskID());
        List<String>tableList = TableName.getTableName("all");
        for (int i=0;i<tableList.size();i++){
            nomalManagerService.deleteByJobtaskId(tableList.get(i),t.getJobTaskID());
        }
        //将dat文件移至归档目录、生成元数据文件
        Satellite satellite=null;
        File S1File=null,S2File=null,R0Meta1=null,RoMeta2=null;     //归档后的原始码流文件。单通道情况则有一个为null
        for(File f:datFiles){
            String[] items=f.getName().split("_");
            satellite= Satellite.valueOf(items[0]);
            if(Channel.S1.getId().equals(items[1])){
                S1File = f;
                R0Meta1 = R0Meta.generateR0Meta(t,jobTaskId,S1File);    //生成原始码流元数据文件
                logger.info("R0Meta1 create Success");
            }else  if(Channel.S2.getId().equals(items[1])){
                S2File = f;
                RoMeta2 = R0Meta.generateR0Meta(t,jobTaskId,S2File);    //生成原始码流元数据文件
                logger.info("R0Meta2 create Success");
            }else{
                throw new Exception("invalid channel id: "+f);
            }
        }
        //生成各流程订单文本
        String unzipOrderXml=null;
        Date time=new Date();//统一用一个任务创建时间
        //TODO 根据卫星分不同的Ro_to_L0
        List<ProcessInfo>dataInfoList = new ArrayList<>();
        //switch (reportUtil.findBianma(t.getSatelliteName())){
        switch (t.getSatelliteName()){
            //todo GF1BCD
            case"GF-1B":
            case"GF-1C":
            case"GF-1D":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"GF1_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.GF1_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
                //todo ZY1E cbers04A
            case"ZY1E":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"ZY1E_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.ZY1E_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
            case "CBERS04A":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"CB4A_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.CB4A_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
            case "HJ-2A":
            case "HJ-2B":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"HJ_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.HJ_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
            case"CASEARTH":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"CAS_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.CAS_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
            case"ZY-3B":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"ZY3_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.ZY3_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
            case"GF-6":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"GF6_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.GF6_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
            case"GF-7":
                dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"GF7_R0_TO_L0");
                if(dataInfoList.size()==0) {
                    unzipOrderXml = ProcessType.GF7_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                }
                break;
        }
        //提交订单。注意，先提交解压缩流程（因为解压缩流程占用资源多，先提交可能会让其先占用到资源）
        //todo 建一个线程，查processInfo表
        if (unzipOrderXml!=null){
            processUtil.submitProcess(unzipOrderXml, Config.submit_order_timeout);
        }
    }
    private Map<String,Object> generateOrderParamsForGF_R0_TO_L0(File R0Meta1, File R0Meta2, WorkflowOrder t, Satellite satellite, String jobTaskId, File S1File, File S2File) throws Exception {
        //先尝试生成解压缩相关参数文件
        String signalId = null;
        if (S1File!=null){
            signalId = S1File.getName().replace(".dat","");
        }else if (S2File!=null){
            signalId = S2File.getName().replace(".dat","");
        }
        String[] items = signalId.split("_");
        // 参数文件所存放位置
        String partPath = DateUtil.getSdfDate().substring(0, 6) + "/" + DateUtil.getSdfDate().substring(0, 8) + "/" + signalId + "_" + DateUtil.getSdfDate().split("_")[1] + "/";
        logger.info("partPath:-----"+partPath);
        File outputDir=new File(Config.archive_root, satellite.name()+"/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId);
        logger.info("outputDir is :-------"+outputDir.toPath());
        if (!outputDir.exists()&&!outputDir.isDirectory()){
            MyHelper.CreateDirectory(outputDir);
        }
        UnzipConfirm unzipConfirm = new UnzipConfirm();
        //再生成工作流订单参数
        Map<String,Object> map=new HashMap<>();
        switch (t.getSatelliteName()){
            case"GF-6":
                Map<String,Object>unzipMap = new HashMap<>();
                Map<String,Object>syncMap =new HashMap<>();
                File SyncFile1 =null,SyncFile2 = null;
                // todo 生成高分6的帧同步订单
                if (S1File!=null){
                    syncMap = generateSyncParaFile(S1File,"S1",t,jobTaskId,partPath);
                    map.put("TASKSYNCFILE1",syncMap.get("TASKSYNCFILE1"));
                    SyncFile1 = new File(syncMap.get("OUT_DIR").toString());
                }
                if (S2File!=null){
                    syncMap = generateSyncParaFile(S2File,"S2",t,jobTaskId,partPath);
                    map.put("TASKSYNCFILE2",syncMap.get("TASKSYNCFILE2"));
                    SyncFile2 = new File(syncMap.get("OUT_DIR").toString());
                }
                map.put("SYNC_OUTPUTDIR",syncMap.get("SYNC_OUTPUTDIR"));
                unzipMap = generateGfUnzipParaFile(SyncFile1,"1",null,null,t,jobTaskId,signalId,partPath);
                map.put("TASKBASEFILE1",unzipMap.get("TASKBASEFILE1"));
                unzipMap = generateGfUnzipParaFile(SyncFile2,"2",null,null,t,jobTaskId,signalId,partPath);
                map.put("TASKBASEFILE2",unzipMap.get("TASKBASEFILE2"));
                break;
            case "GF-7":
                unzipMap = generateGfUnzipParaFile(S1File,"1",S2File,"2",t,jobTaskId,signalId,partPath);
                map.put("TASKBASEFILE1",unzipMap.get("TASKBASEFILE1"));
                break;
            case"CBERS04A":
                //todo  生成通道1的解压缩xml
                unzipConfirm = generateBaseParaFile(S1File,"1",S2File,"2",t,jobTaskId,signalId,partPath,"01");
                unzipConfirm.setStatus(0);
                unzipConfirm.setTaskId(t.getTaskSerialNumber());
                int unzipId = unzipConfirmService.selectMaxId();
                unzipConfirm.setId(unzipId);
                unzipConfirmService.saveConfrim(unzipConfirm.getId(),unzipConfirm.getActivityId(),unzipConfirm.getCancelActivityId(),unzipConfirm.getStatus());
                map.put("TASKBASEFILE1",unzipConfirm.getActivityId());
                //todo  生成通道1的解压缩xml
                unzipConfirm = generateBaseParaFile(S1File,"1",S2File,"2",t,jobTaskId,signalId,partPath,"02");
                unzipConfirm.setStatus(0);
                unzipConfirm.setTaskId(t.getTaskSerialNumber());
                int unzipId2 = unzipConfirmService.selectMaxId();
                unzipConfirm.setId(unzipId2);
                unzipConfirmService.saveConfrim(unzipConfirm.getId(),unzipConfirm.getActivityId(),unzipConfirm.getCancelActivityId(),unzipConfirm.getStatus());
                map.put("TASKBASEFILE2",unzipConfirm.getActivityId());
                break;
            default:
                //todo  生成通道1的解压缩xml
                unzipConfirm = generateBaseParaFile(S1File,"1",S2File,"2",t,jobTaskId,signalId,partPath,"");
                unzipConfirm.setStatus(0);
                unzipConfirm.setTaskId(t.getTaskSerialNumber());
                int unzipId3 = unzipConfirmService.selectMaxId();
                unzipConfirm.setId(unzipId3);
                unzipConfirmService.saveConfrim(unzipConfirm.getId(),unzipConfirm.getActivityId(),unzipConfirm.getCancelActivityId(),unzipConfirm.getStatus());
                map.put("TASKBASEFILE1",unzipConfirm.getActivityId());
                break;
        }
        map.put("S1META",R0Meta1);
        map.put("SIGNALID1",S1File==null?null:S1File.getName().replace(".dat",""));
        map.put("S2META",R0Meta2);
        map.put("SIGNALID2",S2File==null?null:S2File.getName().replace(".dat",""));
        map.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        map.put("SATELLITE", satellite.name());
        map.put("JOBTASKID",jobTaskId);
        map.put("OUTPUTDIR",outputDir);
        map.put("TASKSERIALNUMBER",t.getTaskSerialNumber());
        return map;
    }
    private Map<String,Object> generateSyncParaFile(File dat,String chanel,WorkflowOrder t,String jobTaskId,String pPath) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String[] items = dat.getName().split("_");
        String syncId = t.getSatelliteName().replace("-","0")+"_UNZIP_SYNC_" + DateUtil.getDays()+ "_"+items[2]+"_"+chanel;
        try {
            map.put("SYNCID", syncId);
            map.put("JOBTASKID", jobTaskId);
            map.put("STATION", items[4].substring(0, 2));
            map.put("SIGNALID", dat==null?null:dat.getName().replace(".dat",""));
            map.put("SIGNALFILE", dat);
            map.put("SATELLITENAME",t.getSatelliteName());
            //todo 输出数据到SIGNAL里面吧L0DATA
            File outDir = new File(Config.dataBank_dir+ "/"+items[0]+ "/SIGNAL/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId);
            if (outDir.exists()&&outDir.isDirectory()){
                MyHelper.emptyDir(outDir);
            }
            if (!outDir.exists()&&!outDir.isDirectory()){
                Files.createDirectories(outDir.toPath());
            }
            String name = map.get("SIGNALID").toString()+"_sync.dat";
            map.put("OUT_DIR",outDir.toString()+"/"+name);
            if(chanel.equals("S1")){
                map.put("MODE", "CH1");
                map.put("SOCKPORT", "20000");
            } else {
                map.put("MODE", "CH2");
                map.put("SOCKPORT", "20001");
            }
            UnzipParaTemplate template=UnzipParaTemplate.TASK_SYNC_FILE;
            String str=template.generateParaString(map);
            File f=new File(Config.unzip_bak_dir, pPath + syncId+".param.xml");
            Files.createDirectories(f.getParentFile().toPath());  //必须先尝试创建各级目录
            Files.write(f.toPath(),str.getBytes("UTF-8"));        //参数文件必须规定为UTF-8编码
            if (chanel.equals("S1")){
                map.put("TASKSYNCFILE1",f.toString());
            }else if (chanel.equals("S2")){
                map.put("TASKSYNCFILE2",f.toString());
            }
            map.put("SYNC_OUTPUTDIR",Config.dataBank_dir+ "/"+items[0]+ "/SIGNAL/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId);
        }catch (Exception e){
            logger.info(e);
        }
        return map;
    }
    /*
    *todo 高分三期的解压缩调度程序
    */
    private Map<String,Object> generateGfUnzipParaFile(File dat,String chanel,File dat2,String chanel2,WorkflowOrder t,String jobTaskId,String signalId,String pPath) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("JOBTASKID",t.getJobTaskID());
        String[] items = signalId.split("_");
        File l0DataDir=null,dir=null;
        List<String>sensorList1 = Sensor.fromOMOSensor(t.getSatelliteName());
        if (t.getSatelliteName().equals("GF-6")){
            for (int i=0;i<sensorList1.size();i++){
                l0DataDir = new File(MyHelper.ParseStringToPath(Config.archive_unzip,items,jobTaskId,"/"+sensorList1.get(i)));
                dir = new File(MyHelper.ParseStringToPath(Config.archive_root,items,jobTaskId,"/"+sensorList1.get(i)));
                MyHelper.CreateDirectory(dir);
                int j = i+1;
                map.put("OUTPUTDIR"+j, l0DataDir);
            }
        }else if (t.getSatelliteName().equals("GF-7")){
            l0DataDir = new File(MyHelper.ParseStringToPath(Config.archive_unzip,items,jobTaskId,"/"));
            dir = new File(MyHelper.ParseStringToPath(Config.archive_root,items,jobTaskId,"/"));
            MyHelper.CreateDirectory(dir);
            map.put("OUTPUTDIR1", l0DataDir);
        }
        if (t.getSatelliteName().equals("GF-7")){
            //todo 检查原始数据是1024帧长还是896帧长
            String VCModel = R0Meta.checkMode(dat);
            map.put("VCMMODE",VCModel);
            map.put("SYNCPARAFILE1",dat.toString());
            map.put("SYNCPARAFILE2",dat2.toString());

        } else if (t.getSatelliteName().equals("GF-6")){
            String datName = dat.toString().replace(Config.data_absolute_dir,Config.data_absolute_dir);
            if (chanel.equals("1")){
                map.put("SIGNALID1",datName);
            }
            if(chanel.equals("2")){
                map.put("SIGNALID2",datName);
            }
        }
        map.put("SATELLITE",items[0]);
        map.put("YYYYMMDD_XXXXXX",DateUtil.getSdfDate());
        UnzipParaTemplate template = null;
        if (t.getSatelliteName().equals("GF-6")){
            template=UnzipParaTemplate.GF6_UNZIP_FILE;
        }else if (t.getSatelliteName().equals("GF-7")){
            template=UnzipParaTemplate.GF7_UNZIP_FILE;
        }
        String str=template.generateParaString(map);
        File f=new File(Config.unzip_bak_dir, pPath + "UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX")+"_"+items[0]+"_"+items[2]+"_"+chanel+".param.xml");
        Files.createDirectories(f.getParentFile().toPath());  //必须先尝试创建各级目录
        Files.write(f.toPath(),str.getBytes("UTF-8"));        //参数文件必须规定为UTF-8编码
        if (chanel.equals("1")){
            map.put("TASKBASEFILE1",f.toString());
        }
        if (chanel.equals("2")){
            map.put("TASKBASEFILE2",f.toString());
        }
        return map;
    }
    private UnzipConfirm generateBaseParaFile(File dat1,String chanel1,File dat2,String chanel2,WorkflowOrder t,String jobTaskId,String signalId,String pPath,String CB4ANUmber) throws Exception {
        Map<String, Object> map = new HashMap<>();
        UnzipConfig unzip = unzipConfigService.selectBySaliteName(t.getSatelliteName());
        String[] items = signalId.split("_");
        //File l0DataDir=null,srv=null;
        File dir=null,srv=null;
        //switch (reportUtil.findBianma(t.getSatelliteName()))
        List<String>sensorList1 = new ArrayList<>();
        if (t.getSatelliteName().equals("ZY-3B")){
            sensorList1 = Sensor.fromOMOSensor("ZY302");
        }else {
            sensorList1 = Sensor.fromOMOSensor(t.getSatelliteName());
        }
        for (int i=0;i<sensorList1.size();i++){
           // l0DataDir = new File(MyHelper.ParseStringToPath(Config.archive_unzip,items,jobTaskId,"/"+sensorList1.get(i)));
            dir = new File(MyHelper.ParseStringToPath(Config.archive_root,items,jobTaskId,"/"+sensorList1.get(i)));
            MyHelper.CreateDirectory(dir);
            int j = i+1;
            if (t.getSatelliteName().equals("CBERS04A")||t.getSatelliteName().equals("HJ-2A")||t.getSatelliteName().equals("HJ-2B")){
                map.put("OUTPUTDIR"+j, dir.toString());
            }else {
                String outDir =  MyHelper.ChangeToWindowsPath(dir);
                map.put("OUTPUTDIR"+j, outDir);
            }
        }
        //todo 解压缩改好后，这个加上Config.unzip_dir+,没个卵用，不要他
        srv = new File(MyHelper.ParseStringToPath(Config.archive_root,items,jobTaskId,"/srv"));
        //srvFile = new File(MyHelper.ParseStringToPath(Config.archive_root,items,jobTaskId,"/srv"));
        //MyHelper.CreateDirectory(srvFile);
        map.put("CRATETIME",DateUtil.getTime());
        map.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        map.put("ACTIVITYTYPE","NEW");
        map.put("SATELLITE",items[0]);
        map.put("STATION", items[4].substring(0, 2));
        map.put("ORBITID", items[2]);
        map.put("SENSORLIST",unzip.getSensorList());

        logger.info("任务的优先级为"+t.getTaskPriority());
        //todo  2019/10/30 新增解压缩优先级
        switch (t.getTaskPriority()){
            case "normal":
                map.put("PRIORITY","5");
                break;
            case "preference":
                map.put("PRIORITY","4");
                break;
            case "urgency":
                map.put("PRIORITY","3");
                break;
        }
        //todo 通道1
        if (dat1!=null){
            //todo 单通道放单通道的传感器
            if (dat2==null){
                String sensor1 = "";
                String str[] = unzip.getSensorList().split(";");
                for (int i=0;i<str.length-2;i++){
                    sensor1 = sensor1+str[i]+";";
                }
                map.put("SENSORLIST",sensor1);
            }
            map.put("CHANNEL1",chanel1);
            //todo 暂时定义四个挂载盘的路径，
            if (t.getSatelliteName().equals("ZY1E")){
                map.put("SYNCPARAFILE2",MyHelper.ChangeToWindowsPath(dat1));
                map.put("SKIPHEAD2",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES2",dat1.length());
                }else {
                    map.put("READBYTES2",unzip.getReadBytesS1());
                }
            }else {
                map.put("SYNCPARAFILE1",MyHelper.ChangeToWindowsPath(dat1));
                map.put("SKIPHEAD1",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES1",dat1.length());
                }else {
                    map.put("READBYTES1",unzip.getReadBytesS1());
                }
            }
        }
        //todo 通道2
        if (dat2!=null){
            if (dat1==null){
                //todo 单通道放单通道的传感器
                String sensor2 = "";
                String str[] = unzip.getSensorList().split(";");
                for (int i=2;i<str.length;i++){
                    sensor2 = sensor2+str[i]+";";
                }
                map.put("SENSORLIST",sensor2);
            }
            map.put("CHANNEL2",chanel2);
            if (t.getSatelliteName().equals("ZY1E")){
                map.put("SYNCPARAFILE1",MyHelper.ChangeToWindowsPath(dat2));
                map.put("SKIPHEAD1",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES1",dat2.length());
                }else {
                    map.put("READBYTES1",unzip.getReadBytesS1());
                }
            }else {
                map.put("SYNCPARAFILE2",MyHelper.ChangeToWindowsPath(dat2));
                map.put("SKIPHEAD2",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES2",dat2.length());
                }else {
                    map.put("READBYTES2",unzip.getReadBytesS1());
                }
            }
        }
        if (!"".equals(CB4ANUmber)) {
            map.put("OUTPUTDIR",srv);
            //todo 区分CB4A和windows版原始数据路径
            map.put("SYNCPARAFILE1",dat1.toString());
            map.put("SYNCPARAFILE2",dat2.toString());
            List<String>cb4aSensor = Sensor.fromOMOSensor(t.getSatelliteName()+CB4ANUmber);
            for (int i=1;i<cb4aSensor.size()+1;i++){
                map.put("SENSOR"+i,cb4aSensor.get(i-1));
            }
            if (CB4ANUmber.equals("01")){
                map.put("SENSORLIST",Config.cb4asensorlist1);
            }else if (CB4ANUmber.equals("02")){
                map.put("SENSORLIST",Config.cb4asensorlist2);
            }
        } else {
            map.put("OUTPUTDIR",MyHelper.ChangeToWindowsPath(srv));
            String sensorList = unzip.getSensorList();
            map.put("SENSOR1",sensorList.split(";")[0]);
            map.put("SENSOR2",sensorList.split(";")[1]);
            map.put("SENSOR3",sensorList.split(";")[2]);
            map.put("SENSOR4",sensorList.split(";")[3]);
        }

        UnzipParaTemplate template = null;
        if (dat1==null||dat2==null){
            template = UnzipParaTemplate.Task_UNZIP;
        }else {
            template=UnzipParaTemplate.TASK_BASE_FILE;
        }
        File f=new File(Config.unzip_bak_dir, pPath + "UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX")+"_"+items[0]+"_"+items[2]+".param.xml");
        MyHelper.CreateDirectory(f.getParentFile());
        Files.write(f.toPath(),template.generateParaString(map).getBytes("UTF-8"));        //参数文件必须规定为UTF-8编码
        //todo 创建解压缩文件的时候同时创建一个cancel文件放到cancel文件夹内,直接放到根目录
        Thread.sleep(1000);
        map.put("TARGET_ACTIVITY_ID","UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX"));
        map.put("ACTIVITYTYPE","CANCEL");
        map.put("YYYYMMDD_XXXXXX",DateUtil.getSdfDate());
        UnzipParaTemplate template1 = UnzipParaTemplate.TASK_BASE_FILE;
        String strCancel = template1.generateParaString(map);
        File cancelF = new File(Config.unzip_cancel,"UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX")+"_"+items[0]+"_"+items[2]+".param.xml");
        MyHelper.CreateDirectory(cancelF.getParentFile());
        Files.write(cancelF.toPath(),strCancel.getBytes("UTF-8"));
        //todo 取消的文件
        UnzipConfirm unzipConfirm = new UnzipConfirm();
        unzipConfirm.setActivityId(f.toString());
        unzipConfirm.setCancelActivityId(cancelF.toString());
        return unzipConfirm;
    }

}
