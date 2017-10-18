package com.miot.android.smarthome.manager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.miot.android.smarthome.callback.DeviceIReceiver;
import com.miot.android.smarthome.callback.IReceiver;
import com.miot.android.smarthome.callback.SmartVoiceIReceiver;
import com.miot.android.smarthome.protocol.UdpSocket;
import com.miot.android.smarthome.utils.Mlcc_ParseUtils;
import com.miot.android.smarthome.utils.SmartConsts;
import com.miot.android.smarthome.utils.VspBuildAndParseUtils;

import java.util.Random;

/**
 * 监听设备返回的数据
 */
public class SocketManager implements IReceiver {
    public static int Search_Cu_Port = new Random().nextInt(3000) + 30000;
    public static int Search_Device_Port = 64535;
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    private static SocketManager instance = null;

    public static WifiManager.MulticastLock lock = null;

    public static SocketManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SocketManager.class) {
                if (instance == null) {
                    instance = new SocketManager(context);
                }
            }
        }
        return instance;
    }

    private static Context context = null;

    private DeviceIReceiver deviceIReceiver = null;

    private static UdpSocket udpSocket = new UdpSocket();

    private SocketManager(Context context) {
        this.context = context;
        WifiManager manager = (WifiManager) SocketManager.context.getSystemService(Context.WIFI_SERVICE);
        lock = manager.createMulticastLock("wifi");
    }

    private SmartVoiceIReceiver iReceiver=null;

    public void setiReceiver(SmartVoiceIReceiver iReceiver) {
        this.iReceiver = iReceiver;
    }

    public void setDeviceIReceiver(DeviceIReceiver deviceIReceiver) {
        this.deviceIReceiver = deviceIReceiver;
    }

    /**
     * 初始化 监听端口
     *
     * @param port
     */
    public void init(int port) throws Exception {
        udpSocket.startRecv(port, this);
    }

    public void onDistory() {
        if (udpSocket != null) {
            udpSocket.onStop();
        }
    }

    public boolean send(String ipAddress,String deviceConfig) throws Exception {
        if (deviceConfig.isEmpty()) {
            throw new Exception("configData is empty");
        }
        if (ipAddress.equals("")){
            ipAddress=VspBuildAndParseUtils.LOCALHOST_DEVICE;
        }
        Log.e("error",deviceConfig);
        byte[] bytes = deviceConfig.getBytes(DEFAULT_CHARSET);
        return VspBuildAndParseUtils.send(udpSocket, ipAddress, VspBuildAndParseUtils.Search_Pu_Port, bytes);
    }

    public static void acquire(){
        if(lock!=null){
            lock.acquire();
        }
    }

    public static void release(){
        try {
            if(lock!=null&&lock.isHeld()){
                lock.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (lock!=null&&lock.isHeld()){
                lock.release();
            }
        }
    }

    @Override
    public void onReceive(int localPort, String IpAddress, int port, byte[] src, int len) {
        try {
            byte[] bytes = VspBuildAndParseUtils.encrypt(src);
            String msg = VspBuildAndParseUtils.getMlccContent(bytes, len);
            Log.e("onReceiver",msg);
            if (deviceIReceiver == null) {
                throw new Exception("Not init interface DeviceIReceiver");
            }
            if (localPort == Search_Device_Port) {
                if (Mlcc_ParseUtils.isSmartConnected(msg)) {
                    deviceIReceiver.onSmartConnected(IpAddress, port, msg);
                    return;
                }
                if (Mlcc_ParseUtils.isSetWifiAck(msg)) {
                    deviceIReceiver.onSetWifiAck(msg);
                    return;
                }
                if (!Mlcc_ParseUtils.isPlatformFinishAck(msg)) {
                    deviceIReceiver.onFirstConfigDeviceReceiver(msg);
                    return;
                }
                deviceIReceiver.onFirstConfigFinish(msg);
                return;
            }else if (localPort== SmartConsts.DEVICE_VOIDE_LOCALHOST_PORT){
                if (iReceiver!=null){
                    iReceiver.onVoiceReceiver(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (deviceIReceiver != null) {
                try {
                    deviceIReceiver.smartConfigError("未知异常");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }
}
