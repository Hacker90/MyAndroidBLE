package com.ble.myapplication.ble_service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.util.HashSet;

public abstract class HomeBleService extends Service {
    public static HomeBleService ME = null;

    public HashSet<String> scanDeviceMacAddress;

    public abstract boolean connect(String address);

    public abstract void disConnect(String address);

    /** 通过mac地址得到1个BluetoothDevice对象 */
    public abstract BluetoothDevice getBLEObjOnMacAddress(String address);

    /** 得到连接状态 */
    public abstract boolean getProfileState(String address);

    protected abstract void init();

    public abstract void scan(boolean flag);

    public abstract void writeData(byte[] data, String address);

    public abstract IBinder getBinder();

    public boolean isScanning = false;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
	


    @Override
    public IBinder onBind(Intent intent) {
        return getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scanDeviceMacAddress = new HashSet<String>();
    }


    @Override
    public void onDestroy() {
        if (ME != null)
            ME = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scanDeviceMacAddress.clear();
        return super.onStartCommand(intent, flags, startId);
    }
}
