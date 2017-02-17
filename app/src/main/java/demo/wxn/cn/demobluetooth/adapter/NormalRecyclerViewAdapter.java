package demo.wxn.cn.demobluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import demo.wxn.cn.demobluetooth.R;
import demo.wxn.cn.demobluetooth.activity.DiscoveryDevicesActivity;

/**
 * Created by wangxn on 2017/2/15.
 */
public class NormalRecyclerViewAdapter extends RecyclerView.Adapter<NormalRecyclerViewAdapter.NormalTextViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final DiscoveryDevicesActivity activity;
    private List<BluetoothDevice> devices;

    public NormalRecyclerViewAdapter(DiscoveryDevicesActivity activity) {
        if (this.devices == null) {
            this.devices = new ArrayList<>();
        }
        this.devices.clear();

        this.activity = activity;
        mContext = this.activity.getApplicationContext();
        mLayoutInflater = LayoutInflater.from(mContext);

    }

    @Override
    public NormalTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NormalTextViewHolder(mLayoutInflater.inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, int position) {
        BluetoothDevice device = this.devices.get(position);

        holder.mTvName.setText("name:" + device.getName());
        holder.mTvAddress.setText("address:" + device.getAddress());

        ParcelUuid[] uuids = device.getUuids();
        StringBuffer sb = new StringBuffer();
        if (uuids != null && uuids.length > 0) {
            for (ParcelUuid uuid : uuids) {
                sb.append(uuid.getUuid().toString() + ":");
            }
        }
        holder.mTvUuid.setText("uuids:" + sb.toString());

        int bondStatus = device.getBondState();
        String strBondStatus = "";
        if(BluetoothDevice.BOND_BONDED == bondStatus){
            strBondStatus = "已绑定";  //12
        }else if(BluetoothDevice.BOND_BONDING == bondStatus){
            strBondStatus = "绑定中"; //11
        }else if (BluetoothDevice.BOND_NONE == bondStatus){
            strBondStatus = "未绑定"; //10
        }
        holder.mTvBondsStatus.setText("bondStatus:" + strBondStatus);
    }

    public void add(BluetoothDevice device) {
        if (!this.devices.contains(device)) {
            this.devices.add(device);
            this.notifyDataSetChanged();
        }
    }

    public void clear() {
        this.devices.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.devices == null ? 0 : this.devices.size();
    }

    public class NormalTextViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView mTvName;

        @Bind(R.id.tv_address)
        TextView mTvAddress;

        @Bind(R.id.tv_uuid)
        TextView mTvUuid;

        @Bind(R.id.tv_bonds_status)
        TextView mTvBondsStatus;

        NormalTextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getPosition();
                    Log.d("NormalTextViewHolder", "onClick--> position = " + position);
                    NormalRecyclerViewAdapter.this.activity.connect(devices.get(position));
                }
            });
        }
    }
}