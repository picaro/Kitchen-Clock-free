package com.op.kclock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.op.kclock.model.AlarmClock;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.op.kclock.utils.*;
import com.op.kclock.dialogs.TimePickDialog;
import android.view.*;
import com.op.kclock.utils.DBHelper;

public class PresetsActivity extends Activity implements OnClickListener {

	/** Called when the activity is first created. */
	private List<AlarmClock> histories = null;
	private List<AlarmClock> presets = null;
	private final Map<View, AlarmClock> historyMap = new HashMap<View, AlarmClock>();
	// private Map<View, AlarmClock> presetsMap = new HashMap<View,
	// AlarmClock>();
	private GestureDetector gestureDetector;
	private ActionBar actionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Bundle savedInstanceState = null;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presets);
		final LinearLayout presetsList = (LinearLayout) findViewById(R.id.presets_list);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		DBHelper dbHelper = new DBHelper(getApplicationContext());
		dbHelper.open();

		// Show presets list
		presets = dbHelper.getPresetsList();
		for (final AlarmClock alarm : presets) {

			String name = (alarm.getName() == null || alarm.getName().length() == 0) ? "alarm"
					: alarm.getName();
			final View convertView = inflater.inflate(R.layout.preset_unit,
					null);
			final TextView tvName = (TextView) convertView
					.findViewById(R.id.name);
			tvName.setText(name);
			final TextView tvTime = (TextView) convertView
					.findViewById(R.id.time);
			tvTime.setText(alarm.toString());
			convertView.setOnClickListener(this);
			final View tvDel = convertView.findViewById(R.id.delpreset);
			tvDel.setOnClickListener(new View.OnClickListener() {

				public void onClick(View p2) {
					View p1 = (View) p2.getParent();
					presetsList.removeView(p1);
					AlarmClock alarm = historyMap.get(p1);
					presets.remove(alarm);
					DBHelper dbHelper = new DBHelper(getApplicationContext());
					dbHelper.open();
					dbHelper.deletePreset(alarm.getId());
					dbHelper.close();
					historyMap.remove(p1);

				}

			});
			presetsList.addView(convertView, 0);
			historyMap.put(convertView, alarm);

			// On long click on preset - show edit dialog
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					TimePickDialog timePickDialog = null;
					timePickDialog = new TimePickDialog(PresetsActivity.this);
					timePickDialog
							.requestWindowFeature(Window.FEATURE_NO_TITLE);
					timePickDialog.setAlarm(alarm);

					timePickDialog
							.setDialogResult(new TimePickDialog.OnMyDialogResult() {
								public void finish(AlarmClock newAlarm) {
									tvName.setText(alarm.getName());
									tvTime.setText(alarm.toString());
									DBHelper dbHelper = new DBHelper(getApplicationContext());
									dbHelper.updatePreset(alarm);
								}
							});
					timePickDialog.show();
					return true;
				}
			});

		}

		// show logs list
		LinearLayout logsList = (LinearLayout) findViewById(R.id.logs_list);
		histories = dbHelper.getHistoryList();
		for (AlarmClock alarm : histories) {
			String name = (alarm.getName() == null || alarm.getName().length() == 0) ? "alarm"
					: alarm.getName();
			View convertView = inflater.inflate(R.layout.hist_unit, null);
			TextView tvName = (TextView) convertView.findViewById(R.id.name);
			tvName.setText(name);
			TextView tvTime = (TextView) convertView.findViewById(R.id.time);
			tvTime.setText(alarm.toString());
			convertView.setOnClickListener(this);
			logsList.addView(convertView, 0);
			historyMap.put(convertView, alarm);
		}

		Intent mainActivity = new Intent(this, MainActivity.class);
		mainActivity.putExtra("alarm_extra", "");
		dbHelper.close();

		gestureDetector = new GestureDetector(new MyGestureDetector());
		View logsScroll = findViewById(R.id.llViewLog);
		View presetsScroll = findViewById(R.id.llViewPreset);

		// Set the touch listener for the main view to be our custom gesture
		// listener
		logsScroll.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		});
		presetsScroll.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		});
		initActionBar();
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar
			.setTitle(getApplicationContext().getString(R.string.app_name));
	}
	
	// ============================================================
	// ==================== MENUS ===============================
	// ============================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.presets_options, menu);
		return (super.onCreateOptionsMenu(menu));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings: {
					goSettings();
					return true;

				}
			case R.id.menu_add: {
					//addAlarmDialog();
					return true;
				}
			case R.id.menu_delete_all: {
					//deleteAllAlarms(true);
					return true;
				}
			case R.id.menu_exit: {
					//mNotificationManager.cancelAll();
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				}

		}
		return false;
	}

	private Intent goSettings() {
		Intent i3 = new Intent(this, SettingsActivity.class);
		startActivity(i3);
		return i3;
	}
	//=======
	
	@Override
	public void onClick(View v) {
		AlarmClock alarm = (AlarmClock) historyMap.get(v);
		alarm.setUsageCnt(alarm.getUsageCnt() + 1);
		Intent mainActivity = new Intent(this, MainActivity.class);
		// mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mainActivity.putExtra("alarm_extra", alarm);
		startActivity(mainActivity);

	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			if (Math.abs(e1.getY() - e2.getY()) > MainActivity.SWIPE_MAX_OFF_PATH) {
				return false;
			}

			// left to right swipe
			if (e2.getX() - e1.getX() > MainActivity.SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > MainActivity.SWIPE_THRESHOLD_VELOCITY) {
				PresetsActivity.this.finish();
				PresetsActivity.this.overridePendingTransition(
						R.anim.slide_in_left, R.anim.slide_out_right);
			}

			return false;
		}

		// It is necessary to return true from onDown for the onFling event to
		// register
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

	}

	
	
}
