package com.business.util;


import com.business.entity.Result;
import com.business.entity.TriggerR0ReportOrder;
import com.business.enums.Channel;
import com.business.enums.ProcessType;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-17
 * Time: 上午9:31
 * To change this template use File | Settings | File Templates.
 */
@Component
public class TriggerR0Report{
    private static Logger logger;
    private static File orderFile;
    private static TriggerR0ReportOrder order;
    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;
    private static int progress=0;        //注意，初始为0。必须同步访问，否则可能出乱！
    private static Date time = new Date();   //统一用一个时间
    private static List<String> subOrderXmls = new ArrayList<>();     //注意，这几个列表顺序必须保持一致
    private static List<String> subOrderIds = new ArrayList<>();
    private static List<String> subInfos = new ArrayList<>();
    @Resource
    private static ProcessUtil processUtil;


    public static void main(String[] args) throws Exception{
        PropertyConfigurator.configure(ClassLoader.getSystemResourceAsStream("log4j_triggerR0report.properties"));
        logger = Logger.getLogger(TriggerR0Report.class);

        if (args.length == 0) {
            logger.error("failed to start this program! no order file input!");
            System.exit(100);
        }

        orderFile = new File(args[0]).getAbsoluteFile();   //转为绝对路径
        logger.info("input order file : " + orderFile.getPath());
        if (!orderFile.isFile()) {
            logger.error("order file not found : " + orderFile.getPath());
            System.exit(101);
        }

        try {
            //注意，以包的形式构建，需在包目录下的jaxb.index文件中加入所有的映射类名
            JAXBContext jc = JAXBContext.newInstance(TriggerR0ReportOrder.class.getPackage().getName());
            unmarshaller = jc.createUnmarshaller();
            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //如果unmarshal时不明确指定映射类型，则从JAXBContext所注册的若干类中、取@XmlRootElement标注名与xml根标签名相匹配的类
            //如果存在多个匹配的类，取最后一个注册的类，这存在隐患，可能并不是你想要的那个类，那么unmarshal时必须指定映射类型
            //如果所注册的类都没有@XmlRootElement标注，则unmarshal时也必须指定映射类型
            order = unmarshaller.unmarshal(new StreamSource(orderFile), TriggerR0ReportOrder.class).getValue();
        } catch (Throwable e) {
            logger.error("failed to parse order!", e);
            System.exit(102);
        }

        //订单解析成功之后，再出现异常就需生成result文件
        logger.info("input order : " + order);
/*        udpLogger = new UdpLogger();    //订单解析成功即可上报udp
        udpLogger.setTaskId(order.id);
        udpLogger.setTaskName(order.name);
        udpLogger.setServer(MyHelper.isEmpty(order.server_address)?"172.19.4.219":order.server_address,    //未设置则用默认值
                order.port_number==null||order.port_number<=0?18886:order.port_number);
        udpLogger.submitSysMessage(UdpLogger.SEV_INFORMATION, addProgress(1), "已收到订单");*/

        try {
            TriggerR0ReportConfig.loadConfig();
        } catch (Throwable e) {
            logger.error("failed to load config!", e);
         //   udpLogger.submitSysMessage(UdpLogger.SEV_ERROR, addProgress(1), "加载配置失败！"+e.toString());
            failResult(103, "failed to load config! " + e.toString());
        }

        //触发每个原始码流的质量分析流程
        try {
            if (order.job1_S1 != null) {
                subOrderXmls.add(ProcessType.KJ125_Q64_R0REPORT.generateOrderXml(
                        generateOrderParamsForGF_Q64_R0REPORT(order.jobTaskId1, Channel.S1, new File(order.job1_S1))));
            }
            Thread.sleep(1000);
            if (order.job2_S1 != null) {
                subOrderXmls.add(ProcessType.KJ125_Q64_R0REPORT.generateOrderXml(
                        generateOrderParamsForGF_Q64_R0REPORT(order.jobTaskId2, Channel.S1, new File(order.job2_S1))));
            }
            Thread.sleep(1000);
            if (order.job1_S2 != null) {
                subOrderXmls.add(ProcessType.KJ125_Q64_R0REPORT.generateOrderXml(
                        generateOrderParamsForGF_Q64_R0REPORT(order.jobTaskId1, Channel.S2, new File(order.job1_S2))));
            }
            Thread.sleep(1000);
            if (order.job2_S2 != null) {
                subOrderXmls.add(ProcessType.KJ125_Q64_R0REPORT.generateOrderXml(
                        generateOrderParamsForGF_Q64_R0REPORT(order.jobTaskId2, Channel.S2, new File(order.job2_S2))));
            }

            for (int i = 0; i < subOrderXmls.size(); i++) {
                logger.info("打印下su"+subOrderXmls.size());
                String orderId = processUtil.submitProcess(subOrderXmls.get(i), TriggerR0ReportConfig.submit_order_timeout);
                subOrderIds.add(orderId);
            }
        } catch (Throwable e) {    //已提交的工作流仍然会处理，只是生成垃圾数据
            logger.error("failed to trigger R0Report process!", e);
            //udpLogger.submitSysMessage(UdpLogger.SEV_ERROR, addProgress(1), "触发原始码流质量分析流程失败！" + e.toString());
            failResult(106, "failed to trigger R0Report process! " + e.toString());
        }

        //最后，记录子流程信息。如果记录失败，前面提交的工作流仍然会处理，只是生成垃圾数据
        try {
           // treeManager.saveTreeSubWorkFlow(order.taskId,DateUtil.getTime(),subOrderIds,subInfos);
            //db.saveSubWorkflow(order.orderid, subOrderIds, subInfos);
        } catch (Throwable e) {
            logger.error("failed to save sub-workflow info!", e);
            //udpLogger.submitSysMessage(UdpLogger.SEV_ERROR, addProgress(1), "保存子流程信息失败！"+e.toString());
            failResult(107, "failed to save sub-workflow info! " + e.toString());
        }
        successResult();
    }

