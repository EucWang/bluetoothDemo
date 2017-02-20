package demo.wxn.cn.demobluetooth.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.UUID;

/**
 * Created by wangxn on 2017/2/20.
 */

public class ClientThread extends BasicThread {

    private static final String TAG = "ClientThread";
    private BluetoothDevice mmDevice;

    public ClientThread(BluetoothDevice device, UUID uuid, Handler handler) {
        setHandler(handler);
        BluetoothSocket tmp = null;
        mmDevice = device;
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
//                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e1) {
            Log.e(TAG, "client Socket's create() method failed", e1);
            e1.printStackTrace();

        }
        mmSocket = tmp;
    }


    public void run() {
        try {
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.i(TAG, "1");
                try {
                    Thread.sleep(500);
                    showMsg("将客户端连接服务端的port字段设置为5.");
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 5);
                    mmSocket.connect();
                } catch (IOException e) {
                    Log.i(TAG, "2");
                    e.printStackTrace();
                    Log.e(TAG, "client connect to server failed. to close socket" + ",date:" + new Date(System.currentTimeMillis()).toString());
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                    }
                    return;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.i(TAG, "connect success");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            while (true) {
                try {
                    Thread.sleep(250);
                    readMsgAndShow();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.getMessage());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            cancel();
        }
    }
}
