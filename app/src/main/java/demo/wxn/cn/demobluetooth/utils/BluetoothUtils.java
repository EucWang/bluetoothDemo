package demo.wxn.cn.demobluetooth.utils;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wangxn on 2017/2/15.
 */

public class BluetoothUtils {

    /**
     * 与设备配对
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean setPin(Class btClass, BluetoothDevice btDevice, String str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[] { byte[].class });
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[] { str.getBytes() });
            Log.e("returnValue", "" + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 功能：取消用户输入
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean cancelPairingUserInput(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 功能：取消配对
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean cancelBondProcess(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    @SuppressWarnings("rawtypes")
    public static void printAllInform(Class clsShow) {
        try {
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
                Log.e("method name", hideMethod[i].getName() + ";and the i is:" + i);
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
                Log.e("Field name", allFields[i].getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字符串转换成字节数组
     * @param message
     * @return
     */
//    public static byte[] getHexBytes(String message) {
//        int len = message.length() / 2;
//        char[] chars = message.toCharArray();
//        String[] hexStr = new String[len];
//        byte[] bytes = new byte[len];
//        for (int i = 0, j = 0; j < len; i += 2, j++) {
//            hexStr[j] = "" + chars[i] + chars[i + 1];
//            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
//        }
//        return bytes;
//    }
//
//    /**
//     * 16进制字节数组转换成字符串
//     * @param arr
//     * @return
//     */
//    public static String byteArrayToHex(byte[] arr) {
//        StringBuilder builder = new StringBuilder();
//        if (arr != null && arr.length > 0) {
//            builder = new StringBuilder(arr.length * 2);
//            for (byte b : arr) {
//                builder.append(String.format("%02X", b & 0xFF));
//            }
//        }
//        return builder.toString();
//    }
}
