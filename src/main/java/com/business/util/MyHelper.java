package com.business.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: dbs01
 * Date: 11-5-27
 * Time: 上午10:29
 * To change this template use File | Settings | File Templates.
 */
public class MyHelper {

    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) return true;
        return false;
    }


    public static List<String> string2StringList(String str, String delim) {
        if (isEmpty(str)) return null;

        StringTokenizer st = delim == null ? new StringTokenizer(str) : new StringTokenizer(str, delim);
        List ls = new ArrayList();
        while (st.hasMoreTokens()) {
            ls.add(st.nextToken());
        }
        return ls;
    }

   //清空目录，但不删除目录本身
    public static void emptyDir(File dir) {
        if (!dir.isDirectory()) return;
        for (File f : dir.listFiles())
            removeFile(f);
    }


    //删除给定名称的文件或目录（递归删除）
    public static boolean removeFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++)
                    removeFile(files[i].getAbsoluteFile());
            }
        }

        return file.delete();
    }

    public static void CreateDirectory(File dirpath) throws Exception {
        if (!dirpath.exists() || !dirpath.isDirectory()) {
            Files.createDirectories(dirpath.toPath());
        }
    }

    public static String ParseStringToPath(File config,String[] items,String jobTaskId,String channal){
        return config+ "/"+items[0]+ "/"+ items[3].substring(0,6) + "/" + items[3] + "/" + jobTaskId+"/"+channal;
    }

    public static synchronized String ChangeToWindowsPath(File dat) {
        String filePath = "";
        switch (dat.toString().split("/")[1]) {
            case "KJ125ZL":
                filePath = dat.toString().replace("/KJ125ZL", "Z:");
                break;
            case "KJ125ZL_2":
                filePath = dat.toString().replace("/KJ125ZL_2", "Y:");
                break;
            case "/pools/POOL/TMP":
                filePath = dat.toString().replace("/pools/POOL/TMP", "Z:");
                break;
            case "/pools/POOL/WORk":
                filePath = dat.toString().replace("/pools/POOL/WORk", "Y:");
                break;
            case "raw":
                filePath = "S:" + dat;
                break;
            case "ncsfs":
                filePath = dat.toString().replace("/ncsfs", "T:");
                break;
        }
        return filePath;
    }
}