package com.business.util;

import com.business.config.Config;
import com.business.entity.WorkflowOrder;
import com.business.enums.Channel;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-17
 * Time: 上午9:31
 * To change this template use File | Settings | File Templates.
 */
public class R0Meta {
    private static final Logger logger = Logger.getLogger(R0Meta.class);
    private static LogUtil logUtil;
    public static File generateR0Meta(WorkflowOrder order, String jobTaskId, File signalFile) throws Exception {
        logger.info("generating R0-meta file for : " + signalFile);
        String signalId = signalFile.getName().replace(".dat", "");
        String[] items = signalFile.getName().split("_");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        sb.append("<S0Meta>\n");
        sb.append("  <TaskID>" + jobTaskId + "</TaskID>\n");
        sb.append("  <SignalID>" + signalId + "</SignalID>\n");
        sb.append("  <SatelliteID>" + items[0] + "</SatelliteID>\n");
        sb.append("  <ChannelID>" + Channel.fromId(items[1]).name()  + "</ChannelID>\n");
        sb.append("  <OrbitNumber>" + Long.parseLong(items[2]) + "</OrbitNumber>\n");
        sb.append("  <StationID>" + items[4].substring(0, 2) + "</StationID>\n");
        sb.append("  <AntennaID>" + items[4].substring(2, 3) + "</AntennaID>\n");
        sb.append("  <RecorderID>" + items[4].substring(3, 4) + "</RecorderID>\n");
        sb.append("  <RecorderChannelID>" + items[4].substring(4, 5) + "</RecorderChannelID>\n");
        sb.append("  <FileSize>" +signalFile.length() + "</FileSize>\n");

        //解析原始码流文件内容以提取详细的接收起始、结束时间
        Date[] ds=extractReceiveTime(order,signalFile);
        sb.append("  <ReceiveStartTime>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ds[0].getTime() + 8 * 60 * 60 * 1000) + "</ReceiveStartTime>\n");
        sb.append("  <ReceiveEndTime>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ds[1].getTime() + 8 * 60 * 60 * 1000) + "</ReceiveEndTime>\n");
        sb.append("</S0Meta>");
        File metaDir = new File(Config.dataBank_dir+"/"+items[0]+"/SIGNAL/"+items[3].substring(0,6)+"/"+items[3]);
        MyHelper.CreateDirectory(metaDir.getParentFile());

        //原始码流元数据文件的绝对路径即为：将原始码流数据文件的后缀.dat替换为.meta.xml
        File meta = new File(metaDir,signalFile.getName().replace(".dat", ".meta.xml"));
        Files.write(meta.toPath(), sb.toString().getBytes("UTF-8")); //注意编码
        logger.info("generated R0-meta file : " + meta);
        return meta;
    }

    public static Date[] extractReceiveTime(WorkflowOrder order,File signalFile) throws Exception {
        //从原始码流文件头中解析出接收起始(ret[0])、结束(ret[1])时间
        //参看：《原始码流记录格式》文档
        /*
        offset      说明                    类型     取值范围
        142～149 	记录日期	           年	String	4位数的公元纪年，例如：2011
                                                       月	String	01～12
                                                      日	String	01～31
        150～158	    计划记录起始时间	   时	String	00～23
                                                                  分	String	00～59
                                                                  秒	String	00～59
                                                                毫秒	String	取000～999
        159～167   	计划记录终止时间	   时	String	00～23
                                                                  分	String	00～59
                                                                 秒	String	00～59
                                                              毫秒	String	000～999
         */
        Date [] ret = null;
        logUtil = new LogUtil();
        try {
            DateFormat df=new SimpleDateFormat("yyyyMMddHHmmssSSS");
             ret=new Date[2];
            byte[] b=new byte[26];   //一次性读出所有时间串
            FileInputStream in=new FileInputStream(signalFile);
            in.skip(142);
            in.read(b);
            String str=new String(b);  //无编码问题
            ret[0]=df.parse(str.substring(0,17));
            ret[1]=df.parse(str.substring(0,8)+str.substring(17,26));
            in.close();
        }catch (Exception e){
            logUtil.print(order.getLogPath(),"从原始数据解析时间错误："+e);
            logger.info("从原始数据解析时间错误："+e);
        }
        return  ret;
    }
    public static String checkMode( File file) throws IOException {
        String VCModel = "";
        InputStream is = new FileInputStream(file);
        is.skip(4194304);
        int bytesCounter =0;
        int value = 0;
        StringBuilder sbHex = new StringBuilder();
        StringBuilder sbText = new StringBuilder();
        StringBuilder sbResult = new StringBuilder();
        //todo 查询是否是一样
        StringBuilder sbHex1 = new StringBuilder();
        StringBuilder sbText1 = new StringBuilder();
        StringBuilder sbResult1 = new StringBuilder();

        while ((value = is.read()) != -1) {
            sbHex.append(String.format("%02X ", value));
            if (!Character.isISOControl(value)) {
                sbText.append((char)value);
            }else {
                sbText.append(".");
            }

            //if 16 bytes are read, reset the counter,
            //clear the StringBuilder for formatting purpose only.
            if(bytesCounter==15){
                sbResult.append(sbHex).append("      ").append(sbText).append("\n");
                sbHex.setLength(0);
                sbText.setLength(0);
                bytesCounter=0;
            }else{
                bytesCounter++;
            }
            String syncCode = sbHex.toString();
            //todo 找到头之后跳过1020字节，如果还是同步码，那就不是VCM
            if(syncCode.equals("1A CF FC 1D ")){
                is.skip(4195324);
                int i =0 ;
                while ((value = is.read()) != -1) {
                    i++;
                    sbHex1.append(String.format("%02X ", value));
                    if (!Character.isISOControl(value)) {
                        sbText1.append((char)value);
                    }else {
                        sbText1.append(".");
                    }
                    if(bytesCounter==15){
                        sbResult1.append(sbHex1).append("      ").append(sbText1).append("\n");
                        sbHex1.setLength(0);
                        sbText1.setLength(0);
                        bytesCounter=0;
                    }else{
                        bytesCounter++;
                    }
                    String syncCode1 = sbHex1.toString();
                    if (i==4){
                        if (syncCode1.equals("1A CF FC 1D ")){
                            VCModel = "0";
                            break;
                        }else {
                            VCModel = "1";
                            break;
                        }
                    }
                }
            }
            if (!"".equals(VCModel)){
                break;
            }

        }
        return VCModel;
    }
}
