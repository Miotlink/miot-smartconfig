package com.miot.android.smarthome.smartconfig;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.hiflying.smartlink.ISmartLinker;
import com.hiflying.smartlink.OnSmartLinkListener;
import com.hiflying.smartlink.SmartLinkedModule;
import com.hiflying.smartlink.v7.MulticastSmartLinker;
import com.miot.android.smarthome.callback.DeviceNewIReceiver;
import com.miot.android.smarthome.callback.MSmartConfigReceiver;
import com.miot.android.smarthome.callback.SmartConfigIReceiver;
import com.miot.android.smarthome.entity.FirstData;
import com.miot.android.smarthome.manager.SocketManager;
import com.miot.android.smarthome.manager.SocketNewManager;
import com.miot.android.smarthome.manager.TcpSocketManager;
import com.miot.android.smarthome.utils.MacUtils;
import com.miot.android.smarthome.utils.Mlcc_ParseUtils;
import com.miot.android.smarthome.utils.SmartConsts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/19 0019.
 */
public class MiotNewHF_SmartConfig implements OnSmartLinkListener,DeviceNewIReceiver,SmartConfigIReceiver{
	private static MiotNewHF_SmartConfig instance=null;

	public static MiotNewHF_SmartConfig getInstance(Context context) {
		if (instance==null){
			synchronized (MiotNewSmartConifg.class){
				if (instance==null){
					instance=new MiotNewHF_SmartConfig(context);
				}
			}
		}
		return instance;
	}

	private Context context=null;

	private String isSdkMac="";

	private String isUDPMac="";

	private boolean isRun=true;

	private boolean isPlatform=false;

	private ISmartLinker snifferSmartLinker = null;

	private FirstData firstData=null;

	private SocketNewManager socketManager=null;

	private TcpSocketManager tcpSocketManager=null;

	private String qrcode="";

	private String kindId="";

	private String modelId="";

	private String validationModel = "";

	private String scanMac="";

	private String type="";

	private ArrayList<FirstData> firstDatas=null;

	private MyHfSmartConfigThread myHfSmartConfigThread=null;

	private Map<String,Object> mapValue=null;

	private volatile int failCode=0;

	private int failCodeType=0;

	private String deviceModelId="";



	private MiotNewHF_SmartConfig(Context context){
		this.context=context;

	}
	private MSmartConfigReceiver mSmartConfigReceiver=null;


	public void setmSmartConfigReceiver(MSmartConfigReceiver mSmartConfigReceiver) {
		this.mSmartConfigReceiver = mSmartConfigReceiver;
	}

	public void startSmartConfig(String routeName, String routePass, String json, String qrcode,String validationModel,String scanMac)throws Exception{
		if (routeName.isEmpty()||json.isEmpty()||qrcode.isEmpty()){
			throw new Exception("routeName|| json ||qrcode isEmpty");
		}
		if (qrcode.length()<11){
			throw new Exception("qrcode length is not 11");
		}
		this.qrcode=qrcode;
		this.validationModel=validationModel;
		this.scanMac=scanMac;
		try{
			kindId = String.valueOf(Integer.parseInt(qrcode.substring(1, 4)));
			modelId = String.valueOf(Integer.parseInt(qrcode.substring(4, 8)));
		}catch (Exception e){
		}
		type=qrcode.substring(8,10);
		failCodeType=Integer.parseInt(type)*100;
		firstDatas= (ArrayList<FirstData>) Mlcc_ParseUtils.getFirstData(json);
		if (firstDatas==null){
			throw new Exception("type is not find");
		}
		Collections.sort(firstDatas, new SortIndex());
		if (type.isEmpty()){
			throw new Exception("type is empty ");
		}
		if (!type.equals("30")){
			throw new Exception("qrcode is error ");
		}
		initData();
		socketManager=SocketNewManager.getInstance(context);
		socketManager.setDeviceIReceiver(this);
		socketManager.init(SmartConsts.DEVICE_LOCALHOST_PORT);
		tcpSocketManager=TcpSocketManager.getInstance();
		tcpSocketManager.setSmartConfigIReceiver(this);
		tcpSocketManager.init();
		snifferSmartLinker = MulticastSmartLinker.getInstance();
		if (snifferSmartLinker.isSmartLinking()){
			return;
		}
		snifferSmartLinker.start(context, routePass, routeName);
		snifferSmartLinker.setTimeoutPeriod(90000);
		snifferSmartLinker.setOnSmartLinkListener(this);
		myHfSmartConfigThread=new MyHfSmartConfigThread();
		myHfSmartConfigThread.start();
	}

