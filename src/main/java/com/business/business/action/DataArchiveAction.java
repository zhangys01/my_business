package com.business.business.action;

import com.business.business.Service.TableManagerService;
import com.business.business.Service.UnzipConfigService;
import com.business.business.Service.UnzipConfirmService;
import com.business.business.Service.WorkFlowOrderService;
import com.business.business.config.Config;
import com.business.business.entity.UnzipConfig;
import com.business.business.entity.UnzipConfirm;
import com.business.business.entity.WorkflowOrder;
import com.business.business.enums.Channel;
import com.business.business.enums.ProcessType;
import com.business.business.enums.Satellite;
import com.business.business.util.DateUtil;
import com.business.business.util.ProcessUtil;
import com.business.business.workflow.R0Meta;
import com.business.business.workflow.UnzipParaTemplate;
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
    private TableManagerService tableManagerService;
    @Autowired
    private UnzipConfirmService unzipConfirmService;
    @Autowired
    private UnzipConfigService unzipConfigService;
    @Resource
    ProcessUtil processUtil;
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
        //todo 暂时注释，不知道有啥用
        //deleteTaskIdByCustom(jobTaskId);    // 删除 TaskId 相关记录，记录来源于手动订单
        //todo
        logger.info("清理历史数据"+t.getJobTaskID());
        tableManagerService.deleteByJobtaskId(t.getJobTaskID());

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
        switch (t.getSatelliteName()){
            case"GF-1B":
            case"GF-1C":
            case"GF-1D":
                unzipOrderXml = ProcessType.GF1_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File,time));
                break;
            case"ZY-3B":
                unzipOrderXml = ProcessType.ZY3_R0_TO_L0.generateOrderXml(generateOrderParamsForGF_R0_TO_L0(R0Meta1,RoMeta2,t,satellite,jobTaskId,S1File,S2File,time));
                break;
        }
        //提交订单。注意，先提交解压缩流程（因为解压缩流程占用资源多，先提交可能会让其先占用到资源）
        //todo 建一个线程，查processInfo表
        processUtil.submitProcess(unzipOrderXml, Config.submit_order_timeout);
        //TODO 启动检查线程
        Thread.sleep(25000);
    }
    private Map<String,Object> generateOrderParamsForGF_R0_TO_L0(File R0Meta1, File R0Meta2, WorkflowOrder t, Satellite satellite, String jobTaskId, File S1File, File S2File, Date time) throws Exception {
        //先尝试生成解压缩相关参数文件
        String signalId = null;
        if (S1File!=null){
            signalId = S1File.getName().replace(".dat","");
        }else if (S2File!=null){
            signalId = S2File.getName().replace(".dat","");
        }
        String[] items = signalId.split("_");
        String orderidSuffix= DateUtil.getSdfDate();
        // 参数文件所存放位置
        String partPath = orderidSuffix.substring(0, 6) + "/" + orderidSuffix.substring(0, 8) + "/" + signalId + "_" + orderidSuffix.split("_")[1] + "/";
        logger.info("partPath:-----"+partPath);
        File outputDir=new File(Config.archive_root, satellite.name()+"/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId);
        logger.info("outputDir is :-------"+outputDir.toPath());
        if (!outputDir.exists()&&!outputDir.isDirectory()){
            Files.createDirectories(outputDir.toPath());  //必须先尝试创建各级目录
        }
        UnzipConfirm unzipConfirm = new UnzipConfirm();
        //再生成工作流订单参数
        Map<String,Object> map=new HashMap<>();

        //todo  生成通道1的解压缩xml
        unzipConfirm = generateBaseParaFile(S1File,"1",S2File,"2",t,jobTaskId,orderidSuffix,signalId,partPath);
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
        UnzipConfig unzip = unzipConfigService.selectBySaliteName(t.getSatelliteName());
        String[] items = signalId.split("_");
        File l0DataDir1=null,l0DataDir2 = null,l0DataDir3=null,l0DataDir4=null,srv=null;
        File dir1=null,dir2=null,dir3=null,dir4=null,srvFile=null;
        switch (t.getSatelliteName()){
            case"GF-1B":
            case"GF-1C":
            case"GF-1D":
                //todo 解压缩改好之后，这四个加上 Config.unzip_dir+
                l0DataDir1 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/PAN1");
                l0DataDir2 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/MSS1");
                l0DataDir3 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/PAN2");
                l0DataDir4 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/MSS2");
                //todo Config.archive_root 替换"/KJ125ZL/DataBank/"
                dir1 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/PAN1");
                dir2 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/MSS1");
                dir3 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/PAN2");
                dir4 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/MSS2");
                break;
            case"ZY-3B":
                //todo 解压缩改好之后，这四个加上 Config.unzip_dir+
                l0DataDir1 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/NAD");
                l0DataDir2 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/MUX");
                l0DataDir3 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/FWD");
                l0DataDir4 = new File(Config.archive_unzip+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/BWD");
                dir1 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/NAD");
                dir2 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/MUX");
                dir3 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/FWD");
                dir4 = new File(Config.archive_root+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/BWD");
                break;
        }
        //todo 解压缩改好后，这个加上Config.unzip_dir+
        srv = new File(Config.archive_unzip+"/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/srv");
        srvFile = new File(Config.archive_root+"/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/srv");
        if (!srvFile.exists()||!srvFile.isDirectory()){
            Files.createDirectories(srvFile.toPath());  //必须先尝试创建各级目录
        }
        if (!dir1.exists()||!dir1.isDirectory()){
            Files.createDirectories(dir1.toPath());  //必须先尝试创建各级目录
        }
        if (!dir2.exists()||!dir2.isDirectory()){
            Files.createDirectories(dir2.toPath());  //必须先尝试创建各级目录
        }
        if (!dir3.exists()||!dir3.isDirectory()){
            Files.createDirectories(dir3.toPath());  //必须先尝试创建各级目录
        }
        if (!dir4.exists()||!dir4.isDirectory()){
            Files.createDirectories(dir4.toPath());  //必须先尝试创建各级目录
        }
        Map<String, Object> map = new HashMap<>();
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
            String filePath = "";
            switch (dat1.toString().split("/")[1]){
                case "KJ125ZL":
                    filePath = dat1.toString().replace("/KJ125ZL","Z:");
                    break;
                case "KJ125ZL_2":
                    filePath = dat1.toString().replace("/KJ125ZL_2","Y:");
                    break;
                case "/pools/POOL/TMP":
                    filePath = dat1.toString().replace("/pools/POOL/TMP","Z:");
                    break;
                case "/pools/POOL/WORk":
                    filePath = dat1.toString().replace("/pools/POOL/WORk","Y:");
                    break;
                case "raw":
                    filePath = "S:"+dat1;
                    break;
                case "ncsfs":
                    filePath = dat1.toString().replace("/ncsfs","T:");
                    break;
            }
            map.put("SYNCPARAFILE1",filePath);
            map.put("SKIPHEAD1",unzip.getSkipHeadS1());

            if (unzip.getReadBytesS1()==null||unzip.getReadBytesS1().equals("0")){
                map.put("READBYTES1",dat1.length());
            }else {
                map.put("READBYTES1",unzip.getReadBytesS1());
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

            String filePath = "";
            switch (dat2.toString().split("/")[1]){
                case "KJ125ZL":
                    filePath = dat2.toString().replace("/KJ125ZL","Z:");
                    break;
                case "KJ125ZL_2":
                    filePath = dat2.toString().replace("/KJ125ZL_2","Y:");
                    break;
                case "/pools/POOL/TMP":
                    filePath = dat2.toString().replace("/pools/POOL/TMP","Z:");
                    break;
                case "/pools/POOL/WORk":
                    filePath = dat2.toString().replace("/pools/POOL/WORk","Y:");
                    break;
                case "raw":
                    filePath = "S:"+dat2;
                    break;
                case "ncsfs":
                    filePath = dat2.toString().replace("/ncsfs","T:");
                    break;
            }
            map.put("SYNCPARAFILE2",filePath);
            map.put("SKIPHEAD2",unzip.getSkipHeadS2());
            if (unzip.getReadBytesS2()==null||unzip.getReadBytesS2().equals("0")){
                map.put("READBYTES2",dat2.length());
            }else{
                map.put("READBYTES2",unzip.getReadBytesS2());
            }
        }
        String sensorList = unzip.getSensorList();
        map.put("SENSOR1",sensorList.split(";")[0]);
        map.put("SENSOR2",sensorList.split(";")[1]);
        map.put("SENSOR3",sensorList.split(";")[2]);
        map.put("SENSOR4",sensorList.split(";")[3]);
        map.put("OUTPUTDIR1", l0DataDir1);
        map.put("OUTPUTDIR2", l0DataDir2);
        map.put("OUTPUTDIR3", l0DataDir3);
        map.put("OUTPUTDIR4", l0DataDir4);
        map.put("OUTPUTDIR",srv);
        UnzipParaTemplate template = null;
        if (dat1==null||dat2==null){
            template = UnzipParaTemplate.Task_UNZIP;
        }else {
            template=UnzipParaTemplate.TASK_BASE_FILE;
        }
        String str=template.generateParaString(map);
        File f=new File(Config.unzip_bak_dir, pPath + "UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX")+"_"+items[0]+"_"+items[2]+".param.xml");
        Files.createDirectories(f.getParentFile().toPath());  //必须先尝试创建各级目录
        Files.write(f.toPath(),str.getBytes("UTF-8"));        //参数文件必须规定为UTF-8编码
        //todo 创建解压缩文件的时候同时创建一个cancel文件放到cancel文件夹内,直接放到根目录
        Thread.sleep(1000);
        map.put("TARGET_ACTIVITY_ID","UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX"));
        map.put("ACTIVITYTYPE","CANCEL");
        map.put("YYYYMMDD_XXXXXX",DateUtil.getSdfDate());
        UnzipParaTemplate template1 = UnzipParaTemplate.TASK_BASE_FILE;
        String strCancel = template1.generateParaString(map);
        File cancelF = new File(Config.unzip_cancel,"UNZIP_BASE_"+map.get("YYYYMMDD_XXXXXX")+"_"+items[0]+"_"+items[2]+".param.xml");
        Files.createDirectories(cancelF.getParentFile().toPath());
        Files.write(cancelF.toPath(),strCancel.getBytes("UTF-8"));
        //todo 取消的文件
        UnzipConfirm unzipConfirm = new UnzipConfirm();
        unzipConfirm.setActivityId(f.toString());
        unzipConfirm.setCancelActivityId(cancelF.toString());
        return unzipConfirm;
    }
}
