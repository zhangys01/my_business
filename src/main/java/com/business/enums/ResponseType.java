package com.business.enums;


import com.business.message.Response;
import com.business.util.DateUtil;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-8-1
 * Time: 上午11:53
 * To change this template use File | Settings | File Templates.
 */
public enum ResponseType {   //返回给OMO的响应类型。
    QATaskCon("QATaskCon.xsd","数据质量监测任务确认"),
    QATaskRep("QATaskRep.xsd","数据质量监测任务完成通知"),
    DataArchiveRep("DataArchiveRep.xsd","数据归档完成通知"),
    //新增系统综合状态信息 by2018-10-24
    StatusRep("StatusRep.xsd","系统综合状态信息-质量监测");

    private String xsdFileName;
    private String title;

    private ResponseType(String xsdFileName, String title) {
        this.xsdFileName=xsdFileName;
        this.title=title;
    }

    public String buildResponseFileName(String sources,String taskSerialNumber){  //统一构建响应文件名
        switch (this){
            case QATaskCon:
            case QATaskRep:
                return this.name()+"_"+SystemType.QAS+"_"+sources+"_"+taskSerialNumber+"_"+ DateUtil.getSdfTimes()+".xml";
            default:
                return this.name()+"_"+SystemType.QAS+"_"+sources+"_"+DateUtil.getSdfTimes()+".xml";
        }
    }

    public static String buildQAReportFileRelativePath(String jobtaskId,String satellite, String taskSerialNumber){
        /**
         * 构建质量报告文件相对路径。
         *
         * 质量报告文件命名规范为：
         * QAReport_卫星简称_任务单流水号_生成年月日时分秒(yyyyMMddHHmmss).xls
         *
         * 质量报告文件归档路径规范为：
         * /归档根目录/卫星简称/REPORT/生成年月(yyyyMM)/生成年月日(yyyyMMdd)/质量报告文件名（去掉.xls）/质量报告文件名
         * 例如：/DiskArray/GF01/REPORT/201312/20131212/QAReport_GF01_QA2013000001_20131212235959/QAReport_GF01_QA2013000001_20131212235959.xls
         */
        String sub="QAReport_"+satellite+"_"+taskSerialNumber+"_"+DateUtil.getSdfTimes();
        String test = satellite+"/REPORT/"+
                DateUtil.getSdfMonths()+"/"+
                DateUtil.getDays()+"/"+jobtaskId+"/"+sub+".xlsx";
        return test;
    }

    public void setHeaderInfo(String source, Response response, String authorInfo){
        response.title=this.title;
        response.identificationCode= this.name();
        response.source= SystemType.QAS.name();
        response.destination=source;
        response.createdTime= DateUtil.getTime();
        response.authorInfo= authorInfo;
    }
}
