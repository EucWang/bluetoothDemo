package demo.wxn.cn.demobluetooth.event;

import android.bluetooth.BluetoothDevice;

/**
 * Created by wangxn on 2017/2/15.
 */
public class BluetoothPairingEvent {
    private final BluetoothDevice device;

    public BluetoothPairingEvent(BluetoothDevice btDevice) {
        this.device = btDevice;
    }

    public BluetoothDevice getDevice() {
        return device;
    }


}
