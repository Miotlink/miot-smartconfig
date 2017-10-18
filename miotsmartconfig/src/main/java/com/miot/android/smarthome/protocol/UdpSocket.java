package com.miot.android.smarthome.protocol;

import android.util.Log;

import com.miot.android.smarthome.callback.IReceiver;
import com.miot.android.smarthome.manager.SocketManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UdpSocket implements Runnable {

    public static String TAG = UdpSocket.class.getName();

    private IReceiver receiver = null;

    private DatagramSocket socket = null;

    private Thread thread = null;

    private int localPort = 0;

    public boolean startRecv(int port, IReceiver lrs) {
        try {
            this.receiver = lrs;
            needStop = false;
            localPort = port;
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(localPort));
            thread = new Thread(this);
            thread.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onStop() {
        if (!needStop) {
            needStop = true;
        }
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket.disconnect();
                socket = null;
            }
        }
    }

    public boolean needStop = false;

    private InetAddress address = null;

    private DatagramPacket dPacket = null;

    public boolean send(String ip, int port, byte[] bs, int len) {
        Log.e(TAG, "send: " + ip + ":" + port + " [" + new String(bs) + "]");
        if(socket==null){
            return false;
        }
        try {
            address = InetAddress.getByName(ip);
            dPacket = new DatagramPacket(bs, len, address, port);
            SocketManager.acquire();
            socket.send(dPacket);
            SocketManager.release();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            SocketManager.release();
            return false;
        }catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "send: 消息发送失败.");
            SocketManager.release();
            return false;
        }
        return true;

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
                SocketManager.acquire();
                socket.receive(packet);
                SocketManager.release();
                byte[] bs = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), bs, 0, packet.getLength());
                String host = packet.getAddress().getHostAddress();
                int port = packet.getPort();

                if (receiver != null) {
                    receiver.onReceive(localPort, host, port, bs, bs.length);
                }
            } catch (IOException e) {

                e.printStackTrace();
                SocketManager.release();
            }

        }
    }

}
