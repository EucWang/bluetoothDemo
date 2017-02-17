package demo.wxn.cn.demobluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.system.StructUtsname;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import demo.wxn.cn.demobluetooth.R;
import demo.wxn.cn.demobluetooth.utils.BluetoothUtils;

public class ConnectActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private static final String TAG = "ConnectionActivity";

    final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private UUID MY_UUID = UUID.fromString(SPP_UUID);

    private static final String MY_NAME = "Bluetooth";

    @Bind(R.id.btn_client)
    Button btn_client;

    @Bind(R.id.btn_server)
    Button btn_server;

    @Bind(R.id.btn_send)
    Button btn_send;

    @Bind(R.id.tv_info)
    TextView tv_info;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.et_input)
    EditText et_input;

    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private ConnectThread clientThread;
    private AcceptThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);
//        EventBus.getDefault().register(this);

        adapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();

        device = intent.getParcelableExtra("device");
        ParcelUuid[] uuids = device.getUuids();
        ParcelUuid uuid = uuids[0];
        MY_UUID = uuid.getUuid();

    }

    private boolean hasInit = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasInit) {
            setViews();

            hasInit = true;
        }
    }

    private void setViews() {
        toolbar.setTitle("蓝牙连接");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        btn_client.setOnClickListener(this);
        btn_server.setOnClickListener(this);
        btn_send.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
//        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_client:
                btn_client.setEnabled(false);
                clientThread = new ConnectThread(device);
                showLog("Client初始化线程成功.");
                clientThread.start();
                break;
            case R.id.btn_server:
                btn_server.setEnabled(false);
                serverThread = new AcceptThread();
                showLog("Server线程初始化成功.");
                serverThread.start();
                break;
            case R.id.btn_send:
                if (clientThread != null){
                    String content = et_input.getText().toString();
                    if (content != null && !"".equals(content.trim())) {
                        Log.i(TAG, "ClientThread send content");
                        clientThread.setSendContent(content);
                    }
                }else if (serverThread != null){
                    String content = et_input.getText().toString();
                    if (content != null && !"".equals(content.trim())) {
                        Log.i(TAG, "ServerThread send content");
                        serverThread.setSendContent(content);
                    }
                }
                break;
            default:
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 服务端 接受数据的线程
     */
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = adapter.listenUsingRfcommWithServiceRecord(MY_NAME, MY_UUID);
//                tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(MY_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "server socket create() failed");
            }
            mmServerSocket = tmp;
            Log.i(TAG, "构建方法完成");


            //服务端监听的channel, 也就是Socket的port字段值, 这个值也必须是Client端设置的值,否则连接不上
            try {
                Field fMSocket = mmServerSocket.getClass().getDeclaredField("mSocket");
                fMSocket.setAccessible(true);
                BluetoothSocket mSocket = (BluetoothSocket) fMSocket.get(mmServerSocket);
                Method mGetPort = mSocket.getClass().getDeclaredMethod("getPort");
                mGetPort.setAccessible(true);
                int port = (int) mGetPort.invoke(mSocket);
                showMsg(TAG, "服务端监听的端口号是:" + port);
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
            BluetoothSocket socket = null;
            Log.i(TAG, "acceptThread start()");

            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                showMsg(TAG, "Socket's accept() method failed : " + e.getMessage());
                e.printStackTrace();
                return;
            }

            try {
                Log.i(TAG, "关闭服务Socket");
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;
            Log.i(TAG, "创建接受数据的Socket成功");
            if (socket != null) {
                    try {
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                while (true) {
                    try {
                        Thread.sleep(250);
//                        sendMsg(outputStream);
                        readMsgAndShow(inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 从EditText中读取内容,然后发送出去
         * @param outputStream
         * @throws IOException
         */
        private void sendMsg(OutputStream outputStream) throws IOException {
            if (content != null && !"".equals(content.trim())) {
                synchronized (this) {
                    outputStream.write(content.getBytes("UTF-8"));
                    outputStream.flush();
                    content = null;
                }
            }
        }

        public void setSendContent(String content){
            synchronized (this) {
                this.content = content;
            }
        }

        private String content = null;

        private void readMsgAndShow(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[1024];
            int read = inputStream.read(buffer);
            if (read > 0) {
                String tmp = new String(Arrays.copyOf(buffer, read), "UTF-8");
                if (!tmp.equals(lastContent)){
                    lastContent = tmp;
                    showMsg(TAG, "Client : " + lastContent);
                }
            }
            for (int i=0; i<read;i++){
                buffer[i] = 0;
            }
            read = -1;
        }

        private String lastContent;

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 客户端 发送数据的线程
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e1) {
                Log.e(TAG, "client Socket's create() method failed", e1);
                e1.printStackTrace();

            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                boolean connected = mmSocket.isConnected();
                BluetoothDevice remoteDevice = mmSocket.getRemoteDevice();
                Log.i(TAG, "remote device is connected : " + connected + ",date:" + new Date(System.currentTimeMillis()).toString());

                mmSocket.connect();
            } catch (IOException connectException) {
                Log.i(TAG, "1");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
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
                    sendMsg(mmOutStream);
                    readMsgAndShow(mmInStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.getMessage());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 从EditText中读取内容,然后发送出去
         * @param outputStream
         * @throws IOException
         */
        private void sendMsg(OutputStream outputStream) throws IOException {
            if (content != null && !"".equals(content.trim())) {
                synchronized (this) {
                    outputStream.write(content.getBytes("UTF-8"));
                    outputStream.flush();
                    content = null;
                }
            }
        }

        /**
         * 从输入流中读取内容写入到TextView中显示
         * @param inputStream
         * @throws IOException
         */
        private void readMsgAndShow(InputStream inputStream) throws IOException {
            int read = inputStream.read(buffer);
            if (read > 0) {
                String tmp = new String(Arrays.copyOf(buffer, read), "UTF-8");
                if (!tmp.equals(lastContent)){
                    lastContent = tmp;
                    showMsg(TAG, "Client : " + lastContent);
                }
            }
            for (int i=0; i<read;i++){
                buffer[i] = 0;
            }
            read = -1;
        }

        private byte[] buffer = new byte[1024];
        private String lastContent;

        public void setSendContent(String content){
            synchronized (this) {
                this.content = content;
            }
        }

        private String content = null;

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, e.getMessage());
            }
        }
    }

    public void showMsg(String info) {
        showMsg(null, info);
    }

    /**
     * 通过handler发送信息给一个TextView显示
     *
     * @param info
     */
    public void showMsg(String tag, String info) {
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = info;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    showLog((String) msg.obj);
                    break;
            }
        }
    };

    public void showLog(String log) {
        StringBuffer sb = new StringBuffer(tv_info.getText());
        sb.append("\n" + log);
        tv_info.setText(sb.toString());
    }
}
