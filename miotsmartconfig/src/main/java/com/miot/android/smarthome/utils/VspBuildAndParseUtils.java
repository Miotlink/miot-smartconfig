package com.miot.android.smarthome.utils;


import com.miot.android.smarthome.protocol.UdpSocket;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/2/14 0014.
 */
public class VspBuildAndParseUtils {

	public static final int VERSION = 0x30;
	public static final int INVALID_ID = -1;
	public static final int NONE_SES_ID = 0;
	public static final String LOCALHOST_DEVICE="255.255.255.255";
	public static final int Search_Pu_Port = 64536;
	public static String Charset = "ISO-8859-1";

	private final static int version = VERSION;
	private final static int code =INVALID_ID;
	private final static int sesId =NONE_SES_ID;
	private final static int length = 8;


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

	public static boolean send(UdpSocket udpSocket, String ip, int port, byte[] content) {
		boolean success = false;
		try {
			byte[] bs = formatLsscCmdBuffer(content);
			success = udpSocket.send(ip, port, bs, bs.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;

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


	public static byte[] bulidVspTtContent(byte [] content,int contentLen){
		byte[] bs = null;

		return bs;
	}



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
}
