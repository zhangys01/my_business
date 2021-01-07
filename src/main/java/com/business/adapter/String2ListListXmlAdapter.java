package com.business.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-8-19
 * Time: 下午7:26
 * To change this template use File | Settings | File Templates.
 */
public class String2ListListXmlAdapter extends XmlAdapter<String, List<List<String>>> {
    //两层分隔符连接的字符串与字符串列表的列表间的转换，如：2mCCD/8mCCD;SatGPS/SatEnv
    public static final String DELIMIT_1 = ";";  //第一层分隔符
    public static final String DELIMIT_2 = "/";  //第二层分隔符

    public static List<List<String>> toListList(String str) {
        List ls1 = new ArrayList();
        if (str == null || str.trim().length() == 0) return ls1;
        StringTokenizer st1 = new StringTokenizer(str.trim(), DELIMIT_1);
        while (st1.hasMoreTokens()) {
            List ls2 = new ArrayList();
            StringTokenizer st2 = new StringTokenizer(st1.nextToken(), DELIMIT_2);
            while (st2.hasMoreTokens()) {
                ls2.add(st2.nextToken());
            }
            ls1.add(ls2);
        }
        return ls1;
    }

    public static List<String> toList(String str) {
        List<List<String>> lsls = toListList(str);
        List<String> ret = new ArrayList<>();
        for (List<String> ls : lsls)
            for (String s : ls)
                ret.add(s);
        return ret;
    }


    public static String toString(List<List<String>> list) {
        if (list == null) return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            List<String> sub = list.get(i);
            for (int k = 0; k < sub.size(); k++) {
                sb.append(sub.get(k));
                if (k != sub.size() - 1)
                    sb.append(DELIMIT_2);
            }
            if (i != list.size() - 1)
                sb.append(DELIMIT_1);
        }
        return sb.toString();
    }

    @Override
    public List<List<String>> unmarshal(String str) throws Exception {  //never return null
        return toListList(str);
    }

    @Override
    public String marshal(List<List<String>> list) throws Exception {
        return toString(list);
    }
}
