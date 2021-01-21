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
    private static final Logger logger = Logger.getLogger(DataArchiveAction.class);
    @Autowired
    private NomalManagerService nomalManagerService;
    @Autowired
    private UnzipConfirmService unzipConfirmService;
    @Autowired
    private UnzipConfigService unzipConfigService;
  /*  @Autowired
    private ProcessInfoService processInfoService;*/
    @Resource
    private ProcessUtil processUtil;

    public void processDataArchive(File dataTmpDir, WorkflowOrder t) throws Exception {
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
            }else  if(Channel.S2.getId().equals(items[1])){
                S2File = f;
                RoMeta2 = R0Meta.generateR0Meta(t,jobTaskId,S2File);    //生成原始码流元数据文件
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
                unzipOrderXml = ProcessType.GF1_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File));
                break;
                //todo ZY1E cbers04A
            case"ZY-1E":
               /* dataInfoList = processInfoService.getProcessList(t.getTaskSerialNumber(),"ZY1E_R0_TO_L0");
                if(dataInfoList.size()==0) {*/
                    unzipOrderXml = ProcessType.ZY1E_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
              //  }
                break;
            case "CBERS04A":
                unzipOrderXml = ProcessType.CB4A_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1, RoMeta2, t, satellite, jobTaskId, S1File, S2File));
                break;
            case"CASEARTH":
                unzipOrderXml = ProcessType.CAS_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File));
                break;
            case"ZY-3B":
                unzipOrderXml = ProcessType.ZY3_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File));
                break;
            case"GF-6":
                unzipOrderXml = ProcessType.GF6_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File));
                break;
            case"GF-7":
                unzipOrderXml = ProcessType.GF7_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File));
                break;
        }
        //提交订单。注意，先提交解压缩流程（因为解压缩流程占用资源多，先提交可能会让其先占用到资源）
        //todo 建一个线程，查processInfo表
        processUtil.submitProcess(unzipOrderXml, Config.submit_order_timeout);
        //TODO 启动检查线程
        Thread.sleep(25000);
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

        //todo  生成通道1的解压缩xml
        unzipConfirm = generateBaseParaFile(S1File,"1",S2File,"2",t,jobTaskId,DateUtil.getSdfDate(),signalId,partPath);
        unzipConfirm.setStatus(0);
        unzipConfirm.setTaskId(t.getTaskSerialNumber());
        unzipConfirmService.save(unzipConfirm);
        map.put("TASKBASEFILE1",unzipConfirm.getActivityId());
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
    private UnzipConfirm generateBaseParaFile(File dat1,String chanel1,File dat2,String chanel2,WorkflowOrder t,String jobTaskId,String orderidSuffix,String signalId,String pPath) throws Exception {
        Map<String, Object> map = new HashMap<>();
        UnzipConfig unzip = unzipConfigService.selectBySaliteName(t.getSatelliteName());
        String[] items = signalId.split("_");
        File l0DataDir=null,srv=null;
        File dir=null,srvFile=null;
        //switch (reportUtil.findBianma(t.getSatelliteName()))
        List<String>sensorList1 = new ArrayList<>();
        if (t.getSatelliteName().equals("ZY-3B")){
            sensorList1 = Sensor.fromOMOSensor("ZY302");
        }else {
            sensorList1 = Sensor.fromOMOSensor(t.getSatelliteName());
        }
        for (int i=0;i<sensorList1.size();i++){
            l0DataDir = new File(MyHelper.Creatpathname(Config.archive_unzip,items,jobTaskId,"/"+sensorList1.get(i)));
            dir = new File(MyHelper.Creatpathname(Config.archive_root,items,jobTaskId,"/"+sensorList1.get(i)));
            MyHelper.CreateDirectory(dir);
            int j = i+1;
            map.put("OUTPUTDIR"+j, l0DataDir);
        }
        //todo 解压缩改好后，这个加上Config.unzip_dir+
        srv = new File(MyHelper.Creatpathname(Config.archive_unzip,items,jobTaskId,"/srv"));
        srvFile = new File(MyHelper.Creatpathname(Config.archive_root,items,jobTaskId,"/srv"));
        MyHelper.CreateDirectory(srvFile);

        map.put("CRATETIME",DateUtil.getTime());
        map.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        map.put("ACTIVITYTYPE","NEW");
        map.put("SATELLITE",items[0]);
        map.put("STATION", items[4].substring(0, 2));
        map.put("ORBITID", items[2]);
        map.put("SENSORLIST",unzip.getSensorList());

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
                map.put("SYNCPARAFILE2",MyHelper.FilePath(dat1));
                map.put("SKIPHEAD2",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES2",dat1.length());
                }else {
                    map.put("READBYTES2",unzip.getReadBytesS1());
                }
            }else {
                map.put("SYNCPARAFILE1",MyHelper.FilePath(dat1));
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
                map.put("SYNCPARAFILE1",MyHelper.FilePath(dat2));
                map.put("SKIPHEAD1",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES1",dat2.length());
                }else {
                    map.put("READBYTES1",unzip.getReadBytesS1());
                }
            }else {
                map.put("SYNCPARAFILE2",MyHelper.FilePath(dat2));
                map.put("SKIPHEAD2",unzip.getSkipHeadS1());
                if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                    map.put("READBYTES2",dat2.length());
                }else {
                    map.put("READBYTES2",unzip.getReadBytesS1());
                }
            }
        }
        String sensorList = unzip.getSensorList();
        map.put("SENSOR1",sensorList.split(";")[0]);
        map.put("SENSOR2",sensorList.split(";")[1]);
        map.put("SENSOR3",sensorList.split(";")[2]);
        map.put("SENSOR4",sensorList.split(";")[3]);

        map.put("OUTPUTDIR",srv);
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
