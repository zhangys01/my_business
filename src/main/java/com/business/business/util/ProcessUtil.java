package com.business.business.util;

import com.business.business.Service.ProcessInfoService;
import com.business.business.entity.ProcessInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import redis.clients.jedis.Jedis;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

/**
 *
 * 与访问jbpm工作流引擎相关的帮助类。
 *
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-28
 * Time: 下午12:59
 * To change this template use File | Settings | File Templates.
 */
@Component
public class ProcessUtil {
    private static final Logger logger = Logger.getLogger(ProcessUtil.class);
    //private static IProcessDriver service;
    @Autowired
    private ProcessInfoService processInfoService;
//todo 修改webservice方式为redis方式
    public  void submitProcess(String orderXml,int waitTimeout) throws Exception{
        //向工作流引擎提交流程订单，waitTimeout为提交订单后等待记录创建的超时时间(秒)。记录创建成功则返回orderId，否则抛异常。
        logger.debug("submitting process-order: " + orderXml);
        String orderId=validateOrder(orderXml);
        Jedis redis = new Jedis("10.5.6.225",8715);
        redis.lpush("dpps:queue:order",orderXml);
        //todo 即使订单不规范、或其它后台错误，ws服务端都不会抛异常，所以并不能认为流程一定创建成功。
        //可通过查询数据库来确认！有一定延迟，需等待片刻！
        long waitTotal = 0;  //秒
        //TODO 先休眠五秒，再查询数据库
        Thread.sleep(5000);
        do {

            ProcessInfo info = processInfoService.getById(orderId);
            if (info!=null) {
               logger.info("生成流程"+orderId);
            }
            Thread.sleep(5000);
            waitTotal+=5;
        } while (waitTotal < waitTimeout);
        throw new Exception("jbpm order record not found: " + orderId);
    }


    //验证所生成的订单xml的合法性，避免提交语法错误的订单，如果合法返回订单ID
    protected static String validateOrder(String orderXml) throws Exception{
        final DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
        Document doc=fact.newDocumentBuilder().parse(new InputSource(new StringReader(orderXml)));
        return doc.getDocumentElement().getAttribute("orderid");
    }


}
