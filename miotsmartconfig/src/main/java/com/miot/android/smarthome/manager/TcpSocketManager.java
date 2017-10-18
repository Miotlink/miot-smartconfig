package com.miot.android.smarthome.manager;

import android.util.Log;

import com.miot.android.smarthome.callback.SmartConfigIReceiver;
import com.miot.android.smarthome.callback.TcpIReciver;
import com.miot.android.smarthome.protocol.TcpSocket;
import com.miot.android.smarthome.utils.Mlcc_ParseUtils;
import com.miot.android.smarthome.utils.VspBuildAndParseUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/25 0025.
 */
public class TcpSocketManager implements TcpIReciver{

	private static  TcpSocketManager instance=null;

	public static final String DEFAULT_CHARSET = "ISO-8859-1";

	private static final int port=9600;

	private ArrayList<TcpSocket> list=new ArrayList<>();

	private TcpSocket tcpSocket=new TcpSocket();

	public static synchronized TcpSocketManager getInstance() {
		if (instance==null){
			synchronized (TcpSocketManager.class){
				if (instance==null){
					instance=new TcpSocketManager();
				}
			}
		}
		return instance;
	}

	private SmartConfigIReceiver smartConfigIReceiver=null;

	public void setSmartConfigIReceiver(SmartConfigIReceiver smartConfigIReceiver) {
		this.smartConfigIReceiver = smartConfigIReceiver;
	}

	public TcpSocketManager(){}

	public void init(){
		tcpSocket.setTsr(this);
	}


	public boolean isConnect(String ipAddress)throws Exception{
		boolean isConnected=tcpSocket.connect(ipAddress,port);
		if (isConnected){
			list.add(tcpSocket);
		}
		return isConnected;
	}

	public boolean isSend(String message)throws Exception{
		byte[] bytes = message.getBytes(DEFAULT_CHARSET);
		return tcpSocket.send(bytes,bytes.length);
	}

	@Override
	public void onReceive(byte[] src, int len) throws Exception {
		try {
			byte[] bytes = VspBuildAndParseUtils.encrypt(src);
			String msg = VspBuildAndParseUtils.getMlccContent(bytes, len);
			Log.e("onReceiver",msg);
			if (smartConfigIReceiver == null) {
				throw new Exception("Not init interface DeviceIReceiver");
			}
			if (Mlcc_ParseUtils.isSetWifiAck(msg)) {
				smartConfigIReceiver.onSetWifiAck(msg);
				return;
			}
			if (!Mlcc_ParseUtils.isPlatformFinishAck(msg)) {
				smartConfigIReceiver.onFirstConfigDeviceReceiver(msg);
				return;
			}
			smartConfigIReceiver.onFirstConfigFinish(msg);
		} catch (Exception e) {
			e.printStackTrace();
			if (smartConfigIReceiver != null) {
				try {
					smartConfigIReceiver.smartConfigError("未知异常");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	public void onDisConnect(){
		if (tcpSocket.hasConnect()){
			tcpSocket.disconnect();
		}
	}

	public void onDestory(){
		if (list!=null&&list.size()>0){
			for (TcpSocket tcp:list) {
				if (tcp.hasConnect()){
					tcp.disconnect();
				}
			}
		}
		list.clear();
		if (tcpSocket.hasConnect()){
			tcpSocket.disconnect();
		}
	}

}
