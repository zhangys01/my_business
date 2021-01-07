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
public class String2ListXmlAdapter extends XmlAdapter<String,List<String>> {
    //分隔符连接的字符串与字符串列表间的转换，如：2mCCD;8mCCD
    public static final String DELIMIT=";";  //分隔符

    @Override
    public List<String> unmarshal(String str) throws Exception {  //never return null
        List ls = new ArrayList();
        if (str == null || str.trim().length() == 0) return ls;
        StringTokenizer st = new StringTokenizer(str.trim(), DELIMIT);
        while (st.hasMoreTokens()) {
            ls.add(st.nextToken());
        }
        return ls;
    }

    @Override
    public String marshal(List<String> list) throws Exception {
        return toString(list);
    }

    public static String toString(List<String> list){
        if (list == null) return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1)
                sb.append(DELIMIT);
        }
        return sb.toString();
    }
}
