package com.fiveInARow.utils;

public class ConvertUtils {
	
	public static int getInt(String value, int defaultValue){
		int result = defaultValue;
		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		
		return result;
	}
}
