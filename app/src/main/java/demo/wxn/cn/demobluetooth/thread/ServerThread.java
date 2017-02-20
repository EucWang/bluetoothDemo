package demo.wxn.cn.demobluetooth.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by wangxn on 2017/2/20.
 */

public class ServerThread extends BasicThread {


    private static final String MY_NAME = "BluetoothDemo02202017";
    private static final String TAG = "ServerThread";

    private BluetoothServerSocket mmServerSocket;

    public ServerThread(UUID uuid, Handler handler) {
        setHandler(handler);
        BluetoothServerSocket tmp = null;
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            tmp = adapter.listenUsingRfcommWithServiceRecord(MY_NAME, uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "server socket create() failed");
        }
        mmServerSocket = tmp;
        Log.i(TAG, "create ServerSocket success");

        //服务端监听的channel, 也就是Socket的port字段值, 这个值也必须是Client端设置的值,否则连接不上
        try {
            Field fMSocket = mmServerSocket.getClass().getDeclaredField("mSocket");
            fMSocket.setAccessible(true);
            BluetoothSocket mSocket = (BluetoothSocket) fMSocket.get(mmServerSocket);
            Method mGetPort = mSocket.getClass().getDeclaredMethod("getPort");
            mGetPort.setAccessible(true);
            int port = (int) mGetPort.invoke(mSocket);
            Log.i(TAG, "服务端监听的端口号是:" + port);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            mmSocket = null;
            try {
                mmSocket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.i(TAG, "ServerSocket's accept() method failed : " + e.getMessage());
                e.printStackTrace();
                return;
            }

            try {
                Log.i(TAG, "尝试关闭ServerSocket,只保持当前和远程设备的Socket连接,不接受第二台设备的连接.");
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i(TAG, "创建接受数据的Socket成功");
            if (mmSocket != null) {
                try {
                    mmOutStream = mmSocket.getOutputStream();
                    mmInStream = mmSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true) {
                    try {
                        Thread.sleep(250);
                        readMsgAndShow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }finally{
            cancel();
        }
    }

}
