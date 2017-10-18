package com.miot.android.smarthome.protocol;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpSmartSocket {

	public static String TAG = UdpSmartSocket.class.getName();


	private DatagramSocket socket = null;

	public static UdpSmartSocket instance=null;


	public static UdpSmartSocket getInstance() {
		if (instance==null){
			instance=new UdpSmartSocket();
		}
		return instance;
	}

	public UdpSmartSocket(){
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private InetAddress address = null;


	public boolean sendData(String str){
		byte[] bytes=str.getBytes();
		return send("255.255.255.255", 30000, bytes, bytes.length);
	}


	private DatagramPacket dPacket = null;

	public boolean send(String ip, int port, byte[] bs, int len) {
		try {
			address = InetAddress.getByName(ip);
			dPacket = new DatagramPacket(bs, len, address, port);
			socket.send(dPacket);
		} catch (UnknownHostException e) {
			Log.e(TAG, "send: not find server.");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "send: message fail");
			return false;
		}
		return true;

	}

}
