package com.ble.myapplication.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ble.myapplication.ble_service.Api18Service;
import com.ble.myapplication.ble_service.HomeBleService;
import com.ble.myapplication.constants.PublicConstant;
import com.ble.myapplication.enums.BLEDeviceStatus;
import com.ble.myapplication.utils.LogAndToastUtil;
import com.ble.myapplication.utils.Utility;


public class MainActivity extends Activity {
    private TextView tvShowStatus;

    private ListView mListView;

    public ArrayList<BluetoothDevice> deviceList = null;
    private MyAdapter mAdapter = null;

    private DeviceConnectBroadCastReceiver mBroadCastReceiver = null;

    private BluetoothDevice mDevice = null;
    private ProgressDialog progressDialog;

    //是否打开手机蓝牙的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PublicConstant.REQUEST_ENABLE_BT:
                    init();
                    break;

                default:
                    break;
            }
        }
    }


    public void writeA(View v) {
        if (Utility.checkBLEServiceIsNull()) {
            return;
        }
        if(mDevice == null){
            LogAndToastUtil.toast(this, getString(R.string.device_null));
            return;
        }
        byte[] data = {Utility.int2uint8(0x00), Utility.int2uint8(0x00),Utility.int2uint8(0xff),0x00};
        HomeBleService.ME.writeData(data, mDevice.getAddress());
    }

    public void writeB(View v) {
        if (Utility.checkBLEServiceIsNull()) {
            return;
        }
        if(mDevice == null){
            LogAndToastUtil.toast(this, getString(R.string.device_null));
            return;
        }
        byte[] data = {Utility.int2uint8(0xff), Utility.int2uint8(0x00),Utility.int2uint8(0x00),0x00};
        HomeBleService.ME.writeData(data, mDevice.getAddress());
    }

    public void writeC(View v) {
        if (Utility.checkBLEServiceIsNull()) {
            return;
        }
        if(mDevice == null){
            LogAndToastUtil.toast(this, getString(R.string.device_null));
            return;
        }
        byte[] data = {Utility.int2uint8(0x00), Utility.int2uint8(0xff),Utility.int2uint8(0x00),0x00};
        HomeBleService.ME.writeData(data, mDevice.getAddress());
    }

    public void clickScan(View v) {
        if (Utility.checkBLEServiceIsNull()) {
            return;
        }

        if (HomeBleService.ME.isScanning) {

            LogAndToastUtil.toast(this, "正在扫描...");
            return;
        }

        if (deviceList.size() > 0) {
            HashMap<String, BluetoothDevice> map = new HashMap<String, BluetoothDevice>();
            for (BluetoothDevice b : deviceList) {
                if (!HomeBleService.ME.getProfileState(b.getAddress())) {
                    map.put(b.getAddress(), b);
                }
            }
            HomeBleService.ME.scanDeviceMacAddress.removeAll(map.keySet());
            deviceList.removeAll(map.values());
        }

        mAdapter.notifyDataSetChanged();

        HomeBleService.ME.scanDeviceMacAddress.clear();
        showProgressDialog("提示", "正在扫描...");


        HomeBleService.ME.scan(true);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkIsSupportBLE()) {
            init();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        destroyBroadcast();
        super.onDestroy();
    }

    private boolean checkIsSupportBLE() {
        BluetoothAdapter mBluetoothAdapter = getBluetoothAdapter(this);
        if (mBluetoothAdapter == null) {
            LogAndToastUtil.toast(this, getString(R.string.can_not_support));
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, PublicConstant.REQUEST_ENABLE_BT);
                return false;
            }
        }
        return true;
    }

    private BluetoothAdapter getBluetoothAdapter(Activity activity) {
        BluetoothAdapter mBluetoothAdapter = null;
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion < Build.VERSION_CODES.JELLY_BEAN_MR2) {// 小于android4.3以下
            LogAndToastUtil.toast(activity, activity.getString(R.string.can_not_support));
            activity.finish();
        } else if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {// 大于android4.3
            mBluetoothAdapter = getBluetoothAdapter_api18(activity);
        }
        return mBluetoothAdapter;
    }

    private BluetoothAdapter getBluetoothAdapter_api18(Activity activity) {
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            LogAndToastUtil.toast(activity, activity.getString(R.string.can_not_support));
            activity.finish();
        }

        return bluetoothManager.getAdapter();
    }

    private void destroyBroadcast() {
        if (mBroadCastReceiver != null) {
            unregisterReceiver(mBroadCastReceiver);
            mBroadCastReceiver = null;
        }
    }

    private void init() {
        setContentView(R.layout.main);

        openService();
        initViewAndControl();
        initBroadcast();
    }

    private void initBroadcast() {
        mBroadCastReceiver = new DeviceConnectBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PublicConstant.BROADCAT_DEVICE);
        registerReceiver(mBroadCastReceiver, filter);
    }


    private class itemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (Utility.checkBLEServiceIsNull()) {
                return;
            }

            Utility.otherOperationNeedStopScan();
            // llLeft.setEnabled(true);

            checkOrUncheckBlub(position);
            ;

        }
    };

    private void checkOrUncheckBlub(int position) {
        mDevice = deviceList.get(position);
        if (!HomeBleService.ME.getProfileState(mDevice.getAddress())) {
            showProgressDialog("提示", "正在连接...");
            HomeBleService.ME.connect(mDevice.getAddress());
        } else {
            showProgressDialog("提示", "正在断开...");
            HomeBleService.ME.disConnect(mDevice.getAddress());
        }

    }

    private void initViewAndControl() {
        tvShowStatus = (TextView)findViewById(R.id.tv_show_status);

        deviceList = new ArrayList<BluetoothDevice>();

        mAdapter = new MyAdapter();

        mListView = (ListView) findViewById(R.id.lv_devices);
        mListView.setAdapter(mAdapter);


        mListView.setOnItemClickListener(new itemClickListener());


    }


    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                convertView = li.inflate(R.layout.listview_item, null);
            }

            BluetoothDevice device = deviceList.get(position);

            TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
            TextView tvStatus = (TextView) convertView.findViewById(R.id.tv_status);

            String address = TextUtils.isEmpty(device.getAddress()) ? "" : device.getAddress();
            String name = TextUtils.isEmpty(device.getName()) ? "" : device.getName();
            tvName.setText(address + "<->" + name);

            if (!Utility.checkBLEServiceIsNull()) {
                if (HomeBleService.ME.getProfileState(address)) {
                    tvStatus.setText("点击->断开");
                } else {
                    tvStatus.setText("点击->连接");
                }
            }

            return convertView;
        }
    }

    private void openService() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        Intent serviceIntent = new Intent();
        if (currentApiVersion < Build.VERSION_CODES.JELLY_BEAN_MR2) {// 小于android4.3以下
            LogAndToastUtil.toast(this, getString(R.string.can_not_support));
            finish();
        } else if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {// 大于android4.3
            serviceIntent.setClass(this, Api18Service.class);
        }

        startService(serviceIntent);
    }

    private class DeviceConnectBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int enumBroadCastStatus = intent.getIntExtra("enumBroadcastValue", 0);

            switch (enumBroadCastStatus) {
                case BLEDeviceStatus.AUTO_STOP_SCAN:
                    hideProgressDialog();
                    break;
                default:
                    break;
            }

            BluetoothDevice device = intent.getParcelableExtra("device");
            if (device == null) {
                return;
            } else {
                mDevice = device;
            }

            switch (enumBroadCastStatus) {
                case BLEDeviceStatus.SCAN_NEW_DEVICE:
                    if (deviceList.contains(device)) {
                        return;
                    } else {
                        deviceList.add(device);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;

                case BLEDeviceStatus.DISCONNECTED:
                    mAdapter.notifyDataSetChanged();
                    tvShowStatus.setText(String.format("状态日志:\r\n[%s<->%s]连接断开", mDevice.getAddress(), mDevice.getName()));
                    break;

                case BLEDeviceStatus.CONNECTED:
                    mAdapter.notifyDataSetChanged();
                    tvShowStatus.setText(String.format("状态日志:\r\n[%s<->%s]连接成功", mDevice.getAddress(), mDevice.getName()));
                    break;

                case BLEDeviceStatus.NOTIFY:
                    break;

                default:
                    break;
            }
            hideProgressDialog();
        }
    }

    /*
  * 提示加载
  */
    private void showProgressDialog(String title, String message) {
        if (progressDialog == null) {

            progressDialog = ProgressDialog.show(this, title,
                    message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }

        progressDialog.show();

    }

    /*
     * 隐藏提示加载
     */
    private void hideProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }
}
