package demo.wxn.cn.demobluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import demo.wxn.cn.demobluetooth.R;
import demo.wxn.cn.demobluetooth.adapter.NormalRecyclerViewAdapter;
import demo.wxn.cn.demobluetooth.event.BluetoothNameChangedEvent;
import demo.wxn.cn.demobluetooth.event.BluetoothPairedEvent;
import demo.wxn.cn.demobluetooth.event.BluetoothPairingEvent;
import demo.wxn.cn.demobluetooth.event.DiscoveryStatusEvent;
import demo.wxn.cn.demobluetooth.utils.BluetoothUtils;

/**
 * Created by wangxn on 2017/2/15.
 */
public class DiscoveryDevicesActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static final String TAG = "DiscoveryDevicesActivit";
    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    private BluetoothAdapter adapter;
    private NormalRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_devices);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));//这里用线性宫格显示 类似于grid view
//        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));//这里用线性宫格显示 类似于瀑布流
        recyclerViewAdapter = new NormalRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(recyclerViewAdapter);

        toolbar.setTitle("扫描蓝牙设备");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    private boolean hasStartDiscovery = false;
    @Override
    protected void onResume() {
        super.onResume();

        if (!hasStartDiscovery){
            startDiscovery();
            hasStartDiscovery = true;
        }
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * 导入菜单
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_discovery_devices, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discovery:
                if (canDiscovery) {
                    Log.i(TAG, "开始扫描..");
                    startDiscovery();
                }
                break;
        }
        return true;
    }

    /**
     * 启动扫描
     */
    private void startDiscovery() {
        if (!adapter.isDiscovering()){
            Log.i(TAG, "调用方法， 开启扫描");
            adapter.startDiscovery();
            recyclerViewAdapter.clear();
        } else {
            Log.i(TAG, "扫描正在进行中，请稍等。。");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothNameChangedEvent event){
        Log.i(TAG,"接受到EventBus事件，名称改变事件。");

        BluetoothDevice device = event.getDevice();
        recyclerViewAdapter.add(device);
    }

    private boolean canDiscovery = true;

    /**
     * 扫描状态改变事件
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DiscoveryStatusEvent event) {
        if(0 == event.getStatus()){
//            btnDiscovery.setEnabled(false);
            canDiscovery = false;
        } else if (1 == event.getStatus()){
//            btnDiscovery.setEnabled(true);
            canDiscovery = true;
        }
    }


    public void connect(BluetoothDevice device){
        Log.i(TAG, "connect(), 关闭扫描.");
        adapter.cancelDiscovery();

        int bondStatus = device.getBondState();
        if(BluetoothDevice.BOND_BONDED == bondStatus){
            Log.i(TAG, "设备已经绑定,直接连接");
            toConnectActivity(device);
            //已绑定 12
        }else if(BluetoothDevice.BOND_BONDING == bondStatus){
            Log.i(TAG, "设备正在绑定中...");
            //绑定中 11
        }else if (BluetoothDevice.BOND_NONE == bondStatus){
            //没有绑定 10
            Log.i(TAG, "设备没有绑定,启动绑定");
            try {
                //开启绑定
                BluetoothUtils.createBond(BluetoothDevice.class, device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 和其他蓝牙设备开始进行接触， 配对或者直接进行连接
     * @param device
     */
    public void pairingDevice(BluetoothDevice device) {
        Log.i(TAG, "pairingDevice()");
        //没有绑定 10
        try {
            //开启绑定
            BluetoothUtils.createBond(BluetoothDevice.class, device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动蓝牙配对时触发的广播，
     * TODO: 这里处理不让手机启动对话框
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothPairingEvent event) {
        Log.i(TAG, "EventBus响应事件,处理蓝牙配对广播");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothPairedEvent event) {
        Log.i(TAG, "EventBus响应事件,蓝牙配对成功的广播");

        BluetoothDevice device = event.getDevice();
        toConnectActivity(device);


//        ParcelUuid[] uuids = device.getUuids();
    }

    private void toConnectActivity(BluetoothDevice device) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ConnectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("device", device);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 传递到这里的UUID必须和服务端设备所使用的UUID相匹配。
     * 使用同样的UUID就是简单地将UUID字符串硬编码进你的应用，
     * 并且在服务端和客户端代码中引用它。
     *
     * If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
     * However if you are connecting to an Android peer then please generate your own unique UUID.
     */
    public static final UUID MY_UUID = UUID.randomUUID();

}
