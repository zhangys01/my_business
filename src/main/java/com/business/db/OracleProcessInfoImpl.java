package com.business.db;

import com.business.entity.NomalProduct;
import com.business.entity.Ml0Info;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @author w_kiven
 * @title: OracleProcessInfoImpl
 * @projectName taskManager125
 * @description: TODO
 * @date 2019/11/2515:46
 */
@Component
public class OracleProcessInfoImpl {
    private static final Logger logger = Logger.getLogger(OracleProcessInfoImpl.class);

    public void updateRaw_data(String jobTaskId)throws Exception{
        String sql = "update RAW_DATA set ISARCHIVED = 'Y' where JOBTASKID = '"+jobTaskId+"'";
        logger.info("打印下更新归档:"+sql);
        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            conn = OracleDBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.execute(sql);
            logger.info("更新集中存储表归档表成功"+jobTaskId);
        }catch (Exception e){
            logger.info("更新集中存储表归档表失败"+e);
        }finally {
            OracleDBUtil.closeAll(conn,ptmt,null);
        }
    }

    public void delL0data(String JobTaskId)throws SQLException{
        String sql = "delete from L0DATA where JOBTASKID = '"+JobTaskId+"'";
        PreparedStatement ptmt = null;
        Connection conn = null;
        try {
            logger.info("打印下删除L0:"+sql);
            conn = OracleDBUtil.getConnection();
            ptmt = conn.prepareStatement(sql);
            ptmt.execute(sql);
            logger.info("执行删除集中存储成功");
        }catch (Exception e){
            logger.info("删除0级表失败："+e);
        }finally {
            OracleDBUtil.closeAll(conn,ptmt,null);
        }
    }
    public void insertL0Data(Ml0Info l0DATA)throws Exception{
        String sql = "insert into L0DATA (JOBTASKID,SATELLITEID,ORBIT,DATASTARTTIME,DATAENDTIME,FILEPATH,STORAGE_STATUS)values(?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            logger.info("打印下插入0级表SQL："+sql);
            conn = OracleDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1,l0DATA.getJobtaskid());
            ps.setString(2,l0DATA.getSatelliteid());
            ps.setString(3,l0DATA.getSignalid().split("_")[2]);
            ps.setString(4,l0DATA.getDataStarttime());
            ps.setString(5,l0DATA.getDataEndtime());
            //todo 阉割路径，达到想要的效果
            File file = new File(l0DATA.getFilepath());
            String path = file.getParent();
            String str[] = path.split("/");
            String filePath = "";
            for (int i=0;i<str.length-2;i++){
                if (str[i]!=null&&!str[i].equals("")){
                    filePath = filePath+"/"+str[i];
                }
            }
            ps.setString(6,filePath);
            ps.setString(7,"onDisk");
            ps.execute();
            logger.info("插入集中存储0级数据成功"+l0DATA.getJobtaskid());
        }catch (Exception e){
            logger.error("插入集中存储0级表失败"+e);
        }finally {
            OracleDBUtil.closeAll(conn,ps,null);
        }
    }

    public void delL1Product(String sceneID)throws Exception{
        String sql = "delete from L1PRODUCT where SCENEID = '"+sceneID+"'";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = OracleDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.execute(sql);
        }catch (Exception e){
            logger.error("删除集中存储一级产品表失败"+e);
        }finally {
            OracleDBUtil.closeAll(conn,ps,null);
        }
    }
    public void insertL1product(NomalProduct l1)throws Exception{
        String sql = "insert into L1PRODUCT (SCENEID,PRODUCTLEVEL,SATELLITEID,SENSORID,STATIONID,STARTTIME,ENDTIME,SCENECENTERLAT,SCENECENTERLONG," +
                "UPPERLEFTLAT,UPPERLEFTLONG,UPPERRIGHTLAT,UPPERRIGHTLONG,LOWERLEFTLAT,LOWERLEFTLONG,LOWERRIGHTLAT,LOWERRIGHTLONG,CLOUDCOVER,JOBTASKID,STORAGE_STATUS)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            logger.info("打印一级产品SQL："+sql);
            conn = OracleDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1,l1.getSCENEID());
            ps.setString(2,l1.getPRODUCTLEVEL());
            ps.setString(3,l1.getSATELLITEID());
            ps.setString(4,l1.getSENSORID());
            ps.setString(5,l1.getSTATIONID());
            ps.setString(6,l1.getSTARTTIME());
            ps.setString(7,l1.getENDTIME());
            ps.setDouble(8,l1.getSCENECENTERLAT());
            ps.setDouble(9,l1.getSCENECENTERLONG());
            ps.setDouble(10,l1.getUPPERLEFTLAT());
            ps.setDouble(11,l1.getUPPERLEFTLONG());
            ps.setDouble(12,l1.getUPPERRIGHTLAT());
            ps.setDouble(13,l1.getUPPERRIGHTLONG());
            ps.setDouble(14,l1.getLOWERLEFTLAT());
            ps.setDouble(15,l1.getLOWERLEFTLONG());
            ps.setDouble(16,l1.getLOWERRIGHTLAT());
            ps.setDouble(17,l1.getLOWERRIGHTLONG());
            ps.setString(18,l1.getCLOUDCOVER());
            ps.setString(19,l1.getJOBTASKID());
            ps.setString(20,"onDisk");
            ps.execute();
            logger.info("集中存储一级产品插入成功");
        }catch (Exception e){
            logger.error("插入集中存储L1产品表失败"+e);
        }finally {
            OracleDBUtil.closeAll(conn,ps,null);
        }
    }
    public void delL2Product(String sceneID)throws Exception{
        String sql = "delete from L2PRODUCT where SCENEID = '"+sceneID+"'";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = OracleDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.execute(sql);
        }catch (Exception e){
            logger.error("删除集中存储一级产品表失败"+e);
        }finally {
            OracleDBUtil.closeAll(conn,ps,null);
        }
    }
    public void insertL2product(NomalProduct l2)throws Exception{
        String sql = "insert into L2PRODUCT (SCENEID,PRODUCTLEVEL,SATELLITEID,SENSORID,STATIONID,STARTTIME,ENDTIME,SCENECENTERLAT,SCENECENTERLONG," +
                "UPPERLEFTLAT,UPPERLEFTLONG,UPPERRIGHTLAT,UPPERRIGHTLONG,LOWERLEFTLAT,LOWERLEFTLONG,LOWERRIGHTLAT,LOWERRIGHTLONG,CLOUDCOVER,JOBTASKID,STORAGE_STATUS)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            logger.info("打印二级产品SQL："+sql);
            conn = OracleDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1,l2.getSCENEID());
            ps.setString(2,l2.getPRODUCTLEVEL());
            ps.setString(3,l2.getSATELLITEID());
            ps.setString(4,l2.getSENSORID());
            ps.setString(5,l2.getSTATIONID());
            ps.setString(6,l2.getSTARTTIME());
            ps.setString(7,l2.getENDTIME());
            ps.setDouble(8,l2.getSCENECENTERLAT());
            ps.setDouble(9,l2.getSCENECENTERLONG());
            ps.setDouble(10,l2.getUPPERLEFTLAT());
            ps.setDouble(11,l2.getUPPERLEFTLONG());
            ps.setDouble(12,l2.getUPPERRIGHTLAT());
            ps.setDouble(13,l2.getUPPERRIGHTLONG());
            ps.setDouble(14,l2.getLOWERLEFTLAT());
            ps.setDouble(15,l2.getLOWERLEFTLONG());
            ps.setDouble(16,l2.getLOWERRIGHTLAT());
            ps.setDouble(17,l2.getLOWERRIGHTLONG());
            ps.setString(18,l2.getCLOUDCOVER());
            ps.setString(19,l2.getJOBTASKID());
            ps.setString(20,"onDisk");
            ps.execute();
            logger.info("集中存储二级产品插入成功");
        }catch (Exception e){
            logger.error("插入集中存储L2产品表失败"+e);
        }finally {
            OracleDBUtil.closeAll(conn,ps,null);
        }
    }

}

