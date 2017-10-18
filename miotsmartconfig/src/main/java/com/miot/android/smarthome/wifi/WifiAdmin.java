
package com.miot.android.smarthome.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class WifiAdmin.
 * 
 * @ClassName: WifiAdmin
 * @Description:
 * @author 作者 E-mail <a href="mailto:yubo@51box.cn">禹波</a>
 * @version 创建时间：2014-1-7 11:11:55 Wifi admin.
 */
public class WifiAdmin {// 定义WifiManager对象
	/** The m wifi manager. */
	private final WifiManager mWifiManager;

	// 定义WifiInfo对象
	/** The m wifi info. */
	private final WifiInfo mWifiInfo;

	// 扫描出的网络连接列表
	/** The m wifi list. */
	private List<ScanResult> mWifiList;

	// 网络连接列表
	/** The m wifi configuration. */
	private List<WifiConfiguration> mWifiConfiguration;

	// 定义一个WifiLock
	/** The m wifi lock. */
	WifiLock mWifiLock;

	// 构造器
	/**
	 * Instantiates a new wifi admin.
	 * 
	 * @param context
	 *            the context
	 */
	public WifiAdmin(Context context) {
		// 取得WifiManager对象
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象

		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	/**
	 * Sets the wifi enable.
	 * 
	 * @param isWifi
	 *            the new wifi enable
	 */
	public void setWifiEnable(boolean isWifi) {
		if (isWifi) {
			openWifi();
		} else {
			closeWifi();
		}
	}

	// 打开WIFI
	/**
	 * Open wifi.
	 */
	public void openWifi() {
		// if (!mWifiManager.isWifiEnabled()) {
		mWifiManager.setWifiEnabled(true);
		// }
	}

	// 关闭WIFI
	/**
	 * Close wifi.
	 */
	public void closeWifi() {
		// if (mWifiManager.isWifiEnabled()) {
		mWifiManager.setWifiEnabled(false);
		// }
	}

	/**
	 * Gets the open state.
	 * 
	 * @return the open state
	 */
	public boolean getOpenState() {
		boolean state = false;
		switch (checkState()) {
		case WifiManager.WIFI_STATE_ENABLED:
			state = true;
			break;
		case WifiManager.WIFI_STATE_ENABLING:

			break;
		case WifiManager.WIFI_STATE_DISABLING:

			break;
		case WifiManager.WIFI_STATE_DISABLED:

			break;
		case WifiManager.WIFI_STATE_UNKNOWN:

			break;

		default:
			break;
		}
		return state;
	}

	// 检查当前WIFI状态
	/**
	 * Check state.
	 * 
	 * @return the int
	 */
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// 锁定WifiLock
	/**
	 * Acquire wifi lock.
	 */
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁WifiLock
	/**
	 * Release wifi lock.
	 */
	public void releaseWifiLock() {
		// 判断时候锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 创建一个WifiLock
	/**
	 * Creat wifi lock.
	 */
	public void creatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	// 得到配置好的网络
	/**
	 * Gets the configuration.
	 * 
	 * @return the configuration
	 */
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	public int getConfigurationIndexBySSID(String ssid) {
		if (mWifiConfiguration == null) {
			return -1;
		}
		int index = 0;
		for (WifiConfiguration wifiConfiguration : mWifiConfiguration) {
			if (!TextUtils.isEmpty(wifiConfiguration.SSID) && wifiConfiguration.SSID.replace("\"", "").equals(ssid)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	// 指定配置好的网络进行连接
	/**
	 * Connect configuration.
	 * 
	 * @param index
	 *            the index
	 */
	public void connectConfiguration(int index) {
		// 索引大于配置好的网络索引返回
		if (index < 0 || index > mWifiConfiguration.size()) {
			return;
		}
		// 连接配置好的指定ID的网络
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}

	private boolean isStartScanFlag;

	/**
	 * Start scan.
	 */
	public void startScan() {
		mWifiManager.startScan();
		// 得到扫描结果
		mWifiList = mWifiManager.getScanResults();
		// 得到配置好的网络连接
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
		isStartScanFlag = true;
	}

	// 得到网络列表
	/**
	 * Gets the wifi list.
	 * 
	 * @return the wifi list
	 */
	public List<ScanResult> getWifiList() {
		if (!isStartScanFlag) {
			startScan();
		}
		return mWifiList;
	}

	// 查看扫描结果
	/**
	 * Look up scan.
	 * 
	 * @return the string builder
	 */
	public StringBuilder lookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("/n");
		}
		return stringBuilder;
	}

	// 得到MAC地址
	/**
	 * Gets the mac address.
	 * 
	 * @return the mac address
	 */
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的SSID
	/**
	 * Gets the ssid.
	 * 
	 * @return the ssid
	 */
	public String getSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// 得到接入点的BSSID
	/**
	 * Gets the bssid.
	 * 
	 * @return the bssid
	 */
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	// 得到IP地址
	/**
	 * Gets the iP address.
	 * 
	 * @return the iP address
	 */
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	/**
	 * Gets the network id.
	 * 
	 * @return the network id
	 */
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到WifiInfo的所有信息包
	/**
	 * Gets the wifi info.
	 * 
	 * @return the wifi info
	 */
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// 添加一个网络并连接
	/**
	 * Adds the network.
	 * 
	 * @param wcg
	 *            the wcg
	 */
	public boolean addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
		System.out.println("a--" + wcgID);
		System.out.println("b--" + b);
		return b;
	}

	// 断开指定ID的网络
	/**
	 * Disconnect wifi.
	 * 
	 * @param netId
	 *            the net id
	 */
	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}
	
	 private WifiConfiguration IsExsits(String SSID) {
		 try {
			 List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
			 for (WifiConfiguration existingConfig : existingConfigs) {
				 if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
					 return existingConfig;
				 }
			 }
			 return null;
		 }catch (Exception e){
			 e.printStackTrace();
		 }
		 return null;
	    }  

	/**
	 * 创建wifi配置信息
	 * 
	 * @param SSID
	 * @param password
	 * @param type
	 * @return
	 */
	public WifiConfiguration createWifiInfo(String SSID, String password,
			int type) {

		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		WifiConfiguration tempConfig = this.IsExsits(SSID);  
		if (tempConfig != null) {  
			mWifiManager.removeNetwork(tempConfig.networkId);  
		}  
		if (type == SECURITY_NONE) { // WIFICIPHER_NOPASS
			config.wepKeys[0] = "\"" + "\"";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == SECURITY_WEP) { // WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == SECURITY_PSK) { // WIFICIPHER_WPA
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;


	/**
	 *用来获取路由的频率，可根据此值判断路由器是否为5G
	 */
	public int getfrequency() {
		String tempSsidString = mWifiInfo.getSSID();
		int freque = 0;
		if (tempSsidString != null && tempSsidString.length() > 2) {
			String wifiSsid = tempSsidString.substring(1, tempSsidString.length() - 1);
			List<ScanResult> scanResults=mWifiManager.getScanResults();
			for(ScanResult scanResult:scanResults){
				if(scanResult.SSID.equals(wifiSsid)){
					freque = scanResult.frequency;
					break;
				}
			}
		}
		return freque;
	}

	/**
	 * 获取当前连接无线网的网络强度，0表示较差，1表示网络很差需要重启等操作
	 * @return
	 */
	public int getwifiRssi() {
		int rssi = Math.abs(mWifiInfo.getRssi());
		if (rssi >= 60 && rssi <=75) {
			return 0;
		}else if (rssi >75) {
			return 1;
		}else {
			return 2;
		}
	}
}
