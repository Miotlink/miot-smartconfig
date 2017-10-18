package com.miot.android.smarthome.lib;

import com.miot.android.smarthome.protocol.UdpSmartSocket;
import com.miot.android.smarthome.utils.CRC16Utils;
import com.miot.android.smarthome.utils.HexString;
import com.miot.android.smarthome.utils.SmartConsts;

import java.util.ArrayList;
import java.util.List;


public class SmartUDPBroadcast {

	private UdpSmartSocket cuUdpSocket = UdpSmartSocket.getInstance();
	private int date = 10;


	public SmartUDPBroadcast() {

	}
	public void start(String ssid, String password) {
		char[] chSSID=null;
		char[] chPWD=null;
		try {
			chSSID = ssid.toCharArray();
			chPWD = password.toCharArray();
			sendData(chSSID,chPWD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void sendData(char[] chSSID,char[] chPWD) throws Exception {
		 List<StringBuffer> listSSID = new ArrayList<StringBuffer>();
		List<StringBuffer> listPWD = new ArrayList<StringBuffer>();
		for (int i = 0; i < SmartConsts.SMARTCONFIG_FIRST_SIGN_LEN; i++) {
			send(SmartConsts.SMART_FIRST_SGIN);
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}
		for (int i = 0; i < chSSID.length; i++) {
			int ascii = (int) chSSID[i];
			send(HexString.randomNumStr(ascii));
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}

		for (int i = 0; i < SmartConsts.SMARCONFIG_TWO_NUMBER_SGIN; i++) {
			send(SmartConsts.SMART_TWO_SGIN);
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}

		// password
		for (int i = 0; i < chPWD.length; i++) {
			int ascii = (int) chPWD[i];
			send(HexString.randomNumStr(ascii));
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}
		for (int i = 0; i < SmartConsts.SMARCONFIG_TWO_NUMBER_SGIN; i++) {
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
			send(SmartConsts.SMART_THIRD_SGIN);
		}

		String str = "";
		for (int i = 0; i < chSSID.length; i++) {
			int ascii = (int) chSSID[i];
			str +=ascii + " ";
		}

		for (int i = 0; i < chPWD.length; i++) {
			int ascii = (int) chPWD[i];
			str += ascii + " ";
		}
		String crc = CRC16Utils.getCRC(str);
		int maxLen = CRC16Utils.getCrcMaxLen(crc);
		int minLen =  CRC16Utils.getCrcMinLen(crc);
		Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		send(HexString.randomNumStr(maxLen));
		Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		send(HexString.randomNumStr(minLen));
	}

	private boolean send(String string) {
		return cuUdpSocket.sendData(string);

	}
}
