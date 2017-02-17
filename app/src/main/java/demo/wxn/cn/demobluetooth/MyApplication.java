package demo.wxn.cn.demobluetooth;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by wangxn on 2017/2/14.
 */

public class MyApplication extends Application {
    public List<BluetoothDevice> connectedBluetoothDevices;
}
