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

    public List<ProcessInfo> jbpmOrderExist(String orderId)throws Exception{
        List<ProcessInfo> infoList = new ArrayList<>();
        ProcessInfo info = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            int i=0;
            do {
                conn = DBUtil.getConnection();
                String sql = "select * from pd_processinfo where orderId = '"+orderId+"'";
                ptmt = conn.prepareStatement(sql);
                rs = ptmt.executeQuery();
                while (rs.next()){
                    info = new ProcessInfo();
                    info.setProcessId(rs.getString("processId"));
                    info.setOrderId(rs.getString("orderId"));
                    info.setProcessType(rs.getString("processType"));
                    info.setProcessName(rs.getString("processName"));
                    info.setStatus(rs.getString("status"));
                    info.setCreator(rs.getString("creator"));
                    info.setPlatform(rs.getString("platform"));
                    info.setSensor(rs.getString("sensor"));
                    info.setPriority(rs.getInt("priority"));
                    info.setCreateTime(rs.getString("createTime"));
                    info.setEndTime(rs.getString("endTime"));
                    infoList.add(info);
                }
                i++;
            }while (rs==null&&i<10);

        }catch (Exception e){
            logger.error("未生成流程,"+e);
        }finally {
            DBUtil.closeAll(conn,ptmt,rs);
        }
        return infoList;

    }

    public void deleteTaskIdByCustomDetail(String tableName,String taskId)throws Exception{
        String sql = "delete " + tableName + "  aa" +
                " where aa.id in (select rl2.id" +
                "                   from " + tableName + "  RL2," +
                "                        (select distinct substr(CAT.SCENEID, 0, 33) SC" +
                "                           from GT_M_CAT CAT" +
                "                          WHERE CAT.jobtaskid = '" + taskId + "') C" +
                "                  where RL2.PRODUCTID LIKE SC || '%')";

        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            logger.info("打印下sql"+sql);
            conn = DBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.execute(sql);
        }catch (Exception e){
            logger.error("delete table "+tableName+" error"+e);
        }finally {
           DBUtil.closeAll(conn,ptmt,null);
        }
    }
    public void deleteSignalAuto(String tableName,String taskId)throws Exception{
        String sql = "delete from "+tableName+" where jobtaskid = '"+taskId+"'";
        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.execute(sql);   //执行sql语句
        }catch (Exception e){
            logger.error("delete table:"+tableName+":error,Because:"+e);
        }finally {
            DBUtil.closeAll(conn,ptmt,null);
        }
    }
    public void deleteQAtask(String tableName,String taskId)throws Exception{
        String sql = "delete from "+tableName+" where taskid = '"+taskId+"'";
        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.execute(sql);
        }catch (Exception e){
            logger.error("delete table:"+tableName+":error,Because:"+e);
        }finally {
            DBUtil.closeAll(conn,ptmt,null);
        }
    }

    public List<String >selectGtMr0 (String signalId)throws Exception{
       List<String>ls = new ArrayList<>();
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        Connection conn = null;
       try {
           conn = DBUtil.getConnection();
           String sql = "select t.jobtaskid from GT_M_R0 t where t.signalid = '" + signalId + "'";
           ptmt = conn.prepareStatement(sql);
           rs = ptmt.executeQuery();
           while (rs.next()){
               String test = null;
               test = rs.getString("jobtaskid");
               ls.add(test);
           }
       }catch (Exception e){
            logger.error("select gt_M_R0 error"+e);
       }finally {
           DBUtil.closeAll(conn,ptmt,rs);
       }
       return ls;
    }

    public void checkSignalIDDuplicate(String signalId)throws Exception{
        PreparedStatement ptmt = null;
        PreparedStatement ptmt2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql1 = "select count(signalid) aa from GT_M_R0 where signalid='"+signalId+"'";
            String sql2 = "select count(signalid) bb from GT_R_R0 where signalid='"+signalId+"'";
            ptmt = conn.prepareStatement(sql1);
            ptmt2 = conn.prepareCall(sql2);
            rs = ptmt.executeQuery();       //执行查询，返回查询结果
            rs2 = ptmt2.executeQuery();
            int count = 0;
            int count2 = 0;
            while (rs.next()){
                count = rs.getInt("aa");
            }
            while (rs2.next()){
                count2 = rs2.getInt("bb");
            }
            if (count>0)throw new Exception("duplicate signalid in GT_M_R0: "+signalId);
            if (count2>0)throw new Exception("duplicate signalid in GT_R_R0: "+signalId);
        }catch (Exception e){
            logger.error("duplicate signalid"+signalId);
        }finally {
            DBUtil.closeAll(conn,ptmt,rs);
            DBUtil.closeAll(conn,ptmt2,rs2);
        }
    }
    public synchronized void deleteProductIdByL1A(String tableName,String productId)throws Exception{
        String sql = "delete from " + tableName + " where productid = '"+productId+"'";
        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.execute(sql);      //执行sql语句，这是删除命令

        }catch (Exception e){
            logger.error("delete table"+tableName+"error"+e);
        }finally {
            DBUtil.closeAll(conn,ptmt,null);
        }
    }
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
    public synchronized List<R0Info>getR0Info(String jobTaskId,String orderId){
        List<R0Info> ls = new ArrayList<>();
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = "select signalid,satelliteid,channelid,receivestarttime,receiveendtime,filepath from GT_M_R0 where jobtaskid = '"+jobTaskId+"' and orderid= '"+orderId+"'";
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


    String SQL_QATASK="select q.taskid,q.orderid,q.originator,q.replyfile,q.reply,t.state,q.createtime,t.updatetime," +
            "extractvalue(q.taskinfo,'/*/satellite/text()') satellite"+
            "extractvalue(q.taskinfo,'/*/taskMode/text()') taskMode," +
            "extractvalue(q.taskinfo,'/*/jobTaskID/text()') jobTaskID," +
            "extractvalue(q.taskinfo,'/*/channel/text()') channel," +
            "extractvalue(q.taskinfo,'/*/sensor/text()') sensor," +
            "extractvalue(q.taskinfo,'/*/dataSelectType/text()') dataSelectType," +
            "extractvalue(q.taskinfo,'/*/sceneCountQ63/text()') sceneCountQ63," +
            "extractvalue(q.taskinfo,'/*/orbit/text()') orbit," +
            "extractvalue(q.taskinfo,'/*/QAReportFile/text()') QAReportFile " +
            "from WORKFLOW_QATASK q, workflow_tree t where q.orderid = t.orderid";

    public synchronized List<QATaskWorkflowInfo> getOMOQATaskWorkflowInfo(List<String> taskId){
        List<QATaskWorkflowInfo>ls = new ArrayList<>();
        QATaskWorkflowInfo qa = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            StringBuffer sql=new StringBuffer(SQL_QATASK+" and q.originator=1");
            if(taskId!=null && !taskId.isEmpty()){
                sql.append(" and q.taskid in (");
                for(int i=0;i<taskId.size();i++){
                    sql.append("'"+taskId.get(i)+"'");
                    if(i!=taskId.size()-1) sql.append(",");
                }
                sql.append(")");
            }
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()){
                qa = new QATaskWorkflowInfo();
                qa.taskId = rs.getString("taskid");
                qa.orderId = rs.getString("orderid");
                qa.originator = rs.getInt("originator");
                qa.replyFile = rs.getString("replyfile");
                qa.reply = rs.getInt("reply");
                qa.createTime = rs.getString("createtime");
                qa.satellite = rs.getString("satellite");
                qa.taskMode = rs.getString("taskMode");
                qa.jobTaskID = rs.getString("jobTaskID");
                qa.channel = rs.getString("channel");
                qa.sensor = rs.getString("sensor");
                qa.dataSelectType = rs.getString("dataSelectType");
                qa.sceneCountQ63 = rs.getInt("sceneCountQ63");
                qa.orbit = rs.getInt("orbit");
                qa.QAReportFile = rs.getString("QAReportFile");
                ls.add(qa);
            }
        }catch (SQLException e){
            logger.error("no QATask-workflow record found for taskid="+taskId+"and this Exception is "+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return  ls;
    }

    public synchronized List<QATaskWorkflowInfo> getOMOQATaskWorkflowInfo(String startTime,String endTime){
        List<QATaskWorkflowInfo> ls = new ArrayList<>();
        QATaskWorkflowInfo qa = null;
       // Timestamp start=new Timestamp(startTime.getTime()),end=new Timestamp(endTime.getTime());
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String sql = SQL_QATASK+"and q.originator=1 and (t.createtime between "+startTime+" and "+endTime+")";
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
                qa.satellite = rs.getString("satellite");
                qa.taskMode = rs.getString("taskMode");
                qa.jobTaskID = rs.getString("jobTaskID");
                qa.channel = rs.getString("channel");
                qa.sensor = rs.getString("sensor");
                qa.dataSelectType = rs.getString("dataSelectType");
                qa.sceneCountQ63 = rs.getInt("sceneCountQ63");
                qa.orbit = rs.getInt("orbit");
                qa.QAReportFile = rs.getString("QAReportFile");
                ls.add(qa);
            }
        }catch (SQLException e){
            logger.error("no QATask-workflow record found for createtime from "+startTime+" to "+endTime);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return ls;
    }
    //todo
    /******************DataArchiveInqAction*********************/
    String SQL_ARCHIVE="select d.jobtaskid,d.orderid,d.reply,d.datafile,t.state,d.createtime,t.updatetime " +
            "from WORKFLOW_DATAARCHIVE d, workflow_tree t where d.orderid=t.orderid";
    public synchronized List<ArchiveWorkflowInfo>getArchiveWorkflowInfo(List<String>jobTaskID)throws Exception{
        List<ArchiveWorkflowInfo> ls = new ArrayList<>();
        ArchiveWorkflowInfo info = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try{
            //查询指定jobTaskID列表的归档流程信息
            StringBuffer sql=new StringBuffer(SQL_ARCHIVE);
            if(jobTaskID!=null && !jobTaskID.isEmpty()){
                sql.append(" and d.jobtaskid in (");
                for(int i=0;i<jobTaskID.size();i++){
                    sql.append("'"+jobTaskID.get(i)+"'");
                    if(i!=jobTaskID.size()-1) sql.append(",");
                }
                sql.append(")");
            }
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while(rs.next()){
                info = new ArchiveWorkflowInfo();
                info.jobTaskID = rs.getString("jobtaskid");
                info.orderId = rs.getString("orderid");
                info.reply = rs.getInt("reply");
                info.dataFile = rs.getString("datafile");
                info.state = rs.getInt("state");
                info.createTime = rs.getString("createtime");
                info.updateTime = rs.getString("updatetime");
                ls.add(info);
            }
        }catch (SQLException e){
            logger.error("no archive-workflow record found for jobtaskid="+jobTaskID);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return ls;
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
    public CatalogConfig selectBySatellite(String satellite){
        CatalogConfig catalogConfig = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "select * from catalog_config where satellite = '"+satellite+"'";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                catalogConfig = new CatalogConfig();
                catalogConfig.setID(rs.getInt("ID"));
                catalogConfig.setSatellite(rs.getString("satellite"));
                catalogConfig.setPA(rs.getInt("PA"));
                catalogConfig.setMS(rs.getInt("MS"));
                catalogConfig.setFWD(rs.getInt("FWD"));
                catalogConfig.setNAD(rs.getInt("NAD"));
                catalogConfig.setBWD(rs.getInt("BWD"));
                catalogConfig.setMUX(rs.getInt("MUX"));
            }
        }catch (SQLException e){
            logger.error("no data for： "+satellite+"and error is"+e);
        }finally {
            DBUtil.closeAll(conn,ps,rs);
        }
        return catalogConfig;
    }


    public void insertUnzipConfirm(UnzipConfirm unzipConfirm){
        String sql = "insert into unzip_confirm (taskId,activitityId,cancelActivityId,status) values (?,?,?,?)";
        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.setString(1,unzipConfirm.getTaskId());
            ptmt.setString(2,unzipConfirm.getActivityId());
            ptmt.setString(3,unzipConfirm.getCancelActivityId());
            ptmt.setInt(4,unzipConfirm.getStatus());
            ptmt.execute();
        }catch (Exception e){
            logger.info("insert tableName unzip_confirm error"+e);
        }finally {
            DBUtil.closeAll(conn,ptmt,null);
        }
    }
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
