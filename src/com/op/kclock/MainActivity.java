/**
 *  Kitchen Clock
 *  Copyright (C) 2012 Alexander Pastukhov
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */ 
package com.op.kclock;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.op.kclock.alarm.AlarmService;
import com.op.kclock.alarm.AlarmServiceImpl;
import com.op.kclock.alarm.ClockService;
import com.op.kclock.alarm.WakeUpLock;
import com.op.kclock.cookconst.SettingsConst;
import com.op.kclock.dialogs.TimePickDialog;
import com.op.kclock.misc.Changelog;
import com.op.kclock.misc.Eula;
import com.op.kclock.misc.Log;
import com.op.kclock.model.AlarmClock;
import com.op.kclock.ui.TextViewWithMenu;
import com.op.kclock.utils.DbTool;

public class MainActivity extends Activity implements OnClickListener {

	private Handler handler;

	private TimePickDialog timePickDialog = null;
	public final static String TAG = "AlarmaClockActivity";
	private static NotificationManager mNotificationManager;
	private SharedPreferences mPrefs;

	private List<AlarmClock> alarmList = new ArrayList<AlarmClock>();

	TextView tvOut;
	private DbTool dbTool;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarmclock);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this
				.getApplicationContext());
		dbTool = new DbTool(getApplicationContext());

		//Eula.show(this);
		//Changelog.show(this);

		if (alarmList == null) {
			alarmList = new ArrayList<AlarmClock>();
		} else if (alarmList.size() == 0) {
			if (mPrefs.getBoolean(
					getApplicationContext().getString(
							R.string.pref_savesession_key), true)) {
				Log.d(TAG,"db read true!!");
				alarmList = dbTool.getAlarmsList();
			}
		}

		if (alarmList.size() > 0) {
			drawAlarms();
		} else {
			if (mPrefs.getBoolean(
					getApplicationContext()
							.getString(R.string.pref_addalarmonstart_key), true)) {
				addAlarmDialog();
			}
		}

		Log.d("oo", "start");

		WakeUpLock.acquire(this);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notification();

	}

	public void notification() {
		int icon = R.drawable.stat_notify_alarm;
		CharSequence mTickerText = getString(R.string.timer_started);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, mTickerText,
													 when);
		CharSequence mContentTitle = getString(R.string.app_name);
		CharSequence mContentText = getString(R.string.click_to_open);
		Intent clickIntent = new Intent(this, MainActivity.class);
		clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							 | Intent.FLAG_ACTIVITY_SINGLE_TOP
							 | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
																clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), mContentTitle,
                                        mContentText, contentIntent);
		notification.ledARGB = 0x00000000;
		notification.ledOnMS = 0;
		notification.ledOffMS = 0;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;

		mNotificationManager.notify( SettingsConst.APP_NOTIF_ID, notification);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
      		int width = getWindowManager().getDefaultDisplay().getWidth();
      		int height = getWindowManager().getDefaultDisplay().getHeight();
	  for(AlarmClock alarm:alarmList){
		 alarm.getWidget().setTextSize(width/8); 
	  }
	  if (TimePickDialog.isDialogShowed && timePickDialog != null) {
		  LinearLayout subscr = (LinearLayout) timePickDialog.findViewById(R.id.pick_text);
		  if (height < 500){
			  subscr.setVisibility(View.GONE);
		  } else {
			  subscr.setVisibility(View.VISIBLE);
		  }
	  }
	  stopAllActiveAlarms();
	}
	
	public stopAllActiveAlarms(){
           for(AlarmClock alarm:alarmList){
		 if (alarm.getState() == AlarmClock.TimerState.ALARMING) {
					alarm.alarmSTOP(getApplicationContext());
				}
	   }	
	}

	// Store the instance of an object
	@Override
	public Object onRetainNonConfigurationInstance() {

		Log.d(TAG, "retain");

		if (alarmList != null) // Check that the object exists
			return (alarmList);
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	protected void onStart() {
		super.onStart();
		tvOut = (TextView) findViewById(R.id.tvOut);
		Log.d(TAG, "MainActivity: onStart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		WakeUpLock.acquire(this);
		if(alarmList.size() == 0){
			if (mPrefs.getBoolean(
					getApplicationContext()
							.getString(R.string.pref_addalarmonstart_key), true)) {

				addAlarmDialog();
			}
		}
		Log.d(TAG, "MainActivity: onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		WakeUpLock.release();
		Log.d(TAG, "MainActivity: onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// save to db
		if (mPrefs.getBoolean(
				getApplicationContext()
						.getString(R.string.pref_savesession_key), true)) {
			dbTool.open();
			// select min alarm and make caller
			for (AlarmClock alarm : alarmList) {
				if (alarm.getId() == 0) {
					dbTool.insert(alarm);
				} else {
					dbTool.update(alarm);
				}
			}
			dbTool.close();
		}
		Log.d(TAG, "MainActivity: onStop()");
	}

	/**
	 * this method sets alarm manager to try wake up on given time
	 * 
	 * @param time
	 *            when to try wake up next time
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbTool.close();
		Log.d(TAG, "MainActivity: onDestroy()");
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d("oo", "onrestore");
		super.onRestoreInstanceState(savedInstanceState);
		// alarmList =
		// savedInstanceState.getParcelableArrayList("SAVE_SELECTED");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d("oo", "onsave");
		super.onSaveInstanceState(savedInstanceState);
		// savedInstanceState.putParcelableArrayList("SAVE_SELECTED",
		// alarmList);
	}

	private void drawAlarms() {
		for (AlarmClock alarm : alarmList) {
			drawAlarm(alarm);
		}
	}

	private void addAlarm(AlarmClock newAlarm) {
		if (newAlarm.getElement() == null) {
			drawAlarm(newAlarm);
			alarmList.add(newAlarm);
		}
	}

	private LinearLayout drawAlarm(AlarmClock alarm) {
		if (alarm.getElement() == null){ 
			LinearLayout mainL = (LinearLayout) findViewById(R.id.alarm_layout);
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			LinearLayout itemView = (LinearLayout) inflater.inflate(
					R.layout.alarm_incl, null);
			alarm.setElement(itemView);
			mainL.addView(alarm.getElement(), new TableLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		
		TextViewWithMenu textView = (TextViewWithMenu) (alarm.getWidget());
		// textView.setOutAnimation(out);
		textView.setAlarm(alarm);

		alarm.updateElement();
		alarm.getElement().setOnClickListener(this);

		AlarmService alarmService = new AlarmServiceImpl(this, handler);
		alarmService.setAlarmClock(alarm);
		if (alarm.getThread() == null || alarm.getState() == AlarmClock.TimerState.STOPPED){
			if (mPrefs.getBoolean(
					getApplicationContext().getString(
							R.string.pref_autostart_key), true)){
				if (alarm.getState() != AlarmClock.TimerState.PAUSED){
					alarm.setState(getApplicationContext(), AlarmClock.TimerState.RUNNING);
				}
				
			} else {
				alarm.setState(getApplicationContext(), AlarmClock.TimerState.PAUSED);				
			}
			alarm.setThread(new Thread(alarmService));
			alarm.getThread().start();
		}

		registerForContextMenu(alarm.getWidget());
		// add the itemView
		alarm.updateState(getApplicationContext());
		
		int width = getWindowManager().getDefaultDisplay().getWidth();
		alarm.getWidget().setTextSize(width/8);

		return alarm.getElement();
	}

	// ON-CLICK

	public void onClick(View v) {
		startService(new Intent(this, ClockService.class));

		for (AlarmClock alarm : alarmList) {
			TextViewWithMenu tvTimer = (TextViewWithMenu) alarm.getElement()
					.getChildAt(1);
			if (tvTimer == v) {
				if (alarm.getState() == AlarmClock.TimerState.RUNNING) {
					alarm.setState(getApplicationContext(), AlarmClock.TimerState.PAUSED);
				} else if (alarm.getState() == AlarmClock.TimerState.PAUSED) {
					alarm.setState(getApplicationContext(), AlarmClock.TimerState.RUNNING);
				} else if (alarm.getState() == AlarmClock.TimerState.ALARMING) {
					alarm.alarmSTOP(getApplicationContext());
				}
				break;
			}
		}
	}

	// ============================================================
	// ==================== MENUS ===============================
	// ============================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.alarm_clock_options, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		if (v.getId() == 2){
			
		}
		inflater.inflate(R.menu.alarm_context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings: {
			Intent i3 = new Intent(this, SettingsActivity.class);
			startActivity(i3);
			return true;

		}
		case R.id.menu_add: {
			addAlarmDialog();
			return true;
		}
		case R.id.menu_delete_all: {
			deleteAllAlarms();
			if (mPrefs.getBoolean(
					getApplicationContext()
							.getString(R.string.pref_addalarmonstart_key), true)) {
				addAlarmDialog();
			}
			return true;
		}
		case R.id.menu_exit: {
			//mNotificationManager.cancel(SettingsConst.APP_NOTIF_ID);
			mNotificationManager.cancelAll();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			//onStop();
			finish();
		}

		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		TextViewWithMenu.TextViewMenuInfo menuInfo = (TextViewWithMenu.TextViewMenuInfo) item
				.getMenuInfo();
		TextViewWithMenu text = (TextViewWithMenu) menuInfo.targetView;

		switch (item.getItemId()) {
		case R.id.settime: {
			setAlarmDialog(text.getAlarm());
			return true;
		}
		case R.id.remove: {
			deleteAlarm(text);
			return true;
		}
		}
		return super.onContextItemSelected(item);
	}

	// ============================================================
	// ==================== END MENUS ===============================
	// ============================================================
	private void deleteAllAlarms() {

		for (final AlarmClock alarm : alarmList) {
			if (alarm.getState().equals(AlarmClock.TimerState.ALARMING))
				alarm.alarmSTOP(getApplicationContext());
			alarm.setState(getApplicationContext(), AlarmClock.TimerState.STOPPED);
			alarm.getElement().setVisibility(View.GONE);
			if (alarm.getId() > 0) {
				dbTool.open();
				dbTool.delete(alarm.getId());
				dbTool.close();
			}
			alarm.setElement(null); // TODO clean!
		}
		alarmList.clear();
	}

	private void deleteAlarm(final TextViewWithMenu text) {
		for (final AlarmClock alarm : alarmList) {
			if (alarm.getElement().getChildAt(1) == (TextViewWithMenu) text) {
				// final AlarmClock falarm = alarm;
				Animation hyperspaceJump = AnimationUtils.loadAnimation(this,
						R.anim.hsjump);
				hyperspaceJump
						.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation arg0) {
								alarmList.remove(alarm);
								if (alarm.getState().equals(
										AlarmClock.TimerState.ALARMING))
									alarm.alarmSTOP(getApplicationContext());
								alarm.setState(getApplicationContext(), AlarmClock.TimerState.STOPPED);
							    alarm.getElement().setVisibility(View.GONE);
								if (alarm.getId() > 0) {
									dbTool.open();
									dbTool.delete(alarm.getId());
									dbTool.close();
								}
								alarm.setElement(null); // TODO clean!
								// mainL.removeView(((LinearLayout)alarm.getElement()

							}

							@Override
							public void onAnimationStart(Animation animation) {
							}

							public void onAnimationRepeat(Animation animation) {
							}

						});

				alarm.getElement().startAnimation(hyperspaceJump);

				break;
			}
		}
	}

	private void addAlarmDialog() {
		if (!TimePickDialog.isDialogShowed) setAlarmDialog(null);
	}

	private void setAlarmDialog(AlarmClock alarm) {
		timePickDialog = new TimePickDialog(MainActivity.this);
		timePickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		timePickDialog.setAlarm(alarm);

		timePickDialog.setDialogResult(new TimePickDialog.OnMyDialogResult() {
			public void finish(AlarmClock newAlarm) {
				addAlarm(newAlarm);
				newAlarm.updateElement();
				if (newAlarm.getState() == AlarmClock.TimerState.STOPPED) {
					drawAlarm(newAlarm);
					//newAlarm.setState(getApplicationContext(), AlarmClock.TimerState.RUNNING);
					//AlarmService alarmService = new AlarmServiceImpl(MainActivity.this, handler);
					//alarmService.setAlarmClock(newAlarm);
					//new Thread(alarmService).start();

				}
			}
		});
		timePickDialog.show();
	}

	/*	*//**
	 * Creates new MusicHandler and starts playing, Also creates
	 * ShowStopper.
	 */
	/*
	 * private void playMusic() { music = new MusicHandler();
	 * music.setMusic(this); music.play(true);
	 * 
	 * ShowStopper stopper = new
	 * ShowStopper(PreferenceService.getAlarmLength(this), music, vibrator);
	 * showStopperThread = new Thread(stopper); showStopperThread.start();
	 * 
	 * }
	 */

	/**
	 * Finishes the activity, also closes the various things started by
	 * onCreate.
	 */
	@Override
	public void finish() {
		Log.v(TAG, "finish");
	}

}
