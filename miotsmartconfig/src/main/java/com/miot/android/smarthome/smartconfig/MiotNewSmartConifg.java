package com.miot.android.smarthome.smartconfig;

import android.content.Context;
import android.text.TextUtils;

import com.mediatek.elian.ElianNative;
import com.miot.android.smarthome.callback.DeviceIReceiver;
import com.miot.android.smarthome.callback.MSmartConfigReceiver;
import com.miot.android.smarthome.entity.FirstData;
import com.miot.android.smarthome.lib.SmartConfigAndMulticase;
import com.miot.android.smarthome.lib.SmartConfigAndSmartConfigMulticase;
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
 * Created by Administrator on 2017/2/14 0014.
 */
public class MiotNewSmartConifg implements DeviceIReceiver {


    private static MiotNewSmartConifg instance = null;

    public static MiotNewSmartConifg getInstance(Context context) {
        if (instance == null) {
            synchronized (MiotNewSmartConifg.class) {
                if (instance == null) {
                    instance = new MiotNewSmartConifg(context);
                }
            }
        }
        return instance;
    }


    private SmartConfigAndMulticase smartConfigAndMulticase = null;

    private SmartConfigAndSmartConfigMulticase smart_4004_Config = null;

    private ElianNative elian = null;

    private SocketManager socketManager = null;

    private Context context = null;

    private String kindId = "";

    private String modelId = "";

    private String validationModel = "";

    private String scanMac="";

    private String type = "";

    private MSmartConfigReceiver mSmartConfigReceiver = null;

    private MySmartConfigThread smartConfigThread = null;

    private Map<String, Object> smartConnected = null;

    private boolean isPlatform = false;

    private boolean isComplete = false;

    private boolean isRun = true;

    private String mac = "";

    private String mDeviceReportModelId="";

    private String mDeviceReportMac="";

    private Map<String,Object> mReportMap=null;

    private ArrayList<FirstData> firstDatas = null;

    private FirstData firstData = null;

    private int failCode = 0;

    private volatile int failCodeType = 0;

    private String  ipAddress="";

    public int getFailCode() {
        return failCode;
    }

    public String getMac() {
        return mac;
    }


    public void setmSmartConfigReceiver(MSmartConfigReceiver mSmartConfigReceiver) {
        this.mSmartConfigReceiver = mSmartConfigReceiver;
    }

    private MiotNewSmartConifg(Context context) {
        this.context = context;
    }
    /**
     * 初始化端口信息
     */
    public void init() {

    }

    private void reset() {
        isRun=true;
        smartConnected = null;
        firstData = null;
        index = 0;
        mac = "";
        mDeviceReportModelId="";
        mDeviceReportMac="";

        isPlatform = false;
        isComplete = false;
        platMessage="";

        failCode = 10000 + failCodeType + 10;
        if(smartConnected!=null){
            smartConnected=null;
        }
    }

    private void stopConnected() {
        if (smartConfigAndMulticase != null) {
            smartConfigAndMulticase.isStop();
        }
        if (elian != null) {
            elian.StopSmartConnection();
        }

        if (smart_4004_Config != null) {
            smart_4004_Config.isStop();
        }
    }

    public void startSmartConfig(String routeName, String routePass, String json, String qrcode, String validationModel,String scanMac) throws Exception {

        if (routeName.isEmpty() || json.isEmpty() || qrcode.isEmpty()) {
            throw new Exception("routeName|| json ||qrcode isEmpty");
        }
        if (qrcode.length() < 11) {
            throw new Exception("qrcode length is not 11");
        }
        this.scanMac=scanMac;
        try{
            kindId = String.valueOf(Integer.parseInt(qrcode.substring(1, 4)));
            modelId = String.valueOf(Integer.parseInt(qrcode.substring(4, 8)));
        }catch (Exception e){
        }
        type = qrcode.substring(8, 10);
        if (modelId.equals("284")||modelId.equals("285")) {
            type = "10";
        }
        this.validationModel = validationModel;
        failCode = 0;
        failCodeType = Integer.parseInt(type) * 100;
        firstDatas = (ArrayList<FirstData>) Mlcc_ParseUtils.getFirstData(json);
        if (firstDatas == null) {
            throw new Exception("type is not find");
        }
        firstDatas.add(new FirstData("1000", SmartConsts.PLATFORM_COMPLETE, "fc_complete_ack", "1"));
        Collections.sort(firstDatas, new SortIndex());
        if (type.isEmpty()) {
            throw new Exception("type is empty ");
        }
        switch (type) {
            case "10":
                smartConfigAndMulticase = SmartConfigAndMulticase.getInstance();
                smartConfigAndMulticase.sendData(routeName, routePass);
                break;
            case "20":
                boolean result = ElianNative.LoadLib();
                if (!result) {
                    if (mSmartConfigReceiver != null) {
                        mSmartConfigReceiver.mSmartConfig(12000, "加载lib库失败",null);
                    }
                    return;
                }
                elian = new ElianNative();
                elian.InitSmartConnection("", 1, 0);
                elian.StartSmartConnection(routeName, routePass, "");
                break;
            case "40":
                smart_4004_Config = SmartConfigAndSmartConfigMulticase.getInstance();
                smart_4004_Config.sendData(routeName, routePass);
                break;
        }
        socketManager = SocketManager.getInstance(context);
        socketManager.init(SmartConsts.DEVICE_LOCALHOST_PORT);
        socketManager.setDeviceIReceiver(this);
        reset();
        mReportMap=new HashMap<>();
        smartConfigThread = new MySmartConfigThread();
        smartConfigThread.start();
    }

