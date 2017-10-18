package com.miot.android.smarthome.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacUtils {

	
	public static boolean isMacAddress(String macAddress) {
		String reg = "^([0-9a-fA-F]){2}([:][0-9a-fA-F]{2}){5}";
		if (macAddress.equals("00.00.00.00.00")) {
			return false;
		}
		return Pattern.compile(reg).matcher(macAddress).find();
	}
	
	public static boolean isMobileNO(String mobiles){ 
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,0-9])|(14[0,7])|(17[0,0-9]))\\d{8}$"); 
		Matcher m = p.matcher(mobiles); 
		return m.matches(); 
		} 
	
	public static String MakeMac(String string){
		String s="";
		try{
		String[] macArray = string.split("(?<=\\w)(?=\\w)");
		String[] newArray = new String[macArray.length / 2];
		for (int i = 0; i < macArray.length / 2; i++) {
			newArray[i] = macArray[2 * i] + macArray[2 * i + 1] + ":";
		}
		StringBuilder sb = new StringBuilder();
		for (String str : newArray) {
			sb.append(str);
		}
		s=sb.toString().substring(0, sb.toString().length() - 1).toUpperCase();
		}catch (Exception e){

		}
		return s;

	}

}
