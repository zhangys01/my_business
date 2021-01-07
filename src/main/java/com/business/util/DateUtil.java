package com.business.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 2 * @Author: kiven
 * 3 * @Date: 2018/12/20 8:40
 * @version
 */
public class DateUtil {

	private final static SimpleDateFormat sdfMonths = new SimpleDateFormat("yyyyMM");
	private final static SimpleDateFormat sdfDays = new SimpleDateFormat("yyyyMMdd");
	private final static SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static SimpleDateFormat sdfTimes = new SimpleDateFormat("yyyyMMddHHmmss");
	private final static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd_HHmmss");

	/**
	 * 获取yyyyMMdd_HHmmss格式的时间
	 */
	public static String getSdfDate() {
		return sdfDate.format(new Date());
	}

	/**
	 * 获取YYYYyyyyMMddHHmmss格式
	 *
	 * @return
	 */
	public static String getSdfTimes() {
		return sdfTimes.format(new Date());
	}

	/**
	 * 获取yyyyMM格式
	 */
	public static String getSdfMonths() {
		return sdfMonths.format(new Date());
	}


	/**
	 * 获取YYYYMMDD格式
	 *
	 * @return
	 */
	public static String getDays() {
		return sdfDays.format(new Date());
	}

	/**
	 * 获取YYYY-MM-DD HH:mm:ss格式
	 *
	 * @return
	 */
	public static String getTime() {
		return sdfTime.format(new Date());
	}



}


