package com.miot.android.smarthome.callback;

/**
 * Created by Administrator on 2017/2/14 0014.
 */
public interface DeviceNewIReceiver {
	/**
	 * 配置返回SmartConnected 包
	 * @param IpAddress 获取IP 地址
	 * @param port 端口
	 * @param msg 数据包
	 */
	public void onSmartConnected(String IpAddress, int port, String msg) throws Exception;

}
