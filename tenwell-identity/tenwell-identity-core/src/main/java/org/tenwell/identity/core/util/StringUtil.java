package org.tenwell.identity.core.util;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
	
	public static String nvl(String str, String defaultValue){
		if(str == null || "".equals(str))
			return defaultValue;
		
		return str.toString();
	}
	
	public static String nvl(Object str, String defaultValue){
		if(str == null || "".equals(str))
			return defaultValue;
		
		return nvl(str.toString(), defaultValue);
	}
	
	public static String nvl(Object str){
		if(str == null)
			return "";
		
		return nvl(str.toString());
	}
	
	public static String nvl(String str){
		return nvl(str, "");
	}
	
	
	public static String decodeURL(String str, String enc){
		String resultStr = "";
		try{
			if(str!=null){
				resultStr = URLDecoder.decode(str, enc);
			}
			
		}catch(Exception e){
			LOGGER.error("URL Decode Error", e);
		}
		
		return resultStr;
	}
	
	
	public static String encodeURL(String str, String enc){
		String resultStr = "";
		try{
			resultStr = URLEncoder.encode(str, enc);
		}catch(Exception e){
			LOGGER.error("URL Encode Error", e);
		}
		
		return resultStr;
	}
	
	public static int parseInt(Object num, int defaultValue){
		String str = nvl(num, "0");
		
		return parseInt(str, defaultValue);
	}
	
	public static int parseInt(String num, int defaultValue){
		try{
			int number = Integer.parseInt(num);
			return number;
		} catch(NumberFormatException e){
			LOGGER.error("StringUtil.parseInt", e);
		}
		
		return defaultValue;
	}

}
