package com.op.kclock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.op.kclock.model.AlarmClock;

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

public class PresetsActivity extends Activity implements OnClickListener {

	/** Called when the activity is first created. */
	private List<AlarmClock> histories  = null;
	private List<AlarmClock> presets = null;
	private Map<View, AlarmClock> historyMap = new HashMap<View, AlarmClock>();
	private Map<View, AlarmClock> presetsMap = new HashMap<View, AlarmClock>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Bundle savedInstanceState = null;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presets);
		LinearLayout presetsList = (LinearLayout)findViewById(R.id.presets_list);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		DBHelper dbHelper = new DBHelper(getApplicationContext());
		dbHelper.open();
		
		//Show presets list
		presets = dbHelper.getPresetsList();
		for(final AlarmClock alarm : presets){
			//TextView preset = new TextView(getApplicationContext());
			//String name = (alarm.getName() == null || alarm.getName().length() == 0)?
			//"alarm-":alarm.getName(); 	
			//preset.setText(name + "-" + alarm.toString());
			
			String name = (alarm.getName() == null || alarm.getName().length() == 0)?
					"alarm":alarm.getName(); 	
			final View convertView = inflater.inflate(R.layout.hist_unit, null);
            		final TextView tvName = (TextView) convertView.findViewById(R.id.name);
            		tvName.setText(name);
            		final TextView tvTime = (TextView) convertView.findViewById(R.id.time);
            		tvTime.setText(alarm.toString());
			convertView.setOnClickListener(this);
			presetsList.addView(convertView,0);
			presetsMap.put(convertView,alarm);
			
			// On long click on preset - show edit dialog
			convertView.setOnLongClickListener(new View.OnLongClickListener() { 
		        @Override
		        public boolean onLongClick(View v) {
				TimePickDialog timePickDialog = null;	
				timePickDialog = new TimePickDialog(PresetsActivity.this);
				timePickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				timePickDialog.setAlarm(alarm);
		
				timePickDialog.setDialogResult(new TimePickDialog.OnMyDialogResult() {
					public void finish(AlarmClock newAlarm) {
						tvName.setText(name);
            					tvTime.setText(alarm.toString());
					}
				});
				timePickDialog.show();
				return true;
		        }
    			});
    
		}
		
		// show logs list
		LinearLayout logsList = (LinearLayout)findViewById(R.id.logs_list);
		histories = dbHelper.getHistoryList();
		for(AlarmClock alarm : histories){
			String name = (alarm.getName() == null || alarm.getName().length() == 0)?
					"alarm":alarm.getName(); 	
			View convertView = inflater.inflate(R.layout.hist_unit, null);
            		TextView tvName = (TextView) convertView.findViewById(R.id.name);
            		tvName.setText(name);
            		TextView tvTime = (TextView) convertView.findViewById(R.id.time);
            		tvTime.setText(alarm.toString());
			convertView.setOnClickListener(this);
            		logsList.addView(convertView,0);
			historyMap.put(convertView,alarm);
		}
		
		Intent mainActivity = new Intent(this, MainActivity.class);
		mainActivity.putExtra("alarm_extra", "");
		dbHelper.close();
	}

	@Override
	public void onClick(View v) {
		AlarmClock alarm = (AlarmClock)historyMap.get(v);
		Intent mainActivity = new Intent(this, MainActivity.class);
	//	mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mainActivity.putExtra("alarm_extra", alarm);
		startActivity(mainActivity);

	}


}
