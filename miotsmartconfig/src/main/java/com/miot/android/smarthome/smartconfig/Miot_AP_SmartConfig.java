package com.miot.android.smarthome.smartconfig;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.miot.android.smarthome.callback.MSmartConfigReceiver;
import com.miot.android.smarthome.callback.MiotWiFiCallback;
import com.miot.android.smarthome.entity.FirstData;
import com.miot.android.smarthome.receiver.NetworkBroadcastReceiver;
import com.miot.android.smarthome.tools.MiotlinkTools;
import com.miot.android.smarthome.utils.Mlcc_ParseUtils;
import com.miot.android.smarthome.utils.SmartConsts;
import com.miot.android.smarthome.wifi.WifiAdmin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/13 0013.
 */
public class Miot_AP_SmartConfig implements MiotWiFiCallback {


    public static Miot_AP_SmartConfig instance = null;

    private Context context;

    private WifiAdmin wifiAdmin = null;

    private String route = "";

    private String password = "";

    private String miotlink_ap = "";

    private boolean isRuning = true;

    private boolean isPlatform = false;

    private int index = 0;

    private volatile int failCode = 10010;

    private String configAck = "";

    private String mac = "";

    private boolean isRouteCheck = false;

    private List<FirstData> firstDatas = null;

    private String fcResult = "";

    private String lastResult = "";

    private boolean isSetMiotAp = false;

    public int getFailCode() {
        return failCode;
    }

    public String getMac() {
        return mac;
    }

    public static Miot_AP_SmartConfig getInstance(Context context) {
        if (instance == null) {
            synchronized (Miot_AP_SmartConfig.class) {
                if (instance == null) {
                    instance = new Miot_AP_SmartConfig(context);
                }
            }
        }
        return instance;
    }


    public Miot_AP_SmartConfig(Context context) {
        this.context = context;
        wifiAdmin = new WifiAdmin(context);

    }

    private MSmartConfigReceiver mSmartConfigReceiver = null;

    public void setMSmartConfigReceiver(MSmartConfigReceiver callBack) {
        this.mSmartConfigReceiver = callBack;
    }


    public boolean isSmartConfig(String route, String password, String miotlink_ap, String mac, String json) {
        initData();
        wifiAdmin.startScan();
        this.route = route;
        this.password = password;
        this.miotlink_ap = miotlink_ap;
        this.mac = mac;
        if (json.equals("")) {
            return false;
        }
        firstDatas = Mlcc_ParseUtils.getFirstData(json);
        if (firstDatas.size() <= 0) {
            return false;
        }
        wifiAdmin.addNetwork(wifiAdmin.createWifiInfo(miotlink_ap, "", 0));
        myThread = new MyThread();
        myThread.start();
        failCode = 10010;
        return true;
    }

    private MyThread myThread = null;

    private void initData() {
        NetworkBroadcastReceiver.callback = this;
        MiotlinkTools.initial(context, 1);
        MiotlinkTools.setWifiHandler(handler);
        MiotlinkTools.fcAllDataHandler(handler);
        isRuning = true;
        isPlatform = false;
        isSetMiotAp=false;
        index = 0;
        configAck = "";
        mac = "";
        isRouteCheck = false;
        fcResult = "";
        lastResult = "";
    }

    @Override
    public void isCheck(boolean isWiFI, boolean isMoble, String ssid) {
        if (isWiFI) {
            if (ssid.equals(miotlink_ap)) {
                isPlatform = true;
                isSetMiotAp = true;
                failCode = 10090;
                Log.e("mitlin_ap", "success");
            } else {
                Log.e("mitlin_route", "success");
                if (!isRouteCheck && isSetMiotAp) {
                    isRouteCheck = true;
                    handler.sendEmptyMessage(SmartConsts.SMARTCONFIH_AP_SUCCESS);
                }
            }
        }
    }

    class MyThread extends Thread {
        int count = 0;
        int time = 1000;

        @Override
        public void run() {
            try {
                while (isRuning) {
                    if (count >= 89) {
                        handler.sendEmptyMessage(SmartConsts.SMARTCONFIG_AP_FAIL);
                        isRuning=false;
                        mSmartConfigReceiver.mSmartConfig(failCode,"failCode report",null);
                        return;
                    }
                    if (isPlatform) {
                        failCode = 10090;
                        FirstData firstData = firstDatas.get(index);
                        if (firstData != null) {
                            String string = firstData.getContent().replace("&amp", "&");

                            String[] codeNames = string.split("&");
                            String code="";
                            if (!codeNames[0].equals("CodeName=SetWifi")) {
                                MiotlinkTools.MiotFirst4004_AP_Config(string + "&Mac=" + mac);
                                configAck = firstData.getContentAck_CodeName();
                            } else {
                                if (string.endsWith("&")){
                                    code = "Mac=" + mac + "&ByName=" + miotlink_ap
                                            + "&ApId=" + miotlink_ap + "&StaId=" + route
                                            + "&StaPd=" + password;
                                }else{
                                    code = "&Mac=" + mac + "&ByName=" + miotlink_ap
                                            + "&ApId=" + miotlink_ap + "&StaId=" + route
                                            + "&StaPd=" + password;
                                }
                                MiotlinkTools.MiotFirst4004_AP_Config(string + code);
                                configAck = firstData.getContentAck_CodeName();
                            }
                        }
                        mSmartConfigReceiver.mSmartConfig(140,"isPlatform",null);
                    }
                    mSmartConfigReceiver.mSmartConfig(failCode,"failCode report",null);
                    count++;
                    sleep(time);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.run();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MiotlinkTools.MW_WIFI_CONFIG_ACK:
                    @SuppressWarnings("unchecked")
                    Map<String, String> map = (HashMap<String, String>) msg.obj;
                    isPlatform = false;
                    isRouteCheck = false;
                    handler.sendEmptyMessage(SmartConsts.SMARTCONFIG_AP_CONNECTED_WIFI);
                    failCode = 10090;
                    break;

                case MiotlinkTools.FC_ALL_DATA:
                    fcResult = msg.obj.toString().split("&")[0].split("=")[1].toString();
                    if (fcResult.equals("") || fcResult == null) {
                        return;
                    } else {
                        FirstData firstData = firstDatas.get(index);
                        if (fcResult == null) {
                            return;
                        } else {
                            if (!fcResult.equals(lastResult) || lastResult.equals("")) {
                                if (fcResult.equals(firstData.getContentAck_CodeName())) {
                                    if ((1 + index) == firstDatas.size()) {
                                        isPlatform = false;
                                    } else {
                                        index++;
                                        lastResult = fcResult;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case SmartConsts.SMARTCONFIG_AP_CONNECTED_WIFI:
                    connectWifiState();
                    break;
                case SmartConsts.SMARTCONFIH_AP_SUCCESS:
                    Map<String, Object> mapValue = new HashMap<>();
                    mapValue.put("mac", mac);
                    if (mSmartConfigReceiver != null) {
                        mSmartConfigReceiver.mSmartConfig(1,"success",mapValue);
                    }
                    break;

            }
        }

        ;
    };

    private void connectWifiState() {
        wifiAdmin.connectConfiguration(wifiAdmin.getConfigurationIndexBySSID(route));
    }

    public void stopSmartConfig() {
        NetworkBroadcastReceiver.callback = null;
        MiotlinkTools.setWifiHandler(null);
        MiotlinkTools.fcAllDataHandler(null);
        if (isRuning) {
            isRuning = false;
        }
        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
    }

}
