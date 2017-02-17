package demo.wxn.cn.demobluetooth.event;

import android.bluetooth.BluetoothDevice;

/**
 * Created by wangxn on 2017/2/15.
 */
public class BluetoothNameChangedEvent {
    private BluetoothDevice device;

    public BluetoothNameChangedEvent(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
