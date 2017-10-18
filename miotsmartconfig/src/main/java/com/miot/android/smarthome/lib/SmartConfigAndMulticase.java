package com.miot.android.smarthome.lib;


public class SmartConfigAndMulticase {
	// 广播方式
	private static SmartConfig smartConfig=null;

	// 组播方式
	private static SmartMulticse smartMulticast=null;

	private static SmartConfigAndMulticase instance=null;

	// 是否配置
	private boolean isConfig;

	private String ssid, password;

	public static SmartConfigAndMulticase getInstance() {
		if (instance == null) {
			instance = new SmartConfigAndMulticase();
			smartConfig = new SmartConfig();
			smartMulticast = SmartMulticse.getInstance();
		}
		return instance;
	}

	public void sendData(String ssid, String password) {
		this.ssid = ssid;
		this.password = password;
		isConfig = true;
		new Thread(runnable).start();
	}

	public void isStop() {
		if (isConfig)
		isConfig = false;
		instance=null;
		smartConfig=null;
		smartMulticast=null;

	}
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			try {
				while (isConfig) {
					for (int i = 0; i < 2; i++) {
						smartConfig.sendData(ssid, password);
					}
					Thread.sleep(100);
					for (int i = 0; i < 2; i++) {
						smartMulticast.startConfig(ssid, password);
					}
					Thread.sleep(200);
				}

			} catch (Exception e) {

			}

		}
	};

}
