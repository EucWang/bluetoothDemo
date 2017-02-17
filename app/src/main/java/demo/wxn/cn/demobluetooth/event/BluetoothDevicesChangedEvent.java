package demo.wxn.cn.demobluetooth.event;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by wangxn on 2017/2/14.
 */

public class BluetoothDevicesChangedEvent {


    private List<BluetoothDevice> devices;

    public BluetoothDevicesChangedEvent(List<BluetoothDevice> connectedBluetoothDevices) {
        this.devices = connectedBluetoothDevices;
    }

    public List<BluetoothDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<BluetoothDevice> devices) {
        this.devices = devices;
    }
}
