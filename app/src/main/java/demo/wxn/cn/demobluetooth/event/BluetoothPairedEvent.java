package demo.wxn.cn.demobluetooth.event;

import android.bluetooth.BluetoothDevice;

/**
 * Created by wangxn on 2017/2/15.
 */
public class BluetoothPairedEvent {



    private final BluetoothDevice device;

    public BluetoothPairedEvent(BluetoothDevice device) {
        this.device = device;
    }
    public BluetoothDevice getDevice() {
        return device;
    }
}
