package com.business.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.business.Service.*;
import com.business.action.*;
import com.business.action.QATaskInqAction;
import com.business.adapter.String2ListXmlAdapter;
import com.business.config.Config;
import com.business.constants.Constants;
import com.business.db.OracleProcessInfoImpl;
import com.business.entity.*;
import com.business.enums.Channel;
import com.business.enums.ProcessType;
import com.business.enums.ResponseType;
import com.business.info.ArchiveWorkflowInfo;
import com.business.info.QATaskWorkflowInfo;
import com.business.message.DataArchiveRep;
import com.business.message.QATask;
import com.business.message.QATaskRep;
import com.business.message.Response;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ReportUtil {
    private static final Logger logger = Logger.getLogger(ReportUtil.class);
    @Autowired
    private WorkFlowOrderService orderService;
    @Autowired
    private ProcessInfoService processInfoService;
    @Autowired
    private WorkFlowDataArchiveService workFlowDataArchiveService;
    private Marshaller marshaller;
    /* @Autowired
    private SysDictionariesService dictionariesService;*/
    @Autowired
    private Ml0InfoService ml0InfoService;
    @Resource
    private ProcessUtil processUtil;
    @Resource
    private DataArchiveInqAction dataArchiveInqAction;
    private static String QAReportFile;    //相对路径
    private static OracleProcessInfoImpl oracleInfoImpl;
    private static final int BUFFER_SIZE = 2 * 1024;

    //todo 卫星分类型
  /*  public String findBianma(String name)throws Exception{
        SysDictionaries dictionaries = dictionariesService.getById(dictionariesService.findByName(name).getParentId());
        return dictionaries.getBianma();
    }*/

    public void ReportStatusSuccess(WorkflowOrder order,String taskmode,List<ProcessInfo> infoList)throws Exception{
        if (infoList.size()!=0 && getProcessStatus(infoList,order).equals("success")){
            getReportStatus(order,taskmode);
        }
    }
    public void ReportStatusQ61(WorkflowOrder order, List<ProcessInfo>dataInfoList,QATaskWorkflowInfo wi)throws Exception{
        if (dataInfoList.size()==2){
            if (dataInfoList.get(0).getStatus().equals("Completed")&&dataInfoList.get(1).getStatus().equals("Completed")){
                getReportStatus(order,"Q61");
            }else if(dataInfoList.get(0).getStatus().equals("Aborted")||dataInfoList.get(1).getStatus().equals("Aborted")){
                order.setOrderStatus("4");
                orderService.updateById(order);
                //todo 生成QATaskRep
                wi.state = 0;
                generateQATaskRep(wi,order);
            }
        }else if (dataInfoList.size()==1){
            if (dataInfoList.get(0).getStatus().equals("Completed")){
                getReportStatus(order,"Q61");
            }else if(dataInfoList.get(0).getStatus().equals("Aborted")){
                order.setOrderStatus("4");
                orderService.updateById(order);
                //todo 生成QATaskRep
                wi.state = 0;
                generateQATaskRep(wi,order);
            }
        }
    }
    public void ReportStatusQ61Q63(String status1,WorkflowOrder order,List<ProcessInfo>InfoList,QATaskWorkflowInfo wi)throws Exception{
        String status2="";
        if (InfoList.size()!=0){
            status2 = getProcessStatus(InfoList,order);
        }
        if (status1.equals("success")&&status2.equals("success")){
            getReportStatus(order,"Q61;Q63");
        }else if (status1.equals("error")||status2.equals("error")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            //todo 生成QATaskRep
            wi.state = 0;
            generateQATaskRep(wi,order);
        }else if (status1.equals("success")&&status2.equals("error")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            getReportStatus(order,"Q61");
        }else if (status1.equals("error")&&status2.equals("success")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            getReportStatus(order,"Q63");
        }
    }

    public void ReportStatusQ62Q63(WorkflowOrder order,List<ProcessInfo>InfoList,QATaskWorkflowInfo wi)throws Exception{
        String status2="";
        if (InfoList.size()!=0){
             status2 = getProcessStatus(InfoList,order);
           }
        if (status2.equals("success")) {
            getReportStatus(order,"Q62;Q63");
        } else if (status2.equals("errors")) {
            order.setOrderStatus("4");
            orderService.updateById(order);
            getReportStatus(order,"Q62");
        }
    }
    public void ReportStatusQ61Q62Q63(String status1,WorkflowOrder order, List<ProcessInfo>InfoList,QATaskWorkflowInfo wi)throws Exception{
        String status2 = "";
        if (InfoList.size()!=0){
            status2 = getProcessStatus(InfoList,order);
        }
        if (status1.equals("success")&&status2.equals("success")){
            getReportStatus(order,"Q61;Q62;Q63");
        }else if (status1.equals("error")||status2.equals("error")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            //todo 生成QATaskRep
           
            wi.state = 0;
            generateQATaskRep(wi,order);
        }else if (status1.equals("success")&&status2.equals("error")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            getReportStatus(order,"Q61");
        }else if (status1.equals("error")&&status2.equals("success")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            getReportStatus(order,"Q63");
        }
    }

    public void ReportStatusQ64(ProcessInfo Q64info ,WorkflowOrder order,QATaskWorkflowInfo wi)throws Exception{
        String status1 = "";
        if (Q64info!=null){
        if (Q64info.getStatus().equals("Completed")){
            List<ProcessInfo>reportList = processInfoService.getProcessList(order.getTaskSerialNumber(),"KJ125_Q64_R0REPORT");
            status1 = getProcessStatus(reportList,order);
            if (status1.equals("success")){
                ProcessInfo info = processInfoService.getProcessByName(order.getTaskSerialNumber(),"KJ125_Q64");
                if (info==null){
                    doTriggerQ64(order);
                }else {
                    if(info.getStatus().equals("Completed")){
                        order.setOrderStatus("3");
                        orderService.updateById(order);
                        //todo 生成QATaskRep
                       
                        wi.state = 1;
                        generateQATaskRep(wi,order);
                    }else if (info.getStatus().equals("Aborted")){
                        order.setOrderStatus("4");
                        orderService.updateById(order);
                        //todo 生成QATaskRep
                       
                        wi.state = 0;
                        generateQATaskRep(wi,order);
                    }
                }
            }else if (status1.equals("error")){
                order.setOrderStatus("4");
                orderService.updateById(order);
                //todo 生成QATaskRep
               
                wi.state = 0;
                generateQATaskRep(wi,order);
            }
        }else if (Q64info.getStatus().equals("Aborted")){
            order.setOrderStatus("4");
            orderService.updateById(order);
            //todo 生成QATaskRep
            wi.state = 0;
            generateQATaskRep(wi,order);;
        }
    }
    }

    public void ReportStatusQ65(List<ProcessInfo> infoList ,WorkflowOrder order,QATaskWorkflowInfo wi)throws Exception{
        if (infoList.size()!=0){
            if (infoList.get(0).getStatus().equals("Completed")){
                order.setOrderStatus("3");
                orderService.updateById(order);
                //todo 生成QATaskRep
                wi.state = 1;
                generateQATaskRep(wi,order);
            }else if (infoList.get(0).getStatus().equals("Aborted")){
                order.setOrderStatus("4");
                orderService.updateById(order);
                //todo 生成QATaskRep
               
                wi.state = 0;
                generateQATaskRep(wi,order);
            }
        }
    }
    public String getProcessStatus(List<ProcessInfo> infoList, WorkflowOrder order)throws Exception{
        String status = "";
        for (int i = 0; i < infoList.size(); i++) {
            if (infoList.get(i).getStatus().equals("Completed")) {
                status = "success";
            } else if (infoList.get(i).getStatus().equals("Aborted")) {
                status = "error";
                order.setEndTime(DateUtil.getTime());
                order.setOrderStatus("4");
                orderService.updateById(order);
                break;
            } else {
                status = "running";
                break;
            }
        }
        return status;
    }

    public void getReportStatus(WorkflowOrder order,String orderType)throws Exception{
        QATaskRep rep1;
        QATaskWorkflowInfo wi = new QATaskWorkflowInfo();
        ProcessType processType;
        Map orderParams;
        Date time = new Date();
        ProcessInfo QaInfo = processInfoService.getProcessByName(order.getTaskSerialNumber(), "KJ125_Q61_62_63_QAReport");
        if (QaInfo == null) {
            processType = ProcessType.KJ125_Q61_62_63_QAReport;
            orderParams = generateOrderParamsForGF_Q61_62_63_QAReport(order,orderType);
            //构建流程订单
            String orderXml = processType.generateOrderXml(orderParams);
            String orderId = processUtil.submitProcess(orderXml, Config.submit_order_timeout);
        } else {
            if (QaInfo.getStatus().equals("Completed")) {
                order.setOrderStatus("3");
                order.setEndTime(DateUtil.getTime());
                orderService.updateById(order);
                //todo 生成QATaskRep
               
                wi.state = 1;
                rep1 = generateQATaskRep(wi, order);
            } else if (QaInfo.getStatus().equals("Aborted")) {
                order.setOrderStatus("4");
                order.setEndTime(DateUtil.getTime());
                orderService.updateById(order);
                //todo 生成QATaskRep
               
                wi.state = 0;
                rep1 = generateQATaskRep(wi, order);
            }
        }
    }

    private Map generateOrderParamsForGF_Q61_62_63_QAReport(WorkflowOrder t,String taskmode) throws Exception {
        Map<String, Object> ret = new HashMap();
        ret.put("TASKSERIALNUMBER",t.getTaskSerialNumber());
        ret.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        ret.put("SATELLITE", t.getSatelliteName().replace("-",""));
        ret.put("TASKMODE", taskmode);
        ret.put("JOBTASKID", t.getJobTaskID());
        ret.put("TASKID", t.getTaskSerialNumber());
        ret.put("CHANNEL", t.getChannelID());
        ret.put("SENSOR", t.getSensorName());
        if(t.getDataSelectType() != null&&!"".equals(t.getDataSelectType()))
            ret.put("DATASELECTTYPE", t.getDataSelectType());
        else
            ret.put("DATASELECTTYPE", "AutoType");
        QAReportFile = ResponseType.buildQAReportFileRelativePath(t.getJobTaskID(), t.getSatelliteName().replace("-", ""), t.getTaskSerialNumber());
        File reportFile = new File(Config.dataBank_dir+"/", QAReportFile);
        MyHelper.CreateDirectory(reportFile.getParentFile());
        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }
    public QATaskRep generateQATaskRep(QATaskWorkflowInfo wi,WorkflowOrder order) throws Exception {
        wi.taskMode = order.getTaskMode();
        wi.satellite = order.getSatelliteName();
        wi.jobTaskID = order.getJobTaskID();
        wi.channel  = order.getChannelID();
        wi.sensor = order.getSensorName();
        wi.dataSelectType = order.getDataSelectType();
        if (QAReportFile==null){
            QAReportFile = ResponseType.buildQAReportFileRelativePath(order.getJobTaskID(),order.getSatelliteName().replace("-",""),order.getTaskSerialNumber());
        }
        wi.QAReportFile = QAReportFile;
        // "QAReport_"+order.getSatelliteName().replaceAll("-","")+"_"+order.getTaskSerialNumber()+"_"+DateUtil.getSdfTimes()+".xlsx";
        QATaskRep rep=new QATaskRep();
        //header
        ResponseType.QATaskRep.setHeaderInfo(order.getOrderType().split("_")[1],rep, Config.toOMO_author);
        //content
        rep.taskBasicRepInfo=new ArrayList<>();
        //重用QATaskInqAction的方法，需每次创建新实例
        rep.taskBasicRepInfo.add(new QATaskInqAction().generateTaskInfo(wi));
        //生成响应文件
        String fileName=ResponseType.QATaskRep.buildResponseFileName(order.getOrderType().split("_")[1],wi.taskId);

        generateReponseFile(rep,fileName);
        rep.replyFileName=fileName;
        logger.info("响应文件"+rep);
        return rep;
    }


    public void generateReponseFile(Response response, String fileName) throws Exception {
        if (Config.toOMO_sendingDir==null){
            Config.toOMO_sendingDir=new File("/KJ125ZL/125interface/toOMO_backup","sending");
        }
        //所注册的类必须存在@XmlRootElement标注，否则marshal时无法获知根标签名
        File tmp=new File(Config.toOMO_sendingDir, Constants.TEMP_FILE_PREFIX+fileName); //先写为临时文件名(TEMP_FILE_PREFIX为在前面加！)
        XMLUtil.convertToXml(response,tmp.toString());
       /* JAXBContext jc = JAXBContext.newInstance(QATask.class.getPackage().getName());
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        synchronized (marshaller){  //注意同步使用，看其它线程是否在用
            marshaller = jc.createMarshaller();
            marshaller.marshal(response,tmp);
        }*/
        File dest=new File(Config.toOMO_sendingDir, fileName);
        Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING); //序列化成功后恢复原名。重名替换，省得麻烦
        logger.info("generated response file: "+dest.getPath());       //debug时才会输出
    }

    public void doTriggerQ64(WorkflowOrder wi) throws Exception {
        logger.info("auto-triggering Q64 for: " + wi);
        ProcessType processType = ProcessType.KJ125_Q64;
        Map<String, Object> orderParams = generateOrderParamsForGF_Q64(wi);
        //构建流程订单
        String orderXml = processType.generateOrderXml(orderParams);
        logger.debug("generate process order: \n" + orderXml);
        //提交流程
        String orderId = processUtil.submitProcess(orderXml,Config.submit_order_timeout);
    }

    private Map<String, Object> generateOrderParamsForGF_Q64(WorkflowOrder wi) throws Exception{
        Date time=new Date();
        Map<String, Object> ret = new HashMap<>();
        String satelliteName = wi.getSatelliteName().replaceAll("-","");
        ret.put("YYYYMMDD_XXXXXX", DateUtil.getSdfDate());
        ret.put("TASKSERIALNUMBER",wi.getTaskSerialNumber());
        ret.put("SATELLITE", satelliteName);
        ret.put("SENSOR", wi.getSensorName());
        ret.put("ORBIT", wi.getOrbitNumber());

        List<String> jobs= MyHelper.string2StringList(wi.getJobTaskID(), String2ListXmlAdapter.DELIMIT);
        String job1=jobs.get(0), job2=jobs.get(1);   //差异分析必然有两个作业任务编号
        if (job1.split("/").length>1){
            job1 = job1.split("/")[job1.split("/").length-1];
        }
        if (job2.split("/").length>1){
            job2 = job2.split("/")[job2.split("/").length-1];
        }
        ret.put("JOBTASKID1", job1);
        ret.put("JOBTASKID2", job2);

        //按命名规范构建质量分析报告文件及差异文件的路径
        String date = new SimpleDateFormat("yyyyMM").format(time)+"/"+new SimpleDateFormat("yyyyMMdd").format(time);
        File report1_S1=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job1+"_"+ Channel.S1.name()+".report.xml");
        File report2_S1=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job2+"_"+Channel.S1.name()+".report.xml");
        File report1_S2=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job1+"_"+Channel.S2.name()+".report.xml");
        File report2_S2=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job2+"_"+Channel.S2.name()+".report.xml");
        File diffTxt=new File(Config.dataBank_dir,"/"+satelliteName+"/REPORT/"+date+"/"+wi.getTaskSerialNumber()+"/"+job1+"_"+job2+".diff.txt");
        ret.put("REPORT1_S1", report1_S1.isFile()?report1_S1:null);    //质量分析报告文件可能不全（单通道情况），实际不存在时填空
        ret.put("REPORT2_S1", report2_S1.isFile()?report2_S1:null);
        ret.put("REPORT1_S2", report1_S2.isFile()?report1_S2:null);
        ret.put("REPORT2_S2", report2_S2.isFile()?report2_S2:null);
        ret.put("DIFFTXT", diffTxt);       //差异文件必须存在
        //todo Q64 暂时不知道写啥 先瞎写
        File reportFile = new File(Config.dataBank_dir, "/"+satelliteName+"/REPORT/"+
                new SimpleDateFormat("yyyyMM").format(time)+"/"+
                new SimpleDateFormat("yyyyMMdd").format(time)+"/"+wi.getTaskSerialNumber()+"/"+wi.getTaskSerialNumber()+".xlsx");
        MyHelper.CreateDirectory(reportFile.getParentFile());

        ret.put("QAREPORT", reportFile.getPath());
        return ret;
    }

    public void modifyTask(List<ProcessInfo> infoList, WorkflowOrder order)throws Exception{
        String status = "";
        DataArchiveRep rep;
        oracleInfoImpl = new OracleProcessInfoImpl();
        for (int i = 0; i < infoList.size(); i++) {
            if (infoList.get(i).getStatus().equals("Completed")) {
                status = "success";
            } else if (infoList.get(i).getStatus().equals("Aborted")) {
                status = "error";
                break;
            } else {
                status = "running";
                break;
            }
        }
        WorkFlowDataArchive dataArchive = new WorkFlowDataArchive();
        dataArchive.setJobtaskid(order.getJobTaskID());
        dataArchive.setOrderid(order.getTaskSerialNumber());
        dataArchive.setCreatetime(order.getStartTime());
        dataArchive.setUpdatetime(DateUtil.getTime());
        dataArchive.setReply(0);
        if (status.equals("success")) {
            order.setOrderStatus("3");
            order.setEndTime(DateUtil.getTime());
            orderService.updateById(order);
            //todo 更新集中存储里面的表里为已归档
            oracleInfoImpl.updateRaw_data(order.getJobTaskID());
            logger.info("更新归档状态成功");
            //todo 查询gt_m_l0表，然后对集中存储L0表进行删除，重新插入操作
            List<Ml0Info> l0data = ml0InfoService.getL0Info(order.getJobTaskID(),"");
            oracleInfoImpl.delL0data(order.getJobTaskID());
            oracleInfoImpl.insertL0Data(l0data.get(0));
            //todo 生成DataArchiveRep
            //WorkFlowDataArchive dataArchive = workFlowDataArchiveService.getDataArchive(order.getTaskSerialNumber());
            rep = generateDataArchiveRep(order,dataArchive);
            logger.info("生成归档完成通知成功");
      /*      WorkFlowDataArchive archiveInfo = new WorkFlowDataArchive();
            archiveInfo.setReplyfile(rep.replyFileName);
            archiveInfo.setUpdatetime(DateUtil.getTime());
            archiveInfo.setReply(1);
            workFlowDataArchiveService.updateById(archiveInfo);*/
        } else if (status.equals("error")) {
            order.setEndTime(DateUtil.getTime());
            order.setOrderStatus("4");
            orderService.updateById(order);
            //生成DataArchiveRep
           // WorkFlowDataArchive dataArchive = workFlowDataArchiveService.getDataArchive(order.getTaskSerialNumber());

            rep = generateDataArchiveRep(order, dataArchive);
            logger.info("生成归档完成通知成功");
   /*         WorkFlowDataArchive archiveInfo = new WorkFlowDataArchive();
            archiveInfo.setReplyfile(rep.replyFileName);
            archiveInfo.setUpdatetime(DateUtil.getTime());
            archiveInfo.setReply(1);
            workFlowDataArchiveService.updateById(archiveInfo);*/
        }
    }

    public DataArchiveRep generateDataArchiveRep(WorkflowOrder order, WorkFlowDataArchive wi) throws Exception {
        logger.info("走进归档完成通知的处理流程");
        ArchiveWorkflowInfo archiveWorkflowInfo = new ArchiveWorkflowInfo();
        archiveWorkflowInfo.jobTaskID = wi.getJobtaskid();
        archiveWorkflowInfo.orderId = wi.getOrderid();
        archiveWorkflowInfo.reply = wi.getReply();
       // archiveWorkflowInfo.dataFile = wi.getDatafile();
        archiveWorkflowInfo.createTime = wi.getCreatetime();
        archiveWorkflowInfo.updateTime = wi.getUpdatetime();
        DataArchiveRep rep=new DataArchiveRep();
        //header
        ResponseType.DataArchiveRep.setHeaderInfo(order.getOrderType().split("_")[1],rep, Config.toOMO_author);
        //content
        rep.description = "自动回复";
        rep.dataStatusRepInfo = new ArrayList<>();
        //重用DataArchiveInqAction的方法，需每次创建新实例
        rep.dataStatusRepInfo.add(dataArchiveInqAction.generateJobInfo(archiveWorkflowInfo));
        //生成响应文件
        String fileName=ResponseType.DataArchiveRep.buildResponseFileName(order.getOrderType().split("_")[1],null);
        generateReponseFile(rep,fileName);
        rep.replyFileName=fileName;
        return rep;
    }

    //todo java输入输出流进行压缩文件
    public void toZip1(String srcDir, OutputStream out, boolean KeepDirStructure)
            throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            logger.info("开始进行压缩");
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure);
            long end = System.currentTimeMillis();
            logger.info("压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     *压缩算法
     */
    private void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception {
        FileInputStream in = null;
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            if (sourceFile.isFile()) {
                // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
                zos.putNextEntry(new ZipEntry(name));
                // copy文件到zip输出流中
                int len;
                in = new FileInputStream(sourceFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                // Complete the entry
                zos.closeEntry();
            } else {
                File[] listFiles = sourceFile.listFiles();
                if (listFiles == null || listFiles.length == 0) {
                    // 需要保留原来的文件结构时,需要对空文件夹进行处理
                    if (KeepDirStructure) {
                        // 空文件夹的处理
                        zos.putNextEntry(new ZipEntry(name + "/"));
                        // 没有文件，不需要文件的copy
                        zos.closeEntry();
                    }
                } else {
                    for (File file : listFiles) {
                        // 判断是否需要保留原来的文件结构
                        if (KeepDirStructure) {
                            // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                            // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                            compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
                        } else {
                            compress(file, zos, file.getName(), KeepDirStructure);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    public String execShellscript(String cmd, String hostAddr) {
        Connection conn = null;
        Session sess = null;
        String line = "";
        InputStream stdout = null;
        BufferedReader br = null;
        StringBuffer buffer = new StringBuffer("exec result:");
        buffer.append(System.getProperty("line.separtor"));				//换行
        try {
            conn = getOpenConnection(hostAddr);
            sess = conn.openSession();
            sess.execCommand(cmd);
            stdout = new StreamGobbler(sess.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            while(true) {
                line = br.readLine();
                if(line != null)
                    break;
                else if (line == null)
                    break;
            }
            sess.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stdout != null)
                    stdout.close();
                if(br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }
    /***
     * @param host 对应节点的主机地址 10.5.6.223
     */
    private Connection getOpenConnection(String host) throws IOException {
        Connection conn = new Connection(host, Config.node_port);
        conn.connect();// make sure the connection is opened. this is necessary.
        boolean isAuthenticate = conn.authenticateWithPassword(Config.node_username, Config.node_password);
        // authenticate this connection is connected.
        if(isAuthenticate == false)
            throw new IOException("Authentication failed.");
        return conn;
    }
}

