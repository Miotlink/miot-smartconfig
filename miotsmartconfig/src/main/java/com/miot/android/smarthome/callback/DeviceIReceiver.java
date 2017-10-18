package com.miot.android.smarthome.callback;

/**
 * Created by Administrator on 2017/2/14 0014.
 */
public interface DeviceIReceiver {
	/**
	 * 配置返回SmartConnected 包
	 * @param IpAddress 获取IP 地址
	 * @param port 端口
	 * @param msg 数据包
	 */
	public void onSmartConnected(String IpAddress, int port, String msg) throws Exception;

	/**
	 * 通用配置返回
	 * @param msg
	 */
	public void onFirstConfigDeviceReceiver(String msg)throws Exception;

	/**
	 * 返回结束数据包
	 * @param msg
	 */
	public void onFirstConfigFinish(String msg)throws Exception;


	public void smartConfigError(String message)throws Exception;

	public void onSetWifiAck(String message)throws Exception;
}
