package com.miot.android.smarthome.lib;

import com.miot.android.smarthome.protocol.UdpSmartSocket;
import com.miot.android.smarthome.utils.CRC16Utils;
import com.miot.android.smarthome.utils.HexString;
import com.miot.android.smarthome.utils.SmartConsts;

import java.util.ArrayList;
import java.util.List;


public class SmartConfig  {

	private UdpSmartSocket cuUdpSocket = new UdpSmartSocket();

	public void sendData(String ssid, String password) {
		 char[] chSSID=null;
		 char[] chPWD=null;

		try {
			chSSID = ssid.toCharArray();
			chPWD = password.toCharArray();
			sendData(chSSID,chPWD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendData(char[] chSSID,char[] chPWD) throws Exception {
		List<StringBuffer> listSSID = null;
		List<StringBuffer> listPWD = null;
		listSSID = new ArrayList<StringBuffer>();
		listPWD = new ArrayList<StringBuffer>();
		for (int i = 0; i < SmartConsts.SMARTCONFIG_FIRST_SIGN_LEN; i++) {
			send(SmartConsts.SMART_FIRST_SGIN);
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}
		for (int i = 0; i < chSSID.length; i++) {
			int ascii = (int) chSSID[i];
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < ascii; j++) {
				sb.append(chSSID[i]);
			}
			listSSID.add(sb);
			send(sb.toString());
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}

		for (int i = 0; i < SmartConsts.SMARCONFIG_TWO_NUMBER_SGIN; i++) {
			send(SmartConsts.SMART_TWO_SGIN);
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}

		// password
		for (int i = 0; i < chPWD.length; i++) {
			int ascii = (int) chPWD[i];
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < ascii; j++) {
				sb.append(chPWD[i]);
			}
			listPWD.add(sb);
			send(sb.toString());
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		}
		for (int i = 0; i < SmartConsts.SMARCONFIG_TWO_NUMBER_SGIN; i++) {
			Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
			send(SmartConsts.SMART_THIRD_SGIN);
		}

		String str = "";
		for (int i = 0; i < listSSID.size(); i++) {
			StringBuffer sb = listSSID.get(i);
			str += sb.length() + " ";
		}

		for (int i = 0; i < listPWD.size(); i++) {
			StringBuffer sb = listPWD.get(i);
			str += sb.length() + " ";
		}
		String crc = CRC16Utils.getCRC(str);
		int maxLen = CRC16Utils.getCrcMaxLen(crc)-3;
		int minLen =  CRC16Utils.getCrcMinLen(crc)-3;
		Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		send(HexString.randomString(maxLen));
		Thread.sleep(SmartConsts.SMARTCONFIG_CONFIG_UDP_TIME);
		send(HexString.randomString(minLen));
	}

	private boolean send(String string) {

		byte[] makeFirst = string.getBytes();

		return cuUdpSocket.send("255.255.255.255", 30000, makeFirst,
				makeFirst.length);

	}



}
