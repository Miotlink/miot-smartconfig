package com.miot.android.smarthome.utils;

import java.net.URLEncoder;

public class URLEncoderUtils {

	public static String string = "[\\u4e00-\\u9fa5]+";

	public static char[] URLEncoderChineseSSID(String ssid) throws Exception {
		char[] ssidName = null;
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < ssid.length(); i++) {
			if (ssid.substring(i, i + 1).matches(string)) {
				char[] c = hexString2Bytes(URLEncoder.encode(
						ssid.substring(i, i + 1), "UTF-8").replaceAll("%", ""));
				for (int j = 0; j < c.length; j++) {
					stringBuffer.append(c[j]);
				}
			} else {
				stringBuffer.append(ssid.substring(i, i + 1));
			}
		}
		ssidName = stringBuffer.toString().toCharArray();
		return ssidName;
	}

	public static char[] hexString2Bytes(String src) {
		src = src.replace(" ", "");
		char[] ret = new char[src.length() / 2];
		try {
			for (int i = 0; i < src.length() / 2; i++) {
				ret[i] = (char) Integer.parseInt(
						src.substring(i * 2, 2 * i + 2), 16);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return ret;
	}

}
