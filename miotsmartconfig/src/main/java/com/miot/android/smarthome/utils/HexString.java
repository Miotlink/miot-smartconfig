package com.miot.android.smarthome.utils;

import java.util.Random;

public class HexString {

	private static Random strGen = new Random();;
	private static Random numGen = new Random();;
	private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();;
	private static char[] numbers = ("0123456789").toCharArray();;
	private static String[] hexs = new String[] { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	private static String[] bins = new String[] { "0000", "0001", "0010",
			"0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010",
			"1011", "1100", "1101", "1110", "1111" };

	/**
	 * byte 转 16
	 * 
	 * @param src
	 * @param len
	 * @return
	 */
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

	// 将十六进制数hex转换为二进制数并返回
		public static String convertHexToBin(String hex) {
			StringBuffer buff = new StringBuffer();
			int i;
			for (i = 0; i < hex.length(); i++) {
				buff.append(getBin(hex.substring(i, i + 1)));
			}
			return buff.toString();
		}

		// 将二进制数bin转换为十六进制数并返回
		public static String convertBinToHex(String bin) {
			StringBuffer buff = new StringBuffer(bin);
			int i;
			if (bin.length() % 4 != 0) {// 左补零
				for (i = 0; i < (4 - bin.length() % 4); i++) {
					buff.insert(0, "0");
				}
			}
			bin = buff.toString();
			buff = new StringBuffer();

			for (i = 0; i < bin.length(); i += 4) {
				buff.append(getHex(bin.substring(i, i + 4)));
			}
			return buff.toString();
		}

		// 返回十六进制数的二进制形式
		private static String getBin(String hex) {
			int i;
			for (i = 0; i < hexs.length && !hex.toLowerCase().equals(hexs[i]); i++)
				;
			return bins[i];
		}

		// 返回二进制数的十六进制形式
		private static String getHex(String bin) {
			int i;
			for (i = 0; i < bins.length && !bin.equals(bins[i]); i++)
				;
			return hexs[i];
		}



	/** * 产生随机字符串 * */
	public static final String randomString(int length) {
		if (length < 1) {
			return null;
		}
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[strGen.nextInt(61)];
		}
		return new String(randBuffer);
	}

	/** * 产生随机数值字符串 * */
	public static final String randomNumStr(int length) {
		if (length < 1) {
			return null;
		}
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbers[numGen.nextInt(9)];
		}
		return new String(randBuffer);
	}


}
