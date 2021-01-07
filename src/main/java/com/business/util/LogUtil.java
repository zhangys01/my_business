package com.business.util;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * 1、人工写日志
 * 2 * @Author: kiven
 * 3 * @Date: 2018/12/26 10:08
 * 4
 */
public class LogUtil {
    private static Logger logger = Logger.getLogger(LogUtil.class);
    public  boolean print(String filePath, String code) {
        try {
            File tofile = new File(filePath);
            FileWriter fw = new FileWriter(tofile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(DateUtil.getTime()+":"+code);
            pw.close();
            bw.close();
            fw.close();
            return true;
        } catch (IOException e) {
            logger.info("打印下E"+e);
            return false;
        }
    }
}

