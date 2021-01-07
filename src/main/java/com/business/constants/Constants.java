package com.business.constants;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-10-12
 * Time: 上午11:41
 * To change this template use File | Settings | File Templates.
 */
public class Constants {

    public static final String EXT_DAT = "dat";  //扩展名
    public static final String EXT_TIF = "tif";
    public static final String EXT_RPC = "rpb";

    //新生成、待写入的文件在其文件名前追加此前缀，一旦写完关闭后立刻去掉此前缀。避免读写双方共享访问冲突。todo 但此方法效果不佳！！！
    public static final String TEMP_FILE_PREFIX = "!";

    public static final String LEVEL_L2A = "L2A";

}
