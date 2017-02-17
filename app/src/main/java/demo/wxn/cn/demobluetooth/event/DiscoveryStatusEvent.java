package demo.wxn.cn.demobluetooth.event;

/**
 * Created by wangxn on 2017/2/15.
 */
public class DiscoveryStatusEvent {

    private int status;


    public DiscoveryStatusEvent(int i) {
        this.status = i;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
