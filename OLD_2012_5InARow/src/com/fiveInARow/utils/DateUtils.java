package com.fiveInARow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	private static final String LOGGER_TIME_FORMAT = "dd/MM/yyyy mm:ss:SSSS"; //21 chars
	
	private static SimpleDateFormat m_LoggerTimeFormatter = new SimpleDateFormat(LOGGER_TIME_FORMAT);
	public static String getFormattedNow(){
		return m_LoggerTimeFormatter.format(new Date());
	}
	
	public static String getFormattedDate (long unixTimeStamp){
		Date date = new Date(unixTimeStamp);
		return m_LoggerTimeFormatter.format(date);
	}
	
}
