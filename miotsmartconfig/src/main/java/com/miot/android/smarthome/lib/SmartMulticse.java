package com.miot.android.smarthome.lib;

import com.miot.android.smarthome.protocol.SmartMulticast;
import com.miot.android.smarthome.utils.URLEncoderUtils;


public class SmartMulticse {

	private static SmartMulticse instance = null;

	private static final String HEAD_SING="SmartConfig-V2";

	private static final int   SMARTMULTICAST_PORT=30000;

	private static final int SLEEP_TIME=10;

	public static SmartMulticse getInstance() {
		if (instance == null) {
			instance = new SmartMulticse();
		}
		return instance;
	}

	private SmartMulticast socket=new SmartMulticast();

	private SmartMulticse() {
	}

	public void startConfig(String ssid, String password) {
		char[] charSSID = null;
		try {
			charSSID= URLEncoderUtils.URLEncoderChineseSSID(ssid);
		} catch (Exception e1) {
			charSSID=ssid.toCharArray();
		}
		char[] charPwd = password.toCharArray();
		byte[] buffer = HEAD_SING.getBytes();
		int len = buffer.length;
		int n2 = 0;
		try {
			// 标志位
			for (int i = 1; i <= 5; i++) {
				n2++;
				socket.send(SMARTMULTICAST_PORT, "239.118." + n2 + "." + i, buffer, len);
				Thread.sleep(SLEEP_TIME);
			}
			n2++;
			socket.send(SMARTMULTICAST_PORT, "239.119." + n2 + "." + charSSID.length, buffer,
					len);
			Thread.sleep(SLEEP_TIME);
			// password长度
			n2++;
			socket.send(SMARTMULTICAST_PORT, "239.119." + n2 + "." + password.length(),
					buffer, len);
			Thread.sleep(SLEEP_TIME);
			// ssid内容
			for (int i = 0; i < charSSID.length; i++) {
				n2++;
				socket.send(SMARTMULTICAST_PORT, "239.120." + n2 + "." + ((int) charSSID[i]),
						buffer, len);
				Thread.sleep(SLEEP_TIME);
			}
			// password内容
			for (int i = 0; i < charPwd.length; i++) {
				n2++;
				socket.send(SMARTMULTICAST_PORT, "239.121." + n2 + "." + ((int) charPwd[i]),
						buffer, len);
				Thread.sleep(SLEEP_TIME);
			}
		} catch (Exception e) {
			
		} finally {
			n2 = 0;
		}
	}

}
