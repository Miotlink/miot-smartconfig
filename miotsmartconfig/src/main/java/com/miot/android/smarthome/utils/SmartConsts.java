package com.miot.android.smarthome.utils;

/**
 * Created by Administrator on 2017/9/7 0007.
 */
public class SmartConsts {

	/*************************************SMARTCONFIG*******************************************/
	public static final int SMARTCONFIG_CONFIG_UDP_TIME=10;

	public static final int SMARTCONFIG_FIRST_SIGN_LEN=30;

	public static final int SMARCONFIG_FIRST_SIGN=20;

	public static final int SMARCONFIG_THIRD_SIGN=22;

	public static final int SMARCONFIG_TWO_NUMBER_SGIN=3;

	public static final int SMARTCONFIG_TWO_SGIN=21;

	public static final String SMART_FIRST_SGIN=HexString.randomNumStr(SMARCONFIG_FIRST_SIGN);

	public static final String SMART_TWO_SGIN=HexString.randomNumStr(SMARTCONFIG_TWO_SGIN);

	public static final String SMART_THIRD_SGIN=HexString.randomNumStr(SMARCONFIG_THIRD_SIGN);

	public static final String AP = "0";
	public static final String SA_4004_1 = "1";
	public static final String SA_7681 = "2";
	public static final String SA_HANFENG = "3";
	public static final String SA_4004_2 = "4";
	public static final String SIM = "5";
	public static final String SA_LWXIN = "6";
	public static final String MIOTLINK_IPC = "A";
	/********************************汉枫调用*************************************/
	public static final String PLATFORM_CODENAME="CodeName";

	public static final int HANFENG_SMARCONNECTED=300001;

	public static final int HANFENG_SMARTCONFIG_TIMEOUT=300003;

	public static final int HANFENG_SMARTCONFIG_FINSH=300002;

	public static final int HANFENG_SMARTCONFIG_FAIL=300004;
//////////////////////////////////////////SA 7681//////////////////////////////////////////////////
	public static final String PLATFORM_COMPLETE = "CodeName=fc_complete";

	public static final String PLATFORM_PLATFORM_CONPLETE_ACK = "fc_complete_ack";

	public static final String PLATFORM_FC_COMPLETE_FIN="CodeName=fc_complete_fin&mac=";
	public static final String PLATFORM_FC_PLATFORM_ACK="fc_ml_platform_ack";
	public static final String PLATFORM_FC_PLATFORM="CodeName=fc_ml_platform&pf_url=www.51miaomiao.com&pf_port=28001&pf_ip1=118.190.67.214&pf_ip2=122.225.196.132&";

	public static final int DEVICE_LOCALHOST_PORT = 64535;
	public static final int DEVICE_VOIDE_LOCALHOST_PORT = 63541;
	////////////////////////////////////////AP///////////////////////////////////////////////
	public static final int SMARTCONFIH_AP_SUCCESS=5050;

	public static final int SMARTCONFIG_AP_FAIL=5000;

	public static final int SMARTCONFIG_AP_CONNECTED_WIFI=5011;






}