	private void initData(){
		isUDPMac="";
		isRun=true;
		isSdkMac="";
		isUDPMac="";
		deviceModelId="";
		failCode=10000+failCodeType+110;
		if(mapValue!=null){
			mapValue=null;
		}
	}

	@Override
	public void onLinked(SmartLinkedModule smartLinkedModule) {
		isSdkMac=  MacUtils.MakeMac(smartLinkedModule.getMac());
		ipAddress=smartLinkedModule.getIp();
	}

	@Override
	public void onCompleted() {
		handler.sendEmptyMessageDelayed(SmartConsts.HANFENG_SMARTCONFIG_FINSH,10*1000);
	}

	@Override
	public void onTimeOut() {
		handler.sendEmptyMessageDelayed(SmartConsts.HANFENG_SMARTCONFIG_TIMEOUT,10*1000);
	}
	Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case SmartConsts.HANFENG_SMARCONNECTED:
					break;
				case SmartConsts.HANFENG_SMARTCONFIG_FINSH:
					if (isUDPMac.isEmpty()){
						if(!TextUtils.isEmpty(scanMac)&&!scanMac.equals(isSdkMac.toUpperCase())){
							failCode = 10000 + failCodeType + 130;
							return;
						}
						isPlatform=true;
						firstData=firstDatas.get(index);
					}
					break;
				case SmartConsts.HANFENG_SMARTCONFIG_TIMEOUT:
                    failCode=10000+failCodeType+110;
					if (isUDPMac.isEmpty()){
						mSmartConfigReceiver.mSmartConfig(failCode,"设备配置超时",mapValue);
					}
					break;
				case SmartConsts.HANFENG_SMARTCONFIG_FAIL:
					if (mSmartConfigReceiver!=null){
						failCode=10000+failCodeType+110;
						mSmartConfigReceiver.mSmartConfig(failCode,"设备配置超时",mapValue);
					}
					break;
			}
		}
	};

	private String ipAddress="";

	@Override
	public void onSmartConnected(String IpAddress, int port, String msg) throws Exception {
		this.ipAddress=IpAddress;
		if(msg.isEmpty()){
			return;
		}
		if (mapValue!=null){
			return;
		}
		mapValue=Mlcc_ParseUtils.getParseObject(msg);
		if (mapValue.containsKey("mac")){
			if ((mapValue.get("mac").toString()).equals(isSdkMac)){
				if(!MacUtils.isMacAddress(isSdkMac)){
					failCode=10000+failCodeType+20;
					mapValue=null;
					return;
				}
				if(!TextUtils.isEmpty(scanMac)&&!scanMac.equals(isSdkMac.toUpperCase())){
					failCode = 10000 + failCodeType + 130;
					mapValue=null;
					return;
				}
				isUDPMac=mapValue.get("mac").toString();
				handler.removeMessages(SmartConsts.HANFENG_SMARTCONFIG_FINSH);
				Log.e("out-error","onSmartConnected,msg="+msg+",mapValue="+mapValue);
				deviceModelId= mapValue.containsKey("model")? (String) mapValue.get("model") :"";
				int checkCode=Mlcc_ParseUtils.isReportKindOrModel(validationModel, kindId, modelId, mapValue);
				if (checkCode==40||checkCode==50){
					mSmartConfigReceiver.mSmartConfigToastError(failCode,"modelId is not match",mapValue);
					mapValue=null;
					return;
				}
				if(checkCode==21){
					firstData=firstDatas.get(index);
					mySmartConnected =new MySmartConnectedThread();
					mySmartConnected.start();
					failCode=10000+failCodeType+90;
				}else {
					failCode = 10000 + failCodeType+ checkCode;
				}
			}
		}


	}

	Map<String,Object> platformResult=null;

	private String platMessage="";

	private int index=0;

	@Override
	public void onFirstConfigDeviceReceiver(String msg) throws Exception {

		if (msg.isEmpty()){
			return;
		}
		platformResult = Mlcc_ParseUtils.getParseObject(msg);
		if ( Mlcc_ParseUtils.getParseObject(msg)==null){
			return;
		}
		if (!platformResult.containsKey("mac")){
			return;
		}
		if (firstDatas.size()<=index){
			return;
		}
		if (platMessage.isEmpty()||!platMessage.equals(msg)){
			if (platformResult.containsKey(SmartConsts.PLATFORM_CODENAME)){
				if (firstData.getContentAck_CodeName().equals(platformResult.get(SmartConsts.PLATFORM_CODENAME).toString())){
					index++;
					firstData = firstDatas.get(index);
					platMessage=msg;
				}
			}
		}


	}

	@Override
	public void onFirstConfigFinish(String msg) throws Exception {
	}

	@Override
	public void smartConfigError(String message) throws Exception {
		if (message.isEmpty()){
			return;
		}
	}

	@Override
	public void onSetWifiAck(String message) throws Exception {
		Log.e("out-error","onSetWifiAck,message="+message);
		isPlatform=false;
		if (mSmartConfigReceiver==null){
			return;
		}
		if (message.isEmpty()){
			return;
		}
		if (message.contains("&Mac")&&!message.contains(isSdkMac)){
			return;
		}
		if (isSdkMac.equals("")){
			return;
		}
		if (message.contains("Result=1")){
			if (mapValue==null){
				mapValue=new HashMap<>();
				mapValue.put("mac",isSdkMac);
			}
			mSmartConfigReceiver.mSmartConfig(1,"success",mapValue);
			initData();
			return;
		}
	}

	private boolean isConnected=false;

	private MySmartConnectedThread mySmartConnected=null;

	private void onDesMySmartConnectedThread(){
		isPlatform=true;
		if (myHfSmartConfigThread!=null){
			myHfSmartConfigThread.interrupt();
			myHfSmartConfigThread=null;
		}
	}

	class  MySmartConnectedThread extends Thread{
		@Override
		public void run() {
			super.run();
			while (!isPlatform){
				try {
					isPlatform=tcpSocketManager.isConnect(ipAddress);
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class  MyHfSmartConfigThread extends Thread{
		int count=0;
		@Override
		public void run() {
			super.run();
			try {
				while (isRun){
					if (count>89){
						isRun=false;
						handler.sendEmptyMessage(SmartConsts.HANFENG_SMARTCONFIG_FAIL);
						return;
					}
					if (isPlatform){
						if (firstData!=null){
                            if(firstData.getContent().contains("CodeName=SetWifi")){
                                failCode=10000+failCodeType+90;
                            }
							tcpSocketManager.isSend(firstData.getContent()+"&port="+SocketManager.Search_Device_Port+"&Mac="+isSdkMac);
                        }
					}
					count++;
					sleep(1000);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

//	class MyClient extends Thread{
//		@Override
//		public void run() {
//			super.run();
//			if (tcpSocketManager!=null){
//				try {
//					isPlatform=tcpSocketManager.isConnect(ipAddress);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	public class SortIndex implements Comparator {
		@Override
		public boolean equals(Object object) {
			return false;
		}
		@Override
		public int compare(Object o1, Object o2) {
			FirstData s1 = (FirstData) o1;
			FirstData s2 = (FirstData) o2;
			if (Integer.parseInt(s1.getIndex()) < Integer.parseInt(s2.getIndex()))
				return 1;
			return 0;
		}
	}

    private void stopConnected(){
        if (snifferSmartLinker != null) {
            snifferSmartLinker.stop();
        }
    }

	/**
	 * 销毁
	 */
	public void onDestory() {
		if (snifferSmartLinker != null) {
			snifferSmartLinker.stop();
		}
		if (isRun) {
			isRun = false;
		}
		if (myHfSmartConfigThread != null) {
			myHfSmartConfigThread.interrupt();
			myHfSmartConfigThread = null;
		}
		if(socketManager!=null){
			socketManager.onDistory();
			socketManager=null;
		}
		stopConnected();
	}
}
