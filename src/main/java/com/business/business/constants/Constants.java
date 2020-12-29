package com.business.business.constants;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-12
 * Time: 上午11:41
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final String DUMMY_ORDERID_PREFIX="DUMMY_";    //虚拟流程的orderid前缀
    public static final String PREFIX_JOBTASKID="JOB";    //作业任务编号前缀
    public static final String PREFIX_QATASK="QA";        //运管质量评价任务单流水号前缀

    public static final String EXT_DAT="dat";  //扩展名
    public static final String EXT_TIF="tif";
    public static final String EXT_XML="xml";
    public static final String EXT_RPC="rpb";

    //新生成、待写入的文件在其文件名前追加此前缀，一旦写完关闭后立刻去掉此前缀。避免读写双方共享访问冲突。todo 但此方法效果不佳！！！
    public static final String TEMP_FILE_PREFIX="!";

    public static final String LEVEL_L2A="L2A";

    public static final String TASK_SERIAL_NUMBER="QA0000000000"; //特殊的任务单流水号，用于查询出多个任务单时的反馈文件命名；或常规流程时的报表文件命名。
    public static final String SELFCHECK_SERIAL_NUMBER="000000000"; //特殊的综合状态查询序号，用于主动上报时。

    //以下字串存入Workflow_Tree表的info字段后，将被后台存储过程识别为关键子流程
    public static final String KEY_WORKFLOW_INFO_UNZIP ="UNZIP";     //解压缩子流程需填入
    public static final String KEY_WORKFLOW_INFO_REPORT ="REPORT";  //对于Q63、Q64生成报表子流程
    public static final String KEY_WORKFLOW_INFO_Q64REPORT ="REPORT";  //对于Q64生成报表子流程
    public static final String KEY_WORKFLOW_INFO_Q64R0REPORT ="Q64R0REPORT";  //对于Q64原始码流质量分析子流程需填入

}
