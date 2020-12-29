package com.business.business.util;

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

    public static String formatTimestamp(Timestamp timestamp){
        //输出时间格式为：yyyy-MM-dd HH:mm:ss.SSSSSS  例如：2012-01-02 01:30:49.490541
        final DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //仅表示到秒级，不含秒的小数部分
        final DecimalFormat nf=new DecimalFormat("000000000"); //nanos值最大9位;Timestamp.toString()输出秒的小数部分最多9位，即最高精确到0.000000001秒
        String t1=df.format(timestamp);  //小数点之前的部分
        String t2=nf.format(timestamp.getNanos()).substring(0,6); //小数点之后的部分
        return t1+"."+t2;
    }

    public static Timestamp formatTimestamp(String timeStr){
        //输入时间格式为：yyyy-[m]m-[d]d hh:mm:ss[.f...]
        return Timestamp.valueOf(timeStr);
    }

    //获取bean实例的属性值。该beanObject中必须存在可读的propertyName属性。
    public static Object getBeanPropertyValue(Object beanObject, String propertyName) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(beanObject.getClass(), Object.class);
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            if (props == null) throw new Exception("bean include none property!");
            for (PropertyDescriptor p : props) {
                if (p.getName().equals(propertyName)) return p.getReadMethod().invoke(beanObject);
            }
            throw new Exception("property not found!");
        } catch (Exception e) {
            throw new RuntimeException("failed to get bean property value! bean:" + beanObject.getClass().getName() + " property:" + propertyName, e);
        }
    }

    //获取bean实例的属性值。该beanObject中必须存在可读的propertyName属性。（xiaoyun add）
    public static Object getBeanPropertyValue2(Object beanObject, String propertyName) {
        try {
            Class pe = beanObject.getClass();
            String getXbyName = "get"
                    + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);
            Method getX = pe.getDeclaredMethod(getXbyName);
            Object object = getX.invoke(beanObject);
            return object;
        } catch (Exception e) {
            throw new RuntimeException("property not found!");
        }
    }

    /**
     * 返回true，表示没有相同进程运行，并加锁；
     * 返回false，表示有相同进程运行；
     * 抛出异常，表示检查过程出错。
     */
    public static boolean checkSingleton() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + ".ipslock");
        //System.out.println(file.getPath());
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        FileLock lock = fc.tryLock(0, 1, false);
        if (lock == null) {
            //System.out.println( "已有一个实例在运行 ");
            return false;
        }
        return true;
    }

    public static void writeFile(File file, String content, String encode) throws FileNotFoundException, UnsupportedEncodingException {
        PrintStream out = new PrintStream(new FileOutputStream(file), false, encode);
        out.print(content);
        out.flush();
        out.close();
    }

    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) return true;
        return false;
    }

    public static String getExceptionMessage(Throwable e) {
        //有些异常的getMessage()为空，此时应返回异常类名。
        if (e == null) return "";
        return isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
    }

    public static List<Integer> string2IntList(String str, String delim) {
        if (isEmpty(str)) return null;

        StringTokenizer st = new StringTokenizer(str, delim);
        List ls = new ArrayList();
        while (st.hasMoreTokens()) {
            try {
                ls.add(Integer.parseInt(st.nextToken()));
            } catch (Exception e) {
                //skip
            }
        }
        return ls;
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

    public static int[] integerList2IntArray(List<Integer> intList) {
        if (intList == null) return null;
        int[] intArray = new int[intList.size()];
        for (int i = 0; i < intList.size(); i++) {
            intArray[i] = intList.get(i).intValue();
        }
        return intArray;
    }

    public static Calendar fromDate(Date t) {
        if (t == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        return cal;
    }

    public static String arrayToString(long[] a, String delim) {
        if (a == null) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            sb.append(a[i]);
            if (i != a.length - 1)
                sb.append(delim);
        }
        return sb.toString();
    }

    public static String arrayToString(String[] a, String delim) {
        if (a == null) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            sb.append(a[i]);
            if (i != a.length - 1)
                sb.append(delim);
        }
        return sb.toString();
    }

    public static String listDateToString(List<Date> l, String delim) {
        //时间格式化为：yyyy-MM-dd HH:mm:ss
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (l == null) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l.size(); i++) {
            sb.append(df.format(l.get(i)));
            if (i != l.size() - 1)
                sb.append(delim);
        }
        return sb.toString();
    }

    public static String listToString(List l, String delim) {
        if (l == null) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l.size(); i++) {
            sb.append(l.get(i));
            if (i != l.size() - 1)
                sb.append(delim);
        }
        return sb.toString();
    }

    public static long[] stringTolongArray(String s, String delim) {
        if (isEmpty(s)) return null;

        StringTokenizer st = new StringTokenizer(s, delim);
        List<Long> ls = new ArrayList<Long>();
        while (st.hasMoreTokens()) {
            ls.add(Long.parseLong(st.nextToken()));
        }

        long[] ret = new long[ls.size()];
        for (int i = 0; i < ls.size(); i++) {
            ret[i] = ls.get(i);
        }
        return ret;
    }

    public static String[] stringToStringArray(String s, String delim) {
        if (isEmpty(s)) return null;

        StringTokenizer st = new StringTokenizer(s, delim);
        List<String> ls = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            ls.add(st.nextToken());
        }

        String[] ret = new String[ls.size()];
        for (int i = 0; i < ls.size(); i++) {
            ret[i] = ls.get(i);
        }
        return ret;
    }

    //将params xml文本中的根标签的所有属性转变为Properties对象，标签名不做限制。
    /*public static Properties createParamsProperties(String params) {
        Properties p = new Properties();
        try {
            List<Attribute> as = new SAXBuilder().build(new StringReader(params)).getRootElement().getAttributes();
            for (Attribute a : as) {
                p.setProperty(a.getName(), a.getValue());
            }
            return p;
        } catch (Throwable e) {
            return p;  //解析失败，返回空的属性对象，而不是null。
        }
    }*/

    public static long computeAmountOfFile(File file) {
        //计算指定绝对路径的容量(字节)。如果是文件，为文件大小；如果是目录，为所有文件和递归子目录的大小。
        if (!file.exists()) { //不存在
            return 0;
        } else if (file.isFile()) { //是文件
            return file.length();
        } else {  //是目录
            long amount = 0;
            for (File f : file.listFiles()) {
                amount = amount + computeAmountOfFile(f);   //递归
            }
            return amount;
        }
    }

    public static long computeCountOfFile(File file) {
        //计算指定绝对路径（及递归子目录）下的文件数。
        if (!file.exists()) { //不存在
            return 0;
        } else if (file.isFile()) { //是文件
            return 1;
        } else {  //是目录
            long count = 0;
            for (File f : file.listFiles()) {
                count += computeCountOfFile(f);   //递归
            }
            return count;
        }
    }

    public static long[] computeAmountAndFileCount(File file) {
        //计算指定绝对路径的容量(字节)和所包含的文件数。返回数组：[0]为容量；[1]为文件数
        //如果路径是文件，则容量为该文件大小；文件数为1
        //如果路径是目录，则容量为目录下所有文件（递归）的总大小；文件数为总文件个数
        if (!file.exists()) { //不存在
            return new long[]{0, 0};
        } else if (file.isFile()) { //是文件
            return new long[]{file.length(), 1};
        } else {  //是目录
            long amount = 0;
            long count = 0;
            for (File f : file.listFiles()) {
                long[] ret = computeAmountAndFileCount(f);   //递归
                amount += ret[0];
                count += ret[1];
            }
            return new long[]{amount, count};
        }
    }

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public static String getLocalHostIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    //整理目录名，去掉最后的若干个分隔符（如果有）；如果是根目录则保留。例如：/home/ 转变为 /home
    public static String trimDirSuffix(String dir) {
        dir = dir.trim();
        if (File.separator.equals(dir)) return File.separator;
        if (dir.endsWith(File.separator)) return trimDirSuffix(dir.substring(0, dir.length() - 1));  //递归
        return dir;
    }

    //整理目录名，将所有连续分隔符合并为一个分隔符。例如：/home//data 转变为 /home/data
    public static String trimDir(String dir) {
        if (dir == null) return null;
        dir = dir.trim();
        return trimDirSuffix(dir.replaceAll("\\" + File.separator + "+", "\\" + File.separator));
    }

    //清空目录，但不删除目录本身
    public static void emptyDir(String dir) {
        if (isEmpty(dir)) return;
        emptyDir(new File(dir));
    }

    //清空目录，但不删除目录本身
    public static void emptyDir(File dir) {
        if (!dir.isDirectory()) return;
        for (File f : dir.listFiles())
            removeFile(f);
    }

    //删除给定名称的文件或目录（递归删除）
    public static boolean removeFile(String filePath) {
        if (isEmpty(filePath))
            return false;
        return removeFile(new File(filePath));
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

    //将源文件或源目录拷贝到目标目录下。当拷贝目录时，将拷贝目录本身及所有子目录。
    public static void copyFileFully(File srcFileOrDir, File destDir) throws IOException {
        if (srcFileOrDir.isFile()) {  //直接拷贝文件
            FileChannel srcFileChannel = new FileInputStream(srcFileOrDir).getChannel();
            FileChannel destFileChannel = new FileOutputStream(new File(destDir, srcFileOrDir.getName())).getChannel(); //目标文件名相同
            srcFileChannel.transferTo(0, srcFileChannel.size(), destFileChannel);
            destFileChannel.close();
            srcFileChannel.close();
        } else {  //递归拷贝目录下的所有文件
            File subDestDir = new File(destDir, srcFileOrDir.getName());
            subDestDir.mkdirs();  //先创建目标子目录
            for (File f : srcFileOrDir.listFiles()) {
                copyFileFully(f, subDestDir);
            }
        }
    }

    //将jdom的标签要素输出为字符串
    /*public static String elementToString(Element jdomElement) {
        if (jdomElement == null) return null;
        //保持原始风格
        Format format = Format.getRawFormat();
        format.setEncoding("UTF-8");
        format.setExpandEmptyElements(true);
        format.setOmitDeclaration(true);
        format.setOmitEncoding(true);
        format.setTextMode(Format.TextMode.PRESERVE);
        XMLOutputter out = new XMLOutputter(format);
        return out.outputString(jdomElement);
    }*/

    //查找源目录下所有符合指定扩展名的文件。如果未指定扩展名，则查找所有文件。
    public static List<Path> searchFiles(final Path srcDir, final List<String> fileExtensions) throws Exception {
        if (srcDir == null || !Files.isDirectory(srcDir)) throw new Exception("invalid srcDir! " + srcDir);

        final List<Path> ret = new ArrayList<>();
        Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (fileExtensions == null || fileExtensions.isEmpty()) {
                    ret.add(file);
                    return FileVisitResult.CONTINUE;
                }
                String name = file.toFile().getName();
                for (String ext : fileExtensions) {
                    if (name.endsWith("." + ext)) {   //文件名以".扩展名"结尾
                        ret.add(file);
                        break;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return ret;
    }

    //将源目录下的所有内容、保持其原有的相对目录结构，移至目的目录下（同名文件将被覆盖）。源目录自身不移动。
    public static void moveDirContent(final Path srcDir, final Path destDir) throws Exception {
        if (srcDir == null || !Files.isDirectory(srcDir)) throw new Exception("invalid srcDir! " + srcDir);
        Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(srcDir)) { //源目录本身跳过
                    Path newDir = destDir.resolve(srcDir.relativize(dir));
                    Files.createDirectories(newDir);  //在目的目录下创建对应的子目录
                }
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.move(file, destDir.resolve(srcDir.relativize(file)), StandardCopyOption.ATOMIC_MOVE);
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                if (!dir.equals(srcDir)) {  //源目录本身不删除
                    Files.delete(dir); //删除原来的子目录（应该已经为空）
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    //将源文件移至目的目录下（同名文件将被覆盖）
    public static void moveFile(Path srcFile, Path destDir) throws Exception {
        if (srcFile == null || !Files.isRegularFile(srcFile)) throw new Exception("invalid srcFile! " + srcFile);
        Files.move(srcFile, destDir.resolve(srcFile.getFileName()), StandardCopyOption.ATOMIC_MOVE);
    }

    //清除目标目录下、指定时间之前的各级子目录（目标目录自身不删除）
    public static void cleanDir(final Path targetDir, Date expireTime) throws Exception {
        if (targetDir == null || !Files.isDirectory(targetDir)) throw new Exception("invalid targetDir! " + targetDir);
        if (expireTime == null) throw new Exception("invalid expireTime! " + expireTime);
        final long expire = expireTime.getTime();
        Files.walkFileTree(targetDir, new SimpleFileVisitor<Path>() {
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                if (!dir.equals(targetDir)) {  //目标目录本身不删除
                    if (Files.getLastModifiedTime(dir).toMillis() < expire) {
                        MyHelper.removeFile(dir.toString());  //递归删除该子目录。注意，不要在preVisitDirectory中删除该目录，那时目录处于open状态，无法被删除
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    //清除目标目录及各级子目录下、指定时间之前的文件、以及空目录（目标目录自身不删除）
    public static void cleanDir2(final Path targetDir, Date expireTime) throws Exception {
        if (targetDir == null || !Files.isDirectory(targetDir)) throw new Exception("invalid targetDir! " + targetDir);
        if (expireTime == null) throw new Exception("invalid expireTime! " + expireTime);
        final long expire = expireTime.getTime();
        Files.walkFileTree(targetDir, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.lastModifiedTime().toMillis() < expire) {
                    Files.deleteIfExists(file);
                }
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                if (!dir.equals(targetDir)) {  //目标目录本身不删除
                    dir.toFile().delete();  //尝试删除目录。如果目录为空，则被删除
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    //判断指定文件是否被占用
    static final String FUSER = "/sbin/fuser";
    public static boolean isFileUsed(File f){
        if(File.separatorChar=='/'){  //Unix-like system
            //todo 通过fuser命令判断指定文件是否被某个进程使用。fuser命令退出码非0，则表示指定文件没有被任何进程使用。
            //若命令启动异常，则返回false，认为文件没有被使用。
            try {
                Process p =Runtime.getRuntime().exec(FUSER+" "+f.getPath());
                return p.waitFor()==0;
            } catch (Throwable e) {
                System.out.println("failed to run: "+FUSER+" "+f.getPath()+". "+e.toString());
                return false;
            }
        }else{ //windows system ...
            return !f.renameTo(f);
        }
    }

    //todo（重要）将一条完整的命令行，拆分成[可执行文件,参数1,参数2,...]形式的列表。
    //拆分规则按照shell环境下解析命令行的规则，即：双引号内部作为一个整体，其它按空格拆分。
    //jave的Runtime.exe()执行命令行时，其内部也是先用StringTokenize对命令行进行拆分，但拆分时并不考虑双引号情况。
    //为了能够执行各种复杂的命令行，需自行按通用规则进行拆分，然后可用ProcessBuilder启动命令。
    public static List<String> cmdLine2cmdList(String cmdLine){
        final Pattern p = Pattern.compile("((\"[^\"]*\")|([^ ]+))");  //双引号内部允许为空白
        Matcher m=p.matcher(cmdLine);
        List<String> ret=new ArrayList<>();
        while(m.find()){
            String s=m.group();
            if(s.startsWith("\"")&&s.endsWith("\"")) s=s.substring(1,s.length()-1); //引号包围的参数需去掉两头的引号
            ret.add(s);
        }
        return ret;
    }

    //for test
    public static void main(String[] args) throws Exception{
        String cmdLine="  \" /home/ java\" \"\" -a 192.168.6.16 oracle \"java -Djava.security.egd=file:///dev/urandom -cp config:dist/tran.jar:\" ";
        List<String> array=cmdLine2cmdList(cmdLine);
        System.out.println(cmdLine);
        System.out.println(array);


        /*while(true){
            System.out.println(isFileUsed(new File("d:\\VirtualBox-4.0.2-69518-Win.exe")));
        }*/

        /*File f=new File("e:\\香山红叶_CoolMiniPE_V32.iso");
        while(!f.isFile()){
            System.out.println("not exist.");
        }
        System.out.println(f.length());
        while(!f.renameTo(f)){
            System.out.println(f.length());
        }
        System.out.println("ok.");
        System.out.println(f.length());*/

        //long[] a=computeAmountAndFileCount(new File("e:\\test\\test2"));
        //System.out.println(a[0]);
        //System.out.println(a[1]);

        //File f1=new File("E:\\新华社\\web\\file\\323434\\12356496.cgp");
        //File f2=new File("E:\\moveDir\\20120203123056\\file\\323434\\12356496.cgp");
        //Files.move(f1.toPath(), f2.toPath(), StandardCopyOption.ATOMIC_MOVE);

        //Path srcDir=new File("E:\\新华社").toPath();
        //Path destDir=new File("E:\\moveDir\\20120203123056").toPath();
        //DateFormat DATE_FORMAT=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //cleanDir(destDir,DATE_FORMAT.parse("2012-02-09 17:20:00"));
        //cleanDir2(srcDir,DATE_FORMAT.parse("2012-02-20 17:20:00"));

        //Path srcFile=new File("E:\\新华社\\web\\该到了整理的时候.jpg").toPath();
        //System.out.println(Files.size(srcFile));
        //System.out.println(searchFiles(srcDir,Arrays.asList(new String[]{"cgp","CGP","WMV"})));

        //moveFile(srcFile, destDir);

        //moveDirContent(srcDir,destDir);

        //Path relativize(Path other)


        /*if(!srcDir.isDirectory()) return;
        if(!targetDir.isDirectory()){
            targetDir.mkdirs();
        }
        for(File f:srcDir.listFiles()){
            Files.move(f.toPath(), targetDir.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
        }*/


        /*if(!srcDir.isDirectory()) return;
        if(!targetDir.isDirectory()){
           targetDir.mkdirs(); 
        }
        for(File f:srcDir.listFiles()){
            boolean result=f.renameTo(new File(targetDir,f.getName()));
            System.out.println(result);
        }*/

        //System.out.println(string2StringList(" df  sd    sdd   "," "));

        /*File f=new File("app");
        System.out.println("f.getAbsolutePath() = " + f.getAbsolutePath());
        File f2=f.getAbsoluteFile();
        System.out.println(f2);
        File f3=new File(f2,"MailApp/mailbox");
        System.out.println(f3);*/


        //boolean  result=new File("D:\\dbs_receiver_test\\test.txt").renameTo(new File("D:\\dbs_receiver_test\\abc\\test.txt"));
        //System.out.println(result);

        //System.out.println(MyHelper.checkSingleton());
        //Thread.sleep(999999999999L);

        /*long a = 54L;
        long b = 345L;
        System.out.println(100 * a / b);
        System.out.println((int) (100 * a / b));*//*

        *//*long[] ret=computeAmountAndFileCount(new File("E:\\work\\单向传输"));
        System.out.println(ret[0]);
        System.out.println(ret[1]);

        ret=computeAmountAndFileCount(new File("E:\\work\\单向传输\\单向传输系统方案v1.0_zhengyk.doc"));
        System.out.println(ret[0]);
        System.out.println(ret[1]);*//*

        *//* Long a=null;
             Long b=0L;
             if (a!=b)
             System.out.println(true);*//*

        //System.out.println(new File("c:").exists());
        *//*File signal = new File("D:\\dbs_sender_test\\inbox\\ID1001.in");
        File invalidBackup = new File("D:\\dbs_sender_test\\invalidBackup");
        File dest = new File(invalidBackup, signal.getName() + "." + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        System.out.println(dest);
        System.out.println(signal.renameTo(dest));*//*

        *//*SendRequest req = new SendRequest();
        req.setNumsendrequestid(1001L);
        Object id = (Long) getBeanPropertyValue(req, "numsendrequestid");
        System.out.println(id);
        System.out.println(getBeanPropertyValue(req, "vcdesc"));
        System.out.println(getBeanPropertyValue(req, "abc"));*//*

        *//*SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(MyHelper.class.getClassLoader().getResource("sender_config.xml"));
        Element root = doc.getRootElement();
        Element e=root.getChild("appPlugin");
        System.out.println(e.toString());

        System.out.println(elementToString(e));*//*

        *//*Format format=Format.getRawFormat();
        format.setEncoding("UTF-8");
        format.setExpandEmptyElements(true);
        format.setOmitDeclaration(true);
        format.setOmitEncoding(true);
        format.setTextMode(Format.TextMode.PRESERVE);
        XMLOutputter out=new XMLOutputter(format);
        String txt=out.outputString(e);
        System.out.println(txt);*/

    }

}
