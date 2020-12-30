package com.business.business.db;

import com.business.business.enums.CatalogConfig;
import com.business.business.info.*;
import com.business.business.entity.*;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 1 *
 * 2 * @Author:w_kiven
 * 3 * @Date:2019/2/22 15:33
 * 4
 */
public class ProcessInfoImpl {
    private static final Logger logger = Logger.getLogger(ProcessInfoImpl.class);

    //todo QaTaskInqAction
    public synchronized List<R0Info>getR0Info(String jobTaskId){
        List<R0Info> ls = new ArrayList<>();
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = "select signalid,satelliteid,channelid,receivestarttime,receiveendtime,filepath from GT_M_R0 where jobtaskid = '"+jobTaskId+"'";
            conn = DBUtil.getConnection();
            R0Info r0Info = null;
            ptmt = conn.prepareStatement(sql);
            rs = ptmt.executeQuery();
            while (rs.next()){
                r0Info = new R0Info();
                r0Info.signalID = rs.getString("signalid");
                r0Info.satellite = rs.getString("satelliteid");
                r0Info.channelID = rs.getString("channelid");
                r0Info.receiveStartTime = rs.getString("receivestarttime");
                r0Info.receiveEndTime = rs.getString("receiveendtime");
                r0Info.metaFilePath = rs.getString("filepath");
                ls.add(r0Info);
            }
        }catch (Exception e){
            logger.error("no R0-meta record found for jobtaskid="+jobTaskId+"and this Exception is"+e);
        }finally {
            DBUtil.closeAll(conn,ptmt,rs);
        }
        return ls;
    }
    public synchronized List<R0Info>getR0Info(List<String>jobTaskIds)throws Exception{
        String jobTaskId1 ="";
        String jobTaskId2 = "";
        String job = "";
        List<R0Info> ls = new ArrayList<>();
        if(jobTaskIds ==null ||jobTaskIds.isEmpty())throw new Exception("no jobTaskIDs specified!");
        else{
            jobTaskId1 = jobTaskIds.get(0);
            jobTaskId2 = jobTaskIds.get(1);
            if (jobTaskId1.split("/").length>1){
                jobTaskId1 = jobTaskId1.split("/")[jobTaskId1.split("/").length-1];
                jobTaskId2 = jobTaskId2.split("/")[jobTaskId2.split("/").length-1];
            }
            job = "'"+jobTaskId1+"','"+jobTaskId2+"'";
        }
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = "select signalid,satelliteid,channelid,receivestarttime,receiveendtime,filepath from GT_R_R0 where jobtaskid in ("+job+")";
            conn = DBUtil.getConnection();
            R0Info r0Info = null;
            ptmt = conn.prepareStatement(sql);
            rs = ptmt.executeQuery();
            while (rs.next()){
                r0Info = new R0Info();
                r0Info.signalID = rs.getString("signalid");
                r0Info.satellite = rs.getString("satelliteid");
                r0Info.channelID = rs.getString("channelid");
                r0Info.receiveStartTime = rs.getString("receivestarttime");
                r0Info.receiveEndTime = rs.getString("receiveendtime");
                r0Info.metaFilePath = rs.getString("filepath");
                ls.add(r0Info);
            }
        }catch (Exception e){
            logger.error("no R0 record found for jobtaskid="+job);
        }finally {
            DBUtil.closeAll(conn,ptmt,rs);
        }
        return ls;
    }
    public synchronized List<R0Info>getR0Info(String jobTaskId,List<String>channelIds){
        String channel ="";
        List<R0Info> ls = new ArrayList<>();
        String sql = "";
        if(channelIds !=null &&!channelIds.isEmpty()){
            /*if (channelIds.size()==2){
                channel = "'"+channelIds.get(0)+"','"+channelIds.get(1)+"'";
            }else if (channelIds.size()==1){
                channel = "'"+channelIds.get(0)+"'";
            }*/
             //sql = "select * from GT_M_R0 where jobtaskid = '"+jobTaskId+"' and channelid in ("+channel+")";
            sql = "select * from gt_r_r0 where jobtaskid = '"+jobTaskId+"'";
        }else{
            sql = "select * from gt_r_r0 where jobtaskid = '"+jobTaskId+"'";
        }
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            R0Info r0Info = null;
            ptmt = conn.prepareStatement(sql);
            rs = ptmt.executeQuery();
            while (rs.next()){
                r0Info = new R0Info();
                r0Info.signalID = rs.getString("signalid");
                r0Info.satellite = rs.getString("satelliteid");
                r0Info.channelID = rs.getString("channelid");
                r0Info.receiveStartTime = rs.getString("receivestarttime");
                r0Info.receiveEndTime = rs.getString("receiveendtime");
                r0Info.metaFilePath = rs.getString("filepath");
                ls.add(r0Info);
            }
        }catch (Exception e){
            logger.error("no R0-meta record found for jobtaskid="+jobTaskId);
        }finally {
            DBUtil.closeAll(conn,ptmt,rs);
        }
        return ls;
    }
    public synchronized List<L0Info>getL0Info(String jobTaskId,String signalId)throws Exception{
        List<L0Info> ls = new ArrayList<>();
        String sql = "select * from gt_m_l0 where jobtaskid='"+jobTaskId+"' ";
        if (!"".equals(signalId)){
            sql+="and signalid = '"+signalId+"'";
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            L0Info l0Info = null;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                l0Info = new L0Info();
                l0Info.segmentID = rs.getString("segmentid");
                l0Info.sensor = rs.getString("sensorid");
                l0Info.dataStartTime = rs.getString("datastarttime");
                l0Info.dataEndTime = rs.getString("dataendtime");
                ls.add(l0Info);
            }
        }catch (Exception e){
            logger.error("no L0-meta record found for jobtaskid="+jobTaskId+", signalid="+signalId+" and this Exception is"+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return ls;
    }
    public synchronized List<L0Info>getL0Info(String jobTaskId,List<String>sensors)throws Exception{
        String sensor = "";
        List<L0Info> ls = new ArrayList<>();
        String sql = "";
        if (sensors!=null &&!sensors.isEmpty()){
            for (int i=0;i<sensors.size();i++){
                sensor+=",";
            }
            //sql = "select * from gt_m_l0 where jobtaskid='"+jobTaskId+"' and sensorid in ("+sensor+")";
            sql = "select * from gt_m_l0 where jobtaskid = '"+jobTaskId+"'";
        }else {
            sql = "select * from gt_m_l0 where jobtaskid = '"+jobTaskId+"'";
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            L0Info l0Info = null;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                l0Info = new L0Info();
                l0Info.segmentID = rs.getString("segmentid");
                l0Info.sensor = rs.getString("sensorid");
                l0Info.dataStartTime = rs.getString("datastarttime");
                l0Info.dataEndTime = rs.getString("dataendtime");
                ls.add(l0Info);
            }
        }catch (Exception e){
            logger.error("no L0-meta record found for taskid="+jobTaskId+", sensorid="+sensors+"and this Exception is "+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return ls;
    }
    public synchronized L2AInfo getL2AInfo(String jobTaskId, String sceneId){
        L2AInfo l2AInfo = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = "select t2a.productid,t1a.sceneid,t2a.notecreatetime from GT_M_L2 t2a,GT_M_L1 t1a where t2a.productid_l1a=t1a.productid  and t2a.jobtaskid='"+jobTaskId+"' and t1a.sceneid='"+sceneId+"'";
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                l2AInfo = new L2AInfo();
                l2AInfo.productID = rs.getString("productid");
                l2AInfo.sceneID = rs.getString("sceneid");
                l2AInfo.notecreatetime = rs.getString("notecreatetime");
            }
        }catch (SQLException e){
            logger.error("no L2A-meta record found for jobTaskId="+jobTaskId+", sceneid="+sceneId+"and this Exception is "+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return l2AInfo;
    }

    public synchronized QATaskWorkflowInfo getQaTaskWorkFlowInfo(String taskId){
        QATaskWorkflowInfo qa = new QATaskWorkflowInfo();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = "select q.taskid,q.orderid,q.originator,q.replyfile,q.reply,q.createtime,q.updatetime from WORKFLOW_QATASK q where q.taskid ='"+taskId+"'";
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                qa = new QATaskWorkflowInfo();
                qa.taskId = rs.getString("taskid");
                qa.orderId = rs.getString("orderid");
                qa.originator = rs.getInt("originator");
                qa.replyFile = rs.getString("replyfile");
                qa.reply = rs.getInt("reply");
                qa.createTime = rs.getString("createtime");
                qa.updateTime = rs.getString("updatetime");
            }
        }catch (SQLException e){
            logger.error("this exception is :"+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return qa;
    }

    String SQL_CAT="select sceneid,segmentid,satelliteid,sensorid,filepath from GT_M_CAT";

    public synchronized List<CatInfo> getCatInfo(String jobTaskID,String segmentID){
        List<CatInfo> ls = new ArrayList<>();
        CatInfo info = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = SQL_CAT +" where segmentid like '%"+segmentID+"%' and jobtaskid='"+jobTaskID+"'";
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                info = new CatInfo();
                info.sceneID = rs.getString("sceneid");
                info.segmentID = rs.getString("segmentid");
                info.satellite = rs.getString("satelliteid");
                info.sensor = rs.getString("sensorid");
                info.sceneMetaFilePath = rs.getString("filepath");
                ls.add(info);
            }
        }catch (SQLException e){
            logger.error("no CAT-meta record found for jobtaskid="+jobTaskID+", segmentid including "+segmentID);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return ls;
    }
    //todo triggercatLog


    //todo 获取L0,L1,L2产品
    public L0DATA getL0Data(String jobtaskId)throws Exception{
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "select * from gt_m_l0 where jobtaskid = '"+jobtaskId+"' limit 1"; //limit 1找到一条记录后就不会向下扫描了,提高效率
        L0DATA data = null;
        try {
            logger.info("打印下查询语句"+sql);
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                data = new L0DATA();
                data.setJOBTASKID(rs.getString("jobtaskid"));
                data.setSATELLITEID(rs.getString("satelliteid"));
                data.setORBIT(rs.getString("signalid").split("_")[2]);
                data.setDATASTARTTIME(rs.getString("datastarttime"));
                data.setDATAENDTIME(rs.getString("dataendtime"));
                data.setFILEPATH(rs.getString("filepath"));
            }
        }catch (Exception e){
            logger.error("查询0级表异常："+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return data;
    }
    public L1Product getL1product(String sceneId)throws SQLException{
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "select * from gt_m_l1 where sceneid = '"+sceneId+"'";
        L1Product l1 = null;
        try{
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                l1 = new L1Product();
                l1.setSCENEID(rs.getString("sceneid"));
                l1.setJOBTASKID(rs.getString("jobtaskid"));
                l1.setPRODUCTLEVEL(rs.getString("productlevel"));
                l1.setSATELLITEID(rs.getString("satelliteid"));
                l1.setSENSORID(rs.getString("sensorid"));
                l1.setSTATIONID(rs.getString("stationid"));
                l1.setSTARTTIME(rs.getString("starttime"));
                l1.setENDTIME(rs.getString("endtime"));
                l1.setSCENECENTERLAT(rs.getFloat("scenecenterlat"));
                l1.setSCENECENTERLONG(rs.getDouble("scenecenterlong"));
                l1.setUPPERLEFTLAT(rs.getDouble("upperleftlat"));
                l1.setUPPERLEFTLONG(rs.getDouble("upperleftlong"));
                l1.setUPPERRIGHTLAT(rs.getDouble("upperrightlat"));
                l1.setUPPERRIGHTLONG(rs.getDouble("upperrightlong"));
                l1.setLOWERLEFTLAT(rs.getDouble("lowerleftlat"));
                l1.setLOWERLEFTLONG(rs.getDouble("lowerleftlong"));
                l1.setLOWERRIGHTLAT(rs.getDouble("lowerrightlat"));
                l1.setLOWERRIGHTLONG(rs.getDouble("lowerrightlong"));
                l1.setCLOUDCOVER(rs.getString("cloudcover"));
            }
        }catch (Exception e){
            logger.error("获取一级产品信息异常："+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return l1;
    }
    public L1Product getL2product(String sceneId)throws SQLException{
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        String sql = "select * from gt_m_l2 where productid = '"+sceneId+"_L2A'";
        L1Product l2 = null;
        try{
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                l2 = new L1Product();
                l2.setSCENEID(rs.getString("productid").replace("_L2A",""));
                l2.setJOBTASKID(rs.getString("jobtaskid"));
                l2.setPRODUCTLEVEL(rs.getString("productlevel"));
                l2.setSATELLITEID(rs.getString("satelliteid"));
                l2.setSENSORID(rs.getString("sensorid"));
                l2.setSTATIONID(rs.getString("stationid"));
                l2.setSTARTTIME(rs.getString("starttime"));
                l2.setENDTIME(rs.getString("endtime"));
                l2.setSCENECENTERLAT(rs.getDouble("scenecenterlat"));
                l2.setSCENECENTERLONG(rs.getDouble("scenecenterlong"));
                l2.setUPPERLEFTLAT(rs.getDouble("upperleftlat"));
                l2.setUPPERLEFTLONG(rs.getDouble("upperleftlong"));
                l2.setUPPERRIGHTLAT(rs.getDouble("upperrightlat"));
                l2.setUPPERRIGHTLONG(rs.getDouble("upperrightlong"));
                l2.setLOWERLEFTLAT(rs.getDouble("lowerleftlat"));
                l2.setLOWERLEFTLONG(rs.getDouble("lowerleftlong"));
                l2.setLOWERRIGHTLAT(rs.getDouble("lowerrightlat"));
                l2.setLOWERRIGHTLONG(rs.getDouble("lowerrightlong"));
                l2.setCLOUDCOVER(rs.getString("cloudcover"));
            }
        }catch (Exception e){
            logger.error("获取二级产品信息异常："+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return l2;
    }
    public synchronized String getErrorReason(String orderId) throws Exception {
        List<workFlowTree> subs = new ArrayList<>();
        String errorReason = "工作流处理失败！";
        try {
            subs=getSubWorkflowInfoDesc(orderId);
        } catch (Exception e) {
            logger.error("failed to getSubWorkflowInfo: " + orderId, e);
        }
        if(subs.size()>0)
        {
            workFlowTree tree = subs.get(0);
            errorReason += " orderId:"+tree.getOrderid()+" info:"+tree.getInfo();
        }
        return errorReason;
    }
    public synchronized  List<workFlowTree> getSubWorkflowInfoDesc(String parentOrderId)  {
        List<workFlowTree> treeList = new ArrayList<>();
        workFlowTree tree = null;
        String sql = "select * from workflow_tree where parentorderid = '"+parentOrderId+"' order by state DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                tree = new workFlowTree();
                tree.setOrderid(rs.getString("orderid"));
                tree.setParentorderid(rs.getString("parentorderid"));
                tree.setCreatetime(rs.getString("createtime"));
                tree.setUpdatetime(rs.getString("updatetime"));
                tree.setState(rs.getInt("state"));
                tree.setInfo(rs.getString("info"));
                treeList.add(tree);
            }
        }catch (Exception e){
            logger.error("select work_tree error"+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return treeList;
    }
}