    private static Map<String, Object> generateOrderParamsForGF_Q64_R0REPORT(String jobTaskId,
                                                                             Channel channel, File signalFile) throws Exception {

        if (jobTaskId.split("/").length>1){
            jobTaskId = jobTaskId.split("/")[jobTaskId.split("/").length-1];
        }
        Map<String, Object> map = new HashMap<>();
        String orderIdSuffix = DateUtil.getSdfDate();
        map.put("YYYYMMDD_XXXXXX", orderIdSuffix);
        String satellite = order.satellite;
        map.put("SATELLITE", satellite.replaceAll("-",""));
        map.put("CHANNEL", channel.name());
        map.put("SEGMENTSIZE", order.segmentSize);
        map.put("TASKSERIALNUMBER", order.taskId);
        map.put("SIGNALID", signalFile.getParentFile().getName());   //原始码流文件的父目录名即为signalID
        map.put("SIGNALFILE", signalFile);
        map.put("DIFFTXT", order.diffTxt);
        map.put("PINFILE", "diff.txt");
        map.put("REPORT", new File(new File(order.diffTxt).getParent(), jobTaskId + "_"+channel.name()+".report.xml"));
        return map;
    }

    private static void failResult(int exitCode, String error) {
        Result result = new Result();
        result.id = order.id;
        result.name = order.name;
        result.orderid = order.orderid;
        result.result = Result.FAIL;
        result.message = error;
        File f = new File(orderFile.getParent(), order.id + ".result.xml");
        try {
            marshaller.marshal(result, f);
        } catch (Throwable e) {
            logger.fatal("failed to create fail-result file : " + f.getPath(), e);
            System.exit(254);
        }
        logger.info("fail-result file created :" + f.getPath());
        System.exit(exitCode);
    }

    private static void successResult() {
        Result result = new Result();
        result.id = order.id;
        result.name = order.name;
        result.orderid = order.orderid;
        result.result = Result.SUCCESS;
        File f = new File(orderFile.getParent(), order.id + ".result.xml");
        try {
            marshaller.marshal(result, f);
        } catch (Throwable e) {
            logger.fatal("failed to create success-result file : " + f, e);
            //udpLogger.submitSysMessage(UdpLogger.SEV_ERROR, 99, "无法生成订单成功回复文件！"+e.toString());
            System.exit(255);
        }
        logger.info("success-result file created :" + f.getPath());
        System.exit(0);
    }
}
