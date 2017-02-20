package demo.wxn.cn.demobluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.UUID;
import butterknife.Bind;
import butterknife.ButterKnife;
import demo.wxn.cn.demobluetooth.R;
import demo.wxn.cn.demobluetooth.thread.ClientThread;
import demo.wxn.cn.demobluetooth.thread.ServerThread;

public class ConnectActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {
    private static final String TAG = "ConnectionActivity";

    final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private UUID MY_UUID = UUID.fromString(SPP_UUID);

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
    private ClientThread clientThread;
    private ServerThread serverThread;

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
                clientThread = new ClientThread(device, MY_UUID, handler);
                showLog("Client初始化线程成功.");
                clientThread.start();
                break;
            case R.id.btn_server:
                btn_server.setEnabled(false);
                serverThread = new ServerThread(MY_UUID, handler);
                showLog("Server线程初始化成功.");
                serverThread.start();
                break;
            case R.id.btn_send:
                if (clientThread != null) {
                    String content = et_input.getText().toString();
                    if (content != null && !"".equals(content.trim())) {
                        clientThread.sendMsg(content);
                    }
                } else if (serverThread != null) {
                    String content = et_input.getText().toString();
                    if (content != null && !"".equals(content.trim())) {
                        serverThread.sendMsg(content);
                    }
                }
                break;
            default:
                break;
        }
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
