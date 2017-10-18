package com.miot.android.smarthome.manager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.miot.android.smarthome.callback.DeviceNewIReceiver;
import com.miot.android.smarthome.callback.IReceiver;
import com.miot.android.smarthome.protocol.UdpSocket;
import com.miot.android.smarthome.utils.Mlcc_ParseUtils;
import com.miot.android.smarthome.utils.VspBuildAndParseUtils;

import java.util.Random;

/**
 * 监听设备返回的数据
 */
public class SocketNewManager implements IReceiver {
	public static int Search_Cu_Port = new Random().nextInt(3000) + 30000;
	public static int Search_Device_Port = 64535;
	public static final String DEFAULT_CHARSET = "ISO-8859-1";
	private static SocketNewManager instance = null;

	public static WifiManager.MulticastLock lock = null;

	public static SocketNewManager getInstance(Context context) {
		if (instance == null) {
			synchronized (SocketManager.class) {
				if (instance == null) {
					instance = new SocketNewManager(context);
				}
			}
		}
		return instance;
	}

	private static Context context = null;

	private DeviceNewIReceiver deviceIReceiver = null;

	private static UdpSocket udpSocket = new UdpSocket();

	private SocketNewManager(Context context) {
		this.context = context;
		WifiManager manager = (WifiManager) SocketNewManager.context.getSystemService(Context.WIFI_SERVICE);
		lock = manager.createMulticastLock("wifi");
	}

	public void setDeviceIReceiver(DeviceNewIReceiver deviceIReceiver) {
		this.deviceIReceiver = deviceIReceiver;
	}

	/**
	 * 初始化 监听端口
	 *
	 * @param port
	 */
	public void init(int port) throws Exception {
		udpSocket.startRecv(port, this);
	}

	public void onDistory() {
		if (udpSocket != null) {
			udpSocket.onStop();
		}
	}

	public boolean send(String ipAddress,String deviceConfig) throws Exception {
		if (deviceConfig.isEmpty()) {
			throw new Exception("configData is empty");
		}
		byte[] bytes = deviceConfig.getBytes(DEFAULT_CHARSET);
		return VspBuildAndParseUtils.send(udpSocket, ipAddress, VspBuildAndParseUtils.Search_Pu_Port, bytes);
	}

	public static void acquire(){
		if(lock!=null){
			lock.acquire();
		}
	}

	public static void release(){
		try {
			if(lock!=null&&lock.isHeld()){
				lock.release();
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (lock!=null&&lock.isHeld()){
				lock.release();
			}
		}
	}

	@Override
	public void onReceive(int localPort, String IpAddress, int port, byte[] src, int len) {
		try {
			byte[] bytes = VspBuildAndParseUtils.encrypt(src);
			String msg = VspBuildAndParseUtils.getMlccContent(bytes, len);
			Log.e("onReceiver",msg);
			if (deviceIReceiver == null) {
				throw new Exception("not init interface DeviceIReceiver");
			}
			if (localPort == Search_Device_Port) {
				if (Mlcc_ParseUtils.isSmartConnected(msg)) {
					deviceIReceiver.onSmartConnected(IpAddress, port, msg);
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}
}
