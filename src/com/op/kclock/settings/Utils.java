package com.op.kclock.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.op.kclock.R;
import com.op.kclock.misc.Log;


import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.webkit.WebView;


public class Utils {

	/**
	 * 
	 * @param totalSeconds
	 * @param timer
	 * @return
	 */
	public static String formatTime(long totalSeconds, int timer) {
		if (timer == 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			return sdf.format(new Date(totalSeconds * 1000));
		} else {

			String seconds = Integer.toString((int) (totalSeconds % 60));
			String minutes = Integer.toString((int) (totalSeconds / 60));
			if (seconds.length() < 2) {
				seconds = "0" + seconds;
			}
			if (minutes.length() < 2) {
				minutes = "0" + minutes;
			}
			return minutes + ":" + seconds;
		}
	}


	public static float dp2px(int dip, Context context){
		float scale = context.getResources().getDisplayMetrics().density;
		return dip * scale + 0.5f;
	}

	public static View dialogWebView(Context context, String fileName) {
		View view = View.inflate(context, R.layout.dialog_webview, null);
		WebView web = (WebView) view.findViewById(R.id.wv_dialog);
		web.loadUrl("file:///android_asset/"+fileName);
		return view;
	}

	public static CharSequence readTextFile(Context context, String fileName) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null) buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e) {
			Log.e("readTextFile", "Error readind file " + fileName, e);
			return "";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}


	public static boolean isSdPresent() {
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

}
