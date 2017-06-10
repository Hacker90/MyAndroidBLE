package com.ble.myapplication.utils;


import com.ble.myapplication.ble_service.HomeBleService;


public class Utility {
	public static boolean checkBLEServiceIsNull(){
		return HomeBleService.ME == null;
	}
	
	public static byte int2uint8(int num) {
		short v = (short) num;
		return (byte) (v & 0xFF);
	}
	
	public static int byte2Uint8(byte num) {
		return num & 0xFF;
	}




	public static void otherOperationNeedStopScan(){
		if (!checkBLEServiceIsNull()) {
			if(HomeBleService.ME.isScanning){
				HomeBleService.ME.scan(false);
			}
		}
	}

	
	/**
	 * 线程休眠
	 * @param ms 单位：毫秒
	 */
	public static void ThreadSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static String byteArray2HexString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(String.format(" 0x%02X", b));
		}

		return sb.toString();
	}
}
