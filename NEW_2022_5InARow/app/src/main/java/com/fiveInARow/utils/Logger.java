package com.fiveInARow.utils;

public class Logger {
	private static final String DEBUG_FORMAT_V1 = "## [%1$s] %2$s - %3$s",
			                    ERROR_FORMAT_V1 = "## [%1$s] %2$s - %3$s - EXCEPTION TYPE: %4$s, MESSAGE: \"%5$s\"";
	
	//for thread mode ON
	private static final String DEBUG_FORMAT_V2 = "## [Thread: %1$s][%2$s] %3$s - %4$s",
                                ERROR_FORMAT_V2 = "## [Thread: %1$s][%2$s] %3$s - %4$s - EXCEPTION TYPE: %5$s, MESSAGE: \"%6$s\"";
	
	private static final boolean DEBUG_MODE = true;
	private static final boolean THREAD_DETAILS_ON = true;
	
	public static void d(String tag, String message){
		String full_text = null;
		if(THREAD_DETAILS_ON==true){
			String threadSign = Thread.currentThread().getName() +"-" +Thread.currentThread().getId();
			full_text = String.format(DEBUG_FORMAT_V2,  
					                  threadSign, 
					                  DateUtils.getFormattedNow(), 
					                  tag, 
				                      message);
		} else {
			full_text = String.format(DEBUG_FORMAT_V1, 
									  DateUtils.getFormattedNow(), 
									  tag, 
									  message);
		}
		
		System.out.println(full_text);
	}
	
	public static void e(String tag, String method, Exception ex){
		String full_text = null;
		if(THREAD_DETAILS_ON==true){
			String threadSign = Thread.currentThread().getName() +"-" +Thread.currentThread().getId();
			full_text = String.format(ERROR_FORMAT_V2,  
					                  threadSign, 
					                  DateUtils.getFormattedNow(), 
                                      tag, 
                                      method,
                                      ex.getClass().getSimpleName(), 
                                      (ex!=null) ? ex.getLocalizedMessage() : "");
		} else {
			full_text = String.format(ERROR_FORMAT_V1, 
								      DateUtils.getFormattedNow(), 
                                      tag, 
                                      method,
                                      ex.getClass().getSimpleName(), 
                                      (ex!=null) ? ex.getLocalizedMessage() : "");
		}

		System.err.println(full_text);
		
		if(DEBUG_MODE)
			ex.printStackTrace();
	}
	
}
