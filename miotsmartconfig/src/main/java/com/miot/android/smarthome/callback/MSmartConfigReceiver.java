package com.miot.android.smarthome.callback;

import java.util.Map;

/**
 * 设备配置完成
 */
public interface MSmartConfigReceiver {

	/**
	 * 设备配置成功返回字段
	 * @param map
	 */
	public void mSmartConfig(int errorCode, String errorMessage, Map<String, Object> map);

	/**
	 * 设备配置进度
	 * @param progress
	 */
	public void mSmartConfigProgress(int progress);


	public void mSmartConfigToastError(int errorCode, String errorMessage, Map<String, Object> map);

}
