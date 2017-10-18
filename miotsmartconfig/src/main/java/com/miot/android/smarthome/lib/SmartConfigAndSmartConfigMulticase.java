package com.miot.android.smarthome.lib;

public class SmartConfigAndSmartConfigMulticase implements Runnable{
	// 广播方式
	private static SmartUDPBroadcast smartConfig=new SmartUDPBroadcast();;

	// 组播方式
	private static SmartMulticse smartMulticast=SmartMulticse.getInstance();;

	private static SmartConfigAndSmartConfigMulticase instance=null;

	// 是否配置
	private boolean isConfig;

	private String ssid="";
	private String	password="";

	private Thread myThread=null;

	public static SmartConfigAndSmartConfigMulticase getInstance() {
		if (instance == null) {
			instance = new SmartConfigAndSmartConfigMulticase();
		}
		return instance;
	}


	public void sendData(String ssid, String password) {

		this.ssid = ssid;
		this.password = password;
		isConfig = true;
		myThread=new Thread(this);
		myThread.start();
	}

	public void isStop() {
		isConfig = false;
		if (myThread!=null){
			myThread.interrupt();
			myThread=null;
		}
	}


	@Override
	public void run() {
		try {
			while (isConfig) {
				for (int i = 0; i < 2; i++) {
					smartConfig.start(ssid, password);
				}
				Thread.sleep(50);
					for (int i = 0; i < 2; i++) {
						smartMulticast.startConfig(ssid, password);
					}
					Thread.sleep(50);
			}
		} catch (Exception e) {

		}
	}
}
