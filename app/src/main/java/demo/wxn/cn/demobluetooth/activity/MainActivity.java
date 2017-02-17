package demo.wxn.cn.demobluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import demo.wxn.cn.demobluetooth.R;
import demo.wxn.cn.demobluetooth.event.BluetoothDevicesChangedEvent;
import demo.wxn.cn.demobluetooth.event.StartedBluetoothEvent;
import demo.wxn.cn.demobluetooth.receiver.MyBluetoothReceiver;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static final String TAG = "MainActivity";

    private BluetoothAdapter adapter;

    private MyBluetoothReceiver receiver;

    @Bind(R.id.tv_bonds_devices)
    TextView tv_bonds_devices;

    @Bind(R.id.tv_local_info_value)
    TextView tv_local_info_value;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        registReceiver();
    }

    /**
     * 注册广播
     */
    private void registReceiver() {
        receiver = new MyBluetoothReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

//        intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);       //打开蓝牙的广播
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);        //蓝牙开启关闭状态广播
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);   //开始扫描的广播
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);   //结束扫描的广播

        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);                 //扫描到可用设备，触发的广播
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);         //获得设备的UUID的会触发的广播

        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);    //蓝牙设备的配对请求时，会监听到这个广播
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);     //蓝牙配对状态的广播
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);  //配对之后， 蓝牙连接状态的广播
//        int STATE_DISCONNECTED = 0; //未连接
//        int STATE_CONNECTING = 1; //连接中
//        int STATE_CONNECTED = 2; //连接成功

        intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);   //本地蓝牙设备的名字变化的广播
        intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);          //其他蓝牙设备的名称发生变化的广播

        //ACTION_SCAN_MODE_CHANGED
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);    //设置设备本身是否对其他设备可见的状态改变的广播
//        int SCAN_MODE_NONE = 20;//这个模式不能被发现也不能连接
//        int SCAN_MODE_CONNECTABLE = 21;//这个模式不能被扫描到，但是可以连接
//        int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;//这个模式可以被发现，也能被连接

        registerReceiver(receiver, intentFilter);   //注册广播
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private boolean isStartBluetoothAndDiscovery = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (!isStartBluetoothAndDiscovery) {
            if (startBluetoothAndDiscovery()) {

                //设置本机蓝牙信息
                String address = adapter.getAddress();
                String name = adapter.getName();

                tv_local_info_value.setText("address : " + address + "\nname : " + name);
            }
            isStartBluetoothAndDiscovery = true;
        }

        toolbar.setTitle("蓝牙操作DEMO");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode=" + requestCode + ",resultCode=" + resultCode + ",data=" + data.getAction());
    }

    /**
     * 启动蓝牙，
     * 如果蓝牙已经启动，同时开启 本机对其他设备可见
     *
     * @return true:设备支持蓝牙； false:设备不支持蓝牙
     */
    private boolean startBluetoothAndDiscovery() {
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            Log.w(TAG, "设备不支持蓝牙！");
            return false;
        }

        //判断蓝牙是否开启
        if (!adapter.isEnabled()) {
            adapter.enable();
        } else {
            statusOfDiscovery = 0;

            setVisible2Other();
            getBondedDevices();
        }

        return true;
    }

    /**
     * 接受到蓝牙开启完毕的事件通知，然后开启扫描功能, 然后开启 对其他蓝牙设备可见
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StartedBluetoothEvent event) {
        Log.i(TAG, "接收EventBus通知，开启扫描。");
        statusOfDiscovery = 0;

        setVisible2Other();
        getBondedDevices();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothDevicesChangedEvent event) {
        Log.i(TAG, "接收EventBus通知，监听蓝牙设备连接和连接断开的广播。");
        List<BluetoothDevice> devices = event.getDevices();
        StringBuffer stringBuffer = getStringOfDevices(devices);
//        tv_discovery_devices.setText(stringBuffer.toString());
    }

    @NonNull
    private StringBuffer getStringOfDevices(List<BluetoothDevice> devices) {
        StringBuffer stringBuffer = new StringBuffer();
        if (devices != null && devices.size() > 0) {
            int index = 0;
            for (BluetoothDevice device : devices) {
                String address = device.getAddress();
                String name = device.getName();
                ParcelUuid[] uuids = device.getUuids();
                StringBuffer sb = new StringBuffer();
                if (uuids != null && uuids.length > 0) {
                    for (ParcelUuid uuid : uuids) {
                        sb.append(uuid.getUuid().toString() + ":");
                    }
                }
                int bondState = device.getBondState();
//                int type = device.getType();
                if (index != 0) {
                    stringBuffer.append("\n");
                }
                stringBuffer.append("---------------\nname:" + name + "\naddress:" + address + "\nuuids:" + sb.toString() + "\nbondState:" + bondState);
                index++;
            }
        }
        return stringBuffer;
    }

    /**
     * -1 ： 设备不能扫描
     * 0 : 未开启扫描
     * 1 ： 正在扫描中
     * 2 ： 扫描结束
     */
    private int statusOfDiscovery = -1;

    /**
     * 设置设备本身是否对其他设备可见， 10分钟可见
     * 通过发送ACTION_REQUEST_DISCOVERABLE
     * 的广播，会调用系统的方法
     * <p>
     * 如果仅仅是连接到远程蓝牙设备的话，你并不需要开启可见性。
     * 开启可见性仅仅在你的应用中作为服务端时才是必要的。
     * 因为其他蓝牙设备必须找到你的设备之后才能建立连接。
     */
    public void setVisible2Other() {
        Log.i(TAG, "设置设备本身是否对其他设备300second可见.");
        //设置设备在300s内是可见的
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }


    /**
     * 获取已经绑定的蓝牙设备
     */
    private void getBondedDevices() {
        Log.i(TAG, "获取已经绑定的蓝牙设备");
        Set<BluetoothDevice> devices = adapter.getBondedDevices();

        if (devices.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
//                Log.d(TAG, bluetoothDevice.getAddress());
//                Log.d(TAG, bluetoothDevice.getName());

                ParcelUuid[] uuids = bluetoothDevice.getUuids();
                StringBuffer sb = new StringBuffer();
                if (uuids != null && uuids.length > 0) {
                    for (ParcelUuid uuid : uuids) {
                        sb.append(uuid.getUuid().toString() + ":");
                    }
                }

                stringBuffer.append("\n[ name: " + bluetoothDevice.getName() +
                        "\n  naddress: " + bluetoothDevice.getAddress() +
                        "\n uuids: "+ sb.toString() +
                        " ]");
            }
            if (stringBuffer.length() > 0) {
                tv_bonds_devices.setText(stringBuffer.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next_page, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
         switch (item.getItemId()){
             case R.id.action_next_page:
                 Intent intent = new Intent();
                 intent.setClass(getApplicationContext(), DiscoveryDevicesActivity.class);
                 startActivity(intent);
                 break;
         }
        return true;
    }
}
