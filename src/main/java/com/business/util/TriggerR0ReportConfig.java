package com.business.util;

import org.apache.log4j.Logger;

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
public class TriggerR0ReportConfig {
    private static final Logger logger= Logger.getLogger(TriggerR0Report.class);
    private static Properties p = new Properties();

    public static int submit_order_timeout;    //second

    public static File archive_root;
    //diff config
    public static String diff_pinfile_05;

    //注意，必须先调用此方法加载配置！
    public static void loadConfig() throws Exception {
        InputStream in=ClassLoader.getSystemResourceAsStream("BusinessControl.properties");
        if(in==null) throw new IOException("BusinessControl.properties not found!");
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
        submit_order_timeout = loadIntKey("submit_order_timeout");
        archive_root=loadDirKey("archive_root");
        diff_pinfile_05 = loadStringKey("diff_pinfile_05");
        logger.info(_toString());
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

    public static String _toString() {
        return "TriggerR0ReportConfig{" +
                ", submit_order_timeout=" + submit_order_timeout +
                ", archive_root=" + archive_root +
                ", diff_pinfile_05=" + diff_pinfile_05 +
                '}';
    }
}
