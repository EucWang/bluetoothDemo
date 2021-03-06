package demo.wxn.cn.demobluetooth.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by wangxn on 2017/2/17.
 */

public abstract class BasicThread extends Thread {
    private static final String TAG = "BasicThread";

    protected Handler mHandler;

    protected BluetoothSocket mmSocket;

    protected InputStream mmInStream;

    protected OutputStream mmOutStream;

    protected byte[] buffer = new byte[1024];

    protected String lastContent;

    /**
     * 子类必须调用的方法
     * @param mHandler
     */
    public void setHandler(Handler mHandler){
        this.mHandler = mHandler;
    }

    /**
     * 在线程的run()方法中调用
     * 从输入流中读取内容写入到TextView中显示
     * @throws IOException
     */
    protected void readMsgAndShow() throws IOException {
        int read = mmInStream.read(buffer);
        if (read > 0) {
            String tmp = new String(Arrays.copyOf(buffer, read), "UTF-8");
            if (!tmp.equals(lastContent)){
                lastContent = tmp;
                showMsg(getName() + lastContent);
            }
        }
        for (int i=0; i<read;i++){
            buffer[i] = 0;
        }
        read = -1;
    }

    /**
     * 通过输出流向Socket连接的另一端发送数据
     * @param content
     */
    public void sendMsg(String content){
        if (content != null && !"".equals(content.trim())) {
            try {
                mmOutStream.write(content.getBytes("UTF-8"));
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭Socket的方法
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 在调用这个方法之前handler必须已经设置过了
     * 通过handler发送信息给一个TextView显示
     * @param info
     */
    public void showMsg(String info) {
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = info;
        mHandler.sendMessage(msg);
    }

}
