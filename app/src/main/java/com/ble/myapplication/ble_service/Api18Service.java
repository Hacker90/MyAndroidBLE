package com.ble.myapplication.ble_service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;

import com.ble.myapplication.constants.PublicConstant;
import com.ble.myapplication.constants.UUIDConstant;
import com.ble.myapplication.enums.BLEDeviceStatus;
import com.ble.myapplication.utils.LogAndToastUtil;
import com.ble.myapplication.utils.SendBroadcastUtil;
import com.ble.myapplication.utils.Utility;

import java.util.HashMap;
import java.util.List;

public class Api18Service extends HomeBleService {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;


    private HashMap<String, BluetoothGatt> deviceGatt = null;

    private boolean isConnecting = false;


    @Override
    public void onCreate() {
        super.onCreate();
        LogAndToastUtil.log("service oncreate...");
        ME = this;
        init();
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public Api18Service getService() {
            return Api18Service.this;
        }
    }

    @Override
    public IBinder getBinder() {
        return binder;
    }

    @Override
    protected void init() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        deviceGatt = new HashMap<String, BluetoothGatt>();
    }

    @Override
    public synchronized boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }
        if(isConnecting){
            return false;
        }

        isConnecting = true;

        scan(false);

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (!getProfileState(address)) {
            BluetoothGatt mGatt = deviceGatt.get(address);

            // Previously connected device. Try to reconnect.
            if (mGatt != null) {
                LogAndToastUtil.log("Re-use GATT connection");
                if (mGatt.connect()) {
                    return true;
                } else {
                    LogAndToastUtil.log("GATT re-connect failed.");
                    return false;
                }
            }

            if (device == null) {
                return false;
            }
            device.connectGatt(this, false, mGattCallback);
        } else {
            LogAndToastUtil.log("已连接上");
            return false;
        }
        return true;
    }

    @Override
    public void disConnect(String address) {
        LogAndToastUtil.log("执行断开..");

        BluetoothGatt mGatt = deviceGatt.get(address);

        if (mGatt != null) {
            if (getProfileState(address)) {
                mGatt.disconnect();
            }else{
                SendBroadcastUtil.sendMyBroadcast(this, getBLEObjOnMacAddress(address), BLEDeviceStatus.DISCONNECTED);
            }
        }
    }

    @Override
    public BluetoothDevice getBLEObjOnMacAddress(String address) {
        return mBluetoothAdapter.getRemoteDevice(address);
    }

    @Override
    public boolean getProfileState(String address) {
        BluetoothDevice mDevice = getBLEObjOnMacAddress(address);
        int connectionState = mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT);
        boolean flag = connectionState == BluetoothProfile.STATE_CONNECTED;
    if (flag)
       LogAndToastUtil.log("getProfileState检测->设备已连接上！->:%s", address);
    else
       LogAndToastUtil.log("getProfileState检测->设备未连接！->:%s", address);
        return flag;
    }

    @Override
    public void onDestroy() {
        ME = null;
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public void scan(boolean flag) {
        if (flag == isScanning) {
            return;
        }

        isScanning = flag;
        if (flag) {
            LogAndToastUtil.log("开始搜索蓝牙设备...");
            scanDeviceMacAddress.clear();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    SendBroadcastUtil.sendMyBroadcast(Api18Service.this, BLEDeviceStatus.AUTO_STOP_SCAN);
                    isScanning = false;
                    LogAndToastUtil.log("自动停止了...");
                }
            }, PublicConstant.SCAN_BLE_TIME);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            LogAndToastUtil.log("停止扫描设备...");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    @Override
    public void writeData(byte[] data, String address) {
        BluetoothGatt mGatt = deviceGatt.get(address);
        BluetoothGattCharacteristic canWriteDataCharacteristic = getMyCharacteristicFromAddress(address);
        if (mGatt != null && canWriteDataCharacteristic != null) {
            canWriteDataCharacteristic.setValue(data);

            mGatt.writeCharacteristic(canWriteDataCharacteristic);
        }
    }

    private BluetoothGattCharacteristic getMyCharacteristicFromAddress(String address){
        BluetoothGatt gatt = deviceGatt.get(address);
        if(gatt == null){
            return null;
        }else{
            BluetoothGattService service = gatt.getService(UUIDConstant.SERVICE_UUID);
            if(service == null){
                return null;
            } else {
                return service.getCharacteristic(UUIDConstant.CAN_WRITE_UUID);
            }
        }
    }

    public void openBLENotification(final BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUIDConstant.SERVICE_UUID);


      /* 打开通知*/
        BluetoothGattCharacteristic configCharacteristic = service.getCharacteristic(UUIDConstant.CONFIG_CHARACTERISTIC_UUID);
        if(configCharacteristic == null){
            LogAndToastUtil.log("特征%s不存在。", UUIDConstant.CONFIG_CHARACTERISTIC_UUID);
        }
        gatt.setCharacteristicNotification(configCharacteristic, true);
        BluetoothGattDescriptor configDescriptor = configCharacteristic.getDescriptor(UUIDConstant.CONFIG_DESCRIPTOR_UUID);
        if (configDescriptor != null) {
            configDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(configDescriptor);
            LogAndToastUtil.log("已打开%s，现在可以进行数据传输了。", UUIDConstant.CONFIG_CHARACTERISTIC_UUID);
        } else {
            LogAndToastUtil.log("%s为空", UUIDConstant.CONFIG_CHARACTERISTIC_UUID);
        }

        deviceGatt.put(gatt.getDevice().getAddress(), gatt);
    }

    private void searchService(final BluetoothGatt gatt) {
        new Thread() {
            public void run() {
                gatt.discoverServices();
            }
        }.start();

    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogAndToastUtil.log("回调onConnectionStateChange->status:%s;newState:%s", status, newState);

            deviceGatt.put(gatt.getDevice().getAddress(), gatt);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                searchService(gatt);
            } else {
                isConnecting = false;
                SendBroadcastUtil.sendMyBroadcast(Api18Service.this, gatt.getDevice(), BLEDeviceStatus.DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            LogAndToastUtil.log("回调onServicesDiscovered()->status:%s", status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogAndToastUtil.log("成功发现服务...mac地址:%s", gatt.getDevice().getAddress());

         /**/
         List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
               LogAndToastUtil.log("service的uuid:%s", service.getUuid());
               List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
               for (BluetoothGattCharacteristic cc : characteristics) {
                  LogAndToastUtil.log("特征的uuid:%s", cc.getUuid());
               }
               LogAndToastUtil.log("-------------------------------------");
            }



                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        openBLENotification(gatt);
                        SendBroadcastUtil.sendMyBroadcast(Api18Service.this, gatt.getDevice(), BLEDeviceStatus.CONNECTED);
                        isConnecting = false;
                    }
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            byte[] countArr = characteristic.getValue();
           if (countArr[0] == 0xA && countArr[1] == 0xB) {
                SendBroadcastUtil.sendMyBroadcast(Api18Service.this, gatt.getDevice(), BLEDeviceStatus.NOTIFY);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic chara, int status) {
            LogAndToastUtil.log("回调onCharacteristicWrite()->status:%s;特征id:%s;值:%s", status, chara.getUuid(), Utility.byteArray2HexString(chara.getValue()));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic chara, int status) {
            LogAndToastUtil.log("回调onCharacteristicRead()->status:%s;特征id:%s;值:%s", status, chara.getUuid(), Utility.byteArray2HexString(chara.getValue()));
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogAndToastUtil.log("外围设备->mac地址:%s;名称:%s", device.getAddress(), device.getName());

            if (!scanDeviceMacAddress.contains(device.getAddress())) {
                scanDeviceMacAddress.add(device.getAddress());
                SendBroadcastUtil.sendMyBroadcast(Api18Service.this, device, BLEDeviceStatus.SCAN_NEW_DEVICE);
            }
        }
    };

}