package com.business.enums;

import com.business.config.Config;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-28
 * Time: 下午1:10
 * To change this template use File | Settings | File Templates.
 */
public enum ProcessType {   //预先定义的流程名
    //高分三期
    GF6_R0_TO_L0,
    GF7_R0_TO_L0,
    GF6_R0_TO_R0REPORT,
    GF7_R0_TO_R0REPORT,
    GF6_CAT_TO_REPAIR,
    GF6_CAT_TO_L1A,
    GF7_CAT_TO_L1A,
    GF6_CAT_TO_L2A,
    GF7_CAT_TO_L2A,
    GF6_Q63_CAT_TO_L2A,
    GF7_Q63_CAT_TO_L2A,
    //125
    GF1_R0_TO_L0,
    ZY3_R0_TO_L0,
    ZY1E_R0_TO_L0,
    CB4A_R0_TO_L0,

    KJ125_R0_TO_R0REPORT,
    ZY3B_CAT_TO_L1A,
    ZY3B_CAT_TO_L2A,
    ZY3B_Q63_CAT_TO_L2A,
    GF1_CAT_TO_L1A,
    GF1_CAT_TO_L2A,
    GF1_Q63_CAT_TO_L2A,
    ZY1E_CAT_TO_L1A,
    ZY1E_CAT_TO_L2A,
    ZY1E_Q63_CAT_TO_L2A,
    CB4A_CAT_TO_L1A,
    CB4A_CAT_TO_L2A,
    CB4A_Q63_CAT_TO_L2A,

    KJ125_Q61_62_63_QAReport,
    KJ125_Q64,
    ZY3_Q64_DIFF,
    GF1_Q64_DIFF,
    KJ125_Q64_R0REPORT,
    KJ125_Q65,
    //
    CAS_R0_TO_L0,
    CAS_CAT_TO_L1A,
    CAS_CAT_TO_L2A,
    CAS_Q63_CAT_TO_L2A;
    //对应的流程订单模板文件名为：<流程名>.xml
    private String orderTemplateFileName = name() + ".xml";

    /**
     * 根据流程订单参数，生成订单xml。
     * 订单模板中需要替换的部分用%key%表示，key为易识别的参数名，其命名只能使用字母、数学和下划线，且命名不能为空。
     * 订单生成者根据需要设置模板中key对应的value值，存入Map中（注意，key不带%）。
     * 此方法将遍历Map的所有key，用value值替换模板中对应的%key%部分，如果value为null，则当作空串。
     * 如果模板中的某些key并未包含在Map中，则对应的%key%用空串替换。
     *
     * @param params 参数为null，说明未设置任何key，即模板中的所有%key%都填空串
     * @return 返回填入参数值后的订单模板文本
     */
    public String generateOrderXml(Map<String, Object> params) throws Exception {
        //每次都加载模板文件，这样可支持实时更新模板文件而无需重启。
       // URL url = ClassLoader.getSystemResource(orderTemplateFileName);
//        URL url =ProcessType.class.getClassLoader().getSystemResource(orderTemplateFileName);
//        if (url == null) throw new IOException("order template file not found: " + orderTemplateFileName);
        //todo 订单模板文件必须规定为UTF-8编码
        //String order = new String(Files.readAllBytes(new File(url.getPath()).toPath()), "UTF-8");

        File file = new File(Config.process_template+"/"+orderTemplateFileName);
        if (!file.exists()) throw new IOException("order template file not found: " + orderTemplateFileName);
        String order = new String(Files.readAllBytes(new File(file.getPath()).toPath()), "UTF-8");
        //先查找所有%key%模式，提取所有key
        final Pattern p = Pattern.compile("%(\\w+)%");
        Matcher m = p.matcher(order);
        Map<String, Object> all = new HashMap<>();
        while (m.find()) {
            all.put(m.group(1), "");     //所有key的值初始都设为空串。注意，group(0)得到的是匹配模式的整个字串，包括%；group(1)得到的则是第一个括号（组）中的字串
        }
        //再将用户设置的参数覆盖进来
        if (params != null) all.putAll(params);

        //最后全文替换。注意，如果某个key的值当中恰好也包含"%key%"的文本，则替换结果可能难以预料！！！
        for (Map.Entry<String, Object> entry : all.entrySet()) {
            order = order.replace("%" + entry.getKey() + "%", Objects.toString(entry.getValue(), ""));    //值为null时填入空串
        }
        return order;
    }
}