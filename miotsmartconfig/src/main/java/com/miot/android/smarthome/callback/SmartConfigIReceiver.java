package com.miot.android.smarthome.callback;

/**
 * Created by Administrator on 2017/9/25 0025.
 */
public interface SmartConfigIReceiver {
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
