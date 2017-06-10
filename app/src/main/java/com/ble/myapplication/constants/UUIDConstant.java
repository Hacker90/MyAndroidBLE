package com.ble.myapplication.constants;

import java.util.UUID;

public class UUIDConstant {
	// 服务的UUID
	public static final UUID SERVICE_UUID = UUID.fromString("0000ffb0-0000-1000-8000-00805f9b34fb");

	// 数据传输 特征的UUID
	public static final UUID CAN_WRITE_UUID = UUID.fromString("0000ffb2-0000-1000-8000-00805f9b34fb");

	// 用于配置 描述的UUID
	public static final UUID CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


	public static final UUID CONFIG_CHARACTERISTIC_UUID = UUID.fromString("0000ffb4-0000-1000-8000-00805f9b34fb");
}
