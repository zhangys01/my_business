package com.business.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-28
 * Time: 下午1:10
 * To change this template use File | Settings | File Templates.
 */
public enum UnzipParaTemplate {   //解压缩相关参数文件模板
    TASK_BASE_FILE("125_UNZIP.xml"),
    Task_UNZIP("dan_125_UNZIP.xml");
    private String templateFileName;

    private UnzipParaTemplate(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    /**
     * 根据参数，生成参数文件文本。
     * 文件模板中需要替换的部分用%key%表示，key为易识别的参数名，其命名只能使用字母、数学和下划线，且命名不能为空。
     * 生成者根据需要设置模板中key对应的value值，存入Map中（注意，key不带%）。
     * 此方法将遍历Map的所有key，用value值替换模板中对应的%key%部分，如果value为null，则当作空串。
     * 如果模板中的某些key并未包含在Map中，则对应的%key%用空串替换。
     *
     * @param params 参数为null，说明未设置任何key，即模板中的所有%key%都填空串
     * @return  返回填入参数值后的模板文本
     */
    public String generateParaString(Map<String,Object> params) throws Exception {
        //每次都加载模板文件，这样可支持实时更新模板文件而无需重启。
        URL url=ClassLoader.getSystemResource(templateFileName);
        if(url==null) throw new IOException("template file not found: "+templateFileName);
        //todo 模板文件必须规定为UTF-8编码
        String str=new String(Files.readAllBytes(new File(url.getPath()).toPath()),"UTF-8");

        //先查找所有%key%模式，提取所有key
        final Pattern p = Pattern.compile("%(\\w+)%");
        Matcher m=p.matcher(str);
        Map<String,Object> all=new HashMap<>();
        while(m.find()){
            all.put(m.group(1),"");     //所有key的值初始都设为空串。注意，group(0)得到的是匹配模式的整个字串，包括%；group(1)得到的则是第一个括号（组）中的字串
        }
        //再将用户设置的参数覆盖进来
        if(params!=null) all.putAll(params);

        //最后全文替换。注意，如果某个key的值当中恰好也包含"%key%"的文本，则替换结果可能难以预料！！！
        for(Map.Entry<String,Object> entry:all.entrySet()){
            str=str.replace("%"+entry.getKey()+"%", Objects.toString(entry.getValue(), ""));    //值为null时填入空串
        }
        return str;
    }


  
}