    class MySmartConfigThread extends Thread {
        int count = 0;

        @Override
        public void run() {
            super.run();
            try {
                while (isRun) {
                    if (count > 89) {
                        isRun = false;
                        mSmartConfigReceiver.mSmartConfig(failCode,"",smartConnected);
                        stopConnected();
                        return;
                    }
                    if (isPlatform) {
                        if (!mac.isEmpty()) {
                            if (firstData != null) {
                                failCode = 10000 + failCodeType + 70;
                                socketManager.send(ipAddress,firstData.getContent() + "&mac=" + mac);
                            }
                        }
                        mSmartConfigReceiver.mSmartConfig(140,"isPlatform",smartConnected);
                    }

                    if (isComplete) {
                        if (!mac.isEmpty()) {
                            socketManager.send(ipAddress,SmartConsts.PLATFORM_FC_COMPLETE_FIN + mac);
                            failCode = 10000 + failCodeType + 80;
                            isComplete = false;
                            if (smartConnected != null) {
                                if (mSmartConfigReceiver != null) {
                                    mSmartConfigReceiver.mSmartConfig(1,"success",smartConnected);
                                    reset();
                                    onDestory();
                                }
                            }
                        }
                    }
                    mReportMap.put("deviceReportModelId",mDeviceReportModelId);
                    mReportMap.put("deviceReportMac",mDeviceReportMac);
                    mSmartConfigReceiver.mSmartConfig(failCode,"failCode report",mReportMap);
                    count++;
                    sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSmartConnected(String IpAddress, int port, String msg) throws Exception {
        if (IpAddress.isEmpty() || port == 0 || msg.isEmpty()) {
            mSmartConfigReceiver.mSmartConfig(110, "address port msg isEmpty",null);
            return;
        }
        this.ipAddress=IpAddress;
        if (smartConnected != null) {
            return;
        }
        smartConnected = Mlcc_ParseUtils.getParseObject(msg);
        if (smartConnected==null){
            return;
        }
        if (smartConnected.containsKey("mac")) {
            mac = smartConnected.get("mac").toString();
            if (!MacUtils.isMacAddress(mac)) {
                failCode = 10000 + failCodeType + 20;
                smartConnected=null;
                return;
            }
            if(!TextUtils.isEmpty(scanMac)&&!scanMac.equals(mac.toUpperCase())){
                failCode = 10000 + failCodeType + 130;
                mDeviceReportMac=mac;
                //mSmartConfigReceiver.mSmartConfigToastError(failCode,"mac is not match",smartConnected);
                smartConnected=null;
                return;
            }
        }
        int checkCode=Mlcc_ParseUtils.isReportKindOrModel(validationModel, kindId, modelId, smartConnected);
        failCode = 10000 + failCodeType + checkCode;
        if (checkCode==30||checkCode==40||checkCode==50||checkCode==60){
            mSmartConfigReceiver.mSmartConfigToastError(failCode,"modelId is not match",smartConnected);
            mDeviceReportModelId=smartConnected.containsKey("model")? (String) smartConnected.get("model") :"";
            smartConnected=null;
            return;
        }
        if(checkCode==21){
            firstData = firstDatas.get(index);
            isPlatform = true;
            failCode = 10000 + failCodeType + 70;
        }else {
            failCode = 10000 + failCodeType + checkCode;
        }
    }

    Map<String, Object> platformResult = null;

    private String platMessage = "";

    private int index = 0;

    @Override
    public void onFirstConfigDeviceReceiver(String msg) throws Exception {
        if (msg.isEmpty()) {
            return;
        }
        platformResult = Mlcc_ParseUtils.getParseObject(msg);
        if (Mlcc_ParseUtils.getParseObject(msg) == null) {
            return;
        }
        if (!platformResult.containsKey("mac")) {
            failCode = 10000 + failCodeType + 120;
            return;
        }
        if (!platformResult.get("mac").toString().equals(mac)) {
            return;
        }
        if (platMessage.isEmpty() || !platMessage.equals(msg)) {
            if (platformResult.containsKey(SmartConsts.PLATFORM_CODENAME)) {
                if (firstData.getContentAck_CodeName().equals(platformResult.get(SmartConsts.PLATFORM_CODENAME).toString())) {
                    index++;
                    firstData = firstDatas.get(index);
                    platMessage = msg;
                }
            }
        }

    }

    @Override
    public void onFirstConfigFinish(String msg) throws Exception {
        if (msg.isEmpty()) {
            return;
        }
        if (!msg.contains(mac)) {
            return;
        }
        Map<String, Object> firstConfigFin = Mlcc_ParseUtils.getParseObject(msg);
        if (firstConfigFin != null) {
            if (firstConfigFin.containsKey(SmartConsts.PLATFORM_CODENAME)) {
                if (firstConfigFin.get(SmartConsts.PLATFORM_CODENAME).toString().
                        equals(SmartConsts.PLATFORM_PLATFORM_CONPLETE_ACK)) {
                    isPlatform = false;
                    isComplete = true;
                }
            }
        }

    }

    @Override
    public void smartConfigError(String message) throws Exception {

    }

    @Override
    public void onSetWifiAck(String message) throws Exception {

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

    /**
     * 销毁
     */
    public void onDestory() {
        if (isRun) {
            isRun = false;
        }
        if (smartConfigThread != null) {
            smartConfigThread.interrupt();
            smartConfigThread = null;
        }
        if(socketManager!=null){
            socketManager.onDistory();
            socketManager=null;
        }
        stopConnected();
    }
}
