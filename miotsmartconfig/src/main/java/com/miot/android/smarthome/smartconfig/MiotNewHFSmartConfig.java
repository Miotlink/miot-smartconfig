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
import com.miot.android.smarthome.callback.DeviceIReceiver;
import com.miot.android.smarthome.callback.MSmartConfigReceiver;
import com.miot.android.smarthome.entity.FirstData;
import com.miot.android.smarthome.manager.SocketManager;
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
public class MiotNewHFSmartConfig implements OnSmartLinkListener,DeviceIReceiver {
	private static MiotNewHFSmartConfig instance=null;

	public static MiotNewHFSmartConfig getInstance(Context context) {
		if (instance==null){
			synchronized (MiotNewSmartConifg.class){
				if (instance==null){
					instance=new MiotNewHFSmartConfig(context);
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

	private SocketManager socketManager=null;

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

	private String mDeviceReportModelId="";

	private String mDeviceReportMac="";

	private Map<String,Object> mReportMap=null;

	private MiotNewHFSmartConfig(Context context){
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
		mReportMap=new HashMap<>();
		socketManager=SocketManager.getInstance(context);
		socketManager.setDeviceIReceiver(this);
		socketManager.init(SmartConsts.DEVICE_LOCALHOST_PORT);
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
		isPlatform=false;
		ipAddress="";
		mDeviceReportModelId="";
		mDeviceReportMac="";
		failCode=10000+failCodeType+110;
		if(mapValue!=null){
			mapValue=null;
		}
	}

	@Override
	public void onLinked(SmartLinkedModule smartLinkedModule) {
		isSdkMac=  MacUtils.MakeMac(smartLinkedModule.getMac());
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
							mDeviceReportMac=isSdkMac;
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
					onDestory();
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
					mDeviceReportMac=isSdkMac;
					mapValue=null;
					return;
				}
				isUDPMac=mapValue.get("mac").toString();
				handler.removeMessages(SmartConsts.HANFENG_SMARTCONFIG_FINSH);
				Log.e("out-error","onSmartConnected,msg="+msg+",mapValue="+mapValue);
				int checkCode=Mlcc_ParseUtils.isReportKindOrModel(validationModel, kindId, modelId, mapValue);
				if (checkCode==30||checkCode==40||checkCode==50||checkCode==60){
					mDeviceReportModelId=mapValue.containsKey("model")? (String) mapValue.get("model") :"";
					mSmartConfigReceiver.mSmartConfigToastError(failCode,"modelId is not match",mapValue);
					mapValue=null;
					return;
				}
				if(checkCode==21){
					firstData=firstDatas.get(index);
					isPlatform=true;
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
		Log.e("out-error","onFirstConfigDeviceReceiver:msg="+msg);
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
			onDestory();
			return;
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
							socketManager.send(ipAddress,firstData.getContent()+"&port="+SocketManager.Search_Device_Port+"&Mac="+isSdkMac);
                        }
						mSmartConfigReceiver.mSmartConfig(140,"isPlatform",mapValue);
					}
					mReportMap.put("deviceReportModelId",mDeviceReportModelId);
					mReportMap.put("deviceReportMac",mDeviceReportMac);
					mSmartConfigReceiver.mSmartConfig(failCode,"failCode report",mReportMap);
					count++;
					sleep(1000);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

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
