package demo.wxn.cn.demobluetooth.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import demo.wxn.cn.demobluetooth.MyApplication;
import demo.wxn.cn.demobluetooth.event.BluetoothDevicesChangedEvent;
import demo.wxn.cn.demobluetooth.event.BluetoothNameChangedEvent;
import demo.wxn.cn.demobluetooth.event.BluetoothPairedEvent;
import demo.wxn.cn.demobluetooth.event.BluetoothPairingEvent;
import demo.wxn.cn.demobluetooth.event.DiscoveryStatusEvent;
import demo.wxn.cn.demobluetooth.event.StartedBluetoothEvent;

/**
 * Created by wangxn on 2017/2/14.
 */
public class MyBluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBluetoothReceiver";

    /**
     * 记录当前正在连接的所有蓝牙输入设备
     */
    public List<BluetoothDevice> connectedBluetoothDevices = new ArrayList<BluetoothDevice>();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG,"action:" + action);

        /**
         * 手机蓝牙开启关闭时发送
         */
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state){
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "STATE_TURNING_ON 手机蓝牙正在开启");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "STATE_ON 手机蓝牙开启");
                    EventBus.getDefault().post(new StartedBluetoothEvent());
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "STATE_TURNING_OFF 手机蓝牙正在关闭");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "STATE_OFF 手机蓝牙关闭");
                    break;
            }
        }

        /**
         * startDiscovery(),来扫描设备周边可以使用的其他的蓝牙设备，这个方法会触发下面的广播
         */
        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
            Log.d(TAG, "启动扫描 ");
            EventBus.getDefault().post(new DiscoveryStatusEvent(0));
        }

        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            Log.d(TAG, "结束扫描");
            EventBus.getDefault().post(new DiscoveryStatusEvent(1));
        }

        if (action.equals(BluetoothAdapter.ACTION_REQUEST_ENABLE)){
            Log.d(TAG, "开启蓝牙对其他设备可见。");
        }



        if (action.equals(BluetoothDevice.ACTION_UUID)){
            Log.i(TAG, "获取UUID信息");
//            UUID uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
//            Log.i(TAG, "uuid : " + uuid.toString());
        }


        if (intent.getAction().equals(BluetoothDevice.ACTION_NAME_CHANGED)){
            Log.i(TAG, "其他蓝牙设备名称改变通知");
            BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "扫描到可连接的蓝牙设备："+device.getName() + "," + device.getAddress());

            EventBus.getDefault().post(new BluetoothNameChangedEvent(device));
        }


        if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
            Log.i(TAG, "开启让其他设备可见，scan mode:");
            int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
            Log.i(TAG, "mode : " + mode);

            if (BluetoothAdapter.SCAN_MODE_NONE == mode) {
//                int SCAN_MODE_NONE = 20;//这个模式不能被发现也不能连接
                Log.i(TAG, "这个模式不能被发现也不能连接");
            }else if(BluetoothAdapter.SCAN_MODE_CONNECTABLE == mode){
//            int SCAN_MODE_CONNECTABLE = 21;//这个模式不能被扫描到，但是可以连接
                Log.i(TAG, "这个模式不能被扫描到，但是可以连接");
            }else if(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE == mode){
//            int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;//这个模式可以被发现，也能被连接
                Log.i(TAG, "这个模式可以被发现，也能被连接");
            }
        }

        if (action.equals(BluetoothDevice.ACTION_FOUND)){
            Log.i(TAG, "扫描找到设备。");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            Log.i(TAG, device.getName());
//            Log.i(TAG, device.getAddress());
            EventBus.getDefault().post(new BluetoothNameChangedEvent(device));
        }


        //createBond() 就会触发广播“BluetoothDevice.ACTION_PAIRING_REQUEST
        if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)){
            Log.i(TAG,"创建蓝牙设备配对时，触发的广播");
            BluetoothDevice btDevice = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            EventBus.getDefault().post(new BluetoothPairingEvent(btDevice));

        }

        /**
         * 监听蓝牙设备配对状态的广播
         */
        if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = device.getName();
            Log.d(TAG, "device name: " + name);
            int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            switch (state) {
                case BluetoothDevice.BOND_NONE:
                    Log.d(TAG, "BOND_NONE 删除配对");
                    break;
                case BluetoothDevice.BOND_BONDING:
                    Log.d(TAG, "BOND_BONDING 正在配对");
                    break;
                case BluetoothDevice.BOND_BONDED:
                    Log.d(TAG, "BOND_BONDED 配对成功");
                    EventBus.getDefault().post(new BluetoothPairedEvent(device));
                    break;
            }
        }


        if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)){
            Log.i(TAG, "连接状态改变事件通知");
        }

        //////////////////////////////////////////////////////////////////////////////




        /**
         * 监听蓝牙设备连接和连接断开的广播
         */
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            Log.d(TAG, "接收蓝牙连接的广播, 远程设备: " + device.getName() + " ACTION_ACL_CONNECTED");
//            if (isInputDevice(device)) {
//                connectedBluetoothDevices = ((MyApplication)context).connectedBluetoothDevices;
//                if (!connectedBluetoothDevices.contains(device)) {
//                    connectedBluetoothDevices.add(device);
//                    EventBus.getDefault().post(new BluetoothDevicesChangedEvent(connectedBluetoothDevices));
//                }
//            }
        } else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            Log.d(TAG, "接收蓝牙断开连接的广播, 远程设备: " + device.getName() + " ACTION_ACL_DISCONNECTED");
//            if (isInputDevice(device)) {
//                connectedBluetoothDevices = ((MyApplication)context).connectedBluetoothDevices;
//                connectedBluetoothDevices.remove(device);
//            }
        }

    }

    /**
     * 判断蓝牙设备是否是输入设备,
     * 这里认为 PERIPHERAL是输入设备
     */
    private boolean isInputDevice(BluetoothDevice device) {
        int deviceMajorClass = device.getBluetoothClass().getMajorDeviceClass();
        if (deviceMajorClass == BluetoothClass.Device.Major.PERIPHERAL) {
            return true;
        }
        return false;
    }


}
