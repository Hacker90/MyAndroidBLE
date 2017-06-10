package com.ble.myapplication.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class LogAndToastUtil {
	private static final boolean IS_DEBUG = true;

	/**
	 * 打印日志
	 */
	public static void log(String s, Object... args) {
		if (args != null && args.length > 0) {
			s = String.format(s, args);
		}

		if (IS_DEBUG) {
			Log.i("TAG", s);
		}
	}

	/**
	 * 弹出提示 使用静态成员变量保证toast不延时
	 */
	public static Toast toast;
	public static Context toastContext;

	public static void toast(Context c, String s, Object... args) {
		toastShowLengthTime(c, s, Toast.LENGTH_SHORT, args);
	}



	public static void toastShowLengthTime(Context c, String s, int duration, Object... args) {
		s = String.format(s, args);
		if (!c.equals(toastContext)) {
			toastContext = c;
			toast = Toast.makeText(c, "", duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
		}

		toast.setText(s);
		toast.show();
	}

	public static void clearToast() {
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
		if (toastContext != null)
			toastContext = null;

	}
}
