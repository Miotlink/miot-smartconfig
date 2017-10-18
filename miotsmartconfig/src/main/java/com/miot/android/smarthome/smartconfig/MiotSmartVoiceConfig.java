package com.miot.android.smarthome.smartconfig;

import android.content.Context;

import com.miot.android.smarthome.callback.SmartVoiceIReceiver;
import com.miot.android.smarthome.manager.SocketManager;
import com.miot.android.smarthome.utils.SmartConsts;

/**
 * Created by Administrator on 2017/9/27 0027.
 */
public class MiotSmartVoiceConfig  {

	private static MiotSmartVoiceConfig instance=null;

	public static synchronized MiotSmartVoiceConfig getInstance() {
		if (instance==null){
			synchronized (MiotSmartVoiceConfig.class){
				if (instance==null){
					instance=new MiotSmartVoiceConfig();
				}
			}
		}
		return instance;
	}


	private SocketManager socketManager=null;

	private SmartVoiceIReceiver smartVoiceIReceiver=null;

	public void setSmartVoiceIReceiver(SmartVoiceIReceiver smartVoiceIReceiver) {
		this.smartVoiceIReceiver = smartVoiceIReceiver;
	}

	public void init(Context context)throws Exception{
		socketManager = SocketManager.getInstance(context);
		socketManager.init(SmartConsts.DEVICE_VOIDE_LOCALHOST_PORT);
		socketManager.setiReceiver(smartVoiceIReceiver);
	}

	public void onDestory(){
		if (socketManager!=null){
			socketManager.onDistory();
			socketManager =null;
		}
	}
}
