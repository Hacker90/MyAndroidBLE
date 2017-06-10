package com.ble.myapplication.constants;

public class PublicConstant {
	public static final int REQUEST_ENABLE_BT = 0x14062301;

	/** 扫描设备用时 */
	public static final int SCAN_BLE_TIME = 8 * 1000;

	public static final String APP_NAME = "BLEAndroid";

	public static final String BROADCAT_DEVICE = String.format("%s.android.intent.action.broadcat.device", APP_NAME);
}
