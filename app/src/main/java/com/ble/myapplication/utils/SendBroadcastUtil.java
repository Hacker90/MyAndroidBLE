package com.ble.myapplication.utils;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.ble.myapplication.constants.PublicConstant;


/**
 * 执行广播发送的工具类
 * @author Administrator
 *
 */
public class SendBroadcastUtil {
	public static void sendMyBroadcast(Context cxt, int status){
		Intent intent = new Intent();
		intent.setAction(PublicConstant.BROADCAT_DEVICE);
		intent.putExtra("enumBroadcastValue", status);
		cxt.sendBroadcast(intent);
	}
	
	public static void sendMyBroadcast(Context cxt, BluetoothDevice device, int status){
		Intent intent = new Intent();
		intent.setAction(PublicConstant.BROADCAT_DEVICE);
		intent.putExtra("device", device);
		intent.putExtra("enumBroadcastValue", status);
		cxt.sendBroadcast(intent);
	}

}
