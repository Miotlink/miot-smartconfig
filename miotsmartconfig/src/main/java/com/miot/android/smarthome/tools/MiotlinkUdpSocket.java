package com.miot.android.smarthome.tools;

import android.util.Log;

import com.miot.android.smarthome.protocol.UdpSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class MiotlinkUdpSocket implements Runnable{

	public static String TAG = UdpSocket.class.getName();

	public interface IReceiver {
		public void onReceive(int localPort, String host, int port, byte[] bs,
							  int len);
	}

	private IReceiver receiver = null;

	private DatagramSocket socket = null;

	private Thread thread = null;

	int localPort = 0;

	public boolean startRecv(int port, IReceiver lrs) {
		try {
			localPort = port;
//			System.out.println("---监听端口-"+localPort);
			if(socket==null){
				socket = new DatagramSocket(null);
				socket.setReuseAddress(true);
				socket.bind(new InetSocketAddress(localPort));
				}
			receiver = lrs;
			thread = new Thread(this);
			thread.start();
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return false;
	}

	public boolean needStop = false;

	private InetAddress address = null;

	private DatagramSocket dSocket = null;

	private DatagramPacket dPacket = null;

	public boolean send(String ip, int port, byte[] bs, int len) {
		Log.i(TAG, "send: " + ip + ":" + port + " [" + new String(bs) + "]");
		try {
			address = InetAddress.getByName(ip);
			Log.d(TAG, "send: 已找到服务器,连接中...");
		} catch (UnknownHostException e) {
			Log.e(TAG, "send: 未找到服务器.");
			e.printStackTrace();
			return false;
		}
		dPacket = new DatagramPacket(bs, len, address, port);
		try {
			 MiotlinkTools.lock.acquire();
			 socket.send(dPacket);
			 MiotlinkTools.lock.release();

			Log.d(TAG, "send: 消息发送成功!");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "send: 消息发送失败.");
			return false;
		}
		return true;

	}

	private void logSendData(byte[] bs) {
		String data = "";
		for (int i = 0; i < bs.length; i++) {
			data += bs[i] + " ";
		}
		System.out.println(data);
	}
	@Override
	public void run() {
		if (socket == null || receiver == null) {
			Log.e(TAG, "run: socket and receiver should not be null!");
			return;
		}
		while (!needStop) {
			byte data[] = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
				String msg = new String(packet.getData(), packet.getOffset(),
						packet.getLength());
				byte[] bs = new byte[packet.getLength()];

				System.arraycopy(packet.getData(), packet.getOffset(), bs, 0,
						packet.getLength());
				String host = packet.getAddress().getHostAddress();
				int port = packet.getPort();
				receiver.onReceive(localPort, host, port, bs, bs.length);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

}
