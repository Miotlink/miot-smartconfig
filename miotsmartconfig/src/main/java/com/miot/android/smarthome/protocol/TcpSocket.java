package com.miot.android.smarthome.protocol;




import com.miot.android.smarthome.callback.TcpIReciver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class TcpSocket implements Runnable {

    private static final int VSP_SEND_DATA_FAIL = 5102;
    private static final int VSP_SEND_DATA_SUCCESS = 5101;
    public static String tag = TcpSocket.class.getName();
    public static String DefaultCharSetName = "ISO-8859-1"; // "UTF-8"; //



    public TcpSocket() {

    }



    protected void finalize() {
        this.disconnect();
    }

    public boolean hasConnect() {
        return sc.isConnected();
    }

    private TcpIReciver tsr = null;

    public void setTsr(TcpIReciver tsr) {
        this.tsr = tsr;
    }

    private Socket sc = null;
    private InputStream is = null;
    private DataOutputStream os = null;

    private Thread recvThread = null;
    boolean isRunning = false;
    byte[] recvBuff = new byte[10240];

    private String ip = null;

    private int port = 0;


    public boolean send(String sendString) {
        try {
            return send(sendString.getBytes(DefaultCharSetName),
                    sendString.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Socket getSc() {
        return sc;
    }

    public boolean send(byte[] sendBuff, int length) {
        try {
            synchronized (this) {
                if (sc != null && os != null) {
                    os.write(sendBuff, 0, length);
                    os.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean isConnect = false;

    private int i = 0;


    public boolean connect(String strServerIP, int nPort) {
        try {
            this.ip = strServerIP;
            this.port = nPort;
            sc = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(strServerIP, nPort);

            sc.connect(socketAddress, 5000);
            isConnect = true;
            is = sc.getInputStream();
            os = new DataOutputStream(sc.getOutputStream());
            recvThread = new Thread(null, this);
            recvThread.start();

        } catch (UnknownHostException e) {
            isConnect = false;

            return false;
        } catch (IOException e) {
            isConnect = false;

            return false;
        } catch (Exception e) {

            isConnect = false;
            return false;
        }
        return true;
    }

    void finnalize() {
        this.disconnect();
    }

    public void disconnect() {
        isConnect = false;
        isRunning = false;
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (sc != null) {
                if (sc.isConnected()) {
                    sc.shutdownInput();
                    synchronized (this) {
                        sc.shutdownOutput();
                        sc.close();
                    }
                }
                sc = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is = null;
            os = null;
            sc = null;
        }
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            try {
                int recvLen = is.read(recvBuff);
                if (recvLen < 0) {
                    Thread.sleep(2);
                } else {
                    if (tsr != null) {
                        tsr.onReceive(recvBuff, recvLen);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isSendData() {
        try {
            sc.sendUrgentData(0xFF);
            return true;
        } catch (Exception ex) {
            isRunning = false;
            ex.printStackTrace();
            return false;
        }
    }

}