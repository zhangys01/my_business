package com.business.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-8-5
 * Time: 下午12:32
 * To change this template use File | Settings | File Templates.
 */
public class Config {
    private static Properties p = new Properties();

    public static int redisPort;
    public static int submit_order_timeout;    //second
    public static int node_port;
    public static int scene_select_head;    //从头部选第几景，即顺数第几景
    public static int scene_select_tail;       //从尾部选第几景，即倒数第几景
    public static int move_number;
    public static int running_number;

    public static String redisIp;
    public static String redisOrder;
    public static String[] datafile_prefix;
    public static String node_host;
    public static String node_username;
    public static String node_password;
    public static String toOMO_author;
    public static String dpps_dir;

    public static File archive_root;
    public static File archive_unzip;
    public static File dataBank_dir;
    public static File data_dir;
    public static File local_dir;
   // public static File fromDTC_temp;
    public static File toOMO_backup;
    public static File toOMO_sendingDir;   //内部约定为toOMO_backup下名为“sending”的子目录
    public static File unzip_cancel; //取消文件夹
    public static File unzip_in_dir;
    public static File unzip_bak_dir;
    public static File work_dir;			//武大临时工作目录，从配置文件中读取
    public static File process_template;

    //注意，必须先调用此方法加载配置！
    public static void loadConfig() throws Exception {
        InputStream in=ClassLoader.getSystemResourceAsStream("application.properties");
        if(in==null) throw new IOException("application.properties not found!");
        p = new Properties();
        try{
           p.load(in);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                //skip
            }
        }
        dpps_dir = loadStringKey("dpps_dir");

        redisIp = loadStringKey("redisIp");
        redisPort = loadIntKey("redisPort");
        redisOrder = loadStringKey("redisorder");
        submit_order_timeout = loadIntKey("submit_order_timeout");

        datafile_prefix= loadStringKey("datafile_prefix").split(";");

        archive_root=loadDirKey("archive_root");
        archive_unzip = loadDirKey("archive_unzip");
        dataBank_dir = loadDirKey("dataBank_dir");
        data_dir=loadDirKey("data_dir");
        local_dir = loadDirKey("local_dir");
       // fromDTC_temp=loadDirKey("fromDTC_temp");

        toOMO_backup=loadDirKey("toOMO_backup");
        toOMO_sendingDir=new File(toOMO_backup,"sending");
        toOMO_author = loadStringKey("toOMO_author");  //可为空

        node_host = loadStringKey("node_host");
        node_port = loadIntKey("node_port");
        node_username = loadStringKey("node_username");
        node_password = loadStringKey("node_password");

        scene_select_head = loadIntKey("scene_select_head");
        scene_select_tail = loadIntKey("scene_select_tail");

        unzip_cancel = loadDirKey("unzip_cancel");
        unzip_bak_dir=loadDirKey("unzip_bak_dir");
        unzip_in_dir=loadDirKey("unzip_in_dir");
        work_dir=loadDirKey("work_dir");
        process_template = loadDirKey("process_template");

        move_number = loadIntKey("move_number");
        running_number = loadIntKey("running_number");
    }

    private static String loadStringKey(String key) throws Exception {
        String s=p.getProperty(key);
        if(s==null||s.isEmpty()) throw new Exception(key+" config is missing!");
        return s;
    }

    private static URI loadUrlKey(String key) throws Exception {
        String s=p.getProperty(key);
        if(s==null||s.isEmpty()) throw new Exception(key+" config is missing!");
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new Exception(key+" is not valid URL: "+s,e);
        }
    }

    private static int loadIntKey(String key) throws Exception {
        String s=p.getProperty(key);
        if(s==null||s.isEmpty()) throw new Exception(key+" config is missing!");
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new Exception(key+" is not valid integer: "+s,e);
        }
    }

    private static File loadFileKey(String key) throws Exception {
        String s=p.getProperty(key);
        if(s==null||s.isEmpty()) throw new Exception(key+" config is missing!");
        File f=new File(s).getAbsoluteFile();    //转为绝对路径
        if(!f.isFile()) throw new Exception(key+" is not valid file: "+f);
        return f;
    }

    private static File loadDirKey(String key) throws Exception {
        String s=p.getProperty(key);
        if(s==null||s.isEmpty()) throw new Exception(key+" config is missing!");
        File f=new File(s).getAbsoluteFile();    //转为绝对路径
        if(!f.isDirectory()) throw new Exception(key+" is not valid dir: "+f);
        return f;
    }

    public static boolean isMonitorTarget(File jobTaskIDDir){
        //只要jobtaskID目录下存在带有所配置前缀的文件，就说明该jobtaskID目录为监控目标数据
        File[] fs=jobTaskIDDir.listFiles();
        if(fs==null) return false;  //可能是其它系统的数据，目录瞬间它人被移走，导致listFiles()返回null
        for (File f : fs) {
            for(String prefix:datafile_prefix) {
                if(f.getName().startsWith(prefix)) return true;
            }
        }
        return false;
    }

}
