package com.miot.android.smarthome.tools;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

import com.miot.android.smarthome.utils.Mlcc_ParseUtils;
import com.miot.commom.network.mlcc.agent.Make;
import com.miot.commom.network.mlcc.agent.Parse;
import com.miot.common.network.mlcc.pojo.response.RespBaseAck;
import com.miot.common.network.mlcc.pojo.response.RespFc_completeAck;
import com.miot.common.network.mlcc.pojo.response.RespSearchAck;
import com.miot.common.network.mlcc.pojo.response.RespSetLinkInfoAck;
import com.miot.common.network.mlcc.pojo.response.RespSetWifiAck;
import com.miot.common.network.mlcc.pojo.response.RespSmartConnectedAck;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MiotlinkTools implements MiotlinkUdpSocket.IReceiver,Miot_HanFeng_And_Iot_udpSocket.Miot_HanFeng_IReceiver{
	public static String tag = Tools.class.getName();

	public static boolean isTest = false;

	private Context context;

	public static MiotlinkTools tool = null;

	public static WifiManager.MulticastLock lock = null;

	public static MiotlinkUdpSocket cuUdpSocket = new MiotlinkUdpSocket();

	public static Miot_HanFeng_And_Iot_udpSocket miot_HanFeng_And_Iot_udpSocket=new Miot_HanFeng_And_Iot_udpSocket();
	
	public static int Search_Pu_Port = 64536;

	public static int Search_Cu_Port = new Random().nextInt(3000) + 30000;

	public static final int MW_WIFI_CONFIG_ACK = 1001;

	public static final int MW_SET_LINK_INFO = 1008;

	public static final int MW_SET_SEARCH_ACK = 1000;

	public static final int FC_FIRST_CONFIG = 1010;

	public static final int SMART_CONNECTED_ACK = 1011;

	public static final int FC_COMPLETE_ACK = 1012;

	public static final int FC_UART_INFO_ACK = 1013;

	public static final int FC_ALL_DATA = 1020;

	public static final int SET_GPIO_ACK = 1021;

	public static Handler fcUartInfoHandker = null;

	public static Handler wificonfighandler = null;

	public static Handler setLinkInfo = null;

	public static Handler searchHandle = null;

	public static Handler fcfirstConfig = null;

	public static Handler smartConnectedHandler = null;

	public static Handler fccompleteHandler = null;

	public static Handler fcAllDataHandler = null;

	public static Handler setGpiohHandler = null;
	/**
	 * 配置的方式
	 */
	public static int configType = 0;

	public static void fcUartInfoHandler(Handler handler) {
		fcUartInfoHandker = handler;
	}

	public static void setWifiHandler(Handler handler) {

		wificonfighandler = handler;
	}

	public static void setSearcHandler(Handler handler) {

		searchHandle = handler;
	}

	public static void setLinkInfo(Handler handler) {
		setLinkInfo = handler;
	}

	public static void fcSmartConnect(Handler handler) {
		fcfirstConfig = handler;
	}

	public static void smartConnectedHandler(Handler handler) {
		smartConnectedHandler = handler;
	}

	public static void fcCompleteHandler(Handler handler) {
		fccompleteHandler = handler;
	}

	public static void fcAllDataHandler(Handler handler) {
		fcAllDataHandler = handler;
	}

	public static void ApsetGpiohHandler(Handler handler) {
		setGpiohHandler = handler;
	}

	public static MiotlinkTools initial(Context context, int code) {
		if (code == 1) {
			if (tool == null) {
				tool = new MiotlinkTools();
				tool.context = context;
				WifiManager manager = (WifiManager) tool.context
						.getSystemService(Context.WIFI_SERVICE);
				lock = manager.createMulticastLock("wifi");
				cuUdpSocket.startRecv(Search_Cu_Port, tool);
			}
		}
		return tool;
	}

	public static boolean MiotSearch() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("CodeName", "Search");
		map.put("ip", "192.168.1.1");
		map.put("port", Search_Cu_Port + "");
		byte[] makeSearch = Make.makeSearch(map);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, makeSearch);
	}

	public static int matchStringInArrayDefault0(String[] sa, String s) {
		if (sa == null || s == null) {
			return 0;
		}
		int i = 0;
		for (i = 0; i < sa.length; i++) {
			if (s.equals(sa[i]))
				break;
		}
		return i >= sa.length ? 0 : i;
	}

	public static void testString(byte[] bs) {
		try {
			String string = new String(bs, "ISO-8859-1");
			System.out.println(string);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 1926731071 青岛服务器 MlDip 114*256*256*256+215*256*256+149*256+63 电信服务器
	 * MlDipI 122.225.196.132
	 * 
	 * @param map
	 *            www.51miaomiao.com 杭州服务器 112.124.115.125 1887204221
	 *            192.168.10.107
	 * 
	 *            192.168.10.200
	 * @return
	 */
	public static boolean MiotFirstConfig(Map<String, String> map) {
		map.put("CodeName", "SetWifi");
		map.put("port", Search_Cu_Port + "");
		map.put("DevName", "MiotLink_M4004T");
		map.put("MLinkIp", "www.51miaomiao.com");
		map.put("MlPort", "28001");
		map.put("MlDip", "1926731071");
		map.put("MlDipI", "2061616260");
		map.put("Mode", "2");
		map.put("ApPd", "");
		map.put("tInfo", 5 + "`" + 9800 + "`" + "192.168.1.100");
		map.put("cInfo", 1 + "`" + 9600 + "`" + "192.168.1.100");
		map.put("UartInfo", null);
		byte[] makeFirst = Make.makeSetWifiAck(map);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, makeFirst);
	}

	public static boolean MiotFirst4004_ap_Config(Map<String, String> map) {
		byte[] makeFirst = Make.makeSetWifiAck(map);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, makeFirst);

	}

	public static boolean MiotFirst4004_AP_Config(String string) {
		byte[] bs = null;
		String code = string + "&port=" + MiotlinkTools.Search_Cu_Port;
		try {
			bs = code.getBytes("ISO-8859-1");
			return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
					"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean MiotSetLinkFirstConfig(Map<String, String> map) {
		map.put("CodeName", "SetWifi");
		map.put("port", Search_Cu_Port + "");
		map.put("cInfo", "1`9600`192.168.1.10");
		byte[] makeFirst = Make.makeSetWifiAck(map);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, makeFirst);
	}

	public static boolean MiotSetLinkInfo(Map<String, String> map) {
		map.put("CodeName", "SetLinkInfo");
		map.put("port", Search_Cu_Port + "");
		map.put("MLinkIp", "www.51miaomiao.com");
		map.put("MlPort", "28001");
		byte[] bs = Make.makeSetLinkInfoAck(map);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);
	}

	public static boolean FcSmartConnected(Map<String, String> map) {
		map.put("CodeName", "fc_ml_platform");
		byte[] bs = Make.makeFcPlatFronAck(map);
		testString(bs);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);
	}

	/**
	 * 7681 完成结束语配置
	 * 
	 * @param map
	 * @return
	 */
	public static boolean Fc_completeAck(Map<String, String> map) {
		map.put("CodeName", "fc_complete");
		byte[] bs = Make.makeFc_completeAck(map);
		testString(bs);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);

	}

	public static boolean FcUartInfoAck(Map<String, String> map) {
		map.put("CodeName", "fc_uart");
		byte[] bs = Make.makeFcUartInfoAck(map);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);
	}

	/**
	 * 发送广播的最终完成
	 * 
	 * @param map
	 * @return
	 */
	public static boolean Fc_completeFinAck(Map<String, String> map) {
		map.put("CodeName", "fc_complete_fin");
		byte[] bs = Make.makeFc_completeFinAck(map);
		testString(bs);
		return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
				"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);

	}

	public static boolean SmartConnectedAck(String uart) {
		try {
			byte[] bs = uart.getBytes("ISO-8859-1");
			// testString(bs);
			return MiotlinkTools.sendUdp(MiotlinkTools.cuUdpSocket,
					"255.255.255.255", MiotlinkTools.Search_Pu_Port, 1, bs);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 加密方式
	 */
	public static byte[] encrypt(byte[] src) {
		for (int i = 8; i < src.length; i++) {
			src[i] ^= src[0];
		}
		return src;
	}

	/**
	 * 发送lssccmd 内容的解析类
	 * 
	 * @param content
	 * @return
	 */
	public static byte[] formatLsscCmdBuffer(byte[] content) {
		byte[] bs = null;
		try {
			// bsContent = content;
			int packageLen = content.length + 20;
			int contentlen = content.length + 12;
			bs = new byte[packageLen];
			bs[0] = (byte) 0x30;
			bs[1] = (byte) 104;
			bs[2] = (byte) (packageLen >> 8 & 0xff);// (packLen/256); // //
			bs[3] = (byte) (packageLen >> 0 & 0xff);// (packLen%256); // //
			bs[4] = (byte) (0 >> 24 & 0xff);
			bs[5] = (byte) (0 >> 16 & 0xff);
			bs[6] = (byte) (0 >> 8 & 0xff);
			bs[7] = (byte) (0 >> 0 & 0xff);
			bs[8] = (byte) 0x65;
			bs[9] = (byte) 0;
			bs[10] = (byte) (contentlen / 256);
			bs[11] = (byte) (contentlen % 256);
			bs[12] = (byte) (1 >> 24 & 0xff);
			bs[13] = (byte) (1 >> 16 & 0xff);
			bs[14] = (byte) (1 >> 8 & 0xff);
			bs[15] = (byte) (1 >> 0 & 0xff);
			bs[16] = (byte) (0 >> 24 & 0xff);
			bs[17] = (byte) (0 >> 16 & 0xff);
			bs[18] = (byte) (0 >> 8 & 0xff);
			bs[19] = (byte) (0 >> 0 & 0xff);
			System.arraycopy(content, 0, bs, 20, content.length);
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[] {};
		}
		return encrypt(bs);
	}

	public static boolean sendUdp(MiotlinkUdpSocket udpSocket, String ip,
			int port, int code, byte[] content) {
		boolean success = false;
		try {
			byte[] bs = formatLsscCmdBuffer(content);
			success = udpSocket.send(ip, port, bs, bs.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;

	}

	public static String Charset = "ISO-8859-1";

	public static String getMlccContent(byte[] bs, int len) {
		try {
			if (bs == null || len < 20) {
				return null;
			}
			return new String(bs, 20, len - 20, Charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String bytesToHexString(byte[] src, int len) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0 || len > src.length) {
			return null;
		}
		for (int i = 0; i < len; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 没有加密类型
	 * 
	 * @param version
	 * @param code
	 * @param content
	 * @return
	 */

	RespBaseAck respBaseAck = null;

	@Override
	public void onReceive(int localPort, String host, int port, byte[] src,
			int len) {
		try {
			byte[] bs = encrypt(src);
			String msg = MiotlinkTools.getMlccContent(bs, len);
			try {
				respBaseAck = Parse.parseMLCCPackage(msg.getBytes(), 0,
						msg.getBytes().length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, String> map = null;
			if (respBaseAck != null) {
				map = respBaseAck.getResultMap();
			}
			Message m = new Message();
			if (localPort == Search_Cu_Port) {
				if (respBaseAck instanceof RespSetWifiAck) {
					m.what = MW_WIFI_CONFIG_ACK;
					m.obj = map;
					if (wificonfighandler != null)
						wificonfighandler.sendMessage(m);
				} else if (respBaseAck instanceof RespSetLinkInfoAck) {
					m.what = MW_SET_LINK_INFO;
					m.obj = map;
					if (setLinkInfo != null)
						setLinkInfo.sendMessage(m);
				} else if (respBaseAck instanceof RespSearchAck) {
					m.what = MW_SET_SEARCH_ACK;
					m.obj = map;
					if (searchHandle != null)
						searchHandle.sendMessage(m);
				} else if (Mlcc_ParseUtils.isSmartConnected(msg)) {
					try {
						map = Mlcc_ParseUtils.getParse(msg);
						m.what = FC_FIRST_CONFIG;
						m.obj = map;
						if (fcfirstConfig != null)
							fcfirstConfig.sendMessage(m);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (msg.split("&")[0] == null
							|| msg.split("&")[0].equals("")
							|| !msg.split("&")[0].split("=")[0]
									.equals("CodeName")) {
						return;
					}
					m.what = FC_ALL_DATA;
					m.obj = msg;
					if (fcAllDataHandler != null)
						fcAllDataHandler.sendMessage(m);
				}
			} else if (localPort == 64535) {
				if (respBaseAck != null
						&& respBaseAck instanceof RespSmartConnectedAck) {
					m.what = FC_FIRST_CONFIG;
					m.obj = map;
					if (fcfirstConfig != null)
						fcfirstConfig.sendMessage(m);
				} else if (respBaseAck != null
						&& respBaseAck instanceof RespFc_completeAck) {
					m.what = FC_COMPLETE_ACK;
					m.obj = map;
					if (fccompleteHandler != null)
						fccompleteHandler.sendMessage(m);
				} else if (Mlcc_ParseUtils.isSmartConnected(msg)) {
					try {
						map = Mlcc_ParseUtils.getParse(msg);
						m.what = FC_FIRST_CONFIG;
						m.obj = map;
						if (fcfirstConfig != null)
							fcfirstConfig.sendMessage(m);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						if (msg.split("&")[0] == null
								|| msg.split("&")[0].equals("")
								|| !msg.split("&")[0].split("=")[0]
										.equals("CodeName")) {
							return;
						}
						m.what = FC_ALL_DATA;
						m.obj = msg;
						if (fcAllDataHandler != null)
							fcAllDataHandler.sendMessage(m);
					} catch (Exception e) {
					}
				}

			}
		} catch (Exception e) {

		}
	}

	@Override
	public void miot_onReceive(int localPort, String host, int port, byte[] src,
			int len) {
		byte[] bs = encrypt(src);
		String msg = MiotlinkTools.getMlccContent(bs, len);
		Map<String, String> map = null;
		Message m = new Message();
		if (Mlcc_ParseUtils.isSmartConnected(msg)) {
			try {
				map = Mlcc_ParseUtils.getParse(msg);
				if (map!=null) {
					m.what = FC_FIRST_CONFIG;
					m.obj = map;
					if (fcfirstConfig != null)
						fcfirstConfig.sendMessage(m);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void onDistory(){
		try {
			miot_HanFeng_And_Iot_udpSocket.onDistory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
